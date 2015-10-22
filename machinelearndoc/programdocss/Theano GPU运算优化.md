Theano GPU运算优化 

[toc]
#####一、在GPU上执行程序
######概述
theano设计的一个目标是指定计算执行在一个抽象的等级上，这样内部的函数编译将会获得很大的灵活性来执行这些计算。一个使用这个优势的方法是在图上执行这些计算。

现在在gpu上执行的方法有两个方法， 一个是只支持NVIDIA cards（CUDA backend）,另一个是正在发展， 它既支持Opencl设备，又支持NVIDIA设备。

######CUDA backend
testing Theano with GPU
使用以下的程序判断你的GPU
```python
from theano import function, config, shared, sandbox
import theano.tensor as T
import numpy
import time

vlen = 10 * 30 * 768  # 10 x #cores x # threads per core
iters = 1000

rng = numpy.random.RandomState(22)
x = shared(numpy.asarray(rng.rand(vlen), config.floatX))
f = function([], T.exp(x))
print f.maker.fgraph.toposort()
t0 = time.time()
for i in xrange(iters):
    r = f()
t1 = time.time()
print 'Looping %d times took' % iters, t1 - t0, 'seconds'
print 'Result is', r
if numpy.any([isinstance(x.op, T.Elemwise) for x in f.maker.fgraph.toposort()]):
    print 'Used the cpu'
else:
    print 'Used the gpu'
```
上面的程序仅仅是计算 一批 随机数 exp（）,注意到我们使用共享变量function 来保证x被保存在gpu设备上。
如果指定程序运行在gpu设备上，（运行参数：  device=cpu）,作者的电脑执行用了3秒，但是使用GPU设备 ，用了0.64 秒， 这个GPU 将会总是产生与cpu相同的浮点数，作为对照 （benchmark）使用numpy 的函数 numpy.exp(x.get_value())用了46 秒。
```python
$ THEANO_FLAGS=mode=FAST_RUN,device=cpu,floatX=float32 python check1.py
[Elemwise{exp,no_inplace}(<TensorType(float32, vector)>)]
Looping 1000 times took 3.06635117531 seconds
Result is [ 1.23178029  1.61879337  1.52278066 ...,  2.20771813  2.29967761
  1.62323284]
Used the cpu

$ THEANO_FLAGS=mode=FAST_RUN,device=gpu,floatX=float32 python check1.py
Using gpu device 0: GeForce GTX 580
[GpuElemwise{exp,no_inplace}(<CudaNdarrayType(float32, vector)>), HostFromGpu(GpuElemwise{exp,no_inplace}.0)]
Looping 1000 times took 0.638810873032 seconds
Result is [ 1.23178029  1.61879349  1.52278066 ...,  2.20771813  2.29967761
  1.62323296]
Used the gpu
```
需要注意的是 theano 的 GPU 操作 需要floatx 是float32

######返回一个Device-Allocated Data 的handle
上面例子中的提速不明显是因为上面的函数返回的值是以Numpy ndarray 来执行的，为了用户的方便， 返回值已经从设备中拷贝到主机中了。这就是为什么可以比较容易的使用 "device = gpu"切换设备 ,但是如果你不介意一些处理步骤， 你可以通过改变图对计算的表达产生 GPU-stored result 来大大的提速，参数 “gpu_from_host”操作，意味着，“将数据从主机上复制到GPU”，它在T.exp(x) 被置换为GPU版的exp（）来进行优化。
```python
from theano import function, config, shared, sandbox
import theano.tensor as T
import numpy
import time

vlen = 10 * 30 * 768  # 10 x #cores x # threads per core
iters = 1000

rng = numpy.random.RandomState(22)
x = shared(numpy.asarray(rng.rand(vlen), config.floatX))
f = function([], sandbox.cuda.basic_ops.gpu_from_host(T.exp(x)))
print f.maker.fgraph.toposort()
t0 = time.time()
for i in xrange(iters):
    r = f()
t1 = time.time()
print 'Looping %d times took' % iters, t1 - t0, 'seconds'
print 'Result is', r
print 'Numpy result is', numpy.asarray(r)
if numpy.any([isinstance(x.op, T.Elemwise) for x in f.maker.fgraph.toposort()]):
    print 'Used the cpu'
else:
    print 'Used the gpu'
```
上面程序的输出是
```shell
$ THEANO_FLAGS=mode=FAST_RUN,device=gpu,floatX=float32 python check2.py
Using gpu device 0: GeForce GTX 580
[GpuElemwise{exp,no_inplace}(<CudaNdarrayType(float32, vector)>)]
Looping 1000 times took 0.34898686409 seconds
Result is <CudaNdarray object at 0x6a7a5f0>
Numpy result is [ 1.23178029  1.61879349  1.52278066 ...,  2.20771813  2.29967761
  1.62323296]
Used the gpu
```
这样我们仅仅通过不将结果传回主机减少了50%的 运行时间，函数返回的数据的类型不再是 numpy array 而是 "CudaNdarray" ，可以同通常的 Numpy casting（转型）机制来转换为 numpy array的数据 numpy.asarray()

为了 更进一步的提升 速度 你可以使用 borrow flag ， 可以参考下面borrow的介绍。

###### 什么可以在GPU上进行加速
程序的执行效果可以进一步进行提升， 方法从设备到设备而不同，但是可以提供下面的粗略思路：
* 只计算 float32 的数据类型可以提升计算速度， 仅仅跟换float64 的设备 还是提升的不明显
* 当参数足够大时，可以保持30个处理单元（processors）运行在busy的状态，矩阵相乘， 卷积运算，和一些大型的 element-wise 的操作将会提升的很快（5-50x）
* 索引操作， 纬度shuffle， constant-time reshape 在cpu 和gpu 上计算的速度是相同的。
* 沿 一个tensor的行或者列进行summation运算在GPU上会比在cpu上慢 。
* 从设备上拷贝或从设备上输出数据将会很慢， 将会削弱一个或多个加速函数的效果。

在GPU上提升运行效果的tips
* 考虑在你的.theanorc文件中添加 floatx = float32 ,如果你打算在GPU上执行很多的工作。
* 使用 theano的flag  allow_gc = False  参考 GPU Async capability
* 相对于 dmatrix, dvector, dscalar 优先使用 matrix  Vector  scalar 参数，因因为在floatX = float32的时候， 它们的元素都是float32的。
* 确保你的输出变量是 float32而不是float64的， 越多的float32 的变量 在你的计算图中， GPU可以为你作更多的工作。
* 通过使用共享的float32 类型的变量来保存频繁访问的变量（参考shared）来减少 gpu设备之间的传输， 当使用GPU 的时候， float32 tensor shared 变量将会被存储在GPU中，这样隐式的在使用GPU ops的时候 减少了项GPU的传输
* 当你对程序的执行表现不满意的时候， 你可以在building 你的 function 时 使用 “mode = ProfileModel” 参数， 这会在程序执行完毕后打印函数执行时间的信息， 是否执行的高效，是否一个op 或者apply 执行的时间大于它的限额， 如果你会gpu编程， 看看它在theano.sandbox.cuda中是如何执行的， 确保在cpu op 消耗的时间 Xs(x%)  gpu Xs(x%),和transfer op 使用的时间 是否similar 。它可以告诉你在图中 是gpu 中的计算 耗时还是 有过多的memory transfer。
* Use nvcc options. nvcc supports those options to speed up some computations: -ftz=true to flush denormals values to zeros., –prec-div=false and –prec-sqrt=false options to speed up division and square root operation by being less precise. You can enable all of them with the nvcc.flags=–use_fast_math Theano flag or you can enable them individually as in this example: nvcc.flags=-ftz=true –prec-div=false.

######GPU Async capablilities（gpu 异步存储）
从theano 0.6 开始， 我们使用GPUs的异步存储(asynchronous capability),这允许我们运行的更快，但是也带来了一个可能的问题： 一个错误会在它真实发生的时间后被抛出（raise），这将在描绘 theano apply node的时候带来困难， NVIDIA的一个特征可以用来处理上面的问题。 如果你设置 环境变量 CUDA_LAUNCH-BLOCKING = 1,所有的kernel call 将会自动变成同步的，这将会降低运行效果，但是很好的进行图的 描述(profile) 并且正确的防止错误信息。

这个特性同 theano的gc中间结果时交互作用（影响）。为了更好的利用这个特征，你需要 使 gc 失效， 因为gc会在图中设置同步点， 通过设置“allow_gc = False”来加速函数的执行， 但是这样会增加内存的使用。

######改变共享变量的值 （changing the value of shared variables）
为了改变共享变量的值， 例如提供新的数据来处理， 使用 shared_variable.set_value(new_value). 阅读第二部分了解更多的内容：

