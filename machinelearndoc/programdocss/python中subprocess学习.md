###[python中subprocess学习][0]
[toc]
subprocess 的目的就是启动一个新的进程并且与之通信
subprocess模块中指定一了类： Popen。可以使用Popen来创建进程，并与进程进行复杂的交互。其构造函数如下：
subprocess.Popen(args,bufsize=0,executable=None,stdin=None,stdout=None,stderr=None,preexec_fn=None,close_fds=Flase,shell=Flase,cwd=NOne,en=None,universal_newlines=False,startupinfo=None,creationflags=0)

参数args可以是字符串或者序列类型（如：list，元组），用于指定进程的可执行文件及其参数。如果是序列类型，第一个元素通常是可执行文件的路径。我们也可以显式的使用executeable参数来指定可执行文件的路径。

参数stdin, stdout, stderr分别表示程序的标准输入、输出、错误句柄。他们可以是PIPE，文件描述符或文件对象，也可以设置为None，表示从父进程继承。

如果参数shell设为true，程序将通过shell来执行。

参数env是字典类型，用于指定子进程的环境变量。如果env = None，子进程的环境变量将从父进程中继承。

* subprocess.PIPE
在创建Popen对象时，subprocess.PIPE可以初始化stdin, stdout或stderr参数。表示与子进程通信的标准流。

* subprocess.STDOUT
创建Popen对象时，用于初始化stderr参数，表示将错误通过标准输出流输出。

#####Popen的方法：

* Popen.poll()
　　用于检查子进程是否已经结束。设置并返回returncode属性。

* Popen.wait()
　　等待子进程结束。设置并返回returncode属性。

* Popen.communicate(input=None)
　　与子进程进行交互。向stdin发送数据，或从stdout和stderr中读取数据。可选参数input指定发送到子进程的参数。Communicate()返回一个元组：(stdoutdata, stderrdata)。注意：如果希望通过进程的stdin向其发送数据，在创建Popen对象的时候，参数stdin必须被设置为PIPE。同样，如果希望从stdout和stderr获取数据，必须将stdout和stderr设置为PIPE。

* Popen.send_signal(signal)
　　向子进程发送信号。

* Popen.terminate()
　　停止(stop)子进程。在windows平台下，该方法将调用Windows API TerminateProcess（）来结束子进程。

* Popen.kill()
　　杀死子进程。

* Popen.stdin，Popen.stdout ，Popen.stderr ，官方文档上这么说：
stdin, stdout and stderr specify the executed programs’ standard input, standard output and standard error file handles, respectively. Valid values are PIPE, an existing file descriptor (a positive integer), an existing file object, and None.

* Popen.pid
　　获取子进程的进程ID。

* Popen.returncode
　　获取进程的返回值。如果进程还没有结束，返回None。

######简单用法：
```python
p = subprocess.Popen('dir',shell=True)
p.wait()
```
shell参数根据你要执行的命令的情况来决定，上面是dir命令，就一定要shell=True了，p.wait()可以得到命令的返回值。

如果上面写成a=p.wait()，a就是returncode。那么输出a的话，有可能就是0【表示执行成功】。


######进程间通讯
如果想得到进程的输出，管道是个很好的方法，这样：
```python
p = subprocess.Popen("dir",shell=True,stdout=subprocess.PIPE,stderr = subprocess.PIPE)
(stdoutput,erroroutput) = p.communicate()
```
p.communicate会一直等到进程退出，并将标准输出和标准错误输出返回，这样就可以得到紫禁城的输出了。
下面是一个带输入的communicate的例子：
```python
p = subprocess.Popen('ls',shell=True,stdout=subprocess.PIPE)
stdoutput,erroutput = p.communicate('/home/zxsted')
print stdoutput[0]
print erroutput
```
上面的例子通过communicate给stdin发送数据，然后使用一个tuple接受命令的执行结果。

######合并输出
上面，标准输出和错误输出是分开的，也可以合并起来，只需要将stderr参数设置为subprocess.STDOUT就可以了，这样：
```python
p = subprocess.Popen('dir' ,shell=True,stdout = subprocess.PIPE,stderr=subprocessSTDOUT)
(stdoutput,erroutput) = p.communicate()
```
一行一行的处理子进程的输出：
```python
p = subprocess.Popen("dir",shell=True,stdout=subprocess.PIPE,stderr = subprocess.STDOUT)
while True:
	buff = p.stdout.readline()
    if buff == '' and p.poll() != None:
    	break
```

######死锁
但是如果你使用了管道，而又不去处理管道的输出，那么，如果子进程输出数据过多，死锁就会发生了，比如下面的用法：
```python
p = subprocess.Popen('loginprint',shell=True,stdout=subprocess.PIPE,stderr = subprocess.STDOUT)
p.wait()
```
longprint是一个假想的有大量输出的进程，那么在我的xp, Python2.5的环境下，当输出达到4096时，死锁就发生了。当然，如果我们用p.stdout.readline或者p.communicate去清理输出，那么无论输出多少，死锁都是不会发生的。或者我们不使用管道，比如不做重定向，或者重定向到文件，也都是可以避免死锁的。


######连接多个命令
subprocess还可以连接起来多个命令来执行。
在shell中我们知道，想要连接多个命令可以使用管道。
在subprocess中，可以使用上一个命令执行的输出结果作为下一次执行的输入。例子如下：
```python
p1 = subprocess.Popen('cat ff',shell = True,stdout=subprocess.PIPE)
p2 = subprocess.Popen('tail -2',shell=True,stdin=p1.stdout,stdout=subprocess.PIPE)
print p2.stdout
print p2.stdout.read()
```
上面的例子中，p2使用了第一次执行命令的结果P1的stdout作为输入数据，然后执行tail。

######完整的例子：
下面的例子是用来ping一系列的IP地址，并输出是否这些地址的主机是alive的
```python
#!/usr/bin/env python
#-*-encoding:utf-8-*-

from threading import Thread
import subprocess
from Queue import Queue

num_threads = 3
ips=['127.0.0.1',]

q = Queue()

def pingme(i,queue):
	while 1:
    	ip = queue.get()
        print 'Thread %s pinging %s' % (i,ip)
        ret = subprocess.call('ping -c 1 %s' % ip, shell = True,stdout=open('/dev/null','w'),stderr = subprocess.STDOUT)
        if ret == 0:
        	print '%s is alive' % ip
        elif ret == 1:
        	print '%s is down...' % ip
        queue.task_sone()
        
#start num_threads threads
for i in range(num_threads):
	t = Thread(target=pingme,args=(i,q))
    t.setDaemon(True)
    t.start()
    
for ip in ips:
	q.put(ip)
print 'main thread waiting...'
q.join();
print 'Done'
```
在上面代码中使用subprocess的主要好处是，使用多个线程来执行ping命令会节省大量时间。

假设说我们用一个线程来处理，那么每个 ping都要等待前一个结束之后再ping其他地址。那么如果有100个地址，一共需要的时间=100*平均时间。

如果使用多个线程，那么最长执行时间的线程就是整个程序运行的总时间。【时间比单个线程节省多了】

这里要注意一下Queue模块的学习。

pingme函数的执行是这样的：

启动的线程会去执行pingme函数。

pingme函数会检测队列中是否有元素。如果有的话，则取出并执行ping命令。

这个队列是多个线程共享的。所以这里我们不使用列表。【假设在这里我们使用列表，那么需要我们自己来进行同步控制。Queue本身已经通过信号量做了同步控制，节省了我们自己做同步控制的工作=。=】

代码中q的join函数是阻塞当前线程。下面是e文注释

　Queue.join()

　　Blocks until all items in the queue have been gotten and processed(task_done()).

---------------------------------------------

学习Processing模块的时候，遇到了进程的join函数。进程的join函数意思说，等待进程运行结束。与这里的Queue的join有异曲同工之妙啊。processing模块学习的文章


######[三、方便的函数][1]
1、subprocess.call
subprocess.call (*popenargs , **kwargs )
执行命令，并等待命令结束，再返回子进程的返回值。参数同Popen，查看/usr/lib/python2.7/subprocess.py 
去掉文档，其实是这样的：
```python
def call(*popenargs, **kwargs):
    return Popen(*popenargs, **kwargs).wait()
>>> subprocess.call('ifconfig',shell=True)
```
2、subprocess.check_call
subprocess.check_call (*popenargs , **kwargs )
执行上面的call命令，并检查返回值，如果子进程返回非0，则会抛出CalledProcessError异常，这个异常会有个returncode 
属性，记录子进程的返回值。
```python
def check_call(*popenargs, **kwargs):
    retcode = call(*popenargs, **kwargs)
    if retcode:
        cmd = kwargs.get("args")
        raise CalledProcessError(retcode, cmd)
    return 0
>>> subprocess.check_call('ifconfig')  
>>> subprocess.call('noifconfig')
Traceback (most recent call last):
  File "<stdin>", line 1, in <module>
  File "/usr/local/lib/python2.7/subprocess.py", line 493, in call
    return Popen(*popenargs, **kwargs).wait()
  File "/usr/local/lib/python2.7/subprocess.py", line 679, in __init__
    errread, errwrite)
  File "/usr/local/lib/python2.7/subprocess.py", line 1228, in _execute_child
    raise child_exception
OSError: [Errno 2] No such file or directory
```
异常子进程里抛出的异常，会在父进程中再次抛出。并且，异常会有个叫child_traceback的额外属性，这是个包含子进程错误traceback
信息的字符串。遇到最多的错误回是 OSError，比如执行了一个并不存在的子程序就会产生OSError。另外，如果使用错误的参数调用Popen
，会抛出ValueError。当子程序返回非0时，check_call()还会产生CalledProcessError 异常。
安全性
不像其他的popen函数，本函数不会调用/bin/sh来解释命令，也就是说，命令中的每一个字符都会被安全地传递到子进程里。

3、check_output 
```python
check_output()执行程序，并返回其标准输出.
def check_output(*popenargs, **kwargs):
    process = Popen(*popenargs, stdout=PIPE, **kwargs)
    output, unused_err = process.communicate()
    retcode = process.poll()
    if retcode:
        cmd = kwargs.get("args")
        raise CalledProcessError(retcode, cmd, output=output)
    return output
p=subprocess.check_output('ifconfig')
```
结果是所有行/n分割的一个字符串
可以直接print出来 















[0]:http://blog.csdn.net/imzoer/article/details/8678029
[1]:http://blog.chinaunix.net/uid-26990529-id-3390814.html