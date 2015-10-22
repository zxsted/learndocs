
python 系统学习笔记（一）
分类： python 2013-03-04 13:18 777人阅读 评论(3) 收藏 举报
目标:熟悉python语言，以及学会python的编码方式。

如果你在window下， 去下载 http://www.python.org/getit/
安装起来， 然后运行python， 进入python解释环境。
如果你在ubuntu下， 执行: sudo apt-get install python， 然后在命令行下运行python， 进入python解释环境。

开始学习python

我建议你学习的过程也按照上面来，首先过一遍python官方文档:
http://docs.python.org/2.7/tutorial/index.html
如何查找python的某个功能?
http://docs.python.org/2.7/library/index.html

 C or C++ 扩展 read Extending and Embedding the Python Interpreter and Python/C API Reference  Manual. 

1.写第一个Helloworld
当你学习一种新的编程语言的时候，你编写运行的第一个程序通常都是“Hello
World”程序，这已经成为一种传统了
在命令行的shell提示符下键入python，启动解释器。现在输入print 'Hello World'，然后
按Enter键。你应该可以看到输出的单词Hello World 。（python 2.7  3.3目前已经是函数了）

     如何编写Python程序
下面是保存和运行Python程序的标准流程。
   1. 打开你最喜欢的编辑器。
   2. 输入例子中的程序代码。
   3. 用注释中给出的文件名把它保存为一个文件。我按照惯例把所有的Python程序都以
扩展名.py 保存。
   4. 运行解释器命令python  program.py或者使用IDLE运行程序。

2.运算符

反复练习！！

Python运算符列表

运算符
	

描述

x+y，x-y
	

加、减，“+”号可重载为连接符

x*y，x**y，x/y，x%y
	

相乘、求平方、相除、求余，“*”号可重载为重复，“%”号可重载为格式化

< span>，< span>，<，<=，==，<>，!=
	

比较运算符

+=，-=，*=，/=，%=，**=，< span>，<<=，&=，^=，|=
	

自变运算符

x|y
	

按位或

x^y
	

按位异或

x&y
	

按位与

~x
	

按位取反

x< span>，x<<y
	

x向左或向右移y位

is, is not
	

等同测试

in, not in
	

是否为成员测试

or，and，not
	

逻辑运算符

x[i]，x[i:j]，x.y，x(...)
	

索引，分片，限定引用，函数调用

(...)，[...]，{...}，'...'
	

元组，列表，字典，转化为字符串
 运算符优先顺序

运算符优先顺序列表(从最高到最低)

运算符
	

描述

'expr'
	

字符串转换

{key:expr,...}
	

字典

[expr1,expr2...]
	

列表

(expr1,expr2,...)
	

元组

function(expr,...)
	

函数调用

x[index:index]
	

切片

x[index]
	

下标索引取值

x.attribute
	

属性引用

~x
	

按位取反

+x，-x
	

正，负

x**y
	

幂

x*y，x/y，x%y
	

乘，除，取模

x+y，x-y
	

加，减

x< y>，x<<y
	

移位

x&y
	

按位与

x^y
	

按位异或

x|y
	

按位或

x，x< yspan>，x==y，x!=y，x<=y，x<y
	

比较

x is y，x is not y
	

等同测试

x in y，x not in y
	

成员判断

not x
	

逻辑否

x and y
	

逻辑与

x or y
	

逻辑或

lambda arg,...:expr
	

Lambda匿名函数



3. if 语句
1. if else语句
2. if...elif...elif ..else
3.     if语句的嵌套

编写条件语句时，应该尽量避免使用嵌套语句。嵌套语句不便于阅读，而且可能会忽略一些可能性。
练习
[python] view plaincopy

    #score = raw_input("score:")  
    #score=int(score)  
    score=85  
      
      
    # if else   demo1  
    if( score >60):  
        print 'pass'  
    else:  
        print 'fail'  
      
      
    # if elif else demo2  
    if(score> 90):  
        print 'A'  
    elif(score >80) and(score <90):  
        print 'B'  
    elif(score>70) and (score<80):  
        print 'C'  
    else:  
        print 'D'  
      
      
    #if include if  demo3  
      
      
    a=3;  
    b=4;  
    c=5;  
    if(a>b):  
        if(c>a):  
            print 'Max is c'  
        else:  
            print 'Max is a'  
    else:  
        if(b>c):  
            print 'Max is b'  
        else:  
            print 'Max is c'  
    print 'done'  


        
 
4.for 语句

1、一般格式
Python for循环的首行定义了一个赋值目标（或【一些目标】），以及你想遍历的对象，首行后面是你想重复的语句块（一般都有缩进）
for <target> in <object>:
    <statements>
else:
    <statements>
当ptyhon运行for循环时，会逐个将序列对象中的元素赋值给目标，然后为每个元素执行循环主体。循环主体一般使用赋值的目标来引用序列中当前的元素，就好像那事遍历序列的游标。

for首行中用作赋值目标的变量名通常是for语句所在作用于的变量（可能是新的）。这个变量名没有什么特别的，甚至可以在循环主体中修改。但是当控制权再次回到循环顶端时，就会自动被设成序列的下一个元素。循环之后，这个变量一般都还是引用了最近所用过的元素，也就是序列中最后的元素，除非通过一个 break语句退出了循环。

for语句也支持一个选用的else块，它的工作就像在while循环中一样：如果循环离开时没有碰到break语句，就会执行（也就是序列所有元素都被访问过了）
break和continue语句也可用在for循环中，就像while循环那样。for循环完整的格式如下：
for <target> in <object>:
    <statements>
    if <test>:break
    if <test>:conitnue
else:
    <statements>


[python] view plaincopy

    a = ['a1', 'a2', 'a3']  
    b = ['b1', 'b2']  
          
    # will iterate 3 times,  
    # the last iteration, b will be None  
    print "Map:"  
    for x, y in map(None, a, b):  
      print x, y  
          
    # will iterate 2 times,  
    # the third value of a will not be used  
    print "Zip:"  
    for x, y in zip(a, b):  
      print x, y  
          
    # will iterate 6 times,  
    # it will iterate over each b, for each a  
    # producing a slightly different outpu  
    print "List:"  
    for x, y in [(x,y) for x in a for y in b]:  
        print x, y    


[python] view plaincopy

    #demo for 'for'  
    # -*- coding: cp936 -*-  
      
      
    #for in  
    for i in range(1,5):  
        print i  
          
    #step 2  
    for i in range(1,5,2):  
        print i;  
      
      
    #break  
    for i in range(1,5):  
        if(i==6):  
            break   
    else:  
        print 'break hello'  
      
      
    #求质数  
    import math    
    for i in range(50, 100 + 1):  
        for j in range(2, int(math.sqrt(i)) + 1):  
            if i % j == 0:  
                break  
        else:  
            print i  
              
      
      
    #continue  
    for i in range(1,5):  
        if(i==4):  
            continue  
        print 'no met continue'  
    else:  
        print i  
          


 

5.while 语句
while循环的一般格式如下：
while <test>:
    <statements1>
    if <test2>:break
    if <test3>:continue
    if <test4>:pass
else:
    <statements2>
break和continue可以出现在while（或for）循环主体的任何地方，但通常会进一步嵌套在if语句中，根据某些条件来采取对应的操作。

[python] view plaincopy

    #demo for while  
    a=4;  
    while (a>0):  
        print a;  
        a=a-1;  
        if(a==1):  
            break  
    else:  
        print 'no meet break'  
          
     #continue  
    a=4;  
    while (a>0):  
        print a;  
        a=a-1;  
        if(a==1):  
            continue  
        print 'no meet continue' 
        
        



python 系统学习笔记（二）---string
分类： python 2013-03-04 15:58 851人阅读 评论(0) 收藏 举报
str='python String function'

生成字符串变量str='python String function'

字符串长度获取：len(str)
例：print '%s length=%d' % (str,len(str))

字母处理
全部大写：str.upper()
全部小写：str.lower()
大小写互换：str.swapcase()
首字母大写，其余小写：str.capitalize()
首字母大写：str.title()
print '%s lower=%s' % (str,str.lower())
print '%s upper=%s' % (str,str.upper())
print '%s swapcase=%s' % (str,str.swapcase())
print '%s capitalize=%s' % (str,str.capitalize())
print '%s title=%s' % (str,str.title()) 
格式化相关
获取固定长度，右对齐，左边不够用空格补齐：str.ljust(width)
获取固定长度，左对齐，右边不够用空格补齐：str.ljust(width)
获取固定长度，中间对齐，两边不够用空格补齐：str.ljust(width)
获取固定长度，右对齐，左边不足用0补齐
print '%s ljust=%s' % (str,str.ljust(20))
print '%s rjust=%s' % (str,str.rjust(20))
print '%s center=%s' % (str,str.center(20))
print '%s zfill=%s' % (str,str.zfill(20))

字符串搜索相关
搜索指定字符串，没有返回-1：str.find('t')
指定起始位置搜索：str.find('t',start)
指定起始及结束位置搜索：str.find('t',start,end)
从右边开始查找：str.rfind('t')
搜索到多少个指定字符串：str.count('t')
上面所有方法都可用index代替，不同的是使用index查找不到会抛异常，而find返回-1
print '%s find nono=%d' % (str,str.find('nono'))
print '%s find t=%d' % (str,str.find('t'))
print '%s find t from %d=%d' % (str,1,str.find('t',1))
print '%s find t from %d to %d=%d' % (str,1,2,str.find('t',1,2))
#print '%s index nono ' % (str,str.index('nono',1,2))
print '%s rfind t=%d' % (str,str.rfind('t'))
print '%s count t=%d' % (str,str.count('t'))

字符串替换相关
替换old为new：str.replace('old','new')
替换指定次数的old为new：str.replace('old','new',maxReplaceTimes)
print '%s replace t to *=%s' % (str,str.replace('t', '*'))
print '%s replace t to *=%s' % (str,str.replace('t', '*',1))

字符串去空格及去指定字符
去两边空格：str.strip()
去左空格：str.lstrip()
去右空格：str.rstrip()
去两边字符串：str.strip('d')，相应的也有lstrip，rstrip
str=' python String function '
print '%s strip=%s' % (str,str.strip())
str='python String function'
print '%s strip=%s' % (str,str.strip('d'))

按指定字符分割字符串为数组：str.split(' ')

string.split(s, sep=None, maxsplit=-1)用sep拆分s，返回拆分后的列表，如果sep没有提供或者为None，那么默认的就是空格
str='a b c de'
print '%s strip=%s' % (str,str.split())
str='a-b-c-de'
print '%s strip=%s' % (str,str.split('-'))

string.join的功能刚好与其相反。

 l=string.split("hello world")
string.join(l)
'hello world'
join(list [,sep])是用sep把list组合成一个字符串返回。
字符串判断相关
是否以start开头：str.startswith('start')
是否以end结尾：str.endswith('end')
是否全为字母或数字：str.isalnum()
是否全字母：str.isalpha()
是否全数字：str.isdigit()
是否全小写：str.islower()
是否全大写：str.isupper()
str='python String function'
print '%s startwith t=%s' % (str,str.startswith('t'))
print '%s endwith d=%s' % (str,str.endswith('d'))
print '%s isalnum=%s' % (str,str.isalnum())
str='pythonStringfunction'
print '%s isalnum=%s' % (str,str.isalnum())
print '%s isalpha=%s' % (str,str.isalpha())
print '%s isupper=%s' % (str,str.isupper())
print '%s islower=%s' % (str,str.islower())
print '%s isdigit=%s' % (str,str.isdigit())
str='3423'
print '%s isdigit=%s' % (str,str.isdigit())

#replace string:replace(old,new[,max])
def replaceString(s):
print s.replace("hello", "hi")
print s.replace("hello", "world", 2)
print s.replace("abc", "hi")


小程序 遍历python string 的function 主要利用help 打印出来帮助信息
[python] view plaincopy

    import string  
    #print dir(string)  
    funOrC=[];  
    vars=[];  
    for fv in dir(string):  
        name="string.%s"%fv  
        if(callable(eval(name))):  
            funOrC.append(fv)  
        else:  
            vars.append(fv)  
      
      
    print funOrC  
    print vars  
      
      
    for tmp in funOrC:  
        #print tmp,"###",eval("string.%s"%tmp)  
        help("string.%s"%tmp)  
        print '***************************************************'  




python 系统学习笔记（三）---function
分类： python 2013-03-04 16:54 353人阅读 评论(0) 收藏 举报

目录(?)[+]
函数：

一、什么是函数

很多时候，Python程序中的语句都会组织成函数的形式。通俗地说，函数就是完成特定功能的一个语句组，这组语句可以作为一个单位使用，并且给它取一个名字，这样，我们就可以通过函数名在程序的不同地方多次执行（这通常叫做函数调用），却不需要在所有地方都重复编写这些语句。另外，每次使用函数时可以提供不同的参数作为输入，以便对不同的数据进行处理；函数处理后，还可以将相应的结果反馈给我们。

有些函数是用户自己编写的，通常我们称之为自定义函数；此外，系统也自带了一些函数，还有一些第三方编写的函数，如其他程序员编写的一些函数，我们称为预定义的Python函数，对于这些现成的函数用户可以直接拿来使用。

二、为什么使用函数

我们之所以使用函数，主要是出于两个方面的考虑：一是为了降低编程的难度，通常将一个复杂的大问题分解成一系列更简单的小问题，然后将小问题继续划分成更小的问题，当问题细化为足够简单时，我们就可以分而治之。这时，我们可以使用函数来处理特定的问题，各个小问题解决了，大问题也就迎刃而解了。二是代码重用。我们定义的函数可以在一个程序的多个位置使用，也可以用于多个程序。此外，我们还可以把函数放到一个模块中供其他程序员使用，同时，我们也可以使用其他程序员定义的函数。这就避免了重复劳动，提供了工作效率。

基本语法
def fun(n,m,...)
 ....
 ....
 ....
 (return n)

关于return
1，return可以有，可以没有，
2，没有return的方法返回None，
3，return后面没有表达式也是返回None，
4，函数无法到达结尾也返回None。
关于变量与方法
1，定义的方法名会在“当前符号表”中注册，这样系统就知道这个方法名为一个方法，将

方法赋值给一个变量，这个变量则变成了对应的方法。
2，与我们以前学习的程序层次一样的，每个层次都有自己的符号表。内层符号表是可以

使用外层符号表中的东西，但是已经不是一个层次的，所以没有什么关系，意思是说，

上层联系下层只能通过参数，下层联系上层只能是返回值。并且到现在为止，我们只知

道有值传递。也就是说，函数内部与外部完全没有什么关系。
3，也就是说，到现在为止，函数层与上层之间没有任何关系，它有自己的符号表，参数

只能从上层得到值，却不能改变上层的内容，一切在函数内部使用的变量都是函数本身

的与上层无关。也就是说函数基本不能主动改变上层的东西。

 

函数是重用的程序段。它们允许你给一块语句一个名称，然后你可以在你的程序的任何地方使用这个名称任意多次地运行这个语句块。这被称为调用函数。我们已经使用了许多内建的函数，比如len和range。

函数通过def关键字定义。def关键字后跟一个函数的标识符名称，然后跟一对圆括号。圆括号之中可以包括一些变量名，该行以冒号结尾。接下来是一块语句，它们是函数体。
1、定义函数：

例如：
[python] view plaincopy

    <span style="font-family:Microsoft YaHei;font-size:18px;"># Filename: function1.py   
         
    def sayHello():   
        print('Hello World!') # block belonging to the function   
    # End of function   
         
    sayHello() # call the function   
    sayHello() # call the function again   
      
    </span>  


 
输出：

    C:\Users\Administrator>python D:\python\function1.py
    Hello World!
    Hello World! 

工作原理：

我们使用上面解释的语法定义了一个称为sayHello的函数。这个函数不使用任何参数，因此在圆括号中没有声明任何变量。参数对于函数而言，只是给函数的输入，以便于我们可以传递不同的值给函数，然后得到相应的结果。我们在上程序中调用了两次相同的函数从而避免了对同一程序段写两次。
2、函数形参：

函数取得的参数是你提供给函数的值，这样函数就可以利用这些值做一些事情。这些参数就像变量一样，只不过它们的值是在我们调用函数的时候定义的，而非在函数本身内赋值。
参数在函数定义的圆括号对内指定，用逗号分割。当我们调用函数的时候，我们以同样的方式提供值。注意我们使用过的术语——函数中的参数名称为形参而你提供给函数调用的值称为实参。
使用函数形参：

