####python下paramiko模块学习

#####之一：ssh登录和执行命令

   最近闲着学习python，看到有个paramiko模块，貌似很强大，学着写了个一个小程序，可以直接在window下登录到linux，执行并返回执行结果。

   下面直接贴代码：

 ```python

    #!/usr/bin/env python 
    import paramiko 
     
    #hostname='192.168.0.102' 
    hostname='172.28.102.250' 
    username='root' 
    password='abc' 
     
    #port=22 
    if __name__=='__main__': 
            paramiko.util.log_to_file('paramiko.log') 
            s=paramiko.SSHClient() 
            #s.load_system_host_keys() 
            s.set_missing_host_key_policy(paramiko.AutoAddPolicy()) 
            s.connect(hostname = hostname,username=username, password=password) 
            stdin,stdout,stderr=s.exec_command('ifconfig;free;df -h') 
            print stdout.read() 
            s.close() 
```
下面看下效果，呵呵：

 
```shell
    s\501914252.HCA-FW9CX2X\Desktop\学习资料\python paramiko-ssh.py" 
    eth0      Link encap:Ethernet  HWaddr 00:24:01:01:EA:5C 
              inet addr:172.32.34.240  Bcast:172.32.255.255  Mask:255.255.255.0 
              UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1 
              RX packets:1428 errors:0 dropped:0 overruns:0 frame:0 
              TX packets:2 errors:0 dropped:0 overruns:0 carrier:0 
              collisions:0 txqueuelen:1000 
              RX bytes:190928 (186.4 KiB)  TX bytes:128 (128.0 b) 
              Interrupt:20 
     
    eth1      Link encap:Ethernet  HWaddr 00:1F:29:03:54:3D 
              inet addr:172.28.102.250  Bcast:172.28.255.255  Mask:255.255.0.0 
              UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1 
              RX packets:99698 errors:0 dropped:0 overruns:0 frame:0 
              TX packets:12305 errors:0 dropped:0 overruns:0 carrier:0 
              collisions:0 txqueuelen:0 
              RX bytes:36420324 (34.7 MiB)  TX bytes:923630 (901.9 KiB) 
     
    lo        Link encap:Local Loopback 
              inet addr:127.0.0.1  Mask:255.0.0.0 
              UP LOOPBACK RUNNING  MTU:16436  Metric:1 
              RX packets:0 errors:0 dropped:0 overruns:0 frame:0 
              TX packets:0 errors:0 dropped:0 overruns:0 carrier:0 
              collisions:0 txqueuelen:0 
              RX bytes:0 (0.0 b)  TX bytes:0 (0.0 b) 
     
    peth1     Link encap:Ethernet  HWaddr FE:FF:FF:FF:FF:FF 
              UP BROADCAST RUNNING NOARP  MTU:1500  Metric:1 
              RX packets:99686 errors:0 dropped:0 overruns:0 frame:0 
              TX packets:12333 errors:0 dropped:0 overruns:0 carrier:0 
              collisions:0 txqueuelen:1000 
              RX bytes:36818715 (35.1 MiB)  TX bytes:1006230 (982.6 KiB) 
              Interrupt:20 Memory:f3000000-f3010000 
     
    vif0.1    Link encap:Ethernet  HWaddr FE:FF:FF:FF:FF:FF 
              UP BROADCAST RUNNING NOARP  MTU:1500  Metric:1 
              RX packets:12305 errors:0 dropped:0 overruns:0 frame:0 
              TX packets:99702 errors:0 dropped:0 overruns:0 carrier:0 
              collisions:0 txqueuelen:0 
              RX bytes:923630 (901.9 KiB)  TX bytes:36420811 (34.7 MiB) 
     
    vif1.0    Link encap:Ethernet  HWaddr FE:FF:FF:FF:FF:FF 
              UP BROADCAST RUNNING NOARP  MTU:1500  Metric:1 
              RX packets:5 errors:0 dropped:0 overruns:0 frame:0 
              TX packets:85175 errors:0 dropped:542 overruns:0 carrier:0 
              collisions:0 txqueuelen:500 
              RX bytes:140 (140.0 b)  TX bytes:20377326 (19.4 MiB) 
     
    vif2.0    Link encap:Ethernet  HWaddr FE:FF:FF:FF:FF:FF 
              UP BROADCAST RUNNING NOARP  MTU:1500  Metric:1 
              RX packets:5 errors:0 dropped:0 overruns:0 frame:0 
              TX packets:84859 errors:0 dropped:411 overruns:0 carrier:0 
              collisions:0 txqueuelen:500 
              RX bytes:140 (140.0 b)  TX bytes:20301953 (19.3 MiB) 
     
    vif3.0    Link encap:Ethernet  HWaddr FE:FF:FF:FF:FF:FF 
              UP BROADCAST RUNNING NOARP  MTU:1500  Metric:1 
              RX packets:5 errors:0 dropped:0 overruns:0 frame:0 
              TX packets:84337 errors:0 dropped:546 overruns:0 carrier:0 
              collisions:0 txqueuelen:500 
              RX bytes:140 (140.0 b)  TX bytes:20174994 (19.2 MiB) 
     
    virbr0    Link encap:Ethernet  HWaddr 00:00:00:00:00:00 
              inet addr:192.168.122.1  Bcast:192.168.122.255  Mask:255.255.255.0 
              UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1 
              RX packets:0 errors:0 dropped:0 overruns:0 frame:0 
              TX packets:0 errors:0 dropped:0 overruns:0 carrier:0 
              collisions:0 txqueuelen:0 
              RX bytes:0 (0.0 b)  TX bytes:0 (0.0 b) 
     
    xenbr1    Link encap:Ethernet  HWaddr FE:FF:FF:FF:FF:FF 
              UP BROADCAST RUNNING NOARP  MTU:1500  Metric:1 
              RX packets:86405 errors:0 dropped:0 overruns:0 frame:0 
              TX packets:0 errors:0 dropped:0 overruns:0 carrier:0 
              collisions:0 txqueuelen:0 
              RX bytes:19498121 (18.5 MiB)  TX bytes:0 (0.0 b) 
     
                 total       used       free     shared    buffers     cached 
    Mem:       3359744     464724    2895020          0      31096     246296 
    -/+ buffers/cache:     187332    3172412 
    Swap:      5406712          0    5406712 
    Filesystem            Size  Used Avail Use% Mounted on 
    /dev/mapper/VolGroup00-LogVol00 
                          221G   25G  185G  12% / 
    /dev/sda1              99M   22M   73M  23% /boot 
    tmpfs                 1.8G     0  1.8G   0% /dev/shm 
    none                  1.8G  152K  1.8G   1% /var/lib/xenstored 
```
 代码很简单，大家有兴趣，可以试验下，希望对您的学习和工作有帮助。
 
