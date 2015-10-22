####hadoop 的 Streaming
[toc]
hadoop的Streaming使用Unix的标准流来作为Hadoop和应用程序之间的接口，所以可以使用任何编程语言通过标准输入输出流来写mapreduce程序

Streaming天生适合处理文本文件（尽管到0.21.0 时，他也可以处理二进制流），在文本模式下她有一个数据行的视图。Map的输入数据通过标准输入流传入给map函数，并且是一行一行的传输，最后将行写入到标准输出，Map的输出是一个以制表符分割的行，它以这样的形式写到标准输出。reduce的输入格式相同————通过制表符来分个的键值对————并通过标准输入流进行传输。reduce从标准输入流中来读取输入行，该输入已由Hadoop框架根据键排序过，最后将结果写入标准输出流。
#####使用脚本语言ruby编写map函数
下面使用Streaming来重写按照年份查找最高气温的MapReduce函数
```Ruby
#!/usr/bin/env ruby
STDIN.each_line do |line|
	val = line
    year,temp,q = val[15,4],val[87,5],val[92,1]
    puts "#{year}\t#{temp}" if (temp != "+9999" && a =~/[01459]/)
```

程序通过执行STDIN（一个IO类型的全局变量）中的每一行来迭代执行标准输入中的每一行。该程序块中输入的每一行读取相关字段，如果气温有效，就将年份以及气温写道标准输出（使用puts），其中年份和气温之间有一个 \t 分割
#####本地测试
因为该脚本只是在标准输入和输出上运行，所以最简单的方式是在UNIX 的管道上进行测试，而不是Hadoop中进行测试：
```shell
% cat input/ncndc/smaple.txt | ch02/src/main/ruby/max_temprature.rb

```
#####下面时使用ruby编写的查找最高气温的reduce函数
```Ruby
 last_key,max_val = nil,0
 STDIN.each_line do |line|
 	key,val = line.split("\t")
    if last_key && last_key != key
    	puts "#{last_key}\t#{max_val}"
        last_key,max_val = key,val.to_i
    else
    	last_key.max_val = key,[max_val,val.to_i].max
    end
  end
  puts "#{last_key}\t#{max_val}" if last_key
```
#####下面使用一个管线来模拟整个Mapreduce关系：
```shell
% cat input/ncdc/sample.txt | ch02/src/main/ruby/max_temprature_map.rb | \
sort |ch02/src/main/ruby/max_temprature_reduce.rb
```
#####测试正确俄后，使用hadoop正式运行：
```shell
% hadoop jar $HADOOP_INSTALL/contrib/streaming/hadoop-*-streaming.jar\
-files ch02/src/main/ruby/max_temprature_map.rb,\
ch02/src/main/ruby/max+temprature_reduce.rb \
-input input/ncdc/sample.txt \
-output output\
-mapper ch02/src/main/ruby/max_temprature_map.rb \
-reducer ch02/src/main/ruby/max+temprature_reduce.rb
```

**注意上面的命令的参数 -files 是将脚本文件发送到集群上**

在一个集群上运行庞大的数据集时 ，我们可以使用 -combiner 选项来设置合并函数
从0.21.0 版开始，合并函数可以使任何一个Streaming命令。对于早期版本，合并函数只能使用Java编写，所以一个变通的方法是在mapper中进行手动合并，这里可以将mapper改成流水线
```shell
% hadoop jar $HADOOP_INSTALL/contrib/streaming/hadoop-*-streaming/jar \
-input input/ncdc/sample.txt \
-output output \
-mapper ch02/src/main/ruby/max_temprature_map.rb | sort | ch02/src/main/ruby/max_temprature_reduce.rb\
-reducer ch02/src/main/ruby/max_temprature_reduce.rb \
-file ch02/src/main/ruby/max_temprature_map.rb \
-file ch02/src/main/ruby/max_temprature_reduce.rb
```
**还需要注意 -file 选项的使用。在集群上运行 Streaminig程序时，我们会使用这个参数，将脚本上传到集群,或者直接使用 -files 上传脚本到集群中**
**注意，要保证脚本执行用户一定可以访问到脚本，或者直接使用 执行用户编写脚本**

#####python版本

#####使用python写的map函数

```python
#!/usr/bin/env python

import sys
impirt re

for line in sys.stdin:
	val = line.strip()
    (year, temp , p) = (val[15:19],val[87:92],val[92:93])
    if (temp != "+9999" and re.match([01459]",q)):
    	print "%s\t%s" % (year,temp)
```

#####使用python编写的reduce函数
```python
#!/usr/bin/env python

import sys

last_key, max_val） = (None, 0)
for line in sys.stdin:
	(key,val) = line.strip().split("\t")
    if last_key and last_key != key :
    	print "%s\t%s"%(last_key,max_val)
        (last_key, max_val) = (key , int(val))
    else:
    	(last_key,max_val) = (key,max(max_val,int(val)))
    if last_key:
    	print "%s\t%s" % (last_key,max_val)
```

我们也可以像ruby那样进行测试：
```shell
% cat input/ncdc/sample.txt | ch02/src/python/max_temprature_map.py | \
	sort | ch02/main/src/max_temprature_reduce.py
```


#####Hadoop 的 Pipes
hadoop 的 Pipes 是 Hadoop MapReduce的C\++接口，不同于使用标准输入输出的streaming，Pipe使用套接字作为tasktracker与C++版本的map或者reduce函数的进程之间的通道，而未使用JNI：

使用C++语言编写的MapReduce函数：
```C++
#include <algorithm>
#include <limits>
#include <stdin.h>
#include <string>

#include "hadoop/Pipes.hh"
#include "hadoop/TemplateFactory.hh"
#include "hadoop/StringUtils.hh"

//定义mapper类
class MaxTemperatureMapper : public HadoopPipes::Mapper {
	public:
		MaxTemperatureMapper(HadoopPipes::TaskContext& context) {
    
    	}
        void map(HadoopPipes::MapContext& context) {
            std::string line = context.getInputValue();
            std::string year = line.substr(15,4);
            std::string airTemprature = line.substr(87,5);
            if(airTemprature != "+9999" &&
            (q == "0" || q == "1" || q == "4" || q == "5" || q == "9" )) {
                context.emit(year,airTemprature);
            }
    }; 


//定义reduce类
class MaxTempratureReducer : public HadoopPipes::Reducer{
	public:
    	MaxTempratureReducer(HadoopPipes::TaskContext& context) {
        
        }
        void reduce(HadoopPipes::ReduceContext& context) {
        	int maxValue = INT_MIN;
            while(context.nextValue()) {
            	max_value = std::max(maxValue,
                HadoopUtils::toInt(context.getInputValue()));
            }
            
            context.emit(context.getInputKey(),HadoopUtil.toString(maxValue))
        }
};

int main(int argc,char *argv[]) {
//使用HadoopPipes::TemplateFactory 运行mapper和reducer	returnHadoop::runTask(HadoopPipes::TemplateFactory<MaxTempratureMapper,MaxTempratureReducer>());
}

}
```


