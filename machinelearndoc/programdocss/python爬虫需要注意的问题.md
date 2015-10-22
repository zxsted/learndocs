###[python爬虫需要注意的问题][0]

######1 .中文编码
如果你要在url请求中放入中文，对应相应的中文进行编码的话，可以用：urllib.quote('要编码的字符串')
```python
query = urllib.quote(singername)
url   = 'http://music.baidu.com/search?key='+query
response = urllib.urlopen(url)
text = response.read()
```
######2 . get传参
如果要在GET需要一些参数的话，我们需要对传入的参数进行编码
```python
import urllib
def url_get():
	import urllib
    params = urllib.urlencode({'spam':1,'eggs':2,'bacon':0})
    f = urllib.urlopen("http://www.musi-cal.com/cgi-bin/query?%s"
    %params)
    print f.read()
    
def url_post():
	import urllib
    params = urllib.urlopen({'spam':1,'eggs':2,'bacon':0})
    f = urllib.urlopen("http://www.music-cal.com/cgi-bin/query",
    params)
    print f.read()
```

######3.urllib urllib2 proxy 代理
1. 单个代理
```python
import urllib
def url_proxy():
	proxies={'http':'http://211.167.112.14:80'}  #或者
    #proxies = {'':'211.167.112.14:80'}
    opener = urllib.FancyURLopener(proxies)
    f = opener.open("http://www.dianping.com/shanghai")
    print f.read()
```
2. 多个IP代理
```python
import urllib

def url_proxies():
	proxylist = (
    '211.167.112.14:80',
    '210.32.34.115:8080',
    '115.47.8.39:80',
    '211.151.181.41:80',
    '219.299.26.23:80',
    )
    
    for proxy in proxylist:
    	proxies = {'':proxy}
        opener = urllib.FancyURLopener(proxies)
        f = opener.open('http://www.dianping.com/shanghai')
        print f.read()
```
这回没问题了。

有的时候要模拟浏览器 ，不然做过反爬虫的网站会知道你是robot
例如针对浏览器的限制我们可以设置User-Agent头部，针对防盗链限制，我们可以设置Referer头部
有的网站用了Cookie来限制，主要是涉及到登录和限流，这时候没有什么通用的方法，只能看能否做自动登录或者分析Cookie的问题了。

*仅仅是模拟浏览器访问依然是不行的，如果爬取频率过高依然会令人怀疑，那么就需要用到上面的代理设置了*
```python
import urllib2
def url_user_agent(url):
	proxy = 'http://211.167.112.14:80'
    opener = urllib2.build_opener(urllib2.ProxyHandler({'http':proxy}),
    urllib2.HTTPHandler(debuglevel=1))
    urllib2.install_opener(opener)
    
    i_headers = {"User-Agent":"Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1) Gecko/20090624 Firefox/3.5",\
    "Referer":"http://www.dianping.com/"}
    req = urllib2.Request(url,headers=i_headers)
    
    return urllib2.urlopen(req).read()
    
    
```
就算设置了代理，代理的ip也有可能被封，还有另外一种终极的办法来防止被封，那便是使用time库的sleep()函数。
```python
import time

for i in range(1,10):
	#抓取逻辑
    time.sleep(5)
```

Proxy的使用相当广泛，对于单个应用来说，爬虫是很容易被封禁，如果使用Proxy模式，就能降低被封的风险，所以有需求的同学需要仔细看下Python urllib2对于Proxy的使用：


######4.抓取下拉加载或者点击加载的页面的方法：

加载中的内容应该是ajax请求的，对付ajax请求没有什么好的办法，只有抓取页面的JS，分析JS进行抓取

解决方案：
1. 傻傻的全部下拉完 全部点击加载完（对少量数据还行，大量数据的站去死吧） 在Firefox里面copy出源码信息 进
行正则匹配
2. HttpFox抓包  直接抓ajax地址的数据  分析ajax链接 变换参数  取得json后再正则
下面是两个例子：
[Python抓取花瓣网图片脚本](http://yxmhero1989.blog.163.com/blog/static/112157956201311994027168/)
[python抓取bing主页背景图片]:(http://yxmhero1989.blog.163.com/blog/static/112157956201311743439712/)

######5. 正则处理
python对正则表达式的支持模块。如果http库有选择的余地外，re几乎是没有选择余地的工具。因为有正则表达式的存在，所以让我们可以很灵活的去抠取抓取过来的完整html中所需要的部分。

当然，这篇文章不会详细解释正则表达式，因为如果要系统的介绍正则表达式，或许可以写一本书了。这里只简单提一下我们后面会用到的python正则表达式的用法。

* re.compile()。如果正则表达式比较多，请一 定要先用这个方法先行编译正则表达式，之后再正则表达式的使用就会很非常快，因为大家都知道，python文件在第一次运行会分别创建一个字节码文件，如 果正则表达式作为字符串的时候，在运行时才会被编译，是会影响到python的执行速度的。compile()返回的是一个re对象，该对象拥有re库的search(), match(), findall()等方法，这三个方法，在后面会被频繁的用到，生成被编译的re对象还有一个好处是调用方法不用再传入字符串的正则表达式。

* search()主要用来校验正则表达式能否匹配字符串中的一段，通常用来判断该页面是否有我需要的内容。
* match()用来判断字符串是否完全被一个正则表达式匹配，后面用的比较少。
* findall()用来搜索正则表达式在字符串中的所有匹配，并返回一个列表，如果没有任何匹配，则返回一个空列表.带有子组的正则表达式，findall()返回的列表中的每个元素为一个元组，正则表达式中有几个子组，元组中就会有几个元素，第一个元素为第一个括号中的子组匹配到的元素，以此类推。

* findall()和search()是有类似之处的，都是搜索正则表达式在字符串中的匹配，但是findall()返回一个列表，search()返回一个匹配对象，而且findall()返回的列表中有所有匹配，而search()只返回第一个匹配的匹配对象。

######6、更多的例子
```shell
python urllib下载网页
http://www.cnpythoner.com/post/pythonurllib.html

关于不得不在python中使用代理访问网络的方法
http://blogread.cn/it/wap/article/1967

python使用urllib2抓取防爬取链接
http://www.the5fire.net/python-urllib2-crawler.html

Python实战中阶（一）——爬取网页的一点分享
http://blog.goodje.com/2012-08/python-middle-action-web-crawler.html

Python Urllib2使用：代理及其它
http://isilic.iteye.com/blog/1806403

Python urllib2递归抓取某个网站下图片 
http://blog.csdn.net/wklken/article/details/7364899

用Python抓网页的注意事项
http://blog.raphaelzhang.com/2012/03/issues-in-python-crawler/

urllib.urlretrieve下载图片速度很慢 + 【已解决】给urllib.urlretrieve添加user-agent
http://www.crifan.com/use_python_urllib-

urlretrieve_download_picture_speed_too_slow_add_user_agent_for_urlretrieve/
```




#####例子： 抓取bing后面的背景图品
* 1只能抓取当天图片的版本：
ajax 中的调用网址
 ```shell
 http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1&nc=1361089515117&FORM=HYLH1
 ```
 
 
 ```python
 import urllib,re,sys
 
 def get_bing_back():
 	url = 'http://cn.bing.com'
    html = urllib.urlopen(url).read()
    if not html:
    	print 'open & read bing error'
        return -1
    reg = re.compile(";g_img={url:'(.*?)'",re.S)
    text = re.findall(reg,html)
    #http://s.cn.bing.net/az/hprichbg/rb/LongJi_ZH-CN8658435963_1366x768.jpg
    for image in text:
    	right = imgurl.rindx('/')
        savepath = imgurl.replace(imgurl[:right+1],'')
        urllib.urlretrieve(imgurl,savepath)
get_bing_backphoto()
```

* 上面也可以参考：http://www.isayme.org/python-get-bing-day-pic.html
如今思路变了 可以抓ajax那个连接  根据idx为0-N的数字抓取以往的图片  链接上的参数n只能为1 要是传其他的话 他就一直返回今天的数据 想必写过程序的人都了解。
抓过来都不用python json处理了 因为已经read后已经是str型了 不信你type看看。
然后的代码就这样了 你也可以抓他的时间再加图片后面来记录图片是哪天的

```python
#!/usr/bin/env python
#-*-coding:utf-8-*-
#python 抓取bing主页背景图片

import urllib,re,sys

def get_bing_backphoto():
	if(os.path.exists('photos') == False):
    	os.mkdir('photos')
    reg = re.compile('"url":"(.*?)","urlbase"',re.S)
    for i in range(0,100):
    	url = 'http://cn.bing.com/HPImageArchive.aspx?format=js&idx='+str(i)+'&n=1&nc=1361089515117&FORM=HYLH1'
        html = urllib.urlopen(url).read()
        if html == 'null':
        	print 'open&read bing error!'
            sys.exit(-1)
     text = re.findall(reg,html)
     for imgurl in text:
     	right = imgurl.rindex('/')
        name = imgurl.replace(imgurl[:right+1],'')
        savepath = 'photos/'+name
        urllib.urlretrieve(imgurl,savepath)
        print name + 'save success!'
get_bing_backphoto()
```

* 花瓣网图片 （下拉刷新的ajax）

```python
#####################################################################
#花瓣网的架构： LVS +nginx reverse proxy +NodeJS cluster 使用mysql，Redis作
#为主要的数据存储方案
#思路根据ajax的请求链接，获取其返回的json串，提取里面的key值（），正则处理以下就好了然后
#加上网址： http://img.hb.aicdn.com/ 凑成img_url的链接，设置id起点和终
#点，beauty，
#####################################################################
import urllib,urllib2,re,sys,os
reload(sys)
sys.setdefaultencoding('utf-8')

#直接访问http://huaban.com/favorite/beauty/会返回最新的20张

#url = 'http://huaban.com/favorite/'
if(os.path.exists('beauty') == False):
	os.mkdir('beauty')
    
def get_huaban_beauty():
	start = 46284804
    stop = 46285004
    limit = 100 #
    for i in range(start,stop):
    	url = 'http://huaban.com/favorite/beauty/?max='+str(stop)+'&limmit='+str(limit)+'&wfl=1'
        try:
        	 i_headers = {"User-Agent": "Mozilla/5.0(Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1)\
                         Gecko/20090624 Firefox/3.5",\
                     "Referer": 'http://huaban.com/'}
        	req = urllib2.Request(url,headers=i_headers)
            html = urllib2.urlopen(req).read()
            
            reg = re.compile(('"file":{"farm":"farm1", \
           "bucket":"hbimg",.+?"key":"(.*?)",.+?"type":"image/(.*?)"',re.S)
           groups = re.findall(reg,html)
           
           for att in groups:
           	att_url = att[0]
            img_type = att[1]
            img_url = 'http://img.hb.aicdn.com'+ att_url
            urllib.urlretrieve(img_url,'beauty/'+att_url+':'+img_type)
            print img_url+'.'+img_type+'download success!'
        except:
        	print 'error occurs'
            sys.exit(-1)
get_huaban_beauty()
```

上面的代码还是有一点小bug的：
* id不连续的问题：
譬如说 http://huaban.com/favorite/beauty/?hdr01mbf&max=48112770&limit=20&wfl=1 这样一个链接
48112650  f3a6080bb44eccf688b767d6f7a2d16d793ffd7f15161-CbrPwz
48112395
48112339
48112322
48112178
48112140
48112129
此分类下得到的pin_id很难可能是连续的 因为同时有用户把图片pin到另一个分类  占据id
那么现在的思路就是根据max=48112770获取的json串里面最后一个id作为下个循环的max参数

* 大图有固定的后缀
后缀都是固定的
列表小图 http://img.hb.aicdn.com/daa44953fc2ff0ef4b7c39b152aa8d19ecc85759e09d-AxWwdW_fw192
详情大图 http://img.hb.aicdn.com/daa44953fc2ff0ef4b7c39b152aa8d19ecc85759e09d-AxWwdW_fw554
我们要抓取清晰的大图  肯定要在后面加入_fw554后缀。

那么现在的代码是（可能还有其他bug）：

```python
#!/usr/bin/env python
# -*- encoding:utf-8 -*-
# author :insun
#http://yxmhero1989.blog.163.com/blog/static/112157956201311994027168/
import urllib,urllib2,re,sys,os,time
reload(sys)
sys.setdefaultencoding('utf-8')

#url = 'http://huaban.com/favorite/'
if(os.path.exists('beauty') == False):
    os.mkdir('beauty')

def get_huaban_beauty():
    pin_id = 48145457
    limit = 20 #他默认允许的limit为100
    while pin_id != None:
        url = 'http://huaban.com/favorite/beauty/?max='+str(pin_id)+'&limit='+str(limit)+'&wfl=1'
        try:
            i_headers = {"User-Agent": "Mozilla/5.0(Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1)\
                         Gecko/20090624 Firefox/3.5",\
                     "Referer": 'http://baidu.com/'}
            req = urllib2.Request(url, headers=i_headers) 
            html = urllib2.urlopen(req).read()
        
            reg = re.compile('"pin_id":(.*?),.+?"file":{"farm":"farm1", "bucket":"hbimg",.+?"key":"(.*?)",.+?"type":"image/(.*?)"',re.S)
            groups = re.findall(reg,html)
            print str(pin_id)+ "Start  to catch "+str(len(groups))+" photos"
            for att in groups:
                pin_id = att[0]
                att_url = att[1]+'_fw554'
                img_type = att[2]
                img_url = 'http://img.hb.aicdn.com/' + att_url
                if(urllib.urlretrieve(img_url,'beauty/'+att_url+'.'+img_type)):
                    print img_url +'.'+img_type + ' download success!'
                else:
                    print img_url +'.'+img_type + ' save failed'
            #print pin_id
        except:
            print 'error occurs'


get_huaban_beauty()
```

[0]:http://yxmhero1989.blog.163.com/blog/static/112157956201311821444664/














