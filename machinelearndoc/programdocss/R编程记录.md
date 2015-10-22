R编程记录

[toc]

#####概览
[传送][0]
R是一种基于对象(Object)的语言，所以你在R语言中接触到的每样东西都是一个对象，一串数值向量是一个对象，一个函数是一个对象，一个图形也是一个对象。基于对象的编程(OOP)就是在定义类的基础上，创建与操作对象。

对象中包含了我们需要的数据，同时对象也具有很多属性(Attribute)。其中一种重要的属性就是它的类(Class)，R语言中最为基本的类包括了数值(numeric)、逻辑(logical)、字符(character)、列表(list)，在此基础上构成了一些复合型的类，包括矩阵(matrix)、数组(array)、因子(factor)、数据框(dataframe)。除了这些内置的类外还有很多其它的，用户还可以自定义新的类，但所有的类都是建立在这些基本的类之上的。
* 我们下面来用一个简单线性回归的例子来了解一下对象和类的处理。
```R
1	# 创建两个数值向量
2	x <- runif(100)
3	y <- rnorm(100)+5*x
4	# 用线性回归创建模型，存入对象model
5	model <- lm(y~x)
```
* 查询对象构成信息
好了，现在我们手头上有一个不熟悉的对象model，那么首先来看看它里面藏着什么好东西。最有用的函数命令就是attributes(model)，用来提取对象的各种属性，结果如下：
```R
< attributes(model)
$names
 [1] "coefficients"  "residuals"     "effects"     
 [4] "rank"          "fitted.values" "assign"     
 [7] "qr"            "df.residual"   "xlevels"     
[10] "call"          "terms"         "model"       
$class
[1] "lm"
```
可以看到这个对象的类是“lm”，这意味着什么呢？我们知道对于不同的类有不同的处理方法，那么对于modle这个对象，就有专门用来处理lm类对象的函数，例如plot.lm()。但如果你用普通的函数plot()也一样能显示其图形，Why？因为plot()这种函数会自动识别对象的类，从而选择合适的函数来对付它，这种函数就称为泛型函数（generic function）。你可以用methods(class=lm)来了解有哪些函数可适用于lm对象。
 * attributes(x)
好了，我们已经知道了model的底细了，你还想知道x的信息吧。如果运行attributes(x)，会发现返回了空值。这是因为x是一个向量，对于向量这种内置的基本类，attributes是没有什么好显示的。此时你可以运行mode(x)，可观察到向量的类是数值型。如果运行mode(model)会有什么反应呢？它会显示lm类的基本构成是由list组成的。当然要了解对象的类，也可以直接用class()，如果要消除对象的类则可用unclass()。
 * names(model)
从上面的结果我们还看到names这个属性，这如同你到一家餐厅问服务生要一份菜单，输入names(model)就相当于问model这个对象：Hi，你能提供什么好东西吗？如果你熟悉回归理论的话，就可以从names里头看到它提供了丰富的回归结果，包括回归系数（coefficients）、残差（residuals）等等，调用这些信息可以就象处理普通的数据框一样使用$符号，例如输出残差可以用model$residuals。当然用泛型函数可以达到同样的效果，如residuals(model)，但在个别情况下，这二者结果是有少许差别的
 * str()
我们已经知道了attributes的威力了，那么另外一个非常有用的函数是str()，它能以简洁的方式显示对象的数据结构及其内容，试试看，非常有用的。

#####语言基础

######输入输出
1. 读取键盘输入输出
  * readline()
     如果只有很少的数据量，你可以直接用变量赋值输入数据。若要用交互方式则可以使用readline()函数输入单个数据，但要注意其默认输入格为字符型。
  * scan()
     scan()函数中如果不加参数则也可以用来手动输入数据。如果加上文件名则是从文件中读取数据。

