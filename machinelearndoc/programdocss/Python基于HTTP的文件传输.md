###Python基于HTTP的文件传输
[toc]
####[Python基于HTTP的文件传输示例][1]
本文章来给大家介绍一篇关于Python基于HTTP的文件传输示例 ,希望此方法对各位朋友有帮助，本人是看不懂此文章哦。

因为需要最近看了一下通过POST请求传输文件的内容 并且自己写了Server和Client实现了一个简单的机遇HTTP的文件传输工具

Server:
```python
#coding=utf-8
from BaseHTTPServer import BaseHTTPRequestHandler
import cgi

class   PostHandler(BaseHTTPRequestHandler):

    def do_POST(self):
        form = cgi.FieldStorage(
            fp=self.rfile,
            headers=self.headers,
            environ={'REQUEST_METHOD':'POST',
                     'CONTENT_TYPE':self.headers['Content-Type'],
                     }
        )

        self.send_response(200)
        self.end_headers()
        self.wfile.write('Client: %sn ' % str(self.client_address) )
        self.wfile.write('User-agent: %sn' % str(self.headers['user-agent']))
        self.wfile.write('Path: %sn'%self.path)
        self.wfile.write('Form data:n')

        for field in form.keys():
            field_item = form[field]
            filename = field_item.filename
            filevalue  = field_item.value
            filesize = len(filevalue)#文件大小(字节)
            print len(filevalue)
            with open(filename.decode('utf-8')+'a','wb') as f:
                f.write(filevalue)

        return

if __name__=='__main__':
    from BaseHTTPServer import HTTPServer
    sever = HTTPServer(('localhost',8080),PostHandler)
    print 'Starting server, use <Ctrl-C> to stop'
    sever.serve_forever()
    
```
Client:
```python
#coding=utf-8
import requests

url = 'http://localhost:8080'
path = u'D:快盘阿狸头像.jpg'
print path
files = {'file': open(path, 'rb')}
r = requests.post(url, files=files)
print r.url,r.text
```



####[python实例32[简单的HttpServer]][2]

 

通常地我们要在不同平台间共享文件，samba，ftp，cifs，ntfs的设置都是有点复杂的， 我们可以使用python提供的httpserver来提供基于http方式跨平台的文件共享。

 

一 命令行启动简单的httpserver

进入到web或要共享文件的根目录，然后执行(貌似在python32中此module不存在了)：
```python
python -m SimpleHTTPServer 8000
```
然后你就可以使用http://你的IP地址:8000/来访问web页面或共享文件了。

 

二  代码启动httpserver

simplehttpservertest.py  
```python
import sys
import locale
import http.server
import socketserver

addr = len(sys.argv) < 2 and "localhost" or sys.argv[1]
port = len(sys.argv) < 3 and 80 or locale.atoi(sys.argv[2])

handler = http.server.SimpleHTTPRequestHandler
httpd = socketserver.TCPServer((addr, port), handler)
print ("HTTP server is at: http://%s:%d/" % (addr, port))
httpd.serve_forever()
```
  

需要进入web或要共享的目录，执行下列： 
```shell
simplehttpservertest.py localhost 8008 
```
 

三 第三方的python库Droopy

且支持可以上传文件到共享服务器
http://www.home.unix-ag.org/simon/woof

http://stackp.online.fr/?p=28

 

