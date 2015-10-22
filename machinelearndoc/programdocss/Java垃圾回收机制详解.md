**Java垃圾回收机制详解**

[传送](http://www.importnew.com/16173.html)

[toc]

##### 一、 垃圾回收机制的意义

ava语言中一个显著的特点就是引入了垃圾回收机制，使c++程序员最头疼的内存管理的问题迎刃而解，它使得Java程序员在编写程序的时候不再需要考虑内存管理。由于有个垃圾回收机制，Java中的对象不再有“作用域”的概念，只有对象的引用才有“作用域”。垃圾回收可以有效的防止内存泄露，有效的使用空闲的内存。

ps:内存泄露是指该内存空间使用完毕之后未回收，在不涉及复杂数据结构的一般情况下，Java 的内存泄露表现为一个内存对象的生命周期超出了程序需要它的时间长度，我们有时也将其称为“对象游离”。

##### 二、 垃圾回收机制中的算法

Java语言规范没有明确地说明JVM使用哪种垃圾回收算法，但是任何一种垃圾回收算法一般要做2件基本的事情：（1）发现无用信息对象；（2）回收被无用对象占用的内存空间，使该空间可被程序再次使用。

###### 1.引用计数法

1.1 算法分析

引用计数是垃圾收集器中的早期策略。在这种方法中，堆中每个对象实例都有一个引用计数。当一个对象被创建时，且将该对象实例分配给一个变量，该变量计数设置为1。当任何其它变量被赋值为这个对象的引用时，计数加1（a = b,则b引用的对象实例的计数器+1），但当一个对象实例的某个引用超过了生命周期或者被设置为一个新值时，对象实例的引用计数器减1。任何引用计数器为0的对象实例可以被当作垃圾收集。当一个对象实例被垃圾收集时，它引用的任何对象实例的引用计数器减1。


1.2 优缺点

* 优点
 引用计数收集器可以很快的执行，交织在程序运行中。对程序需要不被长时间打断的实时环境比较有利。
 
* 缺点
 无法检测出循环引用。如父对象有一个对子对象的引用，子对象反过来引用父对象。这样，他们的引用计数永远不可能为0.
 
1.3 无法解决的问题: 循环引用

```java
public class Main {
    public static void main(String[] args) {
        MyObject object1 = new MyObject();
        MyObject object2 = new MyObject();
          
        object1.object = object2;
        object2.object = object1;
          
        object1 = null;
        object2 = null;
    }
}
```

最后面两句将object1和object2赋值为null，也就是说object1和object2指向的对象已经不可能再被访问，但是由于它们互相引用对方，导致它们的引用计数器都不为0，那么垃圾收集器就永远不会回收它们。


###### 2 tracing算法(Tracing Collector) 或 标记-清除算法(mark and sweep)

2.1 根搜索算法

![](http://ww1.sinaimg.cn/mw690/7178f37egw1etbmyb4jugj20ku0ajmy0.jpg)

根搜索算法是从离散数学中的图论引入的，程序把所有的引用关系看作一张图，从一个节点GC ROOT开始，寻找对应的引用节点，找到这个节点以后，继续寻找这个节点的引用节点，当所有的引用节点寻找完毕之后，剩余的节点则被认为是没有被引用到的节点，即无用的节点。

java中可作为GC Root的对象有

1. 虚拟机栈中引用的对象（本地变量表）

2. 方法区中静态属性引用的对象

3. 方法区中常量引用的对象

4. 本地方法栈中引用的对象（Native对象）


2.2 tracing 算法的示意图

![](http://ww1.sinaimg.cn/mw690/7178f37egw1etbmyakm6pj20fi0crmxm.jpg)

2.3 标记清除算法分析
标记-清除算法采用从根集合进行扫描，对存活的对象对象标记，标记完毕后，再扫描整个空间中未被标记的对象，进行回收，如上图所示。标记-清除算法不需要进行对象的移动，并且仅对不存活的对象进行处理，在存活对象比较多的情况下极为高效，但由于标记-清除算法直接回收不存活的对象，因此会造成内存碎片。

###### 3. compacting算法 或 标记-整理算法

![](http://ww3.sinaimg.cn/mw690/7178f37egw1etbmybx9qij20gy0g7js1.jpg)


标记-整理算法采用标记-清除算法一样的方式进行对象的标记，但在清除时不同，在回收不存活的对象占用的空间后，会将所有的存活对象往左端空闲空间移动，并更新对应的指针。标记-整理算法是在标记-清除算法的基础上，又进行了对象的移动，因此成本更高，但是却解决了内存碎片的问题。在基于Compacting算法的收集器的实现中，一般增加句柄和句柄表。

###### 4. copying 算法 （Compacting Collector）
![](http://ww2.sinaimg.cn/mw690/7178f37egw1etbmybcowsj20g308l3yp.jpg)


该算法的提出是为了克服句柄的开销和解决堆碎片的垃圾回收。它开始时把堆分成 一个对象 面和多个空闲面， 程序从对象面为对象分配空间，当对象满了，基于copying算法的垃圾 收集就从根集中扫描活动对象，并将每个 活动对象复制到空闲面(使得活动对象所占的内存之间没有空闲洞)，这样空闲面变成了对象面，原来的对象面变成了空闲面，程序会在新的对象面中分配内存。一种典型的基于coping算法的垃圾回收是stop-and-copy算法，它将堆分成对象面和空闲区域面，在对象面与空闲区域面的切换过程中，程序暂停执行。

###### 5. generation 算法（Generational Collect）

![](http://ww3.sinaimg.cn/mw690/7178f37egw1etbmycakylj20fn08kgmb.jpg)

**年轻代（Young Generation）**

1. 所有新生成的对象首先都是放在年轻代的。年轻代的目标就是尽可能快速的收集掉那些生命周期短的对象。

2. 新生代内存按照8:1:1的比例分为一个eden区和两个survivor(survivor0,survivor1)区。一个Eden区，两个 Survivor区(一般而言)。大部分对象在Eden区中生成。回收时先将eden区存活对象复制到一个survivor0区，然后清空eden区，当这个survivor0区也存放满了时，则将eden区和survivor0区存活对象复制到另一个survivor1区，然后清空eden和这个survivor0区，此时survivor0区是空的，然后将survivor0区和survivor1区交换，即保持survivor1区为空， 如此往复。

3. 当survivor1区不足以存放 eden和survivor0的存活对象时，就将存活对象直接存放到老年代。若是老年代也满了就会触发一次Full GC，也就是新生代、老年代都进行回收

4. 新生代发生的GC也叫做Minor GC，MinorGC发生频率比较高(不一定等Eden区满了才触发)

**年老代（Old Generation）**

1. 在年轻代中经历了N次垃圾回收后仍然存活的对象，就会被放到年老代中。因此，可以认为年老代中存放的都是一些生命周期较长的对象。

2. 内存比新生代也大很多(大概比例是1:2)，当老年代内存满时触发Major GC即Full GC，Full GC发生频率比较低，老年代对象存活时间比较长，存活率标记高。

**持久代（Permanent Generation）**

用于存放静态文件，如Java类、方法等。持久代对垃圾回收没有显著影响，但是有些应用可能动态生成或者调用一些class，例如Hibernate 等，在这种时候需要设置一个比较大的持久代空间来存放这些运行过程中新增的类。




##### 三、 GC （垃圾回收器）

新生代收集器使用的收集器：Serial、PraNew、Parallel Scavenge

老年代收集器使用的收集器：Serial Old、Parallel Old、CMS

![](http://ww1.sinaimg.cn/mw690/7178f37egw1etbmycjfvoj20e40engmi.jpg)


###### Serial收集器（复制算法）

新生代单线程收集器，标记和清理都是单线程，优点是简单高效。

###### Serial Old 收集器(标记整理算法)

老年代单线程收集器，Serial收集器的老年代版本。

###### ParNew 收集器(停止-复制算法)

新生代收集器，可以认为是Serial收集器的多线程版本,在多核CPU环境下有着比Serial更好的表现。

###### Parallel Scavenge收集器(停止-复制算法)

并行收集器，追求高吞吐量，高效利用CPU。吞吐量一般为99%， 吞吐量= 用户线程时间/(用户线程时间+GC线程时间)。适合后台应用等对交互相应要求不高的场景。

###### Parallel Old收集器(停止-复制算法)

Parallel Scavenge收集器的老年代版本，并行收集器，吞吐量优先

###### CMS(Concurrent Mark Sweep)收集器（标记-清理算法）

高并发、低停顿，追求最短GC回收停顿时间，cpu占用比较高，响应时间快，停顿时间短，多核cpu 追求高响应时间的选择



##### 四、GC 的执行机制

由于对象进行了分代处理，因此垃圾回收区域、时间也不一样。GC有两种类型：Scavenge GC和Full GC。

###### Scavenge GC

一般情况下，当新对象生成，并且在Eden申请空间失败时，就会触发Scavenge GC，对Eden区域进行GC，清除非存活对象，并且把尚且存活的对象移动到Survivor区。然后整理Survivor的两个区。这种方式的GC是对年轻代的Eden区进行，不会影响到年老代。因为大部分对象都是从Eden区开始的，同时Eden区不会分配的很大，所以Eden区的GC会频繁进行。因而，一般在这里需要使用速度快、效率高的算法，使Eden去能尽快空闲出来。

###### Full GC

对整个堆进行整理，包括Young、Tenured和Perm。Full GC因为需要对整个堆进行回收，所以比Scavenge GC要慢，因此应该尽可能减少Full GC的次数。在对JVM调优的过程中，很大一部分工作就是对于FullGC的调节。有如下原因可能导致Full GC：

1. 年老代（Tenured）被写满

2. 持久代（Perm）被写满

3. System.gc()被显示调用

4. 上一次GC之后Heap的各域分配策略动态变化

##### 五、 Java有了GC同样会出现内存泄露问题

###### 1. 静态集合类像HashMap、Vector等的使用最容易出现内存泄露，这些静态变量的生命周期和应用程序一致，所有的对象Object也不能被释放，因为他们也将一直被Vector等应用着。

```java
static Vecotr v = new Vector();
for (int i = 1 ; i < 100; i++)
{
	Object o = new Object();
    v.add(o)
    o=null;
}
```

在这个例子中，代码栈中存在Vector 对象的引用 v 和 Object 对象的引用 o 。在 For 循环中，我们不断的生成新的对象，然后将其添加到 Vector 对象中，之后将 o 引用置空。问题是当 o 引用被置空后，如果发生 GC，我们创建的 Object 对象是否能够被 GC 回收呢？答案是否定的。因为， GC 在跟踪代码栈中的引用时，会发现 v 引用，而继续往下跟踪，就会发现 v 引用指向的内存空间中又存在指向 Object 对象的引用。也就是说尽管o 引用已经被置空，但是 Object 对象仍然存在其他的引用，是可以被访问到的，所以 GC 无法将其释放掉。如果在此循环之后， Object 对象对程序已经没有任何作用，那么我们就认为此 Java 程序发生了内存泄漏。

2. 各种连接，数据库连接，网络连接，IO连接等没有显示调用close关闭，不被GC回收导致内存泄露。

3. 监听器的使用，在释放对象的同时没有相应删除监听器的时候也可能导致内存泄露。



##### 10个提升java性能的方法

你是否正打算优化hashCode()方法？是否想要绕开正则表达式？Lukas Eder介绍了很多简单方便的性能优化小贴士以及扩展程序性能的技巧。

最近“全网域（Web Scale）”一词被炒得火热，人们也正在通过扩展他们的应用程序架构来使他们的系统变得更加“全网域”。但是究竟什么是全网域？或者说如何确保全网域？
扩展的不同方面

全网域被炒作的最多的是扩展负载（Scaling load），比如支持单个用户访问的系统也可以支持10 个、100个、甚至100万个用户访问。在理想情况下，我们的系统应该保持尽可能的“无状态化（stateless）”。即使必须存在状态，也可以在网络的不同处理终端上转化并进行传输。当负载成为瓶颈时候，可能就不会出现延迟。所以对于单个请求来说，耗费50到100毫秒也是可以接受的。这就是所谓的横向扩展（Scaling out）。

扩展在全网域优化中的表现则完全不同，比如确保成功处理一条数据的算法也可成功处理10条、100条甚至100万条数据。无论这种度量类型是是否可行，事件复杂度（大O符号）是最佳描述。延迟是性能扩展杀手。你会想尽办法将所有的运算处理在同一台机器上进行。这就是所谓的纵向扩展（Scaling up）。

如果天上能掉馅饼的话（当然这是不可能的），我们或许能把横向扩展和纵向扩展组合起来。但是，今天我们只打算介绍下面几条提升效率的简单方法。
大O符号

Java 7的 ForkJoinPool 和Java8 的并行数据流（parallel Stream） 都对并行处理有所帮助。当在多核处理器上部署Java程序时表现尤为明显，因所有的处理器都可以访问相同的内存。

所以，这种并行处理较之在跨网络的不同机器上进行扩展，根本的好处是几乎可以完全消除延迟。

但不要被并行处理的效果所迷惑！请谨记下面两点：

    并行处理会吃光处理器资源。并行处理为批处理带来了极大的好处，但同时也是非同步服务器（如HTTP）的噩梦。有很多原因可以解释，为什么在过去的几十年中我们一直在使用单线程的Servlet模型。并行处理仅在纵向扩展时才能带来实际的好处。
    并行处理对算法复杂度没有影响。如果你的算法的时间复杂度为 O(nlogn)，让算法在 c 个处理器上运行，事件复杂度仍然为 O(nlogn/c)， 因为 c 只是算法中的一个无关紧要的常量。你节省的仅仅是时钟时间（wall-clock time），实际的算法复杂度并没有降低。

降低算法复杂度毫无疑问是改善性能最行之有效的办法。比如对于一个 HashMap 实例的 lookup() 方法来说，事件复杂度 O(1) 或者空间复杂度 O(1) 是最快的。但这种情况往往是不可能的，更别提轻易地实现。

如果你不能降低算法的复杂度，也可以通过找到算法中的关键点并加以改善的方法，来起到改善性能的作用。假设我们有下面这样的算法示意图：

![](http://incdn1.b0.upaiyun.com/2015/06/12efd009e184ae13f88568ff6fc140c2.png)

该算法的整体时间复杂度为 O(N3)，如果按照单独访问顺序计算也可得出复杂度为 O(N x O x P)。但是不管怎样，在我们分析这段代码时会发现一些奇怪的场景：

    在开发环境中，通过测试数据可以看到：左分支（N->M->Heavy operation）的时间复杂度 M 的值要大于右边的 O 和 P，所以在我们的分析器中仅仅看到了左分支。
    在生产环境中，你的维护团队可能会通过 AppDynamics、DynaTrace 或其它小工具发现，真正导致问题的罪魁祸首是右分支（N -> O -> P -> Easy operation or also N.O.P.E.）。

在没有生产数据参照的情况下，我们可能会轻易的得出要优化“高开销操作”的结论。但我们做出的优化对交付的产品没有起到任何效果。

优化的金科玉律不外乎以下内容：

    良好的设计将会使优化变得更加容易。
    过早的优化并不能解决多有的性能问题，但是不良的设计将会导致优化难度的增加。

理论就先谈到这里。假设我们已经发现了问题出现在了右分支上，很有可能是因产品中的简单处理因耗费了大量的时间而失去响应（假设N、O和 P 的值非常大）， 请注意文章中提及的左分支的时间复杂度为 O(N3)。这里所做出的努力并不能扩展，但可以为用户节省时间，将困难的性能改善推迟到后面再进行。

这里有10条改善Java性能的小建议：
1、使用StringBuilder

StingBuilder 应该是在我们的Java代码中默认使用的，应该避免使用 + 操作符。或许你会对 StringBuilder 的语法糖（syntax sugar）持有不同意见，比如：
	
String x = "a" + args.length + "b";

将会被编译为：

	
0  new java.lang.StringBuilder [16]
 3  dup
 4  ldc <String "a"> [18]
 6  invokespecial java.lang.StringBuilder(java.lang.String) [20]
 9  aload_0 [args]
10  arraylength
11  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [23]
14  ldc <String "b"> [27]
16  invokevirtual java.lang.StringBuilder.append(java.lang.String) : java.lang.StringBuilder [29]
19  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [32]
22  astore_1 [x]

但究竟发生了什么？接下来是否需要用下面的部分来对 String 进行改善呢？

	
String x = "a" + args.length + "b";
 
if (args.length == 1)
    x = x + args[0];

现在使用到了第二个 StringBuilder，而且这个 StringBuilder 不会消耗堆中额外的内存，但却给 GC 带来了压力。

	
StringBuilder x = new StringBuilder("a");
x.append(args.length);
x.append("b");
 
if (args.length == 1);
    x.append(args[0]);
小结

在上面的样例中，如果你是依靠Java编译器来隐式生成实例的话，那么编译的效果几乎和是否使用了 StringBuilder 实例毫无关系。请记住：在  N.O.P.E 分支中，每次CPU的循环的时间到白白的耗费在GC或者为 StringBuilder 分配默认空间上了，我们是在浪费 N x O x P 时间。

一般来说，使用 StringBuilder 的效果要优于使用 + 操作符。如果可能的话请在需要跨多个方法传递引用的情况下选择 StringBuilder，因为 String 要消耗额外的资源。JOOQ在生成复杂的SQL语句便使用了这样的方式。在整个抽象语法树（AST Abstract Syntax Tree）SQL传递过程中仅使用了一个 StringBuilder 。

更加悲剧的是，如果你仍在使用 StringBuffer 的话，那么用 StringBuilder 代替 StringBuffer 吧，毕竟需要同步字符串的情况真的不多。
2、避免使用正则表达式

正则表达式给人的印象是快捷简便。但是在 N.O.P.E 分支中使用正则表达式将是最糟糕的决定。如果万不得已非要在计算密集型代码中使用正则表达式的话，至少要将 Pattern 缓存下来，避免反复编译Pattern。
	
static final Pattern HEAVY_REGEX =
    Pattern.compile("(((X)*Y)*Z)*");

如果仅使用到了如下这样简单的正则表达式的话：
	
String[] parts = ipAddress.split("\\.");

这是最好还是用普通的 char[] 数组或者是基于索引的操作。比如下面这段可读性比较差的代码其实起到了相同的作用。

	
int length = ipAddress.length();
int offset = 0;
int part = 0;
for (int i = 0; i < length; i++) {
    if (i == length - 1 ||
            ipAddress.charAt(i + 1) == '.') {
        parts[part] =
            ipAddress.substring(offset, i + 1);
        part++;
        offset = i + 2;
    }
}

上面的代码同时表明了过早的优化是没有意义的。虽然与 split() 方法相比较，这段代码的可维护性比较差。

挑战：聪明的小伙伴能想出更快的算法吗？
小结

正则表达式是十分有用，但是在使用时也要付出代价。尤其是在 N.O.P.E 分支深处时，要不惜一切代码避免使用正则表达式。还要小心各种使用到正则表达式的JDK字符串方法，比如 String.replaceAll() 或 String.split()。可以选择用比较流行的开发库，比如 Apache Commons Lang 来进行字符串操作。
3、不要使用iterator()方法

这条建议不适用于一般的场合，仅适用于在 N.O.P.E 分支深处的场景。尽管如此也应该有所了解。Java 5格式的循环写法非常的方便，以至于我们可以忘记内部的循环方法，比如：
	
for (String value : strings) {
    // Do something useful here
}

当每次代码运行到这个循环时，如果 strings 变量是一个 Iterable 的话，代码将会自动创建一个Iterator 的实例。如果使用的是 ArrayList 的话，虚拟机会自动在堆上为对象分配3个整数类型大小的内存。

	
private class Itr implements Iterator<E> {
    int cursor;
    int lastRet = -1;
    int expectedModCount = modCount;
    // ...

也可以用下面等价的循环方式来替代上面的 for 循环，仅仅是在栈上“浪费”了区区一个整形，相当划算。

	
int size = strings.size();
for (int i = 0; i < size; i++) {
    String value : strings.get(i);
    // Do something useful here
}

如果循环中字符串的值是不怎么变化，也可用数组来实现循环。
	
for (String value : stringArray) {
    // Do something useful here
}
小结

无论是从易读写的角度来说，还是从API设计的角度来说迭代器、Iterable接口和 foreach 循环都是非常好用的。但代价是，使用它们时是会额外在堆上为每个循环子创建一个对象。如果循环要执行很多很多遍，请注意避免生成无意义的实例，最好用基本的指针循环方式来代替上述迭代器、Iterable接口和 foreach 循环。
讨论

一些与上述内容持反对意见的看法（尤其是用指针操作替代迭代器）详见Reddit上的讨论。
4、不要调用高开销方法

有些方法的开销很大。以 N.O.P.E 分支为例，我们没有提到叶子的相关方法，不过这个可以有。假设我们的JDBC驱动需要排除万难去计算 ResultSet.wasNull() 方法的返回值。我们自己实现的SQL框架可能像下面这样：

	
if (type == Integer.class) {
    result = (T) wasNull(rs,
        Integer.valueOf(rs.getInt(index)));
}
 
// And then...
static final <T> T wasNull(ResultSet rs, T value)
throws SQLException {
    return rs.wasNull() ? null : value;
}

在上面的逻辑中，每次从结果集中取得 int 值时都要调用 ResultSet.wasNull() 方法，但是 getInt() 的方法定义为：

    返回类型：变量值；如果SQL查询结果为NULL，则返回0。

所以一个简单有效的改善方法如下：
	
static final <T extends Number> T wasNull(
    ResultSet rs, T value
)
throws SQLException {
    return (value == null ||
           (value.intValue() == 0 && rs.wasNull()))
        ? null : value;
}

这是轻而易举的事情。
小结

将方法调用缓存起来替代在叶子节点的高开销方法，或者在方法约定允许的情况下避免调用高开销方法。
5、使用原始类型和栈

上面介绍了来自 jOOQ的例子中使用了大量的泛型，导致的结果是使用了 byte、 short、 int 和 long 的包装类。但至少泛型在Java 10或者Valhalla项目中被专门化之前，不应该成为代码的限制。因为可以通过下面的方法来进行替换：
	
//存储在堆上
Integer i = 817598;

……如果这样写的话：
	
// 存储在栈上
int i = 817598;

在使用数组时情况可能会变得更加糟糕：
	
//在堆上生成了三个对象
Integer[] i = { 1337, 424242 };

……如果这样写的话：
	
// 仅在堆上生成了一个对象
int[] i = { 1337, 424242 };
小结

当我们处于 N.O.P.E. 分支的深处时，应该极力避免使用包装类。这样做的坏处是给GC带来了很大的压力。GC将会为清除包装类生成的对象而忙得不可开交。

所以一个有效的优化方法是使用基本数据类型、定长数组，并用一系列分割变量来标识对象在数组中所处的位置。

遵循LGPL协议的 trove4j 是一个Java集合类库，它为我们提供了优于整形数组 int[] 更好的性能实现。
例外

下面的情况对这条规则例外：因为 boolean 和 byte 类型不足以让JDK为其提供缓存方法。我们可以这样写：
	
Boolean a1 = true; // ... syntax sugar for:
Boolean a2 = Boolean.valueOf(true);
 
Byte b1 = (byte) 123; // ... syntax sugar for:
Byte b2 = Byte.valueOf((byte) 123);

其它整数基本类型也有类似情况，比如 char、short、int、long。

不要在调用构造方法时将这些整型基本类型自动装箱或者调用 TheType.valueOf() 方法。

也不要在包装类上调用构造方法，除非你想得到一个不在堆上创建的实例。这样做的好处是为你为同事献上一个巨坑的愚人节笑话。
非堆存储

当然了，如果你还想体验下堆外函数库的话，尽管这可能参杂着不少战略决策，而并非最乐观的本地方案。一篇由Peter Lawrey和 Ben Cotton撰写的关于非堆存储的很有意思文章请点击： OpenJDK与HashMap——让老手安全地掌握（非堆存储！）新技巧。
6、避免递归

现在，类似Scala这样的函数式编程语言都鼓励使用递归。因为递归通常意味着能分解到单独个体优化的尾递归（tail-recursing）。如果你使用的编程语言能够支持那是再好不过。不过即使如此，也要注意对算法的细微调整将会使尾递归变为普通递归。

希望编译器能自动探测到这一点，否则本来我们将为只需使用几个本地变量就能搞定的事情而白白浪费大量的堆栈框架（stack frames）。
小结

这节中没什么好说的，除了在 N.O.P.E 分支尽量使用迭代来代替递归。
7、使用entrySet()

当我们想遍历一个用键值对形式保存的 Map 时，必须要为下面的代码找到一个很好的理由：

	
for (K key : map.keySet()) {
    V value : map.get(key);
}

更不用说下面的写法：

	
for (Entry<K, V> entry : map.entrySet()) {
    K key = entry.getKey();
    V value = entry.getValue();
}

在我们使用 N.O.P.E. 分支应该慎用map。因为很多看似时间复杂度为 O(1) 的访问操作其实是由一系列的操作组成的。而且访问本身也不是免费的。至少，如果不得不使用map的话，那么要用 entrySet() 方法去迭代！这样的话，我们要访问的就仅仅是Map.Entry的实例。
小结

在需要迭代键值对形式的Map时一定要用 entrySet() 方法。
9、使用EnumSet或EnumMap

在某些情况下，比如在使用配置map时，我们可能会预先知道保存在map中键值。如果这个键值非常小，我们就应该考虑使用 EnumSet 或 EnumMap，而并非使用我们常用的 HashSet 或 HashMap。下面的代码给出了很清楚的解释：

	
private transient Object[] vals;
 
public V put(K key, V value) {
    // ...
    int index = key.ordinal();
    vals[index] = maskNull(value);
    // ...
}

上段代码的关键实现在于，我们用数组代替了哈希表。尤其是向map中插入新值时，所要做的仅仅是获得一个由编译器为每个枚举类型生成的常量序列号。如果有一个全局的map配置（例如只有一个实例），在增加访问速度的压力下，EnumMap 会获得比 HashMap 更加杰出的表现。原因在于 EnumMap 使用的堆内存比 HashMap 要少 一位（bit），而且 HashMap 要在每个键值上都要调用 hashCode() 方法和 equals() 方法。
小结

Enum 和 EnumMap 是亲密的小伙伴。在我们用到类似枚举（enum-like）结构的键值时，就应该考虑将这些键值用声明为枚举类型，并将之作为 EnumMap 键。
9、优化自定义hasCode()方法和equals()方法

在不能使用EnumMap的情况下，至少也要优化 hashCode() 和 equals() 方法。一个好的 hashCode() 方法是很有必要的，因为它能防止对高开销 equals() 方法多余的调用。

在每个类的继承结构中，需要容易接受的简单对象。让我们看一下jOOQ的 org.jooq.Table 是如何实现的？

最简单、快速的 hashCode() 实现方法如下：

	
// AbstractTable一个通用Table的基础实现：
 
@Override
public int hashCode() {
 
    // [#1938] 与标准的QueryParts相比，这是一个更加高效的hashCode()实现
    return name.hashCode();
}

name即为表名。我们甚至不需要考虑schema或者其它表属性，因为表名在数据库中通常是唯一的。并且变量 name 是一个字符串，它本身早就已经缓存了一个 hashCode() 值。

这段代码中注释十分重要，因继承自 AbstractQueryPart 的 AbstractTable 是任意抽象语法树元素的基本实现。普通抽象语法树元素并没有任何属性，所以不能对优化 hashCode() 方法实现抱有任何幻想。覆盖后的 hashCode() 方法如下：

	
// AbstractQueryPart一个通用抽象语法树基础实现：
 
@Override
public int hashCode() {
    // 这是一个可工作的默认实现。
    // 具体实现的子类应当覆盖此方法以提高性能。
    return create().renderInlined(this).hashCode();
}

换句话说，要触发整个SQL渲染工作流程（rendering workflow）来计算一个普通抽象语法树元素的hash代码。

equals() 方法则更加有趣：

	
// AbstractTable通用表的基础实现：
 
@Override
public boolean equals(Object that) {
    if (this == that) {
        return true;
    }
 
    // [#2144] 在调用高开销的AbstractQueryPart.equals()方法前，
    // 可以及早知道对象是否不相等。
    if (that instanceof AbstractTable) {
        if (StringUtils.equals(name,
            (((AbstractTable<?>) that).name))) {
            return super.equals(that);
        }
 
        return false;
    }
 
    return false;
}

首先，不要过早使用 equals() 方法（不仅在N.O.P.E.中），如果：

    this == argument
    this“不兼容：参数

注意：如果我们过早使用 instanceof 来检验兼容类型的话，后面的条件其实包含了argument == null。我在以前的博客中已经对这一点进行了说明，请参考10个精妙的Java编码最佳实践。

在我们对以上几种情况的比较结束后，应该能得出部分结论。比如jOOQ的 Table.equals() 方法说明是，用来比较两张表是否相同。不论具体实现类型如何，它们必须要有相同的字段名。比如下面两个元素是不可能相同的：

    com.example.generated.Tables.MY_TABLE
    DSL.tableByName(“MY_OTHER_TABLE”)

如果我们能方便地判断传入参数是否等于实例本身（this），就可以在返回结果为 false 的情况下放弃操作。如果返回结果为 true，我们还可以进一步对父类（super）实现进行判断。在比较过的大多数对象都不等的情况下，我们可以尽早结束方法来节省CPU的执行时间。

    一些对象的相似度比其它对象更高。

在jOOQ中，大多数的表实例是由jOOQ的代码生成器生成的，这些实例的 equals() 方法都经过了深度优化。而数十种其它的表类型（衍生表 （derived tables）、表值函数（table-valued functions）、数组表（array tables）、连接表（joined tables）、数据透视表（pivot tables）、公用表表达式（common table expressions）等，则保持 equals() 方法的基本实现。
10、考虑使用set而并非单个元素

最后，还有一种情况可以适用于所有语言而并非仅仅同Java有关。除此以外，我们以前研究的 N.O.P.E. 分支也会对了解从 O(N3) 到 O(n log n)有所帮助。

不幸的是，很多程序员的用简单的、本地算法来考虑问题。他们习惯按部就班地解决问题。这是命令式（imperative）的“是/或”形式的函数式编程风格。这种编程风格在由纯粹命令式编程向面对象式编程向函数式编程转换时，很容易将“更大的场景（bigger picture）”模型化，但是这些风格都缺少了只有在SQL和R语言中存在的：

声明式编程。

在SQL中，我们可以在不考虑算法影响下声明要求数据库得到的效果。数据库可以根据数据类型，比如约束（constraints）、键（key）、索引（indexes）等不同来采取最佳的算法。

在理论上，我们最初在SQL和关系演算（relational calculus）后就有了基本的想法。在实践中，SQL的供应商们在过去的几十年中已经实现了基于开销的高效优化器CBOs (Cost-Based Optimisers) 。然后到了2010版，我们才终于将SQL的所有潜力全部挖掘出来。

但是我们还不需要用set方式来实现SQL。所有的语言和库都支持Sets、collections、bags、lists。使用set的主要好处是能使我们的代码变的简洁明了。比如下面的写法：
1
	
SomeSet INTERSECT SomeOtherSet

而不是

	
// Java 8以前的写法
Set result = new HashSet();
for (Object candidate : someSet)
    if (someOtherSet.contains(candidate))
        result.add(candidate);
 
// 即使采用Java 8也没有很大帮助
someSet.stream()
       .filter(someOtherSet::contains)
       .collect(Collectors.toSet());

有些人可能会对函数式编程和Java 8能帮助我们写出更加简单、简洁的算法持有不同的意见。但这种看法不一定是对的。我们可以把命令式的Java 7循环转换成Java 8的Stream collection，但是我们还是采用了相同的算法。但SQL风格的表达式则是不同的：
1
	
SomeSet INTERSECT SomeOtherSet

上面的代码在不同的引擎上可以有1000种不同的实现。我们今天所研究的是，在调用 INTERSECT 操作之前，更加智能地将两个set自动的转化为 EnumSet 。甚至我们可以在不需要调用底层的 Stream.parallel() 方法的情况下进行并行 INTERSECT 操作。
总结

在这篇文章中，我们讨论了关于N.O.P.E.分支的优化。比如深入高复杂性的算法。作为jOOQ的开发者，我们很乐于对SQL的生成进行优化。

    每条查询都用唯一的StringBuilder来生成。
    模板引擎实际上处理的是字符而并非正则表达式。
    选择尽可能的使用数组，尤其是在对监听器进行迭代时。
    对JDBC的方法敬而远之。
    等等。

jOOQ处在“食物链的底端”，因为它是在离开JVM进入到DBMS时，被我们电脑程序所调用的最后一个API。位于食物链的底端意味着任何一条线路在jOOQ中被执行时都需要 N x O x P 的时间，所以我要尽早进行优化。

我们的业务逻辑可能没有N.O.P.E.分支那么复杂。但是基础框架有可能十分复杂（本地SQL框架、本地库等）。所以需要按照我们今天提到的原则，用Java Mission Control 或其它工具进行复查，确认是否有需要优化的地方。





##### 十张图理解java

一图胜千言，下面图解均来自Program Creek 网站的Java教程，目前它们拥有最多的票选。如果图解没有阐明问题，那么你可以借助它的标题来一窥究竟。
1、字符串不变性

下面这张图展示了这段代码做了什么
	
String s = "abcd";
s = s.concat("ef");
2、equals()方法、hashCode()方法的区别

![](http://incdn1.b0.upaiyun.com/2014/06/866816a69119a9ca24232d753ef537b8.jpeg)


HashCode被设计用来提高性能。equals()方法与hashCode()方法的区别在于：

    如果两个对象相等(equal)，那么他们一定有相同的哈希值。
    如果两个对象的哈希值相同，但他们未必相等(equal)。
    
![](http://incdn1.b0.upaiyun.com/2014/06/0954391ddfaad41dd3ead5037bfdc1eb.jpeg)

3、Java异常类的层次结构

图中红色部分为受检查异常。它们必须被捕获，或者在函数中声明为抛出该异常。

![](http://incdn1.b0.upaiyun.com/2014/06/fbddd02451798ed512e142809d02bc0c-498x1024.jpeg)

4、集合类的层次结构

注意Collections和Collection的区别。（Collections包含有各种有关集合操作的静态多态方法）
![](http://incdn1.b0.upaiyun.com/2014/06/647d134fddb1872cf1f5a8facbb41557.jpeg)


5、Java同步

Java同步机制可通过类比建筑物来阐明。

![](http://incdn1.b0.upaiyun.com/2014/06/b8d387a03337e0a37f0df743507c5f26.jpg)

6、别名

别名意味着有多个变量指向同一可被更新的内存块，这些别名分别是不同的对象类型。

![](http://incdn1.b0.upaiyun.com/2014/06/5ff0d9d66dd85d01cee8a423544d3b21.jpeg)

7、堆和栈

图解表明了方法和对象在运行时内存中的位置。

![](http://incdn1.b0.upaiyun.com/2014/06/cc55bbfe947afccc45c18dee9cbb8112.png)

8、Java虚拟机运行时数据区域

图解展示了整个虚拟机运行时数据区域的情况。

![](http://incdn1.b0.upaiyun.com/2014/06/80c7346c554563d5738537a353f2bc0b.jpg)







##### Java 8简明教程

以下是《Java 8简明教程》的正文。
“Java并没有没落，人们很快就会发现这一点”

欢迎阅读我编写的Java 8介绍。本教程将带领你一步一步地认识这门语言的新特性。通过简单明了的代码示例，你将会学习到如何使用默认接口方法，Lambda表达式，方法引用和重复注解。看完这篇教程后，你还将对最新推出的API有一定的了解，例如：流控制，函数式接口，map扩展和新的时间日期API等等。
###### 允许在接口中有默认方法实现

Java 8 允许我们使用default关键字，为接口声明添加非抽象的方法实现。这个特性又被称为**扩展方法**。下面是我们的第一个例子：

```java
interface Formula {
    double calculate(int a);
 
    default double sqrt(int a) {
        return Math.sqrt(a);
    }
}
```

在接口Formula中，除了抽象方法caculate以外，还定义了一个默认方法sqrt。Formula的实现类只需要实现抽象方法caculate就可以了。默认方法sqrt可以直接使用。

```java
Formula formula = new Formula() {
    @Override
    public double calculate(int a) {
        return sqrt(a * 100);
    }
};
 
formula.calculate(100);     // 100.0
formula.sqrt(16);           // 4.0


```

formula对象以匿名对象的形式实现了Formula接口。代码很啰嗦：用了6行代码才实现了一个简单的计算功能：a*100开平方根。我们在下一节会看到，Java 8 还有一种更加优美的方法，能够实现包含单个函数的对象。

###### Lambda 表达式

让我们从最简单的例子开始，来学习如何对一个string列表进行排序。我们首先使用Java 8之前的方法来实现：

```java
List<String> names = Arrays.asList("peter", "anna", "mike", "xenia");
 
Collections.sort(names, new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
        return b.compareTo(a);
    }
```

静态工具方法Collections.sort接受一个list，和一个Comparator接口作为输入参数，Comparator的实现类可以对输入的list中的元素进行比较。通常情况下，你可以直接用创建匿名Comparator对象，并把它作为参数传递给sort方法。

除了创建匿名对象以外，Java 8 还提供了一种更简洁的方式，Lambda表达式。

```java
Collections.sort(names, (String a, String b) -> {
    return b.compareTo(a);
});
```

你可以看到，这段代码就比之前的更加简短和易读。但是，它还可以更加简短：

```java
Collections.sort(names, (String a, String b) -> b.compareTo(a));
```

只要一行代码，包含了方法体。你甚至可以连大括号对{}和return关键字都省略不要。不过这还不是最短的写法：

```java
Collections.sort(names, (a, b) -> b.compareTo(a));
```
Java编译器能够自动识别参数的类型，所以你就可以省略掉类型不写。让我们再深入地研究一下lambda表达式的威力吧。

###### 函数式接口

Lambda表达式如何匹配Java的类型系统？每一个lambda都能够通过一个特定的接口，与一个给定的类型进行匹配。一个所谓的函数式接口必须要有且仅有一个抽象方法声明。每个与之对应的lambda表达式必须要与抽象方法的声明相匹配。由于默认方法不是抽象的，因此你可以在你的函数式接口里任意添加默认方法。

任意只包含一个抽象方法的接口，我们都可以用来做成lambda表达式。为了让你定义的接口满足要求，你应当在接口前加上@FunctionalInterface 标注。编译器会注意到这个标注，如果你的接口中定义了第二个抽象方法的话，编译器会抛出异常。

```java
@FunctionalInterface
interface Converter<F, T> {
    T convert(F from);
}
 
Converter<String, Integer> converter = (from) -> Integer.valueOf(from);
Integer converted = converter.convert("123");
System.out.println(converted);    // 123
```
注意，如果你不写@FunctionalInterface 标注，程序也是正确的。

###### 方法和构造函数引用

上面的代码实例可以通过静态方法引用，使之更加简洁：

```java
Converter<String, Integer> converter = Integer::valueOf;
Integer converted = converter.convert("123");
System.out.println(converted);   // 123
```

Java 8 允许你通过::关键字获取方法或者构造函数的的引用。上面的例子就演示了如何引用一个静态方法。而且，我们还可以对一个对象的方法进行引用：

```java
class Something {
    String startsWith(String s) {
        return String.valueOf(s.charAt(0));
    }
}
 
Something something = new Something();
Converter<String, String> converter = something::startsWith;
String converted = converter.convert("Java");
System.out.println(converted);    // "J"
```

让我们看看如何使用::关键字引用构造函数。首先我们定义一个示例bean，包含不同的构造方法：
1
2
3
4
5
6
7
8
9
10
11
	
class Person {
    String firstName;
    String lastName;
 
    Person() {}
 
    Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}

接下来，我们定义一个person工厂接口，用来创建新的person对象：
1
2
3
	
interface PersonFactory<P extends Person> {
    P create(String firstName, String lastName);
}

然后我们通过构造函数引用来把所有东西拼到一起，而不是像以前一样，通过手动实现一个工厂来这么做。
1
2
	
PersonFactory<Person> personFactory = Person::new;
Person person = personFactory.create("Peter", "Parker");

我们通过Person::new来创建一个Person类构造函数的引用。Java编译器会自动地选择合适的构造函数来匹配PersonFactory.create函数的签名，并选择正确的构造函数形式。
Lambda的范围

对于lambdab表达式外部的变量，其访问权限的粒度与匿名对象的方式非常类似。你能够访问局部对应的外部区域的局部final变量，以及成员变量和静态变量。
访问局部变量

我们可以访问lambda表达式外部的final局部变量：
1
2
3
4
5
	
final int num = 1;
Converter<Integer, String> stringConverter =
        (from) -> String.valueOf(from + num);
 
stringConverter.convert(2);     // 3

但是与匿名对象不同的是，变量num并不需要一定是final。下面的代码依然是合法的：
1
2
3
4
5
	
int num = 1;
Converter<Integer, String> stringConverter =
        (from) -> String.valueOf(from + num);
 
stringConverter.convert(2);     // 3

然而，num在编译的时候被隐式地当做final变量来处理。下面的代码就不合法：
1
2
3
4
	
int num = 1;
Converter<Integer, String> stringConverter =
        (from) -> String.valueOf(from + num);
num = 3;

在lambda表达式内部企图改变num的值也是不允许的。
访问成员变量和静态变量

与局部变量不同，我们在lambda表达式的内部能获取到对成员变量或静态变量的读写权。这种访问行为在匿名对象里是非常典型的。
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
	
class Lambda4 {
    static int outerStaticNum;
    int outerNum;
 
    void testScopes() {
        Converter<Integer, String> stringConverter1 = (from) -> {
            outerNum = 23;
            return String.valueOf(from);
        };
 
        Converter<Integer, String> stringConverter2 = (from) -> {
            outerStaticNum = 72;
            return String.valueOf(from);
        };
    }
}
访问默认接口方法

还记得第一节里面formula的那个例子么？ 接口Formula定义了一个默认的方法sqrt，该方法能够访问formula所有的对象实例，包括匿名对象。这个对lambda表达式来讲则无效。

默认方法无法在lambda表达式内部被访问。因此下面的代码是无法通过编译的：
1
	
Formula formula = (a) -> sqrt( a * 100);
内置函数式接口

JDK 1.8 API中包含了很多内置的函数式接口。有些是在以前版本的Java中大家耳熟能详的，例如Comparator接口，或者Runnable接口。对这些现成的接口进行实现，可以通过@FunctionalInterface 标注来启用Lambda功能支持。

此外，Java 8 API 还提供了很多新的函数式接口，来降低程序员的工作负担。有些新的接口已经在Google Guava库中很有名了。如果你对这些库很熟的话，你甚至闭上眼睛都能够想到，这些接口在类库的实现过程中起了多么大的作用。
Predicates

Predicate是一个布尔类型的函数，该函数只有一个输入参数。Predicate接口包含了多种默认方法，用于处理复杂的逻辑动词（and, or，negate）
1
2
3
4
5
6
7
8
9
10
	
Predicate<String> predicate = (s) -> s.length() > 0;
 
predicate.test("foo");              // true
predicate.negate().test("foo");     // false
 
Predicate<Boolean> nonNull = Objects::nonNull;
Predicate<Boolean> isNull = Objects::isNull;
 
Predicate<String> isEmpty = String::isEmpty;
Predicate<String> isNotEmpty = isEmpty.negate();
Functions

Function接口接收一个参数，并返回单一的结果。默认方法可以将多个函数串在一起（compse, andThen）
1
2
3
4
	
Function<String, Integer> toInteger = Integer::valueOf;
Function<String, String> backToString = toInteger.andThen(String::valueOf);
 
backToString.apply("123");     // "123"
Suppliers

Supplier接口产生一个给定类型的结果。与Function不同的是，Supplier没有输入参数。
1
2
	
Supplier<Person> personSupplier = Person::new;
personSupplier.get();   // new Person
Consumers

Consumer代表了在一个输入参数上需要进行的操作。
1
2
	
Consumer<Person> greeter = (p) -> System.out.println("Hello, " + p.firstName);
greeter.accept(new Person("Luke", "Skywalker"));
Comparators

Comparator接口在早期的Java版本中非常著名。Java 8 为这个接口添加了不同的默认方法。
1
2
3
4
5
6
7
	
Comparator<Person> comparator = (p1, p2) -> p1.firstName.compareTo(p2.firstName);
 
Person p1 = new Person("John", "Doe");
Person p2 = new Person("Alice", "Wonderland");
 
comparator.compare(p1, p2);             // > 0
comparator.reversed().compare(p1, p2);  // < 0
Optionals

Optional不是一个函数式接口，而是一个精巧的工具接口，用来防止NullPointerEception产生。这个概念在下一节会显得很重要，所以我们在这里快速地浏览一下Optional的工作原理。

Optional是一个简单的值容器，这个值可以是null，也可以是non-null。考虑到一个方法可能会返回一个non-null的值，也可能返回一个空值。为了不直接返回null，我们在Java 8中就返回一个Optional.
1
2
3
4
5
6
7
	
Optional<String> optional = Optional.of("bam");
 
optional.isPresent();           // true
optional.get();                 // "bam"
optional.orElse("fallback");    // "bam"
 
optional.ifPresent((s) -> System.out.println(s.charAt(0)));     // "b"
Streams

java.util.Stream表示了某一种元素的序列，在这些元素上可以进行各种操作。Stream操作可以是中间操作，也可以是完结操作。完结操作会返回一个某种类型的值，而中间操作会返回流对象本身，并且你可以通过多次调用同一个流操作方法来将操作结果串起来（就像StringBuffer的append方法一样————译者注）。Stream是在一个源的基础上创建出来的，例如java.util.Collection中的list或者set（map不能作为Stream的源）。Stream操作往往可以通过顺序或者并行两种方式来执行。

我们先了解一下序列流。首先，我们通过string类型的list的形式创建示例数据：
1
2
3
4
5
6
7
8
9
	
List<String> stringCollection = new ArrayList<>();
stringCollection.add("ddd2");
stringCollection.add("aaa2");
stringCollection.add("bbb1");
stringCollection.add("aaa1");
stringCollection.add("bbb3");
stringCollection.add("ccc");
stringCollection.add("bbb2");
stringCollection.add("ddd1");

Java 8中的Collections类的功能已经有所增强，你可以之直接通过调用Collections.stream()或者Collection.parallelStream()方法来创建一个流对象。下面的章节会解释这个最常用的操作。
Filter

Filter接受一个predicate接口类型的变量，并将所有流对象中的元素进行过滤。该操作是一个中间操作，因此它允许我们在返回结果的基础上再进行其他的流操作（forEach）。ForEach接受一个function接口类型的变量，用来执行对每一个元素的操作。ForEach是一个中止操作。它不返回流，所以我们不能再调用其他的流操作。
1
2
3
4
5
6
	
stringCollection
    .stream()
    .filter((s) -> s.startsWith("a"))
    .forEach(System.out::println);
 
// "aaa2", "aaa1"
Sorted

Sorted是一个中间操作，能够返回一个排过序的流对象的视图。流对象中的元素会默认按照自然顺序进行排序，除非你自己指定一个Comparator接口来改变排序规则。
1
2
3
4
5
6
7
	
stringCollection
    .stream()
    .sorted()
    .filter((s) -> s.startsWith("a"))
    .forEach(System.out::println);
 
// "aaa1", "aaa2"

一定要记住，sorted只是创建一个流对象排序的视图，而不会改变原来集合中元素的顺序。原来string集合中的元素顺序是没有改变的。
1
2
	
System.out.println(stringCollection);
// ddd2, aaa2, bbb1, aaa1, bbb3, ccc, bbb2, ddd1
Map

map是一个对于流对象的中间操作，通过给定的方法，它能够把流对象中的每一个元素对应到另外一个对象上。下面的例子就演示了如何把每个string都转换成大写的string. 不但如此，你还可以把每一种对象映射成为其他类型。对于带泛型结果的流对象，具体的类型还要由传递给map的泛型方法来决定。
1
2
3
4
5
6
7
	
stringCollection
    .stream()
    .map(String::toUpperCase)
    .sorted((a, b) -> b.compareTo(a))
    .forEach(System.out::println);
 
// "DDD2", "DDD1", "CCC", "BBB3", "BBB2", "AAA2", "AAA1"
Match

匹配操作有多种不同的类型，都是用来判断某一种规则是否与流对象相互吻合的。所有的匹配操作都是终结操作，只返回一个boolean类型的结果。
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
	
boolean anyStartsWithA = 
    stringCollection
        .stream()
        .anyMatch((s) -> s.startsWith("a"));
 
System.out.println(anyStartsWithA);      // true
 
boolean allStartsWithA = 
    stringCollection
        .stream()
        .allMatch((s) -> s.startsWith("a"));
 
System.out.println(allStartsWithA);      // false
 
boolean noneStartsWithZ = 
    stringCollection
        .stream()
        .noneMatch((s) -> s.startsWith("z"));
 
System.out.println(noneStartsWithZ);      // true
Count

Count是一个终结操作，它的作用是返回一个数值，用来标识当前流对象中包含的元素数量。
1
2
3
4
5
6
7
	
long startsWithB = 
    stringCollection
        .stream()
        .filter((s) -> s.startsWith("b"))
        .count();
 
System.out.println(startsWithB);    // 3
Reduce

该操作是一个终结操作，它能够通过某一个方法，对元素进行削减操作。该操作的结果会放在一个Optional变量里返回。
1
2
3
4
5
6
7
8
	
Optional<String> reduced =
    stringCollection
        .stream()
        .sorted()
        .reduce((s1, s2) -> s1 + "#" + s2);
 
reduced.ifPresent(System.out::println);
// "aaa1#aaa2#bbb1#bbb2#bbb3#ccc#ddd1#ddd2"
Parallel Streams

像上面所说的，流操作可以是顺序的，也可以是并行的。顺序操作通过单线程执行，而并行操作则通过多线程执行。

下面的例子就演示了如何使用并行流进行操作来提高运行效率，代码非常简单。

首先我们创建一个大的list，里面的元素都是唯一的：
1
2
3
4
5
6
	
int max = 1000000;
List<String> values = new ArrayList<>(max);
for (int i = 0; i < max; i++) {
    UUID uuid = UUID.randomUUID();
    values.add(uuid.toString());
}

现在，我们测量一下对这个集合进行排序所使用的时间。
顺序排序
1
2
3
4
5
6
7
8
9
10
11
	
long t0 = System.nanoTime();
 
long count = values.stream().sorted().count();
System.out.println(count);
 
long t1 = System.nanoTime();
 
long millis = TimeUnit.NANOSECONDS.toMillis(t1 - t0);
System.out.println(String.format("sequential sort took: %d ms", millis));
 
// sequential sort took: 899 ms
并行排序
1
2
3
4
5
6
7
8
9
10
11
	
long t0 = System.nanoTime();
 
long count = values.parallelStream().sorted().count();
System.out.println(count);
 
long t1 = System.nanoTime();
 
long millis = TimeUnit.NANOSECONDS.toMillis(t1 - t0);
System.out.println(String.format("parallel sort took: %d ms", millis));
 
// parallel sort took: 472 ms

如你所见，所有的代码段几乎都相同，唯一的不同就是把stream()改成了parallelStream(), 结果并行排序快了50%。
Map

正如前面已经提到的那样，map是不支持流操作的。而更新后的map现在则支持多种实用的新方法，来完成常规的任务。
1
2
3
4
5
6
7
	
Map<Integer, String> map = new HashMap<>();
 
for (int i = 0; i < 10; i++) {
    map.putIfAbsent(i, "val" + i);
}
 
map.forEach((id, val) -> System.out.println(val));

上面的代码风格是完全自解释的：putIfAbsent避免我们将null写入；forEach接受一个消费者对象，从而将操作实施到每一个map中的值上。

下面的这个例子展示了如何使用函数来计算map的编码
1
2
3
4
5
6
7
8
9
10
11
	
map.computeIfPresent(3, (num, val) -> val + num);
map.get(3);             // val33
 
map.computeIfPresent(9, (num, val) -> null);
map.containsKey(9);     // false
 
map.computeIfAbsent(23, num -> "val" + num);
map.containsKey(23);    // true
 
map.computeIfAbsent(3, num -> "bam");
map.get(3);             // val33

接下来，我们将学习，当给定一个key值时，如何把一个实例从对应的key中移除：

 
1
2
3
4
5
	
map.remove(3, "val3");
map.get(3);             // val33
 
map.remove(3, "val33");
map.get(3);             // null

另一个有用的方法：
1
	
map.getOrDefault(42, "not found");  // not found

将map中的实例合并也是非常容易的：
1
2
3
4
5
	
map.merge(9, "val9", (value, newValue) -> value.concat(newValue));
map.get(9);             // val9
 
map.merge(9, "concat", (value, newValue) -> value.concat(newValue));
map.get(9);             // val9concat

合并操作先看map中是否没有特定的key/value存在，如果是，则把key/value存入map，否则merging函数就会被调用，对现有的数值进行修改。
时间日期API

Java 8 包含了全新的时间日期API，这些功能都放在了java.time包下。新的时间日期API是基于Joda-Time库开发的，但是也不尽相同。下面的例子就涵盖了大多数新的API的重要部分。
Clock

Clock提供了对当前时间和日期的访问功能。Clock是对当前时区敏感的，并可用于替代System.currentTimeMillis()方法来获取当前的毫秒时间。当前时间线上的时刻可以用Instance类来表示。Instance也能够用于创建原先的java.util.Date对象。
1
2
3
4
5
	
Clock clock = Clock.systemDefaultZone();
long millis = clock.millis();
 
Instant instant = clock.instant();
Date legacyDate = Date.from(instant);   // legacy java.util.Date
Timezones

时区类可以用一个ZoneId来表示。时区类的对象可以通过静态工厂方法方便地获取。时区类还定义了一个偏移量，用来在当前时刻或某时间与目标时区时间之间进行转换。
1
2
3
4
5
6
7
8
9
10
	
System.out.println(ZoneId.getAvailableZoneIds());
// prints all available timezone ids
 
ZoneId zone1 = ZoneId.of("Europe/Berlin");
ZoneId zone2 = ZoneId.of("Brazil/East");
System.out.println(zone1.getRules());
System.out.println(zone2.getRules());
 
// ZoneRules[currentStandardOffset=+01:00]
// ZoneRules[currentStandardOffset=-03:00]
LocalTime

本地时间类表示一个没有指定时区的时间，例如，10 p.m.或者17：30:15，下面的例子会用上面的例子定义的时区创建两个本地时间对象。然后我们会比较两个时间，并计算它们之间的小时和分钟的不同。
1
2
3
4
5
6
7
8
9
10
	
LocalTime now1 = LocalTime.now(zone1);
LocalTime now2 = LocalTime.now(zone2);
 
System.out.println(now1.isBefore(now2));  // false
 
long hoursBetween = ChronoUnit.HOURS.between(now1, now2);
long minutesBetween = ChronoUnit.MINUTES.between(now1, now2);
 
System.out.println(hoursBetween);       // -3
System.out.println(minutesBetween);     // -239

LocalTime是由多个工厂方法组成，其目的是为了简化对时间对象实例的创建和操作，包括对时间字符串进行解析的操作。
1
2
3
4
5
6
7
8
9
10
	
LocalTime late = LocalTime.of(23, 59, 59);
System.out.println(late);       // 23:59:59
 
DateTimeFormatter germanFormatter =
    DateTimeFormatter
        .ofLocalizedTime(FormatStyle.SHORT)
        .withLocale(Locale.GERMAN);
 
LocalTime leetTime = LocalTime.parse("13:37", germanFormatter);
System.out.println(leetTime);   // 13:37
LocalDate

本地时间表示了一个独一无二的时间，例如：2014-03-11。这个时间是不可变的，与LocalTime是同源的。下面的例子演示了如何通过加减日，月，年等指标来计算新的日期。记住，每一次操作都会返回一个新的时间对象。
1
2
3
4
5
6
7
	
LocalDate today = LocalDate.now();
LocalDate tomorrow = today.plus(1, ChronoUnit.DAYS);
LocalDate yesterday = tomorrow.minusDays(2);
 
LocalDate independenceDay = LocalDate.of(2014, Month.JULY, 4);
DayOfWeek dayOfWeek = independenceDay.getDayOfWeek();
System.out.println(dayOfWeek);    // FRIDAY<span style="font-family: Georgia, 'Times New Roman', 'Bitstream Charter', Times, serif; font-size: 13px; line-height: 19px;">Parsing a LocalDate from a string is just as simple as parsing a LocalTime:</span>

解析字符串并形成LocalDate对象，这个操作和解析LocalTime一样简单。
1
2
3
4
5
6
7
	
DateTimeFormatter germanFormatter =
    DateTimeFormatter
        .ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(Locale.GERMAN);
 
LocalDate xmas = LocalDate.parse("24.12.2014", germanFormatter);
System.out.println(xmas);   // 2014-12-24
LocalDateTime

LocalDateTime表示的是日期-时间。它将刚才介绍的日期对象和时间对象结合起来，形成了一个对象实例。LocalDateTime是不可变的，与LocalTime和LocalDate的工作原理相同。我们可以通过调用方法来获取日期时间对象中特定的数据域。
1
2
3
4
5
6
7
8
9
10
	
LocalDateTime sylvester = LocalDateTime.of(2014, Month.DECEMBER, 31, 23, 59, 59);
 
DayOfWeek dayOfWeek = sylvester.getDayOfWeek();
System.out.println(dayOfWeek);      // WEDNESDAY
 
Month month = sylvester.getMonth();
System.out.println(month);          // DECEMBER
 
long minuteOfDay = sylvester.getLong(ChronoField.MINUTE_OF_DAY);
System.out.println(minuteOfDay);    // 1439

如果再加上的时区信息，LocalDateTime能够被转换成Instance实例。Instance能够被转换成以前的java.util.Date对象。
1
2
3
4
5
6
	
Instant instant = sylvester
        .atZone(ZoneId.systemDefault())
        .toInstant();
 
Date legacyDate = Date.from(instant);
System.out.println(legacyDate);     // Wed Dec 31 23:59:59 CET 2014

格式化日期-时间对象就和格式化日期对象或者时间对象一样。除了使用预定义的格式以外，我们还可以创建自定义的格式化对象，然后匹配我们自定义的格式。
1
2
3
4
5
6
7
	
DateTimeFormatter formatter =
    DateTimeFormatter
        .ofPattern("MMM dd, yyyy - HH:mm");
 
LocalDateTime parsed = LocalDateTime.parse("Nov 03, 2014 - 07:13", formatter);
String string = formatter.format(parsed);
System.out.println(string);     // Nov 03, 2014 - 07:13

不同于java.text.NumberFormat，新的DateTimeFormatter类是不可变的，也是线程安全的。

更多的细节，请看这里

 
Annotations

Java 8中的注解是可重复的。让我们直接深入看看例子，弄明白它是什么意思。

首先，我们定义一个包装注解，它包括了一个实际注解的数组
1
2
3
4
5
6
7
8
	
@interface Hints {
    Hint[] value();
}
 
@Repeatable(Hints.class)
@interface Hint {
    String value();
}

只要在前面加上注解名：@Repeatable，Java 8 允许我们对同一类型使用多重注解，

变体1：使用注解容器（老方法）
1
2
	
@Hints({@Hint("hint1"), @Hint("hint2")})
class Person {}

变体2：使用可重复注解（新方法）
1
2
3
	
@Hint("hint1")
@Hint("hint2")
class Person {}

使用变体2，Java编译器能够在内部自动对@Hint进行设置。这对于通过反射来读取注解信息来说，是非常重要的。
1
2
3
4
5
6
7
8
	
Hint hint = Person.class.getAnnotation(Hint.class);
System.out.println(hint);                   // null
 
Hints hints1 = Person.class.getAnnotation(Hints.class);
System.out.println(hints1.value().length);  // 2
 
Hint[] hints2 = Person.class.getAnnotationsByType(Hint.class);
System.out.println(hints2.length);          // 2

尽管我们绝对不会在Person类上声明@Hints注解，但是它的信息仍然可以通过getAnnotation(Hints.class)来读取。并且，getAnnotationsByType方法会更方便，因为它赋予了所有@Hints注解标注的方法直接的访问权限。
1
2
	
@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@interface MyAnnotation {}
先到这里

我的Java 8编程指南就到此告一段落。当然，还有很多内容需要进一步研究和说明。这就需要靠读者您来对JDK 8进行探究了，例如：Arrays.parallelSort, StampedLock和CompletableFuture等等 ———— 我这里只是举几个例子而已。

我希望这个博文能够对您有所帮助，也希望您阅读愉快。完整的教程源代码放在了GitHub上。您可以尽情地fork，并请通过Twitter告诉我您的反馈。




##### java线程面试 50 

不管你是新程序员还是老手，你一定在面试中遇到过有关线程的问题。Java语言一个重要的特点就是内置了对并发的支持，让Java大受企业和程序员的欢迎。大多数待遇丰厚的Java开发职位都要求开发者精通多线程技术并且有丰富的Java程序开发、调试、优化经验，所以线程相关的问题在面试中经常会被提到。

在典型的Java面试中， 面试官会从线程的基本概念问起, 如：为什么你需要使用线程， 如何创建线程，用什么方式创建线程比较好（比如：继承thread类还是调用Runnable接口），然后逐渐问到并发问题像在Java并发编程的过程中遇到了什么挑战，Java内存模型，JDK1.5引入了哪些更高阶的并发工具，并发编程常用的设计模式，经典多线程问题如生产者消费者，哲学家就餐，读写器或者简单的有界缓冲区问题。仅仅知道线程的基本概念是远远不够的， 你必须知道如何处理死锁，竞态条件，内存冲突和线程安全等并发问题。掌握了这些技巧，你就可以轻松应对多线程和并发面试了。

许多Java程序员在面试前才会去看面试题，这很正常。因为收集面试题和练习很花时间，所以我从许多面试者那里收集了Java多线程和并发相关的50个热门问题。我只收集了比较新的面试题且没有提供全部答案。想必聪明的你对这些问题早就心中有数了， 如果遇到不懂的问题，你可以用Google找到答案。若你实在找不到答案，可以在文章的评论中向我求助。你也可以在这找到一些答案Java线程问答Top 12。
50道Java线程面试题

下面是Java线程相关的热门面试题，你可以用它来好好准备面试。
1) 什么是线程？

线程是操作系统能够进行运算调度的最小单位，它被包含在进程之中，是进程中的实际运作单位。程序员可以通过它进行多处理器编程，你可以使用多线程对运算密集型任务提速。比如，如果一个线程完成一个任务要100毫秒，那么用十个线程完成改任务只需10毫秒。Java在语言层面对多线程提供了卓越的支持，它也是一个很好的卖点。欲了解更多详细信息请点击这里。
2) 线程和进程有什么区别？

线程是进程的子集，一个进程可以有很多线程，每条线程并行执行不同的任务。不同的进程使用不同的内存空间，而所有的线程共享一片相同的内存空间。别把它和栈内存搞混，每个线程都拥有单独的栈内存用来存储本地数据。更多详细信息请点击这里。
3) 如何在Java中实现线程？

在语言层面有两种方式。java.lang.Thread 类的实例就是一个线程但是它需要调用java.lang.Runnable接口来执行，由于线程类本身就是调用的Runnable接口所以你可以继承java.lang.Thread 类或者直接调用Runnable接口来重写run()方法实现线程。更多详细信息请点击这里.
4) 用Runnable还是Thread？

这个问题是上题的后续，大家都知道我们可以通过继承Thread类或者调用Runnable接口来实现线程，问题是，那个方法更好呢？什么情况下使用它？这个问题很容易回答，如果你知道Java不支持类的多重继承，但允许你调用多个接口。所以如果你要继承其他类，当然是调用Runnable接口好了。更多详细信息请点击这里。
6) Thread 类中的start() 和 run() 方法有什么区别？

这个问题经常被问到，但还是能从此区分出面试者对Java线程模型的理解程度。start()方法被用来启动新创建的线程，而且start()内部调用了run()方法，这和直接调用run()方法的效果不一样。当你调用run()方法的时候，只会是在原来的线程中调用，没有新的线程启动，start()方法才会启动新线程。更多讨论请点击这里
7) Java中Runnable和Callable有什么不同？

Runnable和Callable都代表那些要在不同的线程中执行的任务。Runnable从JDK1.0开始就有了，Callable是在JDK1.5增加的。它们的主要区别是Callable的 call() 方法可以返回值和抛出异常，而Runnable的run()方法没有这些功能。Callable可以返回装载有计算结果的Future对象。我的博客有更详细的说明。
8) Java中CyclicBarrier 和 CountDownLatch有什么不同？

CyclicBarrier 和 CountDownLatch 都可以用来让一组线程等待其它线程。与 CyclicBarrier 不同的是，CountdownLatch 不能重新使用。点此查看更多信息和示例代码。
9) Java内存模型是什么？

Java内存模型规定和指引Java程序在不同的内存架构、CPU和操作系统间有确定性地行为。它在多线程的情况下尤其重要。Java内存模型对一个线程所做的变动能被其它线程可见提供了保证，它们之间是先行发生关系。这个关系定义了一些规则让程序员在并发编程时思路更清晰。比如，先行发生关系确保了：

    线程内的代码能够按先后顺序执行，这被称为程序次序规则。
    对于同一个锁，一个解锁操作一定要发生在时间上后发生的另一个锁定操作之前，也叫做管程锁定规则。
    前一个对volatile的写操作在后一个volatile的读操作之前，也叫volatile变量规则。
    一个线程内的任何操作必需在这个线程的start()调用之后，也叫作线程启动规则。
    一个线程的所有操作都会在线程终止之前，线程终止规则。
    一个对象的终结操作必需在这个对象构造完成之后，也叫对象终结规则。
    可传递性

我强烈建议大家阅读《Java并发编程实践》第十六章来加深对Java内存模型的理解。
10) Java中的volatile 变量是什么？

volatile是一个特殊的修饰符，只有成员变量才能使用它。在Java并发程序缺少同步类的情况下，多线程对成员变量的操作对其它线程是透明的。volatile变量可以保证下一个读取操作会在前一个写操作之后发生，就是上一题的volatile变量规则。点击这里查看更多volatile的相关内容。
11) 什么是线程安全？Vector是一个线程安全类吗？ （详见这里)

如果你的代码所在的进程中有多个线程在同时运行，而这些线程可能会同时运行这段代码。如果每次运行结果和单线程运行的结果是一样的，而且其他的变量的值也和预期的是一样的，就是线程安全的。一个线程安全的计数器类的同一个实例对象在被多个线程使用的情况下也不会出现计算失误。很显然你可以将集合类分成两组，线程安全和非线程安全的。Vector 是用同步方法来实现线程安全的, 而和它相似的ArrayList不是线程安全的。
12) Java中什么是竞态条件？ 举个例子说明。

竞态条件会导致程序在并发情况下出现一些bugs。多线程对一些资源的竞争的时候就会产生竞态条件，如果首先要执行的程序竞争失败排到后面执行了，那么整个程序就会出现一些不确定的bugs。这种bugs很难发现而且会重复出现，因为线程间的随机竞争。一个例子就是无序处理，详见答案。
13) Java中如何停止一个线程？

Java提供了很丰富的API但没有为停止线程提供API。JDK 1.0本来有一些像stop(), suspend() 和 resume()的控制方法但是由于潜在的死锁威胁因此在后续的JDK版本中他们被弃用了，之后Java API的设计者就没有提供一个兼容且线程安全的方法来停止一个线程。当run() 或者 call() 方法执行完的时候线程会自动结束,如果要手动结束一个线程，你可以用volatile 布尔变量来退出run()方法的循环或者是取消任务来中断线程。点击这里查看示例代码。
14) 一个线程运行时发生异常会怎样？

这是我在一次面试中遇到的一个很刁钻的Java面试题, 简单的说，如果异常没有被捕获该线程将会停止执行。Thread.UncaughtExceptionHandler是用于处理未捕获异常造成线程突然中断情况的一个内嵌接口。当一个未捕获异常将造成线程中断的时候JVM会使用Thread.getUncaughtExceptionHandler()来查询线程的UncaughtExceptionHandler并将线程和异常作为参数传递给handler的uncaughtException()方法进行处理。
15） 如何在两个线程间共享数据？

你可以通过共享对象来实现这个目的，或者是使用像阻塞队列这样并发的数据结构。这篇教程《Java线程间通信》(涉及到在两个线程间共享对象)用wait和notify方法实现了生产者消费者模型。
16) Java中notify 和 notifyAll有什么区别？

这又是一个刁钻的问题，因为多线程可以等待单监控锁，Java API 的设计人员提供了一些方法当等待条件改变的时候通知它们，但是这些方法没有完全实现。notify()方法不能唤醒某个具体的线程，所以只有一个线程在等待的时候它才有用武之地。而notifyAll()唤醒所有线程并允许他们争夺锁确保了至少有一个线程能继续运行。我的博客有更详细的资料和示例代码。
17) 为什么wait, notify 和 notifyAll这些方法不在thread类里面？

这是个设计相关的问题，它考察的是面试者对现有系统和一些普遍存在但看起来不合理的事物的看法。回答这些问题的时候，你要说明为什么把这些方法放在Object类里是有意义的，还有不把它放在Thread类里的原因。一个很明显的原因是JAVA提供的锁是对象级的而不是线程级的，每个对象都有锁，通过线程获得。如果线程需要等待某些锁那么调用对象中的wait()方法就有意义了。如果wait()方法定义在Thread类中，线程正在等待的是哪个锁就不明显了。简单的说，由于wait，notify和notifyAll都是锁级别的操作，所以把他们定义在Object类中因为锁属于对象。你也可以查看这篇文章了解更多。
18) 什么是ThreadLocal变量？

