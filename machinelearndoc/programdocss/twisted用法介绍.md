twisted  用法介绍

[toc]

##### 一、 简介
[传送][0]
wisted是一个用python语言写的事件驱动的网络框架，他支持很多种协议，包括UDP,TCP,TLS和其他应用层协议，比如HTTP，SMTP，NNTM，IRC，XMPP/Jabber。 非常好的一点是twisted实现和很多应用层的协议，开发人员可以直接只用这些协议的实现。其实要修改Twisted的SSH服务器端实现非常简单。很多时候，开发人员需要实现protocol类。

一个Twisted程序由reactor 发起的主循环和一些会调函数组成， 当事件发生了，比如一个client连接到了server， 这时候服务器端的时间就会被触发执行。

##### 二、 实例展现

使用Twisted 写一个简单的TCP服务器

下面的代码是一个TCPServer ， 这个server记录客户端发来的数据信息。

```python
import sys
from twisted.internet.protocol import ServerFactory
from twisted.protocol.basic improt LineReceiver
from twisted.python import log
from twisted.internet import reactor

# 实现一个cmd protocol 用于管理链接和数据传输
# protocol 类的方法都对应者一种事件处理
class CmdProtocol(LineReceiver):
	
    delimiter = '\n'
    
    def connectionMade(self):
    	self.client_ip = self.transport.getPeer()[1]   # 获取客户端的ip
        log.msg("Client connection from %s" % self.client_ip)
        if len(self.factory.clients) > self.factory.clients_max:
        	log.msg("Too many connections.bye!")
            self.client_ip = None
            self.transport.loseConnection()
        else:
        	self.factory.clients.append(self.client_ip)
            
    def connectionLost(self,reason):
    	log.msg('Lost client connection .Reason: %s' % reason)
        
        if self.client_ip:
        	self.factory.clients.remove(self.client_ip)

	def lineReceived(self,line):
    	log.msg('Cmd received from %s:%s' % (self.client_ip,line))
        

class MyFactory(ServerFactory):
	
    protocol = CmdProtocol
    
    def __init__(self,clients_max=10):
    	self.clients_max = client_max
        self.clients = []
        
if __name__ == "__main__":
	
    log.startLogging(sys.stdout)
    reactor.listenTCP(9999,MyFactory(2))
    reactor.run()
    
```

其中下面代码至关重要：
```python
from twisted.internet import reactor
reactor.run()
```

这两行代码会启动reator的主循环。

在上面的代码中我们创建了"ServerFactory"类，这个工厂类负责返回“CmdProtocol”的实例。 每一个连接都由实例化的“CmdProtocol”实例来做处理。 Twisted的reactor会在TCP连接上后自动创建CmdProtocol的实例。如你所见，protocol类的方法都对应着一种事件处理。

当client连上server之后会触发“connectionMade"方法，在这个方法中你可以做一些鉴权之类的操作，也可以限制客户端的连接总数。每一个protocol的实例都有一个工厂的引用，使用self.factory可以访问所在的工厂实例。

上面实现的”CmdProtocol“是twisted.protocols.basic.LineReceiver的子类，LineReceiver类会将客户端发送的数据按照换行符分隔，每到一个换行符都会触发lineReceived方法。稍后我们可以增强LineReceived来解析命令。

Twisted实现了自己的日志系统，这里我们配置将日志输出到stdout

当执行reactor.listenTCP时我们将工厂绑定到了9999端口开始监听。

```shell
user@lab:~/TMP$ python code1.py
2011-08-29 13:32:32+0200 [-] Log opened.
2011-08-29 13:32:32+0200 [-] __main__.MyFactory starting on 9999
2011-08-29 13:32:32+0200 [-] Starting factory <__main__.MyFactory instance at 0x227e320
2011-08-29 13:32:35+0200 [__main__.MyFactory] Client connection from 127.0.0.1
2011-08-29 13:32:38+0200 [CmdProtocol,0,127.0.0.1] Cmd received from 127.0.0.1 : hello server
```

###### 使用twisted 来处理外部进程

下面在上一个server的基础上添加一个命令， 通过这个命令可以读取 /var/log/syslog 的内容

