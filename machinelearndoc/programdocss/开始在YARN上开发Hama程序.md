####开始在YARN上开发Hama程序
[toc]
#####要求
当前的hama和hadoop要求JRE为1.6 或以上版本，并且保重集群间的节点可以通过ssh链接
* Hadoop-0.23.x
* Sun Java JDK 1.6.x或者更高

更加详细的版本参照信息，可以参考[对照表][1]
本教程要求已经安装好了hadoop 0.23.0 ，如果没有安装，可以参考官方安装教程：
http://hadoop.apache.org/common/docs/r0.23.0/

####如何运行 Hama-YARN 样例
现将样例中的jar包进行替换
```shell
bin/yarn jar hama-yarn-0.4.0-incubating.jar org.apache.hama.bsp.YarnSerializePrinting
```
当开始运行时，你可以发现产生的应用master日志中正在产生containers。当container生成完毕，你可以在日志中来自他task的hello world

####写一个 Hama-YARN job
此时BSPModel没有改变，但是提交job的方式产生了变化
可以如下的方式提交job
```java
HamaConfiguration conf = new HamaConfiguration();
conf.set("yarn.resourcemanager.address","0.0.0.0:8040");

YARNBSPJob job = new YARNBSPJob(conf);
job.setBspClass(HelloBSP.class);
job.setJarByCLass(HelloBSP.class);
job.setJobName("Serialize Printing");
job.setMemoryUsedPerTaskInMb(50);
job.setNumBspTask(2);
job.waitForCompletion(fasle);
```
就像你看到的，这里启动了YARNBSPJob 而非 BSPJob
YARNBSPJob提供了扩展的API以支持程序运行在YARN上，例如你可以设置每个task用的内存：
```java
job.setMemoryUsedPerTaskInMb(50);
```

####如何配置一个job
为了成功的向YARN集群中提交Hama job，你需要为其配置一些参数。
* 最重要的配置参数是：*yarn.resourcemanager.address*.它指向了你的ResourceManager运行的机器地址(hostname+port),例如：localhost:8040
* 另一个重要的参数BSPApplicationMaster运行所需要的内存大小：
```shell
hama.appmaster.memory.mb
```
默认被设置为 100mb.ApplicaitonMaster所用的总的内存可以如按照下方式计算：
```shell
int memoryInMb = 3*this.getNumBspTask() + conf.getInt("hama.appmaster.memory.mb",100)
```
这样计算的原因是application master 为每个task产生1-3个线程，每个线程将用掉1mb，加上最小内存用量100，如果你碰到了内存问题，可以将该参数设置的高些

####如何提交一个job
#####一般
你有两种方式提交job，既可以使用shell提交一个打好包的jar，或者使用java application提交，两种方式都需要将hama-yarn jar 配置在classpath中或打在当前的jar包中

#####通过shell
```shell
bin/yarn jar /path_to_jar org.apache.hama.bsp.YarnSerializePrinting
```
这种情况下，路径 /path_to_jar 包含了 hama-yarn jar 并且它已经在你的hadoop application的classpath中了，你可以将org.apache.hama.bsp.YarnSerializePrinting换成包含主方法的hamaJob。

#####通过javaapplication
的一样，你需要配置ResourceManager的路径，此时你可以在一个javaApplication中运行这个job，只需要将其放在mian方法中
```java
HamaConfiguarion conf = new HamaConfiguration();
conf.set("yarn.resourcemanager.address","0.0.0.0:8040");

YARNBSPJob job = new YARNBSPJob(conf);
job.setBspByClass(HelloBSP.class);
job.setJarByClass(HelloBsp.class);
job.setJobname("Serialize Printing");
job.setNumBspPerTaskInMb(50);
job.setNumBspTask(2)
job.waitForComletion(false);
```

####如何将早期的hama修改以运行在YARN上
对于早期的code：
```java
//BSP job configuration
HamaConfiguration conf = new HamaConfiguration();
BSPJob bsp = new HamaConfiguration();
BSPJob bsp = new BSPJob(conf);
bsp.waitForCompletion(true)
```
提交时，你可以仅仅将BSPJob 改为YARNBSPJob

#####如何对BSP application进行debug