ThreadLocal是Java里一种特殊的变量。每个线程都有一个ThreadLocal就是每个线程都拥有了自己独立的一个变量，竞争条件被彻底消除了。它是为创建代价高昂的对象获取线程安全的好方法，比如你可以用ThreadLocal让SimpleDateFormat变成线程安全的，因为那个类创建代价高昂且每次调用都需要创建不同的实例所以不值得在局部范围使用它，如果为每个线程提供一个自己独有的变量拷贝，将大大提高效率。首先，通过复用减少了代价高昂的对象的创建个数。其次，你在没有使用高代价的同步或者不变性的情况下获得了线程安全。线程局部变量的另一个不错的例子是ThreadLocalRandom类，它在多线程环境中减少了创建代价高昂的Random对象的个数。查看答案了解更多。
19) 什么是FutureTask？

在Java并发程序中FutureTask表示一个可以取消的异步运算。它有启动和取消运算、查询运算是否完成和取回运算结果等方法。只有当运算完成的时候结果才能取回，如果运算尚未完成get方法将会阻塞。一个FutureTask对象可以对调用了Callable和Runnable的对象进行包装，由于FutureTask也是调用了Runnable接口所以它可以提交给Executor来执行。
20) Java中interrupted 和 isInterruptedd方法的区别？

