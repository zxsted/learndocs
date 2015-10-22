####Makefile 编写

[toc]

#####一个最简单的Makefile例子

[传送](http://hi.baidu.com/hellosim/item/a5d413a9c52de991141073c2)

######1.hello.c
```c
#include <stdio.h>
int main()
{
    printf("Hello World!\n");
    return 0;
}
```
######2.Makefile
```shell
hello : hello.o
    cc -o hello hello.o

hello.o : hello.c
    cc -c hello.c

clean : 
    rm hello.o
```
说明：cc -o hello hello.o前面是一个tab的空格

######3.执行make，产生可执行文件hello

输出如下：
```shell
cc -c hello.c
cc -o hello hello.o
```
######4.执行make clean，删除产生的中间文件hello.o

输出如下：
```shell
rm hello.o
```

[另转](http://hi.baidu.com/s_rlzheng/blog/item/4bc10a06892e217d030881ef.html)

##### 多文件的例子
本来早就应该学Makefile了，只是我偷懒，现在才学呵呵^_^makefile的好处叫我说，老实说我也说不出什么之所以然来，但是可以肯定的是它是linux平台的软件工程师都要必备的知识^_^


######一、程序的编译及链接

   我们编译程序，无非是想要得到一个可执行文件，而这个过程则是经过这两步：

***.c->编译->***.o->链接->可执行文件。即.c经过编译得到.o文件，.o文件是一个中间文件，再对这些中间文件进行链接最终可得到可执行文件。


######二、Makefile的规则

首先，来看一看Makefile的书写规则：
```shell
target ... : prerequisites ...

            command

            ...

            ...
```

   target也就是一个目标文件，可以是.o文件，也可以是执行文件，还可以是一个标签（Label）。

  prerequisites就是，要生成那个target所需要的文件或是目标。

  command也就是make需要执行的命令（任意的Shell命令）。这里要注意的是在命令前面要加上一个tab键，不是空格，是按一个tab键按出来的空格。

   这是一个文件的依赖关系，也就是说，target这一个或多个的目标文件依赖于prerequisites中的文件，其生成规则定义在command中。说白一点就是说，prerequisites中如果有一个以上的文件比 target文件要新的话，command所定义的命令就会被执行。这就是Makefile的规则，也就是Makefile中最核心的内容。这是Makefile的主线和核心，但要写好一个Makefile还得继续努力。


######三、一个最简单的Makefile例子

如有一个工程，含有3个头文件及四个c文件，那为了生成所需的可执行文件，这时的Makefile可以这样写：
```C
test:main.o t1.o t2.o t3.o

     gcc –o test main.o t1.o t2.o t3.o

main.o:main.c

     gcc –c main.c

t1.o:t1.c t1.h

     gcc –c t1.c

t2.o:t2.c t2.h

     gcc –c t2.c

t3.o:t3.c t3.h

     gcc –c t3.c

clean:

     rm test main.o t1.o t2.o t3.o
```
到这里一个最简单的makefile就写好了，把它的名字保存为Makefile就可以了，这时你只要在终端敲一下make，它就自动帮你编译链接了^_^如果敲入make clean，它将删掉那些中间文件及可执行文件。

#####linux平台makefile编写基础
[传送](http://www.cnblogs.com/goodcandle/archive/2005/11/17/278702.html)

######目的：
   基本掌握了 make 的用法，能在Linux系统上编程。
######环境：
   Linux系统，或者有一台Linux服务器，通过终端连接。一句话：有Linux编译环境。
######准备：
   准备三个文件：file1.c, file2.c, file2.h
   
   file1.c:
   ```shell
   			  #include <stdio.h>
              #include "file2.h"
              int main()
              {
                     printf("print file1$$$$$$$$$$$$$$$$$$$$$$$$\n");
                     File2Print();
                     return 0;
              }
```
 file2.h:
```shell
              #ifndef FILE2_H_
              #define    FILE2_H_

                      #ifdef __cplusplus

                            extern "C" {

                     #endif

                     void File2Print();

                     #ifdef __cplusplus

                            }

                     #endif

              #endif
```

 file2.c:
 ```shell
              #include "file2.h"
              void File2Print()
              {
                     printf("Print file2**********************\n");
              }
```

######基础：
   先来个例子：
   有这么个Makefile文件。（文件和Makefile在同一目录）
 ```shell
       === makefile 开始 ===
              helloworld:file1.o file2.o
                     gcc file1.o file2.o -o helloworld

              file1.o:file1.c file2.h
                     gcc -c file1.c -o file1.o

               file2.o:file2.c file2.h

                     gcc -c file2.c -o file2.o


              clean:

                     rm -rf *.o helloworld

       === makefile 结束 ===
```
一个 makefile 主要含有一系列的规则，如下：
A: B
(tab)<command>
(tab)<command>

每个命令行前都必须有tab符号。

 

上面的makefile文件目的就是要编译一个helloworld的可执行文件。让我们一句一句来解释：
```shell
   helloworld : file1.o file2.o：                 helloworld依赖file1.o file2.o两个目标文件。

   gcc File1.o File2.o -o helloworld：      编译出helloworld可执行文件。-o表示你指定 的目标文件名。

   file1.o : file1.c：    file1.o依赖file1.c文件。

   gcc -c file1.c -o file1.o：                  编译出file1.o文件。-c表示gcc 只把给它的文件编译成目标文件， 用源码文件的文件名命名但把其后缀由“.c”或“.cc”变成“.o”。在这句中，可以省略-o file1.o，编译器默认生成file1.o文件，这就是-c的作用。
              file2.o : file2.c file2.h
              gcc -c file2.c -o file2.o
这两句和上两句相同。

 

       clean:

              rm -rf *.o helloworld
```
当用户键入make clean命令时，会删除*.o 和helloworld文件。

如果要编译cpp文件，只要把gcc改成g++就行了。

写好Makefile文件，在命令行中直接键入make命令，就会执行Makefile中的内容了。

到这步我想你能编一个Helloworld程序了。

######上一层楼：使用变量

  上面提到一句，如果要编译cpp文件，只要把gcc改成g++就行了。但如果Makefile中有很多gcc，那不就很麻烦了。

   第二个例子：
```shell
       === makefile 开始 ===
              OBJS = file1.o file2.o
              CC = gcc
              CFLAGS = -Wall -O -g

              helloworld : $(OBJS)
                     $(CC) $(OBJS) -o helloworld

              file1.o : file1.c file2.h
                     $(CC) $(CFLAGS) -c file1.c -o file1.o

              file2.o : file2.c file2.h
                     $(CC) $(CFLAGS) -c file2.c -o file2.o

 

              clean:

                     rm -rf *.o helloworld
=== makefile 结束 ===
```
   这里我们应用到了变量。要设定一个变量，你只要在一行的开始写下这个变量的名字，后 面跟一个 = 号，后面跟你要设定的这个变量的值。以后你要引用 这个变量，写一个 $ 符号，后面是围在括号里的变量名。

CFLAGS = -Wall -O –g，解释一下。这是配置编译器设置，并把它赋值给CFFLAGS变量。

-Wall：          输出所有的警告信息。

-O：              在编译时进行优化。

-g：               表示编译debug版本。

 

  这样写的Makefile文件比较简单，但很容易就会发现缺点，那就是要列出所有的c文件。如果你添加一个c文件，那就需要修改Makefile文件，这在项目开发中还是比较麻烦的。


######再上一层楼：使用函数

   学到这里，你也许会说，这就好像编程序吗？有变量，也有函数。其实这就是编程序，只不过用的语言不同而已。
  第三个例子：
```shell
       === makefile 开始 ===
              CC = gcc

              XX = g++
              CFLAGS = -Wall -O –g

              TARGET = ./helloworld

              %.o: %.c

                     $(CC) $(CFLAGS) -c $< -o $@

              %.o:%.cpp

                     $(XX) $(CFLAGS) -c $< -o $@

 

              SOURCES = $(wildcard *.c *.cpp)
              OBJS = $(patsubst %.c,%.o,$(patsubst %.cpp,%.o,$(SOURCES)))


              $(TARGET) : $(OBJS)
                     $(XX) $(OBJS) -o $(TARGET)

                     chmod a+x $(TARGET)

clean:

       rm -rf *.o helloworld
=== makefile 结束 ===
```
函数1：wildcard

  产生一个所有以 '.c' 结尾的文件的列表。

  SOURCES = $(wildcard *.c *.cpp)表示产生一个所有以 .c，.cpp结尾的文件的列表，然后存入变量 SOURCES 里。


函数2：patsubst

  匹配替换，有三个参数。第一个是一个需要匹配的式样，第二个表示用什么来替换它，第三个是一个需要被处理的由空格分隔的列表。

OBJS = $(patsubst %.c,%.o,$(patsubst %.cc,%.o,$(SOURCES)))表示把文件列表中所有的.c,.cpp字符变成.o，形成一个新的文件列表，然后存入OBJS变量中。
```shell
%.o: %.c

       $(CC) $(CFLAGS) -c $< -o $@

%.o:%.cpp

       $(XX) $(CFLAGS) -c $< -o $@

       这几句命令表示把所有的.c,.cpp编译成.o文件。
```
   这里有三个比较有用的内部变量。$@ 扩展成当前规则的目的文件名， $< 扩展成依靠      列表中的第一个依靠文件，而 $^ 扩展成整个依靠的列表（除掉了里面所有重 复的文件名）。

 chmod a+x $(TARGET)表示把helloworld强制变成可执行文件。

 到这里，我想你已经能够编写一个比较简单也比较通用的Makefile文件了，上面所有的例子都假定所有的文件都在同一个目录下，不包括子目录。

 那么文件不在一个目录可以吗？
 怎么编写Makefile生成静态库？
 你还想更上一层楼吗？
 
 #####多文件夹下的makfile编写
 [传送](http://blog.sina.com.cn/s/blog_73d4d5fa0100paiy.html)
工程中的代码分别存放在add/add_int.c、add/add_float.c、add/add.h、sub/sub_int.c、sub/sub_float.c、sub/sub.h、main.c中。

 
文件main.c
```C
#include <stdio.h>
#include "add.h"            
#include "sub.h"
int main(void)
{
       int input = 0;
       int a = 10, b = 12;
       float x= 1.23456,y = 9.87654321;
      
       printf("int a+b IS:%d\n",a+b);
       printf("int a-b IS:%d\n",a-b);
       printf("float x+y IS:%f\n",x+y);
       printf("float x-y IS:%f\n",x-y);
 
 
       return 0;      
}
```
加操作 
```C
#ifdef __ADD_H__
#define __ADD_H__
extern int add_int(int a, int b);
extern float add_float(float a, float b);
#endif  
float add_float(float a, float b)
{
       return a+b;  
}  
int add_int(int a, int b)
{
       return a+b;  
}
```
减操作
```C
#ifdef __ADD_H__
#define __ADD_H__
extern float sub_float(float a, float b);
extern int sub_int(int a, int b);
#endif 
float sub_float(float a, float b)
{
       return a-b;   
} 
int sub_int(int a, int b)
{
       return a-b;   
}
```
######命令行编译程序：
```shell
#gcc -c add/add_int.c -o add/add_int.o      #生成add_int.o目标函数
#gcc -c add/add_float.c -o add/add_float.o   #生成add_float.o目标函数
#gcc -c sub/sub_int.c -o sub/sub_int.o       #生成sub_int.o目标函数
#gcc -c sub/sub_float.c -o sub/sub_float.o    #生成sub_float.o目标函数
#gcc -c main.c -o main.o                  #生成main.o目标函数
#gcc -o casu add/add_int.o add/add_float.o sub/sub_int.o sub/sub_float.o main.o
#链接生成cacu
```
######多文件的makefile：
```shell
#生成casu，“；”右边为目标
casu:add_int.o add_float.o sub_int.o sub_float.o main.o
       gcc -o casu add/add_int.o add/add_float.o \          # \为连接符
                     sub/sub_int.o sub/sub_float.o main.o    
#生成add_int.o的规则，将add_int.c编译生成目标文件add_int.o
add_int.o:add/add_int.c add/add.h
       gcc -c -o add/add_int.o add/add_int.c
#生成add_float.o的规则
add_float.o:add/add_float.c add/add.h
       gcc -c -o add/add_float.o add/add_float.c
#生成sub_int.o的规则
sub_int.o:sub/sub_int.c sub/sub.h
       gcc -c -o sub/sub_int.o sub/sub_int.c
#生成sub_float.o的规则
sub_float.o:sub/sub_float.c sub/sub.h
       gcc -c -o sub/sub_float.o sub/sub_float.c
#生成main.o的规则
main.o:main.c add/add.h sub/sub.h
       gcc -c -o main.o main.c -Iadd -Isub
      
#清理规则
clean:
       rm -f casu add/add_int.o add/add_float.o \
             sub/sub_int.o sub/sub_float.o main.o
```
######使用自定义变量的makefile文件：
```shell
CC = gcc                                #CC定义成gcc
CFLAGES =    -Iadd -Isub -O2   #加入头文件搜索路径sub和add，O2为优化#目标文件
OBJS = add/add_int.o add/add_float.o \
              sub/sub_int.o sub/sub_float.o main.o
TARGET = casu                           #生成的可执行文件
RM = rm -f                              #删除的命令
$(TARGET):$(OBJS)                       #TARGET目标，需要先生成OBJS目标
       $(CC) -o $(TARGET) $(OBJS) $(CFLAGES)   #生成可执行文件
$(OBJS):%.o:%.c          #将OBJS中所有扩展名为.o的文件替换成扩展名为.c的文件
       $(CC) -c $(CFLAGES) $< -o $@        #采用CFLAGS指定的选项编译生成目标文件
clean:                               #清理
       -$(RM) $(TARGET) $(OBJS)           #删除所有的目标文件和可执行文件
######使用预定义变量的makefile文件：
CFLAGES =    -Iadd -Isub -O2               #编译选项
OBJS = add/add_int.o add/add_float.o \
               sub/sub_int.o sub/sub_float.o main.o
TARGET = casu                          #生成的可执行文件
$(TARGET):$(OBJS)                       #TARGET目标，需要先生成OBJS目标
       $(CC) -o $(TARGET) $(OBJS) $(CFLAGES)  #生成可执行文件
$(OBJS):%.o:%.c          #将OBJS中所有扩展名为.o的文件替换成扩展名为.c的文件
       $(CC) -c $(CFLAGES) $< -o $@        #采用CFLAGS指定的选项编译生成目标文件
clean:                               #清理
       -$(RM) $(TARGET) $(OBJS)           #删除所有的目标文件和可执行文件
######使用自动变量的makefile文件：
CFLAGES =    -Iadd -Isub -O2               #编译选项
OBJS = add/add_int.o add/add_float.o \
               sub/sub_int.o sub/sub_float.o main.o
TARGET = casu                           #生成的可执行文件
$(TARGET):$(OBJS)                       #TARGET目标，需要先生成OBJS目标
       $(CC) -o  $@  $^ $(CFLAGES)          #生成可执行文件
$(OBJS):%.o:%.c          #将OBJS中所有扩展名为.o的文件替换成扩展名为.c的文件
       $(CC) -c $<  $(CFLAGES) -o $@      #采用CFLAGS指定的选项编译生成目标文件
clean:                               #清理
       -$(RM) $(TARGET) $(OBJS)           #删除所有的目标文件和可执行文件
```
######使用搜索路径的makefile文件：
```shell
CFLAGES =    -Iadd -Isub -O2  
OBJSDIR = .objs
VPATH = add:sub:.        
OBJS = add_int.o add_float.o sub_int.o sub_float.o main.o
TARGET = casu                          
$(TARGET):$(OBJSDIR) $(OBJS)      #先检测OBJSDIR和OBJS依赖项是否存在                 
       $(CC) -o $(TARGET) $(OBJSDIR)/*.o $(CFLAGES)
 #将OBJSDIR目录中所有的.o文件链接成casu          
$(OBJS):%.o:%.c         
       $(CC) -c  $< $(CFLAGES) -o $(OBJSDIR)/$@ #生成目标文件，存放在OBJSDIR目录中
$(OBJSDIR):
       mkdir -p ./$@            #建立目录，-p选项可以忽略父目录不存在的错误
clean:                                
       -$(RM) $(TARGET)        #删除casu
       -$(RM) $(OBJSDIR)/*.o    #删除OBJSDIR目录下的所有.o文件
```
######使用自动推导规则的makefile：
```shell
CFLAGS = -Iadd -Isub -O2         # 用户自定义变量
VPATH=add:sub                # 搜索路径
OBJS = add_int.o add_float.o sub_int.o sub_float.o main.o
TARGET = cacu
$(TARGET):$(OBJS)             #OBJS依赖项的规则自动生成
       $(CC) -o $(TARGET) $(OBJS) $(CFLAGS)    #链接文件
clean:
       -$(RM) $(TARGET)        #“-”表示当前操作失败时不报错，命令继续执行
       -$(RM) $(OBJS)
```
######使用函数的makefile文件：
```C
CC = gcc                               #CC定义成gcc
VPATH =add:sub
CFLAGES =    -Iadd -Isub -O2     #加入头文件搜索路径sub和add，O2为优化#目标文件
TARGET = casu                         #生成的可执行文件
DIRS = sub add .                     #DIRS字符串的值为目录add、sub和当前目录
FILES = $(foreach dir, $(DIRS),$(wildcard $(dir)/*.c))  #查找所用目录下的.c文件
OBJS = $(patsubst %.c,%.o,$(FILES))          #将.c替换成.o
$(TARGET):$(OBJS)                        #TARGET目标，需要先生成OBJS目标
       $(CC) -o $(TARGET) $(OBJS) $(CFLAGES)   #生成可执行文件
clean:                                  #清理
       -$(RM) $(TARGET) $(OBJS)              #删除所有的目标文件和可执行文件
```


#####一个经典的makefile的例子
这个帖子是我在csdn上花了10分下载下来的一个包，里面就这么多东西，那个上传者是他妈的黑。。。。不过我把它共享到这里，不过为了尊重人的劳动成果，我还是贴出人家的下载地址：http://download.csdn.net/source/949149，这里面的内容很经典，但是我水平低，很多看不懂，咳，不过，先占着。呵呵。。。。。

```shell
########################################################################################
#
# Generic Makefile for C/C++ Program
#
# Author: mengk
# Date:   2008/08/30
#=======================================================================================
 
 
 
# 一 、 操作系统及shell相关
########################################################################################
#指定使用的shell及取得操作系统类型，宏定义常用shell命令
 
#指定SHELL ,SHELL := /bin/sh ,或者使用当前SHELL设置
#SHELL := /bin/bash
 
#取得操作系统名称#OS_NAME="Linux:SunOS:HP-UX:AIX"
OS_NAME := $(shell uname -s)
 
 
#把常用的几个系统命令自定义名称和选现,rm命令前面加了一个小减号的意思就是，
#也许某些文件出现问题，但不要管，继续做后面的事
 
AR := ar            
SED:= sed          
AWK:= awk
MV := mv
RM := rm -f
ECHO := echo
 
#=======================================================================================
 
 
# 二 、C编译器选项
########################################################################################
#指定C编译器, 如gcc  编译器
CC      := gcc
#指定C编译时的选项
#CFLAGS         C语言编译器参数,编译时使用。
CFLAGS := -c -g  -W -Wall
 
# CPP ,  C 预编译器的名称，默认值为 $(CC) -E。
CPP :=
#   CPPFLAGS , C 预编译的选项。
CPPFLAGS :=
 
 
# 三 、C++编译器选项
########################################################################################
#=======================================================================================
 
#指定C++编译器, 如g++ 编译器
CXX      := g++
 
#指定C编译时的选项
#CXXFLAGS         C++语言编译器参数,编译时使用。
CXXFLAGS := -c -g -W -Wall
 
# CXXPP ,  C++ 预编译器的名称，默认值为 $(CC) -E。
CXXPP :=
#   CXXPPFLAGS , C++ 预编译的选项。
CXXPPFLAGS :=
 
#=======================================================================================
 
 
# 四、指定额外搜索的头文件路径、库文件路径 、引入的库
########################################################################################
#指定搜索路径, 也可用include指定具体文件路径,编译时使用
# The include files ( C and C++ common).
INCLUDES := -I$(ORACLE_HOME)/rdbms/demo -I$(ORACLE_HOME)/rdbms/public  \
  -I$(ORACLE_HOME)/plsql/public -I$(ORACLE_HOME)/network/public  -I./include -I./include/app -I./include/tools  \
  -I./include/tools/file  -I./include/tools/common
 
# 指定函数库搜索路径DIRECTORY 搜寻库文件(*.a)的路径,加入需要的库搜索路径 功能同–l，由用户指定库的路径，否则编译器将只在标准库的目录找。           
#连接时使用
LIBDIRS :=-L$(ORACLE_HOME)/lib -L$(ORACLE_HOME)/rdbms/lib
 
# 链接器参数,  连接时搜索指定的函数库LDFLAGS。,引入需要的库-lLDFLAGS    指定编译的时候使用的库. 连接库文件开关。例如-lugl，则是把程序同libugl.a文件进行连接。
#连接时使用
#-lclntsh -lnsl -lpthread -Wl,-Bdynamic -lgcc_s    ,同时有动态库和静态库时默认使用动态库，   -Wl,-Bdynamic 指定和动态库相连， -Wl,-Bstatic 指定和静态库相连
CLDFLAGS    :=  -lm  -lclntsh -lnsl -lpthread  -Wl,-Bdynamic  -lgcc_s
CXXLDFLAGS  :=  -lm  -lclntsh -lnsl -lpthread  -Wl,-Bdynamic -lgcc_s  -lstdc++ 
 
#宏定义，如果没有定义宏的值，默认是字符串1 ,定义值为数字时直接写数字，字符和字符串需用 \"和\'转义
#DCPPFLAGS :=  -D${OS_NAME}   -D_TEST1_  -D_TEST2_=2  -D_TEST3_=\"a\"  -D_TEST4_=\'b\' -DOS_NAME=\"${OS_NAME}\"
DCPPFLAGS := -D${OS_NAME}   
 
#各平台'SunOS'   'Linux' link类库差异, 设置特定值
ifeq '${OS_NAME}' 'SunOS'
    CLDFLAGS += -lsocket
    CXXLDFLAGS += -lsocket
    DCPPFLAGS += -D_POSIX_PTHREAD_SEMANTICS -D_REENTRANT
endif
 
#=======================================================================================
 
 
#  五、 指定源文件的路径 、支持的源文件的扩展名 、源文件搜索路径
########################################################################################
# 指定SRC_DIR 源代码文件路径./src  ./src2   src2/src3
SRC_DIR   := .  ./src  ./src/copyfile  ./src/displayfile ./include/tools/file  ./include/tools/common
 
 
#指定支持的源代码扩展名 SFIX     := .out .a .ln  .o  .c  .cc .C  .p  .f  .F 
#.r  .y  .l  .s  .S  .mod  .sym  .def  .h  .info  .dvi  .tex  .texinfo  .texi 
#.txinfo  .w  .ch .web  .sh  .elc  .el
SFIX     :=  .c .C .cpp  .cc .CPP  .c++  .cp  .cxx
 
#在当当前目录找不到的情况下，到VPATH所指定的目录中去找寻文件了。如:VPATH = src:../headers
#（当然，当前目录永远是最高优先搜索的地方）
VPATH := ${SRC_DIR}
 
#定义安装目录            
BIN := ./bin
 
#=======================================================================================
 
 
#  六、 得到源文件名称集合、OBJS目标文件名集合
########################################################################################
 
#依次循环取得各目录下的所有源文件，在各目录下取源文件时过滤不支持的源文件格式，
#得到源文件集合(带路径)
SOURCES := $(foreach x,${SRC_DIR},\
           $(wildcard  \
             $(addprefix  ${x}/*,${SFIX}) ) )
 
#去掉路径信息，去掉扩展名，再追加.o的扩展名，得到目标文件名集合 (不带路径),需要去掉路径信息，否则连接时有可能找不到.o文件
OBJS := $(addsuffix .o ,$(basename $(notdir ${SOURCES}) ) )    
 
 
#去掉路径信息，去掉扩展名，再追加.d的扩展名，得到依赖文件名集合 (不带路径)
#DEPENDS := $(addsuffix .d ,$(basename $(notdir ${SOURCES}) ) )
 
#去掉扩展名，再追加.d的扩展名，得到依赖文件名集合 (带路径)
DEPENDS := $(addsuffix .d ,$(basename  ${SOURCES} ) )  
#DEPENDS := $(SOURCES:$(SFIX)=.d)
 
#=======================================================================================
 
 
#  七、 定义生成程序的名称
########################################################################################
 
#生成可执行程序的名称
PROGRAM   := example
 
#=======================================================================================
 
 
#  八、 定义依赖关系 ，编译、链接规则
########################################################################################
 
#.PHONY”表示，clean是个伪目标文件。
.PHONY : all check  clean  install
 
 
#定义编译、链接任务all
all :  ${PROGRAM}  install
 
#检查源码中，除了C源码外是否有C++源码 ,并定义变量LDCXX存储检查结果
LDCXX := $(strip $(filter-out  %.c , ${SOURCES} ) )
 
#编译器重置
ifdef LDCXX   #有C++源码时,所有源码都使用g++编译，包括C源码，将CC、CFLAGS 的值设置为对应的${CXX}、 ${CXXFLAGS}的值
    CC := ${CXX}                    #重置C编译器为C++编译器
    CFLAGS :=  ${CXXFLAGS}          #重置C编译选现为C++编译选现
    CPP :=  ${CXXPP}                #重置C预编译器为C++预编译器
    CPPFLAGS := ${CXXPPFLAGS}       #重置C预编译的选项为C++预编译的选项
endif
 
#链接
${PROGRAM} :  ${DEPENDS}  ${OBJS} 
ifeq ($(strip $(filter-out  %.c  , ${SOURCES} ) ),)    #只有C源码时使用gcc连接
    ${CC}  ${LIBDIRS}  ${CLDFLAGS}    ${OBJS} -o $@    
else                                                 #有C++源码时使用g++连接
    $(CXX) ${LIBDIRS}  ${CXXLDFLAGS}    ${OBJS} -o $@     
endif
 
# Rules for producing the objects. (.o) BEGIN
#---------------------------------------------------
 
%.o : %.c
    $(CC)      ${DCPPFLAGS}    ${CFLAGS}      ${INCLUDES}   $<
 
%.o : %.C
    $(CXX)     ${DCPPFLAGS}    ${CXXFLAGS}    ${INCLUDES}   $<
 
%.o : %.cc
    ${CXX}     ${DCPPFLAGS}    ${CXXFLAGS}    ${INCLUDES}   $<
 
%.o : %.cpp
    ${CXX}     ${DCPPFLAGS}    ${CXXFLAGS}    ${INCLUDES}   $<
 
%.o : %.CPP
    ${CXX}     ${DCPPFLAGS}    ${CXXFLAGS}    ${INCLUDES}   $<
 
%.o : %.c++
    ${CXX}     ${DCPPFLAGS}    ${CXXFLAGS}    ${INCLUDES}   $<
 
%.o : %.cp
    ${CXX}     ${DCPPFLAGS}    ${CXXFLAGS}    ${INCLUDES}   $<
 
%.o : %.cxx
    ${CXX}     ${DCPPFLAGS}    ${CXXFLAGS}    ${INCLUDES}   $<
 
#---------------------------------------------------
# Rules for producing the objects.(.o) END
 
 
# Rules for creating the dependency files (.d). BEGIN
#---------------------------------------------------
%.d : %.c
    @${CC}     -M   -MD    ${INCLUDES} $<
 
%.d : %.C
    @${CXX}    -MM  -MD    ${INCLUDES} $<
 
%.d : %.cc
    @${CXX}    -MM  -MD    ${INCLUDES} $<
 
%.d : %.cpp
    @${CXX}    -MM  -MD    ${INCLUDES} $<
 
%.d : %.CPP
    @${CXX}    -MM  -MD    ${INCLUDES} $<
 
%.d : %.c++
    @${CXX}    -MM  -MD    ${INCLUDES} $<
 
%.d : %.cp
    @${CXX}    -MM  -MD    ${INCLUDES} $<
 
%.d : %.cxx
    @${CXX}    -MM  -MD    ${INCLUDES} $<
 
#---------------------------------------------------
# Rules for creating the dependency files (.d). END
 
 
#=======================================================================================
 
 
#  九、 定义其他 check  install clean 等任务
########################################################################################
 
#定义检查环境相关的变量的任务
check :
    @${ECHO}  MAKEFILES : ${MAKEFILES}
    @${ECHO}  MAKECMDGOALS : ${MAKECMDGOALS}
    @${ECHO}  SHELL  : ${SHELL}
    @${ECHO}  OS_NAME  : ${OS_NAME}
    @${ECHO}  SRC_DIR : ${SRC_DIR}
    @${ECHO}  SFIX : ${SFIX}
    @${ECHO}  VPATH : ${VPATH}
    @${ECHO}  BIN : ${BIN}
    @${ECHO}  SOURCES : ${SOURCES}
    @${ECHO}  OBJS : ${OBJS}
    @${ECHO}  DEPENDS : ${DEPENDS}
    @${ECHO}  PROGRAM : ${PROGRAM}
    @${ECHO}  CC :  ${CC}
    @${ECHO}  CFLAGS : ${CFLAGS}
    @${ECHO}  CPP : ${CPP}
    @${ECHO}  CPPFLAGS : ${CPPFLAGS}
    @${ECHO}  CXX :  ${CXX}
    @${ECHO}  CXXFLAGS : ${CXXFLAGS}
    @${ECHO}  CXXPP : ${CXXPP}
    @${ECHO}  CXXPPFLAGS : ${CXXPPFLAGS}       
    @${ECHO}  INCLUDES : ${INCLUDES}
    @${ECHO}  LIBDIRS : ${LIBDIRS}
    @${ECHO}  CLDFLAGS : ${CLDFLAGS}
    @${ECHO}  CXXLDFLAGS : ${CXXLDFLAGS}
    @${ECHO}  DCPPFLAGS : ${DCPPFLAGS}
    uname    -a
 
#定义清理的任务 core.*  ,rm命令前面加了一个小减号的意思就是， 也许某些文件出现问题，但不要管，继续做后面的事
clean :
    -${RM} ${BIN}/${PROGRAM}
    -${RM} ${BIN}/*.o
    -${RM} ${BIN}/*.d
    -${RM} *.o
    -${RM} *.d
 
#将目标文件及可执行程序拷贝到安装目录
install :
    -${MV} ${PROGRAM} ${BIN}
    -${MV}  *.o ${BIN}
    -${MV}  *.d ${BIN}
 
 
#=======================================================================================
```