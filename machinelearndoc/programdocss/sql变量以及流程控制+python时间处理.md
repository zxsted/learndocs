[toc]
####[SQL语句中的条件判断](http://www.cnblogs.com/ZetaChow/archive/2010/05/28/2237334.html)CASE

做了这么多年开发，不怕笑话，还真的很少用CASE语句，毕竟很多判断实际上都是在逻辑层就做好了

不过，CASE用起来还是非常方便好用的。

小例子：

判断Status参数值和当前字段值哪个大，当前字段值小就更改，否则就不更改。
```sql
update [Records] set Status = (CASE WHEN Status < @Status THEN @Status ELSE Status END) where GUID=@GUID
```
 

下面是转载的CASE的资料，SQL的联机丛书里也有

 

Case具有两种格式。简单Case函数和Case搜索函数。
* 简单Case函数
```sql
CASE sex
         WHEN '1' THEN '男'
         WHEN '2' THEN '女'
ELSE '其他' END
```
* Case搜索函数
```sql
CASE WHEN sex = '1' THEN '男'
         WHEN sex = '2' THEN '女'
ELSE '其他' END
```

这两种方式，可以实现相同的功能。简单Case函数的写法相对比较简洁，但是和Case搜索函数相比，功能方面会有些限制，比如写判断式。
还有一个需要注意的问题，Case函数只返回第一个符合条件的值，剩下的Case部分将会被自动忽略。
* 比如说，下面这段SQL，你永远无法得到“第二类”这个结果
```sql
CASE WHEN col_1 IN ( 'a', 'b') THEN '第一类'
         WHEN col_1 IN ('a')       THEN '第二类'
ELSE'其他' END
```
下面我们来看一下，使用Case函数都能做些什么事情。

#####一，已知数据按照另外一种方式进行分组，分析。

有如下数据:(为了看得更清楚，我并没有使用国家代码，而是直接用国家名作为Primary Key)
```sql
国家（country）    人口（population）
中国    600
美国    100
加拿大    100
英国    200
法国    300
日本    250
德国    200
墨西哥    50
印度    250
```
根据这个国家人口数据，统计亚洲和北美洲的人口数量。应该得到下面这个结果。
```sql
洲    人口
亚洲    1100
北美洲    250
其他    700
```
想要解决这个问题，你会怎么做？生成一个带有洲Code的View，是一个解决方法，但是这样很难动态的改变统计的方式。
如果使用Case函数，SQL代码如下:
```sql
SELECT  SUM(population),
        CASE country
                WHEN '中国'     THEN '亚洲'
                WHEN '印度'     THEN '亚洲'
                WHEN '日本'     THEN '亚洲'
                WHEN '美国'     THEN '北美洲'
                WHEN '加拿大'  THEN '北美洲'
                WHEN '墨西哥'  THEN '北美洲'
        ELSE '其他' END
FROM    Table_A
GROUP BY CASE country
                WHEN '中国'     THEN '亚洲'
                WHEN '印度'     THEN '亚洲'
                WHEN '日本'     THEN '亚洲'
                WHEN '美国'     THEN '北美洲'
                WHEN '加拿大'  THEN '北美洲'
                WHEN '墨西哥'  THEN '北美洲'
        ELSE '其他' END;
```
同样的，我们也可以用这个方法来判断工资的等级，并统计每一等级的人数。SQL代码如下；
```sql
SELECT
        CASE WHEN salary <= 500 THEN '1'
             WHEN salary > 500 AND salary <= 600  THEN '2'
             WHEN salary > 600 AND salary <= 800  THEN '3'
             WHEN salary > 800 AND salary <= 1000 THEN '4'
        ELSE NULL END salary_class,
        COUNT(*)
FROM    Table_A
GROUP BY
        CASE WHEN salary <= 500 THEN '1'
             WHEN salary > 500 AND salary <= 600  THEN '2'
             WHEN salary > 600 AND salary <= 800  THEN '3'
             WHEN salary > 800 AND salary <= 1000 THEN '4'
        ELSE NULL END;
```
#####二，用一个SQL语句完成不同条件的分组。

