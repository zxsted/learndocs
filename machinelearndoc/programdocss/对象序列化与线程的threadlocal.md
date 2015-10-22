对象序列化与线程的threadlocal

[toc]
##### 使用ThreadLocal变量的时机和方法

[地址](http://www.importnew.com/14398.html)

并发编程中，一个重要的内容是数据共享。当你创建了实现Runnable接口的线程，然后开启使用相同Runnable实例的各种Thread对象，所有 的线程便共享定义在Runnable对象中的属性。也就是说，当你在一个线程中改变任意属性时，所有的线程都会因此受到影响，同时会看到第一个线程修改后的值。有时我们希望如此，比如：多个线程增大或减小同一个计数器变量；但是，有时我们希望确保每个线程，只能工作在它自己的线程实例的拷贝上，同时不会影 响其他线程的数据。

###### 使用ThreadLocal的时机
举个例子，想象你在开发一个电子商务应用，你需要为每一个控制器处理的顾客请求，生成一个唯一的事务ID，同时将其传到管理器或DAO的业务方法中，以便记录日志。一种方案是将事务ID作为一个参数，传到所有的业务方法中。但这并不是一个好的方案，它会使代码变得冗余。

你可以使用ThreadLocal类型的变量解决这个问题。首先在控制器或者任意一个预处理器拦截器中生成一个事务ID，然后在ThreadLocal中 设置事务ID，最后，不论这个控制器调用什么方法，都能从threadlocal中获取事务ID。而且这个应用的控制器可以同时处理多个请求，同时在框架 层面，因为每一个请求都是在一个单独的线程中处理的，所以事务ID对于每一个线程都是唯一的，而且可以从所有线程的执行路径获取。

扩展阅读： [与JAX-RS ResteasyProviderFactory共享上下文数据（ThreadLocalStack实例）](http://howtodoinjava.com/2013/05/13/share-context-data-with-jax-rs-resteasyproviderfactory/)

###### ThreadLocal 类
Java并发API 为使用ThreadLocal 类的局部线程变量提供了一个简洁高效的机制
```java
public class ThreadLocal<T> extends Object { ... }
```
这个类提供了一个局部线程变量。这些变量不同于其所对应的常规变量，对于常规变量，每个线程只能访问（通过get或set方法）其自身所拥有的，独立初始化变量拷贝。在一个类中，ThreadLocal类型的实例是典型的私有、静态（private static）字段，因为我们可以将其作为线程的关联状态（比如：用户ID或者事务ID）

这个类有以下方法：
1. get(): 返回当前线程拷贝的局部线程变量的值。
2. initialValue(): 返回当前线程赋予局部线程变量的初始值。
3. remove(): 移除当前线程赋予局部线程变量的值。
4. set(T value): 为当前线程拷贝的局部线程变量设置一个特定的值。

###### 如何使用ThreadLocal

下面的例子使用两个局部线程变量，即threadId和startDate。它们都遵循推荐的定义方法，即“private static”类型的字段。threadId用来区分当前正在运行的线程，startDate用来获取线程开启的时间。上面的信息将打印到控制台，以此验 证每一个线程管理他自己的变量拷贝。

```java

class DemoTask implements Runnable {

	// Aotmic integer containing the next thread ID to be assigned
    private static final AtomicInteger nextId = new AtomicInteger(0);
    
    // Thread local variable containing each thread`s ID
    private static final ThreadLocal<Integer> threadId =
    new ThreadLocal<Integer>(){
    
    	@Override
        protected Integer initialValue(){
        	return nextId.getAndIncrement();
        }
    };
    
    
 // Return the current thread`s unique ID ,assigning it if necessary
 public int getThreadId(){
 	return threadId.get();
 }
 
 // Returns the current thread`s starting timestammp
 private static final ThreadLocal<Date> startDate = 
 	new ThreadLocal<Date> {
    	protected Date initialValue() {
        	return new Date();
        }
    };
    
    @Override 
    public void run() {
    	System.out.printf("Starting Thread : %s : %s n",
         getThreadId(),startDate.get());
         
         try{
         	TimeUnit.SECONDS.sleep((int) Math.rint(Math.random() * 10));
         } catch ( InterruptedException e) {
           e.printStackTrace();
         }
         System.out.println("Thread finished:%s : %s n",
         getThreadId(),startDate.get());
    }
}
```

现在要验证变量本质上能够维持其自身状态，而与多线程的多次初始化无关。我们首先需要创建执行这个任务的三个线程，然后开启线程，接着验证它们打印到控制台中的信息。

```shell
Starting Thread: 0 : Wed Dec 24 15:04:40 IST 2014
Thread Finished: 0 : Wed Dec 24 15:04:40 IST 2014
 
Starting Thread: 1 : Wed Dec 24 15:04:42 IST 2014
Thread Finished: 1 : Wed Dec 24 15:04:42 IST 2014
 
Starting Thread: 2 : Wed Dec 24 15:04:44 IST 2014
Thread Finished: 2 : Wed Dec 24 15:04:44 IST 2014
```

在上面的输出中，打印出的声明序列每次都在变化。我已经把它们放到了序列中，这样对于每一个线程实例，我们都可以清楚地辨别出，局部线程变量保持着安全状态，而绝不会混淆。自己尝试下！

局部线程通常使用在这样的情况下，当你有一些对象并不满足线程安全，但是你想避免在使用synchronized关键字、块时产生的同步访问，那么，让每个线程拥有它自己的对象实例。

注意：局部变量是同步或局部线程的一个好的替代，它总是能够保证线程安全。唯一可能限制你这样做的是你的应用设计约束。

警告：在webapp服务器上，可能会保持一个线程池，那么ThreadLocal变量会在响应客户端之前被移除，因为当前线程可能被下一个请求重复使用。而 且，如果在使用完毕后不进行清理，它所保持的任何一个对类的引用—这个类会作为部署应用的一部分加载进来—将保留在永久堆栈中，永远不会被垃圾回收机制回收。

##### JAVA 序列化 

[传送](http://www.importnew.com/14465.html)

Java序列化是在JDK 1.1中引入的，是Java内核的重要特性之一。Java序列化API允许我们将一个对象转换为流，并通过网络发送，或将其存入文件或数据库以便未来使用，反序列化则是将对象流转换为实际程序中使用的Java对象的过程。Java同步化过程乍看起来很好用，但它会带来一些琐碎的安全性和完整性问题，在文章的后面部分我们会涉及到，以下是本教程涉及的主题。

1. Java序列化接口
2. 使用序列化和serialVersionUID进行类重构
3. Java外部化接口
4. Java序列化方法
5. 序列化结合继承
6. 序列化代理模式

###### Java序列化接口
如果你希望一个类对象是可序列化的，你所要做的是实现java.io.Serializable接口。序列化一种标记接口，不需要实现任何字段和方法，这就像是一种选择性加入的处理，通过它可以使类对象成为可序列化的对象。

序列化处理是通过ObjectInputStream和ObjectOutputStream实现的，因此我们所要做的是基于它们进行一层封装，要么将其保存为文件，要么将其通过网络发送。我们来看一个简单的序列化示例。

```java

import java.io.Serializable;

public class Employee implements Serializable {

	// private staic final long serialVersionUID = -6470090944414208496L;
 
 private String name;
 private int id;
 transient private int salary;
 private String passwowrd

@Override
public String toString() {
	return "Employee { name = " + name +",id="+id+",salary="+salary+"}" 
}
 /*===========  getter and setter=============*/
}
```
注意一下，这是一个简单的java bean，拥有一些属性以及getter-setter方法，如果你想要某个对象属性不被序列化成流，你可以使用transient关键字，正如示例中我在salary变量上的做法那样。

现在我们假设需要把我们的对象写入文件，之后从相同的文件中将其反序列化，因此我们需要一些工具方法，通过使用ObjectInputStream和ObjectOutputStream来达到序列化的目的。

```java
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
*  A simple class with generic serialize and deseriablize  * method implementions
* @auhtor ted
*/

public class SerializationUtil {
	// deserialize to Object from given file
    public static Object deserialize(String fileName) throws IOException ,ClassNotFoundException {
    FileInputStream fis = new FileInputStream(fileName);
    ObjectInputStream ois = new ObjectInputStream(fis);
    Object obj = ois.readObject();
    ois.close();
    return obj;
    }
    
    // serialize the given object and save it ti file
    public static void serialize(Object obj,String fileName) throws IOException {
    FileOutputStream fos = new FileOutputStream(fileName);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(obj);
    
    fos.close();
    }
}
```
注意一下，方法的参数是Object，它是任何Java类的基类，这样写法以一种很自然的方式保证了通用性。

现在我们来写一个测试程序，看一下Java序列化的实战。

```java
import java.io.Exception;

public class SerializationTest{
 public static void main(String[] args) [
 	String filename = "employee.ser";
    Employee emp = new Employee();
    emp.setId(100);
    emp.setName("Pankaj");
    emp.setSalary(5000);
    
    // serialize to file
    try{
    	SerializatiionUtil.serialize(emp,filename);
    }catch(IOException e) {
    	e.printStackTrace();
        return;
    }
    
    Employee empNew = null;
    try{
    	empNew = (Employee) SerializationUtil.deserialize(filename);
    }catch(ClassNotFoundException | IOException e) {
    	e.printStackTrace();
    }
    
    System.out.println("emp Object::" + emp);
    System.out.println("empNew Object::" + empNew);
 }
}
```

运行输出为：

```shell
emp Object::Employee{name=Pankaj,id=100,salary=5000}
empNew Object::Employee{name=Pankaj,id=100,salary=0}
```
由于salary是一个transient变量，它的值不会被存入文件中，因此也不会在新的对象中被恢复。类似的，静态变量的值也不会被序列化，因为他们是属于类而非对象的。

###### 使用序列化和serialVersionUID 进行类重构

Java序列化允许java类中的一些变化，如果他们可以被忽略的话。一些不会影响到反序列化处理的变化有：

1. 在类中添加一些新的变量。
2. 将变量从transient转变为非tansient，对于序列化来说，就像是新加入了一个变量而已。
3. 将变量从静态的转变为非静态的，对于序列化来说，就也像是新加入了一个变量而已。

不过这些变化要正常工作，java类需要具有为该类定义的serialVersionUID，我们来写一个测试类，只对之前测试类已经生成的序列化文件进行反序列化。

```java
import java.io.IOException;

public class DeserializationTest {
	public static void main(String[] args) {
    	String filename = "empoyee.ser";
        Employee empNew = null;
        
        try{
        	empNew = (Employee) SerializationUtil.deserialize(filename);
        }catch(ClassNotFoundException | IOException e) {
        	e.printStackTrace();
        }
        
        System.out.println("empNew Object::" + empNew);
    }
}
```
现在，在Employee类中去掉”password”变量的注释和它的getter-setter方法，运行。你会得到以下异常。

```shell
java.io.InvalidClassException: com.journaldev.serialization.Employee; local class incompatible: stream classdesc serialVersionUID = -6470090944414208496, local class serialVersionUID = -6234198221249432383
    at java.io.ObjectStreamClass.initNonProxy(ObjectStreamClass.java:604)
    at java.io.ObjectInputStream.readNonProxyDesc(ObjectInputStream.java:1601)
    at java.io.ObjectInputStream.readClassDesc(ObjectInputStream.java:1514)
    at java.io.ObjectInputStream.readOrdinaryObject(ObjectInputStream.java:1750)
    at java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1347)
    at java.io.ObjectInputStream.readObject(ObjectInputStream.java:369)
    at com.journaldev.serialization.SerializationUtil.deserialize(SerializationUtil.java:22)
    at com.journaldev.serialization.DeserializationTest.main(DeserializationTest.java:13)
