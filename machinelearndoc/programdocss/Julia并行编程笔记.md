###Julia并行编程笔记：

#####[Julia计算集群的建立和配置][1]
Julia并行计算集群
 *  使用Julia语言进行并行计算有两个方面。首先是在本机上进行并行计算，采用多核CPU的方式，使程序分布运行在一个CPU的多个核心上。或者采用集群的方式，一台主机调用多个计算节点来运算并行程序。

 *  本地上进行并行计算，需要采用多核CPU。程序主要是addprocs_local(3)函数（新版本为addprocs(3)）来添加多核支持，这里的3为添加3个核心，加上原来的，总共四个运算核心。在加完核心后，可以输出当前参与运算的核心数nprocs()。添加的核心ID顺序依次为2,3,4……

 *  集群上进行并行计算，首先需要建立服务器集群。再没有真实平台的情况下，我们可以建立一个虚拟机集群来模拟。虚拟机集群其实就是一个互相能通信的虚拟机网络，然后在这个网络里进行添加计算节点的操作。这里介绍在集群上进行Julia计算的具体操作：

   *  1.安装最新版本Julia：将Julia更新到最新版本，我采用直接删除旧Julia目录，然后重新下载编译Julia的方式，将Julia更新到最新版本。参考1

   *  2.建立计算机集群：参考文章2建立一个真实Ubuntu电脑，以及其上的三个虚拟节点的计算机网络。按照桥接模式使其相互之间能够互联。

   *    3.配置SSH无密码登陆：按照文章3介绍，使宿主机器和虚拟计算集群之间的机器可以相互无密码登录，这样在添加远程计算节点时，就不用输入密码了。

   *     4.编写程序：集群计算和多核计算程序一直，只是在添加计算核心的时候，改变函数的参数为addprocs({gqk@192.168.0.11, “gqk@192.168.0.12”“gqk@192.168.0.13”})这种方式。添加的核心也是从2开始起步。
```J
addprocs({gqk@192.168.0.11, “gqk@192.168.0.12” “gqk@192.168.0.13”})
```
   * 5.计算和结果：
   ```J
require("zm.jl")
rzm1=remotecall(1,zm,pbInter1,XX,Y,Z)
rzm2=remotecall(2,zm,pbInter2,XX,Y,Z)
rzm3=remotecall(3,zm,pbInter3,XX,Y,Z)
rzm4=remotecall(4,zm,pbInter4,XX,Y,Z)
pbInter=vcat(fetch(rzm1),fetch(rzm2),fetch(rzm3),fetch(rzm4))
```
  该程序首先引入zm.jl文件，该文件定义了pbInter函数，该函数有三个参数x、y、z。在分别赋给1、2、3、4四个节点计算后，采用fetch取回计算结果，用vcat对结果进行连接。

         总结：这里的并行计算是并行架构方面的实验：最新版本的Julia在每个节点上都存在。建立了这些计算节点的互联网络，在此基础上配置了无密码SSH登录方法，最后编写程序实现并行计算。但是目前尚未进行算法的深入考虑。

参考

1.  Ubuntu下安装Julia http://blog.csdn.net/gongqingkui/article/details/8697099

2.  VirtualBox集群建立和网络配置http://blog.csdn.net/gongqingkui/article/details/9148377

#####三、最重要的是并行
1)   为什么要并行
并行计算的基本思想就是把复杂的工作进行分解，分解成可以同时进行的多个子任务，来缩短任务的完成时间，提升系统的吞吐性能。单线程独占模式下的程序需要等待一个任务完成，才会启动另一个任务。如果一个任务在进行大量的I\O操作,其他任务也没有办法开始，只能等待。而这时CPU就会空闲。所以多线程并发就表现出了很好的资源利用能力，一个线程在I\O操作，另一个就可以利用CPU(或其他计算)资源。