四 支持上传的httpserver
```python
#!/usr/bin/env python
#coding=utf-8
# modifyDate: 20120808 ~ 20120810
# 原作者为：bones7456, http://li2z.cn/
# 修改者为：decli@qq.com
# v1.2，changeLog：
# +: 文件日期/时间/颜色显示、多线程支持、主页跳转
# -: 解决不同浏览器下上传文件名乱码问题：仅IE，其它浏览器暂时没处理。
# -: 一些路径显示的bug，主要是 cgi.escape() 转义问题
# ?: notepad++ 下直接编译的server路径问题
 
"""
    简介：这是一个 python 写的轻量级的文件共享服务器（基于内置的SimpleHTTPServer模块），
    支持文件上传下载，只要你安装了python（建议版本2.6~2.7，不支持3.x），
    然后去到想要共享的目录下，执行：
        python SimpleHTTPServerWithUpload.py 1234       
    其中1234为你指定的端口号，如不写，默认为 8080
    然后访问 http://localhost:1234 即可，localhost 或者 1234 请酌情替换。
"""
 
"""Simple HTTP Server With Upload.
 
This module builds on BaseHTTPServer by implementing the standard GET
and HEAD requests in a fairly straightforward manner.
 
"""
 
 
__version__ = "0.1"
__all__ = ["SimpleHTTPRequestHandler"]
__author__ = "bones7456"
__home_page__ = ""
 
import os, sys, platform
import posixpath
import BaseHTTPServer
from SocketServer import ThreadingMixIn
import threading
import urllib
import cgi
import shutil
import mimetypes
import re
import time
 
 
try:
    from cStringIO import StringIO
except ImportError:
    from StringIO import StringIO
     
 
print ""
print '----------------------------------------------------------------------->> '
try:
   port = int(sys.argv[1])
except Exception, e:
   print '-------->> Warning: Port is not given, will use deafult port: 8080 '
   print '-------->> if you want to use other port, please execute: '
   print '-------->> python SimpleHTTPServerWithUpload.py port '
   print "-------->> port is a integer and it's range: 1024 < port < 65535 "
   port = 8080
    
if not 1024 < port < 65535:  port = 8080
serveraddr = ('', port)
print '-------->> Now, listening at port ' + str(port) + ' ...'
print '-------->> You can visit the URL:   http://localhost:' + str(port)
print '----------------------------------------------------------------------->> '
print ""
     
 
def sizeof_fmt(num):
    for x in ['bytes','KB','MB','GB']:
        if num < 1024.0:
            return "%3.1f%s" % (num, x)
        num /= 1024.0
    return "%3.1f%s" % (num, 'TB')
 
def modification_date(filename):
    # t = os.path.getmtime(filename)
    # return datetime.datetime.fromtimestamp(t)
    return time.strftime("%Y-%m-%d %H:%M:%S",time.localtime(os.path.getmtime(filename)))
 
class SimpleHTTPRequestHandler(BaseHTTPServer.BaseHTTPRequestHandler):
 
    """Simple HTTP request handler with GET/HEAD/POST commands.
 
    This serves files from the current directory and any of its
    subdirectories.  The MIME type for files is determined by
    calling the .guess_type() method. And can reveive file uploaded
    by client.
 
    The GET/HEAD/POST requests are identical except that the HEAD
    request omits the actual contents of the file.
 
    """
 
    server_version = "SimpleHTTPWithUpload/" + __version__
 
    def do_GET(self):
        """Serve a GET request."""
        # print "....................", threading.currentThread().getName()
        f = self.send_head()
        if f:
            self.copyfile(f, self.wfile)
            f.close()
 
    def do_HEAD(self):
        """Serve a HEAD request."""
        f = self.send_head()
        if f:
            f.close()
 
    def do_POST(self):
        """Serve a POST request."""
        r, info = self.deal_post_data()
        print r, info, "by: ", self.client_address
        f = StringIO()
        f.write('<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 3.2 Final//EN">')
        f.write("<html>\n<title>Upload Result Page</title>\n")
        f.write("<body>\n<h2>Upload Result Page</h2>\n")
        f.write("<hr>\n")
        if r:
            f.write("<strong>Success:</strong>")
        else:
            f.write("<strong>Failed:</strong>")
        f.write(info)
        f.write("<br><a href=\"%s\">back</a>" % self.headers['referer'])
        f.write("<hr><small>Powered By: bones7456, check new version at ")
        f.write("<a href=\"http://li2z.cn/?s=SimpleHTTPServerWithUpload\">")
        f.write("here</a>.</small></body>\n</html>\n")
        length = f.tell()
        f.seek(0)
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.send_header("Content-Length", str(length))
        self.end_headers()
        if f:
            self.copyfile(f, self.wfile)
            f.close()
         
    def deal_post_data(self):
        boundary = self.headers.plisttext.split("=")[1]
        remainbytes = int(self.headers['content-length'])
        line = self.rfile.readline()
        remainbytes -= len(line)
        if not boundary in line:
            return (False, "Content NOT begin with boundary")
        line = self.rfile.readline()
        remainbytes -= len(line)
        fn = re.findall(r'Content-Disposition.*name="file"; filename="(.*)"', line)
        if not fn:
            return (False, "Can't find out file name...")
        path = self.translate_path(self.path)
        osType = platform.system()
        try:
            if osType == "Linux":
                fn = os.path.join(path, fn[0].decode('gbk').encode('utf-8'))
            else:
                fn = os.path.join(path, fn[0])
        except Exception, e:
            return (False, "文件名请不要用中文，或者使用IE上传中文名的文件。")
        while os.path.exists(fn):
            fn += "_"
        line = self.rfile.readline()
        remainbytes -= len(line)
        line = self.rfile.readline()
        remainbytes -= len(line)
        try:
            out = open(fn, 'wb')
        except IOError:
            return (False, "Can't create file to write, do you have permission to write?")
                 
        preline = self.rfile.readline()
        remainbytes -= len(preline)
        while remainbytes > 0:
            line = self.rfile.readline()
            remainbytes -= len(line)
            if boundary in line:
                preline = preline[0:-1]
                if preline.endswith('\r'):
                    preline = preline[0:-1]
                out.write(preline)
                out.close()
                return (True, "File '%s' upload success!" % fn)
            else:
                out.write(preline)
                preline = line
        return (False, "Unexpect Ends of data.")
 
    def send_head(self):
        """Common code for GET and HEAD commands.
 
        This sends the response code and MIME headers.
 
        Return value is either a file object (which has to be copied
        to the outputfile by the caller unless the command was HEAD,
        and must be closed by the caller under all circumstances), or
        None, in which case the caller has nothing further to do.
 
        """
        path = self.translate_path(self.path)
        f = None
        if os.path.isdir(path):
            if not self.path.endswith('/'):
                # redirect browser - doing basically what apache does
                self.send_response(301)
                self.send_header("Location", self.path + "/")
                self.end_headers()
                return None
            for index in "index.html", "index.htm":
                index = os.path.join(path, index)
                if os.path.exists(index):
                    path = index
                    break
            else:
                return self.list_directory(path)
        ctype = self.guess_type(path)
        try:
            # Always read in binary mode. Opening files in text mode may cause
            # newline translations, making the actual size of the content
            # transmitted *less* than the content-length!
            f = open(path, 'rb')
        except IOError:
            self.send_error(404, "File not found")
            return None
        self.send_response(200)
        self.send_header("Content-type", ctype)
        fs = os.fstat(f.fileno())
        self.send_header("Content-Length", str(fs[6]))
        self.send_header("Last-Modified", self.date_time_string(fs.st_mtime))
        self.end_headers()
        return f
 
    def list_directory(self, path):
        """Helper to produce a directory listing (absent index.html).
 
        Return value is either a file object, or None (indicating an
        error).  In either case, the headers are sent, making the
        interface the same as for send_head().
 
        """
        try:
            list = os.listdir(path)
        except os.error:
            self.send_error(404, "No permission to list directory")
            return None
        list.sort(key=lambda a: a.lower())
        f = StringIO()
        displaypath = cgi.escape(urllib.unquote(self.path))
        f.write('<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 3.2 Final//EN">')
        f.write("<html>\n<title>Directory listing for %s</title>\n" % displaypath)
        f.write("<body>\n<h2>Directory listing for %s</h2>\n" % displaypath)
        f.write("<hr>\n")
        f.write("<form ENCTYPE=\"multipart/form-data\" method=\"post\">")
        f.write("<input name=\"file\" type=\"file\"/>")
        f.write("<input type=\"submit\" value=\"upload\"/>")
        f.write("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp")
        f.write("<input type=\"button\" value=\"HomePage\" onClick=\"location='/'\">")
        f.write("</form>\n")
        f.write("<hr>\n<ul>\n")
        for name in list:
            fullname = os.path.join(path, name)
            colorName = displayname = linkname = name
            # Append / for directories or @ for symbolic links
            if os.path.isdir(fullname):
                colorName = '<span style="background-color: #CEFFCE;">' + name + '/</span>'
                displayname = name
                linkname = name + "/"
            if os.path.islink(fullname):
                colorName = '<span style="background-color: #FFBFFF;">' + name + '@</span>'
                displayname = name
                # Note: a link to a directory displays with @ and links with /
            filename = os.getcwd() + '/' + displaypath + displayname
            f.write('<table><tr><td width="60%%"><a href="%s">%s</a></td><td width="20%%">%s</td><td width="20%%">%s</td></tr>\n'
                    % (urllib.quote(linkname), colorName,
                        sizeof_fmt(os.path.getsize(filename)), modification_date(filename)))
        f.write("</table>\n<hr>\n</body>\n</html>\n")
        length = f.tell()
        f.seek(0)
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.send_header("Content-Length", str(length))
        self.end_headers()
        return f
 
    def translate_path(self, path):
        """Translate a /-separated PATH to the local filename syntax.
 
        Components that mean special things to the local file system
        (e.g. drive or directory names) are ignored.  (XXX They should
        probably be diagnosed.)
 
        """
        # abandon query parameters
        path = path.split('?',1)[0]
        path = path.split('#',1)[0]
        path = posixpath.normpath(urllib.unquote(path))
        words = path.split('/')
        words = filter(None, words)
        path = os.getcwd()
        for word in words:
            drive, word = os.path.splitdrive(word)
            head, word = os.path.split(word)
            if word in (os.curdir, os.pardir): continue
            path = os.path.join(path, word)
        return path
 
    def copyfile(self, source, outputfile):
        """Copy all data between two file objects.
 
        The SOURCE argument is a file object open for reading
        (or anything with a read() method) and the DESTINATION
        argument is a file object open for writing (or
        anything with a write() method).
 
        The only reason for overriding this would be to change
        the block size or perhaps to replace newlines by CRLF
        -- note however that this the default server uses this
        to copy binary data as well.
 
        """
        shutil.copyfileobj(source, outputfile)
 
    def guess_type(self, path):
        """Guess the type of a file.
 
        Argument is a PATH (a filename).
 
        Return value is a string of the form type/subtype,
        usable for a MIME Content-type header.
 
        The default implementation looks the file's extension
        up in the table self.extensions_map, using application/octet-stream
        as a default; however it would be permissible (if
        slow) to look inside the data to make a better guess.
 
        """
 
        base, ext = posixpath.splitext(path)
        if ext in self.extensions_map:
            return self.extensions_map[ext]
        ext = ext.lower()
        if ext in self.extensions_map:
            return self.extensions_map[ext]
        else:
            return self.extensions_map['']
 
    if not mimetypes.inited:
        mimetypes.init() # try to read system mime.types
    extensions_map = mimetypes.types_map.copy()
    extensions_map.update({
        '': 'application/octet-stream', # Default
        '.py': 'text/plain',
        '.c': 'text/plain',
        '.h': 'text/plain',
        })
 
class ThreadingServer(ThreadingMixIn, BaseHTTPServer.HTTPServer):
    pass
     
def test(HandlerClass = SimpleHTTPRequestHandler,
       ServerClass = BaseHTTPServer.HTTPServer):
    BaseHTTPServer.test(HandlerClass, ServerClass)
 
if __name__ == '__main__':
    # test()
     
    #单线程
    # srvr = BaseHTTPServer.HTTPServer(serveraddr, SimpleHTTPRequestHandler)
     
    #多线程
    srvr = ThreadingServer(serveraddr, SimpleHTTPRequestHandler)
       srvr.serve_forever()  
     
```

 