interrupted() 和 isInterrupted()的主要区别是前者会将中断状态清除而后者不会。Java多线程的中断机制是用内部标识来实现的，调用Thread.interrupt()来中断一个线程就会设置中断标识为true。当中断线程调用静态方法Thread.interrupted()来检查中断状态时，中断状态会被清零。而非静态方法isInterrupted()用来查询其它线程的中断状态且不会改变中断状态标识。简单的说就是任何抛出InterruptedException异常的方法都会将中断状态清零。无论如何，一个线程的中断状态有有可能被其它线程调用中断来改变。
21) 为什么wait和notify方法要在同步块中调用？

主要是因为Java API强制要求这样做，如果你不这么做，你的代码会抛出IllegalMonitorStateException异常。还有一个原因是为了避免wait和notify之间产生竞态条件。
22) 为什么你应该在循环中检查等待条件?

处于等待状态的线程可能会收到错误警报和伪唤醒，如果不在循环中检查等待条件，程序就会在没有满足结束条件的情况下退出。因此，当一个等待线程醒来时，不能认为它原来的等待状态仍然是有效的，在notify()方法调用之后和等待线程醒来之前这段时间它可能会改变。这就是在循环中使用wait()方法效果更好的原因，你可以在Eclipse中创建模板调用wait和notify试一试。如果你想了解更多关于这个问题的内容，我推荐你阅读《Effective Java》这本书中的线程和同步章节。
23) Java中的同步集合与并发集合有什么区别？

