log 配置文件

[传送][0]

[toc]

##### 基本配置
* 新建Java Project>>新建package>>新建java类；
* import jar包（一个就够），这里我用的是log4j-1.2.14.jar
* 新建log4j.properties，置于project根目录下；
 
 ```shell
    log4j.rootLogger=info,ServerDailyRollingFile,stdout
    log4j.appender.ServerDailyRollingFile=org.apache.log4j.DailyRollingFileAppender
    log4j.appender.ServerDailyRollingFile.DatePattern='.'yyyy-MM-dd
    log4j.appender.ServerDailyRollingFile.File=c://logs/notify-subscription.log
    log4j.appender.ServerDaliyRollingFile.layout=org.apache.log4j.PatternLayout
    log4j.appender.ServerDaliyRollingFile.layout.ConversionPattern=%d-%m%n
    log4j.appender.ServerDailyRollingFile.Append=true

    log4j.appender.stdout=org.apache.log4j.ConsoleAppender
    log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
    log4j.appender.stdout.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %p[%c]%m%n
 ```
 
 一个测试成功的配置
 ```shell
 log4j.rootLogger=INFO,stdout,R
log4j.threshold=ALL
log4j.appender.R=org.apache.log4j.DailyRollingFileAppender
log4j.appender.R.File=${user.dir}/logs/ctr.log
log4j.appender.R.DatePattern='.'yyyy-MM-dd
log4j.appender.R.encoding=UTF-8
log4j.appender.R.layout=org.apache.log4j.PatternLayout
log4j.appender.R.layout.ConversionPattern=[CTR] %d{yy-MM-dd HH\:mm\:ss} %p %l \: %m%n
log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=[CTR] %d{yy-MM-dd HH\:mm\:ss} %p %l  \: %m%n
log4j.logger.com.ireader=INFO
 ```
* 在main()中 ， 加载log4j：

    ```jave
		PropertyConfigurator.configure("log4j.properties");
    ```
* 测试
![][1]

*  exception : 找不到文件
  1. 用绝对路径， 不推荐。
  2. 将log4j文件置于 bin/ 目录下：
      * 代码中 ， propertyConfigurator.configure("bin/log4j.properties");
      * 代码中， propertyConfigurator.configure(ClassLoader.getSystemResource("log4j.properties"))
      * 注意： 必须位于bin的直接目录下， 不可位于bin更深的目录中，原因参考网址：  http://blog.sina.com.cn/s/blog_3f4755c70100jco1.html
     
* 实在不行则可以在程序中直接设置
  ```java
  private static void initLog4j() {
  	Properties prop = new Properties();
    
    prop.setProperty("log4j.rootLogger","DEBUG,CONSOLE");
    prop.setProperty("log4j.appender.CONSOLE","org.apache.log4j.ConsoleAppender");
    prop.setProperty("log4j.appender.CONSOLE.layout","org.apache.log4j.PatternLayout");
    prop.setProperty("log4j.appender.CONSOLE.layout.ConversionPattern","%d{HH:mm:ss,SSS} [%t] %-5p %C{1}:%m%n");
    
    PropertyConfiguratior.configure(prop)
  }
  ```
  
#####log4j 格式讲解
* log4j.rootLogger=日志级别，appender1, appender2, ….
  * 日志级别：ALL<DEBUG<INFO<WARN<ERROR<FATAL<OFF，不区分大小写
  * 注意，需在控制台输入，只需将其中一个appender定义为stdout即可
  * 注意，rootLogger默认是对整个工程生效
  * 注意，如果只想对某些包操作，那么：log4j.logger.com.hutu=info, stdout，表示该日志对package com.hutu生效
  * 注意，这样做可以区分dev/线上，也可以减小性能影响：if(log.isDebugEnabled()){log.debug();}
  
* log4j.appender.appender1=org.apache.log4j.日志输出到哪儿
 * ConsoleAppender（控制台）
 * FileAppender（文件）
 * DailyRollingFileAppender（每天产生一个日志文件）
 * RollingFileAppender（文件大小到达指定尺寸时产生一个新的文件）
 * WriteAppender（将日志信息以流格式发送到任意指定的地方）
 * JDBCAppender（将日志信息保存到数据库中）

* log4j.appender.appender1.File=文件目录及文件 
   ${user.home}/logs/...

