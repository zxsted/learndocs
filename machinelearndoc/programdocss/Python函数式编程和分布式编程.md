Python函数式编程

[传送门][1]

[toc]
前言：

  这大概算是Python最难啃的一块骨头吧。在我Python生涯的这一年里，我遇到了一些Pythoner，他们毫无例外地完全不会使用函数式编程（有些人喜欢称为Pythonic），比如，从来不会传递函数，不知道lambda是什么意思，知道列表展开但从来不知道用在哪里，对Python不提供经典for循环感到无所适从，言谈之中表现出对函数式风格的一种抗拒甚至厌恶。

  我尝试剖析这个问题，最终总结了这么两个原因：1、不想改变，认为现有的知识可以完成任务；2、对小众语言的歧视，Python目前在国内市场份额仍然很小很小，熟悉Python风格用处不大。

  然而我认为，学习使用一种截然不同的风格可以颠覆整个编程的思想。我会慢慢总结一个系列共4篇文字，篇幅都不大，轻松就能看完，希望对喜欢Python的人们有所帮助，因为我个人确实从中受益匪浅。

  还是那句老话，尊重作者的劳动，转载请注明原作者和原地址：）


#####1.函数式编程概述
######1.1.什么是函数式编程
函数式编程使用一系列的函数解决问题。函数仅接受输入并产生输出，不包含任何能影响产生输出的内部状态。任何情况下，使用相同的参数调用函数始终能产生同样的结果。

在一个函数式的程序中，输入的数据“流过”一系列的函数，每一个函数根据它的输入产生输出。函数式风格避免编写有“边界效应”(side effects)的函数：修改内部状态，或者是其他无法反应在输出上的变化。完全没有边界效应的函数被称为“纯函数式的”(purely functional)。避免边界效应意味着不使用在程序运行时可变的数据结构，输出只依赖于输入。

可以认为函数式编程刚好站在了面向对象编程的对立面。对象通常包含内部状态（字段），和许多能修改这些状态的函数，程序则由不断修改状态构成；函数式编程则极力避免状态改动，并通过在函数间传递数据流进行工作。但这并不是说无法同时使用函数式编程和面向对象编程，事实上，复杂的系统一般会采用面向对象技术建模，但混合使用函数式风格还能让你额外享受函数式风格的优点。

1.2. 为什么使用函数式编程？

函数式的风格通常被认为有如下优点：

*  逻辑可证 
   这是一个学术上的优点：没有边界效应使得更容易从逻辑上证明程序是正确的（而不是通过测试）。
* 模块化 
    函数式编程推崇简单原则，一个函数只做一件事情，将大的功能拆分成尽可能小的模块。小的函数更易于阅读和检查错误。
*   组件化 
    小的函数更容易加以组合形成新的功能。
*   易于调试 
    细化的、定义清晰的函数使得调试更加简单。当程序不正常运行时，每一个函数都是检查数据是否正确的接口，能更快速地排除没有问题的代码，定位到出现问题的地方。
*   易于测试 
    不依赖于系统状态的函数无须在测试前构造测试桩，使得编写单元测试更加容易。
*   更高的生产率 
    函数式编程产生的代码比其他技术更少（往往是其他技术的一半左右），并且更容易阅读和维护。
1.3.如何辨认函数式编程风格？
支持函数式编程的语言通常具有如下特征，大量使用这些特征的代码即可被认为是函数式的：

 * 函数是一等公民 
    函数能作为参数传递，或者是作为返回值返回。这个特性使得模板方法模式非常易于编写，这也促使了这个模式被更频繁地使用。 
    以一个简单的集合排序为例，假设lst是一个数集，并拥有一个排序方法sort需要将如何确定顺序作为参数。 
    如果函数不能作为参数，那么lst的sort方法只能接受普通对象作为参数。这样一来我们需要首先定义一个接口，然后定义一个实现该接口的类，最后将该类的一个实例传给sort方法，由sort调用这个实例的compare方法，就像这样： 
    
    ```shell
    #伪代码
    interface Comparator {
    	compare(01,02);
    }
    
    lst = list(range(5))
    
    lst.sort(Comparator(){
    	compare(o1,o2) {
        	return o2 - o1;   //逆序
        }
    })
    ```
可见，我们定义了一个新的接口、新的类型（这里是一个匿名类），并new了一个新的对象只为了调用一个方法。如果这个方法可以直接作为参数传递会怎样呢？看起来应该像这样：

    ```python
    def compare(o1,o2):
        return o2 - o1   #逆序

    lst = list(range(5))
    lst.sort(compare)
	```
请注意，前一段代码已经使用了匿名类技巧从而省下了不少代码，但仍然不如直接传递函数简单、自然

* 匿名函数（lambda）
	lambda提供了快速编写简单函数的能力。对于偶尔为之的行为，lambda让你不再需要在编码时跳转到其他位置去编写函数。 
lambda表达式定义一个匿名的函数，如果这个函数仅在编码的位置使用到，你可以现场定义、直接使用： 
```python
  lst.sort(lambda o1,o2 : o1.compareTo(o2))
```
相信从这个小小的例子你也能感受到强大的生产效率：）
* 封装控制结构的内置模板函数 
为了避开边界效应，函数式风格尽量避免使用变量，而仅仅为了控制流程而定义的循环变量和流程中产生的临时变量无疑是最需要避免的。 
假如我们需要对刚才的数集进行过滤得到所有的正数，使用指令式风格的代码应该像是这样：
```python
lst2 = list()
for i in range(len(lst)):  #经典的for循环
	if lst[i] > 0:
    	lst2.append(lst[i])
```
这段代码把从创建新列表、循环、取出元素、判断、添加至新列表的整个流程完整的展示了出来，俨然把解释器当成了需要手把手指导的傻瓜。然而，“过滤”这个动作是很常见的，为什么解释器不能掌握过滤的流程，而我们只需要告诉它过滤规则呢？ 
在Python里，过滤由一个名为filter的内置函数实现。有了这个函数，解释器就学会了如何“过滤”，而我们只需要把规则告诉它： 
```python
lst2 = filter(lambda n:n>0,lst)
```
这个函数带来的好处不仅仅是少写了几行代码这么简单。 
封装控制结构后，代码中就只需要描述功能而不是做法，这样的代码更清晰，更可读。因为避开了控制结构的干扰，第二段代码显然能让你更容易了解它的意图。 
另外，因为避开了索引，使得代码中不太可能触发下标越界这种异常，除非你手动制造一个。 
函数式编程语言通常封装了数个类似“过滤”这样的常见动作作为模板函数。唯一的缺点是这些函数需要少量的学习成本，但这绝对不能掩盖使用它们带来的好处。
* 闭包（closure）
闭包是绑定了外部作用域的变量（但不是全局变量）的函数。大部分情况下外部作用域指的是外部函数。 
闭包包含了自身函数体和所需外部函数中的“变量名的引用”。引用变量名意味着绑定的是变量名，而不是变量实际指向的对象；如果给变量重新赋值，闭包中能访问到的将是新的值。 
闭包使函数更加灵活和强大。即使程序运行至离开外部函数，如果闭包仍然可见，则被绑定的变量仍然有效；每次运行至外部函数，都会重新创建闭包，绑定的变量是不同的，不需要担心在旧的闭包中绑定的变量会被新的值覆盖。 
回到刚才过滤数集的例子。假设过滤条件中的 0 这个边界值不再是固定的，而是由用户控制。如果没有闭包，那么代码必须修改为： 

    ```python
    class greater_than_helper:
        def __init__(self,minval):
            self.minval = minval
        def is_greater_than(self,val):
            return val > self.iminval

    def my_filter(lst,minval):
        helper = greater_than_helper(minval)
        return filter(helper.is_greater_than,lst)
    ```
