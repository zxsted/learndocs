####[paramiko的安装与使用][0]
[toc]
#####一、简介
paramiko是用python语言写的一个模块，遵循SSH2协议，支持以加密和认证的方式，进行远程服务器的连接。

由于使用的是python这样的能够跨平台运行的语言，所以所有python支持的平台，如Linux, Solaris, BSD, MacOS X, Windows等，paramiko都可以支持，因此，如果需要使用SSH从一个平台连接到另外一个平台，进行一系列的操作时，paramiko是最佳工具之一。

举个常见的例子，现有这样的需求：需要使用windows客户端，远程连接到Linux服务器，查看上面的日志状态，大家通常使用的方法会是：

1：用telnet

2：用PUTTY

3：用WinSCP

4：用XManager等…

那现在如果需求又增加一条，要从服务器上下载文件，该怎么办？那常用的办法可能会是：

1：Linux上安装FTP并配置

2：Linux上安装Sambe并配置…

大家会发现，常见的解决方法都会需要对远程服务器必要的配置，如果远程服务器只有一两台还好说，如果有N台，还需要逐台进行配置，或者需要使用代码进行以上操作时，上面的办法就不太方便了。

使用paramiko可以很好的解决以上问题，比起前面的方法，它仅需要在本地上安装相应的软件（python以及PyCrypto），对远程服务器没有配置要求，对于连接多台服务器，进行复杂的连接操作特别有帮助。

#####二：安装

安装paramiko有两个先决条件，python和另外一个名为PyCrypto的模块。

通常安装标准的python模块，只需要在模块的根目录下运行：
```shell
python setup.py build

python setup.py install
```
以上两条命令即可，paramiko和PyCrypto也不例外，唯一麻烦的就是安装PyCrypto时，需要GCC库编译，如果没有GCC库会报错，会导致PyCrypto以及paramiko无法安装。

以下以32 位的windows XP为例，说明paramiko的安装过程

 

1. 安装python，2.2以上版本都可以，我使用的是2.5，安装过程略，并假设安装目录是c:\python。

2. 判断本地是否安装了GCC，并在PATH变量可以找到，如果没有，可使用windows 版的GCC，即MinGW，下载地址：http://sourceforge.net/projects/mingw/，然后运行下载后的exe文件进行网络安装，假设目录为C:\mingw，在PATH中加入 C:\mingw\bin，并在c:\python\lib\distutils下新建一个名称是distutils.cfg的文件，填入：
```python
[build] 
compiler=mingw32
 ```

3. 下载PyCrypto ,地址是
```shell
https://www.dlitz.net/software/pycrypto/
```
安装PyCrypto:

解压缩
在dos下进入解压缩的目录，运行
```shell
C:\python\python.exe setup.py build

C:\python\python.exe setup.py install
 ```

安装测试  
　　运行python.exe，在提示符下输入：
```python
Import  Crypto
```
　　如果没有出现错误提示，说明Crypto安装成功

 

#####4：下载paramiko，
地址是http://www.lag.net/paramiko/

解压缩
在dos下进 入解压缩的目录，运行
```python
C:\python\python.exe setup.py build

C:\python\python.exe setup.py install
```
测试paramiko
　　运行python.exe，在提示符下输入：
```python
Import  paramiko
```
　　如果没有出现错误提示，说明paramiko安装成功

 

#####三： 使用paramiko

 

如果大家感觉安装paramiko还是略有麻烦的话，当使用到paramiko提供的方便时便会觉得这是十分值得的。

下面是两种使用paramiko连接到linux服务器的代码

方式一：
```python
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("某IP地址",22,"用户名", "口令")
```
上面的第二行代码的作用是允许连接不在know_hosts文件中的主机。

 

方式二：
```python
t = paramiko.Transport((“主机”,”端口”))
t.connect(username = “用户名”, password = “口令”)
```
如果连接远程主机需要提供密钥，上面第二行代码可改成：
```python
t.connect(username = “用户名”, password = “口令”, hostkey=”密钥”)
 ```

