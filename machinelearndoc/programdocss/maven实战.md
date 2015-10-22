
[toc]


#####Maven实战（五）——[自动化Web应用集成测试][5]

自动化集成测试的角色

本专栏的上一篇文章讲述了Maven与持续集成的一些关系及具体实践，我们都知道，自动化测试是持续集成必不可少的一部分，基本上，没有自动化测试的持续集成，都很难称之为真正的持续集成。我们希望持续集成能够尽早的暴露问题，但这远非配置一个 Hudson/Jenkins服务器那么简单，只有真正用心编写了较为完整的测试用例，并一直维护它们，持续集成才能孜孜不倦地运行测试并第一时间报告问题。

自动化测试这个话题很大，本文不想争论测试先行还是后行，这里强调的是测试的自动化，并基于具体的技术（Maven、 JUnit、Jetty等）来介绍一种切实可行的自动化Web应用集成测试方案。当然，自动化测试还包括单元测试、验收测试、性能测试等，在不同的场景下，它们都能为软件开发带来极大的价值。本文仅限于讨论集成测试，主要是因为笔者觉得这是一个非常重要却常常被忽略的实践。
基于Maven的一般流程

集成测试与单元测试最大的区别是它需要尽可能的测试整个功能及相关环境，对于测试Web应用而言，通常有这么几步：

  1.  启动Web容器
  2.  部署待测试Web应用
  3.  以Web客户端的角色运行测试用例
  4.  停止Web容器

启动Web容器可以有很多方式，例如你可以通过Web容器提供的API采用编程的方式来启动容器，但在Maven的环境下，配置插件显得更简单。如果你了解Maven的生命周期模型，就可能会想到，我们可以在pre-integration-test阶段启动容器，部署待测试应用，然后在integration-test阶段运行集成测试用例，最后在post-integrate-test阶段停止容器。也就是说，对于步骤1，2和4我们只须进行一些简单的配置，不必编写额外的代码。第3步是以黑盒的形式模拟客户端进行测试，需要注意的是，这里通常要求你理解一些基本的HTTP协议知识，例如服务端在什么情况下应该返回HTTP代码 200，什么时候应该返回401错误，以及所支持的Content-Type是什么等等。

至于测试用例该怎么写，除了需要用到一些用来访问Web以及解析响应详细的基础设施工具类之外，其他内容与单元测试大同小异，基本就是准备测试数据、访问服务、验证返回值等等。
一个简单的例子

谈了不少理论，现在该给个具体的例子了，譬如现在有个简单的Servlet，它接受参数a和b，做加法后返回二者之和，如果参数不完整，则返回HTTP 400错误，表示客户端的请求有问题。
```java
public class AddServlet
    extends HttpServlet
{
    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
        throws ServletException,
            IOException
    {
        String a = req.getParameter( "a" );
        String b = req.getParameter( "b" );

        if ( a == null || b == null )
        {
            resp.setStatus( 400 );
            return;
        }

        int result = Integer.parseInt( a ) + Integer.parseInt( b );

        resp.setStatus( 200 );
        resp.getWriter().print( result );
    }
}
```
为了测试这段代码，我们需要一个Web容器，这里暂且使用Jetty，因为目前来说它与Maven集成的相对最好。Jetty提供了一个Jetty Maven Plugin，借助该插件，我们可以随时启动Jetty并部署Maven默认目录布局的Web项目，实现快速开发和测试。这里我们需要的是在pre-integration-test阶段启动Jetty，在post-integrate-test阶段停止容器，对应的POM配置如下：
```xml
      <plugin>
        <groupId>org.mortbay.jetty</groupId>
        <artifactId>jetty-maven-plugin</artifactId>
        <version>7.3.0.v20110203</version>
        <configuration>
          <stopPort>9966</stopPort>
          <stopKey>stop-jetty-for-it</stopKey>
        </configuration>
        <executions>
          <execution>
            <id>start-jetty</id>
            <phase>pre-integration-test</phase>
            <goals>
              <goal>run</goal>
            </goals>
            <configuration>
              <daemon>true</daemon>
            </configuration>
          </execution>
          <execution>
            <id>stop-jetty</id>
            <phase>post-integration-test</phase>
            <goals>
              <goal>stop</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
```
XML代码中第一处configuration是插件的全局配置，stopPort和 stopKey是该插件用来停止Jetty需要用到的TCP端口及消息关键字。接着是两个executation元素，第一个executation将 jetty-maven-plugin的run目标绑定至Maven的pre-integration-test生命周期阶段，表示启动容器，第二个 executation将stop目标绑定至post-integration-test生命周期阶段，表示停止容器。需要注意的是，启动Jetty时我们需要配置deamon为true，让Jetty在后台运行以免阻塞mvn命令。此外，jetty-maven-plugin的run目标也会自动部署当前Web项目。

