

1 Spark基础篇  
    1.1 Spark生态和安装部署  
        在安装过程中，理解其基本操作步骤。  
        安装部署  
          Spark安装简介  
          Spark的源码编译  
          Spark Standalone安装  
          Spark Standalone HA安装  
          Spark应用程序部署工具spark-submit  
        Spark生态  
          Spark（内存计算框架）  
          SparkSteaming（流式计算框架）  
          Spark SQL（ad-hoc）  
          Mllib（Machine Learning）  
          GraphX（bagel将被取代）  
    1.2 Spark运行架构和解析  
        Spark的运行架构  
          基本术语  
          运行架构  
          Spark on Standalone运行过程  
          Spark on YARN 运行过程  
        Spark运行实例解析  
          Spark on Standalone实例解析  
          Spark on YARN实例解析  


    1.3 Spark的监控和调优  
        Spark的监控  
          Spark UI监控,默认端口是4040  
          Ganglia 监控,大数据监控开源框架  
        Spark调优  
          基础性调优方式  
    1.4 Spark编程模型      
        Spark的编程模型  
          Spark编程模型解析  
          RDD的特点、操作、依赖关系  
          Spark应用程序的配置  
        Spark编程实例解析  
          日志的处理  
    1.5 Spark Streaming原理      
          Spark流式处理架构  
          DStream的特点  
          Dstream的操作和RDD的区别  
          Spark Streaming的优化  
        Spark Streaming实例分析  
          常用的实例程序：  
                  文本实例  
                  Window操作  
                  网络数据处理  
    1.6 Spark SQL原理  
          Spark SQL的Catalyst优化器  
          Spark SQL内核  
          Spark SQL和Hive      
        Spark SQL的实例  
          Spark SQL的实例操作demo  
          Spark SQL的编程，需要网络上查找一些资源         


2 中级篇  
    2.1 Spark的多语言编程   
        Spark的scala编程  
        Spark的Python编程（Java一定熟悉啦，不用多说了）  
           对应的应用程序实例，理解基本的处理模式。      


    2.2 Spark 机器学习入门  
        机器学习的原理  
        Mllib简介，实例分析  
    2.3 GraphX 入门  
        图论基础  
        GraphX的简介  
        GraphX例程分析  
    2.4 理解Spark与其它项目的区别和联系  
        Spark和MapReduce、Tez  
        Spark的衍生项目BlinkDB，RSpark  
    2.5 关注Spark的作者的blog和权威网站的文档  


3 高级篇  
   3.1 深入理解Spark的架构和处理模式  

   3.2 Spark源码剖析与研读  
        Spark Core核心模块，  
        掌握下面核心功能的处理逻辑：  
            SparkContext   
            Executor  
            Deploy  
            RDD和Storage  
            Scheduler和Task  
        Spark Examples  
    3.3 思考如何优化和提升，掌握其优缺点，  
        深入思考能不能衍生出有意思的课题。 



Actions具体内容：