五 本地的httpserver

在本地机器没有联网的时候，需要使用如下：来自http://coolshell.cn/articles/1480.html

 如果你只想让这个HTTP服务器服务于本地环境，那么，你需要定制一下你的Python的程序，下面是一个示例：

```python	
import sys
import BaseHTTPServer
from SimpleHTTPServer import SimpleHTTPRequestHandler
HandlerClass = SimpleHTTPRequestHandler
ServerClass  = BaseHTTPServer.HTTPServer
Protocol     = "HTTP/1.0"
 
if sys.argv[1:]:
    port = int(sys.argv[1])
else:
    port = 8000
server_address = ('127.0.0.1', port)
 
HandlerClass.protocol_version = Protocol
httpd = ServerClass(server_address, HandlerClass)
 
sa = httpd.socket.getsockname()
print "Serving HTTP on", sa[0], "port", sa[1], "..."
httpd.serve_forever()
```
 

来自：http://my.oschina.net/leejun2005/blog/71444 







####[用python实现的简单Server/Client文件传输][3]

服务器端：
python 代码
```python
    import SocketServer, time  
      
    class MyServer(SocketServer.BaseRequestHandler):   
        userInfo = {   
            'yangsq'    : 'yangsq',   
            'hudeyong'  : 'hudeyong',   
            'mudan'     : 'mudan' }   
      
        def handle(self):   
            print 'Connected from', self.client_address   
               
            while True:   
                receivedData = self.request.recv(8192)   
                if not receivedData:   
                    continue  
                   
                elif receivedData == 'Hi, server':   
                    self.request.sendall('hi, client')   
                       
                elif receivedData.startswith('name'):   
                    self.clientName = receivedData.split(':')[-1]   
                    if MyServer.userInfo.has_key(self.clientName):   
                        self.request.sendall('valid')   
                    else:   
                        self.request.sendall('invalid')   
                           
                elif receivedData.startswith('pwd'):   
                    self.clientPwd = receivedData.split(':')[-1]   
                    if self.clientPwd == MyServer.userInfo[self.clientName]:   
                        self.request.sendall('valid')   
                        time.sleep(5)   
      
                        sfile = open('PyNet.pdf', 'rb')   
                        while True:   
                            data = sfile.read(1024)   
                            if not data:   
                                break  
                            while len(data) > 0:   
                                intSent = self.request.send(data)   
                                data = data[intSent:]   
      
                        time.sleep(3)   
                        self.request.sendall('EOF')   
                    else:   
                        self.request.sendall('invalid')   
                           
                elif receivedData == 'bye':   
                    break  
      
            self.request.close()   
               
            print 'Disconnected from', self.client_address   
            print  
      
    if __name__ == '__main__':   
        print 'Server is started\nwaiting for connection...\n'   
        srv = SocketServer.ThreadingTCPServer(('localhost', 50000), MyServer)   
        srv.serve_forever()              
```
说明：

    line-55到line-58的作用就相当于java中某个类里面的main函数，即一个类的入口。
    python中SocketServer module里提供了好多实用的现成的类，BaseRequestHandler就是一个，它的作用是为每一个请求fork一个线程，只要继承它，就有这个能力了，哈哈，真是美事。
    当然，我们继承了BaseRequestHandler，就是override它的handle方法，就像java中继承了Thread后要实现run方法一样。实际上这个handle方法的内容和我们的java版本的run函数实现的完全一样。
    line-30到line-43就是处理文件下载的主要内容了。看着都挺眼熟的呵：）
    这里在文件发送完后发了一个“EOF”，告诉client文件传完了。