准备好Web容器环境之后，我们接着看一下测试用例代码：
```java
public class AddServletIT
{
    @Test
    public void addWithParametersAndSucceed()
        throws Exception
    {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet( "http://localhost:8080/add?a=1&b=2" );
        HttpResponse response = httpclient.execute( httpGet );

        Assert.assertEquals( 200, response.getStatusLine().getStatusCode() );
        Assert.assertEquals( "3", EntityUtils.toString( response.getEntity() ) );
    }

    @Test
    public void addWithoutParameterAndFail()
        throws Exception
    {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet( "http://localhost:8080/add" );
        HttpResponse response = httpclient.execute( httpGet );

        Assert.assertEquals( 400, response.getStatusLine().getStatusCode() );
    }
}
```
为了能够访问应用，这里用到了HttpClient，两个测试方法都初始化一个HttpClient，然后创建HttpGet对象用来访问Web地址。第一个测试方法顾名思义用来测试成功的场景，它提供参数 a=1和b=2，执行请求后，验证返回结果成功（HTTP状态码200）并且内容为正确的值3。第二个测试方法则用来测试失败的场景，当不提供参数的时候，服务器应该返回一个HTTP 400错误。该测试类其实是相当粗糙的，例如有硬编码的服务器URL，这里的目的仅仅是通过尽可能简单的代码来展现一个自动化集成测试的实现过程。

上述代码中，测试类的名称为AddServletIT，而不是一般的**Test，IT表示IntegrationTest，这么命名是为了和单元测试区分开来，这样，鉴于Maven默认的测试命名约定，Maven在test生命周期阶段执行单元测试时，就不会涉及集成测试。现在，我们希望Maven在integration-test阶段执行所有以IT结尾命名的测试类，配置Maven Surefire Plugin如下：
```xml
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>2.7.2</version>
        <executions>
          <execution>
            <id>run-integration-test</id>
            <phase>integration-test</phase>
            <goals>
              <goal>test</goal>
            </goals>
            <configuration>
              <includes>
                <include>**/*IT.java</include>
              </includes>
            </configuration>
          </execution>
        </executions>
      </plugin>
```
通过命名规则和插件配置，我们优雅地分离了单元测试和集成测试，而且我们知道在integration-test阶段，Jetty容器已经启动完成了。如果你在使用TestNG，那你还可以使用其测试组的特性来分离单元测试和集成测试，Maven Surefire Plugin对其也有着很好的支持。

一切就绪了，运行 mvn clean install 以自动运行集成测试，我们可以看到如下的输出片段：
```shell
[INFO] --- jetty-maven-plugin:7.3.0.v20110203:run (start-jetty) @ webapp-demo ---
[INFO] Configuring Jetty for project: webapp-demo
[INFO] webAppSourceDirectory /home/juven/git_juven/webapp-demo/src/main/webapp does not exist. Defaulting to /home/juven/git_juven/webapp-demo/src/main/webapp
[INFO] Reload Mechanic: automatic
[INFO] Classes = /home/juven/git_juven/webapp-demo/target/classes
[INFO] Context path = /
...
2011-03-06 14:55:15.676:INFO::Started SelectChannelConnector@0.0.0.0:8080
[INFO] Started Jetty Server
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (run-integration-test) @ webapp-demo ---
[INFO] Surefire report directory: /home/juven/git_juven/webapp-demo/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.juvenxu.webapp.demo.AddServletIT
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.344 sec

Results :

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- jetty-maven-plugin:7.3.0.v20110203:stop (stop-jetty) @ webapp-demo ---
```
可以看到jetty-maven-plugin:7.3.0.v20110203:run对应了start-jetty，maven-surefire- plugin:2.7.2:test对应了run-integration-test，jetty-maven- plugin:7.3.0.v20110203:stop对应了stop-jetty，与我们的配置和期望完全一致。此外两个测试也都成功了！
小结