empNew Object::null
```

原因很显然，上一个类和新类的serialVersionUID是不同的，事实上如果一个类没有定义serialVersionUID，它会自动计算出来并分配给该类。Java使用类变量、方法、类名称、包，等等来产生这个特殊的长数。如果你在任何一个IDE上工作，你都会得到警告“可序列化类Employee没有定义一个静态的final的serialVersionUID，类型为long”。

我们可以使用java工具”serialver”来产生一个类的serialVersionUID，对于Employee类，可以执行以下命令。
```shell
SerializtionExample/bin$serialver -classpath . com.xxx.xxx.Employee
```

记住，从程序本身生成序列版本并不是必须的，我们可以根据需要指定值，这个值的作用仅仅是告知反序列化处理机制，新的类是相同的类的新版本，应该进行可能的反序列化处理。

举个例子，在Employee类中仅仅将serialVersionUID字段的注释去掉，运行SerializationTest程序。现在再将Employee类中的password字段的注释去掉，运行DeserializationTest程序，你会看到对象流被成功地反序列化了，因为Employee类中的变动与序列化处理是相容的。

###### Java 外部化接口
如果你在序列化处理中留个心，你会发现它是自动处理的。有时候我们想要去隐藏对象数据，来保持它的完整性，可以通过实现java.io.Externalizable接口，并提供writeExternal()和readExternal()方法的实现，它们被用于序列化处理。

```java

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