|||
|:--:|:--:|
|reduce(func)	|通过函数func聚集数据集中的所有元素。Func函数接受2个参数，返回一个值。这个函数必须是关联性的，确保可以被正确的并发执行|
|collect()	|在Driver的程序中，以数组的形式，返回数据集的所有元素。这通常会在使用filter或者其它操作后，返回一个足够小的数据子集再使用，直接将整个RDD集Collect返回，很可能会让Driver程序OOM|
|count()	|返回数据集的元素个数|
|take(n)|	返回一个数组，由数据集的前n个元素组成。注意，这个操作目前并非在多个节点上，并行执行，而是Driver程序所在机器，单机计算所有的元素(Gateway的内存压力会增大，需要谨慎使用）|
|first()	|返回数据集的第一个元素（类似于take（1）|
|saveAsTextFile(path)	|将数据集的元素，以textfile的形式，保存到本地文件系统，hdfs或者任何其它hadoop支持的文件系统。Spark将会调用每个元素的toString方法，并将它转换为文件中的一行文本|
|saveAsSequenceFile(path)	|将数据集的元素，以sequencefile的格式，保存到指定的目录下，本地系统，hdfs或者任何其它hadoop支持的文件系统。RDD的元素必须由key-value对组成，并都实现了Hadoop的Writable接口，或隐式可以转换为Writable（Spark包括了基本类型的转换，例如Int，Double，String等等）|
|foreach(func)	|在数据集的每一个元素上，运行函数func。这通常用于更新一个累加器变量，或者和外部存储系统做交互|



（4）Transformation具体内容

|||
|:--:|:--:|  
|map(func)|返回一个新的分布式数据集，由每个原元素经过func函数转换后组成|
|filter(func)|返回一个新的数据集，由经过func函数后返回值为true的原元素组成|
|flatMap(func)|类似于map，但是每一个输入元素，会被映射为0到多个输出元素（因此，func函数的返回值是一个Seq，而不是单一元素）|
|latMap(func)|类似于map，但是每一个输入元素，会被映射为0到多个输出元素（因此，func函数的返回值是一个Seq，而不是单一元素）|
|sample(withReplacement,  frac, seed)|根据给定的随机种子seed，随机抽样出数量为frac的数据|
|union(otherDataset)|返回一个新的数据集，由原数据集和参数联合而成|
|groupByKey([numTasks])|在一个由（K,V）对组成的数据集上调用，返回一个（K，Seq[V])对的数据集。注意：默认情况下，使用8个并行任务进行分组，你可以传入numTask可选参数，根据数据量设置不同数目的Task|
|reduceByKey(func,  [numTasks])|在一个（K，V)对的数据集上使用，返回一个（K，V）对的数据集，key相同的值，都被使用指定的reduce函数聚合到一起。和groupbykey类似，任务的个数是可以通过第二个可选参数来配置的。|
|join(otherDataset,  [numTasks])|在类型为（K,V)和（K,W)类型的数据集上调用，返回一个（K,(V,W))对，每个key中的所有元素都在一起的数据集|
|groupWith(otherDataset,  [numTasks])|在类型为（K,V)和(K,W)类型的数据集上调用，返回一个数据集，组成元素为（K, Seq[V], Seq[W]) Tuples。这个操作在其它框架，称为CoGroup|
|cartesian(otherDataset)|  笛卡尔积。但在数据集T和U上调用时，返回一个(T，U）对的数据集，所有元素交互进行笛卡尔积。|
|flatMap(func)|类似于map，但是每一个输入元素，会被映射为0到多个输出元素（因此，func函数的返回值是一个Seq，而不是单一元素）|







转换（transformation）
下面的列表列出了一些通用的转换。 请参考 RDD API doc (Scala, Java, Python) 和 pair RDD functions doc (Scala, Java) 了解细节.
转换
	
含义
map(func)
	
返回一个新分布式数据集，由每一个输入元素经过func函数转换后组成
filter(func)
	
返回一个新数据集，由经过func函数计算后返回值为true的输入元素组成
flatMap(func)
	
类似于map，但是每一个输入元素可以被映射为0或多个输出元素（因此func应该返回一个序列，而不是单一元素）
mapPartitions(func)
	
类似于map，但独立地在RDD的每一个分块上运行，因此在类型为T的RDD上运行时，func的函数类型必须是Iterator[T] => Iterator[U]
mapPartitionsWithSplit(func)
	
类似于mapPartitions, 但func带有一个整数参数表示分块的索引值。因此在类型为T的RDD上运行时，func的函数类型必须是(Int, Iterator[T]) => Iterator[U]
sample(withReplacement,fraction, seed)
	
根据fraction指定的比例，对数据进行采样，可以选择是否用随机数进行替换，seed用于指定随机数生成器种子
union(otherDataset)
	
返回一个新的数据集，新数据集是由源数据集和参数数据集联合而成
distinct([numTasks]))
	
返回一个包含源数据集中所有不重复元素的新数据集
groupByKey([numTasks])
	
在一个（K,V）对的数据集上调用，返回一个（K，Seq[V])对的数据集
注意：默认情况下，只有8个并行任务来做操作，但是你可以传入一个可选的numTasks参数来改变它
reduceByKey(func, [numTasks])
	