```python
import sys
import os

from twisted.internet.protocol import ServerFactory,ProcessProtocol
from twisted.protocols.basic import LineReceiver
from twisted.python import log
from twisted.internet import reactor


class TailProtocol(ProcessProtocol):
	
    def __init__(self,write_callback):
    	self.write = write_callback
        
    def outReceived(self,data):
    	self.write("begin lastlog\n")
        data = [line for line in data.split('\n') if not line.startwith("==")]
        for d in data:
        	self.write(d+'\n')
        self.write("End lastlog\n")
        
    def processEnded(self,reason):
    	if reason.value.exitCode != 0:
        	log.msg(reason)
            
class CmdProtocol(LineReceiver):
	
    delimiter = '\n'
    
    def processCmd(self,line):
    	if line.startwith('lastlog'):
        	tailProtocol = TailProtocol(self,transport.write)
            
            # 下面是吐出一个子进程 来执行 命令 ， 并将返回结果传递给 tailProtocol 
            reactor.spawnProcess(tailProtocol,'/usr/bin/tail',args=['/usr/bin/tail','-10','/var/log/syslog'])
        elif line.startwith("exit"):
        	self.transport.loseConnection()
        else:
        	self.transport.write('Command not found.\n')
            
    def connectionMade(self):
    	self.client_ip = self.transport.getPeer()[1]   # 获取客户端的ip
        log.msg("Client connection from %s" % self.client_ip)
        if len(self.factory.clients) > self.factory.clients_max:
        	log.msg("Too many connections.bye!")
            self.client_ip = None
            self.transport.loseConnection()
        else:
        	self.factory.clients.append(self.client_ip)
            
    def connectionLost(self,reason):
    	log.msg('Lost client connection .Reason: %s' % reason)
        
        if self.client_ip:
        	self.factory.clients.remove(self.client_ip)

	def lineReceived(self,line):
    	log.msg('Cmd received from %s:%s' % (self.client_ip,line))
        self.processCmd(line)    # 调用 接受数据 处理逻辑
        

class MyFactory(ServerFactory):
	
    protocol = CmdProtocol
    
    def __init__(self,clients_max=10):
    	self.client_max=clients_max
        self.clients=[]
        
if __name__ == "__main__":
	log.startLogging(sys.stdout)
    reactor.listenTCP(9999,MyFactory(2))
    reactor.run()
        
```
在上面的代码中，没从客户端接收到一行内容后会执行processCmd方法，如果收到的一行内容是exit命令，那么服务器端会断开连接，如果收到的是lastlog，我们要吐出一个子进程来执行tail命令，并将tail命令的输出重定向到客户端。这里我们需要实现ProcessProtocol类，需要重写该类的processEnded方法和outReceived方法。在tail命令有输出时会执行outReceived方法，当进程退出时会执行processEnded方法。

如下是执行结果样例：
```shell
user@lab:~/TMP$ python code2.py
2011-08-29 15:13:38+0200 [-] Log opened.
2011-08-29 15:13:38+0200 [-] __main__.MyFactory starting on 9999
2011-08-29 15:13:38+0200 [-] Starting factory <__main__.MyFactory instance at 0x1a5a3f8>
2011-08-29 15:13:47+0200 [__main__.MyFactory] Client connection from 127.0.0.1
2011-08-29 15:13:58+0200 [CmdProtocol,0,127.0.0.1] Cmd received from 127.0.0.1 : test
2011-08-29 15:14:02+0200 [CmdProtocol,0,127.0.0.1] Cmd received from 127.0.0.1 : lastlog
2011-08-29 15:14:05+0200 [CmdProtocol,0,127.0.0.1] Cmd received from 127.0.0.1 : exit
2011-08-29 15:14:05+0200 [CmdProtocol,0,127.0.0.1] Lost client connection. Reason: [Failure instance: Traceback (failure with no frames): <class 'twisted.internet.error.ConnectionDone'>: Connection was closed cleanly.
```

可以使用下面的命令作为客户端发起命令：