##### 之二：利用配置文件登录批量主机

   之前我写过一篇关于python中paramiko模块简单功能的文章，今天继续给大家介绍这个模块的一些用法。

   今天主要是利用python读取配置文件来登录批量主机，并在主机上执行shell命令，废话不说了，直接上代码了，大家可以看看：
```python
    #!/usr/bin/env python 
    import paramiko 
    import os 
    import datetime 
    from ConfigParser import ConfigParser 
    ConfigFile='config.ini' 
    config=ConfigParser() 
    config.read(ConfigFile) 
    hostname1=''.join(config.get('IP','ipaddress')) 
    address=hostname1.split(';') 
    print address 
    username='root' 
    password='abc123' 
    port=22 
    local_dir='/tmp/' 
    remote_dir='/tmp/test/' 
    if __name__=="__main__": 
            for ip in address: 
                    paramiko.util.log_to_file('paramiko.log') 
                    s=paramiko.SSHClient() 
                    s.set_missing_host_key_policy(paramiko.AutoAddPolicy()) 
                    s.connect(hostname=ip,username=username,password=password) 
                    stdin,stdout,stderr=s.exec_command('free;ifconfig;df -h') 
                    print stdout.read() 
                    s.close() 
```
下面再贴上config.ini配置文件内容：
```shell
    [IP] 
     

    ipaddress = 74.63.229.*;69.50.220.* 
```
     

