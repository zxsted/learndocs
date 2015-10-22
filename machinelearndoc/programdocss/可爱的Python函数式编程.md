可爱的Python函数式编程（一）
2013-03-04 09:47 fbm oschina 字号：T | T
一键收藏，随时查看，分享好友！

我们最好从艰难的问题开始出发：“到底什么是函数化编程呢？”其中一个答案可能是这样的，函数化编程就是你在使用Lisp这样的语言时所做的（还有Scheme，Haskell，ML，OCAML，Mercury，Erlang和其他一些语言）。

AD：2014WOT全球软件技术峰会北京站 课程视频发布

摘要：虽然人们总把Python当作过程化的，面向对象的语言，但是他实际上包含了函数化编程中，你需要的任何东西。这篇文章主要讨论函数化编程的一般概念，并说明用Python来函数化编程的技术。

我们最好从艰难的问题开始出发：“到底什么是函数化编程呢？”其中一个答案可能是这样的，函数化编程就是你在使用Lisp这样的语言时所做的（还有Scheme，Haskell，ML，OCAML，Mercury，Erlang和其他一些语言）。这是一个保险的回答，但是它解释得并不清晰。不幸的是对于什么是函数化编程，很难能有一个协调一致的定义，即使是从函数化变成本身出发，也很难说明。这点倒很像盲人摸象。不过，把它拿来和命令式编程（imperative programming）做比较也不错（命令式编程就像你在用C，Pascal，C++，Java，Perl，Awk，TCL和很多其他类似语言时所做的，至少大部分一样 ）。

我个人粗略总结了一下，认为函数式编程至少应该具有下列几点中的多个特点。在谓之为函数式的语言中，要做到这些就比较容易，但要做到其它一些事情不是很难就是完全不可能：

·函数具有首要地位 (对象)。也就是说，能对“数据”做什么事，就要能对函数本身做到那些事（比如将函数作为参数传递给另外一个函数）。

·将递归作为主要的控制结构。在有些函数式语言中，都不存在其它的“循环”结构。

·列表处理作为一个重点（例如，Lisp语言的名字）。列表往往是通过对子列表进行递归取代了循环。

·“纯”函数式语言会完全避免副作用。这么做就完全弃绝了命令式语言中几乎无处不在的这种做法：将第一个值赋给一个变量之后为了跟踪程序的运行状态，接着又将另外一个值赋给同一个变量。