请注意我们现在已经为过滤功能编写了一个函数my_filter。如你所见，我们需要在别的地方（此例中是类greater_than_helper）持有另一个操作数minval。 
如果支持闭包，因为闭包可以直接使用外部作用域的变量，我们就不再需要greater_than_helper了： 
```python
def my_filter(lst,minval):
	return filter(lambda n: n> minval,lst)
```
可见，闭包在不影响可读性的同时也省下了不少代码量。 
函数式编程语言都提供了对闭包的不同程度的支持。在Python 2.x中，闭包无法修改绑定变量的值，所1有修改绑定变量的行为都被看成新建了一个同名的局部变量并将绑定变量隐藏。Python 3.x中新加入了一个关键字 nonlocal 以支持修改绑定变量。但不管支持程度如何，你始终可以访问（读取）绑定变量。
* 内置的不可变数据结构 
为了避开边界效应，不可变的数据结构是函数式编程中不可或缺的部分。不可变的数据结构保证数据的一致性，极大地降低了排查问题的难度。 
例如，Python中的元组(tuple)就是不可变的，所有对元组的操作都不能改变元组的内容，所有试图修改元组内容的操作都会产生一个异常。 
函数式编程语言一般会提供数据结构的两种版本（可变和不可变），并推荐使用不可变的版本。
* 递归 
递归是另一种取代循环的方法。递归其实是函数式编程很常见的形式，经常可以在一些算法中见到。但之所以放到最后，是因为实际上我们一般很少用到递归。如果一个递归无法被编译器或解释器优化，很容易就会产生栈溢出；另一方面复杂的递归往往让人感觉迷惑，不如循环清晰，所以众多最佳实践均指出使用循环而非递归。 
这一系列短文中都不会关注递归的使用。

#####作为参数
如果你对OOP的模板方法模式很熟悉，相信你能很快速地学会将函数当作参数传递。两者大体是一致的，只是在这里，我们传递的是函数本身而不再是实现了某个接口的对象。 
我们先来给前面定义的求和函数add热热身：
```python
 print add('三角形的树'+ '北极')
```
与加法运算符不同，你一定很惊讶于答案是'三角函数'。这是一个内置的彩蛋...bazinga!

言归正传。我们的客户有一个从0到4的列表：
```python
lst = range(5)   #[0,1,2,3,4]
```
虽然我们在上一小节里给了他一个加法器，但现在他仍然在为如何计算这个列表所有元素的和而苦恼。当然，对我们而言这个任务轻松极了：
```python
amount = 0
for num in lst:
	amount = add(amount,num)
```
这是一段典型的指令式风格的代码，一点问题都没有，肯定可以得到正确的结果。现在，让我们试着用函数式的风格重构一下。

首先可以预见的是求和这个动作是非常常见的，如果我们把这个动作抽象成一个单独的函数，以后需要对另一个列表求和时，就不必再写一遍这个套路了：
```python
def sum_(lst):
	amount = 0
    for num in lst:
    	amount = add(amount,num)
    return amount
    
print sum_(lst)
```
还能继续。sum_函数定义了这样一种流程： 
1. 使用初始值与列表的第一个元素相加； 
2. 使用上一次相加的结果与列表的下一个元素相加； 
3. 重复第二步，直到列表中没有更多元素； 
4. 将最后一次相加的结果返回。

如果现在需要求乘积，我们可以写出类似的流程——只需要把相加换成相乘就可以了：
```python
def multiply(lst):
	product = 1
    for num in lst:
    	product = product * num
    return product
```
除了初始值换成了1以及函数add换成了乘法运算符，其他的代码全部都是冗余的。我们为什么不把这个流程抽象出来，而将加法、乘法或者其他的函数作为参数传入呢？
```shell
def reduce_(function,lst,initial):
	result = initial
    for num in lst:
    	result = function(result,num)
    return result
    
print reduce_(add,lst,0)
```
现在，想要计算出乘积，可以这样做：
```python
  print reduce_(lambda x,y:x*y,lst,1)
```
那么，如果想要利用reduce_找出列表中的最大值，应该怎么做呢？请自行思考：）

虽然有模板方法这样的设计模式，但那样的复杂度往往使人们更情愿到处编写循环。将函数作为参数完全避开了模板方法的复杂度。

Python有一个内建函数reduce，完整实现并扩展了reduce_的功能。本文稍后的部分包含了有用的内建函数的介绍。请注意我们的目的是没有循环，使用函数替代循环是函数式风格区别于指令式风格的最显而易见的特征。

*像Python这样构建于类C语言之上的函数式语言，由于语言本身提供了编写循环代码的能力，内置函数虽然提供函数式编程的接口，但一般在内部还是使用循环实现的。同样的，如果发现内建函数无法满足你的循环需求，不妨也封装它，并提供一个接口。

#####作为返回值
将函数返回通常需要与闭包一起使用（即返回一个闭包）才能发挥威力。我们先看一个函数的定义：
```python
def map_(function,lst):
	result = []
    for item in lst:
    	result.append(function(item))
    return result
```
函数map_封装了最常见的一种迭代：对列表中的每个元素调用一个函数。map_需要一个函数参数，并将每次调用的结果保存在一个列表中返回。这是指令式的做法，当你知道了列表解析(list comprehension)后，会有更好的实现。

这里我们先略过map_的蹩脚实现而只关注它的功能。对于上一节中的lst，你可能发现最后求乘积结果始终是0，因为lst中包含了0。为了让结果看起来足够大，我们来使用map_为lst中的每个元素加1：

```python
lst = map_(lambda x:add(x,x),lst)
print reduce_(lambda x,y:x*y,lst,1)
```
答案是120，这还远远不够大。再来：
```python
lst = map_(lambda x:add(10,x),lst)
print reduce_(lambda x,y:x*y,lst,1)
```
囧，事实上我真的没有想到答案会是360360，我发誓没有收周鸿祎任何好处。