有如下数据
```sql
国家（country）    性别（sex）    人口（population）
中国    1    340
中国    2    260
美国    1    45
美国    2    55
加拿大    1    51
加拿大    2    49
英国    1    40
英国    2    60
```
按照国家和性别进行分组，得出结果如下
```sql
国家    男    女
中国    340    260
美国    45    55
加拿大    51    49
英国    40    60
```
普通情况下，用UNION也可以实现用一条语句进行查询。但是那样增加消耗(两个Select部分)，而且SQL语句会比较长。
下面是一个是用Case函数来完成这个功能的例子
```sql
SELECT country,
       SUM( CASE WHEN sex = '1' THEN
                      population ELSE 0 END),  --男性人口
       SUM( CASE WHEN sex = '2' THEN
                      population ELSE 0 END)   --女性人口
FROM  Table_A
GROUP BY country;
```
这样我们使用Select，完成对二维表的输出形式，充分显示了Case函数的强大。

#####三，在Check中使用Case函数。

在Check中使用Case函数在很多情况下都是非常不错的解决方法。可能有很多人根本就不用Check，那么我建议你在看过下面的例子之后也尝试一下在SQL中使用Check。
下面我们来举个例子
公司A，这个公司有个规定，女职员的工资必须高于1000块。如果用Check和Case来表现的话，如下所示
```sql
CONSTRAINT check_salary CHECK
           ( CASE WHEN sex = '2'
                  THEN CASE WHEN salary > 1000
                        THEN 1 ELSE 0 END
                  ELSE 1 END = 1 )
```
如果单纯使用Check，如下所示
```sql
CONSTRAINT check_salary CHECK
           ( sex = '2' AND salary > 1000 )
```
女职员的条件倒是符合了，男职员就无法输入了。 


####ＳＱＬ中的变量


局部变量是可以保存单个特定类型数据值的对象，变量的作用域从声明变量的地方开始到声明变量的批处理或存储过程的结尾。

局部变量使用DECLARE语句定义，并且指定变量的数据类型，然后可以使用SET或SELECT语句为变量初始化；局部变量必须以“@”开头，而且必须先声明后使用。
声明格式：
```sql
DECLARE @变量名 变量类型[,@变量名 变量类型…]
```
 1.  变量名必须以 at 符 (@) 开头

 2. 变量类型可以是任何系统提供的公共语言运行时 (CLR) 用户定义类型或别名数据类型。变量不能是 text、ntext 或 image 数据类型。

 3. 要声明多个局部变量，请在定义的第一个局部变量后使用一个逗号，然后指定下一个局部变量名称和数据类型。

 如： 
```sql
DECLARE @LastName nvarchar(30), @FirstName nvarchar(20), @StateProvince nchar(2);
```
 4. 声明变量时，其值设置为 NULL。

 5. 在批处理中可声明的局部变量的最大值是 10,000。

 6. Ｔansact-SQL 系统函数的名称以两个 at 符号 (@@) 打头。早期版本中，@@functions 被称为全局变量，但它们不是变量，也不具备变量的行为。@@functions 是系统函数，它们的语法遵循函数的规则。
初始化：

局部变量不能使用“变量=变量值”的格式进行初始化，必须使用SELECT或SET语句来设置其初始值（SET 语句是为变量赋值的首选方法）。
初始化格式：
```sql
SELECT @局部变量=变量值

SET @局部变量=变量值
```
如果在单个 SELECT 语句中有多个赋值子句，则 SQL Server 不保证表达式求值的顺序，只有当赋值之间有引用时才能看到影响。

如果 SELECT 语句返回多行而且变量引用一个非标量表达式，则变量被设置为结果集最后一行中表达式的返回值。

例如，在此批处理中将 @EmpIDVariable 设置为返回的最后一行的 EmployeeID 值，此值为 1：
```sql
USE AdventureWorks;
GO
DECLARE @EmpIDVariable int;

SELECT @EmpIDVariable = EmployeeID
FROM HumanResources.Employee
ORDER BY EmployeeID DESC;

SELECT @EmpIDVariable;
GO
```

####SQL 控制流程

Transact-SQL 提供了(BEGIN...END、BREAK、GOTO、CONTINUE、IF...ELSE、WHILE、RETURN、WAITFOR)控制流关键字，用于控制 Transact-SQL 语句、语句块、用户定义函数以及存储过程的执行流。

不使用控制流语言，则各 Transact-SQL 语句按其出现的顺序分别执行。控制流语言使用与程序设计相似的构造使语句得以互相连接、关联和相互依存。

当控制流语句不能跨多个批处理、用户定义函数或存储过程。
#####一、 BEGIN...END

BEGIN 和 END 语句用于将多个 Transact-SQL 语句组合为一个逻辑块(即相当于C语言中{}的功能)。