客户端：
python 代码
```python
    import socket, time  
      
    class MyClient:   
      
        def __init__(self):   
            print 'Prepare for connecting...'   
      
        def connect(self):   
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)   
            sock.connect(('localhost', 50000))   
      
            sock.sendall('Hi, server')   
            self.response = sock.recv(8192)   
            print 'Server:', self.response   
      
            self.s = raw_input("Server: Do you want get the 'thinking in python' file?(y/n):")   
            if self.s == 'y':   
                while True:   
                    self.name = raw_input('Server: input our name:')   
                    sock.sendall('name:' + self.name.strip())   
                    self.response = sock.recv(8192)   
                    if self.response == 'valid':   
                        break  
                    else:   
                        print 'Server: Invalid username'   
      
                while True:   
                    self.pwd = raw_input('Server: input our password:')   
                    sock.sendall('pwd:' + self.pwd.strip())   
                    self.response = sock.recv(8192)   
                    if self.response == 'valid':   
                        print 'please wait...'   
      
                        f = open('b.pdf', 'wb')   
                        while True:   
                            data = sock.recv(1024)   
                            if data == 'EOF':   
                                break  
                            f.write(data)   
                               
                        f.flush()   
                        f.close()   
      
                        print 'download finished'   
                        break  
                    else:   
                        print 'Server: Invalid password'   
                       
      
            sock.sendall('bye')   
            sock.close()   
            print 'Disconnected'   
      
    if __name__ == '__main__':   
        client = MyClient()   
        client.connect()   
```
line-34到line-41处理文件下载，client收到server的“EOF”信号后，就知道文件传完了。

