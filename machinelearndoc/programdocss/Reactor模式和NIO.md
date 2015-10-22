Reactor模式和NIO


[传送][0]

[toc]
#####背景
当前分布式计算　Web Services盛行天下，这些网络服务的底层都离不开对socket的操作。他们都有一个共同的结构：
1. Read request
2. Decode request
3. Process service
4. Encode reply
5. Send reply

经典的网络服务的设计如下图，在每个线程中完成对数据的处理：
![][1]

但这种模式在用户负载增加时，性能将下降非常的快。我们需要重新寻找一个新的方案，保持数据处理的流畅，很显然，事件触发机制是最好的解决办法，当有事件发生时，会触动handler,然后开始数据的处理。

Reactor模式类似于AWT中的Event处理：
![][2]

#####Reactor模式参与者
1.Reactor 负责响应IO事件，一旦发生，广播发送给相应的Handler去处理,这类似于AWT的thread
2.Handler 是负责非堵塞行为，类似于AWT ActionListeners；同时负责将handlers与event事件绑定，类似于AWT addActionListener
![][3]
Java的NIO为reactor模式提供了实现的基础机制，它的Selector当发现某个channel有数据时，会通过SlectorKey来告知我们，在此我们实现事件和handler的绑定。

#####样例代码
我们来看看Reactor模式代码:
```java

public class Reactor implements Runnable{
	
    final Selector selector;
    final ServerSocketChannel serverSocket;
    
    public Reactor(int port) throws IOException {
    	selector = Selector.open();
        
        serverSocket = ServerSocketChannel.open();
        
        InetSocketAddress address = new InetSocketAddress(
        InetAddress.getLocalHost(),port);
        serverSocket.scoket().bind(address);
        
        serverSocket.configureBlocking(false);
        //向selector 注册channel
        SelectionKey sk = serverScoket.register(
                   selector,SelectionKey.OP_ACCEPT);
                   
        logger.debug("-->attach(new Acceptor()) !");
    }
    
    public void run(){    //通常在一个新的线程中
    	try{
        	while(!Thread.interrupted())
            {
            	selector.select();
                Set selected = selector.selectedKeys();
                Iterator it = selected.iterator();
                //Selector 如果发现channel 有OP_ACCEPT 或 READ事件发生
                //下列遍历就会发生
                while(iter.hasNext())
                	//来一个事件就会触发一个accepter 
                    //以后触发 SocketReadHandler
                    dispatch((SelectionKey)(it.next()));
                selected.clear();
            }
        }catch(IOException ex) {
        	logger.debug("reactor stop !" + ex);
        }
    
    }
    
    //运行Acceptor或者ScoketReadHandler
    void dispatch(SelectionKey k) {
    	Runnable r = (Runnable) (k.attachment());
    	if(r != null) {
        // r.run();
        }
    }
    
    //内部类 一个接收器
    class Acceptor implements Runnable{
    	public void run(){
        	tyr{
            	logger.debug("--> ready for accept!");
                SocketChannel c = serverSocker.accept();
                if( c != null)
                   //调用 Handler 来处理channel
                   new SocketReadHandler(selector,c);
            }catch(IOException ex) {
            	logger.debug("accept stop !" + ex);
            }
        }
    }
}

```
以上代码中巧妙使用了SocketChannel的attach功能，将Hanlder和可能会发生事件的channel链接在一起，当发生事件时，可以立即触发相应链接的Handler。

下面是Handler的代码
```java
public class SocketReadHandler implements Runnable{

	public static Logger logger = Logger.getLogger(
    		SocketReadHandler.class);
    
    private Test test = new Test();
    
    final SocketChannel socket;
    final SelectionKey sk;
    
    static final int READING = 0, SENDING = 1;
    int state = READING;
    
    public SocketReadHandler(Selector sel,SockerChannel c)
    			throws IOException {
    	socket = c;
        
        socket.configureBlocking(false);
        sk = socket.register(sel,0);
        
        //将SelectionKey 绑定为本Handler，下一步有事件就调用的run方法
        // 参看 dispatch(SelectionKey k)
        sk.attach(this)
        
        //同时将SelectionKey标记为可读，以方便读取
        sk.interestOps(SelectionKey.OP_READ);
        sel.wakeup();
        
    }
    
    public void run()
    {
       try{
       	//test.read(socket,input);
        	readRequest();
            
       }catch (Exception ex){
       	logger.debug("readRequest error " + ex);
       }
    
    }
    
 /**
 *处理读取data
 */
 private void readRequest() throws Exception{
 	byteBuffer input = ByteBuffer.allocate(1024);     //分配1024字节的yte缓存
    input.clear();
    
    try{
    	
        int bytesRead = socket.read(input);
        .......
        //激活线程池处理这些request
        requestHandle(new Request(socket,btt));
    	......
    }catch(Exception e) {
    
    }
    
 }
}

```
注意在Handler里面又执行了一次attach，这样，覆盖前面的Acceptor，下次该Handler又有READ事件发生时，将直接触发Handler.从而开始了数据的读　处理　写　发出等流程处理。