对分布式程序进行debug一直很困难，因为很少有debug工具允许你连接远程的没有正确运行的机器。
1、开始在本地用小数聚集进行测试运行
在本地运行你需要在config中将bspmaster设置为 “local”。程序在本地运行时，会在debugger的监控下运行。
```shell
conf.set("bsp.master.address","local");
```
在本地模式下执行默认会给job分配20个线程。这样也不利于bug的追踪，你可以减少线程的数目：
```shell
conf.set("bsp.local.tasks.maximum","2")
```
显然，这个配置将tasks设置为2个。
你还可以将输出文件设置为本地文件系统而不是hadoop的分布式系统上。
```java
conf.set("fs.default.name","local');
```
你还可以在hama-site.xml设置上述属性，这个配置文件应该出现在程序运行的classpath中。

2、单节点上运行分布式算法，它将会暴露出运行在集群上的会出现的问题，但是可以在单节点上观察运行log，在这些允许你个日志中最重要的是 grooms和bspmaster的log，确保你的日志运行在info等级下，不然会错过有用的调试信息。

#####使用 bsp application中的log4j
首先，你需要引入log4j客户端api 到你的BSP application的头部
```java
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
```
下面的例子在bsp（）方法的内部使用INFO等级记录程序信息 ：
```java
public static class MyEstimator extends
				BSP<NullWritable,NullWritable,Text,DoubleWritable,DoubleWritable> {
.....
public static final Log LOG = logFactory.getLog(MyEstimator.class);
....

@Override
public void bsp(
BSPPeer<NullWrtable,NullWritable,Text,DoubleWritable,DoubleWritable> peer) throws IOException,SyncException,InterruptedException{

int in = 0;
for (int i = 0; i < iterations; i++) {
double x = 2.0 * Math.random() - 1.0, y = 2.0*Math.random() - 1.0;
if((Math.sqrt(x*x+y*y) < 1.0)){
	in++;
}
}

double data = 4.0*in/iterations;

LOG.info(peer.getPeerName() + ":Logging test:" + data);
peer.send(masterTask,new DoubleWritable(data));
peer.sync();
}
                }
```
在Hama的本地模式下，你将会在console 中发现INFO 信息：
```shell
edward@udanax:~/workspace/hama-trunk$ bin/hama jar examples/target/hama-examples-0.7.0-SNAPSHOT.jar pi
13/05/14 16:02:32 INFO mortbay.log: Logging to org.slf4j.impl.Log4jLoggerAdapter(org.mortbay.log) via org.mortbay.log.Slf4jLog
13/05/14 16:02:32 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
13/05/14 16:02:32 INFO bsp.BSPJobClient: Running job: job_localrunner_0001
13/05/14 16:02:32 INFO bsp.LocalBSPRunner: Setting up a new barrier for 10 tasks!
13/05/14 16:02:32 INFO examples.PiEstimator$MyEstimator: local:6: Logging test: 3.1412
13/05/14 16:02:32 INFO examples.PiEstimator$MyEstimator: local:9: Logging test: 3.1308
13/05/14 16:02:32 INFO examples.PiEstimator$MyEstimator: local:5: Logging test: 3.1304
13/05/14 16:02:32 INFO examples.PiEstimator$MyEstimator: local:4: Logging test: 3.1756
13/05/14 16:02:32 INFO examples.PiEstimator$MyEstimator: local:3: Logging test: 3.1444
13/05/14 16:02:32 INFO examples.PiEstimator$MyEstimator: local:8: Logging test: 3.1452
13/05/14 16:02:32 INFO examples.PiEstimator$MyEstimator: local:0: Logging test: 3.1468
13/05/14 16:02:32 INFO examples.PiEstimator$MyEstimator: local:1: Logging test: 3.1684
13/05/14 16:02:32 INFO examples.PiEstimator$MyEstimator: local:2: Logging test: 3.1256
13/05/14 16:02:32 INFO examples.PiEstimator$MyEstimator: local:7: Logging test: 3.114
13/05/14 16:02:35 INFO bsp.BSPJobClient: Current supersteps number: 0
13/05/14 16:02:35 INFO bsp.BSPJobClient: The total number of supersteps: 0
13/05/14 16:02:35 INFO bsp.BSPJobClient: Counters: 7
13/05/14 16:02:35 INFO bsp.BSPJobClient:   org.apache.hama.bsp.JobInProgress$JobCounter
13/05/14 16:02:35 INFO bsp.BSPJobClient:     SUPERSTEPS=0
13/05/14 16:02:35 INFO bsp.BSPJobClient:     LAUNCHED_TASKS=10
13/05/14 16:02:35 INFO bsp.BSPJobClient:   org.apache.hama.bsp.BSPPeerImpl$PeerCounter
13/05/14 16:02:35 INFO bsp.BSPJobClient:     SUPERSTEP_SUM=10
13/05/14 16:02:35 INFO bsp.BSPJobClient:     TIME_IN_SYNC_MS=59
13/05/14 16:02:35 INFO bsp.BSPJobClient:     TOTAL_MESSAGES_SENT=10
13/05/14 16:02:35 INFO bsp.BSPJobClient:     TOTAL_MESSAGES_RECEIVED=10
13/05/14 16:02:35 INFO bsp.BSPJobClient:     TASK_OUTPUT_RECORDS=1
Estimated value of PI is        3.14224
Job Finished in 3.141 seconds
```
在Hama的分布式模式下，每一个BSP task 产生它自己的log文件在  {$HAMA_HOME}/logs/tasklogs  文件夹下：
```shell
edward@udanax:~/workspace/hama-trunk$ bin/hama jar examples/target/hama-examples-0.7.0-SNAPSHOT.jar pi
13/05/14 16:14:13 INFO mortbay.log: Logging to org.slf4j.impl.Log4jLoggerAdapter(org.mortbay.log) via org.mortbay.log.Slf4jLog
13/05/14 16:14:14 INFO bsp.BSPJobClient: Running job: job_201305141614_0001
13/05/14 16:14:17 INFO bsp.BSPJobClient: Current supersteps number: 0
13/05/14 16:14:20 INFO bsp.BSPJobClient: Current supersteps number: 1
13/05/14 16:14:20 INFO bsp.BSPJobClient: The total number of supersteps: 1
13/05/14 16:14:20 INFO bsp.BSPJobClient: Counters: 6
13/05/14 16:14:20 INFO bsp.BSPJobClient:   org.apache.hama.bsp.JobInProgress$JobCounter
13/05/14 16:14:20 INFO bsp.BSPJobClient:     SUPERSTEPS=1
13/05/14 16:14:20 INFO bsp.BSPJobClient:     LAUNCHED_TASKS=3
13/05/14 16:14:20 INFO bsp.BSPJobClient:   org.apache.hama.bsp.BSPPeerImpl$PeerCounter
13/05/14 16:14:20 INFO bsp.BSPJobClient:     SUPERSTEP_SUM=3
13/05/14 16:14:20 INFO bsp.BSPJobClient:     TIME_IN_SYNC_MS=243
13/05/14 16:14:20 INFO bsp.BSPJobClient:     TOTAL_MESSAGES_SENT=3
13/05/14 16:14:20 INFO bsp.BSPJobClient:     TOTAL_MESSAGES_RECEIVED=3
Estimated value of PI is        3.1460000000000004
Job Finished in 6.396 seconds
edward@udanax:~/workspace/hama-trunk$ cat logs/tasklogs/job_201305141614_0001/attempt_201305141614_0001_00000
attempt_201305141614_0001_000000_0.err  attempt_201305141614_0001_000001_0.err  attempt_201305141614_0001_000002_0.err  
attempt_201305141614_0001_000000_0.log  attempt_201305141614_0001_000001_0.log  attempt_201305141614_0001_000002_0.log  
edward@udanax:~/workspace/hama-trunk$ cat logs/tasklogs/job_201305141614_0001/attempt_201305141614_0001_000000_0.log 
...
13/05/14 16:14:16 INFO examples.PiEstimator$MyEstimator: localhost:61003Logging test: 3.1496
...
```