```shell
user@lab:~$ netcat 127.0.0.1 9999
test
Command not found.
lastlog
Begin lastlog
Aug 29 15:02:03 lab sSMTP[5919]: Unable to locate mail
Aug 29 15:02:03 lab sSMTP[5919]: Cannot open mail:25
Aug 29 15:02:03 lab CRON[4945]: (CRON) error (grandchild #4947 failed with exit status 1)
Aug 29 15:02:03 lab sSMTP[5922]: Unable to locate mail
Aug 29 15:02:03 lab sSMTP[5922]: Cannot open mail:25
Aug 29 15:02:03 lab CRON[4945]: (logcheck) MAIL (mailed 1 byte of output; but got status 0x0001, #012)
Aug 29 15:05:01 lab CRON[5925]: (root) CMD (command -v debian-sa1 > /dev/null && debian-sa1 1 1)
Aug 29 15:10:01 lab CRON[5930]: (root) CMD (test -x /usr/lib/atsar/atsa1 && /usr/lib/atsar/atsa1)
Aug 29 15:10:01 lab CRON[5928]: (CRON) error (grandchild #5930 failed with exit status 1)
Aug 29 15:13:21 lab pulseaudio[3361]: ratelimit.c: 387 events suppressed
 
  
End lastlog
exit
```

###### 使用 Deferred 对象

reactor是一个循环，这个循环在等待事件的发生。 这里的事件可以是数据库操作，也可以是长时间的计算操作。 只要这些操作可以返回一个Deferred对象。Deferred对象可以自动得在事件发生时触发回调函数。reactor会block当前代码的执行。

现在我们要使用Defferred对象来计算SHA1哈希。

```shell

import sys
import os
import hashlib

from twisted.internet.protocol import ServerFacotry,ProcessProtocol
from twisted.protocols.basic import LineReceiver
from twisted.python import log
from twisted.internet import reactor,threads

#  展示 文件末尾的部分的协议
class TailProtocol(ProcessProtocol):
	def __init__(self,write_callback):
    	self.write = write_callback
        
    def outRecived(self,data):
    	self.write("Begin lastlog\n")
        data = [ line for line in data.split('\n') if not line.startwith("==")]
        for d in data:
        	self.write(d+'\n')
        self.write("End lastlog\n")
        
    def processEnded(self,reason):
    	if reason.value.exitCode != 0:
        	log.msg(reason)

# 定一个hash 计算类
class HashCompute(object):
	
    def __init__(self,path,write_callback):
    	self.path=path
        self.write=write_callback
        
    def blockingMehtod(self):
    	os.path.isfile(self.path)
        data = file(self.path).read()
        # uncomment to add more delay
        # import time
        # time.sleep(10)
        # 计算 sha1 值
        return hashlib.sha1(data).hexdigest()  
    
    # 将 计算方法 封装到一个deferr线程中
    def compute(self):
    
    	
        # 将执行结果封装到 一个defer对象 ， deferToThread 调用后会立刻返回一个
        # defer 对象
    	d = threads.deferToThread(self.blockingMethod)  # 将计算函数封装到线程中
        d.addCallback(self.ret)  # 为 deffer 对象 添加执行回调函数
        d.addErrback(self.err)   # 为 deffer 对象 添加错误回调函数
        
    def ret(self,hdata):
    	self.write("File hash is : %s\n"% hdata)
        
    def err(self,failure):
    	self.write("An error occured: %s\n" %failure.getErrorMessage() )
        
class CmdProtocol(LineReceiver):
	
    delimier = '\n'
    
    def processCmd(self,line):
    	if line.statswith('lastlog'):
        	tailProtocol = TailProtocol(self.transport.write)
            reactor.spawnProcess(tailProtocol,'/usr/bin/tail' ,args=['/usr/bin/tail','-10','/var/log/syslog'])
        elif line.startwith('comhash'):
        	try:
            	useless,path = line.split(' ')
            except:
            	self.transport.write('please provide a path.\n')
                return
            hc = HashCompute(path,self.transport.write)
            hc.compute()
        elif line.startwith('exit'):
        	self.transport.loseConnection()
        else:
        	self.transport.write('Command not found.\n')
            
    def connectionMade(self):
    	self.client_ip = self.transport.getPeer()[1]
        log.msg("Client connection from %s" % self.client_ip)
        if len(self.factory.clients) >= self.factory.clients_max:
        	log.msg("Too Manny connections,bye!")
            self.client_ip=None
            self.transport.loseConnection()
        else:
        	self.factory.clients.append(self.client_ip)
           
     def connectionLost(self,reason):
     	log.msg("Lost client connection .Reason : %s " % reason)
        if self.client_ip:
        	self.factory.clients.remove(self.client_ip)
            
     def lineReceived(self,line):
     	log.msg('Cmd received from %s : %s' % (self.client_ip,line))
        self.processCmd(line)
        
class MyFactory(ServerFactory):
	
    protocol = CmdProtocol
    
    def __init__(self,clients_max=10):
    	self.clients_max = client_max
        self.clients = []
        
if __name__ == "__main__":
	log.startLogging(sys.stdout)
    reactor.listenTCP(9999,MyFactory(2))
    reactor.run()
```