例
```sql
IF (@@ERROR <> 0)
BEGIN
   SET @ErrorSaveVariable = @@ERROR
   PRINT 'Error encountered, ' +
         CAST(@ErrorSaveVariable AS VARCHAR(10))
END
```
注意：

 1. BEGIN 和 END 语句块必须至少包含一条 Transact-SQL 语句。

 2. BEGIN 和 END 语句必须成对使用，任何一个均不能单独使用。BEGIN、 END 必须单独出现在一行中。
BEGIN 和 END 语句用于下列情况：

    （1）WHILE 循环需要包含语句块。
    （2）CASE 函数的元素需要包含语句块。
    （3）IF 或 ELSE 子句需要包含语句块。

#####二、GOTO

GOTO 语句使 Transact-SQL 批处理的执行跳至标签。不执行 GOTO 语句和标签之间的语句。使用下列语法定义标签名：
```sql
label_name:
```
尽量少使用 GOTO 语句。过多使用 GOTO 语句可能会使 Transact-SQL 批处理的逻辑难于理解。使用 GOTO 实现的逻辑几乎完全可以使用其他控制流语句实现。GOTO 最好用于跳出深层嵌套的控制流语句。

标签是 GOTO 的目标，它仅标识了跳转的目标。标签不隔离其前后的语句。执行标签前面语句的用户将跳过标签并执行标签后的语句。除非标签前面的语句本身是控制流语句（如 RETURN），这种情况才会发生。

示例
```sql
IF (SELECT SYSTEM_USER()) = 'payroll'
   GOTO calculate_salary
-- Other program code would appear here.
-- When the IF statement evaluates to TRUE, the statements
-- between the GOTO and the calculate_salary label are
-- ignored. When the IF statement evaluates to FALSE the
-- statements following the GOTO are executed.
calculate_salary:
   -- Statements to calculate a salary would appear after the label.
```
#####三、IF...ELSE

IF 语句用于条件的测试。得到的控制流取决于是否指定了可选的 ELSE 语句(总之就一句话，和其他语言的if…..else 没有什么区别)。

例、（来自MSDN）
```sql
IF (@ErrorSaveVariable <> 0)
BEGIN
   PRINT 'Errors encountered, rolling back.'
   PRINT 'Last error encountered: ' +
      CAST(@ErrorSaveVariable AS VARCHAR(10))
   ROLLBACK
END
ELSE
BEGIN
   PRINT 'No Errors encountered, committing.'
   COMMIT
END
RETURN @ErrorSaveVariable
```
#####四、 WHILE...BREAK 或 CONTINUE

只要指定的条件为 True 时，WHILE 语句就会重复语句或语句块。

CONTINUE语句可以让程序跳过CONTINUE语句之后的语句，回到WHILE循环的第一行。BREAK语句则让程序完全跳出循环，结束WHILE循环（一句话，和其他语言的continue、break没有什么区别）

如果将 SELECT 语句用作 WHILE 语句的条件，则 SELECT 语句必须在括号中（）。
语法格式：
```sql
WHILE<条件表达式>
BEGIN
    <命令行或程序块>
    [BREAK]
    [CONTINUE]
    [命令行或程序块]
END
```
例：
创建表：
```sql
CREATE TABLE PERSON_W
(ID int, char(10), char(6),工资 int,年龄 int)
```
循环插入记录：
```sql
DECLARE @inde int
SET @inde =10
WHILE(@inde<15)
BEGIN
     INSERT INTO PERSON_W SELECT @inde,'A','ZHANG',100,20
     SET @inde=@inde+1
END
```
查看表数据：
```sql
SELECT * FROM PERSON_W
```
结果：
```shell
ＩＤ 部门 员工 工资 年龄

10    A             ZHANG     100    20
11    A             ZHANG     100    20
12    A             ZHANG     100    20
13    A             ZHANG     100    20
14    A             ZHANG     100    20
```
#####五、RETURN

RETURN 语句无条件终止查询、存储过程或批处理。存储过程或批处理中 RETURN 语句后面的语句都不执行。

当在存储过程中使用 RETURN 语句时，此语句可以指定返回给调用应用程序、批处理或过程的整数值。如果 RETURN 未指定值，则存储过程返回 0。

大多数存储过程按常规使用返回代码表示存储过程的成功或失败。没有发生错误时存储过程返回值 0。任何非零值表示有错误发生。

