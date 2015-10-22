maven-spring整合

[toc]
##### Spring3中用注解直接注入properties中的值 
[传送](http://sunjun041640.blog.163.com/blog/static/256268322013112325324373/)
在bean中（可能是controller，service，dao etc） 中，使用@Value注解：
```java
@service

public class TestService{

	@Value("${system.username}")
    String loginUserName;
}
```
在spring中定义源文件：
```xml
<context:property-placeholder>
```
或者
org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
(也可以使用自定义扩展上面类的子类)
```xml
<bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
	<property name="locations">
    	<array>
        	<value>classpath:config.properties<value>
        <array>
    </property>
</bean>
```
config.properties文件中的内容：
```shell
system.username=admin
```
这样config.properties中的system.username就注入到loginName属性中了

#####maven管理多模块项目
######定义
首先明确多模块项目的含义，它是指一个应用中包含多个module，一般的，一个应用单独部署成服务，只是在打包的时候，maven会将各个module组合在一起。各个模块一般单独打包成jar放在lib目录中，当然web应用回答报称war
######与多应用的区别
这里说的多模块项目与那种单独自立门户的多个application区分开来，多个application也可能有包级的关联，但是他们各自分开了，不属于多模块的范畴。

######文件结构
[传送](http://xiemingmei.iteye.com/blog/1070529)
maven对多模块的管理是这样的，它存在一个parent模块，但是没有程序代码，只包含一个pom.xml，该pom是用来给子模块来引用的。
目录结构如下：
```txt
simple-parent
+-simple-weather
	+-src
    +-target
    \-pom.xml
+-simple-webapp
	+-src
    +-target
    \-pom.xml
\pom.xml
```
在这个目录结构中，一个父模块包含了两个子模块。
各个pom.xml的内容大致如下
parent/pom.xml
```xml
<modules>
	<groupId>org.sonatype.mavenbook.multi</groupId>
    <artifactId>parent</artifactId>
    <version>0.8-SNAPSHOT</version>
    <packing>pom</packing>
    <module>simple-weatcher</module>
    <module>simple-webapp</module>
</modules>

<dependencies>
	<dependency>
    	<groupId>velocity</groupId>
        <artifactId>velocity</artfactId>
        <version>1.5</version>
    </dependency>
</dependencies>
```
simple-weatcher/pom.xml:
```xml
<parent>
	<groupId>org.sonatype.mavenbook.multi</groupId>
    <artifactId>simple-weather</artifactId>
    <version>0.8-SNAPSHOT</version>
</parent>

<dependencies>
	<dependency>
    	<groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>3.8.1</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```
simple-webapp/pom.xml
```xml
<parent>
	<groupId>org.sonatype.mavenbook.multi</groupId>
    <artifactId>simple-parent</artifactId>
    <version>0.8-SNAPSHOT</version>
</parent>
<dependencies>
	<denpendecy>
    	<groupId>org.apache.geronimo.specs</groupId>   
 		<artifactId>geronimo-servlet_2.4_spec</artifactId>   
 		<version>1.1.1</version>  
    </dependency>
    
	<!--配置模块间的依赖-->    
    <denpendecy>
    	<groupId>org.sonatype.mavenbook.multi</groupId>   
 		<artifactId>simple-weather</artifactId>   
 		<version>0.8-SNAPSHOT</version>  
    </dependency>
    
</dependencies>
```

如果按照父pom.xml打包，会输出simple-weather**.jar, simple-webapp**.war两个包；
如果按simple-weather/pom.xml打包，则只会输出 simple-weather**.jar；
如果按simple-webapp/pom.xml打包，则只会输出 simple-webapp**.war。

另外，子模块会继承父模块的包依赖，使用mvn dependency:tree可以查看各个模块的包依赖列表，simple-weather,simple-webapp项目都有引用到 velocity包。

虽然这是一个application下包含了多个module的结构，但是在eclipse中，还是得对每个子module单独建project来管理源码。具体可以分别在simple-weather、simple-webapp目录下使用mvn eclipse:eclipse来创建eclipse project，创建完毕后，你就可以在文件.classpath中看到，包依赖关系已经按照pom.xml中的配置自动生成了。

#####现实中的maven项目
[传送](http://juvenshun.iteye.com/blog/242651)

一个现实生活中的Maven项目只有一个POM文件，那是不现实的。典型的项目会和数据库交互，和Web Service交互，有自己的业务逻辑，暴露Web Service，有命令行应用程序，有Web应用程序……这个时候，必须分一下模块，松散耦合，清晰关联，增加重用等等好处不用多说。这两章内容，就是介绍怎样使用Maven的模块集成及聚合相关功能的。
######模块关系
看一看多模块企业级项目的模块关系图：
![](http://juvenshun.iteye.com/upload/picture/pic/21841/7d4ef8b3-5a2f-30de-8663-4cbf4879bb43.png)
这里有一个父模块，一个web应用，一个命令行应用，一个持久化模块，一个业务逻辑模块，一个模型对象模块。基本上，这个图能很典型的代表很多项目的结构了。该图中，展现了模块之间的各种关系，有依赖（dependency），传递性依赖（transitive dependency），子模块（module of），继承（inherits from）。

######配置文件解释
* 配置子模块的xml片段
```xml
    <modules>  
      <module>simple-command</module>  
      <module>simple-model</module>  
      <module>simple-weather</module>  
      <module>simple-persist</module>  
      <module>simple-webapp</module>  
    </modules>  
```
* 配置继承的xml片段
```xml
    <parent>  
      <groupId>org.sonatype.mavenbook.ch07</groupId>  
      <artifactId>simple-parent</artifactId>  
      <version>1.0</version>  
    </parent>  
    <artifactId>simple-model</artifactId>  
    <packaging>jar</packaging>  
```
* 配置依赖的XML片段
```xml
    <dependency>  
      <groupId>org.sonatype.mavenbook.ch07</groupId>  
      <artifactId>simple-model</artifactId>  
      <version>1.0</version>  
    </dependency>  
```
######传递依赖性解释
关于传递性依赖，这里做个简单的解释，项目A依赖于项目B，而项目B依赖于项目C，那么项目A就自动会拥有对于项目C的传递性依赖。 如上图中，simple-webapp模块直接依赖于simple-weather模块，而simple-weather模块直接依赖于simple-model模块，那么simple-webapp就拥有对于simple-model的传递性依赖。

#####使用assemble 将maven项目pom.xml中的jar打包
[传送](http://www.cnblogs.com/enshrineZither/p/3402616.html)
######方法一： 将pom.xml引入的jar包打包到zip文件夹中
1. pom.xml文件的配置
```xml
<build>
		<plugins>
			<plugin>
				<!-- NOTE: We don't need a groupId specification because the group is 
					org.apache.maven.plugins ...which is assumed by default. -->
				<artifactId>maven-assembly-plugin</artifactId>
				<version>2.4</version>
				<configuration>
					<descriptors>
						<descriptor>src/main/assembly/src.xml</descriptor>
					</descriptors>
					<descriptorRefs>
						<descriptorRef>jar-with-dependencies</descriptorRef>
					</descriptorRefs>
				</configuration>
				<executions>
					<execution>
						<id>make-assembly</id> <!-- this is used for inheritance merges -->
						<phase>package</phase> <!-- bind to the packaging phase -->
						<goals>
							<goal>single</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>
```
2. src.xml文件内容
```xml
<assembly xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/assembly-1.0.0.xsd">
    <id>package</id>
    <formats>
        <format>zip</format>
    </formats>
    <includeBaseDirectory>true</includeBaseDirectory>
    <fileSets>
        <fileSet>
            <directory>src/main/bin</directory>
            <outputDirectory>/</outputDirectory>
        </fileSet>
        <fileSet>
            <directory>src/main/config</directory>
            <outputDirectory>config</outputDirectory>
        </fileSet>
    </fileSets>
    <dependencySets>
        <dependencySet>
            <outputDirectory>lib</outputDirectory>
            <scope>runtime</scope>
        </dependencySet>
    </dependencySets>
</assembly>
```
该方法可以将所有引入的包打成jar包，方便自己保存和引入
3. 最后一步
cmd进入在项目根目录下，键入"mvn package",ok

######方法二、将pom引入的jar包打进你的项目jar包内
1. pom.xml配置
```xml
<!-- 打包配置 start -->
	<build>
		<!-- <finalName>im-dal-service</finalName>
		<sourceDirectory>src/main/java</sourceDirectory>
		<resources>
			控制资源文件的拷贝
			<resource>
				<directory>src/main/resources</directory>
				<targetPath>${project.build.directory}</targetPath>
			</resource>
		</resources> -->
		<plugins>
			<plugin>
				<artifactId>maven-assembly-plugin</artifactId>
				<configuration>
					<descriptors>
						<descriptor>src/main/assembly/src.xml</descriptor>
					</descriptors>
				</configuration>
			</plugin>
		</plugins>

	</build>
```
2. src.xml内容
```xml
<?xml version="1.0" encoding="UTF-8"?>
<assembly xmlns="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.0 http://maven.apache.org/xsd/assembly-1.1.0.xsd">
  <id>jar-with-dependencies</id>
  <formats>
    <format>jar</format>
  </formats>
  <includeBaseDirectory>false</includeBaseDirectory>
  <dependencySets>
    <dependencySet>
      <unpack>false</unpack>
      <scope>runtime</scope>
    </dependencySet>
  </dependencySets>
  <fileSets>
    <fileSet>
      <directory>/lib</directory>
    </fileSet>
  </fileSets>
</assembly>
```
3. 最后一步
cmd进入在项目根目录下，键入 "mvn assemably:assenbly",ok


#####Maven Assembly插件功能解释
[传送](http://blueram.iteye.com/blog/1684070)

######功能简述
你是否想要创建一个包含脚本、配置文件以及所有运行时所依赖的元素（jar）Assembly插件能帮你构建一个完整的发布包。

Assembly插件会生成 “assemblies”， 此特性等同于的Maven 1 distribution plug-in.。该插件不仅支持创建二进制归档文件，也支持创建源码归档文件。这些assemblies定义在一个assembly描述符文件里。你可以选择自定义assembly描述符或者直接使用插件自带的三个预定义描述符中的任何一个.

目前Assembly插件支持如下格式的归档文件:

  *  zip
  *  tar.gz
  *  tar.bz2
  *  jar
  *  dir
  *  war
  *  and any other format that the ArchiveManager has been configured for

Maven 2上使用assembly的简单步骤:

   * 从预定义描述符里选择一个或者自己编写一个assembly描述符号。
   * 工程的pom.xml里配置Assembly插件。
   * 在工程根目录下运行”mvn assembly:assembly”命令 。

如何自定义assembly描述符，详见Assembly Descriptor Format.

#####什么是Assembly
“assembly”是把一组文件、目录、依赖元素组装成一个归档文件. 比如, 假设一个 Maven project定义了一个JAR artifact，它包含控制台应用程序和Swing应用程序 。这样一个工程可以定义两套包含描述符，一套给给控制台应用，另一套给Swing应用程序，它们包含各自的脚本、目录和依赖。

Assembly Plugin的描述符可以定义任何一个文件或者目录归档方式。举个例子，如果的你的Maven 2工程包含”src/main/bin”这个目录，你可以指示Assembly插件复制“src/main/bin”目录下所有的文件到bin目录里（归档文件里的目录），并且可以修改它们的权限属性（UNIX mode）。

######The Maven Assembly Plugin
Maven 2.0的Assembly插件目的是提供一个把工程依赖元素、模块、网站文档等其他文件存放到单个归档文件里。
使用任何一个预定义的描述符你可以轻松的构建一个发布包。这些描述符能处理一些常用的操作,如：把依赖的元素的归档到一个jar文件. 当然, 你可以自定义描述符来更灵活的控制依赖，模块，文件的归档方式。
maven-assembly-plugin : 是maven中针对打包任务而提供的标准插件
1. 在pom.xml 文件里面的配置说明
```xml
    <plugin>  
        <artifactId>maven-assembly-plugin</artifactId>  
        <executions>  <!--执行器 mvn assembly:assembly-->  
            <execution>  
                <id>make-zip</id><!--名字任意 -->    
            <phase>package</phase><!-- 绑定到package生命周期阶段上 -->    
            <goals>    
               <goal>single</goal><!-- 只运行一次 -->    
            </goals>    
                <configuration>  
                         <descriptors> <!--描述文件路径-->  
                              <descriptor>src/main/resources/zip.xml</descriptor>  
                        </descriptors>  
                </configuration>  
            </execution>  
        </executions>  
     </plugin>  
```
2. zip.xml配置文件
```xml
    <assembly  
        xmlns="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.0"  
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  
        xsi:schemaLocation="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.0 http://maven.apache.org/xsd/assembly-1.1.0.xsd">  
        <id>release</id>  
        <formats>  
            <format>zip</format>  
        </formats>  
        <fileSets>  
            <fileSet>  
                <directory>${project.basedir}\src\main\config</directory>  
                <!-- 过滤 -->  
                <excludes>  
                    <exclude>*.xml</exclude>  
                </excludes>  
                <outputDirectory>\</outputDirectory>  
            </fileSet>  
        </fileSets>  
          
        <dependencySets>  
            <dependencySet>  
                <useProjectArtifact>true</useProjectArtifact>  
                <outputDirectory>lib</outputDirectory><!-- 将scope为runtime的依赖包打包到lib目录下。 -->  
                <scope>runtime</scope>  
            </dependencySet>  
        </dependencySets>  
    </assembly>  
```
3. zip.xml格式属性说明
1. 打包的文件格式
可以有：tar.zip war zip
<formats>
 <format>zip</format>
</formats>
2. 需要打包的路径
<directory>${project.basedir}</directory>
3. 打包后输出的路径
<outputDirectory>/</outputDirectory>
4. 打包需要包含的文件
 <excludes>
        <exclude>junit:junit</exclude>
        <exclude>commons-lang:commons-lang</exclude>
        <exclude>commons-logging:commons-logging</exclude>
</excludes>
5. 当前项目构件是否包含在这个依赖集合里。
<useProjectArtifact>true</useProjectArtifact>
6. 依赖包打包到目录下
<dependencySets>
  <dependencySet>
   <outputDirectory>lib</outputDirectory><!-- 将scope为runtime的依赖包打包到lib目录下。 -->
   <useProjectArtifact>true</useProjectArtifact>
   <scope>runtime</scope>
  </dependencySet>
</dependencySets>

#####一些较系统的教程

Maven搭建struts2+spring+hibernate环境 ： http://blog.csdn.net/Sgl731524380/article/category/1393847


Maven4MyEclipse 搭建ssh2+extjs项目： http://blog.csdn.net/zsstudio/article/category/1390718

利用Maven继承关系简化项目POM配置： http://my.oschina.net/noahxiao/blog/61305

利用maven组装项目： http://bill-xing.iteye.com/blog/1152450

maven 与 ivy 教程： http://www.blogjava.net/aoxj/category/37028.html?Show=All

利用felix和struts2实现osgi web: http://bill-xing.iteye.com/blog/793412#bc2343467

jbpm4.1 和 spring整合 : http://bill-xing.iteye.com/category/79176



#####一个正式工程的样例（将工程打成可执行jar ，并装配放置到指定目录下）
[传送](http://bill-xing.iteye.com/blog/1152450)
一个java工程中，一定会用的很多第三方的jar包。如何快速发布自己的工程和项目是一个需要掌握的技巧。maven是现在比较流行的项目管理工具。通过它的maven-jar-plugin和maven-assembly-plugin可以实现一个工程的快速自动发布。

我要进行发布的目标工程是一个osgi的插件工程，具有如下的特点：

 1. 通过org.tkxing.main.OptimizatorMain启动apache felix osgi框架和调用在felix osgi中发布的服务。
 2. osgi的系统和业务bundle放在bundles目录中。
 3. felix框架的配置文件放在conf目录中。
 4. 利用log4j作为系统的日志管理系统，配置文件log4j.xml放在项目根目录下。

希望通过maven完成的系统目录如下：
![](http://dl.iteye.com/upload/attachment/538888/1cd6a179-20b2-3a74-b140-8de593d80edd.png)
在完成的过程中，利用maven-jar-plugin和maven-assembly-plugin分成两步完成工程的发布。
######1. 生成可执行jar包
利用maven-jar-plugin生成这个可执行的jar包，需要完成两项的设置，一是jar包的main class，二是jar包利用其他jar的classpath，在pom文件中设置如下：
```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <version>2.3.1</version>
    <configuration>
    	<archive>
        	<manifest>
            	<addClassPath>true</addClassPath>
                <classpathPrefix>lib</classpathPrefix>
                <mainClass>org.tkxing.main.OptimizatorMain</mainClass>
            </manifest>
        </archive>
        
        <executions>
        	<execution>
            	<phase>package</phase>
            </execution>
        <executions>
    </configuration>
</plugin>
```
其中的manifest标签中的内容会将添加在生成的jar包的manifest.mf文件中

######2. 装配整个工程
利用maven的maven-assembly-plugin来完成整个项目的装配工作，在pom文件中，设置装配的具体要求，利用assembly.xml文件定义：
```xml
<assembly  
    xmlns="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.0"  
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  
    xsi:schemaLocation="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.0 http://maven.apache.org/xsd/assembly-1.1.0.xsd">  
    <id>jar-with-dependencies</id>
    <formats>
    	<format>dir</format>
    </formats>
    <includeBaseDirectory>false</includeBseDirectory>
    
    <dependencySets>
    	<!--将本工程编译成的可执行jar包装配到指定的目录内-->
    	<dependencySet>
        	<useProjectArtifact>true</useProjectArtifact>
            <outputDirectory>/</outputDirectory>
            <includes>
            	<include>org.tkxing.stock:org.tkxing.stock.test</include>
            </includes>
        </dependencySet>
        
        <!--将依赖的第三发包装配到指定的目录xia-->
        <dependencySet>
        	<useProjectArtifact>false</useProjectArtifact>
            <outputDirectory>lib/</outputDirectory>
            
            <excludes>
            	<exclude>org.springframework:spring-beans</exclude>  
                <exclude>org.springframework:spring-asm</exclude>  
                <exclude>org.springframework:spring-core</exclude>  
                <exclude>org.springframework:spring-aop</exclude>  
                <exclude>org.springframework:spring-context</exclude>  
                <exclude>org.springframework:spring-expression</exclude>  
                <exclude>org.springframework:spring-jms</exclude>  
                <exclude>org.springframework:spring-tx</exclude> 
            </excludes>
        </dependencySet>
        
        <fileSets>
        	<fileSet>
            	<directory>conf</directory>
                <outputDirectory>conf</outputDirectory>
            </fileSet>
            
            <fileSet>
            	<directory>bundles</directory>
                <outputDirectory>bundles</outputDirectory>
            </fileSet>
        </fileSets>
        
        <files>
        	<file>
            	<source>log4j.xml</source>
                <outputDirectory>/</outputDirectory>
            </file>
        </files>
    </dependencySets>
```

 其中，formats部分，定义装配的结果是一个目录。

   dependencySets部分中定义了两个dependencySet，第一个dependencySet的目的是把可运行的jar放在根目录下，利用的include选项；第二个dependencySet的目的是把所有依赖的jar包放在lib目录下，并利用excludes排除重复的jar包。
   fileSets部分定义了把bundles和conf目录进行打包
   files部分定义了打包log4j.xml文件。
通过以上两步，就完成了一个工程的打包。

完整的pom.xml文件如下：
```xml
    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">  
        <modelVersion>4.0.0</modelVersion>  
      
        <parent>  
            <groupId>org.tkxing.stock</groupId>  
            <artifactId>org.tkxing.stock</artifactId>  
            <version>1.0</version>  
        </parent>  
      
      
        <artifactId>org.tkxing.stock.test</artifactId>  
        <packaging>jar</packaging>  
      
        <dependencies>  
            <dependency>  
                <groupId>com.googlecode</groupId>  
                <artifactId>transloader</artifactId>  
                <version>0.4</version>  
            </dependency>  
      
      
            <dependency>  
                <groupId>org.tkxing.stock</groupId>  
                <artifactId>org.tkxing.stock.dao.impl</artifactId>  
                <version>1.0</version>  
            </dependency>  
            <dependency>  
                <groupId>org.tkxing.stock</groupId>  
                <artifactId>org.tkxing.stock.ruleengine.impl</artifactId>  
                <version>1.0</version>  
            </dependency>  
      
            <dependency>  
                <groupId>org.tkxing.stock</groupId>  
                <artifactId>org.tkxing.stock.sourcedata.getter</artifactId>  
                <version>1.0</version>  
            </dependency>  
            <dependency>  
                <groupId>org.tkxing.stock</groupId>  
                <artifactId>org.tkxing.stock.sourcedata.getter.impl</artifactId>  
                <version>1.0</version>  
            </dependency>  
      
            <dependency>  
                <groupId>org.tkxing.stock</groupId>  
                <artifactId>org.tkxing.stock.sourcedata.disassemble</artifactId>  
                <version>1.0</version>  
            </dependency>  
            <dependency>  
                <groupId>org.tkxing.stock</groupId>  
                <artifactId>org.tkxing.stock.sourcedata.disassemble.impl</artifactId>  
                <version>1.0</version>  
            </dependency>  
      
            <dependency>  
                <groupId>org.jboss.javassist</groupId>  
                <artifactId>com.springsource.javassist</artifactId>  
                <version>3.9.0.GA</version>  
            </dependency>  
      
            <dependency>  
                <groupId>org.hibernate</groupId>  
                <artifactId>com.springsource.org.hibernate</artifactId>  
                <version>3.3.2.GA</version>  
            </dependency>  
      
            <dependency>  
                <groupId>org.antlr</groupId>  
                <artifactId>com.springsource.antlr</artifactId>  
                <version>2.7.7</version>  
            </dependency>  
            <dependency>  
                <groupId>org.apache.commons</groupId>  
                <artifactId>com.springsource.org.apache.commons.collections  
                </artifactId>  
                <version>3.2.0</version>  
            </dependency>  
            <dependency>  
                <groupId>org.dom4j</groupId>  
                <artifactId>com.springsource.org.dom4j</artifactId>  
                <version>1.6.1</version>  
            </dependency>  
            <dependency>  
                <groupId>javax.xml.stream</groupId>  
                <artifactId>com.springsource.javax.xml.stream</artifactId>  
                <version>1.0.1</version>  
            </dependency>  
            <dependency>  
                <groupId>org.objectweb.asm</groupId>  
                <artifactId>com.springsource.org.objectweb.asm</artifactId>  
                <version>1.5.3</version>  
            </dependency>  
            <dependency>  
                <groupId>org.slf4j</groupId>  
                <artifactId>com.springsource.slf4j.nop</artifactId>  
                <version>1.6.1</version>  
            </dependency>  
      
            <dependency>  
                <groupId>javax.transaction</groupId>  
                <artifactId>com.springsource.javax.transaction</artifactId>  
                <version>1.1.0</version>  
            </dependency>  
      
            <dependency>  
                <groupId>org.apache.commons</groupId>  
                <artifactId>com.springsource.org.apache.commons.dbcp</artifactId>  
                <version>1.2.2.osgi</version>  
            </dependency>  
            <dependency>  
                <groupId>org.springframework</groupId>  
                <artifactId>org.springframework.aspects</artifactId>  
                <version>3.0.4.RELEASE</version>  
            </dependency>  
            <dependency>  
                <groupId>org.springframework</groupId>  
                <artifactId>org.springframework.context.support</artifactId>  
                <version>3.0.4.RELEASE</version>  
            </dependency>  
      
      
            <!--  
                <dependency> <groupId>org.aspectj</groupId>  
                <artifactId>com.springsource.org.aspectj.weaver</artifactId>  
                <version>1.6.8.RELEASE</version> </dependency> <dependency>  
                <groupId>org.aspectj</groupId>  
                <artifactId>com.springsource.org.aspectj.runtime</artifactId>  
                <version>1.6.8.RELEASE</version> </dependency>  
            -->  
      
            <dependency>  
                <groupId>com.sun.xml</groupId>  
                <artifactId>com.springsource.com.sun.tools.xjc</artifactId>  
                <version>2.1.7</version>  
            </dependency>  
      
            <dependency>  
                <groupId>javax.activation</groupId>  
                <artifactId>com.springsource.javax.activation</artifactId>  
                <version>1.1.1</version>  
            </dependency>  
            <dependency>  
                <groupId>org.antlr</groupId>  
                <artifactId>com.springsource.org.antlr.runtime</artifactId>  
                <version>3.1.3</version>  
            </dependency>  
            <dependency>  
                <groupId>com.thoughtworks.xstream</groupId>  
                <artifactId>com.springsource.com.thoughtworks.xstream</artifactId>  
                <version>1.3.1</version>  
            </dependency>  
      
            <dependency>  
                <groupId>org.mvel</groupId>  
                <artifactId>mvel2</artifactId>  
                <version>2.0.19</version>  
            </dependency>  
            <dependency>  
                <groupId>net.sourceforge.jexcelapi</groupId>  
                <artifactId>com.springsource.jxl</artifactId>  
                <version>2.6.6</version>  
            </dependency>  
            <dependency>  
                <groupId>org.tkxing.stock</groupId>  
                <artifactId>org.tkxing.stock.sourcedata.intergration</artifactId>  
                <version>1.0</version>  
                <type>jar</type>  
                <scope>compile</scope>  
            </dependency>  
            <dependency>  
                <groupId>org.tkxing.stock</groupId>  
                <artifactId>org.tkxing.stock.predict</artifactId>  
                <version>1.0</version>  
                <scope>compile</scope>  
            </dependency>  
            <dependency>  
                <groupId>org.tkxing.stock</groupId>  
                <artifactId>org.tkxing.stock.predict.impl</artifactId>  
                <version>1.0</version>  
                <scope>compile</scope>  
            </dependency>  
      
            <dependency>  
                <groupId>org.tkxing.activemq</groupId>  
                <artifactId>activemqengine</artifactId>  
                <version>1.0</version>  
            </dependency>  
      
            <dependency>  
                <groupId>org.apache.activemq</groupId>  
                <artifactId>activemq-core</artifactId>  
                <version>5.5.0</version>  
                <exclusions>  
                    <exclusion>  
                        <groupId>org.osgi</groupId>  
                        <artifactId>org.osgi.core</artifactId>  
                    </exclusion>  
                </exclusions>  
            </dependency>  
      
            <dependency>  
                <groupId>org.objectweb.howl</groupId>  
                <artifactId>com.springsource.org.objectweb.howl</artifactId>  
                <version>1.0.2</version>  
            </dependency>  
      
      
        </dependencies>  
      
        <build>  
      
            <plugins>  
          
                <plugin>  
                    <artifactId>maven-compiler-plugin</artifactId>  
                    <configuration>  
                        <compilerVersion>1.5</compilerVersion>  
                        <fork>true</fork>  
                        <source>1.5</source>  
                        <target>1.5</target>  
                    </configuration>  
                </plugin>  
        
                <plugin>  
                    <groupId>org.apache.maven.plugins</groupId>  
                    <artifactId>maven-jar-plugin</artifactId>  
                    <version>2.3.1</version>  
                    <configuration>  
                         <archive>    
                              <manifest>    
                                   <addClasspath>true</addClasspath>  
                                   <classpathPrefix>lib/</classpathPrefix>  
                                   <mainClass>org.tkxing.main.OptimizatorMain</mainClass>    
                              </manifest>    
                         </archive>    
                    </configuration>  
                    <executions>  
                        <execution>  
                            <phase>package</phase>  
                        </execution>  
                    </executions>  
                </plugin>  
      
              <plugin>    
                  <artifactId>maven-assembly-plugin</artifactId>    
                  <configuration>    
                        <descriptors>    
                            <descriptor>assembly.xml</descriptor>    
                       </descriptors>    
                  </configuration>    
                    <executions>  
                        <execution>  
                            <phase>install</phase>  
                            <goals>  
                                <goal>single</goal>  
                            </goals>  
                            <configuration>  
                                <outputDirectory>d:/temp/stock</outputDirectory>  
                            </configuration>  
                        </execution>  
                    </executions>  
                    
             </plugin>    
         
            </plugins>  
      
        </build>  
      
      
    </project>  
```


 












