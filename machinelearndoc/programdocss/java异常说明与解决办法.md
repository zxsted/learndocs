####java异常说明与解决办法
[原文][1]

[toc]
######1. java.lang.nullpointerexception
  这个异常大家肯定都经常遇到，异常的解释是"程序遇上了空指针"，简单地说就是调用了未经初始化的对象或者是不存在的对象，这个错误经常出现在创建图片，调用数组这些操作中，比如图片未经初始化，或者图片创建时的路径错误等等。对数组操作中出现空指针，很多情况下是一些刚开始学习编程的朋友常犯的错误，即把数组的初始化和数组元素的初始化混淆起来了。数组的初始化是对数组分配需要的空间，而初始化后的数组，其中的元素并没有实例化，依然是空的，所以还需要对每个元素都进行初始化（如果要调用的话）

######  2. java.lang.classnotfoundexception
  这个异常是很多原本在jb等开发环境中开发的程序员，把jb下的程序包放在wtk下编译经常出现的问题，异常的解释是"指定的类不存在"，这里主要考虑一下类的名称和路径是否正确即可，如果是在jb下做的程序包，一般都是默认加上package的，所以转到wtk下后要注意把package的路径加上。

 ###### 3. java.lang.arithmeticexception
  这个异常的解释是"数学运算异常"，比如程序中出现了除以零这样的运算就会出这样的异常，对这种异常，大家就要好好检查一下自己程序中涉及到数学运算的地方，公式是不是有不妥了。

######  4. java.lang.arrayindexoutofboundsexception
  这个异常相信很多朋友也经常遇到过，异常的解释是"数组下标越界"，现在程序中大多都有对数组的操作，因此在调用数组的时候一定要认真检查，看自己调用的下标是不是超出了数组的范围，一般来说，显示（即直接用常数当下标）调用不太容易出这样的错，但隐式（即用变量表示下标）调用就经常出错了，还有一种情况，是程序中定义的数组的长度是通过某些特定方法决定的，不是事先声明的，这个时候，最好先查看一下数组的length，以免出现这个异常。

######  5. java.lang.illegalargumentexception
  这个异常的解释是"方法的参数错误"，很多j2me的类库中的方法在一些情况下都会引发这样的错误，比如音量调节方法中的音量参数如果写成负数就会出现这个异常，再比如g.setcolor(int red,int green,int blue)这个方法中的三个值，如果有超过255的也会出现这个异常，因此一旦发现这个异常，我们要做的，就是赶紧去检查一下方法调用中的参数传递是不是出现了错误。

######  6. java.lang.illegalaccessexception
  这个异常的解释是"没有访问权限"，当应用程序要调用一个类，但当前的方法即没有对该类的访问权限便会出现这个异常。对程序中用了package的情况下要注意这个异常。

  其他还有很多异常，我就不一一列举了，我要说明的是，一个合格的程序员，需要对程序中常见的问题有相当的了解和相应的解决办法，否则仅仅停留在写程序而不会改程序的话，会极大影响到自己的开发的。关于异常的全部说明，在api里都可以查阅。

######算术异常类：ArithmeticExecption

######空指针异常类：NullPointerException

######类型强制转换异常：ClassCastException

######数组负下标异常：NegativeArrayException

######数组下标越界异常：ArrayIndexOutOfBoundsException

######违背安全原则异常：SecturityException

######文件已结束异常：EOFException

######文件未找到异常：FileNotFoundException

######字符串转换为数字异常：NumberFormatException

######操作数据库异常：SQLException

######输入输出异常：IOException

######方法未找到异常：NoSuchMethodException

######java.lang.AbstractMethodError

抽象方法错误。当应用试图调用抽象方法时抛出。

######java.lang.AssertionError

断言错。用来指示一个断言失败的情况。

######java.lang.ClassCircularityError

类循环依赖错误。在初始化一个类时，若检测到类之间循环依赖则抛出该异常。

######java.lang.ClassFormatError

类格式错误。当Java虚拟机试图从一个文件中读取Java类，而检测到该文件的内容不符合类的有效格式时抛出。

######java.lang.Error

错误。是所有错误的基类，用于标识严重的程序运行问题。这些问题通常描述一些不应被应用程序捕获的反常情况。

######java.lang.ExceptionInInitializerError

初始化程序错误。当执行一个类的静态初始化程序的过程中，发生了异常时抛出。静态初始化程序是指直接包含于类中的static语句段。

######java.lang.IllegalAccessError

违法访问错误。当一个应用试图访问、修改某个类的域（Field）或者调用其方法，但是又违反域或方法的可见性声明，则抛出该异常。

######java.lang.IncompatibleClassChangeError

不兼容的类变化错误。当正在执行的方法所依赖的类定义发生了不兼容的改变时，抛出该异常。一般在修改了应用中的某些类的声明定义而没有对整个应用重新编译而直接运行的情况下，容易引发该错误。

######java.lang.InstantiationError

实例化错误。当一个应用试图通过Java的new操作符构造一个抽象类或者接口时抛出该异常.

######java.lang.InternalError

内部错误。用于指示Java虚拟机发生了内部错误。

######java.lang.LinkageError

链接错误。该错误及其所有子类指示某个类依赖于另外一些类，在该类编译之后，被依赖的类改变了其类定义而没有重新编译所有的类，进而引发错误的情况。

######java.lang.NoClassDefFoundError

未找到类定义错误。当Java虚拟机或者类装载器试图实例化某个类，而找不到该类的定义时抛出该错误。