下面给出实际的例子：

######3.1 windows对linux运行任意命令,并将结果输出

如果linux服务器开放了22端口，在windows端，我们可以使用paramiko远程连接到该服务器，并执行任意命令，然后通过 print或其它方式得到该结果，

代码如下：

```python
#!/usr/bin/python 
import paramiko
 
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("某IP地址",22,"用户名", "口令")
stdin, stdout, stderr = ssh.exec_command("你的命令")
print stdout.readlines()
ssh.close()
```
其中的”你的命令”可以任意linux支持的命令，如一些常用的命令：

复制代码
```shell
df：查看磁盘使用情况
uptime：显示系统运行时间信息
cat：显示某文件内容
mv/cp/mkdir/rmdir:对文件或目录进行操作
/sbin/service/ xxxservice start/stop/restart：启动、停止、重启某服务
netstat -ntl |grep 8080：查看8080端口的使用情况 
 或者 nc -zv localhost ：查看所有端口的使用情况 
find / -name XXX：查找某文件
...
```
这样一来，对于linux的任何操作几乎都可以通过windows端完成，如果对该功能进行引申，还可以同时管理多台服务器。

 

######3.2 从widnows端下载linux服务器上的文件

复制代码
```python
#!/usr/bin/python 
import paramiko
 
t = paramiko.Transport((“主机”,”端口”))
t.connect(username = “用户名”, password = “口令”)
sftp = paramiko.SFTPClient.from_transport(t)
remotepath=’/var/log/system.log’
localpath=’/tmp/system.log’
sftp.get(remotepath, localpath)
t.close()
```
 

######3.3 从widnows端上传文件到linux服务器

```python
#!/usr/bin/python 
import paramiko

t = paramiko.Transport((“主机”,”端口”))
t.connect(username = “用户名”, password = “口令”)
sftp = paramiko.SFTPClient.from_transport(t)
remotepath=’/var/log/system.log’
localpath=’/tmp/system.log’
sftp.put(localpath,remotepath)
t.close()
```

我对paramiko的使用也刚刚开始，以上若有不对的地方，敬请大家指正！先谢了！



####Python的包管理工具Pip
接触了Ruby，发现它有个包管理工具RubyGem很好用，并且有很完备的文档系统http://rdoc.info
发现Python下也有同样的工具，包括easy_install和Pip。不过，我没有细看easy_install的方法，这就简单的介绍一下Pip的安装与使用：
准备：
```shell
$ curl -O http://python-distribute.org/distribute_setup.py
$ python distribute_setup.py
```
安装：
```shell
$ curl -O https://raw.github.com/pypa/pip/master/contrib/get-pip.py
$ python get-pip.py
```
使用方法：
```shell
$ pip install SomePackage
$ pip search "query"
$ pip install --upgrade SomePackage
$ pip install --upgrade SomePackage==version
```
补充：
包安装后的py文件路径：/usr/local/lib/python2.7/dist-packages



 ####[python模块paramiko的上传下载和远程执行命令方法][1]