例：（来自ＭＳＤＮ）
```sql
USE AdventureWorks;
GO
-- Create a procedure that takes one input parameter
-- and returns one output parameter and a return code.
CREATE PROCEDURE SampleProcedure @EmployeeIDParm INT,
    @MaxTotal INT OUTPUT
AS
-- Declare and initialize a variable to hold @@ERROR.
DECLARE @ErrorSave int;
SET @ErrorSave = 0;
-- Do a SELECT using the input parameter.
SELECT c.FirstName, c.LastName, e.Title
FROM HumanResources.Employee AS e
JOIN Person.Contact AS c ON e.ContactID = c.ContactID
WHERE EmployeeID = @EmployeeIDParm;
-- Save any nonzero @@ERROR value.
IF (@@ERROR <> 0)
    SET @ErrorSave = @@ERROR;
-- Set a value in the output parameter.
SELECT @MaxTotal = MAX(TotalDue)
FROM Sales.SalesOrderHeader;
IF (@@ERROR <> 0)
    SET @ErrorSave = @@ERROR;
-- Returns 0 if neither SELECT statement had
-- an error, otherwise returns the last error.
RETURN @ErrorSave;
GO
```
执行存储过程的 Transact-SQL 批处理或存储过程可以将返回代码检索到整数变量中：
```sql
DECLARE @ReturnStatus int;
DECLARE @MaxTotalVariable int;
DECLARE @MaxTotal int;
EXECUTE @ReturnStatus = SampleProcedure @EmployeeIDParm = 65 ,@MaxTotal = @MaxTotalVariable OUTPUT;
PRINT ' ';
PRINT 'Return code = ' + CAST(@ReturnStatus AS CHAR(10));
PRINT 'Maximum Order Total = ' + CAST(@MaxTotalVariable AS CHAR(15));
GO
```
调用存储过程的应用程序可以将返回代码所对应的参数标记与整型变量绑定。
#####六、WAITFOR

WAITFOR 语句挂起批处理、存储过程或事务的执行，直到发生以下情况：

    （１）已超过指定的时间间隔。
    （２）到达一天中指定的时间。
    （３）指定的 RECEIVE 语句至少修改一行或并将其返回到 Service Broker 队列。

实际的时间延迟可能随着指定的时间而变化，并取决于服务器的活动级别。时间计数器在计划完与 WAITFOR 语句关联的线程后启动。如果服务器忙碌，则可能不会立即计划线程；因此，时间延迟可能比指定的时间要长。

WAITFOR 语句由下列子句之一指定：
```sql
    DELAY 关键字后为 time_to_pass，是指完成 WAITFOR 语句之前等待的时间。完成 WAITFOR 语句之前等待的时间最多为 24 小时。
    在下面的示例中，执行 SELECT 语句之前使用 DELAY 关键字等待两秒钟：

    WAITFOR DELAY '00:00:02'
    SELECT EmployeeID FROM AdventureWorks.HumanResources.Employee;

    TIME 关键字后为 time_to_execute，指定 WAITFOR 语句完成所用的时间。
    下面的示例使用 TIME 关键字等到晚上 10 点 (22:00) 才执行 AdventureWorks 数据库检查，从而确保正确地分配并使用所有页：

    USE AdventureWorks;
    GO
    BEGIN
        WAITFOR TIME '22:00';
        DBCC CHECKALLOC;
    END;
    GO
```
  RECEIVE 语句子句，从 Service Broker 队列检索一条或多条消息。使用 RECEIVE 语句指定 WAITFOR 时，如果当前未显示任何消息，该语句将等待消息到达队列。
  后面带有 timeout 的 TIMEOUT 关键字将指定 Service Broker 等待消息到达队列的时间（毫秒）。可以在 RECEIVE 语句或 GET CONVERSATION GROUP 语句中指定 TIMEOUT。







 ####[python datetime处理时间](http://www.cnblogs.com/lhj588/archive/2012/04/23/2466653.html)

  Python提供了多个内置模块用于操作日期时间，像calendar，time，datetime。time模块我在之前的文章已经有所介绍，它提供 的接口与C标准库time.h基本一致。相比于time模块，datetime模块的接口则更直观、更容易调用。今天就来讲讲datetime模块。

  datetime模块定义了两个常量：datetime.MINYEAR和datetime.MAXYEAR，分别表示datetime所能表示的最 小、最大年份。其中，MINYEAR = 1，MAXYEAR = 9999。（对于偶等玩家，这个范围已经足够用矣~~）

   datetime模块定义了下面这几个类：

   datetime.date：表示日期的类。常用的属性有year, month, day；
   datetime.time：表示时间的类。常用的属性有hour, minute, second, microsecond；
   datetime.datetime：表示日期时间。
   datetime.timedelta：表示时间间隔，即两个时间点之间的长度。
   datetime.tzinfo：与时区有关的相关信息。（这里不详细充分讨论该类，感兴趣的童鞋可以参考python手册）

   注 ：上面这些类型的对象都是不可变（immutable）的。

   下面详细介绍这些类的使用方式。