#####Hama BSP Graph教程

######1、BSP 
Hama提供纯BSP模型，支持消息传递与全局通信。BSP模型由一系列超步组成，每一个超步包含以下3个部分：
  * 本地计算
  * 进程通信
  * 障栅同步
针对大量的科学计算问题，使用BSP模型可以编写高性能的并行计算算法。

**通过继承 org.apapche.hama.bsp.BSP**类。创建自己的BSP类

继承类必须实现以下方法：
```java
public abstract void bsp(BSPPeer<K1,V1,K2,V2,M extends Writable> peer) throws IOException ,SyncException,InterruptedException{

}
```

每个BSP程序由一系列的超步组成，但是BSP方法只被调用一次，这一点同mapre不同。在计算的前后，可以选择实现setup() 和 cleanup()方法，对每次计算的数据作进一步的处理。建议在计算结束或者计算失败时执行cleanup（）

* 配置job
```java
HamaConfiguration conf = new HamaConfiguration();
BSPJob job = new BSPJob(conf,MyBSP.class);
job.setJobName("My BSP program");
job.setBspClass(MyBSP.class);
job.setInputFormat(NullInputFormat.class)
job.setOutputKeyClass(Text.class)
...
job.waitForCompletion(true);
```

* 用户接口
  * 输入输出
  对BSPJob进行设置时，输入输出的路径形式如下：
  ```java
  job.setINputPath(new Path("/tmp/sequence.dat"));
  job.setInputFormat(org.apache.hama.bsp.SequenceFileInputFormat.class);
  or,
  SequenceFileInputFormat.addInputPath(job,new Path("/tmp/sequence.data"));
  or,
  SequenceFileInputFormat.addInputPath(job,"/tmp/seq1.dat,/tmp/seq2.dat,/tmp/seq3.dat");
  job.setOutputKey(Text.class);
  job.setOutputValueClass(IntWritalbe.class);
  job.setOutputFormat(TextOutputFormat.class);
  FileOutputFormat.setOutputPath(job,new Path("/tmp/result"));
  ```
  以上三种方式可以任选一种作为输入代码。
  然后，对输入的数据的读取和输出数据。BSP创建一个方法，以BSPPeer作为参数。BSPPeer包含了通信、计数、和IO接口。读取一个文件代码如下：
  ```java
  @Oerride
  public final void bsp(
  BSPPeer<LongWritable,Text,Text,LongWritalbe,Text> peer)
  				throws IOException,InterruptedException,SyncException{
     
     //这个方法从文件中读取下一个key-value记录
     KeyValuePair<LongWritable,Text> pair = peer.readNext();
     
     //这是上面方法的另一种实现方式
     LongWritalb key = new LongWritalbe();
     Text value = new Text();
     
     peer.readNext(value,key);
     
     //write
     peer.write(value,key);
      }
  ```
  
  可以对输入文件进行重复读取
  
  ```java
  for(int i = 0 ; i < 5; i++) {
  	LongWritalbe key = new LongWritable();
    Text value = new Text();
    while(peer.readNext(key,value)) {
    	//读取所有的东西
    }
    //重新读取文件
    peer.reopenInput()
  }
  ```
  * 通信
  |方法|描述|
  |---|---|
  | send(String peerName,BSPMessage msg) | 向另外一个peer发送消息 |
  | getCurrentMessage() | 返回接收到的消息|
  | getNumCurrentMessage() | 返回接收到的消息数|
  | sync() |障栅同步 |
  | getPeerName() |返回peer的名称| 
  | getAllPeerNames()|返回所有peer的名称|
  | getSuperstepCount()|返回超步数| 
  
  以上方法都比较灵活，下面是一个向所有peer传递消息的代码
  ```java
  public void bsp(
  BSPPeer<NullWritable,NullWritalbe,Text,DoubleWritalbe,Text> peer)
  			throws IOException,SyncException,InterruptedExcetion{
    for(String peerName : peer.getAllPeerNames()) {
    	peer.send(peerName,new Text("Hello from" + peer.getPeerName(),System.currentTimeMillis()));
    }        
    peer.sync()    //同步一次
    }
  ```