2)     如何并行
Julia提供了一个基于消息传递的多进程环境，能够同时在多处理器上使用独立的内存空间运行程序。这个内存空间由每个CPU单独控制，他们之间通过内部消息机制来通信。Julia的消息机制不同于MPI，并不是收发，而是看起来更像函数调用的方式。

并行程序的两个要素远程引用和远程调用：远程引用是引用其他特定处理器的对象，这个引用可被其它任何处理器访问。远程调用是某处理器调用其它（或自身）处理器。远端调用的结果放回远程引用，可以使用fetch来抓取结果。例如：r=remote_call(2,rand,2,2)这个远程调用中，第一个参数为核心序号，第二个参数为调用的函数名，其后为该函数rand的参数。获取该结果为fetch(r)。

而宏@spawnat用来进行调用，更为方便。如 @spawnat2 1+fetch(r)在第二个核心上进行加1操作。如果要立即获取结果，请使用remote_call_fetch(2, ref, r, 1, 1)。

为了保证程序在所有核心上都可以使用，建议使用require("myfile")的方法导入文件。了解当前系统核数的情况，请采用np = nprocs()方法。

3)     其它并行方法的问题和解决思路
使用它并行程序做运算，我们需要：合理地把复杂任务进行拆解，考虑拆解后的并行子任务的个数和机器处理核的个数关系，以及每个子任务处理的时间，对之上的数据进行平衡处理。提升比较明显的是那种处理的数据量很大，或者要执行的数据处理任务繁重，并且这些任务本身就可以分解为互不相关的子任务。

随之而来的问题是：开发复杂性增高，需要考虑子任务的协调，以及彼此间的通信，而且还要基于机器性能考虑开线程个数。

Julia是采用远程调用和动态数组的方式来解决这些问题。现在举例说明。

四、一个例子
1)     例子说明
北京市有3723条公交路线（包括上下行）。现需求根据本月历史数据，计算每条线路的平均运行时间。本实验中，我们展示采用随机生成数据。实验环境是：3GHz*4core CPU,4GB内存，带GPU卡，Linux操作系统。采用的Julia版本为0.1.2。

2)     解决办法
Julia的并行解决办法，是将该问题分解为平均1000条公交线的4个数据集，交给不同的CPU内核来运算。首先我们定义一个最内部求平均运行时间的函数。
```J
functionzm(a::Array,x::Int32,y::Int32,z::Int32)

        b=reshape(a,x,y,z)

        sum=Array(Float32,x,y)

        fori=1:x

                forj=1:y

                        fork=1:z sum[i,j]= sum[i,j] + b[i,j,k] end

                end

        end

        sum/z

end
```
该函数返回求出的平均数。该平均数结果为二维数组。

现在编写主程序，在主文件里生成三维随机数数组，他们的值为1。该随机数组为4000*40*100，代表4000条线路上的40个站点每天100趟的车次信息。程序主体如下：
```J
#importall Base  #需导入该包

const X=4000; const Y=40; const Z=200;const NP=4; XX=1000  #定义常量

busIni = Array(Int32,X,Y,Z)  #定义数据

fill!(busIni,1)  #填充为1

busInter = Array(Int32,X,Y,Z)  #定义中间计算结果数组

addprocs_local(3)  #增加并行运算的核数，增加三个，就总共为四个核参与运算。

pastart=time()  #计时开始

pbInter1=busInter[1:XX,1:Y,1:Z]  #拆分数组

pbInter2=busInter[XX+1:2XX,1:Y,1:Z]

pbInter3=busInter[2XX+1:3XX,1:Y,1:Z]

pbInter4=busInter[3XX+1:X,1:Y,1:Z]

require("zm.jl")  #导入ZM函数

rzm1=remote_call(1,zm,pbInter1,XX,Y,Z)  #在1号核上调用zm函数，传入的值为拆分的第一个数组。

rzm2=remote_call(2,zm,pbInter2,XX,Y,Z)

rzm3=remote_call(3,zm,pbInter3,XX,Y,Z)

rzm4=remote_call(4,zm,pbInter4,XX,Y,Z)

pbInter=vcat(fetch(rzm1),fetch(rzm2),fetch(rzm3),fetch(rzm4))  #取回数据并组合

println("耗时", time()-pastart, "s")  #显示计算时间
```
3)     性能表现
在上述实验配置下，不同规模运行情况