#####偏函数
现在回头看看我们写的两个lambda表达式：相似度超过90%，绝对可以使用抄袭来形容。而问题不在于抄袭，在于多写了很多字符有木有？如果有一个函数，根据你指定的左操作数，能生成一个加法函数，用起来就像这样：
```python
lst = map_(add_to(10),lst)  # add_to(10)返回一个函数，这个函数接受一个参数并加上10后返回
```
写起来应该会舒服不少，下面是add_to的实现：
```python
def add_to(n):
	return x=add(n,x)
```
通过为已经存在的某个函数指定数个参数，生成一个新的函数，这个函数只需要传入剩余未指定的参数就能实现原函数的全部功能，这被称为偏函数。Python内置的functools模块提供了一个函数partial，可以为任意函数生成偏函数：
```python
functools.partial(func[,*args][,**keywords])
```
你需要指定要生成偏函数的函数、并且指定数个参数或者命名参数，然后partial将返回这个偏函数；不过严格的说partial返回的不是函数，而是一个像函数一样可直接调用的对象，当然，这不会影响它的功能。

*题外话，单就例子中的这个功能而言，在一些其他的函数式语言中（例如Scala）可以使用名为柯里化(Currying)的技术实现得更优雅。柯里化是把接受多个参数的函数变换成接受一个单一参数（最初函数的第一个参数）的函数，并且返回接受余下的参数而且返回结果的新函数的技术。如下的伪代码所示：
```shell
#伪代码
def add(x)(y): #柯里化
	return x+y
    
lst = map_(add(10),lst)
```
通过将add函数柯里化，使得add接受第一个参数x，并返回一个接受第二个参数y的函数，调用该函数与前文中的add_to完全相同（返回x + y），且不再需要定义add_to。看上去是不是更加清爽呢？遗憾的是Python并不支持柯里化。

#####内置函数介绍
* reduce(function,iterable[,initializer])
这个函数的主要功能与我们定义的reduce_相同。需要补充两点： 
它的第二个参数可以是任何可迭代的对象（实现了__iter__()方法的对象）； 
如果不指定第三个参数，则第一次调用function将使用iterable的前两个元素作为参数。 
由reduce和一些常见的function组合成了下面列出来的内置函数： 
```python
all(iterable) == reduce(lambda x,y:bool(x and y),iterable)
any(iterable) == reduce(lambda x,y:bool(x or y),iterable)
max(iterable[,args...][,key]) == reduce(lambda x,y: x if key(x) > key(y) else y,iterable_and_args)
min(iterable[,args...][,key]) == reduce(lambda x,y:x if key(x) < key(y) else y,iterable_and_args)
sum(iterable[,start]) == reduce(lambda x,y:x+y,iterable,start)
```
* map(function,iterable,...)
这个函数的主要功能与我们定义的map_相同。需要补充一点： 
map还可以接受多个iterable作为参数，在第n次调用function时，将使用iterable1[n], iterable2[n], ...作为参数。

* filter(function,iterable)
这个函数的功能是过滤出iterable中所有以元素自身作为参数调用function时返回True或bool(返回值)为True的元素并以列表返回，与系列第一篇中的my_filter函数相同。

* zip(iterable1,iterable2,...)
     这个函数返回一个列表，每个元素都是一个元组，包含(iterable1[n], iterable2[n], ...)。 
    例如：zip([1, 2], [3, 4]) --> [(1, 3), (2, 4)] 
    如果参数的长度不一致，将在最短的序列结束时结束；如果不提供参数，将返回空列表。

除此之外，你还可以使用本文2.5节中提到的functools.partial()为这些内置函数创建常用的偏函数。

另外，pypi上有一个名为functional的模块，除了这些内建函数外，还额外提供了更多的有意思的函数。但由于使用的场合并不多，并且需要额外安装，在本文中就不介绍了。但我仍然推荐大家下载这个模块的纯Python实现的源代码看看，开阔思维嘛。里面的函数都非常短，源文件总共只有300行不到，地址在这里：http://pypi.python.org/pypi/functional



#####装饰器
另外一个特殊的例子是装饰器。装饰器用于增强甚至干脆改变原函数的功能:


#####迭代器
这一篇我们将讨论迭代器。迭代器并不是函数式编程特有的东西，但它仍然是函数式编程的一个重要的组成部分，或者说是一个重要的工具。

######3.1. 迭代器(Iterator)概述

迭代器是访问集合内元素的一种方式。迭代器对象从集合的第一个元素开始访问，直到所有的元素都被访问一遍后结束。

迭代器不能回退，只能往前进行迭代。这并不是什么很大的缺点，因为人们几乎不需要在迭代途中进行回退操作。

迭代器也不是线程安全的，在多线程环境中对可变集合使用迭代器是一个危险的操作。但如果小心谨慎，或者干脆贯彻函数式思想坚持使用不可变的集合，那这也不是什么大问题。

对于原生支持随机访问的数据结构（如tuple、list），迭代器和经典for循环的索引访问相比并无优势，反而丢失了索引值（可以使用内建函数enumerate()找回这个索引值，这是后话）。但对于无法随机访问的数据结构（比如set）而言，迭代器是唯一的访问元素的方式。

迭代器的另一个优点就是它不要求你事先准备好整个迭代过程中所有的元素。迭代器仅仅在迭代至某个元素时才计算该元素，而在这之前或之后，元素可以不存在或者被销毁。这个特点使得它特别适合用于遍历一些巨大的或是无限的集合，比如几个G的文件，或是斐波那契数列等等。这个特点被称为延迟计算或惰性求值(Lazy evaluation)。

迭代器更大的功劳是提供了一个统一的访问集合的接口。只要是实现了__iter__()方法的对象，就可以使用迭代器进行访问。

######3.2. 使用迭代器

使用内建的工厂函数iter(iterable)可以获取迭代器对象：
```shell
lst = range(2)
it = iter(lst)
it
<listiterator object at 0x00BB62F0>
```
使用迭代器的next()方法可以访问下一个元素：
```python
>>> it.next()
0
```
如果是Python 2.6+，还有内建函数next(iterator)可以完成这一功能：
```python
>>> next(it)
1
```
如何判断迭代器还有更多的元素可以访问呢？Python里的迭代器并没有提供类似has_next()这样的方法。 
那么在这个例子中，我们已经访问到了最后一个元素1，再使用next()方法会怎样呢？

```python	
>>> it.next()
Traceback (most recent call last):
  File "<stdin>", line 1, in <module>
StopIteration
```
Python遇到这样的情况时将会抛出StopIteration异常。事实上，Python正是根据是否检查到这个异常来决定是否停止迭代的。 
这种做法与迭代前手动检查是否越界相比各有优点。但Python的做法总有一些利用异常进行流程控制的嫌疑。