应用程序对Hadoop C++ 库链接提供了一个与tasktracker子进程进行通讯的简单封装 （待写）

与java接口不同 ，C++ 接口中的键和值按照字节进行缓冲，使用标准模板（STL）中的字符串表示。

程序的入口函数main（）方法，它调用HadoopPipes::runTask,该函数链接到Java附近成，并在Mapper和reducer之间来回封送数据，runTask()方法被传入一个Factory参数，由此新建Mapper和Reducer实例，创建mapper还是reducer，Java父进程可以通过套接字进行控制，我们可以用重载模板Factory来设置combiner、partitioner、record reader和record Writer。

编译运行：
现在我们可以使用makefile编译链接上面的程序
```makefile
CC = g++
CPPFLAGS = -m32 -I$(HADOOP_INSTALL)/c++/$（PLATFORM）/include

max_tempretur: max_temprature.cpp
	$ (CC) $(CPPFLAGS) $< -Wall -L$(HADOOP_INSTALL)/c++/$(PLATFORM)/lib
    -lhadooppipes \ -lhadoputils -lpthread -g -O2 -o $@
```
在makefile中设置了许多环境变量 ，除了上面的设置外还需要设置PLATFORM变量，指定了操作系统、体系结构和数据模型,例如在32位linux系统中进行编译
```shell
% export PLATFORM=linex-i386-32
%make
```
编译成功后，可以在当前安目录中找到名为 max_temprature的可执行文件

Pipe不能在独立模式运行，因为它依赖于hadoop的分布式缓存机制，而该机制只有在HDFS运行时才会起作用。

首先将可执行文件发送到hdfs上,以便启动map和reduce任务时，tasktraker能够找到关联的可执行文件
```shell
% hadoop fs -put max_temprature bin/max_temprature
```
示例数据也需要上传到HDFS中
现在可以运行这个作业，我们使用hadoop Pipes命令使其运行，使用-program参数来传递在HDFS中可执行文件的URI
```shell
% hadoop pipes \
-D hadoop.pipes.java.recordreader=true \    #表示
-D hadoop.pipes.java.recordWriter=true \
-input sample.txt \
-output output \
-program bin/max_temprature      #可执行文件的URI
```

MapReduce脚本
使用Hadoop Streaming ,TRANSFORM,MAP,REDUCE子句这样的方法可以在HIVE中调用外部脚本。

```python
#!/usr/bin/env python

import re
import sys

for line in sys.stdin:
	(year,temp,q) = line.strip().split()
    if(temp ！= “9999” and re.match("[01459]",q)):
    	print "%s\t%s" % (year,temp)
```

可以以如下形式使用脚本：
```sql
ADD FILE /path/to/is_goog_quality.py;
FROM records2 
SELECT transform(year ,temperature,quality)
USING 'is_good_quality.py'
as year,tempature;
```

如果要用查询的嵌套形式，可以指定map和reduce函数。这一次我们使用MAP和REDUCE关键字，但是在这两个地方用 SELECT TRANSFORM 也可以达到同样的效果，max_temprature_reduce.py
```hql
FROM(
FROM record2 MAP year,temperature,quality USING 'is_good_quality.py'
AS year,temperature) map_output
REDUCE year,temperature
USING 'max_temperature_reduce.py' AS year,temperature;
```


##### Hadoop Streaming 编程
Hadoop Streaming是Hadoop提供的多语言编程工具，通过该工具，用户可采用任何语言编写MapReduce程序，本文将介绍几个Hadoop Streaming编程实例，大家可重点从以下几个方面学习：

 1. 对于一种编写语言，应该怎么编写Mapper和Reduce，需遵循什么样的编程规范

 2.  如何在Hadoop Streaming中自定义Hadoop Counter

 3. 如何在Hadoop Streaming中自定义状态信息，进而给用户反馈当前作业执行进度

 4.  如何在Hadoop Streaming中打印调试日志，在哪里可以看到这些日志

 5. 如何使用Hadoop Streaming处理二进制文件，而不仅仅是文本文件
 
##### Hadoop Streaming 编程

###### 简介

Hadoop Streaming是Hadoop提供的一个编程工具，它允许用户使用任何可执行文件或者脚本文件作为Mapper和Reducer，例如：

采用shell脚本语言中的一些命令作为mapper和reducer（cat作为mapper，wc作为reducer）
```shell
$HADOOP_HOME/bin/hadoop jar $HADOOP_HOME/contrib/streaming/hadoop-*-streaming.jar \
-input myInputDirs \
-output myOutputDir \
-mapper cat \
-reducer wc 
```
本文安排如下，第二节介绍Hadoop Streaming的原理，第三节介绍Hadoop Streaming的使用方法，第四节介绍Hadoop Streaming的程序编写方法，在这一节中，用C++、C、shell脚本 和python实现了WordCount作业，第五节总结了常见的问题.

###### 一 hadoop streaming 原理
mapper和reducer会从标准输入中读取用户数据，一行一行处理后发送给标准输出。Streaming工具会创建MapReduce作业，发送给各个tasktracker，同时监控整个作业的执行过程。

如果一个文件（可执行或者脚本）作为mapper，mapper初始化时，每一个mapper任务会把该文件作为一个单独进程启动，mapper任务运行时，它把输入切分成行并把每一行提供给可执行文件进程的标准输入。 同时，mapper收集可执行文件进程标准输出的内容，并把收到的每一行内容转化成key/value对，作为mapper的输出。 默认情况下，一行中第一个tab之前的部分作为key，之后的（不包括tab）作为value。如果没有tab，整行作为key值，value值为null。

对于reducer，类似。

以上是Map/Reduce框架和streaming mapper/reducer之间的基本通信协议。

###### 3. Hadoop Streamining 用法
```shell
#HADOOP_HOME/bin/hadoop jar \
$HADOOP_HOME/contrib/streaming/hadoop-*-streaminig.jar [options]

options:
（1）-input：输入文件路径

（2）-output：输出文件路径

（3）-mapper：用户自己写的mapper程序，可以是可执行文件或者脚本

（4）-reducer：用户自己写的reducer程序，可以是可执行文件或者脚本

（5）-file：打包文件到提交的作业中，可以是mapper或者reducer要用的输入文件，如配置文件，字典等。

（6）-partitioner：用户自定义的partitioner程序

（7）-combiner：用户自定义的combiner程序（必须用java实现）

（8）-D：作业的一些属性（以前用的是-jonconf），具体有：
1）mapred.map.tasks：map task数目
2）mapred.reduce.tasks：reduce task数目
3）stream.map.input.field.separator/stream.map.output.field.separator： map task输入/输出数
据的分隔符,默认均为\t。
4）stream.num.map.output.key.fields：指定map task输出记录中key所占的域数目
5）stream.reduce.input.field.separator/stream.reduce.output.field.separator：reduce task输入/输出数据的分隔符，默认均为\t。
6）stream.num.reduce.output.key.fields：指定reduce task输出记录中key所占的域数目
```

