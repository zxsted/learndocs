#### [hive 创建/删除/截断][0] 表(翻译自Hive wiki)
[toc]

 
#####简单的创建表
```sql
create table table_name (
  id                int,
  dtDontQuery       string,
  name              string
)
```
 

 
#####创建有分区的表
```sql
create table table_name (
  id                int,
  dtDontQuery       string,
  name              string
)
partitioned by (date string)
```
一个表可以拥有一个或者多个分区，每个分区以文件夹的形式单独存在表文件夹的目录下。

分区是以字段的形式在表结构中存在，通过describe table命令可以查看到字段存在，但是该字段不存放实际的数据内容，仅仅是分区的表示。

在Hive Select查询中一般会扫描整个表内容，会消耗很多时间做没必要的工作。有时候只需要扫描表中关心的一部分数据，因此建表时引入了partition概念。表中的一个 Partition 对应于表下的一个目录,Partition 就是辅助查询，缩小查询范围，加快数据的检索速度和对数据按照一定的规格和条件进行管理。

 
#####典型的默认创建表
```sql
CREATE TABLE page_view(
     viewTime INT, 
     userid BIGINT,
     page_url STRING, 
     referrer_url STRING,
     ip STRING COMMENT 'IP Address of the User')
 COMMENT 'This is the page view table'
 PARTITIONED BY(dt STRING, country STRING)
 ROW FORMAT DELIMITED
   FIELDS TERMINATED BY '\001'
   COLLECTION ITEMS TERMINATED BY '\002'
   MAP KEYS TERMINATED BY '\003'
 STORED AS TEXTFILE;
```

 

这里创建了表page_view,有表的注释，一个字段ip的注释，分区有两列,分别是dt和country。

[ROW FORMAT DELIMITED]关键字，是用来设置创建的表在加载数据的时候，支持的列分隔符。不同列之间用一个'\001'分割,集合(例如array,map)的元素之间以'\002'隔开,map中key和value用'\003'分割。

 

[STORED AS file_format]关键字是用来设置加载数据的数据类型,默认是TEXTFILE，如果文件数据是纯文本，就是使用 [STORED AS TEXTFILE]，然后从本地直接拷贝到HDFS上，hive直接可以识别数据。

 
#####常用的创建表
```sql
CREATE TABLE login(
     userid BIGINT,
     ip STRING, 
     time BIGINT)
 PARTITIONED BY(dt STRING)
 ROW FORMAT DELIMITED
   FIELDS TERMINATED BY '\t'
 STORED AS TEXTFILE;
```

 
#####创建外部表

如果数据已经存在HDFS的'/user/hadoop/warehouse/page_view'上了，如果想创建表，指向这个路径，就需要创建外部表:
复制代码
```sql
CREATE EXTERNAL TABLE page_view(
     viewTime INT, 
     userid BIGINT,
     page_url STRING, 
     referrer_url STRING,
     ip STRING COMMENT 'IP Address of the User',
     country STRING COMMENT 'country of origination')
 COMMENT 'This is the staging page view table'
 ROW FORMAT DELIMITED FIELDS TERMINATED BY '\054'
 STORED AS TEXTFILE
 LOCATION '/user/hadoop/warehouse/page_view';
```

创建表，有指定EXTERNAL就是外部表，没有指定就是内部表，内部表在drop的时候会从HDFS上删除数据，而外部表不会删除。

外部表和内部表一样，都可以有分区，如果指定了分区，那外部表建了之后，还要修改表添加分区。

外部表如果有分区，还可以加载数据，覆盖分区数据，但是外部表删除分区，对应分区的数据不会从HDFS上删除，而内部表会删除分区数据。

 
#####指定数据库创建表

如果不指定数据库，hive会把表创建在default数据库下，假设有一个hive的数据库mydb,要创建表到mydb,如下:
```sql
CREATE TABLE mydb.pokes(foo INT,bar STRING);
```
或者是
```sql
use mydb; --把当前数据库指向mydb
CREATE TABLE pokes(foo INT,bar STRING);
```
 
#####复制表结构
```sql
CREATE TABLE empty_table_name LIKE table_name;
```
根据table_name创建一个空表empty_table_name,empty_table_name没有任何数据。