例如：
[python] view plaincopy

    <span style="font-family:Microsoft YaHei;font-size:18px;"># Filename: func_param.py   
         
    def printMax(a, b):   
        if a > b:   
            print(a, 'is maximum')   
        elif a == b:   
            print(a, 'is equal to', b)   
        else:   
            print(b, 'is maximum')   
         
    printMax(3, 4) # directly give literal values   
         
    x = 5  
    y = 7  
         
    printMax(x, y) # give variables as arguments   
      
    </span>  


输出：

    C:\Users\Administrator>python D:\python\func_param.py
    4 is maximum
    7 is maximum 

工作原理：

这里，我们定义了一个称为printMax的函数，这个函数需要两个形参，a和b。我们使用if..else语句找出两者之中较大的一个数，并且打印较大的那个数。
在第一个printMax使用中，我们直接把数，即实参，提供给函数。在第二个使用中，我们使用变量调用函数。printMax(x, y)使实参x的值赋给形参a，实参y的值赋给形参b。在两次调用中，printMax函数的工作完全相同。
3、局部变量：

当你在函数定义内声明变量的时候，它们与函数外具有相同名称的其他变量没有任何关系，即变量名称对于函数来说是 局部 的。这称为变量的作用域 。所有变量的作用域是它们被定义的块，从它们的名称被定义的那点开始。

例如：
[python] view plaincopy

    <span style="font-family:Microsoft YaHei;font-size:18px;"># Filename: func_local.py   
         
    x = 50  
         
    def func(x):   
        print('x is', x)   
        x = 2  
        print('Changed local x to', x)   
         
    func(x)   
    print('x is still', x)   
      
    </span>  


 
输出：

    C:\Users\Administrator>python D:\python\func_local.py
    x is 50
    Changed local x to 2
    x is still 50 

工作原理：

在函数中，我们第一次使用x的 值 的时候，Python使用函数声明的形参的值。
接下来，我们把值2赋给x。x是函数的局部变量。所以，当我们在函数内改变x的值的时候，在主块中定义的x不受影响。
在最后一个print语句中，我们证明了主块中的x的值确实没有受到影响。
4、全局变量：

如果你想要为一个定义在函数外的变量赋值，那么你就得告诉Python这个变量名不是局部的，而是全局的。我们使用global语句完成这一功能。没有global语句，是不可能为定义在函数外的变量赋值的。
你可以使用定义在函数外的变量的值（假设在函数内没有同名的变量）。然而，并不推荐这样做，并且我们应该尽量避免这样做，因为这使得程序的读者会不清楚这个变量是在哪里定义的。使用global语句可以清楚地表明变量是在外面的块定义的。

我们可以这样使用：
[python] view plaincopy

    <span style="font-family:Microsoft YaHei;font-size:18px;"># Filename: func_global.py   
         
    x = 50  
         
    def func():   
        global x   
         
        print('x is', x)   
        x = 2  
        print('Changed global x to', x)   
         
    func()   
    print('Value of x is', x)   
      
    </span>  


输出：

    C:\Users\Administrator>python D:\python\func_global.py
    x is 50
    Changed global x to 2
    Value of x is 2 

工作原理：

global语句被用来声明x是全局的——因此，当我们在函数内把值赋给x的时候，这个变化也反映在我们在主块中使用x的值的时候。
你可以使用同一个global语句指定多个全局变量。例如global x, y, z。
5、外部变量：

上面我们已经知道如何使用局部变量和全局变量，还有一个外部变量是在以上两种变量之间的变量。当我们在函数内声明了外部变量则在函数中就可见了。

由于任何东西在python内都是可执行代码，所以你可以在任何位置定义函数，就如以下例子中的func_inner()定义在func_outer()内也是可以的。

以下例子说明如何使用外部变量：
[python] view plaincopy

    <span style="font-family:Microsoft YaHei;font-size:18px;"># Filename: func_nonlocal.py   
         
    def func_outer():   
        x = 2  
        print('x is', x)   
         
        def func_inner():   
            nonlocal x   
            x = 5  
         
        func_inner()   
        print('Changed local x to', x)   
         
    func_outer()   
      
    </span>  


输出：

    C:\Users\Administrator>python D:\python\func_nonlocal.py
    x is 2
    Changed local x to 5 

工作原理：

当我们在func_inner()函数中的时候，在函数func_outer()内第一行定义的变量x既不是内部变量(它不在func_inner块内)也不是全局变量(它也不在主程序块内)，这时我们使用nonlocal x声明我们需要使用这个变量。

你可以尝试改变声明方式，然后观察这几种变量的区别。
6、默认参数值

对于一些函数，你可能希望它的一些参数是可选的，如果用户不想要为这些参数提供值的话，这些参数就使用默认值。这个功能借助于默认参数值完成。你可以在函数定义的形参名后加上赋值运算符（=）和默认值，从而给形参指定默认参数值。
注意，默认参数值应该是一个常数。更加准确的说，默认参数值应该是不可变的。

使用默认参数值：

例如：
[python] view plaincopy

    <span style="font-family:Microsoft YaHei;font-size:18px;"># Filename: func_default.py   
         
    def say(message, times = 1):   
        print(message * times)   
         
    say('Hello')   
    say('World', 5)   
      
    </span>  


 
输出：

    C:\Users\Administrator>python D:\python\func_default.py
    Hello
    WorldWorldWorldWorldWorld 

工作原理：

名为say的函数用来打印一个字符串任意所需的次数。如果我们不提供一个值，那么默认地，字符串将只被打印一遍。我们通过给形参times指定默认参数值1来实现这一功能。
在第一次使用say的时候，我们只提供一个字符串，函数只打印一次字符串。在第二次使用say的时候，我们提供了字符串和参数5，表明我们想要打印这个字符串消息5遍。

注：

只有在形参表末尾的那些参数可以有默认参数值，即你不能在声明函数形参的时候，先声明有默认值的形参而后声明没有默认值的形参。
这是因为赋给形参的值是根据位置而赋值的。例如，def func(a, b=5)是有效的，但是def func(a=5, b)是无效的。
7、关键字（Keyword）参数：

如果你的某个函数有许多参数，而你只想指定其中的一部分，那么你可以通过命名来为这些参数赋值——这被称作关键字参数，我们使用名字（关键字）而不是位置（我们前面所一直使用的方法）来给函数指定实参。
这样做有两个优势：一，由于我们不必担心参数的顺序，使用函数变得更加简单了；二、假设其他参数都有默认值，我们可以只给我们想要的那些参数赋值。

使用关键参数：
[python] view plaincopy

    <span style="font-family:Microsoft YaHei;font-size:18px;"># Filename: func_key.py   
         
    def func(a, b=5, c=10):   
        print('a is', a, 'and b is', b, 'and c is', c)   
         
    func(3, 7)   
    func(25, c=24)   
    func(c=50, a=100)</span>  


 
输出：

    C:\Users\Administrator>python D:\python\func_key.py
    a is 3 and b is 7 and c is 10
    a is 25 and b is 5 and c is 24
    a is 100 and b is 5 and c is 50 

工作原理：

名为func的函数有一个没有默认值的参数，和两个有默认值的参数。
在第一次使用函数的时候， func(3, 7)，参数a得到值3，参数b得到值7，而参数c使用默认值10。
在第二次使用函数func(25, c=24)的时候，根据实参的位置变量a得到值25。根据命名，即关键参数，参数c得到值24。变量b根据默认值，为5。
在第三次使用func(c=50, a=100)的时候，我们使用关键参数来完全指定参数值。注意，尽管函数定义中，a在c之前定义，我们仍然可以在a之前指定参数c的值。
8、可变（VarArgs）参数：

有些时候你可能希望定义一个可以接受任意个数参数的函数，你可以使用星号来完成。

例如：
[python] view plaincopy

    <span style="font-family:Microsoft YaHei;font-size:18px;"># Filename: total.py   
         
    def total(initial=5, *numbers, **keywords):   
        count = initial   
        for number in numbers:   
            count += number   
        for key in keywords:   
            count += keywords[key]   
        return count   
         
    print(total(10, 1, 2, 3, vegetables=50, fruits=100))   
      
    </span>  


输出：

    C:\Users\Administrator>python D:\python\total.py
    166 

工作原理：

当我们以星号声明一个形参，如*param，从这个位置开始到结束的实参都将被收集在'param'元组内，类似的，当我们以双星号声明一个形参，例如**param，则从这个位置开始到结束的实参都将会被收集在一个叫'param'的字典中。

对于元组和字典，我们后面有详细讲解。
9、关键字限定（Keyword-only）参数：

如果我们希望某些关键形参只能通过关键字实参的到而不是通过位置得到，我们可以将其声明在星号参数后面。

例如：
[python] view plaincopy

    <span style="font-family:Microsoft YaHei;font-size:18px;"># Filename: keyword_only.py   
         
    def total(initial=5, *numbers, extra_number):   
        count = initial   
        for number in numbers:   
            count += number   
        count += extra_number   
        print(count)   
         
    total(10, 1, 2, 3, extra_number=50)   
    total(10, 1, 2, 3)   
    # Raises error because we have not supplied a default argument value for 'extra_number'   
      
    </span>  


 
输出：

    C:\Users\Administrator>python D:\python\keyword_only.py
    66
    Traceback (most recent call last):
      File "D:\python\keyword_only.py", line 11, in <module>
        total(10, 1, 2, 3)
    TypeError: total() needs keyword-only argument extra_number 

工作原理：

在星号形参后声明的形成就成了关键字限定参数。如果没有为这些实参提供一个默认值，那么必须在调用函数时以关键字实参为其赋值，否则将引发错误，如上例所示。

注意这里用到的x+=y等同于x=x+y。如果你不需要星号形参只需要关键字限定形参则可以省略星号形参的参数名，如total(initial=5, *, extra_number)。
10、return语句

return语句用来从一个函数返回即跳出函数。我们也可选从函数返回一个值。

例如：
[python] view plaincopy

    <span style="font-family:Microsoft YaHei;font-size:18px;"># Filename: func_return.py   
         
    def maximum(x, y):   
        if x > y:   
            return x   
        elif x == y:   
            return 'The numbers are equal'  
        else:   
            return y   
         
    print(maximum(2, 3))</span>  


输出：

    C:\Users\Administrator>python D:\python\func_return.py
    3 

工作原理：

maximum函数返回参数中的最大值，在这里是提供给函数的数。它使用简单的if..else语句来找出较大的值，然后返回那个值。
注意，没有返回值的return语句等价于return None。None是Python中表示没有任何东西的特殊类型。例如，如果一个变量的值为None，可以表示它没有值。
除非你提供你自己的return语句，每个函数都在结尾暗含有return None语句。通过运行print someFunction()，你可以明白这一点，函数someFunction没有使用return语句，如同：
 
[python] view plaincopy

    <span style="font-family:Microsoft YaHei;font-size:18px;">def someFunction():   
        pass  
    print(someFunction())   
      
    </span>  


输出就是None。

提示：python已经包含了一个被称作max的内建函数，它的功能即是寻找最大值，你可以尽可能使用这个函数。
11、文档字符串（DocStrings）：

Python有一个很奇妙的特性，称为文档字符串，它通常被简称为docstrings。DocStrings是一个重要的工具，由于它帮助你的程序文档更加简单易懂。你甚至可以在程序运行的时候，从函数恢复文档字符串！

例如：
[python] view plaincopy

    <span style="font-family:Microsoft YaHei;font-size:18px;"># Filename: func_doc.py   
         
    def printMax(x, y):   
        '''''Prints the maximum of two numbers.  
        
        The two values must be integers.'''  
        x = int(x) # convert to integers, if possible   
        y = int(y)   
         
        if x > y:   
            print(x, 'is maximum')   
        else:   
            print(y, 'is maximum')   
         
    printMax(3, 5)   
    print(printMax.__doc__)   
      
    </span>  


输出：

    C:\Users\Administrator>python D:\python\func_doc.py
    5 is maximum
    Prints the maximum of two numbers.     The two values must be integers. 

工作原理：

在函数的第一个逻辑行的字符串是这个函数的文档字符串。同时，DocStrings也适用于模块和类，我们会在后面相应的章节学习它们。
文档字符串的惯例是一个多行字符串，它的首行以大写字母开始，句号结尾。第二行是空行，从第三行开始是详细的描述。 强烈建议在函数中使用文档字符串时遵循这个惯例。
你可以使用__doc__（注意双下划线）调用printMax函数的文档字符串属性（属于函数的名称）。Python把每一样东西都作为对象，包括这个函数。
如果你已经在Python中使用过help()，那么你已经看到过DocStings的使用了！它所做的只是抓取函数的__doc__属性，然后整洁地展示给你。你可以对上面这个函数尝试一下——只是在你的程序中包括help(printMax)。记住按q退出help。
自动化工具也可以以同样的方式从你的程序中提取文档。因此，我们强烈建议你对你所写的任何正式函数编写文档字符串。随你的Python发行版附带的pydoc命令，与help()类似地使用DocStrings。





python 系统学习笔记（四）--list
分类： python 2013-03-05 09:47 626人阅读 评论(3) 收藏 举报

目录(?)[+]

1、列表

列表是一种用于保存有序元素集合的数据结构，即你可以在列表中存储元素序列。考虑一个购物清单，上面有你需要购买的物品列表，只不过你可能希望以分号分隔他们而到Python变成了逗号。

列表元素被包含在方括号中，这样Python就会才会明白你指定的是一个列表。一旦列表创建完毕，我们可以对其元素进行添加，删除和搜索。正是因为可以执行添加和删除操作，我们将列表称作可变类型，即这种类型可以被修改。
对象和类的快速简介

列表是使用对象和类的一个例子。当我们对变量i赋值时，例如赋值5，这相当于创建一个Int类（类型）的对象（实例），你可以参看help(int)来更好的理解它。

一个类同样可以拥有方法，即函数，而他们只能应用与这个类。并且只有当你拥有一个类的对象时才能使用这些功能。例如，Python为列表提供了一个append方法允许我们将元素添加到列表的表尾。如，mylist.append(‘an item’)将字符串添加到列表mylist的尾部。注意，要用点号访问对象方法。

一个类还可以拥有字段，而字段只不过是专门应用与一个类的变量而已。当你拥有对应类的对象时就能使用这些变量了。字段同样利用点号访问，如，mylist.field。

1、list：列表（即动态数组，C++标准库的vector，但可含不同类型的元素于一个list中）
a = ["I","you","he","she"]      ＃元素可为任何类型。

下标：按下标读写，就当作数组处理
以0开始，有负下标的使用
0第一个元素，-1最后一个元素，
-len第一个元素，len-1最后一个元素
取list的元素数量                
len(list)   #list的长度。实际该方法是调用了此对象的__len__(self)方法。 

创建连续的list
L = range(1,5)      #即 L=[1,2,3,4],不含最后一个元素
L = range(1, 10, 2) #即 L=[1, 3, 5, 7, 9]

list的方法
L.append(var)   #追加元素
L.insert(index,var)
L.pop(var)      #返回最后一个元素，并从list中删除之
L.remove(var)   #删除第一次出现的该元素
L.count(var)    #该元素在列表中出现的个数
L.index(var)    #该元素的位置,无则抛异常 
L.extend(list)  #追加list，即合并list到L上
L.sort()        #排序
L.reverse()     #倒序
list 操作符:,+,*，关键字del
a[1:]       #片段操作符，用于子list的提取
[1,2]+[3,4] #为[1,2,3,4]。同extend()
[2]*4       #为[2,2,2,2]
del L[1]    #删除指定下标的元素
del L[1:3]  #删除指定下标范围的元素
list的复制
L1 = L      #L1为L的别名，用C来说就是指针地址相同，对L1操作即对L操作。函数参数就是这样传递的
L1 = L[:]   #L1为L的克隆，即另一个拷贝。
        
list comprehension
   [ <expr1> for k in L if <expr2> ]

定义list

li = ["a", "b", "mpilgrim", "z", "example"]

向 list 中增加元素

li.append("new") 

li.insert(2, "new")

li.extend(["two", "elements"])


extend (扩展) 与 append (追加) 的差别
Lists 的两个方法 extend 和 append 看起来类似，但实际上完全不同。extend 接受一个参数，这个参数总是一个 list，并且把这个 list 中的每个元素添加到原 list 中。
在这里 list 中有 3 个元素 ('a'、'b' 和 'c')，并且使用另一个有 3 个元素 ('d'、'e' 和 'f') 的 list 扩展之，因此新的 list 中有 6 个元素。
另一方面，append 接受一个参数，这个参数可以是任何数据类型，并且简单地追加到 list 的尾部。在这里使用一个含有 3 个元素的 list 参数调用 append 方法。
原来包含 3 个元素的 list 现在包含 4 个元素。为什么是 4 个元素呢？因为刚刚追加的最后一个元素本身是个 list。List 可以包含任何类型的数据，也包括其他的 list。这或许是您所要的结果，或许不是。如果您的意图是 extend，请不要使用 append。