1：连接远程linux主机并执行命令
```python
#!/usr/bin/env python 
import paramiko   
hostname='192.168.0.102'  
username='root'  
password='abc'   
port=22     
paramiko.util.log_to_file('paramiko.log')          
s=paramiko.SSHClient()                 
s.set_missing_host_key_policy(paramiko.AutoAddPolicy())          
s.connect(hostname = hostname,port=port,username=username, password=password)          
stdin,stdout,stderr=s.exec_command('free;df -h')          
print stdout.read()          
s.close()  
```
执行结果如下：
```shell
             total       used       free     shared    buffers     cached 
Mem:       2074940    2057420      17520          0      42416    1867968 
-/+ buffers/cache:     147036    1927904 
Swap:      2096472        240    2096232 
Filesystem            Size  Used Avail Use% Mounted on 
/dev/sda1              30G   12G   17G  42% / 
none                 1014M     0 1014M   0% /dev/shm 
/dev/sda3             2.0G  289M  1.6G  16% /var 
/dev/sdb1             135G   14G  115G  11% /data 
/dev/sdc1             135G  127G  880M 100% /data1 
/dev/sdd1             135G   99G   30G  78% /data2 
```
2：连接远程linux主机上传下载文件（paramiko模块是用SFTP协议来实现的）
```python
#!/usr/bin/env python  
import paramiko,datetime,os 
hostname='192.168.0.102'  
username='root'  
password='abc123'  
port=22  
local_dir='/tmp/'  
remote_dir='/tmp/test/' 
try: 
    t=paramiko.Transport((hostname,port))          
    t.connect(username=username,password=password)          
    sftp=paramiko.SFTPClient.from_transport(t)  
    #files=sftp.listdir(dir_path)          
    files=sftp.listdir(remote_dir)          
    for f in files:                  
        print ''                  
        print '#########################################'                  
        print 'Beginning to download file  from %s  %s ' % (hostname,datetime.datetime.now())                
        print 'Downloading file:',os.path.join(remote_dir,f) 
        sftp.get(os.path.join(remote_dir,f),os.path.join(local_dir,f))#下载               
        #sftp.put(os.path.join(local_dir,f),os.path.join(remote_dir,f))#上传                 
        print 'Download file success %s ' % datetime.datetime.now()        
        print ''                  
        print '##########################################'   
    t.close() 
except Exception:  
       print "connect error!"  
```
执行结果：
```shell
#########################################  
Beginning to download file  from 192.168.0.102  2012-11-05 15:49:01.334686  
Downloading file: /tmp/test/wgetrc Download file success 2012-11-05 15:49:05.955184   
##########################################   
 
#########################################  
Beginning to download file  from 192.168.0.102  2012-11-05 15:49:05.955342  
Downloading file: /tmp/test/xinetd.conf Download file success 2012-11-05 15:49:10.929568   
##########################################   
 
#########################################  
Beginning to download file  from 192.168.0.102  2012-11-05 15:49:10.929740  
Downloading file: /tmp/test/warnquota.conf Download file success 2011-12-05 15:49:14.213570   
##########################################  
```

####[ssh批量登录并执行命令的python实现代码][2]

有个任务是在这些电脑上执行某些命令，者说进行某些操作，比如安装某些软件，拷贝某些文件，批量关机等。如果一台一台得手工去操作，费时又费力，如果要进行多个操作就更麻烦啦

 