表1  运行时间对比

 

串行时间

并行时间

加速比

1600000000

11.2

3.1

3.61

4)     下一步优化
下一步可能的研究和优化点

1．进一步提高数据划分和组合的效率，减少IO时间。

2．优化和提高并行算法，使其更搞笑。

3．开发通用并行算法程序库。

五、总结
Julia为开发和运行并行程序、解决大规模数据运算问题提供了方便快捷、功能强大的语言工具。构建基于Julia和高性能计算机的云平台，将高性能计算机的计算能力通过服务的方式提供给异地客户，将是一个很有前景的应用。

参考

1.       Julia主页

2.       Ubuntu下安装Julia

3.       Julia运行程序

4.       第一个Julia程序




####[julia 与并行计算（部分有参考和转载）][3]
并行计算


（1）查看当前是否是多核运行环境

julia> nprocs() # 一般默认的启动是单核

1

（2）如果不是，可以增加

julia> nprocs()

1

julia> addprocs(2) # 增加二个CPU核

2-element Array{Any,1}:

 2

 3

julia> nprocs()

3

 

（3）指定某个核去进行相应的计算

julia> r = remotecall(2, rand, 2, 2)

RemoteRef(2,1,2)

julia> fetch(r)

2x2 Array{Float64,2}:

 0.307291  0.132549

 0.279847  0.0266977

julia> s=remotecall(3, rand, 2, 2)

RemoteRef(3,1,4)

 

julia> fetch(s)

2x2 Array{Float64,2}:

 0.906434  0.272713

 0.910273  0.227246

julia> remotecall_fetch(2, getindex, r, 1, 1)

0.307290542962672

julia> remotecall_fetch(2, getindex, s, 2, 1)

0.9102734845481404

但remotecall或remotecall_fetch都有一个问题，即是核的位置是需要记住的。

julia> a=@spawn rand(2,2)

RemoteRef(2,1,12)

julia> b=@spawn maximum([2,2])

RemoteRef(3,1,13)

julia> c=@spawn minimum([2,1])

RemoteRef(2,1,14)

julia> fetch(a)

2x2 Array{Float64,2}:

 0.634395  0.770792

 0.882946  0.346484

julia> fetch(b)

2

julia> fetch(c)

1

 

（4）并行计算的顺序和单核的情况不一样

julia> @parallel [println("$i") for i =1:10]

         From worker 3:       6

         From worker 3:       7

         From worker 3:       8

         From worker 3:       9

         From worker 3:       10

         From worker 2:       1

         From worker 2:       2

         From worker 2:       3

         From worker 2:       4

         From worker 2:       5

10-element DArray{Any,1,Array{Any,1}}:

 nothing

 nothing

 nothing

 nothing

 nothing

 nothing

 nothing

 nothing

 nothing

 nothing

 

# 布置一个其它核运行的任务，等其计算后，再取过来。

julia> b=@parallel [i for i =1:10];

julia> fetch(b)

10-element DArray{Int64,1,Array{Int64,1}}:

  1

  2

  3

  4

  5

  6

  7

  8

  9

 10

   可见，取过来的值，完全是有序的。







####[julia的几种画图方法][4]
一、画图及可视化

方法一：

using Gadfly

using Cairo

julia> myplot =plot(x=rand(10),y=rand(10))

draw(PNG("myplot.png",4inch, 3inch), myplot)

 myplot = plot(..)

 # draw on every available backend

draw(SVG("myplot.svg", 4inch, 3inch), myplot)