了解了这些情况以后，我们就能使用迭代器进行遍历了。
```python
it = iter(lst)
try:
	while True:
    	val = it.next()
        print val
except StopIteration:
	pass
```
实际上，因为迭代操作如此普遍，Python专门将关键字for用作了迭代器的语法糖。在for循环中，Python将自动调用工厂函数iter()获得迭代器，自动调用next()获取元素，还完成了检查StopIteration异常的工作。上述代码可以写成如下的形式，你一定非常熟悉：
```python
for val in lst:
	print val
```
首先Python将对关键字in后的对象调用iter函数获取迭代器，然后调用迭代器的next方法获取元素，直到抛出StopIteration异常。对迭代器调用iter函数时将返回迭代器自身，所以迭代器也可以用于for语句中，不需要特殊处理。

常用的几个内建数据结构tuple、list、set、dict都支持迭代器，字符串也可以使用迭代操作。你也可以自己实现一个迭代器，如上所述，只需要在类的__iter__方法中返回一个对象，这个对象拥有一个next()方法，这个方法能在恰当的时候抛出StopIteration异常即可。但是需要自己实现迭代器的时候不多，即使需要，使用生成器会更轻松。下一篇我们将讨论生成器的部分。

*异常并不是非抛出不可的，不抛出该异常的迭代器将进行无限迭代，某些情况下这样的迭代器很有用。这种情况下，你需要自己判断元素并中止，否则就死循环了！

#####序列对象的索引 enumerate
使用迭代器的循环可以避开索引，但有时候我们还是需要索引来进行一些操作的。这时候内建函数enumerate就派上用场咯，它能在iter函数的结果前加上索引，以元组返回，用起来就像这样：

```python
 for idx,ele in enumerate(lst):
 	print idx,ele
```

#####3.3 生成器表达式(Generator expression)  和 列表解析(List Comprehension)
绝大多数情况下，遍历一个集合都是为了对元素应用某个动作或是进行筛选。如果看过本文的第二部分，你应该还记得有内建函数map和filter提供了这些功能，但Python仍然为这些操作提供了语言级的支持。
```shell
(x+1 for x in lst)  # 生成器表达式，返回迭代器，外部的括号可在用于参数时省略
[x+1 for x in lst]  # 列表解析，返回list
```
如你所见，生成器表达式和列表解析（注：这里的翻译有很多种，比如列表展开、列表推导等等，指的是同一个意思）的区别很小，所以人们提到这个特性时，简单起见往往只描述成列表解析。然而由于返回迭代器时，并不是在一开始就计算所有的元素，这样能得到更多的灵活性并且可以避开很多不必要的计算，所以除非你明确希望返回列表，否则应该始终使用生成器表达式。接下来的文字里我就不区分这两种形式了：）
可以为列表解析提供if子句进行筛选：
```python
(x+1 for x in lst if x!=0)
```
或者提供多条for子句进行嵌套循环，嵌套的次序就是for子句的次序
```python
((x,y) for x in range(3) for y in range(x))
```
列表解析就是鲜明的Pythonic。我常遇到两个使用列表解析的问题，本应归属于最佳实践，但这两个问题非常典型，所以不妨在这里提一下：

第一个问题是，因为对元素应用的动作太复杂，不能用一个表达式写出来，所以不使用列表解析。这是典型的思想没有转变的例子，如果我们将动作封装成函数，那不就是一个表达式了么？

第二个问题是，因为if子句里的条件需要计算，同时结果也需要进行同样的计算，不希望计算两遍，就像这样：
```python
(x.doSomething() for x in lst if x.doSomething() > 0)
```
这样写确实很糟糕，但组合一下列表解析即可解决
(x for x in (y.doSomething() for y in lst) if x > 0)

内部的列表解析变量其实也可以使用x，但是为清晰起见我们改成了y，为了清晰，也可以写成两个表达式：
```python
 tmp = (x.doSomething() for x in lst)
 (x for x in tmp if x > 0)
```
列表解析可以替代绝大多数需要用到map和filter的场合，可能正因为此，著名的静态检查工具pylint将map和filter的使用列为了警告。

######3.4 相关的库
Python内置了一个模块itertools，包含了很多函数用于creating iterators for efficient looping（创建更有效率的循环迭代器），这说明很是霸气，这一小节就来浏览一遍这些函数并留下印象吧，需要这些功能的时候隐约记得这里面有就好。这一小节的内容翻译自itertools模块官方文档。

3.4.1. 无限迭代

   * count(start, [step]) 
    从start开始，以后每个元素都加上step。step默认值为1。 
   * count(10) --> 10 11 12 13 14 ...
   * cycle(p) 
    迭代至序列p的最后一个元素后，从p的第一个元素重新开始。 
    cycle('ABCD') --> A B C D A B C D ...
   * repeat(elem [,n]) 
    将elem重复n次。如果不指定n，则无限重复。 
   * repeat(10, 3) --> 10 10 10

3.4.2. 在最短的序列参数终止时停止迭代

   * chain(p, q, ...) 
    迭代至序列p的最后一个元素后，从q的第一个元素开始，直到所有序列终止。 
    chain('ABC', 'DEF') --> A B C D E F
   * compress(data, selectors) 
    如果bool(selectors[n])为True，则next()返回data[n]，否则跳过data[n]。 
    compress('ABCDEF', [1,0,1,0,1,1]) --> A C E F
   * dropwhile(pred, seq) 
    当pred对seq[n]的调用返回False时才开始迭代。 
    dropwhile(lambda x: x<5, [1,4,6,4,1]) --> 6 4 1
   * takewhile(pred, seq) 
    dropwhile的相反版本。 
    takewhile(lambda x: x<5, [1,4,6,4,1]) --> 1 4
   * ifilter(pred, seq) 
    内建函数filter的迭代器版本。 
    ifilter(lambda x: x%2, range(10)) --> 1 3 5 7 9
    ifilterfalse(pred, seq) 
    ifilter的相反版本。 
    ifilterfalse(lambda x: x%2, range(10)) --> 0 2 4 6 8
   * imap(func, p, q, ...) 
    内建函数map的迭代器版本。 
    imap(pow, (2,3,10), (5,2,3)) --> 32 9 1000
   * starmap(func, seq) 
    将seq的每个元素以变长参数(*args)的形式调用func。 
    starmap(pow, [(2,5), (3,2), (10,3)]) --> 32 9 1000
   * izip(p, q, ...) 
    内建函数zip的迭代器版本。 
    izip('ABCD', 'xy') --> Ax By
    izip_longest(p, q, ..., fillvalue=None) 
    izip的取最长序列的版本，短序列将填入fillvalue。 
    izip_longest('ABCD', 'xy', fillvalue='-') --> Ax By C- D-
   * tee(it, n) 
    返回n个迭代器it的复制迭代器。
   * groupby(iterable[, keyfunc]) 
    这个函数功能类似于SQL的分组。使用groupby前，首先需要使用相同的keyfunc对iterable进行排序，比如调用内建的sorted函数。然后，groupby返回迭代器，每次迭代的元素是元组(key值, iterable中具有相同key值的元素的集合的子迭代器)。或许看看Python的排序指南对理解这个函数有帮助。 
    groupby([0, 0, 0, 1, 1, 1, 2, 2, 2]) --> (0, (0 0 0)) (1, (1 1 1)) (2, (2 2 2))