```sql 
#####create-table-as-selectt (CTAS)
```
CTAS创建的表是原子性的，这意味着，该表直到所有的查询结果完成后，其他用户才可以看到完整的查询结果表。

CTAS唯一的限制是目标表，不能是一个有分区的表，也不能是外部表。

简单的方式
```sql
CREATE TABLE new_key_value_store
  AS 
SELECT (key % 1024) new_key, concat(key, value) key_value_pair FROM key_value_store;
```
复杂的方式
```sql
CREATE TABLE new_key_value_store
   ROW FORMAT SERDE "org.apache.hadoop.hive.serde2.columnar.ColumnarSerDe"
   STORED AS RCFile AS
SELECT (key % 1024) new_key, concat(key, value) key_value_pair
FROM key_value_store
SORT BY new_key, key_value_pair;
```
 
#####删除表
```sql
DROP TABLE table_name;
DROP TABLE IF EXISTS table_name;
```
删除表会移除表的元数据和数据，而HDFS上的数据，如果配置了Trash，会移到.Trash/Current目录下。

删除外部表时，表中的数据不会被删除。

 
#####截断表
```sql
TRUNCATE TABLE table_name;
TRUNCATE TABLE table_name PARTITION (dt='20080808');
```
从表或者表分区删除所有行，不指定分区，将截断表中的所有分区，也可以一次指定多个分区，截断多个分区。



####[Hive创建表和分区][1]
#####Hive创建表和分区
```sql
CREATE EXTERNAL TABLE IF NOT EXISTS data_zh(

ROWKEY STRING,

STATION INT,

YEAR INT,

MONTH INT,

DAY INT,

HOUR INT,

MINUTE INT,

)

PARTITIONED BY (AGE INT) 指定分区(此列并没真正存储列，也就是不存于你的数据中。但是如果你的数据从Oracle按年份导出，按照年份分区，把每一年数据放到age对应的目录下)

ROW FORMAT DELIMITED

FIELDS TERMINATED BY ‘,’ 字段分隔符

LINES TERMINATED BY ‘\n’ 行分隔符

STORED AS TEXTFILE; 作为文本存储
```
#####Hive加载数据到表中
```sql
LOAD DATA INPATH ‘/data/’ OVERWRITE INTO TABLE data_zh; #加载某个目录下所有数据，存在分区数据不能这样加载
```
#####分区加载数据
```sql
创建分区

ALTER TABLE data_zhp ADD PARTITION(AGE= 1998)

加载数据

LOAD DATA INPATH ‘/data/1998.txt’ INTO TABLE data_zhp PARTITION(YEAR=1998);

指定分区目录

LOCATION ‘ /hiveuser/hive/warehouse/data_zh.db/data_zh/2012.txt’;(指定分区所在位置)

加载数据到指定分区，分区表加载方法
```

#####下面为将现有表，修改为分区表。
注意创建分区的时候未指定分区表，不能创建分区。需要新建表哥，用动态分区导入数据，动态分区数量有限。参考Programming Hive记得最大上限应该是10000
Hive修改现有表添加分区
```sql
添加分区

ALTER TABLE data_zh ADD IF NOT EXISTS

PARTITION (year = 1998) LOCATION ‘/hiveuser/hive/warehouse/data_zh.db/data_zh/1998.txt’
```
删除分区
```sql
ALTER TABLE data_zhp DROP IF EXISTS PARTITION(year =1998);
```
修改现有表添加分区

创建分区表
```sql
CREATE EXTERNAL TABLE IF NOT EXISTS data_zhp(

ROWKEY STRING,

STATION INT,

MONTH INT,

DAY INT,

HOUR INT,

MINUTE INT,

)

PARTITIONED BY (YEAR INT)

ROW FORMAT DELIMITED

FIELDS TERMINATED BY ‘,’

LINES TERMINATED BY ‘\n’

STORED AS TEXTFILE;
```
动态分区指令
```sql
set hive.exec.dynamic.partition=true;

set hive.exec.dynamic.partition.mode=nonstrict;

#set hive.enforce.bucketing = true;
未分区表数据导入分区表

insert overwrite table data_zhp partition (YEAR,MONTH) select * from data_zh;
```