另外，Hadoop本身还自带一些好用的Mapper和Reducer：
（1）    Hadoop聚集功能
Aggregate提供一个特殊的reducer类和一个特殊的combiner类，并且有一系列的“聚合器”（例如“sum”，“max”，“min”等）用于聚合一组value的序列。用户可以使用Aggregate定义一个mapper插件类，这个类用于为mapper输入的每个key/value对产生“可聚合项”。Combiner/reducer利用适当的聚合器聚合这些可聚合项。要使用Aggregate，只需指定“-reducer aggregate”。

（2）字段的选取（类似于Unix中的‘cut’）
Hadoop的工具类org.apache.hadoop.mapred.lib.FieldSelectionMapReduc帮助用户高效处理文本数据，就像unix中的“cut”工具。工具类中的map函数把输入的key/value对看作字段的列表。 用户可以指定字段的分隔符（默认是tab），可以选择字段列表中任意一段（由列表中一个或多个字段组成）作为map输出的key或者value。 同样，工具类中的reduce函数也把输入的key/value对看作字段的列表，用户可以选取任意一段作为reduce输出的key或value。

###### 4. Mapper 和 Reducer 实现
本节试图用尽可能多的语言编写Mapper和Reducer，包括Java，C，C++，Shell脚本，python等（初学者运行第一个程序时，务必要阅读第5部分 “常见问题及解决方案”！！！！）。

由于Hadoop会自动解析数据文件到Mapper或者Reducer的标准输入中，以供它们读取使用，所有应先了解各个语言获取标准输入的方法。

（1）    Java语言：

见Hadoop自带例子

 (2) c++ ：
 
 ```shell
 string key;
 while(cin >> key) {
 cin>>value;
 ....
 }
 ```
(3) shell 脚本
```shell
 管道
```
（4） python 脚本
```shell
import sys
for line in sys.stdin:
 ......
```
为了说明各种语言编写Hadoop Streaming程序的方法，下面以WordCount为例，WordCount作业的主要功能是对用户输入的数据中所有字符串进行计数。

1. C语言

```c
//mapper
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
 
#define BUF_SIZE        2048
#define DELIM   "\n"
 
int main(int argc, char *argv[]){
     char buffer[BUF_SIZE];
     while(fgets(buffer, BUF_SIZE - 1, stdin)){
            int len = strlen(buffer);
            if(buffer[len-1] == '\n')
             buffer[len-1] = 0;
 
            char *querys  = index(buffer, ' ');
            char *query = NULL;
            if(querys == NULL) continue;
            querys += 1; /*  not to include '\t' */
 
            query = strtok(buffer, " ");
            while(query){
                   printf("%s\t1\n", query);
                   query = strtok(NULL, " ");
            }
     }
     return 0;
}
//---------------------------------------------------------------------------------------
//reducer
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
 
#define BUFFER_SIZE     1024
#define DELIM   "\t"
 
int main(int argc, char *argv[]){
 char strLastKey[BUFFER_SIZE];
 char strLine[BUFFER_SIZE];
 int count = 0;
 
 *strLastKey = '\0';
 *strLine = '\0';
 
 while( fgets(strLine, BUFFER_SIZE - 1, stdin) ){
   char *strCurrKey = NULL;
   char *strCurrNum = NULL;
 
   strCurrKey  = strtok(strLine, DELIM);
   strCurrNum = strtok(NULL, DELIM); /* necessary to check error but.... */
 
   if( strLastKey[0] == '\0'){
     strcpy(strLastKey, strCurrKey);
   }
 
   if(strcmp(strCurrKey, strLastKey)) {
     printf("%s\t%d\n", strLastKey, count);
     count = atoi(strCurrNum);
   } else {
     count += atoi(strCurrNum);
   }
   strcpy(strLastKey, strCurrKey);
 
 }
 printf("%s\t%d\n", strLastKey, count); /* flush the count */
 return 0;
}
```

2. C++ 语言实现

```c++
//mapper
#include <stdio.h>
#include <string>
#include <iostream>
using namespace std;
 
int main(){
        string key;
        string value = "1";
        while(cin>>key){
                cout<<key<<"\t"<<value<<endl;
        }
        return 0;
}
//------------------------------------------------------------------------------------------------------------
//reducer
#include <string>
#include <map>
#include <iostream>
#include <iterator>
using namespace std;
int main(){
        string key;
        string value;
        map<string, int> word2count;
        map<string, int>::iterator it;
        while(cin>>key){
                cin>>value;
                it = word2count.find(key);
                if(it != word2count.end()){
                        (it->second)++;
                }
                else{
                        word2count.insert(make_pair(key, 1));
                }
        }
 
        for(it = word2count.begin(); it != word2count.end(); ++it){
                cout<<it->first<<"\t"<<it->second<<endl;
        }
        return 0;
}
```

3. shell脚本实现类

```shell
$HADOOP_HOME/bin/hadoop  jar $HADOOP_HOME/hadoop-streaming.jar \
    -input myInputDirs \
    -output myOutputDir \
    -mapper cat \
   -reducer  wc
```

详细版
* mapper.sh

```shell
#! /bin/bash
while read LINE;do
	for word in $LINE
    do
    	echo "$word 1"
    done
done
```

reducer.sh

```shell
# ! /bin/bash
count = 0
started = 0
word = ""
while read LINE;do
	newword=`echo $LINE | cut -d ' ' -f1`
    if [ "$word" != "$newWord" ]; then
    	[ $started -ne 0 ] && echo "$word\t$count"
        word=$newword
        count=1
        started=1
    else
    	count=$(( $count + 1 ))
    fi
 done
 echo "$word\t$count"
```

4. python 脚本实现
mapper.py

```shell
#!/usr/bin/env python

import sys

word2count = {}

for line in sys.stdin:
	line = line.strip()
    
    # split the line into words while removing any empty strings
    words = filter(lambda word:word,line.split)
    
    #
    for word in words:
    	print '%s\t%s' % (word,1)
```
reducer.py

```python
#! /usr/bin/env python

from operator import itemgetter
import sys

word2count={}

for line in sys.stdin:
	line=line.strip()
    
    word,count = line.split()
    
    try:
    	count=int(count)
        word2count[word] = word2count.get(word,0) + count
    except ValueError :
    
    	pass
 
sorted_word2count = sorted(word2count.items(),key=itemgetter(0))

#
for word,count in sorted_word2count:
	print "%s\t%s" % (word,count)
    
```