Exercise 
考虑逻辑回归函数
```python
import numpy 
import theano
import theano.tensor as T

rng = numpy.random

N = 400
feats = 784
D=(rng.randn(N,feats).astype(theano.config.floatX),
  rng.randint(size=N,low=0,high = 2).astype(theano.config.floatX))
training_steps = 10000

#Declare Theano symbolic variables
x = T.matrix("x")
y = T.vector("y")
#将模型参数设置为 共享变量
w = theano.shared(rng.randn(feats).astype(theano.config.floatX),name="w")

b = theano.shared(numpy.asarray(0.,dtype=theano.config.floatX),name="b")
x.tag.test_value = D[0]
y.tag.test_value = D[1]

#print "Initial model:"
#print w.get_value(),b.get_value()

#Construct Theano expression graph
p_1 = 1 / (1 + T.exp(-T.dot(x,w) - b))  # 计算一个向量的 概率
prediction = p_1 > 0.5   #   ,根据阈值返回预测值
xent = -y * T.log(p_1) - (1-y) * T.log(1-p_1)  # 交叉熵
cost = xent.mean() + 0.01*(w**2).sum()   # cost函数
gw,gb = T.grad(cost,[w,b])

# 将函数编译成theano 的函数
train = theano.function(
	inputs=[x,y],
    outputs = [prediction,xent],
    updates = {w:w - 0.01*gw,b:b - 0.01*gb},
    name='train')
predict = theano.function(input=[x], outputs = prediction,
    name="predict")
    
if any([x.op.__class__.__name__ in ['Gemv', 'CGemv','Gemm','CGemm'] for x in train.maker.fgraph.toposort()]):
	print 'Used the cpu'
elif and([x.op.__class__.__name__ in ['GpuGemm','GpuGemv'] for x in
train.maker.fgraph.toposort()]):
	print "Used the gpu"
else:
	print "ERROR ,not able to tell if theano use the cpu or the GPU"
    print train.maker.fgraph.toposort()
    
for i in range(training_steps):
	pred,err = train(D[0],D[1])
    
#print "Final model:"
#print w.get_value(),b.get_value()

print "target values for D"
print D[1]

print "prediction on D"
print predict(D[0])
```
从cpu到gpu有提升么
提升是从什么地方来的(use profileMode)
提升点
```shell
1. 共享变量使用 float32 的dtype ，这样默认被迁移到 gpu的memory space中
2. there is a limit of one GPU per process
3. 使用theano标识 device=gpu 来要求使用GPU device
4. 使用 device = gpu{0,1,...} 来指定 gpu ，如果你有多于一个的gpu
5. 使用 theano的flag  floatX = float32 (通过 theano.config.floatX) in your code
6. 将输入转型，在他们输入到共享变量之前。
7. 避免thenao将int32自动转型为 float64：
    * 在你的程序中手动的设置转型  [u]int{8,16}
    * 在你的程序中手动的对mean操作进行转型（这里包含除以一个int64 的长度变量）
    * 注意新的转型机制的发展
```

###### GpuArray Backend
 如果上面的几点你都做过了， 那么你需要 安装 libgpuarray库作为计算工具， 

###### 返回一个handle来处理 设备相关（Device-Allocated data）的 数据
 对于默认的在GPU上运行的函数仍然返回 标准的 numpy 的 ndarray,在结果返回之前一个转换函数（transfer function）被插入进来，来确保同GPU上运行的结果一致（类型），这允许更换一些代码运行的设备， 只需要改变 flag ： device 的值 而不需要直接修改代码。
 
 如果你不介意牺牲一些弹性（loss of flexibility），你可以要求theano 直接返回 GPU object， 例如下面的程序：
 
 ```python
 from theano import function,config,shared,tensor,sandbox
 import numpy
 import time
 
 vlen = 10*30*768  #
 iters = 1000
 
 rng = numpy.random.RandomState(22)
 x = shared(numpy.asarray(rng.rand(vlen),config.floatX))
 f = function([],sandbox.gpuarray.basic_ops.gpu_from_host(tensor.exp(x)))
 print f.maker.fgraph.toposort()
 t0 = time.time()
 for i in xrange(iters):
 	r = f()
 t1 = time.time()
 print 'Looping %d times took' % iters,t1-t0,' seconds'
 print 'Result is ',numpy.asarray(r)
 if numpy.any([isinstance(x.op.tensor.Elemwise) and 
   ('Gpu' not in type(x.op).__name__) for x in f.maker.fgraph.toposort()]):
   print 'Used the cpu'
  else:
   print 'Used the gpu'
 ```
 
在这里 theano.sandbox.gpuarray.basic.gpu_from_host()  的意思是 '将input复制到GPU'，但是在优化阶段，reuslt 已经在gpu中了， 它将会被转移， 这里用来告诉theano 我们希望result保存在GPU中。
输出结果：
```shell
$ THEANO_FLAGS=device=cuda0 python check2.py
Using device cuda0: GeForce GTX 275
[GpuElemwise{exp,no_inplace}(<GpuArray<float64>>)]
Looping 1000 times took 0.455810785294 seconds
Result is [ 1.23178032  1.61879341  1.52278065 ...,  2.20771815  2.29967753
  1.62323285]
Used the gpu
```
每次循环调用使用的时间明显要比前两次的少，主要的提速是因为gpu执行的异步特性， 意思是这个work并没有完成仅仅是 "launched"，我们等会再讲。
为了进一步提速，你可以使用 borrow 标识

###### GPU 异步 存储 (GPU Async Capabilities)
默认的 ，所有GPU  的函数运行的是异步的（asynchronously）, 这意味着， 他们只是被计划执行和返回的， 这是被底层的libgpuarray透明化了。

一个强制的同步点被引入， 是当设备和主机之间的memory transfer， 另一个同步点是当我们释放GPU上的活动的内存buffer （active buffer 是那些仍然被kernel使用的buffer）

可以对指定的GpuArray进行强制同步 ，通过调用它的 sync() 方法，这在作基准测试的时候获得准确的时间时较为重要。

强制同步点同中间结果的gc之间会互相有影响（interact）， 为了获得更快的速度，你可以取消gc的功能（使用 theano的标识 ： allow_gc = False），但是要注意内存使用的控制。





#####二、共享变量 使用 borrow参数

######概述
在theano中重用内存是一种提升程序速度的方法， 了解theano是如何绑定（alias）缓存对于程序的加速和正确运行十分重要。
下面将详述theano控制内存的一些规则， 并且解释什么时候你可以通过改变（alter）一些function和 method的默认的行为可以提升程序的运行速度。

######Memory Model ： 双空间
直到theano控制内存有许多规则， 最主要的思想是 theano管理着一个内存池，theano跟踪池内变量的改变。
1. theano空间特征
 * theano管理它自己的内存空间，这个空间与普通的不含有theano代码的python程序的内存空间不重合。
 * theano 的函数（function）只改变态自己内存空间中的buffer的内容
 * theano的内存空间包含 关联共享变量 和 evaluate function 的临时变量 的buffer
 * 在物理上， theano的memory space 的范围 可以延伸到整个主机（host）， 一个或多个GPU device 上，未来有可能包含远程机器。
 * 分配到一个共享变量的theano内存空间是固定，且唯一的： 它不会再被分配给其他的共享变量。
 * theano的内存在theano function 不运行或者theano的library 代码不运行的时候是固定的。
 * theano函数的默认行为是 向用户空间输出函数结果， 并且期望从用户空间获取输入。

2. theano双空间控制调优
theano持有的内存（Theano-managed memory ）和用户持有的内存（user-managed memory） 的间的区别（distinction），可以被某些函数打破（如 shared ，get_value,和in与out 的构造函数）。只要这些函数将属性参数 borrow 设置为True。 这种设置可以使这些函数的执行速度大大提升，因为避免了复制操作， 但是也会给整个程序带来一些不可预想的错误（ 通过绑定空间 by aliasing memory）

######在创建共享变量是使用borrow
borrow参数可以在 共享变量的构造函数中使用
```python
import numpy,theano
np_array = numpy.ones(2,dtype='float32')

s_default = theano.shared(np_array)
s_false = theano.shared(np_array,borrow=False)
s_true  = theano.shared(np_array,borrow=True)
```

默认和明确设置 borrow 变量为False的情况， 我们创建的共享变量将会得到np_array的一个deep copy，np_array变量的改变，对共享变量不会有影响。
```python
np_array += 1  # 现在是一个 持有2.0的数组   对数组进行了改变

s_default.get_value()  # -> array([1.0,1.0])  #默认的没有改变
s_false.get_value()    # -> array([1.0,1.0])  #borrow设置为false的没有改变
s_true.get_value()     # -> array([2.0,2.0])  #borrow设置为true的改变了（说明了他们共享同一空间）
```
当我们使用gpu作为运行设备时，当我们对np_array进行改变时，改变会在s_true.get_value()中显现， 因为Numpy arrays是可变的（mutable），并且s_truct 使用 np_array对象作为了外部缓存就像使用它的内部缓存（internal buffer）。

但是，这种 numpy array 和 s_true 之间的连接并不保证一定会发生， 即使这种关联最终发生了，他也有可能只是临时连接。不能确保连接发生的原因是 因为， 如果theano 正在使用着的是GPU 设备，这时 borrow 标识将不会有作用。只会临时起作用的原因是 ： 如果我们调用theano的function来更新共享变量这种关联关系可能会发生也可能不会发生（当函数被允许通过改变共享变量的buffer来update共享变量，这时将会保存它俄连接， 当通过改变共享变量指向的buffer时，这种连接将会被断开）。

最佳实践：
  1. 当构建一个代表了一个巨大的对象的共享变量 ，并且我们也不想在内存中create它的复本，我们将borrow设置为true是可以的
  2. 对通过 side-effect 进行改变的的共享变量 设置borrow 是不可信的，因为一些设备（GPU devices）将不会执行这个设置。
  
###### 在对共享变量的值进行存取时设置 borrow
* 提取
  borrow 参数可以控制 对共享变量值的存取
  ```python
  s = theano.shared(np_array)
  
  v_false = s.get_value(borrow=False)     # borrow属性默认设置为false
  v_true  = s.get_value(borrow=True)
  ```
  当get_value 的borrow参数设置为false的时候，意味着对返回的值将不会分配theano 的内部存储空间（就是直接输出到内存中了）。当borrow 为 true的时候，意味着返回值将会被分配到theano的内部存储空间，但是两种方法都有可能创建内部存储空间的副本。
  
  borrow设置为True 时仍然产生副本的原因是， 共享变量的内存表示也许同你期望的不同。当你使用一个numpy array 创建一个 共享变量时，使用get_value（）必须返回numpy，这就是theano使用时 GPU 是透明的原因， 但是当你使用GPU时（以后也许是远程机器），numpy.ndarray 将可能不是在internalmemory中存储的类型， 如果你想theano直接返回gpu中的数据而不复制它，你可以使用 参数 return_internal_type=True在调用get_value()时，他将不会对internal中的数据进行转型，（并且总是立即返回（constant time））