下面给大家看下效果：
```shell
 

    [root@centos6 python]# clear 
    [root@centos6 python]# python paramiko-config.py 
    ['74.63.229.*', '69.50.220.*'] 
                 total       used       free     shared    buffers     cached 
    Mem:        393216      22308     370908          0          0          0 
    -/+ buffers/cache:      22308     370908 
    Swap:            0          0          0 
    lo        Link encap:Local Loopback 
              inet addr:127.0.0.1  Mask:255.0.0.0 
              inet6 addr: ::1/128 Scope:Host 
              UP LOOPBACK RUNNING  MTU:16436  Metric:1 
              RX packets:14 errors:0 dropped:0 overruns:0 frame:0 
              TX packets:14 errors:0 dropped:0 overruns:0 carrier:0 
              collisions:0 txqueuelen:0 
              RX bytes:956 (956.0 B)  TX bytes:956 (956.0 B) 
     
    venet0    Link encap:UNSPEC  HWaddr 00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00 
              UP BROADCAST POINTOPOINT RUNNING NOARP  MTU:1500  Metric:1 
              RX packets:36498 errors:0 dropped:0 overruns:0 frame:0 
              TX packets:36433 errors:0 dropped:0 overruns:0 carrier:0 
              collisions:0 txqueuelen:0 
              RX bytes:8698019 (8.2 MiB)  TX bytes:5322427 (5.0 MiB) 
     
    venet0:0  Link encap:UNSPEC  HWaddr 00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00 
              inet addr:74.63.229.*  P-t-P:74.63.229.56  Bcast:0.0.0.0  Mask:255.255.255.255 
              UP BROADCAST POINTOPOINT RUNNING NOARP  MTU:1500  Metric:1 
     
    Filesystem            Size  Used Avail Use% Mounted on 
    /dev/simfs             10G  408M  9.7G   4% / 
    tmpfs                 192M     0  192M   0% /lib/init/rw 
    tmpfs                 192M     0  192M   0% /dev/shm 
     
                 total       used       free     shared    buffers     cached 
    Mem:        262144     154120     108024          0      50948      62668 
    -/+ buffers/cache:      40504     221640 
    Swap:       262136          0     262136 
    eth0      Link encap:Ethernet  HWaddr 00:16:3E:27:61:01 
              inet addr:69.50.220.*  Bcast:69.50.223.255  Mask:255.255.240.0 
              UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1 
              RX packets:43755717 errors:0 dropped:0 overruns:0 frame:0 
              TX packets:79002 errors:0 dropped:0 overruns:0 carrier:0 
              collisions:0 txqueuelen:1000 
              RX bytes:3003027220 (2.7 GiB)  TX bytes:39705224 (37.8 MiB) 
     
    lo        Link encap:Local Loopback 
              inet addr:127.0.0.1  Mask:255.0.0.0 
              UP LOOPBACK RUNNING  MTU:16436  Metric:1 
              RX packets:0 errors:0 dropped:0 overruns:0 frame:0 
              TX packets:0 errors:0 dropped:0 overruns:0 carrier:0 
              collisions:0 txqueuelen:0 
              RX bytes:0 (0.0 b)  TX bytes:0 (0.0 b) 
     
    Filesystem            Size  Used Avail Use% Mounted on 
    /dev/sda1              15G  4.7G  9.4G  33% / 
    none                  128M     0  128M   0% /dev/shm 
     
    [root@centos6 python]# 
```
   呵呵，代码十分简单，我在linux虚拟机下执行没有问题，上面的2个实体ip是我的2个VPS，登录和执行都很快，但是在window下却报错，比较困惑，也没有深究，希望有知道的同仁指点下。

   这个例子这个例子十分简单，大家可以扩张下，在运维工作中，我们管理的机器可不止2台吧，你就可以你管理的服务器IP都写在配置文件里，中间又分号隔开就可以了，还有一点就是你可能执行的命令也有很多，其实也可以写在配置文件里，而不必像我这样写死在代码里面，其他扩展功能大家可以自己去看看

 