###### 5. 常见问题以及解决方案
（1）作业总是运行失败，

提示找不多执行程序， 比如“Caused by: java.io.IOException: Cannot run program “/user/hadoop/Mapper”: error=2, No such file or directory”：

可在提交作业时，采用-file选项指定这些文件， 比如上面例子中，可以使用“-file Mapper -file Reducer” 或者 “-file Mapper.py -file Reducer.py”， 这样，Hadoop会将这两个文件自动分发到各个节点上，比如：

```shell
    $HADOOP_HOME/bin/hadoop  jar $HADOOP_HOME/contrib/streaming/hadoop-*-streaming.jar \

    -input myInputDirs \

    -output myOutputDir \

    -mapper Mapper.py\

    -reducer Reducerr.py\

    -file Mapper.py \

    -file Reducer.py
```

（2）用脚本编写时，第一行需注明脚本解释器，默认是shell   （3）如何对Hadoop Streaming程序进行测试？   Hadoop Streaming程序的一个优点是易于测试，比如在Wordcount例子中，可以运行以下命令在本地进行测试：

```python
    cat input.txt | python Mapper.py | sort | python Reducer.py
```

或者
```shell
    cat input.txt | ./Mapper | sort | ./Reducer
````


##### hadoop streaming 高级编程

###### 1. 简介
本文主要介绍了Hadoop Streaming的一些高级编程技巧，包括，怎样在mapredue作业中定制输出输出格式？怎样向mapreduce作业中传递参数？怎么在mapreduce作业中加载词典？怎样利用Hadoop Streamng处理二进制格式的数据等。

关于Hadoop Streaming的基本编程方法，可参考：Hadoop Streaming编程，Hadoop编程实例。

###### 2. 在mapreduce 作业中定值输入输出格式

Hadoop 0.21.0之前的版本中的Hadoop Streaming工具只支持文本格式的数据，而从Hadoop 0.21.0开始，也支持二进制格式的数据。这里介绍文本文件的输入输出格式定制，关于二进制数据的格式，可参考第5节。

Hadoop Streaming提交作业的格式为：

```shell	
Usage: $HADOOP_HOME/bin/hadoop jar \
 
$HADOOP_HOME/hadoop-streaming.jar [options]
```

其中，-D选项中的一些配置属性可定义输入输出格式，具体如下（注意，对于文本而言，每一行中存在一个key/value对，这里只能定制key和value之间的分割符，而行与行之间的分隔符不可定制，只能是\n）：

```shell
（1）stream.map.input.field.separator/stream.map.output.field.separator： map task输入/输出数据的分隔符,默认均为\t。

（2）stream.num.map.output.key.fields：指定map task输出记录中key所占的域数目，如

每一行形式为，Key1\tkey2\tkey3\tvalue，采用默认的分隔符，且stream.num.map.output.key.fields设为2，则Key1\tkey2表示key，key3\tvalue表示value。

（3）stream.reduce.input.field.separator/stream.reduce.output.field.separator：reduce task输入/输出数据的分隔符，默认均为\t。

（4）stream.num.reduce.output.key.fields：指定reduce task输出记录中key所占的域数目
```

###### 3. 向mapreduce作业传递参数
提交作业时，使用-cmdenv选项以环境变量的形式将你的参数传递给mapper/reducer，如：

```shell
$HADOOP_HOME/bin/hadoop jar \
 
contrib/streaming/hadoop-0.20.2-streaming.jar \
 
-input input \
 
-ouput output \
 
-cmdenv grade=1 \
 
…….
```

然后编写mapper或reducer时，使用main函数的第三个参数捕获你传入的环境变量，如：

```shell
int main(int argc, char *argv[], char *env[]){
 
int i, grade;
 
for (i = 0; env[i] != NULL; i++)
 
if(strncmp(env[i], “grade=”, 6) == 0)
 
grade=atoi(env[i]+6);
 
……
 
}
```

###### 4.在mapreduce作业中加载辞典
提交作业时使用 -file 选项

```shell
$HADOOP_HOME/bin/hadoop jar \
 
contrib/streaming/hadoop-0.20.2-streaming.jar \
 
-input input \
 
-ouput output \
 
-file dict.txt \
 
…….
```

然后编写mapper或reducer时，像本地文件一样打开并使用dic.txt文件，如：

```shell
int main(int argc, char *argv[], char *env[]){
 
FILE *fp;
 
char buffer[1024];
 
fp = fopen("dict.txt","r");
 
if (!fp) return 1;
 
while (fgets(buffer, 1024, fp)!=NULL) {
 
……
 
}
 
……
 
}
```

如果要加载非常大的词典或配置文件，Hadoop Streaming还提供了另外一个选项-files，该选项后面跟的是HDFS上的一个文件（将你的配置文件放到HDFS上，再大也可以！！！），你可以在程序中像打开本地文件一样打开该文件，此外，你也可以使用#符号在本地建一个系统链接，如：

```shell
$HADOOP_HOME/bin/hadoop jar \
 
contrib/streaming/hadoop-0.20.2-streaming.jar \
 
-file  hdfs://host:fs_port/user/dict.txt#dict_link \
 
…….
```
在代码中这样做：

如：

```shell
int main(int argc, char *argv[], char *env[]){
 
FILE *fp;
 
char buffer[1024];
 
fp = fopen("dict_link ","r"); //or fp = fopen("dict.txt ","r");
 
if (!fp) return 1;
 
while (fgets(buffer, 1024, fp)!=NULL) {
 
……
 
}
 
……
 
}
```

###### 5. 处理二进制格式的数据
从Hadoop 0.21.0开始，streaming支持二进制文件（具体可参考：HADOOP-1722），用户提交作业时，使用-io选项指明二进制文件格式。0.21.0版本中增加了两种二进制文件格式，分别为：

（1） rawbytes：key和value均用【4个字节的长度+原始字节】表示

（2） typedbytes：key和value均用【1字节类型+4字节长度+原始字节】表示

用户提交作业时，如果用-io指定二进制格式为typedbytes，则map的输入输出，reduce的输入输出均为typedbytes，如果想细粒度的控制这几个输入输出，可采用以下几个选项：

```shell
-D stream.map.input=[identifier]
 
-D stream.map.output=[identifier]
 
-D stream.reduce.input=[identifier]
 
-D stream.reduce.output=[identifier]
```

你如果采用的python语言，下面是从 HADOOP-1722 中得到的一个例子（里面用到了解析typedbytes的python库，见：http://github.com/klbostee/typedbytes ）：

mapper脚本如下

```python
import sys
 
import typedbytes
 
input = typedbytes.PairedInput(sys.stdin)
 
output = typedbytes.PairedOutput(sys.stdout)
 