局域网内有一百多台电脑，全部都是linux操作系统，所有电脑配置相同，系统完全相同（包括用户名和密码），ip地址是自动分配的。现在有个任务是在这些电脑上执行某些命令，者说进行某些操作，比如安装某些软件，拷贝某些文件，批量关机等。如果一台一台得手工去操作，费时又费力，如果要进行多个操作就更麻烦啦。 
或许你会想到网络同传， 网络同传是什么？就是在一台电脑上把电脑装好，配置好，然后利用某些软件，如“联想网络同传”把系统原样拷贝过去，在装系统时很有用，只要在一台电脑上装好，同传以后所有的电脑都装好操作系统了，很方便。同传要求所有电脑硬件完全相同，在联想的电脑上装的系统传到方正电脑上肯定会出问题的。传系统也是很费时间的，根据硬盘大小，如果30G硬盘，100多台电脑大约要传2个多小时，反正比一台一台地安装快！但是如果系统都传完了，发现忘了装一个软件，或者还需要做些小修改，再同传一次可以，但是太慢，传两次半天时间就没了。这时候我们可以利用ssh去控制每台电脑去执行某些命令。 
先让我们回忆一下ssh远程登录的过程：首先执行命令 ssh username@192.168.1.x ，第一次登录的时候系统会提示我们是否要继续连接，我们要输入“yes”，然后等一段时间后系统提示我们输入密码，正确地输入密码之后我们就能登录到远程计算机，然后我们就能执行命令了。我们注意到这里面有两次人机交互，一次是输入‘yes'，另一次是输入密码。就是因为有两次交互我们不能简单的用某些命令去完成我们的任务。我们可以考虑把人机交互变成自动交互，python的pexpect模块可以帮我们实现自动交互。下面这段代码是用pexpect实现自动交互登录并执行命令的函数： 
复制代码 代码如下:
```python
#!/usr/bin/env python 
# -*- coding: utf-8 -*- 
import pexpect 
def ssh_cmd(ip, passwd, cmd): 
ret = -1 
ssh = pexpect.spawn('ssh root@%s "%s"' % (ip, cmd)) 
try: 
i = ssh.expect(['password:', 'continue connecting (yes/no)?'], timeout=5) 
if i == 0 : 
ssh.sendline(passwd) 
elif i == 1: 
ssh.sendline('yes\n') 
ssh.expect('password: ') 
ssh.sendline(passwd) 
ssh.sendline(cmd) 
r = ssh.read() 
print r 
ret = 0 
except pexpect.EOF: 
print "EOF" 
ssh.close() 
ret = -1 
except pexpect.TIMEOUT: 
print "TIMEOUT" 
ssh.close() 
ret = -2 
return ret 
```
利用pexpect模块我们可以做很多事情，由于他提供了自动交互功能，因此我们可以实现ftp，telnet，ssh，scp等的自动登录，还是比较实用的。根据上面的代码相信读者已经知道怎么实现了（python就是那么简单！）。 
用上面的代码去完成任务还是比较费时间的，因为程序要等待自动交互出现，另外ubuntu用ssh连接就是比较慢，要进行一系列的验证，这样才体现出ssh的安全。我们要提高效率，在最短的时间内完成。后来我发现了python里面的paramiko模块，用这个实现ssh登录更加简单。看下面的代码： 
复制代码 代码如下:
```python
#-*- coding: utf-8 -*- 
#!/usr/bin/python 
import paramiko 
import threading 
def ssh2(ip,username,passwd,cmd): 
try: 
ssh = paramiko.SSHClient() 
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy()) 
ssh.connect(ip,22,username,passwd,timeout=5) 
for m in cmd: 
stdin, stdout, stderr = ssh.exec_command(m) 
# stdin.write("Y") #简单交互，输入 ‘Y' 
out = stdout.readlines() 
#屏幕输出 
for o in out: 
print o, 
print '%s\tOK\n'%(ip) 
ssh.close() 
except : 
print '%s\tError\n'%(ip) 
if __name__=='__main__': 
cmd = ['cal','echo hello!']#你要执行的命令列表 
username = "" #用户名 
passwd = "" #密码 
threads = [] #多线程 
print "Begin......" 
for i in range(1,254): 
ip = '192.168.1.'+str(i) 
a=threading.Thread(target=ssh2,args=(ip,username,passwd,cmd)) 
a.start() 
```
上面的程序还是有些技巧的： 
1.利用多线程，同时发出登录请求，同时去连接电脑，这样速度快很多，我试了一下，如果不用多线程，直接一个一个挨着执行的话，大约5～10秒钟才能对一台电脑操作完，具体时间要根据命令的来决定，如果是软件安装或者卸载时间要更长一些。这样下来怎么也要一二十分钟，用多线程后就快多了，所有的命令执行完用了不到2分钟！ 
2.最好用root用户登录，因为安装或者卸载软件的时候如果用普通用户又会提示输入密码，这样又多了一次交互，处理起来就比较麻烦！安装软件时apt-get install xxx 最好加上“-y”参数，因为有时安装或删除软件时提示是否继续安装或卸载，这又是一次自动交互！加上那个参数后就没有人机交互了。 
3. 循环时循环所有ip，因为计算机的ip是路由器自动分配的，保险起见，最好全部都执行，保证没有遗漏的主机 
4.远端执行命令时如果有交互，可以这样用 stdin.write("Y")来完成交互，“Y”就是输入“Y”。 
5.把所有的命令放到一个列表里面，遍历列表可以依次执行列表里面的命令 
6.为了更好的进行控制，最好在电脑上提前把root用户打开，装好ssh服务器并让其开机自动执行。



