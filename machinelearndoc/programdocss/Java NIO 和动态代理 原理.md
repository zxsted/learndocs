####[Java NIO 和动态代理 原理][10]
[toc]
#####前言
最近在分析hadoop的RPC(Remote Procedure Call Protocol ，远程过程调用协议，它是一种通过网络从远程计算机程序上请求服务，而不需要了解底层网络技术的协议。[参考百度百科][0]）机制时，发现hadoop的RPC机制的实现主要用到了两个技术：动态代理和java NIO。为了能够正确地分析hadoop的RPC源码，我觉得很有必要先研究一下java NIO的原理和具体实现。

#####java NIO 
[java NIO系列教程传送门][1]

######一、阻塞I/O通信模型
假如现在你对阻塞I/O已有了一定了解，我们知道阻塞I/O在调用InputStream.read()方法时是阻塞的，它会一直等到数据到来时（或超时）才会返回；同样，在调用ServerSocket.accept()方法时，也会一直阻塞到有客户端连接才会返回，每个客户端连接过来后，服务端都会启动一个线程去处理该客户端的请求。阻塞I/O的通信模型示意图如下：
![阻塞IO原理图][2]
如果你细细分析，一定会发现阻塞I/O存在一些缺点。根据阻塞I/O通信模型，我总结了它的两点缺点：
1. 当客户端多时，会创建大量的处理线程。且每个线程都要占用栈空间和一些CPU时间

2. 阻塞可能带来频繁的上下文切换，且大部分上下文切换可能是无意义的。

在这种情况下非阻塞式I/O就有了它的应用前景。

######二、java NIO 原理以及通信模型

Java NIO是在jdk1.4开始使用的，它既可以说成“新I/O”，也可以说成非阻塞式I/O。下面是java NIO的工作原理：

1. 由一个专门的线程来处理所有的 IO 事件，并负责分发。
2. 事件驱动机制：事件到的时候触发，而不是同步的去监视事件。
3. 线程通讯：线程之间通过 wait,notify 等方式通讯。保证每次上下文切换都是有意义的。减少无谓的线程切换。

阅读过一些资料之后，下面贴出我理解的java NIO的工作原理图：
![NIO原理图][3]
（注：每个线程的处理流程大概都是读取数据、解码、计算处理、编码、发送响应。）
Java NIO的服务端只需启动一个专门的线程来处理所有的 IO 事件，这种通信模型是怎么实现的呢？呵呵，我们一起来探究它的奥秘吧。java NIO采用了双向通道（channel）进行数据传输，而不是单向的流（stream），在通道上可以注册我们感兴趣的事件。一共有以下四种事件：<table>
<tr><td>事件名</td>对应值<td></td></tr>
<tr><td>服务端接受客户端连接事件</td><td>SelectionKey.OP_ACCEPT(16)</td></tr>
<tr><td>客户端连接服务端事件</td><td>SelectionKey.OP_CONNECT(8)</td></tr>
<tr><td>读事件</td><td>SelectionKey.OP_READ(1)</td></tr>
<tr><td>写事件</td><td>SelectionKey.OP_WRITE(4)</td></tr>
</table>

服务端和客户端各自维护一个管理通道的对象，我们称之为selector，该对象能检测一个或多个通道 (channel) 上的事件。我们以服务端为例，如果服务端的selector上注册了读事件，某时刻客户端给服务端发送了一些数据，阻塞I/O这时会调用read()方法阻塞地读取数据，而NIO的服务端会在selector中添加一个读事件。服务端的处理线程会轮询地访问selector，如果访问selector时发现有感兴趣的事件到达，则处理这些事件，如果没有感兴趣的事件到达，则处理线程会一直阻塞直到感兴趣的事件到达为止。下面是我理解的java NIO的通信模型示意图：
![IO事件触发轮询图][4]

######java NIO 使用SocketChannel 服务端和客户端代码实现
* 服务端

```java
package cn.nio

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;


/**
*NIO服务端
*@author ted
*/

public class NIOServer{

	//管道管理器
	private Selector selector;
    
    /**
    *获得一个ServerSocket通道，并对该通道作一些初始化的工作
    *@param port 绑定的端口号
    *@throws IOException
    */
    public void initServer(int port) throws IOException {
    	//获得一个ServerSocket通道
        ServerSocketChannel serverchannel = ServerSocketChanel.open();
        //设置通道为非阻塞
        serverChannel.configureBlocking(false);
        //将该管道对应的ServerSocket绑定到port端口
        serverChannel.socket().bind(new IntetSocketAddress(port));
        //获得一个通道管理器
        this.selector = Selector.open();
        //将管道管理器和该通道绑定，并为该通道注册SelectionKey.OP_ACCEPT时间，
        //注册事件后，当该事件到达后，selector.select()会返回，如果该时间没到达
        //selector，select()一直阻塞
        serverChannel.register(selector,SelectionKey.OP_ACCEPT);
    }
    
    /**
    *采用轮询的方式监听selector上是否有要处理的事件，如果有，则进行处理
    *@throws IOException
    */
    @SuppressWarnings("unchecked")
    public void listen() throws IOException{
    	System.out.println("服务端启动成功！");
        //轮询访问selector
        while(true){
        	//当注册的时间到达时，方法返回；否则，该方法会一直阻塞
            selector.select();
            //获得selector 中选中的项的迭代器，选中的项为注册的事件
            Iterator ite = this.selector.selectedKeys().iterator();
            while(ite.hasNext()) {
            	SelectionKey key = (SelectionKey) ite.next();
                //删除已经选中的key，以防重复处理
                ite.remove();
                //客户端请求连接事件
                if(key.isAcceptable()){   //如果客户端的触发事件是接收事件
                	ServerSocketChannel server  = (ServerSocketChannel) key.channel();    //为每一个连接事件建立一个客户端与服务器的管道，进行通信
                    //获得与客户端连接的通道
                    SocketChannel channel = server.accept();
                    //设置成非阻塞
                    channel.configureBlocking(false);
                    
                    //在这里可以给客户端发送消息
                    channel.write(ByteBuffer.wrap(new String("向客户端发送了一条消息").getBytes()))
                    //在和客户端链接成功后，为了可以接收到客户端的信息，需要给管道设置读的权限
                    channel.register(this.selector,SelectionKey.OP_READ);
                } else if (key.isReadable()){  //如果触发事件是读事件，则读取发送来的信息
                	read(key)
                }
            }
        }
    }
	/**
    *处理读取客户端发来的信息的事件
    *@param key
    *@throws IOException
    */
    public void read(SelectionKey key) throws IOException {
    	//服务端可读取消息：得到时间发生的Socket通道
        SocketChannel channel = (SocketChannel) key.channel();
        //创建读取的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(10);
        channel.read(buffer);
        byte[] data = buffer.array();
        String msg = new String(data).trim();
        System.out.println("服务端收到信息："+msg);
        ByteBuffer outBuffer = ByteBuffer.wrap(msg.getBytes());
        channel.write(outBuffer);    //将消息回送给客户端
    }
    
    /**
	*启动服务端测试
    *@throws IOException
    */
    public static void main(String[] args) {
    	NIOServer server = new NIOServer();
        server.initServer(8000);
        server.listen();
    }
}
```

* 客户端：

```java
package cn.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

    /**
    *NIO客户端
    *@author ted
    */
    public class NIOClient {
    	//管道管理器
        private Selector selector;
        
        /**
        *获得一个SOcket通道，并对该通道进行一些初始化工作
        *@param ip 连接的服务器的IP
        *@param port 连接的服务器的端口号
        *@throws IOException
        */
        public void initClient(String ip,int port) throws IOException {
        	//获得一个Socket通道
            SocketChannel channel = SocketChannel.open();
            //设置管道为非阻塞
            channel.configureBlocking(false);
            //获得一个通道管理器
            this.selectot = Selector.open();
            
            //客户端连接服务器，其实执行方法并没有实现链接，需要在listen（）方法中
            //调用Channel.finishConnect();才能完成连接
            channel.register(selector,SelectionKey.OP_CONNECT);
        
        }
        
	/**
    *采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理
    *throws IOException
    */
    @Suppressarnings("unchecked")
    public void listen() throws IOExcpetion {
    	//轮询访问selector
        while(true) {
        	selector.select();
            //获得selector中选中项的迭代器
            Iterator ite = this.selector.selectedKeys().iterator();
            while (ite.hasNext()) {
            	SelectionKey key = (SelectionKey) ite.next();
                //删除已选的key，以防止重复处理
                ite.remove();
                //连接事件发生
                if (key.isConnectable()) {
                	SocketChannel channel = (SocketChannel) key.channel();
                    //如果正在连接，则完成连接
                    if(channel.isConnectionPending()) {
                    	channel.finishConnect();
                    }
                    //设置成非阻塞
                    channel.configureBlocking(false);
                    
                    //在这里向服务器发送信息
                    channel.write(ByteBuffer.wrap(new String("向服务器发送的信息").getBytes()));
                    //在和服务器端连接成功后，为了可以接收到服务器端的信息，给服务器设置读的权利
                    channel.register(this.selector,SelectionKey.OP_READ);
                }else if (key.isReadable()) {
                	read(key);
                }
            }
        }
    }
    
    /**
    *处理读取服务器端发送来的信息的事件
    *@param key
    *@throws IOException
    */
    public void read(SelectionKey key) {
    	//与服务器端一样的
        SokcetChannel channel = (SocketChannel) key.channel();
        //创建读取的缓冲区
        ByteBuffer = ByteBuffer.allocate(10);
        channel.read(buffer);
        Byte[] data = buffer.array();
        String msg = new String(data).trim();
        System.out.println("接收到服务器端的信息：" + msg);
        ByteBuffer outBuffer = ByteBUffer.wrap(msg.getBytes());
        channel.write(outBuffer);  //将消息会送给服务器端
    }
    
    /**
    *启动客户端
    * throws IOExcpetion
    */
    public static void main(String[] args) {
    	NIOClient client = new NIOClient();
        client.inintClient("localhost",8000);
        client.listen();
    }
    }
```



###### [一般的java NIO][5] 
* Java 标准 io 回顾 
  Java 标准 IO 类库是 io 面向对象的一种抽象。基于本地方法的底层实现，我们无须关注底层实现。 InputStream\OutputStream( 字节流 ) ：一次传送一个字节。 Reader\Writer( 字符流 ) ：一次一个字符。
  
* nio 简介
 nio 是 java New IO 的简称，在 jdk1.4 里提供的新 api 。 Sun 官方标榜的特性如下：
–     为所有的原始类型提供 (Buffer) 缓存支持。
–     字符集编码解码解决方案。
–     Channel ：一个新的原始 I/O 抽象。
–     支持锁和内存映射文件的文件访问接口。
–     提供多路 (non-bloking) 非阻塞式的高伸缩性网络 I/O 。
本文将围绕这几个特性进行学习和介绍。 
* Buffer&Channel
Channel 和 buffer 是 NIO 是两个最基本的数据类型抽象。
 * Buffer:
–        是一块连续的内存块。
–        是 NIO 数据读或写的中转地。
 * Channel:
–        数据的源头或者数据的目的地
–        用于向 buffer 提供数据或者读取 buffer 数据 ,buffer 对象的唯一接口。
–        异步 I/O 支持 
![buffer 和 channel关系][6]
例子：

```java
package sample;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class CopyFile {
	public static void main(String[] args){
    	String infile = "C:\\copy.sql"
        Stirng outfile = "C:\\copy.txt"
        //源文件的目标文件的输入输出流
        FileInputStream fin = new FileInputStream(infile);
        FileOutputStream fout = new FileOutputStream(outfile);
        //获取输入输出通道
        FileChannel fcin = fin.getChannel();
        FileChannel fcout = fout.getChannel();
        //创建缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while(true){
        	//clear方法重设缓冲区，使它可以接受读入的数据
            buffer.clear();
            //从输入通道中将数据读到缓冲区
            int r = fcin.read(buffer);
            //read方法返回读取的字节数，可能为0，如果该通道已经到达流的末尾，则返回-1
            if (r == -1) {
            	break
            }
            //flip方法可以让缓冲区将读入的数据写入另一个通道
            buffer.flip();
            //从输出通道中将数据写入缓冲区
            fcout.write(buffer)
        }
    }
}
```
buffer的内部构造如下：
![buffer的内部构造][7]
一个 buffer 主要由 position,limit,capacity 三个变量来控制读写的过程。此三个变量的含义见如下表格： 
表格还没写

* Buffer 常见方法：
 * flip(): 写模式转换成读模式
 * rewind() ：将 position 重置为 0 ，一般用于重复读。
 * clear() ：清空 buffer ，准备再次被写入 (position 变成 0 ， limit 变成 capacity) 。
 * compact(): 将未读取的数据拷贝到 buffer 的头部位。
 * mark() 、 reset():mark 可以标记一个位置， reset 可以重置到该位置。

* Buffer 常见类型： ByteBuffer 、 MappedByteBuffer 、 CharBuffer 、 DoubleBuffer 、 FloatBuffer 、 IntBuffer 、 LongBuffer 、 ShortBuffer 。

* channel 常见类型 :FileChannel 、 DatagramChannel(UDP) 、 SocketChannel(TCP) 、 ServerSocketChannel(TCP) 

###### nio.charset 
  
字符编码解码 : 字节码本身只是一些数字，放到正确的上下文中被正确被解析。向 ByteBuffer 中存放数据时需要考虑字符集的编码方式，读取展示 ByteBuffer 数据时涉及对字符集解码。

Java.nio.charset 提供了编码解码一套解决方案。

以我们最常见的 http 请求为例，在请求的时候必须对请求进行正确的编码。在得到响应时必须对响应进行正确的解码。

以下代码向 baidu 发一次请求，并获取结果进行显示。例子演示到了 charset 的使用。 

```java
package nio.readpage;
import java.nio.ByteBuffer;  
import java.nio.channels.SocketChannel;  
import java.nio.charset.Charset;  
import java.net.InetSocketAddress;  
import java.io.IOException; 

public class BaiduReader {
	private Charset charset = Charset.forName("GBK");  //创建GBK字符集
    private SocketChannel channel;
    public  void readHTMLContent(){
    	try{
        	InetSocketAddress socketAddress = new InetSocketAddress(
            "www.baidu.com",80);
            //1. 打开连接
            channel = SocketChannel.open(socketAddress);
            //2. 发送请求使用GBK编码
            channel.write(charset.encode("GET"+"/ HTTP/1.1" + "\r\n\r\n"));
            //3. 读取数据
            ByteBuffer buffer = ByteBuffer.allocate(1024); //创建1024字节的缓冲区
            while(channel.read(buffer) != -1) {
            	buffer.flip();   //flip方法在读缓冲区字符操作之前调用。
                System.out.println(charset.decode(buffer));
                //使用Charset.decode方法将字节转换为字符串
                buffer.clear(); //清空缓冲区
            }
        }catch(IOException e) {
        	System.out.println(e.toString());
        }finally{
        	if (channel != null) {
            	try{
                	channel.close()
                }catch(IOException e){}
            }
        }
    }
    
    public static void main(String[] args) {
    	new BaiduReader().readHTMLContent();
    }
}

```
###### 非阻塞 IO

关于非阻塞 IO 将从何为阻塞、何为非阻塞、非阻塞原理和异步核心 API 几个方面来理解。

何为阻塞？

一个常见的网络 IO 通讯流程如下 : 
![net io][8]

从该网络通讯过程来理解一下何为阻塞 :

在以上过程中若连接还没到来，那么 accept 会阻塞 , 程序运行到这里不得不挂起， CPU 转而执行其他线程。

在以上过程中若数据还没准备好， read 会一样也会阻塞。

阻塞式网络 IO 的特点：多线程处理多个连接。每个线程拥有自己的栈空间并且占用一些 CPU 时间。每个线程遇到外部为准备好的时候，都会阻塞掉。阻塞的结果就是会带来大量的进程上下文切换。且大部分进程上下文切换可能是无意义的。比如假设一个线程监听一个端口，一天只会有几次请求进来，但是该 cpu 不得不为该线程不断做上下文切换尝试，大部分的切换以阻塞告终。 

何为非阻塞？

下面有个隐喻：

一辆从 A 开往 B 的公共汽车上，路上有很多点可能会有人下车。司机不知道哪些点会有哪些人会下车，对于需要下车的人，如何处理更好？

1. 司机过程中定时询问每个乘客是否到达目的地，若有人说到了，那么司机停车，乘客下车。 ( 类似阻塞式 )

2. 每个人告诉售票员自己的目的地，然后睡觉，司机只和售票员交互，到了某个点由售票员通知乘客下车。 ( 类似非阻塞 )

很显然，每个人要到达某个目的地可以认为是一个线程，司机可以认为是 CPU 。在阻塞式里面，每个线程需要不断的轮询，上下文切换，以达到找到目的地的结果。而在非阻塞方式里，每个乘客 ( 线程 ) 都在睡觉 ( 休眠 ) ，只在真正外部环境准备好了才唤醒，这样的唤醒肯定不会阻塞。

  非阻塞的原理

把整个过程切换成小的任务，通过任务间协作完成。

由一个专门的线程来处理所有的 IO 事件，并负责分发。

事件驱动机制：事件到的时候触发，而不是同步的去监视事件。

线程通讯：线程之间通过 wait,notify 等方式通讯。保证每次上下文切换都是有意义的。减少无谓的进程切换。

以下是异步 IO 的结构： 

![非阻塞][9]
Reactor 就是上面隐喻的售票员角色。每个线程的处理流程大概都是读取数据、解码、计算处理、编码、发送响应。 

异步 IO 核心 API

Selector

异步 IO 的核心类，它能检测一个或多个通道 (channel) 上的事件，并将事件分发出去。

使用一个 select 线程就能监听多个通道上的事件，并基于事件驱动触发相应的响应。而不需要为每个 channel 去分配一个线程。

SelectionKey

包含了事件的状态信息和时间对应的通道的绑定。

例子 1 单线程实现监听两个端口。 ( 见 nio.asyn 包下面的例子。 )

例子 2 NIO 线程协作实现资源合理利用。 (wait,notify) 。 ( 见 nio.asyn.multithread 下的例子 ) 
[nioSamples.rar ]:(http://dl.iteye.com/topics/download/cba1c77f-63df-3494-acc3-524cb2e067b4)


##### 动态代理
直接上代码：

```java
package cn.tex.dynamicproxy

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/** 
 * 动态代理类使用到了一个接口InvocationHandler和一个代理类Proxy ，这两个类配合使用实现了动态代理的功能。 
 * 那么什么是动态代理呢？ 
 * 我们平常说的代理类是指： 给每个具体类写一个代理类，以后要使用某个具体类时，只要创建它的代理类的对象，然后调用代理类的方法就可以了。 
 * 可是如果现在有许多的具体类，那就需要有许多的代理类才可以，这样很显然不合适。所以动态代理就应运而生了，我们只要写一个类实现 
 * InvocationHandler 并实现它的invoke方法，然后再用Proxy的工厂方法newProxyInstance（）创建一个代理对象，这个对象同样可以实现对具体类的代理功能。 
 * 而且想代理哪个具体类，只要给Handler（以下代码中的Invoker）的构造器传入这个具体对象的实例就可以了。感觉是不是自己为该具体类造了一个代理类呢？呵呵~ 
 */
 
//接口类
interface AbstractClass {
	public void show();
}

//具体类 A
class ClassA implements AbstractClass {
	@Override
    public void show() {
    	System.out.println("原生类1正在运行");
    }
}

//具体类 B
class ClassB implements AbstractClass {
	@override
    public void show() {
    	System.out.println("原生类2正在运行");
    }
}

//dynamic proxy class ,need realize  interface InvocationHandler接口
class Invoker implements InvocationHandler {
	AbstractClass ac;       //多态
    
    public Invoker(AbstractClass ac) { //装填被代理类的方法
    	this.ac = ac;
    }
    
    @Override
    public Object invoke(Object proxy,Method method,Object[] args) 
    		throws Throwable {
    	//TODO：添加调用前处理
        method.invoke(ac,arg);
        //TODO：添加调用后处理
        return null;
    }
}

/**
*测试类
*/
class DynamicProxyTest {
	public static void main(String[] args) {
    	//创建具体类的处理对象
        Invoker invoker1 = new Invoker(new ClassA());
        //获得具体类ClassA的代理
        AbstractClass acl = (AbstractClass) Proxy.newProxyInstance(
        		AbstractClass.class.getClassLoader(),
                new Class[]{ AbstractClass.class},invoker1)
        //调用ClassA的show()方法
        acl.show();
    }
}
```
































[0]:http://baike.baidu.com/view/32726.htm
[1]:http://ifeve.com/overview/
[2]:http://dl.iteye.com/upload/attachment/0066/2121/3158e45b-1bb9-37a2-ba3e-3982b604eeff.jpg
[3]:http://dl.iteye.com/upload/attachment/0066/2123/c17e2880-a712-349f-a818-2c921303f224.jpg
[4]:http://dl.iteye.com/upload/attachment/0066/3190/0184183e-286c-34f1-9742-4adaa28b7003.jpg
[5]:http://www.iteye.com/topic/834447
[6]:http://dl.iteye.com/upload/attachment/361546/f4a0aefc-127c-3c9e-975f-36cce5173a35.jpg
[7]:http://dl.iteye.com/upload/attachment/361548/d6236f08-e617-34be-81f2-c53c126de3d7.jpg
[8]:http://dl.iteye.com/upload/attachment/361552/6fb291bd-3b9a-3067-a1e6-41cce3804409.jpg
[9]:http://dl.iteye.com/upload/attachment/361554/7dac568f-b3bf-38f7-8392-f5b6d1fb2b3f.jpg
[10]:http://weixiaolu.iteye.com/blog/1479656