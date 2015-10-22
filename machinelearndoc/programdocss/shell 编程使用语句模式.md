shell 编程使用语句模式

[toc]


##### getopts 使用方法
[传送][1]

###### 一、getopts 简介

由于shell命令行的灵活性，自己编写代码判断时，复杂度会比较高。使用内部命令 getopts 可以很方便地处理命令行参数。一般格式为：

```shell
getopts options variable
```

　getopts 的设计目标是在循环中运行，每次执行循环，getopts 就检查下一个命令行参数，并判断它是否合法。即检查参数是否以 - 开头，后面跟一个包含在 options 中的字母。如果是，就把匹配的选项字母存在指定的变量 variable 中，并返回退出状态0；如果 - 后面的字母没有包含在 options 中，就在 variable 中存入一个 ？，并返回退出状态0；如果命令行中已经没有参数，或者下一个参数不以 - 开头，就返回不为0的退出状态。
 
###### 二、 使用举例

cat args
```shell
#!/bin/bash

# 使用 getopts 读取命令参数 

while getopts h:ms option
do 
	case "$option" in
    h)
    	echo "option :h ,value $OPTARG"
        echo "next arg index: $OPTIND" ;;
        
    m) 
    	echo "option: m"
        echo "next arg index:$OPTIND" ;;
    s)
    	echo "option:s"
        echo "next arg index:$OPTIND" ;;
    \?)
    	echo "Usage: args [-h n] [-m] [-s]"
        echo "-h means hours"
        echo "-m means minutes"
        echo "-s means seconds"
        exit 1;;
        
   esac
   
done

echo "*** do something now ***"
```

命令执行：

./args -h 100 -ms

```shell
option:h, value 100
next arg index:3
option:m
next arg index:3
option:s
next arg index:4
*** do something now ***
```

./args -t
```shell
./args: illegal option -- t
Usage: args [-h n] [-m] [-s]
-h means hours
-m means minutes
-s means seconds
```

说明：

1. getopts 允许将选项堆叠在一起（ 如 -ms）
2. 如果要带参数，需要在对应选项后加 ： （如h后需要加参数 h:ms）,此时选线项和参数之间至少有一个空白字符分隔，这样的选项不能堆叠。
3. 如果在需要参数的选项之后没有找到参数，它就在给定的变量中存入 ? ，并向标准错误中写入错误消息。否则将实际参数写入特殊变量 ：OPTARG
4. 另外一个特殊变量：OPTIND，反映下一个要处理的参数索引，初值是 1，每次执行 getopts 时都会更新。


##### ssh 

###### 实现远程机器无密码验证
```shell
#!/bin/bash

scp ~/.ssh/id_dsa.pub  $1@$2:~/
ssh $1@￥2 " touch ~/.ssh/authorized_keys ; cat ~/kd_dsa.pub >> ~/.ssh/autorized_keys; chmod 644  ~/.ssh/authorized_keys; exit"
```
######  比较远程和本地的文件
```shell
$ ssh user@host cat /path/to/remotefile | diff  /path/to/localfile
```
###### 通过ssh挂载目录/文件系统
从http://fuse.sourceforge.net/sshfs.html下载sshfs，它允许你跨网络安全挂载一个目录。
```shell
$sshfs name@server:/path/to/folder  /path/to/mount/point

```

###### 通过中间主机建立SSH链接
```shell
$ ssh -t recahable_host ssh unreachable_host
```
Unreachable_host表示从本地网络无法直接访问的主机，但可以从reachable_host所在网络访问，这个命令通过到reachable_host的“隐藏”连接，创建起到unreachable_host的连接。

###### 将自己的SSH 公钥复制到远程主机， 开启五米码登录
```shell
ssh-copy-id username@hostname
```

###### 通过 ssh 链接远程屏幕
```shell
ssh -t remote_host screen -r
```
###### 通过ssh运行复杂的远程shell命令
```shell
$ssh host -l user "`cat cmd.txt`"

# 或者
ssh host -l user $(<cmd.txt)
```

###### 通过ssh将MySql 数据库复制到新的机器
```shell
$mysqldump -add-drop-table -extended-insert -force -log-error=error.log -uUSER -pPASS OLD_DB_NAME | ssh -C user@newhost "mysql -uUser -pPass NEW_DB_NAME"
```
通过压缩SSH隧道Dump一个MySql数据库， 将其作为输入传递给mysql命令，我认为是迁移数据库到新服务器最快最好的方法。


###### 删除文本文件中的一行， 修复 "SSH主机密钥更改"的警告
```shell
$sed -i 8d ~/.ssh/known_hosts
```

###### 从一台没有 SSH-COPY-ID 命令的主机将你的 SSH 公钥复制到服务器
```shell
$ cat ~/.ssh/id_rsa.pub | ssh user@machine  "mkdir ~/.ssh; cat >> ~./ssh/authorized_keys"
```

