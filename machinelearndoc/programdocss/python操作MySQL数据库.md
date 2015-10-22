
####[python操作mysql](http://blog.csdn.net/ssw_1990/article/details/23367905)
pythonmysqlmysqldb增删改查database

1、如果操作系统是ubuntu，则直接sudo apt-get install python-mysqldb，安装完成之后可以在python解释器中测试一下，输入python如下代码：import MySQLdb，如果不报错，则证明安装成功。


2、用python建立数据库

[python] view plaincopy在CODE上查看代码片派生到我的代码片
```python
    import MySQLdb    
      
    try:  
        # 建立和数据库系统的连接    
        conn = MySQLdb.connect(host='localhost', user='root', passwd='root', port=3306)    
        # 获取操作游标    
        cursor = conn.cursor()    
        # 执行SQL，创建一个数据库   
        cursor.execute('create database python')    
        # 关闭连接，释放资源    
        cursor.close()  
        conn.close()  
    except MySQLdb.Error, e:  
            print 'Mysql Error %d: %s' % (e.args[0], e.args[1])   

```
3、创建表，插入更新一条数据，插入多条数据

[python] view plaincopy在CODE上查看代码片派生到我的代码片
```python
    import MySQLdb    
      
    try:  
        # 建立和数据库系统的连接    
        conn = MySQLdb.connect(host='localhost', user='root', passwd='root', port=3306)    
        # 获取操作游标    
        cursor = conn.cursor()    
        # 执行SQL，创建一个数据库  
        cursor.execute('create database if not exists python')    
        # 选择数据库    
        conn.select_db('python')  
        # 执行SQL，创建一个数据表  
        cursor.execute('create table test(id int, info varchar(100))')    
        value = [1, 'hi python']  
        # 插入一条记录    
        cursor.execute('insert into test values(%s, %s)', value)  
      
        values = []          
        # 生成插入参数值    
        for i in range(20):    
                values.append((i, 'Hello mysqldb, I am recoder' + str(i)))    
        # 插入多条记录    
        cursor.executemany('insert into test values(%s, %s)', values)  
        # 更新数据  
        cursor.execute('update test set info='Hello mysqldb, I am pythoner' where id=3')  
        # 提交事务  
        conn.commit()  
        # 关闭连接，释放资源    
        cursor.close()  
        conn.close()  
    except MySQLdb.Error, e:  
            print 'Mysql Error %d: %s' % (e.args[0], e.args[1])   

```
4、查询数据，删除数据

[python] view plaincopy在CODE上查看代码片派生到我的代码片
```python
    import MySQLdb    
    try:  
        conn = MySQLdb.connect(host='localhost', user='root', passwd='root', db='python', port=3306)    
        cursor = conn.cursor()    
        count = cursor.execute('select * from test')    
        print '总共有%s条记录' % count  
          
        # 获取一条记录，每条记录做为一个元组返回    
        print '只获取一条记录:'    
        result = cursor.fetchone()  
        print result      
        print 'ID: %s info: %s' % result     
            
        # 获取五条记录，注意由于之前执行了fetchone()，所以游标已经指到第二条记录，即从第二条开始的所有记录    
        print '只获取五条记录:'    
        results = cursor.fetchmany(5)    
        for r in results:    
            print r    
      
        # 删除一条记录  
        cursor.execute('delete from test where id=0')  
      
        print '获取所有结果:'    
        # 重置游标位置，0为偏移量，mode＝absolute | relative，默认为relative    
        cursor.scroll(0, mode='absolute')    
        # 获取所有结果    
        results = cursor.fetchall()    
        for r in results:    
            print r    
        conn.commit()  
        cursor.close()  
        conn.close()   
    except MySQLdb.Error, e:  
             print 'Mysql Error %d: %s' % (e.args[0], e.args[1])  
```
说明：

charset属性根据需要自行指定，否则中文出现乱码现象。


####[python操作MySQL数据库](http://www.cnblogs.com/rollenholt/archive/2012/05/29/2524327.html)