2. 读取文件
  * read.table() 
   读取本地表格文件的主要函数是read.table()，其中的file参数设定了文件路径，注意路径中斜杠的正确用法（如"C:/data/sample.txt"），header参数设定是否带有表头。sep参数设定了列之间的间隔方式。该函数读取数据后将存为data.frame格式，而且所有的字符将被转为因子格式，如果你不想这么做需要记得将参数stringsAsFactors设为FALSE。
   ```R
   read.table(file,header= False,sep="",encoding="UTF-8",...)
   #as.is 设置是否将字符型转化为 因子 false 转化 true不转化， 还可以设置不需要转化的列号
   #row.names 设置行名， 不设置默认为 1，2，3
   #clo.names 同上，
   #nrows 最大读取的行数
   #skip  跳过前n行
   #blank.lines.skip 跳过空白行
   #例子
   root<-"c:/users/liming/Desktop/data/"
   file <- paste(root,"read.table.data.txt",sep="")
   read.data <- read.table(file,header=True,as.is=(2,5))
   ```
  * read.fwf函数
   当文件内部的数据是固定宽度时， 可以使用函数read.fwf 来读取
   ```R
   read.fwf(file,widths,sep="\t",as.is=False,...)
   #参数同read.table很像，但是提供了widths参数来设定不同列的宽度
   file = paste(root,"read.fwf",sep="")
   read.data <- read.fwf(file,widths=c(4,20,20,3))
   ```
  * scan函数
    * 可以指定输出变量的数据类型
    * 输出对象的类型灵活，可以是数据框、向量、矩阵、列表
    * 对于大型文件的读取更快，因为可以预先设置变量类型，而不是读取完毕后再检查数据类型的一致性，
    ```R
    #file
    #what 读取后，用于说明各列数据的类型，可以使用logical.integer.numeric.complex.charactar,raw ,list
    #sep
    #skip
    #nlines
    #encoding
    #na.string  代表确实的数据类型
    file=paste(root,"data.txt",sep="")
    weibo <- scan(file,what="character",sep="@",encoding="utf-8")
    ```
  * read.csv
  ```R
  file = paste(root,"data.csv",sep="")
  data.csv<-read.csv(file)     #数据框对象
  ```
  * EXCEL
  ```R
  library(RODBC)
  excel_file <- odbcConnectExcel("D:/r/lab/data/grades.txt")
  sheet_grades<-sqlFetch(excel_file,"grades")  #数据框
  close(execel_file)
  ```
  * 读取网页表格
  ```R
  url<-'http://www.google.com/adplanner/static/top1000/'
  data<-readHTMLTable(url)
  names(data)
  head(data[[2]])
  ```
  * 读取文本文件
  有时候需要读取的数据存放在非结构化的文本文件中，例如电子邮件数据或微博数据。这种情况下只能依靠readLines()函数，将文档转为以行为单位存放的list格式。例如我们希望读取wikipedia的主页html文件的前十行。
  ```R
  data <- readLines('http://en.wikipedia.org/wiki/Main_Page',n=10)
  ```
  * 批量读取本地文件
  在批量读取文档时一般先将其存放在某一个目录下。先用dir()函数获取目录中的文件名，然后用paste()将路径合成，最后用循环或向量化方法处理文档。例如：
  ```R
  doc.names <- dir("path")
  doc.path <- sapply(doc.names,function(names) paste(path,names,sep="/"))
  doc <- sapply(doc.path,function(doc) readLines(doc))
  ```
3.输出
write.table()与write.csv()函数可以很方便的写入表格型数据文档，而cat()函数除了可以在屏幕上输出之外，也能够输出成文件。
```R
write(x,file,append=False)
#write.table 和 write.csv 使用类似
#cat 函数可以将R语言命令输出至一个外部文件，然后通过source函数运行该批处理文件
cat(...,file="",sep="",append=False)
# ...是预输出的命令，其他参数可以参考write函数说明
```

######字符串处理

一个例子：

我们来看一个处理邮件的例子，目的是从该文本中抽取发件人的地址。该文本在此可以下载到。邮件的全文如下所示：
```shell
    ----------------------------
    Return-Path: skip@pobox.com
    Delivery-Date: Sat Sep  7 05:46:01 2002
    From: skip@pobox.com (Skip Montanaro)
    Date: Fri, 6 Sep 2002 23:46:01 -0500
    Subject: [Spambayes] speed
    Message-ID: <15737.33929.716821.779152@12-248-11-90.client.attbi.com>

    If the frequency of my laptop's disk chirps are any indication, I'd say
    hammie is about 3-5x faster than SpamAssassin.

    Skip
    ----------------------------
```
 
```shell
	# 用readLines函数从本地文件中读取邮件全文。
	data <- readLines('data') 
	# 判断对象的类，确定是一个文本型向量，每行文本是向量的一个元素。
	class(data) 
	# 从这个文本向量中找到包括有"From:"字符串的那一行
	email <- data[grepl('From:',data)]
	#将其按照空格进行分割，分成一个包括四个元素的字符串向量。
	from <- strsplit(email,' ')
	# 上面的结果是一个list格式，转成向量格式。
	from <- unlist(from)
	# 最后搜索包含'@'的元素，即为发件人邮件地址。
	from <- from[grepl('@',from)]
```
尽管R语言的主要处理对象是数字，而字符串有时候也会在数据分析中占到相当大的份量。特别是在文本数据挖掘日趋重要的背景下，在数据预处理阶段你需要熟练的操作字符串对象。当然如果你擅长其它的处理软件，比如Python，可以让它来负责前期的脏活。

* 获取字符串长度：
  nchar()能够获取字符串的长度，它也支持字符串向量操作。注意它和length()的结果是有区别的。
  ```R
  d <- "4分50秒"
  len <- nchar(d)
  ```

* 字符串粘合：
  paste()负责将若干个字符串相连结，返回成单独的字符串。其优点在于，就算有的处理对象不是字符型也能自动转为字符型。
  ```R
  data <- "4分50秒"
  data <- paste("网站停留时间:","3小时",data,sep="" )
  data
  ```