###### 在有 SSH-COPY-ID 命令的主机 一步传输公钥
```shell
$ ssh-keygen ; ssh-copy-id user@host; ssh user@host
```

###### 继续scp大文件
它可以恢复失败的rsync命令，当你通过VPN传输大文件，如备份的数据库时这个命令非常有用，需要在两边的主机上安装rsync。
```shell
$rsync -partial -progress -rsh=ssh $file_sorce $user@$host:$destination_file local 

rsync –partial –progress –rsh=ssh $file_source $user@$host:$destination_file local -> remote
或

rsync –partial –progress –rsh=ssh $user@$host:$remote_file $destination_file remote -> local
```

###### 保持ssh会话永久打开
打开一个SSH会话后，让其保持永久打开，对于使用笔记本电脑的用户，如果需要在Wi-Fi热点之间切换，可以保证切换后不会丢失连接。
```shell
autossh -M50000 -t server.example.com 'screen -raAd mysession'
```


###### shell 实现 ssh 自动登录

[传送](http://blog.pureisle.net/archives/2198.html)

mac下没有找到好用的类似secureCRT，就自己写了个自动登录的脚本，分享一下,如果是新浪的，就基本不用修改代码就直接能用。（其实是想表示虽然那么久没更新，但博主还活着~）
文件名：ssh_auto_login

```shell
#！/usr/bin/expect
############################
#
#   ssh 模拟登录器 的 expect 脚本
#
#  @author ted
#############################

if { $argc<4} {
	puts "Error params:$argv"
    puts "Expect params: user passwd ip port [translate_id]"
    exit 1
}

set default_passcode "这里是填通道机的默认密码"

set user [index $argv 0]
set password [index $argv 1]
set ip [index $argv 2]
set port [index $argv 3]
set timeout 10

while 1 {

	spawn ssh -p $port $user@$ip
    # 如果最后的字符匹配则执行命令 \r 结尾表示确定
    expect {
    	"*yes/no" {send "yes\r";exp_continue}
        "*password:" {send "$password\r"}
    }
    
    # 这里是需要通过通道机登录时的匹配流程， 根据需要自行修改
    expect {
    "*PASSCODE:" {
    	send_user "请输入通道机动态密码："
        expect_user -re "(.*)\n"
        set random_passcode $expect_out(1,string)
        send "$default_passcode$random_passcode\r"
        expect {
        	"Access Denied" {continue}
            "Enter: " {send "1\r"}
        }
        set translate_ip [index $argv 4]
        
        if { $translate_ip != "" } {
        	expect "*):" {send "$translate_ip\r"}
        }
    }
    # "Last login:*"{}
    }
    break
}

# 无法匹配$,还不知道怎么解决
#expect -re "*\$" { puts "test123" ; send "source /etc/profile\r"}
#expect "*\$" {send "cd ~ \r"}
send_user "login success!"
interact
```
上边是ssh的自动登录，可以配合下边的shell使用，很方便。
文件名：xxx_launcher

```shell
#!/bin/bash

#################################
#
#   登录服务器
#
#  @author ted
#
#################################

channel_user="user_namexxx"
channel_passwd="xxxx"

# 内网通道机
interact_ip1=xxx.xxx.xxx.xxx
# 联通
unicom_ip1=xxx.xxx.xxx.xxx
#电信
telecon_ip1=xxx.xxx.xxx.xxx

case "$1" in
	ci)
    	expect ssh_auto_login $ channel_user $channel_passwd $internal_ip3 22
        ;;
	cl)
    	expect ssh_auto_login $ channel_user $channel_passwd $unicom_ip3 22
        ;;
	cd)
    expect ssh_auto_login $ channel_user $channel_passwd $telecom_ip3 22
    ;;
    149)
    expect ssh_auto_login $ channel_user $channel_passwd xxx.xxx.xxx.xxx 22
    49)
    expect ssh_auto_login $ channel_user $channel_passwd $unicom_ip3 22 需要通道机跳转的ip xxx.xxx.xxx.xxx
    
    
    
```


##### 字符串变量操作

```shell
${name:?error message} 
 # 检查某个变量是否存在，如果不存在就输出error message
 
${var%suffix},${var#prefix}
  # 输出var变量，除了前缀或者后缀外的部分
```

##### 数据处理

相关的命令有： 
1. 排序命令：sort uniq uniq -u uniq -d
2. 文本文件的维护工具： cut paste join
3. 取交并补：

```shell
cat a b | sort | uniq > c   # c is a union b
cat a b | sort | uniq -d > c # c is intersect b
cat a b | sort | uniq -u > c # c is difference a - b
```
注意 sort 命令的 -k 参数 ：
1.  -k1 会根据整个行进行排序
2.  -k1.1 只会排序第一列

##### 系统调试命令
1. 硬盘，CPU 内存 网络的状态 ：iostat netstat top atop htop dstat 
2. free vmstat   了解内存的状态 ， 其中cache 是Linux 内核中文件缓存的大小。

gdb： 使用gdb 链接到一个正在运行的程序， 并得到其 stack trace

/proc/ 文件加:
/proc/cpuinfo  /proc/xxx/cwd  /proc/xxx/exe /proc/xxx/fd  /proc/xxx/smaps

##### tar 解压缩包的时候 使用-C选项 指定文件夹的位置
```shell
$tar xvf -C tmp/a/b/c newarc.tar.gz
```

##### 将命令与控制操作符组合使用
1. 仅当另一个命令返回0退出时才运行某个命令
 ```shell
	$ cd tmp/a/b/c && tar xvf ~/archive.tar
 ```

2. 仅当另一个命令返回非0状态时才运行某个命令
 ```shell
  $ cd tmp/a/b/c || mkdir -p tmp/a/b/c
 ```
 
3. 上面两种情况结合使用
 ```shell
 ~$ cd tmp/a/b/c || mkdir -p tmp/a/b/c && tar xvf -C tmp/a/b/c ~/archive.tar
 ```
 
##### 使用wget 和 shell 脚本下载一系列路径有规律的资源：
1. 使用wget 和 shell 脚本下载一系列路径有规律的资源：

```shell
#!/bin/basg
issue="http://dl.fullcicirclemegazine.org/issue"
country=_en

for index in `seq 1 53`
do
wget -c $issue$index$country.pdf
done
```

##### for 循环

```shell
for x in {1..3}
do
echo $x
done

for x in `seq 1 3`
do 
echo $x
done

initty=/dev/tty[1-8]
for tty in $initty
do 
echo $tty
done
```

##### shell 脚本中打印指定级别的log

```shell
#!/bin/bash

#set -x
#set -v

DEBUG=1

function log_debug() {
	if [ $DEBUG -gt 0 ];then
    	if [ $# -lt 1 ]
        then
        	echo "no message";
        else
        	echo "DEBUG:$1"
        fi
    fi
}

echo -n "Can you writte device drivers(y/n)?"
read answer
answer="echo $answer | tr [a-z] [A-Z]"
if [ answer == Y ]; then
	log_debug "call log_debug"
    echo "Wow ,you must be very skiiled!"
else
	echo "Neither can I ,I'm just an example shell script"
fi 
```
根据上面还可以写出其他级别的调试日志输出函数。
根据java中的log4j来说还有 log_info(),log_warn(),log_error(),log_fatal() 等日志输出函数


##### 文件批量复制并更名

```shell
#!/bin/bash 
#set -x

path="/home/banxi1988/work/python"
srcdir="images_test/"
dstdir="imgages_dst/"

if [ ! -d "$srcdir" ]
then
	echo "images_test 目录不存在！"
    exit 1
fi

if [ -d "dstdir" ]
then
	echo -n "$dstdir 已经存在，是否要删除（y/n）?"
    read reply
    reply=`echo $reply | tr [a-z] [A-Z]`
    if [ $reply = Y ]
    then
    	echo "正在删除目标目录..."
        rmdir "$dstdir"
        echo "$dstdir 已经删除！"
        echo "正在创建新的dstdir目录"
        mkdir "$dstdir"
        echo "$dstdir 目录创建成功"
    else
    	echo "您选择了不删除目标目录！"
    fi
fi

#  转换源文件夹下的文件
#set -x
for file in $(ls $ srcdir)
do
	#echo "文件：$file"
    
    fileSuffix=`echo $file | gawk --posix '{ print substr($1,match($1,/py[0-9]{2}/),(length($1) - 4))}'`
    filePrefix=`echo $fileSuffix | gwak '{print substr($1,0,match($1,match($1,/\./)-1))}'`
    filename=`echo $file | gawk --posix '{print substr($1,0,match($1,/py[0-9]{2}/)-2)}'`
    newFileName="${filePrefix}_$fileName.png"
    #echo "newFileName:$newFileName"
#set -x
	cp "$srcdir$file" "$dstdir$newFileName"
#set +x
done
```
注意： awk 默认是不支持 {} 的正则的， 所以要开启 --posix 或者 --re-internal  选项才可以。

在使用变量时除了使用 $varName的这种方式以外， 还可以使用 ${varName}这种方式。

##### 使用关联数组
[传送_关联](http://blog.chinaunix.net/uid-20671208-id-3595390.html)
[传送_切片](http://www.cnblogs.com/chengmo/archive/2010/09/30/1839632.html)
###### 1. 数组的定义
```shell
$a=(1 2 3 4 5)
$echo $a
1
```

###### 2.数组的读取与赋值
```shell
#1. 得到长度
echo ${#a[@]}
5

#2. 读取
echo ${a[2]}
3

#3. 以子符串的形式得到所有的值
echo ${a[*]}

#4. 以数组的形式得到所有的值
echo ${a[@]}

#5. 赋值 直接通过 数组名[下标] 就可以对其进行引用赋值，如果下标不存在，自动添加新一个数组元素
a[5]=100

# 6. 删除
a=(1 2 3 4 5)
unset a
echo ${a[*]

a=(1 2 3 4 5)
unset a[1]
echo ${a[*]
echo ${#a[*]}
```

###### 3. 特殊使用方法
1. 直接通过 ${数组名[@或*]:起始位置:长度} 切片原先数组，返回是字符串，中间用“空格”分开，因此如果加上”()”，将得到切片数组，上面例子：c 就是一个新数据。

```shell
# 1. 分片
a=(1 2 3 4 5)
echo ${a[@]:0:3}
1 2 3

echo ${a[@]:1:4}
2 3 4 5

c=(${a[@]:1:4})
echo ${#c[@]}

echo ${c[*]}
2 3 4 5
```

2. 替换
调用方法是：${数组名[@或*]/查找字符/替换字符} 该操作不会改变原先数组内容，如果需要修改，可以看上面例子，重新定义数据。

```shell
a=(1 2 3 4 5)
echo ${a[@]/3/100}
1 2 100 4 5
a=(${a[@]/3/100})
echo ${a[@]}

```

###### 关联数组
所谓关联数组就是使用字符串作为索引值对数组进行访问的数组；
1. 关联数组的声明
```shell
declare -A arrayName
```
2. 关联数组初始化与元素添加
 * 使用 "[index]=value" 列表的方式对关联数组添加元素：
   ```shell
   declare -A assArray1;
   assArray1=([index1]=cat [index2]=dogs [index98]=fish);
   ```
 * 使用 "arrayName[index]=value" 的方式对关联数组进行添加元素：
   ```shell
    declare -A assArray2;
    assArray2[pear]=100;
    assArray2[apple]=3000;
    assArray2[orange]=70000;
   ```
3. 关联数组的访问：
  3.1 关联数组的元素访问
   关联数组使用 "arrayName[index]" 的方式访问数组元素
   ```shell
   ${arrayName[index]}
   ```
  3.2 遍历关联数组的所有元素
   ```shell
    ${arrayName[*]}
    ${arrayName[@]}
   ```
  3.3 获取关联数组的元素个数
   ```shell
    ${#arrayName[*]}
    ${#arrayName[@]}
   ```
  3.4 获取关联数组所有可访问元素的索引值
   ```shell
    ${!arrayName[*]}
    ${!arrayName[@]}
   ```
   
 
  
##### ubuntu下如何用命令行运行deb安装包

转自：http://hi.baidu.com/xiboliya/blog/item/fee581d46cf5e41fa08bb7fb.html
```shell
如果ubuntu要安装新软件，已有deb安装包（例如：iptux.deb），但是无法登录到桌面环境。那该怎么安装？答案是：使用dpkg命令。
dpkg命令常用格式如下：
sudo dpkg -I iptux.deb#查看iptux.deb软件包的详细信息，包括软件名称、版本以及大小等（其中-I等价于--info）
sudo dpkg -c iptux.deb#查看iptux.deb软件包中包含的文件结构（其中-c等价于--contents）
sudo dpkg -i iptux.deb#安装iptux.deb软件包（其中-i等价于--install）
sudo dpkg -l iptux#查看iptux软件包的信息（软件名称可通过dpkg -I命令查看，其中-l等价于--list）
sudo dpkg -L iptux#查看iptux软件包安装的所有文件（软件名称可通过dpkg -I命令查看，其中-L等价于--listfiles）
sudo dpkg -s iptux#查看iptux软件包的详细信息（软件名称可通过dpkg -I命令查看，其中-s等价于--status）
sudo dpkg -r iptux#卸载iptux软件包（软件名称可通过dpkg -I命令查看，其中-r等价于--remove）
注：dpkg命令无法自动解决依赖关系。如果安装的deb包存在依赖包，则应避免使用此命令，或者按照依赖关系顺序安装依赖包。
```

##### 日期操作 date
#####显示当前日期
显示当前时间
命令：
* date
* date '+%c'
* date '+%D'
* date '+%x'
* date '+%T'
* date '+%X'

#####时间的加减
######使用 --date 参数进行加减
* date +%Y%m%d   //显示当前年月日
* date +%Y%m%d --date="+1 day"   //显示后一天的日期
* date +%Y%m%d --date="-1 day"   //显示前一天的日期
* date +%Y%m%d --date="-1 month"  //显示上一个月的日期
* date +%Y%m%d --date="+1 month"  //显示下一个月的日期
* date +%Y%m%d --date="-1 year"   //显示上一年的日期
* date +%Y%m%d --date="+1 year"   //显示下一年的日期
* date +%Y%m%d-%H%M%S --date="+1 hour"
* date +%Y%m%d-%H%M%S --date="+1 minute"
* date +%Y%m%d-%H%M%S --date="+30 second"

######使用 -d参数进行计算
* date -d '-100 days'
* date -d '-100 days' +%y%m
* date -d '-100 days' +%Y%m
* date -d '100 days' +%Y%m
* date -d next-day +%Y%m%d
* date -d tomorrow +%Y%m%d
* date -d last-day +%Y%m%d
* date -d yesterday +%Y%m%d
* date -d last-month +%Y%m
* date -d '30 days ago'
* date -d 'dec 14 -2 weeks'


说明：

date 命令的另一个扩展是 -d 选项，该选项非常有用。使用这个功能强大的选项，通过将日期作为引号括起来的参数提供，您可以快速地查明一个特定的日期。-d 选项还可以告诉您，相对于当前日期若干天的究竟是哪一天，从现在开始的若干天或若干星期以后，或者以前（过去）。通过将这个相对偏移使用引号括起来，作为 -d 选项的参数，就可以完成这项任务。
具体说明如下：
* date -d "nov 22"  今年的 11 月 22 日是星期三
* date -d '2 weeks' 2周后的日期
* date -d 'next monday' (下周一的日期)
* date -d next-day +%Y%m%d（明天的日期）或者：date -d tomorrow +%Y%m%d
* date -d last-day +%Y%m%d(昨天的日期) 或者：date -d yesterday +%Y%m%d
* date -d last-month +%Y%m(上个月是几月)
* date -d next-month +%Y%m(下个月是几月)
使用 ago 指令，您可以得到过去的日期：
* date -d '30 days ago' （30天前的日期）
使用负数以得到相反的日期：
* date -d 'dec 14 -2 weeks' （相对:dec 14这个日期的两周前的日期）
* date -d '-100 days' (100天以前的日期)
* date -d '50 days'(50天后的日期)
######对一个给定的日期变量 进行日期计算
* date -d 'dec 14 -2 weeks' +%Y%m
* date -d 'dec 14 -2 weeks' +%Y%m%d

* ```shell
	DATE=`date -d last-day +%Y%m%d`
    echo $DATE
    date -d "$DATE -20 days" -%Y%m%d
    last_hour=3
    date -d "$DATE -$last_hour hours" +%Y%m%d-%H%M%S
    
    ```




##### sudo 的使用技巧


###### 一、概述

[传送](http://os.51cto.com/art/201010/229477.htm)
现在，许多Linux用户都熟悉sudo。Ubuntu在普及sudo上做了不少工作，强迫而非鼓励用户转换到root帐号安装软件和执行其他管理任务。但是关于sudo，还有许多是用户和管理员应当知晓的。

许多用户不甚知晓的是sudo可以用来以任何用户身份执行命令，而不单是root用户。在经验丰富的管理员手里，sudo可用来建立细粒度的权限，授予用户执行一些管理任务的权限却不用洞门大开。让我们来看看利用sudo控制系统访问权限且用户依然能保有效率的一些最佳实践吧。

** 记住，你必须使用visudo命令来编辑/etc/sudoers文件。**

授予信任的用户以完全访问权限，让他们能以任何用户身份执行任何命令，这个办法看上去既诱人又简单。请将这种诱惑拒之门外，因为你想要将访问权限限制到可能的最低限度。

1. 限定帐号切换
只要有一丝可能，就不要配置sudo允许用户切换到其他帐号。作为替代，可尝试配置sudo允许用户以他们需要用来操作的用户身份执行特定命令。例如，用户需要安装软件了，可允许他们以root用户身份运行RPM或APT，但不用转换为root用户。

2. 不要使用ALL
最常见的错误就是授予ALL权限——这意味着可以访问所有命令、访问所有用户，或者访问其它任何权限排列。虽然权限锁定耗时费力，但这样的麻烦值得一受。

3. 分割sudoers
如果有许多系统要管理，又不想复制同样的/etc/sudoers文件到所有系统，那么可以将sudoers文件分割成几块，并用特定的sudo配置调用include文件。例如，如果想在管理Apache和MySQL时使用同一套指令，就可以分出一个独立的sudo.mysql文件，并使用include指令从主sudoers文件调用它。

4. 善用组
如果可能，按组授权，而不是按单个用户授权。例如，有一个admin组具有管理软件包和更新的管理特权。在这种情况下，用不着每增加或删除一个用户就编辑sudoers文件——只需确保在admin组中对该用户合适地管理和增加/删除就行了。

5. 超时设置
确保有合适的超时设置。太短的话，用户会很快感到灰心丧气。好办法是设置为5分钟左右。

6. 最从正确路径
通过在sudo中指定secure_path指令锁定二进制文件的路径——确保用户不能在secure_path之外执行命令。

7. 将日志记录到其他文件
默认情况下，sudo可能将日志与其它系统消息一同记录在一个普通的messages日志文件中。对单用户系统如Ubuntu桌面而言，这是可接受的方案，但是对服务器也这么做可不妙。配置sudo以使其拥有自己的日志文件，这样sudo的使用和sudoers的变化更为透明。

8. 哪些地方不可以使用sudo命令

是的，sudo是个强大的工具，但是要配置好它不容易，而且难于维护。如果有经验的管理员在系统不多的情况下使用，它是实现基于角色访问控制的完备方法。不过如果是更大型的企业，拥有数十名IT人员和几十乃至几百服务器的情形，sudo的权限很快就展露无遗。可以使用其它工具为sudo提供支持。一种方法是**使用配置框架如Puppet来跨多系统管理sudo配置**。对那些主要基于Linux和Unix的企业来说这会是特别有效的，尽管Puppet的学习曲线可能有点陡峭。

倘若企业已经在混合了Linux和Windows服务器的网络里部署了Microsoft Active Directory（活动目录），那么也可以使用Likewise Enterprise将Linux和Unix系统纳入Active Directory管理。这样不仅可将Linux和Unix登录和Active Directory可信网络挂钩，而且也能在网络中对所有服务器管理sudo配置。

可以找到其他工具来协助补充sudo，以提供更为健壮的特权用户管理。重要的一点是评估网络并确定是否sudo独力就能满足需要。对于小企业来说，sudo往往够好了——如果能按照最佳实践来做并充分了解sudo配置的话。如果未能正确管理sudo，几乎比简单地共享root信任权还要糟，因为它提供的是错误的安全感。懂得怎么使用sudo，并按照这些最佳实践来做，那么你就可以去悠哉游哉，大快朵颐了。


###### 使用sudo命令为ubuntu分配管理权限

[传送](http://os.51cto.com/art/201109/288814.htm)

ubuntu系统与windows系统在使用习惯上有很大的区别，可能刚开始使用的用户还很不习惯。在ubuntu系统上有些命令普通用户没有权限运行的，而sudo命令是指用管理员ROOT运行这个命令，下文介绍的是利用sudo命令为Ubuntu分配管理权限的方法。

Ubuntu有一个与众不同的特点，那就是初次使用时，你无法作为root来登录系统，为什么会这样？这就要从系统的安装说起。对于其他Linux系统来说，一般在安装过程就设定root密码，这样用户就能用它登录root帐户或使用su命令转换到超级用户身份。与之相反，Ubuntu默认安装时，并没有给root用户设置口令，也没有启用root帐户。问题是要想作为root用户来运行命令该怎么办呢？没关系，我们可以使用sudo命令达此目的。


sudo是linux下常用的允许普通用户使用超级用户权限的工具，该命令为管理员提供了一种细颗粒度的访问控制方法，通过它人们既可以作为超级用户又可以作为其它类型的用户来访问系统。这样做的好处是，管理员能够在不告诉用户root密码的前提下，授予他们某些特定类型的超级用户权限，这正是许多系统管理员所梦寐以求的。

###### 1. sudo的缺省配置

默认时，Ubuntu为sudo提供了一个基本的配置，该配置保存在/etc目录下的sudoers文件中。**在修改该配置文件时，务必使用visudo工具来进行编辑，因为该工具会自动对配置语法进行严格检查，如果发现错误，在保存退出时给出警告，并提示你哪段配置出错，从而确保该配置文件的正确性**。相反，如果使用其它的文本编辑程序的话，一旦出错，就会给系统带来严重的后果。下面给出的是Ubuntu默认的/etc/sudoers文件内容：

```shell
#Userprivilegespecification字串7

root ALL=(ALL)ALL

#Member soft head min group may gain root privileges

%admin ALL=(ALL)ALL 字串6
```

**下面对以上配置做简要说明**：

 1. 第一项配置的作用，是允许root用户使用sudo命令变成系统中任何其它类型的用户。
 2. 第二个配置规定，管理组中的所有成员都能以root的身份执行所有命令。

因此，在默认安装的Ubuntu系统中，要想作为root身份来执行命令的话，只要在sudo后面跟上欲执行的命令即可。下面用一个例子加以说明，如果您想执行apt-getupdate的话，应当在命令行中键入以下内容：

```shell
$sudo apt-get update 字串1
```

######  2. 配置文件语法详解

接下来，我们用一个实例来详细解释/etc/sudoers文件的配置语法，请看下面的例子：

```shell
jorge ALL=(root) /usr/bin/find,/bin/rm 
```

上面的第一栏规定它的适用对象：用户或组，就本例来说，它是用户jorge。此外，因为系统中的组和用户可以重名，要想指定该规则的适用对象是组而非用户的话，**组对象的名称一定要用百分号%开头**。

第二栏指定该规则的适用主机。当我们在多个系统之间部署sudo环境时，这一栏格外有用，这里的ALL代表所有主机。但是，对于桌面系统或不想将sudo部署到多个系统的情况，这一栏就换成相应的主机名。

第三栏的值放在括号内，指出第一栏规定的用户能够以何种身份来执行命令。本例中该值设为root，这意味着用户jorge能够以root用户的身份来运行后面列出的命令。该值也可以设成通配符ALL，jorge便能作为系统中的任何用户来执行列出的命令了。

最后一栏（即/usr/bin/find,/bin/rm）是使用逗号分开的命令表，这些命令能被第一栏规定的用户以第三栏指出的身份来运行它们。本例中，该配置允许jorge作为超级用户运行/usr/bin/find和/bin/rm这两个命令。需要指出的是，这里列出的命令一定要使用绝对路径。

###### 3. sudo命令的使用方法

现在的问题是，用户jorge怎样利用分配给他的权限呢？其实很简单，只要在命令行模式下使用sudo命令字串1

加上他想运行的程序就可以了，比如：

```shell
jorge@ubuntu:~$sudo find . !-name '*.avi' -exec rm-f \\\\{\\\\}\\\\;
```

倘若jorge企图执行/etc/sudoers文件规定之外的程序（比如find或rm）的话，sudo命令便会以失败而告终，并给出警告信息，指出他无权以超级用户身份来运行这些命令。

要想以非root用户身份来运行命令，必须使用-u选项来指定想要作为的用户；否则的话，sudo会默认为root用户，比如要想以fred身份来执行ls命令，就应该这样：

```shell
$sudo -u fred ls /home/fred
```

就像您看到的那样，我们可以利用这些规则为系统创建具体的角色。例如，要让一个组负责帐户管理，你一方面不想让这些用户具备完全的root访问权限，另一方面还得让他们具有增加和删除用户的权利，那么我们可以在系统上创建一个名为accounts的组，然后把那些用户添加到这个组里。之后，再使用visudo为/etc/sudoers添加下列内容：

```shell
%account All=(root) /usr/sbin/useradd,/usr/sbin/userdel,/usr/sbin/usrmod
```

现在好了，accounts组中的任何成员都能运行useradd、userdel和usermod命令了。如果过一段时间后，您发现该角色还需要其他工具，只要在该表的尾部将其添上就行了。这样真是方便极了！

**注意：（黑客后门方法）**
需要注意的是，当我们为用户定义可以运行的命令时，必须使用完整的命令路径。这样做是完全出于安全的考虑，如果我们给出的命令只是简单的userad而非/usr/sbin/useradd，那么用户有可能创建一个他自己的脚本，也叫做userad，然后放在它的本地路径中，如此一来他就能够通过这个名为useradd的本地脚本，作为root来执行任何他想要的命令了。这是相当危险的！

###### 配置免密码使用
sudo命令的另一个便捷的功能，是它能够指出哪些命令在执行时不需要输入密码。这很有用，尤其是在非交互式脚本中以超级用户的身份来运行某些命令的时候。例如，想要让用户作为超级用户不必输入密码就能执行kill命令，以便用户能立刻杀死一个失控的进程。为此，**在命令行前边加上NOPASSWD:属性**即可。例如，可以在/etc/sudoers文件中加上下面一行，从而让jorge获得这种权力：

```shell
jorge ALL=(root) NOPASSWD:/bin/kill,/usr/bin/killall
```

这样一来，jorge就能运行以下命令，作为root用户来杀死失控的rm进程了。
```shell
jorge@ubuntu:~$sudokillallrm字串8
```

###### 如何启用root 账户

通过以上介绍，我们发现sudo的确很好用，但是如果您早就习惯了在root下工作，想回味一下过去的感觉该怎么办呢？很简单，只要为root设置一个root密码就行了：

```shell
$sudo passwd root 
```
好了，现在您能直接作为root登录了。

######  后记

1. 为什么 sudo 比另一个办法好
sudo是提升权限的最出色、最安全的方法。
我们不妨看一下提升权限的另一个方法。作为切换用户命令，**"su"会要求你输入根密码，并且给你一个超级用户提示符，以#符号表示**。这个#符号意味着"危险！你已作根用户登录上去！"你下达的第一个命令也许顺利执行完毕。但是你一旦忘了，会继续以根用户身份登录。要是打错一个字，就完蛋了！你清除了整个硬驱，而不是清除你下载的那个盗版mp3文件。你的Web服务器和家庭公司统统不见了！

如果是sudo，你就得在每一个命令之前输入"sudo"。因而，你没必要记得切回到常规用户模式，那样发生的事故就会更少。

我们试着用sudo重启。**sudo要求你提供用户密码。请注意：它要求你提供的是你的密码，而不是根密码**。

2. suderos 文件

这个文件可谓是sudo的基础。它控制着谁可以使用sudo命令来获得提升的权限。它通常位于/etc/sudoers。想编辑这个文件，最有效最安全的方式就是，使用visudo命令。这个命令会以提升权限启动vi编辑器，那样你就能编辑并保存该文件。它还会给sudoers文件上文件锁，那样别人无法编辑该文件。一旦你完成了编辑工作，它会分析文件，查找有无简单的错误。编辑sudo文件要比仅仅使用任何旧的文本编辑器来得安全得多。

该文件含有许多参数。你可以指定哪些用户或哪些用户助可以执行哪些命令。我们准备为自己授予访问sudo的权限，为此只要在底部添加：

```shell
username  ALL=(ALL)   ALL //为用户"username"授予sudo访问权 
%wheel    ALL=(ALL)   ALL //为属于wheel用户组的所有用户授予sudo访问权 
```

3. 几个选项

与任何优秀的命令一样，也有几个很棒的选项可以让sudo处理更多的事务。

 *   sudo -b会在后台运行命令。这对显示许多实时输出内容的命令来说很有用。
 *   sudo -s 会运行以提升权限指定的外壳，为你提供#提示符（别忘了退出！）
 *   sudo su -会让你成为根用户，并装入你那些自定义的用户环境变量。



##### 高效linux 操作 命令串集合

###### 1. 查找java 项目中 ，main方法定义在那个文件中

```shell
 grep -r|I " \<main " .
```

使用find 实现

```shell
 find -type f -name *.java -exec grep -l "\<main" {} \;
```

###### 2. 快速的建立一个文件，并输入一些内容
```shell
cat >> filename  # 输入一些内容后按 Ctrl + d 结束
```
###### 3. 将文本文件中的DOS中的换行符转化为UNIX的换行符
```shell
tr -s "\r" "\n" <inputfile
```
如果要保存到 outputfile 文件
```shell
tr -s "\r" "\n" < inputfile > outputfile
```

###### 4. 查看某年 某月是星期几
```shell
cal 5 208
```

###### 5. 使用python 实现 局域网内的文件共享
一般来说类linux系统之间的文件共享通常使用nfs,而linux
和Windows文件共享则使用samba,这两个服务器功能强大，但是使用起来免不了要配置一番
如果你的机器上装了python可以使用python自带的http服务器
进入你要共享文件的目录执行
```shell
python -m SimpleHTTPServer
```
对方只要在浏览器里输入你的IP地方加8000端口(例如192.168.1.104:8000)就可以看到共享目录下文件。

###### 6. 用最简单的命令杀死一个进程
以前为了结束一个进程通常是 ps -aux | grep xxx

然后再查看该进程的ID,最后是 kill 进程ID。

如果你管道用的很熟，awk又会用一点，则有可能打下如此拉风的命令

```shelll
ps ax | grep firefox | grep -v grep | awk '{print $1}' | xargs kill -9
```

其实你没必要使用那么多的管道加awk，有个很简单的命令pkill

执行
```shell
pkill -9 firefox 
```
就终结firefox进程。


#####  实用的网络流量监控脚本

```shell
 #!/bin/bash  
 2 if [ -n "$1" ]; then  
 3     eth_name=$1  
 4 else  
 5     eth_name="eth0" 
 6 fi  
 7 i=0 
 8 send_o=`/sbin/ifconfig $eth_name | grep bytes | awk '{print $6}' | awk -F : '{print $2}'`  
 9 recv_o=`/sbin/ifconfig $eth_name | grep bytes | awk '{print $2}' | awk -F : '{print $2}'`  
10 send_n=$send_o  
11 recv_n=$recv_o  
12 while [ $i -le 100000 ]; do  
13     send_l=$send_n  
14     recv_l=$recv_n  
15 sleep 1  
16 send_n=`/sbin/ifconfig $eth_name | grep bytes | awk '{print $6}' | awk -F : '{print $2}'`  
17 recv_n=`/sbin/ifconfig $eth_name | grep bytes | awk '{print $2}' | awk -F : '{print $2}'`  
18 i=`expr $i + 1`  
19 send_r=`expr $send_n - $send_l`  
20 recv_r=`expr $recv_n - $recv_l`  
21 total_r=`expr $send_r + $recv_r`  
22 send_ra=`expr \( $send_n - $send_o \) / $i`  
23 recv_ra=`expr \( $recv_n - $recv_o \) / $i`  
24 total_ra=`expr $send_ra + $recv_ra`  
25 sendn=`/sbin/ifconfig $eth_name | grep bytes | awk -F \( '{print $3}' | awk -F \) '{print $1}'`  
26 recvn=`/sbin/ifconfig $eth_name | grep bytes | awk -F \( '{print $2}' | awk -F \) '{print $1}'`  
27 clear  
28 echo  "Last second  :   Send rate: $send_r Bytes/sec  Recv rate: $recv_r Bytes/sec  Total rate: $total_r Bytes/sec"  
29 echo  "Average value:   Send rate: $send_ra Bytes/sec  Recv rate: $recv_ra Bytes/sec  Total rate: $total_ra Bytes/sec"  
30 echo  "Total traffic after startup:    Send traffic: $sendn  Recv traffic: $recvn"  
31 done
```

[1]:http://www.cnblogs.com/xiangzi888/archive/2012/04/03/2430736.html