Visitor 设计模式

[toc]
[传送][0]

##### 常规实例

###### 类图
![][1]

我知道这个模式很久了，但是我至今都不需要它。Java通过本地方式处理多态：方法被调用时是基于调用这个方法的对象运行时的类型，而是不是基于调用对象编译时的类型。
```java
interface Animal{
     void eat();
}
public class Dog implements Animal {
    public void eat() {
        System.out.println("Gnaws bones");
    }
}
Animal a = new Dog();
a.eats(); // Prints "Gnaws bones"
```
然而，以上的方式对于参数类型却无法有效的运行
```java
public class Feeder {
    public void feed(Dog d) {
        d.eat();
  }
     public void feed(Cat c) {
        c.eat();
    }
}
 
Feeder feeder = new Feeder();
Object o = new Dog();
feeder.feed(o); // Cannot compile!
```
这个问题被称之为双重派发，因为它既要求被调用的方法既基于调用方法的实例，同时也基于方法的参数类型。而对于参数类型而言，Java不是基于本地化方式来处理。为了能够编译通过，下面的代码是必须的：

```java
if (o instanceof Dog) {
    feeder.feed((Dog) o);
} else if (o instanceof Cat) {
    feeder.feed((Cat) o);
} else {
    throw new RuntimeException("Invalid type");
}
```

随着更多重载方法的出现，情况也会变得更加复杂——方法中出现更多的参数，复杂度也会呈指数级别提高。在维护阶段，添加更多的重载的方法需要阅读所有代码，如果程序填充了太多不必要的代码需就要去更新它。多个参数通过嵌套多个if来实现，这对于维护会变得更加糟糕。访问者模式是一种优雅的方式来解决以上同样的效果，不使用多个if，而使用Animal类中的一个单独的方法来作为解决的代价。

随着更多重载方法的出现，情况也会变得更加复杂——方法中出现更多的参数，复杂度也会呈指数级别提高。在维护阶段，添加更多的重载的方法需要阅读所有代码，如果程序填充了太多不必要的代码需就要去更新它。多个参数通过嵌套多个if来实现，这对于维护会变得更加糟糕。访问者模式是一种优雅的方式来解决以上同样的效果，不使用多个if，而使用Animal类中的一个单独的方法来作为解决的代价。

```java
public interface Animal {
	void eat();
    void accept(Vistor v);
}

public class Cat {
	public void eat() { ... }
    public void accept(Visitor v) {
    v.visit(this)
    }
}

public class Dog {
	public void eat() { ... }
    
    public void accept(Visitor v) {
    	v.visit(this);
    }
    
 public class FeaderVisitor {
 	public visit(Cat c) {
    	new Feeder().feed(c);
    }
    
	public void visit(Dog d) {
    	new Feeder().feed(d);
    }
 }
}

```

###### 优势：
* 没有逻辑的评价出现
* 只是在Animal和FeederVisitor之间建立依赖,FeederVisitor中只限于visit方法
* 按照推论，当添加新的Animal子类的时候，Feeder类可以保持不变
* 当添加一个新的Animal子类的时候，FeederVisitor类实现一个额外的方法去处理它即可
* 其他的横切逻辑也可以遵循相同的模式，比如：一个来教动物新把戏的训练特征

对于一些简单的例子使用如此长的代码似乎有杀鸡用宰牛刀的感觉。然而，我的经验教会了我像上面简单的填充代码，当随着项目的发展业务逻辑变负责是致命的。


##### 在访问值模式中使用反射
[传送](http://www.importnew.com/12536.html)

集合类型在面向对象编程中很常用，这也带来一些代码相关的问题。比如，“怎么操作集合中不同类型的对象？”

一种做法就是遍历集合中的每个元素，然后根据它的类型而做具体的操作。这会很复杂，尤其当你不知道集合中元素的类型时。如果y要打印集合中的元素，可以写一个这样的方法：

```java
public void messyPrintCollection(Collection collection) {
	Iterator iterator = collection.iterator()
    
    while(iterator.hasNext()) 
    System.out.println(iterator.next().toString())
}
```
看起来很简单。仅仅调用了Object.toString()方法并打印出了对象，对吧？但如果你的集合是一个包含hashtable的vector呢？那会变得更复杂。你必须检查集合返回对象的类型：

```java
public void messyPrintCollection(Collection collection) {
	Iterator iterator = collection.iterator()
    
    while(iterator.hasNext()) {
    Object  o = iterator.next();
    if (o instanceof Collection)
    	messyPrintCollection((Collection) o);
    else
        System.out.println(o.toString());
    }
}
```

好了，现在可以处理内嵌的集合对象，但其他对象返回的字符串不是你想要的呢？假如你想在字符串对象加上引号，想在Float对象后加一个f，你该怎么做？代码会变得更加复杂：

```java
public void messyPrintCollection(Collection collection) {
    Iterator iterator = collection.iterator()
    while (iterator.hasNext()) {
        Object o = iterator.next();
        if (o instanceof Collection)
            messyPrintCollection((Collection)o);
        else if (o instanceof String)
            System.out.println("'"+o.toString()+"'");
        else if (o instanceof Float)
            System.out.println(o.toString()+"f");
        else
            System.out.println(o.toString());
    }
}
```

代码很快就变杂乱了。你不想让代码中包含一大堆的if-else语句！怎么避免呢？访问者模式可以帮助你。

为实现访问者模式，你需要创建一个Visitor接口，为被访问的集合对象创建一个Visitable接口。接下来需要创建具体的类来实现Visitor和Visitable接口。这两个接口大致如下：

```java
public interface Visitor
{
	public void visitCollection(Collection collection);
    public void visitString(String string);
    public void visitFloat(Float float);
}

public interface Visitable
{
	public void accept(Visistor visitor);
}
```

对于一个具体的String 类， 可以这么实现
```java
public class VisitableString implements Visitable
{
	private String value;
    public VisitableString(String string) {
    	value = string;
    }
    public void accept(Visitor visitor) {
    	visitor.visitString(this);
    }
}
```

在accept 方法中，根据不同的类型，调用visitor 中对应的方法
```java
	visitor.visitString(this)
```
具体的Visitor 的实现方法如下：
```java
public class PrintVisitor implements Visitor
{
	public void visitCollection(Collection collection) {
    	Iterator iterator = collection.iterator();
        
        while(iterator.hasNext()) {
        	Object o = iterator.next();
            if (o instanceof Visitable)
            	((Visitable)o).accept(this);
        }
    }
    
    public void visitString(String string) {
    	System.out.println("'"+string+"'");
    }
    
    public void visitFloat(Float float) {
    	System.out.prntln(float.toString() + "f");
    }
}
```

到时候，只要实现了VisitableFloat类和VisitableCollection类并调用合适的visitor方法，你就可以去掉包含一堆if-else结构的messyPrintCollection方法，采用一种十分清爽的方式实现了同样的功能。visitCollection()方法调用了Visitable.accept(this)，而accept()方法又反过来调用了visitor中正确的方法。这就是双分派：Visitor调用了一个Visitable类中的方法，这个方法又反过来调用了Visitor类中的方法。

尽管实现visitor后，if-else语句不见了，但还是引入了很多附加的代码。你不得不将原始的对象——String和Float，打包到一个实现Visitable接口的类中。虽然很烦人，但这一般来说不是个问题。因为你可以限制被访问集合只能包含Visitable对象。

然而，这还有很多附加的工作要做。更坏的是，当你想增加一个新的Visitable类型时怎么办，比如VisitableInteger？这是访问者模式的一个主要缺点。如果你想增加一个新的Visitable类型，你不得不改变Visitor接口以及每个实现Visitor接口方法的类。你可以不把Visitor设计为接口，取而代之，可以把Visitor设计为一个带有空操作的抽象基类。这与Java GUI中的Adapter类很相似。这么做的问题是你会用尽单次继承，而常见的情形是你还想用继承实现其他功能，比如继承StringWriter类。这同样只能成功访问实现Visitable接口的对象。
幸运的是，Java可以让你的访问者模式更灵活，你可以按你的意愿增加Visitable对象。怎么实现呢？答案是使用反射。使用反射的ReflectiveVisitor接口只需要一个方法：

```java
public interface ReflectiveVisitor {
	public void visit(Object o);
}
```

好了，上面很简单。Visitable接口先不动，待会我会说。现在，我使用反射实现PrintVisitor类。

```java
public class PrintVisitor implements ReflectiveVisitor
{
	public void visitCollection(Collection collection)
    { ... same as above ... }
    public visitString(String string) 
    { ... same as above ... }
    public void visitFloat(Float float)
    { ... same as above ... }
    public void default(Object o) {
    	System.out.println(o.toString());
    }
    
    public void visit(Object o) {
    	String methodName = o.getClass().getName();
        methodName = "visit" + methodName.subString(methodName.lastIndexOf('.')+1);
        
        //使用反射调用 method
        try{
        	Method m = getClass().getMethod(methodName,
            new Class[]{o.getClass() });
            
            m.invoke(this,new Object[]{0});
        }catch(NoSuchMethodException e) {
        	default(0);
        }
    }
    
}
```
现在你无需使用Visitable包装类（包装了原始类型String、Float）。你可以直接访问visit()，它会调用正确的方法。visit()的一个优点是它会分派它认为合适的方法。这不一定使用反射，可以使用完全不同的一种机制。

在新的PrintVisitor类中，有对应于Collections、String和Float的操作方法；对于不能处理的类型，可以通过catch语句捕捉。对于不能处理的类型，可以通过扩展visit()方法来尝试处理它们的所有超类。首先，增加一个新的方法getMethod(Class c)，返回值是一个可被触发的方法。它会搜索Class c的所有父类和接口，以找到一个匹配方法。

```java
protected Method getMethod(Class c) {
	Class newc = c;
    Method m = null;
    
    while( m == null && newc != Object.class) {
    	String method = newc.getName();
        method = "visit" + method.subString(method.lastIndexOf('.') + 1);
        try{
        	m = getClass().getMethod(method,new Class[]{newc})
        }catch(NoSuchMethodException e) {
        	newc = newc.getSuperclass();
        }
    }
    
    if (newc == Object.class) {
    	Class[] interfaces = c.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
        	String method = interfaces[i].getName();
            method = "visit"+method.subString(method.lastIndexOf('.')+1);
            
            try{
            	m = getClass().getMethod(method,new Class[]{interfaces[i]});
            }catch (NoSuchMethodException e) {}
        }
    }
    
    if (m == null) {
    	try{
        m = thisclass.getMethod("visitObject",new Class[]{Object.class});
        }catch(Exception e) {
           // can not happen
        }
        
    }
    return m;
}
```
这看上去很复杂，实际上并不。大致来说，首先根据传入的class名称搜索可用方法；如果没找到，就尝试从父类搜索；如果还没找到，就从接口中尝试。最后，（仍没找到）可以使用visitObject()作为默认方法。

由于大家对传统的访问者模式比较熟悉，这里沿用了之前方法命名的惯例。但是，有些人可能注意到，把所有的方法都命名为“visit”并通过参数类型不同来区分，这样更高效。然而，如果你这么做，你必须把visit(Object o)方法的名称改为其他，比如dispatch(Object o)。否则，（当没有对应处理方法时），你无法退回到默认的处理方法，并且当你调用visit(Object o)方法时，为了确保正确的方法调用，你必须将参数强制转化为Object。

为了利用getMethod()方法，现在需要修改一下visit()方法。

```java
public void visit(Object object) {
	try{
    	Method method = getMethod(getClass(),object.getClass());
        method.invoke(this,new Object[]{object});
    }catch(Exception e) {
    
    }
}
```

现在，visitor类更加强大了——可以传入任意的对象并且有对应的处理方法。另外，有一个默认处理方法，visitObject(Object o)，的好处就是就可以捕捉到任何没有明确说明的类型。再稍微修改下，你甚至可以添加一个visitNull()方法。

我仍保留Visitable接口是有原因的。传统访问者模式的另一个好处是它可以通过Visitable对象控制对象结构的遍历顺序。举例来说，假如有一个实现了Visitable接口的类TreeNode，它在accept()方法中遍历自己的左右节点。

```java
public void accept(Visitor visitor) {
	visitor.visitTreeNode(this);
    visitor.visitTreeNode(leftsubtree);
    visitor.visitTreeNode(rightsubtree);
}
```

这样，只要修改下Visitor类，就可以通过Visitable 类控制遍历：

```java
public void visit(Object object) throws Exception
{
	Method method = getMethod(getClass(),object.getClass());
    method.invoke(this,new Object[]{object});
    if (object instanceof Visitable)
    {
    	callAccept((Visitable) object);
    }
}

public void callAccept(Visitable visitable) {
	visitable.accept(this);
}
```

如果你实现了Visitable对象的结构，你可以保持callAccept()不变，就可以使用Visitable控制的对象遍历。如果你想在visitor中遍历对象结构，你只需重写allAccept()方法，让它什么都不做。

当使用几个不同的visitor去操作同一个对象集合时，访问者模式的力量就会展现出来。比如，当前有一个解释器、中序遍历器、后续遍历器、XML编写器以及SQL编写器，它们可以处理同一个对象集合。我可以轻松地为这个集合再写一个先序遍历器或者一个SOAP编写器。另外，它们可以很好地兼容它们不识别的类型，或者我愿意的话可以让它们抛出异常。


######总结

使用Java反射，可以使访问者模式提供一种更加强大的方式操作对象结构，可以按照需求灵活地增加新的Visitable类型。我希望在你的编程之旅中可以使用访问者模式。





























[0]:http://www.importnew.com/11319.html
[1]:http://incdn1.b0.upaiyun.com/2014/05/dd1a0f5091fc4096e1ce3a99e2d6e468.png