* 同步
当所有的进程都进入同步状态，接下来就进入下一个超步。需要注意的是，sync（）方法并不是BSP Job的结束。如前所述，所有的通信方法都非常灵活。例如，可以在一个for循环中执行sync（），这样就可以对迭代顺寻进行控制

```java
@Override
public void bsp(
BSPPeer<NullWritable,NullWritable,Text,DoubleWritable,Text> peer)
			throws IOException,SyncException,InterruptedException{
 for (int i = 0; i < 100; i++) {
 	//send some messages
    peer.sync();
 }           
 }
```

下面给出一个求取PI值的完整例子：
```java
private static TMP_OUTPUT = new Path("/tmp/pi-" System.currentTimeMillis());

public static class MyEstimator extends BSP<NullWritable,NullWritable,Text,DoubleWritable,DoubleWritable> {
  public static Log LOG = LogFactory.getLong(MyEstimate.class);
  private String masterTask;
  private static finall int iterations = 10000;
  
  @Override
  public void bsp(
  BSPPeer<NullWritable,NullWritalbe,Text,DoubleWritable,DoubleWritable> peer) throws IOException,SyncException,InterruptedException {
  int in = 0;
  for(int i = 0; i < iterations;i++) {
  	double x = 2.0 * Math.random() - 1.0 ,y = 2.0 * Math.random() - 1.0;
    if ((Math.sqrt(x*x+y*y) < 1.0)) {
    	in++;
    }
  }
  
  double data = 4.0 * in/iterations;
  
  peer.send(masterTask,new DoubleWrtalbe(data));
  peer.sync();
  }
  
  @Override
  piublic void setup(BSPPeer<NullWritalbe,NullWritable,Text,DoubleWritable,DoubleWritable> peer) throws IOException{
  	//选出master节点
    this.masterTask = peer.getPeerName(peer.getNumPeers()/2);
  }
  
  @Override
  piublic void cleanup( BSPPeer<NullWritable,NullWritalbe,Text,DoubleWritalbe,DoubleWritable> peer) throws IOException {
  	 if(peer.getPeerName().equals(masterTask)) {
     	double pi = 0.0;
        int numPeers = peer.getNumCurrentMessage();
        DoubleWritable received;
        while((received = peer.getCurrentMessage()) != null) {
        	pi += received.get();
        }
        
        pi = pi/numPeers;
        peer.write(new Text("Estimate value of PI is"),new DoubleWritalbe(pi));
     }
  }
}

//从临时文件中读取结果输出到控制台，然后删除临时文件
static void printOutput(HamaConfiguration conf) throws IOException {
	FileSystem fs = FileSystem.get(conf);
    FileStatus[] files = fs.listStatus(TMP_OUTPUT);
    for(int i=0; i < file.length;i++) {
    	if(files[i].getLen() > 0) {
        	FSDataInputStream in = fs.open(files[i].getPath());
            IOUtils.copyBytes(in,System.out,conf,false);
            in.close();
            break;
        }
    }
    fs.delete(TMP_OUTPUT,true);
}

public static void main(String[] args) throws InterruptedException,
	IOException,ClassNotFoundException {
    //BSP job configuration
    HamaConfiguration conf = new HamaConfiguraton();
    
    BSPJob bsp = new BSPjob(conf,PiEstimator.class);
    //set the job name
    bsp.setJobName("pi Estimation Example");
    bsp.setBspClass(MyEstimate.class);
    bsp.setInputFormat(NullInputFormat.class);
    bsp.setOutputKeyClass(Text.class);
    bsp.setOutputValueClass(DoubleWritalbec.class);
    bsp.setOutputFormat(TextOutputFormat.class);
    FileOutputFormat.setOutputPath(bsp,TMP_OUTPUT);
    
    BSPJobClient jobclient = new BSPJobClient(conf);
    ClusterStatus cluster = jobClient.getClusterStatus(true);
    
    //设置task的个数
    if(args.length > 0) {
    	bsp.setNumBspTask(Integer.parseInt(args[0]))  
    }else{
    	bsp.setNumBspTask(cluster.getMaxTasks());
    }
    
    long startTime = System.CurrentTimeMillis();
    if(bsp.waitForCompletion(true)) {
    	printOutput(conf);
        System.out.println("Job Finished in" + (System.currentTimeMillis() - startTime)/1000.0+ "seconds");
    }
    }
```