将数据读出后，可以将这些数据处理线程做成一个线程池，这样，数据读出后，立即扔到线程池中，这样加速处理速度：

![][4]

更进一步，我们可以使用多个Selector分别处理连接和读事件。

一个高性能的Java网络服务机制就要形成，激动人心的集群并行计算即将实现。


#####理论介绍
[传送门][6]

######两种I/O多路复用模式：Reactor和Proactor
Reactor 的 类图 来自[小武哥的博客]:事件处理模式之Reactor(一)http://www.wuzesheng.com/?p=1607
![][7]
![][9]
![][8]
![][10]

 一般地,I/O多路复用机制都依赖于一个事件多路分离器(Event Demultiplexer)。分离器对象可将来自事件源的I/O事件分离出来，并分发到对应的read/write事件处理器(Event Handler)。开发人员预先注册需要处理的事件及其事件处理器（或回调函数）；事件分离器负责将请求事件传递给事件处理器。两个与事件分离器有关的模式是Reactor和Proactor。Reactor模式采用同步IO，而Proactor采用异步IO。
 在Reactor中，事件分离器负责等待文件描述符或socket为读写操作准备就绪，然后将就绪事件传递给对应的处理器，最后由处理器负责完成实际的读写工作。
 而在Proactor模式中，处理器--或者兼任处理器的事件分离器，只负责发起异步读写操作。IO操作本身由操作系统来完成。传递给操作系统的参数需要包括用户定义的数据缓冲区地址和数据大小，操作系统才能从中得到写出操作所需数据，或写入从socket读到的数据。事件分离器捕获IO操作完成事件，然后将事件传递给对应处理器。比如，在windows上，处理器发起一个异步IO操作，再由事件分离器等待IOCompletion事件。典型的异步模式实现，都建立在操作系统支持异步API的基础之上，我们将这种实现称为“系统级”异步或“真”异步，因为应用程序完全依赖操作系统执行真正的IO工作。
 举个例子，将有助于理解Reactor与Proactor二者的差异，以读操作为例（类操作类似）。
 在Reactor中实现读：
 - 注册读就绪事件和相应的事件处理器
 - 事件分离器等待事件
 - 事件到来，激活分离器，分离器调用事件对应的处理器。
 - 事件处理器完成实际的读操作，处理读到的数据，注册新的事件，然后返还控制权。
 与如下Proactor（真异步）中的读过程比较：
 - 处理器发起异步读操作（注意：操作系统必须支持异步IO）。在这种情况下，处理器无视IO就绪事件，它关注的是完成事件。
 - 事件分离器等待操作完成事件
 - 在分离器等待过程中，操作系统利用并行的内核线程执行实际的读操作，并将结果数据存入用户自定义缓冲区，最后通知事件分离器读操作完成。
 - 事件分离器呼唤处理器。
 - 事件处理器处理用户自定义缓冲区中的数据，然后启动一个新的异步操作，并将控制权返回事件分离器。
 
######实践现状 
 由Douglas Schmidt等人开发的开源C++开发框架ACE，提供了大量与平台无关，支持并发的底层类（线程，互斥量等），且在高抽象层次上，提供了两组不同的类--ACE Reactor和ACE Proactor的实现。不过，虽然二者都与平台无关，提供的接口却各异。
 ACE Proactor在windows平台上具有更为优异的性能表现，因为windows在操作系统提供了高效的异步API支持（见http://msdn2.microsoft.com/en-us/library/aa365198.aspx）。
 然而，并非所有的操作系统都在系统级大力支持异步。像很多Unix系统就没做到。因此，在Unix上，选择ACE Reactor解决方案可能更好。但这样一来，为了获得最好的性能，网络应用的开发人员必须为不同的操作系统维护多份代码：windows上以ACE Proactor为基础，而Unix系统上则采用ACE Reactor解决方案。
 