最后需要说明一下python的文件，由于是内置类型，所以不想java那样有那么多的reader，writer，input，ouput啊。python中，在打开或建立一个文件时，主要是通过模式（mode）来区别的。

python的网络编程确实简单，因为它提供了各种功能的已经写好的类，直接继承就Ok了。

python还在学习中，上面的例子跑通是没问题，但写得肯定不够好，还得学习啊





####[Python ftp 文件上传和文件下载][4]

Python ftp 文件上传和文件下载
```python
    import ftplib  
    import os  
    import socket  
      
    HOST='192.168.30.109'  
    FILE='test'  
      
    def main():  
        try:  
            f=ftplib.FTP(HOST)  
        except (socket.error,socket.gaierror),e:  
            print 'ERROR:cannot reach "%s"'% HOST  
            return  
        print '***connected to host "%s"' % HOST  
          
        try:  
            f.login(user='test',passwd='123')  
        except ftplib.error_perm:  
            print 'ERROR:cannot login anonymously'  
            f.quit()  
            return  
        print '***Logged in as "test""'   
          
        try:  
            f.retrbinary('RETR %s' % FILE,open(FILE,'wb').write)  
        except ftplib.error_perm:  
            print 'ERROR:cannot read file "%s"' % FILE  
            os.unlink(FILE)  
        else:  
            print '***Downloaded "%s" to CWD' % FILE  
            return  
        try:  
            f.storbinary('STOR %s' % FILE,open(FILE,'rb'))  
        except ftplib.error_perm:  
            print 'ERROR:cannot up file "%s"' % FILE  
            os.unlink(FILE)  
        else:  
            print '***upload "%s" to ftp' % FILE  
            f.quit()  
            return  
        
    if __name__=='__main__':  
        main()  
```



####[Python模拟HTTP Post上传文件][5]

使用urllib2模块构造http post数据结构，提交有文件的表单(multipart/form-data)，本示例提交的post表单带有两个参数及一张图片，代码如下：
```python
#buld post body data
        boundary = '----------%s' % hex(int(time.time() * 1000))
        data = []
        data.append('--%s' % boundary)
        
        data.append('Content-Disposition: form-data; name="%s"\r\n' % 'username')
        data.append('jack')
        data.append('--%s' % boundary)
        
        data.append('Content-Disposition: form-data; name="%s"\r\n' % 'mobile')
        data.append('13800138000')
        data.append('--%s' % boundary)
        
        fr=open(r'/var/qr/b.png','rb')
        data.append('Content-Disposition: form-data; name="%s"; filename="b.png"' % 'profile')
        data.append('Content-Type: %s\r\n' % 'image/png')
        data.append(fr.read())
        fr.close()
        data.append('--%s--\r\n' % boundary)
    
        http_url='http://remotserver.com/page.php'
        http_body='\r\n'.join(data)
        try:
            #buld http request
            req=urllib2.Request(http_url, data=http_body)
            #header
            req.add_header('Content-Type', 'multipart/form-data; boundary=%s' % boundary)
            req.add_header('User-Agent','Mozilla/5.0')
            req.add_header('Referer','http://remotserver.com/')
            #post data to server
            resp = urllib2.urlopen(req, timeout=5)
            #get response
            qrcont=resp.read()
            print qrcont
            
            
        except Exception,e:
            print 'http error'
```