date类

   date类表示一个日期。日期由年、月、日组成（地球人都知道~~）。date类的构造函数如下：

   class datetime.date(year, month, day)：参数的意义就不多作解释了，只是有几点要注意一下：

   year的范围是[MINYEAR, MAXYEAR]，即[1, 9999]；
   month的范围是[1, 12]。（月份是从1开始的，不是从0开始的~_~）；
   day的最大值根据给定的year, month参数来决定。例如闰年2月份有29天；

   date类定义了一些常用的类方法与类属性，方便我们操作：

   date.max、date.min：date对象所能表示的最大、最小日期；
   date.resolution：date对象表示日期的最小单位。这里是天。
   date.today()：返回一个表示当前本地日期的date对象；
   date.fromtimestamp(timestamp)：根据给定的时间戮，返回一个date对象；
   datetime.fromordinal(ordinal)：将Gregorian日历时间转换为date对象；（Gregorian Calendar ：一种日历表示方法，类似于我国的农历，西方国家使用比较多，此处不详细展开讨论。）

   使用例子：
```python
    from  datetime  import  *  
    import  time  
      
    print   'date.max:' , date.max  
    print   'date.min:' , date.min  
    print   'date.today():' , date.today()  
    print   'date.fromtimestamp():' , date.fromtimestamp(time.time())  
      
    # # ---- 结果 ----   
    # date.max: 9999-12-31   
    # date.min: 0001-01-01   
    # date.today(): 2010-04-06   
    # date.fromtimestamp(): 2010-04-06   

    from datetime import *  
    import time  
      
    print 'date.max:', date.max  
    print 'date.min:', date.min  
    print 'date.today():', date.today()  
    print 'date.fromtimestamp():', date.fromtimestamp(time.time())  
      
    # # ---- 结果 ----  
    # date.max: 9999-12-31  
    # date.min: 0001-01-01  
    # date.today(): 2010-04-06  
    # date.fromtimestamp(): 2010-04-06  
```
   date提供的实例方法和属性：

   date.year、date.month、date.day：年、月、日；
   date.replace(year, month, day)：生成一个新的日期对象，用参数指定的年，月，日代替原有对象中的属性。（原有对象仍保持不变）
   date.timetuple()：返回日期对应的time.struct_time对象；
   date.toordinal()：返回日期对应的Gregorian Calendar日期；
   date.weekday()：返回weekday，如果是星期一，返回0；如果是星期2，返回1，以此类推；
   data.isoweekday()：返回weekday，如果是星期一，返回1；如果是星期2，返回2，以此类推；
   date.isocalendar()：返回格式如(year，month，day)的元组；       
   date.isoformat()：返回格式如'YYYY-MM-DD’的字符串；
   date.strftime(fmt)：自定义格式化字符串。在下面详细讲解。

   使用例子：
```python
    now = date( 2010 ,  04 ,  06 )  
    tomorrow = now.replace(day = 07 )  
    print   'now:' , now,  ', tomorrow:' , tomorrow  
    print   'timetuple():' , now.timetuple()  
    print   'weekday():' , now.weekday()  
    print   'isoweekday():' , now.isoweekday()  
    print   'isocalendar():' , now.isocalendar()  
    print   'isoformat():' , now.isoformat()  
      
    # # ---- 结果 ----   
    # now: 2010-04-06 , tomorrow: 2010-04-07   
    # timetuple(): (2010, 4, 6, 0, 0, 0, 1, 96, -1)   
    # weekday(): 1   
    # isoweekday(): 2   
    # isocalendar(): (2010, 14, 2)   
    # isoformat(): 2010-04-06   

    now = date(2010, 04, 06)  
    tomorrow = now.replace(day = 07)  
    print 'now:', now, ', tomorrow:', tomorrow  
    print 'timetuple():', now.timetuple()  
    print 'weekday():', now.weekday()  
    print 'isoweekday():', now.isoweekday()  
    print 'isocalendar():', now.isocalendar()  
    print 'isoformat():', now.isoformat()  
      
    # # ---- 结果 ----  
    # now: 2010-04-06 , tomorrow: 2010-04-07  
    # timetuple(): (2010, 4, 6, 0, 0, 0, 1, 96, -1)  
    # weekday(): 1  
    # isoweekday(): 2  
    # isocalendar(): (2010, 14, 2)  
    # isoformat(): 2010-04-06  
```
   date还对某些操作进行了重载，它允许我们对日期进行如下一些操作：

   date2 = date1 + timedelta  # 日期加上一个间隔，返回一个新的日期对象（timedelta将在下面介绍，表示时间间隔）
   date2 = date1 - timedelta   # 日期隔去间隔，返回一个新的日期对象
   timedelta = date1 - date2   # 两个日期相减，返回一个时间间隔对象
   date1 < date2  # 两个日期进行比较

   注： 对日期进行操作时，要防止日期超出它所能表示的范围。

   使用例子：