同步集合与并发集合都为多线程和并发提供了合适的线程安全的集合，不过并发集合的可扩展性更高。在Java1.5之前程序员们只有同步集合来用且在多线程并发的时候会导致争用，阻碍了系统的扩展性。Java5介绍了并发集合像ConcurrentHashMap，不仅提供线程安全还用锁分离和内部分区等现代技术提高了可扩展性。更多内容详见答案。
24） Java中堆和栈有什么不同？

为什么把这个问题归类在多线程和并发面试题里？因为栈是一块和线程紧密相关的内存区域。每个线程都有自己的栈内存，用于存储本地变量，方法参数和栈调用，一个线程中存储的变量对其它线程是不可见的。而堆是所有线程共享的一片公用内存区域。对象都在堆里创建，为了提升效率线程会从堆中弄一个缓存到自己的栈，如果多个线程使用该变量就可能引发问题，这时volatile 变量就可以发挥作用了，它要求线程从主存中读取变量的值。
更多内容详见答案。
25） 什么是线程池？ 为什么要使用它？

创建线程要花费昂贵的资源和时间，如果任务来了才创建线程那么响应时间会变长，而且一个进程能创建的线程数有限。为了避免这些问题，在程序启动的时候就创建若干线程来响应处理，它们被称为线程池，里面的线程叫工作线程。从JDK1.5开始，Java API提供了Executor框架让你可以创建不同的线程池。比如单线程池，每次处理一个任务；数目固定的线程池或者是缓存线程池（一个适合很多生存期短的任务的程序的可扩展线程池）。更多内容详见这篇文章。
26） 如何写代码来解决生产者消费者问题？