public classes implements Externalizable {
	
    private int id;
    private String name;
    private String gender;
    
    @Override
    public void writeExternal(ObjectOuput out) throws IOException {
    	out.writeInt(id)l
        out.writeObject(name + "xyz");
        out.writeObject("abc" + gender);
    }
    
    @Override
    public void readExternal(ObjectInput in ) throws IOException ,ClassNotFoundException{
    	id = in.readInt();
        // 读取要与写入时 顺序相同
        name = (String) in.readObject();
        if (!name.endsWith("xyz"))
        	throw new IOExcpetion("corrupted data");
        name = name.substring(0,name.length() - 3);
        
        gender = (String) in.readObject();
        if (!gender.startWith("abc"))
        	throw new IOException("coorrupted data");
        gender=gender.substring(3);
    }
    
    @Override
    public String toString() {
    	return "Person{id=" + id + ",name="+name+",gender="+gender+"}";
    }
    
    /*===============geter and setter==================*/
}
```

注意，在将其转换为流之前，我已经更改了字段的值，之后读取时会得到这些更改，通过这种方式，可以在某种程度上保证数据的完整性，我们可以在读取流数据之后抛出异常，表明完整性检查失败。来看一个测试程序。

```java

import java.io.FileInputStream;
import java.io.FileOutputSteam;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class ExternalizationTest {

	public static void mian(String[] args) {
    
    	String fileName = "person.ser";
        Person person = new Person();
        person.setId(1);
        person.setName("pamkaj");
        person.serGender("Male");
        
        try{
        	FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(person);
            oos.close();
        }catch(IOException e) [
        	e.printStackTrace();
        }
        
        FileInputStream fis;
        
        try{
        	fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Person p =(Person) ois.readObject();
            ois.close();
            System.out.println("Person Object read = " + p);
        } catch (IOExcpetion  | ClassNotFoundException e) {
        e.printStackTrace();
        }
    }
}
```
运行以上测试程序，可以得到以下输出。
```python
Person Object Read=Person{id=1,name=Pankaj,gender=Male}
```
那么哪个方式更适合被用来做序列化处理呢？实际上使用序列化接口更好，当你看到这篇教程的末尾时，你会知道原因的。


###### Java 序列化方法

我们已经看到了，java的序列化是自动的，我们所要做的仅仅是实现序列化接口，其实现已经存在于ObjectInputStream和ObjectOutputStream类中了。不过如果我们想要更改存储数据的方式，比如说在对象中含有一些敏感信息，在存储/获取它们之前我们要进行加密/解密，这该怎么办呢？这就是为什么在类中我们拥有四种方法，能够改变序列化行为。

如果以下方法在类中存在，它们就会被用于序列化处理。

    readObject(ObjectInputStream ois)：如果这个方法存在，ObjectInputStream readObject()方法会调用该方法从流中读取对象。
    writeObject(ObjectOutputStream oos)：如果这个方法存在，ObjectOutputStream writeObject()方法会调用该方法从流中写入对象。一种普遍的用法是隐藏对象的值来保证完整性。
    Object writeReplace()：如果这个方法存在，那么在序列化处理之后，该方法会被调用并将返回的对象序列化到流中。
    Object readResolve()：如果这个方法存在，那么在序列化处理之后，该方法会被调用并返回一个最终的对象给调用程序。一种使用方法是在序列化类中实现单例模式，你可以从序列化和单例中读到更多知识。

通常情况下，当实现以上方法时，应该将其设定为私有类型，这样子类就无法覆盖它们了，因为它们本来就是为了序列化而建立的，设定为私有类型能避免一些安全性问题。


###### 序列化结合继承

有时候我们需要对一个没有实现序列化接口的类进行扩展，如果依赖于自动化的序列化行为，而一些状态是父类拥有的，那么它们将不会被转换为流，因此以后也无法获取。

在此，readObject()和writeObject()就可以派上大用处了，通过提供它们的实现，我们可以将父类的状态存入流中，以便今后获取。我们来看一下实战。

```java
public class SuperClass {
	