* 字符串分割：
  strsplit()负责将字符串按照某种分割形式将其进行划分，它正是paste()的逆操作。
  ```R
  data <- "网站停留时间：3小时4分50秒"
  data <- strsplit(strsplit(data,split=":")[[1]][2],split="小时")
  data[[1]]
  [1]"3"  "4分50秒"
  data <- unlist（data）
  data
  [1]
  h <- as.numeric(data[1])    #提取小时数
  h
  min.sec <- data[2]
  min.sec <- unlist(strsplit(min.sec,split="分"))
  min.sec
  min <- as.numeric(min.sec[1])
  
  sec <- min.sec[2]
  sec <- unlist(strsplit(sec,split="秒"))
  sec
  sec <- as.numeric(sec[1])
  times <- 3600 * h + 60* min +sec
  times
  ```
  通常使用unlist(strsplit(<字符串>,split=<分割符>))函数将字符串分割成多个字符串的向量。

* 字符串截取：substr()能对给定的字符串对象取出子集，其参数是子集所处的起始和终止位置。
  ```R
  substr(x,start,stop)   #截取字符串
  a <- "haghfff"
  substr(a,2,4)
  [1] "agh"
  substr(a,2,4) <- "kkk"   #将字符串a的第2～4个字符串替换为kkk
  ```
* 字符串替代：
  gsub()负责搜索字符串的特定表达式，并用新的内容加以替代。sub()函数是类似的，但只替代第一个发现结果。
  ```R
  sub(pattern,replacement,x)
  gsub(pattern,replacement,x)
  ```
* 字符替换
  ```R
  chartr(old,new,x)
  ```
  
* 查找指定（pattern）的字串在字符串中的起始位置和长度
   b <- regexpr(pattern,text)
   起始位置 b[1]  长度 b[[2]]
   
   b <- gregexpr(pattern,text)
   b[[1]][1]  第一个起始位置
   b[[2]][1]  第一个长度
* 字符串匹配：
   grep()负责搜索给定字符串对象中特定表达式 ，并返回其位置索引。grepl()函数与之类似，但其后面的"l"则意味着返回的将是逻辑值。
  ```R
  a <- list("GET /News.htm HTTP/1.0","GET /feed.html HTTP/2.0")
  c <- grep(".html",a)
  [1] 2
  ```
######数据类型

######向量化运算
和matlab一样，R语言以向量为基本运算对象。也就是说，当输入的对象为向量时，对其中的每个元素分别进行处理，然后以向量的形式输出。R语言中基本上所有的数据运算均能允许向量操作。不仅如此，R还包含了许多高效的向量运算函数，这也是它不同于其它软件的一个显著特征。向量化运算的好处在于避免使用循环，使代码更为简洁、高效和易于理解。本文来对apply族函数作一个简单的归纳，以便于大家理解其中的区别所在。

所谓apply族函数包括了apply,sapply,lappy,tapply等函数，这些函数在不同的情况下能高效的完成复杂的数据处理任务，但角色定位又有所不同。

* apply()
apply()函数的处理对象是矩阵或数组，它逐行或逐列的处理数据，其输出的结果将是一个向量或是矩阵。下面的例子即对一个随机矩阵求每一行的均值。要注意的是apply与其它函数不同，它并不能明显改善计算效率，因为它本身内置为循环运算。
```R
m.data <- matrix(rnorm(100),ncol=10)
apply(m.data,1,mean)
```
* lapply()
lappy()的处理对象是向量、列表或其它对象，它将向量中的每个元素作为参数，输入到处理函数中，最后生成结果的格式为列表。在R中数据框是一种特殊的列表，所以数据框的列也将作为函数的处理对象。下面的例子即对一个数据框按列来计算中位数与标准差。
```R
f.data <- data.frame(x=rnorm(10),y=runif(10))
lapply(f.data,FUN=function(x) list(median=median(x),sd=sd(x))
```
* sapply()
 sapply()可能是使用最为频繁的向量化函数了，它和lappy()是非常相似的，但其输出格式则是较为友好的矩阵格式。
 ```R
 sapply(f.data,FUN=function(x)list(median=median(x),sd=sd(x)))
 class(test)
 ```
* tapply()
tapply()的功能则又有不同，它是专门用来处理分组数据的，其参数要比sapply多一个。我们以iris数据集为例，可观察到Species列中存放了三种花的名称，我们的目的是要计算三种花瓣萼片宽度的均值。其输出结果是数组格式。
```R
head(iris)
attach(iris)
tapply(Sepal.Width,INDEX=Species,FUN=mean)
```
与tapply功能非常相似的还有aggregate()，其输出是更为友好的数据框格式。而by()和上面两个函数是同门师兄弟。
另外还有一个非常有用的函数replicate()，它可以将某个函数重复运行N次，常常用来生成较复杂的随机数。下面的例子即先建立一个函数，模拟扔两个骰子的点数之和，然后重复运行10000次。
```R
game <- function() {
n <- sample(1:6,2,replace=T)
return(sum(n))
}
replicate(n=10000,game())
```
最后一个有趣的函数Vectorize()，它能将一个不能进行向量化运算的函数进行转化，使之具备向量化运算功能。