在现实中你解决的许多线程问题都属于生产者消费者模型，就是一个线程生产任务供其它线程进行消费，你必须知道怎么进行线程间通信来解决这个问题。比较低级的办法是用wait和notify来解决这个问题，比较赞的办法是用Semaphore 或者 BlockingQueue来实现生产者消费者模型，这篇教程有实现它。
27） 如何避免死锁？


Java多线程中的死锁
死锁是指两个或两个以上的进程在执行过程中，因争夺资源而造成的一种互相等待的现象，若无外力作用，它们都将无法推进下去。这是一个严重的问题，因为死锁会让你的程序挂起无法完成任务，死锁的发生必须满足以下四个条件：

    互斥条件：一个资源每次只能被一个进程使用。
    请求与保持条件：一个进程因请求资源而阻塞时，对已获得的资源保持不放。
    不剥夺条件：进程已获得的资源，在末使用完之前，不能强行剥夺。
    循环等待条件：若干进程之间形成一种头尾相接的循环等待资源关系。

避免死锁最简单的方法就是阻止循环等待条件，将系统中所有的资源设置标志位、排序，规定所有的进程申请资源必须以一定的顺序（升序或降序）做操作来避免死锁。这篇教程有代码示例和避免死锁的讨论细节。
28) Java中活锁和死锁有什么区别？