```python
    now = date.today()  
    tomorrow = now.replace(day = 7 )  
    delta = tomorrow - now  
    print   'now:' , now,  ' tomorrow:' , tomorrow  
    print   'timedelta:' , delta  
    print  now + delta  
    print  tomorrow > now  
      
    # # ---- 结果 ----   
    # now: 2010-04-06  tomorrow: 2010-04-07   
    # timedelta: 1 day, 0:00:00   
    # 2010-04-07   
    # True   

    now = date.today()  
    tomorrow = now.replace(day = 7)  
    delta = tomorrow - now  
    print 'now:', now, ' tomorrow:', tomorrow  
    print 'timedelta:', delta  
    print now + delta  
    print tomorrow > now  
      
    # # ---- 结果 ----  
    # now: 2010-04-06  tomorrow: 2010-04-07  
    # timedelta: 1 day, 0:00:00  
    # 2010-04-07  
    # True  
```
Time类

   time类表示时间，由时、分、秒以及微秒组成。（我不是从火星来的~~）time类的构造函数如下：

   class datetime.time(hour[ , minute[ , second[ , microsecond[ , tzinfo] ] ] ] ) ：各参数的意义不作解释，这里留意一下参数tzinfo，它表示时区信息。注意一下各参数的取值范围：hour的范围为[0, 24)，minute的范围为[0, 60)，second的范围为[0, 60)，microsecond的范围为[0, 1000000)。

   time类定义的类属性：

   time.min、time.max：time类所能表示的最小、最大时间。其中，time.min = time(0, 0, 0, 0)， time.max = time(23, 59, 59, 999999)；
   time.resolution：时间的最小单位，这里是1微秒；

   time类提供的实例方法和属性：

   time.hour、time.minute、time.second、time.microsecond：时、分、秒、微秒；
   time.tzinfo：时区信息；
   time.replace([ hour[ , minute[ , second[ , microsecond[ , tzinfo] ] ] ] ] )：创建一个新的时间对象，用参数指定的时、分、秒、微秒代替原有对象中的属性（原有对象仍保持不变）；
   time.isoformat()：返回型如"HH:MM:SS"格式的字符串表示；
   time.strftime(fmt)：返回自定义格式化字符串。在下面详细介绍；

使用例子：
```python
    from  datetime  import  *  
    tm = time(23 ,  46 ,  10 )  
    print   'tm:' , tm  
    print   'hour: %d, minute: %d, second: %d, microsecond: %d'  \  
            % (tm.hour, tm.minute, tm.second, tm.microsecond)  
    tm1 = tm.replace(hour = 20 )  
    print   'tm1:' , tm1  
    print   'isoformat():' , tm.isoformat()  
      
    # # ---- 结果 ----   
    # tm: 23:46:10   
    # hour: 23, minute: 46, second: 10, microsecond: 0   
    # tm1: 20:46:10   
    # isoformat(): 23:46:10   

    from datetime import *  
    tm = time(23, 46, 10)  
    print 'tm:', tm  
    print 'hour: %d, minute: %d, second: %d, microsecond: %d' \  
            % (tm.hour, tm.minute, tm.second, tm.microsecond)  
    tm1 = tm.replace(hour = 20)  
    print 'tm1:', tm1  
    print 'isoformat():', tm.isoformat()  
      
    # # ---- 结果 ----  
    # tm: 23:46:10  
    # hour: 23, minute: 46, second: 10, microsecond: 0  
    # tm1: 20:46:10  
    # isoformat(): 23:46:10  
```
   像date一样，也可以对两个time对象进行比较，或者相减返回一个时间间隔对象。这里就不提供例子了。