·函数式编程不是不鼓励就是完全禁止使用语句，而是通过对表达式(换句话说，就是函数加上参数）求值（evaluation of expressions）完成任务. 在最纯粹的情形下，一个程序就是一个表达式（再加上辅助性的定义）

·函数式编程中最关心的是要对什么进行计算，而不是要怎么来进行计算。

·在很多函数式编程语言中都会用到“高阶”（higher order）函数 (换句话说，高阶函数就是对对函数进行运算的函数进行运算的函数）。

函数式编程的倡导者们认为，所有这些特性都有助于更快地编写出更多更简洁并且更不容易出Bug的代码。而且，计算机科学、逻辑学和数学这三个领域中的高级理论家发现，函数式编程语言和程序的形式化特性在证明起来比命令式编程语言和程序要简单很多。

Python内在的函数式功能

自Python 1.0起，Python就已具有了以上所列中的绝大多数特点。但是就象Python所具有的大多数特性一样，这些特点出现在了一种混合了各种特性的语言中。 和Python的OOP（面向对象编程） 特性非常象，你想用多少就用多少，剩下的都可以不管（直到你随后需要用到它们为止）。在Python 2.0中，加入了列表解析（list comprehensions）这个非常好用的”语法糖“。 尽管列表解析没有添加什么新功能，但它让很多旧功能看起来好了不少。

Python中函数式编程的基本要素包括functionsmap()、reduce()、filter()和lambda算子（operator）。 在Python 1.x中，apply()函数也可以非常方便地拿来将一个函数的列表返回值直接用于另外一个函数。Python 2.0为此提供了一个改进后的语法。可能有点让人惊奇，使用如此之少的函数（以及基本的算子）几乎就足以写出任何Python程序了；更加特别的是，几乎用不着什么执行流程控制语句。

所有(if,elif,else,assert,try,except,finally,for,break,continue,while,def)这些都都能通过仅仅使用函数式编程中的函数和算子就能以函数式编程的风格处理好。尽管真正地在程序中完全排除使用所有流程控制命令可能只在想参加”Python混乱编程“大赛（可将Python代码写得跟Lisp代码非常象）时才有意义，但这对理解函数式编程如何通过函数和递归表达流程控制很有价值。

剔除流程控制语句

剔除练习首先要考虑的第一件事是，实际上，Python会对布尔表达式求值进行“短路”处理。这就为我们提供了一个if/elif/else分支语句的表达式版（假设每个分支只调用一个函数，不是这种情况时也很容易组织成重新安排成这种情况）。 这里给出怎么做：

对Python中的条件调用进行短路处理

    # Normal statement-based flow control  
    if <cond1>:   func1()  
    elif <cond2>: func2()  
    else:         func3()  
     
    # Equivalent "short circuit" expression  
    (<cond1> and func1()) or (<cond2> and func2()) or (func3())  
     
    # Example "short circuit" expression  
    >>> x = 3 
    >>> def pr(s): return s  
    >>> (x==1 and pr('one')) or (x==2 and pr('two')) or (pr('other'))  
    'other' 
    >>> x = 2 
    >>> (x==1 and pr('one')) or (x==2 and pr('two')) or (pr('other'))  
    'two' 

我们的表达式版本的条件调用看上去可能不算什么，更象是个小把戏；然而，如果我们注意到lambda算子必须返回一个表达式，这就更值得关注了。既然如我们所示，表达式能够通过短路包含一个条件判断，那么，lambda表达式就是个完全通用的表达条件判断返回值的手段了。我们来一个例子：

Python中短路的Lambda

    >>> pr = lambda s:s  
    >>> namenum = lambda x: (x==1 and pr("one")) \  
    ....                  or (x==2 and pr("two")) \  
    ....                  or (pr("other"))  
    >>> namenum(1)  
    'one' 
    >>> namenum(2)  
    'two' 
    >>> namenum(3)  
    'other' 

将函数作为具有首要地位的对象

前面的例子已经表明了Python中函数具有首要地位，但有点委婉。当我们用lambda操作创建一个函数对象时， 我们所得到的东西是完全通用的。就其本质而言，我们可以将我们的对象同名字"pr"和"namenum"绑定到一起, 以完全相同的方式，我们也也完全可以将数字23或者字符串"spam" 同这些名字绑定到一起。但是，就象我们可以无需将其绑定到任何名字之上就能直接使用数字23（也就是说，它可以用作函数的参数）一样，我们也可以直接使用我们使用lambda创建的函数对象，而无需将其绑定到任何名字之上。在Python中，函数就是另外一种我们能够就像某种处理的值。

我们对具有首要地位的对象做的比较多的事情就是，将它们作为参数传递给函数式编程固有的函数map()、reduce()和filter()。这三个函数接受的第一个参数都是一个函数对象。

·map()针对指定给它的一个或多个列表中每一项对应的内容，执行一次作为参数传递给它的那个函数 ，最后返回一个结果列表。

·reduce()针对每个后继项以及最后结果的累积结果，执行一次作为参数传递给它的那个函数；例如，reduce(lambda n,m:n*m, range(1,10))是求"10的阶乘"的意思（换言之，将每一项和前面所得的乘积进行相乘）

·filter()使用那个作为参数传递给它的函数，对一个列表中的所有项进行”求值“，返回一个由所有能够通过那个函数测试的项组成的经过遴选后的列表。

我们经常也会把函数对象传递给我们自己定义的函数，不过一般情况下这些自定义的函数就是前文提及的内建函数的某种形式的组合。

通过组合使用这三种函数式编程内建的函数， 能够实现范围惊人的“执行流程”操作(全都不用语句，仅仅使用表达式实现)。

Python中的函数式循环

替换循环语言和条件状态语言块同样简单。for可以直接翻译成map()函数。正如我们的条件执行，我们会需要简化语句块成简单的函数调用（我们正在接近通常能做的）：

替换循环

    for e in lst:  func(e)      # statement-based loop  
    map(func,lst)           # map()-based loop 

通过这种方法，对有序程序流将有一个相似的函数式方式。那就是，命令式编程几乎是由大量“做这，然后做那，之后做其它的”语句组成。map()让我们只要做这样：

Map-based 动作序列

    # let's create an execution utility function  
    do_it = lambda f: f()  
     
    # let f1, f2, f3 (etc) be functions that perform actions  
     
    map(do_it, [f1,f2,f3])   # map()-based action sequence 

通常，我们的整个主要的程序都可以使用一个map表达式加上一些函数列表的执行来完成这个程序。最高级别的函数的另一个方便的特性是你可以把它们放在一个列表里。

翻译while会稍稍复杂一些，但仍然可以直接地完成：

Python中的函数式"while"循环

    # statement-based while loop  
    while <cond>:  
        <pre-suite>  
        if <break_condition>:  
            break 
        else:  
            <suite>  
     
    # FP-style recursive while loop  
    def while_block():  
        <pre-suite>  
        if <break_condition>:  
            return 1 
        else:  
            <suite>  
        return 0 
     
    while_FP = lambda: (<cond> and while_block()) or while_FP()  
    while_FP() 

在翻译while循环时，我们仍然需要使用while_block()函数，这个函数本身里面可以包含语句而不是仅仅包含表达式。但我们可能还能够对这个函数再进行更进一步的剔除过程（就像前面模版中的对if/else进行短路处理一样）。 还有，<cond>很难对普通的测试有什么用，比如while myvar==7，既然循环体（在设计上）不能对任何变量的值进行修改（当然，在while_block()中可以修改全局变量）。有一种方法可以用来为 while_block()添加更有用的条件判断，让 while_block()返回一个有意义的值，然后将这个返回值同循环结束条件进行比较。现在应该来看一个剔除其中语句的具体例子了：

Python中'echo'循环

    # imperative version of "echo()"  
    def echo_IMP():  
        while 1:  
            x = raw_input("IMP -- ")  
            if x == 'quit':  
                break 
            else 
                print x  
    echo_IMP()  
     
    # utility function for "identity with side-effect"  
    def monadic_print(x):  
        print x  
        return x  
     
    # FP version of "echo()"  
    echo_FP = lambda: monadic_print(raw_input("FP -- "))=='quit' or echo_FP()  
    echo_FP() 

在上面的例子中我们所做的，就是想办法将一个涉及I/O、循环和条件判断的小程序，表达为一个递归方式的纯粹的表达式 （确切地说，表达为一个可以在需要的情况下传递到别的地方的函数对象）。我们 的确仍然使用了实用函数monadic_print()，但这个函数是完全通用的，而且可以用于以后我们可能会创建的每个函数式程序的表达式中（它的代价是一次性的）。请注意，任何包含monadic_print(x)的表达式的 值都是一样的，好像它只是包含了一个x而已。函数式编程中（特别是在Haskell中）的函数有一种叫做"monad"（一元）的概念，这种一元函数“实际什么都不做，只是在执行过程中产生一个副作用”。

避免副作用

在做完这些没有非常明智的理由陈述，并把晦涩的嵌套表达式代替他们之后，一个很自然的问题是“为什么要这样做？！”　我描述的函数式编程在Python中都实现了。但是最重要的特性和一个有具体用处——就是避免副作用（或至少它们阻止如monads的特殊区域）。程序错误的大部分——并且这些问题驱使程序员去debug——出现是因为在程序的运行中变量获取了非期望的值。函数式编程简单地通过从不给变量赋值而绕过了这个问题。

现在让我们看一段非常普通的命令式代码。这段代码的目的是打印出乘积大于25的一对一对数字所组成的一个列表。组成每对数字的每一个数字都是取自另外的两个列表。这种事情和很多程序员在他们的编程中经常做的一些事情比较相似。命令式的解决方式有可能就象下面这样：

命令式的"打印大乘积"的Python代码

    # Nested loop procedural style for finding big products  
    xs = (1,2,3,4)  
    ys = (10,15,3,22)  
    bigmuls = []  
    # ...more stuff...  
    for x in xs:  
        for y in ys:  
            # ...more stuff...  
            if x*y > 25:  
                bigmuls.append((x,y))  
                # ...more stuff...  
    # ...more stuff...  
    print bigmuls 

这个项目足够小了，好像没有地方会出什么差错。但有可能在这段代码中我们会嵌入一些同时完成其它任务的代码。用"more stuff"（其它代码）注释掉的部分，就是有可能存在导致出现bug的副作用的地方。在那三部分的任何一点上，变量sxs、ys、bigmuls、x、y都有可能在这段按照理想情况简化后的代码中取得一个出人意料的值。还有，这段代码执行完后，后继代码有可能需要也有可能不需要对所有这些变量中的值有所预期。显而易见，将这段代码封装到函数/实例中，小心处理变量的作用范围，就能够避免这种类型的错误。你也可以总是将使用完毕的变量del掉。但在实践中，这里指出的这种类型的错误很常见。

以一种函数式的途径一举消除这些副作用所产生的错误，这样就达到了我们的目的。一种可能的代码如下：

以函数式途径达到我们的目的

    bigmuls = lambda xs,ys: filter(lambda (x,y):x*y > 25, combine(xs,ys))  
    combine = lambda xs,ys: map(None, xs*len(ys), dupelms(ys,len(xs)))  
    dupelms = lambda lst,n: reduce(lambda s,t:s+t, map(lambda l,n=n: [l]*n, lst))  
    print bigmuls((1,2,3,4),(10,15,3,22)) 

在例子中我们绑定我们的匿名（lambda）函数对象到变量名，但严格意义上讲这并不是必须的。我们可以用简单的嵌套定义来代替之。这不仅是为了代码的可读性，我们才这样做的；而且是因为combine()函数在任何地方都是一个非常好的功能函数（函数从两个输入的列表读入数据生成一个相应的pair列表）。函数dupelms()只是用来辅助函数combine()的。即使这个函数式的例子跟命令式的例子显得要累赘些，不过一旦你考虑到功能函数的重用，则新的bigmuls()中代码就会比命令式的那个要稍少些。

这个函数式例子的真正优点在于：在函数中绝对没有改变变量的值。这样就不可能在之后的代码（或者从之前的代码）中产生不可预期的副作用。显然，在函数中没有副作用，并不能保证代码的正确性，但它仍然是一个优点。无论如何请注意，Python（不像很多其它的函数式语言）不会阻止名字bigmuls，combine和dupelms的再次绑定。如果combine()运行在之后的程序中意味着有所不同时，所有的预测都会失效。你可能会需要新建一个单例类来包含这个不变的绑定（也就是说，s.bigmuls之类的）；但是这一例并没有空间来做这些。

一个明显值得注意的是，我们特定的目标是定制Python 2的一些特性。而不是命令式的或函数式编程的例子，最好的（也是函数式的）方法是：

    print [(x,y) for x in (1,2,3,4) for y in (10,15,3,22) if x*y > 25] 

结束语

我已经列出了把每一个Python控制流替换成一个相等的函数式代码的方法（在程序中减少副作用）。高效翻译一个特定的程序需要一些额外的思考，但我们已经看出内置的函数式功能是全面且完善的。在接下来的文章里，我们会看到更多函数式编程的高级技巧；并且希望我们接下来能够摸索到函数式编程风格的更多优点和缺点。


摘要：本专栏继续让David对Python中的函数式编程(FP)进行介绍。读完本文，可以享受到使用不同的编程范型（paradigm）解决问题所带来的乐趣。David在本文中对FP中的多个中级和高级概念进行了详细的讲解。

一个对象就是附有若干过程（procedure）的一段数据。。。一个闭包（closure）就是附有一段数据的一个过程（procedure）。

在我讲解函数式编程的上一篇文章，第一部分，中，我介绍了FP中的一些基本概念。 本文将更加深入的对这个内容十分丰富的概念领域进行探讨。在我们探讨的大部分内容中，Bryn Keller的"Xoltar Toolkit"为我们提供一些非常有价值的帮助作用。Keller将FP中的许多强项集中到了一个很棒且很小的模块中，他在这个模块中用纯Python代码实现了这些强项。除了functional模块外，Xoltar Toolkit还包含了一个延迟（lazy）模块，对“仅在需要时”才进行求值提供了支持。许多传统的函数式语言中也都具有延迟求值的手段，这样，使用Xoltar Toolkit中的这些组件，你就可以做到使用象Haskell这样的函数式语言能够做到的大部分事情了。

绑定（Binding）

有心的读者会记得，我在第一部分中所述的函数式技术中指出过Python的一个局限。具体讲，就是Python中没有任何手段禁止对用来指代函数式表达式的名字进行重新绑定。 在FP中，名字一般是理解为对比较长的表达式的简称，但这里面隐含了一个诺言，就是“同一个表达式总是具有同一个值”。如果对用来指代的名字重新进行绑定，就会违背这个诺言。例如， 假如我们如以下所示，定义了一些要用在函数式程序中的简记表达式:

Python中由于重新绑定而引起问题的FP编程片段

    >>> car = lambda lst: lst[0]  
    >>> cdr = lambda lst: lst[1:]  
    >>> sum2 = lambda lst: car(lst)+car(cdr(lst))  
    >>> sum2(range(10))  
    1 
    >>> car = lambda lst: lst[2]  
    >>> sum2(range(10))  
    5 

非常不幸，程序中完全相同的表达式sum2(range(10))在两个不同的点求得的值却不相同, 尽管在该表达式的参数中根本没有使用任何可变的（mutable）变量。

幸运的是， functional模块提供了一个叫做Bindings(由鄙人向Keller进行的提议，proposed to Keller by yours truly)的类，可以用来避免这种重新绑定（至少可以避免意外的重新绑定，Python并不阻止任何拿定主意就是要打破规则的程序员）。尽管要用Bindings类就需要使用一些额外的语法，但这么做就能让这种事故不太容易发生。 Keller在functional模块里给出的例子中，有个Bindings的实例名字叫做let（我推测这么叫是为了仿照ML族语言中的let关键字）。例如，我们可以这么做：

Python中对重新绑定进行监视后的FP编程片段

    >>> from functional import *  
    >>> let = Bindings()  
    >>> let.car = lambda lst: lst[0]  
    >>> let.car = lambda lst: lst[2]  
    Traceback (innermost last):  
      File "<stdin>", line 1, in ?  
      File "d:\tools\functional.py", line 976, in __setattr__  
        raise BindingError, "Binding '%s' cannot be modified." % name  
    functional.BindingError:  Binding 'car' cannot be modified.  
    >>> car(range(10))  
    0 

显而易见，在真正的程序中应该去做一些事情，捕获这种"BindingError"异常，但发出这些异常这件事，就能够避免产生这一大类的问题。

functional模块随同Bindings一起还提供了一个叫做namespace的函数，这个函数从Bindings实例中弄出了一个命名空间 (实际就是个字典) 。如果你想计算一个表达式，而该表达式是在定义于一个Bindings中的一个（不可变）命名空间中时，这个函数就可以很方便地拿来使用。Python的eval()函数允许在命名空间中进行求值。举个例子就能说明这一切：

Python中使用不可变命名空间的FP编程片段

    >>> let = Bindings()      # "Real world" function names  
    >>> let.r10 = range(10)  
    >>> let.car = lambda lst: lst[0]  
    >>> let.cdr = lambda lst: lst[1:]  
    >>> eval('car(r10)+car(cdr(r10))', namespace(let))  
    >>> inv = Bindings()      # "Inverted list" function names  
    >>> inv.r10 = let.r10  
    >>> inv.car = lambda lst: lst[-1]  
    >>> inv.cdr = lambda lst: lst[:-1]  
    >>> eval('car(r10)+car(cdr(r10))', namespace(inv))  
    17 

闭包（Closure）

FP中有一个特别有引人关注的概念叫做闭包。实际上，闭包充分引起了很多程序员的关注，即使通常意义上的非函数式编程语言，比如Perl和Ruby，都包含了闭包这一特性。此外，Python 2.1 目前一定会添加上词法域（lexical scoping）， 这样一来就提供的闭包的绝大多数功能。

那么，闭包到底是什么？Steve Majewski最近在Python新闻组中对这个概念的特性提出了一个准确的描述：

就是说，闭包就象是FP的Jekyll，OOP（面向对象编程）的 Hyde （或者可能是将这两个角色互换）（译者注：Jekyll和Hyde是一部小说中的两个人物）. 和象对象实例类似，闭包是一种把一堆数据和一些功能打包一起进行传递的手段。

先让我们后退一小步，看看对象和闭包都能解决一些什么样的问题，然后再看看在两样都不用的情况下这些问题是如何得到解决的。函数返回的值通常是由它在计算过程中使用的上下文决定的。最常见可能也是最显然的指定该上下文的方式就是给函数传递一些参数，让该函数对这些参数进行一些运算。但有时候在参数的“背景”（background）和“前景”（foreground）两者之间也有一种自然的区分，也就是说，函数在某特定时刻正在做什么和函数“被配置”为处于多种可能的调用情况之下这两者之间有不同之处。

在集中处理前景的同时，有多种方式进行背景处理。一种就是“忍辱负重”，每次调用时都将函数需要的每个参数传递给函数。这通常就相对于在函数调用链中不断的将很多值（或者是一个具有很多字段的数据结构）传上传下，就是因为在链中的某个地方可能会用到这些值。下面举个简单的例子：

用了货船变量的Python代码片段

    >>> def a(n):  
    ...     add7 = b(n)  
    ...     return add7  
    ...  
    >>> def b(n):  
    ...     i = 7 
    ...     j = c(i,n)  
    ...     return j  
    ...  
    >>> def c(i,n):  
    ...     return i+n  
    ...  
    >>> a(10)     # Pass cargo value for use downstream  
    17 

在上述的货船变量例子中，函数b()中的变量n毫无意义，就只是为了传递给函数c()。另一种办法是使用全局变量:

使用全局变量的Python代码片段

    >>> N = 10 
    >>> def  addN(i):  
    ...     global N  
    ...     return i+N  
    ...  
    >>> addN(7)   # Add global N to argument  
    17 
    >>> N = 20 
    >>> addN(6)   # Add global N to argument  
    26 

全局变量N只要你想调用ddN()就可以直接使用，就不需要显式地传递这个全局背景“上下文”了。有个稍微更加Python化的技巧，可以用来在定义函数时，通过使用缺省参数将一个变量“冻结”到该函数中：

使用冻结变量的Python代码片段

    >>> N = 10 
    >>> def addN(i, n=N):  
    ...     return i+n  
    ...  
    >>> addN(5)   # Add 10  
    15 
    >>> N = 20 
    >>> addN(6)   # Add 10 (current N doesn't matter)  
    16 

我们冻结的变量实质上就是个闭包。我们将一些数据“附加”到了addN()函数之上。对于一个完整的闭包而言，在函数addN()定义时所出现的数据，应该在该函数被调用时也可以拿到。然而，本例中（以及更多更健壮的例子中），使用缺省参数让足够的数据可用非常简单。函数addN()不再使用的变量因而对计算结构捕获产生丝毫影响。

现在让我们再看一个用OOP的方式解决一个稍微更加现实的问题。今年到了这个时候，让我想起了颇具“面试”风格的计税程序，先收集一些数据，数据不一定有什么特别的顺序，最后使用所有这些数据进行一个计算。让我们为这种情况些个简化版本的程序：

Python风格的计税类/实例

    class TaxCalc:  
        def taxdue(self):return (self.income-self.deduct)*self.rate  
    taxclass = TaxCalc()  
    taxclass.income = 50000 
    taxclass.rate = 0.30 
    taxclass.deduct = 10000 
    print"Pythonic OOP taxes due =", taxclass.taxdue() 

在我们的TaxCalc类 (或者更准确的讲，在它的实例中)，我们先收集了一些数据，数据的顺序随心所欲，然后所有需要的数据收集完成后，我们可以调用这个对象的一个方法，对这堆数据进行计算。所有的一切都呆在一个实例中，而且，不同的实例可以拥有一堆不同的数据。能够创建多个实例，而多个实例仅仅是数据不同，这通过“全局变量”和“冻结变量”这两种方法是无法办到的。"货船"方法能够做到这一点，但从那个展开的例子中我们能够看出，它可能不得不在开始时就传递多个数值。讨论到这里，注意到OOP风格的消息传递方式可能会如何来解决这一问题会非常有趣(Smalltalk或者Self与此类似，我所用过的好几种xBase的变种OOP语言也是类似的）：

Smalltalk风格的(Python) 计税程序

    class TaxCalc:  
        def taxdue(self):return (self.income-self.deduct)*self.rate  
        def setIncome(self,income):  
            self.income = income  
            return self 
        def setDeduct(self,deduct):  
            self.deduct = deduct  
            return self 
        def setRate(self,rate):  
            self.rate = rate  
            return self 
    print"Smalltalk-style taxes due =", \  
          TaxCalc().setIncome(50000).setRate(0.30).setDeduct(10000).taxdue() 

每个"setter"方法都返回self可以让我们将每个方法调用的结果当作“当前”对象进行处理。这和FP中的闭包方式有些相似。

通过使用Xoltar toolkit，我们可以生成完整的闭包，能够将数据和函数结合起来，获得我们所需的特性；另外还可以让多个闭包（以前成为对象）包含不同的数据： 

Python的函数式风格的计税程序

    from functional import *  
     
    taxdue        = lambda: (income-deduct)*rate  
    incomeClosure = lambda income,taxdue: closure(taxdue)  
    deductClosure = lambda deduct,taxdue: closure(taxdue)  
    rateClosure   = lambda rate,taxdue: closure(taxdue)  
     
    taxFP = taxdue  
    taxFP = incomeClosure(50000,taxFP)  
    taxFP = rateClosure(0.30,taxFP)  
    taxFP = deductClosure(10000,taxFP)  
    print"Functional taxes due =",taxFP()  
     
    print"Lisp-style taxes due =", \  
          incomeClosure(50000,  
              rateClosure(0.30,  
                  deductClosure(10000, taxdue)))() 

我们所定义的每个闭包函数可以获取函数定义范围内的任意值，然后将这些值绑定到改函数对象的全局范围之中。然而，一个函数的全局范围并不一定就是真正的模块全局范围，也和不同的闭包的“全局”范围不相同。闭包就是“将数据带”在了身边。

在我们的例子中，我们利用了一些特殊的函数把特定的绑定限定到了一个闭包作用范围之中(income, deduct, rate)。要想修改设计，将任意的绑定限定在闭包之中，也非常简单。只是为了好玩，在本例子中我们也使用了两种稍微不同的函数式风格。第一种风格连续将多个值绑定到了闭包的作用范围；通过允许taxFP成为可变的变量，这些“添加绑定”的代码行可以任意顺序出现。然而，如果我们想要使用tax_with_Income这样的不可变名字，我们就需要以特定的顺序来安排这几行进行绑定的代码，将靠前的绑定结果传递给下一个绑定。无论在哪种情况下，在全部所需数据都绑定进闭包范围之后，我们就可以调用“种子”（seeded）方法了。

第二种风格在我看来，更象是Lisp（那些括号最象了）。除去美学问题，这第二种风格有两点值得注意。第一点就是完全避免了名字绑定，变成了一个单个的表达式，连语句都没有使用（关于为什么不使用语句很重要，请参见 P第一部分）。

第二点是闭包的“Lips”风格的用法和前文给出的“Smalltalk”风格的信息传递何其相似。实际上两者都在调用taxdue()函数/方法的过程中积累了所有值(如果以这种原始的方式拿不到正确的数据，两种方式都会出错）。“Smalltalk”风格的方法中每一步传递的是一个对象，而“Lisp”风格的方法中传递是持续进行的。 但实际上，函数式编程和面向对象式编程两者旗鼓相当。

尾递归

在本文中，我们干掉了函数式编程领域中更多的内容。剩下的要比以前（本小节的题目是个小玩笑；很不幸，这里还没有解释过尾递归的概念）少多了（或者可以证明也简单多了？）。阅读functional模块中的源代码是继续探索FP中大量概念的一种非常好的方法。该模块中的注释很完备，在注释里为模块中的大多数方法/类提供了相关的例子。其中有很多简化性的元函数（meta-function）本专栏里并没有讨论到的，使用这些元函数可以大大简化对其它函数的结合（combination）和交互（interaction ）的处理。对于想继续探索函数式范型的Python程序员而言，这些绝对值得好好看看。



可爱的 Python : Python中的函数式编程，第三部分

2013/03/07 | 分类： Python, 开发 | 0 条评论 | 标签： Python, 函数式编程
分享到： 2

英文原文：Charming Python: Functional programming in Python, Part 3，翻译：开源中国

摘要：  作者David Mertz在其文章《可爱的Python：“Python中的函数式编程”》中的第一部分和第二部分中触及了函数式编程的大量基本概念。本文中他将继续前面的讨论，解释函数式编程的其它功能，如currying和Xoltar Toolkit中的其它一些高阶函数。

表达式绑定

有一位从不满足于解决部分问题读者，名叫Richard Davies，提出了一个问题，问是否可以将所有的绑定全部都转移到一个单个的表达式之中。首先让我们简单看看，我们为什么想这么做，然后再看看由comp.lang.python中的一位朋友提供的一种异常优雅地写表达式的方式。

让我们回想一下功能模块的绑定类。使用该类的特性，我们可以确认在一个给定的范围块内，一个特定的名字仅仅代表了一个唯一的事物。
具有重新绑定向导的 Python 函数式编程(FP)
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
	
>>> from functional import *
>>> let = Bindings()
>>> let.car = lambda lst: lst[0]
>>> let.car = lambda lst: lst[2]
Traceback (innermost last):
  File "<stdin>", line 1, in ?
  File "d:\tools\functional.py", line 976, in __setattr__
 
raise BindingError, "Binding '%s' cannot be modified." % name
functional.BindingError:  Binding 'car' cannot be modified.
>>> let.car(range(10))
0

绑定类在一个模块或者一个功能定义范围内做这些我们希望的事情，但是没有办法在一条表达式内使之工作。然而在ML家族语言(译者注：ML是一种通用的函数式编程语言),在一条表达式内创建绑定是很自然的事。
Haskell 命名绑定表达式
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
	
-- car (x:xs) = x  -- *could* create module-level binding
list_of_list = [[1,2,3],[4,5,6],[7,8,9]]
 
-- 'where' clause for expression-level binding
firsts1 = [car x | x <- list_of_list] where car (x:xs) = x
 
-- 'let' clause for expression-level binding
firsts2 = let car (x:xs) = x in [car x | x <- list_of_list]
 
-- more idiomatic higher-order 'map' technique
firsts3 = map car list_of_list where car (x:xs) = x
 
-- Result: firsts1 == firsts2 == firsts3 == [1,4,7]

Greg Ewing 发现用Python的list概念实现同样的效果是有可能的；甚至我们可以用几乎与Haskell语法一样干净的方式做到。
Python 2.0+ 命名绑定表达式
1
2
3
4
	
>>> list_of_list = [[1,2,3],[4,5,6],[7,8,9]]
>>> [car_x for x in list_of_list for car_x in
 (x[0],)]
[1, 4, 7]

在列表解析（list comprehension）中将表达式放入一个单项元素（a single-item tuple）中的这个小技巧，并不能为使用带有表达式级绑定的高阶函数提供任何思路。要使用这样的高阶函数，还是需要使用块级（block-level）绑定，就象以下所示：
Python中的使用块级绑定的’map()’
1
2
3
4
5
	
>>> list_of_list = [[1,2,3],[4,5,6],[7,8,9]]
>>> let = Bindings()
>>> let.car = lambda l: l[0]
>>> map(let.car,list_of_list)
[1, 4, 7]

这样真不错，但如果我们想使用函数map()，那么其中的绑定范围可能会比我们想要的更宽一些。然而，我们可以做到的，哄骗列表解析让它替我们做名字绑定，即使其中的列表并不是我们最终想要得到的列表的情况下也没问题：
从Python的列表解析中“走下舞台”
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
	
# Compare Haskell expression:
# result = func car_car
#          where
#              car (x:xs) = x
#              car_car = car (car list_of_list)
#              func x = x + x^2
>>> [func for x in list_of_list
...      
for car in (x[0],)
...      
for func in (car+car**2,)][0]
2

我们对list_of_list列表中第一个元素的第一个元素进行了一次算数运算，而且期间还对该算术运算进行了命名（但其作用域仅仅是在表达式的范围内）。作为一种“优化”，我们可以不用费心创建多于一个元素的列表就能开始运算了，因为我们结尾处用的索引为0，所以我们仅仅选择的是第一个元素。：
从列表解析中高效地走下舞台
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
	
# Compare Haskell expression:
# result = func car_car
#          where
#              car (x:xs) = x
#              car_car = car (car list_of_list)
#              func x = x + x^2
>>> [func for x in list_of_list
...      
for car in (x[0],)
...      
for func in (car+car**2,)][0]
2

高阶函数：currying

Python内建的三个最常用的高阶函数是：map()、reduce()和filter()。这三个函数所做的事情 —— 以及谓之为“高阶”（higher-order）的原因 —— 是接受其它函数作为它们的（部分）参数。还有别的一些不属于内置的高阶函数，还会返回函数对象。
藉由函数对象在Python中具有首要地位， Python一直都有能让其使用者构造自己的高阶函数的能力。举个如下所示的小例子：
Python中一个简单函数工厂（function factory）
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
	
>>> def
 foo_factory():
...   
def
 foo():
...       
print
"Foo function from factory"
...   
return foo
...
>>> f = foo_factory()
>>> f()
Foo function from factory

本系列文章的第二部分我讨论过的Xoltar Toolkit中，有一组非常好用的高阶函数。Xoltar的functional模块中提供的绝大多数高阶函数都是在其它各种不同的传统型函数式编程语言中发展出来的高阶函数，其有用性已经过多年的实践验证。

可能其中最著名、最有用和最重要的高阶函数要数curry()了。函数curry()的名字取自于逻辑学家Haskell Curry，前文提及的一种编程语言也是用他姓名当中的名字部分命名的。”currying”背后隐含的意思是，（几乎）每一个函数都可以视为只带一个参数的部分函数（partial function）。要使currying能够用起来所需要做的就是让函数本身的返回值也是个函数，只不过所返回的函数“缩小了范围”或者是“更加接近完整的函数”。这和我在第二部分中提到的闭包特别相似 —— 对经过curry后的返回的后继函数进行调用时一步一步“填入”最后计算所需的更多数据（附加到一个过程（procedure）之上的数据）
现在让我们先用Haskell中一个很简单例子对curry进行讲解，然后在Python中使用functional模块重复展示一下这个简单的例子：
在Haskell计算中使用Curry
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
	
computation a b c d = (a + b^2+ c^3 + d^4)
check = 1 + 2^2 + 3^3 + 5^4
 
fillOne   = computation 1
-- specify "a"
fillTwo   = fillOne 2    
-- specify "b"
fillThree = fillTwo 3    
-- specify "c"
answer    = fillThree 5  
-- specify "d"
 
-- Result: check == answer == 657

现在使用Python：

在Python计算中使用Curry
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
	
>>> from functional import curry
>>> computation = lambda a,b,c,d: (a + b**2 + c**3 + d**4)
>>> computation(1,2,3,5)
657
>>> fillZero  = curry(computation)
>>> fillOne   = fillZero(1) 
# specify "a"
>>> fillTwo   = fillOne(2)  
# specify "b"
>>> fillThree = fillTwo(3)  
# specify "c"
>>> answer    = fillThree(5)
# specify "d"
>>> answer
657

第二部分中提到过的一个简单的计税程序的例子，当时用的是闭包（这次使用curry()），可以用来进一步做个对比：

Python中curry后的计税程序
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
	
from functional import *
 
taxcalc = lambda income,rate,deduct: (income-(deduct))*rate
 
taxCurry = curry(taxcalc)
taxCurry = taxCurry(50000)
taxCurry = taxCurry(0.30)
taxCurry = taxCurry(10000)
print "Curried taxes due =",taxCurry
 
print "Curried expression taxes due =", \
      curry(taxcalc)(50000)(0.30)(10000)

和使用闭包不同，我们需要以特定的顺序（从左到右）对参数进行curry处理。当要注意的是，functional模块中还包含一个rcurry()类，能够以相反的方向进行curry处理（从右到左）。
从一个层面讲，其中的第二个print语句同简单的同普通的taxcalc(50000,0.30,10000)函数调用相比只是个微小的拼写方面的变化。但从另一个不同的层面讲，它清晰地一个概念，那就是，每个函数都可以变换成仅仅带有一个参数的函数，这对于刚刚接触这个概念的人来讲，会有一种特别惊奇的感觉。

其它高阶函数

除了上述的curry功能，functional模块简直就是一个很有意思的高阶函数万能口袋。此外，无论用还是不用functional模块，编写你自己的高阶函数真的并不难。至少functional模块中的那些高阶函数为你提供了一些很值一看的思路。
它里面的其它高阶函数在很大程度上感觉有点象是“增强”版本的标准高阶函数map()、filter()和reduce()。这些函数的工作模式通常大致如此：将一个或多个函数以及一些列表作为参数接收进来，然后对这些列表参数运行它前面所接收到的函数。在这种工作模式方面，有非常大量很有意思也很有用的摆弄方法。还有一种模式是：拿到一组函数后，将这组函数的功能组合起来创建一个新函数。这种模式同样也有大量的变化形式。下面让我们看看functional模块里到底还有哪些其它的高阶函数。

sequential()和also()这两个函数都是在一系列成分函数（component function）的基础上创建一个新函数。然后这些成分函数可以通过使用相同的参数进行调用。两者的主要区别就在于，sequential()需要一个单个的函数列表作为参数，而also()接受的是一系列的多个参数。在多数情况下，对于函数的副作用而已这些会很有用，只是sequential()可以让你随意选择将哪个函数的返回值作为组合起来后的新函数的返回值。
顺序调用一系列函数(使用相同的参数)
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
21
	
>>> def a(x):
...     print x,
...     return "a"
...
>>> def b(x):
...     print x*2,
...     return "b"
...
>>> def c(x):
...     print x*3,
...     return "c"
...
>>> r = also(a,b,c)
>>> r
<functional.sequential instance at 0xb86ac>
>>> r(5)
5 10 15
'a'
>>> sequential([a,b,c],main=c)('x')
x xx xxx
'c'

isjoin()和conjoin()这两个函数同equential()和also()在创建新函数并对参数进行多个成分函数的调用方面非常相似。只是disjoin()函数用来查询成分函数中是否有一个函数的返回值（针对给定的参数）为真；conjoin()函数用来查询是否所有的成分函数的返回值都为真。在这些函数中只要条件允许就会使用逻辑短路，因此disjoin()函数可能不会出现某些副作用。joinfuncs()i同also()类似，但它返回的是由所有成分函数的返回值组成的一个元组（tuple），而不是选中的某个主函数。

前文所述的几个函数让你可以使用相同的参数对一系列函数进行调用，而any()、all()和 none_of()这三个让你可以使用一个参数列表对同一个函数进行多次调用。在大的结构方面，这些函数同内置的map()、reduce()和filter()有点象。 但funtional模块中的这三个高阶函数中都是对一组返回值进行布尔（boolean）运算得到其返回值的。例如：
对一系列返回值的真、假情况进行判断
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
	
>>> from functional import *
>>> isEven = lambda n: (n%2 == 0)
>>> any([1,3,5,8], isEven)
1
>>> any([1,3,5,7], isEven)
0
>>> none_of([1,3,5,7], isEven)
1
>>> all([2,4,6,8], isEven)
1
>>> all([2,4,6,7], isEven)
0

有点数学基础的人会对这个高阶函数非常感兴趣：iscompose(). 将多个函数进行合成（compostion）指的是，将一个函数的返回值同下个函数的输入“链接到一起”。对多个函数进行合成的程序员需要负责保证函数间的输入和输出是相互匹配的，不过这个条件无论是程序员在何时想使用返回值时都是需要满足的。举个简单的例子和阐明这一点：
创建合成函数
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
	
>>> def minus7(n): return n-7
...
>>> def times3(n): return n*3
...
>>> minus7(10)
3
>>> minustimes = compose(times3,minus7)
>>> minustimes(10)
9
>>> times3(minus7(10))
9
>>> timesminus = compose(minus7,times3)
>>> timesminus(10)
23
>>> minus7(times3(10))
23

后会有期

衷心希望我对高阶函数的思考能够引起读者的兴趣。无论如何，请动手试一试。试着编写一些你自己的高阶函数；一些可能很有用，很强大。告诉我它如何运行；或许这个系列之后的章节会讨论读者不断提供的新观点，新想法。