######改进方案
  在这部分，我们将尝试应对为Proactor和Reactor模式建立可移植框架的挑战。在改进方案中，我们将Reactor原来位于事件处理器内的read/write操作移至分离器(不妨将这个思路称为“模拟异步”)，以此寻求将Reactor多路同步IO转化为模拟异步IO。以读操作为例子，改进过程如下：
  - 注册读就绪事件及其处理器，并为分离器提供数据缓冲区地址，需要读取数据量等信息。
  - 分离器等待事件（如在select()上等待）
  - 事件到来，激活分离器。分离器执行一个非阻塞读操作（它有完成这个操作所需的全部信息），最后调用对应处理器。
  - 事件处理器处理用户自定义缓冲区的数据，注册新的事件（当然同样要给出数据缓冲区地址，需要读取的数据量等信息），最后将控制权返还分离器。
  如我们所见，通过对多路IO模式功能结构的改造，可将Reactor转化为Proactor模式。改造前后，模型实际完成的工作量没有增加，只不过参与者间对工作职责稍加调换。没有工作量的改变，自然不会造成性能的削弱。对如下各步骤的比较，可以证明工作量的恒定：
  标准/典型的Reactor：
  - 步骤1：等待事件到来（Reactor负责）
  - 步骤2：将读就绪事件分发给用户定义的处理器（Reactor负责）
  - 步骤3：读数据（用户处理器负责）
  - 步骤4：处理数据（用户处理器负责）
  改进实现的模拟Proactor：
  - 步骤1：等待事件到来（Proactor负责）
  - 步骤2：得到读就绪事件，执行读数据（现在由Proactor负责）
  - 步骤3：将读完成事件分发给用户处理器（Proactor负责）
  - 步骤4：处理数据（用户处理器负责） 
 
  对于不提供异步IO API的操作系统来说，这种办法可以隐藏socket API的交互细节，从而对外暴露一个完整的异步接口。借此，我们就可以进一步构建完全可移植的，平台无关的，有通用对外接口的解决方案。
   
 上述方案已经由Terabit P/L公司（http://www.terabit.com.au/）实现为TProactor。它有两个版本：C++和JAVA的。C++版本采用ACE跨平台底层类开发，为所有平台提供了通用统一的主动式异步接口。
  Boost.Asio库，也是采取了类似的这种方案来实现统一的IO异步接口。

<-----------

最近在项目中使用了Boost.Asio类库，其就是以Proactor这种设计模式来实现，参见：Proactor（The Boost.Asio library is based on the Proactor pattern. This design note outlines the advantages and disadvantages of this approach.），其设计文档链接：http://asio.sourceforge.net/boost_asio_0_3_7/libs/asio/doc/design/index.html

First, let us examine how the Proactor design pattern is implemented in asio, without reference to platform-specific details.

![][5]

Proactor design pattern (adapted from [1])

当然这两I/O设计模式，也在ACE中被大量应用，这在ACE的相关书籍中都有介绍，其中在“ACE开发者”网站中有很多不错的介绍文章。

如：ACE技术论文集-第8章 前摄器（Proactor）：用于为异步事件多路分离和分派处理器的对象行为模式

ACE技术论文集-第7章 ACE反应堆（Reactor）的设计和使用：用于事件多路分离的面向对象构架

ACE程序员教程-第6章 反应堆（Reactor）：用于事件多路分离和分派的体系结构模式

ACE应用-第2章 JAWS：高性能Web服务器构架

 

Proactor模式在单CPU单核系统应用中有着无可比拟的优势，现在面临的问题是：在多CPU多核的系统中，它如何更好地应用多线程的优势呢？？？这是很值思考和实践的，也许会产生另外一种设计模式来适应发展的需要啦。

[0]:http://www.jdon.com/concurrent/reactor.htm
[1]:http://www.jdon.com/concurrent/images/classic.jpg
[2]:http://www.jdon.com/concurrent/images/awt.jpg
[3]:http://www.jdon.com/concurrent/images/reactor.jpg
[4]:http://www.jdon.com/concurrent/images/pool.jpg
[5]:http://p.blog.csdn.net/images/p_blog_csdn_net/roger_77/206689/o_proactor.png
[6]:http://blog.csdn.net/roger_77/article/details/1555170
[7]:http://www.wuzesheng.com/wp-content/uploads/2011/03/reactor.jpg
[8]:http://pic002.cnblogs.com/images/2012/300959/2012081415122480.jpg
[9]:http://hi.csdn.net/attachment/201101/31/0_1296459716YE2V.gif
[10]:http://pic002.cnblogs.com/images/2012/300959/2012081415134767.png