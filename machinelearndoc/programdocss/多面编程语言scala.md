多面编程语言scala

[toc]

###### 概述

如Scala官网宣称的：“Object-OrientedMeetsFunctional”，这一句当属对Scala最抽象的精准描述，它把近二十年间大行其道的面向对象编程与旧而有之的函数式编程有机结合起来，形成其独特的魔力。希望通过本文能够吸引你去了解、尝试Scala，体验一下其独特魅力，练就自己的寒冰掌、火焰刀。

回首初次接触Scala，时光已忽忽过去四五年。从当初“Scala取代Java”的争论，到今天两者的相安无事，Scala带给了我们哪些有意义的尝试呢？在我掌握的众多编程语言之中，Scala无疑是其中最让我感到舒适的，如Scala官网宣称的：“Object-OrientedMeetsFunctional”，这一句当属对Scala最抽象的精准描述，它把近二十年间大行其道的面向对象编程与旧而有之的函数式编程有机结合起来，形成其独特的魔力。不知你是否看过梁羽生的著作《绝塞传烽录》？里面白驼山主宇文博的绝学：左手“寒冰掌”、右手“火焰刀”，用来形容Scala最为合适了，能够将OOP与FP结合得如此完美的语言，我认为唯有Scala。

众所周知，Java称不上纯粹的面向对象语言，但Scala却拥有纯粹的面向对象特性，即便是1+1这么简单的事情，实际上也是执行1.+（1）。而在对象组合方面，Scala拥有比接口更加强大的武器──特质（trait）。

Scala同时作为一门函数式编程语言，理所当然地具备了函数式语言的函数为头等“公民”、方法无副作用等特性。事实上，Scala更吸引我的并不是OOP特性，而是FP特性！一边是OOP、一边是FP，这就是多面的Scala，极具魅力而且功能强大。

在多核时代，现代并发语言不断涌现出来，例如Erlang、Go、Rust，Scala当然也位列其中。Scala的并发特性，堪称Scala最吸引开发者的招牌式特性！Scala是静态类型的。许多人会把vals=”ABC”这样的当作动态类型特性，而vals:String=”ABC”才认为是静态类型特性。实际上，这无关类型争论，而是类型系统实现的范畴。是的，在Scala里，你可以放心大胆地使用vals=”ABC”，而Scala里强大的类型推断和模式匹配，绝对会让你爱不释手。

此外，Scala作为JVM语言，理所当然享有Java庞大而优质的资源，与Java间可实现无缝交互，事实上，Scala最终当然是编译为Java字节码。

本文将把重点放在Scala的特色之处。作为一门完备而日趋成熟的语言，Scala的知识点有不少，本文当然无法做到面面俱到，但希望能够带你感受Scala魅力，并理解其重要概念。


##### scala 的面向对象

###### 类的定义

```scala
class Person(val name:String,var age : Int, var identity:String) {
	println("测试信息")
}

val person = new Person("小强",32,"程序员")

println(person.name + " 今年： " + person.age + "岁，是一名" + person.identity)
```

我们知道，动态语言一般都提供了REPL环境，同时，动态语言的程序代码都是以脚本方式解释运行的，这给开发带来了不少的便利。Scala虽然是静态类型系统的语言，但同样提供了这两个福利，让你倍感贴心。

因此，你可以任意采取以下运行方式：
```shell
    在命令行窗口或终端输入：scala，进入Scala的REPL窗口，逐行运行上述代码；
    此外，也可以将上述代码放入某个后缀名为.scala的文件里，如test.scala，然后通过脚本运行方式运行： scala test.scala。
```
测试信息“小强今年32岁，是一名程序员”结果出来了！

多么简单，类的定义就这么多，却能够做这么多事情，想想Java的实现吧，差别太大了。我们先来分析下代码。假设在上述第二种方式的test.scala文件中，注释掉后面两行并保存，运行：
```shell
    scalac test.scala
    javap -p Person
```
我们先是把文件编译成字节码（这实际上是跟Java编译对应的第三种编译/运行方式），之后反编译并查看结果：