    private int id;
    private String value;
    
    /*========getter and setter==============*/
}
```

父类是一个简单的java bean ，没有实现序列化接口
```java

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputValidation;
import java.io.Serializable;

public class SubClass extends SuperClass implemts Serializable,ObjectInputValid {
	
    private static final long serialVersionUID = -1322322139926390329L;
    
    private String name;
    
    /*========setter and getter====================*/
    ....
    /*=============================================*/
    
    @Override
    public String toString() {
    	return "SubClass { id=" + getId()+",value="+getValue()+",name="+getName()+"}";
    }
    
    // 为父类添加序列化和反序列化辅助方法
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
    
    // 写入和写出的顺序必须相同
    setId(ois.readInt());
    serValue((String) ois.readObject());
    }
    
    private void writeObject(ObjectOutputStream oos)
    throws IOException,ClassNotFoundException {
    	oos.defaultWriteObject();
        
        oos.writeInt(getId());
        oos.writeObject(getValue());
    }
    
    @Override
    public void validateObject() throws InvalidObjectException {
    	//validate the object here
        if (name == null || "".equals(name))
        throw new IvalidObjectException("name can be null  or empty");
        if (getId()<=0) throw new InvalidObjectException("ID can not be negative or zero");
    }
}
```

注意，将额外数据写入流和读取流的顺序应该是一致的，我们可以在读与写之中添加一些逻辑，使其更安全。

同时还需要注意，这个类实现了ObjectInputValidation接口，通过实现validateObject()方法，可以添加一些业务验证来确保数据完整性没有遭到破坏。

以下通过编写一个测试类，看一下我们是否能够从序列化的数据中获取父类的状态。

```java
import java.io.IOException;