List 运算符
Lists 也可以用 + 运算符连接起来。list = list + otherlist 相当于 list.extend(otherlist)。但 + 运算符把一个新 (连接后) 的 list 作为值返回，而 extend 只修改存在的 list。也就是说，对于大型 list 来说，extend 的执行速度要快一些。
Python 支持 += 运算符。li += ['two'] 等同于 li.extend(['two'])。+= 运算符可用于 list、字符串和整数，并且它也可以被重载用于用户自定义的类中 (更多关于类的内容参见 第 5 章)。
* 运算符可以作为一个重复器作用于 list。li = [1, 2] * 3 等同于 li = [1, 2] + [1, 2] + [1, 2]，即将三个 list 连接成一个。
list 的分片 (slice)

您可以通过指定 2 个索引得到 list的子集，叫做一个 “slice” 。返回值是一个新的 list，它包含了 list 中按顺序从第一个 slice 索引 (这里为 li[1]) 开始，直到但是不包括第二个 slice 索引 (这里为 li[3]) 的所有元素。

如果一个或两个 slice 索引是负数，slice 也可以工作。如果对您有帮助，您可以这样理解：从左向右阅读 list，第一个 slice 索引指定了您想要的第一个元素，第二个 slice 索引指定了第一个您不想要的元素。返回的值为在其间的每个元素。

List 从 0 开始，所以 li[0:3] 返回 list 的前 3 个元素，从 li[0] 开始，直到但不包括 li[3]。

Slice 简写

如果左侧分片索引为 0，您可以将其省略，默认为 0。所以 li[:3] 同  “list 的分片 (slice)” 的 li[0:3] 是一样的。

同样的，如果右侧分片索引是 list 的长度，可以将其省略。所以 li[3:] 同 li[3:5] 是一样的，因为这个 list 有 5 个元素。

请注意这里的对称性。在这个包含 5 个元素的 list 中，li[:3] 返回前 3 个元素，而 li[3:] 返回后 2 个元素。实际上，li[:n] 总是返回前 n 个元素，而 li[n:] 将返回剩下的元素，不管 list 有多长。

如果将两个分片索引全部省略，这将包括 list 的所有元素。但是与原始的名为 li 的 list 不同，它是一个新 list，恰好拥有与 li 一样的全部元素。li[:] 是生成一个 list 完全拷贝的一个简写。

检索列表的值，四种方式：in、not in、count、index，后两种方式是列表的方法。
示例列表：a_list = ['a','b','c','hello']：
判断值是否在列表中，in操作符：
'a' in a_list  //判断值a是否在列表中，并返回True或False

判断值是否不在列表，not in操作符：
'a' not in a_list   //判断a是否不在列表中，并返回True或False

统计指定值在列表中出现的次数，count方法：
a_list.count('a')  //返回a在列表中的出现的次数

在 list 中搜索

查看指定值在列表中的位置，index方法：
a_list.index('a')   //返回a在列表中每一次出现的位置，默认搜索整个列表
a_list.index('a',0,3)  //返回a在指定切片内第一次出现的位置


使用join链接list成为字符串

params = {"server":"mpilgrim", "database":"master", "uid":"sa", "pwd":"secret"}

";".join(["%s=%s" % (k, v) for k, v in params.items()])

输出'pwd=secret;database=master;uid=sa;server=mpilgrim

join 只能用于元素是字符串的 list; 它不进行任何的类型强制转换。连接一个存在一个或多个非字符串元素的 list 将引发一个异常。


利用list 传参数

def a(b,c):   

    print b,c   

a(1,2)   

a(*[1,2])

a(**{'b':1,'c':2})  #字典 


list的映射解析

   li = [1, 9, 8, 4]

   li = [elem*2 for elem in li]


列表过滤

li = ["a", "mpilgrim", "foo", "b", "c", "b", "d", "d"]

print [elem for elem in li if len(elem) > 1]

print [elem for elem in li if elem != "b"]

print [elem for elem in li if li.count(elem) == 1]

 


练习题

1.删除list里面的重复元素,并对其排序，然后对里面元素乘以2 并去掉大于10的元素

  输入input1=[1,3,5,1,7]  input2=[2,3,8] input3=[3,4]

  要求输出 b=[2,4,6,8,10] ，其过程要求用函数实现 参考上一章可变参数可变（VarArgs）参数

 举例 一下是一个简单的求和
[python] view plaincopy

    sum=0  
    a=[1,2,3,4]  
    def uniq(*Nums):  
        global sum  
        for num in Nums:  
            sum=num+sum  
      
    uniq(*a)  
    print sum  

提示过程，传入元素为list的list  然后遍历list 进行合并......




python 系统学习笔记（五）---字典
分类： python 2013-03-05 15:55 471人阅读 评论(4) 收藏 举报
字典类似于你通过联系人名字查找地址和联系人详细情况的地址簿，即，我们把键（名字）和值（详细情
况）联系在一起。注意，键必须是唯一的，就像如果有两个人恰巧同名的话，你无法找到正确的信息。
注意，你只能使用不可变的对象（比如字符串）来作为字典的键，但是你可以不可变或可变的对象作为字
典的值。基本说来就是，你应该只使用简单的对象作为键。
键值对在字典中以这样的方式标记：d = {key1 : value1, key2 : value2 }。注意它们的
键/值对用冒号分割，而各个对用逗号分割，所有这些都包括在花括号中。
记住字典中的键/值对是没有顺序的。如果你想要一个特定的顺序，那么你应该在使用前自己对它们排序。
字典是dict类的实例/对象。

dictionary： 字典（即C++标准库的map）
dict = {'ob1':'computer', 'ob2':'mouse', 'ob3':'printer'}
每一个元素是pair，包含key、value两部分。key是Integer或string类型，value 是任意类型。
键是唯一的，字典只认最后一个赋的键值。

    通过键来存取，而非偏移量；
    键值对是无序的；
    键和值可以是任意对象；
    长度可变，任意嵌套；
    在字典里，不能再有序列操作，虽然字典在某些方面与列表类似，但不要把列表套在字典上 

    字典的创建

    1.直接创建： phonebook={'Alice':'2341','Beth':'9102','Cecil':'3258'} （键和值用冒号隔开，各个键值对键用逗号隔开）

    2.dict函数：

        >>>items=[('name','Gumby'),('age',42)] #dict的第一种方法

        >>>d=dict(items)

            {'age':42,'name':'Gumby'}

        >>>d=dict(name='Gumby',age=42) #dict的第二种方法

            {'age':42,'name':'Gumby'}

        >>>d['***al']='boy'

             {'age':42,'name':'Gumby','***ual':'boy'}

    *字典的格式化字符串* （很有用）

    在转换说明符中的%字符后面，可以加上用圆括号括起来的键，后面再更其他说明元素。

        phonebook={'Alice':'2341','Beth':'9102','Cecil':'3258'} 

        print "Alice's phone number is
        %(Alice)s,Cecil's phone number is %(Cecil)s"%phonebook #%(Alice)s的s表示输出字符串的意思

    -->Alice's phone number is 2341,Cecil's phone number is 3258

    注：这类字符串格式化在模板系统中非常有用，string.Template类对于这类应用也是非常有用的。

    字典的方法

    1.clear

       清除字典中的所有项：

        phonebook.clear() #phonebook是一个字典对象

    2.copy

       浅复制：只复制字典中的父对象，对子对象采取引用的办法。改变子对象的内容会影响到复制和被复制的字典。

    例：

        x={'username':'admin','machines':['foo','bar','baz']}

        y=x.copy()

        y['username']='Allen'

        y['machines'].remove('bar')

        print y

        print x

    输出：


        {'username':'Allen','machines':['foo','baz']} #y
        {'username':'admin','machines':['foo','baz']} #x 链表子对象中的值改变，会影响两个字典。子对象采取应用的方法。

    深度复制：deepcopy，完全复制，新字典的改变不会影响原来的字典。

        from copy import deepcopy

        d={'username':'admin','machines':['foo','bar','baz']}

        dc=deepcopy(d)

    3.fromkeys
       使用给定的键建立新的字典，每个键默认对应的值为None。

        {}.fromkeys(['name','age'])
        -->{'age':None,'name':None}
        dict.fromkeys(['name','age']) #直接在所有字典的类型dict上调用方法
        -->{'age':None,'name':None}
        dict.fromkeys(['name','age'],'(unknown)') #不用None作为默认值，自己提供默认值
        -->{'age':'(unknown)','name':'(unknown)' }

    4.get

        get方法是一个更宽松的访问字典项的方法。一般来说，如果试图访问字典中不存在的项时会出错。而用get就不会。

    例：

        >>>d={} #空字典

        >>>print d['name']

        Traceback........ #出错信息

        >>>print d.get('name')

        None  #可以自定义默认值代替None，如d.get('name','N/A'),如果没有name项就打印N/A

    5.has_key
       检查字典中是否含有给出的键。d.has_key(k)相当于k in d。Python3.0中不包含这个函数。

        >>>d={'name':'fuss'}

        >>>d.has_key('age')

        False

        >>>d.has_key('name')

        True

    6.items和iteritems
       items方法将所有字典项以列表方式返回。

        >>> d={'name':'fu','age':24,'***al':'boy'}

        >>> d.items()

        [('age', 24), ('name', 'fu'), ('***ual', 'boy')] #返回没有特殊的顺序


       iteritems方法的作用大致相同，但是会返回一个迭代器对象而不是列表：

        >>> it=d.iteritems()

        >>> it

        <dictionary-itemiterator object at 0x011DA580>

        >>> list(it)

        [('age', 24), ('name', 'fu'), ('***ual', 'boy')]

    7.keys和iterkeys
        keys方法将字典中的键以列表的形式返回，而iterkeys则返回针对键的迭代器。

        >>>d.keys()

        ['age', 'name', '***ual']

    8.pop
       用来获得对应于给定键的值，然后将这个键值对从字典移除。

        >>>d={'x':1,'y':2}

        >>>d.pop('x')

        1

        >>>d

        {'y':2}

    9.popitem
       popitem方法类似于list.pop，后者会弹出列表的最后一个元素。但popitem弹出的是随机项，以为字典没有"最后的元素"或其他有关顺序的概念。

        >>>d={'x':1,'y':2} 

        >>>d.popitem()

        ('x':1) #随机的

    10.setdefault
         setdefault方法在某种程度上类似于get方法，就是能获得与给定键关联的值，除此之外，setdefault还能在字典中不含有给定键的情况下设定相应的键值。

        >>>d={}

        >>>d.setdefault('name','N/A')

        'N/A'

        >>>d

        {'name':'N/A'}

        >>>d['name']='Allen'

        >>>d.setdefault('name','N/A')

        'Allen'

        >>>d

        {'name':'Allen'}

    11.update
         update方法可以利用一个字典项更新另外一个字典：

        >>>d={'x':123,'y':456}

        >>>b={'x':789}

        >>>d.update(b)

        >>>d

        {'x':789,'y':456}

         提供的字典项会被添加到旧的字典中，若有相同的键则会进行覆盖。
    12.values和itervalues
         values方法一列表的形式返回字典的值(itervalues返回值的迭代器)。与keys对比(对应关系)
    例：

        d={'name':'fu','age':24,'***ual':'boy'}

        print d.keys()       #返回字典的键列表    

        print d.values()    #返回字典的值列表

        ['age', 'name', '***ual']  #键列表 

        ['fu',24,'boy']   #值列表


[python] view plaincopy

    # -*- coding: cp936 -*-  
      
    params = {"server":"mpilgrim", "database":"master", "uid":"sa", "pwd":"secret"}  
      
    print params.keys()    
      
    print  params.values()  
      
    print params.items()  
      
    print [ k for k, v in params.items()]                 
      
    print [v for k, v in params.items()]                 
      
    print ["%s=%s" % (k, v) for k, v in params.items()]  
      
    print params.has_key("server")  
      
    print params.get("server")  
      
    params["pwd"]="hello pwd"  
      
    print params.items()  
      
    params["hello list"]=[1,2,3,4]  
      
    print params  
      
    del params["hello list"]  
      
    print params  
      
    #print params.popitem()  
      
    #遍历  
      
    for key in params.keys():  
      
        print key, '\t', params[key]  
      
          
      
    #join 连接  
      
    print ";".join(["%s=%s" % (k, v*2) for k, v in params.items()])  


 

利用字典实现简单的switch 类型语句。（第一节的时候已经写了）

[python] view plaincopy

    from __future__ import division  
      
    x = 1  
      
    y = 2  
      
    operator = "/"  
      
    result = {  
      
        "+" : x + y,  
      
        "-" : x - y,  
      
        "*" : x * y,  
      
        "/" : x / y  
      
    }  
      
    print result.get(operator)  



练习题

颠倒字典中的键和值。用一个字典做输入，输出另一个字典，用前者的键做值，前者的
值做键。但是如果value 相同要求保留前一个 由于字典本身的顺序是不定的 所以可能先保存ghi 也可能是jkl （本题稍微有点问题 别较真 本身没事实际意义）

   d={'abc':1,'def':2,'ghi':3,'jkl':3}  =》{1:'abc',2:'def',3:'ghi'(or 3:jkl)}
   
   




python 系统学习笔记（六）---元组
分类： python 2013-03-06 16:41 397人阅读 评论(2) 收藏 举报
元组
元组和列表十分类似，只不过元组和字符串一样是 不可变的  即你不能修改元组。元组通过圆括号中用逗号
分割的项目定义。元组通常用在使语句或用户定义的函数能够安全地采用一组值的时候，即被使用的元组
的值不会改变。
使用元组

    #元组由不同的元素组成，每个元素可以存储不同类型的数据，例如   字符串、数字和元组   
    #元组通常代表一行数据，而元组中的元素则代表不同的数据项   
    创建元组，不定长，但一旦创建后则不能修改长度     
    空元组   tuple_name = ()  
    #如果创建的元组只有一个元素，那么该元素后面的逗号是不可忽略的  （１，）
    #不可修改元素   

    >>> user=(1,2,3)  
    >>> user[0]=2  
      
    Traceback (most recent call last):  
      File "<pyshell#5>", line 1, in <module>  
        user[0]=2  
    TypeError: 'tuple' object does not support item assignment 

     
    [python] view plaincopy

        test=(1,2,3,4)  
        print test  
        test=(test,5)  
        print test  
        #元组支持＋和切片操作   
        test= test[:1][0]   
        print test  
        #print type(test)  
        print dir(tuple)  
          
        add=(5,)  
        test=test+add  
        print test  
          
        #去重  
        print set((2,2,3,4,4))  
          
        #解包     
        test = (1,2,3)    
        a,b,c = test  
        print a,b,c  
          
        #遍历  
        for elem in test:  
            print elem  
          
        for item in range(len(test)):  
            print test[item]  
          
        #二元遍历  
        test=((1,2),(3,4),(5,6))  
        for elem in test:  
            for item in elem:  
                print item  


     

    习题
     #求序列类型的最大元素和最小元素

    #入口 :  序列类型List

    #返回 : ( 最大元素, 最小元素 ) 应用元组

     

    sample
    [python] view plaincopy

        def hello():  
            return 'hello','world'  
        print hello()  



python 系统学习笔记（七）---set
分类： python 2013-03-06 22:05 402人阅读 评论(0) 收藏 举报

    python的set和其他语言类似, 是一个无序不重复元素集, 基本功能包括关系测试和消除重复元素. 集合对象还支持union(联合), intersection(交), difference(差)和sysmmetric difference(对称差集)等数学运算.  
    sets 支持 x in set, len(set),和 for x in set。作为一个无序的集合，sets不记录元素位置或者插入点。因此，sets不支持 indexing, slicing, 或其它类序列（sequence-like）的操作。 

Help on class set in module __builtin__:

class set(object)
 |  set() -> new empty set object
 |  set(iterable) -> new set object
 |  
 |  Build an unordered collection of unique elements.
 |  
 |  Methods defined here:
 |  
 |  __and__(...)
 |      x.__and__(y) <==> x&y
 |  
 |  __cmp__(...)
 |      x.__cmp__(y) <==> cmp(x,y)
 |  
 |  __contains__(...)
 |      x.__contains__(y) <==> y in x.
 |  
 |  __eq__(...)
 |      x.__eq__(y) <==> x==y
 |  
 |  __ge__(...)
 |      x.__ge__(y) <==> x>=y
 |  
 |  __getattribute__(...)
 |      x.__getattribute__('name') <==> x.name
 |  
 |  __gt__(...)
 |      x.__gt__(y) <==> x>y
 |  
 |  __iand__(...)
 |      x.__iand__(y) <==> x&y
 |  
 |  __init__(...)
 |      x.__init__(...) initializes x; see help(type(x)) for signature
 |  
 |  __ior__(...)
 |      x.__ior__(y) <==> x|y
 |  
 |  __isub__(...)
 |      x.__isub__(y) <==> x-y
 |  
 |  __iter__(...)
 |      x.__iter__() <==> iter(x)
 |  
 |  __ixor__(...)
 |      x.__ixor__(y) <==> x^y
 |  
 |  __le__(...)
 |      x.__le__(y) <==> x<=y
 |  
 |  __len__(...)
 |      x.__len__() <==> len(x)
 |  
 |  __lt__(...)
 |      x.__lt__(y) <==> x<y
 |  
 |  __ne__(...)
 |      x.__ne__(y) <==> x!=y
 |  
 |  __or__(...)
 |      x.__or__(y) <==> x|y
 |  
 |  __rand__(...)
 |      x.__rand__(y) <==> y&x
 |  
 |  __reduce__(...)
 |      Return state information for pickling.
 |  
 |  __repr__(...)
 |      x.__repr__() <==> repr(x)
 |  
 |  __ror__(...)
 |      x.__ror__(y) <==> y|x
 |  
 |  __rsub__(...)
 |      x.__rsub__(y) <==> y-x
 |  
 |  __rxor__(...)
 |      x.__rxor__(y) <==> y^x
 |  
 |  __sizeof__(...)
 |      S.__sizeof__() -> size of S in memory, in bytes
 |  
 |  __sub__(...)
 |      x.__sub__(y) <==> x-y
 |  
 |  __xor__(...)
 |      x.__xor__(y) <==> x^y
 |  
 |  add(...)
 |      Add an element to a set.
 |      
 |      This has no effect if the element is already present.
 |  
 |  clear(...)
 |      Remove all elements from this set.
 |  
 |  copy(...)
 |      Return a shallow copy of a set.
 |  
 |  difference(...)
 |      Return the difference of two or more sets as a new set.
 |      
 |      (i.e. all elements that are in this set but not the others.)
 |  
 |  difference_update(...)
 |      Remove all elements of another set from this set.
 |  
 |  discard(...)
 |      Remove an element from a set if it is a member.
 |      
 |      If the element is not a member, do nothing.
 |  
 |  intersection(...)
 |      Return the intersection of two or more sets as a new set.
 |      
 |      (i.e. elements that are common to all of the sets.)
 |  
 |  intersection_update(...)
 |      Update a set with the intersection of itself and another.
 |  
 |  isdisjoint(...)
 |      Return True if two sets have a null intersection.
 |  
 |  issubset(...)
 |      Report whether another set contains this set.
 |  
 |  issuperset(...)
 |      Report whether this set contains another set.
 |  
 |  pop(...)
 |      Remove and return an arbitrary set element.
 |      Raises KeyError if the set is empty.
 |  
 |  remove(...)
 |      Remove an element from a set; it must be a member.
 |      
 |      If the element is not a member, raise a KeyError.
 |  
 |  symmetric_difference(...)
 |      Return the symmetric difference of two sets as a new set.
 |      
 |      (i.e. all elements that are in exactly one of the sets.)
 |  
 |  symmetric_difference_update(...)
 |      Update a set with the symmetric difference of itself and another.
 |  
 |  union(...)
 |      Return the union of sets as a new set.
 |      
 |      (i.e. all elements that are in either set.)
 |  
 |  update(...)
 |      Update a set with the union of itself and others.
 |  
 |  ----------------------------------------------------------------------
 |  Data and other attributes defined here:
 |  
 |  __hash__ = None
 |  
 |  __new__ = <built-in method __new__ of type object>
 |      T.__new__(S, ...) -> a new object with type S, a subtype of T



[python] view plaincopy

    # -*- coding: cp936 -*-  
    print help(set)  
    #set(['h', 'e', 'l', 'o'])  
    x=set('hello')  
    print x  
    #set(['d', 'r', 'o', 'w', 'l'])  
    y=set('world')  
    print y  
      
      
    # 交集  
    print x&y  
    #set(['l', 'o'])  
      
      
    print x|y  # 并集  
    #set(['e', 'd', 'h', 'l', 'o', 'r', 'w'])  
      
      
    print x - y # 差集   
    #set(['h', 'e'])  
      
      
    print x^y # 对称差集（项在t或s中，但不会同时出现在二者中）  
      
      
      
      
    a = [11,22,33,44,11,22]    
    print  set(a)    
    #set([33, 11, 44, 22])  
      
      
    print [i for i in set(a)] #去重  
      
      
    s=set('hello')  
    t=set('world')  
    len(s)  
      
      
    #set 的长度  
    x='h'  
    print x in s  
      
      
    #测试 x 是否是 s 的成员  
      
      
    x not in s  
      
      
    #测试 x 是否不是 s 的成员  
      
      
    s.issubset(t)  
    #s <= t  
    #测试是否 s 中的每一个元素都在 t 中  
      
      
    s.issuperset(t)  
    s >= t  
    #测试是否 t 中的每一个元素都在 s 中  
      
      
    s.union(t)  
    s | t  
    #返回一个新的 set 包含 s 和 t 中的每一个元素  
      
      
    s.intersection(t)  
    s & t  
    #返回一个新的 set 包含 s 和 t 中的公共元素  
      
      
    s.difference(t)  
    s - t  
    #返回一个新的 set 包含 s 中有但是 t 中没有的元素  
      
      
    s.symmetric_difference(t)  
    s ^ t  
    #返回一个新的 set 包含 s 和 t 中不重复的元素  
      
      
    s.copy()  
      
      
    #回 set “s”的一个浅复制  




    请注意：union(), intersection(), difference() 和 symmetric_difference() 的非运算符（non-operator，就是形如 s.union()这样的）版本将会接受任何 iterable 作为参数。相反，它们的运算符版本（operator based counterparts）要求参数必须是 sets。这样可以避免潜在的错误，如：为了更可读而使用 set('abc') & 'cbs' 来替代 set('abc').intersection('cbs')。从 2.3.1 版本中做的更改：以前所有参数都必须是 sets。   
    另外，Set 和 ImmutableSet 两者都支持 set 与 set 之间的比较。两个 sets 在也只有在这种情况下是相等的：每一个 set 中的元素都是另一个中的元素（二者互为subset）。一个 set 比另一个 set 小，只有在第一个 set 是第二个 set 的 subset 时（是一个 subset，但是并不相等）。一个 set 比另一个 set 打，只有在第一个 set 是第二个 set 的 superset 时（是一个 superset，但是并不相等）
     子 set 和相等比较并不产生完整的排序功能。例如：任意两个 sets 都不相等也不互为子 set，因此以下的运算都会返回 False：a<b, a==b, 或者a>b。因此，sets 不提供 __cmp__ 方法。  
    因为 sets 只定义了部分排序功能（subset 关系），list.sort() 方法的输出对于 sets 的列表没有定义。
    
    
    
    




python 系统学习笔记（八）---文件操作
分类： python 2013-03-07 09:59 518人阅读 评论(1) 收藏 举报

模式
	

描述

r
	

以读方式打开文件，可读取文件信息。

w
	

以写方式打开文件，可向文件写入信息。如文件存在，则清空该文件，再写入新内容

a
	以追加模式打开文件（即一打开文件，文件指针自动移到文件末尾），如果文件不存在则创建

r+
	

以读写方式打开文件，可对文件进行读和写操作。

w+
	

消除文件内容，然后以读写方式打开文件。

a+
	

以读写方式打开文件，并把文件指针移到文件尾。

b
	

以二进制模式打开文件，而不是以文本模式。该模式只对Windows或Dos有效，类Unix的文件是用二进制模式进行操作的。

 文件对象方法

方法
	

描述

f.close()
	

关闭文件，记住用open()打开文件后一定要记得关闭它，否则会占用系统的可打开文件句柄数。

f.fileno()
	

获得文件描述符，是一个数字

f.flush()
	

刷新输出缓存

f.isatty()
	

如果文件是一个交互终端，则返回True，否则返回False。

f.read([count])
	

读出文件，如果有count，则读出count个字节。

f.readline()
	

读出一行信息。

f.readlines()
	

读出所有行，也就是读出整个文件的信息。

f.seek(offset[,where])
	

把文件指针移动到相对于where的offset位置。where为0表示文件开始处，这是默认值 ；1表示当前位置；2表示文件结尾。

f.tell()
	

获得文件指针位置。

f.truncate([size])
	

截取文件，使文件的大小为size。

f.write(string)
	

把string字符串写入文件。

f.writelines(list)
	

把list中的字符串一行一行地写入文件，是连续写入文件，没有换行。


fp.read([size])                     #size为读取的长度，以byte为单位

fp.readline([size])                 #读一行，如果定义了size，有可能返回的只是一行的一部分

fp.readlines([size])                #把文件每一行作为一个list的一个成员，并返回这个list。其实它的内部是通过循环调用readline()来实现的。如果提供size参数，size是表示读取内容的总长，也就是说可能只读到文件的一部分。

fp.write(str)                      #把str写到文件中，write()并不会在str后加上一个换行符

fp.writelines(seq)            #把seq的内容全部写到文件中(多行一次性写入)。这个函数也只是忠实地写入，不会在每行后面加上任何东西。

fp.close()                        #关闭文件。python会在一个文件不用后自动关闭文件，不过这一功能没有保证，最好还是养成自己关闭的习惯。  如果一个文件在关闭后还对其进行操作会产生ValueError

fp.flush()                                      #把缓冲区的内容写入硬盘

fp.fileno()                                      #返回一个长整型的”文件标签“

fp.isatty()                                      #文件是否是一个终端设备文件（unix系统中的）

fp.tell()                                         #返回文件操作标记的当前位置，以文件的开头为原点

fp.next()                                       #返回下一行，并将文件操作标记位移到下一行。把一个file用于for … in file这样的语句时，就是调用next()函数来实现遍历的。

fp.seek(offset[,whence])              #将文件打操作标记移到offset的位置。这个offset一般是相对于文件的开头来计算的，一般为正数。但如果提供了whence参数就不一定了，whence可以为0表示从头开始计算，1表示以当前位置为原点计算。2表示以文件末尾为原点进行计算。需要注意，如果文件以a或a+的模式打开，每次进行写操作时，文件操作标记会自动返回到文件末尾。

fp.truncate([size])                       #把文件裁成规定的大小，默认的是裁到当前文件操作标记的位置。如果size比文件的大小还要大，依据系统的不同可能是不改变文件，也可能是用0把文件补到相应的大小，也可能是以一些随机的内容加上去。

 
[python] view plaincopy

    # -*- coding: cp936 -*-  
    poem = '''''\ 
    Programming is fun 
    When the work is done 
    if you wanna make your work also fun: 
            use Python! 
    '''  
    #写文件  
    f=open('hello.txt','w')  
    print f.write(poem)  
    f.close  
       
    #读文件  
    f=open('hello.txt','r')  
    rf=f.readlines()  
    for elem in rf:  
        print elem  
    f.close  
      
      
    #追加  
    f=open('hello.txt','a+')  
    f.write('thank you')  
    f.close  
              
    #按行读文件  
    f = open('hello.txt','r')     
    line=f.readline()  
    while line:  
        print line     
        line=f.readline()#如果没有这行会造成死循环     
    #f.close   
      
    #f = open('hello.txt','r')     
    print 'read() function:'              #读取整个文件     
    print f.read()  
    #f.close()  
      
    print 'readline() function:'          #返回文件头，读取一行     
    f.seek(0)     
    print f.readline()  
    print f.tell()              #显示当前位置    
    f.seek(20) #第二行开始  
    print f.readline()  
      
    f.close()  


 

练习题

把字典dic={key1:value1,key2:value2,key3:value3}写入 ini 文件 保存为 key1=value1 .... 然后再读回 ini 的配置保存到字典。








 python 系统学习笔记（九）---异常处理
分类： python 2013-03-11 16:27 512人阅读 评论(2) 收藏 举报

     Python的异常处理能力是很强大的，可向用户准确反馈出错信息。在Python中，异常也是对象，可对它进行操作。所有异常都是基类Exception的成员。所有异常都从基类Exception继承，而且都在exceptions模块中定义。Python自动将所有异常名称放在内建命名空间中，所以程序不必导入exceptions模块即可使用异常。一旦引发而且没有捕捉SystemExit异常，程序执行就会终止。如果交互式会话遇到一个未被捕捉的SystemExit异常，会话就会终止。

try/except：捕捉由代码中的异常并恢复，匹配except里面的错误，并自行except中定义的代码，后继续执行程序（发生异常后，由except捕捉到异常后，不会中断程序，继续执行try语句后面的程序）
try/finally: 无论异常是否发生，都执行清理行为 （发生异常时程序会中断程序，只不过会执行finally后的代码）
raise: 手动在代码中接触发异常。
assert: 有条件地在程序代码中触发异常。
with/as  实现环境管理器。



1:try语句:

该种异常处理语法的规则是：

    执行try下的语句，如果引发异常，则执行过程会跳到第一个except语句。
    如果第一个except中定义的异常与引发的异常匹配，则执行该except中的语句。
      如果引发的异常不匹配第一个except，则会搜索第二个except，允许编写的except数量没有限制。
    如果所有的except都不匹配，则异常会传递到下一个调用本代码的最高层try代码中。
    如果没有发生异常，则执行else块代码。
    用户定义的异常要写成类的实例，而不是字符串。
    finally可以和except和else分句出现在相同的try语句内

1.1使用try和except语句来捕获异常

try:
   block
except [exception,[data…]]:
   block

try的完整形式：try/多个except/else语句
else是可选的
try首行底下的代码块代表此语句的主要动作：试着执行的程序代码。except分句定义try代码块内引发的异常处理器，而else分句（如果有）则是提供没有发生异常时候要执行的处理器。
 
import sys
try:
    s=raw_input('test EOFError:')
except EOFError:
    print 'meet EOError'
    sys.exit()
except:
    print 'hello'
1.2 使用try跟finally:

语法如下:

try:
   block
finally:
   block

该语句的执行规则是：

·   执行try下的代码。

·   如果发生异常，在该异常传递到下一级try时，执行finally中的代码。

·   如果没有发生异常，则执行finally中的代码。

第二种try语法在无论有没有发生异常都要执行代码的情况下是很有用的。例如我们在python中打开一个文件进行读写操作，我在操作过程中不管是否出现异常，最终都是要把该文件关闭的。

这两种形式相互冲突，使用了一种就不允许使用另一种，而功能又各异

try:

    f=open('dic.ini','w')

    f.write('123')

finally:

    print 'close file'

    f.close()

1.3 统一try/except/finally分句
try:
    main-action:
except Exception1:
    hander1
except Exception2:
    hander2
...
else:
    else-block
finally:
    finally-block
这语句中main-action代码会先执行。如果该程序代码（main-action）引发异常，那么except代码块都会逐一测试，寻找与抛出的异常相符的语句。如果引发异常的是Exception1则会执行hander1代码块，如果引发异常的是Exception2，则会执行hander2代码块。以此类推。如果没有
引发异常，将会执行else-block代码块。
无论前面发生什么，当main-action代码块完成时。finally-block都会执行。


2.用raise语句手工引发一个异常: 自定义异常

raise [exception[,data]]

在Python中，要想引发异常，最简单的形式就是输入关键字raise，后跟要引发的异常的名称。异常名称标识出具体的类：Python异常是那些类的对象。执行raise语句时，Python会创建指定的异常类的一个对象。raise语句还可指定对异常对象进行初始化的参数。为此，请在异常类的名称后添加一个逗号以及指定的参数（或者由参数构成的一个元组）。

例:

try:
    raise MyError #自己抛出一个异常
except MyError:
    print 'a error'


[python] view plaincopy

    class  ShortInputException(Exception):  
      
        '''''A user-defined exception class.'''  
      
        def  __init__ (self, length, atleast):  
      
            Exception.__init__(self)  
      
            self.length = length  
      
            self.atleast = atleast  
      
    try :  
      
        s =  raw_input( 'Enter something --> ')  
      
        if len (s) <  3 :  
      
            raise ShortInputException(len (s), 3 )  
      
        # Other work can continue as usual here  
      
    except EOFError:  
      
        print '\nWhy did you do an EOF on me?'  
      
    except ShortInputException, x:  
      
        print 'ShortInputException: The input was of length %d, \  
      
        was expecting at least %d' % (x.length,x.atleast)  
      
    else:  
      
        print 'No exception was raised.'  



