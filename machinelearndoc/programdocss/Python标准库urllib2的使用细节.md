####[Python标准库urllib2的使用细节][0]
[toc]
原文[传送门][1]
#####1 Proxy的设置
urllib2默认会使用环境变量http_proxy来设置HTTP Proxy。如果想在程序中明确控制Proxy，而不受环境影响，可以使用下面的方式：
```python
import urillib2

enable_proxy = True
proxy_handler = urillib2.ProxyHandler({"http":'http://some-proxy.com:8080'})
null_proxy_handler = urllib2.ProxyHandler({})

if enable_proxy:
	opener = urllib2.build_opener(proxy_handler)
else:
	opener = urllib2.build_opener(null_proxy_handler)
    
urllib2.install_opener(opener)
```
这里要注意一个细节：使用urllib2.install_opener()会设置urllib2的全局opener。这样后面的使用会很方便，但是不能作更细粒度的控制，比如在程序中使用两个不同的Proxy设置等。较为好的做法是不使用install_opener去更改全局设置，而只是直接调用opener的open方法代替全局的urlopen方法


#####2、timeout设置
在老版本中，urllib2的API并没有暴露Timeout的设置，只能更改Socket的全局Timeout的值
```python
import urllib2
import socket

socket.setdefaulttimeout(10) #10后超时
urllib2.socket.setdefaulttimeout(10) #另一种方式
```
在新的Python2.6版本中，超时可以通过urllib2.urlopen（）的timeout参数直接设置
```python
import urllib2
reponse = urlllib2.urlopen('http://www.google.com',timeout=10)
```
#####3 在HTTP Request中加入特定的Header
要加入Header，需要使用Request对象：
```python
import urllib2
request = urllib2.Request(url)
request.add_header('User-Agent','fake-client')
response = urllib2.urlopen(request)
```
对有些header要特别留意，Server端会针对这些header作检查
  * User-Agent有些Server或Proxy会检查该值，用来判断是否是浏览器发起的Request
  * Content-Type在使用REST接口时，Server会检查该值，用来确定HTTP Body中的内容该怎样分析。
  常见的取值有：
  1. application/xml :在XML RPC,如RESTful/SOAP调用时使用
  2. application/json :在JSON RPC调用时调用
  3. application/x-www-form-urlencoded: 浏览器提交Web表单时使用
  4. ......
  在使用RPC调用时提供的RESTful或SOAP服务时，Content-Type设置错误会导致Server拒绝服务。
  
#####4 Redicrect
urillib2 默认情况下会针对3xx HTTP返回码自动进行Redict动作，无须人工配置。要检测是否发生了Redict动作，只要检查一下Response的URL和Request的URL是否一致就可以了。
```python
import urllib2
response = urllib2.urlopen('http://www.google.cn')
redicted = response.geturl() == 'http://www.google.cn'
```
如果不想自动Redict，除了使用更低层的httplib库之外，还可以使用自定义的HTTPRedirectHandler类。
```python
import urllib2

class RedirectHandler(urllib2.HTTPRedirectHandler):
	def http_error_301(self,req,fp,code,msg,header):
    	pass
    def http_error_302(self,req,fp,code,msg,headers):
    	pass
        
opener = urllib2.build_opener(RedirectHandler)
opener.open('http://www.google.cn')
```
#####5、Cookie
urllib2 对Cookie的处理也是自动的。如果需要得到某个Cokkie项的值，可以这么作：
```python
import urllib2
import cookielib

cookie = cookielib.CookieJar()
opener = urllib2.build_opener(urllib2,HTTPCookieProcessor(cookie))
response = opener.open('http://www.google.com')
for item in cookie:
	if item.name == 'some_cookie_item_name':
    	print item.value
```

#####5.使用HTTP的put和delete方法
urllib2只支持Http的GET和POST方法，如果要使用HTTP PUT 和 DELETE，只能使用比较底层的httplib库。我们可以使用下面的方式 使urllib可以发出HTTP PUT 或DELETE的包
```python
import urllib2

request = urllib2.Request(uri,data=data)
equest.get_method = lambda:'PUT' # or 'DELETE'
response = urllib2.urlopen(request)
```
#####7.得到HTTP的返回码
```python
import urllib2
try:
	reponse = urllib2.urlopen('http://restrict.web.com')
except urllib2.HTTPError,e:
	print e.code
```

##### 8 Debug Log
使用urllib2时，可以通过下面的方法将Debug Log打开，着要收发包的内容就会在屏幕上打开，方便我们调试，在一定程度上可以省去抓包的工作
```python
import urllib2
httpHandler = urllib2.HTTPHandler(debuglevel=1)
httpsHandler = urllib2.HTTPSHandler(debuglevel=1)
opener = urllib2.build_opener(httpHandler,httpsHandler)

urllib2.install_opener(opener)
response=urllib2.urlopen('http://www.google.com')
```

