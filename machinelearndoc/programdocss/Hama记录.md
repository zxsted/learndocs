####Hama记录
[toc]
######配置和启动hama 
Apache Hame是Google Pregel的开源实现，与Hadoop适合于分布式大数据处理不同，Hama主要用于分布式的矩阵、graph、网络算法的计算。

简单说，Hama是在HDFS上实现的BSP(Bulk Synchronous Parallel)计算框架，弥补Hadoop在计算能力上的不足。

* 关于bsp计算模型：
http://ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=375451&tag=1
http://en.wikipedia.org/wiki/Bulk_synchronous_parallel
关于hama和bsp的架构和编程方法，参考hama文档：
http://hama.apache.org/hama_bsp_tutorial.html
北交的一篇硕士学位论文第二、第三章部分也介绍了hama的架构：
http://cdmd.cnki.com.cn/Article/CDMD-10004-1012355651.htm
[引用请注明原文：http://blog.csdn.net/bhq2010/article/details/8510201]
* 下载Hama:
http://hama.apache.org/downloads.html
下载0.6.0版的binary包，解压后得到hama-0.6.0目录，记作HAMA_HOME
* 配置Hama:
Hama可以有三种工作模式：单机、分布式、伪分布式模式
hama的配置文件在HAMA_HOME/conf目录下，有：
hama-env.sh：该文件中包含了hama守护进程所用的环境变量，其中的JAVA_HOME环境变量是必须设置的，使其指向1.5以上版本的JDK的安装目录；
groomservers：该文件中列出了groomserver守护进程所在的节点，每个一行；
hama-default.xml：该文件包含了hama守护进程的默认设置，该文件无需更改；
hama-site.xml：该文件包含了特定hama守护进程和bsp job的设置，配置hama时，将需要的设置写在这里，可以覆盖hama-default.xml中的设置，该文件的一个官方的示例如下：
```xml
<?xml version="1.0"?>  
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>  
<configuration>  
  <property>  
    <name>bsp.master.address</name>  
    <value>host1.mydomain.com:40000</value>  
    <description>The address of the bsp master server. Either the  
    literal string "local" or a host:port for distributed mode  
    </description>  
  </property>  
  
  <property>  
    <name>fs.default.name</name>  
    <value>hdfs://host1.mydomain.com:9000/</value>  
    <description>  
      The name of the default file system. Either the literal string  
      "local" or a host:port for HDFS.  
    </description>  
  </property>  
  
  <property>  
    <name>hama.zookeeper.quorum</name>  
    <value>host1.mydomain.com,host2.mydomain.com</value>  
    <description>Comma separated list of servers in the ZooKeeper Quorum.  
    For example, "host1.mydomain.com,host2.mydomain.com,host3.mydomain.com".  
    By default this is set to localhost for local and pseudo-distributed modes  
    of operation. For a fully-distributed setup, this should be set to a full  
    list of ZooKeeper quorum servers. If HAMA_MANAGES_ZK is set in hama-env.sh  
    this is the list of servers which we will start/stop zookeeper on.  
    </description>  
  </property>  
</configuration>
```
* 启动hama
执行HAMA_HOME/bin目录下的start-bsp.sh脚本。
学习的时候在伪分布模式下运行就可以了，上述的配置文件中的地址都用localhost代替，zookeeper配置项省略，先启动hdfs再启动hama.
通过http://localhost:40013可以查看hama的状态
* 注意：
1、伪分布用localhost如果不行的话，要看一下hadoop的配置文件，
[html] view plaincopy
    fs.default.name  
这一项要和hadoop的配置一致。
2、注意hadoop的版本要和HAMA_HOME/lib中的hadoop-core jar包的版本一致，不然即使能正常启动hama，也可能会在运行hama作业时出错（如：老版本的hadoop可能缺少hama所需的一些类或者方法）

######在eclipse中编译hama源码、写hama job 
* 写hama job
如果只是写hama job，根本不需要eclipse，所有的代码都可一在一个java文件中搞定。不过用惯了eclipse的人表示vim之类的实在用不惯。
在eclipse中可以建一个user library：
在eclipse菜单栏中：Window->Preferences->Java->Build Path->User Libraries->New新建一个user library，例如hama-0.6.0，勾选System Library。然后Add External JARs，将HAMA_HOME/lib中的jar包和HAMA_HOME下的jar包加进来。
新建Java Project时将这个user library加入工程，就OK了，可以试试hama example中计算PI的例子：
```java
    import java.io.IOException;  
      
    import org.apache.commons.logging.Log;  
    import org.apache.commons.logging.LogFactory;  
    import org.apache.hadoop.fs.FSDataInputStream;  
    import org.apache.hadoop.fs.FileStatus;  
    import org.apache.hadoop.fs.FileSystem;  
    import org.apache.hadoop.fs.Path;  
    import org.apache.hadoop.io.DoubleWritable;  
    import org.apache.hadoop.io.IOUtils;  
    import org.apache.hadoop.io.NullWritable;  
    import org.apache.hadoop.io.Text;  
    import org.apache.hama.HamaConfiguration;  
    import org.apache.hama.bsp.BSP;  
    import org.apache.hama.bsp.BSPJob;  
    import org.apache.hama.bsp.BSPJobClient;  
    import org.apache.hama.bsp.BSPPeer;  
    import org.apache.hama.bsp.ClusterStatus;  
    import org.apache.hama.bsp.FileOutputFormat;  
    import org.apache.hama.bsp.NullInputFormat;  
    import org.apache.hama.bsp.TextOutputFormat;  
    import org.apache.hama.bsp.sync.SyncException;  
      
    public class PiEstimator  
    {  
        private static Path TMP_OUTPUT = new Path("/tmp/pi-"  
            + System.currentTimeMillis());  
      
        public static class MyEstimator  
            extends  
            BSP<NullWritable, NullWritable, Text, DoubleWritable, DoubleWritable>  
        {  
        public static final Log LOG = LogFactory.getLog(MyEstimator.class);  
        private String masterTask;  
        private static final int iterations = 10000;  
      
        @Override  
        public void bsp(  
            BSPPeer<NullWritable, NullWritable, Text, DoubleWritable, DoubleWritable> peer)  
            throws IOException, SyncException, InterruptedException  
        {  
      
            int in = 0;  
            for (int i = 0; i < iterations; i++)  
            {  
            double x = 2.0 * Math.random() - 1.0, y = 2.0 * Math.random() - 1.0;  
            if ((Math.sqrt(x * x + y * y) < 1.0))  
            {  
                in++;  
            }  
            }  
      
            double data = 4.0 * in / iterations;  
      
            peer.send(masterTask, new DoubleWritable(data));  
            peer.sync();  
        }  
      
        @Override  
        public void setup(  
            BSPPeer<NullWritable, NullWritable, Text, DoubleWritable, DoubleWritable> peer)  
            throws IOException  
        {  
            // Choose one as a master  
            this.masterTask = peer.getPeerName(peer.getNumPeers() / 2);  
        }  
      
        @Override  
        public void cleanup(  
            BSPPeer<NullWritable, NullWritable, Text, DoubleWritable, DoubleWritable> peer)  
            throws IOException  
        {  
            if (peer.getPeerName().equals(masterTask))  
            {  
            double pi = 0.0;  
            int numPeers = peer.getNumCurrentMessages();  
            DoubleWritable received;  
            while ((received = peer.getCurrentMessage()) != null)  
            {  
                pi += received.get();  
            }  
      
            pi = pi / numPeers;  
            peer.write(new Text("Estimated value of PI is"),  
                new DoubleWritable(pi));  
            }  
        }  
        }  
      
        static void printOutput(HamaConfiguration conf) throws IOException  
        {  
        FileSystem fs = FileSystem.get(conf);  
        FileStatus[] files = fs.listStatus(TMP_OUTPUT);  
        for (int i = 0; i < files.length; i++)  
        {  
            if (files[i].getLen() > 0)  
            {  
            FSDataInputStream in = fs.open(files[i].getPath());  
            IOUtils.copyBytes(in, System.out, conf, false);  
            in.close();  
            break;  
            }  
        }  
      
        fs.delete(TMP_OUTPUT, true);  
        }  
      
        public static void main(String[] args) throws InterruptedException,  
            IOException, ClassNotFoundException  
        {  
        // BSP job configuration  
        HamaConfiguration conf = new HamaConfiguration();  
      
        BSPJob bsp = new BSPJob(conf, PiEstimator.class);  
        // Set the job name  
        bsp.setJobName("Pi Estimation Example");  
        bsp.setBspClass(MyEstimator.class);  
        bsp.setInputFormat(NullInputFormat.class);  
        bsp.setOutputKeyClass(Text.class);  
        bsp.setOutputValueClass(DoubleWritable.class);  
        bsp.setOutputFormat(TextOutputFormat.class);  
        FileOutputFormat.setOutputPath(bsp, TMP_OUTPUT);  
      
        BSPJobClient jobClient = new BSPJobClient(conf);  
        ClusterStatus cluster = jobClient.getClusterStatus(true);  
      
        if (args.length > 0)  
        {  
            bsp.setNumBspTask(Integer.parseInt(args[0]));  
        } else  
        {  
            // Set to maximum  
            bsp.setNumBspTask(cluster.getMaxTasks());  
        }  
      
        long startTime = System.currentTimeMillis();  
        if (bsp.waitForCompletion(true))  
        {  
            printOutput(conf);  
            System.out.println("Job Finished in "  
                + (System.currentTimeMillis() - startTime) / 1000.0  
                + " seconds");  
        }  
        }  
    }  
```
Run as Java Application即可，这样运行是在单机模式下的，不需要安装和启动Hama集群。如果要在集群上运行可以将工程Export成Jar文件，发到集群上运行。
* 编译hama源码
hama的源码工程是用maven构建的，下载hama的src包，解压；
在eclipse中安装m2e即可Import->Maven->Existing Maven Project->选择解压后的hama源码所在的目录，就可以导入，第一次导入时，maven会去下载依赖的包，所以时间比较长。
导入后有hama-core\hama-graph等6个工程，之后就可以用maven插件编译、调试，研究hama源码了。
如果不用eclipse，则需要下载安装maven2，过程google一下，随处可见。
为了省事，可以下载Juno版的eclipse for jave EE developer.这个版本的eclipse中带有了完整的m2e插件。

######编写BSP程序
Hama中提供了BSP框架的编程接口，就像MapReduce一样方便使用。
[引用请注明出处：http://blog.csdn.net/bhq2010/article/details/8531243]
* BSP框架
首先明确一下BSP的概念：
BSP是一个计算框架，按照这个框架编写的BSP程序会在集群的各个节点上做本地的I/O和计算，这一点和MapReduce相似（其实BSP的提出比MapReduce还要早差不多10年，应该算前辈才是～），但不同的是BSP框架中，各个节点之间可以进行比较有效的通信。
一个BSP程序（或者叫BSP Job）的执行过程中包含了若干个超步(Supersteps)，每个超步的执行过程又有以下三个步骤：
 * 各个节点本地的计算->节点间通信->节点同步
第一和第二个步骤之前其实没有明确的界限。在一个超步中，各个结点在进入同步状态之前可以随时进行I/O和通信。
当某个结点认为自己的计算任务已经完成时，可以进入同步状态并挂起。当一个超步中所有的结点都进入同步状态时，一个超步就结束了，各个节点从挂起处开始继续执行，所有结点都退出时，整个BSP程序就结束了。
继承BSP类
Hama中编写BSP程序和Hadoop MapReduce差不多，首先写一个类，继承Hama API中的BSP抽象类，例如：
```java
    public static class MyEstimator  
         extends BSP<NullWritable, NullWritable, Text, DoubleWritable, DoubleWritable>  
```
这个类不一定是static的，以上只是hama.example里计算PI的一个例程。
然后要实现BSP类中的抽象方法bsp，例如：
```java
    public void bsp(BSPPeer<NullWritable, NullWritable, Text, DoubleWritable, DoubleWritable> peer)  
        throws IOException, SyncException, InterruptedException  
    {.....}  
```
此外，和Hadoop MapReduce类似，BSP类中还有两个方法可以重载：setup和cleanup
这两个方法分别在一个BSP程序执行前后进行初始化和清理的工作。
* 文件I/O
在配置BSP Job时，可以为其指定输入输出格式和路径，和Hadoop很相似，例如：
```java
    job.setInputPath(new Path("/tmp/sequence.dat");  
      job.setInputFormat(org.apache.hama.bsp.SequenceFileInputFormat.class);  
      or,  
      SequenceFileInputFormat.addInputPath(job, new Path("/tmp/sequence.dat"));  
      or,  
      SequenceFileInputFormat.addInputPaths(job, "/tmp/seq1.dat,/tmp/seq2.dat,/tmp/seq3.dat");  
        
      job.setOutputKeyClass(Text.class);  
      job.setOutputValueClass(IntWritable.class);  
      job.setOutputFormat(TextOutputFormat.class);  
      FileOutputFormat.setOutputPath(job, new Path("/tmp/result"));  
```
其中setInputFormat和setOutputFormat是设置输入和输出文件的格式的，默认是文本格式的，这和Hadoop的setInputFormatClass、setOutputFormatClass作用一样。这样在bsp方法中就可以用BSPPeer类型的参数peer来读取输入文件（通常是在HDFS上）并向输出文件中写入了，例如： 
```java
    public final void bsp(  
          BSPPeer<LongWritable, Text, Text, LongWritable, Text> peer)  
          throws IOException, InterruptedException, SyncException {  
            
          // this method reads the next key value record from file  
          KeyValuePair<LongWritable, Text> pair = peer.readNext();  
      
          // the following lines do the same:  
          LongWritable key = new LongWritable();  
          Text value = new Text();  
          peer.readNext(key, value);  
            
          // write  
          peer.write(value, key);  
      }  
```
 * 需要重新打开输入文件重新读取，可以用peer.reopenInput()方法。
 * 此外，在bsp中也可已随意访问合法的文件，不过这些文件IO就没法在配置BSP Job时指定，而只能硬编码了。
* 计算结点间通信
Hama为BSP提供的通信API如下：
|方法|描述|
|---|---|
|send(String peerName, BSPMessage msg) |	Sends a message to another peer.|
|getCurrentMessage() |	Returns a received message.|
|getNumCurrentMessages() |	Returns the number of received messages.|
|sync() |	Barrier synchronization.|
|getPeerName() |	Returns a peer's hostname.|
|getAllPeerNames() |	Returns all peer's hostname.|
|getSuperstepCount() |	Returns the count of supersteps|
这些都是bsp方法的参数peer的方法，像上面调用read、write方法一样调用即可。
* 同步
调用peer.sync()方法可以使当前节点进入同步状态，当所有的节点都进入同步状态后，同步完成，开始下一个超步或者结束Job。

######消息的发送与存储
Hama的文档目前还不详细，没有找到关于Hama如何发送消息的说明，只好自己动手做实验了。
按照BSP的模型，每一个超步应该是有明确的三个步骤的：计算->通信->同步
但是Hama当中，在节点进入同步状态之前，是否可以和其他结点即时地收发消息呢？如果可以，无疑会使得bsp程序更加灵活，但是这样也会带来不必要的麻烦：如果bsp程序设计不当，各个节点之间随意通信可能会使得程序的性能非常糟糕。并且这样也增加了容错的难度。
为了搞清楚通信的情况，做了如下实验：
打开计算PI的example，在MyEstimator的bsp方法中调用peer.send之前设置断点，其实send方法的注释文档就已经告诉我们一些有用的信息了：
```shell
  * Send a data with a tag to another BSPSlave corresponding to hostname.
   * Messages sent by this method are not guaranteed to be received in a sent
   * order.
```
可见hama bsp并不严格地保证消息的接收和发送顺序一致。
在eclipse中开始debug（单击模式，此时hama用多线程来模拟多个节点的计算），发现调用send最终执行的是org.apache.hama.bsp.message.AbstractMessageManager.send()中的一段代码：
```shell
    InetSocketAddress targetPeerAddress = null;  
    // Get socket for target peer.  
    if (peerSocketCache.containsKey(peerName)) {  
      targetPeerAddress = peerSocketCache.get(peerName);  
    } else {  
      targetPeerAddress = BSPNetUtils.getAddress(peerName);  
      peerSocketCache.put(peerName, targetPeerAddress);  
    }  
    MessageQueue<M> queue = outgoingQueues.get(targetPeerAddress);  
    if (queue == null) {  
      queue = getQueue();  
    }  
    queue.add(msg);  
    peer.incrementCounter(BSPPeerImpl.PeerCounter.TOTAL_MESSAGES_SENT, 1L);  
    outgoingQueues.put(targetPeerAddress, queue);  
    notifySentMessage(peerName, msg);  
```
可见这里是将要发送的消息添加到队列中了，并没有将消息发送出去。
继续debug，发现调用sync方法执行的是如下的代码：
```java
// normally all messages should been send now, finalizing the send phase  
messenger.finishSendPhase();  
Iterator<Entry<InetSocketAddress, MessageQueue<M>>> it = messenger  
    .getMessageIterator();  
  
while (it.hasNext()) {  
  Entry<InetSocketAddress, MessageQueue<M>> entry = it.next();  
  final InetSocketAddress addr = entry.getKey();  
  final Iterable<M> messages = entry.getValue();  
  
  final BSPMessageBundle<M> bundle = combineMessages(messages);  
  // remove this message during runtime to save a bit of memory  
  it.remove();  
  try {  
    messenger.transfer(addr, bundle);  
  } catch (Exception e) {  
    LOG.error("Error while sending messages", e);  
  }  
}  
  
if (this.faultToleranceService != null) {  
  try {  
    this.faultToleranceService.beforeBarrier();  
  } catch (Exception e) {  
    throw new IOException(e);  
  }  
}  
  
long startBarrier = System.currentTimeMillis();  
enterBarrier();  
  
if (this.faultToleranceService != null) {  
  try {  
    this.faultToleranceService.duringBarrier();  
  } catch (Exception e) {  
    throw new IOException(e);  
  }  
}  
  
// Clear outgoing queues.  
messenger.clearOutgoingQueues();  
  
leaveBarrier();  
  
incrementCounter(PeerCounter.TIME_IN_SYNC_MS,  
    (System.currentTimeMillis() - startBarrier));  
incrementCounter(PeerCounter.SUPERSTEP_SUM, 1L);  
  
currentTaskStatus.setCounters(counters);  
  
if (this.faultToleranceService != null) {  
  try {  
    this.faultToleranceService.afterBarrier();  
  } catch (Exception e) {  
    throw new IOException(e);  
  }  
}  
  
umbilical.statusUpdate(taskId, currentTaskStatus); 
```
从第一行的注释即可看出，之前send要发送的消息在开始同步时才会真正地发送出去。此外，貌似在 
```java
messenger.clearOutgoingQueues();
```
中会准备好本地的消息队列，之后才可以读取从其他结点发送过来的消息，具体怎么收消息还没研究好，经过实验发现似乎是在sync返回之后，才能接受到从其他节点发送过来的消息，在sync之前getCurrentMessage()得到的消息总是空值。
由此大概得出了结论：**hama bsp在一个超步中只能发消息或者处理上一个超步中接收到的消息。**
此外，Hama源码中org.apache.hama.bsp.message.AbstractMessageManager中，用于接收消息的localQueue是用getQueue方法初始化的，而getQueue默认返回的是MemoryQueue，也就是说：除非配置了使用DiskQueue，Hama bsp会将收到的消息放在内存中。从org.apache.hama.bsp.message.AbstractMessageManager.sned()的实现也可以到，发送队列也是用getQueue初始化的，也就有了和接受队列一样的队列类型。

再者，上一个超步中接收到的数据，必须在紧接着的下一个超步中处理完毕，否则接收队列会被清空。

######Zookeeper的配置问题一例 
hama集群启动后貌似正常，运行ecample：
```java
$ bin/hama jar hama-examples-0.6.0.jar pi  
```
时出错，报无法连接zookeeper：
```java
    13/03/21 01:37:41 INFO bsp.BSPJobClient: Running job: job_201303210137_0001  
    13/03/21 01:37:44 INFO bsp.BSPJobClient: Current supersteps number: 0  
    attempt_201303210137_0001_000007_0: 13/03/21 01:37:16 INFO sync.ZKSyncClient: Initializing ZK Sync Client  
    attempt_201303210137_0001_000007_0: 13/03/21 01:37:16 INFO sync.ZooKeeperSyncClientImpl: Start connecting to Zookeeper! At iir455-199/10.77.30.199:61002  
    attempt_201303210137_0001_000007_0: 13/03/21 01:37:16 ERROR sync.ZooKeeperSyncClientImpl: org.apache.zookeeper.KeeperException$ConnectionLossException: KeeperErrorCode = ConnectionLoss for /bsp/job_201303210137_0001/peers  
    attempt_201303210137_0001_000007_0: 13/03/21 01:37:16 ERROR sync.ZKSyncClient: Error checking zk path /bsp/job_201303210137_0001/peers/iir455-199:61002  
    attempt_201303210137_0001_000007_0: org.apache.zookeeper.KeeperException$ConnectionLossException: KeeperErrorCode = ConnectionLoss for /bsp/job_201303210137_0001/peers/iir455-199:61002  
    attempt_201303210137_0001_000007_0:     at org.apache.zookeeper.KeeperException.create(KeeperException.java:99)  
    attempt_201303210137_0001_000007_0:     at org.apache.zookeeper.KeeperException.create(KeeperException.java:51)  
    attempt_201303210137_0001_000007_0:     at org.apache.zookeeper.ZooKeeper.exists(ZooKeeper.java:1041)  
    attempt_201303210137_0001_000007_0:     at org.apache.zookeeper.ZooKeeper.exists(ZooKeeper.java:1069)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.sync.ZKSyncClient.isExists(ZKSyncClient.java:108)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.sync.ZKSyncClient.writeNode(ZKSyncClient.java:262)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.sync.ZooKeeperSyncClientImpl.registerTask(ZooKeeperSyncClientImpl.java:270)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.sync.ZooKeeperSyncClientImpl.register(ZooKeeperSyncClientImpl.java:250)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.BSPPeerImpl.initializeSyncService(BSPPeerImpl.java:338)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.BSPPeerImpl.<init>(BSPPeerImpl.java:169)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.GroomServer$BSPPeerChild.main(GroomServer.java:1262)  
    attempt_201303210137_0001_000007_0: 13/03/21 01:37:16 ERROR sync.ZKSyncClient: Error creating zk path /bsp/job_201303210137_0001/peers/iir455-199:61002  
    attempt_201303210137_0001_000007_0: org.apache.zookeeper.KeeperException$ConnectionLossException: KeeperErrorCode = ConnectionLoss for /bsp  
    attempt_201303210137_0001_000007_0:     at org.apache.zookeeper.KeeperException.create(KeeperException.java:99)  
    attempt_201303210137_0001_000007_0:     at org.apache.zookeeper.KeeperException.create(KeeperException.java:51)  
    attempt_201303210137_0001_000007_0:     at org.apache.zookeeper.ZooKeeper.exists(ZooKeeper.java:1041)  
    attempt_201303210137_0001_000007_0:     at org.apache.zookeeper.ZooKeeper.exists(ZooKeeper.java:1069)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.sync.ZKSyncClient.createZnode(ZKSyncClient.java:135)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.sync.ZKSyncClient.writeNode(ZKSyncClient.java:282)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.sync.ZooKeeperSyncClientImpl.registerTask(ZooKeeperSyncClientImpl.java:270)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.sync.ZooKeeperSyncClientImpl.register(ZooKeeperSyncClientImpl.java:250)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.BSPPeerImpl.initializeSyncService(BSPPeerImpl.java:338)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.BSPPeerImpl.<init>(BSPPeerImpl.java:169)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.GroomServer$BSPPeerChild.main(GroomServer.java:1262)  
    attempt_201303210137_0001_000007_0: 13/03/21 01:37:16 INFO ipc.Server: Starting SocketReader  
    attempt_201303210137_0001_000007_0: 13/03/21 01:37:16 INFO ipc.Server: IPC Server Responder: starting  
    attempt_201303210137_0001_000007_0: 13/03/21 01:37:16 INFO ipc.Server: IPC Server listener on 61002: starting  
    attempt_201303210137_0001_000007_0: 13/03/21 01:37:16 INFO message.HadoopMessageManagerImpl:  BSPPeer address:iir455-199 port:61002  
    attempt_201303210137_0001_000007_0: 13/03/21 01:37:16 INFO ipc.Server: IPC Server handler 0 on 61002: starting  
    attempt_201303210137_0001_000007_0: 13/03/21 01:37:17 ERROR sync.ZKSyncClient: Error checking zk path /bsp/job_201303210137_0001/sync/-1  
    attempt_201303210137_0001_000007_0: org.apache.zookeeper.KeeperException$ConnectionLossException: KeeperErrorCode = ConnectionLoss for /bsp/job_201303210137_0001/sync/-1  
    attempt_201303210137_0001_000007_0:     at org.apache.zookeeper.KeeperException.create(KeeperException.java:99)  
    attempt_201303210137_0001_000007_0:     at org.apache.zookeeper.KeeperException.create(KeeperException.java:51)  
    attempt_201303210137_0001_000007_0:     at org.apache.zookeeper.ZooKeeper.exists(ZooKeeper.java:1041)  
    attempt_201303210137_0001_000007_0:     at org.apache.zookeeper.ZooKeeper.exists(ZooKeeper.java:1069)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.sync.ZKSyncClient.isExists(ZKSyncClient.java:108)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.sync.ZKSyncClient.writeNode(ZKSyncClient.java:262)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.sync.ZooKeeperSyncClientImpl.enterBarrier(ZooKeeperSyncClientImpl.java:99)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.BSPPeerImpl.doFirstSync(BSPPeerImpl.java:345)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.BSPPeerImpl.<init>(BSPPeerImpl.java:233)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.GroomServer$BSPPeerChild.main(GroomServer.java:1262)  
    attempt_201303210137_0001_000007_0: 13/03/21 01:37:17 ERROR sync.ZKSyncClient: Error creating zk path /bsp/job_201303210137_0001/sync/-1  
    attempt_201303210137_0001_000007_0: org.apache.zookeeper.KeeperException$ConnectionLossException: KeeperErrorCode = ConnectionLoss for /bsp  
    attempt_201303210137_0001_000007_0:     at org.apache.zookeeper.KeeperException.create(KeeperException.java:99)  
    attempt_201303210137_0001_000007_0:     at org.apache.zookeeper.KeeperException.create(KeeperException.java:51)  
    attempt_201303210137_0001_000007_0:     at org.apache.zookeeper.ZooKeeper.exists(ZooKeeper.java:1041)  
    attempt_201303210137_0001_000007_0:     at org.apache.zookeeper.ZooKeeper.exists(ZooKeeper.java:1069)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.sync.ZKSyncClient.createZnode(ZKSyncClient.java:135)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.sync.ZKSyncClient.writeNode(ZKSyncClient.java:282)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.sync.ZooKeeperSyncClientImpl.enterBarrier(ZooKeeperSyncClientImpl.java:99)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.BSPPeerImpl.doFirstSync(BSPPeerImpl.java:345)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.BSPPeerImpl.<init>(BSPPeerImpl.java:233)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.GroomServer$BSPPeerChild.main(GroomServer.java:1262)  
    attempt_201303210137_0001_000007_0: 13/03/21 01:37:17 FATAL bsp.GroomServer: SyncError from child  
    attempt_201303210137_0001_000007_0: org.apache.hama.bsp.sync.SyncException  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.sync.ZooKeeperSyncClientImpl.enterBarrier(ZooKeeperSyncClientImpl.java:137)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.BSPPeerImpl.doFirstSync(BSPPeerImpl.java:345)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.BSPPeerImpl.<init>(BSPPeerImpl.java:233)  
    attempt_201303210137_0001_000007_0:     at org.apache.hama.bsp.GroomServer$BSPPeerChild.main(GroomServer.java:1262)  
    13/03/21 01:37:47 INFO bsp.BSPJobClient: Job failed.  
```
zookeeper用的不是hama自带的，是一个3个节点的集群，查看zookeeper日志（通常在启动zookeeper的用户的home目录下）也是正常的，测试zookeeper:
```java
$ bin/zkCli.sh -server ***:2181  
```
也没有问题。

查看hama的配置文件时发现了问题。hama有两个xml配置文件，hama-site.xml和hama-default.xml.前者可以覆盖后者中的默认配置。

我在hama-site.xml中配置了hama.zookeeper.quorum，却没有配置zookeeper的端口，本以为默认的应该和zookeeper默认的一样，可不想实际上hama默认的zookeeper端口是21810，而不是2181，所以在hama-site.xml中添加：
```java
    <property>  
      <name>hama.zookeeper.property.clientPort</name>  
      <value>2181</value>  
      <description>Property from ZooKeeper's config zoo.cfg.  
        The port at which the clients will connect.  
      </description>  
    </property>  
```
重启hama，运行pi样例，OK了：
```shell
    [iir@iir455-200 hama-0.6.0]$ bin/hama jar hama-examples-0.6.0.jar pi  
    13/03/21 01:47:41 INFO bsp.BSPJobClient: Running job: job_201303210147_0001  
    13/03/21 01:47:44 INFO bsp.BSPJobClient: Current supersteps number: 0  
    13/03/21 01:47:50 INFO bsp.BSPJobClient: Current supersteps number: 1  
    13/03/21 01:47:50 INFO bsp.BSPJobClient: The total number of supersteps: 1  
    13/03/21 01:47:50 INFO bsp.BSPJobClient: Counters: 6  
    13/03/21 01:47:50 INFO bsp.BSPJobClient:   org.apache.hama.bsp.JobInProgress$JobCounter  
    13/03/21 01:47:50 INFO bsp.BSPJobClient:     SUPERSTEPS=1  
    13/03/21 01:47:50 INFO bsp.BSPJobClient:     LAUNCHED_TASKS=21  
    13/03/21 01:47:50 INFO bsp.BSPJobClient:   org.apache.hama.bsp.BSPPeerImpl$PeerCounter  
    13/03/21 01:47:50 INFO bsp.BSPJobClient:     SUPERSTEP_SUM=21  
    13/03/21 01:47:50 INFO bsp.BSPJobClient:     TIME_IN_SYNC_MS=7313  
    13/03/21 01:47:50 INFO bsp.BSPJobClient:     TOTAL_MESSAGES_SENT=21  
    13/03/21 01:47:50 INFO bsp.BSPJobClient:     TOTAL_MESSAGES_RECEIVED=21  
    Estimated value of PI is    3.1463428571428564  
    Job Finished in 10.379 seconds  
```
######获取各个peer(task)的信息、确定master task 
有时候在bsp job中需要确定一个master task，这就需要获取各个peer(task)的信息，如host name、端口号等等。
hama中获取peer的主机名和端口号很方便：
```java
peer.getAllPeerNames(); 
```
这个方法会返回一个String[]，每一项对应一个peer，内容是"hostname:port"，hostname就是peer所在机器的主机名，端口就是这个peer在那台机器上用来和其他peer通信的端口。在单机模式下hostname是local，port是0、1、2、3.。。，分布式模式下hostname是hama配置文件和/etc/hosts文件中相应的主机名，端口号默认配置下是61001、61002.。。等等
可以在hama bsp的setup方法中根据需要选举一个tast作为master，例如:
```java
public void setup(  
        BSPPeer<NullWritable, NullWritable, NullWritable, NullWritable, TupleWritable> peer)  
        throws IOException, SyncException, InterruptedException  
{  
    String[] allPeerNames = peer.getAllPeerNames();  
    int port = 0;  
    Configuration conf = new Configuration("master");  
    String master = conf.getItemValue("master");  
    for (String peerName : allPeerNames)  
    {  
        if (peerName.split(":")[0].equals(master))  
        {  
            if (port == 0 || Integer.parseInt(peerName.split(":")[1]) < port)  
            {  
                port = Integer.parseInt(peerName.split(":")[1]);  
                masterTask = peerName;  
            }  
        }  
    }  
    if (masterTask.equals(peer.getPeerName()))  
    {  
        //向其他peer发送消息  
    }  
} 
```
这里的Configuration是项目中自定义的，不是hama的Configuration。从项目配置文件中读取master所在的主机名，然后选择这台主机上端口号最小的一个作为master。
**注意**：也许有时候在确定了master task之后还需要给其他的peer发送消息，这种情况下不要忘了在发送消息的代码之后加上一个peer.sync()，这个方法在setup方法中也是可以用的。如果不加sync的话，hama在执行完setup方法之后是不会进行同步的，找到hama源码中执行bsp job的一段，即：
单机模式下org.apache.hama.bspLocalBSPRunner类中的run方法：
```java
    peer = new BSPPeerImpl(job, conf, new TaskAttemptID(new TaskID(  
        job.getJobID(), id), id), new LocalUmbilical(), id, splitname,  
        realBytes, new Counters());  
    // Throw the first exception and log all the other exception.  
    Exception firstException = null;  
    try {  
      bsp.setup(peer);  
      bsp.bsp(peer);  
    } catch (Exception e) {  
      LOG.error("Exception during BSP execution!", e);  
      firstException = e;  
    } finally {  
      try {  
        bsp.cleanup(peer);  
      } catch (Exception e) {  
        LOG.error("Error cleaning up after bsp execution.", e);  
        if (firstException == null)  
          firstException = e;  
      } finally {  
        try {  
          peer.clear();  
          peer.close();  
        } catch (Exception e) {  
          LOG.error("Exception closing BSP peer,", e);  
          if (firstException == null)  
            firstException = e;  
        } finally {  
          if (firstException != null)  
            throw firstException;  
        }  
      }  
```
分布式模式下org.apache.hama.bspBSPTask类中的runBSP方法：
```java
    @SuppressWarnings("unchecked")  
    private final static <KEYIN, VALUEIN, KEYOUT, VALUEOUT, M extends Writable> void runBSP(  
        final BSPJob job,  
        BSPPeerImpl<KEYIN, VALUEIN, KEYOUT, VALUEOUT, M> bspPeer,  
        final BytesWritable rawSplit, final BSPPeerProtocol umbilical)  
        throws Exception {  
      
      BSP<KEYIN, VALUEIN, KEYOUT, VALUEOUT, M> bsp = (BSP<KEYIN, VALUEIN, KEYOUT, VALUEOUT, M>) ReflectionUtils  
          .newInstance(job.getConfiguration().getClass("bsp.work.class", BSP.class),  
              job.getConfiguration());  
      
      // The policy is to throw the first exception and log the remaining.  
      Exception firstException = null;  
      try {  
        bsp.setup(bspPeer);  
        bsp.bsp(bspPeer);  
      } catch (Exception e) {  
        LOG.error("Error running bsp setup and bsp function.", e);  
        firstException = e;  
      } finally {  
        try {  
          bsp.cleanup(bspPeer);  
        } catch (Exception e) {  
          LOG.error("Error cleaning up after bsp executed.", e);  
          if (firstException == null)  
            firstException = e;  
        } finally {  
      
          try {  
            bspPeer.close();  
          } catch (Exception e) {  
            LOG.error("Error closing BSP Peer.", e);  
            if (firstException == null)  
              firstException = e;  
          }  
          if (firstException != null)  
            throw firstException;  
        }  
      }  
    }  
```
可见，调用setup之后紧接着就是调用bsp方法，setup中不加sync的话，可能master task正在发送消息，其他的peer已经进入bsp函数执行第一个超步了，这时他们试图读取master task发来的消息是读不到的。当然啦，也可以发第一轮消息发送放在第一个超步中。 
######peers之间通信速度测试 
昨天下午对HDFS的速度进行了测试，晚上又对Hama的peer间通信通信速度进行了测试。
软硬件环境：
和之前的hdfs测试中用的是一样的：http://blog.csdn.net/bhq2010/article/details/8740154
hama安装的是0.6.0版本。
测试过程与结果：
在setup方法中选出一个master task作为主peer
在bsp中写了2个超步，第一个超步读取本地的文件，并将其一部分发送给master
master在第二个超步中接收到其他peer发过来的消息（数据），将其写入本地的文件中。
**有一点需要注意**应该设定bspTask个数为集群中节点的个数，这样通常每个节点上会有且仅有一个bsp任务。少了会使得有些节点上没有bsp任务，多了会使得一个节点上的多个bsp任务同时读取一个文件，然后就挂掉了。
测试程序如下：
```java
import java.io.BufferedReader;  
import java.io.BufferedWriter;  
import java.io.File;  
import java.io.FileReader;  
import java.io.FileWriter;  
import java.io.IOException;  
  
import org.apache.commons.logging.Log;  
import org.apache.commons.logging.LogFactory;  
import org.apache.hadoop.fs.Path;  
import org.apache.hadoop.io.NullWritable;  
import org.apache.hadoop.io.Text;  
import org.apache.hama.HamaConfiguration;  
import org.apache.hama.bsp.BSP;  
import org.apache.hama.bsp.BSPJob;  
import org.apache.hama.bsp.BSPJobClient;  
import org.apache.hama.bsp.BSPPeer;  
import org.apache.hama.bsp.ClusterStatus;  
import org.apache.hama.bsp.FileOutputFormat;  
import org.apache.hama.bsp.NullInputFormat;  
import org.apache.hama.bsp.TextOutputFormat;  
import org.apache.hama.bsp.sync.SyncException;

public class HamaTest
{
	private static Path TMP_OUTPUT = new Path("/tmp/pi"+System.currentTimeMillis());
    
    public static class CommunicationTest extends BSP<NullWritable,NullWritable,Text,Text,Text>
    {
    	public static final Log LOG = LogFactory.getLog(CommunicationTest.class);
        private String masterTask;
        
        @Override
        public void bsp(
        BSPPeer<NullWritable,NullWritable,Text,Text,Text> BSPPeer)
        	throws IOException,SyncException,InterruptedException{
       	File f = new File("/data/external_links_en.txt");
        if(f.exists())
        {
        	int i =0;
            FileReader fr = new FileReader("/data/external_links_en.nt");
            BufferedReader reader = new BufferedReader(fr);
            String line = null;
            while((line = reader.readLine()) != null)
            {
            	i++;
                if(i > 661700)
                {
                	break;
                }
                peer.send(masterTask,new Text(line));   //向master节点发送消息
            }
            reader.close();
        }
        peer.sync();
        
        if(peer.getPeerName().equals(masterTask))
        {
        	Text received;
            FileWriter fw = new FileWriter("/data/tempres");
            BufferedWrtier writer = new BUfferedWriter(fw);
            while((received = peer.getCurrentMessage()) != null)
            {
            	writer.write(received.roString() + "\n");
            }
            writer.close();
        }
        peer.sync();
       }
       
       //setup函数
       public void setup(
       BSPPeer<NullWritable,NullWritable,Text,Text,Text> peer)
                          throws IOException{
       	//选择一个peer作为master
        String[] allPeerNames = peer.getAllPeerNames();
        int port = 0;
        for(String peerName: allPeerNames) {
        	if(peerName.split(":")[0].equals("master_ip"))
            {
            	if(port == 0 || Integer.parseInt(peerName.split(":")[1]) < port){
                port = Integer.parseInt(peerName.split(":")[1]);
                masterTask = peerName;
                }
            }
        }
        
        try{
        	peer.sync();
        }catch(SyncException e){
        	e.printStackTrace();
        }catch(InterruptedException e) {
        	e.printStackTrace();
        }
       }
       
       @Override
       public void cleanup(
       BSPPeer<NullWritable,NullWritable,Text,Text,Text> peer) throws IOException{
       
       }
    }
    
    public static void main(String[] args) throws InterruptedException,IOException,ClassNotFoundException{
    HamaConfiguration conf = new HamaConfiguration();
    
    BSPJob bsp = new BSPJob(conf,HamaTest.class);
    bsp.setJobName("Cnnection Speed Test");
    bsp.setBspClass(CommunicationTest.class);
    bsp.setInputFormat(NullInputFormat.class);
    bsp.setOutputKeyClass(Text.class);
    bsp.setOutputValueClass(Text.class);
    bsp.setOutputFormat(TextOutputFormat.class);
    FileOutputFormat.setOutputPath(bsp,TMP_OUTPUT);
    
    BSPJobClient jobClient = new BSPJobClient(conf);
    ClusterStatus cluster = jobClient.getClusterStatus(true);
    
    if(args.length > 0) 
    {
    	bsp.setNumBspTask(Integer.parseInt(args[0]));
    }else{
    	bsp.setNumBspTask(cluster.getMaxTasks());
    }
    
    long startTime = System.currentTimeMillis();
    if(bsp.waitForCompletion(true))
    {
    	System.out.println("Job Finished in " 
             + (System.currentTimeMillis() -startTime)/1000.0
             + "seconds");
    }
    }
}
```
一共有6个节点上有数据，每个读取前661700行，大约70MB，六个一共410MB，做了5次，第1次和第4次报了java.io.IOException: java.lang.OutOfMemoryError: Java heap space错误，其他用时分别是：117.733秒、127.06秒、94.117秒。报内存不足也不出意料，如果再把数据量加大一点，那么就根本跑不完了，参考http://blog.csdn.net/bhq2010/article/details/8548070，默认配置下Hama是将要发送的和接收到的消息都缓存在内存中的，而主节点上剩余的内存只有3GB左右，运行Hama可能还要吃掉很大一部分。
如果改成只发前66170行，则用时在20秒左右，可见时间消耗主要在通信上。


* 小结：
Hama的peer之间通信速度和健壮性都不理想：
1、从六个节点向一个节点传410MB的消息居然平均用了110秒，去掉启动任务的大约10秒钟，其平均的传输速度只有4MB/s；
2、非常吃内存，剩余将近3GB的内存，竟然跑一个几百兆通信量的Job就会报内存不足，当然这也可能是Hama配置的问题，睡完觉了再查查文档；
所以还是不要用hama本身的同步通信功能传递大量的数据，它只适合在同步计算过程中发送少量的消息。






