for (key, value) in input:
 
for word in value.split():
 
output.write((word, 1))
```
reducer 脚本

```python
import sys
 
import typedbytes
 
from itertools import groupby
 
from operator import itemgetter
 
input = typedbytes.PairedInput(sys.stdin)
 
output = typedbytes.PairedOutput(sys.stdout)
 
for (key, group) in groupby(input, itemgetter(0)):
 
values = map(itemgetter(1), group)
 
output.write((key, sum(values)))
```

###### 6. 自定义counter 并增加counter的值

用户采用某种语言编写的mapper或者reducer可采用标准错误输出（stderr）自定义和改变counter值，格式为：reporter:counter:<group>,<counter>,<amount>，如，在C语言编写的mapper/reducer中：

```shell
fprintf(stderr, “reporter:counter:group,counter1,1”); //将组group中的counter1增加1
```

注：用户定义的自定义counter的最终结果会在桌面或者web界面上显示出来。

如果你想在mapreduce作业执行过程中，打印一些状态信息，同样可使用标准错误输出，格式为：reporter:status:<message>，如，在C语言编写的mapper/reducer中：

```shell
fprintf(stderr, “reporter:status:mapreduce job is started…..”); //在shell桌面上打印“mapreduce job is started…..”
```

###### 7. 在mapreduce使用Linux Pipes

迄今为止（0.21.0版本之前，包括0.21.0），Hadoop Streaming是不支持Linux Pipes，如：-mapper “cut -f1 | sed s/foo/bar/g”会报”java.io.IOException: Broken pipe”错误。

###### 8. 在mapreduce中获取JobConf的属性值

在0.21.0版本中，streaming作业执行过程中，JobConf中以mapreduce开头的属性（如mapreduce.job.id）会传递给mapper和reducer，关于这些参数，可参考：http://hadoop.apache.org/mapreduce/docs/r0.21.0/mapred_tutorial.html#Configured+Parameters

其中，属性名字中的“.”会变成“_”，如mapreduce.job.id会变为mapreduce_job_id，用户可在mapper/reducer中获取这些属性值直接使用（可能是传递给环境变量参数，即main函数的第三个参数，本文作业还未进行验证）。

###### 9. 一些Hadoop Streaming的开源软件包

（1） 针对Hadoop Streaming常用操作的C++封装包（如自定义和更新counter，输出状态信息等）：https://github.com/dgleich/hadoopcxx

（2） C++实现的typedbytes代码库：https://github.com/dgleich/libtypedbytes

（3） python实现的typedbytes代码库： http://github.com/klbostee/typedbytes

（4） Java实现的typedbytes代码库(Hadoop 0.21.0代码中自带)

###### 10. 总结

Hadoop Streaming使得程序员采用各种语言编写mapreduce程序变得可能，它具备程序员所需的大部分功能接口，同时由于这种方法编写mapreduce作业简单快速，越来越多的程序员开始尝试使用Hadoop Steraming。

##### hadoop streaming 编程实例

下面是本文要处理的问题：
（1） 对于一种编写语言，应该怎么编写Mapper和Reduce，需遵循什么样的编程规范

（2） 如何在Hadoop Streaming中自定义Hadoop Counter

（3） 如何在Hadoop Streaming中自定义状态信息，进而给用户反馈当前作业执行进度

（4） 如何在Hadoop Streaming中打印调试日志，在哪里可以看到这些日志[](http://)

###### 1. C++ 版wordCount 
* mapper 实现 

```c++
#include <iostream>
#include <string>
using namespace std;
int main() {
  string key;
  while(cin >> key) {
    cout << key << "\t" << "1" << endl;
    // Define counter named counter_no in group counter_group
    cerr << "reporter:counter:counter_group,counter_no,1\n";
    // dispaly status
    cerr << "reporter:status:processing......\n";
    // Print logs for testing
    cerr << "This is log, will be printed in stdout file\n";
  }
  return 0;
}
```

* Reducer 实现 
```c++
#include <iostream>
#include <string>
 
using namespace std;
int main() { //reducer将会被封装成一个独立进程，因而需要有main函数
  string cur_key, last_key, value;
  cin >> cur_key >> value;
  last_key = cur_key;
  int n = 1;
  while(cin >> cur_key) { //读取map task输出结果
    cin >> value;
    if(last_key != cur_key) { //识别下一个key
      cout << last_key << "\t" << n << endl;
      last_key = cur_key;
      n = 1;
    } else { //获取key相同的所有value数目
      n++; //key值相同的，累计value值
    }
  }
  cout << last_key << "\t" << n << endl;
  return 0;
}
```
* 编译运行
  编译以上两个程序
  
  ```shell
  g++ -o mapper mapper.cpp
  g++ -o reducer reducer.cpp
  ```

  测试一下：
  ```shell
   echo “dong xicheng is here now, talk to dong xicheng now” | ./mapper | sort | ./reducer
  ```
 注：上面这种测试方法会频繁打印以下字符串，可以先注释掉，这些字符串hadoop能够识别

 ```shell
    reporter:counter:counter_group,counter_no,1
    reporter:status:processing……
    This is log, will be printed in stdout file
 ```
 
 测试通过后，可以通过以下脚本将作业提交到集群中 (run_cpp_mr.sh):
 
 ```shell
 HADOOP_HOME=/opt/yarn_client
 INPUT_PATH=/test/input
 OUTPUT_path=/test/output
 echo "clearing output path: $OUTPUT_PATH"
 $HADOOP_HOME/bin/hadoop fs -rmr $OUTPUT_PATH
 
 ${HADOOP_HOME}/bin/hadoop jar \
   ${HADOOP_HOME}/share/hadoop/tools/lib/hadoop-streaming-2.2.0.jar \
   -files mapper,reducer \
   -input $INPUT_PATH
   -output $OUTPUT_PATH \
   -mapper mapper
   -reducer reducer
 ```
 
###### 2. Shell 版 wordcount
mapper.sh

```shell
#!/bin/bash

while read LINE;do
	for word in $LINE
    do
    	echo "$word 1"
        
        echo "reporter:counter:counter_group,count_no,1" > &2
        echo "reproter:counter:status,processing ... ..." > &2
        echo "This is long for testing ,will be printed in stdout file"> &2
    done
done
```

Reducer.sh

```shell
count=0
started=0
while read LINE ;do
	newword=`echo $Line | cut -d ' ' -f1 `
    if [ "$word" != "$newword" ];then
    	[ $started -ne 0 ] && echo "$word\t$count"
        word=$newword
        count=1
        started=1
    else
    	count=$(( $count + 1 ))
    fi