在一个（K，V)对的数据集上调用时，返回一个（K，V）对的数据集，使用指定的reduce函数，将相同key的值聚合到一起。类似groupByKey，reduce任务个数是可以通过第二个可选参数来配置的
aggregateByKey(zeroValue)(seqOp,combOp, [numTasks])
	
根据提供的函数进行聚合。When called on a dataset of (K, V) pairs, returns a dataset of (K, U) pairs where the values for each key are aggregated using the given combine functions and a neutral “zero” value. Allows an aggregated value type that is different than the input value type, while avoiding unnecessary allocations. Like ingroupByKey, the number of reduce tasks is configurable through an optional second argument.
sortByKey([ascending], [numTasks])
	
在一个（K，V)对的数据集上调用，K必须实现Ordered接口，返回一个按照Key进行排序的（K，V）对数据集。升序或降序由ascending布尔参数决定
join(otherDataset, [numTasks])
	
在类型为（K,V)和（K,W)类型的数据集上调用时，返回一个相同key对应的所有元素对在一起的(K, (V, W))数据集
cogroup(otherDataset, [numTasks])
	
在类型为（K,V)和（K,W)的数据集上调用，返回一个 (K, Seq[V], Seq[W])元组的数据集。这个操作也可以称之为groupwith
cartesian(otherDataset)
	
笛卡尔积，在类型为 T 和 U 类型的数据集上调用时，返回一个 (T, U)对数据集(两两的元素对)
pipe(command,[envVars])
	
Pipe each partition of the RDD through a shell command, e.g. a Perl or bash script. RDD elements are written to the process’s stdin and lines output to its stdout are returned as an RDD of strings.
coalesce(numPartitions)
	
Decrease the number of partitions in the RDD to numPartitions. Useful for running operations more efficiently after filtering down a large dataset.
repartition(numPartitions)
	
Reshuffle the data in the RDD randomly to create either more or fewer partitions and balance it across them. This always shuffles all data over the network.

动作（actions）
下面的列表列出了一些通用的action操作. 请参考 RDD API doc (Scala, Java, Python) 和 pair RDD functions doc (Scala, Java) 了解细节.
动作
	
含义
reduce(func)
	
通过函数func（接受两个参数，返回一个参数）聚集数据集中的所有元素。这个功能必须可交换且可关联的，从而可以正确的被并行执行。
collect()
	
在驱动程序中，以数组的形式，返回数据集的所有元素。这通常会在使用filter或者其它操作并返回一个足够小的数据子集后再使用会比较有用。
count()
	
返回数据集的元素的个数。
first()
	
返回数据集的第一个元素（类似于take（1））
take(n)
	
返回一个由数据集的前n个元素组成的数组。注意，这个操作目前并非并行执行，而是由驱动程序计算所有的元素
takeSample(withReplacement,num, seed)
	
返回一个数组，在数据集中随机采样num个元素组成，可以选择是否用随机数替换不足的部分，Seed用于指定的随机数生成器种子
takeOrdered(n,[ordering])
	
Return the firstn elements of the RDD using either their natural order or a custom comparator.
saveAsTextFile(path)
	
将数据集的元素，以textfile的形式，保存到本地文件系统，HDFS或者任何其它hadoop支持的文件系统。对于每个元素，Spark将会调用toString方法，将它转换为文件中的文本行
saveAsSequenceFile(path)
	
将数据集的元素，以Hadoop sequencefile的格式，保存到指定的目录下，本地系统，HDFS或者任何其它hadoop支持的文件系统。这个只限于由key-value对组成，并实现了Hadoop的Writable接口，或者隐式的可以转换为Writable的RDD。（Spark包括了基本类型的转换，例如Int，Double，String，等等）
saveAsObjectFile(path)
(Java and Scala)
	
Write the elements of the dataset in a simple format using Java serialization, which can then be loaded usingSparkContext.objectFile().
countByKey()
	
对(K,V)类型的RDD有效，返回一个(K，Int)对的Map，表示每一个key对应的元素个数
foreach(func)
	
在数据集的每一个元素上，运行函数func进行更新。这通常用于边缘效果，例如更新一个累加器，或者和外部存储系统进行交互，例如HBase