blockingMethod从文件系统读取一个文件计算SHA1，这里我们使用twisted的deferToThread方法，这个方法返回一个Deferred对象。这里的Deferred对象是调用后马上就返回了，这样主进程就可以继续执行处理其他的事件。当传给deferToThread的方法执行完毕后会马上触发其回调函数。如果执行中出错，blockingMethod方法会抛出异常。如果成功执行会通过hdata的ret返回计算的结果。


##### 详细介绍并发编程 defer 的作用

[传送][1]

###### 导读
这篇文档介绍了异步编程模型，以及在Twisted中抽象出的Deferred——象征着“承诺了一定会有的”结果，并且可以把最终结果传递给处理函数（Python中实现了__call__()方法的对象都可以称之为“函数”，方法也是以函数的形式存在的，因此将所有“function”译作“函数”。——译者注）。

这篇文档适合于刚接触Twisted的读者，并且熟悉Python编程语言，至少从概念上熟悉网络的核心概念，诸如服务器、客户端和套接口。这篇文档将给你一个对并发编程的上层概观（交叉执行许多任务），以及Twisted的并发模型：非阻塞编码或者叫异步编码。

在讨论过包含有Deferred的并发模型之后，将介绍当函数返回了一个Deferred对象时，处理结果的方法。

###### 一 、 并发编程介绍

要完成某些计算任务经常需要不少时间，其原因有两点：

 1.   任务是计算集中型的（比如，求一个很大整数的所有因数），并且需要相当的CPU时间进行计算；或者
 2.   任务并不是计算集中型的，但是需要等待某些数据，以产生结果。

**等待回应**

网络编程的基本功能就是等待数据。想象你有一个函数，这个函数会总结一些信息并且作为电子邮件发送。函数需要连接到一个远程服务器、等待服务器的回应、检查服务器能否处理这封电子邮件、等待回应、发送电子邮件、等待确认信息，然后断开连接。

这其中任何一步都有可能占用很长时间。你的程序可能使用所有可能模型中最简单的一个——它实际上只是停下来等着数据的发送和接收，但在这种情况下，它有非常明显的基本限制：它不能同时发送多封电子邮件；并且在发送电子邮件的时候，它其实什么也做不了。

因此，除了最简单的之外，所有网络程序都会避免这种模型。另外还有许多不同的模型，它们都允许你的程序在等待数据以继续某个任务的同时，继续做手头上的其他任务，这些模型你都可以采纳。

**不等待数据**

编写网络程序有很多种办法，主要有这么几种：

  1.  在不同的操作系统进程中处理各个连接，这种情况下，操作系统会处理进程调度，比如当一个进程等待的时候，让其他进程继续工作；
  2.   在不同的线程中处理各个连接1，这种情况下，线程框架会处理好诸如“当一个线程等待时，让其他线程继续工作”的问题；这种方法有很多变种，比如说在一个有限大小的线程池中处理所有连接，本质上来说，这是同一个想法的优化版。
  3.  在一个线程中，使用非阻塞的系统调用来处理所有连接。

**非阻塞调用**

上述第三种模型就是使用Twisted框架时的标准模型：非阻塞调用。

当在一个线程中处理多个连接时，调度就成为了应用程序的责任，而不是操作系统的。调度通常的实现方案是：
*当连接准备好读或是写时，调度系统会调用一个之前注册过的函数——通常被叫做异步，事件驱动或是基于回调的编程*。