done
echo "$word\t$count"
```

3. 测试程序

```shell
echo “dong xicheng is here now, talk to dong xicheng now” | sh mapper.sh | sort | sh reducer.sh
```
上面这种测试方法会频繁打印以下字符串，可以先注释掉，这些字符串hadoop能够识别

```shell
    reporter:counter:counter_group,counter_no,1

    reporter:status:processing……

    This is log, will be printed in stdout file
```

测试通过后，可通过以下脚本将作业提交到集群中（run_shell_mr.sh）：

```shell
#!/bin/bash
HADOOP_HOME=/opt/yarn-client
INPUT_PATH=/test/input
OUTPUT_PATH=/test/output
echo "Clearing output path: $OUTPUT_PATH"
$HADOOP_HOME/bin/hadoop fs -rmr $OUTPUT_PATH
 
${HADOOP_HOME}/bin/hadoop jar\
   ${HADOOP_HOME}/share/hadoop/tools/lib/hadoop-streaming-2.2.0.jar\
  -files mapper.sh,reducer.sh\
  -input $INPUT_PATH\
  -output $OUTPUT_PATH\
  -mapper "sh mapper.sh"\
  -reducer "sh reducer.sh"
```

3. 程序说明
在Hadoop Streaming中，标准输入、标准输出和错误输出各有妙用，其中，标准输入和输出分别用于接受输入数据和输出处理结果，而错误输出的意义视内容而定：

（1）如果标准错误输出的内容为：reporter:counter:group,counter,amount，表示将名称为counter，所在组为group的hadoop counter值增加amount，hadoop第一次读到这个counter时，会创建它，之后查找counter表，增加对应counter值

（2）如果标准错误输出的内容为：reporter:status:message，则表示在界面或者终端上打印message信息，可以是一些状态提示信息

（3）如果采用错误输出的内容不是以上两种情况，则表示调试日志，Hadoop会将其重定向到stderr文件中。注：每个Task对应三个日志文件，分别是stdout、stderr和syslog，都是文本文件，可以在web界面上查看这三个日志文件内容，也可以登录到task所在节点上，到对应目录中查看。

另外，需要注意一点，默认Map Task输出的key和value分隔符是\t，Hadoop会在Map和Reduce阶段按照\t分离key和value，并对key排序，注意这点非常重要，当然，你可以使用stream.map.output.field.separator指定新的分隔符。 


##### 利用Hadoop Streaming 处理二进制文件

Hadoop Streaming是Hadoop提供的多语言编程工具，用户可以使用自己擅长的编程语言（比如python、php或C#等）编写Mapper和Reducer处理文本数据。Hadoop Streaming自带了一些配置参数可友好地支持多字段文本数据的处理，参与Hadoop Streaming介绍和编程，可参考我的这篇文章：“Hadoop Streaming编程实例”。然而，随着Hadoop应用越来越广泛，用户希望Hadoop Streaming不局限在处理文本数据上，而是具备更加强大的功能，包括能够处理二进制数据；能够支持多语言编写Combiner等组件。随着Hadoop 2.x的发布，这些功能已经基本上得到了完整的实现，本文将介绍如何使用Hadoop Streaming处理二进制格式的文件，包括SequenceFile，HFile等。

注：本文用到的程序实例可在百度云：hadoop-streaming-binary-examples 下载。

在详细介绍操作步骤之前，先介绍本文给出的实例。假设有这样的SequenceFile，它保存了手机通讯录信息，其中，key是好友名，value是描述该好友的一个结构体或者对象，为此，本文使用了google开源的protocol buffer这一序列化/反序列化框架，protocol buffer结构体定义如下：

```shell
option java_package = "";
option java_outer_classname="PersonInfo";
 
message Person {
  optional string name = 1;
  optional int32 age = 2;
  optional int64 phone = 3;
  optional string address = 4;
}
```
SequenceFile文件中的value便是保存的Person对象序列化后的字符串，这是典型的二进制数据，不能像文本数据那样可通过换行符解析出每条记录，因为二进制数据的每条记录中可能包含任意字符，包括换行符。

一旦有了这样的SequenceFile之后，我们将使用Hadoop Streaming编写这样的MapReduce程序：这个MapReduce程序只有Map Task，任务是解析出文件中的每条好友记录，并以name \t age,phone,address的文本格式保存到HDFS上。


###### 1. 准备数据

首先，我们需要准备上面介绍的SequenceFile数据，生成数据的核心代码如下：

```shell
final SequenceFile.Writer out =
        SequenceFile.createWriter(fs, getConf(), new Path(args[0]),
                Text.class, BytesWritable.class);
Text nameWrapper = new Text();
BytesWritable personWrapper = new BytesWritable();
System.out.println("Generating " + num + " Records......");
for(int i = 0; i < num; i++) {
  genOnePerson(nameWrapper, personWrapper);
  System.out.println("Generating " + i + " Records," + nameWrapper.toString() + "......");
  out.append(nameWrapper, personWrapper);
}
out.close();
```
当然，为了验证我们产生的数据是否正确，需要编写一个解析程序，核心代码如下：

```shell
Reader reader = new Reader(fs, new Path(args[0]), getConf());
Text key = new Text();
BytesWritable value = new BytesWritable();
while(reader.next(key, value)) {
  System.out.println("key:" + key.toString());
  value.setCapacity(value.getSize()); // Very important!!! Very Tricky!!!
  PersonInfo.Person person = PersonInfo.Person.parseFrom(value.getBytes());
  System.out.println("age:" + person.getAge()
          + ",address:" + person.getAddress()
          +",phone:" + person.getPhone());
}
reader.close();
```

需要注意的，Value保存类型为BytesWritable，使用这个类型非常容易犯错误。当你把一堆byte[]数据保存到BytesWritable后，通过BytesWritable.getBytes()再读到的数据并不一定是原数据，可能变长了很多，这是因为BytesWritable采用了自动内存增长算法，你保存的数据长度为size时，它可能将数据保存到了长度为capacity（capacity>size）的buffer中，这时候，你通过BytesWritable.getBytes()得到的数据最后一些字符是多余的，如果里面保存的是protocol buffer序列化后的字符串，则无法反序列化，这时候可以使用BytesWritable.setCapacity (value.getSize())将后面多余空间剔除掉。

###### 2. 使用hadoop streaming 编写C++ 程序
为了说明Hadoop Streaming如何处理二进制格式数据，本文仅仅以C++语言为例进行说明，其他语言的设计方法类似。

先简单说一下原理。当输入数据是二进制格式时，Hadoop Streaming会对输入key和value进行编码后，通过标准输入传递给你的Hadoop Streaming程序，目前提供了两种编码格式，分别是rawtypes和         typedbytes，你可以设计你想采用的格式，这两种编码规则如下（具体在文章“Hadoop Streaming高级编程”中已经介绍了）：

rawbytes：key和value均用【4个字节的长度+原始字节】表示

typedbytes：key和value均用【1字节类型+4字节长度+原始字节】表示

本文将采用第一种编码格式进行说明。采用这种编码意味着你不能想文本数据那样一次获得一行内容，而是依次获得key和value序列，其中key和value都由两部分组成，第一部分是长度（4个字节），第二部分是字节内容，比如你的key是dongxicheng，value是goodman，则传递给hadoop streaming程序的输入数据格式为11 dongxicheng 7 goodman。为此，我们编写下面的Mapper程序解析这种数据：

```shell
int main() {

String key,value;

while(!cin.eof()) {
	if (!FileUtil::ReadString(key,cin))
    	break;
    FileUtil::ReadString(value,cin);
    Person person;
    ProtoUtil::ParseFromString(value,person);
    
    count<<person.name<<"\t"<<person.age()
    	<<","<<person.address()
        <<","<<person.phone()<<endl;
}
return 0;
}
```

其中，辅助函数实现如下：

```shell
class ProtoUtil {

