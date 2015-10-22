####[Gevent中文教程][0]
定义：gevent是一个并发性库基于libev，它提供了一个较为纯净的API用来处理各类问题和网络相关任务。
#####介绍：
这教程的结构假设一个中等层次的Python程序员且没有什么**并发性**的知识．目的是给你知道怎么使用gevent,帮助你解决*并发性*的问题，和开始编写*异步的应用程序*。

#####核心
gevent中主要使用Greenlet，给python提供了一个轻量级的协同程序，作为一个C的扩展模块，Greenlets主程序运行的所有系统进程是合理安排的. 这不同于任何multiprocessing或者multithreading提供的库和POSIX线程，这**是真正的并行多处理器或多线程库提供真正的并行结构**。

#####同步&异步执行
并发的核心思想是一个更大的任务可以分解成多个子任务，其运行不依赖于其他任务的集合，因此可以异步运行 ，而不是一个在时间 同步。两个执行程序间的转换是一个关联转换。
在gevent中一个关联转换可以通过 yielding 来实现.在这个例子,两个程序的转换是通过调用 gevent.sleep(0).

```python
import gevent

def foo():
	print('Runing in foo')
    gevent.sleep(0)
    print('Explicit context switch to foo again')
    
def bar():
	print('Explicit context to bar')
    gevent.sleep(0)
    print('Implicit context switch back to bar')
    
gevent.joinall([
gevent.spawn(foo),
gevent.spawn(bar),
])
```
```shell
Running in foo
Explicit context to bar
Explicit context switch to foo again
Implicit context switch back to bar
```

在调解器里面清楚地看到程序在两个转换之间是怎么运行的.

gevent真正的能力在于我们把它用于网络和IO相关的功能会很好的合作安排.

```python
import time
import gevent
from gevent import select

start = time.time()
tic = lambda:"at %1.1f sedconds" %(time.time() - start)

def gr1():
	#Busy waits for a second ,but we don`t want to stick around..
    print 'Started Pooling:',tic()
    select.select([],[],[],2)
    print 'Ended Polling',tic()
    