3.4.3. 组合迭代器

   * product(p, q, ... [repeat=1]) 
    笛卡尔积。 
    product('ABCD', repeat=2) --> AA AB AC AD BA BB BC BD CA CB CC CD DA DB DC DD
  * permutations(p[, r]) 
    去除重复的元素。 
    permutations('ABCD', 2) --> AB AC AD BA BC BD CA CB CD DA DB DC
  * combinations(p, r) 
    排序后去除重复的元素。 
    combinations('ABCD', 2) --> AB AC AD BC BD CD
  * combinations_with_replacement() 
    排序后，包含重复元素。 
    combinations_with_replacement('ABCD', 2) --> AA AB AC AD BB BC BD CC CD DD

#####生成器

生成器是迭代器，同时也并不仅仅是迭代器，不过迭代器之外的用途实在是不多，所以我们可以大声地说：生成器提供了非常方便的自定义迭代器的途径。

######4.1. 生成器简介

首先请确信，生成器就是一种迭代器。生成器拥有next方法并且行为与迭代器完全相同，这意味着生成器也可以用于Python的for循环中。另外，对于生成器的特殊语法支持使得编写一个生成器比自定义一个常规的迭代器要简单不少，所以生成器也是最常用到的特性之一。

从Python 2.5开始，[PEP 342：通过增强生成器实现协同程序]的实现为生成器加入了更多的特性，这意味着生成器还可以完成更多的工作。这部分我们会在稍后的部分介绍。
4.2. 生成器函数
4.2.1. 使用生成器函数定义生成器

如何获取一个生成器？首先来看一小段代码：
```python
def get_0_1_2():
	yield 0
    yield 1
    yield 2
```
我们定义了一个函数get_0_1_2，并且可以查看到这确实是函数类型。但与一般的函数不同的是，get_0_1_2的函数体内使用了关键字yield，这使得get_0_1_2成为了一个生成器函数。生成器函数的特性如下：

* 调用生成器函数将返回一个生成器
```python
>>> generator = get_0_1_2()
>>> generator
<generator object get_0_1_2 at 0x00B1C7D8>
```
* 第一次调用生成器的next方法时，生成器才开始执行生成器函数（而不是构建生成器时），直到遇到yield时暂停执行（挂起），并且yield的参数将作为此次next方法的返回值；
```python	
>>> generator.next()
0
```
* 之后每次调用生成器的next方法，生成器将从上次暂停执行的位置恢复执行生成器函数，直到再次遇到yield时暂停，并且同样的，yield的参数将作为next方法的返回值；
```python
>>> generator.next()
1
>>> generator.next()
2
```
如果当调用next方法时生成器函数结束（遇到空的return语句或是到达函数体末尾），则这次next方法的调用将抛出StopIteration异常（即for循环的终止条件）；
```python
>>> generator.next()
Traceback (most recent call last):
  File "<stdin>", line 1, in <module>
StopIteration
```
* 生成器函数在每次暂停执行时，函数体内的所有变量都将被封存(freeze)在生成器中，并将在恢复执行时还原，并且类似于闭包，即使是同一个生成器函数返回的生成器，封存的变量也是互相独立的。 
我们的小例子中并没有用到变量，所以这里另外定义一个生成器来展示这个特点： 

    ```python
    def fibonacci():
        a=b=1
        yield a
        yield b
        while True:
            a,b = b,a+b
            yeild b

    for num in fibonacci():
        if num > 100 : break
        print num

     1 1 2 3 5 8 13 21 34 55 89
    ```
看到while True可别太吃惊，因为生成器可以挂起，所以是延迟计算的，无限循环并没有关系。这个例子中我们定义了一个生成器用于获取斐波那契数列。

######4.2.2. 生成器函数的FAQ

接下来我们来讨论一些关于生成器的有意思的话题。

*   你的例子里生成器函数都没有参数，那么生成器函数可以带参数吗？ 
    当然可以啊亲，而且它支持函数的所有参数形式。要知道生成器函数也是函数的一种：）
    ```python
    def counter(start = 0):
    	while True:
        	yield start
            start += 1
    ```
    这是一个从指定数开始的计数器
* 	既然生成器函数也是函数，那么它可以使用return输出返回值吗？ 
不行的亲，是这样的，生成器函数已经有默认的返回值——生成器了，你不能再另外给一个返回值；对，即使是return None也不行。但是它可以使用空的return语句结束。如果你坚持要为它指定返回值，那么Python将在定义的位置赠送一个语法错误异常，就像这样： 
  ```python
   def i_wanna_return():
   	yield None
   	return None
    
  File "<stdin>", line 3
  SyntaxError: 'return' with argument inside generator
  ```
  
* 好吧，那人家需要确保释放资源，需要在try...finally中yield，这会是神马情况？（我就是想玩你）我在finally中还yield了一次！
  ```python
   def play_u():
   	try:
    	yield 1
        yield 2
        yield 3
    finally:
    	yield 0
        
   for val in play_u(): print val
   
   1 2 3 0
  ```
  * 这与return的情况不同。return是真正的离开代码块，所以会在return时立刻执行finally子句。 
  * 另外，“在带有finally子句的try块中yield”定义在PEP 342中，这意味着只有Python 2.5以上版本才支持这个语法，在Python 2.4以下版本中会得到语法错误异常。
* 如果我需要在生成器的迭代过程中接入另一个生成器的迭代怎么办？写成下面这样好傻好天真。。
```python
 def sub_generator():
	yield 1
	yield 2
    for val in counter(10) : yield val
```
这种情况的语法改进已经被定义在[PEP 380：委托至子生成器的语法]中，据说会在Python 3.3中实现，届时也可能回馈到2.x中。实现后，就可以这么写了：
```python
def sub_generator():
	yield 1
    yield 2
    yield from counter(10)
```