###### 循环与条件
* 循环
for (n in x) ｛expr}
R中最基本的是for循环，其中n为循环变量，x通常是一个序列。n在每次循环时从x中顺序取值，代入到后面的expr语句中进行运算。下面的例子即是以for循环计算30个Fibonacci数。
```R
x <- c(1,1)
for (i in 3:30) {
x[i] <- x[i-1]+x[i-2]
}
```
while (condition) {expr}
当不能确定循环次数时，我们需要用while循环语句。在condition条件为真时，执行大括号内的expr语句。下面即是以while循环来计算30个Fibonacci数。
```R
x <- c(1,1)
i <- 3
while (i &lt;= 30) {
x[i] <- x[i-1]+x[i-2]
i <- i +1
}
```
* 条件
if (conditon) {expr1} else {expr2}
if语句用来进行条件控制，以执行不同的语句。若condition条件为真，则执行expr1，否则执行expr2。ifesle()函数也能以简洁的方式构成条件语句。下面的一个简单的例子是要找出100以内的质数。
```R
x <- 1:100
y <- rep(T,100)
for (i in 3:100) {
if (all(i%%(2:(i-1))!=0)){
y[i] <- TRUE
} else {y[i] <- FALSE
}
}
print(x[y])
```
在上面例子里，all()函数的作用是判断一个逻辑序列是否全为真，%%的作用是返回余数。在if/else语句中一个容易出现的错误就是else没有放在｝的后面，若你执行下面的示例就会出现错误。
```R
logic = 3
x<- c(2,3)
if (logic == 2){
y <- x^2
}
else {
y<-x^3
}
show(y)
```
* 一个例子
本例来自于"introduction to Scientific Programming and Simulatoin Using R"一书的习题。有这样一种赌博游戏，赌客首先将两个骰子随机抛掷第一次，如果点数和出现7或11，则赢得游戏，游戏结束。如果没有出现7或11，赌客继续抛掷，如果点数与第一次扔的点数一样，则赢得游戏，游戏结束，如果点数为7或11则输掉游戏，游戏结束。如果出现其它情况，则继续抛掷，直到赢或者输。用R编程来计算赌客赢的概率，以决定是否应该参加这个游戏。
```R
craps <- function() {
#returns TRUE if you win, FALSE otherwise
    initial.roll <- sum(sample(1:6,2,replace=T))
	if (initial.roll == 7 || initial.roll == 11) return(TRUE)
	while (TRUE) {
	current.roll <- sum(sample(1:6,2,replace=T))
	if (current.roll == 7 || current.roll == 11) {
	return(FALSE)
	} else if (current.roll == initial.roll) {
	return(TRUE)
	}
	}
	}
	mean(replicate(10000, craps()))
```
从最终结果来看，赌客赢的概率为0.46，长期来看只会往外掏钱，显然不应该参加这个游戏了。最后要说的是，本题也可以用递归来做。

######程序查错

写程序难免会出错，有时候一个微小的错误需要花很多时间来调试程序来修正它。所以掌握必要的调试方法能避免很多的无用功。

基本的除错方法是跟踪重要变量的赋值情况。在循环或条件分支代码中加入显示函数能完成这个工作。例如cat('var',var,'\n')。在确认程序运行正常后，可以将这行代码进行注释。好的编程风格也能有效的减少出错的机会。在编写代码时先写出一个功能最为简单的功能，然后在此基础上逐步添加其它复杂的功能。对输出结果进行绘图或统计汇总也能揭示一些潜在的问题。

另一种避免出错的方法是尽量使用函数。使用函数能将一个大的程序分解成几个小型的模块。一个函数模块只负责实现某一种功能的实现。这样容易理解程序，而且容易针对各函数的输入、计算、输出分别进行查错调试。R语言中函数的运行不会影响到全局变量，所以使用函数基本上不会有什么副作用。

但是在使用函数时需要注意的问题是输入参数的不可预测性。未预料到的输入参数会产生奇怪的或是错误的输出，所以在函数起始部分就要用条件语句来检查参数的正确与否。如果输入参数不正确，可以用下面的语句来停止程序执行stop('your message here.')。

对函数进行调试的重要工具是browser()，它可以使我们进入调试模式逐行运行代码。在函数中的某一行插入browser()后，在函数执行时会在这一行暂停中断，并显示一个提示符。此时我们可以在提示符后输入任何R语言的交互式命令进行检查调试。输入n则会逐行运行程序，并提示下一行将运行的语句。输入c会直接跳到下一个中断点。而输入Q则会直接跟出调试模式。

debug()函数和browser()是相似的，如果你认为某个函数，例如fx(x)，有问题的话，使用debug(fx(x))即可进入调试模式。它本质上是在函数的第一行加入了browser，所以其它提示和命令都是相同的。其它与程序调试有关的函数还包括:trace(),setBreakpoint(),traceback(),recover()

#####R语言多元统计分析
地址： http://www.plob.org/2012/09/19/3465.html