![](http://img.ptcms.csdn.net/article/201506/25/558b9d405e4b5.jpg)


这个结果跟Java实现的代码类似（生成的getter和 setter跟Java实现有所不同，但在这里不是什么问题），可见，Scala帮我们做了多少简化工作。这段代码有以下值得注意的地方：

我们可以把字段定义和构造函数直接写在Scala的类定义里，其中，关键字val的含义是“不可变”，var 为“可变”，Scala的惯用法是优先考虑val，因为这更 贴近函数式编程风格；

    在Scala中，语句末尾的分号是可选的；
    Scala默认类访问修饰符为public；
    注意println（”测试信息”）这一行，将在主构造函数里执行；
    val与var两者对应Java声明的差异性已在反编译代码中体现了。


###### 伴生对象与伴生类

伴生对象与伴生类在Scala的面向对象编程方法中占据极其重要的位置，例如Scala中许多工具方法都是由伴 生对象提供的。

伴生对象首先是一个单例对象，单例对象用关键字object定义。在Scala中，单例对象分为两种，一种是并未自动关联到特定类上的单例对象，称为独立对象 （Standalone Object）；另一种是关联到一个类上的单例对象，该单例对象与该类共有相同名字，则这种单例对象称为伴生对象（Companion Object），对应类称为伴生类。

Java中的类，可以既有静态成员，又有实例成员。而在Scala中没有静态成员（静态字段和静态方法），因为静态成员从严格意义而言是破坏面向对象纯洁性的，因此，Scala借助伴生对象来完整支持类一级的属 性和操作。伴生类和伴生对象间可以相互访问对方的 private字段和方法。

接下来看一个伴生类和伴生对象的例子（Person. scala）。
```scala
class Person private (val name:String) {
	private def getUniqueSkill() = name + "的必杀技" 
     + Person.uniquekill
}


object Person {
	private val uniqueSkill = "scala!"
    private val person = new Person("小强")
    
    def printUniqueSkill  =  println(person.getUniqueSkill())
}

Person.printUniqueSkill()
```

这是一个典型的伴生类和伴生对象的例子，注意以下说明：

    伴生类Person的构造函数定义为private，虽然这不是必须的，却可以有效防止外部实例化Person类，使得Person类只能供对应伴生对象使用；
    每个类都可以有伴生对象，伴生类与伴生对象写在同一个文件中；
    在伴生类中，可以访问伴生对象的private字段Person.uniqueSkill；
    而在伴生对象中，也可以访问伴生类的private方法 Person.getUniqueSkill（）；
    最后，在外部不用实例化，直接通过伴生对象访问Person.printUniqueSkill（）方法。


###### 特质 (Trait)

Scala的特质类似于Java中的接口作用，专门用来解决现实编程中的横切关注点矛盾，可以在类或实例中混入（Mixin）这些特质。实际上，特质最终会被编译成Java的接口及相应的实现类。Scala的特质提供的特性远比Java的接口灵活，让我们直接来看点有趣的东西吧。

![](http://img.ptcms.csdn.net/article/201506/25/558b9e9c66de5.jpg)

我们先是定义了一个Programmer抽象类。最后定义了四个不同程序员的Trait，且都继承自Programmer抽象类，然后，通过不同的特质排列组合，看看我们产生的结果是什么样子的：

所有程序员都至少掌握一门编程语言。

我掌握Scala。我掌握Golang。

所有程序员都至少掌握一门编程语言。

我掌握Scala。我掌握Golang。我掌握PHP。……

Wow~！有趣的事情发生了，通过混入不同的特质组合，不同的程序员都可以有合适的词来介绍自己，而每个程序员的共性就是：“所有程序员都至少掌握一门编程语言”。让我们来解释一下具体思路：

这段代码里面，特质通过with混入实例，如：new  Programmer with Scalaist。当然，特质也可以混入类中；

    为什么信息可以传递呢？比如我掌握Scala。我掌握Golang。我掌握PHP？答案就在super.getSkill（）上。该调用不是对父类的调用，而是对其左边混入的Trait的调用，如果到左边第一个，就是调用Programmer抽象类的getSkill（）方法。这是Trait的一个链式延时绑定特性，那么在现实中，这个特性就表现出极大的灵活性，可以根据需要任意搭配，大大降低代码量。

Scala的面向对象特性，暂先介绍到这里。其实还有好些内容，限于篇幅，实在是有点意犹未尽的感觉。



##### scala 的函数式风格

Scala的魅力之一就是其函数式编程风格实现。如果把上面介绍的面向对象特性看成是Scala的“寒冰掌”，让你感受到了迥异于Java实现的特性，那么，Scala强大而魔幻的函数式特性，就是其另一大杀招“火焰刀”，喷发的是无坚不摧的怒焰之火。

###### 集合类型

Scala常用集合类型有Array、Set、Map、Tuple和List等。Scala提供了可变（mutable）与不可变（immutable）的集合类型版本，多线程应用中应该使用不可变版本，这很容易理解。

    Array：数组是可变的同类对象序列；
    Set：无序不重复集合类型，有可变和不可变实现；
    Map：键值对的映射，有可变和不可变实现；
    Tuple：可以包含不同类元素，不可变实现；
    List：Scala的列表是不可变实现的同类对象序列，因应函数式编程特性的需要。
    List大概是日常开发中使用最多的集合类型了。

这些集合类型包含了许多高阶函数，如：map、find、filter、fold、reduce等等，构建出浓郁的函数式风格用法，接下来我们就来简单了解一下：

```scala
val l = List("Javascript","scala","Golang").map(e => e + "很好～")
val l = List("Javascript","scala","Golang").map(_ + "很好～")

l.foreach(e=>println(e))
```
输出如下：
```shell
JavaScript很棒~

Scala很棒~

Golang很棒~
```

map（）函数在List上迭代，对List中的每个元素，都会调用以参数形式传入的Lambda表达式（或者叫匿名函数）。其结果是创建一个新的List，其元素内容都发生了相应改变，可以从输出结果观察到。注意，代码中有一行是速写法代码，我个人比较喜欢这种形式，但在复杂代码中可读性差一些。

最后，我们用了另一个foreach（）方法来迭代输出结果。

高阶函数、Lambda表达式，都是纯正的函数式编程风格。如果你接触过Haskell，就会发现Scala函数式风格的实现，在骨子里像极了Haskell，感觉非常亲切。在编写Scala代码的过程中，将处处体现出它的函数式编程风格，高效而简洁。

限于篇幅，我们只能浅尝辄止，如果有兴趣，可以进一步参考我以前写的两篇相关博文，里面有比较详细的描述：七八个函数，两三门语言㈠和七八个函数，两三门语言㈡•完结篇。

###### 高阶函数、柯里化、不全函数和闭包

实际上我们在前面已经见识过Scala的高阶函数（Higher-order Function）了，只不过是Scala自带的map（）和foreach（）。高阶函数在维基百科中的定义 是：“高阶函数是至少满足下列一个条件的函数：接 受函数作为输入；输出一个函数”。接下来，我们来实现一个自己的高阶函数──求圆周 长和圆面积：

```scala
def cycle(r:Array[Float],cacl:Float => Float ):Map[Float,Float] = {
	var result : Map[Float,Float] = Map()
    
    r.foreach(e=>result += (e->cacl(e)))
    result
}

println("圆周长：" + cycle(Array(1.0f,2.3f,4.5f),e=>2 * 3.14f * e))
println("圆面积：" + cycle(Array(1.0f,2.3f,4.5f), e=>3.14f*e*e))
```

我们定义了一个高阶函数cycle。输入参数中传入一个函数值calc，其类型是函数，接收Float输入，输出也是Float。在实现里，我们会调用calc函数。在调用时，我们分别传入求圆周长和圆面积的匿名函数，用于实现calc函数的逻辑。

这样，我们用一个高阶函数cycle，就可以满足求圆周长和圆面积的需求，不需要分别定义两个函数来处理不同任务，而且代码直观简洁。最后，我们打印结果，输出一组半径分别对应的圆周长和圆面积。在这里，我们用到了映射Map：

圆周长：Map（1.0 -> 6.28, 2.3 -> 14.444, 4.5 -> 28.26）

圆面积：Map（1.0 -> 3.14, 2.3 -> 16.6106, 4.5 -> 63.585）

接下来，我们对上述代码稍加改动：
```scala
def cycle(r:Array[Float])(cacl:Float => Float ):Map[Float,Float] = {
	var result : Map[Float,Float] = Map()
    
    r.foreach(e=>result += (e->cacl(e)))
    result
}

println("圆周长：" + cycle(Array(1.0f,2.3f,4.5f),e=>2 * 3.14f * e))
println("圆面积：" + cycle(Array(1.0f,2.3f,4.5f), e=>3.14f*e*e))
```

输出结果同上。

注意到了吗？我们把cycle函数的两个输入参数进行了拆分（如上述代码第一行），同时在调用cycle函数时，方式也有所不同（如上述代码最后两行）。这是什么意思？

这在函数式编程中称为柯里化（Curry），柯里化可以把函数定义中原有的一个参数列表转变为接收多个参数列表。在函数式编程中，一个参数列表里含多个参数的函数都是柯里函数，可以柯里化。

要知道，在函数式编程里，函数是一等的，当然函数也可以作为参数和返回被传递。这对初次接触函数式编程的开发者而言确实比较抽象。上述代码的理解，你可以这样想象：（cacl: Float => Float）是函数cycle2（r: Array[Float]）的输入参数！进一步，可以这么理解：cacl取一个参 数，变成了一个不全函数（Partially Function）cycle2 （r: Array[Float]），所谓不全函数就是它还有参数未确定，你想要完整用它的话，还需要继续告知它未定的 参数，如（cacl: Float => Float）。

还没完！根据上述描述，我们继续看看如何用各种Hacker的调用方式：

![](http://img.ptcms.csdn.net/article/201506/25/558ba0cb5c2f3.jpg)

可以用valc21=cycle2 _、val c22 = cycle2（Array （1.0f, 2.3f, 4.5f）） _诸如此类的方式创建不全函数，并调用它。

看得出来，不全函数同样可以提升代码的简洁程度，比如本例代码中，参数Array（1.0f, 2.3f, 4.5f）是固定不 变的，我们就不用每次都在调用cycle2时传入它，可以 先定义c22，再用c22来处理。

函数式崇尚的“函数是第一等公民”理念可不容小觑。函数，就是这么任性！接下来，我们来了解下闭包（Closure）的概念，依旧先看个简单的例子：

```scala
var high = 2.0f
val caclCylinderVolume = (e:Float) => println(3.14f * e * e *high)

val r = Array(1.0f,2.3f,4.5f)

r.map(caclCylinderVolume)

high = 3.0f
r.map(caclCylinderVolume)
```
这个例子用来求圆柱体的体积。这里定义了一个caclCylinderVolume函数（因为函数式风格里函数是一等公民，所以可以用这样的函数字面量方式来定义。或者也可以称之为代码块），函数里面引用了一个自由变量high，caclCylinderVolume函数并未绑定high。而在caclCylinderVolume函数运行时，要先“闭合”函数及其所引用变量high的外部上下文，这样也就绑定了变量high，此时绑定了变量high的函数对象称为闭包。

由代码可知，由于函数绑定到了变量high本身，因此，high如果发生改变，将影响函数的运算结果；而如果在函数里更新了变量，那这种更新在函数之外也会被体现。


###### 模式匹配

scala 的模式匹配实现非常强大。模式匹配为编程过程带来了莫大便利，在Scala并发编程中也得到了广泛应用。

```scala
val pattern = """^(S|s)cala-(\d+\.\d+)""".r

List("Scala","Scala-2.11.6","Golang","PHP").foreach{ lang=>
lang match {
	case "Scala" => println("多面者 Scala～")
    case pattern(_,version) => println("你的scala 版本是：" + version)
    case item if (item.length = 6) => println("八成是干净整洁的Golang")
    case _ => println(lang + "语言呢？")
}
}
```

输出结果如下：

多面者Scala~

你的Scala版本是：2.11.6

八成是干净简洁的Go、PHP语言呢？

可见，模式匹配特性非常好用，可以灵活应对许多复杂的应用场景：



    第一个case表达式匹配普通的字面量；
    第二个case表达式匹配正则表达式；
    第三个case表达式使用了if判断，这种方式称为模式护卫（Pattern Guard），可以对匹配条件加以过滤；
    第四个case表达式使用了“_”来处理未匹配前面几项的情况。

此外，Scala的模式匹配还有更多用法，如case类匹配、option类型匹配，同时还能带入变量，匹配各种集合类型。综合运用模式匹配，能够极大提升开发效率。

###### 并发编程

现代语言的特性往往是随硬件环境和技术趋势演进的，多核时代的来临，互联网大规模复杂业务处理，都对传统语言提出了挑战，于是，新展现的语言几乎都非常关注并发特性，Scala亦然。

Scala语言并发设计采用Actor模型，借鉴了Erlang的Actor实现，并且在Scala2.10之后，改为使用AkkaActor模型库。Actor模型主要特征如下：

    “一切皆是参与者”，且各个actor间是独立的；
    发送者与已发送消息间解耦，这是Actor模型显著特点，据此实现异步通信；
    actor是封装状态和行为的对象，通过消息交换进行相互通信，交换的消息存放在接收方的邮箱中；actor可以有父子关系，父actor可以监管子actor，子actor唯一的监管者就是父actor；
    一个actor就是一个容器，它包含了状态、行为、一个邮箱（邮箱用来接受消息）、子actor和一个监管策略。

我们先来看个例子感受下：

```scala
import akka.actor.Actor
import akka.actor.Probs
import akka.actor.ActorSystem
import akka.routing.RoundRobinPool

// 计算用的Actor
class CalcActor extends Actor {
	var count = 0
    
    def receive = {
    	case value : Int =>println("序号为：" + value + ".")
        
        count += 1
        if (count == 4) {
        context.stop(self)
        }
        
        case _ => println("Unknown ...")
    }
}

object Concurrency {
	def main(args:Array[String]):Unit = {
    	val system = ActorSystem("ConcurrencyActorSystem")
   
   
   		val calcActor = system.actorOf(Props[CalcActor]).withRouter (
        RoundRobinPool(nrOfInstances = 4),name = "calcActor"
        )
        
        for(i <- 1 to 4 ) calcActor ! i
        Thread.sleep(1000)
        system.shutdown()
    }
}

```

在这里，Concurrency是CalcActor的父actor。在Concurrency中先要构建一个Akka系统：

```scala
val system = ActorSystem("ConcurrencyActorSystem")
   
   
   		val calcActor = system.actorOf(Props[CalcActor]).withRouter (
        RoundRobinPool(nrOfInstances = 4),name = "calcActor"
        )
```


同时，这里的设置将会在线程池里初始化称为“routee”的子actor（这里是CalcActor），数量为4，也就是我们需要4个CalcActor实例参与并发计算。这一步很关键。actor是一个容器，使用actorOf来创建Actor实例时，也就意味着需指定具体Actor实例，即指定哪个actor在执行任务，该actor必然要有“身份”标识，否则怎么指定呢？！

在Concurrency中通过以下代码向CalcActor发送序号并启动并发计算：

for（i<-1to4）calcActor!i

然后，在CalcActor的receive中，通过模式匹配，对接收值进行处理，直到接收值处理完成。在运行结果就会发现每次输出的顺序都是不一样的，因为我们的程序是并发计算。比如某次的运行结果如下。

    序号为：1。
    序号为：3。
    序号为：2。
    序号为：4。

actor是异步的，因为发送者与已发送消息间实现了解耦；在整个运算过程中，我们很容易理解发送者与已发送消息间的解耦特征，发送者和接收者各种关心自己要处理的任务即可，比如状态和行为处理、发送的时机与内容、接收消息的时机与内容等。当然，actor确实是一个容器，且五脏俱全：我们用类来封装，里面也封装了必须的逻辑方法。Akka基于JVM，虽然可以穿插混合应用函数式风格，但实现模式是面向对象，天然讲究抽象与封装，其当然也能应用于Java语言。我们的Scala之旅就要告一个段落了！Scala功能丰富而具有一定挑战度，上述三块内容，每一块都值得扩展详述，但由于篇幅关系，在此无法一一展开。

希望通过本文能够吸引你去了解、尝试Scala，体验一下其独特魅力，练就自己的寒冰掌、火焰刀。


##### 分布式语言对比


[七八个函数，两三门语言㈠"](http://zhuanlan.zhihu.com/guagua/19969771)
[七八个函数，两三门语言㈡·完结篇](http://zhuanlan.zhihu.com/guagua/19972732)
[到底什么是函数式编程思维](http://www.zhihu.com/question/28292740)
[分布式机器学习的故事：Docker改变世界](http://zhuanlan.zhihu.com/cxwangyi/19902938)



###### 如果对方是一个 Scala 爱好者，有什么办法说服他使用 Go？
113
王益，http://arxiv.org/abs/1405.4402
收起
彳亍、杨其斌、曾杰瑜 等人赞同
我不知道有什么办法说服人放弃一种语言，用另一种。不是因为我们没法客观比较，而是一般人们会因为各种原因拒绝客观比较。

我在腾讯工作的时候，我们团队和深圳一个团队合并。我注意到深圳一位同事用Scala，写的程序相对用Java简练清晰，让人眼前一亮。我因此专门学习了一段时间Scala。于此同时，我也在学习Go。两个学习过程同时开始，没有什么偏见。但是后来开始开发Peacock（http://arxiv.org/abs/1405.4402）的时候我决定用Go，而不是Scala。原因有二：

    JVM程序dockerization的成本太高了：每个docker container里得安装JVM和标准库，花掉几百MB。一台机器如果跑100个container，一个机群100台机器，一个月要为此给Google Compute Engine或者Amazon AWS多付多少钱？程序员可以不算账，老板可不行。我在“在未来，Go语言能否撼动Java在Android、Hadoop大数据、云计算领域的地位？ - 王益的回答”里也提到这个问题。不用container技术可以吗？这可以做另外一个问题讨论了。只提醒一点：Docker用的Linux kernel cgroup系统调用是2007年Google Borg的开发者贡献给Linus的，而Google Borg是Google MapReduce框架的代码量只有Hadoop的百分之一，而功能却强大得多的根本原因。更详细的讨论请见这里：分布式机器学习的故事：Docker改变世界 - Occam's Razor - 知乎专栏。
    Scala的并发语法（和其他很多想法）直接借鉴于functional programming languages学术研究成果，不够贴近工程需要。12年前，我的同学王垠教了我DrScheme（现在叫做Racket了）。这是MIT开发的计算机系本科生的启蒙语言。其中有一种语法叫future，也就是Scala里支持并发的语法。Future是1972年就写进书里了《google.com 的页面》的。它要求一个并发单元最后会返回，并且要返回一个值。这个要求很符合pure functional programming（程序里除了IO不允许有side effect）的调调，但是不符合实际工程工作需要——我要起一个并发单元执行一个Web server，这事儿显然就没有什么返回值。Go的goroutine显然更务实——不需要返回什么值。那结果怎么传回来呢？用channel啊。实际上，goroutine + channel可以完全复现future语法：只需要把一个future定义为一个返回channel的goroutine即可——代码行数都和Scala一样。请看这里的例子：http://www.golangpatterns.info/concurrency/futures。

Peacock现在已经在腾讯广告系统和其他产品里应用。它的训练系统运行在数百台机器上，一个任务里的并发单元以百万计。很多大规模并行机器学习系统的并发规模都如此甚至更大，包括Google Machine Translation背后的language model training system，以及广告点击率预估系统（请参见：Research Blog: Lessons learned developing a practical large scale machine learning system）。在这样的架构下，上述两个因素就成了决定性因素了。

回到主题。我不确定Scala任何时候都比Go好用，但是上述实践让我基本明白，大规模并行系统的开发还是用Go比较现实。虽然我看到很多朋友说JVM语言的“生态好”，但是恐怕这很快就会随着Docker、etcd、CoreOS和Kubernetes引导开源社区而变成过去式了，所以我不敢把“生态好”当做一个重要因素来验证Scala更适合开发大规模并发系统。




###### 到底什么是函数式编程思维？
[传送](http://www.zhihu.com/topic/19585411)

编程范式
函数式编程是一种编程范式，我们常见的编程范式有命令式编程（Imperative programming），函数式编程，逻辑式编程，常见的面向对象编程是也是一种命令式编程。

命令式编程是面向计算机硬件的抽象，有变量（对应着存储单元），赋值语句（获取，存储指令），表达式（内存引用和算术运算）和控制语句（跳转指令），一句话，命令式程序就是一个冯诺依曼机的指令序列。

而函数式编程是面向数学的抽象，将计算描述为一种表达式求值，一句话，函数式程序就是一个表达式。

函数式编程的本质
函数式编程中的函数这个术语不是指计算机中的函数（实际上是Subroutine），而是指数学中的函数，即自变量的映射。也就是说一个函数的值仅决定于函数参数的值，不依赖其他状态。比如sqrt(x)函数计算x的平方根，只要x不变，不论什么时候调用，调用几次，值都是不变的。

在函数式语言中，函数作为一等公民，可以在任何地方定义，在函数内或函数外，可以作为函数的参数和返回值，可以对函数进行组合。

纯函数式编程语言中的变量也不是命令式编程语言中的变量，即存储状态的单元，而是代数中的变量，即一个值的名称。变量的值是不可变的（immutable），也就是说不允许像命令式编程语言中那样多次给一个变量赋值。比如说在命令式编程语言我们写“x = x + 1”，这依赖可变状态的事实，拿给程序员看说是对的，但拿给数学家看，却被认为这个等式为假。

函数式语言的如条件语句，循环语句也不是命令式编程语言中的控制语句，而是函数的语法糖，比如在Scala语言中，if else不是语句而是三元运算符，是有返回值的。

严格意义上的函数式编程意味着不使用可变的变量，赋值，循环和其他命令式控制结构进行编程。

从理论上说，函数式语言也不是通过冯诺伊曼体系结构的机器上运行的，而是通过λ演算来运行的，就是通过变量替换的方式进行，变量替换为其值或表达式，函数也替换为其表达式，并根据运算符进行计算。λ演算是图灵完全（Turing completeness）的，但是大多数情况，函数式程序还是被编译成（冯诺依曼机的）机器语言的指令执行的。

函数式编程的好处
由于命令式编程语言也可以通过类似函数指针的方式来实现高阶函数，函数式的最主要的好处主要是不可变性带来的。没有可变的状态，函数就是引用透明（Referential transparency）的和没有副作用（No Side Effect）。

一个好处是，函数即不依赖外部的状态也不修改外部的状态，函数调用的结果不依赖调用的时间和位置，这样写的代码容易进行推理，不容易出错。这使得单元测试和调试都更容易。

不变性带来的另一个好处是：由于（多个线程之间）不共享状态，不会造成资源争用(Race condition)，也就不需要用锁来保护可变状态，也就不会出现死锁，这样可以更好地并发起来，尤其是在对称多处理器（SMP）架构下能够更好地利用多个处理器（核）提供的并行处理能力。

2005年以来，计算机计算能力的增长已经不依赖CPU主频的增长，而是依赖CPU核数的增多，如图：
（图片来源：The Free Lunch Is Over: A Fundamental Turn Toward Concurrency in Software ）

图中深蓝色的曲线是时钟周期的增长，可以看到从2005年前已经趋于平缓。 在多核或多处理器的环境下的程序设计是很困难的，难点就是在于共享的可变状态。在这一背景下，这个好处就有非常重要的意义。

由于函数是引用透明的，以及函数式编程不像命令式编程那样关注执行步骤，这个系统提供了优化函数式程序的空间，包括惰性求值和并性处理。

还有一个好处是，由于函数式语言是面向数学的抽象，更接近人的语言，而不是机器语言，代码会比较简洁，也更容易被理解。

函数式编程的特性
由于变量值是不可变的，对于值的操作并不是修改原来的值，而是修改新产生的值，原来的值保持不便。例如一个Point类，其moveBy方法不是改变已有Point实例的x和y坐标值，而是返回一个新的Point实例。

class Point(x: Int, y: Int){
    override def toString() = "Point (" + x + ", " + y + ")"

    def moveBy(deltaX: Int, deltaY: Int) = {
        new Point(x + deltaX, y + deltaY)
    }
} 

（示例来源：Anders Hejlsberg在echDays 2010上的演讲）

同样由于变量不可变，纯函数编程语言无法实现循环，这是因为For循环使用可变的状态作为计数器，而While循环或DoWhile循环需要可变的状态作为跳出循环的条件。因此在函数式语言里就只能使用递归来解决迭代问题，这使得函数式编程严重依赖递归。

通常来说，算法都有递推（iterative）和递归（recursive）两种定义，以阶乘为例，阶乘的递推定义为：
而阶乘的递归定义
递推定义的计算时需要使用一个累积器保存每个迭代的中间计算结果，Java代码如下：

public static int fact(int n){
  int acc = 1;

  for(int k = 1; k <= n; k++){
    acc = acc * k;
  }

  return acc;
}

而递归定义的计算的Scala代码如下：

def fact(n: Int):Int= {
  if(n == 0) return 1
  n * fact(n-1)
}

我们可以看到，没有使用循环，没有使用可变的状态，函数更短小，不需要显示地使用累积器保存中间计算结果，而是使用参数n（在栈上分配）来保存中间计算结果。
（示例来源：1. Recursion）

当然，这样的递归调用有更高的开销和局限（调用栈深度），那么尽量把递归写成尾递归的方式，编译器会自动优化为循环，这里就不展开介绍了。

一般来说，递归这种方式于循环相比被认为是更符合人的思维的，即告诉机器做什么，而不是告诉机器怎么做。递归还是有很强大的表现力的，比如换零钱问题。

问题：假设某国的货币有若干面值，现给一张大面值的货币要兑换成零钱，问有多少种兑换方式。
递归解法：

def countChange(money: Int, coins: List[Int]): Int = { 
  if (money == 0) 
    1 
  else if (coins.size == 0 || money < 0) 
    0 
  else 
    countChange(money, coins.tail) + countChange(money - coins.head, coins) 
}

（示例来源：有趣的 Scala 语言: 使用递归的方式去思考）
从这个例子可以看出，函数式程序非常简练，描述做什么，而不是怎么做。

函数式语言当然还少不了以下特性：

    高阶函数（Higher-order function）
    偏应用函数（Partially Applied Functions）
    柯里化（Currying）
    闭包（Closure）


高阶函数就是参数为函数或返回值为函数的函数。有了高阶函数，就可以将复用的粒度降低到函数级别，相对于面向对象语言，复用的粒度更低。

举例来说，假设有如下的三个函数，

def sumInts(a: Int, b: Int): Int =
  if (a > b) 0 else a + sumInts(a + 1, b)

def sumCubes(a: Int, b: Int): Int =
  if (a > b) 0 else cube(a) + sumCubes(a + 1, b)

def sumFactorials(a: Int, b: Int): Int =
  if (a > b) 0 else fact(a) + sumFactorials(a + 1, b)

分别是求a到b之间整数之和，求a到b之间整数的立方和，求a到b之间整数的阶乘和。

其实这三个函数都是以下公式的特殊情况
\sum_{n=a}^{b}{f(n)}
三个函数不同的只是其中的f不同，那么是否可以抽象出一个共同的模式呢？

我们可以定义一个高阶函数sum：

def sum(f: Int => Int, a: Int, b: Int): Int =
  if (a > b) 0
  else f(a) + sum(f, a + 1, b)

其中参数f是一个函数，在函数中调用f函数进行计算，并进行求和。

然后就可以写如下的函数

def sumInts(a: Int, b: Int) = sum(id, a, b)
def sumCubs(a: Int, b: Int) = sum(cube, a, b)
def sumFactorials(a: Int, b: Int) = sum(fact, a, b)

def id(x: Int): Int = x
def cube(x: Int): Int = x * x * x
def fact(x: Int): Int = if (x == 0) 1 else fact(x - 1)

这样就可以重用sum函数来实现三个函数中的求和逻辑。
（示例来源：https://d396qusza40orc.cloudfront.net/progfun/lecture_slides/week2-2.pdf）

高阶函数提供了一种函数级别上的依赖注入（或反转控制）机制，在上面的例子里，sum函数的逻辑依赖于注入进来的函数的逻辑。很多GoF设计模式都可以用高阶函数来实现，如Visitor，Strategy，Decorator等。比如Visitor模式就可以用集合类的map()或foreach()高阶函数来替代。

函数式语言通常提供非常强大的集合类（Collection），提供很多高阶函数，因此使用非常方便。

比如说，我们想对一个列表中的每个整数乘2，在命令式编程中需要通过循环，然后对每一个元素乘2，但是在函数式编程中，我们不需要使用循环，只需要使用如下代码：

scala> val numbers = List(1, 2, 3, 4)
numbers: List[Int] = List(1, 2, 3, 4)

scala> numbers.map(x=>x*2)
res3: List[Int] = List(2, 4, 6, 8)

（示例来源：Programming Scala: Tackle Multi-Core Complexity on the Java Virtual Machine一书的Introduction）

其中x=>x*2是一个匿名函数，接收一个参数x，输出x*2。这里也可以看出来函数式编程关注做什么（x*2），而不关注怎么做（使用循环控制结构）。程序员完全不关心，列表中的元素是从前到后依次计算的，还是从后到前依次计算的，是顺序计算的，还是并行进行的计算，如Scala的并行集合（Parallel collection）。

使用集合类的方法，可以使对一些处理更简单，例如上面提到的求阶乘的函数，如果使用集合类，就可以写成：

def fact(n: Int): Int = (1 to n).reduceLeft((acc,k)=>acc*k)

其中(1 to n)生成一个整数序列，而reduceLeft()高阶函数通过调用匿名函数将序列化简。

那么，在大数据处理框架Spark中，一个RDD就是一个集合。以词频统计的为例代码如下：

val file = spark.textFile("hdfs://...")
val counts = file.flatMap(line => line.split(" "))
                 .map(word => (word, 1))
                 .reduceByKey(_ + _)
counts.saveAsTextFile("hdfs://...")

（示例来源：https://spark.apache.org/examples.html）

示例里的flatMap()，map()，和集合类中的同名方法是一致的，这里的map方法的参数也是一个匿名函数，将单词变成一个元组。写这个函数的人不用关心函数是怎么调度的，而实际上，Spark框架会在多台计算机组成的分布式集群上完成这个计算。

此外，如果对比一下Hadoop的词频统计实现：WordCount - Hadoop Wiki ，就可以看出函数式编程的一些优势。

函数式编程语言还提供惰性求值（Lazy evaluation，也称作call-by-need），是在将表达式赋值给变量（或称作绑定）时并不计算表达式的值，而在变量第一次被使用时才进行计算。这样就可以通过避免不必要的求值提升性能。在Scala里，通过lazy val来指定一个变量是惰性求值的，如下面的示例所示：

scala> val x = { println("x"); 15 }
x
x: Int = 15

scala> lazy val y = { println("y"); 13 }
y: Int = <lazy>

scala> y
y
res3: Int = 13

scala> y
res4: Int = 13

（示例来源：scala - What does a lazy val do?）

可以看到，在Scala的解释器中，当定义了x变量时就打印出了“x”，而定义变量y时并没有打印出”y“，而是在第一次引用变量y时才打印出来。

函数式编程语言一般还提供强大的模式匹配（Pattern Match）功能。在函数式编程语言中可以定义代数数据类型（Algebraic data type），通过组合已有的数据类型形成新的数据类型，如在Scala中提供case class，代数数据类型的值可以通过模式匹配进行分析。

总结
函数式编程是给软件开发者提供的另一套工具箱，为我们提供了另外一种抽象和思考的方式。

函数式编程也有不太擅长的场合，比如处理可变状态和处理IO，要么引入可变变量，要么通过Monad来进行封装（如State Monad和IO Monad） 





