def gr2():
	#Busy waits for a second,but we dont want to stick around..
    print 'Started Polling:',tic()
    select.select([],[],[],2
    
 def gr3():
	print 'Hey lets do some stuff while the greenlets pool,at',tic()
    gevent.sleep(1)
    
gevent.joinall([
gevent.spawn(gr1),
gevent.spawn(gr2),
gevent.spawn(gr3),
])
```
结果：
```shell
Started Polling:  at 0.0 seconds
Started Polling:  at 0.0 seconds
Hey lets do some stuff while the greenlets poll, at at 0.0 seconds
Ended Polling:  at 2.0 seconds
Ended Polling:  at 2.0 seconds
```

一个比较综合的例子,定义一个task函数,它是不确定的(并不能保证相同的输入输出).在这种情况运行task函数的作用只是暂停其执行几秒钟的随机数.

```python
import gevent
import gevent

def task(pid):
	"""
    Some non-deterministic task
    """
    gevent.sleep(random.randint(0,2)*0.001)
    print('Task',pid,'done')
    
def synchronous():
	for i in range(1,10):
    	task(i)
        
def asychronous():
	threads = [gevent.spawn(task,i) for i in xrange(10)]
    gevent.joinall(threads)
    
print('Synchronous:')
sychronous()

print('Asynchronous:')
asynchronous()
```
```shell

Synchronous:
Task 1 done
Task 2 done
Task 3 done
Task 4 done
Task 5 done
Task 6 done
Task 7 done
Task 8 done
Task 9 done
Asynchronous:
Task 1 done
Task 6 done
Task 5 done
Task 0 done
Task 9 done
Task 8 done
Task 7 done
Task 4 done
Task 3 done
Task 2 done
```
在同步的情况所有任务都会顺序的运行,当每个任务执行的时候导致主程序 blocking.

程序重要的部分是包装起来的函数gevent.spawn , 它是Greenlet的线程. 初始化的greenlets储存在一个数组threads ,然后提交给 gevent.joinall函数,然后阻塞当前的程序去运行所有greenlets.只有当所有greenlets停止的时候程序才会继续运行.

要注意的是异步的情况程序是无序的,异步的执行时间是远少于同步的.事实上同步去完成每个任务停止2秒的话,结果是要20秒才能完成整个队列.在异步的情况最大的运行时间大概就是2秒,因为每个任务的执行都不会阻塞其他的任务.

一个更常见的情况,是从服务器上异步获取数据,请求之间 fetch() 的运行时间会给服务器带来不同的负载.

```python
import gevent.monkey
gevent.monkey.patch_socket()

import gevent
import urllib2
import slmplejson as json

def fetch(pid):
	response = urllib2.urlopen('http://json-time.appsot.com/time/json')
    result = response.read()
    json_result = json.loads(result)
    datetime = json_result['datetime']
    
    print 'Process',pid,datetime
    return json_result['datetime']
    
def synchronous():
	for i in range(1,10):
    	fetch()
        
def asynchronous():
	threads = []
    for i in range(1,10):
    	threads.append(gevent.spawn(fetch,i))
    gevent.joinall(threads)
    
print 'Synchronous:'
synchronous()

print 'Asynchronous:'
asynchronous()
```

#####确定性
正如之前提到的，greenlets是确定性的．给相同的输入就总会提供相同的输出．例如展开一个任务来比较一个multiprocessing pool和一个gevent pool.
```python
import time

def echo(i):
	time.sleep(0.001)
    return i
    
    
#Non Deterministic Process Pool

from multiprocessing.pool import Pool

p = Pool(10)
run1 = [a for a in p.imap_unordered(echo,xrange(10))]
run2 = [a for a in p.imap_unordered(echo,xrange(10))]
run3 = [a for a in p.imap_unordered(echo,xrange(10))]
run4 = [a for a in p.imap_unordered(echo,xrange(10))]

print (run1 == run2 == run3 == run4)

#Deterministic Gevent Pool
from gevent import Pool

p = Pool(10)
run1 = [a for a in p.imap_unordered(echo,xrange(10))]
run2 = [a for a in p.imap_unordered(echo,xrange(10))]
run3 = [a for a in p.imap_unordered(echo,xrange(10))]
run4 = [a for a in p.imap_unordered(echo,xrange(10))]

print (run1 == run2 == run3 == run4)
```
结果：
```shell
False
True
```


#####spwan Threads

gevent提供了一些Greenlet初始化的封装.部分比较常用的模块是
```python
import gevent
from gevent import Greenlet

def foo(message,n):
	gevent.sleep(n)
    print(message)
    
# Initialize a new Greenlet instance running the named function
# foo
thread1 = Greanlet.spawn(foo,"Hello",1)

# Wrapper for creating and runing a new Greenlet from the named 
# function foo, with the passed arguments
thread2 = gevent.spawn(foo,"I could",2)

#Lambda expressins
thread3 = gevent.spawn(lambda x:(x+1),2)

threads = [thread1,thread2,thread3]

#Block untill all threads complete
gevent.joinall(threads)
   
```

除了用Greenlet的基类，你也可以用Greenlet的子类，重载_run 方法.
```python
from gevent iport Greenlet

class MyGreenlet(Greenlet):
	def __init__(self,message,n)
    	Greenlet.__init__(self)
        self.message = message
        self.n = n
        
     def _run(self):
     print(self.message)
     gevent.sleep(self.n)
     
g = MyGreenlset("Hi there!" 3)
g.start()
g.join()

```


#####greenlet state状态

像其他编程，Greenlets会以不同的方式失败．一个greenlet可能会抛出一个异常， 失败会使程序停止或者消耗系统很多资源.

greenlet内部的状态通常是一个按时间变化的参数.以下几个状态让你可以监听线程的状态．


  * started -- Boolean, indicates whether the Greenlet has been started. 表明是否Greenlet已经开始
  * ready() -- Boolean, indicates whether the Greenlet has halted. 表明是否Greenlet已经停止
  * successful() -- Boolean, indicates whether the Greenlet has halted and not thrown an exception. 表明是否Greenlet已经停止并且没有抛出异常
  * value -- arbitrary, the value returned by the Greenlet. 任意，Greenlet返回的值
  * exception -- exception, uncaught exception instance thrown inside the greenlet 　异常,greenlet内部实例没有被捕抓的异常
  
```python
import gevent

def win():
	return "You win!"
    
def fail():
	raise Exception('You fail at filing.')
    
winner = gevent.spawn(win)
loser  = gevent.spawn(fail)


print(winner.started)
print(loser.started)


try:
	gevent.joinall([winner,loser])
except Exception as e:
	print('This will never be reached!')
    
print(winner.value)
print(loser.value)

print(winner.ready())
print(loser.ready())

print(winner.successful())
print(loser.successful())

# The exception raised in fail, will not propogate outside the
# greenlet. A stack trace will be printed to stdout but it
# will not unwind the stack of the parent.

print(loser.exception)

# It is possible though to raise the exception again outside
# raise loser.exception
# or with
# loser.get()
```
结果：
```shell

True
True
You win!
None
True
True
True
False
You fail at failing.
```

#####program shutdown 程序关闭

当主程序接受到一个SIGQUIT的时候,Greenlets的失败可能会让程序的执行比预想中长时间．这样的结果称为"zombie processes" ，需要让Python解析器以外的程序杀掉．

一个常用的模块是在主程序中监听SIGQUIT事件和退出前调用 gevent.shutdown .
```python
import gevent
import signal

def run_forever():
	gevent.sleep(1000)
    
if __name__ == "__main__":
	gevent.signal(signal.SIGQUIT,gevent.shutdown)
    thread = gevent.spawn(run_forever)
    thread.join()
```
#####超时设定
超时是对一推代码或者一个Greenlet运行时间的一种约束.

```python
import gevent
from gevent import Timeout

seconds = 10

timeout = Timeout(seconds)
timeout.start()

def wait():
	gevent.sleep(10)
    
try:
	gevent.spawn(wait).join()
except Timeout:
	print'Could not complete'
```
或者是带着一个语境的管理在一个with的状态.

```python
import gevent
from gevent import Timeout

time_to_wait = 5 
class TooLong(Exception):
	pass
   
with Timeout(time_to_wait,ToolLong):
	gevent.sleep(10)
```
另外，gevent同时也提供timeout的参数给各种Greenlet和数据结构相关的调用．例如：
```python
import gevent
from gevent import Timeout

def wait():
	gevent.sleep(2)
    
timer = Timeout(1).start()
thread1 = gevent.spawn(wait)

try:
	thread1.join(timeout=timer)
except:
	print('Thread 1 time out')
    
timer = Timeout.start_new(1)
thread2 = gevent.spawn(wait)

try: 
	thread2.get(timeout=timer)
except Timeout:
	print('Thread 2 timed out')
    
try:
	gevent.with_timeout(1,wait)
except TImeout:
	print('Thread 3 time out')
```

#####data structures 数据结构
* Event事件
事件是Greenlets内部一种异步通讯的形式.

```python
import gevent
from gevent.event import AsyncResult

a = AsyncResult()

def setter():
	'''
    After 3 seconds set wake all threads waitting on the value of a
    '''
    gevent.sleep(3)
    a.set()
    
def waitter():
	'''
    after 3 seconds the get call will unblock
    '''
    a.get()
    print "I live"
    
gevent.joinall([
gevent.spawn(setter),
gevent.spawn(waiter),
])
```

Event对象的一个扩展AsyncResult,可以让你发送一个值连同唤醒调用.这样有时候调用一个将来或者一个延迟,然后它就可以保存涉及到一个将来的值可以用于任意时间表．
```pyhton
import gevent
from gevent.event import AsyncResult
a = AsyncResult()

def setter():
	'''
    after 3 seconds  set the result of a
    '''
    gevent.sleep(3)
    a.set("Hello!")
    
def waiter():
	'''
    after 3 seconds the get call will unblock 
    after the setter puts a value into the AsyncResult
    '''
    print a.get()
    
gevent.joinall([
gevent.spawn(setter),
gevent.spawn(waiter),
])
```
##### queue队列
Queues是一组数据的排序，有常用的 put / get操作，但也可以以另一种方式写入，就是当他们在Greenlets之间可以安全地操作．
例如如果一个Greenlet在队列中取出一个元素，同样的元素就不会被另一个正在执行的Greenlet取出．

```python
import gevent
from gevent.queue import Queue

task = Queue()    #一个消息队列

def worker(n):
	while not task.empty():
    	task = tasks.get()
        print('Worker %s got task %s' % (n,task))
        gevent.sleep(0)
        
     print('Quitting time!')
     
def boss():
	for i in xrange(1,25):
    	tasks.put_nowait(i)
        
gevent.spawn(boss).join()

gevent.joinall([
gevent.spawn(worker,'steve'),
gevent.spawn(worker,'john'),
gevent.spawn(worker,'nancy'),
])
```
Queues也可以在 put或者get的时候阻塞，如果有必要的话．

每个put和get操作不会有阻塞的情况.put_nowait和get_nowait也不会阻塞，但在操作中抛出gevent.queue.Empty或者gevent.queue.Full是不可能的.

在这个例子中，我们有一个boss同时给工人任务，有一个限制是说队列中不能超过３个工人，这个限制意味着put操作会阻塞当队伍中没有空间．相反的get操作会阻塞如果队列中没有元素可取，也可以加入一个timeout的参数来允许队列带着一个异常gevent.queue.Empty退出，如果在Timeout时间范围内没有工作．

```python
import gevent
from gevent.queue import Queue,Empty

tasks = Queue(maxsize=3)

def worker(n):
	try:
    	while True:
        	task = taaks.get(timeout=1) #decremetns queue size by 1
            print('Worker %s got task %s' % (n,task))
            gevent.sleep(0)
    except Empty:
    	print 'Quitting time!'
        
def boss():
	'''
    Boss will wlt to hand out work untill a individual worker is 
    freee since maxsize of task queue is 3
    '''
    for i in xrange(1,10):
    	tasks.put(i)
    print 'Assigned all work in iteraion 1'
    
    for i in xrange(1,20)：
    	tasks.put(i)
    print 'Assigned all work in iteration 2'
    
    
gevent.joinall([
gevent.spawn(boss)
gevent.spawn(worker,'steve')
gevent.spawn(worker,'john')
gevent.spawn(worker,'bob')
])
```

#####Actor:
baseclass:
```python
class Actor(gevent.Greeenlet):
	def __init__(self):
    	self.inbox = queue.Queue()
        Greenlet.__init__(self)
        
    def receive(self,message):
    	'''
        Define in your subclass
        '''
        rasie NotImplemented()
        
    def _run(self):
    	self.running = True
        
        while self.running:
        	message = self.inbox.get()
            self.receive(message)
```
subclass:
```python
import gevent
from gevent.queue import Queue
from gevent import Greenlet

class Pinger(Actor):
	def receive(self,message):
    	print message
        pong.inbox.put('ping')
        gevent.sleep(0)
        
class Ponger(Actor):
	def recive(self,message):
    	print message
        ping.inbox.put('pong')
        gevent.sleep(0)
        
        
ping = Pinger()
pong = Ponger()

ping.start()
pong.start()

ping.inbox.put('start')
gevent.joinall([ping,pong])
```

#####realworld application
######Gevent ZeroMQ
ZeroMQ根据其作者的描述是"一个socket库作为一个并发性的框架".它是非常强大的消息传送层在建立并发性结构和分布式应用的时候.
ZeroMQ提供了各种socket基元,最简单的就是一对Request-Response socket. 一个socket有２个有用方法 send和recv,两者一般都会有阻塞操作.但这已经被一个作者叫Travis Cline，基于gevent 写的briliant库补救了．socket 属于 ZeroMQ sockets 是一种不会阻塞的方式．你可以安装 gevent-zeromq 从　PyPi 取到: pip install gevent-zeromq

```python
# Note: Remember to ``pip install pyzmq gevent_zeromq``
import gevent
from gevent_zeromq import zmq

# Global Context
context = zmq.Context()

def server():
    server_socket = context.socket(zmq.REQ)
    server_socket.bind("tcp://127.0.0.1:5000")

    for request in range(1,10):
        server_socket.send("Hello")
        print('Switched to Server for ', request)
        # Implicit context switch occurs here
        server_socket.recv()

def client():
    client_socket = context.socket(zmq.REP)
    client_socket.connect("tcp://127.0.0.1:5000")

    for request in range(1,10):

        client_socket.recv()
        print('Switched to Client for ', request)
        # Implicit context switch occurs here
        client_socket.send("World")

publisher = gevent.spawn(server)
client    = gevent.spawn(client)

gevent.joinall([publisher, client])
```







####初试Gevent – 高性能的Python并发框架

Gevent是一个基于greenlet的Python的并发框架，以微线程greenlet为核心，使用了epoll事件监听机制以及诸多其他优化而变得高效。

于greenlet、eventlet相比，性能略低，但是它封装的API非常完善，最赞的是提供了一个monkey类，可以将现有基于Python线程直接转化为greenlet，相当于proxy了一下（打了patch）。

今天有空就迫不及待的试一下效果。

1、安装

Gevent依赖libevent和greenlet，需要分别安装。
```shell	
#libevent 1.4.x
sudo apt-get install libevent-dev
 
#python_dev
sudo apt-get install python-dev
 
#easy_install
wget -q http://peak.telecommunity.com/dist/ez_setup.py
sudo python ./ez_setup.py
 
#greenlet
wget http://pypi.python.org/packages/source/g/greenlet/greenlet-0.3.1.tar.gz#md5=8d75d7f3f659e915e286e1b0fa0e1c4d
tar -xzvf greenlet-0.3.1.tar.gz
cd greenlet-0.3.1/
sudo python setup.py install
 
#gevent
wget http://pypi.python.org/packages/source/g/gevent/gevent-0.13.6.tar.gz#md5=7c836ce2315d44ba0af6134efbcd38c9
tar -xzvf gevent-0.13.6.tar.gz
cd gevent-0.13.6/
sudo python setup.py install
```
至此，安装完毕。

2、测试代码：XML-RPC

这里必须使用支持线程的XML-RPC，否则无法发挥gevent的优势！

传统版本：
需要说明的是，这个并很多资料描述的非单线程，而是一个select版本，所以某些时候比线程版本性能好。
```shell	
from SocketServer import ThreadingMixIn
from SimpleXMLRPCServer import SimpleXMLRPCServer, SimpleXMLRPCRequestHandler
 
from SocketServer import TCPServer
 
TCPServer.request_queue_size = 10000
 
#Logic function
def add(a, b):
    return a + b
 
#Logic function 2
def gen(n):
    return '0' * n
 
#create server
server = SimpleXMLRPCServer(('', 8080), SimpleXMLRPCRequestHandler,False)
server.register_function(add, "add")
server.register_function(gen, "gen")
server.serve_forever()
```
线程版本：
```python	
from SocketServer import ThreadingMixIn
from SimpleXMLRPCServer import SimpleXMLRPCServer, SimpleXMLRPCRequestHandler
 
#Threaded XML-RPC
class TXMLRPCServer(ThreadingMixIn, SimpleXMLRPCServer): pass
 
#Logic function
def add(a, b):
    return a + b
 
#Logic function 2
def gen(n):
    return "0" * n
 
#create server
server = TXMLRPCServer(('', 8080), SimpleXMLRPCRequestHandler)
server.register_function(add, "add")
server.register_function(gen, "gen")
server.serve_forever()
```
3、测试客户端
```python	
from xmlrpclib import ServerProxy
 
#Execute RPC
server = ServerProxy("http://localhost:8080")
#print server.add(3,5)
print server.gen(2048)
```
4、gevent的monkey包装后的XML-RPC

monkey是非入侵式的patch，只需要显示调用你需要patch的东西就行了，别看我用了三行，其实可以patch_all()的
```python	
from SocketServer import ThreadingMixIn
from SimpleXMLRPCServer import SimpleXMLRPCServer, SimpleXMLRPCRequestHandler
from gevent import monkey
 
#Threaded XML-RPC && Monkey Patch
monkey.patch_socket() #Just 2 line!
monkey.patch_thread() #Just 3 line!
monkey.patch_select() #Just 3 line!
class TXMLRPCServer(ThreadingMixIn, SimpleXMLRPCServer): pass
 
#Logic function
def add(a, b):
    return a + b
 
#Logic function 2
def gen(n):
    return "0" * n
 
#create server
server = TXMLRPCServer(('', 8080), SimpleXMLRPCRequestHandler)
server.register_function(add, "add")
server.register_function(gen, "gen")
server.serve_forever()
```
5、测试结果

现在只有一台机器，下午去实验室两台机器跑了以后，放上结果。对gevent还是比较寄希望的，希望不要太差。。

客户端的特殊配置：
```python
echo -e ’1024\t65535′ | sudo tee /proc/sys/net/ipv4/ip_local_port_range
echo 1 | sudo tee /proc/sys/net/ipv4/tcp_tw_recycle
echo 1 | sudo tee /proc/sys/net/ipv4/tcp_syncookies
ulimit -n 10240
```
服务器端的特殊配置：
```python
echo “10152 65535″ > /proc/sys/net/ipv4/ip_local_port_range
echo 1 | sudo tee /proc/sys/net/ipv4/tcp_tw_recycle
sysctl -w fs.file-max=128000
sysctl -w net.ipv4.tcp_keepalive_time=300
sysctl -w net.core.somaxconn=250000
sysctl -w net.ipv4.tcp_max_syn_backlog=2500
sysctl -w net.core.netdev_max_backlog=2500
ulimit -n 10240
```
然后说让大家比较失望的结果：测试效果非常失败，经常出现异常情况，根据我的分析是默认的XML-RPC没有backlog(或者默认太低)，导致压力一大，就会fail accept，从而导致RESET（connection refused）。
所以说对monkey的patch不要抱太大希望，他是和原代码密切相关的。

补充：已经找到修改默认backlog的方法，如下：
```python
from SocketServer import TCPServer #修改这个全局变量即可 TCPServer.request_queue_size = 5000
	
from SocketServer import TCPServer
#修改这个全局变量即可
TCPServer.request_queue_size = 5000
```
当然测试数据说明，不要过分迷恋monkey，那只是个传说~

测试数据：
```shell
c=500 n=50000
默认：2845/s, 8M
多线程：1966/s, 51M
gevent：1888/s, 11M

c=1000 n=100000
默认：3096/s, 8M
多线程：1895/s, 52M
gevent：1936/s, 11M

c=5000 n=500000
默认：3009/s, 8M
多线程：失败，无法创建新线程
gevent：1988/s, 11M

c=10000 n=1000000
默认：2883/s, 8M
多线程：失败，无法创建新线程
gevent：1992/s, 20M
```
monkey的优点就是：省内存，我是和线程的相比。
我仔细的分析了一下，XML-RPC使用CPU的比例还是很大的，相比较于直接http的计算，xmlrpc还是属于cpu密集型。
在这种CPU占用很高，需要反复争夺微greenlet的情况下，gevent并不具有优势。
或者从另一种角度说，测试机不够强大，喂不饱gevent（可以看到，随着并发线程升高，gevent的性能不降反升，而默认的则在不断下降）



















[0]:http://blog.csdn.net/ewing333/article/details/7611742
[1]:http://www.coder4.com/archives/1522