####hive sql 学习笔记(1)

#####一、 创建表
   在官方的wiki里，example是这样的：
```sql
    CREATE [EXTERNAL] TABLE [IF NOT EXISTS] table_name   
      [(col_name data_type [COMMENT col_comment], ...)]   
      [COMMENT table_comment]   
      [PARTITIONED BY (col_name data_type   
        [COMMENT col_comment], ...)]   
      [CLUSTERED BY (col_name, col_name, ...)   
      [SORTED BY (col_name [ASC|DESC], ...)]   
      INTO num_buckets BUCKETS]   
      [ROW FORMAT row_format]   
      [STORED AS file_format]   
      [LOCATION hdfs_path]  

```

 

 [ROW FORMAT DELIMITED]关键字，是用来设置创建的表在加载数据的时候，支持的列分隔符；
[STORED AS file_format]关键字是用来设置加载数据的数据类型。Hive本身支持的文件格式只有：Text File，Sequence File。如果文件数据是纯文本，可以使用 [STORED AS TEXTFILE]。如果数据需要压缩，使用 [STORED AS SEQUENCE] 。通常情况，只要不需要保存序列化的对象，我们默认采用[STORED AS TEXTFILE]。

 

  那么我们创建一张普通的hive表，hive sql就如下：
```sql
    CREATE TABLE test_1(id INT, name STRING, city STRING) SORTED BY TEXTFILE ROW FORMAT DELIMITED FIELDS TERMINATED BY ‘\t’  ;
```
   其中，hive支持的字段类型，并不多，可以简单的理解为数字类型和字符串类型，详细列表如下：
```sql
    TINYINT   
    SMALLINT  
    INT  
    BIGINT  
    BOOLEAN   
    FLOAT  
    DOUBLE  
    STRING  
```
 

注意partitioned by 的位置：
```sql
create table webdata2(vstart string,vend string,hdid int,userid  int,sid int,refsid  int,active  int,duration int,mdomain string,sdomain string,refsdomain string,ieid    int,refieid string,url     string,totaltime int,param2 int,param4 string,param4code string) partitioned by(pid int,daytime string) row format delimited fields terminated by '\t' stored as SEQUENCEFILE;
```
   Hive的表，与普通关系型数据库，如mysql在表上有很大的区别，所有hive的表都是一个文件，它是基于Hadoop的文件系统来做的。

   hive总体来说可以总结为三种不同类型的表。



1. 普通表 
    普通表的创建，如上所说，不讲了。其中，一个表，就对应一个表名对应的文件。

2. 外部表


  EXTERNAL 关键字可以让用户创建一个外部表，在建表的同时指定一个指向实际数据的路径（LOCATION），Hive 创建内部表时，会将数据移动到数据仓库指向的路径；若创建外部表，仅记录数据所在的路径，不对数据的位置做任何改变。在删除表的时候，内部表的元数据和数据会被一起删除，而外部表只删除元数据，不删除数据。具体sql如下：
```sql
    CREATE EXTERNAL TABLE test_1(id INT, name STRING, city STRING) SORTED BY TEXTFILE ROW FORMAT DELIMITED FIELDS TERMINATED BY'\t’ LOCATION ‘hdfs://http://www.cnblogs.com/..’  
```
3. 分区表


   有分区的表可以在创建的时候使用 PARTITIONED BY 语句。一个表可以拥有一个或者多个分区，每一个分区单独存在一个目录下。而且，表和分区都可以对某个列进行 CLUSTERED BY 操作，将若干个列放入一个桶（bucket）中。也可以利用SORT BY 对数据进行排序。这样可以为特定应用提高性能。具体SQL如下：