####[300行python代码的轻量级HTTPServer实现文件上传下载][6]
```python	
#!/usr/bin/env python
#coding=utf-8
# modifyDate: 20120808 ~ 20120810
# 原作者为：bones7456, http://li2z.cn/
# 修改者为：decli@qq.com
# v1.2，changeLog：
# +: 文件日期/时间/颜色显示、多线程支持、主页跳转
# -: 解决不同浏览器下上传文件名乱码问题：仅IE，其它浏览器暂时没处理。
# -: 一些路径显示的bug，主要是 cgi.escape() 转义问题
# ?: notepad++ 下直接编译的server路径问题
 
"""
    简介：这是一个 python 写的轻量级的文件共享服务器（基于内置的SimpleHTTPServer模块），
    支持文件上传下载，只要你安装了python（建议版本2.6~2.7，不支持3.x），
    然后去到想要共享的目录下，执行：
        python SimpleHTTPServerWithUpload.py 1234       
    其中1234为你指定的端口号，如不写，默认为 8080
    然后访问 http://localhost:1234 即可，localhost 或者 1234 请酌情替换。
"""
 
"""Simple HTTP Server With Upload.
 
This module builds on BaseHTTPServer by implementing the standard GET
and HEAD requests in a fairly straightforward manner.
 
"""
 
__version__ = "0.1"
__all__ = ["SimpleHTTPRequestHandler"]
__author__ = "bones7456"
__home_page__ = ""
 
import os, sys, platform
import posixpath
import BaseHTTPServer
from SocketServer import ThreadingMixIn
import threading
import urllib, urllib2
import cgi
import shutil
import mimetypes
import re
import time
 
try:
    from cStringIO import StringIO
except ImportError:
    from StringIO import StringIO
     
def get_ip_address(ifname):
    import socket
    import fcntl
    import struct
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    return socket.inet_ntoa(fcntl.ioctl(
        s.fileno(),
        0x8915, # SIOCGIFADDR
        struct.pack('256s', ifname[:15])
    )[20:24])
 
class GetWanIp:
    def getip(self):
        try:
           myip = self.visit("http://ip.taobao.com/service/getIpInfo.php?ip=myip")
        except:
            print "ip.taobao.com is Error"
            try:
                myip = self.visit("http://www.bliao.com/ip.phtml")
            except:
                print "bliao.com is Error"
                try:
                    myip = self.visit("http://www.whereismyip.com/")
                except: # 'NoneType' object has no attribute 'group'
                    print "whereismyip is Error"
                    myip = "127.0.0.1"
        return myip
    def visit(self,url):
        #req = urllib2.Request(url)
        #values = {'User-Agent': 'Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537',
        #            'Referer': 'http://ip.taobao.com/ipSearch.php',
        #            'ip': 'myip'
        #         }
        #data = urllib.urlencode(values)
        opener = urllib2.urlopen(url, None, 3)
        if url == opener.geturl():
            str = opener.read()
        return re.search('(\d+\.){3}\d+',str).group(0)
 
def showTips():
    print ""
    print '----------------------------------------------------------------------->> '
    try:
        port = int(sys.argv[1])
    except Exception, e:
        print '-------->> Warning: Port is not given, will use deafult port: 8080 '
        print '-------->> if you want to use other port, please execute: '
        print '-------->> python SimpleHTTPServerWithUpload.py port '
        print "-------->> port is a integer and it's range: 1024 < port < 65535 "
        port = 8080
     
    if not 1024 < port < 65535:  port = 8080
    # serveraddr = ('', port)
    print '-------->> Now, listening at port ' + str(port) + ' ...'
    osType = platform.system()
    if osType == "Linux":
        print '-------->> You can visit the URL:     http://'+ GetWanIp().getip() + ':' +str(port)
    else:
        print '-------->> You can visit the URL:     http://127.0.0.1:' +str(port)
    print '----------------------------------------------------------------------->> '
    print ""
    return ('', port)
 
serveraddr = showTips()   
 
def sizeof_fmt(num):
    for x in ['bytes','KB','MB','GB']:
        if num < 1024.0:
            return "%3.1f%s" % (num, x)
        num /= 1024.0
    return "%3.1f%s" % (num, 'TB')
 
def modification_date(filename):
    # t = os.path.getmtime(filename)
    # return datetime.datetime.fromtimestamp(t)
    return time.strftime("%Y-%m-%d %H:%M:%S",time.localtime(os.path.getmtime(filename)))
 
class SimpleHTTPRequestHandler(BaseHTTPServer.BaseHTTPRequestHandler):
 
    """Simple HTTP request handler with GET/HEAD/POST commands.
 
    This serves files from the current directory and any of its
    subdirectories.  The MIME type for files is determined by
    calling the .guess_type() method. And can reveive file uploaded
    by client.
 
    The GET/HEAD/POST requests are identical except that the HEAD
    request omits the actual contents of the file.
 
    """
 
    server_version = "SimpleHTTPWithUpload/" + __version__
 
    def do_GET(self):
        """Serve a GET request."""
        # print "....................", threading.currentThread().getName()
        f = self.send_head()
        if f:
            self.copyfile(f, self.wfile)
            f.close()
 
    def do_HEAD(self):
        """Serve a HEAD request."""
        f = self.send_head()
        if f:
            f.close()
 
    def do_POST(self):
        """Serve a POST request."""
        r, info = self.deal_post_data()
        print r, info, "by: ", self.client_address
        f = StringIO()
        f.write('<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 3.2 Final//EN">')
        f.write("<html>\n<title>Upload Result Page</title>\n")
        f.write("<body>\n<h2>Upload Result Page</h2>\n")
        f.write("<hr>\n")
        if r:
            f.write("<strong>Success:</strong>")
        else:
            f.write("<strong>Failed:</strong>")
        f.write(info)
        f.write("<br><a href=\"%s\">back</a>" % self.headers['referer'])
        f.write("<hr><small>Powered By: bones7456, check new version at ")
        f.write("<a href=\"http://li2z.cn/?s=SimpleHTTPServerWithUpload\">")
        f.write("here</a>.</small></body>\n</html>\n")
        length = f.tell()
        f.seek(0)
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.send_header("Content-Length", str(length))
        self.end_headers()
        if f:
            self.copyfile(f, self.wfile)
            f.close()
         
    def deal_post_data(self):
        boundary = self.headers.plisttext.split("=")[1]
        remainbytes = int(self.headers['content-length'])
        line = self.rfile.readline()
        remainbytes -= len(line)
        if not boundary in line:
            return (False, "Content NOT begin with boundary")
        line = self.rfile.readline()
        remainbytes -= len(line)
        fn = re.findall(r'Content-Disposition.*name="file"; filename="(.*)"', line)
        if not fn:
            return (False, "Can't find out file name...")
        path = self.translate_path(self.path)
        osType = platform.system()
        try:
            if osType == "Linux":
                fn = os.path.join(path, fn[0].decode('gbk').encode('utf-8'))
            else:
                fn = os.path.join(path, fn[0])
        except Exception, e:
            return (False, "文件名请不要用中文，或者使用IE上传中文名的文件。")
        while os.path.exists(fn):
            fn += "_"
        line = self.rfile.readline()
        remainbytes -= len(line)
        line = self.rfile.readline()
        remainbytes -= len(line)
        try:
            out = open(fn, 'wb')
        except IOError:
            return (False, "Can't create file to write, do you have permission to write?")
                 
        preline = self.rfile.readline()
        remainbytes -= len(preline)
        while remainbytes > 0:
            line = self.rfile.readline()
            remainbytes -= len(line)
            if boundary in line:
                preline = preline[0:-1]
                if preline.endswith('\r'):
                    preline = preline[0:-1]
                out.write(preline)
                out.close()
                return (True, "File '%s' upload success!" % fn)
            else:
                out.write(preline)
                preline = line
        return (False, "Unexpect Ends of data.")
 
    def send_head(self):
        """Common code for GET and HEAD commands.
 
        This sends the response code and MIME headers.
 
        Return value is either a file object (which has to be copied
        to the outputfile by the caller unless the command was HEAD,
        and must be closed by the caller under all circumstances), or
        None, in which case the caller has nothing further to do.
 
        """
        path = self.translate_path(self.path)
        f = None
        if os.path.isdir(path):
            if not self.path.endswith('/'):
                # redirect browser - doing basically what apache does
                self.send_response(301)
                self.send_header("Location", self.path + "/")
                self.end_headers()
                return None
            for index in "index.html", "index.htm":
                index = os.path.join(path, index)
                if os.path.exists(index):
                    path = index
                    break
            else:
                return self.list_directory(path)
        ctype = self.guess_type(path)
        try:
            # Always read in binary mode. Opening files in text mode may cause
            # newline translations, making the actual size of the content
            # transmitted *less* than the content-length!
            f = open(path, 'rb')
        except IOError:
            self.send_error(404, "File not found")
            return None
        self.send_response(200)
        self.send_header("Content-type", ctype)
        fs = os.fstat(f.fileno())
        self.send_header("Content-Length", str(fs[6]))
        self.send_header("Last-Modified", self.date_time_string(fs.st_mtime))
        self.end_headers()
        return f
 
    def list_directory(self, path):
        """Helper to produce a directory listing (absent index.html).
 
        Return value is either a file object, or None (indicating an
        error).  In either case, the headers are sent, making the
        interface the same as for send_head().
 
        """
        try:
            list = os.listdir(path)
        except os.error:
            self.send_error(404, "No permission to list directory")
            return None
        list.sort(key=lambda a: a.lower())
        f = StringIO()
        displaypath = cgi.escape(urllib.unquote(self.path))
        f.write('<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 3.2 Final//EN">')
        f.write("<html>\n<title>Directory listing for %s</title>\n" % displaypath)
        f.write("<body>\n<h2>Directory listing for %s</h2>\n" % displaypath)
        f.write("<hr>\n")
        f.write("<form ENCTYPE=\"multipart/form-data\" method=\"post\">")
        f.write("<input name=\"file\" type=\"file\"/>")
        f.write("<input type=\"submit\" value=\"upload\"/>")
        f.write("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp")
        f.write("<input type=\"button\" value=\"HomePage\" onClick=\"location='/'\">")
        f.write("</form>\n")
        f.write("<hr>\n<ul>\n")
        for name in list:
            fullname = os.path.join(path, name)
            colorName = displayname = linkname = name
            # Append / for directories or @ for symbolic links
            if os.path.isdir(fullname):
                colorName = '<span style="background-color: #CEFFCE;">' + name + '/</span>'
                displayname = name
                linkname = name + "/"
            if os.path.islink(fullname):
                colorName = '<span style="background-color: #FFBFFF;">' + name + '@</span>'
                displayname = name
                # Note: a link to a directory displays with @ and links with /
            filename = os.getcwd() + '/' + displaypath + displayname
            f.write('<table><tr><td width="60%%"><a href="%s">%s</a></td><td width="20%%">%s</td><td width="20%%">%s</td></tr>\n'
                    % (urllib.quote(linkname), colorName,
                        sizeof_fmt(os.path.getsize(filename)), modification_date(filename)))
        f.write("</table>\n<hr>\n</body>\n</html>\n")
        length = f.tell()
        f.seek(0)
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.send_header("Content-Length", str(length))
        self.end_headers()
        return f
 
    def translate_path(self, path):
        """Translate a /-separated PATH to the local filename syntax.
 
        Components that mean special things to the local file system
        (e.g. drive or directory names) are ignored.  (XXX They should
        probably be diagnosed.)
 
        """
        # abandon query parameters
        path = path.split('?',1)[0]
        path = path.split('#',1)[0]
        path = posixpath.normpath(urllib.unquote(path))
        words = path.split('/')
        words = filter(None, words)
        path = os.getcwd()
        for word in words:
            drive, word = os.path.splitdrive(word)
            head, word = os.path.split(word)
            if word in (os.curdir, os.pardir): continue
            path = os.path.join(path, word)
        return path
 
    def copyfile(self, source, outputfile):
        """Copy all data between two file objects.
 
        The SOURCE argument is a file object open for reading
        (or anything with a read() method) and the DESTINATION
        argument is a file object open for writing (or
        anything with a write() method).
 
        The only reason for overriding this would be to change
        the block size or perhaps to replace newlines by CRLF
        -- note however that this the default server uses this
        to copy binary data as well.
 
        """
        shutil.copyfileobj(source, outputfile)
 
    def guess_type(self, path):
        """Guess the type of a file.
 
        Argument is a PATH (a filename).
 
        Return value is a string of the form type/subtype,
        usable for a MIME Content-type header.
 
        The default implementation looks the file's extension
        up in the table self.extensions_map, using application/octet-stream
        as a default; however it would be permissible (if
        slow) to look inside the data to make a better guess.
 
        """
 
        base, ext = posixpath.splitext(path)
        if ext in self.extensions_map:
            return self.extensions_map[ext]
        ext = ext.lower()
        if ext in self.extensions_map:
            return self.extensions_map[ext]
        else:
            return self.extensions_map['']
 
    if not mimetypes.inited:
        mimetypes.init() # try to read system mime.types
    extensions_map = mimetypes.types_map.copy()
    extensions_map.update({
        '': 'application/octet-stream', # Default
        '.py': 'text/plain',
        '.c': 'text/plain',
        '.h': 'text/plain',
        })
 
class ThreadingServer(ThreadingMixIn, BaseHTTPServer.HTTPServer):
    pass
     
def test(HandlerClass = SimpleHTTPRequestHandler,
       ServerClass = BaseHTTPServer.HTTPServer):
    BaseHTTPServer.test(HandlerClass, ServerClass)
 
if __name__ == '__main__':
    # test()
     
    #单线程
    # srvr = BaseHTTPServer.HTTPServer(serveraddr, SimpleHTTPRequestHandler)
     
    #多线程
    srvr = ThreadingServer(serveraddr, SimpleHTTPRequestHandler)
     
    srvr.serve_forever()
```
REF: 

1、httpserver
=======================================
This httpserver is a enhanced version of SimpleHTTPServer. 
It was write in python, I use some code from bottle[https://github.com/defnull/bottle] 
It support resuming download, you can set the document root, it has more 
friendly error hit, and it can handle mimetype gracefully

https://github.com/lerry/httpserver/blob/master/httpserver.py

2、基于 java netty 的 SimpleHTTPServer，

由于windows不支持某些 netty low-level API，该代码仅能运行在 linux 下：

https://github.com/dvliman/SimpleHTTPServer 
[1]:
[2]:http://www.cnblogs.com/itech/archive/2011/12/31/2308697.html
[3]:http://blog.sina.com.cn/s/blog_56146dc501009exm.html
[4]:http://www.linuxidc.com/Linux/2011-12/48790.htm
[5]:http://www.cnblogs.com/chy710/p/3791317.html
[6]: