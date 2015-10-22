###《大话设计模式》Python版代码实现

2013-04-10 09:59 五岳 博客园 字号：T | T

《大话设计模式》的代码使用C#写成的，而在本人接触到的面向对象语言中，只对C++和Python还算了解，为了加深对各个模式的理解，将其改写成了Python代码

AD：2014WOT全球软件技术峰会北京站 课程视频发布

上一周把《大话设计模式》看完了，对面向对象技术有了新的理解，对于一个在C下写代码比较多、偶尔会用到一些脚本语言写脚本的人来说，很是开阔眼界。《大话设计模式》的代码使用C#写成的，而在本人接触到的面向对象语言中，只对C++和Python还算了解，为了加深对各个模式的理解，我在网上下载了一个C++版的源代码，并根据自己的理解边读这本书边动手实践C++源代码，同时将其改写成了Python代码，算是一箭三雕吧。

由于这些代码的目的是展示各个设计模式而非完成一个具体的复杂任务，基于C++版本改写，例子的取材也和《大话设计模式》基本相同，再加上个人水平有限，因此这些Python版代码写的比较简单，虽然能跑起来是不假，不过难免有bug，而且实现也不一定最优，C++的味道比较浓而不够pythonic，还请高手包容指正。不过我还是尽量把或多或少有些pythonic的东西放在每个模式的“代码特点”部分进行展示，而这个“代码特点”里也不仅仅是pythonic的东西。

使用Python版本为2.6。

配图同样摘自《大话设计模式》，因此是C#风格的UML类图，为了便于排版已经缩小了。

[toc]
####一、简单工厂模式

模式特点：工厂根据条件产生不同功能的类。

程序实例：四则运算计算器，根据用户的输入产生相应的运算类，用这个运算类处理具体的运算。

代码特点：C/C++中的switch...case...分支使用字典的方式代替。

使用异常机制对除数为0的情况进行处理。
![简单工厂模式](http://images.51cto.com/files/uploadimg/20130410/1026170.png)
```python

    简单工厂模式   
     
    class Operation:  
        def GetResult(self):  
            pass 
     
    class OperationAdd(Operation):  
        def GetResult(self):  
            return self.op1+self.op2  
     
     
    class OperationSub(Operation):  
        def GetResult(self):  
            return self.op1-self.op2  
     
     
    class OperationMul(Operation):  
        def GetResult(self):  
            return self.op1*self.op2  
     
     
    class OperationDiv(Operation):  
        def GetResult(self):  
            try:  
                result = self.op1/self.op2  
                return result  
            except:  
                print "error:divided by zero." 
                return 0 
     
    class OperationUndef(Operation):  
        def GetResult(self):  
            print "Undefine operation." 
            return 0 
     
    class OperationFactory:  
        operation = {}  
        operation["+"] = OperationAdd();  
        operation["-"] = OperationSub();  
        operation["*"] = OperationMul();  
        operation["/"] = OperationDiv();  
        def createOperation(self,ch):          
            if ch in self.operation:  
                op = self.operation[ch]  
            else:  
                op = OperationUndef()  
            return op  
     
    if __name__ == "__main__":  
        op = raw_input("operator: ")  
        opa = input("a: ")  
        opb = input("b: ")  
        factory = OperationFactory()  
        cal = factory.createOperation(op)  
        cal.op1 = opa  
        cal.op2 = opb  
        print cal.GetResult() 
```
####二、策略模式

模式特点：定义算法家族并且分别封装，它们之间可以相互替换而不影响客户端。

程序实例：商场收银软件，需要根据不同的销售策略方式进行收费

代码特点：不同于同例1，这里使用字典是为了避免关键字不在字典导致bug的陷阱。

![策略模式](http://images.51cto.com/files/uploadimg/20130410/1026171.png)
    策略模式   
```python
     
    class CashSuper:  
        def AcceptCash(self,money):  
            return 0 
     
    class CashNormal(CashSuper):  
        def AcceptCash(self,money):  
            return money  
     
    class CashRebate(CashSuper):  
        discount = 0 
        def __init__(self,ds):  
            self.discount = ds  
        def AcceptCash(self,money):  
            return money * self.discount  
     
    class CashReturn(CashSuper):  
        total = 0;  
        ret = 0;  
        def __init__(self,t,r):  
            self.total = t  
            self.ret = r  
        def AcceptCash(self,money):  
            if (money>=self.total):  
                return money - self.ret  
            else:  
                return money  
     
    class CashContext:  
        def __init__(self,csuper):  
            self.cs = csuper  
        def GetResult(self,money):  
            return self.cs.AcceptCash(money)  
     
    if __name__ == "__main__":  
        money = input("money:")  
        strategy = {}  
        strategy[1] = CashContext(CashNormal())  
        strategy[2] = CashContext(CashRebate(0.8))  
        strategy[3] = CashContext(CashReturn(300,100))  
        ctype = input("type:[1]for normal,[2]for 80% discount [3]for 300 -100.")  
        if ctype in strategy:  
            cc = strategy[ctype]  
        else:  
            print "Undefine type.Use normal mode." 
            cc = strategy[1]  
        print "you will pay:%d" %(cc.GetResult(money)) 
```
####三、装饰模式

模式特点：动态地为对象增加额外的职责

程序实例：展示一个人一件一件穿衣服的过程。

代码特点：无
![装饰模式](http://images.51cto.com/files/uploadimg/20130410/1026172.png)
    装饰模式   
```python     
    class Person:  
        def __init__(self,tname):  
            self.name = tname  
        def Show(self):  
           print "dressed %s" %(self.name)  
     
    class Finery(Person):  
        componet = None 
        def __init__(self):  
            pass 
        def Decorate(self,ct):  
            self.componet = ct  
        def Show(self):  
        if(self.componet!=None):  
            self.componet.Show()  
     
    class TShirts(Finery):  
        def __init__(self):  
            pass 
        def Show(self):  
            print "Big T-shirt " 
            self.componet.Show()  
     
    class BigTrouser(Finery):  
        def __init__(self):  
            pass 
        def Show(self):  
            print "Big Trouser " 
            self.componet.Show()  
     
    if __name__ == "__main__":  
        p = Person("somebody")  
        bt = BigTrouser()  
        ts = TShirts()  
        bt.Decorate(p)  
        ts.Decorate(bt)  
        ts.Show() 
```

####四、代理模式

模式特点：为其他对象提供一种代理以控制对这个对象的访问。

程序实例：同模式特点描述。

代码特点：无
![代理模式](http://images.51cto.com/files/uploadimg/20130410/1026173.png)
    代理模式   
```python
    class Interface :  
        def Request(self):  
        return 0 
     
    class RealSubject(Interface):   
        def Request(self):  
            print "Real request." 
     
    class Proxy(Interface):  
        def Request(self):  
            self.real = RealSubject()  
            self.real.Request()  
     
    if __name__ == "__main__":  
        p = Proxy()  
        p.Request() 
```
####五、工厂方法模式

模式特点：定义一个用于创建对象的接口，让子类决定实例化哪一个类。这使得一个类的实例化延迟到其子类。

程序实例：基类雷锋类，派生出学生类和志愿者类，由这两种子类完成“学雷锋”工作。子类的创建由雷锋工厂的对应的子类完成。

代码特点：无
![工厂方法模式](http://images.51cto.com/files/uploadimg/20130410/1026174.png)
    工厂方法模式   
```python     
    class LeiFeng:  
        def Sweep(self):  
            print "LeiFeng sweep" 
     
    class Student(LeiFeng):  
        def Sweep(self):  
            print "Student sweep" 
     
    class Volenter(LeiFeng):  
        def Sweep(self):  
            print "Volenter sweep" 
     
    class LeiFengFactory:  
        def CreateLeiFeng(self):  
            temp = LeiFeng()  
            return temp  
     
    class StudentFactory(LeiFengFactory):  
        def CreateLeiFeng(self):  
            temp = Student()  
            return temp  
     
    class VolenterFactory(LeiFengFactory):  
        def CreateLeiFeng(self):  
            temp = Volenter()  
            return temp  
     
    if __name__ == "__main__":  
        sf = StudentFactory()  
        s=sf.CreateLeiFeng()  
        s.Sweep()  
        sdf = VolenterFactory()  
        sd=sdf.CreateLeiFeng()  
        sd.Sweep() 
```
####六、原型模式

模式特点：用原型实例指定创建对象的种类，并且通过拷贝这些原型创建新的对象。

程序实例：从简历原型，生成新的简历

代码特点：简历类Resume提供的Clone()方法其实并不是真正的Clone，只是为已存在对象增加了一次引用。

Python为对象提供的copy模块中的copy方法和deepcopy方法已经实现了原型模式，但由于例子的层次较浅，二者看不出区别。
![原型模式](http://images.51cto.com/files/uploadimg/20130410/1026175.png)
    原型模式   
```python     
    import copy  
    class WorkExp:  
        place=""  
        year=0 
     
    class Resume:  
        name = '' 
        age = 0 
        def __init__(self,n):  
            self.name = n  
        def SetAge(self,a):  
            self.age = a  
        def SetWorkExp(self,p,y):  
            self.place = p  
            self.year = y  
        def Display(self):  
            print self.age  
            print self.place  
            print self.year  
        def Clone(self):  
        #实际不是“克隆”，只是返回了自身  
            return self 
     
    if __name__ == "__main__":  
        a = Resume("a")  
        b = a.Clone()  
        c = copy.copy(a)  
        d = copy.deepcopy(a)  
        a.SetAge(7)  
        b.SetAge(12)  
        c.SetAge(15)  
        d.SetAge(18)  
        a.SetWorkExp("PrimarySchool",1996)  
        b.SetWorkExp("MidSchool",2001)  
        c.SetWorkExp("HighSchool",2004)  
        d.SetWorkExp("University",2007)  
        a.Display()  
        b.Display()  
        c.Display()  
        d.Display() 
```
####七、模板方法模式

模式特点：定义一个操作中的算法骨架，将一些步骤延迟至子类中。

程序实例：考试时使用同一种考卷（父类），不同学生上交自己填写的试卷（子类方法的实现）

代码特点：无
![模板方法模式](http://images.51cto.com/files/uploadimg/20130410/1026176.png)
    模板方法模式   
```python     
    class TestPaper:  
        def TestQuestion1(self):  
            print "Test1:A. B. C. D." 
            print "(%s)" %self.Answer1()  
     
        def TestQuestion2(self):  
            print "Test1:A. B. C. D." 
            print "(%s)" %self.Answer2()  
        def Answer1(self):  
            return ""  
        def Answer2(self):  
            return ""  
     
    class TestPaperA(TestPaper):  
        def Answer1(self):  
            return "B" 
        def Answer2(self):  
            return "C";  
     
    class TestPaperB(TestPaper):  
        def Answer1(self):  
            return "D" 
        def Answer2(self):  
            return "D";  
     
    if __name__ == "__main__":  
        s1 = TestPaperA()  
        s2 = TestPaperB()  
        print "student 1" 
        s1.TestQuestion1()  
        s1.TestQuestion2()  
        print "student 2" 
        s2.TestQuestion1()  
        s2.TestQuestion2() 
```
####八、外观模式

模式特点：为一组调用提供一致的接口。

程序实例：接口将几种调用分别组合成为两组，用户通过接口调用其中的一组。

代码特点：无
![外观模式](http://images.51cto.com/files/uploadimg/20130410/1026177.png)
    外观模式   
```python     
    class SubSystemOne:  
        def MethodOne(self):  
            print "SubSysOne" 
     
    class SubSystemTwo:  
        def MethodTwo(self):  
            print "SubSysTwo" 
     
    class SubSystemThree:  
        def MethodThree(self):  
            print "SubSysThree" 
     
    class SubSystemFour:  
        def MethodFour(self):  
            print "SubSysFour" 
     
     
    class Facade:  
        def __init__(self):  
            self.one = SubSystemOne()  
            self.two = SubSystemTwo()  
            self.three = SubSystemThree()  
            self.four = SubSystemFour()  
        def MethodA(self):  
            print "MethodA" 
            self.one.MethodOne()  
            self.two.MethodTwo()  
            self.four.MethodFour()  
        def MethodB(self):  
            print "MethodB" 
            self.two.MethodTwo()  
            self.three.MethodThree()  
     
    if __name__ == "__main__":  
        facade = Facade()  
        facade.MethodA()  
        facade.MethodB() 
```
####九、建造者模式

模式特点：将一个复杂对象的构建(Director)与它的表示(Builder)分离，使得同样的构建过程可以创建不同的表示(ConcreteBuilder)。

程序实例：“画”出一个四肢健全（头身手腿）的小人

代码特点：无
![建造者模式](http://images.51cto.com/files/uploadimg/20130410/1026178.png)
    建造者模式   
```python     
    class Person:  
        def CreateHead(self):  
            pass 
        def CreateHand(self):  
            pass 
        def CreateBody(self):  
            pass 
        def CreateFoot(self):  
            pass 
     
    class ThinPerson(Person):  
        def CreateHead(self):  
            print "thin head" 
        def CreateHand(self):  
            print "thin hand" 
        def CreateBody(self):  
            print "thin body" 
        def CreateFoot(self):  
            print "thin foot" 
     
    class ThickPerson(Person):  
        def CreateHead(self):  
            print "thick head" 
        def CreateHand(self):  
            print "thick hand" 
        def CreateBody(self):  
            print "thick body" 
        def CreateFoot(self):  
            print "thick foot" 
     
    class Director:  
        def __init__(self,temp):  
            self.p = temp  
        def Create(self):  
            self.p.CreateHead()  
            self.p.CreateBody()  
            self.p.CreateHand()  
            self.p.CreateFoot()  
     
    if __name__ == "__main__":  
        p = ThickPerson()  
        d = Director(p)  
        d.Create() 
```
####十、观察者模式

模式特点：定义了一种一对多的关系，让多个观察对象同时监听一个主题对象，当主题对象状态发生变化时会通知所有观察者。

程序实例：公司里有两种上班时趁老板不在时偷懒的员工：看NBA的和看股票行情的，并且事先让老板秘书当老板出现时通知他们继续做手头上的工作。

程序特点：无
![观察者模式](http://images.51cto.com/files/uploadimg/20130410/1026179.png)
    观察者模式   
```python     
    class Observer:  
        def __init__(self,strname,strsub):  
            self.name = strname  
            self.sub = strsub  
        def Update(self):  
            pass 
     
    class StockObserver(Observer):  
        #no need to rewrite __init__()  
        def Update(self):  
            print "%s:%s,stop watching Stock and go on work!" %(self.name,self.sub.action)  
     
    class NBAObserver(Observer):  
        def Update(self):  
            print "%s:%s,stop watching NBA and go on work!" %(self.name,self.sub.action)  
     
    class SecretaryBase:  
        def __init__(self):  
            self.observers = []  
        def Attach(self,new_observer):  
            pass   
        def Notify(self):  
            pass 
     
    class Secretary(SecretaryBase):  
        def Attach(self,new_observer):  
            self.observers.append(new_observer)  
        def Notify(self):  
            for p in self.observers:  
                p.Update()  
     
    if __name__ == "__main__":  
        p = Secretary()  
        s1 = StockObserver("xh",p)  
        s2 = NBAObserver("wyt",p)  
        p.Attach(s1);  
        p.Attach(s2);  
        p.action = "WARNING:BOSS ";  
        p.Notify() 
```
####十一、抽象工厂模式

模式特点：提供一个创建一系列相关或相互依赖对象的接口，而无需指定它们的类。

程序实例：提供对不同的数据库访问的支持。

IUser和IDepartment是两种不同的抽象产品，它们都有Access和SQL Server这两种不同的实现；IFactory是产生IUser和IDepartment的抽象工厂，根据具体实现（AccessFactory和SqlFactory）产生对应的具体的对象（CAccessUser与CAccessDepartment，或者CSqlUser与CSqlDepartment）。

代码特点：无
![抽象工厂模式](http://images.51cto.com/files/uploadimg/20130410/10261710.png)
    抽象工厂模式   
```python     
    class IUser:  
        def GetUser(self):  
            pass 
        def InsertUser(self):  
            pass 
     
    class IDepartment:  
        def GetDepartment(self):  
            pass 
        def InsertDepartment(self):  
            pass 
     
    class CAccessUser(IUser):  
        def GetUser(self):  
            print "Access GetUser" 
        def InsertUser(self):  
            print "Access InsertUser" 
     
     
    class CAccessDepartment(IDepartment):  
        def GetDepartment(self):  
            print "Access GetDepartment" 
        def InsertDepartment(self):  
            print "Access InsertDepartment" 
     
    class CSqlUser(IUser):  
        def GetUser(self):  
            print "Sql GetUser" 
        def InsertUser(self):  
            print "Sql InsertUser" 
     
     
    class CSqlDepartment(IDepartment):  
        def GetDepartment(self):  
            print "Sql GetDepartment" 
        def InsertDepartment(self):  
            print "Sql InsertDepartment" 
     
    class IFactory:  
        def CreateUser(self):  
            pass 
        def CreateDepartment(self):  
            pass 
     
    class AccessFactory(IFactory):  
        def CreateUser(self):  
            temp=CAccessUser()  
            return temp  
        def CreateDepartment(self):  
            temp = CAccessDepartment()  
            return temp  
     
    class SqlFactory(IFactory):  
        def CreateUser(self):  
            temp = CSqlUser()  
            return temp  
        def CreateDepartment(self):  
            temp = CSqlDepartment()  
            return temp  
     
    if __name__ == "__main__":  
        factory = SqlFactory()  
        user=factory.CreateUser()  
        depart=factory.CreateDepartment()  
        user.GetUser()  
        depart.GetDepartment() 
```
####十二、状态模式

模式特点：当一个对象的内在状态改变时允许改变其行为，这个对象看起来像是改变了其类。

程序实例：描述一个程序员的工作状态，当需要改变状态时发生改变，不同状态下的方法实现不同

代码特点：无
![状态模式](http://images.51cto.com/files/uploadimg/20130410/10261711.png)
    状态模式   
 ```python    
    class State:  
        def WirteProgram(self):  
            pass 
     
    class Work:  
        def __init__(self):  
            self.hour = 9 
            self.current = ForenoonState()  
        def SetState(self,temp):  
            self.current = temp  
        def WriteProgram(self):  
            self.current.WriteProgram(self)  
     
    class NoonState(State):  
        def WriteProgram(self,w):  
            print "noon working" 
            if (w.hour<13):  
                print "fun." 
            else:  
                print "need to rest." 
     
    class ForenoonState(State):  
        def WriteProgram(self,w):  
            if (w.hour<12):  
                print "morning working" 
                print "energetic" 
            else:  
                w.SetState(NoonState())          
                w.WriteProgram()  
     
    if __name__ == "__main__":  
        mywork = Work()  
        mywork.hour = 9 
        mywork.WriteProgram()  
        mywork.hour =14 
        mywork.WriteProgram() 
```
####十三、适配器模式

模式特点：将一个类的接口转换成为客户希望的另外一个接口。

程序实例：用户通过适配器使用一个类的方法。

代码特点：无
![适配器模式](http://images.51cto.com/files/uploadimg/20130410/10261712.png)
    适配器模式   
 ```python    
    class Target:  
        def Request():  
            print "common request." 
     
    class Adaptee(Target):  
        def SpecificRequest(self):  
            print "specific request." 
     
    class Adapter(Target):  
        def __init__(self,ada):  
            self.adaptee = ada  
        def Request(self):  
            self.adaptee.SpecificRequest()  
     
    if __name__ == "__main__":  
        adaptee = Adaptee()  
        adapter = Adapter(adaptee)  
        adapter.Request() 
```
####十四、备忘录模式

模式特点：在不破坏封装性的前提下捕获一个对象的内部状态，并在该对象之外保存这个状态，以后可以将对象恢复到这个状态。

程序实例：将Originator对象的状态封装成Memo对象保存在Caretaker内

代码特点：无
![备忘录模式](http://images.51cto.com/files/uploadimg/20130410/10261713.png)
    备忘录模式   
```python     
    class Originator:  
        def __init__(self):  
            self.state = ""  
        def Show(self):  
            print self.state  
        def CreateMemo(self):  
            return Memo(self.state)  
        def SetMemo(self,memo):  
            self.state = memo.state  
     
    class Memo:  
        state= ""  
        def __init__(self,ts):  
            self.state = ts  
     
    class Caretaker:  
        memo = ""  
     
    if __name__ == "__main__":  
        on = Originator()  
        on.state = "on" 
        on.Show()  
        c = Caretaker()  
        c.memo=on.CreateMemo()  
        on.state="off" 
        on.Show()  
        on.SetMemo(c.memo)  
        on.Show() 
```
####十五、组合模式

模式特点：将对象组合成成树形结构以表示“部分-整体”的层次结构

程序实例：公司人员的组织结构

代码特点：无
![组合模式](http://images.51cto.com/files/uploadimg/20130410/10261714.png)
    组合模式   
```python     
    class Component:  
        def __init__(self,strName):  
            self.m_strName = strName  
        def Add(self,com):  
            pass 
        def Display(self,nDepth):  
            pass 
     
    class Leaf(Component):  
        def Add(self,com):  
            print "leaf can't add" 
        def Display(self,nDepth):  
            strtemp = ""  
            for i in range(nDepth):  
                strtemp=strtemp+"-" 
            strtemp=strtemp+self.m_strName  
            print strtemp  
     
    class Composite(Component):  
        def __init__(self,strName):  
            self.m_strName = strName  
            self.c = []  
        def Add(self,com):  
            self.c.append(com)  
        def Display(self,nDepth):  
            strtemp=""  
            for i in range(nDepth):  
                strtemp=strtemp+"-" 
            strtemp=strtemp+self.m_strName  
            print strtemp  
            for com in self.c:  
                com.Display(nDepth+2)  
     
    if __name__ == "__main__":  
        p = Composite("Wong")  
        p.Add(Leaf("Lee"))  
        p.Add(Leaf("Zhao"))  
        p1 = Composite("Wu")  
        p1.Add(Leaf("San"))  
        p.Add(p1)  
        p.Display(1); 
```
####十六、迭代器模式

模式特点：提供方法顺序访问一个聚合对象中各元素，而又不暴露该对象的内部表示

说明：这个模式没有写代码实现，原因是使用Python的列表和for ... in list就能够完成不同类型对象聚合的迭代功能了。
![迭代器模式](http://images.51cto.com/files/uploadimg/20130410/10261715.png)

####十七、单例模式

模式特点：保证类仅有一个实例，并提供一个访问它的全局访问点。

说明：为了实现单例模式费了不少工夫，后来查到一篇博文对此有很详细的介绍，而且实现方式也很丰富，通过对代码的学习可以了解更多Python的用法。以下的代码出自GhostFromHeaven的专栏，地址：http://blog.csdn.net/ghostfromheaven/article/details/7671853。不过正如其作者在Python单例模式终极版所说：

我要问的是，Python真的需要单例模式吗？我指像其他编程语言中的单例模式。

答案是：不需要！

因为，Python有模块（module），最pythonic的单例典范。

模块在在一个应用程序中只有一份，它本身就是单例的，将你所需要的属性和方法，直接暴露在模块中变成模块的全局变量和方法即可！
![单例模式](http://images.51cto.com/files/uploadimg/20130410/10261716.png)

    单例模式（四种方法）   
  ```python   
    #-*- encoding=utf-8 -*-  
    print '----------------------方法1--------------------------' 
    #方法1,实现__new__方法  
    #并在将一个类的实例绑定到类变量_instance上,  
    #如果cls._instance为None说明该类还没有实例化过,实例化该类,并返回  
    #如果cls._instance不为None,直接返回cls._instance  
    class Singleton(object):  
        def __new__(cls, *args, **kw):  
            if not hasattr(cls, '_instance'):  
                orig = super(Singleton, cls)  
                cls._instance = orig.__new__(cls, *args, **kw)  
            return cls._instance  
     
    class MyClass(Singleton):  
        a = 1 
     
    one = MyClass()  
    two = MyClass()  
     
    two.a = 3 
    print one.a  
    #3  
    #one和two完全相同,可以用id(), ==, is检测  
    print id(one)  
    #29097904  
    print id(two)  
    #29097904  
    print one == two  
    #True  
    print one is two  
    #True  
     
    print '----------------------方法2--------------------------' 
    #方法2,共享属性;所谓单例就是所有引用(实例、对象)拥有相同的状态(属性)和行为(方法)  
    #同一个类的所有实例天然拥有相同的行为(方法),  
    #只需要保证同一个类的所有实例具有相同的状态(属性)即可  
    #所有实例共享属性的最简单最直接的方法就是__dict__属性指向(引用)同一个字典(dict)  
    #可参看:http://code.activestate.com/recipes/66531/  
    class Borg(object):  
        _state = {}  
        def __new__(cls, *args, **kw):  
            ob = super(Borg, cls).__new__(cls, *args, **kw)  
            ob.__dict__ = cls._state  
            return ob  
     
    class MyClass2(Borg):  
        a = 1 
     
    one = MyClass2()  
    two = MyClass2()  
     
    #one和two是两个不同的对象,id, ==, is对比结果可看出  
    two.a = 3 
    print one.a  
    #3  
    print id(one)  
    #28873680  
    print id(two)  
    #28873712  
    print one == two  
    #False  
    print one is two  
    #False  
    #但是one和two具有相同的（同一个__dict__属性）,见:  
    print id(one.__dict__)  
    #30104000  
    print id(two.__dict__)  
    #30104000  
     
    print '----------------------方法3--------------------------' 
    #方法3:本质上是方法1的升级（或者说高级）版  
    #使用__metaclass__（元类）的高级python用法  
    class Singleton2(type):  
        def __init__(cls, name, bases, dict):  
            super(Singleton2, cls).__init__(name, bases, dict)  
            cls._instance = None 
        def __call__(cls, *args, **kw):  
            if cls._instance is None:  
                cls._instance = super(Singleton2, cls).__call__(*args, **kw)  
            return cls._instance  
     
    class MyClass3(object):  
        __metaclass__ = Singleton2  
     
    one = MyClass3()  
    two = MyClass3()  
     
    two.a = 3 
    print one.a  
    #3  
    print id(one)  
    #31495472  
    print id(two)  
    #31495472  
    print one == two  
    #True  
    print one is two  
    #True  
     
    print '----------------------方法4--------------------------' 
    #方法4:也是方法1的升级（高级）版本,  
    #使用装饰器(decorator),  
    #这是一种更pythonic,更elegant的方法,  
    #单例类本身根本不知道自己是单例的,因为他本身(自己的代码)并不是单例的  
    def singleton(cls, *args, **kw):  
        instances = {}  
        def _singleton():  
            if cls not in instances:  
                instances[cls] = cls(*args, **kw)  
            return instances[cls]  
        return _singleton  
     
    @singleton 
    class MyClass4(object):  
        a = 1 
        def __init__(self, x=0):  
            self.x = x  
     
    one = MyClass4()  
    two = MyClass4()  
     
    two.a = 3 
    print one.a  
    #3  
    print id(one)  
    #29660784  
    print id(two)  
    #29660784  
    print one == two  
    #True  
    print one is two  
    #True  
    one.x = 1 
    print one.x  
    #1  
    print two.x  
    #1 
```
####十八、桥接模式

模式特点：将抽象部分与它的实现部分分离，使它们都可以独立地变化。

程序实例：两种品牌的手机，要求它们都可以运行游戏和通讯录两个软件，而不是为每个品牌的手机都独立编写不同的软件。

代码特点：虽然使用了object的新型类，不过在这里不是必须的，是对在Python2.2之后“尽量使用新型类”的建议的遵从示范。
![桥接模式](http://images.51cto.com/files/uploadimg/20130410/10261717.png)
    桥接模式   
```python     
    class HandsetSoft(object):  
        def Run(self):  
            pass 
     
    class HandsetGame(HandsetSoft):  
        def Run(self):  
            print "Game" 
     
    class HandsetAddressList(HandsetSoft):  
        def Run(self):  
            print "Address List" 
     
    class HandsetBrand(object):  
        def __init__(self):  
            self.m_soft = None 
        def SetHandsetSoft(self,temp):  
            self.m_soft= temp  
        def Run(self):  
            pass 
     
    class HandsetBrandM(HandsetBrand):  
        def Run(self):  
            if not (self.m_soft == None):  
                print "BrandM" 
                self.m_soft.Run()  
     
    class HandsetBrandN(HandsetBrand):  
        def Run(self):  
            if not (self.m_soft == None):  
                print "BrandN" 
                self.m_soft.Run()  
     
    if __name__ == "__main__":  
        brand = HandsetBrandM()  
        brand.SetHandsetSoft(HandsetGame())  
        brand.Run()  
        brand.SetHandsetSoft(HandsetAddressList())  
        brand.Run() 
```
####十九、命令模式

模式特点：将请求封装成对象，从而使可用不同的请求对客户进行参数化；对请求排队或记录请求日志，以及支持可撤消的操作。

程序实例：烧烤店有两种食物，羊肉串和鸡翅。客户向服务员点单，服务员将点好的单告诉大厨，由大厨进行烹饪。

代码特点：注意在遍历列表时不要用注释的方式删除，否则会出现bug。bug示例程序附在后面，我认为这是因为remove打乱了for迭代查询列表的顺序导致的。
![命令模式](http://images.51cto.com/files/uploadimg/20130410/10261718.png)
    命令模式   
```python     
    class Barbucer:  
        def MakeMutton(self):  
            print "Mutton" 
        def MakeChickenWing(self):  
            print "Chicken Wing" 
     
    class Command:  
        def __init__(self,temp):  
            self.receiver=temp  
        def ExecuteCmd(self):  
            pass 
     
    class BakeMuttonCmd(Command):  
        def ExecuteCmd(self):  
            self.receiver.MakeMutton()  
     
    class ChickenWingCmd(Command):  
        def ExecuteCmd(self):  
            self.receiver.MakeChickenWing()  
     
    class Waiter:  
        def __init__(self):  
            self.order =[]  
        def SetCmd(self,command):  
            self.order.append(command)  
            print "Add Order" 
        def Notify(self):  
            for cmd in self.order:  
                #self.order.remove(cmd)  
                #lead to a bug  
                cmd.ExecuteCmd()  
                  
     
    if __name__ == "__main__":  
        barbucer=Barbucer()  
        cmd=BakeMuttonCmd(barbucer)  
        cmd2=ChickenWingCmd(barbucer)  
        girl=Waiter()  
        girl.SetCmd(cmd)  
        girl.SetCmd(cmd2)  
        girl.Notify() 
```
在for中remove会导致bug的展示代码：

    bug   
     
    c=[0,1,2,3]  
    for i in c:  
        print i  
        c.remove(i)  
     
    #output:  
    #0  
    #2 

####二十、职责链模式

模式特点：使多个对象都有机会处理请求，从而避免发送者和接收者的耦合关系。将对象连成链并沿着这条链传递请求直到被处理。

程序实例：请假和加薪等请求发给上级，如果上级无权决定，那么递交给上级的上级。

代码特点：无
![职责链模式](http://images.51cto.com/files/uploadimg/20130410/10261719.png)
    职责链模式   
```python     
    class Request:  
        def __init__(self,tcontent,tnum):  
            self.content = tcontent  
            self.num = tnum  
     
    class Manager:  
        def __init__(self,temp):  
            self.name = temp  
        def SetSuccessor(self,temp):  
            self.manager = temp  
        def GetRequest(self,req):  
            pass 
     
    class CommonManager(Manager):  
        def GetRequest(self,req):  
            if(req.num>=0 and req.num<10):  
                print "%s handled %d request." %(self.name,req.num)  
            else:  
                self.manager.GetRequest(req)  
     
    class MajorDomo(Manager):  
        def GetRequest(self,req):  
            if(req.num>=10):  
                print "%s handled %d request." %(self.name,req.num)  
     
    if __name__ == "__main__":  
        common = CommonManager("Zhang")  
        major = MajorDomo("Lee")  
        common.SetSuccessor(major)  
        req = Request("rest",33)  
        common.GetRequest(req)  
        req2 = Request("salary",3)  
        common.GetRequest(req2) 
```
####二十一、中介者模式

模式特点：用一个对象来封装一系列的对象交互，中介者使各对象不需要显示地相互引用，从而使耦合松散，而且可以独立地改变它们之间的交互。

程序实例：两个对象通过中介者相互通信

代码特点：无
![中介者模式](http://images.51cto.com/files/uploadimg/20130410/10261720.png)
    中介者模式   
 ```python    
    class Mediator:  
        def Send(self,message,col):  
            pass 
     
    class Colleague:  
        def __init__(self,temp):  
            self.mediator = temp  
     
    class Colleague1(Colleague):  
        def Send(self,message):  
            self.mediator.Send(message,self)  
        def Notify(self,message):  
            print "Colleague1 get a message:%s" %message  
     
    class Colleague2(Colleague):  
        def Send(self,message):  
            self.mediator.Send(message,self)  
        def Notify(self,message):  
            print "Colleague2 get a message:%s" %message  
     
    class ConcreteMediator(Mediator):  
        def Send(self,message,col):  
            if(col==col1):  
                col2.Notify(message)  
            else:  
                col1.Notify(message)  
     
    if __name__ == "__main__":  
        m =ConcreteMediator()  
        col1 = Colleague1(m)  
        col2 = Colleague1(m)  
        m.col1=col1  
        m.col2=col2  
        col1.Send("How are you?");  
        col2.Send("Fine."); 
```
####二十二、享元模式

模式特点：运用共享技术有效地支持大量细粒度的对象。

程序实例：一个网站工厂，根据用户请求的类别返回相应类别的网站。如果这种类别的网站已经在服务器上，那么返回这种网站并加上不同用户的独特的数据；如果没有，那么生成一个。

代码特点：为了展示每种网站的由用户请求的次数，这里为它们建立了一个引用次数的字典。

之所以不用Python的sys模块中的sys.getrefcount()方法统计引用计数是因为有的对象可能在别处被隐式的引用，从而增加了引用计数。 
![享元模式](http://images.51cto.com/files/uploadimg/20130410/10261720.png)
    享元模式   
 ```python    
    import sys  
     
    class WebSite:  
        def Use(self):  
            pass 
     
    class ConcreteWebSite(WebSite):  
        def __init__(self,strName):  
            self.name = strName  
        def Use(self,user):  
            print "Website type:%s,user:%s" %(self.name,user)  
     
    class UnShareWebSite(WebSite):  
        def __init__(self,strName):  
            self.name = strName  
        def Use(self,user):  
            print "UnShare Website type:%s,user:%s" %(self.name, user)  
     
    class WebFactory:  
        def __init__(self):  
            test = ConcreteWebSite("test")  
            self.webtype ={"test":test}  
            self.count = {"test":0}  
        def GetWeb(self,webtype):  
            if webtype not in self.webtype:  
                temp = ConcreteWebSite(webtype)  
                self.webtype[webtype] = temp  
                self.count[webtype] =1 
            else:  
                temp = self.webtype[webtype]  
                self.count[webtype] = self.count[webtype]+1 
            return temp  
        def GetCount(self):  
            for key in self.webtype:  
                #print "type: %s, count:%d" %(key,sys.getrefcount(self.webtype[key]))  
                print "type: %s, count:%d " %(key,self.count[key])  
     
    if __name__ == "__main__":  
        f = WebFactory()  
        ws=f.GetWeb("blog")  
        ws.Use("Lee")  
        ws2=f.GetWeb("show")  
        ws2.Use("Jack")  
        ws3=f.GetWeb("blog")  
        ws3.Use("Chen")  
        ws4=UnShareWebSite("TEST")  
        ws4.Use("Mr.Q")  
        print f.webtype  
        f.GetCount() 
```python
####二十三、解释器模式

模式特点：给定一个语言，定义它的文法的一种表示，并定义一个解释器，这个解释器使用该表示来解释语言中的句子。

程序实例：（只是模式特点的最简单示范）

代码特点：无
![解释器模式](http://images.51cto.com/files/uploadimg/20130410/10261722.png)
    解释器模式   
```python     
    class Context:  
        def __init__(self):  
            self.input=""  
            self.output=""  
     
    class AbstractExpression:  
        def Interpret(self,context):  
            pass 
     
    class Expression(AbstractExpression):  
        def Interpret(self,context):  
            print "terminal interpret" 
     
    class NonterminalExpression(AbstractExpression):  
        def Interpret(self,context):  
            print "Nonterminal interpret" 
     
    if __name__ == "__main__":  
        context= ""  
        c = []  
        c = c + [Expression()]  
        c = c + [NonterminalExpression()]  
        c = c + [Expression()]  
        c = c + [Expression()]  
        for a in c:  
            a.Interpret(context) 
```
####二十四、访问者模式

模式特点：表示一个作用于某对象结构中的各元素的操作。它使你可以在不改变各元素的类的前提下定义作用于这些元素的新操作。

程序实例：对于男人和女人（接受访问者的元素，ObjectStructure用于穷举这些元素），不同的遭遇（具体的访问者）引发两种对象的不同行为。

代码特点：无
![访问者模式](http://images.51cto.com/files/uploadimg/20130410/10261723.png)
    访问者模式   
```python     
    # -*- coding: UTF-8 -*-  
    class Person:  
        def Accept(self,visitor):  
            pass 
     
    class Man(Person):  
        def Accept(self,visitor):  
            visitor.GetManConclusion(self)  
     
    class Woman(Person):  
        def Accept(self,visitor):  
            visitor.GetWomanConclusion(self)  
     
    class Action:  
        def GetManConclusion(self,concreteElementA):  
            pass 
        def GetWomanConclusion(self,concreteElementB):  
            pass 
     
    class Success(Action):  
        def GetManConclusion(self,concreteElementA):  
            print "男人成功时，背后有个伟大的女人" 
        def GetWomanConclusion(self,concreteElementB):  
            print "女人成功时，背后有个不成功的男人" 
     
    class Failure(Action):  
        def GetManConclusion(self,concreteElementA):  
            print "男人失败时，闷头喝酒，谁也不用劝" 
        def GetWomanConclusion(self,concreteElementB):  
            print "女人失败时，眼泪汪汪，谁也劝不了" 
     
     
    class ObjectStructure:  
        def __init__(self):  
            self.plist=[]  
        def Add(self,p):  
            self.plist=self.plist+[p]  
        def Display(self,act):  
            for p in self.plist:  
                p.Accept(act)  
     
    if __name__ == "__main__":  
        os = ObjectStructure()  
        os.Add(Man())  
        os.Add(Woman())  
        sc = Success()  
        os.Display(sc)  
        fl = Failure()  
        os.Display(fl) 
```