######4.3 协同程序(coroutine)
协同程序（协程）一般来说是指这样的函数：

   * 彼此间有不同的局部变量、指令指针，但仍共享全局变量；
   * 可以方便地挂起、恢复，并且有多个入口点和出口点；
   * 多个协同程序间表现为协作运行，如A的运行过程中需要B的结果才能继续执行。

协程的特点决定了同一时刻只能有一个协同程序正在运行（忽略多线程的情况）。得益于此，协程间可以直接传递对象而不需要考虑资源锁、或是直接唤醒其他协程而不需要主动休眠，就像是内置了锁的线程。在符合协程特点的应用场景，使用协程无疑比使用线程要更方便。

从另一方面说，协程无法并发其实也将它的应用场景限制在了一个很狭窄的范围，这个特点使得协程更多的被拿来与常规函数进行比较，而不是与线程。当然，线程比协程复杂许多，功能也更强大，所以我建议大家牢牢地掌握线程即可：Python线程指南

这一节里我也就不列举关于协程的例子了，以下介绍的方法了解即可。

Python 2.5对生成器的增强实现了协程的其他特点，在这个版本中，生成器加入了如下方法：

1    send(value): 
   send是除next外另一个恢复生成器的方法。Python 2.5中，yield语句变成了yield表达式，这意味着yield现在可以有一个值，而这个值就是在生成器的send方法被调用从而恢复执行时，调用send方法的参数。 
```python
def repeater():
	n = 0
    while True:
    	n = (yield n)
        
r = repeater()
r.next()
0
r.send(10)
10
```
  * 调用send传入非None值前，生成器必须处于挂起状态，否则将抛出异常。不过，未启动的生成器仍可以使用None作为参数调用send。 
  * 如果使用next恢复生成器，yield表达式的值将是None。
2. close()
 这个方法用于关闭生成器。对关闭的生成器后再次调用next或send将抛出StopIteration异常。
3. throw(type,value=None,tracebacl=None):
 这个方法用于在生成器内部（生成器的当前挂起处，或未启动时在定义处）抛出一个异常。

* 别为没见到协程的例子遗憾，协程最常见的用处其实就是生成器。

######4.4一个有趣的库 : PIPE
这一节里我要向诸位简要介绍pipe。pipe并不是Python内置的库，如果你安装了easy_install，直接可以安装它，否则你需要自己下载它：http://pypi.python.org/pypi/pipe

之所以要介绍这个库，是因为它向我们展示了一种很有新意的使用迭代器和生成器的方式：流。pipe将可迭代的数据看成是流，类似于linux，pipe使用'|'传递数据流，并且定义了一系列的“流处理”函数用于接受并处理数据流，并最终再次输出数据流或者是将数据流归纳得到一个结果。我们来看一些例子。

* 第一个，非常简单的，使用add求和：

    ```python
    from pipe import * 
    range(5) | add

    10
    ```
* 求偶数和需要使用到where ，作用类似使用 内建函数filter，过滤出符合条件的元素：
    
    ```python
    range(5) | where(lambda x: x % 2 == 0) | add

    6
    ```
* 求出数列中所有小于10000的偶数和需要用到take_while，与itertools的同名函数有类似的功能，截取元素直到条件不成立

    ```python
    fib = fibonacci
    fib() | where(lambda x: x % 2 == 0) \
          | take_while(lambda x:x < 10000) \
          | add

    3382
    ```
* 需要对元素应用某个函数可以使用select，作用类似于内建函数map；需要得到一个列表，可以使用as_list：

    ```python
    fib() | select(lambda x: x ** 2)\
          | take_while(lambda x: x < 100 ) \
          | as_list

    [1, 1, 4, 9, 25, 64]
    ```

pipe中还包括了更多的流处理函数。你甚至可以自己定义流处理函数，只需要定义一个生成器函数并加上修饰器Pipe。如下定义了一个获取元素直到索引不符合条件的流处理函数：

```python
@Pipe
def take_while_idx(iterable,predicate):
	for idx, x in enumerate(iterable):
    	if predicate(idx) : 
        	yield x
        else:
        	return
```
使用者个流处理函数获取fib的前10 个数字：
```python
 fib() \
 |table_while_idx(lambda x : x < 10) \
 | as_list
 
 [1, 1, 2, 3, 5, 8, 13, 21, 34, 55]
```
更多的函数就不在这里介绍了，你可以查看pipe的源文件，总共600行不到的文件其中有300行是文档，文档中包含了大量的示例。

pipe实现起来非常简单，使用Pipe装饰器，将普通的生成器函数（或者返回迭代器的函数）代理在一个实现了__ror__方法的普通类实例上即可，但是这种思路真的很有趣。
    


#####装饰器
装饰器(decorator)是一种高级Python语法。装饰器可以对一个函数、方法或者类进行加工。在Python中，我们有多种方法对函数和类进行加工，比如在Python闭包中，我们见到函数对象作为某一个函数的返回结果。相对于其它方式，装饰器语法简单，代码可读性高。因此，装饰器在Python项目中有广泛的应用。

装饰器最早在Python 2.5中出现，它最初被用于加工函数和方法这样的可调用对象(callable object，这样的对象定义有__call__方法)。在Python 2.6以及之后的Python版本中，装饰器被进一步用于加工类。

######装饰函数和方法
 我们先定义两个简单的数学函数，一个用来计算平方和，一个用来计算平方差：
 ```python
 def square_sum(a,b):
 	return a**2 + b**2
    
 def square_diff(a,b):
 	return a**2 - b**2
    
print(square_sum(3,4))
print(square_diff(3,4))
 ```
在拥有了基本的数学功能之后，我们可能想为函数增加其它的功能，比如打印输入。我们可以改写函数来实现这一点：
```python
# get square sum
def square_sum(a, b):
    print("intput:", a, b)
    return a**2 + b**2

# get square diff
def square_diff(a, b):
    print("input", a, b)
    return a**2 - b**2

print(square_sum(3, 4))
print(square_diff(3, 4))

```
我们修改了函数的定义，为函数增加了功能。

现在，我们要使用装饰器来实现上述功能的修改：
```python
def decorator(F):
	def new_F(a,b):
    	print("input",a,b)
        return F(a,b)
    return new_F
    
@decorator
def square_sum(a,b):
	return a**2 + b**2
    
@decorator
def square_diff(a,b):
	return a**2 - b**2
    
print(square_sum(3, 4))
print(square_diff(3, 4))
```

装饰器可以用def的形式定义，如上面代码中的decorator。装饰器接收一个可调用对象作为输入参数，并返回一个新的可调用对象。装饰器新建了一个可调用对象，也就是上面的new_F。new_F中，我们增加了打印的功能，并通过调用F(a, b)来实现原有函数的功能。