在这种模型下，之前发送电子邮件的函数应该是象这样的：

   1. 调用一个连接函数，用以连接到远程服务器；
   2. 连接函数立刻返回，暗示着当连接建立后，将调用电子邮件发送的通知；并且连接一旦建立，系统就会通知发送电子邮件的函数，连接已经准备就绪。

于我们最初阻塞的步骤相比，上面这种非阻塞的步骤有什么好处呢？当发送电子邮件的函数在连接建立之前无法继续的时候，程序的其他部分依然可以执行其他任务，比如说开始为其他电子邮件的连接执行类似的步骤。于是，整个程序就不会卡在等待一个连接的建立上。

**callback**

callback（回调）是通知应用程序数据已经就绪的经典模型。**应用程序在调用一个方法，试图获取一些数据时，同时提供一个callback函数，当数据就绪时，这个 callback函数会被调用，并且就绪的数据就是调用的参数之一**。因此，应用程序应该在callback函数中，继续执行之前获取这些数据时，想要执行的任务（当时没能马上得到这些数据，所以当时无法执行这些任务——译者注）。

*在同步编程中，一个函数会先请求数据，然后等待数据，最后处理它。在异步编程中，一个函数请求数据之后，会在数据准备就绪后，让外部库调用其callback函数*。

###### Deferred 作用说明
Twisted使用Deferred对象来管理callback序列。作为Twisted库的“客户端”，应用程序将一连串函数添加到Deferred对象中，当异步请求的结果准备就绪时，这一连串函数将被按顺序调用（这一连串函数被称为一个callback序列，或是一条callback链），一起添加的还有另外一连串函数，当异步请求出现错误的时候，他们将被调用（称作一个errback序列，或是一条errback链）。异步库代码会在结果准备就绪时，调用第一个callback，或是在出现错误时，调用第一个errback，然后Deferred对象就会将callback或errback的返回结果传递给链中的下一个函数。

###### Deferred 处理的问题

Deferred被设计用来帮助解决第二类并发问题——非计算集中型任务，并且延迟时间是可以估计的。等待硬盘访问，数据库访问和网络访问的函数都属于这一类，尽管时间延迟不尽相同。

Deferred被设计用来使Twisted程序可以无阻塞地等待数据，直到数据准备就绪。为了达到这个目的，Deferred为库与应用程序提供了一个简单的callback管理接口。库可以通过调用Deferred.callback来把准备就绪的数据传回给应用程序，或者调用Deferred.errback来报告一个错误。应用程序可以按它们希望的顺序，设置结果处理逻辑，以callback与errback的形式添加到Deferred对象中去。

使 CPU尽可能的有效率，是Deferred与该问题的其他解决方案背后的基本思路。如果一个任务在等待数据，与其让CPU（以及程序本身！）眼巴巴地等着数据（对于进程，这通常叫做“阻塞”），不如让程序同时执行些别的操作，并且同时留意着某些信号——一旦数据准备就绪，程序就可以（但不是立即——译者注）回到刚才的地方继续执行。

在Twisted里，一个函数返回一个Deferred对象，意味着它给调用者一个信号：它在等待数据。当数据准备就绪后，程序就会激活那个Deferred对象的callback链，来处理数据。

###### Deferred---数据即将到来的信号

在之前我们发送电子邮件的例子中，父函数调用了一个连接远程服务器的函数。异步性要求这个连接函数必须不等待结果而直接返回，父函数才可以做其他事情。可是，父函数或者它的控制程序又是怎么知道连接尚未建立的呢？当连接建立后，它又是怎么使用连接的呢？

Twisted有一个对象可以作为这种情况的一个信号。连接函数返回一个twisted.internet.defer.Deferred对象，给出一个操作尚未完成的信号。

Deferred有两个目的。第一，它说“我是一个信号，只是通知你不管你刚才要我做的什么，结果还没有出来”。第二，你可以让Deferred在结果出来后执行你的东西。

**callback**

添加一个callback——这就是你让Deferred在结果出来后执行你东西的办法，也就是让Deferred在结果出来后调用一个方法。