* graph  图形计算
hama提供了Graph包，支持定点为中心的图计算，使用较少的代码就可以实现gogle Pregel风格的应用。

* Vertex API
 实现一个Hama Graph应用包括对预定义的Vertex类进行子类化，模板参数设计3种类型，定点，边、和消息（Vertices,edges,and messages）: 
 
```java
public static class Vertex<V extends Writable, E extends Writable,M extends Writable> implements VertexInterface<V,E,M>{

public void compute(Iterator<M> messages) throws IOException;
}
```

用户重写compute（）方法，该方法在每个超步的活跃定点中执行。Compute（）方法可以查询当前定点及边的信息，并向其他定点发送消息。

* VertexReader API
通过集成 org.apache.hama.graph.VertexInputReader 类，根据自己的文件格式创建自己的VertexReader类，示例：
```java
public static class PagerankTextReader extends 
		VertexInputReader<LongWritable,Text,Text,NullWritable,DoubleWritable> {
 /**
 输入文件格式：
 *输入文件的基本元素如下：
 * VERTEX——ID\t(n-tab separated VERTEX_IDs)
 *例如
 *1\t2\t3\t4
 *2\t3\t1
 *
 */ 
 /**
 *解析节点，同hadoop类似，以一个行为单位输入，以制表符作为分隔符，
 *将每一行分割为String类型的数组，最后转化为vertex类的一个实例
 */
 public boolean parseVertex(LongWritable key,Text value,
 		Vertex<Text,NullWritable,DoubleWritable> vertex) throws Exception{
  String[] split = value.toString().split("\t");
  for(int i=0; i < split.length;i++) {
  	if(i == 0) {
    	vertex.setVertexID(new Text(split[i]));
    }else {
    	vertex.addEdge(new Edge<Text,NullWritable>(new Text(split[i]),null));  //注：null的位置是边的权重
    }
  }
  return true;
  }
 }
```