* log4j.appender.appender1.MaxFileSize=最大文件大小
* log4j.appender.appender1.MaxBackupIndex=备份文件个数
   * 其中，appender1是在第一行定义过的；
   * 文件目录及文件，例如，/home/admin/logs/hutudan.log
   * 最大文件大小，例如，100KB
   * 备份文件个数，例如，1
* log4j.appender.ServerDailyRollingFile.DatePattern=日志后缀格式
  * 例如，'.'yyyy-MM-dd
* log4j.appender.appender1.layout=org.apache.log4j.日志布局格式
  * HTMLLayout（以HTML表格形式布局）
  * SimpleLayout（包含日志信息的级别和信息字符串）
  * TTCCLayout（包含日志产生的时间，执行绪，类别等信息）
  * PatternLayout（可以灵活的指定布局格式，常用）
* log4j.appender.appender1.layout.ConversionPattern=日志输出格式
 * 例如，%d - %m%n或%d{yyyy-MM-dd HH:mm:ss} %p [%c] %m%n
 * %c 输出日志信息所属的类的全名
 * %d 输出日志时间点的日期或时间，默认格式为ISO8601，也可以在其后指定格式，比如：%d{yyy-M-dd HH:mm:ss }，输出类似：2002-10-18- 22：10：28
 * %f 输出日志信息所属的类的类名
 * %l 输出日志事件的发生位置，即输出日志信息的语句处于它所在的类的第几行
 * %m 输出代码中指定的信息，如log(message)中的message
 * %n 输出一个回车换行符，Windows平台为“rn”，Unix平台为“n”
 * %p 输出优先级，即DEBUG，INFO，WARN，ERROR，FATAL。如果是调用debug()输出的，则为DEBUG，依此类推
 * %r 输出自应用启动到输出该日志信息所耗费的毫秒数
 * %t 输出产生该日志事件的线程名
 * 可参考：http://blog.sina.com.cn/s/blog_4e4dd5570100qowy.html
 
* log4j.appender.ServerDailyRollingFile.Append=true
  例如，不解释，追加往后写便是
  
##### 总结：
 * Logger类：完成日志记录，设置日志信息级别
 * Appender类：决定日志去向，终端、DB、硬盘
 * Layout类：决定日志输出的样式，例如包含当前线程、行号、时间

##### eclipse 工程/文件目录

###### 1. 获取系统根目录
  * System.getProperty("user.home");
  * 输出： C：\Users\hutu

###### 2. 获取工程目录
  * System.getProperty("user.dir");
  * 输出： D:\workspaces\workspace1\myProject

###### 3. 新建文件位于工程目录
  * new File("xxx.txt").getAbsolutePath();
  * 例如输出 ：  D:\workspaces\workspace1\myProject

###### 4. 配置文件路径
  * ApplicationContext context = new ClassPathXmlApplicationContext("Config.xml");
  * 倒是也可以用绝对路径，真心不推荐啊，太不优雅了；
  * 或者，将log4j文件置于bin/目录下：
  * 代码中，PropertyConfigurator.configure("bin/log4j.properties");
  * 代码中，PropertyConfigurator.configure(ClassLoader.getSystemResource("log4j.properties"));
  * 注意，必须位于bin直接目录下，不可位于bin更深层的目录当中。可是这究竟是为神马捏？

###### 5. 查询某类的 .class 文件所在目录
  * Main.class.getResource("");
  * 输出：  D:\workspaces\workspace1\myProject\bin\com\hutu\log4j\
  * 注意，查询包上级路径，只需将参数改作“/”
 
###### 6. 查询thread 上下文所在文件的目录
  * Thread.currentThread().getContextClassLoader().getResource("");
  * 输出： D:\workspaces\workspace1\myProject\bin\
###### 7. 查询某类的classloader所在目录
  * Main.class.getClassLoader().getResource("");
  * 输出： D:\workspaces\workspace1\myProject\bin\

###### 8. 查询 classloader所在目录
 * ClassLoader.getSystemResource("");
 * 例如输出，D:\workspaces\workspace1\myProject\bin\






































[0]:http://www.cnblogs.com/alipayhutu/archive/2012/06/21/2558249.html
[1]:http://images.cnblogs.com/cnblogs_com/alipayhutu/201206/201206212136408383.png