datetime类

   datetime是date与time的结合体，包括date与time的所有信息。它的构造函数如下：datetime.datetime (year, month, day[ , hour[ , minute[ , second[ , microsecond[ , tzinfo] ] ] ] ] )，各参数的含义与date、time的构造函数中的一样，要注意参数值的范围。

   datetime类定义的类属性与方法：

   datetime.min、datetime.max：datetime所能表示的最小值与最大值；
   datetime.resolution：datetime最小单位；
   datetime.today()：返回一个表示当前本地时间的datetime对象；
   datetime.now([tz])：返回一个表示当前本地时间的datetime对象，如果提供了参数tz，则获取tz参数所指时区的本地时间；
   datetime.utcnow()：返回一个当前utc时间的datetime对象；
   datetime.fromtimestamp(timestamp[, tz])：根据时间戮创建一个datetime对象，参数tz指定时区信息；
   datetime.utcfromtimestamp(timestamp)：根据时间戮创建一个datetime对象；
   datetime.combine(date, time)：根据date和time，创建一个datetime对象；
   datetime.strptime(date_string, format)：将格式字符串转换为datetime对象；

使用例子：
```python
    from  datetime  import  *  
    import  time  
      
    print   'datetime.max:' , datetime.max  
    print   'datetime.min:' , datetime.min  
    print   'datetime.resolution:' , datetime.resolution  
    print   'today():' , datetime.today()  
    print   'now():' , datetime.now()  
    print   'utcnow():' , datetime.utcnow()  
    print   'fromtimestamp(tmstmp):' , datetime.fromtimestamp(time.time())  
    print   'utcfromtimestamp(tmstmp):' , datetime.utcfromtimestamp(time.time())  
      
    # ---- 结果 ----   
    # datetime.max: 9999-12-31 23:59:59.999999   
    # datetime.min: 0001-01-01 00:00:00   
    # datetime.resolution: 0:00:00.000001   
    # today(): 2010-04-07 09:48:16.234000   
    # now(): 2010-04-07 09:48:16.234000   
    # utcnow(): 2010-04-07 01:48:16.234000  # 中国位于+8时间，与本地时间相差8   
    # fromtimestamp(tmstmp): 2010-04-07 09:48:16.234000   
    # utcfromtimestamp(tmstmp): 2010-04-07 01:48:16.234000   

    from datetime import *  
    import time  
      
    print 'datetime.max:', datetime.max  
    print 'datetime.min:', datetime.min  
    print 'datetime.resolution:', datetime.resolution  
    print 'today():', datetime.today()  
    print 'now():', datetime.now()  
    print 'utcnow():', datetime.utcnow()  
    print 'fromtimestamp(tmstmp):', datetime.fromtimestamp(time.time())  
    print 'utcfromtimestamp(tmstmp):', datetime.utcfromtimestamp(time.time())  
      
    # ---- 结果 ----  
    # datetime.max: 9999-12-31 23:59:59.999999  
    # datetime.min: 0001-01-01 00:00:00  
    # datetime.resolution: 0:00:00.000001  
    # today(): 2010-04-07 09:48:16.234000  
    # now(): 2010-04-07 09:48:16.234000  
    # utcnow(): 2010-04-07 01:48:16.234000  # 中国位于+8时间，与本地时间相差8  
    # fromtimestamp(tmstmp): 2010-04-07 09:48:16.234000  
    # utcfromtimestamp(tmstmp): 2010-04-07 01:48:16.234000  
```
  datetime类提供的实例方法与属性（很多属性或方法在date和time中已经出现过，在此有类似的意义，这里只罗列这些方法名，具体含义不再逐个展开介绍，可以参考上文对date与time类的讲解。）：

   datetime.year、month、day、hour、minute、second、microsecond、tzinfo：
   datetime.date()：获取date对象；
   datetime.time()：获取time对象；
   datetime. replace ([ year[ , month[ , day[ , hour[ , minute[ , second[ , microsecond[ , tzinfo] ] ] ] ] ] ] ])：
   datetime. timetuple ()
   datetime. utctimetuple ()
   datetime. toordinal ()
   datetime. weekday ()
   datetime. isocalendar ()
   datetime. isoformat ([ sep] )
   datetime. ctime ()：返回一个日期时间的C格式字符串，等效于time.ctime(time.mktime(dt.timetuple()))；
   datetime. strftime (format)

   像date一样，也可以对两个datetime对象进行比较，或者相减返回一个时间间隔对象，或者日期时间加上一个间隔返回一个新的日期时间对象。这里不提供详细的例子，看客自己动手试一下~~
格式字符串

   datetime、date、time都提供了strftime()方法，该方法接收一个格式字符串，输出日期时间的字符串表示。下表是从python手册中拉过来的，我对些进行了简单的翻译（翻译的有点噢口~~）。

格式字符  意义