有一个Twisted库函数返回Deferred，就是twisted.web.client.getPage（这是一个异步获取网页的函数。——译者注）。在这个例子里，我们调用了getPage——它返回了一个Deferred，然后添加了一个callback来处理返回的页面内容——当然处理是发生在数据准备就绪之后：

```python
from twisted.web.client import getPage

from twisted.internet import reactor

def printContents(contents):
    '''
    这就是“callback”函数，被添加到Deferred，当“承诺了一定会有的数据”准备就绪后，
    Deffered会调用它
    '''

    print "Deferred调用了printContents，内容如下："
    print contents

    # 停止Twisted事件处理系统————这通常有更高层的办法
    reactor.stop()

# 调用getPage，它会马上返回一个Deferred————承诺一旦页面内容下载完了，
# 就会把他们传给我们的callback们
deferred = getPage('http://twistedmatrix.com/')        //一旦调用getPage下载页面完成,则立即调用callback对应的函数.即addCallback(printContents)
 中的printContents函数.



# 给Deferred添加一个callback————要求它在页面内容下载完后，调用printContents
deferred.addCallback(printContents)

# 启动Twisted事件处理系统，同样，这不是通常的办法
reactor.run()

添加两个callback是一种非常常见的Deferred用法。第一个callback的返回结果会传给第二个callback：

from twisted.web.client import getPage

from twisted.internet import reactor

def lowerCaseContents(contents):
    '''
    这是一个“callback”函数，被添加到Deferred，当“承诺了一定会有的数据”准备就绪后，
    Deffered会调用它。它把所有的数据变成小写
    '''

    return contents.lower()

def printContents(contents):
    '''
    这是一个“callback”函数，在lowerCaseContents之后被添加到Deferred，
    Deferred会把lowerCaseContents的返回结果作为参数，调用这个callback
    '''

    print contents
    reactor.stop()

deferred = getPage('http://twistedmatrix.com/')

# 向Deferred中添加两个callback————让Deferred在页面内容下载完之后执行
# lowerCaseContents，然后将其返回结果作为参数，调用printContents
deferred.addCallback(lowerCaseContents)　　　　                          //当有多个addCallback的时候,第一个addCallback(lowerCaseContents)执行后的结果,会当作第二个addCallback(printContents)
 函数的参数



deferred.addCallback(printContents)

reactor.run()
```

**错误处理**

正如异步函数会在其结果产生之前返回，在有可能检测到错误之前返回也是可以的：失败的连接，错误的数据，协议错误，等等。正如你可以将callback添加到Deferred，你也可以将错误处理逻辑（“errback”）添加到Deferred，当出现错误，数据不能正常取回时，Deferred会调用它：

```python
from twisted.web.client import getPage

from twisted.internet import reactor

def errorHandler(error):
    '''
    这是一个“errback”函数，被添加到Deferred，当出现错误事件是，Deferred将会调用它
    '''

    # 这么处理错误并不是很实际，我们只是把它打出来：
    print "An error has occurred: <%s>" % str(error)
    # 然后我们停止整个处理过程：
    reactor.stop()

def printContents(contents):
    '''
    这是一个“callback”函数，被添加到Deferred，Deferred会把页面内容作为参数调用它
    '''

    print contents
    reactor.stop()

# 我们请求一个不存在的页面，来演示错误链
deferred = getPage('http://twistedmatrix.com/does-not-exist')

# 向Deferred添加callback，以处理页面内容
deferred.addCallback(printContents)

# 向Deferred添加errback，以处理任何错误
deferred.addErrback(errorHandler)

reactor.run()
```

###### 参考资料
因为Deferred的抽象是Twisted编程中如此核心的一部分，以至于另外还有几篇关于它的详细的指南：

    [使用Deferred][2]，一篇关于使用Deferred的更完整的指南，包括链式Deferred。
    [产生Deferred][3]，一篇关于创建Deferred和触发他们callback链的指南。











[0]:http://www.jb51.net/article/64182.htm
[1]:http://www.cnblogs.com/zhangjing0502/archive/2012/05/16/2504415.html
[2]:http://fantix.org/twisted-doc-zh/nightly/online/howto/defer.html
[3]:http://fantix.org/twisted-doc-zh/nightly/online/howto/gendefer.html