本文出自 “你是路人甲还是霍元甲” 博客，请务必保留此出处http://world77.blog.51cto.com/414605/706999


#####之三：上传批量文件到远程主机

   今天我继续给大家介绍paramiko这个模块的其他功能，主要介绍利用paramiko来上传文件到远程主机，呵呵，其实就是paramiko模块中put方法的介绍，下面不废话，直接上代码，大家感兴趣，可以看下：

 ```shell

    [root@centos6 python]# clear 
    [root@centos6 python]# cat paramiko-upload.py
```
```python
    #!/usr/bin/env python 
    import paramiko 
    import os 
    import datetime 
    hostname='74.63.229.*' 
    username='root' 
    password='abc123' 
    port=22 
    local_dir='/tmp/' 
    remote_dir='/tmp/test/' 
    if __name__=="__main__": 
     #    try: 
            t=paramiko.Transport((hostname,port)) 
            t.connect(username=username,password=password) 
            sftp=paramiko.SFTPClient.from_transport(t) 
    #        files=sftp.listdir(dir_path) 
            files=os.listdir(local_dir) 
            for f in files: 
                    print '' 
                    print '#########################################' 
                    print 'Beginning to upload file %s ' % datetime.datetime.now() 
                    print 'Uploading file:',os.path.join(local_dir,f) 
     
                   # sftp.get(os.path.join(dir_path,f),os.path.join(local_path,f)) 
                    sftp.put(os.path.join(local_dir,f),os.path.join(remote_dir,f)) 
     
                    print 'Upload file success %s ' % datetime.datetime.now() 
                    print '' 
                    print '##########################################' 
     
         #except Exception: 
    #       print "error!" 
            t.close() 
     
    [root@centos6 python]# 
```
下面给大家演示下效果：

 
```shell
    [root@centos6 python]# python paramiko-upload.py 
     
    ######################################### 
    Beginning to upload file 2011-10-15 15:02:51.453422 
    Uploading file: /tmp/ipt.out 
    Upload file success 2011-10-15 15:02:53.051348 
     
    ########################################## 
     
    ######################################### 
    Beginning to upload file 2011-10-15 15:02:53.051500 
    Uploading file: /tmp/ipt.err 
    Upload file success 2011-10-15 15:02:54.304115 
     
    ########################################## 
    [root@centos6 python]# 
```
 呵呵，效果还是不错的吧，不过在本地文件夹中，机local_dir下面包含文件夹的会报错，大家如果在工作中项使用的话，需要注意这点，希望本文能给你的学习和工作带来帮助，相信大家在平时的运维工作中还是需要这个功能的，呵呵。。。。

     

   注意：本文同步在我的个人独立博客www.50rescue.com, python的百科残书上同步发布。。。


#####之四：从远程主机批量下载文件到本机

   前面我们已经学习了paramiko的上传功能，这里就要给大家介绍下他的下载功能，呵呵，不废话了，直接上代码，感兴趣的可以研究下：
```shell
    [root@centos6 python]# cat paramiko-download.py 
```
```python
    #!/usr/bin/env python 
    import paramiko 
    import os 
    import datetime 
    hostname='74.63.229.*' 
    username='root' 
    password='abc123' 
    port=22 
    local_dir='/tmp/' 
    remote_dir='/tmp/test/' 
    if __name__=="__main__": 
     #    try: 
            t=paramiko.Transport((hostname,port)) 
            t.connect(username=username,password=password) 
            sftp=paramiko.SFTPClient.from_transport(t) 
    #        files=sftp.listdir(dir_path) 
            files=sftp.listdir(remote_dir) 
            for f in files: 
                    print '' 
                    print '#########################################' 
                    print 'Beginning to download file  from %s  %s ' % (hostname,datetime.datetime.now()) 
                    print 'Downloading file:',os.path.join(remote_dir,f) 
     
                    sftp.get(os.path.join(remote_dir,f),os.path.join(local_dir,f)) 
                   # sftp.put(os.path.join(local_dir,f),os.path.join(remote_dir,f)) 
     
                    print 'Download file success %s ' % datetime.datetime.now() 
                    print '' 
                    print '##########################################' 
     
         #except Exception: 
    #       print "error!" 
            t.close() 
     
    [root@centos6 python]# 
```
    呵呵，代码和前面上传功能稍有区别，这里就不写注释了，我的变量名都是和直观的就能让你明白意思了，哈哈，下面看下演示功能吧，看下效果：

 