```sql
    CREATE TABLE test_1(id INT, name STRING, city STRING) PARTITIONED BY (pt STRING) SORTED BY TEXTFILE ROW FORMAT DELIMITED FIELDS TERMINATED BY‘\t’   
```
   Hive的排序，因为底层实现的关系，比较不同于普通排序，这里先不讲。

 

   桶的概念，主要是为性能考虑，可以理解为对分区内列，进行再次划分，提高性能。在底层，一个桶其实是一个文件。如果桶划分过多，会导致文件数量暴增，一旦达到系统文件数量的上限，就杯具了。哪种是最优数量，这个哥也不知道。

 

  分区表实际是一个文件夹，表名即文件夹名。每个分区，实际是表名这个文件夹下面的不同文件。分区可以根据时间、地点等等进行划分。比如，每天一个分区，等于每天存每天的数据；或者每个城市，存放每个城市的数据。每次查询数据的时候，只要写下类似 where pt=2010_08_23这样的条件即可查询指定时间得数据。

 

   总体而言，普通表，类似mysql的表结构，外部表的意义更多是指数据的路径映射。分区表，是最难以理解，也是最hive最大的优势。之后会专门针对分区表进行讲解。

 

#####二、 加载数据


  Hive不支持一条一条的用insert语句进行插入操作，也不支持update的操作。数据是以load的方式，加载到建立好的表中。数据一旦导入，则不可修改。要么drop掉整个表，要么建立新的表，导入新的数据。

官方指导为：
```sql
    LOAD DATA [LOCAL] INPATH 'filepath' [OVERWRITE] INTO TABLE tablename [PARTITION (partcol1=val1, partcol2=val2 ...)]  
```
   Hive在数据load这块，大方向分为两种方式，load文件或者查询一张表，或者将某张表里的额查询结果插入指定表。
如果划分更细一点个人归纳总结为4种不同的方式的load：

 

1. Load data到指定的表 
    直接将file，加载到指定的表，其中，表可以是普通表或者分区表。具体sql如下：
```sql
    LOAD DATA LOCAL INPATH '/home/admin/test/test.txt' OVERWRITE INTO TABLE test_1  
```

  关键字[OVERWRITE]意思是是覆盖原表里的数据，不写则不会覆盖。
    关键字[LOCAL]是指你加载文件的来源为本地文件，不写则为hdfs的文件。
    其中

     ‘/home/admin/test/test.txt’为绝对路径

 

2. load到指定表的分区 
    直接将file，加载到指定表的指定分区。表本身必须是分区表，如果是普通表，导入会成功，但是数据实际不会被导入。具体sql如下：
```sql
    LOAD DATA LOCAL INPATH '/home/admin/test/test.txt' OVERWRITE INTO TABLE test_1 PARTITION（pt=’xxxx）  
```
   load数据，hive支持文件夹的方式，将文件夹内的所有文件，都load到指定表中。Hdfs会将文件系统内的某文件夹路径内的文件，分散到不同的实际物理地址中。这样，在数据量很大的时候，hive支持读取多个文件载入，而不需要限定在唯一的文件中。
    
3. insert+select 


  这个是完全不同于文件操作的数据导入方式。官方指导为：
```sql
    Standard syntax:   
    INSERT OVERWRITE TABLE tablename1 [PARTITION (partcol1=val1, partcol2=val2 ...)] select_statement1 FROM from_statement    
      
    Hive extension (multiple inserts):   
    FROM from_statement   
    INSERT OVERWRITE TABLE tablename1 [PARTITION (partcol1=val1, partcol2=val2 ...)] select_statement1   
    [INSERT OVERWRITE TABLE tablename2 [PARTITION ...] select_statement2] ...   
      
    Hive extension (dynamic partition inserts):   
    INSERT OVERWRITE TABLE tablename PARTITION (partcol1[=val1], partcol2[=val2] ...) select_statement FROM from_statement  
```
   这个的用法，和上面两种直接操作file的方式，截然不同。从sql语句本身理解，就是把查询到的数据，直接导入另外一张表。这个暂时不仔细分析，之后查询章节，再细讲。


4. alter 表，对分区操作


   在对表结构进行修改的时候，我们可以增加一个新的分区，在增加新分区的同时，将数据直接load到新的分区当中。
```sql
    ALTER TABLE table_name ADD  
      partition_spec [ LOCATION 'location1' ]   
      partition_spec [ LOCATION 'location2' ] ...  
```



#### hive QL 插入语法