相对于单元测试来说，集成测试更难编写，因为需要准备更多的环境，本文只涉及了Web容器最简单的情形，实际的开发情形中，你可能会遇到数据库，第三方Web服务，更复杂的容器配置和数据格式等等，这都使得编写集成测试变得让人畏惧。然而反过来考虑，无论如何你都需要测试，虽然这个自动化过程的投入很大，但收益往往更加客观，这不仅仅是手动测试时间的节省，更重要的是，你无法保证手动测试能被高频率的反复执行，也就无法保证问题能被尽早暴露。

对于Web应用来说，编写集成测试有助于你考虑和设计Web应用对外暴露的接口，这种“开发实现”/“测试审察”之间的角色转换往往能造就更清晰的设计，这也是编写测试最大的好处之一。

Maven用户能够得益于Maven的插件系统，不仅能节省大量的编码，还能得到稳定的工具，Jetty Maven Plugin和Maven Surefire Plugin就是最好的例子。本文只涉及了Jetty，如果读者的环境是Tomcat或者JBoss等其他容器，则需要查阅相关的文档以得到具体的实现细节，你可能对Tomcat Maven Plugin、JBoss Maven Plugin、或者Cargo Maven2 Plugin感兴趣。

#####mavan实战 九——[打包的技巧][9]
打包“这个词听起来比较土，比较正式的说法应该是”构建项目软件包“，具体说就是将项目中的各种文件，比如源代码、编译生成的字节码、配置文件、文档，按照规范的格式生成归档，最常见的当然就是JAR包和WAR包了，复杂点的例子是Maven官方下载页面的分发包，它有自定义的格式，方便用户直接解压后就在命令行使用。作为一款”打包工具“，Maven自然有义务帮助用户创建各种各样的包，规范的JAR包和WAR包自然不再话下，略微复杂的自定义打包格式也必须支持，本文就介绍一些常用的打包案例以及相关的实现方式，除了前面提到的一些包以外，你还能看到如何生成源码包、Javadoc包、以及从命令行可直接运行的CLI包。
######Packaging的含义

任何一个Maven项目都需要定义POM元素packaging（如果不写则默认值为jar）。顾名思义，该元素决定了项目的打包方式。实际的情形中，如果你不声明该元素，Maven会帮你生成一个JAR包；如果你定义该元素的值为war，那你会得到一个WAR包；如果定义其值为POM（比如是一个父模块），那什么包都不会生成。除此之外，Maven默认还支持一些其他的流行打包格式，例如ejb3和ear。你不需要了解具体的打包细节，你所需要做的就是告诉Maven，”我是个什么类型的项目“，这就是约定优于配置的力量。