```shell
    [root@centos6 python]# clear 
    [root@centos6 python]# python paramiko-download.py 
     
    ######################################### 
    Beginning to download file  from 74.63.229.*  2011-11-05 15:49:01.334686 
    Downloading file: /tmp/test/wgetrc 
    Download file success 2011-11-05 15:49:05.955184 
     
    ########################################## 
     
    ######################################### 
    Beginning to download file  from 74.63.229.*  2011-11-05 15:49:05.955342 
    Downloading file: /tmp/test/xinetd.conf 
    Download file success 2011-11-05 15:49:10.929568 
     
    ########################################## 
     
    ######################################### 
    Beginning to download file  from 74.63.229.*  2011-11-05 15:49:10.929740 
    Downloading file: /tmp/test/warnquota.conf 
    Download file success 2011-11-05 15:49:14.213570 
     
    ########################################## 
```
   呵呵，效果还是不错的，至此，paramiko的上传下载都已经介绍完 了，呵呵，下面讲虾米内容呢，千万别走开，精彩内容继续为你放松，下一次，我将为你介绍和前面讲过的，读取配置文件，上传批量文件到多部服务器，敬请关注。。。
   



#####之五：批量主机上传文件

  今天我继续为大家介绍如何利用paramiko模块给批量主机上传文件，其实之前都介绍过了，无非就是把一些零碎的东西拼在一起，呵呵，就是利用python读取配置文件里面的批量主机的IP地址，然后分别给他们上传文件，下面不废话了，直接上代码了：

 
```shell
    [root@centos6 python]# vi paramiko-sftp-mulit-upload.py 
```
```python
    #!/usr/bin/env python 
    import paramiko 
    import os 
    import datetime 
    from ConfigParser import ConfigParser 
    ConfigFile='config.ini' 
    config=ConfigParser() 
    config.read(ConfigFile) 
    hostname1=''.join(config.get('IP','ipaddress')) 
    address=hostname1.split(';') 
    print address 
    username='root' 
    password='itpschina123' 
    port=22 
    local_dir='/tmp/' 
    remote_dir='/tmp/test/' 
    if __name__=="__main__": 
     #    try: 
            for ip in address: 
                     t=paramiko.Transport((ip,port)) 
                     t.connect(username=username,password=password) 
                     sftp=paramiko.SFTPClient.from_transport(t) 
    #                files=sftp.listdir(dir_path) 
                     files=os.listdir(local_dir) 
                     print files 
                     for f in files: 
                            print '####################################################' 
                            print 'Begin to upload file  to %s ' % ip 
                            print 'Uploading ',os.path.join(local_dir,f) 
     
                            print datetime.datetime.now() 
                            sftp.put(os.path.join(local_dir,f),os.path.join(remote_dir,f)) 
                            print datetime.datetime.now() 
                            print '####################################################' 
                     t.close() 
```
下面是config.ini的配置文件内容：
```shell
    [IP] 
    #ipaddress = 192.168.0.102;192.168.0.103 
    ipaddress = 74.63.229.*;69.50.220.* 
```
   呵呵，感兴趣的话，去试验下吧，代码都贴出来了，如果前面几期运行都很很顺利的话，这次也没有问题的，毕竟我都是在我的虚拟机上验证过的，如果有问题，请留言。。。