####[一个用ssh来远程登录多台机器并执行命令的脚本][3]


功能类似于multissh。事实上我也抄了这个名字//grin。
要求安装了pexpect 这个包先。
用法见usage:
```shell
Usage: ./multissh.py -f cmdfile -l username -c cmd -n nodesfile -v -r
execut cmd on remote hosts (all hosts in ./hosts.txt by default)
-v verbose
-r recording hosts on which mission succeeded and failed
-l username
-c cmd to be executed remotely
-n file containing the nodes
-f file conaining the cmd
-h show the usage
```
就是指定一个文件比如nodes.txt以及命令以后，它可以自动登录 到nodes.txt包含的节点里执行命令。可以在源文件里替换进你自己的密码，也可以使用公钥密钥登录 不需输入密码。指定了v选项的话得到在远端每台主机上的详细输出。指定了r选项的话记录下那些节点成功那些节点失败。
我前面的帖子里有关于ansi_color的一个脚本，拿过来可以配合使用得到彩色输出
Python代码  
```python
  1. #!/usr/bin/python   
  2. import sys    
  3. import os    
  4. import getopt    
  5. import pexpect    
  6. try:    
  7.    from ansi_color import * #就是我前面帖子里关于ansi_color的几个定义    
  8. except ImportError:    
  9.    def color_str(s, *args):    
 10.        return s    
 11.    fg_green = None    
 12.    fg_red = None    
 13.    fg_blue = None    
 14. password="123456" #替换成你自己的密码。    
 15. def do(cmds, dst, username, outfile):    
 16.    global verbose, is_quiet, good_hosts    
 17.    print "executing \"%s\""%(repr(cmds))    
 18.    try:    
 19.        prompt = "^.*\(.*\):|\$"    
 20.        hostname = dst    
 21.        sshcmd = '<b style="color: black; background-color: rgb(153, 255, 153);">ssh</b> %s'%(hostname)    
 22.        if username != None:    
 23.            sshcmd = sshcmd + " -l %s"%username    
 24.        s = pexpect.spawn(command=sshcmd, timeout=20)    
 25.        s.logfile_read = outfile    
 26.        s.setecho(True)    
 27.        i = -1    
 28.        while (i<>0):    
 29.            i = s.expect([prompt,"Are you sure you want to continue connecting (yes/no)?","Password:"])    
 30.            if i == 1:    
 31.                s.sendline("yes")    
 32.            elif i == 2:    
 33.                s.sendline(password)    
 34.        for cmd in cmds:         
 35.            s.sendline(cmd)    
 36.            s.expect(prompt)    
 37.        s.sendline("exit")    
 38.        s.close()    
 39.        if verbose:    
 40.            print    
 41.        print "["+color_str("OK!", fg_green)+"]"    
 42.        if recording:    
 43.            print>>f_good, hostname    
 44.            f_good.flush()    
 45.        good_hosts.append(hostname)    
 46.    except pexpect.ExceptionPexpect:    
 47.        if verbose:    
 48.            print    
 49.        print "["+color_str("Fail!", fg_red)+"]"    
 50.        if recording:    
 51.            print>>f_bad, hostname    
 52.            f_bad.flush()    
 53.        bad_hosts.append(hostname)    
 54. def print_usage():    
 55.    print "Usage:\t ./make_do.py -f cmdfile -l username -c cmd -n nodesfile -v -r"    
 56.    print "execut cmd on remote hosts (all hosts in ./hosts.txt by default)"    
 57.    print "\t-v verbose"    
 58.    print "\t-r recording hosts on which mission succeeded and failed"    
 59.    print "\t-l username"    
 60.    print "\t-c cmd to be executed remotely"    
 61.    print "\t-n file containing the nodes"    
 62.    print "\t-f file conaining the cmd"    
 63.    print "\t-h show the usage"    
 64.    sys.exit(-1)    
 65. if __name__ == "__main__":    
 66.    try:    
 67.        opts, args=getopt.getopt(sys.argv[1:], "l:f:n:c:vhr",["login_name", "cmdfile","nodesfile","command","help","verbose", "recording"])    
 68.    except getopt.GetoptError, err:    
 69.        print str(err)    
 70.        print_usage()    
 71.    if opts == [] and args == []:    
 72.        print_usage()    
 73.    hosts = None    
 74.    cmds = None    
 75.    outfile = open("/dev/null", "w")    
 76.    verbose = False    
 77.    username = None    
 78.    recording = False    
 79.    for o, ra in opts:    
 80.        a = ra.strip(" \t\n")    
 81.        if o in ("-h", "--help"):    
 82.            print_usage()    
 83.        elif o in ("-n", "--nodesfile"):    
 84.            h = open(a, 'r')    
 85.            hosts = [l.strip(" \t\n") for l in h]    
 86.        elif o in ("-c", "--command"):    
 87.            cmds = [a]    
 88.        elif o in ("-f", "--cmdfile"):    
 89.            cmdfile =  open(a, "r")    
 90.            cmds = [cmd.strip(' \n') for cmd in cmdfile]    
 91.        elif o in ("-v",  "--verbose"):    
 92.            outfile = sys.stdout    
 93.            verbose = True    
 94.        elif o in ("-r", "--recording"):    
 95.            recording = True    
 96.        elif o in ("-l", "--login_name"):    
 97.            username = a    
 98.    if hosts is None:    
 99.        print "using default ./hosts.txt"    
100.        h = open(os.path.join(os.path.expanduser("."), "hosts.txt"),'r')    
101.        hosts = [dst.strip(' \n') for dst in h]    
102.    if cmds is None:    
103.        print "-c or -f must specified"    
104.        print_usage()    
105.    if recording:    
106.        f_good = open("good_hosts.txt","w")    
107.        f_bad = open("bad_hosts.txt","w")    
108.    good_hosts =[]    
109.    bad_hosts =[]    
110.    for i in range(len(hosts)):    
111.        dst = hosts[i]    
112.        print "%d/%d: ["%(i+1, len(hosts))+ color_str(dst, fg_blue)+"]"    
113.        do(cmds, dst, username, outfile)    
114.    print "%d hosts suceed!"%len(good_hosts)    
115.    outfile.close()    
116.    h.close()  
```
 另附一个邮件列表组的讨论内容：
 ```shell
python-cn邮件列表 写道
python 能不能模拟键盘输入字符，类似于TCL的post，比如在我用SSH 连接到LINUX，然后用PYTHON 来输入ls命令。急用 
pyexpect
2008/11/25 zhezh80 <zhezh80@...>:
> python能不能模拟键盘输入字符，类似于TCL的post，比如在我用SSH连接到LINUX，然后用PYTHON来输入ls命令。急用
>

你这个并非模拟键盘输入，
而仅仅是远程通过 ssh 执行命令，

请阅以前讨论过的 python-paramiko
 ```

[0]:http://www.cnblogs.com/gannan/archive/2012/02/06/2339883.html
[1]:http://blog.csdn.net/gzh0222/article/details/10591337
[2]:http://www.jb51.net/article/30397.htm
[3]:http://tinypig.iteye.com/blog/422468