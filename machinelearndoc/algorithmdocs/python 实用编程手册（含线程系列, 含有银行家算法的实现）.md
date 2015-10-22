[教程系列地址][0]

[toc]

##### logging模块

######背景介绍
学一门新技术或者新语言，我们都要首先学会如何去适应这们新技术，其中在适应过程中，我们必须得学习如何调试程序并打出相应的log信息来，正所谓“只要log打的好，没有bug解不了”，在我们熟知的一些信息技术中，log4xxx系列以及开发Android app时的android.util.Log包等等都是为了开发者更好的得到log信息服务的。在Python这门语言中，我们同样可以根据自己的程序需要打出log。

log信息不同于使用打桩法打印一定的标记信息，log可以根据程序需要而分出不同的log级别，比如info、debug、warn等等级别的信息，只要实时控制log级别开关就可以为开发人员提供更好的log信息，与log4xx类似，logger，handler和日志消息的调用可以有具体的日志级别（Level），只有在日志消息的级别大于logger和handler的设定的级别，才会显示。下面我就来谈谈我在Python中使用的logging模块一些方法。

###### logging 模块介绍
Python的logging模块提供了通用的日志系统，熟练使用logging模块可以方便开发者开发第三方模块或者是自己的Python应用。同样这个模块提供不同的日志级别，并可以采用不同的方式记录日志，比如文件，HTTP、GET/POST，SMTP，Socket等，甚至可以自己实现具体的日志记录方式。下文我将主要介绍如何使用文件方式记录log。

logging模块包括logger，handler，filter，formatter这四个基本概念。

1. logger:
 提供日志接口，供应用代码使用。logger最长用的操作有两类：配置和发送日志消息。可以通过logging.getLogger(name)获取logger对象，如果不指定name则返回root对象，多次使用相同的name调用getLogger方法返回同一个logger对象。

2. handler
将日志记录（log record）发送到合适的目的地（destination），比如文件，socket等。一个logger对象可以通过addHandler方法添加0到多个handler，每个handler又可以定义不同日志级别，以实现日志分级过滤显示。

3. filter
提供一种优雅的方式决定一个日志记录是否发送到handler。

4. formatter
 指定日志记录输出的具体格式。formatter的构造方法需要两个参数：消息的格式字符串和日期字符串，这两个参数都是可选的。
 
###### 基本使用方法

一些小型的程序我们不需要构造太复杂的log系统，可以直接使用logging模块的basicConfig函数即可，代码如下
```python
import logging

log_file="./basic_logger.log"

logging.basicConfig(filename=log_file,level=logging.DEBUG)

logging.debug("this is a debug msg!")
logging.info("this is a info msg!")
logging.warn("this is a warn msg!")
logging.error("this is a error msg！")
logging.error("this is a critical msg!")
```
运行程序时我们就会在该文件的当前目录下发现basic_logger.log文件，查看basic_logger.log内容如下：
```shell
INFO:root:this is a info msg!
DEBUG:root:this is a debug msg!
WARNING:root:this is a warn msg!
ERROR:root:this is a error msg!
CRITICAL:root:this is a critical msg!
```

需要说明的是我将level设定为DEBUG级别，所以log日志中只显示了包含该级别及该级别以上的log信息。信息级别依次是：notset、debug、info、warn、error、critical。如果在多个模块中使用这个配置的话，只需在主模块中配置即可，其他模块会有相同的使用效果。

###### 较高级的版本
上述的基础使用比较简单，没有显示出logging模块的厉害，适合小程序用，现在我介绍一个较高级版本的代码，我们需要依次设置logger、handler、formatter等配置。
```python
import logging

log_file="./nomal_logger.log"
log_level=logging.DEBUG

logger=logging.getLogger("loggingmodule.NomalLogger")
handler=logging.FileHandler(log_file)
format=logging.Formatter("[%(levelname)s][%(funcName)s][%(asctime)s]%(message)s")

handler.setFormat(formatter)
logger.addHandler(handler)
logger.setLevel(log_level)

#test
logging.debug("this is a debug msg!")
logging.info("this is a info msg!")
logging.warn("this is a warn msg!")
logging.error("this is a error msg！")
logging.error("this is a critical msg!")
```

日志文件内容
```shell
[DEBUG][][2012-08-12 17:43:59,295]this is a debug msg!
[INFO][][2012-08-12 17:43:59,295]this is a info msg!
[WARNING][][2012-08-12 17:43:59,295]this is a warn msg!
[ERROR][][2012-08-12 17:43:59,295]this is a error msg!
[CRITICAL][][2012-08-12 17:43:59,295]this is a critical msg!
```
这个对照前面介绍的logging模块，不难理解，下面的最终版本将会更加完整。

###### 完善版本
这个最终版本我用singleton设计模式来写一个Logger类，代码如下：

```python
import logging.handlers

class FinalLogger:

	logger=None
    
    levels={
    "n":logging.NOTEST,
    "d":logging.DEBUG,
    "i":logging.INFO,
    "w":logging.WARN,
    "e":logging.ERROR,
    "c":logging.CRITICAL}
    
   	log_level="d"
    log_file="final_logger.log"
    log_max_byte=10*1024*1024
    log_backup_count=5
    
    @staticmethod
    def getLogger():
    	if FinalLogger.logger is not None:
        	return FinalLogger.logger
            
        FinalLogger.logger=logging.Logger("oggingmodule.FinalLOgger")
        log_handler=logging.handlers.RotatingFileHandler(filename=FinalLogger.log_file \ maxBytes=FinalLogger.log_max_byte \ backupCount=FinalLogger.log_backup_count)
        log_fmt=logging.Formatter("%(levelname)s[%(funcName)s[%(asctime)s]%(message)s]")
        log_handler.setFormatter(log_handler)
        FinalLogger.logger.addHanler(log_handler)
        FileLogger.logger.setLevel(FinalLogger.levels.get(FinalLogger.log_level))
        return FinalLogger.logger
        
        
if __name__ == "__main__":
	logger=FinalLogger.getLogger()
    logger.debug("this is a debug msg!)
    logging.info("this is a info msg!")
	logging.warn("this is a warn msg!")
	logging.error("this is a error msg！")
	logging.error("this is a critical msg!")
```
输出：
```
logging.info("this is a info msg!")
logging.warn("this is a warn msg!")
logging.error("this is a error msg！")
logging.error("this is a critical msg!")
```
这个final版本，也是我一直用的，读者朋友也可以再加上其他的一些Handler，比如StreamHandler等等来获取更多的log信息，当然也可以将你的log信息通过配置文件来完成。


##### dom 模块生成xml文件

在Python中解析XML文件也有Dom和Sax两种方式，这里先介绍如何是使用Dom解析XML，这一篇文章是Dom生成XML文件，下一篇文章再继续介绍Dom解析XML文件。

在生成XML文件中，我们主要使用下面的方法来完成。

###### 主要方法
1. 生成XML节点(node)
```python
createElement("node_name")
```

2. 给节点添加属性值（Attribute）
```python
node.setAttribute("att_name","arr_value")
```

3. 节点的标签值（data）
```python
createTextNode("node_value")
```

4. 添加到指定节点的下面
```python
prev_node.appendChild(cur_node)
```
这里的prev_node要添加节点的上一层节点，而cur_node 即为当前要添加的节点。

###### 代码演示
```python
import xml.dom.minidom as Dom

if __name__ == "__main__":
	doc = Dom.Document()
    root_node = doc.createElement("book_store")
    root_node.setAttribute("name", "newhua")  
    root_node.setAttribute("website", "http://www.ourunix.org")  
    doc.appendChild(root_node)  
    
    book_node = doc.createElement("book1")  
 
    book_name_node = doc.createElement("name")  
    book_name_value = doc.createTextNode("hamlet")  
    book_name_node.appendChild(book_name_value)  
    book_node.appendChild(book_name_node)  
 
    book_author_node = doc.createElement("author")  
    book_author_value = doc.createTextNode("William Shakespeare")  
    book_author_node.appendChild(book_author_value)  
    book_node.appendChild(book_author_node)  
 
    root_node.appendChild(book_node)  
    
    f=open("book_store.xml","w")
    f.write(doc.toprettyxml(indent="\t",newl="\n",encoding="utf-8"))
    f.close()
    
```

该代码在当前目录下生成一个book_store.xml文件，如下
```shell
<?xml version="1.0" encoding="utf-8"?>  
<book_store name="newhua" website="http://www.ourunix.org">  
    <book1>  
        <name>hamlet</name>  
        <author>William Shakespeare</author>  
    </book1>  
</book_store>
```

当然一旦你掌握了这些基本方法之后，我们可以用一个类来更好的完成，这个类我们称之为XMLGenerator，代码如下：

```python

import xml.dom.minidom as Dom  
 
class XMLGenerator:  
    def __init__(self, xml_name):  
        self.doc = Dom.Document()  
        self.xml_name = xml_name  
 
    def createNode(self, node_name):  
        return self.doc.createElement(node_name)  
 
    def addNode(self, node, prev_node = None):  
        cur_node = node  
        if prev_node is not None:  
            prev_node.appendChild(cur_node)  
        else:  
            self.doc.appendChild(cur_node)  
        return cur_node  
 
    def setNodeAttr(self, node, att_name, value):  
        cur_node = node  
        cur_node.setAttribute(att_name, value)  
 
    def setNodeValue(self, cur_node, value):  
        node_data = self.doc.createTextNode(value)  
        cur_node.appendChild(node_data)  
 
    def genXml(self):  
        f = open(self.xml_name, "w")  
        f.write(self.doc.toprettyxml(indent = "\t", newl = "\n", encoding = "utf-8"))  
        f.close()  
 
if __name__ == "__main__":  
    myXMLGenerator = XMLGenerator("book_store.xml")  
 
    #xml root node  
    node_book_store = myXMLGenerator.createNode("book_store")  
    myXMLGenerator.setNodeAttr(node_book_store, "name", "new hua")  
    myXMLGenerator.setNodeAttr(node_book_store, "website", "http://www.ourunix.org")  
    myXMLGenerator.addNode(node = node_book_store)  
 
    #book01  
    node_book_01 = myXMLGenerator.createNode("book")  
 
    node_book_01_name = myXMLGenerator.createNode("name")  
    myXMLGenerator.setNodeValue(node_book_01_name, "Hamlet")  
    myXMLGenerator.addNode(node_book_01_name, node_book_01)  
 
    node_book_01_author = myXMLGenerator.createNode("author")  
    myXMLGenerator.setNodeValue(node_book_01_author, "William Shakespeare")  
    myXMLGenerator.addNode(node_book_01_author, node_book_01)  
 
    node_book_01_price = myXMLGenerator.createNode("price")  
    myXMLGenerator.setNodeValue(node_book_01_price, "$20")  
    myXMLGenerator.addNode(node_book_01_price, node_book_01)  
 
    node_book_01_grade = myXMLGenerator.createNode("grade")  
    myXMLGenerator.setNodeValue(node_book_01_grade, "good")  
    myXMLGenerator.addNode(node_book_01_grade, node_book_01)  
 
    myXMLGenerator.addNode(node_book_01, node_book_store)  
 
    #book 02  
    node_book_02 = myXMLGenerator.createNode("book")  
 
    node_book_02_name = myXMLGenerator.createNode("name")  
    myXMLGenerator.setNodeValue(node_book_02_name, "shuihu")  
    myXMLGenerator.addNode(node_book_02_name, node_book_02)  
 
    node_book_02_author = myXMLGenerator.createNode("author")  
    myXMLGenerator.setNodeValue(node_book_02_author, "naian shi")  
    myXMLGenerator.addNode(node_book_02_author, node_book_02)  
 
    node_book_02_price = myXMLGenerator.createNode("price")  
    myXMLGenerator.setNodeValue(node_book_02_price, "$200")  
    myXMLGenerator.addNode(node_book_02_price, node_book_02)  
 
    node_book_02_grade = myXMLGenerator.createNode("grade")  
    myXMLGenerator.setNodeValue(node_book_02_grade, "good")  
    myXMLGenerator.addNode(node_book_02_grade, node_book_02)  
 
    myXMLGenerator.addNode(node_book_02, node_book_store)  
 
    #gen  
    myXMLGenerator.genXml()
```

同样这个方法会在本目录下生成一个book_store.xml文件，如下：
```shell
<?xml version="1.0" encoding="utf-8"?>  
<book_store name="new hua" website="http://www.ourunix.org">  
    <book>  
        <name>Hamlet</name>  
        <author>William Shakespeare</author>  
        <price>$20</price>  
        <grade>good</grade>  
    </book>  
    <book>  
        <name>shuihu</name>  
        <author>naian shi</author>  
        <price>$200</price>  
        <grade>good</grade>  
    </book>  
</book_store>
```

这个版本算一个稍微高级的版本，但由于时间有限还很显得粗糙，读者可以发挥出更好的。


##### 使用dom模块解析XML文件

这一篇文章接着前一篇来接续讲解如何使用Dom方式操作XML数据，这一篇文章主要介绍如何解析（parse）XML文件，本文实例XML文件是上一篇的生成的文件，我们看看能不能完整的读出来，这个XML文件内容如下：

```shell
<?xml version="1.0" encoding="utf-8"?>  
<book_store name="new hua" website="http://www.ourunix.org">  
    <book>  
        <name>Hamlet</name>  
        <author>William Shakespeare</author>  
        <price>$20</price>  
        <grade>good</grade>  
    </book>  
    <book>  
        <name>shuihu</name>  
        <author>naian shi</author>  
        <price>$200</price>  
        <grade>good</grade>  
    </book>  
</book_store>
```

###### 主要方法

1. 加载读取XML文件
```python
minidom.parse(filename)
```

2. 获取XML文档对象
```python
doc.docnumentElement
```

3. 获取XML节点属性
```python
node.getAttribute(AttributeName)
```

4. 获取XML节点对象集合
```python
node.getElementsByTagName(TagName)
```

5. 获取 XML 节点值
```python
node.childNodes[index].nodeValue
```

###### 代码演示

同样先用一个简单版本来演示下如何使用Dom解析XML文件，代码如下：

```python
import xml.dom.minidom as Dom
import sys

if __name__ == "__main__":
	try:
    	xml_file=Dom.parse("./book_store.xml")
    except Exception ,e:
    	print e
        sys.exit()
    node_root=xml_file.documentElement
    name=node_root.getAttribute("name")
    website=node_root.getAttribute("website")
    print "name of book store: %s \n website of book store:%s" %(name,website)
    
    node_book_list=node_root.getElementsByTagName("book")
    for book_node in node_book_list:
    	book_name_node = book_node.getElementsBytagName("name")[0]
         book_name_value = book_name_node.childNodes[0].data  
 
        book_author_node = book_node.getElementsByTagName("author")[0]  
        book_author_value = book_author_node.childNodes[0].data  
 
        book_price_node = book_node.getElementsByTagName("price")[0]  
        book_price_value = book_price_node.childNodes[0].data  
 
        book_grade_node = book_node.getElementsByTagName("grade")[0]  
        book_grade_value = book_grade_node.childNodes[0].data  
 
        print "book: %s\t author: %s\t price: %s\t grade: %s\t" %(book_name_value, book_author_value, book_price_value, book_grade_value)
```
运行结果如下：
```shell
name of book store: new hua
website of book store: http://www.ourunix.org
book: Hamlet  author: William Shakespeare  price: $20  grade: good
book: shuihu  author: naian shi  price: $200  grade: good
```

同样接着来一个所谓的高级版本：

```python
import xml.dom.minidom as Dom  
import sys  
 
class XMLParser:  
    def __init__(self, xml_file_path):  
        try:  
            self.xml = Dom.parse(xml_file_path)  
        except:  
            sys.exit()  
        self.book_list = list()  
 
    def getNodeName(self, prev_node, node_name):  
        return prev_node.getElementsByTagName(node_name)  
 
    def getNodeAttr(self, node, att_name):  
        return node.getAttribute(att_name)  
 
    def getNodeValue(self, node):  
        return node.childNodes[0].data.encode("utf-8")  
 
    def parse(self):  
        node_root = self.xml.documentElement  
        print "store: %s, website: %s" %(self.getNodeAttr(node_root, "name"), \  
                                     self.getNodeAttr(node_root, "website"))  
 
        node_book_list = self.getNodeName(node_root, "book")  
 
        for node_book in node_book_list:  
            book_info = dict()  
            node_book_name = self.getNodeName(node_book, "name")[0]  
            book_name_value = self.getNodeValue(node_book_name)  
            book_info["name"] = book_name_value  
 
            node_book_author = self.getNodeName(node_book, "author")[0]  
            book_author_value = self.getNodeValue(node_book_author)  
            book_info["author"] = book_author_value  
 
            node_book_price = self.getNodeName(node_book, "price")[0]  
            book_price_value = self.getNodeValue(node_book_price)  
            book_info["price"] = book_price_value  
 
            node_book_grade = self.getNodeName(node_book, "grade")[0]  
            book_garde_value = self.getNodeValue(node_book_grade)  
            book_info["grade"] = book_garde_value  
 
            self.book_list.append(book_info)  
 
    def getBookList(self):  
        return self.book_list  
 
if __name__ == "__main__":  
    myXMLParser = XMLParser("book_store.xml")  
    myXMLParser.parse()  
    print myXMLParser.getBookList()
```


##### 使用threading模块实现多线程编程一[综述]

Python这门解释性语言也有专门的线程模型，Python虚拟机使用GIL（Global Interpreter Lock，全局解释器锁）来互斥线程对共享资源的访问，但暂时无法利用多处理器的优势。在Python中我们主要是通过thread和 threading这两个模块来实现的，其中Python的threading模块是对thread做了一些包装的，可以更加方便的被使用，所以我们使用 threading模块实现多线程编程。这篇文章我们主要来看看Python对多线程编程的支持。

在语言层面，Python对多线程提供了很好的支持，可以方便地支持创建线程、互斥锁、信号量、同步等特性。下面就是官网上介绍threading模块的基本资料及功能：

###### 实现模块

thread:多线程的底层支持模块，一般不建议使用
threading： 对thread 进行了封装， 建议写线程的操作对象化

1. threading模块

Thread 线程类，这是我们用的最多的一个类，你可以指定线程函数执行或者继承自它都可以实现子线程功能；

Timer与Thread类似，但要等待一段时间后才开始运行；
Lock 锁原语，这个我们可以对全局变量互斥时使用；
RLock 可重入锁，使单线程可以再次获得已经获得的锁；
Condition 条件变量，能让一个线程停下来，等待其他线程满足某个“条件”；
Event 通用的条件变量。多个线程可以等待某个事件发生，在事件发生后，所有的线程都被激活；
Semaphore为等待锁的线程提供一个类似“等候室”的结构；
BoundedSemaphore 与semaphore类似，但不允许超过初始值；
Queue：实现了多生产者（Producer）、多消费者（Consumer）的队列，支持锁原语，能够在多个线程之间提供很好的同步支持。

2. Thread 类

是你主要的线程类，可以创建进程实例。该类提供的函数包括：
getName(self) 返回线程的名字
isAlive(self) 布尔标志，表示这个线程是否还在运行中
isDaemon(self) 返回线程的daemon标志
join(self, timeout=None) 程序挂起，直到线程结束，如果给出timeout，则最多阻塞timeout秒
run(self) 定义线程的功能函数
setDaemon(self, daemonic) 把线程的daemon标志设为daemonic
setName(self, name) 设置线程的名字
start(self) 开始线程执行

Queue提供的类

Queue队列
LifoQueue后入先出（LIFO）队列
PriorityQueue 优先队列
接下来

接下来的一系列文章，将会用一个一个示例来展示threading的各个功能，包括但不限于：两种方式起线程、threading.Thread类的重要函数、使用Lock互斥及RLock实现重入锁、使用Condition实现生产者和消费者模型、使用Event和Semaphore多线程通信


##### 使用threading模块实现多线程编程二[两种方式起线程]

在Python中我们主要是通过thread和threading这两个模块来实现的，其中Python的threading模块是对thread做了一些包装的，可以更加方便的被使用，所以我们使用threading模块实现多线程编程。一般来说，使用线程有两种模式，一种是创建线程要执行的函数，把这个函数传递进Thread对象里，让它来执行；另一种是直接从Thread继承，创建一个新的class，把线程执行的代码放到这个新的 class里。

###### 将函数传递进Thread  对象
```python
import threading

def thread_fun(num):
	for n in range(0,int(num)):
    	print "I com from %s,num:%s"%(threading.currentThread().getName(),n)
        
def main(thread_num):
	thread_list=list()
    
    # 创建线程对象
    for i in range(0,threadnum):
    	thread_name="thread_%s" %i
        thread_list.append(threading.Thread(target=Thread_fun,name=Thread_name,args=(20,)))
        
    # 启动所有线程
    for thread in thread_list:
    	thread.start()
        
    # 主线程中等待所有子线程退出
    
    for thread in thread_list:
    	thread.join()
        
if __name__ == "__main__":
	main(3)
```

程序启动了3个线程，并且打印了每一个线程的线程名字，这个比较简单吧，处理重复任务就派出用场了，下面介绍使用继承threading的方式；

###### 集成自 threading.Thread 类

```python
import threading

class MyThread(threading.Thread):

	def __init__(self):
    	threading.Thread.__init__(self)
        
    def run(self):
    	print "I am %s" % self.name
        
if __name__ == "__main__":
	for thread in range(0,5):
    	t = MyThread()
        t.start()
```
接下来的文章，将会介绍如何控制这些线程，包括子线程的退出，子线程是否存活及将子线程设置为守护线程(Daemon)。


##### 使用threading模块实现多线程编程三[threading.Thread类的重要函数]

这篇文章主要介绍threading模块中的主类Thread的一些主要方法，实例代码如下：
```python
import threading  
 
class MyThread(threading.Thread):  
    def __init__(self):  
        threading.Thread.__init__(self)  
 
    def run(self):  
        print "I am %s" % (self.name)  
 
if __name__ == "__main__":  
    for i in range(0, 5):  
        my_thread = MyThread()  
        my_thread.start()
```

###### name相关

你可以为每一个thread指定name，默认的是Thread-No形式的，如上述实例代码打印出的一样：

```shell
I am Thread-1
I am Thread-2
I am Thread-3
I am Thread-4
I am Thread-5
```
当然你可以指定每一个thread的name，这个通过setName方法，代码：

```shell
def __init__(self):  
    threading.Thread.__init__(self)  
    self.setName("new" + self.name)
```

###### join 方法

join方法原型如下，这个方法是用来阻塞当前上下文，直至该线程运行结束：
```python
def join(self, timeout=None):  
        timeout可以设置超时
timeout可以设置超时蚕食
```

###### setDaemon 方法
当我们在程序运行中，执行一个主线程，如果主线程又创建一个子线程，主线程和子线程就分兵两路，当主线程完成想退出时，会检验子线程是否完成。如果子线程未完成，则主线程会等待子线程完成后再退出。但是有时候我们需要的是，只要主线程完成了，不管子线程是否完成，都要和主线程一起退出，这时就可以用setDaemon方法，并设置其参数为True。

当然这上面列举的只是我们在编程是经常使用到的方法，更多方法，可以参见：Higher-level threading interface

##### 使用threading模块实现多线程编程四[使用Lock互斥锁]

前面已经演示了Python：使用threading模块实现多线程编程二两种方式起线程和Python：使用threading模块实现多线程编程三threading.Thread类的重要函数，这两篇文章的示例都是演示了互不相干的独立线程，现在我们考虑这样一个问题：假设各个线程需要访问同一公共资源，我们的代码该怎么写？

```python
import threading  
import time  
 
counter = 0  
 
class MyThread(threading.Thread):  
    def __init__(self):  
        threading.Thread.__init__(self)  
 
    def run(self):  
        global counter  
        time.sleep(1);  
        counter += 1  
        print "I am %s, set counter:%s" % (self.name, counter)  
 
if __name__ == "__main__":  
    for i in range(0, 200):  
        my_thread = MyThread()  
        my_thread.start()
```

解决上面的问题，我们兴许会写出这样的代码，我们假设跑200个线程，但是这200个线程都会去访问counter这个公共资源，并对该资源进行处理(counter += 1)，代码看起来就是这个样了，但是我们看下运行结果：

```shell
I am Thread-69, set counter:64
I am Thread-73, set counter:66I am Thread-74, set counter:67I am Thread-75, set counter:68I am Thread-76, set counter:69I am Thread-78, set counter:70I am Thread-77, set counter:71I am Thread-58, set counter:72I am Thread-60, set counter:73I am Thread-62, set counter:74I am Thread-66,set counter:75I am Thread-70, set counter:76I am Thread-72, set counter:77I am Thread-79, set counter:78I am Thread-71, set counter:78
```
打印结果我只贴了一部分，从中我们已经看出了这个全局资源(counter)被抢占的情况，问题产生的原因就是没有控制多个线程对同一资源的访问，对数据造成破坏，使得线程运行的结果不可预期。这种现象称为“线程不安全”。在开发过程中我们必须要避免这种情况，那怎么避免？这就用到了我们在综述中提到的互斥锁了。

###### 互斥锁

Python编程中，引入了对象互斥锁的概念，来保证共享数据操作的完整性。每个对象都对应于一个可称为” 互斥锁” 的标记，这个标记用来保证在任一时刻，只能有一个线程访问该对象。在Python中我们使用threading模块提供的Lock类。

我们对上面的程序进行整改，为此我们需要添加一个互斥锁变量mutex = threading.Lock()，然后在争夺资源的时候之前我们会先抢占这把锁mutex.acquire()，对资源使用完成之后我们在释放这把锁mutex.release()。代码如下：

```shell

import threading
import time

counter=0
mutex=threading.Lock()

class MyThread(threading.Thread):
	def __init__(self):
    	threading.Thread.__init__(self)
        
    def run(self):
    	global counter,mutex
        time.sleep(1)
        if mutex.acquire():
        	counter+=1
            print "I am %s,set counter:%s" % (self.name,counter)
            mutex.release()
            
if __name__ == "__main__":
	for i in range(0,100):
    	my_thread = Mythread()
        my_thread.start()
```

###### 同步阻塞
当一个线程调用Lock对象的acquire()方法获得锁时，这把锁就进入“locked”状态。因为每次只有一个线程1可以获得锁，所以如果此时另一个线程2试图获得这个锁，该线程2就会变为“blo同步阻塞状态。直到拥有锁的线程1调用锁的release()方法释放锁之后，该锁进入“unlocked”状态。线程调度程序从处于同步阻塞状态的线程中选择一个来获得锁，并使得该线程进入运行（running）状态。

###### 进一步考虑
通过对公共资源使用互斥锁，这样就简单的到达了我们的目的，但是如果我们又遇到下面的情况：

```shell
遇到锁嵌套的情况该怎么办，这个嵌套是指当我一个线程在获取临界资源时，又需要再次获取；
如果有多个公共资源，在线程间共享多个资源的时候，如果两个线程分别占有一部分资源并且同时等待对方的资源；
```
上述这两种情况会直接造成程序挂起，即死锁，下面我们会谈死锁及可重入锁RLock。


##### 使用threading模块实现多线程编程五[死锁的形成]

前一篇文章Python：使用threading模块实现多线程编程四[使用Lock互斥锁]我们已经开始涉及到如何使用互斥锁来保护我们的公共资源了，现在考虑下面的情况–

如果有多个公共资源，在线程间共享多个资源的时候，如果两个线程分别占有一部分资源并且同时等待对方的资源，这会引起什么问题？

###### 死锁的概念

所谓死锁： 是指两个或两个以上的进程在执行过程中，因争夺资源而造成的一种互相等待的现象，若无外力作用，它们都将无法推进下去。此时称系统处于死锁状态或系统产生了死锁，这些永远在互相等待的进程称为死锁进程。 由于资源占用是互斥的，当某个进程提出申请资源后，使得有关进程在无外力协助下，永远分配不到必需的资源而无法继续运行，这就产生了一种特殊现象死锁。

```python
import threading

countA,countB = 0,0

mutexA,mutexB = threading.Lock(),threading.Lock()

class MyThread(threading.Thread):
	def __init__(self):
    	threading.Thread.__init__(self)
        
    def run(self):
    	self.fun1()
        self.fun2()
        
    def fun1(self):
    	global mutexA,mutexB
        if mutexA.accquire():
        	print "I am %s,get res: %s" %(self.name,"ResA")
            
            if mutexB.acquir():
            	print "I an %s,get res : %s" % (self.name,"ResB")
                mutexB.release()
                
        mutexA.release()
        
    def fun2(self):
    	global mutexA,mutexB
        if mutexB.acquire():
        	print "I am %s,get res: %s" % (self.name,"ResB")
            
            if mutexA.acquire():
            	print "I am %s,get res : %s" % (self.name,"ResA")
            	mutexA.release()
         mutexB.release()
         
if __name__ == "__main__":
	for i in range(0,100):
    	my_thread = MyThread()
        my_thread.start()
           
```
代码中展示了一个线程的两个功能函数分别在获取了一个竞争资源之后再次获取另外的竞争资源，我们看运行结果：
```shell
I am Thread-1 , get res: ResA
I am Thread-1 , get res: ResB
I am Thread-2 , get res: ResAI am Thread-1 , get res: ResB
```
可以看到，程序已经挂起在那儿了，这种现象我们就称之为”死锁“。


###### 避免死锁

避免死锁主要方法就是：正确有序的分配资源，避免死锁算法中最有代表性的算法是Dijkstra E.W 于1968年提出的银行家算法。


##### 使用threading模块实现多线程编程六[可重入锁RLock]

考虑这种情况：如果一个线程遇到锁嵌套的情况该怎么办，这个嵌套是指当我一个线程在获取临界资源时，又需要再次获取。

根据这种情况，代码如下：

```java

import threading
import time

counter=0
mutex = threading.Lock()

class MyThread(threading.Thread):
	
    def __init__(self):
    	threading.Thread.__init__(self)
        
    def run(self):
    	global counter , metux
        time.sleep(1)
        
        if mutex.acquire():  # 对称序代码第一次加锁
        	coutner += 1
            print "I am %s,set counter: %s" % (self.name,counter)
            if mutex.acquire(): # 对称序代码第二次加锁
            	counter += 1 
                print "I am %s ,set counter:%s" % (self.name,counter)
                mutex.release()
            mutex.release()
            
if  __name__ == "__main__":
	
    for i in range(0,200):
    	my_thread = MyThead()
        my_thread.start()
     	
```
代码运行的结果为：
```shell
I am Thread-1， set counter:1
```

之后就直接挂起了，这种情况形成了最简单的死锁。

那有没有一种情况可以在某一个线程使用互斥锁访问某一个竞争资源时，可以再次获取呢？在Python中为了支持在同一线程中多次请求同一资源，python提供了“可重入锁”：threading.RLock。这个RLock内部维护着一个Lock和一个counter变量，counter记录了acquire的次数，从而使得资源可以被多次require。直到一个线程所有的acquire都被release，其他的线程才能获得资源。上面的例子如果使用RLock代替Lock，则不会发生死锁：

代码只需将上述的：

```java
mutex = threading.Lock()
```
替换成：
```java
mutex = threading.RLock()
```

即可



##### 用threading模块实现多线程编程七[使用Condition实现复杂同步]

目前我们已经会使用Lock去对公共资源进行互斥访问了，也探讨了同一线程可以使用RLock去重入锁，但是尽管如此我们只不过才处理了一些程序中简单的同步现象，我们甚至还不能很合理的去解决使用Lock锁带来的死锁问题。所以我们得学会使用更深层的解决同步问题。

Python提供的Condition对象提供了对复杂线程同步问题的支持。Condition被称为条件变量，除了提供与Lock类似的acquire和release方法外，还提供了wait和notify方法。

使用Condition的主要方式为：线程首先acquire一个条件变量，然后判断一些条件。如果条件不满足则wait；如果条件满足，进行一些处理改变条件后，通过notify方法通知其他线程，其他处于wait状态的线程接到通知后会重新判断条件。不断的重复这一过程，从而解决复杂的同步问题。

下面我们通过很著名的“生产者-消费者”模型来来演示下，在Python中使用Condition实现复杂同步。

```java

import threading
import time

condition=threading.Condition()   # 创建条件变量

products=0

class Producer(threading.Thread):
	
    def __init__(self):
    	threading.Thread.__init__(self)
        
    def run(self):
    	global condition,products
        while True:
        	if condition.acquire():  # 为执行逻辑加锁
            	if products < 10:
                	products += 1
                    print "Producer(%s) : deliver one ,now products:%s" % (self.name,products)
                    condition.notify()  # 创建完成，释放信号量
                else:
                	print "Producer(%s):already 10,stop deliver , now products: %s" % (self.name,products)
                    condition.wait()  # 缓存已满 ，使用信号量让生产者 等待放信号量
                condition.release()
                time.sleep(2)
                
class Consumer(threading.Thread):
	def __init__(self):
    	threading.Thread.__init__(self)
        
    def run(self):
    	global condition,products
        
        while True:
        	if condition.acquire():  # 为执行逻辑加锁
            	if products > 1:   # 如果缓冲还有 内容则消费一个
                	products -= 1
                    print "Consumer(%s):consume one, new products:%s" % (self.name,products)
                    condition.notify()  #  因为消费空出了一个空间， 那么释放信号量， 通知生产者 可以生产了。
                else：   # 如果缓冲已经没有了， 则消费线程使用信号量进行等待
                print "Consumer(%s):only 1,stop consume,products: %s" % (self.name,products)
                condition.wait()
            condition.release()   # 线程 执行方法结束， 那么释放锁
            time.sleep(2)
    
if __name__ == "__main__":
	for p in range(0,2):
    	p = Producer()
        p.start()
        
    for c in range(0,10):
    	c = Consumer()
        c.start()
```
代码中主要实现了生产者和消费者线程，双方将会围绕products来产生同步问题，首先是2个生成者生产products ，而接下来的10个消费者将会消耗products，代码运行如下：

```shell
Producer(Thread-1):deliver one, now products:1
Producer(Thread-2):deliver one, now products:2
Consumer(Thread-3):consume one, now products:1
Consumer(Thread-4):only 1, stop consume, products:1
Consumer(Thread-5):only 1, stop consume, products:1
Consumer(Thread-6):only 1, stop consume, products:1
Consumer(Thread-7):only 1, stop consume, products:1
Consumer(Thread-8):only 1, stop consume, products:1
Consumer(Thread-10):only 1, stop consume, products:1
Consumer(Thread-9):only 1, stop consume, products:1
Consumer(Thread-12):only 1, stop consume, products:1
Consumer(Thread-11):only 1, stop consume, products:1
```

另外:
1. Condition 对象的构造函数可以接受一个 Lock / RLock 对象作为参数， 如果没有指定， 则Condition对象会在内部自行创建一个RLock； 
2. 除了notify 方法外， Condition 对象还提供了notifayAll 方法，
可以通知 waitting 池中的所有线程尝试acquire 内部锁 。 由于上述机制，
处于waitting 状态的线程只能通过notify方法唤醒， 所以notifyAll的作用在于防止有线程永远处于状态。

##### 使用threading模块实现多线程编程八[使用Event实现线程间通信]

使用threading.Event可以实现线程间相互通信，之前的Python：使用threading模块实现多线程编程七[使用Condition实现复杂同步]我们已经初步实现了线程间通信的基本功能，但是更为通用的一种做法是使用threading.Event对象。使用threading.Event可以使一个线程等待其他线程的通知，我们把这个Event传递到线程对象中，Event默认内置了一个标志，初始值为False。一旦该线程通过wait()方法进入等待状态，直到另一个线程调用该Event的set()方法将内置标志设置为True时，该Event会通知所有等待状态的线程恢复运行。

```java
import threading
import time

class MyThread(threading.Thread):
	def __init__(self,signal):
    	threading.Thread.__init__(self)
        self.signal = signal
        
    def run(self):
    	print "I am %s,I will sleep ..." % self.name
        self.signal.wait()
        print "I am %s,Iawakce ..." % self.name
        
    if __name__ == "__main__":
    	signal = threading.Event()
        for t in range(0,3):
        	thread = MyThread(signal)
            thread.start()
            
        print "main thread sleep 3 seconds ..."
        
        time.sleep(3)
        
        signal.set()
```

运行效果如下：
```java
I am Thread-1,I will sleep ...
I am Thread-2,I will sleep ...
I am Thread-3,I will sleep ...
main thread sleep 3 seconds...
I am Thread-1, I awake...I am Thread-2, I awake...
 
I am Thread-3, I awake...
```

##### Event  详细介绍

Python提供了Event对象用于线程间通信，它是由线程设置的信号标志，如果信号标志位真，则其他线程等待直到信号接触。

   Event对象实现了简单的线程通信机制，它提供了设置信号，清楚信号，等待等用于实现线程间的通信。
   
1. 设置信号量
   使用Eent 的 set() 方法可以设置Event 对象内部的信号标识为真， Eent对象提供了isSet() 方法来判断其内部信号标识的状态。 当使用 event对象的set（） 方法后， isSet() 方法返回真。
   
2.  清除信号量
   使用Event 对象的clear() 方法清除Event对象内部的信号标识， 即使将其设置为假， 当使用Event的clear（） 方法后， isSet() 方法返回假
   
3. 等待
   Event 对象的wait方法只有在内部对象为真的时候才会很快的执行并返回，当Event对象的内部信号标识位为假时， 则wait方法一直等待直到其为真时才返回。
   
下面是一个具体的例子：

```python
import threading

class mythread(threading.Thread):

	def __init__(self,threadname):
    	threading.Thread.__init__(self,name=threadname)
        
    def run(self):
    	global event
        if event.isSet():   # 判断内部标记位是否为True
        	event.clear()
            event.wait()
            print self.getName()
            
        else:
        	print self.getName()
            event.set()
            
event = threading.Event()
event.set()

t1=[]
for i in range(10):
	t = mythread(str(i))
    t1.append(t)
    
for i in t1:
	t.start()
```

运行的结果为：

```shell
>>>
1
0
3
2
5
4
7
6
9
8
>>> 
```
   
##### pyton 版 银行家算法 解决 线程冲突

###### 背景介绍

在银行中，客户申请贷款的数量是有限的，每个客户在第一次申请贷款时要声明完成该项目所需的最大资金量，在满足所有贷款要求时，客户应及时归还。银行家在客户申请的贷款数量不超过自己拥有的最大值时，都应尽量满足客户的需要。在这样的描述中，银行家就好比操作系统，资金就是资源，客户就相当于要申请资源的进程。

死锁产生的必要条件：

（1）互斥条件：即一个资源每次只能被一个进程使用，在操作系统中这是真实存在的情况。

（2）保持和等待条件：有一个进程已获得了一些资源，但因请求其他资源被阻塞时，对已获得的资源保持不放。

（3）不剥夺条件：有些系统资源是不可剥夺的，当某个进程已获得这种资源后，系统不能强行收回，只能由进程使用完时自己释放。

（4）环路等待条件：若干个进程形成环形链，每个都占用对方要申请的下一个资源。

###### 解决方法 

3、死锁预防：死锁预防是采用某种策略，限制并发进程对资源的请求，破坏死锁产生的4个必要条件之一，使系统在任何时刻都不满足死锁的必要条件。

（1）预先静态分配法。破坏了“不可剥夺条件”。预先分配所需资源，保证不等待资源。该方法的问题是降低了对资源的请求，降低进程的并发程度；有时可能无法预先知道所需资源。

（2）资源有序分配法。破坏了“环路条件”。把资源分类按顺序。保证不形成环路。该方法存在的问题是限制进程对资源的请求；由于资源的排序占用系统开销。

4、死锁避免：避免是指进程在每次申请资源时判断这些操作是否安全，典型算法是“银行家算法”。但这种算法会增加系统的开销。

5、死锁检测：判断系统是否处于死锁状态，如果是，则执行死锁解除策略。

6、死锁解除：与死锁检测结合使用，它使用的方式就是剥夺。即将资源强行分配给别的进程。
操作系统的期末实验,没用c/c++。而用python试试，而且试了试用PyInstaller将python程序转化为了exe文件，pyinstaller果然很好用很方便。
程序就是输入模拟的进程数N，资源种类数M，然后程序随机初始化数据，随机产生请求，直到所有进程运行完毕。
程序写得很烂，勿怪T.T，算是联系下python吧~~~版本号是2.7.6
代码如下：

###### 算法描述

某系统有四种互斥资源R1,R2,R3和R4，可用资源数分别是3、5、6和8。假设在T0时刻有P1、P2、P3和P4四个进程，并且这些进程对资源的最大需求量和已分配资源数如下表所示，那么在T0时刻系统中R1、R2、R3和R4的剩余资源数分别为（1）。如果从T0时刻开始进程按   （2）     顺序逐个调度执行，那么系统状态是安全的。 

![][1]

（1）A.3、5、6和8          B.3、4、2和2
 C.0、1、2和1          D.0、1、0和1

（2）A.P1→P2→P4→P3      B.P2→P1→P4→P3
 C.P3→P2→P1→P4      D.P4→P2→P3→P1
     
     
求剩余资源：
用可用资源数减去那些已分配的资源数：

![][2]

R1=3-(1+0+1+1)=3-3=0

R2=5-(1+1+1+1)=5-4=1

R3=6-(2+2+1+1)=6-6=0

R4=8-(4+2+0+1)=8-7=1

所以（1）选择D。

求出还需要的资源数

![][3]


分析，因为剩余的可用资源为（0,1,0,1），与上面的还需资源数比较，只有满足P3的还需资源数，所以，淘汰了ABD，选择C。

验证C.P3→P2→P1→P4


###### wiki 中处理流程的介绍：

```shell
Allocation　　　Max　　　Available
 　　ＡＢＣＤ　　ＡＢＣＤ　　ＡＢＣＤ
 P1　００１４　　０６５６　　１５２０　
 P2　１４３２　　１９４２　
 P3　１３５４　　１３５６
 P4　１０００　　１７５０
```

我們會看到一個資源分配表，要判斷是否為安全狀態，首先先找出它的Need，Need即Max(最多需要多少資源)減去Allocation(原本已經分配出去的資源)，計算結果如下：

```shell
 NEED
 ＡＢＣＤ
 ０６４２　
 ０５１０
 ０００２
 ０７５０
```

然后加一个全部都为false的标识位：
```shell
FINISH
 false
 false
 false
 false
```
接下來找出need比available小的(千萬不能把它當成4位數 他是4個不同的數)

```shell
 NEED　　Available
 ＡＢＣＤ　　ＡＢＣＤ
 ０６４２　　１５２０
 ０５１０<-
 ０００２
 ０７５０
```
p2 的需求小于能用的， 所以给它再回收

```shell
 NEED　　Available
 ＡＢＣＤ　　ＡＢＣＤ
 ０６４２　　１５２０
 ００００　＋１４３２
 ０００２－－－－－－－
 ０７５０　　２９５２
```

此時P2 FINISH的false要改成true(己完成)

```shell
FINISH
 false
 true
 false
 false
```
接下來繼續往下找，發現P3的需求為0002，小於能用的2952，所以資源配置給他再回收

```shell
　NEED　　Available
 ＡＢＣＤ　　Ａ　Ｂ　Ｃ　Ｄ
 ０６４２　　２　９　５　２
 ００００　＋１　３　５　４
 ０００２－－－－－－－－－－
 ０７５０　　３　12　10　6
```
同樣的將P3的false改成true

```shell
 FINISH
 false
 true
 true
 false
```
依此類推，做完P4→P1，當全部的FINISH都變成true時，就是安全狀態。


###### 伪代码

```shell
p  - 进程的集合
Mp - 进程P的最大请求资源
Cp - 进程P当前被分配的资源

A - 当前可用的资源

while( P.length != 0) {
	found = false
    foreach( p in P) {
    	if (Mp - Cp <= A) {
        	/* p 可以获得他所需要的资源，假設他得到資源後執行；執行終止，並釋放所擁有的資源*/
            A = A + Cp;
            P = P - {p}
            found = True;
            
        }
    }
    
    if (! found) return FAIL;

}
return OK;
```





![][4]

###### 一个python 版的实现
[地址](http://blog.csdn.net/pygzx/article/details/17526723)
```python

import random
import copy
import os

class Simulation(object):

	N=-1
    M=-1
    MAX=10
    claim=[]
    allocation=[]
    resource=[]
    available=[]
    need=[]
    FINISH=[]
    done=[]
    
    def __init__(self):
    	self.N = int(raw_input('Input number of Processes: '))
        self.M = int(raw_input('Input number of Resources:'))
        for i in range(self.M):
        	self.resource.append(random.randint(1,self.MAX))
        self.available = copy.deepcopy(self.resource)
        self.allocation = [[] for i in range(self.N)]
        self.need = [[] for i range(self.N)]
        self.FINISH = [False for i in range(self.N)]
        
        for i in range(self.N):
        	tmp=[]
            for j in range(self.M):
            	tmp.append(random.randint(1,self.resource[j]))
                self.allocation[i].append(0)
                self.need[i].append(tmp[j])
            self.claim.append(tmp)
            
        self.show()
        raw_input()
        
	def getRandomRequest(self):
    	def requestOK(request): 
        	for i in range(self.M):
            	if request[i]:
                	return True
            return False
            
        no = random.randint(0,self.N-1)
        while no in self.done:
        	no = random.randint(0,self.N - 1)
        while True:
        	request = []
            for i in range(self.M)
            	request.append(random.randint(0,self.need[np][i]))
            if requestOK(request): break
        for i in range(self.M):
        	if request[i] > self.available[i]:
            	print 'process',no,'request',request,'faild,request > available'
                return None
        for i in range(self.M):
        	self.available[i] -= request[i]
            self.allocation[no][i] += request[i]
            self.need[no][i] -= request[i]
            
        if self.isSafe(request):
        	print 'process',no,'request',request,'success,safe'
        if self.isComplete(no):
        	print 'Process %d has done!' % no
            self.FINISH[no]=True
            self.done.append(no)
            for k in range(self.M):
            	self.available[k] += self.allication[np][k]
                self.need[np][k] = self.allocation[np][k] = 0
       self.show()
       raw_input()
   else:
   	print "process",no,'request',request,'faild,is not safe'
    self.available[i] += request[i]
    self.allocation[no][i] -= request[i]
    self.need[no][i] += request[i]
    
   def isComplete(self,no):
   	for i in range(self.M):
    	if self.need[no][i] != 0:
        	return False
    return True
    
   def isSafe(self,request):
   	def valid(need,work):
    	for i in range(self.M):
        	if need[i] > work[i]:return False
        return True
        
   work = copy.deepcopy(self.available)
   finish = [self.FINISH[i] for i in range(self.N)]
   count = len(self.done)
   i = 0
   while  i < self.N:
   	if valid(self.need[i],work) and not finish[i]:
    	for j in range(self.M):
        	work[j] += self.allocation[i][j] + self.need[i][j]
        finish[i] = True
        count += 1
        if count == self.N: return True
        i = 0
     else:
     	i += 1
  return False
  
  def show(self):
  	print "Resource Vector : \n%s" % self.resource
    print 'Claim Matrix'
    for i in range(self.N):
    	print self.claim[i]
        
    print 'Allocation Matrix:'
    for i in range(self.N):
    	print self.allocation[i]
    print 'Need Matrix:'
    for i in range(self.N):
    	print self.need[i]
        
    print 'Available Vector : \n %s' % self.available
    print 'Done Queue: \n %s \n\n\n' % self.done
    
  def isOver(self):
  	if len(self.dome) == self.N:
    	return True
    return False
    
   def mian(self):
   	while not self.isOver():
    	self.getRandomRequest()
    print "All Done"
    
if __name__ == "__main__":
	simulation = Simulation()
    simulation.main()

```




##### 使用python 对服务器进行监控

###### 一、Python 版本说明

Python 是由 Guido van Rossum 开发的、可免费获得的、非常高级的解释型语言。其语法简单易懂，而其面向对象的语义功能强大（但又灵活）。Python 可以广泛使用并具有高度的可移植性。本文 Linux 服务器是 Ubuntu 12.10， Python 版本 是 2.7 。如果是 Python 3.0 版本的语法上有一定的出入。另外这里笔者所说的 Python 是 CPython，CPython 是用 C 语言实现的 Python 解释器，也是官方的并且是最广泛使用的 Python 解释器。除了 CPython 以外，还有用 Java 实现的 Jython 和用.NET 实现的 IronPython，使 Python 方便地和 Java 程序、.NET 程序集成。另外还有一些实验性的 Python 解释器比如 PyPy。CPython 是使用字节码的解释器，任何程序源代码在执行之前先要编译成字节码。它还有和几种其它语言（包括 C 语言）交互的外部函数接口。 


###### 二、 工作原理：基于/proc 文件系统

1. 通过 伪文件系统 同内核交互
Linux 系统为管理员提供了非常好的方法，使其可以在系统运行时更改内核，而不需要重新引导内核系统，这是通过/proc 虚拟文件系统实现的。

2. 文件虚拟系统是一种内核和内核模块用来向进程（process）发送信息的机制
/proc 文件虚拟系统是一种内核和内核模块用来向进程（process）发送信息的机制（所以叫做“/proc”），这个伪文件系统允许与内核内部数据结构交互，获取有关进程的有用信息，在运行中（on the fly）改变设置（通过改变内核参数）。

3. /proc 存在于内存而不是硬盘中
与其他文件系统不同，/proc 存在于内存而不是硬盘中。proc 文件系统提供的信息如下：

* 进程信息：
  系统中的任何一个进程，在 proc 的子目录中都有一个同名的进程 ID，可以找到 cmdline、mem、root、stat、statm，以及 status。某些信息只有超级用户可见，例如进程根目录。每一个单独含有现有进程信息的进程有一些可用的专门链接，系统中的任何一个进程都有一个单独的自链接指向进程信息，其用处就是从进程中获取命令行信息。

* 系统信息：
  如果需要了解整个系统信息中也可以从/proc/stat 中获得，其中包括 CPU 占用情况、磁盘空间、内存对换、中断等。

* CPU 信息：
  利用/proc/CPUinfo 文件可以获得中央处理器的当前准确信息。

* 负载信息：
  /proc/loadavg 文件包含系统负载信息。

* 系统内存信息：
  /proc/meminfo 文件包含系统内存的详细信息，其中显示物理内存的数量、可用交换空间的数量，以及空闲内存的数量等。

|文件或目录名称| 描述|
|:--:|:--:|
|apm	|高级电源管理信息|
|cmdline	|这个文件给出了内核启动的命令行|
|CPUinfo	|中央处理器信息|
|devices	|可以用到的设备（块设备/字符设备）|
|dma	|显示当前使用的 DMA 通道|
|filesystems	|核心配置的文件系统|
|ioports	|当前使用的 I/O 端口|
|interrupts	|这个文件的每一行都有一个保留的中断|
|kcore	|系统物理内存映像|
|kmsg	|核心输出的消息，被送到日志文件|
|mdstat	|这个文件包含了由 md 设备驱动程序控制的 RAID 设备信息|
|loadavg	|系统平均负载均衡|
|meminfo	|存储器使用信息，包括物理内存和交换内存|
|modules	|这个文件给出可加载内核模块的信息。lsmod 程序用这些信息显示有关模块的名称，大小，使用数目方面的信息|
|net	|网络协议状态信息|
|partitions	|系统识别的分区表|
|pci	|pci 设备信息|
|scsi	|scsi 设备信息|
|self	|到查看/proc 程序进程目录的符号连接|
|stat	|这个文件包含的信息有 CPU 利用率，磁盘，内存页，内存对换，全部中断，接触开关以及赏赐自举时间|
|swaps	|显示的是交换分区的使用情况|
|uptime	|这个文件给出自从上次系统自举以来的秒数，以及其中有多少秒处于空闲|
|version	|这个文件只有一行内容，说明正在运行的内核版本。可以用标准的编程方法进行分析获得所需的系统信息|

**注：下面本文的几个例子都是使用 Python 脚本读取/proc 目录中的主要文件来实现实现对 Linux 服务器的监控的 。**

###### 对cpu监控

cpu1.py
```python
#!/usr/bin/env python

from __future__ import print_function
from collections improt OrderedDict
import pprint

def CPUinfo():
	"""
    Return the information in /proc/CPUinfo
    as a dictionary in the following format:
    CPU_info['proc0']={...}
    CPU_info['proc1']={...}
    """
    
    CPUinfo = OrderedDict()
    procinfo = OrderedDict()
    
    nprocs = 0
    
    with open("/proc/CPUinfo") as f:
    	for line in f:
        	if not line.strip():
            	# end of one processor
                CPUinfo['proc%s' % nprocs] = procinfo
                nprocs= nprocs+1
                # Reset
                procinfo = OrderedDict()
            else:
            	if len(line.split(":")) == 2:
                	procinfo[line.split(":")[0].strip()] = line.split(":")[1].strip()
                else:
                	procinfo[line.split(":")[0].strip()] = ''
                    
     return CPUinfo
     
if __name__ == "__main__":

	CPUinfo = CPUinfo()
    for processer in CPUinfo.keys():
    	print(CPUinfo[processor]['model name'])

```

简单说明一下清单 1，读取/proc/CPUinfo 中的信息，返回 list，每核心一个 dict。其中 list 是一个使用方括号括起来的有序元素集合。List 可以作为以 0 下标开始的数组。Dict 是 Python 的内置数据类型之一, 它定义了键和值之间一对一的关系。OrderedDict 是一个字典子类，可以记住其内容增加的顺序。常规 dict 并不跟踪插入顺序，迭代处理时会根据键在散列表中存储的顺序来生成值。在 OrderedDict 中则相反，它会记住元素插入的顺序，并在创建迭代器时使用这个顺序。

可以使用 Python 命令运行脚本 CPU1.py 结果见图 1 


![](http://www.ibm.com/developerworks/cn/linux/1312_caojh_pythonlinux/image002.png)


###### 对系统负载进行监控

作用： 获取系统的负载信息
```python
#!/usr/bin/env python
#-*-encoding:utf-8-*-

import os

def load_stat():
	
    loadavg = {}
    
    f = open("/proc/loadavg")
    
    con = f.read.split()
    f.close()
    
    loadavg['lavg_1'] = con[0]
    loadavg['lavg_5'] = con[1]
    loadavg['lavg_15'] = con[2]
    loadavg['nr'] = con[3]
    loadavg['last_pid'] = con[4]
    return loadavg
    
print "loadavg," load_stat()["lavg_15"]
```

 简单说明一下清单 2：清单 2 读取/proc/loadavg 中的信息，import os ：Python 中 import 用于导入不同的模块，包括系统提供和自定义的模块。其基本形式为：import 模块名 [as 别名]，如果只需要导入模块中的部分或全部内容可以用形式：from 模块名 import *来导入相应的模块。OS 模块 os 模块提供了一个统一的操作系统接口函数，os 模块能在不同操作系统平台如 nt，posix 中的特定函数间自动切换，从而实现跨平台操作。

![](http://www.ibm.com/developerworks/cn/linux/1312_caojh_pythonlinux/image004.png)

###### 对内存信息的获取

mem.py
```python
#!/usr/bin/env python
#-*-encoding:utf-8-*-


from __future__ import print_function
from collections import OrderedDict

def meminfo():
	"""
    Return the information in /proc/meminfo
    as a dictionary 
    """
    meminfo = OrderedDict()
    
    with open("/proc/meminfo") as f:
    	for line in f:
        	meminfo[line.split(":")[0]] = line.split(":")[1].strip()
    return meminfo
    
if __name__ == "__main__":

	# print(meminfo)
    meminfo = meminfo()
    print("Total memory: {0}".format(meminfo["MemTotal"]))
    print("Free Memory:{0}".fromat(meminfo["MemFree"]))
```

简单说明一下清单 3：清单 3 读取 proc/meminfo 中的信息，Python 字符串的 split 方法是用的频率还是比较多的。比如我们需要存储一个很长的数据，并且按照有结构的方法存储，方便以后取数据进行处理。当然可以用 json 的形式。但是也可以把数据存储到一个字段里面，然后有某种标示符来分割。 Python 中的 strip 用于去除字符串的首位字符，最后清单 3 打印出内存总数和空闲数。 


![](http://www.ibm.com/developerworks/cn/linux/1312_caojh_pythonlinux/image006.png)

###### 对网络接口的监控

```python
#!/usr/bin/env python
#-*-encoding:utf-8-*-

import time
import sys

if len(sys.argv) > 1:
	INTERFACE = sys.argv[1]
else:
	INTERFACE = "eth0"
    
STATS = []

print "Interface:",INTERFACE

def rx():
	ifstat = open("/proc/net/dev").readlines()
    for interface in ifstat:
    	if INTERFACE in interface:
        	stat = float(interface.split()[1])
            STATS[0:] = [stat]
            
def tx():
	ifstat = open("/proc/net/dev").readlines()
    for interface in ifstat:
    	if INTERFACE in interface:
        	stat = float(interface.split()[9])
            STATS[1:] = [stat]
            
print 'In'+" "*10+"Out"

rx()
tx()

while True:
	time.sleep(1)
    rxstat_0 = list(STATS)
    rx()
    tx()
    RX = float(STATS[0])
    RX_O = rxstat_0[1]
    TX = float(STATS[1])
    TX_O = rxstat_0[1]
    
    rx_rate = round((RX -RX_O)/1024/1024,3 )
    tx_rate = round((TX -TX_O)/1024/1024,3 )
    print rx_tate,"MB      ",tx_rate,"MB"

```
 简单说明一下清单 4：清单 4 读取/proc/net/dev 中的信息，Python 中文件操作可以通过 open 函数，这的确很像 C 语言中的 fopen。通过 open 函数获取一个 file object，然后调用 read()，write()等方法对文件进行读写操作。另外 Python 将文本文件的内容读入可以操作的字符串变量非常容易。文件对象提供了三个“读”方法： read()、readline() 和 readlines()。每种方法可以接受一个变量以限制每次读取的数据量，但它们通常不使用变量。 .read() 每次读取整个文件，它通常用于将文件内容放到一个字符串变量中。然而 .read() 生成文件内容最直接的字符串表示，但对于连续的面向行的处理，它却是不必要的，并且如果文件大于可用内存，则不可能实现这种处理。.readline() 和 .readlines() 之间的差异是后者一次读取整个文件，象 .read() 一样。.readlines() 自动将文件内容分析成一个行的列表，该列表可以由 Python 的 for ... in ... 结构进行处理。另一方面，.readline() 每次只读取一行，通常比 .readlines() 慢得多。仅当没有足够内存可以一次读取整个文件时，才应该使用 .readline()。最后清单 4 打印出网络接口的输入和输出情况。

可以使用 Python 命令运行脚本 net.py 结果见图 4 #Python net.py 

![](http://www.ibm.com/developerworks/cn/linux/1312_caojh_pythonlinux/image008.png)

###### 监控服务器的脚本

```python
#!/usr/bin/env python
#-*-encoding:utf-8-*-

###

import sys,os,time

while True:
	time.sleep(1)
    try:
    	ret = os.popen('ps -C apache -o pid,cmd').readlines()
        if len(ret) < 2:
        	print "apahce 服务器出现异常退出，4秒后恢复"
        time.sleep(3)
        os.system("service apache restart")
    except:
    	print "Error",sys.exec_info()[1]
        

```
设置文件权限为执行属性（使用命令 chmod +x crtrl.py），然后加入到/etc/rc.local 即可，一旦 Apache 服务器进程异常退出，该脚本自动检查并且重启。 简单说明一下清单 5 这个脚本不是基于/proc 伪文件系统的，是基于 Python 自己提供的一些模块来实现的 。这里使用的是 Python 的内嵌 time 模板，time 模块提供各种操作时间的函数。 

###### 总结

在实际工作中，Linux 系统管理员可以根据自己使用的服务器的具体情况编写一下简单实用的脚本实现对 Linux 服务器的监控。本文介绍一下使用 Python 脚本实现对 Linux 服务器 CPU 、系统负载、内存和 网络使用情况的监控脚本的编写方法。 
[原文地址]:http://www.ibm.com/developerworks/cn/linux/1312_caojh_pythonlinux/

##### 使用python 远程操纵服务器

###### 简介
 功能：实现同时对多台linux服务器通过ssh执行同一命令。 技术基础: python pexpect，部支持windows。 
 
###### 概述

 *   功能：实现同时对多台linux服务器通过ssh执行同一命令。

 *   技术基础: python pexpect，部支持windows。

 *   参数：
    
    *    固定参数pwd：远程服务器密码，用户名目前写死是root，可自行修改。

    *    可选参数-c CMDS：要执行的命令，比如："ls -l","cd /home/test && test.py&如果不选择，会从当前目前的cmd.txt读取。

    *   可选参数-s SERVERS：目标服务器，比如192.168.0.1,最后一位数可以用-表示一个区间，分号用于分割不同的ip。如果不选择，会从当前目前的ip.txt读取。

###### 主角本

```python
#!/usr/bin/env python
#-*-encoding:utf-8-*-
import argparse
import common # deal with argument
sparser = argparse.ArgumentParser()
parser.add_argument('pwd', action="store",help=u'password')
parser.add_argument('-c', dest='cmds', action="store", help=u'command')
parser.add_argument('-s', dest='servers', action="store", help=u'hosts')
parser.add_argument('--version', action='version',version = '%(prog)s 1.1 ted 2015-05-12')
options = parser.parse_args()
servers = []

if options.servers:
	raw_servers = options.servers.split(";") # 使用 ； 分割传入的server列表
    for server in raw_servers:
    	if '-' in server:        # 使用 “-”分割 主机地址区段的起始地址
        	server_list = server.split(".")
            base = ".".join(server_list[:3])
            indices = server_list[-1].split("-")
            start,end = indices
            
            for item in range(int(start),int(end)+1):
            	servers.append("{0}.{1}".format(base,item))
                
        else:
        	servers.append(server) 
else:
	for item in open("ip.txt"):
    	servers.append(item)

cmds=[]
if options.cmds:
	comds.append(options.cmds)
    
else:
	for item in open("cmds.txt")
    	cmds.append(item)
        
for host in servers:
	print
    print ("*"*80)
    print ("\nConnect to host: {0}".format(host))
    c = common.Ssh()
    c.connect(host,"root",options.pwd)
    for item in cmds:
    	c.command(item)
    c.close()

```

###### ssh 登录类库
common.py
```python

class Ssh(object):
	
    client = None
    
    @classmethod
    def connect(cls,ip,username="root",password="123456",prompt="]#",
    silent=False):
    	## Ssh to remote server
        ssh_newkey = "are you sure you want to continue connecting"
        child = pexpect.spwan("ssh " + username + "@"+ip,maxread=5000)
        
        i = 1
        # Enter password
        while i != 0:
        	i = child.expect([prompt,'assword:*',ssh_newkey,pexpect.TIMEOUT,\
            'key.*? faild'])
            if not silent:
            	print child.before,child.after,
            if i == 0:    # find prompt
            	pass
            elif i == 1: # Enter password
            	child.send(password+"\r")
            if i == 2: # SSH does not have the public key,Just accept
            	child.sendline('yes\r')
            if i == 3:  # TimeOut
            	raise Exception("ERROR TIMEOUT! SSH could not login.")
            if i == 4:  # new key
            	print child.before,child.after
                os.remove(os.path.expanduser("~") + './ssh/known_hosts')
                
       Ssh.client = child
       
	@classmethod
    def command(cls,cmd,prompt=']#',silent=False):
    	Ssh.client.buffer=''
        Ssh.client.send(cmd+"\r")
        ## Ssh.client.setwinsize(400,400)
        Ssh.client.expect(prompt)
        if not silent:
        	print Ssh.client.before,Ssh.client.after.
        return Ss.client.before,Ssh.client.after
        
    def close(cls,):
    	Ssh.client.close()
            
```

结果：

```shell
# ./batch.py -husage: batch.py [-h] [-c CMDS] [-s SERVERS] [--version] pwdpositional arguments:  pwd         password
 
optional arguments:
  -h, --help  show this help message and exit
  -c CMDS     command
  -s SERVERS  hosts
  --version   show program's version number and exit
 
# ./batch.py password -s "192.168.0.71-76;123.1.149.26" -c "cat /etc/redhat-release"
 
********************************************************************************
 
Connect to host: 192.168.0.71
Last login: Thu May  8 17:04:02 2014 from 183.14.8.49
[root@localhost ~ ]# cat /etc/redhat-release
CentOS release 5.8 (Final)
[root@localhost ~ ]#
********************************************************************************
 
Connect to host: 192.168.0.72
Last login: Thu May  8 17:03:05 2014 from 192.168.0.232
[root@localhost ~ ]# cat /etc/redhat-release
CentOS release 5.8 (Final)
[root@localhost ~ ]#
********************************************************************************
 
Connect to host: 192.168.0.73
Last login: Thu May  8 17:02:29 2014 from 192.168.0.232
[root@localhost ~ ]# cat /etc/redhat-release
CentOS release 5.8 (Final)
[root@localhost ~ ]#
********************************************************************************
 
Connect to host: 192.168.0.74
Last login: Thu May  8 17:02:32 2014 from 192.168.0.232
[root@localhost ~ ]# cat /etc/redhat-release
CentOS release 5.8 (Final)
[root@localhost ~ ]#
********************************************************************************
 
Connect to host: 192.168.0.75
root@192.168.0.75's p assword:  
Last login: Thu May  8 17:02:56 2014 from 192.168.0.232
[root@localhost ~ ]# cat /etc/redhat-releaseCentOS release 6.4 (Final)
[root@localhost ~ ]#
********************************************************************************
 
Connect to host: 192.168.0.76
Last login: Thu May  8 17:03:00 2014 from 192.168.0.232
[root@localhost ~ ]# cat /etc/redhat-releaseCentOS release 5.8 (Final)
[root@localhost ~ ]#
********************************************************************************
 
Connect to host: 123.1.149.26
Last login: Thu May  8 16:46:56 2014 from 183.56.157.199
[root@LINUX ~ ]# cat /etc/redhat-releaseRed Hat Enterprise Linux Server release 6.5 (Santiago)
[root@LINUX ~ ]#
[root@AutoTest batch]#
```



#### python 模块的常用的几种安装方法

##### Python模块安装方法

###### 一、 方法1： 单文件模块
直接把文件拷贝到 $python_dir/Lib

###### 二、方法2： 多文件模块，带setup.py

下载模块包，进行解压，进入模块文件夹，执行：
python setup.py install

python setup.py uninstall

###### 三、 方法3：easy_install 方式

 先下载ez_setup.py,运行python ez_setup 进行easy_install工具的安装，之后就可以使用easy_install进行安装package了。
  easy_install  packageName
  easy_install  package.egg

###### 四、 方法4：pip 方式

先进行pip工具的安裝：easy_install pip（pip 可以通过easy_install 安裝，而且也会装到 Scripts 文件夹下。）

安裝：pip install PackageName

更新：pip install -U PackageName

移除：pip uninstall PackageName

搜索：pip search PackageName

帮助：pip help

---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

注：虽然Python的模块可以拷贝安装，但是一般情况下推荐制作一个安装包，即写一个setup.py文件来安装。
setup.py文件的使用如下:
% python setup.py build     #编译
% python setup.py install    #安装
% python setup.py sdist      #制作分发包
% python setup.py bdist_wininst    #制作windows下的分发包
% python setup.py bdist_rpm

setup.py文件的编写
setup.py中主要执行一个 setup函数，该函数中大部分是描述性东西，最主要的是packages参数，列出所有的package，可以用自带的find_packages来动态获取package。所以setup.py文件的编写实际是很简单的。
简单的例子:
setup.py文件：

```python
from setuptools import setup, find_packages
setup(
       name = " mytest " ,
       version = " 0.10 " ,
       description = " My test module " ,
       author = " Robin Hood " ,
       url = " http://www.csdn.net " ,
       license = " LGPL " ,
       packages = find_packages(),
       scripts = [ " scripts/test.py " ],
       )

mytest.py

import sys
def get():
     return sys.path

scripts/test.py

import os
print os.environ.keys() 



```
setup中的scripts表示将该文件放到 Python的Scripts目录下，可以直接用。OK，简单的安装成功，可以运行所列举的命令生成安装包，或者安装该python包。本机测试成功(win32-python25)！


##### 附注：setuptools工具安装方法
（方法一）. 使用ez_setup.py安装setuptools
　　进入https://pypi.python.org/pypi/setuptools下载ez_setup.py
 这是 setuptools 自豪的一种安装方式，只需要一个大约 8K 作为的脚本ez_setup.py，就能自动为用户安装包括 setuptools 自身在内的许多 Python 包。 使用这种方式，用户只需要下载 ez_setup。py 并运行，就可以自动下载和安装适合用户当前 Python 版本的适当的 setuptools egg 文件(当然，用户需要 Python 2.3.5 以上的版本，64 位操作系统的用户则需要 Python 2.4 以上的版本)。此外，这段脚本还会将可执行的 easy_install 脚本安装到用户所有的操作系统 Python 可执行脚本正常应该安装的位置(例如，Windows 用户会安装到 Python 安装目录下的 Scripts 目录中)。关于这种安装方法的更详细说明和注意事项，请参考其官方说明（见扩展阅读）。简单的安装命令如下： 　　wget -q ez_setup。py下载地址（见扩展阅读） 安装完后，最好确保
（方法二）. 使用完整的安装包安装setuptools
　　当然，用户也可以直接使用 setuptools发布版本来安装。对于使用 Windows 的用户，这也是挺方便的方法，许多 Linux 发行版的官方包管理仓库都包含 setuptools 的某个版本。例如，如果你跟我一样使用 Ubuntu ，那安装 setuptools 只是简单的进行如下操作：
 apt-get install python-setuptools
安装 easy_install package-name，比如 easy_install pylab
模块卸载 easy_install -m package-name， 比如easy_install -m pylab
easy_install -m 包名，可以卸载软件包，但是卸载后还要手动删除遗留文件。

setuptools它可以自动的安装模块，只需要你提供给它一个模块名字就可以，并且自动帮你解决模块的依赖问题。一般情况下用setuptools给安装的模块会自动放到一个后缀是.egg的目录里。
在Windows里，easy_install这个命令在python安装目录下的scripts里面，所以需要把scripts加到环境变量的PATH里，这样用起来就更方便，linux下不需要注意这个问题


##### python 打包

python的第三方模块越来越丰富，涉及的领域也非常广，如科学计算、图片处理、web应用、GUI开发等。当然也可以将自己写的模块进行打包或发布。一简单的方法是将你的类包直接copy到python的lib目录，但此方式不便于管理与维

python的第三方模块越来越丰富，涉及的领域也非常广，如科学计算、图片处理、web应用、GUI开发等。当然也可以将自己写的模块进行打包或发布。一简单的方法是将你的类包直接copy到python的lib目录，但此方式不便于管理与维护，存在多个 python版本时会非常混乱。现介绍如何编写 setup.py来对一个简单的python模块进行打包。

###### 一、编写模块
进入项目目录
```shell
#cd /home/pysetup
#vi foo.py
``` 
Python代码
```python
   1. class MyClass():   
   2.     def __init__(self):   
   3.         self.blog = "http://blog.liuts.com"  
   4.     def printblog(self):   
   5.         print self.blog   
   6.     def printBblog(self):   
   7.         print self.blog.swapcase()  

class MyClass():
	def __init__(self):
    	self.blog = "http://blog.liuts.com" 
   	def printblog(self):
    	print self.blog 
    def printBblog(self): 
    	print self.blog.swapcase()
```

二、编写setup.py

```shell
#vi setup.py
```
Python代码
```python
   1. from distutils.core import setup   
   2. setup(name='Myblog',   
   3.       version='1.0',   
   4.       description='My Blog Distribution Utilities',   
   5.       author='Liu tiansi',   
   6.       author_email='liutiansi@gmail.com',   
   7.       url='http://blog.liuts.com',   
   8.       py_modules=['foo'],   
   9.      )  

from distutils.core import setup 
  	setup(name='Myblog',
    	  version='1.0', 
          description='My Blog Distribution Utilities',
          author='Liu tiansi',
          author_email='liutiansi@gmail.com',
          url='http://blog.liuts.com',
          py_modules=['foo'],      )
```

###### 三、setup.py参数说明

```shell
#python setup.py build     # 编译
#python setup.py install    #安装
#python setup.py sdist      #生成压缩包(zip/tar.gz)
#python setup.py bdist_wininst  #生成NT平台安装包(.exe)
#python setup.py bdist_rpm #生成rpm包
```

或者直接"bdist 包格式"，格式如下：

```shell
#python setup.py bdist --help-formats
  --formats=rpm      RPM distribution
  --formats=gztar    gzip'ed tar file
  --formats=bztar    bzip2'ed tar file
  --formats=ztar     compressed tar file
  --formats=tar      tar file
  --formats=wininst  Windows executable installer
  --formats=zip      ZIP file
```

###### 四、打包
```shell
#python setup.py sdist
running sdist

warning: sdist: manifest template 'MANIFEST.in' does not exist (using default file list)
warning: sdist: standard file not found: should have one of README, README.txt

writing manifest file 'MANIFEST'
creating Myblog-1.0
making hard links in Myblog-1.0...
hard linking foo.py -> Myblog-1.0
hard linking setup.py -> Myblog-1.0
creating dist
tar -cf dist/Myblog-1.0.tar Myblog-1.0
gzip -f9 dist/Myblog-1.0.tar
removing 'Myblog-1.0' (and everything under it)
```

提示两条warning可以忽略，不影响打包，当然一个完善的项目必须有README及MANIFEST.in(项目文件清单)文件。
#ls dist

Myblog-1.0.tar.gz


###### 五、安装
```shell
#tar -zxvf Myblog-1.0.tar.gz
#cd Myblog-1.0.tar.gz
#python setup.py install (此命令大家再熟悉不过了)
running install
running build
running build_py
creating build/lib.linux-x86_64-2.6
copying foo.py -> build/lib.linux-x86_64-2.6
running install_lib
copying build/lib.linux-x86_64-2.6/foo.py -> /usr/local/lib/python2.6/dist-packages
byte-compiling /usr/local/lib/python2.6/dist-packages/foo.py to foo.pyc
running install_egg_info
Writing /usr/local/lib/python2.6/dist-packages/Myblog-1.0.egg-info
```

###### 六、测试
```shell
>>> from foo import MyClass
>>> app=MyClass()
>>> app.print printblog()
>>> app.printblog()
http://blog.liuts.com
>>> app.printBblog()
HTTP://BLOG.LIUTS.COM
>>>
```


##### python 下划线变量的含义

_xxx      不能用'from module import *'导入
__xxx__ 系统定义名字
__xxx    类中的私有变量名

核心风格：避免用下划线作为变量名的开始。

"单下划线" 开始的成员变量叫做保护变量，意思是只有类对象和子类对象自己能访问到这些变量；
"双下划线" 开始的是私有成员，意思是只有类对象自己能访问，连子类对象也不能访问到这个数据。

以单下划线开头（_foo）的代表不能直接访问的类属性，需通过类提供的接口进行访问，不能用“from xxx import *”而导入；以双下划线开头的（__foo）代表类的私有成员；以双下划线开头和结尾的（__foo__）代表python里特殊方法专用的标识，如 __init__（）代表类的构造函数。

 结论：

1、_xxx      以单下划线开头的表示的是protected类型的变量。即保护类型只能允许其本身与子类进行访问。弱内部变量标示，如，当使用“from M import ”时，不会将以一个下划线开头的对象引入。

2、__xxx    双下划线的表示的是私有类型的变量。只能是允许这个类本身进行访问了,连子类也不可以,用于命名一个类属性（类变量），调用时名字被改变（在类FooBar内部，__boo变成_FooBar__boo ，如 self._FooBar__boo )
）

3、__xxx___ 定义的是特列方法。用户控制的命名空间内的变量或是属性，如__init__，__import__或是__file__。只有当文档有说明时使用，不要自己定义这类变量。（就是说这些是python内部定义的变量名）


##### 理解Python命名机制（单双下划线开头）

###### 引子
我热情地邀请大家猜测下面这段程序的输出：

```python
class A(object):
       def __init__(self):
              self.__private()
              self.public()
       def __private(self):
              print 'A.__private()'
       def public(self):
              print 'A.public()'
class B(A):
       def __private(self):
              print 'B.__private()'
       def public(self):
              print 'B.public()'
b = B()

```

###### 初探
正确的答案是：
A.__private()
B.public()
如果您已经猜对了，那么可以不看我这篇博文了。如果你没有猜对或者心里有所疑问，那我的这篇博文正是为您所准备的。
一切由为什么会输出“A.__private()”开始。但要讲清楚为什么，我们就有必要了解一下Python的命名机制。
据 Python manual，变量名（标识符）是Python的一种原子元素。当变量名被绑定到一个对象的时候，变量名就指代这个对象，就像人类社会一样，不是吗？当变 量名出现在代码块中，那它就是本地变量；当变量名出现在模块中，它就是全局变量。模块相信大家都有很好的理解，但代码块可能让人费解些。在这里解释一下：
代码块就是可作为可执行单元的一段Python程序文本；模块、函数体和类定义都是代码块。不仅如此，每一个交互脚本命令也是一个代码块；一个脚本文件也是一个代码块；一个命令行脚本也是一个代码块。
接下来谈谈变量的可见性，我们引入一个范围的概念。范围就是变量名在代码块的可见性。 如果一个代码块里定义本地变量，那范围就包括这个代码块。如果变量定义在一个功能代码块里，那范围就扩展到这个功能块里的任一代码块，除非其中定义了同名 的另一变量。但定义在类中的变量的范围被限定在类代码块，而不会扩展到方法代码块中。

###### 迷踪
据上节的理论，我们可以把代码分为三个代码块：类A的定义、类B的定义和变量b的定义。根据类定义，我们知道代码给类A定义了三个成员变量（Python的函数也是对象，所以成员方法称为成员变量也行得通。）；类B定义了两个成员变量。这可以通过以下代码验证：

```python
>>> print '\n'.join(dir(A))
_A__private
__init__
public
>>> print '\n'.join(dir(B))
_A__private
_B__private
__init__
public
```
咦，为什么类A有个名为_A__private的 Attribute 呢？而且__private消失了！这就要谈谈Python的私有变量轧压了。

###### 探究

懂Python的朋友都知道Python把以两个或以上下划线字符开头且没有以两个或以上下划线结尾的变量当作私有变量。私有变量会在代码生成之前被转换为长格式（变为公有）。转换机制是这样的：在变量前端插入类名，再在前端加入一个下划线字符。这就是所谓的私有变量轧压（Private name mangling）。如类 A里的__private标识符将被转换为_A__private，这就是上一节出现_A__private和__private消失的原因了。

再讲两点题外话：
一是因为轧压会使标识符变长，当超过255的时候，Python会切断，要注意因此引起的命名冲突。
二是当类名全部以下划线命名的时候，Python就不再执行轧压。如：

```python
>>> class ____(object):
       def __init__(self):
              self.__method()
       def __method(self):
              print '____.__method()'
>>> print '\n'.join(dir(____))
__class__
__delattr__
__dict__
__doc__
__getattribute__
__hash__
__init__
__method              # 没被轧压
__module__
__new__
__reduce__
__reduce_ex__
__repr__
__setattr__
__str__
__weakref__
>>> obj = ____()
____.__method()
>>> obj.__method()      # 可以外部调用
____.__method()
```
现在我们回过头来看看为什么会输出“A.__private()”吧！

###### 真相

相信现在聪明的读者已经猜到答案了吧？如果你还没有想到，我给你个提示：真相跟C语言里的宏预处理差不多。
因为类A定义了一个私有成员函数（变量），所以在代码生成之前先执行私有变量轧压（注意到上一节标红的那行字没有？）。轧压之后，类A的代码就变成这样了：
class A(object):
       def __init__(self):
              self._A__private()          # 这行变了
              self.public()
       def _A__private(self):           # 这行也变了
              print 'A.__private()'
       def public(self):
              print 'A.public()'
是不是有点像C语言里的宏展开啊？
因为在类B定义的时候没有覆盖__init__方法，所以调用的仍然是A.__init__，即执行了self._A__private()，自然输出“A.__private()”了。
下面的两段代码可以增加说服力，增进理解：

```python
>>> class C(A):
       def __init__(self):          # 重写 __init__ ，不再调用 self._A__private
              self.__private()       # 这里绑定的是 _C_private
              self.public()
       def __private(self):
              print 'C.__private()'
       def public(self):
              print 'C.public()'
>>> c = C()
C.__private()
C.public()
############################
>>> class A(object):
       def __init__(self):
              self._A__private()   # 调用一个没有定义的函数， Python 会把它给我的 
              self.public()
       def __private(self):
              print 'A.__private()'
       def public(self):
              print 'A.public()'
>>>a = A()
A.__private()
A.public()

```




[0]:http://www.ourunix.org/post/category/rd/python
[1]:http://static.oschina.net/uploads/img/201410/29094825_kprK.png
[2]:http://static.oschina.net/uploads/img/201410/29094825_fWIi.png
[3]:http://static.oschina.net/uploads/img/201410/29094825_nPNS.png
[4]:http://static.oschina.net/uploads/img/201410/29094825_20Ot.png