assert可以有条件地在程序代码中触发异常，可以认为是有条件的raise.
牢记：assert几乎都是用来收集用户定义的约束条件，而不是捕捉内在的程序设计错误。因为Python会自动收集程序的设计错误，通常咩有必要写assert去捕捉超出索引值，类型不匹配以及除数为0之类的事。
引发的异常为:AssertionError。如果没有被try捕捉到，就会终止程序。
该语句形式:
assert  <test>,<data>


[python] view plaincopy

    def f(x):  
      
        assert x>0,'x must be great zerot'  
      
        return x**2  
      
    f(-1)  



3.内置Exception类
Python把内置异常组织成层次，来支持各种捕捉模式
Exception：    异常的顶层根超类
StandardError:    所有内置错误异常的超类
ArithmeticError:    所有数值错误的超类
OverflowError:    识别特定的数值错误的子类

[python] view plaincopy

    import exceptions  
      
    help(exceptions)  


4. 采用sys模块回溯最后的异常

import sys
try:
   block
except:
   info=sys.exc_info()
   print info[0],":",info[1]

或者以如下的形式:

import sys
    tp,val,td = sys.exc_info()

sys.exc_info()的返回值是一个tuple, (type, value/message, traceback)

这里的type ---- 异常的类型

value/message ---- 异常的信息或者参数

traceback ---- 包含调用栈信息的对象。

从这点上可以看出此方法涵盖了traceback.

习题：

利用异常打印出行号和函数名称 利用 raise

提示：
[python] view plaincopy

    f = sys.exc_info()[2].tb_frame.f_back  
    f.f_code.co_name, f.f_lineno  #函数名 行号  


sayHello
[python] view plaincopy

    import sys  
    class  PrintNameLine(Exception):  
      
        def  __init__ (self,say):  
      
            Exception.__init__(self)  
      
            self.sayhello = say  
      
                   
    try :  
        raise PrintNameLine('helloworld')  
    except PrintNameLine,x:  
        print '%s'%(x.sayhello )  
        
        
        
        
        
        




python 系统学习笔记（十）---类
分类： python 2013-03-11 19:15 334人阅读 评论(0) 收藏 举报
self
类的方法与普通的函数只有一个特别的区别——它们必须有一个额外的第一个参数名称，但是在调用这个
方法的时候你不为这个参数赋值，Python会提供这个值。这个特别的变量指对象本身，按照惯例它的名称
是self。
虽然你可以给这个参数任何名称，但是 强烈建议  你使用self这个名称——其他名称都是不赞成你使用
的。 
Python中的self等价于C++中的self指针和Java、C#中的this参考。
你一定很奇怪Python如何给self赋值以及为何你不需要给它赋值。举一个例子会使此变得清晰。假如你
有一个类称为MyClass和这个类的一个实例MyObject 。当你调用这个对象的方
法MyObject.method(arg1, arg2) 的时候，这会由Python自动转
为MyClass.method(MyObject, arg1, arg2) ——这就是self的原理了。
这也意味着如果你有一个不需要参数的方法，你还是得给这个方法定义一个self参数。

类对象支持两种操作：属性引用和实例化。

属性引用使用和Python中所有的属性引用一样的标准语法： obj.name。类对象创建后，类命名空间中所有的命名都是有效属性名。所以如果类定义是这样：

class MyClass:
    "A simple example class"
    i = 12345
    def f(self):
        return 'hello world'

那么 MyClass.i 和 MyClass.f 是有效的属性引用，分别返回一个整数和一个方法对象。也可以对类属性赋值，你可以通过给 MyClass.i 赋值来修改它。 __doc__ 也是一个有效的属性，返回类的文档字符串：“A simple example class”。

类的实例化使用函数符号。只要将类对象看作是一个返回新的类实例的无参数函数即可。例如（假设沿用前面的类）：

x = MyClass()

以上创建了一个新的类实例并将该对象赋给局部变量x。

这个实例化操作（“调用”一个类对象）来创建一个空的对象。很多类都倾向于将对象创建为有初始状态的。因此类可能会定义一个名为__init__() 的特殊方法，像下面这样：

    def __init__(self):
        self.data = []

类定义了 __init__() 方法的话，类的实例化操作会自动为新创建的类实例调用 __init__() 方法。所以在下例中，可以这样创建一个新的实例：

x = MyClass()

当然，出于弹性的需要， __init__() 方法可以有参数。事实上，参数通过 __init__()传递到类的实例化操作上。

__init__方法

在Python的类中有很多方法的名字有特殊的重要意义。现在我们将学习__init__ 方法的意义。

__init__ 方法在类的一个对象被建立时，马上运行。这个方法可以用来对你的对象做一些你希望的 初始

化 。注意，这个名称的开始和结尾都是双下划线。

使用__init__方法


[python] view plaincopy

    class SayHello:  
      
        def __init__(self,hellostr):  
      
            self.data = hellostr  
      
        def sayHi(self,histr):  
      
            print 'hi'  
      
    x=SayHello("hello world")  
      
    print '%s' %(x.data)  
      
    x.sayHi('hi')  




第一种称作数据属性。这相当于Smalltalk中的“实例变量”或C++中的“数据成员”。和局部变量一样，数据属性不需要声明，第一次使用时它们就会生成
 self.data 就是数据属性

第二种方法属性

通常方法是直接调用的：

x.sayHi('hi')

在我们的例子中，这会返回字符串‘hi’。 



继承

当然，如果一种语言不支持继承就，“类”就没有什么意义。派生类的定义如下所示：

class DerivedClassName(BaseClassName):
    
    .
    .
    .
    

命名 BaseClassName （示例中的基类名）必须与派生类定义在一个作用域内。除了类，还可以用表达式，基类定义在另一个模块中时这一点非常有用：

class DerivedClassName(modname.BaseClassName):

派生类定义的执行过程和基类是一样的。构造派生类对象时，就记住了基类。这在解析属性引用的时候尤其有用：如果在类中找不到请求调用的属性，就搜索基类。如果基类是由别的类派生而来，这个规则会递归的应用上去。

派生类的实例化没有什么特殊之处：DerivedClassName() （示列中的派生类）创建一个新的类实例。方法引用按如下规则解析：搜索对应的类属性，必要时沿基类链逐级搜索，如果找到了函数对象这个方法引用就是合法的。

派生类可能会覆盖其基类的方法。因为方法调用同一个对象中的其它方法时没有特权，基类的方法调用同一个基类的方法时，可能实际上最终调用了派生类中的覆盖方法。（对于C++程序员来说，Python中的所有方法本质上都是虚方法。）

派生类中的覆盖方法可能是想要扩充而不是简单的替代基类中的重名方法。有一个简单的方法可以直接调用基类方法，只要调用：“BaseClassName.methodname(self, arguments)”。有时这对于客户也很有用。（要注意的中只有基类在同一全局作用域定义或导入时才能这样用。）

 
[python] view plaincopy

    #!/usr/bin/python  
      
    # Filename: inherit.py  
      
    class  SchoolMember:  
      
        '''''Represents any school member.'''  
      
        def  __init__ (self, name, age):  
      
            self.name = name  
      
            self.age = age  
      
            print  '(Initialized SchoolMember: %s)'  %self.name  
      
        def  tell(self):  
      
            '''''Tell my details.'''  
      
            print  'Name:"%s" Age:"%s"'  % (self.name,self.age),  
      
    class  Teacher(SchoolMember):  
      
        '''''Represents a teacher.'''  
      
        def  __init__ (self, name, age, salary):  
      
            SchoolMember.__init__(self, name, age)  
      
            self.salary = salary  
      
            print '(Initialized Teacher: %s)'  % self.name  
      
        def  tell(self):  
      
            SchoolMember.tell(self)  
      
            print 'Salary: "%d"' % self.salary  
      
    class  Student(SchoolMember):  
      
        '''''Represents a student.'''  
      
        def  __init__ (self, name, age, marks):  
      
            SchoolMember.__init__(self, name, age)  
      
            self.marks = marks  
      
            print '(Initialized Student: %s)'  % self.name  
      
        def  tell(self):  
      
            SchoolMember.tell(self)  
      
            print 'Marks: "%d"' % self.marks  
      
    t = Teacher('Mrs. Shrividya' , 40, 30000)  
      
    s = Student('Swaroop', 22, 75)  
      
    print  # prints a blank line  
      
    members = [t, s]  
      
    for member in  members:  
      
        member.tell()  # works for both Teachers and Students  



四、运算符重载
重载的关键概念
*运算符重载让类拦截常规的Python运算。
*类可重载所有Python表达式运算。
*类可重载打印，函数调用，属性点号运算等运算。
*重载使类实例的行为像内置类型。
*重载是通过提供特殊名称的类方法来实现的。
如果类中提供了某些特殊名称的方法，当类实例出现在运算有关的表达式的时候，Python就会自动调用这些方法。
1、 常见的运算符重载方法
方法        重载        调用
__init__    构造器方法    对象建立：X=Class()
__del__        析构方法    对象收回
__add__        运算符+        X+Y,X+=Y
__sub__        运算符-        X-Y,X-=Y
__or__        运算符|(位OR)    X|Y X|=Y
__repr__,__str__ 打印，转换    print X【__str__】、repr(X)、str(X)
__call__    函数调用    X()
__getattr__    点号运算    X.undefined
__setattr__    属性赋值语句    X.any=Value
__getitem__    索引运算    X[key],没有__iter__时的for循环和其他迭代器
__setitem__    索引赋值语句    X[key]=value
__len__        长度            len(X),真值测试
__cmp__        比较            X==Y,X
__lt__        特定的比较        X<Y(or else __cmp__)
__eq__        特定的比较        X==Y(or else __cmp__)
__radd__    左侧加法 +        Noninstance + X
__iadd__    实地（增强的）的加法    X+=Y（or else __add__)
__iter__    迭代环境        用于循环，测试，列表，映射及其他
所有重载方法的名称前后都有两个下划线字符，以便把同类中定义的变量名区别开来。特殊方法名称和表达式或运算的映射关系，是由Python语言预先定义好的。
所有运算符重载的方法都是选用的：如果没有写某个方法，那么定义的类就不支持该运算。多数重载方法只用在需要对象行为表现得就像内置函数一样的高级程序中。然而，__init__构造方法常出现在绝大多数类中。
__getitem__拦截索引运算
__getitem__方法拦截实例的索引运算。当实例X出现X[i]这样的索引运算中时，Python会调用这个实例继承的__getitem__方法。
（如果有），把X作为第一个参数传递，并且放括号内的索引值传递给第二个参数

 python实现 单例模式

[python] view plaincopy

    #!/usr/bin/env python    
      
    # -*- coding:utf-8 -*-    
      
        
      
    import os    
      
        
      
    class IOLoop(object):    
     
        @classmethod    
      
        def instance(self):    
      
            if not hasattr(self, "_instance"):    
      
                self._instance = self()    
      
            return self._instance    
     
      
     
        @classmethod    
      
        def initialized(self):    
      
            """Returns true if the singleton instance has been created."""    
      
            return hasattr(self, "_instance")    
      
        
      
        def service(self):    
      
          print 'Hello,World'    
      
        
      
    print IOLoop.initialized()    
      
    ioloop = IOLoop.instance()    
      
    ioloop.service()    
      
        
      
    #if os.fork() == 0:  
      
    print IOLoop.initialized()    
      
    ioloop = IOLoop.instance()    
      
    ioloop.service()   



练习题
写一个类继承一个基类


[python] view plaincopy

    # Creating a class hierarchy with an abstract base class.   
        
    class Employee:   
       """Abstract base class Employee"""  
        
       def __init__(self, first, last):   
          """Employee constructor, takes first name and last name.  
          NOTE: Cannot create object of class Employee."""  
        
          if self.__class__ == Employee:   
             raise NotImplementedError,"Cannot create object of class Employee"  
        
          self.firstName = first   
          self.lastName = last   
        
       def __str__(self):   
          """String representation of Employee"""  
        
          return "%s %s" % (self.firstName, self.lastName)   
        
       def _checkPositive(self, value):   
          """Utility method to ensure a value is positive"""  
        
          if value < 0:   
             raise ValueError,"Attribute value (%s) must be positive" % value   
          else:   
             return value   
        
       def earnings(self):   
          """Abstract method; derived classes must override"""  
        
          raise NotImplementedError, "Cannot call abstract method"  
        
    class Boss(Employee):   
       """Boss class, inherits from Employee"""  
        
       def __init__(self, first, last, salary):   
          """Boss constructor, takes first and last names and salary"""  
        
          Employee.__init__(self, first, last)   
          self.weeklySalary = self._checkPositive(float(salary))   
        
       def earnings(self):   
          """Compute the Boss's pay"""  
        
          return self.weeklySalary   
        
       def __str__(self):   
          """String representation of Boss"""  
        
          return "%17s: %s" % ("Boss", Employee.__str__(self))   
        
    class CommissionWorker(Employee):   
       """CommissionWorker class, inherits from Employee"""  
        
       def __init__(self, first, last, salary, commission, quantity):   
          """CommissionWorker constructor, takes first and last names,  
          salary, commission and quantity"""  
        
          Employee.__init__(self, first, last)   
          self.salary = self._checkPositive(float(salary))   
          self.commission = self._checkPositive(float(commission))   
          self.quantity = self._checkPositive(quantity)   
        
       def earnings(self):   
          """Compute the CommissionWorker's pay"""  
        
          return self.salary + self.commission * self.quantity   
        
       def __str__(self):   
          """String representation of CommissionWorker"""  
        
          return "%17s: %s" % ("Commission Worker",   
             Employee.__str__(self))   
        
    class PieceWorker(Employee):   
       """PieceWorker class, inherits from Employee"""  
        
       def __init__(self, first, last, wage, quantity):   
          """PieceWorker constructor, takes first and last names, wage  
          per piece and quantity"""  
        
          Employee.__init__(self, first, last)   
          self.wagePerPiece = self._checkPositive(float(wage))   
          self.quantity = self._checkPositive(quantity)   
        
       def earnings(self):   
          """Compute PieceWorker's pay"""  
        
          return self.quantity * self.wagePerPiece   
        
       def __str__(self):   
          """String representation of PieceWorker"""  
        
          return "%17s: %s" % ("Piece Worker",   
             Employee.__str__(self))   
        
    class HourlyWorker(Employee):   
       """HourlyWorker class, inherits from Employee"""  
        
       def __init__(self, first, last, wage, hours):   
          """HourlyWorker constructor, takes first and last names,  
          wage per hour and hours worked"""  
        
          Employee.__init__(self, first, last)   
          self.wage = self._checkPositive(float(wage))   
          self.hours = self._checkPositive(float(hours))   
        
       def earnings(self):   
          """Compute HourlyWorker's pay"""  
        
          if self.hours <= 40:   
             return self.wage * self.hours   
          else:   
             return 40 * self.wage + (self.hours - 40) * self.wage * 1.5  
        
       def __str__(self):   
          """String representation of HourlyWorker"""  
        
          return "%17s: %s" % ("Hourly Worker",   
             Employee.__str__(self))   
        
    # main program   
        
    # create list of Employees   
    employees = [ Boss("John", "Smith", 800.00),   
                  CommissionWorker("Sue", "Jones", 200.0, 3.0, 150),   
                  PieceWorker("Bob", "Lewis", 2.5, 200),   
                  HourlyWorker("Karen", "Price", 13.75, 40) ]   
        
    # print Employee and compute earnings   
    for employee in employees:   
       print "%s earned $%.2f" % (employee, employee.earnings())  




 python 系统学习笔记（十一）---sys