#####1.insert 语法格式为：
基本的插入语法：
```sql
INSERT OVERWRITE TABLE tablename [PARTITON(partcol1=val1,partclo2=val2)]select_statement FROM from_statement
insert overwrite table test_insert select * from test_table;
```
对多个表进行插入操作：
```sql
FROM fromstatte
INSERT OVERWRITE TABLE tablename1 [PARTITON(partcol1=val1,partclo2=val2)]select_statement1
INSERT OVERWRITE TABLE tablename2 [PARTITON(partcol1=val1,partclo2=val2)]select_statement2

from test_table                     
insert overwrite table test_insert1
select key
insert overwrite table test_insert2
select value;
```
insert的时候，from子句即可以放在select 子句后面，也可以放在 insert子句前面。
hive不支持用insert语句一条一条的进行插入操作，也不支持update操作。数据是以load的方式加载到建立好的表中。数据一旦导入就不可以修改。

#####2.通过查询将数据保存到filesystem
```sql
INSERT OVERWRITE [LOCAL] DIRECTORY directory SELECT.... FROM .....
```
导入数据到本地目录：
```sql
insert overwrite local directory '/home/zhangxin/hive' select * from test_insert1;
```
产生的文件会覆盖指定目录中的其他文件，即将目录中已经存在的文件进行删除。

导出数据到HDFS中：
```sql
insert overwrite directory '/user/zhangxin/export_test' select value from test_table;
```
同一个查询结果可以同时插入到多个表或者多个目录中：
```sql
from test_insert1
insert overwrite local directory '/home/zhangxin/hive' select *
insert overwrite directory '/user/zhangxin/export_test' select value;
```




####[hive 动态分区插入数据][2]
```sql
set hive.exec.dynamic.partition=true;
set hive.exec.dynamic.partition.mode=nonstrict;
insert overwrite table ds_ddf_try_requer_suc_fdt0 partition(pt)
select
          gmt_create      
   ,success_date     
   ,user_id                        
   ,try_request_cnt                
   ,category_level1                
   ,total_try_price
            ,concat(date_format(success_date,'yyyy-MM-dd','yyyyMMdd'),'000000') pt
from
ds_ddf_try_requer_suc_fdt0_bak
where success_date>='2012-10-01'
and success_date <'2012-11-15'
;
```
####[Hive几种数据导入方式][3]

写在前面的话，学Hive这么久了，发现目前国内还没有一本完整的介绍Hive的书籍，而且互联网上面的资料很乱，于是我决定写一些关于《Hive的那些事》序列文章，分享给大家。我会在接下来的时间整理有关Hive的资料，如果对Hive的东西感兴趣，请关注本博客。http://www.iteblog.com/archives/tag/hive的那些事

　　好久没写Hive的那些事了，今天开始写点吧。今天的话题是总结Hive的几种常见的数据导入方式，我总结为四种：
* 从本地文件系统中导入数据到Hive表；
* 从HDFS上导入数据到Hive表；
* 从别的表中查询出相应的数据并导入到Hive表中；
* 在创建表的时候通过从别的表中查询出相应的记录并插入到所创建的表中。

我会对每一种数据的导入进行实际的操作，因为纯粹的文字让人看起来很枯燥，而且学起来也很抽象。好了，开始操作！

#####　一、从本地文件系统中导入数据到Hive表
 
　　先在Hive里面创建好表，如下：
  ```sql
1	hive> create table wyp
2	    > (id int, name string,
3	    > age int, tel string)
4	    > ROW FORMAT DELIMITED
5	    > FIELDS TERMINATED BY '\t'
6	    > STORED AS TEXTFILE;
7	OK
8	Time taken: 2.832 seconds
```
这个表很简单，只有四个字段，具体含义我就不解释了。本地文件系统里面有个/home/wyp/wyp.txt文件，内容如下：
```shell
1	[wyp@master ~]$ cat wyp.txt
2	1       wyp     25      13188888888888
3	2       test    30      13888888888888
4	3       zs      34      899314121
```
wyp.txt文件中的数据列之间是使用\t分割的，可以通过下面的语句将这个文件里面的数据导入到wyp表里面，操作如下：
```shell
1	hive> load data local inpath 'wyp.txt' into table wyp;
2	Copying data from file:/home/wyp/wyp.txt
3	Copying file: file:/home/wyp/wyp.txt
4	Loading data to table default.wyp
5	Table default.wyp stats:
6	[num_partitions: 0, num_files: 1, num_rows: 0, total_size: 67]
7	OK
8	Time taken: 5.967 seconds
```
这样就将wyp.txt里面的内容导入到wyp表里面去了（关于这里面的执行过程大家可以参见本博客的《Hive表与外部表》），可以到wyp表的数据目录下查看，如下命令：
```shell
1	hive> dfs -ls /user/hive/warehouse/wyp ;
2	Found 1 items
3	-rw-r--r--3 wyp supergroup 67 2014-02-19 18:23 /hive/warehouse/wyp/wyp.txt
```
数据的确导入到wyp表里面去了。
　　和我们熟悉的关系型数据库不一样，Hive现在还不支持在insert语句里面直接给出一组记录的文字形式，也就是说，Hive并不支持INSERT INTO …. VALUES形式的语句。

