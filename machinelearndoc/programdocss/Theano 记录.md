Theano 记录

$x(n) = tanh(Wx(n-1) + W_1^{in}u(n) + w_2^{in}u(n-4) +
W^{feedback}y(n-2))$
$y(n) = W^{out}x(n-3)$

[toc]

#####numpy数组
```python
import numpy
print "ok!"
#将普通python的list转化为numpy的array类型
print numpy.asarray([1,2],[3,4],[5,6]).shape  
#取出指定位置的元素
print numpy.asarray([1,2],[3,4],[5,6])[2,0]
```
output:
```shell
OK!
(3,2)
5
```
什么是广播操作（broadcasting）？
为了让不同形状（shape）的数组进行运算（诸如： + - * /）,numpy提供了广播机制，让小的数组广播成与大的数组一致，然后进行运算，下面是一个例子：
```shell
a = numpy.asarray([1.0,2.0,3.0])
b = 2.0
print a*b
print b*a
```
output：
```shell
[2. 4. 6.]
[2. 4. 6.]
```


#####起步：Theano如何做运算

下面是theano中的两个double类型的变量运算
```python
import theano.tensor as T
from theano import function

#将变量x申明成theano中的标记变量
x = T.dscalar('x')   
y = T.dscalar('y')
#给出计算表达式
z = x + y

#将上面的参数和计算表达式compile成theano函数
f = function([x,y],z)

#使用上面compile成的函数进行计算
print f(2,3)

```
输出：
```shell
5.0
```

下面是两个矩阵相乘
```python
#格式化变量
x = T.dmatrix('x')   #注意这里变量使用矩阵类型进行格式化
y = T.dmatrix('y')

#定义计算表达式
z = x + y

#将上面的变量和表达式compile成theano函数
f2 = function([x,y],z)

#使用theano函数进行运算
print f2([1,2],[3,4] , [10,20],[30,40])
```

这里出现了3个专有名词：
scalars  ---------- 标量： 只有大小，没有方向。 如欧式空间中的两点间的距离
matrixes ---------- 矩阵： 一种数学标记方法，通常由行和列组成，
vectors  ---------- 向量： 也称之为矢量，即有方向的向量，与标量相对。

可以用来作加法运算的变量：

The following types are available：
byte: bscalar, bvector, bmatrix, brow, bcol, btensor3, btensor4
16-bit integers: wscalar, wvector, wmatrix, wrow, wcol, wtensor3, wtensor4
32-bit integers: iscalar, ivector, imatrix, irow, icol, itensor3, itensor4
64-bit integers: lscalar, lvector, lmatrix, lrow, lcol, ltensor3, ltensor4
float: fscalar, fvector, fmatrix, frow, fcol, ftensor3, ftensor4
double: dscalar, dvector, dmatrix, drow, dcol, dtensor3, dtensor4
complex: cscalar, cvector, cmatrix, crow, ccol, ctensor3, ctensor4

下面是实例：
```python
import numpy

import theano.tensor as T
from  theano  import function
#from nupy import asarray
#from PIL import Image

#im=Image.open("/Users/chaimwu/DeskTop/test.png")
#im.rotate(45).show()
print "OK!"
#print random.rand(4,4)

print numpy.asarray([1,2],[3,4],[5,6]).shape
print numpy.asarray([1,2],[3,4],[5,6])[2,0]

a = numpy.asarray([1.0,2.0,3.0])

b = 2.0

print a*b
print b*a

#使用标量
x = T.dscalar('x')
y = T.dscalar('y')
z = x + y
f1 = function([x,y],z)

print f1(1,3)

#使用矩阵
x = T.dmatrix('x')
y = T.dmatrix('y')

z = x + y    #函数表达式

f2 = function([x,y],z)

print f2([[1,2],[3,4]] , [[10,20][30,40]])

#还是使用矩阵
x = T.dmatrix()
y = T.dmatrix()

z = x + 2**y

f3 = function([x,y],z)

print f3( [[1,2],[3,4]] , [[10,20][30,40]])


#使用矢量 
x = T.dvector()
y = T.devctor()

z = 2*x + 2**y

f4 = function([x,y],z)

print f4([2,3],[4,5])

```


#####使用theano的一些小技巧
* 第一个技巧
  默认值的设置
  ```python
  x = T.dmatrix()
  s2 = (1 + T.tanh(x/2))/2
  
  #表达式 Param（x,default=[[0,10],[-1,-2]]） 就是将 x 设置为
  # 矩阵 [[0,10],[-1,-2]]
  logsitic2 =function([Param(x,default=[[0,10],[-1,-2]])],s2)
  
  print logistic2()
  
  ```
  输出：
  ```shell
  ［ 0.5 0.9999546 ]
   [ 0.26894142 0.11920292］
  ```
  
* 第二个技巧
  多路输出
  
  ```python
   #注意下面使用的是复数形式 
  a,b = T.dmatrix('a','b')
  diff = a - b
  abs_diff = abs(diff)
  diff_squared = diff**2
  
  f = function([a,b],[diff,abs_diff,diff_squared])
  
  print f([[1,1],[2,3]],[[2,5],[4,7]])
  
  ```
  
#####使用theano中的共享变量 

* 这里的函数定义比较特别需要注意  它使用了updates 参数作为 共享变量的跟新规则
* 还有需要注意的是共享变量不许直接进行计算，只能进行set_value() 和 get_value()操作

```python
import numpy
import theano.tensor as T

#导入常用的对象 ： 函数 变量 共享变量
from theano import function,Param,shated

#state是共享变量
state = shared(0)
#inc是标量 （作为增量）
inc = T.iscalar("inc")

#这里使用 updates 作为共享变脸的更新规则
acc = function([inc],state,updates=[(state,state + inc)])

# 这里的函数的值就是state 输入值就是inc  函数的定义是每次输出都与更新延迟一步
print "function value is : %f " %acc(2)

#输出共享变量的值
print "shared varince is : %f" % state.get_value()

print "function value is : %f " %acc(200000)
print "shared varince is : %f" % state.get_value()

state.set_value(-1)
print "function value is : %f " %acc(3)
print "shared varince is : %f" % state.get_value()

```
#####共享变量的奥秘
 为什么要用Update方法呢？
1、方便，运算效率比get set方法更高
2、可以用同样的结构支持多种变量

也许还有更多的细节。。后续使用到再补充。
```python
state = shared(0)
inc = T.iscalar('inc')
state_f = state*2 + inc
foo = T.scalar(dtype=state.dtype)   #用于每步跟新共享变量 ，但不是以updates的方式而是以 givens=[(state,foo)]  直接给定的方式

acc = function([inc],state,updates=[(state,state+inc)])
#用于每步跟新共享变量 ，但不是以updates的方式而是以 givens=[(state,foo)]  直接给定的方式
dcc = function([inc,foo] ,state_f,givens=[(statee,foo)])

fcc = function([inc],state_f,updates=[(state,state+inc)])

print acc(2)
print dcc(2)
print fcc(2)

print state.get_value()

```

#####使用随机变量
随机变量很多时候在处理噪声时使用
```shell
#使用一个种子来产生随机数
srng = RandomStreams(seed = 234)

#产生一个均匀随机变量
rv_u = srng.uniform((2,2))
#产生一个正态分布的随机变量
rv_n = srng.normal((2,2))

#compile成随机数生成函数
f = function([],rv_u)
g = function([],rv_n,no_default_updates=True)
nearly_zeros = function([],rv_u+rv_u-2*rv_u)

print f()
print g()
print nearly_zeros()
```


