PageRank的例子，

```java
public static class PageRankVertex extends 
			Vertex<Text,NullWritable,DoubleWritable> {
   
   @Override
   public void compute(Iterator<DoubleWritable> messages) throws IOException {
   if(this.getSuperstep() == 0) { //当是第一次超步计算时
   	this.setValue(new DoubleWritalbe(1.0/(double) this.getNumVertices()));
   }
   
   if(this.getSuperStepCount() >= 1) { //如果不是第一个超步
   	double sum = 0;
    while(messages.hasNext()) {   //取出所有的消息，进行处理
    	DoubleWritable msg = message.next();
        sum += msg.get();
    }
    
    double ALPHA = (1 - 0.85) / (double) this.getNumVertices();
    this.setValue(new DoubleWritalbe(ALPHA+(0.85 * sum)));
   }
   
   if(this.getSuperstepCount() <  this.getMaxIteration()) {
   	int numEdges = this.getOutEdges().size();    //获取相邻节点序列的长度
    sendMessageToNeighbors(new DoubleWritable(this.getValue().get()/numEdges)); //项相邻节点发送消息
   }
   } 
   }
```


####节点之间发送消息的例子
1. Serialize Printing of "Hello BSP"

```Java
public class ClassSerializePrinting extends BSP<NullWritable,NullWritable,IntWritable,Text> {
	public static final NUM_SUPERSTEPS = 15;
    
    @Override
    public void bsp(BSPPeer<NullWritable,NullWritable,IntWritable,Text> bspPeer)
         throws IOExcpetion,SyncException,InterruptedException{
    	
        for(int i = 0; i < NUM_SUPERSTEPS; i++) {
          for(String otherPeer: bspPeer.getAllPeerNames()) {
          bspPeer.send(otherPeer,new IntegerMessage(bspPeer.getPeerName(),i));
          }
          
          bspPeer.sync();
          
          while((msg = (IntegerMessage) bspPeer.getCurrentMessage()) != null) {
          bspPeer.write(new IntWritable(msg.getData()),new Text(msg.getTag()));
          }
        }
    }
}
```

####计数器
同mapreduce一样你可以使用计数器
计数器是一个枚举类型，只可以增加，你可以使用他们在你的代码中做一些有意义的度量（metric），例如loop执行的频数。
下面是一个使用计数器的例子：
```java
//enum definition
enum LoopCounter{
LOOPS
}

@Override
public void bsp(
BSPPeer<NullWritable,NullWritable,Text,DoubleWritable,DoubleWritable> peer) throws IOException,SyncException,InterruptedException{
	for(int i = 0; i < iterations; i++) {
    	//details ommitted
        peer.getCount(LoopCOunter.LOOPS).increment(1L);
    }
    //reset ommited
}
```

####set和cleanup
从4，0开始你可以使用Setup和Cleanup方法，下面是使用的例子：
```java
public class MyEstimator extends BSP<NullWritable,NullWritable,Text,DoubleWritable,DoubleWritable> {
	@Override
    public void setup(
    BSPPeer<NullWritable,NullWritable,Text,DoubleWritable,DoubleWritable>)
    throws IOException{
    	//Setup:Choose one as a master
        this.masterTask = peer.getPeerName(peer.getNumPeers()/2);
    }
    
    @Override
    public void cleanup(
    BSPPeer<NullWritable,NullWritable,Text,DoubleWritable,DoubleWritable>)
    throws IOException{
    //your cleanup here
    }
    
    @Override
    public void bsp(
    BSPPeer<NullWritable,NullWritable,Text,DoubleWritable,DoubleWritable>)
    throws IOException,SyncExcpetion,InterruptedException{
    //your computation here
    }
}
```
Setup方法在bsp方法之前调用，并且cleanup方法在bsp方法之后执行，你可以在setup和cleanup方法中执行所有的操作： sync，send ，increment counters，向输出写 甚至从输入读。

####combiners
combiners被用在通信的信息可以用算法进行聚合时使用来聚集信息以减少节点之间的通信量，例如：
min,max,sum和average,注意它是使用在sender端的。假设下面的场景：你想要向接受节点发送0到1000并且在接受节点统计所有节点发送的数值信息