%a 星期的简写。如 星期三为Web
%A 星期的全写。如 星期三为Wednesday
%b 月份的简写。如4月份为Apr
%B月份的全写。如4月份为April 
%c:  日期时间的字符串表示。（如： 04/07/10 10:43:39）
%d:  日在这个月中的天数（是这个月的第几天）
%f:  微秒（范围[0,999999]）
%H:  小时（24小时制，[0, 23]）
%I:  小时（12小时制，[0, 11]）
%j:  日在年中的天数 [001,366]（是当年的第几天）
%m:  月份（[01,12]）
%M:  分钟（[00,59]）
%p:  AM或者PM
%S:  秒（范围为[00,61]，为什么不是[00, 59]，参考python手册~_~）
%U:  周在当年的周数当年的第几周），星期天作为周的第一天
%w:  今天在这周的天数，范围为[0, 6]，6表示星期天
%W:  周在当年的周数（是当年的第几周），星期一作为周的第一天
%x:  日期字符串（如：04/07/10）
%X:  时间字符串（如：10:43:39）
%y:  2个数字表示的年份
%Y:  4个数字表示的年份
%z:  与utc时间的间隔 （如果是本地时间，返回空字符串）
%Z:  时区名称（如果是本地时间，返回空字符串）
%%:  %% => %

例子：
```python
    dt = datetime.now()  
    print   '(%Y-%m-%d %H:%M:%S %f): ' , dt.strftime( '%Y-%m-%d %H:%M:%S %f' )  
    print   '(%Y-%m-%d %H:%M:%S %p): ' , dt.strftime( '%y-%m-%d %I:%M:%S %p' )  
    print   '%%a: %s '  % dt.strftime( '%a' )  
    print   '%%A: %s '  % dt.strftime( '%A' )  
    print   '%%b: %s '  % dt.strftime( '%b' )  
    print   '%%B: %s '  % dt.strftime( '%B' )  
    print   '日期时间%%c: %s '  % dt.strftime( '%c' )  
    print   '日期%%x：%s '  % dt.strftime( '%x' )  
    print   '时间%%X：%s '  % dt.strftime( '%X' )  
    print   '今天是这周的第%s天 '  % dt.strftime( '%w' )  
    print   '今天是今年的第%s天 '  % dt.strftime( '%j' )  
    print   '今周是今年的第%s周 '  % dt.strftime( '%U' )  
      
    # # ---- 结果 ----   
    # (%Y-%m-%d %H:%M:%S %f):  2010-04-07 10:52:18 937000   
    # (%Y-%m-%d %H:%M:%S %p):  10-04-07 10:52:18 AM   
    # %a: Wed    
    # %A: Wednesday    
    # %b: Apr    
    # %B: April    
    # 日期时间%c: 04/07/10 10:52:18    
    # 日期%x：04/07/10    
    # 时间%X：10:52:18    
    # 今天是这周的第3天    
    # 今天是今年的第097天    
    # 今周是今年的第14周   

        dt = datetime.now()  
        print '(%Y-%m-%d %H:%M:%S %f): ', dt.strftime('%Y-%m-%d %H:%M:%S %f')  
        print '(%Y-%m-%d %H:%M:%S %p): ', dt.strftime('%y-%m-%d %I:%M:%S %p')  
        print '%%a: %s ' % dt.strftime('%a')  
        print '%%A: %s ' % dt.strftime('%A')  
        print '%%b: %s ' % dt.strftime('%b')  
        print '%%B: %s ' % dt.strftime('%B')  
        print '日期时间%%c: %s ' % dt.strftime('%c')  
        print '日期%%x：%s ' % dt.strftime('%x')  
        print '时间%%X：%s ' % dt.strftime('%X')  
        print '今天是这周的第%s天 ' % dt.strftime('%w')  
        print '今天是今年的第%s天 ' % dt.strftime('%j')  
        print '今周是今年的第%s周 ' % dt.strftime('%U')  
          
        # # ---- 结果 ----  
        # (%Y-%m-%d %H:%M:%S %f):  2010-04-07 10:52:18 937000  
        # (%Y-%m-%d %H:%M:%S %p):  10-04-07 10:52:18 AM  
        # %a: Wed   
        # %A: Wednesday   
        # %b: Apr   
        # %B: April   
        # 日期时间%c: 04/07/10 10:52:18   
        # 日期%x：04/07/10   
        # 时间%X：10:52:18   
        # 今天是这周的第3天   
        # 今天是今年的第097天   
        # 今周是今年的第14周  
```