#####9、urllib2.urlopen超时问题
[原文传送门][2]
**问题描述：**
没有设置timeout参数，结果在网络环境不好的情况下，时常出现read()方法没有任何反应的问题，程序卡死在read方法中，这个问题的原因是：没有设置链接超时。解决方法：给加上timeout就ok了，*注意：*设置了timeout之后read超时会抛出socket.timeout异常，想要程序稳定，还需要给urlopen加上异常处理，再加上异常重试，程序就完美了：
```python
import urllib2
url = 'http://www.facebook.com'
fails = 0
while True:
	try:
    	if fails >= 20:
        	break;
        req = urllib2.Request(url)
        response = urllib2.urlopen(req,None,3)
        page = response.read()
    except:
    	fails += 1
        print '网络连接出现问题，正在尝试再次请求：'，fails
    else:
    	break
```

加入超时限制：
我们在爬取网络数据时，会因为对方网速缓慢、服务器超时等原因，导致urllib2.urlopen（）之后的read（）操作（下载内容）卡死；下面是三个超时限制的措施：

* 1、为urlopen设置可选参数 timeout

```python
import urllib2
#http://classweb.loxa.com.tw/dino123/air/P1000772.jpg
r = urllib2.Request("http://classweb.loxa.com.tw/dino123/air/P1000775.jpg")
try:
	print 1111111111111111111111
    f = urllib2.urlopen(r,data=None,timeout=3)
    print 2222222222222222222222
    result = f.read()
    print 3333333333333333333333
excpet Exception,e:
	print "444444444444444444444----------" + str(e)
    
print "555555555555555555555"
```

* 设置全局的socket超时：

```python
import socket
socket.setdefaulttimeout(10.0)
```
或者使用： httplib2 or timeout_urllib2：
http://code.google.com/p/httplib2/wiki/Examples
http://code.google.com/p/timeout-urllib2/source/browse/trunk/timeout_urllib2.py

* 使用定时器 timer
```python
from urllib2 import urlopen
from threading import Timer
url = 'http://www.python.org'
def handler(fh):
	fh.close()
fh = urlopen(url)
t = timer(20.0,handler,[fh])
t.start()
data = fh.read()  # 如果二进制文件需要转化为二进制的读取方式
t.cancel()
```

#####[urllib 与 urllib2的区别和协作][0]
* urllib：比较简单，功能相对也比较弱，可以从指定的URL下载文件，或是对一些字符串进行编码解码以使他们成为特定的 URL串。
* urllib2：它有各种各样的Handler啊，Processor啊可以处理更复杂的问 题，比如网络认证，使用代理服务器，使用cookie等等

HTTP是基于请求和应答机制的--客户端提出请求，服务端提供应答。urllib2用一个Request对象来映射你提出的HTTP请求,在它最简单的使用形式中你将用你要请求的地址创建一个Request对象，通过调用urlopen并传入Request对象，将返回一个相关请求response对象，这个应答对象如同一个文件对象，所以你可以在Response中调用.read()。

urllib 和urllib2(python3已经合并成一个了)都是接受URL请求的相关模块，但是提供了不同的功能。（老外写的）
1. urllib2可以接受一个Request类的实例来设置URL请求的headers，urllib仅可以接受URL:这意味着你不可以伪装你的User Agent字符串等。
2. urllib提供urlencode方法用来GET查询字符串的产生，而urllib2没有。这是为何urllib常和urllib2一起使用的原因。Data同样可以通过在Get请求的URL本身上面编码来传送。urllib.urlencode(data) 

下面介绍几个方法

1. 加上get或者post的数据
 ```python
 data = {"name":"hank","passwd":"hjz"}

 #对参数进行编码
 params = urllib.urlencode(data)
 urllib2.urlopen(url,data=params)
 ```

2. 添加header头
```python
header = {"User-Agent":"Mozilla-Firefox5.0"}
urllib2.urlopen(url,urllib.urlencode(data),header)
```

3. 使用opener和handler
```python
opener = urllib2.build_opener(handler)
urllib2.install_opener(opener)
```

4. 添加session  *实际上是添加cookie*
 ```python
 cj = cookielib.CookieJar()
 cjhandler = urllib2.HTTPCookieProcessor(cj)
 opener = urllib2.build_openr(cjhandler)
 ```

5. 中文编码
如果在url中请求中放入中文，对相应的中进行编码的化，可以使用：urllib.quote('要编码的字符串')
```python
query = urllib.quote(singername)
url = 'http://music.baidu.com/search?key='+query
response = urllib.urlopen(url)
text = response.read()
```






[3]:http://hankjin.blog.163.com/blog/static/3373193720105140583594/


[0]:http://www.cnblogs.com/yuxc/archive/2011/08/01/2123995.html
[1]:http://zhuoqiang.me/a/python-urllib2-usage
[2]:http://blog.csdn.net/waterforest_pang/article/details/16885259