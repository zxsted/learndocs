hadoop 分布式缓存

[toc]

#### 一 、 应用在分布式缓存中
[传送](http://blog.csdn.net/jokes000/article/details/7084351)


##### 概念：

下面我们先通过一个表格来看下，在hadoop中，使用全局变量或全局文件共享的几种方法
[传送](http://blog.itpub.net/29754888/viewspace-1220340/)
[分布式缓存API](http://blog.sina.com.cn/s/blog_72ef7bea0101c5eg.html)

序号 	方法
1 	使用Configuration的set方法，只适合数据内容比较小的场景
2 	将共享文件放在HDFS上，每次都去读取，效率比较低
3 	将共享文件放在DistributedCache里，在setup初始化一次后，即可多次使用，缺点是不支持修改操作，仅能读取 



reduce-side join技术是灵活的，但是有时候它仍然会变得效率极低。由于join直到reduce()阶段才会开始，我们将会在网络中传递shuffle所有数据，而在大多数情况下，我们会在join阶段丢掉大多数传递的数据。因此我们期望能够在map阶段完成整个join操作。
##### 主要技术难点：
在map阶段完成join的主要困难就是mapper可能需要与一个它自己不能获得的数据进行join操作，如果我们能够保证这样子的数据可被mapper获得，那我们这个技术就可用。举个例子，如果我们知道两个源数据被分为同样大小的partition，而且每个partition都以适合作为join key的key值排序的话，那每个mapper()就可以获取所有join操作需要的数据。事实上，Hadoop的org.apache.hadoop.mared.join包中包含了这样的帮助类来实现mapside join，但不幸的是，这样的情况太少了。而且使用这样的类会造成额外的开销。因此，我们不会继续讨论这个包。
##### 什么情况下使用？
情况1：如果我们知道两个源数据被分为同样大小的partition，而且每个partition都以适合作为join key的key值排序

情况2：当join大型数据时，通常只有一个源数据十分巨大，另一个数据可能就会呈数量级的减小。例如，一个电话公司的用户数据可能只有千万条用户数据，但他的交易记录数据可能会有十亿条数量级以上的具体电话记录。当小的数据源可以被分配到mapper的内存中时，我们可以获得效果明显的性能提高，只要通过将小的数据源拷贝到每一台mapper机器上，使mapper在map阶段就进行join操作。这个操作就叫做replicate join。

##### 解决方案：
Hadoop有一个叫做分布式缓存(distributed cache)的机制来将数据分发到集群上的所有节点上。它通常用来分发所有mapper需要的包含“background”数据的文件。例如你使用Hadoop来分类文档，你可能会有一个关键字的列表，你将使用distributed cache来保证所有mapper能够获得这些keywords（"background data"）。
操作步骤：
1.将数据分发到每个节点上：

```shell
DistributedCache.addCacheFile(new Path(args[0]).toUri(),conf);
```

2. 在每个mapper上使用DistrubutedCaceh.getLocalCacheFiles() 来获取文件，之后再进行相应的操作

```java
DistributedCache.getLocalCacheFiles();
```


#### 二、 Yarn（MapReduce 2.0）下分布式缓存（DistributedCache）的注意事项

##### 1、问题
最近公司的集群从 Apache hadoop 0.20.203 升级到了 CDH 4，迈进了 Hadoop 2.0 的新时代，虽然新一代的 hadoop 努力做了架构、API 上的各种兼容， 但总有“照顾不周”的地方，下面说的这个有关分布式缓存的案例就是于此有关：一些 MR job 迁移到 Yarn 上后，发觉没数据了，而且没有报错。
查了下数据源和代码，发现是分布式缓存（DistributedCache）的用法有点小变化。以前的老代码大致如下： 

(1) 在main函数中添加分布式缓存文件

```shell
String cacheFilePath = "/dsap/rawdata/cmc_uniparameter/20140308/part-m-00000"

DistributedCache.addCacheFile(new Path(cacheFilePath).toUri(),job.getConfiguration());
```
2. 在mr中初始化的时候读取缓存文件做数据字典：

```java

Path[] paths = DistributedCache.getLocalCacheFiles(context.getConfiguration());


for(Path path: paths) {
	if(path.toString().contains("cmc_unitparameter)) {
    
    }
}
```

3. 结果：

这两段代码在 MR1 时代毫无问题，但是到了 MR2 时代 if 是永远为 false 的。
特意对比了下 MR1 和 MR2 时代的 path 格式，可以看到在 MRv2 下，Path 中不包含原始路径信息了：
```shell	
MR1 Path:   hdfs://host:fs_port/dsap/rawdata/cmc_unitparameter/20140308/part-m-00000
MR1 Path:   hdfs://host:fs_port/dsap/rawdata/cmc_unitparameter/20140308/part-m-00000
 
 
MR2 Path:   /data4/yarn/local/usercache/root/appcache/application_1394073762364_1884/container_1394073762364_1884_01_000006/part-m-00000
MR2 Path:   /data17/yarn/local/usercache/root/appcache/application_1394073762364_1884/container_1394073762364_1884_01_000002/part-m-00000
MR2 Path:   /data23/yarn/local/usercache/root/appcache/application_1394073762364_1884/container_1394073762364_1884_01_000005/part-m-00000
```

看了上面两种差异我想你能明白为啥分布式缓存在 MR2 下面“失效了”。。。 


##### 2. 解决方案

解决这个问题并不难

其实在 MR1 时代我们上面的代码是不够规范的，每次都遍历了整个分布式缓存，我们应该用到一个小技巧：createSymlink


1. main函数中为每个缓存文件添加符号链接 ： 类似与 HTTP URL 的# 锚点一样

```java
...
String cacheFilePath = "/dsap/rawdata/cmc_unitparameter/20140308/part-m-00000"；

path inPath = new Path(cacheFilePath);

// # 号之后的名称是对上面文件的链接， 不同的链接名不能相同， 虽然由你随便取
String inputPathLink = inPath.toUri().toString() + "#" + "DIYFILEName";
DistrubutedCache.addCacheFile(new URI(inPathLink),job.getCOnfiguraiton());
```

加了软连接后，path信息的最后部分是刚才的DIYFileName:

```shell
 /data4/yarn/local/usercache/root/appcache/application_1394073762364_1966/container_1394073762364_1966_01_000005/cmcs_paracontrolvalues
/data4/yarn/local/usercache/root/appcache/application_1394073762364_1966/container_1394073762364_1966_01_000005/cmc_unitparameter
```

2. 在需要使用缓存文件的俄地方直接根据刚才的 # 号后面自定义的文件名读取即可
```shell
BufferedReader br = null;
br = new BufferedReader(new InputStreamReader(new FileInputStream("DIYFileName")));

```

##### 一个例子

Hadoop 分布式缓存实现目的是在所有的MapReduce调用一个统一的配置文件，首先将缓存文件放置在HDFS中，然后程序在执行的过程中会可以通过设定将文件下载到本地具体设定如下：

```java
public static void main(String[] args) throws IOException,CalssNotFoundException,InterruptedException {

Confgiuration conf = new Configuration();
conf.set("fs.default.name"."hdfs://192.168.1.45:9000");
FileSystem  fs = FileSystem.get(conf);
fs.delete(new Path("CASICJNJP/gongda/Test_gd20140104"));

conf.set("mapred.job.tracker","192.168.1.45:9001);
conf.set("mapred.jar","/home/hadoop/workspace/jar/OBDDataSelectWithImeiTxt.jar");     
Jobe job = new Job(conf,"myTaxiAnalyze");


DistributedCache.createSymlink(job.getConfiguration());  //

try{
DistributedCaceh.addCacheFile(new URL("/user/hadoop/CASICJNJP/DistributeFiles/imei.txt"),job.getCOnfiguration());
} catch(URLSyntaxException e1) {
	e1.printStackTrace();
}

job.setMapperClass(OBDDataSelectMaper.class);
        job.setReducerClass(OBDDataSelectReducer.class);
        //job.setNumReduceTasks(10);
        //job.setCombinerClass(IntSumReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        
        FileInputFormat.addInputPath(job, new Path("/user/hadoop/CASICJNJP/SortedData/20140104"));
        FileOutputFormat.setOutputPath(job, new Path("CASICJNJP/gongda/SelectedData"));
        
        System.exit(job.waitForCompletion(true)?0:1);

}

```

代码中标红的为将HDFS中的/user/hadoop/CASICJNJP/DistributeFiles/imei.txt作为分布式缓存

```java

public class OBDDataSelectMapper extends Mapper<Object,Text,Text,Text,Text> {
	String[] strs;
    String[]  ImeiTimes;
    String timei;
    Strig time;
    private java.util.List<Integer> ImeiList = new java.util.ArrayList<Integer>();
    
    protected void setup(Context context) throws IOException,INterruptedException {
    	try{
        
        Path[] cacheFiles = DistributeCahce.getCacheFiles(context.getConfiguration());
        
        if (cacheFiles != null && cacheFiles.length >0) {
        	String line ;
            BufferedReader br = new BufferedReader(new FileReader(cacheFiles[0].toStrig()));
            
            
        try{
        	line = br.readLine();
            while((line = br.readLine()) != null) {
            ImeiList.add(Integer.parseInt(line));
            
        } finally {
        	br.close();
        }
        }
        } catch(IOException e) {
        	System.err.println("Exception reading DistributedCache:" +e);
        }
    }
    
    
    public void map(Object key,Text value ,Context context)
    	throws IOException ,InterruptedException{
        	try{
            	strs = value.toString().split("\");
                ImeiTimes = strs[0].split("_");
                timei = IMeiTimes[0];
                if(ImeiList.contains(Integer.parseInt(timei))) {
                	context.write(new Text(strs[0],value));
                }
            } catch(Exception ex) {
            	
            }
        }
}
```

上述标红代码中在Map的setup函数中加载分布式缓存。


#### mapreduce 如何调用第三方 jar 包

[传动](http://blog.csdn.net/evo_steven/article/details/14521197)

MapReduce程式调用第三方包：我在使用过程中需要用到hbase的jar包，若要使用，常规是添加到每台机器的classpath中，但是通过DistributeCache，在初始化前加入就ok了。要不就要将这些jar包打成一个新jar，通过hadoop jar  XXX.jar运行，但是不利于代码更新和维护。

##### 解决方法介绍：
我们知道，在Hadoop中有一个叫做DistributedCache的东东，它是用来分发应用特定的只读文件和一个jar包的，以供Map-Reduce框架在启动任务和运行的时候使用这些缓冲的文件或者是把第三方jar包添加到其classpath路径中去，要注意的是DistributedCache的使用是有一个前提的，就它会认为这些通过urls来表示的文件已经在hdfs文件系统里面，所以这里在使用的时候第一步就是要把这些文件上传到HDFS中。


然后Hadoop框架会把这些应用所需要的文件复制到每个准备启动的节点上去，它会把这些复制到mapred.temp.dir配置的目录中去，以供相应的Task节点使用。


这里要注意的DistriubtedCache分发的文件分成公有与私有文件，公有文件可以给HDFS中的所有用户使用，而私有文件只能被特定的用户所使用，用户可以配置上传文件的访问权限来达到这种效果。


```java

public boolean run(Configuration conf,String inputPath,String OutPath,
String category) throws Exception{
	Job job = new Job(conf,""DIP_DIPLOGFILTER-"+catefory);
    
    // 添加第三方jar包 到hdfs
    DistributedCache.addFileToClassPath(new Path("/libs/hbase-0.92.1-cdh4.0.0-security.jar"),job.getCOnfiguration());
    job.setJarByClass(AnalysisLoader.class);
    
    job.setMapperClass(AnalysisMapper.class);
        job.setMapOutputKeyClass(ComplexKey.class);
        job.setMapOutputValueClass(Text.class);

        job.setPartitionerClass(ComplexKeyPartitioner.class);
//        job.setCombinerClass(AnalysisReducer.class);
        job.setReducerClass(AnalysisReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(LogConfig.reduceCount);
        String hdfs = ServerConfig.getHDFS();
        
        String[] inputPaths = inputPath.split(",");
        for (String p : inputPaths) {
        	if(!p.startsWith(hdfs)) {
            	p = hdfs + p;
            }
            
            MultipleInputs.addInputPath(job,new Path(p),
            TextInputFormat.class,AnalysisMapper.class);
            
        }
        
        FileOutputFormat.setOutputPath(job,new Path(outPath));
        
        return(job.waitForCompiletion(true));

}
```

DistributeCache的使用一般分成三步：
1. 配置应用程序的cache,把需要使用的文件上传到DFS中去

```shell

    $ bin/hadoop fs -copyFromLocal lookup.dat /myapp/lookup.dat    
    $ bin/hadoop fs -copyFromLocal map.zip /myapp/map.zip    
    $ bin/hadoop fs -copyFromLocal mylib.jar /myapp/mylib.jar  
    $ bin/hadoop fs -copyFromLocal mytar.tar /myapp/mytar.tar  
    $ bin/hadoop fs -copyFromLocal mytgz.tgz /myapp/mytgz.tgz  
    $ bin/hadoop fs -copyFromLocal mytargz.tar.gz /myapp/mytargz.tar.gz  
```

2. 配置 JobConf

```java
JobConf job = new JobConf();

DistributedCache.addCacheFile(new URI("/myapp/lookup.dat#lookup.dat"),job); // 这里的lookup.dat加了一个符号连接  
DistributedCache.addCacheArchive(new URI("/myapp/map.zip", job);  
DistributedCache.addFileToClassPath(new Path("/myapp/mylib.jar"), job); // 这里是把相应的jar包加到Task的启动路径上去  
DistributedCache.addCacheArchive(new URI("/myapp/mytar.tar", job);  
DistributedCache.addCacheArchive(new URI("/myapp/mytgz.tgz", job);  
DistributedCache.addCacheArchive(new URI("/myapp/mytargz.tar.gz", job); 
```

3. 在Mapper 或者 Reducer 任务中使用这些文件

```java
public static class MapClass extends MapReduceBase 
        implements Mapper<K,V,K,V> {
	private Path[]  localArchives;
    private Path[]  loacalFiles;
    
    public void configure(JobConf job) {
    	// Get the cached archives/files  
        localArchives = DistributedCache.getLocalCacheArchives(job);  // 得到本地打包的文件，一般是数据文件，如字典文件  
        localFiles = DistributedCache.getLocalCacheFiles(job);        // 得到本地缓冲的文件，一般是配置文件等  
    }
    
    
    public void map(K key,V value , OutputCollector<K,V> output,Report reporter)
    
    throws IOException {
    	// 
        
        
        output.collect(k,v);
    }
        
 }
```

使用新的MP接口要注意的地方：
1. 我们知道，新的MP接口使用了Job这个类来对MP任务进行配置，这里使用的时候要注意一点
   Configuration conf = new Configuration();
   // 对conf加入配置信息  - 正确方法
   Job job = new Job(conf,"word count");
   // 对conf加入配置信息 - 这是有问题的，这些配置不会生效，因为这里生成Job的时候它会对conf进行复制，这个看一下Job的源代码就知道。
   // 这里可以用job.getConfiguration()来得到其内部的conf对象，这样就不会有问题。


2. 如果你在启动MP任务之前调用了第三方jar包的类，那这就会有问题，会在启动任务的时候找不到这个类。这个问题我还没有找到好的解决办法，一个办法就是把这些类想办法移到MP任务中，如果有朋友知道更加好的办法，请告诉我一下，多谢了。我感觉Nutch中也会有同样的问题，什么时候研究一下Nutch的代码，说不定会有很多关于Hadoop方面的收获。


#### 使用maven 对mapreduce 进行部署与第三方包的管理

[传送](http://www.tuicool.com/articles/6NjQ7fm)

Mapreduce部署是总会涉及到第三方包依赖问题，这些第三方包配置的方式不同，会对mapreduce的部署便捷性有一些影响，有时候还会导致脚本出错。本文介绍几种常用的配置方式:

1. HADOOP_CLASSPATH

    在hadoop的相关配置文件中，添加CLASSPATH路径，那么在hadoop的各个进程启动时都会载入这些包，因此对于mapreduce-job jar中则不需要额外的引入这些jars，所以mapreduce-job jar会比较小[瘦jar]，便于传输；但它的问题也比较明显，如果mapreduce-job中新增了其他引用jar，则必须重新启动hadoop的相关进程。

  我们可以在hadoop-env.sh中，增加如下配置：
  
 ```shell
 export HADOOP_CLASSPATH=$HADOOP_CLASSPATH:/path/customer/jars
 ```
 其中“/path/customer/jars”路径为自己的第三方jar所在的本地路径，我们需要在集群中所有的hadoop机器上都同步这些jar。

 瘦jar的打包方式(maven)：
    
    
```shell
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <executions>
    	 <execution>
      <id>copy-dependencies</id>
      <phase>prepare-package</phase>
      <goals>
        <goal>copy-dependencies</goal>
      </goals>
      <configuration>
        <outputDirectory>${project.build.directory}/lib</outputDirectory>
        <overWriteReleases>false</overWriteReleases>
        <overWriteSnapshots>false</overWriteSnapshots>
        <overWriteIfNewer>true</overWriteIfNewer>
      </configuration>
    </execution>
    </executions>
</plugin>

<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-jar-plugin</artifactId>
  <configuration>
    <archive>
      <manifest>
        <addClasspath>true</addClasspath>
        <classpathPrefix>lib/</classpathPrefix>
        <mainClass>com.app.script.Main</mainClass>
      </manifest>
    </archive>
  </configuration>
</plugin>
```

使用了copy-dependencies插件，当使用“mvn package”命令打包之后，第三方引用包会被copy到打包目录下的lib文件中(并非mapreduce-job jar内部的lib文件中)，开发者只需要把这些jars上传到所有hadoop集群即可。


##### 2. mapred.child.env

我们可以指定mapreduce的task子进程启动时加载第三方jars，而不是让所有的hadoop子进程都加载。通过在mapred-site.xml中增加如下配置：

```java
<property>
    <name>mapred.child.env</name>
    <value>LD_LIBRARY_PATH=/path/customer/jars</value>
    <!-- 
      LD_LIBRARY_PATH=$HADOOP_HOME/mapred-lib/thirdparty
    -->
</property>
```

这种方式和1)类似，不过更加便捷，每个mapper或者reducer子进程启动时都会重新加载第三方jars，所以当jars有变动时，只需要直接覆盖它们即可，而无需重启hadoop或者yarn。


##### 3. -libjars 选项

我们可以在使用“hadoo jar”命令时，向启动的job传递“libjars”选项参数，同时配合ToolRunner工具来解析参数并运行Job，这种方式是推荐的用法之一，因为它可以简单的实现job的依赖包和hadoop classpath解耦，可以为每个job单独设置libjars参数。这些jars将会在job提交之后复制到hadoop“共享文件系统中”(hdfs,/tmp文件夹中)，此后taskTracker即可load到本地并在任务子进程中加载。

    libjars中需要指定job依赖的所有的jar全路径，并且这些jars必须在当前本地文件系统中(并非集群中都需要有此jars)，暂时还不支持hdfs。对于在HADOOP_CLASSPATH或者mapred.child.env中已经包含了jars，则不需要再-libjars参数中再次指定。因为libjars需要指定jar的全路径名，所以如果jars特别多的话，操作起来非常不便，所以我们通常将多个job共用的jars通过HADOOP_CLASSPATH或者mapred.child.end方式配置，将某个job依赖的额外的jars(少量的)通过-libjars选项指定。
    
```java
hadoop jar statistic-mr.jar com.statistic.script.Main -libjars /path/cascading-core-2.5.jar,/path/cascading-hadoop-2.5.jar
```


##### 4. Fatjar

 胖jar，即将mapreduce-job jar所依赖的所有jar都“shade”到一个jar中，最终package成一个“独立”的可运行的jar；当然hadoop并不需要这个jar是“可运行的”，它只需要这个jar在运行时不需要额外的配置“--classpath”即可。此外Fatjar仍然可以使用HADOOP_CLASSPATH或者map.child.env所加载的jars，因为我们在打包时可以将这些jars排除，以减少fatjar的大小。

    fatjar只不过是一种打包的方式，也仍然可以和“-libjars”选项配合。不过从直观上来说，fatjar确实是解决“-libjars”不方便的技巧。

    此例中，我们使用cascading来开发一个mapreduce job，但是我们又不希望cascading的相关依赖包被放入HADOOP_CLASSPATH中，因为其他的job可能不需要或者其他的job有可能使用其他版本的cascading；所以就使用Fatjar，把job程序和cascading的依赖包全部“shade”在一起。

    使用maven assambly插件来完成fatjar的打包工作：
    
POM.XML
```xml

<build>
	<finalName>statistic-mapred</finalName>
    <plugins>
    	<plugin>
        	<groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-assembly-plugin</artifactId>
            <configuration>
            	<descirptors>
                	<descriptor>src/assembly.xml</descripor>
                   
                </descriptors>
                
                <archive>
                <!-- optional -->
          <!--
          <manifest>
            <mainClass>com.script.Main</mainClass>
            <addClasspath>true</addClasspath>
          </manifest>
          -->
                </archive>
            </configuration>
            
            <executions>
            	<execution>
                	<id>make-assembly</id>
                    <phase>package</phase>
                    <goals>
                    	<goal>single</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

2. assambly.xml

```xml
<assembly>
	<id>cascading</id>
    <formats>
    	<format>jar</format>
    </formats>
    
    <includeBaseDirectory>false</includeBaseDirectory>
    <dependencySets>
    	<unpack>true</unpack>
        <scope>runtime</scope>
        
        <!--
      <excludes>
        <exclude>org.apache.hadoop:*</exclude>
      </excludes>
      -->
      <!-- very small jar -->
      
      <includes>
      	<include>cascading:*</include>
        <include>thirdparty:*</include>
      </includes>
      </dependencySet>
    </dependencySets>
    
    <fileSets>
    	<fileSet>
        	<directory>${project.build.outputDirectory}</directory>
            <outputDirectory></outputDirectory>
        </fileSet>
    </fileSets>
</assembly>
```
在assambly.xml中我们通过<include>标签来包含需要被“shade”的第三方依赖包，并且采用了unpack(解压)方式，此例中我们只将cascading的jar打进fatjar中，对于其他包将会被忽略，因为这些包已经在hadoop中存在(比如hadoop，hdfs，mapreduce，已经其他的常用包，都可以共用hadoop的)，最终我们的打包结果如下：


![](http://img0.tuicool.com/iaAVji.jpg)


有了fatjar，确实大大的减少了开发工程师部署mapreduce的复杂度和出错的可能性，如果你有即备的maven环境，建议使用fatjar的方式。将fatjar直接放在hadoop中使用“hadoop jar”指令即可执行，几乎无需关心依赖包遗漏的问题。

 此外，需要备注一下，在使用cascading时，如果采用了HADOOP_CLASSPATH或者mapred.child.env方式管理依赖时，会偶尔抛出：
    
```java
Split class cascading.tap.hadoop.MultiInputSplit not found
```

尽管cascading的所有依赖包都在CLASSPATH中，也无法解决这个问题，不确定究竟发生了什么！！后来采用了fatjar之后，问题解决！！


#### 较详细的 董的博客

DistributedCache是Hadoop提供的文件缓存工具，它能够自动将指定的文件分发到各个节点上，缓存到本地，供用户程序读取使用。它具有以下几个特点：缓存的文件是只读的，修改这些文件内容没有意义；用户可以调整文件可见范围（比如只能用户自己使用，所有用户都可以使用等），进而防止重复拷贝现象；按需拷贝，文件是通过HDFS作为共享数据中心分发到各节点的，且只发给任务被调度到的节点。本文将介绍DistributedCache在Hadoop 1.0和2.0中的使用方法及实现原理。

Hadoop DistributedCache有以下几种典型的应用场景：
1）分发字典文件，一些情况下Mapper或者Reducer需要用到一些外部字典，比如黑白名单、词表等；

2）map-side join：当多表连接时，一种场景是一个表很大，一个表很小，小到足以加载到内存中，这时可以使用DistributedCache将小表分发到各个节点上，以供Mapper加载使用；

3）自动化软件部署：有些情况下，MapReduce需依赖于特定版本的库，比如依赖于某个版本的PHP解释器，一种做法是让集群管理员把这个版本的PHP装到各个机器上，这通常比较麻烦，另一种方法是使用DistributedCache分发到各个节点上，程序运行完后，Hadoop自动将其删除。

Hadoop提供了两种DistributedCache使用方式，一种是通过API，在程序中设置文件路径，另外一种是通过命令行（-files，-archives或-libjars）参数告诉Hadoop，个人建议使用第二种方式，该方式可使用以下三个参数设置文件：

1. -files：
   将指定的本地/hdfs文件分发到各个Task的工作目录下，不对文件进行任何处理；

2. -archives：
   将指定文件分发到各个Task的工作目录下，并对名称后缀为“.jar”、“.zip”，“.tar.gz”、“.tgz”的文件自动解压，默认情况下，解压后的内容存放到工作目录下名称为解压前文件名的目录中，比如压缩包为dict.zip,则解压后内容存放到目录dict.zip中。为此，你可以给文件起个别名/软链接，比如dict.zip#dict，这样，压缩包会被解压到目录dict中。

3. -libjars：
 指定待分发的jar包，Hadoop将这些jar包分发到各个节点上后，会将其自动添加到任务的CLASSPATH环境变量中。

前面提到，DistributedCache分发的文件是有可见范围的，有的文件可以只对当前程序可见，程序运行完后，直接删除；有的文件只对当前用户可见（该用户所有程序都可以访问）；有的文件对所有用户可见。DistributedCache会为每种资源（文件）计算一个唯一ID，以识别每个资源，从而防止资源重复下载，举个例子，如果文件可见范围是所有用户，则在每个节点上，第一个使用该文件的用户负责缓存该文件，之后的用户直接使用即可，无需重复下载。那么，Hadoop是怎样区分文件可见范围的呢？

在Hadoop 1.0版本中，Hadoop是以HDFS文件的属性作为标识判断文件可见性的，需要注意的是，待缓存的文件即使是在Hadoop提交作业的客户端上，也会首先上传到HDFS的某一目录下，再分发到各个节点上的，因此，HDFS是缓存文件的必经之路。对于经常使用的文件或者字典，建议放到HDFS上，这样可以防止每次重复下载，做法如下：

比如将数据保存在HDFS的/dict/public目录下，并将/dict和/dict/public两层目录的可执行权限全部打开（在Hadoop中，可执行权限的含义与linux中的不同，该权限只对目录有意义，表示可以查看该目录中的子目录），这样，里面所有的资源（文件）便是所有用户可用的，并且第一个用到的应用程序会将之缓存到各个节点上，之后所有的应用程序无需重复下载，可以在提交作业时通过以下命令指定：

    -files hdfs:///dict/public/blacklist.txt, hdfs:///dict/public/whilelist.txt

如果有多个HDFS集群可以指定namenode的对外rpc地址：

    -files hdfs://host:port/dict/public/blacklist.txt, hdfs://host:port/dict/public/whilelist.txt

DistributedCache会将blacklist.txt和whilelist.txt两个文件缓存到各个节点的一个公共目录下，并在需要时，在任务的工作目录下建立一个指向这两个文件的软连接。

如果可执行权限没有打开，则默认只对该应用程序的拥有者可见，该用户所有应用程序可共享这些文件。

一旦你对/dict/public下的某个文件进行了修改，则下次有作业用到对应文件时，会发现文件被修改过了，进而自动重新缓存文件。

对于一些频繁使用的字典，不建议存放在客户端，每次通过-files指定，这样的文件，每次都要经历以下流程：上传到HDFS上—》缓存到各个节点上—》之后不再使用这些文件，直到被清除，也就是说，这样的文件，只会被这次运行的应用程序使用，如果再次运行同样的应用程序，即使文件没有被修改，也会重新经历以上流程，非常耗费时间，尤其是字典非常多，非常大时。

DistributedCache内置缓存置换算法，一旦缓存（文件数目达到一定上限或者文件总大小超过某一上限）满了之后，会踢除最久没有使用的文件。

在Hadopo 2.0中，自带的MapReduce框架仍支持1.0的这种DistributedCache使用方式，但DistributedCache本身是由YARN实现的，不再集成到MapReduce中。YARN还提供了很多相关编程接口供用户调用，有兴趣的可以阅读源代码。

下面介绍Hadoop 2.0中，DistributedCache通过命令行分发文件的基本使用方式：

（1）运行Hadoop自带的example例子， dict.txt会被缓存到各个Task的工作目录下，因此，直接像读取本地文件一样，在Mapper和Reducer中，读取dict.txt即可：

```shell	
bin/Hadoop jar \
share/hadoop/mapreduce/hadoop-mapreduce-examples-2.2.0.jar \
wordcount \
-files hdfs:///dict/public/dict.txt \
/test/input \
/test/output
```
（2）Hadoop Streaming例子，需要通过-files指定mapper和reducer可执行文件或者脚本文件，这些文件就是通过DistributedCache分发到各个节点上的。
```shell	
#!/bin/bash
HADOOP_HOME=/opt/yarn-client
INPUT_PATH=/test/input/data
OUTPUT_PATH=/test/output/data
echo "Clearing output path: $OUTPUT_PATH"
$HADOOP_HOME/bin/hadoop fs -rmr $OUTPUT_PATH
 
${HADOOP_HOME}/bin/hadoop jar\
   ${HADOOP_HOME}/share/hadoop/tools/lib/hadoop-streaming-2.2.0.jar\
  -D mapred.reduce.tasks=2\
  -files mapper,reducer\
  -input $INPUT_PATH\
  -output $OUTPUT_PATH\
  -mapper mapper\
  -reducer reducer
```
（3）接下给出一个缓存压缩文件的例子，假设压缩文件为dict.zip，里面存的数据为：
```shell	
data/1.txt
data/2.txt
mapper.list
reducer.list
```
通过-archives参数指定dict.zip后，该文件被解压后，将被缓存（实际上是软连接）到各个Task的工作目录下的dict.zip目录下，组织结构如下：
```shell	
dict.zip/
    data/
        1.txt
        2.txt
    mapper.list
    reducer.list
```
你可以在Mapper或Reducer程序中，使用类似下面的代码读取解压后的文件：

```shell	
File file2 = read(“dict.zip/data/1.txt”, “r”);
…….
File file3 = read(“dict.zip/mapper.list”, “r”);
```
如果你想直接将内容解压到Task工作目录下，而不是子目录dict.zip中，可以用“-files”（注意，不要使用-archives，“-files”指定的文件不会被解压）指定dict.zip，并自己在程序中实现解压缩：
```shell	
#include <cstdlib>
…….
system(“unzip –q dict.zip”); //C++代码
……
```
总之，Hadoop DistributedCache是一个非常好用的工具，合理的使用它能够解决很多非常困难的问题。 


#### DistributedCache小记

##### DistributedCache 简介



    DistributedCache是hadoop框架提供的一种机制,可以将job指定的文件,在job执行前,先行分发到task执行的机器上,并有相关机制对cache文件进行管理.

     

    常见的应用场景有:

  	分发第三方库(jar,so等);分发算法需要的词典文件;分发程序运行需要的配置;分发多表数据join时小表数据简便处理等

     

    主要的注意事项有:

    1. DistributedCache只能应用于分布式的情况,包括伪分布式,完全分布式.有些api在这2种情况下有移植性问题.

    2. 需要分发的文件,必须提前放到hdfs上.默认的路径前缀是hdfs://的,不是file://

    3. 需要分发的文件,最好在运行期间是只读的.

    4. 不建议分发较大的文件,比如压缩文件,可能会影响task的启动速度.


##### 二、 DistrutedCache 相关的配置

MRv1

|属性名|默认值|备注|
|:--:|:--:|:--:|
|mapred.local.dir|${hadoop.tmp.dir}/mapred/local|The local directory where MapReduce stores intermediate data files. May be a comma-separated list of directories on different devices in order to spread disk i/o. Directories that do not exist are ignored.|
|local.cache.size|10737418240(10G) |The number of bytes to allocate in each local TaskTracker directory for holding Distributed Cache data.|
|mapreduce.tasktracker.cache.local.numberdirectories|10000 |The maximum number of subdirectories that should be created in any particular distributed cache store. After this many directories have been created, cache items will be expunged regardless of whether the total size threshold has been exceeded.|
|mapreduce.tasktracker.cache.local.keep.pct|0.95(作用于上面2个参数)|It is the target percentage of the local distributed cache that should be kept in between garbage collection runs. In practice it will delete unused distributed cache entries in LRU order until the size of the cache is less than mapreduce.tasktracker.cache.local.keep.pct of the maximum cache size. This is a floating point value between 0.0 and 1.0. The default is 0.95.|

MRv2

的yarn架构的代码还没有看过,不过从配置里可以看出相关的如下配置,本文主要基于MRv1.

yarn.nodemanager.local-dirs

yarn.nodemanager.delete.debug-delay-sec

yarn.nodemanager.local-cache.max-files-per-directory

yarn.nodemanager.localizer.cache.cleanup.interval-ms

yarn.nodemanager.localizer.cache.target-size-mb 


##### 三、 DistributeCache 的使用方式

1. 通过配置

可以配置这三个属性值:

mapred.cache.files,

mapred.cache.archives,

mapred.create.symlink (值设为yes 如果要建link的话)

如果要分发的文件有多个的话,要以逗号分隔(貌似在建link的时候,逗号分隔前后还不能有空格,否则会报错)

2. 使用命令行

在pipes和streaming里面可能会用到

-files  Specify comma-separated files to be copied to the Map/Reduce cluster

-libjars  Specify comma-separated jar files to include in the classpath

-archives  Specify comma-separated archives to be unarchived on the compute machines

如：

-files hdfs://host:fs_port/user/testfile.txt

-files hdfs://host:fs_port/user/testfile.txt#testfile

-files hdfs://host:fs_port/user/testfile1.txt,hdfs://host:fs_port/user/testfile2.txt

-archives hdfs://host:fs_port/user/testfile.jar

-archives hdfs://host:fs_port/user/testfile.tgz#tgzdir

3. 代码调用

DistributedCache.addCacheFile(URI,conf) / DistributedCache.addCacheArchive(URI,conf)

DistributedCache.setCacheFiles(URIs,conf) / DistributedCache.setCacheArchives(URIs,conf)

如果要建link,需要增加DistributedCache.createSymlink(Configuration)

 

获取cache文件可以使用

getLocalCacheFiles(Configuration conf)
getLocalCacheArchives(Configuration conf)

 

代码调用常常会有各样的问题,一般我比较倾向于通过createSymlink的方式来使用,就把cache当做当前目录的文件来操作,简单很多.

常见的通过代码来读取cache文件的问题如下:

a.getLocalCacheFiles在伪分布式情况下,常常返回null.

b.getLocalCacheFiles其实是把DistributedCache中的所有文件都返回.需要自己筛选出所需的文件.archives也有类似的问题.

c.getLocalCacheFiles返回的是tt机器本地文件系统的路径,使用的时候要注意,因为很多地方默认的都是hdfs://,可以自己加上file://来避免这个问题

 

4.symlink

给分发的文件,在task运行的当前工作目录建立软连接,在使用起来的时候会更方便.没有上面的各种麻烦

mapred.create.symlink 需要设置为yes,不是true或Y之类哦

 

5.实际文件存放情况

下图显示的为tt机器上实际文件的状况 (只有yarn集群的截图)

![](http://images.cnitblog.com/blog/73083/201309/29142043-ac58a8d04e0a400cb36060f4e25e48c9.png)


四、 DistributedCache 的内部使用流程



    1.每个tasktracker启动时,都会产生一个TrackerDistributedCacheManager对象,用来管理该tt机器上所有的task的cache文件.

    2.在客户端提交job时,在JobClient内,对即将cache的文件,进行校验

       以确定文件是否存在,文件的大小,文件的修改时间,以及文件的权限是否是private or public.

    3.当task在tt初始化job时,会由TrackerDistributedCacheManager产生一个TaskDistributedCacheManager对象,来管理本task的cache文件.

    4.和本task相关联的TaskDistributedCacheManager,获取并解压相关cache文件到本地相应目录

       如果本tt机器上已经有了本job的其他task,并已经完成了相应cache文件的获取和解压工作,则不会重复进行.

       如果本地已经有了cache文件,则比较修改时间和hdfs上的文件是否一致,如果一致则可以使用.

    5.当task结束时,会对该cache进行ref减一操作.

    6.TrackerDistributedCacheManager有一个clearup线程,每隔1min会去处理那些无人使用的,目录大小大于local.cache.size或者子目录个数大于mapreduce.tasktracker.cache.local.numberdirectories的cache目录.
    
    
    
    
#### 迭代式MapReduce解决方案

[传送](http://hongweiyi.com/2012/02/iterative-mapred-distcache/)

##### 迭代式Mapreduce简介

普通的MapReduce任务是将一个任务分割成map与reduce两个阶段。map阶段负责过滤、筛选、检查输入数据，并将处理后的结果写入本地磁盘中；reduce阶段则负责远程读入map的本地输出结果，对数据进行归并、分析等处理，之后再将结果写入HDFS中。其数据流过程如下：

(k, v) -> map -> (k1, v1), (k1, v2), (k2,v3) -> sort&shuffle -> (k1, list(v1, v2)), (k2, v3)

而迭代式的MapReduce任务需要迭代执行以上过程多次，由于每次任务都是独立的，则需要不断的读取、写入、传输数据，如果还是按照普通的MapReduce一样运行MR任务，性能将会非常低下。

本文拿PageRank做一个例子，PageRank是Google的网页排名算法，是基于网页与网页之间的链接关系计算而得，计算过程需要不断的迭代（单次MR任务），获取一个新的PR值后，再继续迭代，直到两次迭代之间的PR差值小于某一个阈值即停止。

PageRank计算数据分为两个部分：

URL
	

RANK
	

	

URL
	

OUT_LINK

www.a.com
	

1
	  	

www.a.com
	

www.b.com

www.b.com
	

1
	  	

www.a.com
	

www.c.com

www.c.com
	

1
	  	

www.b.com
	

www.a.com

www.d.com
	

1
	  	

www.b.com
	

www.c.com
  	  	  	

www.c.com
	

www.d.com
  	  	  	

www.d.com
	

www.b.com


PR值表                                                网页链接关系表


##### 二、 问题分析说明

迭代式作业的缺点很突出，在[这篇博客](http://dongxicheng.org/mapreduce/iterative-mapreduce-intro/)有详细的介绍，本篇主要需要解决的问题是：如何减少不必要的数据传输与读写。

正如前面所示，PageRank的计算数据分为了两种：PR值表以及网页链接关系表。其中PR值是随着迭代而不断变化，称之为动态数据；而网页链接关系，在计算中，不会有任何的改变，称之为静态数据。

我现在能想到的，再参考了网上的实现方式，基本上都是将静态数据与动态数据合并成一个文件，同时读入(mapper)->写出(mapper)->传输(reducer)->写出(reducer)。

![](http://www.hongweiyi.com/wp-content/uploads/2012/02/image_thumb.png)

我们可以来估算一下时间，先不考虑磁盘IO，仅算静态数据传输时间一项。其中模拟实验数据为：100w个链接地址；随机生成最多1000个外链；结果数据3.22G（动态数据8.5M）；实验环境网络带宽100M；迭代次数20次。

单轮迭代，3.22G的数据会从mapper中读入再全部写入到本地磁盘，reducer再从mapper中将3.22G的临时数据传输到相应的taskTracker上。100M带宽的网络，传输速率约为10M/s，计算公式即为：3.22G×1024 / 10 = 330s = 5.5min。迭代20次，5.5×20 = 110min = 1.8h。简单的估算一下，3G左右的数据，在百兆带宽的网络环境，仅静态数据传输这一项就会占去近两小时（这是最坏情况，不考虑数据在本地的情况）！而网页数据远远不止3.22G，如果到了TB乃至PB级的话，耗时应该就不是开发者所能接受的了。

##### 三、问题解决方法

为了减少不必要数据的传输与读写，开发者就一定要做到以下几点：

1、 将静态数据与动态数据分离，但需要保证在一次（以及下一次）迭代中，结合动静数据；

2、 输出结果中尽量减少数据量，原则上只能有动态数据，不能包含静态数据。

每次map过程中，都需要读入一行PR值表元组，同时也要读入多行对应的链接关系表元组，虽然在map中无法控制两个分离文件的读入顺序，但我们可以预先将动态数据加载进内存作为索引，读入一行后，再查找内存获取需要的数据。这样的方式很容易的就可以想到分布式缓存技术，先前我还在考虑是用Memocached还是Redis，但多看看后好像是多此一举了。MapReduce自带了Distributed Cache技术，可以参见《Mapreduce API文档》。

Haoop中自带的分布式缓存，即DistributedCache对象，可以方便map task之间或者reduce task之间共享一些信息，缓存数据会被分发到集群的所有节点上。需要注意的是，DistributedCache是read-only的。

操作步骤：

1. 将数据分发到每个节点上：

DistributedCache.addCacheFile(new Path(args[0]).toUri(), conf); 

2. 在每个mapper上获取cache文件，便可加载进内存：

DistributedCache.getLocalCacheFiles(conf);

3. Reducer写出动态数据，下一次迭代中，再将新的动态数据加载至DistributedCache中。

将动态数据作为缓存文件的后，整个迭代过程，只有大量减少磁盘IO，且在很大程度上减少了网络带宽负荷与无效数据传输时间。

##### 四、总结

以上的方法理论上支持大多数迭代式Mapreduce模型，如pagerank、SSSP（Single Source Shortest Path）等。参考<董的博客>，再加上自己的实践，提出以下一些问题：

（1） 每次迭代，如果所有task重复重新创建，代价将非常高。怎样重用task以提高效率（task pool）？

说明： hadoop自身提供了task JVM reuse的功能。不过该功能仅限于同一个Job内，而我们每次迭代都会重新运行一个job，故自带功能不适用（或者我还不会用）。但是我们可否考虑job复用呢？

（2） 何时迭代终止，怎样改变编程模型，允许用户指定合适终止迭代。

说明：就PageRank来说，迭代中止的条件是每次迭代结果相差小于一个阈值，即PR结果达到平衡。我们就可以将前一次结果直接输出到Reducer中，或者可以从DistributedCache读取前一次PR值，并做判断。

但是一个PR结果符合条件并不能说明任务就结束了，需要所有的（或者说大多数）的结果均满足条件才能中止任务。那么，这个大多数结果满足条件的数据该怎么存放以及读取呢？还有就是，怎么找到一个通过的编程模型去适应其它的迭代式MR任务呢？

（3）就算没有静态数据，动态数据生成也不小

100W行数据3.22G，64M的split有52个，每个2W行数据。由于是随机生成的，平均每行500个链接地址，每个连接地址都会生成一行临时结果<URL_ID AER_PR>，估算一下也有150M（实际140M），那么3.22数据，最后生成临时数据为7G+。

如不加任何优化的话，那铁定是不行的。后面的文章再说说优化问题，在这个实验环境下，可将7G的文件压缩到不到300M。

（4）DistributedCache API的使用

一直觉得Hadoop的版本管理十分混乱，新旧API杂乱，文档不更新！所以DistributedCache API一直没用好，到时候整理一下，顺带说说如何添加第三方jar包。

以上的讨论还待我的继续研究了，性能分析比较以后的文章给填上。如对迭代式MapReduce任务感兴趣的童鞋可以参考一下Apache开源项目Mahout，还有Google的一篇论文<Pregel: A System for Large-Scale Graph Processing>：中文；英文。

##### （二） DistributedCache 

###### 1、DistributedCache In Hadoop

此篇文章主要是前一篇的后续，主要讲Hadoop的分布式缓存机制的原理与运用。

分布式缓存在MapReduce中称之为DistributedCache，它可以方便map task之间或者reduce task之间共享一些信息，同时也可以将第三方包添加到其classpath路径中去。Hadoop会将缓存数据分发到集群的所有准备启动的节点上，复制到在mapred.temp.dir中配置的目录。

###### 2、 DistributedCache 的使用

DistributedCache的使用的本质其实是添加Configuraton中的属性：mapred.cache.{files|archives}。图方便的话，可以使用DistributedCache类的静态方法。

不省事法：
```shell
conf.set("mapred.cache.files", "/data/data");

conf.set("mapred.cache. archives", "/data/data.zip");
```
省事法：
```shell
DistributedCache. addCacheFile(URI, Configuration)

DistributedCache.addArchiveToClassPath(Path, Configuration, FileSystem)
```
需要注意的是，上面几行代码需要写在Job类初始化之前，否则在运行会中找不到文件（被折磨了很长时间），因为Job初始化时将传入Configuration对象克隆一份给了JobContext。

在MapReduce的0.21版本以后的org.apache.hadoop.mapreduce均移到org.apache.hadoop.mapred包下。但文档中提供的configure方法是重写的MapReduceBase中的，而新版本中map继承于mapper，reduce继承于reducer，所以configure方法一律改成了setup。要获得cache数据，就得在map/reduce task中的setup方法中取得cache数据，再进行相应操作：

```java
@Override
protected void setup(Context context) throws IOException,InterruptedException{

	super.setup(context);
    URI[] uris = DistributedCache.getCahceFiles(context.getConfiguration());
    
    Path[] paths = DistributedCache.getLocalCacheFiles(context.getConfiguration());
    
}
```

而三方库的使用稍微简单，只需要将库上传至hdfs，再用代码添加至classpath即可：

```java
DistributedCache.addArchiveToClassPath(new Path("/data/test.jar"),conf);

```


###### 3. symlink 的使用

Symlink其实就是hdfs文件的一个快捷方式，只需要在路径名后加入#linkname，之后在task中使用linkname即使用相应文件，如下：

```java
conf.set("mapred.cache.files","/data/data#mData");
conf.set("mapred.cache.archives","/data/data.zip#mDataZip");
```

在 maper中使用
```java
@Override
protected void setup(Context context) throws IOException,
InterruptedException {

	super.setup(context);
    FileReader reader = new FileReader(new File("mData"));
    BufferedReader reader = new BufferedReader(reader);
    
    
}
```

在使用symlink 之前要告知hadoop 如下：

```java
conf.set("mapred.create.symlink","yes");   

DistributedCache.createSymlink(COnfiguration)
```


###### 4. 注意事项

1）缓存文件（数据、三方库）需上传至HDFS，方能使用；

2）缓存较小的情况下，建议将数据全部读入相应节点内存，提高访问速度；

3）缓存文件是read-only的，不能修改。若要修改得重新输出，将新输出文件作为新缓存进入下一次迭代。



#### 将MapReduce的结果输出至Mysql数据库

```java
package com.sun.mysql;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.db.DBConfiguration;
import org.apache.hadoop.mapreduce.lib.db.DBOutputFormat;
import org.apache.hadoop.mapreduce.lib.db.DBWritable;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;


/**
 * 将mapreduce的结果数据写入mysql中
 * @author asheng
 */
public class WriteDataToMysql {
/**
     * 重写DBWritable
     * @author asheng
     * TblsWritable需要向mysql中写入数据
     */
    public static class TblsWritable implements Writable, DBWritable
    {  
            String tbl_name;  
            String tbl_type;  
            public TblsWritable()
            {  
            
            }  
            public TblsWritable(String tbl_name,String tab_type)
            {  
            this.tbl_name = tbl_name;
            this.tbl_type = tab_type;
            }  
            @Override  
            public void write(PreparedStatement statement) throws SQLException
            {
                    statement.setString(1, this.tbl_name);  
                    statement.setString(2, this.tbl_type);  
            }  
            @Override  
            public void readFields(ResultSet resultSet) throws SQLException
            {  
                    this.tbl_name = resultSet.getString(1);  
                    this.tbl_type = resultSet.getString(2);  
            }  
            @Override  
            public void write(DataOutput out) throws IOException
            {  
                    out.writeUTF(this.tbl_name);
                    out.writeUTF(this.tbl_type);
            }  
            @Override  
            public void readFields(DataInput in) throws IOException
            {  
                    this.tbl_name = in.readUTF();  
                    this.tbl_type = in.readUTF();  
            }  
            public String toString()
            {  
                return new String(this.tbl_name + " " + this.tbl_type);  
            }  
    }
    public static class ConnMysqlMapper extends Mapper<LongWritable,Text,Text,Text>
    //TblsRecord是自定义的类型，也就是上面重写的DBWritable类
   {  
        public void map(LongWritable key,Text value,Context context)throws IOException,InterruptedException
        {  
        //<首字母偏移量,该行内容>接收进来，然后处理value，将abc和x作为map的输出
        //key对于本程序没有太大的意义，没有使用
        String name = value.toString().split(" ")[0];
        String type = value.toString().split(" ")[1];
                context.write(new Text(name),new Text(type));  
        }  
   }  
    public static class ConnMysqlReducer extends Reducer<Text,Text,TblsWritable,TblsWritable>
    {  
        public void reduce(Text key,Iterable<Text> values,Context context)throws IOException,
                                                                                                                InterruptedException
        {  
        //接收到的key value对即为要输入数据库的字段，所以在reduce中：
        //wirte的第一个参数，类型是自定义类型TblsWritable，利用key和value将其组合成TblsWritable，
                                                                                                                    然后等待写入数据库
        //wirte的第二个参数，wirte的第一个参数已经涵盖了要输出的类型，所以第二个类型没有用，设为null
        for(Iterator<Text> itr = values.iterator();itr.hasNext();)
                 {  
                     context.write(new TblsWritable(key.toString(),itr.next().toString()),null);
                 }  
        }  
    }  
    public static void main(String args[]) throws IOException, InterruptedException, ClassNotFoundException
    {
        Configuration conf = new Configuration();

        DBConfiguration.configureDB(conf, "com.mysql.jdbc.Driver","jdbc:mysql://127.0.0.1:3306/mapreduce_test","root", "root");    
        Job job = new Job(conf,"test mysql connection");  
        job.setJarByClass(ReadDataFromMysql.class);  
          
        job.setMapperClass(ConnMysqlMapper.class);  
        job.setReducerClass(ConnMysqlReducer.class);  
          
        job.setOutputKeyClass(Text.class);  
        job.setOutputValueClass(Text.class);
        
        job.setInputFormatClass(TextInputFormat.class);  
        job.setOutputFormatClass(DBOutputFormat.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        
        DBOutputFormat.setOutput(job, "lxw_tabls", "TBL_NAME","TBL_TYPE");
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
//执行输入参数为/home/asheng/hadoop/in/test3.txt
//test3.txt中的内容为
/*
abc x
def y
chd z
*/
//即将abc x分别做为TBL_NAME,和TBL_TYPE插入数据库中


//输出结果在mysql数据库中查看
//select * from lxw_tabls;
//发现新增三行
/*
abc x
def y
chd z
*/
```

#### mapreduce实现多文件自定义输出 

本人在项目中遇到一个问题，就是在处理日志的时候，需要有多个key，比如一行日志是 domain sip minf h b

而我处理的时候需要map输出为 key：domain+minf value h+"|"+b 和 key：sip+minf value h+"|"+b，而且还要做逻辑运算，比如相同的key的value要做累加，

普通的mr通常情况下，计算结果会以part-000*输出成多个文件，并且输出的文件数量和reduce数量一样，这样就没法区分各个输出在哪个文件中，所以这样也不利于后续将mr的运行结果再做处理。

下面介绍我的处理过程，啥也不说了，上代码：

ComplexKey  是我重写的类，实现了WritableComparable接口，便于对key排序，之所以排序，是希望将相同的key放到同一个reduce中去处理。


```java
import java.io.DataInput;  
import java.io.DataOutput;  
import java.io.IOException;  
  
import org.apache.hadoop.io.Text;  
import org.apache.hadoop.io.WritableComparable;  
  
public class ComplexKey implements WritableComparable<ComplexKey> {  
  
    private Text name;  
  
    private Text value;  
  
    private Text minf;  
  
    public ComplexKey() {  
        this.name = new Text();  
  
        this.value = new Text();  
  
        this.minf = new Text();  
    }  
  
    public ComplexKey(String name, String value, String minf) {  
        this.name = new Text(name);  
  
        this.value = new Text(value);  
  
        this.minf = new Text(minf);  
    }  
  
    public Text getName() {  
        return name;  
    }  
  
    public void setName(Text name) {  
        this.name = name;  
    }  
  
    public Text getValue() {  
        return value;  
    }  
  
    public void setValue(Text value) {  
        this.value = value;  
    }  
  
    public Text getMinf() {  
        return minf;  
    }  
  
    public void setMinf(Text minf) {  
        this.minf = minf;  
    }  
  
    @Override  
    public int compareTo(ComplexKey c) {  
        int compare = 0;  
  
        compare = name.compareTo(c.name);  
        if (compare != 0) {  
            return compare;  
        }  
  
        compare = value.compareTo(c.value);  
        if (compare != 0) {  
            return compare;  
        }  
  
        compare = minf.compareTo(c.minf);  
        if (compare != 0) {  
            return compare;  
        }  
  
        return 0;  
    }  
  
    @Override  
    public void readFields(DataInput in) throws IOException {  
        name.readFields(in);  
  
        value.readFields(in);  
  
        minf.readFields(in);  
    }  
  
    @Override  
    public void write(DataOutput out) throws IOException {  
        name.write(out);  
  
        value.write(out);  
  
        minf.write(out);  
    }  
  
}  

```

分区类
```java
import org.apache.hadoop.io.Text;  
import org.apache.hadoop.mapreduce.Partitioner;  
  
public class ComplexKeyPartitioner extends Partitioner<ComplexKey, Text> {  
  
    @Override  
    public int getPartition(ComplexKey key, Text value, int numPartitions) {  
        return Math.abs(key.getValue().hashCode()) % numPartitions;  
    }  
  
}
```


map

```java
import java.io.IOException;  
import java.math.BigInteger;  
import java.util.HashMap;  
import java.util.Map;  
import java.util.Set;  
  
import org.apache.hadoop.io.LongWritable;  
import org.apache.hadoop.io.Text;  
import org.apache.hadoop.mapreduce.Mapper;  
import org.apache.hadoop.mapreduce.Reducer.Context;  
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;  
//<span style="color:#FF0000">map中的结果都放到了</span></span> mapoutput <span style="color:#000000"><span style="color:#FF0000">，在clean的时候同意处理，将逻辑放在这边，是为了减小reduce的压力，之前累加的逻辑放入reduce，发现100G的数据，要跑大约10多分钟  
   而map只用了1分钟，但是放入map中后，整个处理过程不到两分钟</span>。   
public class AnalysisMapper extends  
        Mapper<LongWritable, Text, ComplexKey, Text> {  
    private MultipleOutputs<ComplexKey, Text> outputs;  
    private Map<String,String> mapoutput = new HashMap<String,String>();  
    private Set<String> outputkeyset;  
    private String[] mapkey;  
    private String[] mapvalue;  
    private BigInteger paravalue;  
    protected void setup(Context context) throws IOException,  
            InterruptedException {  
        outputs = new MultipleOutputs<ComplexKey, Text>(context);  
    };  
    protected void map(LongWritable key, Text value, Context context)  
            throws IOException, InterruptedException {  
        String line = value.toString();  
        if (line == null || line.isEmpty()) {  
            return;  
        }  
  
        String[] words = line.split("\t");  
//      System.out.println("words.length:"+words.length);  
        if (words.length != 17 && words.length != 18) {  
//          System.out.println("line:"+value.toString());  
            return;  
        }  
  
//      if (words[0] == null || words[0].isEmpty() || words[1] == null  
//              || words[1].isEmpty() || words[2] == null || words[2].isEmpty()  
//              || words[14] == null || words[14].isEmpty()  
//              || words[16] == null || words[16].isEmpty()) {  
//          return;  
//      }  
        BigInteger hit,bit;  
        Text hb;  
//      System.out.println("words.length:"+words.length);  
        if(words[1].equals("172.20.20.37")){  
            if(words.length == 17){  
//              System.out.println("mapoutput17:"+mapoutput.size());  
                  
                hb = new Text(words[14] + "|" + words[16]);  
                if(null != mapoutput.get("domain"+"|"+words[2]+"|"+words[0])){//如果结果中已经存在 domain|minf  
                    mapvalue = (mapoutput.get("domain"+"|"+words[2]+"|"+words[0])).toString().split("\\|");  
  
                    hit = new BigInteger(mapvalue[0]);  
                    bit = new BigInteger(mapvalue[1]);  
                    hit = hit.add(new BigInteger(words[14]));  
                    bit = bit.add(new BigInteger(words[16]));  
                    mapoutput.put("domain"+"|"+words[2]+"|"+words[0], hit+"|"+bit);  
                }else{  
                    mapoutput.put("domain"+"|"+words[2]+"|"+words[0], words[14]+"|"+words[16]);  
                }  
                if(null != mapoutput.get("sip"+"|"+words[1]+"|"+words[0])){//如果结果中已经存在 sip|minf  
                    mapvalue = (mapoutput.get("sip"+"|"+words[1]+"|"+words[0])).toString().split("\\|");  
                    hit = new BigInteger(mapvalue[0]);  
                    bit = new BigInteger(mapvalue[1]);  
                    hit = hit.add(new BigInteger(words[14]));  
                    bit = bit.add(new BigInteger(words[16]));  
                    mapoutput.put("sip"+"|"+words[1]+"|"+words[0], hit+"|"+bit);  
                }else{  
                    mapoutput.put("sip"+"|"+words[1]+"|"+words[0], words[14]+"|"+words[16]);  
                }  
            }else if(words.length == 18){  
//              System.out.println("mapoutput18:"+mapoutput.size());  
                hb = new Text(words[15] + "|" + words[17]);  
                if(null != mapoutput.get("domain"+"|"+words[2]+"|"+words[0])){//如果结果中已经存在 domain|minf  
                    mapvalue = (mapoutput.get("domain"+"|"+words[2]+"|"+words[0])).toString().split("\\|");  
  
                    hit = new BigInteger(mapvalue[0]);  
                    bit = new BigInteger(mapvalue[1]);  
                    hit = hit.add(new BigInteger(words[15]));  
                    bit = bit.add(new BigInteger(words[17]));  
                    mapoutput.put("domain"+"|"+words[2]+"|"+words[0], hit+"|"+bit);  
                }else{  
                    mapoutput.put("domain"+"|"+words[2]+"|"+words[0], words[15]+"|"+words[17]);  
                }  
                if(null != mapoutput.get("sip"+"|"+words[1]+"|"+words[0])){//如果结果中已经存在 sip|minf  
                    mapvalue = (mapoutput.get("sip"+"|"+words[1]+"|"+words[0])).toString().split("\\|");  
                    hit = new BigInteger(mapvalue[0]);  
                    bit = new BigInteger(mapvalue[1]);  
                    hit = hit.add(new BigInteger(words[15]));  
                    bit = bit.add(new BigInteger(words[17]));  
                    mapoutput.put("sip"+"|"+words[1]+"|"+words[0], hit+"|"+bit);  
                }else{  
                    mapoutput.put("sip"+"|"+words[1]+"|"+words[0], words[15]+"|"+words[17]);  
                }  
            }  
        }  
          
    };  
<span style="color:#FF0000">//多个输出，每个会有不同的key</span>  
 protected void cleanup(Context context) throws IOException,  
        InterruptedException {  
        outputkeyset = mapoutput.keySet();  
        for(String outputkey : outputkeyset){  
            mapkey = outputkey.split("\\|");  
            if(mapkey[0].equals("domain")){  
                mapvalue = mapoutput.get(outputkey).split("\\|");  
//              System.out.println("domainh:"+mapvalue[0]);  
                ComplexKey domain = new ComplexKey("domain", mapkey[1], mapkey[2]);  
                Text hb = new Text(mapvalue[0] + "|" + mapvalue[1]);  
                <span style="color:#FF0000">context.write(domain, hb);</span>  
            }else if(mapkey[0].equals("sip")){  
                mapvalue = mapoutput.get(outputkey).split("\\|");  
                ComplexKey sip = new ComplexKey("sip", mapkey[1], mapkey[2]);  
                Text hb = new Text(mapvalue[0] + "|" + mapvalue[1]);  
//              System.out.println("siph:"+mapvalue[0]);  
                <span style="color:#FF0000">context.write(sip, hb);</span>  
            }  
//          else if(mapkey[0].equals("httpcode")){  
//              ComplexKey sip = new ComplexKey("httpcode", mapkey[1], mapkey[2]);  
//              Text h = new Text(mapoutput.get(outputkey));  
//              <span style="color:#FF0000">context.write(sip, h);</span>  
//          }  
        }  
        outputs.close();  
    };  
}  
```

reducer

```java
ackage sina.dip.logfilter.mr;  
  
import java.io.IOException;  
import java.math.BigInteger;  
  
import org.apache.hadoop.io.Text;  
import org.apache.hadoop.mapreduce.Reducer;  
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;  
/** 
 * 根据不同的key处理不同的逻辑，然后输出到相应的目录下 
 * 
 */  
public class AnalysisReducerBack1 extends Reducer<ComplexKey, Text, Text, Text> {  
  
    private MultipleOutputs<Text, Text> outputs;  
  
    protected void setup(Context context) throws IOException,  
            InterruptedException {  
        outputs = new MultipleOutputs<Text, Text>(context);  
    };  
    <span style="color:#FF0000">//根据不同的key，处理不同的逻辑，并输出到不同的目录下</span>  
    protected void reduce(<span style="color:#FF0000">ComplexKey</span> key, Iterable<Text> values, Context context)  
            throws IOException, InterruptedException {  
        Text oKey = null,oValue = null;  
        BigInteger h = new BigInteger("0"),b = new BigInteger("0");  
        if(key.getName().toString().equals("sip") || key.getName().toString().equals("domain")){  
              
              
            for (Text value : values) {  
                String[] words = value.toString().split("\\|");  
                h = h.add(new BigInteger(words[0]));  
                b = b.add(new BigInteger(words[1]));  
//              h += Integer.valueOf(words[0]);  
//              b += Integer.valueOf(words[1]);  
            }  
  
            oKey = new Text(key.getValue() + "\t" + key.getMinf());  
            oValue = new Text(h + "\t" + b);  
        }else if(key.getName().toString().equals("httpcode")){  
            for (Text value : values) {  
                h = h.add(new BigInteger(value.toString()));  
//              h += Integer.valueOf(value.toString());  
            }  
  
            oKey = new Text(key.getValue() + "\t" + key.getMinf());  
            oValue = new Text(String.valueOf(h));  
        }  
          
  
        <span style="color:#FF0000">outputs.write(oKey, oValue, key.getName().toString()+"/"+key.getName().toString());</span>  
<span style="color:#FF0000">//根据key输出，比如domain的key，则输出到了outputpath/domain/domain-part-000x;</span>  
<span style="color:#FF0000">//或者设置为outputs.write(oKey, oValue, key.getName().toString());</span>则输出为<span style="color:#FF0000">outputpath/domain-part-000x;</span>  
      };  
  
    protected void cleanup(Context context) throws IOException,  
            InterruptedException {  
        outputs.close();  
    };  
  
}  

```

driver

```java
    package sina.dip.logfilter.mr;  
      
      
    import org.apache.hadoop.conf.Configuration;  
    import org.apache.hadoop.filecache.DistributedCache;  
    import org.apache.hadoop.fs.Path;  
    import org.apache.hadoop.io.Text;  
    import org.apache.hadoop.mapreduce.Job;  
      
    import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;  
    import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;  
    import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;  
      
    import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;  
    import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;  
      
    import sina.dip.logfilter.DipFilterLogData;  
    import sina.dip.logfilter.config.LogConfig;  
    import sina.dip.logfilter.config.ServerConfig;  
    import sina.dip.logfilter.util.FileUtil;  
      
      
      
    public class AnalysisLoader {  
          
        /** 
         * @param args 
         * @param conf 
         * @return 
         * @throws Exception 
         */  
        public boolean run(Configuration conf, String inputPath, String outPath,String category)  
        throws Exception {  
            Job job = new Job(conf, "DIP_DIPLOGFILTER-"+category);  
            DistributedCache.addFileToClassPath(new Path("/libs/hbase-0.92.1-cdh4.0.0-security.jar"), job.getConfiguration());  
                   <span style="color:#FF0000">//解决第三包的调用问题，在其他的文章中有介绍</span>  
                     job.setJarByClass(AnalysisLoader.class);  
      
      
            job.setInputFormatClass(TextInputFormat.class);  
            job.setOutputFormatClass(TextOutputFormat.class);  
      
            job.setMapperClass(AnalysisMapper.class);  
            <span style="color:#FF0000">job.setMapOutputKeyClass(ComplexKey.class);</span>  
            job.setMapOutputValueClass(Text.class);  
      
            <span style="color:#FF0000">job.setPartitionerClass(ComplexKeyPartitioner.class);</span>  
    //      job.setCombinerClass(AnalysisReducer.class);  
            job.setReducerClass(AnalysisReducer.class);  
            job.setOutputKeyClass(Text.class);  
            job.setOutputValueClass(Text.class);  
            job.setNumReduceTasks(LogConfig.reduceCount);  
            String hdfs = ServerConfig.getHDFS();  
              
            String[] inputPaths =inputPath.split(",");  
            for (String p : inputPaths) {  
                if (!p.startsWith(hdfs)) {  
                    p = hdfs + p;  
                }  
                MultipleInputs.addInputPath(job, new Path(p),TextInputFormat.class, AnalysisMapper.class);  
            }  
              
            FileOutputFormat.setOutputPath(job, new Path(outPath));  
              
            return(job.waitForCompletion(true));  
              
              
        }  
    }  

```

#### 五、从Java代码远程提交YARN MapReduce任务 

[传送](http://blog.csdn.net/mercedesqq/article/details/16885115#)

在Hadoop上运行MapReduce任务的标准做法是把代码打包到jar里面，上传到服务器，然后用命令行启动。如果你是从一个Java应用中想要启动一个MapReduce，那么这个方法真是又土又麻烦。

其实YARN是可以通过Java程序向Hadoop集群提交MapReduce任务的。与普通的任务不同的是，远程提交的Job由于读不到服务器上的mapred-site.xml和yarn-site.xml，所以在Job的Configuration里面需要添加一些设置，然后再提交就可以了。

贴上一个示例代码，大家一看就明白了：

```java
    public class RemoteMapReduceService {  
        public static String startJob() throws Exception {  
            Job job = Job.getInstance();  
            job.setJobName("xxxx");  
            /*************************** 
             *...... 
             *在这里，和普通的MapReduce一样，设置各种需要的东西 
             *...... 
             ***************************/  
                      
            //下面为了远程提交添加设置：  
            Configuration conf = job.getConfiguration();  
            conf.set("mapreduce.framework.name", "yarn");  
            conf.set("hbase.zookeeper.quorum", "MASTER:2181");  
            conf.set("fs.default.name", "hdfs://MASTER:8020");  
            conf.set("yarn.resourcemanager.resource-tracker.address", "MASTER:8031");  
            conf.set("yarn.resourcemanager.address", "MASTER:8032");  
            conf.set("yarn.resourcemanager.scheduler.address", "MASTER:8030");  
            conf.set("yarn.resourcemanager.admin.address", "MASTER:8033");  
            conf.set("yarn.application.classpath", "$HADOOP_CONF_DIR,"  
                +"$HADOOP_COMMON_HOME/*,$HADOOP_COMMON_HOME/lib/*,"  
                +"$HADOOP_HDFS_HOME/*,$HADOOP_HDFS_HOME/lib/*,"  
                +"$HADOOP_MAPRED_HOME/*,$HADOOP_MAPRED_HOME/lib/*,"  
                +"$YARN_HOME/*,$YARN_HOME/lib/*,"  
                +"$HBASE_HOME/*,$HBASE_HOME/lib/*,$HBASE_HOME/conf/*");  
            conf.set("mapreduce.jobhistory.address", "MASTER:10020");  
            conf.set("mapreduce.jobhistory.webapp.address", "MASTER:19888");  
            conf.set("mapred.child.java.opts", "-Xmx1024m");  
              
            job.submit();  
            //提交以后，可以拿到JobID。根据这个JobID可以打开网页查看执行进度。  
            return job.getJobID().toString();  
        }  
    }  

```


#### 六、 Java API 读取HDFS的单文件

```java
/**
 * 获取可重复推荐的类目，以英文逗号分隔
 * @param filePath
 * @param conf
 * @return
 */
public String getRepeatRecCategoryStr(String filePath) {
    final String DELIMITER = "\t";
    final String INNER_DELIMITER = ",";
     
    String categoryFilterStrs = new String();
    BufferedReader br = null;
    try {
        FileSystem fs = FileSystem.get(new Configuration());
        FSDataInputStream inputStream = fs.open(new Path(filePath));
        br = new BufferedReader(new InputStreamReader(inputStream));
         
        String line = null;
        while (null != (line = br.readLine())) {
            String[] strs = line.split(DELIMITER);
            categoryFilterStrs += (strs[0] + INNER_DELIMITER);
        }
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        if (null != br) {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
     
    return categoryFilterStrs;
}
```

```java
package com.hdfs;
  2 
  3 import java.io.FileInputStream;
  4 import java.io.IOException;
  5 import java.io.InputStream;
  6 
  7 import org.apache.hadoop.conf.Configuration;
  8 import org.apache.hadoop.fs.FSDataOutputStream;
  9 import org.apache.hadoop.fs.FileStatus;
 10 import org.apache.hadoop.fs.FileSystem;
 11 import org.apache.hadoop.fs.Path;
 12 import org.apache.hadoop.io.IOUtils;
 13 
 14 public class HdfsTest {
 15     
 16     //创建新文件
 17     public static void createFile(String dst , byte[] contents) throws IOException{
 18         Configuration conf = new Configuration();
 19         FileSystem fs = FileSystem.get(conf);
 20         Path dstPath = new Path(dst); //目标路径
 21         //打开一个输出流
 22         FSDataOutputStream outputStream = fs.create(dstPath);
 23         outputStream.write(contents);
 24         outputStream.close();
 25         fs.close();
 26         System.out.println("文件创建成功！");
 27     }
 28     
 29     //上传本地文件
 30     public static void uploadFile(String src,String dst) throws IOException{
 31         Configuration conf = new Configuration();
 32         FileSystem fs = FileSystem.get(conf);
 33         Path srcPath = new Path(src); //原路径
 34         Path dstPath = new Path(dst); //目标路径
 35         //调用文件系统的文件复制函数,前面参数是指是否删除原文件，true为删除，默认为false
 36         fs.copyFromLocalFile(false,srcPath, dstPath);
 37         
 38         //打印文件路径
 39         System.out.println("Upload to "+conf.get("fs.default.name"));
 40         System.out.println("------------list files------------"+"\n");
 41         FileStatus [] fileStatus = fs.listStatus(dstPath);
 42         for (FileStatus file : fileStatus) 
 43         {
 44             System.out.println(file.getPath());
 45         }
 46         fs.close();
 47     }
 48     
 49     //文件重命名
 50     public static void rename(String oldName,String newName) throws IOException{
 51         Configuration conf = new Configuration();
 52         FileSystem fs = FileSystem.get(conf);
 53         Path oldPath = new Path(oldName);
 54         Path newPath = new Path(newName);
 55         boolean isok = fs.rename(oldPath, newPath);
 56         if(isok){
 57             System.out.println("rename ok!");
 58         }else{
 59             System.out.println("rename failure");
 60         }
 61         fs.close();
 62     }
 63     //删除文件
 64     public static void delete(String filePath) throws IOException{
 65         Configuration conf = new Configuration();
 66         FileSystem fs = FileSystem.get(conf);
 67         Path path = new Path(filePath);
 68         boolean isok = fs.deleteOnExit(path);
 69         if(isok){
 70             System.out.println("delete ok!");
 71         }else{
 72             System.out.println("delete failure");
 73         }
 74         fs.close();
 75     }
 76     
 77     //创建目录
 78     public static void mkdir(String path) throws IOException{
 79         Configuration conf = new Configuration();
 80         FileSystem fs = FileSystem.get(conf);
 81         Path srcPath = new Path(path);
 82         boolean isok = fs.mkdirs(srcPath);
 83         if(isok){
 84             System.out.println("create dir ok!");
 85         }else{
 86             System.out.println("create dir failure");
 87         }
 88         fs.close();
 89     }
 90     
 91     //读取文件的内容
 92     public static void readFile(String filePath) throws IOException{
 93         Configuration conf = new Configuration();
 94         FileSystem fs = FileSystem.get(conf);
 95         Path srcPath = new Path(filePath);
 96         InputStream in = null;
 97         try {
 98             in = fs.open(srcPath);
 99             IOUtils.copyBytes(in, System.out, 4096, false); //复制到标准输出流
100         } finally {
101             IOUtils.closeStream(in);
102         }
103     }
104     
105     
106     public static void main(String[] args) throws IOException {
107         //测试上传文件
108         //uploadFile("D:\\c.txt", "/user/hadoop/test/");
109         //测试创建文件
110         /*byte[] contents =  "hello world 世界你好\n".getBytes();
111         createFile("/user/hadoop/test1/d.txt",contents);*/
112         //测试重命名
113         //rename("/user/hadoop/test/d.txt", "/user/hadoop/test/dd.txt");
114         //测试删除文件
115         //delete("test/dd.txt"); //使用相对路径
116         //delete("test1");    //删除目录
117         //测试新建目录
118         //mkdir("test1");
119         //测试读取文件
120         readFile("test1/d.txt");
121     }
122 
123 }
```

```java
import java.io.IOException;  
import java.net.URI;  
import java.net.URISyntaxException;  
  
import org.apache.hadoop.conf.Configuration;  
import org.apache.hadoop.fs.FSDataInputStream;  
import org.apache.hadoop.fs.FSDataOutputStream;  
import org.apache.hadoop.fs.FileStatus;  
import org.apache.hadoop.fs.FileSystem;  
import org.apache.hadoop.fs.FileUtil;  
import org.apache.hadoop.fs.Path;  
import org.apache.hadoop.io.IOUtils;  
  
  
public class HDFSTest {  
      
    //在指定位置新建一个文件，并写入字符  
    public static void WriteToHDFS(String file, String words) throws IOException, URISyntaxException  
    {  
        Configuration conf = new Configuration();  
        FileSystem fs = FileSystem.get(URI.create(file), conf);  
        Path path = new Path(file);  
        FSDataOutputStream out = fs.create(path);   //创建文件  
  
        //两个方法都用于文件写入，好像一般多使用后者  
        out.writeBytes(words);    
        out.write(words.getBytes("UTF-8"));  
          
        out.close();  
        //如果是要从输入流中写入，或是从一个文件写到另一个文件（此时用输入流打开已有内容的文件）  
        //可以使用如下IOUtils.copyBytes方法。  
        //FSDataInputStream in = fs.open(new Path(args[0]));  
        //IOUtils.copyBytes(in, out, 4096, true)        //4096为一次复制块大小，true表示复制完成后关闭流  
    }  
      
    public static void ReadFromHDFS(String file) throws IOException  
    {  
        Configuration conf = new Configuration();  
        FileSystem fs = FileSystem.get(URI.create(file), conf);  
        Path path = new Path(file);  
        FSDataInputStream in = fs.open(path);  
          
        IOUtils.copyBytes(in, System.out, 4096, true);  
        //使用FSDataInoutStream的read方法会将文件内容读取到字节流中并返回  
        /** 
         * FileStatus stat = fs.getFileStatus(path); 
      // create the buffer 
       byte[] buffer = new byte[Integer.parseInt(String.valueOf(stat.getLen()))]; 
       is.readFully(0, buffer); 
       is.close(); 
             fs.close(); 
       return buffer; 
         */  
    }  
      
    public static void DeleteHDFSFile(String file) throws IOException  
    {  
        Configuration conf = new Configuration();  
        FileSystem fs = FileSystem.get(URI.create(file), conf);  
        Path path = new Path(file);  
        //查看fs的delete API可以看到三个方法。deleteonExit实在退出JVM时删除，下面的方法是在指定为目录是递归删除  
        fs.delete(path,true);  
        fs.close();  
    }  
      
    public static void UploadLocalFileHDFS(String src, String dst) throws IOException  
    {  
        Configuration conf = new Configuration();  
        FileSystem fs = FileSystem.get(URI.create(dst), conf);  
        Path pathDst = new Path(dst);  
        Path pathSrc = new Path(src);  
          
        fs.copyFromLocalFile(pathSrc, pathDst);  
        fs.close();  
    }  
      
    public static void ListDirAll(String DirFile) throws IOException  
    {  
        Configuration conf = new Configuration();  
        FileSystem fs = FileSystem.get(URI.create(DirFile), conf);  
        Path path = new Path(DirFile);  
          
        FileStatus[] status = fs.listStatus(path);  
        //方法1    
        for(FileStatus f: status)  
        {  
            System.out.println(f.getPath().toString());    
        }  
        //方法2    
        Path[] listedPaths = FileUtil.stat2Paths(status);    
        for (Path p : listedPaths){   
          System.out.println(p.toString());  
        }  
    }  
      
    public static void main(String [] args) throws IOException, URISyntaxException  
    {  
        //下面做的是显示目录下所有文件  
        ListDirAll("hdfs://ubuntu:9000/user/kqiao");  
          
        String fileWrite = "hdfs://ubuntu:9000/user/kqiao/test/FileWrite";  
        String words = "This words is to write into file!\n";  
        WriteToHDFS(fileWrite, words);  
        //这里我们读取fileWrite的内容并显示在终端  
        ReadFromHDFS(fileWrite);  
        //这里删除上面的fileWrite文件  
        DeleteHDFSFile(fileWrite);  
        //假设本地有一个uploadFile，这里上传该文件到HDFS  
//      String LocalFile = "file:///home/kqiao/hadoop/MyHadoopCodes/uploadFile";  
//      UploadLocalFileHDFS(LocalFile, fileWrite    );  
    }  
}
```

#### hadoop2.4新api与旧api调用例子对比说明

##### 程序说明：
下面的mapreduce程序的功能只是计算文件booklist.log的行数，最后输出结果。

       分别调用旧包和新包的方法编写了两分带有main函数的java代码。

       a,新建了mapreduce工程后，先把hadoop的配置目录下的xml都拷贝到src目录下。

       b,在工程src同级目录旁建立conf目录，并放一个log4j.properties文件。

       c, src目录下建立bookCount目录，然后再添加后面的子java文件。

       d, 右击"run as application"或选择hadoop插件菜单"run on hadoop"来触发执行MapReduce程序即可运行。
       
 

##### 老API使用mapred包的代码

文件BookCount.java：

```java
    package bookCount;



    import java.io.IOException;

    import java.util.Iterator;



    import org.apache.hadoop.fs.Path;

    import org.apache.hadoop.io.IntWritable;

    import org.apache.hadoop.io.LongWritable;

    import org.apache.hadoop.io.Text;

    import org.apache.hadoop.mapred.FileInputFormat;

    import org.apache.hadoop.mapred.FileOutputFormat;

    import org.apache.hadoop.mapred.JobClient;

    import org.apache.hadoop.mapred.JobConf;

    import org.apache.hadoop.mapred.MapReduceBase;

    import org.apache.hadoop.mapred.Mapper;

    import org.apache.hadoop.mapred.OutputCollector;

    import org.apache.hadoop.mapred.Reducer;

    import org.apache.hadoop.mapred.Reporter;

    import org.apache.log4j.Logger;

    import org.apache.log4j.PropertyConfigurator;





    public class BookCount {

           public static Logger logger = Logger.getLogger(BookCount.class);

          

           public static void main(String[] args) throws IOException {

                  PropertyConfigurator.configure("conf/log4j.properties");

                  logger = Logger.getLogger(BookCount.class);

                  logger.info("AnaSpeedMr starting");

                  System.setProperty("HADOOP_USER_NAME", "hduser");

                  JobConf conf = new JobConf(BookCount.class);

                  conf.setJobName("bookCount_sample_job");

                  FileInputFormat.setInputPaths(conf, new Path("booklist.log"));

                  FileOutputFormat.setOutputPath(conf, new Path("booklistResultDir"));

                  conf.setMapperClass(BookCountMapper.class);

                  conf.setReducerClass(BookCountReducer.class);

                  conf.setOutputKeyClass(Text.class);

                  conf.setOutputValueClass(IntWritable.class);

                  JobClient.runJob(conf);

           }

          

          

           static class BookCountMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {

                  

                  @Override

                  public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {

                         output.collect(new Text("booknum"), new IntWritable(1));

                         logger.info("foxson_mapper_ok");

                         System.out.println("foxsonMapper");

                  }

           }



           static class BookCountReducer extends MapReduceBase implements Reducer<Text, IntWritable, Text, LongWritable> {

                  @Override

                  public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, LongWritable> output, Reporter reporter) throws IOException {

                         long sumBookNum  = 0;

                         while (values.hasNext()) {

                                sumBookNum =sumBookNum+1;

                                values.next();

                         }

                         logger.info("foxson_BookCountReducer_ok");

                         output.collect(key, new LongWritable(sumBookNum));

                         System.out.println("foxsonReduce");

                  }

           }



    }

```

##### 新API使用mapreduce包的例子

[传送](http://www.aboutyun.com/thread-8251-1-1.html)

文件BookCountNew.java：

```java
package bookCount;



import java.io.IOException;



import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.conf.Configured;

import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.IntWritable;

import org.apache.hadoop.io.LongWritable;

import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;

import org.apache.hadoop.mapreduce.Mapper;

import org.apache.hadoop.mapreduce.Reducer;

import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import org.apache.hadoop.util.Tool;

import org.apache.hadoop.util.ToolRunner;

import org.apache.log4j.Logger;

import org.apache.log4j.PropertyConfigurator;



public class BookCountNew extends Configured implements Tool {

       public static final Logger logger = Logger.getLogger(BookCountNew.class);



       public static void main(String[] args) throws Exception {

              PropertyConfigurator.configure("conf/log4j.properties");

              logger.info("BookCountNew starting");

              System.setProperty("HADOOP_USER_NAME", "hduser");

              Configuration conf = new Configuration();

              int res = ToolRunner.run(conf, new BookCountNew(), args);

              logger.info("BookCountNew end");

              System.exit(res);

       }



       @Override

       public int run(String[] arg0) throws Exception {

              try {

                     Configuration conf = getConf();

                     Job job = Job.getInstance(conf, "bookCount_new_sample_job");

                     job.setJarByClass(getClass());

                     job.setMapperClass(BookCountMapper.class);

                     job.setMapOutputKeyClass(Text.class);

                     job.setMapOutputValueClass(IntWritable.class);

                     job.setReducerClass(BookCountReducer.class);

                     job.setInputFormatClass(TextInputFormat.class);

                     job.setOutputFormatClass(TextOutputFormat.class);

                     TextInputFormat.addInputPath(job, new Path("booklist.log"));

                     TextOutputFormat.setOutputPath(job, new Path("booklistResultDir"));

                     job.setOutputKeyClass(Text.class);

                     job.setOutputValueClass(IntWritable.class);

                     System.exit(job.waitForCompletion(true) ? 0 : 1);

              } catch (Exception e) {

                     logger.error(e.getMessage());

                     e.printStackTrace();

              }

              return 0;

       }



       static class BookCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

              @Override

              public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

                     context.write(new Text("booknum"), new IntWritable(1));

                     logger.info("foxson_mapper_ok");

                     System.out.println("foxsonMapper");

              }

       }



       static class BookCountReducer extends Reducer<Text, IntWritable, Text, LongWritable> {

              @Override

              public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

                     long sumBookNum = 0;

                     for (IntWritable value : values) {

                            sumBookNum = sumBookNum + 1;

                     }

                     logger.info("foxson_BookCountReducer_ok");

                     context.write(key, new LongWritable(sumBookNum));

                     System.out.println("foxsonReduce");

              }



       }



}
```


1.查看hadoop2.4在线api
首先打开下面链接
http://hadoop.apache.org/docs/r2.4.0/api/index.html


* 旧API

```java
// Create a new JobConf
JobConf job = new JobConf(new Configuration(), MyJob.class);

// Specify various job-specific parameters
job.setJobName("myjob");

FileInputFormat.setInputPaths(job, new Path("in"));
FileOutputFormat.setOutputPath(job, new Path("out"));

job.setMapperClass(MyJob.MyMapper.class);
job.setCombinerClass(MyJob.MyReducer.class);
job.setReducerClass(MyJob.MyReducer.class);

job.setInputFormat(SequenceFileInputFormat.class);
job.setOutputFormat(SequenceFileOutputFormat.class);
```
* 新API

```java
// Create a new Job
     Job job = new Job(new Configuration());
     job.setJarByClass(MyJob.class);
     
     // Specify various job-specific parameters     
     job.setJobName("myjob");
     
     job.setInputPath(new Path("in"));
     job.setOutputPath(new Path("out"));
     
     job.setMapperClass(MyJob.MyMapper.class);
     job.setReducerClass(MyJob.MyReducer.class);

     // Submit the job, then poll for progress until the job is complete
     job.waitForCompletion(true);
```

#### Hadoop中mapred包和mapreduce包的区别与联系

今天写了段代码突然发现，很多类在mapred和mapreduce中分别都有定义，下面是小菜写的一段代码：


```java
    public  class MyJob extends Configured implements Tool
    {
       
        public static class MapClass extends MapReduceBase implements Mapper<Text, Text, Text, Text>
        {//
            public void map(Text key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException
            {
                output.collect(value, key);
            }
            
        }

        public static class Reduce extends MapReduceBase implements Reducer<Text, Text, Text, Text>
        {

            @Override
            public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter)     throws IOException
            {
                String csv = "";
                while (values.hasNext())
                {
                    csv += csv.length() > 0 ? "," : "";
                    csv += values.next().toString();               
                }
                output.collect(key, new Text(csv));
            }
            
        }
```


主要看run方法：
上面代码中的Jobconf无可厚非，只有在mapred包中有定义，这个没问题。

但是FileInputFormat和FileOutputFormat在mapred和mapreduce中都有定义，刚开始脑海里对这些都没有概念，就引用了mapreduce中的FileInputFormat和FIleOutputFormat。

这样操作就带来了后面的问题

FileInputFormat.setInputPaths(job, in);
FileOutputFormat.setOutputPath(job, out);
这两条语句不能通过编译，为什么呢，因为FileInputFormat.setInputPaths和FileOutputFormat.setOutputPath的第一个参数都是Job，而不是JobConf，

后来，无意中，看到mapred包中也有这两个类的定义，于是火箭速度修改为mapred下的包，OK，顺利通过编译！

下面还有 job.setOutputFormat(TextOutputFormat.class);语句编译不同通过，提示参数需要扩展。。。的参数；于是小菜也去mapred下面查找是否存在此类，正如期望，也存在此类，当即立段，修改为此包下的类，顺利编译通过，此时，颇有成就感！
可是现在小菜发现，mapred包下和mapreduce包下同时都存在又相应的类，不知道是为什么，那么下面就有目标的请教搜索引擎啦，呵呵，比刚才有很大进步。

结果令小菜很失望，就找到了一个符合理想的帖子。但是通过这个帖子，小菜知道了，mapred代表的是hadoop旧API，而mapreduce代表的是hadoop新的API。

OK，小菜在google输入框中输入“hadoop新旧API的区别”，结果很多。看了之后，又结合权威指南归结如下：

1.    首先第一条，也是小菜今天碰到这些问题的原因，新旧API不兼容。所以，以前用旧API写的hadoop程序，如果旧API不可用之后需要重写，也就是上面我的程序需要重写，如果旧API不能用的话，如果真不能用，这个有点儿小遗憾！

2.    新的API倾向于使用抽象类，而不是接口，使用抽象类更容易扩展。例如，我们可以向一个抽象类中添加一个方法(用默认的实现)而不用修改类之前的实现方法。因此，在新的API中，Mapper和Reducer是抽象类。

3.    新的API广泛使用context object(上下文对象)，并允许用户代码与MapReduce系统进行通信。例如，在新的API中，MapContext基本上充当着JobConf的OutputCollector和Reporter的角色。

4.    新的API同时支持"推"和"拉"式的迭代。在这两个新老API中，键/值记录对被推mapper中，但除此之外，新的API允许把记录从map()方法中拉出，这也适用于reducer。分批处理记录是应用"拉"式的一个例子。

5.    新的API统一了配置。旧的API有一个特殊的JobConf对象用于作业配置，这是一个对于Hadoop通常的Configuration对象的扩展。在新的API中，这种区别没有了，所以作业配置通过Configuration来完成。作业控制的执行由Job类来负责，而不是JobClient，并且JobConf和JobClient在新的API中已经荡然无存。这就是上面提到的，为什么只有在mapred中才有Jobconf的原因。

6.   输出文件的命名也略有不同，map的输出命名为part-m-nnnnn，而reduce的输出命名为part-r-nnnnn，这里nnnnn指的是从0开始的部分编号。
这样了解了二者的区别就可以通过程序的引用包来判别新旧API编写的程序了。小菜建议最好用新的API编写hadoop程序，以防旧的API被抛弃！！！

小菜水平有限，如果哪位大牛看到文中的不足和错误，请指正，小菜会尽快更改文中错误，好让其他入门者不走我的弯路！


####  hadoop下的程序测试及调试信息 
[传送](http://blog.csdn.net/jackydai987/article/details/6446270)

今天不是什么新的内容，主要介绍下0.20版本下hadoop的调试、计数器、调试信息输出等内容。

相信很多人学习hadoop都是从hadoop权威指南开始的，但权威指南使用的hadoop版本是0.19版本的，而有部分人（其中包括我）使用的0.20版本的。相信大家都知道0.20版本相对于0.19版本有了重大的改变。提供了一系列新的API。具体哪些我这里就不具体说了。其中一个跟测试、调试密切相关的就是在0.20版本出现了Context  object（上下文对象）.所以本篇日志就记录一下我在0.20版本下的测试、调试程序。这里有要特别提示下，这些方法都是我自己摸索的，不敢保证一定效果最好或者最简洁，比如计数器那个我也见过其他实现方法。所以如果有错请大家指出。先谢谢了。

 

先来说说测试，老规矩直接上代码，注释在代码里：

```java

public class TestMapper {

	@Test
    public void processReduce() throws IOException {
    
    	wordCountReduce reducer = new WordcountReduce(); // reduce 测试
        LongWritable key = new LongWritable(1234);
        List<LongWritable> list = new ArrayList<LongWritable>();
        list.add(new LongWritable(10));
        list.add(new LongWritable(2));
        Iterable<LongWritable> values = list; // 构造iterable
		//OutputCollector<LongWritable, LongWritable> output = mock(OutputCollector.class);  // 老版本测试  
        Reducer.Context context = mock(Reducer.Context.class); //这里要注明Reduce.context上下文对象  
        try{
        	// reduce.reduce(key,values,context); // 使用 上下文对象代替上面的的output
            verify(context).write(new LongWritable(12), new LongWritable(1234));  
        }catch(InterruptedException e) {
        	e.printStackTrace();
        }
        
        
    }
    
    @Test void processMap() throws IOException {
    	wordcountMapper mapper = new WOrdCountMapper(); // map 测试
        Text value = new Text("1234");
        LongWritable key = new LongWritable(1);
        //OutputCollector<LongWritable, Text> output = mock(OutputCollector.class); 老版本测试  
        Mapper.Context context = mock(Mapper.Context.class);
        
        try{
        	// mapper.map(key,value,output,null); 老版本测试
            mapper.map(key,value,context);
            verify(context).write(new LongWritable(1234), new LongWritable(1)); 
        }catch(InterruptedException e){
        e.printStackTrace();
        }
    }
    
    public static  class wordcountMapper extends  
    Mapper<LongWritable, Text, LongWritable, LongWritable>{  
    public void map(LongWritable key, Text value, Context context)throws IOException, InterruptedException{  
            String one = value.toString();  
            context.write(new LongWritable(Integer.parseInt(one)) , key);  
        }  
    }  
public  class wordcountReduce extends  
    Reducer<LongWritable, LongWritable, LongWritable, LongWritable>{  
    public void reduce(LongWritable key, Iterable<LongWritable>values, Context context)throws IOException, InterruptedException{  
        int sum = 0;  
        for (LongWritable str : values){  
            sum += str.get();  
            }  
            context.write(new LongWritable(sum), key );  
        }  
    }  
}


```

接着来讨论下hadoopp的调试信息，我介绍两种：计数器、直接打印调试信息

这里我只给出主要代码，main函数里面的代码十分简单，我就不给出了。


```java

    public static  class wordcountMapper extends  
            Mapper<LongWritable, Text, Text, IntWritable>{  
            enum Temper{  
                OVER_100  
            } //使用权威指南里的方法定义枚举类型  
            private final static IntWritable one = new IntWritable(1);  
            private Text word = new Text();  
            public void map(LongWritable key, Text value, Context context)throws IOException, InterruptedException{  
                String line = value.toString();  
                StringTokenizer itr = new StringTokenizer(line);  
                while(itr.hasMoreElements()){  
                    word.set(itr.nextToken());  
                    context.write(word, one);  
                }  
                System.out.println("map"); //直接打印调试信息。  
                context.setStatus("this kk!!"); //设置状态  
                Counter c = context.getCounter(Temper.OVER_100); //通过context直接获取计数器这个跟0.19版不一样  
                c.increment(1); //计数器加一  
            }  
        }  
        public static  class wordcountReduce extends  
            Reducer<Text, IntWritable, Text, IntWritable>{  
            public void reduce(Text key, Iterable<IntWritable>values, Context context)throws IOException, InterruptedException{  
                int sum = 0;  
                for (IntWritable str : values){  
                    sum += str.get();  
                }  
                context.write(key, new IntWritable(sum));  
                System.out.println("reduce"); //直接打印调试信息  
            }  
        }  

```

使用上面代码的效果，我现在来给大家指出：

1:我们上面使用了计数器，当我们提交作业运行完成后，可以通过namenode返回的信息查看计数器的值

![](http://hi.csdn.net/attachment/201105/25/0_130633796486c4.gif)

当然我也可以通过hadoop提供的web页面查看计数器的值

![](http://hi.csdn.net/attachment/201105/25/0_13063380887381.gif)

当然我们也可以查看每个map里计数器的取值。

我前两天看了一篇文章，也是在0.20中使用计数器的，那篇文章中说的是可以直接使用

Counter c = context.getCounter("Counter","Counter1");来设置计数器，应该有可以。这里就算多提供了一种使用计数器的方法。

2: 我们程序中使用了System.out.println("map");  System.out.println("reduce"); //直接打印调试信息。

这里我也有两种查看方式，一种是在web页面中查看

![](http://hi.csdn.net/attachment/201105/25/0_13063382235r8j.gif)

还有一种是直接在datanode的log目录下查看

具体路径是 hadoop-0.20/logs/userlogs/attempt-*****-*****/stdout文件中查看


3:程序中我们还使用了context.setStatus("this kk!!"); //设置状态

这个也是在web中可以查看

![](http://hi.csdn.net/attachment/201105/25/0_1306338424mXdq.gif)


今天这篇文章没什么技术含量。就当对新版本的学习了。


####  MapReduce下的数据传递 

我们写MapReduce程序时，有时需要将一定的值（这里是少量 的）从cilent传到map或者reduce.又或者从map传到reduce。

我们先来讨论比较简单的第一种。

解决办法，在main()函数中通过xml文件设定需要传送的值。然后在map函数中读取就行了。

第二种肯定不能使用第一种的方法。因为map阶段跟reduce阶段不一定在同一台机子上，就算map设定了值，reduce也不能够读出来。这里我们就只有使用笨办法了，直接从文件里读。

闲话少说，上代码：

```java
    public class xml_test {  
        public static int getFileInt(String filename) //从文件中读取预设值  
        {  
            int temp = 0;  
            Configuration config = new Configuration();  
            FSDataInputStream dis = null;  
            try {  
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();   
                    FileSystem hdfs = FileSystem.get(config);  
                    dis = hdfs.open(new Path(filename));  
                    IOUtils.copyBytes(dis, baos, 4096, false); //写入ByteArrayOutputStream  
                    String str = baos.toString();  //这里我假定只有一行，多行可以使用循环。  
                    str = str.substring(0, str.length() - 1); //最后一个是回车，需要过滤掉，不然在整形转换时会出错  
                    temp = Integer.parseInt(str);  
                } catch (IOException e) {  
                    // TODO Auto-generated catch block  
                    e.printStackTrace();  
                }     
                finally{  
                    IOUtils.closeStream(dis);  
                }  
            return temp;  
    }  
        public static  class wordcountMapper extends  
            Mapper<LongWritable, Text, Text, IntWritable>{  
            int temp2 = getFileInt("/user/jackydai/int"); //从文件中读取预设的值  
            //这里也可以通过args参数传一个文件进来，这样更灵活  
            private Text word = new Text();  
            public void map(LongWritable key, Text value, Context context)throws IOException, InterruptedException{  
                int temp1 = 0;  
                Configuration mapconf = context.getConfiguration();  
                temp1 = mapconf.getInt("count", 0); //map读取值  
                IntWritable one = new IntWritable(temp1 + temp2); //求和后输出，检测是否正确  
                String line = value.toString();  
                StringTokenizer itr = new StringTokenizer(line);  
                while(itr.hasMoreElements()){  
                    word.set(itr.nextToken());  
                    context.write(word, one);  
                }  
            }  
        }  
        public static  class wordcountReduce extends  
            Reducer<Text, IntWritable, Text, IntWritable>{  
            public void reduce(Text key, Iterable<IntWritable>values, Context context)throws IOException, InterruptedException{  
                int sum = 0;  
                for (IntWritable str : values){  
                    sum += str.get();  
                }  
                context.write(key, new IntWritable(sum));  
            }  
        }  
        public static  void main(String args[])throws Exception{  
              
            Configuration conf = new Configuration();  
              
            conf.setInt("count", 2); //设置值为2，需要注意的是设置值需要在new job之前  
              
            Job job = new Job(conf, "xml_test");  
              
            job.setJarByClass(xml_test.class);  
              
            job.setInputFormatClass(TextInputFormat.class);  
              
            job.setOutputKeyClass(Text.class);  
            job.setOutputValueClass(IntWritable.class);  
              
            job.setMapperClass(wordcountMapper.class);  
            job.setReducerClass(wordcountReduce.class);  
            job.setCombinerClass(wordcountReduce.class);  
              
            FileInputFormat.setInputPaths(job, new Path(args[1]));  
            FileOutputFormat.setOutputPath(job, new Path(args[2]));  
              
            job.waitForCompletion(true);  
        }  
    }  
```

#### 最简单的 分布式缓存文件例子

[传送](http://blog.csdn.net/kingjinzi_2008/article/details/7741320)

分布式缓存一个最重要的应用就是在进行join操作的时候，如果一个表很大，另一个表很小很小，我们就可以将这个小表进行广播处理，即每个计算节点上都存一份，然后进行map端的连接操作，经过我的实验验证，这种情况下处理效率大大高于一般的reduce端join，广播处理就运用到了分布式缓存的技术。

DistributedCache将拷贝缓存的文件到Slave节点在任何Job在节点上执行之前，文件在每个Job中只会被拷贝一次，缓存的归档文件会被在Slave节点中解压缩。将本地文件复制到HDFS中去，接着J哦不Client会通过addCacheFile() 和addCacheArchive()方法告诉DistributedCache在HDFS中的位置。当文件存放到文地时，JobClient同样获得DistributedCache来创建符号链接，其形式为文件的URI加fragment标识。当用户需要获得缓存中所有有效文件的列表时，JobConf 的方法 getLocalCacheFiles() 和getLocalArchives()都返回一个指向本地文件路径对象数组。

下面贴一下我的部分代码：

在run函数中

```java

DistributedCache.createSymlink(job.getConfiguration());

try{//#的作用是以后用的时候直接input就可以了
	DistributedCache.addCacheFile(new URI(args[1]+"/#input"),job.getCofiguration());
}catch(URISyntaxException e1) {
	e1.printStackTrace();
}
```

在map端打开分布式缓存的文件并读如Hashtable中

```java
private Hashtable<String, DefinedMyself> word_hash = new Hashtable<String,DefinedMyself>>();

public void setup(Context context) throws IOException,InterruptedException{
	String[] selected_region = null;
    Path p[] = DistributedCache.getLocalCacheFiles(context.getConfiguration());
    
    FileReader reader = new FileReader("input");
    BufferedReader br = new BufferedReader(reader);
    System.out.println("this is OK!");
    String s1 = null;
    
    int i = 0; 
    
    while((s1 = br.readLine()) != null) {
    	String[] word = s1.split("\\|");
        
    }
}

br.close();
reader.close();


```


####  Hadoop 中的采样器－附主要使用源码 

http://blog.csdn.net/kingjinzi_2008/article/details/7683551

 Hadoop-采样器－多输入路径－只采一个文件－（MultipleInputs+getsample(conf.getInputFormat) :http://blog.csdn.net/kingjinzi_2008/article/details/7695367
 












