分类： python 2013-03-11 20:07 350人阅读 评论(0) 收藏 举报
sys.argv           命令行参数List，第一个元素是程序本身路径 
sys.modules.keys() 返回所有已经导入的模块列表 
sys.exc_info()     获取当前正在处理的异常类,exc_type、exc_value、exc_traceback当前处理的异常详细信息 
sys.exit(n)        退出程序，正常退出时exit(0) 
sys.hexversion     获取Python解释程序的版本值，16进制格式如：0x020403F0 
sys.version        获取Python解释程序的版本信息 
sys.maxint         最大的Int值 
sys.maxunicode     最大的Unicode值 
sys.modules        返回系统导入的模块字段，key是模块名，value是模块 
sys.path           返回模块的搜索路径，初始化时使用PYTHONPATH环境变量的值 
sys.platform       返回操作系统平台名称 
sys.stdout         标准输出
sys.stdin          标准输入
sys.stderr         错误输出
sys.exc_clear()    用来清除当前线程所出现的当前的或最近的错误信息
sys.exec_prefix    返回平台独立的python文件安装的位置
sys.byteorder      本地字节规则的指示器，big-endian平台的值是'big',little-endian平台的值是'little'
sys.copyright      记录python版权相关的东西
sys.api_version    解释器的C的API版本
sys.version_info  sys.version_info(2, 4, 3, 'final', 0) 'final'表示最终,也有'candidate'表示候选，表示版本级别，是否有后继的发行
sys.displayhook(value)      如果value非空，这个函数会把他输出到sys.stdout，并且将他保存进__builtin__._.指在python的交互式解释器里，'_'代表上次你输入得到的结果，hook是钩子的意思，将上次的结果钩过来
sys.getdefaultencoding()    返回当前你所用的默认的字符编码格式
sys.getfilesystemencoding() 返回将Unicode文件名转换成系统文件名的编码的名字
sys.setdefaultencoding(name)用来设置当前默认的字符编码，如果name和任何一个可用的编码都不匹配，抛出LookupError，这个函数只会被site模块的sitecustomize使用，一旦别site模块使用了，他会从sys模块移除
sys.builtin_module_names    Python解释器导入的模块列表 
sys.executable              Python解释程序路径 
sys.getwindowsversion()     获取Windows的版本 
sys.stdin.readline()        从标准输入读一行，sys.stdout.write("a") 屏幕输出a 

sys.startswith() 是用来判断一个对象是以什么开头的，比如在python命令行输入“'abc'.startswith('ab')”就会返回True

问题: 实现命令行读参数读文件
"python readfile.py  c:/test.txt d:/test.txt"“python  readfile.py --help”，那么sys.argv[0]就代表“test.py”

[python] view plaincopy

    import sys  
    def readfile(filename):  
        '''''Print a file to the standard output.'''  
        f = file(filename)  
        while True:  
              line = f.readline()  
              if len(line) == 0:  
                 break  
              print line,  
        f.close()  
      
      
      
      
    # Script starts from here  
    if len(sys.argv) < 2:  
        print 'No action specified.'  
        sys.exit()  
      
      
    if sys.argv[1].startswith('--'):  
       option = sys.argv[1][2:]  
       # fetch sys.argv[1] but without the first two characters  
       if option == 'version':  
          print 'Version 1.0'  
       elif option == 'help':  
          print ''''' 
               This program prints files to the standard output. 
               Any number of files can be specified. 
               Options include: 
               --version : Prints the version number 
               --help    : Display this help 
               --file    : file'''  
       else:  
           print 'Unknown option.'  
           sys.exit()  
    else:  
        for filename in sys.argv[1:]:  
            readfile(filename)  





参照此例子修改sendemail.py  
--to xx  --content xx  --topic xx  --attach xx






python 系统学习笔记（十二）---os os.path os.walk
分类： python 2013-03-12 09:20 464人阅读 评论(0) 收藏 举报

得到当前工作目录，即当前Python脚本工作的目录路径: os.getcwd()

返回指定目录下的所有文件和目录名:os.listdir()

函数用来删除一个文件:os.remove()

删除多个目录：os.removedirs（r“c：\python”）

检验给出的路径是否是一个文件：os.path.isfile()

检验给出的路径是否是一个目录：os.path.isdir()

判断是否是绝对路径：os.path.isabs()

检验给出的路径是否真地存:os.path.exists()

返回一个路径的目录名和文件名:os.path.split()     eg os.path.split('/home/swaroop/byte/code/poem.txt') 结果：('/home/swaroop/byte/code', 'poem.txt') 

os.path.join(path,name):连接目录与文件名或目录

分离扩展名：os.path.splitext()

获取路径名：os.path.dirname()

获取文件名：os.path.basename()

运行shell命令: os.system()

读取和设置环境变量:os.getenv() 与os.putenv()

给出当前平台使用的行终止符:os.linesep    Windows使用'\r\n'，Linux使用'\n'而Mac使用'\r'

指示你正在使用的平台：os.name       对于Windows，它是'nt'，而对于Linux/Unix用户，它是'posix'

重命名：os.rename（old， new）

创建多级目录：os.makedirs（r“c：\python\test”）

创建单个目录：os.mkdir（“test”）

获取文件属性：os.stat（file）

修改文件权限与时间戳：os.chmod（file）

终止当前进程：os.exit（）

获取文件大小：os.path.getsize（filename）


os 模块的文件/目录访问函数

文件处理
mkfifo()/mknod() 创建命名管道/创建文件系统节点
remove()/unlink() 删除文件   os.remove()函数用来删除一个文件。
rename()/renames() 重命名文件   
*stat() 返回文件信息(包含stat(), lstat(), xstat())
symlink() 创建符号链接
utime() 更新时间戳
tmpfile() 创建并打开('w+b')一个新的临时文件
walk() 生成一个目录树下的所有文件名

目录/文件夹
chdir()/fchdir() 改变当前工作目录/通过一个文件描述符改变当前工作目录
chroot() 改变当前进程的根目录
listdir() 列出指定目录的文件   返回指定目录下的所有文件和目录名。 os.listdir(dirname)：列出dirname下的目录和文件
getcwd()/getcwdu() 返回当前工作目录/功能相同, 但返回一个 Unicode 对象  os.getcwd()函数得到当前工作目录，即当前Python脚本工作的目录路径。
mkdir()/makedirs() 创建目录/创建多层目录
rmdir()/removedirs() 删除目录/删除多层目录

访问/权限
access() 检验权限模式
chmod() 改变权限模式
chown()/lchown() 改变 owner 和 group ID/功能相同, 但不会跟踪链接
umask() 设置默认权限模式

文件描述符操作
open() 底层的操作系统 open (对于文件, 使用标准的内建 open() 函数)
read()/write() 根据文件描述符读取/写入数据
dup()/dup2() 复制文件描述符号/功能相同, 但是是复制到另一个文件描述符

设备号
makedev() 从 major 和 minor 设备号创建一个原始设备号
major()/minor() 从原始设备号获得 major/minor 设备号



os.path 模块中的路径名访问函数

分隔
basename() 去掉目录路径, 返回文件名
dirname() 去掉文件名, 返回目录路径
join() 将分离的各部分组合成一个路径名
split() 返回 (dirname(), basename()) 元组
splitdrive() 返回 (drivename, pathname) 元组
splitext() 返回 (filename, extension) 元组

信息
getatime() 返回最近访问时间
getctime() 返回文件创建时间
getmtime() 返回最近文件修改时间
getsize() 返回文件大小(以字节为单位)

查询
exists() 指定路径(文件或目录)是否存在
isabs() 指定路径是否为绝对路径
isdir() 指定路径是否存在且为一个目录
isfile() 指定路径是否存在且为一个文件
islink() 指定路径是否存在且为一个符号链接
ismount() 指定路径是否存在且为一个挂载点
samefile() 两个路径名是否指向同个文件

walk 使用遍历

os模块提供的walk方法很强大，能够把给定的目录下的所有目录和文件遍历出来。

方法：os.walk(path),遍历path，返回一个对象，他的每个部分都是一个三元组,('目录x'，[目录x下的目录list]，目录x下面的文件)
[python] view plaincopy

    import os  
    def walk_dir(dir,topdown=True):  
        for root, dirs, files in os.walk(dir, topdown):  
            for name in files:  
                if(name.find('graph')!=-1):  
                    print name  
               # print(os.path.join(name))  
            for name in dirs:  
                #pass  
                print(os.path.join(name))             
    #dir = raw_input('please input the path:')  
    dir=r"C:\Users\Administrator\Desktop\python_test\AMD"  
    walk_dir(dir)  
    
    
    





python 系统学习笔记（十三）---lambda
分类： python 2013-03-12 11:00 290人阅读 评论(0) 收藏 举报
1 python lambda会创建一个函数对象，但不会把这个函数对象赋给一个标识符，而def则会把函数对象赋值给一个变量。
2 python lambda它只是一个表达式，而def则是一个语句。

lambda 语句被用来创建新的函数对象，并且在运行时返回它们。

#!/usr/bin/python
# Filename: lambda.py
def make_repeater (n):
    return lambda  s: s*n
twice = make_repeater(2 )
print  twice( 'word' )
print  twice( 5 )

这里，我们使用了make_repeater 函数在运行时创建新的函数对象，并且返回它。lambda 语句用来创
建函数对象。本质上，lambda 需要一个参数，后面仅跟单个表达式作为函数体，而表达式的值被这个新
建的函数返回。注意，即便是print语句也不能用在lambda形式中，只能使用表达式。

2多个参数的：
m = lambda x,y,z: (x-y)*z
print m(3,1,2)
结果是4
 
作业会和下一章 list 的排血一起 这里仅简单介绍一下
主要应用的好处
不重复利用的函数可以用这个代替
当然也有神奇的简洁！！！！
print   reduce(lambda   x,y:x*y,   range(1,   1001))

reduce与range都是Python的内置函数。

range（1，1001）表示生成1到1000的连续整数列表（List）。

reduce（functionA，iterableB），functionA为需要两个变量的函数，并返回一个值。iterableB为可迭代变量，如List等。reduce函数将B中的元素从左到右依次传入函数A中，再用函数A返回的结果替代传入的参数，反复执行，则可将B reduce成一个单值。在此，是将1到1000的连续整数列表传入lambda函数并用两个数的积替换列表中的数，实际的计算过程为：(...((1×2)×3)×4)×...×1000)，最后的结果即1000的阶乘。







python 系统学习笔记（十四）---排序
分类： python 2013-03-12 12:00 344人阅读 评论(0) 收藏 举报

 在 Python 中, 当需要对一个 list 排序时, 一般可以用 list.sort() 或者 sorted(iterable[, cmp[, key[, reverse]]]).
其中:
cmp(e1, e2) 是带两个参数的比较函数, 返回值: 负数: e1 < e2, 0: e1 == e2, 正数: e1 > e2. 默认为 None, 即用内建的比较函数.
key 是带一个参数的函数, 用来为每个元素提取比较值. 默认为 None, 即直接比较每个元素.
reverse 是一个布尔值, 表示是否反转比较结果.

1.cmp

我们希望按照自己定义的排序规则来排序（例如，按关键词的权重排序，按人的年龄排序，等等）。
若List中每个元素都是2-tuple，tuple中第一个元素为String类型的keyword，第二个元素为该字符串对应的权重（int类型），希望按照权重排序（从高到低），则可以这样：

def my_cmp(E1, E2):
    return -cmp(E1[1], E2[1])    #compare weight of each 2-tuple
                    #return the negative result of built-in cmp function
                    #thus we get the descend order
L = [('a', 11), ('b', 10), ('c', 12)]
L.sort(my_cmp)
print L

2.按照key 排序 这个会比cmp 执行次数少

def my_key(E1):

    return E1[1]  #return key 11,10,12

L = [('a', 11), ('b', 10), ('c', 12)]

L.sort(key=my_key,reverse=1) #key 大到小 reverse=0 时候从小到大

print L

3. itemgetter 返回 多排序

简单介绍itemgetter 
[python] view plaincopy

    from operator import itemgetter, attrgetter   
      
    a = [1,2,3]      
      
    b=itemgetter(1)      
      
    print  b(a)      
      
    b=itemgetter(1,0)      
      
    print b(a)      
      
    b=itemgetter(1)      
      
    print  b(a)    
      
    b=itemgetter(1,0)      
      
    print b(a)   


  

按照key排序

[python] view plaincopy

    from operator import itemgetter, attrgetter  
      
    L = [('a', 11), ('b', 10), ('c', 12)]  
      
    L.sort(key=itemgetter(1))  
      
    print L  



按照key （1,2）排序 11 11 12为主 B A C 次要排序
[python] view plaincopy

    from operator import itemgetter, attrgetter  
      
    L = [('a', 11,'B'), ('b', 11,'A'), ('c', 12,'C')]  
      
    L.sort(key=itemgetter(1,2))  
      
    print L  



4 对由字典排序
[python] view plaincopy

    d = {'data1':3, 'data2':1, 'data3':2, 'data4':4}     
      
    sorted(d.iteritems(), key=itemgetter(1), reverse=True)     
      
    print d  


 

返回pair
[python] view plaincopy

    d = {'data1':3, 'data2':1, 'data3':2, 'data4':4}  
    a=sorted(d.iteritems(), key=itemgetter(1), reverse=True)  
    print a  



习题 

把1 和2 的内容用 lambda 实现
[python] view plaincopy

    #1.  
    L = [('a', 11), ('b', 10), ('c', 12)]  
    L.sort(lambda elem1,elem2: cmp(elem1[1],elem2[1]))  
    print L  
    #2.  
    L = [('a', 11), ('b', 10), ('c', 12)]  
    L.sort(key=lambda elem: elem[1])  
    print L  
    
    










python 系统学习笔记（十五）---正则表达式
分类： python 2013-03-12 15:36 378人阅读 评论(0) 收藏 举报

目录(?)[+]
剽窃自http://www.cnblogs.com/huxi/archive/2010/07/04/1771073.html  请勿见怪
1. 正则表达式基础
1.1. 简单介绍

正则表达式并不是Python的一部分。正则表达式是用于处理字符串的强大工具，拥有自己独特的语法以及一个独立的处理引擎，效率上可能不如str自带的方法，但功能十分强大。得益于这一点，在提供了正则表达式的语言里，正则表达式的语法都是一样的，区别只在于不同的编程语言实现支持的语法数量不同；但不用担心，不被支持的语法通常是不常用的部分。如果已经在其他语言里使用过正则表达式，只需要简单看一看就可以上手了。

下图展示了使用正则表达式进行匹配的流程：
re_simple

正则表达式的大致匹配过程是：依次拿出表达式和文本中的字符比较，如果每一个字符都能匹配，则匹配成功；一旦有匹配不成功的字符则匹配失败。如果表达式中有量词或边界，这个过程会稍微有一些不同，但也是很好理解的，看下图中的示例以及自己多使用几次就能明白。

下图列出了Python支持的正则表达式元字符和语法：  
pyre
1.2. 数量词的贪婪模式与非贪婪模式

正则表达式通常用于在文本中查找匹配的字符串。Python里数量词默认是贪婪的（在少数语言里也可能是默认非贪婪），总是尝试匹配尽可能多的字符；非贪婪的则相反，总是尝试匹配尽可能少的字符。例如：正则表达式"ab*"如果用于查找"abbbc"，将找到"abbb"。而如果使用非贪婪的数量词"ab*?"，将找到"a"。
1.3. 反斜杠的困扰

与大多数编程语言相同，正则表达式里使用"\"作为转义字符，这就可能造成反斜杠困扰。假如你需要匹配文本中的字符"\"，那么使用编程语言表示的正则表达式里将需要4个反斜杠"\\\\"：前两个和后两个分别用于在编程语言里转义成反斜杠，转换成两个反斜杠后再在正则表达式里转义成一个反斜杠。Python里的原生字符串很好地解决了这个问题，这个例子中的正则表达式可以使用r"\\"表示。同样，匹配一个数字的"\\d"可以写成r"\d"。有了原生字符串，你再也不用担心是不是漏写了反斜杠，写出来的表达式也更直观。
1.4. 匹配模式

正则表达式提供了一些可用的匹配模式，比如忽略大小写、多行匹配等，这部分内容将在Pattern类的工厂方法re.compile(pattern[, flags])中一起介绍。
2. re模块
2.1. 开始使用re

Python通过re模块提供对正则表达式的支持。使用re的一般步骤是先将正则表达式的字符串形式编译为Pattern实例，然后使用Pattern实例处理文本并获得匹配结果（一个Match实例），最后使用Match实例获得信息，进行其他的操作。
[python] view plaincopy

    # encoding: UTF-8   
    import re   
        
    # 将正则表达式编译成Pattern对象   
    pattern = re.compile(r'hello')   
        
    # 使用Pattern匹配文本，获得匹配结果，无法匹配时将返回None   
    match = pattern.match('hello world!')   
        
    if match:   
        # 使用Match获得分组信息   
        print match.group()   
        
    ### 输出 ###   
    # hello  


 
re.compile(strPattern[, flag]):

这个方法是Pattern类的工厂方法，用于将字符串形式的正则表达式编译为Pattern对象。 第二个参数flag是匹配模式，取值可以使用按位或运算符'|'表示同时生效，比如re.I | re.M。另外，你也可以在regex字符串中指定模式，比如re.compile('pattern', re.I | re.M)与re.compile('(?im)pattern')是等价的。
可选值有：

    re.I(re.IGNORECASE): 忽略大小写（括号内是完整写法，下同）
    M(MULTILINE): 多行模式，改变'^'和'$'的行为（参见上图）
    S(DOTALL): 点任意匹配模式，改变'.'的行为
    L(LOCALE): 使预定字符类 \w \W \b \B \s \S 取决于当前区域设定
    U(UNICODE): 使预定字符类 \w \W \b \B \s \S \d \D 取决于unicode定义的字符属性
    X(VERBOSE): 详细模式。这个模式下正则表达式可以是多行，忽略空白字符，并可以加入注释。以下两个正则表达式是等价的： 

 