	public:
    	static bool ParseFromString(const string& str,Person &person){
        if(person.ParseFromString(str))
        	return true
        return false;
        }
};

class FileUtil {

public :
	static bool ReadInt(unsigned int * len,istream &stream) {
    if(!stream.read((char * ) len ,sizeof(unsigned int)))
    	return false;
    * len = bswap_32(*len);
    return true;
    }
    
    static bool ReadString(string &str,istream &stream) {
    unsigned int len;
    if(!ReadInt(&len ,stream))
    	return false;
    str.resize(len);
    if(!ReadBytes(&str[0],len,stream)){
    	return false;
    }
    return true;
    }
    
  static bool ReadBytes(char *ptr,unsigned int len ,istream &stram){
  	stream.read(ptr,sizeof(unsigned char) * len);
    if(stream.eof()) return false;
    return true;
  }
};
```
该程序需要注意以下几点：

（1）注意大小端编码规则，解析key和value长度时，需要对长度进行字节翻转。

（2）注意循环结束条件，仅仅靠!cin.eof()判定是不够的，仅靠这个判定会导致多输出一条重复数据。

（3）本程序只能运行在linux系统下，windows操作系统下将无法运行，因为windows下的标准输入cin并直接支持二进制数据读取，需要将其强制以二进制模式重新打开后再使用。

###### 3. 程序测试与运行
程序写好后，第一步是编译C++程序。由于该程序需要运行在多节点的Hadoop集群上，为了避免部署或者分发动态库带来的麻烦，我们直接采用静态编译方式，这也是编写Hadoop C++程序的基本规则。为了静态编译以上MapReduce程序，安装protocol buffers时，需采用以下流程（强调第一步），

```shell
./configure-disable-shared
make -j4
make install
```
然后使用以下命令编译程序， 生成可执行文件ProtoMapper

```shell
g++ -o ProtoMapper ProtoMapper.cpp person.pb.cc `pkg-config –cflags –static –libs protobuf` -lpthread
```

在正式将程序提交到Hadoop集群之前，需要先在本地进行测试，本地测试运行脚本如下：

```shell
#!/bin/bash
HADOOP_HOME=/opt/dong/yarn-client
INPUT_PATH=/tmp/person.seq
OUTPUT_PATH=file:///tmp/output111
echo "Clearing output path: $OUTPUT_PATH"
$HADOOP_HOME/bin/hadoop fs -rmr $OUTPUT_PATH
 
${HADOOP_HOME}/bin/hadoop jar\
   ${HADOOP_HOME}/share/hadoop/tools/lib/hadoop-streaming-2.2.0.jar\
  -D mapred.reduce.tasks=0\
  -D stream.map.input=rawbytes\
  -files ProtoMapper\
  -jt local\
  -fs local\
  -input $INPUT_PATH\
  -output $OUTPUT_PATH\
  -inputformat SequenceFileInputFormat\
  -mapper ProtoMapper
```

注意以下几点：

（1）使用stream.map.input指定输入数据解析成rawbytes格式

（2） 使用-jt和-fs两个参数将程序运行模式设置为local模式

（3）使用-inputformat指定输入数据格式为SequenceFileInputFormat

（4）使用mapred.reduce.tasks将Reduce Task数目设置为0

在本地tmp/output111目录下查看测试结果是否正确，如果没问题，可改写该脚本（去掉-fs和-jt两个参数，将输入和输出目录设置成HDFS上的目录），将程序直接运行在Hadoop上。


##### 提高streaming 脆弱代码的健壮性

[传送](http://blog.factual.com/cn/practical-hadoop-streaming/)

我曾简单地认为仅仅通过设置类似于“stop_failing_dammit=true”的选项，就可以让Hadoop Job在运行过程中重试失败的任务，同时跳过持续失败的数据记录。但实际在配置上所需的工作量也是出奇的少。在深入研究后，我终于搞懂了为什么这部分在文档中不那么容易描述清楚。Hadoop假定你的程序能够处理好所有的异常，但是结果往往是Hadoop Streaming出现异常时，你无法在你的程序中做任何补救措施。

###### 处理无法预知的异常
为了处理无法预知的异常导致任务失败，你只需要设置足够的任务重试次数，以保证每个Hadoop任务(task)都可以顺利完成。一个极端的方式是设定足够大的任务重试次数来保证Job永不失败。而比较合理的是以FAILURE_RATE * TOTAL_RECORDS / NUMBER_OF_TASKS的倍数设置任务的重试次数。

对于mapper，可以用如下选项设置重试次数：

```shell
mapred.map.max.attempts
```
对于reducer，可以用如下选项设置重试次数：

```shell
mapred.reduce.max.attempts
```
如果同一个任务(Task)在运行过程中失败的次数超过上述设定的值，你的Hadooop Job将会被终止掉。

###### 处理有规律的异常
之前我曾提到过有些Web页面会导致libxml内存段错误，这类异常在ruby中是无法捕获的，所以Hadoop任务(Task)在处理到有问题的数据记录时总会失败。

幸运的是，Hadoop存在一个功能可以跳过不好的数据记录。然而当我们想方设法利用这个功能来提高Hadoop Job的稳定性时，我们发现仅仅设置mapred.skip.mode.enabled=true是不够的。

尤其是对于Hadoop Streaming，Hadoop是无法准确知道哪一条数据记录导致任务(Task)的失败，所以它无法自动地在重试任务时跳过有问题的数据记录。我们可以通过一些设置来引导Hadoop查找到引起任务失败的数据记录，同时可以节约Hadoop在查找方面的所耗费时间。当一个任务失败，skip模式即被触发，Hadoop能够通过折半搜索的方式获取失败的数据记录范围。

* 触发skip模式
如果同一条数据记录导致失败超过一定的次数(默认值为2)，Hadoop便进入skip模式。

```shell
mapred.skip.attempts.to.start.skipping=2
```

* 均衡重试次数和丢弃的数据量

一旦Hadoop进入skip模式，我们可以选择是跳过有问题数据记录，还是丢弃整块输入数据，或者跳过介于两者之间的数据记录。这个设定可以控制Hadoop搜索有问题的数据记录范围所耗费的时间。

```shell
mapred.skip.map.max.skip.records=1               #only skip the bad record
mapred.skip.map.max.skip.records=0               #don’t go into skip mode
mapred.skip.map.max.skip.records=Long.MAX_VALUE  #don’t try to narrow
```

* 加点Hadoop调试信息

在每一条数据记录被处理完毕后，通过更新计数器有助于我们搞清问题的根源。在Hadoop Streaming中，我们可以在程序中输出如下的计数信息：

```shell
reporter:counter:SkippingTaskCounters,MapProcessedRecords,1
```

###### 组合在一起

mapper 程序

```shell
#!/usr/bin/env ruby
STDIN.each do |line|
  if line
    line.chomp!
    if line=='Naughty'
      #simulate a failure
      warn "No Presents!"
      exit!
    else
      warn "reporter:counter:SkippingTaskCounters,MapProcessedRecords,1"
      puts 'You get presents'
    end
  end