#####R ggplot绘图
地址： http://blog.csdn.net/u014801157/article/category/2215813
R语言画图中文乱码： http://www.klshu.com/1807.html


教程选自30分钟学会ggplot2

###### 1. ggplot2 的基本概念
* 数据（data） 和映射(Mapping)
* 标度 （Scale）   图例 和 坐标刻度
* 几何对象（Geometric）
* 统计变换（Statistics）
* 坐标系统(Coordinate)
* 图层（Layer）
* 分面（Facet）

###### 具体实例

准备数据：
```R
library(ggplot2)

str(mpg)
'data.frame':	234 obs. of  11 variables:
 $ manufacturer: Factor w/ 15 levels "audi","chevrolet",..: 1 1 1 1 1 1 1 1 1 1 ...
 $ model       : Factor w/ 38 levels "4runner 4wd",..: 2 2 2 2 2 2 2 3 3 3 ...
 $ displ       : num  1.8 1.8 2 2 2.8 2.8 3.1 1.8 1.8 2 ...
 $ year        : int  1999 1999 2008 2008 1999 1999 2008 1999 1999 2008 ...
 $ cyl         : int  4 4 4 4 6 6 6 4 4 4 ...
 $ trans       : Factor w/ 10 levels "auto(av)","auto(l3)",..: 4 9 10 1 4 9 1 9 4 10 ...
 $ drv         : Factor w/ 3 levels "4","f","r": 2 2 2 2 2 2 2 1 1 1 ...
 $ cty         : int  18 21 20 21 16 18 18 18 16 20 ...
 $ hwy         : int  29 29 31 30 26 26 27 26 25 28 ...
 $ fl          : Factor w/ 5 levels "c","d","e","p",..: 4 4 4 4 4 4 4 4 4 4 ...
 $ class       : Factor w/ 7 levels "2seater","compact",..: 2 2 2 2 2 2 2 2 2 2 ...

```

* 绘制散点图
1. 初始绘图
```R
library(ggplot2)
p<-ggplot(data=mpg,mapping=aes(x=cty,y=hwy))
p+geom_point()
```
2. 将年份映射到颜色属性
```R
library(ggplot2)
p<-ggplot(data=mpg,mapping=aes(x=cty,y=hwy ,colour=factor(year)))
p+geom_point()
```
3. 增加平滑曲线
```R
library(ggplot2)
p<-ggplot(data=mpg,mapping=aes(x=cty,y=hwy))
p+geom_point()+stat_smooth()
```
4. 也可以这样写：
```R
p<-ggplot(mpg,aes(x=cty,y=hwy))
p+geom_point(aes(colour=factor(year))) + stat_smooth()
```

5. 写在一行的方式
```R
d <- ggplot() + geom_point(data=mpg,aes(x=cty,y=hwy,colour=factor(year)))+
   stat_smooth(data=mpg,aes(x=cty,y=hwy))
```

6. 使用标度来修改颜色取值
```R
p<-ggplot(mpg,aes(x=cty,y=hwy))
p+geom_point(aes(colour=factor(year))) + stat_smooth() +
  scale_color_manual(values=c('blue','red'))
```
7. 将排量映射到散点大小
```R
p<-ggplot(mpg,aes(x=cty,y=hwy))
p+geom_point(aes(colour=factor(year),size=displ)) + stat_smooth() +
  scale_color_manual(values=c('blue','red'))
```
8. 手动设置散点大小
```R
p<-ggplot(mpg,aes(x=cty,y=hwy))
p+geom_point(aes(colour=factor(year),size=displ),alpha=0.5,position='jitter') + stat_smooth() + scale_color_manual(values=c('blue2','red4')) + scale_size_continuous(range=c(4,10))
```
9. 用坐标控制图形显示的范围
```R
p<-ggplot(mpg,aes(x=cty,y=hwy))
p + geom_point(aes(colour=factor(year),size=displ),alpha=0.5,position="jitter") + stat_smooth() + scale_color_manual(values = c('blue2','red4')) + scale_size_continuous(range=c(4,10)) + coord_cartesian(xlim=c(15,25),ylim-c(15,40))    # 设置坐标范围
```

10.使用facet分别显示不同年份的数据
```R
p<-ggplot(mpg,aes(x=cty,y=hwy))
 p + geo_point(aes(color=class,size=displ), alpha=0.5,position = "jitter") + stat_smooth() + scale_size_continuous(range=c(4,10)) + facet_wrap(~year,ncol=1)
```

11. 增加图名并精细修改图例
```R
p <- ggplot(mpg,aes(x=cty,y=hwy))
p + geom_point(aes(colour=class,size=displ), alpha=0.5,position="jitter") + stat_smooth() + scale_size_continuous(range=c(4,10)+ facet_wrap(~year,ncol=1) + opts(title='汽车油耗与型号')+labs(y='每加仑高速公路行驶距离'，x='每加仑城市公路行驶距离')+ guides(size=guide_legend(title='排量'),colour=guide_legend(title='车型',override.aes=list(size=5)))
```