为了更好的理解Maven的默认打包方式，我们不妨来看看简单的声明背后发生了什么，对一个jar项目执行mvn package操作，会看到如下的输出：
```shell
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ git-demo ---
[INFO] Building jar: /home/juven/git_juven/git-demo/target/git-demo-1.2-SNAPSHOT.jar
```
相比之下，对一个war项目执行mvn package操作，输出是这样的：
```shell
[INFO] --- maven-war-plugin:2.1:war (default-war) @ webapp-demo ---
[INFO] Packaging webapp
[INFO] Assembling webapp [webapp-demo] in [/home/juven/git_juven/webapp-demo/target/webapp-demo-1.0-SNAPSHOT]
[INFO] Processing war project
[INFO] Copying webapp resources [/home/juven/git_juven/webapp-demo/src/main/webapp]
[INFO] Webapp assembled in [90 msecs]
[INFO] Building war: /home/juven/git_juven/webapp-demo/target/webapp-demo-1.0-SNAPSHOT.war
```
对应于同样的package生命周期阶段，Maven为jar项目调用了maven-jar-plugin，为war项目调用了maven-war-plugin，换言之，packaging直接影响Maven的构建生命周期。了解这一点非常重要，特别是当你需要自定义打包行为的时候，你就必须知道去配置哪个插件。一个常见的例子就是在打包war项目的时候排除某些web资源文件，这时就应该配置maven-war-plugin如下：
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-war-plugin</artifactId>
    <version>2.1.1</version>
    <configuration>
      <webResources>
        <resource>
          <directory>src/main/webapp</directory>
          <excludes>
            <exclude>**/*.jpg</exclude>
          </excludes>
        </resource>
      </webResources>
    </configuration>
  </plugin>
```
######源码包和Javadoc包

本专栏的《坐标规划》一文中曾解释过，一个Maven项目只生成一个主构件，当需要生成其他附属构件的时候，就需要用上classifier。源码包和Javadoc包就是附属构件的极佳例子。它们有着广泛的用途，尤其是源码包，当你使用一个第三方依赖的时候，有时候会希望在IDE中直接进入该依赖的源码查看其实现的细节，如果该依赖将源码包发布到了Maven仓库，那么像Eclipse就能通过m2eclipse插件解析下载源码包并关联到你的项目中，十分方便。由于生成源码包是极其常见的需求，因此Maven官方提供了一个插件来帮助用户完成这个任务：
```xml
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-source-plugin</artifactId>
    <version>2.1.2</version>
    <executions>
      <execution>
        <id>attach-sources</id>
        <phase>verify</phase>
        <goals>
          <goal>jar-no-fork</goal>
        </goals>
      </execution>
    </executions>
  </plugin>
```
类似的，生成Javadoc包只需要配置插件如下：
```xml
  <plugin>          
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <version>2.7</version>
    <executions>
      <execution>
        <id>attach-javadocs</id>
          <goals>
            <goal>jar</goal>
          </goals>
      </execution>
    </executions>
  </plugin>    
```
为了帮助所有Maven用户更方便的使用Maven中央库中海量的资源，中央仓库的维护者强制要求开源项目提交构件的时候同时提供源码包和Javadoc包。这是个很好的实践，读者也可以尝试在自己所处的公司内部实行，以促进不同项目之间的交流。
######可执行CLI包

除了前面提到了常规JAR包、WAR包，源码包和Javadoc包，另一种常被用到的包是在命令行可直接运行的CLI（Command Line）包。默认Maven生成的JAR包只包含了编译生成的.class文件和项目资源文件，而要得到一个可以直接在命令行通过java命令运行的JAR文件，还要满足两个条件：
```xml
    JAR包中的/META-INF/MANIFEST.MF元数据文件必须包含Main-Class信息。
    项目所有的依赖都必须在Classpath中。
```
Maven有好几个插件能帮助用户完成上述任务，不过用起来最方便的还是maven-shade-plugin，它可以让用户配置Main-Class的值，然后在打包的时候将值填入/META-INF/MANIFEST.MF文件。关于项目的依赖，它很聪明地将依赖JAR文件全部解压后，再将得到的.class文件连同当前项目的.class文件一起合并到最终的CLI包中，这样，在执行CLI JAR文件的时候，所有需要的类就都在Classpath中了。下面是一个配置样例：
```xml
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>1.4</version>
    <executions>
      <execution>
        <phase>package</phase>
        <goals>
          <goal>shade</goal>
        </goals>
        <configuration>
          <transformers>
            <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
              <mainClass>com.juvenxu.mavenbook.HelloWorldCli</mainClass>
            </transformer>
          </transformers>
        </configuration>
      </execution>
    </executions>
  </plugin>
```
上述例子中的，我的Main-Class是com.juvenxu.mavenbook.HelloWorldCli，构建完成后，对应于一个常规的hello-world-1.0.jar文件，我还得到了一个hello-world-1.0-cli.jar文件。细心的读者可能已经注意到了，这里用的是cli这个classifier。最后，我可以通过java -jar hello-world-1.0-cli.jar命令运行程序。
自定义格式包

实际的软件项目常常会有更复杂的打包需求，例如我们可能需要为客户提供一份产品的分发包，这个包不仅仅包含项目的字节码文件，还得包含依赖以及相关脚本文件以方便客户解压后就能运行，此外分发包还得包含一些必要的文档。这时项目的源码目录结构大致是这样的：
```shell
pom.xml
src/main/java/
src/main/resources/
src/test/java/
src/test/resources/
src/main/scripts/
src/main/assembly/
README.txt
```
除了基本的pom.xml和一般Maven目录之外，这里还有一个src/main/scripts/目录，该目录会包含一些脚本文件如run.sh和run.bat，src/main/assembly/会包含一个assembly.xml，这是打包的描述文件，稍后介绍，最后的README.txt是份简单的文档。

我们希望最终生成一个zip格式的分发包，它包含如下的一个结构：
```shell
bin/
lib/
README.txt
```
其中bin/目录包含了可执行脚本run.sh和run.bat，lib/目录包含了项目JAR包和所有依赖JAR，README.txt就是前面提到的文档。

描述清楚需求后，我们就要搬出Maven最强大的打包插件：maven-assembly-plugin。它支持各种打包文件格式，包括zip、tar.gz、tar.bz2等等，通过一个打包描述文件（该例中是src/main/assembly.xml），它能够帮助用户选择具体打包哪些文件集合、依赖、模块、和甚至本地仓库文件，每个项的具体打包路径用户也能自由控制。如下就是对应上述需求的打包描述文件src/main/assembly.xml：
```xml
<assembly>
  <id>bin</id>
  <formats>
    <format>zip</format>
  </formats>
  <dependencySets>
    <dependencySet>
      <useProjectArtifact>true</useProjectArtifact>
      <outputDirectory>lib</outputDirectory>
    </dependencySet>
  </dependencySets>
  <fileSets>
    <fileSet>
      <outputDirectory>/</outputDirectory>
      <includes>
        <include>README.txt</include>
      </includes>
    </fileSet>
    <fileSet>
      <directory>src/main/scripts</directory>
      <outputDirectory>/bin</outputDirectory>
      <includes>
        <include>run.sh</include>
        <include>run.bat</include>
      </includes>
    </fileSet>
  </fileSets>
</assembly>
```
   首先这个assembly.xml文件的id对应了其最终生成文件的classifier。
   其次formats定义打包生成的文件格式，这里是zip。因此结合id我们会得到一个名为hello-world-1.0-bin.zip的文件。（假设artifactId为hello-world，version为1.0）
   dependencySets用来定义选择依赖并定义最终打包到什么目录，这里我们声明的一个depenencySet默认包含所有所有依赖，而useProjectArtifact表示将项目本身生成的构件也包含在内，最终打包至输出包内的lib路径下（由outputDirectory指定）。
   fileSets允许用户通过文件或目录的粒度来控制打包。这里的第一个fileSet打包README.txt文件至包的根目录下，第二个fileSet则将src/main/scripts下的run.sh和run.bat文件打包至输出包的bin目录下。

打包描述文件所支持的配置远超出本文所能覆盖的范围，为了避免读者被过多细节扰乱思维，这里不再展开，读者若有需要可以去参考这份文档。

最后，我们需要配置maven-assembly-plugin使用打包描述文件，并绑定生命周期阶段使其自动执行打包操作：
```xml
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-assembly-plugin</artifactId>
    <version>2.2.1</version>
    <configuration>
      <descriptors>
        <descriptor>src/main/assembly/assembly.xml</descriptor>
      </descriptors>
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
```
运行mvn clean package之后，我们就能在target/目录下得到名为hello-world-1.0-bin.zip的分发包了。
小结
打包是项目构建最重要的组成部分之一，本文介绍了主流Maven打包技巧，包括默认打包方式的原理、如何制作源码包和Javadoc包、如何制作命令行可运行的CLI包、以及进一步的，如何基于个性化需求自定义打包格式。这其中涉及了很多的Maven插件，当然最重要，也是最为复杂和强大的打包插件就是maven-assembly-plugin。事实上Maven本身的分发包就是通过maven-assembly-plugin制作的，感兴趣的读者可以直接查看源码一窥究竟。

[0]:http://www.infoq.com/cn/author/%E8%AE%B8%E6%99%93%E6%96%8C
[5]:http://www.infoq.com/cn/news/2011/03/xxb-maven-5-integration-test
[9]:http://www.infoq.com/cn/news/2011/06/xxb-maven-9-package/