```java
public void bsp(BSPPeer<NullWritable,NullWritable,NullWritable,NullWritable,IntegerMessage> peer) throws IOException,SyncException,Interruption {
	for(int i = 0; i < 1000; i++) {
    	peer.send(masterTask,newIntegerMessage(peer.getPeerName(),i));
    }
    peer.sync();
    
    if(peer.getPeerName().equals(masterTask)) {
    	IntegerMessage received;
        while((received = peer.getCurrentMessage()) != null) {
        	sum += reveived.getData();
        }
    }
}
```
如果你继续使用从前的方法，，诶个bsp处理线程，都将发送1000的整数信息到masterTask，你可以使用Combiner 来sum整数信息，从而发送更加简介的信息，使得程序更加安全，下面是combines的例子：
```java
public static class SumCombiner extends Combiner{
	@Override
    public BSPMessageBundle combine(Iterable<BSPMessage> messages){
    BSPMessageBundle bundle = new BSPMessageBundle();
    int sum=0;
    
    Iterator<BSPMessage> it = messages.iterator();
    while(it.hasNext()) {
    sum += ((IntegerMessage) it.next()).getData();
    }
    
    bundle.addMessage(new IntegerMessage("Sum",sum));
    return bundle;
    }
}
```

####HAMA的执行流程
BSPJobClient 
1. 为job创建split
2. writeNewSplits()
3. job.set("bsp.job.split.file",submitSplitFile.toString());
4. 将执行任务的peer设置为 split.length、

执行过程中（JobInProgress）：
1. 接受split
2. 将split参数添加到TaskInprogress 的构造器中

任务 （Task）:
1. 从Groom中获取split
2. 初始化所有的东西

####Design of grpah module
Hama包含Grsph模块，支持以点为中心的图形计算，它类似于google的Pregel 类型的计算模型

######介绍
Graph API在bsp框架的顶层实现，它包括三个主要的类型： 
1. VertexInputReader :
    它支持中任意的文本数据或者二进制数据中读取并构造节点类型
     the loadVertices() method reads the records from assigned split, and then loads the converted Vertex objects by the user-defined VertexInputReader.parseVertex() method into memory Vertices storage.
2. GraphJob： 他负责Graph job的配置
GraphJob provides some additional Get/Set methods extending the core BSPJob interface for supporting the Graph specific configurations, such as setMaxIteration, setAggregatorClass, setVertexInputReaderClass, and setVertexOutputWriterClass. Rest APIs e.g., InputFormat, OutputFormat etc. are the same with core BSPJob interface
3. GrpahJobRunner： 它调度执行Vertex的compute方法
GraphJobunner 是与BSP程序交互的核心，它执行在Vertex.compute()中执行的method，并且创建输出和输入。同普通的BSP程序一样，它由3个方法组成： setup(), cleanup() , bsp().
* setup() 阶段： 初始化
* bsp()阶段： he main computations of the vertices. The message communications among vertices are also handled by BSP communication interface in this phase.
* cleanup() phase: output write phase after completing the computations of the vertices.



####动态图：删除和添加节点
要注意的问题：
1. 删除和增加节点 发生在一个super step之后。
2. ertexAPI 是建立杂BSP Peer之上的，这意味着：你集群的每个节点包含指定数目的BSP peer，并且每个BSP peer 包含多个节点
3. 添加新的vertex需要通过partioner来分发（distrubuted）到正确的peers。
4. 新的和旧的vertex，将包含童谣的super step的计数器，许多算法根据时间来改变它的行为，这时你需要开发自己的计数器

This is an example of how to manipulate Graphs dynamically. The input of this example is a number in each row. We assume that the is a vertex with ID:1 which is responsible to create a sum vertex that will aggregate the values of the other vertices. During the aggregation, sum vertex will delete all other vertices.

Input example:

1
2
3
4
Output example:

sum 12
we also add the number of vertices that exist in the last superstep from two different methods

