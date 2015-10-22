linux里通过shell调用SQL脚本的例子，用m4动态传入参数
```shell
#!/bin/sh

#set environment
#****************
passwd="leopard"
maxsize=20
nowtime=$(date +%Y%m%d%H%M%S.%N)
workdir=/u02/app/script/shell
alertfile=$workdir/"alert.log"
echo $nowtime >> $alertfile
echo "------------------" >> $alertfile
workfile=''

#*Main Procedure
#*moniter system informations ,such as cpu/io/memory
#****************************************
workfile=$workdir/"buff.sys."$workfile$nowtime
echo $nowtime > $workfile
echo "--------------------" >> $workfile


#获取sql的函数
fetch_sql(){
m4 -DBIGBEAR="$spid" q_topN.sql > $tmpN     #将参数传到临时文件中
sqlplus bigbear/$passwd @$tmpN | grep '^\#' | sed -n '2,$p' | while read line
do 
	##,b.sql_id,b.sql_address,b.sid,b.serial#
    sqlid=$(echo $line | awk '{print $2}')
    sqladdr=$(echo $line | awk '{print $3}')
    sid=$(echo $line | awk '{print $4}')
    serial=$(echo $line | awk '{print $5}')
    echo "$line"
done
rm -f $tmpN
}

tmpN=$workdir/q_topN.sql.$nowtime  

top -n 1 -u oracle -b | sed -n '8,10p' | awk '{print $1,$9}' | while read line
do
	spid=$(echo $line | awk '{print $1}')
    percent=$(echo $line | awk '{print $2}' | awk -F. '{print $1}')
    if [["$percent" -le "90"]]; then
    	fech_sql"$spid"
    fi
done

```    
显示 q_topN.sql的内容

```shell
-cat q_topN.sql

SET ECHO OFF
SET LINESIZE 60
col sql_id format a10
col aql_address format a14
col sid a14
col serial #a14

select '##' as "##",b.sql_id,b.sql_address,b.sid,b.serial#
from v$process a, v$session b
where a.addr = b.paddr
  and a.spid = BIGBEAR
/

quit

```





























```