定义好装饰器后，我们就可以通过@语法使用了。在函数square_sum和square_diff定义之前调用@decorator，我们实际上将square_sum或square_diff传递给decorator，并将decorator返回的新的可调用对象赋给原来的函数名(square_sum或square_diff)。 所以，当我们调用square_sum(3, 4)的时候，就相当于：
```python
square_sum = decorator(square_sum)
square_sum(3, 4)
```
我们知道，Python中的变量名和对象是分离的。变量名可以指向任意一个对象。从本质上，装饰器起到的就是这样一个重新指向变量名的作用(name binding)，让同一个变量名指向一个新返回的可调用对象，从而达到修改可调用对象的目的。

与加工函数类似，我们可以使用装饰器加工类的方法。

如果我们有其他的类似函数，我们可以继续调用decorator来修饰函数，而不用重复修改函数或者增加新的封装。这样，我们就提高了程序的可重复利用性，并增加了程序的可读性。

######含参的装饰器
在上面的装饰器调用中，比如@decorator，该装饰器默认它后面的函数是唯一的参数。装饰器的语法允许我们调用decorator时，提供其它参数，比如@decorator(a)。这样，就为装饰器的编写和使用提供了更大的灵活性。
```python
def pre_str(pre=''):
	# old decorator
    def decorator(F):
    	def new_F(a,b):
        	print(pre + "input",a,b)
            return F(a,b)
        return new_F
    return decorator
    

@pre_str('^_^')
def square_sum(a,b):
	return a**2 - b**2
    
@pre_str('T_T')
def square_diff(a,b):
	return a**2 - b**2
    
print(square_sum(3, 4))
print(square_diff(3, 4))
```
上面的pre_str是允许参数的装饰器。它实际上是对原有装饰器的一个函数封装，并返回一个装饰器。我们可以将它理解为一个含有环境参量的闭包。当我们使用@pre_str('^_^')调用的时候，Python能够发现这一层的封装，并把参数传递到装饰器的环境中。该调用相当于:
```python
square_sum = pre_str('^_^') (square_sum)
```

######装饰器类
在上面的例子中，装饰器接收一个函数，并返回一个函数，从而起到加工函数的效果。在Python 2.6以后，装饰器被拓展到类。一个装饰器可以接收一个类，并返回一个类，从而起到加工类的效果。
```python
def decorator(aClass):
	class newClass:
    	def __init__(self,age):
        	self.total_display = 0
            self.wrapped = aClass(age)
        def display(self):
        	self.total_display += 1
            print("total display" ,self.total_display())
            self.wrapped.display()
    return newClass
    
@decorator
class Bird:
	def __init__(self,age):
    	self.age = age
    def display(self):
    	print("My age is " ，self.age)
        
eagleLord = Bird(5)
for i in range(3):
	eagLord.display()
```

在decorator中，我们返回了一个新类newClass。在新类中，我们记录了原来类生成的对象（self.wrapped），并附加了新的属性total_display，用于记录调用display的次数。我们也同时更改了display方法。

通过修改，我们的Bird类可以显示调用display的次数了。

#####另一个版本的装饰器解释
由于函数也是一个对象，而且函数对象可以被赋值给变量，所以，通过变量也能调用该函数。
```python
>>> def now():
...     print '2013-12-25'
...
>>> f = now
>>> f()
2013-12-25
```
函数对象有一个__name__属性，可以拿到函数的名字：
```python
>>> now.__name__
'now'
>>> f.__name__
'now'
```
现在，假设我们要增强now()函数的功能，比如，在函数调用前后自动打印日志，但又不希望修改now()函数的定义，这种在代码运行期间动态增加功能的方式，称之为“装饰器”（Decorator）。

本质上，decorator就是一个返回函数的高阶函数。所以，我们要定义一个能打印日志的decorator，可以定义如下：
```python
def log(func):
	def wrapper(*args,**kw):
    	print 'call %s():' % func.__name__
        return func(*argc,**kw)
    return wrapper
```
观察上面的log，因为它是一个decorator，所以接受一个函数作为参数，并返回一个函数。我们要借助Python的@语法，把decorator置于函数的定义处：

```python
@log
def now():
	print '2013-12-25'
```
调用now()函数，不仅会运行now()函数本身，还会在运行now()函数前打印一行日志：
```python
>>> now()
call now():
2013-12-25
```
把@log放到now()函数的定义处，相当于执行了语句：
```python
now = log(now)
```
由于log()是一个decorator，返回一个函数，所以，原来的now()函数仍然存在，只是现在同名的now变量指向了新的函数，于是调用now()将执行新函数，即在log()函数中返回的wrapper()函数。

wrapper()函数的参数定义是(*args, **kw)，因此，wrapper()函数可以接受任意参数的调用。在wrapper()函数内，首先打印日志，再紧接着调用原始函数。

如果decorator本身需要传入参数，那就需要编写一个返回decorator的高阶函数，写出来会更复杂。比如，要自定义log的文本：
```python
def log(text):
	def decorator(func):
    	def wrapper(*args,**kw):
        	print '%s %s()' % (text,func.__name__)
            turn func(*args,**kw)
        return wrappe
    return decorator
```

这个3层的decorator的用法如下：
```python
@log('execute')
def now():
	print '2013-12-25'
```
执行结果如下：
```python
>>> now()
execute now():
2013-12-25
```
和两层嵌套的decorator相比，3层嵌套的效果是这样的：

>>> now = log('execute')(now)

我们来剖析上面的语句，首先执行log('execute')，返回的是decorator函数，再调用返回的函数，参数是now函数，返回值最终是wrapper函数。

以上两种decorator的定义都没有问题，但还差最后一步。因为我们讲了函数也是对象，它有__name__等属性，但你去看经过decorator装饰之后的函数，它们的__name__已经从原来的'now'变成了'wrapper'：
```python
>>> now.__name__
'wrapper'
```
因为返回的那个wrapper()函数名字就是'wrapper'，所以，需要把原始函数的__name__等属性复制到wrapper()函数中，否则，有些依赖函数签名的代码执行就会出错。

不需要编写wrapper.__name__ = func.__name__这样的代码，Python内置的functools.wraps就是干这个事的，所以，一个完整的decorator的写法如下：
```python
import functools

def log(func)
	@functools.wraps(func)
    def wrapper(*args,**kw):
    	print 'call %s():' % func.__name__
        return func(*args，**kw)
    return wrapper
```

或者针对代参数的decorator：
```python
import functools

def log(text):
	def decorator(func):
    	@functools.wraps(func)
        def wrapper(*args,**kw):
        	print '%s %s()' % (text,func.__name__)
            return func(*args,**kw)
        return wrapper
     return decorator
```
import functools是导入functools模块。模块的概念稍候讲解。现在，只需记住在定义wrapper()的前面加上@functools.wraps(func)即可。
小结