[python] view plaincopy

    a = re.compile(r"""\d +  # the integral part  
                       \.    # the decimal point  
                       \d *  # some fractional digits""", re.X)   
    b = re.compile(r"\d+\.\d*")   


re提供了众多模块方法用于完成正则表达式的功能。这些方法可以使用Pattern实例的相应方法替代，唯一的好处是少写一行re.compile()代码，但同时也无法复用编译后的Pattern对象。这些方法将在Pattern类的实例方法部分一起介绍。如上面这个例子可以简写为：
[python] view plaincopy

    m = re.match(r'hello', 'hello world!')   
    print m.group()   


 

re模块还提供了一个方法escape(string)，用于将string中的正则表达式元字符如*/+/?等之前加上转义符再返回，在需要大量匹配元字符时有那么一点用。
2.2. Match

Match对象是一次匹配的结果，包含了很多关于此次匹配的信息，可以使用Match提供的可读属性或方法来获取这些信息。

属性：

    string: 匹配时使用的文本。
    re: 匹配时使用的Pattern对象。
    pos: 文本中正则表达式开始搜索的索引。值与Pattern.match()和Pattern.seach()方法的同名参数相同。
    endpos: 文本中正则表达式结束搜索的索引。值与Pattern.match()和Pattern.seach()方法的同名参数相同。
    lastindex: 最后一个被捕获的分组在文本中的索引。如果没有被捕获的分组，将为None。
    lastgroup: 最后一个被捕获的分组的别名。如果这个分组没有别名或者没有被捕获的分组，将为None。 

方法：

    group([group1, …]):
    获得一个或多个分组截获的字符串；指定多个参数时将以元组形式返回。group1可以使用编号也可以使用别名；编号0代表整个匹配的子串；不填写参数时，返回group(0)；没有截获字符串的组返回None；截获了多次的组返回最后一次截获的子串。
    groups([default]):
    以元组形式返回全部分组截获的字符串。相当于调用group(1,2,…last)。default表示没有截获字符串的组以这个值替代，默认为None。
    groupdict([default]):
    返回以有别名的组的别名为键、以该组截获的子串为值的字典，没有别名的组不包含在内。default含义同上。
    start([group]):
    返回指定的组截获的子串在string中的起始索引（子串第一个字符的索引）。group默认值为0。
    end([group]):
    返回指定的组截获的子串在string中的结束索引（子串最后一个字符的索引+1）。group默认值为0。
    span([group]):
    返回(start(group), end(group))。
    expand(template):
    将匹配到的分组代入template中然后返回。template中可以使用\id或\g<id>、\g<name>引用分组，但不能使用编号0。\id与\g<id>是等价的；但\10将被认为是第10个分组，如果你想表达\1之后是字符'0'，只能使用\g<1>0。 

[python] view plaincopy

    import re   
    m = re.match(r'(\w+) (\w+)(?P<sign>.*)', 'hello world!')   
        
    print "m.string:", m.string   
    print "m.re:", m.re   
    print "m.pos:", m.pos   
    print "m.endpos:", m.endpos   
    print "m.lastindex:", m.lastindex   
    print "m.lastgroup:", m.lastgroup   
        
    print "m.group(1,2):", m.group(1, 2)   
    print "m.groups():", m.groups()   
    print "m.groupdict():", m.groupdict()   
    print "m.start(2):", m.start(2)   
    print "m.end(2):", m.end(2)   
    print "m.span(2):", m.span(2)   
    print r"m.expand(r'\2 \1\3'):", m.expand(r'\2 \1\3')   
        
    ### output ###   
    # m.string: hello world!   
    # m.re: <_sre.SRE_Pattern object at 0x016E1A38>   
    # m.pos: 0   
    # m.endpos: 12   
    # m.lastindex: 3   
    # m.lastgroup: sign   
    # m.group(1,2): ('hello', 'world')   
    # m.groups(): ('hello', 'world', '!')   
    # m.groupdict(): {'sign': '!'}   
    # m.start(2): 6   
    # m.end(2): 11   
    # m.span(2): (6, 11)   
    # m.expand(r'\2 \1\3'): world hello!  


2.3. Pattern

Pattern对象是一个编译好的正则表达式，通过Pattern提供的一系列方法可以对文本进行匹配查找。

Pattern不能直接实例化，必须使用re.compile()进行构造。

Pattern提供了几个可读属性用于获取表达式的相关信息：

    pattern: 编译时用的表达式字符串。
    flags: 编译时用的匹配模式。数字形式。
    groups: 表达式中分组的数量。
    groupindex: 以表达式中有别名的组的别名为键、以该组对应的编号为值的字典，没有别名的组不包含在内。 

[python] view plaincopy

    import re   
    m = re.match(r'(\w+) (\w+)(?P<sign>.*)', 'hello world!')   
        
    print "m.string:", m.string   
    print "m.re:", m.re   
    print "m.pos:", m.pos   
    print "m.endpos:", m.endpos   
    print "m.lastindex:", m.lastindex   
    print "m.lastgroup:", m.lastgroup   
        
    print "m.group(1,2):", m.group(1, 2)   
    print "m.groups():", m.groups()   
    print "m.groupdict():", m.groupdict()   
    print "m.start(2):", m.start(2)   
    print "m.end(2):", m.end(2)   
    print "m.span(2):", m.span(2)   
    print r"m.expand(r'\2 \1\3'):", m.expand(r'\2 \1\3')   
        
    ### output ###   
    # m.string: hello world!   
    # m.re: <_sre.SRE_Pattern object at 0x016E1A38>   
    # m.pos: 0   
    # m.endpos: 12   
    # m.lastindex: 3   
    # m.lastgroup: sign   
    # m.group(1,2): ('hello', 'world')   
    # m.groups(): ('hello', 'world', '!')   
    # m.groupdict(): {'sign': '!'}   
    # m.start(2): 6   
    # m.end(2): 11   
    # m.span(2): (6, 11)   
    # m.expand(r'\2 \1\3'): world hello!  


实例方法[ | re模块方法]：

    match(string[, pos[, endpos]]) | re.match(pattern, string[, flags]):
    这个方法将从string的pos下标处起尝试匹配pattern；如果pattern结束时仍可匹配，则返回一个Match对象；如果匹配过程中pattern无法匹配，或者匹配未结束就已到达endpos，则返回None。
    pos和endpos的默认值分别为0和len(string)；re.match()无法指定这两个参数，参数flags用于编译pattern时指定匹配模式。
    注意：这个方法并不是完全匹配。当pattern结束时若string还有剩余字符，仍然视为成功。想要完全匹配，可以在表达式末尾加上边界匹配符'$'。
    示例参见2.1小节。
    search(string[, pos[, endpos]]) | re.search(pattern, string[, flags]):
    这个方法用于查找字符串中可以匹配成功的子串。从string的pos下标处起尝试匹配pattern，如果pattern结束时仍可匹配，则返回一个Match对象；若无法匹配，则将pos加1后重新尝试匹配；直到pos=endpos时仍无法匹配则返回None。
    pos和endpos的默认值分别为0和len(string))；re.search()无法指定这两个参数，参数flags用于编译pattern时指定匹配模式。
    [python] view plaincopy
        import re   
        m = re.match(r'(\w+) (\w+)(?P<sign>.*)', 'hello world!')   
            
        print "m.string:", m.string   
        print "m.re:", m.re   
        print "m.pos:", m.pos   
        print "m.endpos:", m.endpos   
        print "m.lastindex:", m.lastindex   
        print "m.lastgroup:", m.lastgroup   
            
        print "m.group(1,2):", m.group(1, 2)   
        print "m.groups():", m.groups()   
        print "m.groupdict():", m.groupdict()   
        print "m.start(2):", m.start(2)   
        print "m.end(2):", m.end(2)   
        print "m.span(2):", m.span(2)   
        print r"m.expand(r'\2 \1\3'):", m.expand(r'\2 \1\3')   
            
        ### output ###   
        # m.string: hello world!   
        # m.re: <_sre.SRE_Pattern object at 0x016E1A38>   
        # m.pos: 0   
        # m.endpos: 12   
        # m.lastindex: 3   
        # m.lastgroup: sign   
        # m.group(1,2): ('hello', 'world')   
        # m.groups(): ('hello', 'world', '!')   
        # m.groupdict(): {'sign': '!'}   
        # m.start(2): 6   
        # m.end(2): 11   
        # m.span(2): (6, 11)   
        # m.expand(r'\2 \1\3'): world hello!  

    split(string[, maxsplit]) | re.split(pattern, string[, maxsplit]):
    按照能够匹配的子串将string分割后返回列表。maxsplit用于指定最大分割次数，不指定将全部分割。
    [python] view plaincopy
        import re   
            
        p = re.compile(r'\d+')   
        print p.split('one1two2three3four4')   
            
        ### output ###   
        # ['one', 'two', 'three', 'four', '']   

    findall(string[, pos[, endpos]]) | re.findall(pattern, string[, flags]):
    搜索string，以列表形式返回全部能匹配的子串。
    [python] view plaincopy
        import re   
            
        p = re.compile(r'\d+')   
        print p.findall('one1two2three3four4')   
            
        ### output ###   
        # ['1', '2', '3', '4']   

    finditer(string[, pos[, endpos]]) | re.finditer(pattern, string[, flags]):
    搜索string，返回一个顺序访问每一个匹配结果（Match对象）的迭代器。
    [python] view plaincopy
        import re   
            
        p = re.compile(r'\d+')   
        for m in p.finditer('one1two2three3four4'):   
            print m.group(),   
            
        ### output ###   
        # 1 2 3 4   

    sub(repl, string[, count]) | re.sub(pattern, repl, string[, count]):
    使用repl替换string中每一个匹配的子串后返回替换后的字符串。
    当repl是一个字符串时，可以使用\id或\g<id>、\g<name>引用分组，但不能使用编号0。
    当repl是一个方法时，这个方法应当只接受一个参数（Match对象），并返回一个字符串用于替换（返回的字符串中不能再引用分组）。
    count用于指定最多替换次数，不指定时全部替换。
    [python] view plaincopy
        import re   
            
        p = re.compile(r'(\w+) (\w+)')   
        s = 'i say, hello world!'  
            
        print p.sub(r'\2 \1', s)   
            
        def func(m):   
            return m.group(1).title() + ' ' + m.group(2).title()   
            
        print p.sub(func, s)   
            
        ### output ###   
        # say i, world hello!   
        # I Say, Hello World!   

    subn(repl, string[, count]) |re.sub(pattern, repl, string[, count]):
    返回 (sub(repl, string[, count]), 替换次数)。
    [python] view plaincopy
        import re   
            
        p = re.compile(r'(\w+) (\w+)')   
        s = 'i say, hello world!'  
            
        print p.subn(r'\2 \1', s)   
            
        def func(m):   
            return m.group(1).title() + ' ' + m.group(2).title()   
            
        print p.subn(func, s)   
            
        ### output ###   
        # ('say i, world hello!', 2)   
        # ('I Say, Hello World!', 2)  















 python 系统学习笔记（十五）---正则表达式
        </a></span>
    </h1>
</div>

    <div class="article_manage">
        <span class="link_categories">
        分类：
            <a href="/ychw365/article/category/1356427" onclick="_gaq.push(['_trackEvent','function', 'onclick', 'blog_articles_fenlei']);">python</a> 
        </span>
    <span class="link_postdate">2013-03-12 15:36</span>
    <span class="link_view" title="阅读次数">378人阅读</span>
    <span class="link_comments" title="评论次数"><a href="#comments" onclick="_gaq.push(['_trackEvent','function', 'onclick', 'blog_articles_pinglun'])">评论</a>(0)</span>
    <span class="link_collect"><a href="javascript:void(0);" onclick="javascript:_gaq.push(['_trackEvent','function', 'onclick', 'blog_articles_shoucang']);collectArticle('python 系统学习笔记（十五）---正则表达式','8663982');return false;" title="收藏">收藏</a></span>
    <span class="link_report"><a href="#report"  onclick="javascript:_gaq.push(['_trackEvent','function', 'onclick', 'blog_articles_jubao']);report(8663982,2);return false;" title="举报">举报</a></span>
    
</div>

  
    
<div id="article_content" class="article_content">

<h2>剽窃自<a href="http://www.cnblogs.com/huxi/archive/2010/07/04/1771073.html">http://www.cnblogs.com/huxi/archive/2010/07/04/1771073.html</a>&nbsp; 请勿见怪</h2>
<h2>1. 正则表达式基础</h2>
<h3>1.1. 简单介绍</h3>
<p>正则表达式并不是Python的一部分。正则表达式是用于处理字符串的强大工具，拥有自己独特的语法以及一个独立的处理引擎，效率上可能不如str自带的方法，但功能十分强大。得益于这一点，在提供了正则表达式的语言里，正则表达式的语法都是一样的，区别只在于不同的编程语言实现支持的语法数量不同；但不用担心，不被支持的语法通常是不常用的部分。如果已经在其他语言里使用过正则表达式，只需要简单看一看就可以上手了。</p>
<p>下图展示了使用正则表达式进行匹配的流程： <br>
<img title="re_simple" border="0" alt="re_simple" src="http://images.cnblogs.com/cnblogs_com/huxi/WindowsLiveWriter/Python_10A67/re_simple_38246a58-83be-4adf-9f30-6d735e9b9b47.png" width="474" height="212" style="border-right-width:0px; display:inline; border-top-width:0px; border-bottom-width:0px; border-left-width:0px">
</p>
<p>正则表达式的大致匹配过程是：依次拿出表达式和文本中的字符比较，如果每一个字符都能匹配，则匹配成功；一旦有匹配不成功的字符则匹配失败。如果表达式中有量词或边界，这个过程会稍微有一些不同，但也是很好理解的，看下图中的示例以及自己多使用几次就能明白。</p>
<p>下图列出了Python支持的正则表达式元字符和语法：&nbsp;&nbsp; <br>
<img title="pyre" border="0" alt="pyre" src="http://images.cnblogs.com/cnblogs_com/huxi/Windows-Live-Writer/Python_10A67/pyre_ebb9ce1c-e5e8-4219-a8ae-7ee620d5f9f1.png" width="799" height="1719" style="border-bottom:0px; border-left:0px; padding-left:0px; padding-right:0px; display:inline; border-top:0px; border-right:0px; padding-top:0px"></p>
<h3>1.2. 数量词的贪婪模式与非贪婪模式</h3>
<p>正则表达式通常用于在文本中查找匹配的字符串。Python里数量词默认是贪婪的（在少数语言里也可能是默认非贪婪），总是尝试匹配尽可能多的字符；非贪婪的则相反，总是尝试匹配尽可能少的字符。例如：正则表达式&quot;ab*&quot;如果用于查找&quot;abbbc&quot;，将找到&quot;abbb&quot;。而如果使用非贪婪的数量词&quot;ab*?&quot;，将找到&quot;a&quot;。</p>
<h3>1.3. 反斜杠的困扰</h3>
<p>与大多数编程语言相同，正则表达式里使用&quot;\&quot;作为转义字符，这就可能造成反斜杠困扰。假如你需要匹配文本中的字符&quot;\&quot;，那么使用编程语言表示的正则表达式里将需要4个反斜杠&quot;\\\\&quot;：前两个和后两个分别用于在编程语言里转义成反斜杠，转换成两个反斜杠后再在正则表达式里转义成一个反斜杠。Python里的原生字符串很好地解决了这个问题，这个例子中的正则表达式可以使用r&quot;\\&quot;表示。同样，匹配一个数字的&quot;\\d&quot;可以写成r&quot;\d&quot;。有了原生字符串，你再也不用担心是不是漏写了反斜杠，写出来的表达式也更直观。</p>
<h3>1.4. 匹配模式</h3>
<p>正则表达式提供了一些可用的匹配模式，比如忽略大小写、多行匹配等，这部分内容将在Pattern类的工厂方法re.compile(pattern[, flags])中一起介绍。</p>
<h2>2. re模块</h2>
<h3>2.1. 开始使用re</h3>
<p>Python通过re模块提供对正则表达式的支持。使用re的一般步骤是先将正则表达式的字符串形式编译为Pattern实例，然后使用Pattern实例处理文本并获得匹配结果（一个Match实例），最后使用Match实例获得信息，进行其他的操作。</p>
<pre class="python" name="code"># encoding: UTF-8 
import re 
  