这是上题的扩展，活锁和死锁类似，不同之处在于处于活锁的线程或进程的状态是不断改变的，活锁可以认为是一种特殊的饥饿。一个现实的活锁例子是两个人在狭小的走廊碰到，两个人都试着避让对方好让彼此通过，但是因为避让的方向都一样导致最后谁都不能通过走廊。简单的说就是，活锁和死锁的主要区别是前者进程的状态可以改变但是却不能继续执行。
29） 怎么检测一个线程是否拥有锁？

我一直不知道我们竟然可以检测一个线程是否拥有锁，直到我参加了一次电话面试。在java.lang.Thread中有一个方法叫holdsLock()，它返回true如果当且仅当当前线程拥有某个具体对象的锁。你可以查看这篇文章了解更多。
30) 你如何在Java中获取线程堆栈？

对于不同的操作系统，有多种方法来获得Java进程的线程堆栈。当你获取线程堆栈时，JVM会把所有线程的状态存到日志文件或者输出到控制台。在Windows你可以使用Ctrl + Break组合键来获取线程堆栈，Linux下用kill -3命令。你也可以用jstack这个工具来获取，它对线程id进行操作，你可以用jps这个工具找到id。
31) JVM中哪个参数是用来控制线程的栈堆栈小的

这个问题很简单， -Xss参数用来控制线程的堆栈大小。你可以查看JVM配置列表来了解这个参数的更多信息。
32） Java中synchronized 和 ReentrantLock 有什么不同？