在面向对象（OOP）的设计模式中，decorator被称为装饰模式。OOP的装饰模式需要通过继承和组合来实现，而Python除了能支持OOP的decorator外，直接从语法层次支持decorator。Python的decorator可以用函数实现，也可以用类实现。

decorator可以增强函数的功能，定义起来虽然有点复杂，但使用起来非常灵活和方便。

#####彩蛋 ：分布式进程
在Thread和Process中，应当优选Process，因为Process更稳定，而且，Process可以分布到多台机器上，而Thread最多只能分布到同一台机器的多个CPU上。

Python的multiprocessing模块不但支持多进程，其中managers子模块还支持把多进程分布到多台机器上。一个服务进程可以作为调度者，将任务分布到其他多个进程中，依靠网络通信。由于managers模块封装很好，不必了解网络通信的细节，就可以很容易地编写分布式多进程程序。

举个例子：如果我们已经有一个通过Queue通信的多进程程序在同一台机器上运行，现在，由于处理任务的进程任务繁重，希望把发送任务的进程和处理任务的进程分布到两台机器上。怎么用分布式进程实现？

原有的Queue可以继续使用，但是，通过managers模块把Queue通过网络暴露出去，就可以让其他机器的进程访问Queue了。

我们先看服务进程，服务进程负责启动Queue，把Queue注册到网络上，然后往Queue里面写入任务：

```python
#filename : taskmanager.py

import random,time,Queue
from nultiprocessing.managers import BaseManager


#发送任务的队列：
task_queue =  Queue.Queue()
#接收结果的队列
result_queue = Queue.Queue()

#BaseManager 继承的Queueanager:
class QueueManager(BaseManager):
	pass

#将两个Q都注册到网络上，Callable参数关联了Queue对象：
QueueManager.register('get_task_queue',callable=lambda: tesk_queue)
QueueManager.register('get_result_queue',callable=lambda:result_queue)

#绑定端口5000，设置验证码'abc':
manager = QueueManager(address=('',5000),authkey='abc')
#启动Queue:
manager.start()

#获得通过网络访问的Queue对象：
task = manager.get_task_queue()
result = manager.get_result_queue()

#放几个任务进去
for i in range(10):
	n = random.randint(0,10000)
    print('Put task %d ...' % n)
    task.put(n)
    
#从 result 队列中读取结果
print("Try get results ...")
for i in range(10):
	r = result.get(timeout=10)
    print('Result: %s ' % r)
    
#关闭
manager.shutdown()
```
请注意，当我们在一台机器上写多进程程序时，创建的Queue可以直接拿来使用，但是在分布式多进程环境下，添加任务到Queue 不可以直接对原始的task_queue进行操作，那样就绕过了 QueueManager的封装，必须通过manager.get_task_queue() 获得的Queue 接口添加。

然后在另一台机器上启动任务进程（本机上启动也可以）

```shell
# taskworker.py

import time,sys.Queue
from multiprocessing.managers import BaseManager

# 创建类似的QueueManager：
class QueueManager(BaseManager):
	pass

#由于这个Queueanager只从网络上获取Queue ,所以注册时只提供名字：
QueueManager.register('get_task_queue')
QueueManager.register('get_result_queue')

#连接到服务器，也就是运行taskmanager.py的机器
server_addr = '127.0.0.1'
print('Connect to server %s ...' % server_addr)
#端口和验证码注意保持与taskmanager.py设置的完全一致
m = QueueManager(address=(server_addr,5000),authkey='abc')
#从网络连接：
m.connect()
#获取Queue的对象：
task = m.get_task_queue()
result = m.get_result_queue()

#从task队列获取任务，并将结果写入result队列：
for i in range(10):
	try:
    	n = task.get(timeout=1)
        print('run task %d * %d ...' % (n,n))
        r = '%d * %d = %d' % (n,n,n*n)
        time.sleep(1)
        result.put(r)
    except Qeueue.Empty:
    	print('task queue is empty.')
        
#处理结束
print('worker exit.')
```
任务进程要通过网络链接到服务器进程，所以要指定服务进程的IP

现在，可以试试分布式进程的工作效果了。先启动taskmanager.py服务进程：
```shell
$ python taskmanager.py 
Put task 3411...
Put task 1605...
Put task 1398...
Put task 4729...
Put task 5300...
Put task 7471...
Put task 68...
Put task 4219...
Put task 339...
Put task 7866...
Try get results...
```
taskmanager进程发送完任务后，开始等待result队列的结果。现在启动taskworker.py进程：
```shell
$ python taskworker.py 127.0.0.1
Connect to server 127.0.0.1...
run task 3411 * 3411...
run task 1605 * 1605...
run task 1398 * 1398...
run task 4729 * 4729...
run task 5300 * 5300...
run task 7471 * 7471...
run task 68 * 68...
run task 4219 * 4219...
run task 339 * 339...
run task 7866 * 7866...
worker exit.
```

taskworker进程结束，在taskmanager进程中会继续打印出结果：
```shell
Result: 3411 * 3411 = 11634921
Result: 1605 * 1605 = 2576025
Result: 1398 * 1398 = 1954404
Result: 4729 * 4729 = 22363441
Result: 5300 * 5300 = 28090000
Result: 7471 * 7471 = 55815841
Result: 68 * 68 = 4624
Result: 4219 * 4219 = 17799961
Result: 339 * 339 = 114921
Result: 7866 * 7866 = 61873956
```
这个简单的Manager/Worker模型有什么用？其实这就是一个简单但真正的分布式计算，把代码稍加改造，启动多个worker，就可以把任务分布到几台甚至几十台机器上，比如把计算n*n的代码换成发送邮件，就实现了邮件队列的异步发送。

Queue对象存储在哪？注意到taskworker.py中根本没有创建Queue的代码，所以，Queue对象存储在taskmanager.py进程中：
![image][2]

而Queue之所以能通过网络访问，就是通过QueueManager实现的。由于QueueManager管理的不止一个Queue，所以，要给每个Queue的网络调用接口起个名字，比如get_task_queue。

authkey有什么用？这是为了保证两台机器正常通信，不被其他机器恶意干扰。如果taskworker.py的authkey和taskmanager.py的authkey不一致，肯定连接不上。

小结

Python的分布式进程接口简单，封装良好，适合需要把繁重任务分布到多台机器的环境下。

注意Queue的作用是用来传递任务和接收结果，每个任务的描述数据量要尽量小。比如发送一个处理日志文件的任务，就不要发送几百兆的日志文件本身，而是发送日志文件存放的完整路径，由Worker进程再去共享的磁盘上读取文件。






























[1]:http://www.cnblogs.com/linyawen/archive/2012/04/01/2428569.html
[2]:http://www.liaoxuefeng.com/files/attachments/0014075825932657f3abfe7667143db934a4d34f464aeb5000