# 将正则表达式编译成Pattern对象 
pattern = re.compile(r'hello') 
  
# 使用Pattern匹配文本，获得匹配结果，无法匹配时将返回None 
match = pattern.match('hello world!') 
  
if match: 
    # 使用Match获得分组信息 
    print match.group() 
  
### 输出 ### 
# hello</pre>
<p><br>
&nbsp;</p>
<div>
<div id="highlighter_592125" class="syntaxhighlighter  py ie"><strong>re.compile(strPattern[, flag]):
</strong></div>
</div>
<p>这个方法是Pattern类的工厂方法，用于将字符串形式的正则表达式编译为Pattern对象。 第二个参数flag是匹配模式，取&#20540;可以使用按位或运算符'|'表示同时生效，比如re.I | re.M。另外，你也可以在regex字符串中指定模式，比如re.compile('pattern', re.I | re.M)与re.compile('(?im)pattern')是等价的。
<br>
可选&#20540;有： </p>
<ul>
<li>re.<strong>I</strong>(re.IGNORECASE): 忽略大小写（括号内是完整写法，下同） </li><li><strong>M</strong>(MULTILINE): 多行模式，改变'^'和'$'的行为（参见上图） </li><li><strong>S</strong>(DOTALL): 点任意匹配模式，改变'.'的行为 </li><li><strong>L</strong>(LOCALE): 使预定字符类 \w \W \b \B \s \S 取决于当前区域设定 </li><li><strong>U</strong>(UNICODE): 使预定字符类 \w \W \b \B \s \S \d \D 取决于unicode定义的字符属性
</li><li><strong>X</strong>(VERBOSE): 详细模式。这个模式下正则表达式可以是多行，忽略空白字符，并可以加入注释。以下两个正则表达式是等价的：
</li></ul>
<div>
<div id="highlighter_559145" class="syntaxhighlighter  py ie">
<div class="toolbar"><span></span>&nbsp;</div>
<div class="toolbar"><span></span><pre class="python" name="code">a = re.compile(r&quot;&quot;&quot;\d +  # the integral part 
                   \.    # the decimal point 
                   \d *  # some fractional digits&quot;&quot;&quot;, re.X) 
b = re.compile(r&quot;\d+\.\d*&quot;) 

</pre><br>
</div>
</div>
</div>
<p>re提供了众多模块方法用于完成正则表达式的功能。这些方法可以使用Pattern实例的相应方法替代，唯一的好处是少写一行re.compile()代码，但同时也无法复用编译后的Pattern对象。这些方法将在Pattern类的实例方法部分一起介绍。如上面这个例子可以简写为：</p>
<pre class="python" name="code">m = re.match(r'hello', 'hello world!') 
print m.group() 

</pre>
<p><br>
&nbsp;</p>
<p>re模块还提供了一个方法<span style="font-family:courier new">escape(string)，用于将string中的正则表达式元字符如*/&#43;/?等之前加上转义符再返回，在需要大量匹配元字符时有那么一点用。</span></p>
<h3>2.2. Match</h3>
<p>Match对象是一次匹配的结果，包含了很多关于此次匹配的信息，可以使用Match提供的可读属性或方法来获取这些信息。</p>
<p>属性：</p>
<ol>
<li><strong>string</strong>: 匹配时使用的文本。 </li><li><strong>re</strong>: 匹配时使用的Pattern对象。 </li><li><strong>pos</strong>: 文本中正则表达式开始搜索的索引。&#20540;与Pattern.match()和Pattern.seach()方法的同名参数相同。
</li><li><strong>endpos</strong>: 文本中正则表达式结束搜索的索引。&#20540;与Pattern.match()和Pattern.seach()方法的同名参数相同。
</li><li><strong>lastindex</strong>: 最后一个被捕获的分组在文本中的索引。如果没有被捕获的分组，将为None。 </li><li><strong>lastgroup</strong>: 最后一个被捕获的分组的别名。如果这个分组没有别名或者没有被捕获的分组，将为None。 </li></ol>
<p>方法：</p>
<ol>
<li><strong>group([group1, …]): </strong><br>
获得一个或多个分组截获的字符串；指定多个参数时将以元组形式返回。group1可以使用编号也可以使用别名；编号0代表整个匹配的子串；不填写参数时，返回group(0)；没有截获字符串的组返回None；截获了多次的组返回最后一次截获的子串。
</li><li><strong>groups([default]):</strong> <br>
以元组形式返回全部分组截获的字符串。相当于调用group(1,2,…last)。default表示没有截获字符串的组以这个&#20540;替代，默认为None。 </li><li><strong>groupdict([default]): <br>
</strong>返回以有别名的组的别名为键、以该组截获的子串为&#20540;的字典，没有别名的组不包含在内。default含义同上。 </li><li><strong>start([group]):</strong> <br>
返回指定的组截获的子串在string中的起始索引（子串第一个字符的索引）。group默认&#20540;为0。 </li><li><strong>end([group]): <br>
</strong>返回指定的组截获的子串在string中的结束索引（子串最后一个字符的索引&#43;1）。group默认&#20540;为0。 </li><li><strong>span([group]): <br>
</strong>返回(start(group), end(group))。 </li><li><strong>expand(template):</strong> <br>
将匹配到的分组代入template中然后返回。template中可以使用\id或\g&lt;id&gt;、\g&lt;name&gt;引用分组，但不能使用编号0。\id与\g&lt;id&gt;是等价的；但\10将被认为是第10个分组，如果你想表达\1之后是字符'0'，只能使用\g&lt;1&gt;0。
</li></ol>
<div>
<div id="highlighter_465853" class="syntaxhighlighter  py ie">
<div class="toolbar"><span></span><pre class="python" name="code">import re 
m = re.match(r'(\w+) (\w+)(?P&lt;sign&gt;.*)', 'hello world!') 
  
print &quot;m.string:&quot;, m.string 
print &quot;m.re:&quot;, m.re 
print &quot;m.pos:&quot;, m.pos 
print &quot;m.endpos:&quot;, m.endpos 
print &quot;m.lastindex:&quot;, m.lastindex 
print &quot;m.lastgroup:&quot;, m.lastgroup 
  
print &quot;m.group(1,2):&quot;, m.group(1, 2) 
print &quot;m.groups():&quot;, m.groups() 
print &quot;m.groupdict():&quot;, m.groupdict() 
print &quot;m.start(2):&quot;, m.start(2) 
print &quot;m.end(2):&quot;, m.end(2) 
print &quot;m.span(2):&quot;, m.span(2) 
print r&quot;m.expand(r'\2 \1\3'):&quot;, m.expand(r'\2 \1\3') 
  
### output ### 
# m.string: hello world! 
# m.re: &lt;_sre.SRE_Pattern object at 0x016E1A38&gt; 
# m.pos: 0 
# m.endpos: 12 
# m.lastindex: 3 
# m.lastgroup: sign 
# m.group(1,2): ('hello', 'world') 
# m.groups(): ('hello', 'world', '!') 
# m.groupdict(): {'sign': '!'} 
# m.start(2): 6 
# m.end(2): 11 
# m.span(2): (6, 11) 
# m.expand(r'\2 \1\3'): world hello!</pre><br>
</div>
</div>
</div>
<h3>2.3. Pattern</h3>
<p>Pattern对象是一个编译好的正则表达式，通过Pattern提供的一系列方法可以对文本进行匹配查找。</p>
<p>Pattern不能直接实例化，必须使用re.compile()进行构造。</p>
<p>Pattern提供了几个可读属性用于获取表达式的相关信息：</p>
<ol>
<li>pattern: 编译时用的表达式字符串。 </li><li>flags: 编译时用的匹配模式。数字形式。 </li><li>groups: 表达式中分组的数量。 </li><li>groupindex: 以表达式中有别名的组的别名为键、以该组对应的编号为&#20540;的字典，没有别名的组不包含在内。 </li></ol>
<div>
<div id="highlighter_277154" class="syntaxhighlighter  py ie">
<div class="toolbar"><span></span><pre class="python" name="code">import re 
m = re.match(r'(\w+) (\w+)(?P&lt;sign&gt;.*)', 'hello world!') 
  
print &quot;m.string:&quot;, m.string 
print &quot;m.re:&quot;, m.re 
print &quot;m.pos:&quot;, m.pos 
print &quot;m.endpos:&quot;, m.endpos 
print &quot;m.lastindex:&quot;, m.lastindex 
print &quot;m.lastgroup:&quot;, m.lastgroup 
  
print &quot;m.group(1,2):&quot;, m.group(1, 2) 
print &quot;m.groups():&quot;, m.groups() 
print &quot;m.groupdict():&quot;, m.groupdict() 
print &quot;m.start(2):&quot;, m.start(2) 
print &quot;m.end(2):&quot;, m.end(2) 
print &quot;m.span(2):&quot;, m.span(2) 
print r&quot;m.expand(r'\2 \1\3'):&quot;, m.expand(r'\2 \1\3') 
  
### output ### 
# m.string: hello world! 
# m.re: &lt;_sre.SRE_Pattern object at 0x016E1A38&gt; 
# m.pos: 0 
# m.endpos: 12 
# m.lastindex: 3 
# m.lastgroup: sign 
# m.group(1,2): ('hello', 'world') 
# m.groups(): ('hello', 'world', '!') 
# m.groupdict(): {'sign': '!'} 
# m.start(2): 6 
# m.end(2): 11 
# m.span(2): (6, 11) 
# m.expand(r'\2 \1\3'): world hello!</pre><br>
</div>
</div>
</div>
<p>实例方法[ | re模块方法]：</p>
<ol>
<li><strong>match(string[, pos[, endpos]]) | re.match(pattern, string[, flags]): <br>
</strong>这个方法将从string的pos下标处起尝试匹配pattern；如果pattern结束时仍可匹配，则返回一个Match对象；如果匹配过程中pattern无法匹配，或者匹配未结束就已到达endpos，则返回None。
<br>
pos和endpos的默认&#20540;分别为0和len(string)；re.match()无法指定这两个参数，参数flags用于编译pattern时指定匹配模式。 <br>
注意：这个方法并不是完全匹配。当pattern结束时若string还有剩余字符，仍然视为成功。想要完全匹配，可以在表达式末尾加上边界匹配符'$'。 <br>
示例参见2.1小节。 </li><li><strong>search(string[, pos[, endpos]]) | re.search(pattern, string[, flags]):
<br>
</strong>这个方法用于查找字符串中可以匹配成功的子串。从string的pos下标处起尝试匹配pattern，如果pattern结束时仍可匹配，则返回一个Match对象；若无法匹配，则将pos加1后重新尝试匹配；直到pos=endpos时仍无法匹配则返回None。
<br>
pos和endpos的默认&#20540;分别为0和len(string))；re.search()无法指定这两个参数，参数flags用于编译pattern时指定匹配模式。 <br>
<div>
<div id="highlighter_392900" class="syntaxhighlighter  py ie">
<div class="toolbar"><span></span><pre class="python" name="code">import re 
m = re.match(r'(\w+) (\w+)(?P&lt;sign&gt;.*)', 'hello world!') 
  
print &quot;m.string:&quot;, m.string 
print &quot;m.re:&quot;, m.re 
print &quot;m.pos:&quot;, m.pos 
print &quot;m.endpos:&quot;, m.endpos 
print &quot;m.lastindex:&quot;, m.lastindex 
print &quot;m.lastgroup:&quot;, m.lastgroup 
  
print &quot;m.group(1,2):&quot;, m.group(1, 2) 
print &quot;m.groups():&quot;, m.groups() 
print &quot;m.groupdict():&quot;, m.groupdict() 
print &quot;m.start(2):&quot;, m.start(2) 
print &quot;m.end(2):&quot;, m.end(2) 
print &quot;m.span(2):&quot;, m.span(2) 
print r&quot;m.expand(r'\2 \1\3'):&quot;, m.expand(r'\2 \1\3') 
  
### output ### 
# m.string: hello world! 
# m.re: &lt;_sre.SRE_Pattern object at 0x016E1A38&gt; 
# m.pos: 0 
# m.endpos: 12 
# m.lastindex: 3 
# m.lastgroup: sign 
# m.group(1,2): ('hello', 'world') 
# m.groups(): ('hello', 'world', '!') 
# m.groupdict(): {'sign': '!'} 
# m.start(2): 6 
# m.end(2): 11 
# m.span(2): (6, 11) 
# m.expand(r'\2 \1\3'): world hello!</pre><br>
</div>
</div>
</div>
</li><li><strong>split(string[, maxsplit]) | re.split(pattern, string[, maxsplit]): <br>
</strong>按照能够匹配的子串将string分割后返回列表。maxsplit用于指定最大分割次数，不指定将全部分割。 <br>
<div>
<div id="highlighter_206743" class="syntaxhighlighter  py ie">
<div class="toolbar"><span></span><pre class="python" name="code">import re 
  
p = re.compile(r'\d+') 
print p.split('one1two2three3four4') 
  
### output ### 
# ['one', 'two', 'three', 'four', ''] 

</pre><br>
</div>
</div>
</div>
</li><li><strong>findall(string[, pos[, endpos]]) | re.findall(pattern, string[, flags]):
<br>
</strong>搜索string，以列表形式返回全部能匹配的子串。 <br>
<div>
<div id="highlighter_573417" class="syntaxhighlighter  py ie">
<div class="toolbar"><span></span><pre class="python" name="code">import re 
  
p = re.compile(r'\d+') 
print p.findall('one1two2three3four4') 
  
### output ### 
# ['1', '2', '3', '4'] 

</pre><br>
</div>
</div>
</div>
</li><li><strong>finditer(string[, pos[, endpos]]) | re.finditer(pattern, string[, flags]):
<br>
</strong>搜索string，返回一个顺序访问每一个匹配结果（Match对象）的迭代器。 <br>
<div>
<div id="highlighter_297474" class="syntaxhighlighter  py ie">
<div class="toolbar"><span></span><pre class="python" name="code">import re 
  
p = re.compile(r'\d+') 
for m in p.finditer('one1two2three3four4'): 
    print m.group(), 
  
### output ### 
# 1 2 3 4 

</pre><br>
</div>
</div>
</div>
</li><li><strong>sub(repl, string[, count]) | re.sub(pattern, repl, string[, count]): <br>
</strong>使用repl替换string中每一个匹配的子串后返回替换后的字符串。 <br>
当repl是一个字符串时，可以使用\id或\g&lt;id&gt;、\g&lt;name&gt;引用分组，但不能使用编号0。 <br>
当repl是一个方法时，这个方法应当只接受一个参数（Match对象），并返回一个字符串用于替换（返回的字符串中不能再引用分组）。 <br>
count用于指定最多替换次数，不指定时全部替换。 <br>
<div>
<div id="highlighter_961166" class="syntaxhighlighter  py ie">
<div class="toolbar"><span></span><pre class="python" name="code">import re 
  
p = re.compile(r'(\w+) (\w+)') 
s = 'i say, hello world!'
  
print p.sub(r'\2 \1', s) 
  
def func(m): 
    return m.group(1).title() + ' ' + m.group(2).title() 
  
print p.sub(func, s) 
  
### output ### 
# say i, world hello! 
# I Say, Hello World! 

</pre><br>
</div>
</div>
</div>
</li><li><strong>subn(repl, string[, count]) |re.sub(pattern, repl, string[, count]): <br>
</strong>返回 (sub(repl, string[, count]), 替换次数)。 <br>
<div>
<div id="highlighter_646832" class="syntaxhighlighter  py ie">
<div class="toolbar"><span></span><pre class="python" name="code">import re 
  
p = re.compile(r'(\w+) (\w+)') 
s = 'i say, hello world!'
  
print p.subn(r'\2 \1', s) 
  
def func(m): 
    return m.group(1).title() + ' ' + m.group(2).title() 
  
print p.subn(func, s) 
  
### output ### 
# ('say i, world hello!', 2) 
# ('I Say, Hello World!', 2)</pre><br>
</div>
</div>
</div>
</li></ol>
<p><strong></strong>&nbsp;</p>
<p></p>
<div id="MySignature"></div>
<div class="clear"></div>
<img alt="" src="http://www.cnblogs.com/huxi/aggbug/1771073.html?type=1&amp;webview=1" width="1" height="1"> 后期会加入我自己的部分 我今天时间来不急了
<p></p>
<div id="blog-comments-placeholder">
<div id="comments_pager_top"></div>
<br>
</div>

</div>



   
   