坚持每天学一点，每天积累一点点，作为自己每天的业余收获，这个文章是我在吃饭的期间写的，利用自己零散的时间学了一下python操作MYSQL，所以整理一下。

我采用的是MySQLdb操作的MYSQL数据库。先来一个简单的例子吧：

```python	
import MySQLdb
 
try:
    conn=MySQLdb.connect(host='localhost',user='root',passwd='root',db='test',port=3306)
    cur=conn.cursor()
    cur.execute('select * from user')
    cur.close()
    conn.close()
except MySQLdb.Error,e:
     print "Mysql Error %d: %s" % (e.args[0], e.args[1])
```
　　请注意修改你的数据库，主机名，用户名，密码。

下面来大致演示一下插入数据，批量插入数据，更新数据的例子吧：

```python	
import MySQLdb
 
try:
    conn=MySQLdb.connect(host='localhost',user='root',passwd='root',port=3306)
    cur=conn.cursor()
     
    cur.execute('create database if not exists python')
    conn.select_db('python')
    cur.execute('create table test(id int,info varchar(20))')
     
    value=[1,'hi rollen']
    cur.execute('insert into test values(%s,%s)',value)
     
    values=[]
    for i in range(20):
        values.append((i,'hi rollen'+str(i)))
         
    cur.executemany('insert into test values(%s,%s)',values)
 
    cur.execute('update test set info="I am rollen" where id=3')
 
    conn.commit()
    cur.close()
    conn.close()
 
except MySQLdb.Error,e:
     print "Mysql Error %d: %s" % (e.args[0], e.args[1])
```
　　请注意一定要有conn.commit()这句来提交事务，要不然不能真正的插入数据。

运行之后我的MySQL数据库的结果就不上图了。

```python	
import MySQLdb
 
try:
    conn=MySQLdb.connect(host='localhost',user='root',passwd='root',port=3306)
    cur=conn.cursor()
     
    conn.select_db('python')
 
    count=cur.execute('select * from test')
    print 'there has %s rows record' % count
 
    result=cur.fetchone()
    print result
    print 'ID: %s info %s' % result
 
    results=cur.fetchmany(5)
    for r in results:
        print r
 
    print '=='*10
    cur.scroll(0,mode='absolute')
 
    results=cur.fetchall()
    for r in results:
        print r[1]
     
 
    conn.commit()
    cur.close()
    conn.close()
 
except MySQLdb.Error,e:
     print "Mysql Error %d: %s" % (e.args[0], e.args[1])
```
　　运行结果就不贴了，太长了。

查询后中文会正确显示，但在数据库中却是乱码的。经过我从网上查找，发现用一个属性有可搞定：

在Python代码 
```python
conn = MySQLdb.Connect(host='localhost', user='root', passwd='root', db='python') 中加一个属性：
 改为：
conn = MySQLdb.Connect(host='localhost', user='root', passwd='root', db='python',charset='utf8') 
```
charset是要跟你数据库的编码一样，如果是数据库是gb2312 ,则写charset='gb2312'。

 

下面贴一下常用的函数：

然后,这个连接对象也提供了对事务操作的支持,标准的方法
```python
commit() 提交
rollback() 回滚

cursor用来执行命令的方法:
callproc(self, procname, args):用来执行存储过程,接收的参数为存储过程名和参数列表,返回值为受影响的行数
execute(self, query, args):执行单条sql语句,接收的参数为sql语句本身和使用的参数列表,返回值为受影响的行数
executemany(self, query, args):执行单挑sql语句,但是重复执行参数列表里的参数,返回值为受影响的行数
nextset(self):移动到下一个结果集

cursor用来接收返回值的方法:
fetchall(self):接收全部的返回结果行.
fetchmany(self, size=None):接收size条返回结果行.如果size的值大于返回的结果行的数量,则会返回cursor.arraysize条数据.
fetchone(self):返回一条结果行.
scroll(self, value, mode='relative'):移动指针到某一行.如果mode='relative',则表示从当前所在行移动value条,如果 mode='absolute',则表示从结果集的第一行移动value条.
```