import com.journaldev.serialization.SerializationUtil;

public class InheritanceSerializationTest {

	public static void main(String[] args) {
    
    	String fileName = "subClass.ser";
        
        SubClass subClass = new SubClass();
        subClass.setId(0);
        subClass.setValue("Date")
        subClass.setName("Pankaj");
        
        try{
        	serializationUtil.serialize(subClass,fileName);
            
        }catch(IOException e) {
        	e.printStackTrace();
            return;
        }
        
        try{
        	SubClass subNew = (SubClass) SerializationUtil.deserialize(fileName);
            System.out.println("SubCllass read = " + subNew);
        } catch(ClassNotFoundException | IOException e) {
        	e.printStackTrace();
        }
        
    }
}
```

运行以上测试程序，可以得到以下输出：
```java
SubClass read = SubClass{id = 10,value=Data,name=Pankaj};
```
因此通过这种方式，可以序列化父类的状态，即便它没有实现序列化接口。当父类是一个我们无法改变的第三方的类，这个策略就有用武之地了。

###### 序列化代理模式

Java序列化也带来了一些严重的误区，比如：

  类的结构无法大量改变，除非中断序列化处理，因此即便我们之后已经不需要某些变量了，我们也需要保留它们，仅仅是为了向后兼容。
  序列化会导致巨大的安全性危机，一个攻击者可以更改流的顺序，继而对系统造成伤害。举个例子，用户角色被序列化了，攻击者可以更改流的值为admin，再执行恶意代码。

序列化代理模式是一种使序列化能达到极高安全性的方式，在这个模式下，一个内部的私有静态类被用作序列化的代理类，该类的设计目的是用于保留主类的状态。这个模式的实现需要合理实现readResolve()和writeReplace()方法。

让我们先来写一个类，实现了序列化代码模式，之后再对其进行分析，以便更好的理解原理。

```java
import java.io.InvalidObjectException;
import java.io.ObjectInputStreaml;
import java.io.Serializable;