，但是将会依赖上下文环境因素返回各种数据类型的数据（环境如： the compute device,numpy array de dtype）
```python
v_internal = s.get_value(borrow=True,return_internal_type=True)
```
也可以在borrow=False的情况下联合使用 return_internal_type=True,这时将会返回internal object 的 deep copy ，但是这只是debug时使用的，并不是一般情况下的设置。
为了透明的使用theano可以做的各种优化，一个原则是 get_value() 默认总是返回该共享变量构建是传入数据的类型， 所以如果你手动的在gpu中创建一个数据变量，并将其封装为共享变量，get_value函数 返回的依然是gpudata ，即使你将return_interal_type=false
设置上。
**最佳实践：**
 * 当你的程序不改变return value的时候，给get_value()设置为true是可行的。
  * 不要使用这个通过side_effect来改变 一个 “shared” 变量，这将会使得你的代码是设备相关的。
  * 通过 side-effect 来改变 GPU变量是不可行的
 
* Assigning
  共享变量还有一个 set_value 方法 ，它可以接受一个 borrow=True 参数。borrow=True  意味着Theano将会重用你为变量的内部存储空间提供的buffer。
  一个标准的手动更新共享变量的方法如下：
  ```python
  s.set_value(
  some_inplace_fn(s.get_value(borrow=True)),borrow=Ture
  )
  ```
  这种形式的运行与计算设备无关，并且之后会通过一个副本来使它可以暴露给theano的内部变量，它处理的速度同in-place一样。
  
  当共享变量绑定到GPU，与gpu存储空间之间交换数据将会花费很大，下面是几个确保快速和高效使用GPU内存和宽带的tip：
  * 0.3.1版本之前，set_value 不是在GPU中进行in_place 计算的， 这意味着gpu将会在释放旧存储空间之前释放新的存储空间， 如果程序使用的内存接近gpu的最大限制，你的程序将会失败
  解决方法： 更新theano的版本
  * 如果你频繁的交换共享数据的几个chunk，如果可行，你将会希望重用第一次分配的内存， 这回即迅速有存储高效。
  解决方法： 更新到最新版本的thenao， 并且保证你的数据的每个chunk 具有相同的size
  * 当前的GPU仅支持复制连续空间的存储内容，

######在构建函数对象时 使用 borrowing(in,out variabling)
borrow参数也可以提供给 In 和 Out 对象 ，来控制它如何处理它的输入变量和输出变量
```python
x = theano.tensor.matrix()
y = 2 * x
f = theano.function(theano.In(x,borrow=True),theano.Out(y,borrow=True))
```
构建function对象时， 这意味着 theano将会将你提供的这个变量临时作为theano 池内的一部分。结果是，你的input将会计算其他变量时作为一个buffer被重用（会被重写），
borrowing  一个output 变量 意味着 theano将会意味着theano将不会坚持每次调用该函数的时候分配一个新的buffer，它将会重用之前调用时创建的变量，并且重写它的空间。结果是它将会通过side-effect重写 返回变量的存储空间。  这些返回值，将会在计算其他编译的function对象时被重写， （例如， 输出变量被设置为共享变量）。 所以在调用其他的函数时， 要慎用borrow参数，默认的不使用borrow的internal参数。

It is also possible to pass a return_internal_type=True flag to the Out variable which has the same interpretation as the return_internal_type flag to the shared variable’s get_value function. Unlike get_value(), the combination of return_internal_type=True and borrow=True arguments to Out() are not guaranteed to avoid copying an output value. They are just hints that give more flexibility to the compilation and optimization of the graph.

对于GPU图， borrowing 可以有主要的速度影响， 参考下面的代码：
```python
from theano import function ,config,shared,sandbox,tensor,Out
import numpy
import time

vlen = 10 * 30 * 768 # 10 x #cores x # threads per core
iters = 1000

rng = numpy.random.RandomState(22)
x = shared(numpy.asarray(rng.rand(vlen),config.floatX))
f1 = function([],sandbox.cuda.basic_ops.gpu_from_host(tensor.exp(x)))
f2 = function([],
      Out(sandbox.cuda.basic_ops.gpu_from_host(tensor.exp(x)),
      borrow=True))
t0 = time.time()

for i in xrange(iters):
	r = f1()
t1 = time.time()
no_borrow = t1 - t0
t0 = time.time()
for i in xrange(iters):
	r = f2()
t1 = time.time()
print 'Looping',iters,'time took',no_borrow,'second without borrow',
print 'and' , t1 - t0,'seconds with borrow.'
if numpy.any([isinstance(x.op,tensor.Elemwise) and 
        ('Gpu' not in type(x.op).__name__)  for x in f1.maker.fgraph.toposort()]):
    print 'Used the cpu'
 else
 	print 'Used the gpu'
```

它产生的输出是：
```shell
$THEANO_FLAGS=device=gpu0,floatX=float32 python test.py
Using gpu device 0: GeForce GTX 275
Looping 1000 times took 0.368273973465 seconds without borrow and 0.0240728855133 seconds with borrow.
Used the gpu
```
* 最佳实践
当输入 一个输入变量x 在函数计算完成后将不被需要，你将将其设置为 addition workspace，
这时，你将考虑将其设置为 In(x,borrow=True),它将会将函数运行的更快，并且减少它的内存使用，
当一个返回值y很大的时候，（在内存占用），并且你只需要读取它一次，仅仅是在它返回的时候，这时你需要考虑 为其设置 Out(y,borrow=True). 




##### scan函数