#####　二、HDFS上导入数据到Hive表
　　从本地文件系统中将数据导入到Hive表的过程中，其实是先将数据临时复制到HDFS的一个目录下（典型的情况是复制到上传用户的HDFS home目录下,比如/home/wyp/），然后再将数据从那个临时目录下移动（注意，这里说的是移动，不是复制！）到对应的Hive表的数据目录里面。既然如此，那么Hive肯定支持将数据直接从HDFS上的一个目录移动到相应Hive表的数据目录下，假设有下面这个文件/home/wyp/add.txt，具体的操作如下：
```sql
1	[wyp@master /home/q/hadoop-2.2.0]$ bin/hadoop fs -cat /home/wyp/add.txt
2	5       wyp1    23      131212121212
3	6       wyp2    24      134535353535
4	7       wyp3    25      132453535353
5	8       wyp4    26      154243434355
```
　　上面是需要插入数据的内容，这个文件是存放在HDFS上/home/wyp目录（和一中提到的不同，一中提到的文件是存放在本地文件系统上）里面，我们可以通过下面的命令将这个文件里面的内容导入到Hive表中，具体操作如下：
```sql
01	hive> load data inpath '/home/wyp/add.txt' into table wyp;
02	Loading data to table default.wyp
03	Table default.wyp stats:
04	[num_partitions: 0, num_files: 2, num_rows: 0, total_size: 215]
05	OK
06	Time taken: 0.47 seconds
07	 
08	hive> select * from wyp;
09	OK
10	5       wyp1    23      131212121212
11	6       wyp2    24      134535353535
12	7       wyp3    25      132453535353
13	8       wyp4    26      154243434355
14	1       wyp     25      13188888888888
15	2       test    30      13888888888888
16	3       zs      34      899314121
17	Time taken: 0.096 seconds, Fetched: 7 row(s)
```
　　从上面的执行结果我们可以看到，数据的确导入到wyp表中了！请注意load data inpath ‘/home/wyp/add.txt’ into table wyp;里面是没有local这个单词的，这个是和一中的区别。