public class Data implements Serializable {

	private static final long serialVersionUID = 2087368867376448459L;
    rivate String data;
 
    public Data(String d){
        this.data=d;
    }
 
    public String getData() {
        return data;
    }
 
    public void setData(String data) {
        this.data = data;
    }
 
    @Override
    public String toString(){
        return "Data{data="+data+"}";
    }
    
    
    // 使用静态内部类实现序列化代理模式
    private static class DateProxy implements Serializable{
    private static final long serialVersionUID = 8333905273185436744L;
    private String dataProxy;
    private static final String PREFIX = "ABC";
    private static final String SUFFIX = "DEFG";
    
    public DataProxy (Data d) {
    	// obscuring data for security
        this.dataProxy = PREFIX + d.data + SUFFIX;
    }
    
    private Object readResolve() throws InvalidObjectException {
    	if (dataProxy.startsWith(PREFIX) && dataProxy.endsWith(SUFFIX)) {
        return new Data(dataProxy.substring(3,dataProxy.length() -4));
        } else 
        throw new InvalidObjectException("data corrupted");
    }
    
    }
    
    // replacing serialized object to DataProxy object
    
    private Object writeReplace(){
    	return new DataProxy(this);
    }
    private void readObject(ObjectInputStream ois) 
    throws InvalidObjectException {
    	throw new IvalidObjectException("Proxy is not used,something fishy");
    }
}
```


* Data和DataProxy类都应该实现序列化接口。
* DataProxy应该能够保留Data对象的状态。
* DataProxy是一个内部的私有静态类，因此其他类无法访问它。
* DataProxy应该有一个单独的构造方法，接收Data作为参数。
* Data类应该提供writeReplace()方法，返回DataProxy实例，这样当Data对象被序列化时，返回的流是属于DataProxy类的，不过DataProxy类在外部是不可见的，所有它不能被直接使用。
* DataProxy应该实现readResolve()方法，返回Data对象，这样当Data类被反序列化时，在内部其实是DataProxy类被反序列化了，之后它的readResolve()方法被调用，我们得到了Data对象。
* 最后，在Data类中实现readObject()方法，抛出InvalidObjectException异常，防止黑客通过伪造Data对象的流并对其进行解析，继而执行攻击。

下面对其进行测试， 检查它是否正常工作
```java
import java.io.IOException;
import com.journaldev.serialization.SerializationUtil;

public class SerializationProxyTest{

	public static void main(String[] args) {
    	String filename = "data.ser";
        
        Data data = new Date("Pankaj");
        
        try {
        	SerializationUtil.serialize(data,fileName);
        }catch(IOException e) {
        	e.printStackTrace();
        }
        
        try{
        	SerializationUtil.deserialize(fileName);
            System.out.println(newData);
        } catch(ClassNotFoundException  | IOException e) {
        	e.printStackTrace();
        }
    }
}
```

运行以上的测试程序,输出如下内容：
```java
Data{data=Pankaj}
```
如果你打开data.ser文件，可以看到DataProxy对象已经被作为流存入了文件中。

这就是Java序列化的所有内容，看上去很简单但我们应当谨慎地使用它，通常来说，最好不要依赖于默认实现。你可以从上面的链接中下载项目，玩一玩，这能让你学到更多。




























