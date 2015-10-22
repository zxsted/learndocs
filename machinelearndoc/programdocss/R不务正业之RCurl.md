####[R不务正业之RCurl][0]

首先感谢COS论坛同意我在这篇水文中用RCurl做一些简单的演示，但由这些功能的延展而给COS论坛造成的任何损失或破坏，请各位自己负责。

#####一、RCurl是什么
混迹于各大社区，经常会看到关于浏览器之争的口水战：某某浏览器的市场份额如何如何，某某浏览器的速度如何如何，某某浏览器支持的功能多么强大等等。各个网友也根据自己的喜好，将自身归档于某某浏览器阵营，以此找些心灵的归属。估计类似的口水之争将永远的进行下去（是啊，不然闲着干什么呢？）。如果换个角度看这些争论，也正反应出浏览器在大家日常生活中的地位：想想每天坐在电脑前，用的最多的软件是什么呢？但是提到浏览器阵营中的cURL——一款杀人放火、居家旅游必备的命令行浏览器，则普及率要不少。可它的功能绝不逊色于我们日常用的各大浏览器。R的RCurl包是对cURL库—libcurl的封装。感谢Duncan Temple Lang等牛人的无私工作，我们才可以在R中运用cURL，将R和cURL这两大开源利器的优势完美的结合到一起。

#####二、用RCurl浏览网页
想想我们平时绝大部分时间是怎么用浏览器的？第一步：打开自己钟爱的那款浏览器；第二部：输入某个网址，如http://cos.name/；第三部：回车；第四步：拖拖鼠标，看自己想看的东西；第五步：点进某个链接，接着看。在关注呈现的信息的时候，大多数人都不大会去关心上述的5步（或者更多步）中浏览器（客户端）和网站（服务器端）是如何工作的。其实客户端和服务器端一直在保持联系：告诉对方想干什么，是否同意等等内容？比如我们浏览http://cos.name/时，浏览器给服务器端提交了如下的一些内容：
```shell
GET /HTTP/1.1
Host:cos.name
User-Agent:Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1.6)
Accept:text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Accept-Language:en-us
Accept-Encoding:gzip,deflate
Accept-Charset:GB2312,utf-8;q=0.7,*;q=0.7
Keep-Alive:300
Connection:keep-alive
```
通过上面的头信息，浏览器除了告诉COS服务器想浏览哪些内容，还告诉对方用了什么浏览器、想要什么样的数据格式、用什么协议/方法接收等非常细节的内容。COS服务器收到这些请求后，同样会提供一个物品清单：
```shell
HTTP/1.x200 OK
Date:Fri, 01 Jan 2010 13:11:20 GMT
Server:Apache/2.2.14 (Unix) X-Powered-By: PHP/5.2.11
X-Pingback:http://cos.name/xmlrpc.php
Vary:Accept-Encoding
Content-Encoding:gzip
Content-Length:13973
Keep-Alive:timeout=10, max=30
Connection:Keep-Alive
Content-Type:text/html; charset=UTF-8
```
通过这个清单，COS服务器告诉客户端：服务器是什么配置、你的协议我接受了、我给你的内容是什么格式等等信息。
当用RCurl这款客户端时，我们需要一一配置提交给服务器的内容，所以不妨先随心所欲、照葫芦画瓢的模仿一下上面的头信息：
```R
myHttpheader<- c(
"User-Agent"="Mozilla/5.0(Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1.6)",
"Accept"="text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
"Accept-Language"="en-us",
"Connection"="keep-alive",
"Accept-Charset"="GB2312,utf-8;q=0.7,*;q=0.7"
)
```
然后可以运用getURL函数实现cURL的网页浏览：
```R
temp<- getURL("http://cos.name/",httpheader=myHttpheader)
```
返回值temp为一个字符串，其实就是我们用普通浏览器的“页面另存为”->“html文件”中所包含的内容。
如何看getURL的头信息呢？不妨再多设定几个RCurl的参数：
```R
d =debugGatherer()
temp<- getURL("http://cos.name/",httpheader=myHttpheader,
debugfunction=d$update,verbose= TRUE)
此时d中包含了你所要的信息，其中：
cat(d$value()[3])
中为RCurl提交服务器的头信息，而
cat(d$value()[2])
```
中为服务器端返回的头信息。看看跟正常浏览器的交互内容是不是一样呢？怎么看一般浏览器的头信息呢？如果你用Firefox的话，用扩展Live http headers就可以了。