draw(PNG("myplot.png", 4inch, 3inch), myplot)

draw(PDF("myplot.pdf", 4inch, 3inch), myplot)

draw(PS("myplot.ps", 4inch, 3inch), myplot)

draw(D3("myplot.js", 4inch, 3inch), myplot)

 

方法二：使用Winston  问题：暂时没有发现能够画时间（datetime为横坐标的图！）

# 画二个点

julia> usingWinston

julia>display(plot(rand(1,100)))# 如果不用display可能有些情况不会显示图形

################################################

julia> plot(rand(1,100))

FramedPlot(...)

# 画二个点

julia>  figure1= FramedPlot();

point1 = Points(3, 3);

add(figure1, point1);

point2 = Points(2, 2);

add(figure1, point2);

Winston.display(figure1);

# 画一个曲线

x = linspace(0, 3pi, 100)

 c = cos(x)

 s = sin(x)

 p = FramedPlot(

        title="title!",

        xlabel="\\Sigma x^2_i",

        ylabel="\\Theta_i")

 

 add(p,FillBetween(x, c, x, s))

 add(p, Curve(x, c,color="red"))

 add(p, Curve(x, s,color="blue"))

 

 

 

#####################################################

p = FramedPlot(

        aspect_ratio=1,

        xrange=(0,100),

        yrange=(0,100))

##############

 n = 21

 x = linspace(0,100, n)

 yA = 40 .+10randn(n)

 yB = x .+5randn(n)

 a = Points(x, yA,kind="circle")

 setattr(a,label="a points")

 b = Points(x, yB)

 setattr(b,label="b points")

 style(b,kind="filled circle")

 s = Slope(1,(0,0), kind="dotted")

 setattr(s,label="slope")

 l = Legend(.1, .9,{a,b,s})

 add(p, s, a, b, l)

################

x = linspace(pi, 3pi, 60)

 c = cos(x)

 s = sin(x)

 

 p =FramedPlot(aspect_ratio=1)

 setattr(p.frame1,draw_grid=true, tickdir=1)

 

 setattr(p.x1,label="bottom", subticks=1)

 setattr(p.y1,label="left", draw_spine=false)

 setattr(p.x2,label="top", range=(10,1000), log=true)

 

 setattr(p.y2,label="right", draw_ticks=false,

    ticklabels=["-1", "-1/2", "0","1/2", "1"])

 add(p, Curve(x, c,kind="dash"))

 add(p, Curve(x,s))

##

julia> x2 = rand(10,10)

10x10 Array{Float64,2}:

 0.564009  0.0189715 0.668941   …  0.990236   0.423469   0.594781

 0.588723  0.747681  0.515079      0.00496721  0.0813008 0.450878

 0.672323  0.469073  0.118781      0.0282153   0.208464  0.52431 

 0.159263  0.297813  0.828949      0.642512    0.915281  0.806548

 0.563958  0.281605  0.93284       0.168205    0.434135  0.927634

 0.724086  0.986885  0.96821    …  0.671119   0.86806    0.374594

 0.702004  0.153323  0.379379      0.479773    0.0251379 0.138291

 0.53355   0.232235  0.0172869     0.976708    0.429525  0.743494

 0.765813  0.243128  0.603948      0.271188    0.697181  0.0147205

 0.398034  0.677016  0.504992      0.350921    0.517337  0.352407

 

julia> display(imagesc(x2))

 

方法三：PyPlot.

因为studio 装不了，搞不清楚具体什么原因，就没有用这个画图了，但也是可以画的，据说，用起来还是比较方便的。



[1]:http://blog.csdn.net/gongqingkui/article/details/9175859
[2]:http://blog.csdn.net/gongqingkui/article/details/8921579
[3]:http://blog.csdn.net/wowotuo/article/details/38234415
[4]:http://blog.csdn.net/wowotuo/article/details/38233957