[传送](http://www.cnblogs.com/huashiyiqike/p/3553325.html)


```python
theano.scan(fn,sequences=None,
       outputs_info,
       non_sequences=None,
       n_steps=None,
       truncate_gradient=-1,
       go_backwords=False,
       mode=None,
       name=None,
       profile=False)
       
:outputs_info is the list of Theano variables or dictionaries describing the initial state of the outputs computed recurrently. 

 **output_info 是一个描述 scan函数循环时 的初始变量的的字典**
 

```

fn是每一步所用的函数，sequences是输入，outputs_info是scan输出在起始的状
态。

sequences and outputs_info are all parameters of fn in ordered sequence（
sequences 和 outputs_info 都是 fn执行的 **顺序** 序列）.


一个例子：

```python
scan(fn,
	sequences = [dict(input=Sequence1,taps=[-3,2,-1])
    ,sequence2
    ,dict(input = Sequence3,taps=3)]
    ,
    outputs_info = [
    dict(initial=Output1,taps=[-3,-5])
    ,dict(initial=Output2,taps=None),
    Output3]
    ,
    non_sequences=[Argument1,Argument2])
    
    
    
```
**下面是期望的一次传入函数的参数列表的顺序**：

```shell
    Sequence1[t-3]
    Sequence1[t+2]
    Sequence1[t-1]
    Sequence2[t]
    Sequence3[t+3]
    Output1[t-3]
    Output1[t-5]
    Output3[t-1]
    Argument1
    Argument2
```

另一个例子：

```python
import theano
import theano.tensor as T

mode = theano.Mode(linker='cvm')
import numpy as np


def fun(a,b):
	return a+b
    
input = T.vector("input")

# output_info 给定的初始参数为 0
output,update = theano.scan(fn,sequences=input,outputs_info=[T.as_tensor_variable(np.asarray(1,inputdtype))])

# 将从scan中获取的函数 编译成theano的function

out = theano.function(inputs=[input,],outputs=output)


in1 = numpy.array([1,2,3])

print out(in1)


```

再来一个计算矩阵的例子：

```python
import theano
import theano.tensor as T

mode = theano.Mode(linker='cvm')
import numpy as np

def fun(a,b):
	return a+b
    
input=T.matrix("input")

output,update = theano.scan(fun,
		sequences=input,
        outputs_info=[
        T.as_tensor_vaiable(np.asarray([0,0,0],input.dtype))
        ])
        
out = theano.function(inputs=[input,],output=output)

in1 = numpy.array([1,2,3],[4,5,6])

print(in1)

print out(in1)


```



##### 共享变量（shared variable）

shared variable 相当于全局变量 ， 
	可以使用  .get_value() 获取 ， .set_value() 进行修改，
    在function中使用update() 来修改可以并行。
    
scan的输出是一个symbol，用来在后面的theano function里作为output和update的规则。当sequences=None时，n_steps应有一个值来限制对后面theano function里的input的循环次数。当sequences不为空时，theano function直接对sequences循环：


```python
components, updates = theano.scan(
fn=lambda coefficient, power, free_variable: coefficient *  (free_variable ** power),

                                  outputs_info=None,
                                  sequences=[coefficients, theano.tensor.arange(max_coefficients_supported)],
                                  non_sequences=x)
```
这个例子中，

theano.tensor.arange(max_coefficients_supported)类似于enumerate的index，coefficientes相当与enumerate里到序列值。这里根据顺序，x为free_variable.


##### Debug

http://deeplearning.net/software/theano/tutorial/debug_faq.html

theano.config.compute_test_value = 'warn'

    off: Default behavior. This debugging mechanism is inactive.
    raise: Compute test values on the fly. Any variable for which a test value is required, but not provided by the user, is treated as an error. An exception is raised accordingly.
    warn: Idem, but a warning is issued instead of an Exception.
    ignore: Silently ignore the computation of intermediate test values, if a variable is missing a test value.


```python

def inspect_inputs(i,node,fn):
	print i,node,"input(s) value(s):",[input[0] for input in fn.inputs],
    
def inspect_outputs(i,node,fn):
	print "output(s) value(s):", [output[0] for output in fn.outputs]


X = theano.tensor.dscalar('x')

f = theano.function([x],[5*x],
  				mode=theano.compile.MonitorMode(
                        pre_func=inspect_inputs,
                        post_func=inspect_outputs)
				)
                
f(3)


```

mode = 'DEBUG_MODE' 很慢，无效？

使用print

```python
x = theano.tensor.devctor('x')

x_printed = theano.printing.Print('this is a very important value')(x)

f = theano.function([x], x * 5)
f_with_print = theano.function([x], x_printed * 5)

#this runs the graph without any printing
assert numpy.all( f([1, 2, 3]) == [5, 10, 15])

#this runs the graph with the message, and value printed
assert numpy.all( f_with_print([1, 2, 3]) == [5, 10, 15])

```

##### theano 笔记
[传送](http://blog.csdn.net/u012428391/article/category/2509039)

###### 标量相加

```python
import theano.tensor as T
from theano import function

x = T.dscalar("x")
y = T.dscalar("y")

z = x + y

f = function([x,y],z)
```
输入定义两个符号变量来代替数值，**输出是一个0维的numpy.ndarray数组**。

##### 矩阵相加

把输入类型换一下就行了，矩阵如果维数不同，会遵循NumPy的广播规则。
```python
    import theano.tensor as T  
    from theano import function  
    x = T.dmatrix('x')  
    y = T.dmatrix('y')  
    z = x + y  
    f = function([x, y], z)  

```
定义一个公式如：a ** 2 + b ** 2 + 2 * a* b

这里每个变量都需要单独申明。

```python
    import theano  
    a = theano.tensor.vector()  
    b = theano.tensor.vector()  
    out = a ** 2 + b ** 2 + 2 * a * b  
    f = theano.function([a,b],out)  
    print f([0, 1],[1,2])  
    >>>   
    [ 1. 9.]  
```

###### 支持多输出

```python
    import theano.tensor as T  
    from theano import function  
    a, b = T.dmatrices('a', 'b')  
    diff = a - b  
    abs_diff = abs(diff)  
    diff_squared = diff**2  
    f = function([a, b], [diff, abs_diff,diff_squared])  
    print f([[1, 1], [1, 1]], [[0, 1], [2,3]])  
    >>>   
    [array([[ 1.,  0.],  
          [-1., -2.]]), array([[ 1.,  0.],  
          [ 1.,  2.]]), array([[ 1.,  0.],  
          [ 1.,  4.]])]  
```

###### 设置默认参数

和标准Python一样，缺省参数必须在非缺省之后，也可以定义缺省变量名。

```python
    import theano.tensor as T  
    from theano import function  
    from theano import Param  
    x, y = T.dscalars('x', 'y')  
    z = x + y  
    f = function([x, Param(y, default=1,name='by_name')],z)  
    print f(33)  
    print f(33, 2)  
    print f(33,by_name=3)  
    >>>   
    34.0  
    35.0  
    36.0  
```

###### 共享变量

为了在GPU上更好的性能，引入共享变量，以累加器为例。

```python
    import theano.tensor as T  
    from theano import function  
    from theano import shared  
    state = shared(0)  
    inc = T.iscalar('inc')  
    accumulator = function([inc], state,updates=[(state, state+inc)])  
    print state.get_value()  
    accumulator(1)  
    print state.get_value()  
    accumulator(300)  
    print state.get_value()  
    state.set_value(-1)  
    print accumulator(3)  
    print state.get_value()  
    >>>   
    0  
    1  
    301  
    -1  
    2  
```
state的值在调用函数之后才刷新。而且可以定义多个函数共用同一个共享变量，例如这个减法器。

```python

    decrementor = function([inc], state,updates=[(state, state-inc)])  
    print decrementor(2)  
    print state.get_value()  
    >>>   
    2  
    0  
```

###### given 参数
如果在某个函数中，共用了这个共享变量，但是又不想变动它的值，那么可以使用given参数替代这个变量。而旧的state不发生变化。

```python
    fn_of_state = state * 2 + inc  
    foo = T.scalar(dtype=state.dtype)  
    skip_shared = function([inc, foo],fn_of_state,  
                               givens=[(state,foo)])  
    print skip_shared(1, 3)  
    print state.get_value()  
    >>>   
    7  
    0  
```

###### 产生随机数

和C中的srand()一样，都是伪随机数。

```python

    from theano import function  
    from theano.tensor.shared_randomstreamsimport RandomStreams  
    srng = RandomStreams(seed=234)#种子  
    rv_u = srng.uniform((2,2))#均匀分布  
    rv_n = srng.normal((2,2))#正态分布  
    f = function([], rv_u)#每次调用，每次都会更新  
    g = function([], rv_n,no_default_updates=True)#如果以后一直用这组随机数，就不再更新  
    nearly_zeros = function([], rv_u + rv_u- 2 * rv_u)  
    print nearly_zeros()#函数每次执行只获得一个随机数，即使表达式里面有3个随机数  
```

###### 种子流：
上述2个随机变量，可以全局设定同一个种子，也可以是分别设定。

```python
    #分别设置，使用.rng.set_value()函数  
    rng_val =rv_u.rng.get_value(borrow=True) # Get the rng for rv_u  
    rng_val.seed(89234) # seeds thegenerator  
    rv_u.rng.set_value(rng_val,borrow=True)  
    #全局设置，使用.seed()函数  
    srng.seed(902340)  
```

###### 函数间共享流

```python
    state_after_v0 =rv_u.rng.get_value().get_state()#保存调用前的state  
    nearly_zeros()       # this affects rv_u's generator  
    v1 = f()#第一个调用，之后state会变化  
    rng = rv_u.rng.get_value(borrow=True)  
    rng.set_state(state_after_v0)#为其state还原  
    rv_u.rng.set_value(rng, borrow=True)  
    v2 = f()             # v2 != v1输出更新后state对应的随机数  
    v3 = f()             # v3 == v1再次更新又还原成原来的state了  
```

###### 在2张Theano图间复制状态

```python
     import theano  
    import numpy  
    import theano.tensor as T  
    from theano.sandbox.rng_mrg importMRG_RandomStreams  
    from theano.tensor.shared_randomstreamsimport RandomStreams  
       
    class Graph():  
       def __init__(self, seed=123):  
           self.rng = RandomStreams(seed)  
           self.y = self.rng.uniform(size=(1,))  
       
    g1 = Graph(seed=123)  
    f1 = theano.function([], g1.y)  
       
    g2 = Graph(seed=987)  
    f2 = theano.function([], g2.y)  
       
    print 'By default, the two functionsare out of sync.'  
    print 'f1() returns ', f1()  
    print 'f2() returns ', f2()  
    #输出不同的随机值  
    def copy_random_state(g1, g2):  
       if isinstance(g1.rng, MRG_RandomStreams):  
    #类型判断：其第一个参数为对象，第二个为类型名或类型名的一个列表。其返回值为布尔型。  
           g2.rng.rstate = g1.rng.rstate  
       for (su1, su2) in zip(g1.rng.state_updates, g2.rng.state_updates):#打包  
           su2[0].set_value(su1[0].get_value())#赋值  
       
    print 'We now copy the state of thetheano random number generators.'  
    copy_random_state(g1, g2)  
    print 'f1() returns ', f1()  
    print 'f2() returns ', f2()  
    #输出相同的随机值  
    >>>   
    By default, the two functions are outof sync.  
    f1() returns  [ 0.72803009]  
    f2() returns  [ 0.55056769]  
    We now copy the state of the theanorandom number generators.  
    f1() returns  [ 0.59044123]  
    f2() returns  [ 0.59044123]  
```


##### theano 逻辑回归函数解析 

###### 首先是生成随机数对象

```python
importnumpy  
importtheano  
importtheano.tensor as T  
rng= numpy.random 
```

###### 数据初始化

有400张照片，这些照片不是人的就是狗的。

每张照片是28*28=784的维度。

D[0]是训练集，是个400*784的矩阵，每一行都是一张照片。

D[1]是每张照片对应的标签，用来记录这张照片是人还是狗。

training_steps是迭代上限。

```python
    N= 400  
    feats= 784  
    D= (rng.randn(N, feats), rng.randint(size=N, low=0, high=2))  
    training_steps= 10000  
```

```python
    #Declare Theano symbolic variables  
    x= T.matrix("x")  
    y= T.vector("y")  
    w= theano.shared(rng.randn(feats), name="w")  
    b= theano.shared(0., name="b")  
    print"Initial model:"  
    printw.get_value(), b.get_value()  
```

x是输入的训练集，是个矩阵，把D[0]赋值给它。

y是标签，是个列向量，400个样本所以有400维。把D[1]赋给它。

w是权重列向量，维数为图像的尺寸784维。

b是偏倚项向量，初始值都是0，这里没写成向量是因为之后要广播形式。


```python

    #Construct Theano expression graph  
    p_1= 1 / (1 + T.exp(-T.dot(x, w) - b))   #Probability that target = 1  
    prediction= p_1 > 0.5                    # Theprediction thresholded  
    xent= -y * T.log(p_1) - (1-y) * T.log(1-p_1) # Cross-entropy loss function  
    cost= xent.mean() + 0.01 * (w ** 2).sum()# The cost to minimize  
    gw,gb = T.grad(cost, [w, b])             #Compute the gradient of the cost  
                                              # (we shall return to this in a  
                                              #following section of this tutorial)  
```

###### 构造预测和训练函数。

```python
    #Train  
    fori in range(training_steps):  
        pred,err = train(D[0], D[1])  
    print"Final model:"  
    printw.get_value(), b.get_value()  
    print"target values for D:", D[1]  
    print"prediction on D:", predict(D[0])  

```

##### 图结构

图结构（Graph Structures）是了解Theano内在工作原理的基础。

Theano编程的核心是用符号占位符把数学关系表示出来。

 

图结构的组成部分

如图实现了这段代码：

```python
    importtheano.tensor as T  
    x= T.matrix('x')  
    y= T.matrix('y')  
    z= x + y  
```

![](http://img.blog.csdn.net/20140829101128316?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveWNoZW5nX3NqdHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

变量节点（variable nodes）

红色表示。变量节点都有owner，其中x与y的owner为none。z的owner为apply。

 

操作节点（op nodes）

绿色表示。表示各个变量之间的运算（例如+, -, **, sum(),tanh()等等）。

 

应用节点（apply nodes）

蓝色表示。其他节点都连在上面。


###### 分析nodes对应属性

对于以下代码，分析其节点属性。

```python

importtheano.tensor as T  
x= T.dmatrix('x')  
y= x * 2.  
>>>y.owner.op.name  
'Elemwise{mul,no_inplace}'#y的owner是apply而apply的op是'Elemwise{mul,no_inplace}'  
>>>len(y.owner.inputs)  
2#两个输入  
>>>y.owner.inputs[0]  
x#第一个输入是x矩阵  
>>>y.owner.inputs[1]  
InplaceDimShuffle{x,x}.0 
```

注意这里第二个输入并不是2，而是和x同样大小的矩阵框架，因为等会要广播才能相乘

```python
    >>>type(y.owner.inputs[1])  
    <class'theano.tensor.basic.TensorVariable'>  
    >>>type(y.owner.inputs[1].owner)  
    <class'theano.gof.graph.Apply'>  
    >>>y.owner.inputs[1].owner.op  
    <class'theano.tensor.elemwise.DimShuffle object at 0x14675f0'>#用DimShuffle把2广播出来  
    >>>y.owner.inputs[1].owner.inputs  
    [2.0]#矩阵框架的owner才是2  
```


###### 自动优化

编译Theano其实是编译了一张图。这张图从输入变量开始贯穿全图直到输出变量。Theano可以检测关键子图，来进行替换，防止重复，以达到优化的目的。比如用x替换xy/y。

举个例子

```python
>>>import theano  
>>>a = theano.tensor.vector("a")     # declare symbolic variable  
>>>b = a + a ** 10                    #build symbolic expression  
>>>f = theano.function([a], b)        #compile function  
>>>print f([0, 1, 2])      
```

![](http://img.blog.csdn.net/20140829101152637?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveWNoZW5nX3NqdHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


![](http://img.blog.csdn.net/20140829101213884?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveWNoZW5nX3NqdHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


###### theano 导数

导数使用T.grad计算。

这里使用pp()打印梯度的符号表达式。

第3行输出是打印了经过优化器简化的符号梯度表达式，与第1个输出相比确实简单多了。

fill((x** TensorConstant{2}), TensorConstant{1.0})指创建一个x**2大小的矩阵，并填充1。

```python
    importtheano.tensor as T  
    fromtheano import pp  
    fromtheano import function  
    x= T.dscalar('x')  
    y= x ** 2  
    gy= T.grad(y, x)  
    printpp(gy)  
    f= function([x], gy)  
    printf(4)  
    printpp(f.maker.fgraph.outputs[0])  
    >>>   
    ((fill((x** TensorConstant{2}), TensorConstant{1.0}) * TensorConstant{2}) * (x **(TensorConstant{2} - TensorConstant{1})))  
    8.0  
    (TensorConstant{2.0}* x)  

```

T.grad的第1个参数必须是标量

例如计算逻辑函数sigmoid的导数：

![](http://latex.codecogs.com/gif.latex?\frac{{ds\left(%20x%20\right)}}{{dx}}%20=%20s\left(%20x%20\right)%20\cdot%20\left(%20{1%20-%20s\left(%20x%20\right)}%20\right))

```python
importtheano.tensor as T  
fromtheano import function  
x= T.dmatrix('x')  
s= T.sum(1 / (1 + T.exp(-x)))  
gs= T.grad(s, x)  
dlogistic= function([x], gs)  
printdlogistic([[0, 1], [-1, -2]])  
>>>   
[[0.25        0.19661193]  
 [ 0.19661193 0.10499359]] 
```

######计算雅克比（Jacobian）矩阵

雅克比矩阵是向量的一阶偏导数：

![](http://latex.codecogs.com/gif.latex?J%20=%20\left[%20{\begin{array}{*{20}{c}}{\frac{{\partial%20{u_1}}}{{\partial%20{x_1}}}}&{\frac{{\partial%20{u_1}}}{{\partial%20{x_2}}}}&%20\cdots%20&{\frac{{\partial%20{u_1}}}{{\partial%20{x_n}}}}\\{\frac{{\partial%20{u_2}}}{{\partial%20{x_1}}}}&{\frac{{\partial%20{u_2}}}{{\partial%20{x_2}}}}&%20\cdots%20&{\frac{{\partial%20{u_2}}}{{\partial%20{x_n}}}}\\%20\vdots%20&%20\vdots%20&%20\ddots%20&%20\vdots%20\\{\frac{{\partial%20{u_n}}}{{\partial%20{x_1}}}}&{\frac{{\partial%20{u_n}}}{{\partial%20{x_2}}}}&%20\cdots%20&{\frac{{\partial%20{u_n}}}{{\partial%20{x_n}}}}\end{array}}%20\right])

用T.arrange生成从0到y.shape[0]的序列。循环计算。

scan可以提高创建符号循环效率。

lambda~是python内建的magicfunction.

```python

x = T.dvector("x")
y = x ** 2

J,updates = theano.scan(lambda i,y,x:T.grad(y[i],x) ,sequences=T.arange(y.shape[0]),non_sequences=[y,x])

f = function([x],J,updates=updates)

f([4,4])

>>>
[[ 8.  0.]  
 [ 0. 8.]]
```

###### 计算 海森矩阵(Hessian) 

海森矩阵是多元函数的二阶偏导数方阵。

![](http://latex.codecogs.com/gif.latex?H\left(%20f%20\right)%20=%20\left[%20{\begin{array}{*{20}{c}}{\frac{{{\partial%20^2}f}}{{\partial%20x_1^2}}}&{\frac{{{\partial%20^2}f}}{{\partial%20{x_1}\partial%20{x_2}}}}&%20\cdots%20&{\frac{{{\partial%20^2}f}}{{\partial%20{x_1}\partial%20{x_n}}}}\\{\frac{{{\partial%20^2}f}}{{\partial%20{x_2}\partial%20{x_1}}}}&{\frac{{{\partial%20^2}f}}{{\partial%20x_2^2}}}&%20\cdots%20&{\frac{{{\partial%20^2}f}}{{\partial%20{x_2}\partial%20{x_n}}}}\\%20\vdots%20&%20\vdots%20&%20\ddots%20&%20\vdots%20\\{\frac{{{\partial%20^2}f}}{{\partial%20{x_n}\partial%20{x_1}}}}&{\frac{{{\partial%20^2}f}}{{\partial%20{x_n}\partial%20{x_2}}}}&%20\cdots%20&{\frac{{{\partial%20^2}f}}{{\partial%20x_n^2}}}\end{array}}%20\right])


只要用T.grad(cost,x)替换雅克比矩阵的一些y即可。

```python
x = T.dvector('x')
y = x ** 2
cost = y.sum()
gy = T.grad(cost,x)

H,updates = theano.scan(lambda i,gy,x:T.grad(gy[i],x),sequences=T.arange(gy.shape[0]),non_sequences=[gy,x])

f = function([x],H,updates = updates)

f([4,4])

>>>
[[2.  0.]  
 [ 0. 2.]]  

```

###### 雅克比右乘

x可以由向量扩展成矩阵。雅克比右乘使用Rop:

```python
    W = T.dmatrix('W')  
    V =T.dmatrix('V')  
    x =T.dvector('x')  
    y =T.dot(x, W)  
    JV =T.Rop(y, W, V)  
    f =theano.function([W, V, x], JV)  
    printf([[1, 1], [1, 1]], [[2, 2], [2, 2]], [0,1])  
    >>>   
    [2.  2.]  
```

###### 雅克比左乘

雅克比左乘使用Lop:

![](http://latex.codecogs.com/gif.latex?\frac{{\partial%20f\left(%20x%20\right)}}{{\partial%20x}}v)

```python
    import theano  
    import theano.tensor as T  
    from theano import function  
    x = T.dvector('x')  
    v =T.dvector('v')  
    x =T.dvector('x')  
    y =T.dot(x, W)  
    VJ =T.Lop(y, W, v)  
    f =theano.function([v,x], VJ)  
    print f([2, 2], [0, 1])  
    >>>   
    [[0.  0.]  
     [ 2. 2.]]  
```

###### 海森矩阵乘以向量

可以使用Rop

![](http://latex.codecogs.com/gif.latex?v\frac{{\partial%20f\left(%20x%20\right)}}{{\partial%20x}})

```python
    import theano  
    import theano.tensor as T  
    from theano import function  
    x= T.dvector('x')  
    v= T.dvector('v')  
    y= T.sum(x ** 2)  
    gy= T.grad(y, x)  
    Hv= T.Rop(gy, x, v)  
    f= theano.function([x, v], Hv)  
    printf([4, 4], [2, 2])  
    >>>   
    [4.  4.]  
```

##### theano 配置设置与编译模型

###### 配置

config模块包含了各种用于修改Theano的属性。在Theano导入时，许多属性都会被检查，而有些属性是只读模式。

一般约定，在用户代码内部config模块的属性不应当被修改。

Theano的这些属性都有默认值，但是你也可以在你的.theanorc文件里面修改，并且使用THEANO_FLAGS的环境变量进行修改。

优先顺序是：

1. theano.config.<property>的赋值

2. THEANO_FLAGS的赋值

3..theanorc（或者在THEANORC文件中表示）的赋值

通过打印theano.config可以展示当前的配置：

```python
python-c 'import theano; print theano.config' | less
```


例如，修改笔记（二）中的逻辑回归函数，设置精度为float32

 
```python
#!/usr/bin/envpython  
#Theano tutorial  
#Solution to Exercise in section 'Configuration Settings and Compiling Modes

importnumpy  
importtheano  
importtheano.tensor as tt  
   
theano.config.floatX= 'float32'  
   
rng= numpy.random  
   
N= 400  
feats= 784  
D= (rng.randn(N, feats).astype(theano.config.floatX),  
rng.randint(size=N,low=0, high=2).astype(theano.config.floatX))  
training_steps= 10000  
   
#Declare Theano symbolic variables  
x= tt.matrix("x")  
y= tt.vector("y")  
w= theano.shared(rng.randn(feats).astype(theano.config.floatX),name="w")  
b= theano.shared(numpy.asarray(0., dtype=theano.config.floatX),name="b")  
x.tag.test_value= D[0]  
y.tag.test_value= D[1]  
#print"Initial model:"  
#printw.get_value(), b.get_value()  
   
#Construct Theano expression graph  
p_1= 1 / (1 + tt.exp(-tt.dot(x, w) - b))  #Probability of having a one  
prediction= p_1 > 0.5  # The prediction that isdone: 0 or 1  
xent= -y * tt.log(p_1) - (1 - y) * tt.log(1 - p_1) # Cross-entropy  
cost= tt.cast(xent.mean(), 'float32') + \  
       0.01 * (w ** 2).sum()  # The cost to optimize  
gw,gb = tt.grad(cost, [w, b])  
   
#Compile expressions to functions  
train= theano.function(  
            inputs=[x, y],  
            outputs=[prediction, xent],  
            updates={w: w - 0.01 * gw, b: b -0.01 * gb},  
            name="train")  
predict= theano.function(inputs=[x], outputs=prediction,  
            name="predict")  
   
ifany([x.op.__class__.__name__ in ['Gemv', 'CGemv', 'Gemm', 'CGemm'] for x in  
train.maker.fgraph.toposort()]):  
    print 'Used the cpu'  
elifany([x.op.__class__.__name__ in ['GpuGemm', 'GpuGemv'] for x in  
train.maker.fgraph.toposort()]):  
    print 'Used the gpu'  
else:  
    print 'ERROR, not able to tell if theanoused the cpu or the gpu'  
    print train.maker.fgraph.toposort()  
   
fori in range(training_steps):  
    pred, err = train(D[0], D[1])  
#print"Final model:"  
#printw.get_value(), b.get_value()  
   
print"target values for D"  
printD[1]  
   
print"prediction on D"  
printpredict(D[0])  


```

用time python file.py运行，可得：

[python] view plaincopy

    real  0m15.055s  
    user 0m11.527s  
    sys   0m0.801s  


Mode

每次调用theano.function时，Theano变量输入和输出的符号化关系都被优化和编译了。

而这些编辑都通过made参数的值来控制。

Theano定义以下mode：

 

FAST_COMPILE：

[python] view plaincopy

    compile.mode.Mode(linker='py',optimizer='fast_compile')  


只应用少量的图优化并且只使用Python实现。

 

FAST_RUN：

[python] view plaincopy

    compile.mode.Mode(linker='cvm',optimizer='fast_run')  


使用所有的优化并且在可能的情况下使用C实现。

 

DebugMode：

[python] view plaincopy

    compile.debugmode.DebugMode()  


检查所有优化的正确性，并且比较C与Python实现。这种模式比别的模式耗时都长，但是可以识别出各种问题。

 

ProfileMode（不赞成使用）：

[python] view plaincopy

    compile.profilemode.ProfileMode()  


与FAST_RUN相同的优化，但是打印出一些设置信息。

 

默认的模式是FAST_RUN，但是通过传递关键字参数给theano.function，可以控制config.mode，从而改变模式。

 

Linkers

一个mode由2个部分组成：1个优化器和1个Linker。


![](http://img.blog.csdn.net/20140902192842603?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveWNoZW5nX3NqdHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

[1]   gc指计算中间过程的碎片收集。否则在Theano函数调用之间，操作所使用的内存空间将被保存起来。为了不重新分配内存，降低开销（overhead），使其速度更快。

[2]   默认linker

[3]   不推荐使用

 

使用DebugMode

一般你应当使用FAST_RUN 或者FAST_COMPILE模式，当你定义新的类型的表达式或者优化方法时，先用DebugMode（mode='DebugMode'）运行是很有用的，DebugMode通过运行一些自检和判断程序来帮助诊断出将会导致错误输出的可能的编程错误。值得注意的是，DebugMode比FAST_RUN或者 FAST_COMPILE模式要慢得多，所以只在开发期使用。

 

举个例子：

```python
import theano  
importtheano.tensor as T  
x= T.dvector('x')  
f= theano.function([x], 10 * x, mode='DebugMode')  
f([5])  
f([0])  
f([7]) 
```

运行后，如果有问题，输出会提示异常，如果依然不能解决，请联系本领域的专家。

但是DebugMode也不是万能的，因为有些错误只在特定的输入条件下才会出现。

如果你使用构造器而不是关键词DebugMode，就可以通过配构造器变量来配置。而关键词设置太严格了。

 

ProfileMode不推荐使用

 

检索时间信息

图编译好之后，运行就可以了。然后调用profmode.print_summary()，返回各自时间信息，例如你的图大多数时间花在什么地方了等等。

 

还是以逻辑回归为例

生成ProfileMode实例

```python
    fromtheano import ProfileMode  
    profmode= theano.ProfileMode(optimizer='fast_run', linker=theano.gof.OpWiseCLinker())  
```

在函数末尾声明一下

```python

    train = theano.function(  
               inputs=[x,y],  
               outputs=[prediction,xent],  
               updates={w:w - 0.01 * gw, b: b - 0.01 * gb},  
               name="train",mode=profmode)  
    #如果是Module则这样声明：  
    # m = theano.Module()  
    # minst = m.make(mode=profmode)  
```    



取回时间信息

文件末尾添加

```python

    profmode.print_summary()  

```

则运行效果是这样的

```python
    ProfileMode.print_summary()  
    ---------------------------  
       
    Timesince import 6.183s  
    Theanocompile time: 0.000s (0.0% since import)  
        Optimization time: 0.000s  
        Linker time: 0.000s  
    Theanofct call 5.452s (88.2% since import)  
       Theano Op time 5.003s 80.9%(since import)91.8%(of fct call)  
       Theano function overhead in ProfileMode0.449s 7.3%(since import) 8.2%(of fct call)  
    10000Theano fct call, 0.001s per call  
    Restof the time since import 0.730s 11.8%  
       
    Theanofct summary:  
    <%total fct time> <total time> <time per call> <nb call><fct name>  
       100.0% 5.452s 5.45e-04s 10000 train  
       
    SingleOp-wise summary:  
    <%of local_time spent on this kind of Op> <cumulative %> <selfseconds> <cumulative seconds> <time per call> [*]<nb_call> <nb_op> <nb_apply> <Op name>  
       87.9%  87.9%  4.400s  4.400s 2.20e-04s * 20000  1  2 <class 'theano.tensor.blas_c.CGemv'>  
       10.8%  98.8%  0.542s  4.942s 5.42e-06s * 100000 10 10 <class 'theano.tensor.elemwise.Elemwise'>  
        0.5%  99.3%  0.023s  4.966s 1.17e-06s * 20000  1  2 <class 'theano.tensor.basic.Alloc'>  
        0.4%  99.6%  0.018s  4.984s 6.05e-07s * 30000  2  3 <class'theano.tensor.elemwise.DimShuffle'>  
        0.3%  99.9%  0.013s  4.997s 1.25e-06s * 10000  1  1 <class 'theano.tensor.elemwise.Sum'>  
        0.1% 100.0%  0.007s  5.003s 3.35e-07s * 20000  1  2 <class 'theano.compile.ops.Shape_i'>  
       ... (remaining 0 single Op account for0.00%(0.00s) of the runtime)  
    (*)Op is running a c implementation  
       
    Op-wisesummary:  
    <%of local_time spent on this kind of Op> <cumulative %> <selfseconds> <cumulative seconds> <time per call> [*]  <nb_call> <nb apply> <Opname>  
       87.9%  87.9%  4.400s  4.400s 2.20e-04s * 20000  2CGemv{inplace}  
        6.3%  94.3%  0.318s  4.718s 3.18e-05s * 10000  1Elemwise{Composite{[Composite{[Composite{[sub(mul(i0, i1), neg(i2))]}(i0,scalar_softplus(i1), mul(i2, i3))]}(i0, i1, i2, scalar_softplus(i3))]}}  
        2.1%  96.3%  0.103s  4.820s 1.03e-05s * 10000  1Elemwise{Composite{[Composite{[Composite{[Composite{[mul(i0, add(i1, i2))]}(i0,neg(i1), true_div(i2, i3))]}(i0, mul(i1, i2, i3), i4, i5)]}(i0, i1, i2,exp(i3), i4, i5)]}}[(0, 0)]  
        1.6%  98.0%  0.082s  4.902s 8.16e-06s * 10000  1Elemwise{ScalarSigmoid{output_types_preference=transfer_type{0}}}[(0, 0)]  
        0.5%  98.4%  0.023s  4.925s 1.17e-06s * 20000  2 Alloc  
        0.3%  98.7%  0.013s  4.938s 1.25e-06s * 10000  1 Sum  
        0.2%  98.9%  0.012s  4.950s 6.11e-07s * 20000  2InplaceDimShuffle{x}  
        0.2%  99.1%  0.008s  4.959s 8.44e-07s * 10000  1Elemwise{gt,no_inplace}  
        0.1%  99.2%  0.007s  4.965s 6.80e-07s * 10000  1Elemwise{sub,no_inplace}  
        0.1%  99.4%  0.007s  4.972s 3.35e-07s * 20000  2 Shape_i{0}  
        0.1%  99.5%  0.006s  4.978s 6.11e-07s * 10000  1Elemwise{Composite{[sub(neg(i0), i1)]}}[(0, 0)]  
        0.1%  99.6%  0.006s  4.984s 5.93e-07s * 10000  1InplaceDimShuffle{1,0}  
        0.1%  99.7%  0.005s  4.989s 5.33e-07s * 10000  1Elemwise{neg,no_inplace}  
        0.1%  99.8%  0.005s  4.994s 4.85e-07s * 10000  1Elemwise{Cast{float32}}  
        0.1%  99.9%  0.005s  4.999s 4.60e-07s * 10000  1Elemwise{inv,no_inplace}  
        0.1% 100.0%  0.004s  5.003s 4.25e-07s * 10000  1Elemwise{Composite{[sub(i0, mul(i1, i2))]}}[(0, 0)]  
       ... (remaining 0 Op account for   0.00%(0.00s) of the runtime)  
    (*)Op is running a c implementation  
       
    Apply-wisesummary:  
    <%of local_time spent at this position> <cumulative %%> <applytime> <cumulative seconds> <time per call> [*] <nb_call><Apply position> <Apply Op name>  
       54.7%  54.7%  2.737s  2.737s 2.74e-04s  * 10000  7 CGemv{inplace}(Alloc.0, TensorConstant{1.0}, x, w,TensorConstant{0.0})  
       33.2%  87.9%  1.663s  4.400s 1.66e-04s  * 10000 18 CGemv{inplace}(w, TensorConstant{-0.00999999977648}, x.T,Elemwise{Composite{[Composite{[Composite{[Composite{[mul(i0, add(i1, i2))]}(i0,neg(i1), true_div(i2, i3))]}(i0, mul(i1, i2, i3), i4, i5)]}(i0, i1, i2,exp(i3), i4, i5)]}}[(0, 0)].0, TensorConstant{0.999800026417})  
        6.3%  94.3%  0.318s  4.718s 3.18e-05s  * 10000 13 Elemwise{Composite{[Composite{[Composite{[sub(mul(i0, i1),neg(i2))]}(i0, scalar_softplus(i1), mul(i2, i3))]}(i0, i1, i2,scalar_softplus(i3))]}}(y, Elemwise{Composite{[sub(neg(i0), i1)]}}[(0, 0)].0,Elemwise{sub,no_inplace}.0, Elemwise{neg,no_inplace}.0)  
        2.1%  96.3%  0.103s  4.820s 1.03e-05s  * 10000 16 Elemwise{Composite{[Composite{[Composite{[Composite{[mul(i0, add(i1,i2))]}(i0, neg(i1), true_div(i2, i3))]}(i0, mul(i1, i2, i3), i4, i5)]}(i0, i1,i2, exp(i3), i4, i5)]}}[(0,0)](Elemwise{ScalarSigmoid{output_types_preference=transfer_type{0}}}[(0,0)].0, Alloc.0, y, Elemwise{Composite{[sub(neg(i0), i1)]}}[(0, 0)].0,Elemwise{sub,no_inplace}.0, Elemwise{Cast{float32}}.0)  
        1.6%  98.0%  0.082s  4.902s 8.16e-06s  * 10000 14 Elemwise{ScalarSigmoid{output_types_preference=transfer_type{0}}}[(0,0)](Elemwise{neg,no_inplace}.0)  
        0.3%  98.3%  0.015s  4.917s 1.53e-06s  * 10000 12 Alloc(Elemwise{inv,no_inplace}.0, Shape_i{0}.0)  
        0.3%  98.5%  0.013s  4.930s 1.25e-06s  * 10000 17 Sum(Elemwise{Composite{[Composite{[Composite{[Composite{[mul(i0,add(i1, i2))]}(i0, neg(i1), true_div(i2, i3))]}(i0, mul(i1, i2, i3), i4,i5)]}(i0, i1, i2, exp(i3), i4, i5)]}}[(0, 0)].0)  
        0.2%  98.7%  0.008s  4.938s 8.44e-07s  * 10000 15Elemwise{gt,no_inplace}(Elemwise{ScalarSigmoid{output_types_preference=transfer_type{0}}}[(0,0)].0, TensorConstant{(1,) of 0.5})  
        0.2%  98.9%  0.008s  4.946s 8.14e-07s  * 10000  5 Alloc(TensorConstant{0.0}, Shape_i{0}.0)  
        0.1%  99.0%  0.007s  4.953s 6.80e-07s  * 10000  4 Elemwise{sub,no_inplace}(TensorConstant{(1,) of 1.0}, y)  
        0.1%  99.1%  0.006s  4.959s 6.16e-07s  * 10000  6 InplaceDimShuffle{x}(Shape_i{0}.0)  
        0.1%  99.2%  0.006s  4.965s 6.11e-07s  * 10000  9 Elemwise{Composite{[sub(neg(i0), i1)]}}[(0, 0)](CGemv{inplace}.0,InplaceDimShuffle{x}.0)  
        0.1%  99.4%  0.006s  4.972s 6.07e-07s  * 10000  0 InplaceDimShuffle{x}(b)  
        0.1%  99.5%  0.006s  4.977s 5.93e-07s  * 10000  2 InplaceDimShuffle{1,0}(x)  
        0.1%  99.6%  0.005s  4.983s 5.33e-07s  * 10000 11 Elemwise{neg,no_inplace}(Elemwise{Composite{[sub(neg(i0), i1)]}}[(0,0)].0)  
       ... (remaining 5 Apply instances account for0.41%(0.02s) of the runtime)  
    (*)Op is running a c implementation  

```

##### theano  - 载入与保存


入与保存

Python标准的保存类别实体并重新载入它们的途径是pickle机制。许多Theano对象可以由此被序列化（或者反序列化），然而pickle的局限性在于，被序列化的类别实例的代码或者数据并没有被同时保存。因此重新载入先前版本的类可能会出问题。

因此，需要寻求基于预期保存和重新载入的耗时的不同机制。

对于短期（比如临时文件和网络转录），Theano的pickle是可行的。

对于长期（比如从实验中保存模型）不应当依赖于Theano的pickle对象。

推荐在任何其他Python项目的过程中的保存和载入底层共享对象。

 

Pickle基础

pickle和cPickle模块功能相似，但是cPickle用C编码，要更快一些。

可以用cPickle.dump把对象序列化（或者保存或者pickle）为一个文件。

```python
    importcPickle  
    f= file('obj.save', 'wb')  
    cPickle.dump(my_obj,f, protocol=cPickle.HIGHEST_PROTOCOL)  
    f.close()  

```
使用了cPickle.HIGHEST_PROTOCOL，使得保存对象的过程大大加快。

使用了’b’二进制模式，是为了在Unix和Windows系统之间保持可移植性。

 

使用cPickle.load把文件反序列化（或载入，或unpickle）

```python
    f= file('obj.save', 'rb')  
    loaded_obj= cPickle.load(f)  
    f.close()  
```

可以同时pickle多个对象到同一个文件：

```python
= file('objects.save', 'wb')  
forobj in [obj1, obj2, obj3]:  
    cPickle.dump(obj, f,protocol=cPickle.HIGHEST_PROTOCOL)  
f.close()  

```
也可以按照同样的顺序载入：


```python
    f= file('objects.save', 'rb')  
    loaded_objects= []  
    fori in range(3):  
        loaded_objects.append(cPickle.load(f))  
    f.close()  
```

短期序列化

如果有信心，pickle整个模型是个好办法。

这种情况是指，你在项目中执行同样的保存和重载操作，或者这个类已经稳定运行很久了。

通过定义__getstate__ method和__setstate__可以控制从项目中保存何种pickle。

如果模型类包含了正在使用数据集的链接，而又不想pickle每个模型实例，上述控制方法会很实用。

```python
    def__getstate__(self):  
        state = dict(self.__dict__)  
        del state['training_set']  
        return state  
       
    def__setstate__(self, d):  
        self.__dict__.update(d)  
    self.training_set =cPickle.load(file(self.training_set_file, 'rb'))  
```

长期序列化

如果想要保存的类运行不稳定，例如有函数创建或者删除、类成员重命名，应该只保存或载入类的不可变部分。

依然是使用定义__getstate__ method和__setstate__

例如只想要保存权重矩阵W和偏倚项b：

```python
    def__getstate__(self):  
        return (self.W, self.b)  
       
    def__setstate__(self, state):  
        W, b = state  
        self.W = W  
    self.b = b  

```

如果更新了下列函数来表现变量名称的改变，那么即使W和b被重命名为weights和bias，之前的pickle文件依然是可用的：

```python
    def__getstate__(self):  
        return (self.weights, self.bias)  
       
    def__setstate__(self, state):  
        W, b = state  
        self.weights = W  
    self.bias = b  

```

条件

-IfElse与Switch

-switch比ifelse更通用，因为switch是逐位操作。

-switch把2个输出变量都计算了，所以比ifelse要慢（只算1个）。

```python

import theano.tensor as T
from theano.ifelse import ifelse
import theano, time , numpy

a,b = T.scalar("a",'b')
x,y = T.matrices('x','y')


z_switch = T.switch(T.lt(a,b),T.mean(x),T.mean(y))
z_lazy = ifelse(T.lt(a,b),T.mean(x),T.mean(y))

f_switch = theano.function([a,b,x,y],z_switch,
		mode = theano.Model(linker = 'vm'))
f_lazyifelse = theano.function([a,b,x,y],z_lazy,
		mode = theano.Model(linker="vm"))
        

val1= 0.  
val2= 1.  
big_mat1= numpy.ones((10000, 1000))  
big_mat2= numpy.ones((10000, 1000))  
   
n_times= 10  
   
tic= time.clock()  
fori in xrange(n_times):  
    f_switch(val1, val2, big_mat1, big_mat2)  
print'time spent evaluating both values %f sec' % (time.clock() - tic)  
   
tic= time.clock()  
fori in xrange(n_times):  
    f_lazyifelse(val1, val2, big_mat1,big_mat2)  
print'time spent evaluating one value %f sec' % (time.clock() - tic)  
```

测试结果

```python
    time spent evaluating both values 0.200000 sec  
    time spent evaluating one value 0.110000 sec  
```


可见ifelse确实快了1倍，但是必须使用vm或者cvm作为Linker，而未来cvm会作为默认Linker出现。


##### theano scan 使用总结

###### 简单计算 A的k次方

1. python版
```python
result=1
for i in xrange(k):
	result = result * A
```

2. theano方式
 这里有三个要处理的事物： result 的初始值， result 的计算公式，不变量A
 
 使用 non_sequence 来设置不变量
 使用 outputs_info 来设置结果初始值
 计算过程 使用fn定义
 
```python
import thenao
import theano.tensor as T

k = T.iscalar("k")
A = T.vector("A")

# Symbolic description of the result
result, updates = theano.scan(fn=lambda prior_result, A: prior_result * A,
                              outputs_info=T.ones_like(A),
                              non_sequences=A,
                              n_steps=k)

# We only care about A**k, but scan has provided us with A**1 through A**k.
# Discard the values that we don't care about. Scan is smart enough to
# notice this and not waste memory saving them.
final_result = result[-1]

# compiled function that returns A**k
power = theano.function(inputs=[A,k], outputs=final_result, updates=updates)

print power(range(10),2)
print power(range(10),4)



```

注意这里results 是scan 各步返回的结果 在构建function的时候只取最后一个值，就可以了，
thenao会自动优化忽略前面的结果。


###### 2. 计算一个，theaono 对变量的第一个纬度进行循环

```python

import thenao
import theano.tensor as T

coefficients = theano.tensor.vector("coefficients")
x = T.scalar("x")

max_coefficients_supported = 10000

# Generate the components of the polynomial
components, updates = theano.scan(fn=lambda coefficient, power, free_variable: coefficient * (free_variable ** power),
                                  outputs_info=None,
                                  sequences=[coefficients, theano.tensor.arange(max_coefficients_supported)],
                                  non_sequences=x)
# Sum them up
polynomial = components.sum()

# Compile a function
calculate_polynomial = theano.function(inputs=[coefficients, x], outputs=polynomial)

# Test
test_coefficients = numpy.asarray([1, 0, 2], dtype=numpy.float32)
test_value = 3
print calculate_polynomial(test_coefficients, test_value)
print 1.0 * (3 ** 0) + 0.0 * (3 ** 1) + 2.0 * (3 ** 2)

```

注意函数传递的变量顺序：

```python
sequences (if any), prior result(s) (if needed), non-sequences (if any)
```


##### Simple accumulation into a scalar, ditching lambda

Although this example would seem almost self-explanatory, it stresses a pitfall to be careful of: the initial output state that is supplied, that is outputs_info, must be of a shape similar to that of the output variable generated at each iteration and moreover, it must not involve an implicit downcast of the latter.

```python
import numpy as np
import theano
import theano.tensor as T

up_to = T.iscalar("up_to")

# define a named function, rather than using lambda
def accumulate_by_adding(arange_val, sum_to_date):
    return sum_to_date + arange_val
seq = T.arange(up_to)

# An unauthorized implicit downcast from the dtype of 'seq', to that of
# 'T.as_tensor_variable(0)' which is of dtype 'int8' by default would occur
# if this instruction were to be used instead of the next one:
# outputs_info = T.as_tensor_variable(0)

outputs_info = T.as_tensor_variable(np.asarray(0, seq.dtype))
scan_result, scan_updates = theano.scan(fn=accumulate_by_adding,
                                        outputs_info=outputs_info,
                                        sequences=seq)
triangular_sequence = theano.function(inputs=[up_to], outputs=scan_result)

# test
some_num = 15
print triangular_sequence(some_num)
print [n * (n + 1) // 2 for n in xrange(some_num)]

```


##### 更复杂的例子

```python

import numpy as np
import theano
import theano.tensor as T

location = T.imatrix("location")
values = T.vector("values")
output_model = T.matrix("output_model")


def set_value_at_position(a_location, a_value, output_model):
    zeros = T.zeros_like(output_model)
    zeros_subtensor = zeros[a_location[0], a_location[1]]
    return T.set_subtensor(zeros_subtensor, a_value)
    
    
result,updates = theano.scan(fn=set_value_at_position,
		outputs_info = None,
        sequences=[location,values],
        non_sequences=output_model)
        
assign_values_at_positions = theano.function(
			inputs=[location,values,output_model],
            outputs=result)
            
test_locations = numpy.asarray([[1, 1], [2, 3]], dtype=numpy.int32)
test_values = numpy.asarray([42, 50], dtype=numpy.float32)
test_output_model = numpy.zeros((5, 5), dtype=numpy.float32)
print assign_values_at_positions(test_locations, test_values, test_output_model)

```


##### 使用共享变量进行Gibbs Sampling

```python
import numpy as np
import theano
import theano.tensor as T

W = theano.shared(W_values) # we assume that ``W_values`` contains the
                            # initial values of your weight matrix

bvis = theano.shared(bvis_values)
bhid = theano.shared(bhid_values)

trng = T.shared_randomstreams.RandomStreams(1234)

def OneStep(vsample) :
    hmean = T.nnet.sigmoid(theano.dot(vsample, W) + bhid)
    hsample = trng.binomial(size=hmean.shape, n=1, p=hmean)
    vmean = T.nnet.sigmoid(theano.dot(hsample, W.T) + bvis)
    return trng.binomial(size=vsample.shape, n=1, p=vmean,
                     dtype=theano.config.floatX)

sample = theano.tensor.vector()

values, updates = theano.scan(OneStep, outputs_info=sample, n_steps=10)

gibbs10 = theano.function([sample], values[-1], updates=updates)

```


```python

import numpy as np
import theano
import theano.tensor as T


a = theano.shared(1)
values.updates=theano.scan(lambda :{a:a+1},n_steps=10)

b = a+1
c = updates[a]+1

f = theano.function([],[b,c],updates=updates)


print b
print c
print a.value
```

We will see that because b does not use the updated version of a, it will be 2, c will be 12, while a.value is 11. If we call the function again, b will become 12, c will be 22 and a.value 21. If we do not pass the updates dictionary to the function, then a.value will always remain 1, b will always be 2 and c will always be 12.



The second observation is that if we use shared variables ( W, bvis, bhid) but we do not iterate over them (ie scan doesn’t really need to know anything in particular about them, just that they are used inside the function applied at each step) you do not need to pass them as arguments. Scan will find them on its own and add them to the graph. However, passing them to the scan function is a good practice, as it avoids Scan Op calling any earlier (external) Op over and over. This results in a simpler computational graph, which speeds up the optimization and the execution. To pass the shared variables to Scan you need to put them in a list and give it to the non_sequences argument. Here is the Gibbs sampling code updated:



```python

import numpy as np
import theano
import theano.tensor as T

W = theano.shared(W_values)

bvis = theano.shared(bvis_values)
bhid = theano.shared(bhid_values)

trng = T.shared_randomstreams.RandomStreams(1234)

def OneStep(vsample,W,bvis,bhid):
	hmean = T.nnet.sigmoid(theano.dot(vsample,W) + bhid)
    hsample = trng.binomial(size=hmean.shape,n=1,p=hmean)
    vmean = T.nnet.sigmoid(theano.dot(hsample,W.T)+bvis)
    return trng.binomial(size=vsample.shape,n=1,p=vmean,
    dtype=theano.config.floatX)
    
    
sample=theano.tensor.vector()

values,updates = theano.scan(fn=OneStep,
	outputs_info=sample,
    non_sequences=[W,bvis,bhid],
    n_steps=10)
    
    
gibbs10 = theano.function([sample],values[-1],updates=updates)

```

###### 使用scan 训练rnn  带有时序标识Multiple outputs, several taps values - Recurrent Neural Network with Scan

```python

import numpy as np
import theano
import theano.tensor as T


def oneStep(u_tm4,u_t,x_tm3,x_tm1,y_tm1,W,W_in_1,W_in_2,W_feedback,W_out):
	x_t = T.tanh(theano.dot(x_tm1,W) + \
    theano.dot(u_t,W_in_1) + \
    thenao.dot(u_tm4,W_in_2) + \
    theano.dot(y_tm1,W_feedback))
    
    y_t = theano.dot(x_tm3,W_out)
    
    return [x_t,y_t]

u = T.matrix()    # it is a sequence of vectors
x0 = T.matrix()   # initial state of x has to be a matrix, since
                # it has to cover x[-3]
y0 = T.vector() # y0 is just a vector since scan has only to provide
                # y[-1]
                
                
([x_vals,y_vals],updates) = theano.scan(fn = oneStep,
		sequences=dict(input=u,taps=[-4,-0]),
        outputs_info=[dict(initial=x0,taps=[-3,-1]),y0],
        non_sequences=[W,W_in_1,W_in_2,W_feedback,W_out],
        strict=True)
        
```


###### 使用停止条件的scan
repeat-until  结构

```python

def power_of_2(previous_power,max_value):
	return previous_power*2, theano.scan_module.until(previous_power*2 > max_value)
    
max_value=T.scalar()

values,_ = theano.scan(power_of_2,
		outputs_info=T.constant(1.),
        non_sequences=max_value,
        n_steps=1024)
        
f = theano.function([max_value],values)

print f(45)
```


