#####三、RCurl的Handles
在RCurl的目前版本中，有170多个(!!!!!!!!!)cURL系统参数可以设置，具体可以用
names(getCurlOptionsConstants())
查看一下，各个参数的详细说明则可以参照libcurl的官方说明文档。
如此众多参数，如果每次都设定，是不是会非常的繁琐？幸好在RCurl中有一个非常强大的功能可以有效的解决这个问题：那就是cRULhandles（当然，cRUL handles的优势不止这一个）。cRULhandles类似于行走江湖的一个百宝箱：根据自己的喜好设好后，每次背箱出发就行了。同时cRULhandles还根据客户端、服务器端参数的设定在动态的变化，随时更新内容。如下便定义了一个最基本的cRULhandles：
```R
cHandle<- getCurlHandle(httpheader = myHttpheader)
```
在getURL中可以如下应用：
```R
d =debugGatherer()
temp <- getURL("http://cos.name/", .opts = list(debugfunction=d$update,verbose = TRUE), curl=cHandle)
```
此时，cHandle中的cRUL系统参数debugfunction、verbose均发生及时的更新。

#####四、用RCurl实现直接登录
上面提及的getURL函数仅仅实现了页面浏览的最简单功能。如果想用RCurl登录到某个网站（如http://cos.name/bbs/）怎么实现呢？还是继续看看你在正常登录过程中，客户端提交给服务器端的信息吧，然后照葫芦画一个。
```R
http://cos.name/bbs/login.php?

POST/bbs/login.php? HTTP/1.1
Host:cos.name
User-Agent:Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1.6)Gecko/20091201 Firefox/3.5.6
Accept:text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Accept-Language:en-us
Accept-Encoding:gzip,deflate
Accept-Charset:GB2312,utf-8;q=0.7,*;q=0.7
Keep-Alive:300
Connection:keep-alive
Referer:http://cos.name/bbs/login.php
Content-Type:application/x-www-form-urlencoded
Content-Length:110
forward=&jumpurl=http%3A%2F%2Fcos.name%2Fbbs%2F&step=2&lgt=0&pwuser=yourname&pwpwd=yourpw&hideid=0&cktime=31536000
````
这个头信息的核心就是客户端用POST方法给http://cos.name/bbs/login.php?提交了一个字符串（即头信息最后的两行，其中包含了你的用户名和密码），请求服务器端给予身份认证。至于为什么提交那么稀奇古怪的一串字符，就要问谢老大了：）。
如果要让RCurl提交相同的字符串，需要将上面的那段关键字符串转变成如下的格式：
```R
c(name1=”info1”,name2=”info2”,…)。字符串的处理工作交给R就行了：
myPost<- function(x){
 post <-scan(x,what="character",quiet=TRUE,sep="\n")
 abcd=strsplit(post,"&")[[1]]
 abc=gsub("(^.*)(=)(.*$)","\\3",abcd)
 abcnames=gsub("(^.*)(=)(.*$)","\\1",abcd)
 names(abc)=abcnames
 return(abc)
}
postinfo<- myPost("clipboard")
```
然后用RCurl中的postForm函数，将postinfo提交给服务器：
```R
temp<- postForm("http://cos.name/bbs/login.php?",.params=postinfo,
 .opts=list(cookiefile=""),curl=cHandle,style="post")
 ```
用
```R
cat(d$value()[2])
```
查看一下cos.name给你的客户端反馈了那些内容？作为登录认证的cookies是不是已经在里面了？到这一步，RCurl已经成功的登录到http://cos.name/bbs/了，需要的一切认证信息都已经记录到百宝箱cHandle中了。用
```R
getCurlInfo(cHandle)[["cookielist"]]
```
看看你想要的cookie是不是在那里了？接着用cHandle登录一下R子论坛吧，验证一下你是否真正的成功了？
```R
temp<- getURL("http://cos.name/bbs/thread.php?fid=15",
curl=cHandle,.encoding="gbk")
```
#####五、用RCurl实现间接登录
由于“这事儿不能说太细”，我们有时候不能用上面的方法来完成RCurl登录认证。那能不能让RCurl来使用其他浏览器客户端与服务器端已经建立好的认证呢？答案是可以尝试一下的：）所谓的认证信息一般就是服务器端在你的浏览器里面写下的cookies，把他们导出来交给RCurl，RCurl同样可以做好你需要的cURLhandle。
先用你的常规浏览器（此处假定为Firefox）正常登录到http://cos.name/bbs/，然后再用Firefox的扩展Firecookie看看当前页面的cookie信息：你需要的就是它们了。将这些cookie信息导出成RCurl能够识别的格式，然后提交给RCurl就万事大吉了。
```R
d2 =debugGatherer()
cHandle2<- getCurlHandle(httpheader=myHttpheader,followlocation=1,
 debugfunction=d2$update,verbose=TRUE,
 cookiefile="yourcookiefile.txt")
 ```
接着去cos.name的R论坛看看：
```R
temp<- getURL("http://cos.name/bbs/thread.php?fid=15",
curl=cHandle2,.encoding="gbk")
```
验证一下temp里面是不是已经有你的大名了呢？
```R
grep("yourname",temp)
```
如果有的话那么恭喜你：RCurl已经成功接管你的登录权限了。

#####六、登录后RCurl能继续干什么
实现了登录认证的RCurl handles，这仅仅是第一步。能用它和R+RCurl继续做些什么呢？这时候，只要闭上眼睛、海阔天空的想一下平时怎么样用浏览器就有答案了：
1、能不能让RCurl帮我数一下某VIP网络俱乐部中王小麻子灌了多少水？
2、能不能帮顶一下王二麻子发表的美女yy贴？
3、为了给我的外甥女选秀投票，点鼠标点的手都抬不起来了，能不能让RCurl来帮我做呢？
4、我天天去某网站下载文档，绝对的体力活！
5、半夜起来偷菜，太困了，交给RCurl做就好了。
6、用RCurl玩twitter、写博客就好了。
7、我就想用RCurl看门户网站的体育新闻。
8、我就想在各个网站的论坛上发个“顶”字，顺便留下我的牛皮膏药小广告。
9、……
天有多高，RCurl有多强……

#####七、结束语
没有想到会唠叨这么多的废话。但这篇水文仅涉及到libcurl、RCurl中的一点皮毛而已，更多的内容请参考DuncanTemple Lang写的RCurl帮助文档和libcurl官网。客观讲cURL属于浏览器中的一把剪刀，由于它强大的易编程属性，RCurl会带来一些意想不到的破坏性。但要记住：技术本身可能是无罪的，任何的破坏都可能是我们自己造成的。网络中的ID是现实中你的一个延伸，她同样有完整的人格和生命力，所以请尊重和爱护网络中的自己。
最后，希望这篇水文没有影响到你的好心情。


#####课后解疑：
```shell
>library(XML)
>theurl <- "http://home.sina.com"
>download.file(theurl, "tmp.html")
>txt <- readLines("tmp.html")
>txt <- htmlTreeParse(txt, error=function(...){}, useInternalNodes = TRUE)
>g <- xpathSApply(txt, "//p", function(x) xmlValue(x))
>head(grep(" ", g, value=T))
```
方法一无法辨认出中文字。
```shell
>library(RCurl)
>theurl <- getURL("http://home.sina.com",encoding='GB2312')
>Encoding(theurl)
[1]"unknown"
>txt <- readLines(con=textConnection(theurl),encoding='GB2312')
>txt[5:10]
```
方法二也是无法辨认出中文字。


回复 第21楼 的 ryusukekenji：把GB2312改一下，试下utf8啥的，事实上的原因有垃圾新浪用了繁体字，那个编码是big5/utf8的，然后你可以这么做：
```shell
library(RCurl)
theurl <- getURL("http://home.sina.com",encoding='utf8')
#Encoding(theurl)
#[1]"latin1"
txt <- readLines(con=textConnection(theurl),encoding='utf8')
write.table(file='D:/fileas.txt',txt)
```
查看文件，这回就对了。（由于我的R终端设置成不能显示中文，所以就查看文件吧。） 


2  反复看了medo的帖子以及“RCurl能够识别的格式”，但还是对这个cookiefile的格式不甚了解。
在firefox下通过firebug导出cos的Cookie是类似这样的格式：
cos.name    FALSE    /    FALSE    bbpress_logged_in_c0c92aaf0db2e1fa00b8a476daa2244a    82550%7C1324980031%7Ce1c60de3182c3542a68fcd9c844c2cdb
然后用lz说的间接登录的办法登录没有成功。求各位前辈解答cookies.txt的格式应该是什么样的？
我的代码如下：
```R
library(RCurl)
myHttpheader<- c(
    "User-Agent"="Mozilla/5.0(Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1.6)",
    "Accept"="text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Accept-Language"="en-us",
    "Connection"="keep-alive",
    "Accept-Charset"="GB2312,utf-8;q=0.7,*;q=0.7")
d2<- debugGatherer()
cHandle2<- getCurlHandle(httpheader=myHttpheader,followlocation=1,debugfunction=d2$update,verbose=T,cookiefile="~/cookies.txt")
getCurlInfo(cHandle2)[['cookielist']]   ### 这里取出来是空的
temp2<- getURL("http://cos.name/cn",curl=cHandle2,.encoding="utf8")
grep("xxx",temp2)
````







[0]:http://cos.name/cn/topic/17816