end
```

运行job

```shell
hadoop jar $HADOOP_HOME/contrib/streaming/hadoop-0.20.1+169.127-streaming.jar    \
  -D mapred.skip.mode.enabled=true                                               \
  -D mapred.skip.map.max.skip.records=1                                          \
  -D mapred.skip.attempts.to.start.skipping=2                                    \
  -D mapred.map.tasks=1000                                                       \
  -D mapred.map.max.attempts=10                                                  \
  -D mapred.reduce.tasks=0                                                       \
  -D mapred.job.name="Skip Bad Records Test"                                     \
  -input  "/user/hadoop/samples/skip-bad-records/skip_records_test.txt"          \
  -output "/user/hadoop/samples/skip-bad-records/output/"                        \
  -mapper "$APP_PATH/samples/map_reduce/naughty_or_nice.rb"
```

输出结果：

![](http://devblog.factual.com/wp-content/uploads/2011/04/skip_size_1.png)

![](http://devblog.factual.com/wp-content/uploads/2011/04/skip_size_1.png)

可以看到程序处理完所有的好的数据记录而跳过了10条含有”Naughty”字符串的数据记录。

为了测试mapred.skip.map.max.skip.records选项，将mapred.skip.map.max.skip.records设定为2重新运行上面的Job：

![](http://devblog.factual.com/wp-content/uploads/2011/04/skip_size_2.png)

![](http://devblog.factual.com/wp-content/uploads/2011/04/skip_size_2.png)


可以看到一些好的数据记录因为和那些坏的数据记录相邻而同时被忽略掉。

###### 最后的一些问题

可以看到设定的选项人为的增加了Hadoop Job需要处理数据记录的总数量，而mapred.skip.map.auto.incr.proc.count=false选项无法通过Hadoop jobconfig来设定，只能设定给整个Hadoop集群，不过那样的话对于同时使用Java和Streaming的Hadoop集群并不是很适合。



###### 官方提供的python 的mr 脚本

mapper :

```python
#!/usr/bin/env python 
"""A more advanced Mapper, using Python iterators and generators."""
import sys 
def read_input(file): 
    for line in file: 
        # split the line into words 
        yield line.split() 
def main(separator='\t'): 
    # input comes from STDIN (standard input) 
    data = read_input(sys.stdin) 
    for words in data: 
        # write the results to STDOUT (standard output); 
        # what we output here will be the input for the 
        # Reduce step, i.e. the input for reducer.py 
        # 
        # tab-delimited; the trivial word count is 1
        for word in words: 
            print '%s%s%d' % (word, separator, 1) 
if __name__ == "__main__": 
    main()
```

reducer:

```shell
#!/usr/bin/env python 
"""A more advanced Reducer, using Python iterators and generators."""
from itertools import groupby 
from operator import itemgetter 
import sys 
def read_mapper_output(file, separator='\t'): 
    for line in file: 
        yield line.rstrip().split(separator, 1) 
def main(separator='\t'): 
    # input comes from STDIN (standard input) 
    data = read_mapper_output(sys.stdin, separator=separator) 
    # groupby groups multiple word-count pairs by word, 
    # and creates an iterator that returns consecutive keys and their group: 
    #   current_word - string containing a word (the key) 
    #   group - iterator yielding all ["<current_word>", "<count>"] items 
    for current_word, group in groupby(data, itemgetter(0)): 
        try: 
            total_count = sum(int(count) for current_word, count in group) 
            print "%s%s%d" % (current_word, separator, total_count) 
        except ValueError: 
            # count was not a number, so silently discard this item 
            pass 
if __name__ == "__main__": 
    main()
```


##### mapreduce 中使用计数器

本文主要介绍了MapReduce中的自定义计数器的相关内容。

![](http://www.it165.net/uploadfile/files/2014/0504/20140504092225293.jpg)

可以看到最上方的关键字：Counters，这就表示计数器。

在这里，只有一个制表符缩进的表示计数器组，有两个制表符缩进的表示计数器组下的计数器。如File Output Format Counters就表示文件输出的计数器组，里面的Bytes Written表示输出的字符数，在输出的文本中，hello,you,me加起来是10个字符，2,1,1加起来是3个字符，中间在加上3个制表符，前两行中有2个换行符，最后一行有一个结束符，总共19个，跟计数器的19相等。

同时在第4组中，我们可以看到Reduce input records是4，Map output records也是4，说明了Map的输出就是Reduce的输入。

那么这些都是系统的计数器，如何自定义计数器呢？

例如，这里我们要记录一下hello出现的次数，只需要在自己的Mapper中加上计数器的相关内容即可，代码如下：

```java
static class MyMapper extends Mapper<LongWritable, Text, Text, LongWritable>{
    protected void map(LongWritable k1, Text v1, Context context) throws java.io.IOException ,InterruptedException {
        Counter helloCounter = context.getCounter("Sensitive Words", "hello");
        String line = v1.toString();
        if(line.contains("hello")){
            helloCounter.increment(1L);
        }
        final String[] splited = line.split(" ");
        for (String word : splited) {
            context.write(new Text(word), new LongWritable(1));
        }
        };
    }
```

对比原来的mapper，我们发现，只需要通过context获取计数器，然后根据需要记录相关内容即可。

以下是执行过程中控制台输出的内容：


![](http://www.it165.net/uploadfile/files/2014/0504/20140504092225294.jpg)

我们可以看到计数器增加到了20个，同时多了自定义计数器组Sensitive Words和组里的计数器hello，输入文件中有两个hello，因此这里hello计数器显示为2。(eclipse显示有限，最后一行没有显示出来，因此截图不全)