###### 直方图
1. 初始图像
```R
p<-ggplot(mpg,aes(x=hwy))
p+geom_histogram()
```
2.  复杂的
```R
p<- ggplot(mpg,aes(x=hwy))
p+geom_histogram(aes(fill=facotr(year),y=..density..),alpha=0.3,colour='black') + stat_density(geom='line', position='identity',size=1.5,aes(colour=factor(year))) + facet_wrap(~year,ncol=1)
```

###### 条形图
```R
p <- ggplot(mpg,aes(x=class))
p+geom_bar()
```

1. 根据计数排序后绘制的条形图
```R
class2 <- mpg$class; class2 <- reorder(class2,class2,length)
mpg$class2 <- class2
p <- ggplot(mpg,aes(x=class2))
p+geom_bar(aes(fill=class2)) 
```
2. 根据年份分别绘制条形图， position控制位置调整方式
```R
p <- ggplot(mpg, aes(class2,fill=factor(year))) 
p + geom_bar(position='identity',alpha=0.5)
```

3. 并立方式
```R
p+geom_bar(position='dodge')
```
4. 叠加方式
```R
p + geom_bar(position='stack')
```
5. 相对比例
```R
p + geom_bar(position='fill')
```

6. 分面显示
```shell
p + geom_bar(aes(fill=class2)) + facet_wrap(~year)
```

7. 化为饼图
```R
p <- ggplot(mpg,aes(x=factor(1),fill=factor(class))) + geom_var(width=1)
p+coord_polar(theta="y")
```

###### 箱线图
```R
p <- ggplot(mpg,aes(class,hwy,fill=class))
p+geom_boxplot()

# 另一种 绘制方法
p + geom_violin(alpha=0.3,width=0.9) + geom_jitter(shape=21)

```


###### 观察密集散点的方法
```R
p<- ggplot(diamonds,aes(carat,price))
p + geom_point()
```
使用的方法主要有：
1. 增加扰动 (jitter)
2. 增加透明度(alpha)
3. 二维直方图(stat_bin2d)
4. 密度图（stat_density2d）

1. 绘制二维直方图
```R
p + stat_bin2d(bins = 60)
```
2. 绘制二维密度图
```R
p + stat_density2d(aes(fill=..level..),geom="polygon") +
coord_cartesian(xlim=c(0,1.5),ylim=c(0,6000)) +   # 限制 坐标范围 scale_file_continuous(high='red2',low='blue4')    # 定义颜色连续范围
```
###### 更加高级的绘图
1.风向图

```R
# 随机生成100 次风向，并汇集到16个区间内
dir <- cut_interval(runif(100,0,360), n =16)

# 随机生成100 次风速， 并划分为4中强度
mag <- cut_interval(rgamma(100,15),4)
sample <- data.frame(dir=dir,mag=mag)

# 将风向映射到x轴，频数映射到y轴， 风速大小映射到填充色， 生成条形图后再转换为极坐标形式即可
p <- ggplot(sample,aes(x=dir,y=..count..,fill=mag))
p+geom_bar() + coord_polar() 
```

###### 插入数学符号
```R
# 使用的是老版本的ggplot2， 新的没有测试
intercept<- sin(4) -slope*4
x <- seq(from=0,to=2 * pi,by=0.01)
y<-sin(x)

p<-ggplot(data.frame(x,y),aes(x,y))
p + geom_area(fill=alpha('blue',0.3)) +geom_abline(intercept=intercept,slope=slope,linetype=2) +
scale_x_continuous(breaks=c(0,pi,2*pi), labels = c('0',expression(pi),expression(2 * pi))) + geom_text(parse=T,aes(x=pi/2,y=0.3,label='integral(sin(x)*dx,0,pi)'))
+ geom_line() + geom_point(aes(x=4,y=sin(4)),size=5,colour=alpha('red',0.5))
```
###### 绘制时间序列
```R
library(quantmod)
library(ggplot2)
getSymbols('^SSEC',src='yahoo',from='1997-01-01')
close <- (Cl(SSEC))
time<-index(close)
value <- as.vector(close)
yrng <- range(value)
xrng <- range(time)
data <- data.frame(start=asDate(c('1997-01-01','2003-01-01')),end=as.Date(c('2002-12-30','2012-01-20')),core=c('jiang','hu'))
timepoint <- as.Date('1999-07-02','2001-07-26','2005-04-29','2008-01-10','2013-03-31')
events <- c('事务-1'，'事务-2'，'事务-3'，'事务-4'，'事务-5')
data2 <- data.frame(timepoints,events,stock=value[time %in% timepoint])


p <- ggplot(data.frame(time,value),aes(time,value))
p + geom_line(size=1,colour='turquoise4') + 
geom_rect(alpha=0.2,aes(NULL,NULL,xmin=start,xmax=end,fill = core),ymin=yrng[1],ymax=yrng[2],data=data) +
scale_fill_manual(values=c('blue','red')) + 
geom_text(aes(timepoint,stock,label=events),data=data2,vjust=-2,size=5) + geom_point(aes(timepoint,stock),data=data2,size=5,colour='red',alpha=0.5) 

```