```java
public class DynamicGraph{

//加载类
	public static GraphTextReader extends VertexInputReader<LongWritable,Text,Text,NullWritable,IntWritable> {
    @Overred
    public booelan parseVertex(LongWritable key, Text value,
    Vertex<Text,NullWritable,IntWritable> vertex) throws Exception{
    vertex.setVertexID(value);
    vertex.setValue(new IntWritable(Intger.parseInt(value.toString())));
    return true
    }
    }
    
  public static class GraphVertex extends Vertex<Text,NullWritable,IntWritable> {
  
  //创建sum节点
  private void createSumVertex() throws IOException{
  if(this.getVertexID().toString().equals("1")){
  Text new_id = new Text("sum");
  this.addVertex(new_id,new ArrayList<Edge<Text,NullWritable>>(),new IntWritable(0));
  }
  }
  //向sum节点发送自己的信息并删除自己
  private void sendAllValuesToSumAndRemove() throws IOException{
  	if(!this.getVertexID().toString().equals("sum")) {
    this.sendMessage(new Text("sum"),this.getValue());
    this.remove();
    }
  }
  
  //这个是在sum节点上运行的
  private void calculateSum(Iterable<IntWritable> msgs) throws IOException{
  if(this.getVertexID().toString().equals("sum")){
  int s = 0;
  for (IntWritable i : msgs) {
  s += i.get();
  }
  s += this.getPeer().getCounter(GraphJobCounter.INPUT_VERTICES).getCounter();
  s += this.getNumVertices();
  this.setValue(new IntWritable(this.getValue().get() + s));
  }else{  
  throw new UnsupportedOperationException("We have more vertecies than we expected: " + this.getVertexID() + " " + this.getValue()); 
  }
  }
  
  //主体类
  @Override
  public void compute(Iterable<IntWritable> msgs) throws IOException{
  	if (this.getSuperstepCount() == 0) {
    	createSumVertex();    //定义master节点
    }else if(this.getSuperstepCount() == 1) {
    	sendAllValuesToSumAndRemove();  //向master节点发送消息
    }else if(this.getSuperstepCount() == 2){
    	calculateSum( msgs);            //汇总计数
    }else {
        this.voteToHalt();              //停机
    }
  }
  }
  
  public static void main(String[] args) throws IOException,
  		InterruptedException,ClassNotFoundException{
  if(args.length != 2) {
  	printUsage();
  }
  
  HamaConfiguration conf = new HamaConfiguration(new Configuration());
  GraphJob graphjob = createJob(args,conf);
  long startTime = System.currentTimeMillis();
  if(graphJob.waitforCompletion(true)){
  System.out.println("Job Finished in" +
           (System.currentTimeMillis() -startTime)/1000.0 + "seconds");
  }
  }
  
  private static void printUsage(){
  System.out.println("Usage:<input><output>");
  System.eixt(-1);
  }
  
  private static GraphJob createJob(String[] args, HamaConfiguration conf) throws IOException {
  GraphJob graphJob = new GraphJob(conf,DynamicGraph.class);
  graphJob.setJobName("Dynamic Graph");
  graphJob.setVertexClass(GraphVertex.class);
  
  graphJob.setInputPath(new Path(args[0]));   //设置输入路径
  graphJob.setOutputPath(new Path(args[1]));  //设置输出路径
  
  graphJob.setVertexIDClass(Text.class);
  graphJob.setVertexValueClass(IntWritable.class);   //设置点值
  graphJob.setEdgeValueClass(NullWritable.class);    //设置边权重类型
  
  graphJob.setInputFormat(TextInputFormat.class);
  
  graphJob.setVertexInputReaderClass(GraphTextReader.class);  //设置点和边的初始化类
  graphJob.setPartitioner(HashPartitioner.class);    //设置partitioner
  
  graphJob.setOutputFormat(TextOutputFormat.class);
  graphJob.setOutputKeyClass(Text.class);
  graphJob.setOutputValueClass(IntWritable.clas);
  
  return graphJob;
  }
}
```
对上面的说明：
DynamicGraph 包含了三个重要的部分：

第一个部分是 定义 input reader  ，这个class重写了parseVertex方法以用于从inputfile读入数据来构建图中的边和定点。
第二个部分是一个标准的boilerplate 并提交一个Job
第三个是最重要的部分 GraphVertex class，这个是最重要的部分，它包含了计算方法：
它包含了三个子方法：
   1. In the createSumVertex method we can see the creation of a new vertex with ID the text sum and value 0.
   2. sendAllValuesToSumAndRemove we can see that each vertex that runs this method is deleting itself by running this.remove().
   3. calculateSum is called that summarizes the values of all vertices in the sum vertex. The interesting part of the last method is that also adding the number of input vertices this.getPeer().getCounter(GraphJobCounter.INPUT_VERTICES).getCounter() and the current number of vertices that exist on the running superstep this.getNumVertices().

执行的流程是：
addVertex --> create new vertex instance --> find a proper BSP peer to host the new vertex through the partitioner --> serialize the new vertex and send it as a map message to the peer --> the destination peer, on the new superstep, during executing parseMessages method, will receive the serialized object ---> GraphJobRunner::addVertex will add the new vertex in the data structure that keeps all vertices (VerticesInfo)





























































[1]:http://wiki.apache.org/hama/CompatibilityTable