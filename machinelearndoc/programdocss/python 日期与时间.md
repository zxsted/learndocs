###python 日期与时间 (time,datetime包)
[toc]

#####概述
在应用程序的开发过程中，难免要跟日期、时间处理打交道。如：记录一个复杂算法的执行时间；网络通信中数据包的延迟等等。Python中提供了time, datetime calendar等模块来处理时间日期，今天对time模块中最常用的几个函数作一个介绍。
######time.time
time.time()函数返回从1970年1月1日以来的秒数，这是一个浮点数。
######time.sleep
可以通过调用time.sleep来挂起当前的进程。time.sleep接收一个浮点型参数，表示进程挂起的时间。
######time.clock
在windows操作系统上，time.clock() 返回第一次调用该方法到现在的秒数，其精确度高于1微秒。可以使用该函数来记录程序执行的时间。下面是一个简单的例子：
```java
import time

print time.clock() #1
time.sleep(2)
print time.clock() #2
time.sleep(3)
print time.clock() #3
#---- result  
#3.91111160776e-06  
#1.99919151736  
#4.99922364435
```
######time.gmtime
　该函数原型为：time.gmtime([sec])，可选的参数sec表示从1970-1-1以来的秒数。其默认值为time.time()，函数返回time.struct_time类型的对象。（struct_time是在time模块中定义的表示时间的对象），下面是一个简单的例子
```java
import time  
  
print time.gmtime()  #获取当前时间的struct_time对象  
print time.gmtime(time.time() - 24 * 60 * 60)  #获取昨天这个时间的struct_time对象  
  
#---- result  
#time.struct_time(tm_year=2009, tm_mon=6, tm_mday=23, tm_hour=15, tm_min=16, tm_sec=3, tm_wday=1, tm_yday=174, tm_isdst=0)  
#time.struct_time(tm_year=2009, tm_mon=6, tm_mday=22, tm_hour=15, tm_min=16, tm_sec=3, tm_wday=0, tm_yday=173, tm_isdst=0)
```
######time.loacltime
time.localtime与time.gmtime非常类似，也返回一个struct_time对象，可以把它看作是gmtime()的本地化版本。
######time.mktime
　time.mktime执行与gmtime(), localtime()相反的操作，它接收struct_time对象作为参数，返回用秒数来表示时间的浮点数。例如:
```python
import time  
  
#下面两个函数返回相同（或相近）的结果  
print time.mktime(time.localtime())  
print time.time()
```
######time.strftime
time.strftime将日期转换为字符串表示，它的函数原型为：time.strftime(format[, t])。参数format是格式字符串（格式字符串的知识可以参考：time.strftime），可选的参数t是一个struct_time对象。下面的例子将struct_time对象转换为字符串表示：
```python
    import time  
      
    print time.strftime('%Y-%m-%d %H:%M:%S', time.gmtime())  
    print time.strftime('Weekday: %w; Day of the yesr: %j')  
      
    #---- result  
    #2009-06-23 15:30:53  
    #Weekday: 2; Day of the yesr: 174  
```
######time.strptime
按指定格式解析一个表示时间的字符串，返回struct_time对象。该函数原型为：time.strptime(string, format)，两个参数都是字符串，下面是一个简单的例子，演示将一个字符串解析为一个struct_time对象：
```python
import time  
  
print time.strptime('2009-06-23 15:30:53', '%Y-%m-%d %H:%M:%S')  
  
#---- result  
#time.struct_time(tm_year=2009, tm_mon=6, tm_mday=23, tm_hour=15, tm_min=30, tm_sec=53, tm_wday=1, tm_yday=174, tm_isdst=-1)
```



Python具有良好的时间和日期管理功能。实际上，计算机只会维护一个挂钟时间(wall clock time)，这个时间是从某个固定时间起点到现在的时间间隔。时间起点的选择与计算机相关，但一台计算机的话，这一时间起点是固定的。其它的日期信息都是从这一时间计算得到的。此外，计算机还可以测量CPU实际上运行的时间，也就是处理器时间(processor clock time)，以测量计算机性能。当CPU处于闲置状态时，处理器时间会暂停。

#####time包
time包基于C语言的库函数(library functions)。Python的解释器通常是用C编写的，Python的一些函数也会直接调用C语言的库函数。
```python
import time
print(time.time)   #wall clock time ,unit,second
print(time.clock)  #processor clock time,unit; second
```
time.sleep()可以将程序置于休眠状态，直到某时间间隔之后再唤醒程序，让程序继续运行。
```python
import time
print('start')
time.sleep(10)   # sleep for 10 seconds
print('wake up')
```
当我们需要定时的查看程序运行状态时，就可以使用该方法。

time包还定义了struct_time对象。该对象实际上是将挂钟时间转换为年、月、日、时、分、秒……等日期信息，存储在该对象的各个属性中(tm_year, tm_mon, tm_mday...)。下面方法可以将挂钟时间转换为struct_time对象:

```python
st = time.gmtime()  #返回struct_time格式的UTC时间
st = time.localtime() #返回struct_time格式的当地时间，当地地区根据系统环境决定。

s = time.mktime(st) #将struct_time格式转换成wall clock time
```

#####datetime包
######1）简介
datetime包是基于time包的一个高级包， 为我们提供了多一层的便利。

datetime可以理解为date和time两个组成部分。date是指年月日构成的日期(相当于日历)，time是指时分秒微秒构成的一天24小时中的具体时间(相当于手表)。你可以将这两个分开管理(datetime.date类，datetime.time类)，也可以将两者合在一起(datetime.datetime类)。由于其构造大同小异，我们将只介绍datetime.datetime类。

比如说我现在看到的时间，是2012年9月3日21时30分，我们可以用如下方式表达：
```python
import datetime
t = datetime.datetime(2012,9,3,21,30)
print(t)
```
所返回的t有如下属性:

hour, minute, second, microsecond

year, month, day, weekday   # weekday表示周几

######2) 运算

datetime包还定义了时间间隔对象(timedelta)。一个时间点(datetime)加上一个时间间隔(timedelta)可以得到一个新的时间点(datetime)。比如今天的上午3点加上5个小时得到今天的上午8点。同理，两个时间点相减会得到一个时间间隔。
```python
import datetime
t = datetime.datetime(2012,9,3,21,30)
t_next = datetime.datetime(2012,9,5,23,30)
delta1 = datetime.timedelta(seconds=600)
delta2 = datetime.timedelta(weeks=3)

print(t+delta1)
print(t+delta2)
print(t_next - t)
```
在给datetime.timedelta传递参数（如上的seconds和weeks）的时候，还可以是days, hours, milliseconds, microseconds。

两个datetime对象还可以进行比较。如使用上面的t和t_next：
```python
print(t > t_next)
```

######3) datetime对象与字符串转换

假如我们有一个的字符串，我们如何将它转换成为datetime对象呢？

一个方法是用上一讲的正则表达式来搜索字符串。但时间信息实际上有很明显的特征，我们可以用格式化读取的方式读取时间信息。
```python
from datetime import datetime
format="output-%Y-%m-%d-%H%M%S.txt"
str = "output-1997-12-23-030000.txt"
t   = datetime.strptime(str,format)
```
strptime,p = parsing

我们通过format来告知Python我们的str字符串中包含的日期的格式。在format中，%Y表示年所出现的位置, %m表示月份所出现的位置……。

反过来，我们也可以调用datetime对象的strftime()方法，来将datetime对象转换为特定格式的字符串。比如上面所定义的t_next:

```python
print(t_next.strftime(format))
```
strftime, f = formatting

具体的格式写法可参阅官方文档。 如果是Linux系统，也可查阅date命令的手册($man date)，两者相通。


####python中关于时间和日期函数的常用计算总结（time和datetime）

######1、获取当前时间的两种方法
```python
import datetime,time
now = time.strftime("%Y-%m-%d %H:%M:%S")
print now
now = date.time.now()
print now
```
######2、获取上一个月的最后一天的日期（本月的第一天减去1天）
```python
last = datetime.date(datetime.date.today().year,datetime.date.today().month,1)-datetime.timedelta(1)
print last
```
######3、获取时间差
```python
starttime = datetime.datetime.now()
#long running
endtime = datetime.datetime.now()
print(endtime - starttime).sends
```

######4、计算当前时间向后10个小时的时间
```python
d1 = datetime.datetime.now()
d3 = d1 + datetime.timedelta(hours = 10)
d3.ctime()
```
其本上常用的类有：datetime和timedelta两个。它们之间可以相互加减。每个类都有一些方法和属性可以查看具体的值，如 datetime可以查看：天数(day)，小时数(hour)，星期几(weekday())等;timedelta可以查看：天数(days)，秒数 (seconds)等。

######5、python中时间日期格式化符号
```shell
%y 两位数的年份表示（00-99）
%Y 四位数的年份表示（000-9999）
%m 月份（01-12）
%d 月内中的一天（0-31）
%H 24小时制小时数（0-23）
%I 12小时制小时数（01-12） 
%M 分钟数（00=59）
%S 秒（00-59）

%a 本地简化星期名称
%A 本地完整星期名称
%b 本地简化的月份名称
%B 本地完整的月份名称
%c 本地相应的日期表示和时间表示
%j 年内的一天（001-366）
%p 本地A.M.或P.M.的等价符
%U 一年中的星期数（00-53）星期天为星期的开始
%w 星期（0-6），星期天为星期的开始
%W 一年中的星期数（00-53）星期一为星期的开始
%x 本地相应的日期表示
%X 本地相应的时间表示
%Z 当前时区的名称
%% %号本身
```



[更详细的介绍datetime]:http://www.jb51.net/article/31129.htm
[更详细的介绍time]:http://www.jb51.net/article/49326.htm



