######java.lang.NoSuchFieldError

域不存在错误。当应用试图访问或者修改某类的某个域，而该类的定义中没有该域的定义时抛出该错误。

######java.lang.NoSuchMethodError

方法不存在错误。当应用试图调用某类的某个方法，而该类的定义中没有该方法的定义时抛出该错误。

######java.lang.OutOfMemoryError

内存不足错误。当可用内存不足以让Java虚拟机分配给一个对象时抛出该错误。

######java.lang.StackOverflowError

堆栈溢出错误。当一个应用递归调用的层次太深而导致堆栈溢出时抛出该错误。

######java.lang.ThreadDeath

线程结束。当调用Thread类的stop方法时抛出该错误，用于指示线程结束。

######java.lang.UnknownError

未知错误。用于指示Java虚拟机发生了未知严重错误的情况。

######java.lang.UnsatisfiedLinkError

未满足的链接错误。当Java虚拟机未找到某个类的声明为native方法的本机语言定义时抛出。

######java.lang.UnsupportedClassVersionError

不支持的类版本错误。当Java虚拟机试图从读取某个类文件，但是发现该文件的主、次版本号不被当前Java虚拟机支持的时候，抛出该错误。

######java.lang.VerifyError

验证错误。当验证器检测到某个类文件中存在内部不兼容或者安全问题时抛出该错误。

######java.lang.VirtualMachineError

虚拟机错误。用于指示虚拟机被破坏或者继续执行操作所需的资源不足的情况。

######java.lang.ArithmeticException

算术条件异常。譬如：整数除零等。

######java.lang.ArrayIndexOutOfBoundsException

数组索引越界异常。当对数组的索引值为负数或大于等于数组大小时抛出。

######java.lang.ArrayStoreException

数组存储异常。当向数组中存放非数组声明类型对象时抛出。

######java.lang.ClassCastException

类造型异常。假设有类A和B（A不是B的父类或子类），O是A的实例，那么当强制将O构造为类B的实例时抛出该异常。该异常经常被称为强制类型转换异常。

######java.lang.ClassNotFoundException

找不到类异常。当应用试图根据字符串形式的类名构造类，而在遍历CLASSPAH之后找不到对应名称的class文件时，抛出该异常。

######java.lang.CloneNotSupportedException

不支持克隆异常。当没有实现Cloneable接口或者不支持克隆方法时,调用其clone()方法则抛出该异常。

######java.lang.EnumConstantNotPresentException

枚举常量不存在异常。当应用试图通过名称和枚举类型访问一个枚举对象，但该枚举对象并不包含常量时，抛出该异常。

######java.lang.Exception

根异常。用以描述应用程序希望捕获的情况。

######java.lang.IllegalAccessException

违法的访问异常。当应用试图通过反射方式创建某个类的实例、访问该类属性、调用该类方法，而当时又无法访问类的、属性的、方法的或构造方法的定义时抛出该异常。

######java.lang.IllegalMonitorStateException

违法的监控状态异常。当某个线程试图等待一个自己并不拥有的对象（O）的监控器或者通知其他线程等待该对象（O）的监控器时，抛出该异常。

######java.lang.IllegalStateException

违法的状态异常。当在Java环境和应用尚未处于某个方法的合法调用状态，而调用了该方法时，抛出该异常。

######java.lang.IllegalThreadStateException

违法的线程状态异常。当县城尚未处于某个方法的合法调用状态，而调用了该方法时，抛出异常。

######java.lang.IndexOutOfBoundsException

索引越界异常。当访问某个序列的索引值小于0或大于等于序列大小时，抛出该异常。

######java.lang.InstantiationException

实例化异常。当试图通过newInstance()方法创建某个类的实例，而该类是一个抽象类或接口时，抛出该异常。

######java.lang.InterruptedException

被中止异常。当某个线程处于长时间的等待、休眠或其他暂停状态，而此时其他的线程通过Thread的interrupt方法终止该线程时抛出该异常。

######java.lang.NegativeArraySizeException

数组大小为负值异常。当使用负数大小值创建数组时抛出该异常。

######java.lang.NoSuchFieldException

属性不存在异常。当访问某个类的不存在的属性时抛出该异常。

######java.lang.NoSuchMethodException

方法不存在异常。当访问某个类的不存在的方法时抛出该异常。

######java.lang.NullPointerException

空指针异常。当应用试图在要求使用对象的地方使用了null时，抛出该异常。譬如：调用null对象的实例方法、访问null对象的属性、计算null对象的长度、使用throw语句抛出null等等。

######java.lang.NumberFormatException

数字格式异常。当试图将一个String转换为指定的数字类型，而该字符串确不满足数字类型要求的格式时，抛出该异常。

######java.lang.RuntimeException

运行时异常。是所有Java虚拟机正常操作期间可以被抛出的异常的父类。

######java.lang.SecurityException

安全异常。由安全管理器抛出，用于指示违反安全情况的异常。

######java.lang.StringIndexOutOfBoundsException

字符串索引越界异常。当使用索引值访问某个字符串中的字符，而该索引值小于0或大于等于序列大小时，抛出该异常。

java.lang.TypeNotPresentException

类型不存在异常。当应用试图

[1]:http://zhidao.baidu.com/link?url=GO2GC0Th2lurBdWV2zphHHMD6SxQOphbuQG9QdwypKDXE-uTqY5j_9fOcpteFR_L4DeDx39QIJX47vybMBPy2q