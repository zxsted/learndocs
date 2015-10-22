[系列传送](http://codingstandards.iteye.com/blog/1112967)
[toc]
#####date命令用法
######用法说明
date命令可以用来显示和修改系统日期时间，注意不是time命令。
######常用参数
* 格式：date
显示当前日期时间
格式：date mmddHHMM
格式：date mmddHHMMYYYY
格式：date mmddHHMM.SS
格式：date mmddHHMMYYYY.SS
设置当前日期时间，只有root用户才能执行，执行完之后还要执行 clock -w 来同步到硬件时钟。
mm为月份，dd为日期，HH为小时数，MM为分钟数，YYYY为年份，SS为秒数。
* 日期转字符串 
格式：date +FORMAT
根据指定格式显示当前时间。比如 date +%Y-%m-%d 就是以 YYYY-mm-dd 的形式显示当前日期，其中YYYY是年份，mm为月份，dd为日期。
常用format：
%Y  YYYY格式的年份（Year）
%m  mm格式的月份（），01-12
%d   dd格式的日期（day of month），01-31
%H   HH格式的小时数（），00-23
%M  MM格式的分钟数（），00-59
%S   SS格式的秒数（），00-59
%F   YYYY-mm-dd格式的完整日期（Full date），同%Y-%m-%d
%T   HH-MM-SS格式的时间（Time），同%H:%M:%S
%s   自1970年以来的秒数。C函数time(&t) 或者Java中 System.currentTimeMillis()/1000, new Date().getTime()/1000
%w   星期几，0-6，0表示星期天
%u   星期几，1-7，7表示星期天
注意以上格式是可以任意组合的，还可以包括非格式串，比如 date "+今天是%Y-%d-%m，现在是$H:%M:%S"
更多格式 man date 或 info date
* 字符串转时间
  格式：date -d STRING
格式：date --date=STRING
格式：date -d STRING +FORMAT
显示用STRING指定的日期时间（display time described by STRING, not ‘now’）。
* 使用字符串设置日期
格式：date -s STRING
格式：date --set=STRING
设置当前时间为STRING指定的日期时间。
STRING可谓变化多样，支持很多种日期时间的描述方式。下面列举一些常用的日期表示方式，希望能够举一反三。
######指定日期：
date -d YYYY-mm-dd
指定时间，日期是今天：
date -d HH:MM:SS
指定日期时间：
date -d "YYYY-mm-dd HH:MM:SS"
指定1970年以来的秒数：
date -d '1970-01-01 1251734400 sec utc'      （2009年 09月 01日 星期二 00:00:00 CST）
date -d '1970-01-01 1314177812 sec utc'      （2011年 08月 24日 星期三 17:23:32 CST）
######今天：
date
date -d today
date -d now
######明天：
date -d tomorrow
date -d next-day
date -d next-days
date -d "next day"
date -d "next days"
date -d "+1 day"
date -d "+1 days"
date -d "1 day"
date -d "1 days"
date -d "-1 day ago"
date -d "-1 days ago"
######昨天：
date -d yesterday
date -d last-day
date -d last-days
date -d "last day"
date -d "last days"
date -d "-1 day"
date -d "-1 days"
date -d "1 day ago"
date -d "1 days ago"
######前天：
date -d "2 day ago"
date -d "2 days ago"
date -d "-2 day"
date -d "-2 days"
######大前天：
date -d "3 day ago"
date -d "3 days ago"
date -d "-3 day"
date -d "-3 days"
######上周，一周前：
date -d "1 week ago"
date -d "1 weeks ago"
######上个星期五（不是上周五）：
date -d "last-friday"
date -d "last friday"
######上月，一月前：
date -d last-month
date -d last-months
date -d "-1 month"
date -d "-1 months"
######下月，一月后：
date -d next-month
date -d next-months
date -d "+1 month"
date -d "+1 months"
######去年，一年前：
date -d last-year
date -d last-years
date -d "-1 year"
date -d "-1 years"
######明年，一年后：
date -d next-year
date -d next-years
date -d "+1 year"
date -d "+1 years"
######一小时前：
date -d "last-hour"
date -d "last-hours"
date -d "1 hour ago"
date -d "1 hours ago"
######一小时后：
date -d "1 hour"
date -d "1 hours"
######一分钟前：
date -d "1 minute ago"
date -d "1 minutes ago"
######一分钟后：
date -d "1 minute"
date -d "1 minutes"
######一秒前：
date -d "1 second ago"
date -d "1 seconds ago"
######一秒后：
date -d "1 second"
date -d "1 seconds"

######使用示例：
* 示例一 显示和设置日期时间
```shell
[root@node56 ct08]# date 
2011年 08月 20日 星期六 17:37:11 CST
[root@node56 ct08]# date 08220942 
2011年 08月 22日 星期一 09:42:00 CST
[root@node56 ct08]# clock -w 
[root@node56 ct08]# date 
2011年 08月 22日 星期一 09:42:01 CST
[root@node56 ct08]#
```
* 示例二 显示指定日期时间
```shell
[root@node56 ~]# date 
2011年 08月 23日 星期二 07:41:03 CST
[root@node56 ~]# date -d next-day +%Y%m%d 
20110824
[root@node56 ~]# date -d next-day +%F 
2011-08-24
[root@node56 ~]# date -d next-day '+%F %T' 
2011-08-24 07:41:47
[root@node56 ~]# date -d last-day '+%F %T' 
2011-08-22 07:43:46
[root@node56 ~]# 
[root@node56 ~]# date -d yesterday '+%F %T' 
2011-08-22 07:44:31
[root@node56 ~]# date -d tomorrow '+%F %T' 
2011-08-24 07:45:19
[root@node56 ~]# date -d last-month +%Y%m 
201107
[root@node56 ~]# date -d next-month +%Y%m 
201109
[root@node56 ~]# date -d next-year +%Y 
2012
[root@node56 ~]#
```
* 示例三 写一个脚本来计算母亲节和父亲节的日期
母亲节（每年5月的第二个星期日 ）
　　2005年5月8日
　　2006年5月14日
　　2007年5月13日
　　2008年5月11日
　　2009年5月10日
　　2010年5月9日
　　2011年5月8日
　　2012年5月13日
父亲节（6月第三个星期日），下面是最近几年的父亲节日期
　　2005年6月19日
　　2006年6月18日
　　2007年6月17日
　　2008年6月15日
　　2009年6月21日
　　2010年6月20日
　　2011年6月19日
　　2012年6月17日
下面的脚本用来计算指定年份的母亲节和父亲节（calc_date.sh）：
```shell
#!/bin/sh

#母亲节（每年5月第二个星期日）
#usage:monther_day[year]
monther_day()
{
	local may1 #5月1日   #定义函数的局部变量，否则是全局可见的
    if [ "$1" ]; then
    	may1=$1-05-01   #也可以是$1/05/01
    else
    	may1=5/1   #也可以是 05/01，但不能是 05-01
    fi
    #date -d $may1
    #看5月1日是星期几，
    local w=(date +may1)   # %w 0=星期天 1-6=星期一到星期六
    #echo $w
    if [ $w -eq 0 ]; then  #如果5月1日是星期天，就跳过一个星期
    	date +%F -d "$may1 +1 week"
    else #如果5月1日不是星期天，就跳过2个星期，再减去W天
    	date +%F -d "may1 +2week - w day"
    fi
}

#父亲节（每年6月的第三个星期日）
#usage:father_day[year]
father_day()
{
	local june1    #保存6月1日的日期
    if[ "$1" ]; then
    	june1=$1-06-01
    else
    	june1=6/1
    fi
    #因为采用1-7表示星期几，简化了计算逻辑
    local w=(date+june1) #%u 7=星期天，1-6=星期一到六
    date +%F -d "june1+3week -w day"
}

#usag:./calc_date.sh[year]
if [ "$1" ];then
	echo Monther Day of year $1 is (mother day "1")
    echo Fatcher Day of year $1 is (father day "1")
else
	echo Monther Day of this year is $(mother_day)
    echo Father Day of this year is $(father_day)
fi
```
执行结果：
```shell
[root@node56 ~]# ./calc_date.sh 
Mother Day of this year is 2011-05-08
Father Day of this year is 2011-06-19
[root@node56 ~]# ./calc_date.sh 2011 
Mother Day of year 2011 is 2011-05-08
Father Day of year 2011 is 2011-06-19
[root@node56 ~]# ./calc_date.sh 2010 
Mother Day of year 2010 is 2010-05-09
Father Day of year 2010 is 2010-06-20
[root@node56 ~]# ./calc_date.sh 2009 
Mother Day of year 2009 is 2009-05-10
Father Day of year 2009 is 2009-06-21
[root@node56 ~]# ./calc_date.sh 2008 
Mother Day of year 2008 is 2008-05-11
Father Day of year 2008 is 2008-06-15
[root@node56 ~]# ./calc_date.sh 2007 
Mother Day of year 2007 is 2007-05-13
Father Day of year 2007 is 2007-06-17
[root@node56 ~]# ./calc_date.sh 2006 
Mother Day of year 2006 is 2006-05-14
Father Day of year 2006 is 2006-06-18
[root@node56 ~]# ./calc_date.sh 2005 
Mother Day of year 2005 is 2005-05-08
Father Day of year 2005 is 2005-06-19
[root@node56 ~]# ./calc_date.sh 2012 
Mother Day of year 2012 is 2012-05-13
Father Day of year 2012 is 2012-06-17
```

* 示例四：查看某年某月日历的形式：
```shell
[root@node56 ~]# cal 5 2012 
     五月 2012      
日 一 二 三 四 五 六
       1  2  3  4  5
 6  7  8  9 10 11 12
13 14 15 16 17 18 19
20 21 22 23 24 25 26
27 28 29 30 31
```