#####　　三、从别的表中查询出相应的数据并导入到Hive表中
　　假设Hive中有test表，其建表语句如下所示：
```sql
01	hive> create table test(
02	    > id int, name string
03	    > ,tel string)
04	    > partitioned by
05	    > (age int)
06	    > ROW FORMAT DELIMITED
07	    > FIELDS TERMINATED BY '\t'
08	    > STORED AS TEXTFILE;
09	OK
10	Time taken: 0.261 seconds
```
　　大体和wyp表的建表语句类似，只不过test表里面用age作为了分区字段（关于什么是分区字段，请参见本博客的《Hive的数据存储模式》中的介绍，其详细的介绍本博客将会在接下来的时间内介绍，请关注本博客！）。下面语句就是将wyp表中的查询结果并插入到test表中：
```sql
01	hive> insert into table test
02	    > partition (age='25')
03	    > select id, name, tel
04	    > from wyp;
05	#####################################################################
06	           这里输出了一堆Mapreduce任务信息，这里省略
07	#####################################################################
08	Total MapReduce CPU Time Spent: 1 seconds 310 msec
09	OK
10	Time taken: 19.125 seconds
11	 
12	hive> select * from test;
13	OK
14	5       wyp1    131212121212    25
15	6       wyp2    134535353535    25
16	7       wyp3    132453535353    25
17	8       wyp4    154243434355    25
18	1       wyp     13188888888888  25
19	2       test    13888888888888  25
20	3       zs      899314121       25
21	Time taken: 0.126 seconds, Fetched: 7 row(s)
```
　　通过上面的输出，我们可以看到从wyp表中查询出来的东西已经成功插入到test表中去了！如果目标表（test）中不存在分区字段，可以去掉partition (age=’25′)语句。当然，我们也可以在select语句里面通过使用分区值来动态指明分区：
```sql
01	hive> set hive.exec.dynamic.partition.mode=nonstrict;
02	hive> insert into table test
03	    > partition (age)
04	    > select id, name,
05	    > tel, age
06	    > from wyp;
07	#####################################################################
08	           这里输出了一堆Mapreduce任务信息，这里省略
09	#####################################################################
10	Total MapReduce CPU Time Spent: 1 seconds 510 msec
11	OK
12	Time taken: 17.712 seconds
13	 
14	 
15	hive> select * from test;
16	OK
17	5       wyp1    131212121212    23
18	6       wyp2    134535353535    24
19	7       wyp3    132453535353    25
20	1       wyp     13188888888888  25
21	8       wyp4    154243434355    26
22	2       test    13888888888888  30
23	3       zs      899314121       34
24	Time taken: 0.399 seconds, Fetched: 7 row(s)
```
　　这种方法叫做动态分区插入，但是Hive中默认是关闭的，所以在使用前需要先把hive.exec.dynamic.partition.mode设置为nonstrict。当然，Hive也支持insert overwrite方式来插入数据，从字面我们就可以看出，overwrite是覆盖的意思，是的，执行完这条语句的时候，相应数据目录下的数据将会被覆盖！而insert into则不会，注意两者之间的区别。例子如下：
```sql
1	hive> insert overwrite table test
2	    > PARTITION (age)
3	    > select id, name, tel, age
4	    > from wyp;
```
　　更可喜的是，Hive还支持多表插入，什么意思呢？在Hive中，我们可以把insert语句倒过来，把from放在最前面，它的执行效果和放在后面是一样的，如下：
```sql
01	hive> show create table test3;
02	OK
03	CREATE  TABLE test3(
04	  id int,
05	  name string)
06	Time taken: 0.277 seconds, Fetched: 18 row(s)
07	 
08	hive> from wyp
09	    > insert into table test
10	    > partition(age)
11	    > select id, name, tel, age
12	    > insert into table test3
13	    > select id, name
14	    > where age>25;
15	 
16	hive> select * from test3;
17	OK
18	8       wyp4
19	2       test
20	3       zs
21	Time taken: 4.308 seconds, Fetched: 3 row(s)
```
　　可以在同一个查询中使用多个insert子句，这样的好处是我们只需要扫描一遍源表就可以生成多个不相交的输出。这个很酷吧！

#####四、在创建表的时候通过从别的表中查询出相应的记录并插入到所创建的表中
　　在实际情况中，表的输出结果可能太多，不适于显示在控制台上，这时候，将Hive的查询输出结果直接存在一个新的表中是非常方便的，我们称这种情况为CTAS（create table .. as select）如下：
 ```sql
01	hive> create table test4
02	    > as
03	    > select id, name, tel
04	    > from wyp;
05	 
06	hive> select * from test4;
07	OK
08	5       wyp1    131212121212
09	6       wyp2    134535353535
10	7       wyp3    132453535353
11	8       wyp4    154243434355
12	1       wyp     13188888888888
13	2       test    13888888888888
14	3       zs      899314121
15	Time taken: 0.089 seconds, Fetched: 7 row(s)
```
　　数据就插入到test4表中去了，CTAS操作是原子的，因此如果select查询由于某种原因而失败，新表是不会创建的！
　　好了，很晚了，今天就到这，洗洗睡！


[0]:http://www.cnblogs.com/ggjucheng/archive/2013/01/04/2844393.html
[1]: http://blog.csdn.net/maixia24/article/details/14525659
[2]: http://blog.sina.com.cn/s/blog_768833ed01016ek9.html
[3]: http://www.iteblog.com/archives/949