Java在过去很长一段时间只能通过synchronized关键字来实现互斥，它有一些缺点。比如你不能扩展锁之外的方法或者块边界，尝试获取锁时不能中途取消等。Java 5 通过Lock接口提供了更复杂的控制来解决这些问题。 ReentrantLock 类实现了 Lock，它拥有与 synchronized 相同的并发性和内存语义且它还具有可扩展性。你可以查看这篇文章了解更多
33） 有三个线程T1，T2，T3，怎么确保它们按顺序执行？

在多线程中有多种方法让线程按特定顺序执行，你可以用线程类的join()方法在一个线程中启动另一个线程，另外一个线程完成该线程继续执行。为了确保三个线程的顺序你应该先启动最后一个(T3调用T2，T2调用T1)，这样T1就会先完成而T3最后完成。你可以查看这篇文章了解更多。
34) Thread类中的yield方法有什么作用？

Yield方法可以暂停当前正在执行的线程对象，让其它有相同优先级的线程执行。它是一个静态方法而且只保证当前线程放弃CPU占用而不能保证使其它线程一定能占用CPU，执行yield()的线程有可能在进入到暂停状态后马上又被执行。点击这里查看更多yield方法的相关内容。
35） Java中ConcurrentHashMap的并发度是什么？

ConcurrentHashMap把实际map划分成若干部分来实现它的可扩展性和线程安全。这种划分是使用并发度获得的，它是ConcurrentHashMap类构造函数的一个可选参数，默认值为16，这样在多线程情况下就能避免争用。欲了解更多并发度和内部大小调整请阅读我的文章How ConcurrentHashMap works in Java。
36） Java中Semaphore是什么？

Java中的Semaphore是一种新的同步类，它是一个计数信号。从概念上讲，从概念上讲，信号量维护了一个许可集合。如有必要，在许可可用前会阻塞每一个 acquire()，然后再获取该许可。每个 release()添加一个许可，从而可能释放一个正在阻塞的获取者。但是，不使用实际的许可对象，Semaphore只对可用许可的号码进行计数，并采取相应的行动。信号量常常用于多线程的代码中，比如数据库连接池。更多详细信息请点击这里。
37）如果你提交任务时，线程池队列已满。会时发会生什么？

这个问题问得很狡猾，许多程序员会认为该任务会阻塞直到线程池队列有空位。事实上如果一个任务不能被调度执行那么ThreadPoolExecutor’s submit()方法将会抛出一个RejectedExecutionException异常。
38) Java线程池中submit() 和 execute()方法有什么区别？

两个方法都可以向线程池提交任务，execute()方法的返回类型是void，它定义在Executor接口中, 而submit()方法可以返回持有计算结果的Future对象，它定义在ExecutorService接口中，它扩展了Executor接口，其它线程池类像ThreadPoolExecutor和ScheduledThreadPoolExecutor都有这些方法。更多详细信息请点击这里。
39) 什么是阻塞式方法？

阻塞式方法是指程序会一直等待该方法完成期间不做其他事情，ServerSocket的accept()方法就是一直等待客户端连接。这里的阻塞是指调用结果返回之前，当前线程会被挂起，直到得到结果之后才会返回。此外，还有异步和非阻塞式方法在任务完成前就返回。更多详细信息请点击这里。
40) Swing是线程安全的吗？ 为什么？

你可以很肯定的给出回答，Swing不是线程安全的，但是你应该解释这么回答的原因即便面试官没有问你为什么。当我们说swing不是线程安全的常常提到它的组件，这些组件不能在多线程中进行修改，所有对GUI组件的更新都要在AWT线程中完成，而Swing提供了同步和异步两种回调方法来进行更新。点击这里查看更多swing和线程安全的相关内容。
41） Java中invokeAndWait 和 invokeLater有什么区别？

这两个方法是Swing API 提供给Java开发者用来从当前线程而不是事件派发线程更新GUI组件用的。InvokeAndWait()同步更新GUI组件，比如一个进度条，一旦进度更新了，进度条也要做出相应改变。如果进度被多个线程跟踪，那么就调用invokeAndWait()方法请求事件派发线程对组件进行相应更新。而invokeLater()方法是异步调用更新组件的。更多详细信息请点击这里。
42) Swing API中那些方法是线程安全的？

这个问题又提到了swing和线程安全，虽然组件不是线程安全的但是有一些方法是可以被多线程安全调用的，比如repaint(), revalidate()。 JTextComponent的setText()方法和JTextArea的insert() 和 append() 方法也是线程安全的。
43) 如何在Java中创建Immutable对象？

这个问题看起来和多线程没什么关系， 但不变性有助于简化已经很复杂的并发程序。Immutable对象可以在没有同步的情况下共享，降低了对该对象进行并发访问时的同步化开销。可是Java没有@Immutable这个注解符，要创建不可变类，要实现下面几个步骤：通过构造方法初始化所有成员、对变量不要提供setter方法、将所有的成员声明为私有的，这样就不允许直接访问这些成员、在getter方法中，不要直接返回对象本身，而是克隆对象，并返回对象的拷贝。我的文章how to make an object Immutable in Java有详细的教程，看完你可以充满自信。
44） Java中的ReadWriteLock是什么？

一般而言，读写锁是用来提升并发程序性能的锁分离技术的成果。Java中的ReadWriteLock是Java 5 中新增的一个接口，一个ReadWriteLock维护一对关联的锁，一个用于只读操作一个用于写。在没有写线程的情况下一个读锁可能会同时被多个读线程持有。写锁是独占的，你可以使用JDK中的ReentrantReadWriteLock来实现这个规则，它最多支持65535个写锁和65535个读锁。
45) 多线程中的忙循环是什么?

忙循环就是程序员用循环让一个线程等待，不像传统方法wait(), sleep() 或 yield() 它们都放弃了CPU控制，而忙循环不会放弃CPU，它就是在运行一个空循环。这么做的目的是为了保留CPU缓存，在多核系统中，一个等待线程醒来的时候可能会在另一个内核运行，这样会重建缓存。为了避免重建缓存和减少等待重建的时间就可以使用它了。你可以查看这篇文章获得更多信息。
46）volatile 变量和 atomic 变量有什么不同？