###### 地图绘制
```R
library(ggplot2)
library(gpclib)
library(maptools)
load(url("http://gadm.org/data/rda/CHN_adm1.RData"))
water <- c(1085,325,1473,3524,1079,2935,3989,2790,4147,358,2046,434,1652,2490,451,3362,1467,871,2145,182,1000,12278,448,377,182,1221,313,152,4976,0000,5298,2005)

gpclibPermit()
china.map <- fortify(gadm,region='D_1')
vals <- data.frame(id=unique(china.map$id),val=water)

ggplot(vals,aes(map_id = id)) + geom_map(aes(fila=val
,map=china.map$lat)+scale_fill_continuous(limits=c(0,2200
),low='red2',high='yellow',guide="colorbar") + opts(title="中国人均水资源拥有量",axis.line=theme_blank













#####R 面向对象
地址： http://blog.csdn.net/u014801157/article/category/2215795
#####实际应用例子
[传送][1]

######R利用RJDBC连接MySQL
使用版本2.14.2以上

* 准备JDBC Driver
例如放在：
 ```shell
 /application/search/software/mysql-connector-java-5.1.18-bin.jar
 ```
 
* 安装RJDBC
需要外网访问能力。CRAN镜像使用 18: China (Beijing 2)。
正确安装完后，能够在 library() 的结果中刚看到此包。
```shell
install.packages("RJDBC")
```

* 连接和断开数据库
 ```shell
 library(RJDBC)
 drv =     JDBC("com.mysql.jdbc.Driver","/application/search/software/mysql-connector-java-5.1.18-bin.jar",identifier.quote="")
 
 conn<-dbConnect(drv,"jdbc:mysql://10.10.XX.XXX:3306/test","user","password")
  
 ...
 dbDisconnect(conn)