这是个有趣的问题。首先，volatile 变量和 atomic 变量看起来很像，但功能却不一样。Volatile变量可以确保先行关系，即写操作会发生在后续的读操作之前, 但它并不能保证原子性。例如用volatile修饰count变量那么 count++ 操作就不是原子性的。而AtomicInteger类提供的atomic方法可以让这种操作具有原子性如getAndIncrement()方法会原子性的进行增量操作把当前值加一，其它数据类型和引用变量也可以进行相似操作。
47) 如果同步块内的线程抛出异常会发生什么？

这个问题坑了很多Java程序员，若你能想到锁是否释放这条线索来回答还有点希望答对。无论你的同步块是正常还是异常退出的，里面的线程都会释放锁，所以对比锁接口我更喜欢同步块，因为它不用我花费精力去释放锁，该功能可以在finally block里释放锁实现。
48） 单例模式的双检锁是什么？

这个问题在Java面试中经常被问到，但是面试官对回答此问题的满意度仅为50%。一半的人写不出双检锁还有一半的人说不出它的隐患和Java1.5是如何对它修正的。它其实是一个用来创建线程安全的单例的老方法，当单例实例第一次被创建时它试图用单个锁进行性能优化，但是由于太过于复杂在JDK1.4中它是失败的，我个人也不喜欢它。无论如何，即便你也不喜欢它但是还是要了解一下，因为它经常被问到。你可以查看how double checked locking on Singleton works这篇文章获得更多信息。
49） 如何在Java中创建线程安全的Singleton？

这是上面那个问题的后续，如果你不喜欢双检锁而面试官问了创建Singleton类的替代方法，你可以利用JVM的类加载和静态变量初始化特征来创建Singleton实例，或者是利用枚举类型来创建Singleton，我很喜欢用这种方法。你可以查看这篇文章获得更多信息。
50) 写出3条你遵循的多线程最佳实践

这种问题我最喜欢了，我相信你在写并发代码来提升性能的时候也会遵循某些最佳实践。以下三条最佳实践我觉得大多数Java程序员都应该遵循：

    给你的线程起个有意义的名字。
    这样可以方便找bug或追踪。OrderProcessor, QuoteProcessor or TradeProcessor 这种名字比 Thread-1. Thread-2 and Thread-3 好多了，给线程起一个和它要完成的任务相关的名字，所有的主要框架甚至JDK都遵循这个最佳实践。
    避免锁定和缩小同步的范围
    锁花费的代价高昂且上下文切换更耗费时间空间，试试最低限度的使用同步和锁，缩小临界区。因此相对于同步方法我更喜欢同步块，它给我拥有对锁的绝对控制权。
    多用同步类少用wait 和 notify
    首先，CountDownLatch, Semaphore, CyclicBarrier 和 Exchanger 这些同步类简化了编码操作，而用wait和notify很难实现对复杂控制流的控制。其次，这些类是由最好的企业编写和维护在后续的JDK中它们还会不断优化和完善，使用这些更高等级的同步工具你的程序可以不费吹灰之力获得优化。
    多用并发集合少用同步集合
    这是另外一个容易遵循且受益巨大的最佳实践，并发集合比同步集合的可扩展性更好，所以在并发编程时使用并发集合效果更好。如果下一次你需要用到map，你应该首先想到用ConcurrentHashMap。我的文章Java并发集合有更详细的说明。

51) 如何强制启动一个线程？

这个问题就像是如何强制进行Java垃圾回收，目前还没有觉得方法，虽然你可以使用System.gc()来进行垃圾回收，但是不保证能成功。在Java里面没有办法强制启动一个线程，它是被线程调度器控制着且Java没有公布相关的API。
52) Java中的fork join框架是什么？

fork join框架是JDK7中出现的一款高效的工具，Java开发人员可以通过它充分利用现代服务器上的多处理器。它是专门为了那些可以递归划分成许多子模块设计的，目的是将所有可用的处理能力用来提升程序的性能。fork join框架一个巨大的优势是它使用了工作窃取算法，可以完成更多任务的工作线程可以从其它线程中窃取任务来执行。你可以查看这篇文章获得更多信息。
53） Java多线程中调用wait() 和 sleep()方法有什么不同？

Java程序中wait 和 sleep都会造成某种形式的暂停，它们可以满足不同的需要。wait()方法用于线程间通信，如果等待条件为真且其它线程被唤醒时它会释放锁，而sleep()方法仅仅释放CPU资源或者让当前线程停止执行一段时间，但不会释放锁。你可以查看这篇文章获得更多信息。

以上就是50道热门Java多线程和并发面试题啦。我没有分享所有题的答案但给未来的阅读者提供了足够的提示和线索来寻找答案。如果你真的找不到某题的答案，联系我吧，我会加上去的。这篇文章不仅可以用来准备面试，还能检查你对多线程、并发、设计模式和竞态条件、死锁和线程安全等线程问题的理解。我打算把这篇文章的问题弄成所有Java多线程问题的大合集，但是没有你的帮助恐怖是不能完成的，你也可以跟我分享其它任何问题，包括那些你被问到却还没有找到答案的问题。这篇文章对初学者或者是经验丰富的Java开发人员都很有用，过两三年甚至五六年你再读它也会受益匪浅。它可以扩展初学者尤其有用因为这个可以扩展他们的知识面，我会不断更新这些题，大家可以在文章后面的评论中提问，分享和回答问题一起把这篇面试题完善。



##### IntelliJ IDEA 与 eclipse 的对比（快捷键的映射）


作为一个资深的Eclipse用户，我想对IntelliJ IDEA做一个更为严谨的审视。JetBrains的工作人员非常的友善，并为Podcastpedia.org和Codingpedia.org这两个 工程给予了我一个开放源码的许可证。在这片文章中，我列出来Eclipse中常用且与IntelliJ等同的一些操作。写这篇文章为了以后遗忘时能够再用做个记录，也为或许能帮助到其他的人。
###### 快捷键

要事先说！下表中列出了在两个IDE之中我最常用的快捷键：

|描述|Eclipse|IntelliJ|
|:--:|:--:|:--:|
||||
代码补全
	

Ctrl+space
	

ctrl+space

打开类或者接口

（两个IDE都支持使用“驼峰字符”前缀的方式来过滤查找列表，进而轻松完成搜索；比如：可以使用“PoDI”来检索PodcastDaoImpl类）
	

 Ctrl+Shift +T
	

Ctrl+N

快速打开文件/资源
	

Ctrl+Shift+R
	

Shift+F6

打开声明
	

F3
	

Ctrl+B

查看Javadoc/详情
	

鼠标滑过（F2聚焦）
	

Ctrl+Q

快速修复
	

Alt+1
	

Alt+Enter

导入所有须要的包
	

Ctrl+Shift+O
	

Ctrl+Alt+O

保存文件/保存所有文件
	

Ctrl+S/Ctrl+Shift+S
	

自动保存

当前文件快速定位弹出框（成员，方法）
	

Ctrl+O
	

Ctrl+F12

源码（生成getter和setter，构造器等）
	

Alt+Up /Alt+Down
	

Alt+Insert

当前语法补全

if，do-while，try-catch，return（方法调用）等正确的语法构造（如：添加括号）
		Ctrl+Shift+Enter

抽取常量
	

Ctrl+1->抽取常亮
	

Ctrl+Alt+C

抽取变量
	

Ctrl+1->抽取变量
	

Ctrl+Alt+V
增加、删除以及移动数行代码

在当前插入符添加一行
	

Shift+Enter
	

Shift+Enter

复制一行或代码段
	

Ctrl+Alt+Up/Down
	

Ctrl+D

删除一行代码
	

Ctrl+D
	

Ctrl+Y

选中代码向上或者向下移动
	

Alt+Up/Down
	

Shift+Alt+Up/Down
查找/搜索

查找类/变量在工作区或工程中使用
	

Ctrl+Shift+G
	

Alt+F7

在工程或者工作区中查找文本
	

Ctrl+H (选择文件搜索)
	

Ctrl+Shift+F
导航

回退（撤消最后导航操作）
	

Alt+Left
	

Ctrl+Alt+Left

标签/编辑之间的导航
	

Ctrl + Page Down / Up
	

Alt + Left/Alt + Right

跳转某一行
	

Ctrl+L
	

Ctrl+G

导航到最近的文件
	

Ctrl + E
	

Ctrl + E

在编辑器之间快速切换方法
		

Alt + Up / Down
调试

运行一行
	

F6
	

F8

进入下一次计算
	

F5
	

F7

运行到下一个断点
	

F7
	

Shift+F8

回复运行
	

F8
	

F9


##### 编译器链接

很多时候我们在编辑一个文件，同时还需要编辑其他的文件。假如FF类是一个经常编辑的类，同时又需要对同一个包中的其他类进行编辑—通过链接编辑器的功能，可以迅速在同包的类之间进行切换。这个功能为我们提供了什么样的便利？每当编辑了一个文件，它会立即显示其所在包浏览器视图/项目视图中的位置。如果使用展开式的包视图，它会按功能对类划分并显示，而不使用分层（dao层, service层等）的方式来展示类。这也是我强烈推荐的展示方式，因为真的很方便。

Eclipse

在工程浏览视图或者包浏览视图可以看到并使用链接编辑器（Link to Editor）的按钮。

![](http://incdn1.b0.upaiyun.com/2015/04/feca7d2e3278a39d5a066dd22b689d21.png)


如果不想使用该功能，依然可以使用Alt+Shift+W快捷键来查看包视图或工程视图并设置其显示位置。

![](http://incdn1.b0.upaiyun.com/2015/04/a96c9956512cf6432a7ea58dd0ce204a.png)

 IntelliJ

在工程视图或者包视图中选择设置，然后勾选根据源码自动滚动（Autoscroll From Source）功能；

![](http://incdn1.b0.upaiyun.com/2015/04/d1514431876d0361dc88166b4e15e3c9.png)

如果不想使用该功能，依然可以使用快捷键Alt+F1来导航并设置显示的位置；

![](http://incdn1.b0.upaiyun.com/2015/04/0e8478ea71c7894824945b3f23288c83.png)

###### IntelliJ的魅力之处
默认设置了许多的功能

IntelliJ本身就自带了众多的功能（如：GitHub的集成）。当然，在Eclipse你也可以通过选择不同版本的插件来获取到足够的功能，只是需要自己来配置这些插件。

使用鼠标滚轮改变字体大小

在IntelliJ中，可以使用鼠标滚轮来改变字体大小（我在浏览器中经常使用该功能）。但是这个功能需要手动激活。

  1.  打开IDE的设置（Ctrl+Shift+S或点击 文件菜单>Setting）
  2.  在编辑器页面（在搜索框中输入“Editor”），确保Change font size (Zoom) with Ctrl+MouseWheel这个选项被选中。

![](http://incdn1.b0.upaiyun.com/2015/04/d024e1c53ca09e5bf021f7aa9c7540d3-1024x735.png)

在IDE中直接启动命令行终端

使用快捷键： Alt + F12

灵活易用的模板

输入p，然后使用快捷键Ctrl+J，就可以获取以下选项：

    psf – public static final
    psfi – public static final int
    psfs – public static final String
    psvm – main method declaration

对JavaScript、HTML5的强力支持

商业版的IntelliJ应该包含了对 HTML5、CSS3、SASS、LESS、JavaScript、CoffeeScript、Node.js、ActionScript以及其他语言的代码辅助功能。我将尽快地确认这些内容。
相比Eclipse IntelliJ的不足之处
无法最大化控制台

在Eclipse中，可以使用Ctrl+M快捷键或者双击标签来最大化当前的控制台。但是在IntelliJ中并没有类似的方式来。
鼠标悬停显示Javadoc

当然，在IntelliJ中可以使用Ctrl+Q快捷键来获取上述的功能。但当鼠标悬停代码就能看到部分Javadoc的功能在Eclipse中显得是那么的友好。
总结

在我看来，每一个IDE都很棒，IntelliJ看起来更加的现代，但有时候我又喜欢经典版的Eclipse，这可能是因为过去经常使用Eclipse。以后可能会继续受这个因素的影响。

到这里就是我全部的经验，后续将继续添加一些在使用Eclipse和IntelliJ遇到的功能以及功能上的差异，敬请期待。