```

* 使用数据库
```shell
sbListTables(conn)
支持Prepare Statement的SQL查询
sql <- "select * from user where age > ?"
result <- dbGetQuery(conn,sql,'6')
```
######R利用RHive连接Hadoop Hive

* R的安装和编译
R的安装直接利用 R-2.15.0.tar.gz 解压编译完成。
编译时需要加上--enable-R-static-lib --enable-R-shlib 参数，否则，安装 Rserver 时会报错。
```shell
tar -xf R-2.15.0.tar.gz
cd R-2.15.0
./configure --enable-R-static-lib --enable-R-shlib
make clean
make
```

* RHive安装
 在安装RHive之前，需要检查一下环境变量。
 ```R
 echo $HIVE_HOME
 ```
 如果没有设置的话，请在 ./bash_profile、 ./bash_rc 、或者 /etc/profile中添加：
 
 ```shell
 export HIVE_HOME=/application/search/hive
 ```
 安装RHive，进入R之后，执行以下命令，需要外网访问能力。CRAN镜像可以使用
 **18：China(Beijing 2)**会比较块
 正确安装完成后，能够在 library()的结果中刚看到此包
 ```shell
 install.packages('RHive')
 ```
 
* Hive的连接
需要注意的是，有时候HiveQL语句句尾加上分号会报错。
```R
library(RHive)
rhive.init()
rhive.env()
rhive.connect("10.10.XX.XX")
...
rhive.close()
```

* RHive的使用
需要注意的是，有时候HiveQL语句后面加上分号后会报错
```RHive
rhive.list.tables()
rhive.desc.table("imageclick")
res <- rhive.query("select count(1) from imageclick where duration > '20120315' group by referer limit 10")
##RHive中支持两种 Query，返回为data.frame
rhive.query(query,fetchsize=40,limit=-1,hiveclient=rhive.default("hiveclient"))
rhive.big.query(query,fetchsize=40,limit = -1, memlimit = 57374182,hiveclinet=rhive.defaults("hiveclient"))
```

######R wordcloud 词云的实现

* 思路
 对一天日志中的搜索请求做了标注，这些标注数据存放于MySQL中。
利用RJDBC，R能够方便的从MySQL中读取某个被标注为舆情监控的相关搜索词。用户在做舆情搜索时，很多搜索词都已经按照空格做过分词，
R WordCloud实现很简单。
将这些搜索词作为Corpus，进行按照空格分隔，计算词频，然后然后利用WordCloud将其展示出来。
* 数据提取
MySQL的数据标记，已经完成，因此
MySQL的数据标注，已经完成，因此对于此此实验只需要从MySQL取出即可。
R通过RJDBC访问的一些信息请参见：http://qing.weibo.com/2090594487/7c9bf0b7330017vr.html
```R
library(RJDBC)
drv <- JDBC("com.mysql.jdbc.Driver","D:\\software\\develop\\mysql55\\mysql-connector-java-5.1.18-bin.jar",identifier.quote="'")
conn <- dbConnect(drv,"jdbc:mysql://IP:3306/test","user","password")
sql <- "select query from t_records where tag=? and subtag like ?"
x <- dbGetQuery(conn,sql,'yuqing','%张家口%')
```
这样，我们就拿到了所有Tag为舆情和张家口的搜索词。
数据结果展示参考如下

* 画WordCloud
画WordCloud可以直接利用tm 和 wordcloud 包来实现
```R
library(tm)
library(wordcloud)
mycorpus <- Corpus(VectorSource(x[,1])) #将MySQL结果集中的搜索词放在corpus中
mycorpus <- tm_map(mycorpus,stripWhitespace)  #利用空白分词
doc.matrix <- TermDocumentMatrix(mycorpus.control = list(minWordLength = 1))   #构建DocumentMatrix ，用于计算词频
dm <- as.matrix(doc.matrix)
v <- sort(rowSums(dm),decreasing=T)    #将搜索词按照词频排序
d <- data.frame(word=names(v),freq=v)
mycolors <- colorRampPalette(c("white","red"))(200)
wc <- wordcloud(d$word,d$freq,colors=mycolors[100:200])   #画图
```
* 其他问题
尝试用tm为googl爬虫的搜索词构建DocumentMatrix，结果内存溢出而失败，后续可以考察是否有分布式执行的方式。
目前的wordcloud展现结果比较丑陋，可以考虑利用其它画图方式和字体比率修正得更好看一些。

###### R利用幂律分布的Alpha值检查用户搜索行为的变化
* 基本思路
幂律关注的是分布，而非具体的点击量，因此能够比较好的解释群体用户的行为方式。幂律的Alpha值越小，表示用户点击次数从少到多衰减越慢，说明用户对于搜索结果的点击数越多。
相比具体的点击量、CTR数据，幂律更关注的是点击次数的分布，因此受流量波动影响小。下图的横轴为Click点击次数，纵轴为对应的频次。
统计了两个月内的每次用户搜索的对应点击次数，使用R语言计算其每日的Alpha变化。

* 利用R进行幂律计算
```R
library(igraph)
library(stats4)
res <- read.table("clipboard") #偷了个懒，将数据导入到Excel中，从剪切板导入点击数据
datastr <- read.table("clipboard") #直接从剪切版中导入处理的日期列表
zz<-file("ex.data","w")
for(i in row(datastr)) {  #分别计算每天的Alpha值
res.sub<-subset(res.V1 == datastr[i,1]);
a <- power.law.fit(res.sub[,2]);
dput(a@coef,file=zz)
}
close(zz)   #将幂律计算的结果写到外部文件中
```
经过处理之后，得到Alpha计算结果，利用图像能够比较清晰的看出其变化趋势。


* 结果分析
从上面的结果数据里，我们可以看到非常有意思的内容。
新闻Alpha值基本变化不大，利用K检验和W检验，我们可以得出新闻Alpha值的分布能够符合正态分布。
```R
nx <- alpha[,4]
ks.test(nx,"pnorm",mean = mean(nx),sd=sqrt(var(nx)))
shapiro.test(nx)
```
图片Alpha值，在图片新版上线的5月11日有个很明显的变化，从原来的2.4左右，变化到1.7左右。
首先检查5月1日到10日，5月11日到20日，两组图片Alpha值均符合正态分布。
利用T检验，能够检查到，5月11日前后10天数据具有明显差距。 
```R
t.test(alpha[1:10,3],alpha[11:20,3])
```
网页Alpha值，在6月17日前后，有微弱下降，经检验，此变化是显著地，这个时点确实有产品变化。首先检6月7日到17日，6月18日到28日，两组图片Alpha值均符合正态分布。利用T检验，能够检查到，6月17日前后11天数据具有明显差距。
取同期的网页访问点击效率CTR数据，进行T检验，并不能检查出显著差距。利用T检验，还检查了6月6日前后11天的网页Alpha、6月17日前后11天的新闻Alpha，均未能检查出明显差距。

* 结论
从上面的验证可以看出，利用幂律Alpha的变化，能够确实体现系统发生的变化，未来可以长期观察此数据，作为衡量搜索引擎质量提升的一种监测手段。
需要提醒注意的是，这种度量仅仅能够体现有点击的搜索结果，没有点击的搜索，没法度量。

* 附注
Alpha的变化到底是指质量提升还是下降，这一点是有待商榷的。
对于图片搜索来说，我们可以大致认为Alpha下降，意味着用户对于搜索结果更感兴趣，每次搜索引发了更多的点击。在一般意义上来说，能够认为是质量提升。
但是对于网页搜索来说，Alpha下降，不能简单来看，因为不能判断以前点的少的原因是什么？如果是认为以前搜索质量差而点击量少，那么这个Alpha的下降，就意味着质量更好了。如果参考Google的说法，Google追求的是用户更快的找到所需要的内容并离开，从这个意义上来说，Alpha应该上升才会更好。综合这两个方面，随着搜索引擎质量的提升，Alpha会先降后升，并达到一个上限。这个变化中的最低点，会有什么特别的意义呢？好奇中。。。































[0]:http://www.plob.org/2012/09/21/3574.html
[1]:http://qing.blog.sina.com.cn/2090594487/profile