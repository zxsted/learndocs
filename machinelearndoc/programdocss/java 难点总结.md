####java 难点总结

[toc]
#####Java中创建数组的总结
[传送](http://blog.csdn.net/eric_sunah/article/details/7262486)
######间接创建java繁星数组的方法
在java中，不能通过T[] tarr = new T[10]的方式来创建数组，最简单的方式是通过Array.newInstance(Class<t> type ,int size)的方式来创建数组，如下面的程序：
```java
public class ArrayMaker<T> {
	private Class<T> type;
    
    public ArrayMaker(Class<T> type) {
    	this.type = type;
    }
    
    T[] createArray(int size) {
    	return (T[]) Array.newInstance(type,size);
    }
    
    List<T> createList(){
    	return new ArrayList<T>();
    }
    
    public static void main(String[] args) {
    /*   
         * Even though kind is stored as Class<T> , erasure means that it is actually just being stored as a Class, with   
         * no parameter. So, when you do some thing with it, as in creating an array, Array.newInstance( ) doesn’t   
         * actually have the type information that’s implied in kind; so it cannot produce the specific result, wh ich   
         * must therefore be cast, which produces a warning that you cannot satisfy.   
         */    
     ArrayMaker<Type> am2 = new ArrayMaker<Type>(Type.class);
     System.out.println(Arrays.asList(am2.createArray(10)));
     System.out.println(Arrays.asList(am2.createList()));
}
}

class Type {
	@Override
    public String toString(){
    	return "type";
    }
}
```
上面的这个例子比较简单，但是如果你有接触过泛型数组，你便对他的复杂度有一定的了解，由于创建泛型数组比较复杂，所以在实际的应用过程中一般会选择List的对泛型进行存储，如果实在需要使用泛型数组，则需要注意数组的在运行时的类型，think in java这本书中，对泛型数组的处理通过四个小程序对其进行了比较完整的描述。
######程序一
下面这个程序说明了，使用泛型数组中容易出现的问题，
```java
class Generic<T> {

}

public class ArrayofGeneric{
	public static void main(String[] args) {
    	Generic<Integer>[] genArr;
        /*   
         * will throw ClassCastException :The problem is that arrays keep track of their actual type, and that type is   
         * established at the point of creation of the array. So even though genArr has been cast to a Generic < Integer   
         * >[] , that information only exists at compile time (and without the @SuppressWarnings annotation, you’d get a   
         * warning for that cast). At run time, it’s still an array of Object, and that causes problems.   
         */    
        // genArr = (Generic<Integer>[]) new Object[] {};    
        /* can not create a generic of array */    
        // genArr=new Generic<Integer>[2];    
        genArr = (Generic<Integer>[]) new Generic[2];    
        System.out.println(genArr);      
        
    }
}
```
######程序二
这个程序主要是说明在程序的执行过程中，泛型数组的类型信息会被擦除，且在运行的过程中数组的类型有且仅有Object[]，如果我们强制转换成T[]类型的话，虽然在编译的时候不会有异常产生，但是运行时会有ClassCastException抛出。
```java
/**   
 *    
 * Because of erasure, the runtime type of the array can only be Object[]. If we immediately cast it to T[], then at   
 * compile time the actual type of the array is lost, and the compiler may miss out on some potential error checks.   
 *    
 *    
 *    
 * archive $ProjectName: $   
 *    
 * @author Admin   
 *    
 * @version $Revision: $ $Name: $   
 */ 
 
public class ArrayOfGeneric2<T> {
	public T[] ts;
    
    public ArrayOfGenric2(int size) {
    	ts = (T[]) new Object[size];
    }
    
    public T get(int index) {
    	return ts[index];
    }
    
    public T[] rep(){
    	return ts;
    }
    
    public void set(int index,T t) {
    	ts[index] = t;
    }
    
    public static void main(String[] args) {
    	ArrayOfGeneric2<String> aog2 = new ArrayOfGeneric2<String>(10);
        Object[] objs = aog2.rep();
        System.out.println(objs);
        
        /*will throw ClassCastException*/
        //String[] strs = aog2.rep();
        //System.out.println(strs);
    }
}
```

######程序三：
主要说明的是在对象中通过对Object[] 来保存数据时，则生成对象是可以对其持有的对象在T和Object之间进行转换，但是当设计到数组的转换时，还是会报ClassCastException
```java
/**   
 *    
 * Initially, this doesn’t look very different compare with ArrayOfGeneric2.java , just that the cast has been moved.   
 * Without the ©SuppressWarnings annotations, you will still get "unchecked" warnings. However, the internal   
 * representation is now Object[] rather than T[]. When get( ) is called, it casts the object to T, which is in fact the   
 * correct type, so that is safe. However, if you call rep( ) , it again attempts to cast the Object[] to a T[], which   
 * is still incorrect, and produces a warning at compile time and an exception at run time. Thus there’s no way to   
 * subvert the type of the underlying array, which can only be Object[]. The advantage of treating array internally as   
 * Object[] instead of T[] is that it’s less likely that you’ll forget the runtime type of the array and accidentally   
 * introduce a bug (although the majority, and perhaps all, of such bugs would be rapidly detected at run time)   
 *    
 *    
 *    
 * archive $ProjectName: $   
 *    
 * @author Admin   
 *    
 * @version $Revision: $ $Name: $   
 */    
 
 public class ArrayOfGeneric3<T> {
 	Object[] ts;
    
    public ArrayOfGeneric3(int size) {
    	ts = new Object[size];
    }
    
    public T get(int index) {
    	return (T) ts[index];
    }
    
    public T[] rep() {
    	return (T[]) ts;
    }
    
    public void set(int index,T t) {
    	ts[index] = t;
    }
    
    
    public static void main(String[] args) {
    	ArrayOfGeneric3<Integer> aog2 = new ArrayOfGeneric3<Integer>(10);
        Ojbect[] objs = aog2.rep();
        for(int i = 0; i < 10; i++) {
        	aog2.set(i,i);
            System.out.println(aog2.get(i));
        }
        Integer[] strs = aog2.rep();
        System.out.println(strs);
    }
 }
```

######程序4 ： 是泛型数组相对而言比较完美的解决方案
```java
/**   
 *    
 * The type token Class<T> is passed into the constructor in order to recover from the erasure, so that we can create   
 * the actual type of array that we need, although the warning from the cast must be suppressed with @SuppressWarnings.   
 * Once we do get the actual type, we can return it and get the desired results, as you see in main( ). The runtime type   
 * of the array is the exact type T[].   
 *    
 * @author Admin   
 *    
 * @version $Revision: $ $Name: $   
 */    
 public class ArrayOfGeneric4<T> {
 	T[] ts;
    
    public ArrayOfGeneric4(Class<T> type,int size) {
    	/*to sulution array of generic key codel*/
        ts = (T []) Array.newInstance(type,size);
    }
    
    public T get(int index) {
    	return ts[index];
    }
    
    public T[] rep() {
    	return ts;
    }
    
    public void set(int index,T t) {
    	ts[index] = t;
    }
    
    public static void main(String[] args) {
    	ArrayOfGeneric4<Integer> aog2 = new ArrayOfGeneric4<Integer>(Integer.class,10);
        Object[] objs = ago2.rep();
        for(int i = 0; i < 10; i++) {
        	aog2.set(i,i);
            System.out.println(aog2.get(i));
        }
        try{
        	Integer[] strs = aog2.rep();
            System.out.println("user Array.newInstance to create generic of array was successfull !!!!!");
        }catch(Exception ex) {
        	ex.printStackTrace();
        }
    }
 }
```

######一个实用的例子
[传送](http://www.oschina.net/code/snippet_86510_1141)
```java
import java.lang.reflect.Array;

/**
*数组的工具
*@author David Day
*/

public class ArrayUtils {
	/**
     * 根据数组类型的class创建对应类型的数组
     * @param <T> 目标类型
     * @param clazz
     * @param length 数组长度
     * @return
    */
    public static<T> newArrayByArrayClass(Class<T[]> clazz,int length) 	   {
    	return (T[]) Array.newInstance(clazz.getComponnentType(),length);
    	}
        
    /**
    * 根据普通类型的class创建数组
     * @param <T> 目标类型
     * @param clazz
     * @param length 数组长度
     * @return
    */
    public static<T> T[] newArrayByClass(Class<T> clazz,int length) {
    	return (T[]) Array.newInstance(clazz,length);
    }
    
    public static void main(String[] args) {
    //判断一个class是否是数组类型，可以使用Class实例的isArray方法
    String[] byArray = newArrayClass(String[].class,10);
    String[] byOne	= newArrayByClass(String.class,10);
    }
    
}

```

######两个较详细的教程：
1. Java总结篇系列：Java泛型 : http://www.cnblogs.com/lwbqqyumidi/p/3837629.html
2. Java笔记 – 泛型 泛型方法 泛型接口 擦除 边界 通配符 : 
http://www.itzhai.com/java-bi-ji-fan-xing-fan-xing-fang-fa-fan-xing-jie-kou-ca-chu-bian-jie-tong-pei-fu.html
Java实现双数组Trie树(DoubleArrayTrie,DAT):http://blog.csdn.net/dingyaguang117/article/details/7608568
http://www.cnblogs.com/lwbqqyumidi/category/395241.html


#####java序列化
###### 简介：
Java平台允许我们在内存中创建可复用的Java对象，但一般情况下，只有当JVM处于运行时，这些对象才可能存在，即，这些对象的生命周期不会比JVM的生命周期更长。但在现实应用中，就可能要求在JVM停止运行之后能够保存(持久化)指定的对象，并在将来重新读取被保存的对象。Java对象序列化就能够帮助我们实现该功能。

使用Java对象序列化，在保存对象时，会把其状态保存为一组字节，在未来，再将这些字节组装成对象。必须注意地是，对象序列化保存的是对象的"状态"，即它的成员变量。由此可知，对象序列化不会关注类中的静态变量。

除了在持久化对象时会用到对象序列化之外，当使用RMI(远程方法调用)，或在网络中传递对象时，都会用到对象序列化。Java序列化API为处理对象序列化提供了一个标准机制，该API简单易用，在本文的后续章节中将会陆续讲到。
######实例
在Java中，只要一个类实现了java.io.Serializable接口，那么它就可以被序列化。此处将创建一个可序列化的类Person，本文中的所有示例将围绕着该类或其修改版。

Gender类，是一个枚举类型，表示性别
```java
public enum Gender{
	MALE,FEMALE
}
```
如果熟悉Java枚举类型的话，应该知道每个枚举类型都会默认继承类java.lang.Enum，而该类实现了Serializable接口，所以枚举类型对象都是默认可以被序列化的。

Person类，实现了Serializable接口，它包含三个字段：name，String类型；age，Integer类型；gender，Gender类型。另外，还重写该类的toString()方法，以方便打印Person实例中的内容。
```java
public class Person implements Serializable {  
 
    private String name = null;  
 
    private Integer age = null;  
 
    private Gender gender = null;  
 
    public Person() {  
        System.out.println("none-arg constructor");  
    }  
 
    public Person(String name, Integer age, Gender gender) {  
        System.out.println("arg constructor");  
        this.name = name;  
        this.age = age;  
        this.gender = gender;  
    }  
 
    public String getName() {  
        return name;  
    }  
 
    public void setName(String name) {  
        this.name = name;  
    }  
 
    public Integer getAge() {  
        return age;  
    }  
 
    public void setAge(Integer age) {  
        this.age = age;  
    }  
 
    public Gender getGender() {  
        return gender;  
    }  
 
    public void setGender(Gender gender) {  
        this.gender = gender;  
    }  
 
    @Override 
    public String toString() {  
        return "[" + name + ", " + age + ", " + gender + "]";  
    }  
} 
```
SimpleSerial，是一个简单的序列化程序，它先将一个Person对象保存到文件person.out中，然后再从该文件中读出被存储的Person对象，并打印该对象。
```java
public class SimpleSerial {
	public static void main(String[] args) throws Exception{
    	File file = new File("person.out");
        
        ObjectOutputStream oout = new ObjectOutputStream(new FIleOutputStream(file));
        Person person = new Person("john",100,Gender.MALE);
        oout.writeObject(person);
        oout.close();
        
        ObjectInputStream oin = new ObjectInputStream(new FileInputStream(file));
        Object newPerson = oin.readObject(); //没有强制类型转换到Person类型
        oin.close();
        System.out.println(newPerson);
    }
}
```
上述程序输出的结果为：
```java
arg constructor
[john ,31,MALE]
```
此时必须注意的是，当重新读取被保存的Person对象时，并没有调用Person的任何构造器，看起来就像是直接使用字节将Person对象还原出来的。

当Person对象被保存到person.out文件中之后，我们可以在其它地方去读取该文件以还原对象，但必须确保该读取程序的CLASSPATH中包含有Person.class(哪怕在读取Person对象时并没有显示地使用Person类，如上例所示)，否则会抛出ClassNotFoundException。

######Serializable的作用
为什么一个类实现了Serializable接口，它就可以被序列化呢？在上节的示例中，使用ObjectOutputStream来持久化对象，在该类中有如下代码：
```java
private void writeObject0(Object obj, boolean unshared) throws IOException {  
      ...
    if (obj instanceof String) {  
        writeString((String) obj, unshared);  
    } else if (cl.isArray()) {  
        writeArray(obj, desc, unshared);  
    } else if (obj instanceof Enum) {  
        writeEnum((Enum) obj, desc, unshared);  
    } else if (obj instanceof Serializable) {  
        writeOrdinaryObject(obj, desc, unshared);  
    } else {  
        if (extendedDebugInfo) {  
            throw new NotSerializableException(cl.getName() + "\n" 
                    + debugInfoStack.toString());  
        } else {  
            throw new NotSerializableException(cl.getName());  
        }  
    }  
    ...  
} 
```
从上述代码可知，如果被写对象的类型是String，或数组，或Enum，或Serializable，那么就可以对该对象进行序列化，否则将抛出NotSerializableException。

######默认序列化机制
如果仅仅只是让某个类实现Serializable接口，而没有其它任何处理的话，则就是使用默认序列化机制。使用默认机制，在序列化对象时，不仅会序列化当前对象本身，还会对该对象引用的其它对象也进行序列化，同样地，这些其它对象引用的另外对象也将被序列化，以此类推。所以，如果一个对象包含的成员变量是容器类对象，而这些容器所含有的元素也是容器类对象，那么这个序列化的过程就会较复杂，开销也较大。

######控制序列化
* transient关键字
当某个字段被声明为transient后，默认序列化机制就会忽略该字段。此处将Person类中的age字段声明为transient，如下所示，
```java
public class Person implements Serializable {
	...
    transient private Integer age = null;
    ...
}
```
再次执行SimpleSerial程序，会有以下输出：
```java
arg constructor
[jhon ,null, MALE]
```
可见，age字段未被序列化
* writeObject()方法与readObject（）方法
对于上述已被声明为transitive的字段age，除了将transitive关键字去掉之外，是否还有其它方法能使它再次可被序列化？方法之一就是在Person类中添加两个方法：writeObject()与readObject()，如下所示：

```java
public class Person implements Serializable {
 ...
 transient private Integer age = null;
 ...
}

private void writeObject(ObjectOutputStream out) throws IOException{
 out.defaultWriteObject();
 out.WriteInt(age);
}

private void readObject(ObjectInputStream in) throws IOException,ClassNotFOundException{
	in.defaultReaderObject();
    age = in.readInt();
}
}
```

在writeObject()方法中会先调用ObjectOutputStream中的defaultWriteObject()方法，该方法会执行默认的序列化机制，如5.1节所述，此时会忽略掉age字段。然后再调用writeInt()方法显示地将age字段写入到ObjectOutputStream中。readObject()的作用则是针对对象的读取，其原理与writeObject()方法相同。再次执行SimpleSerial应用程序，则又会有如下输出：
```java
arg constructor  
[John, 31, MALE] 
```
必须注意地是，writeObject()与readObject()都是private方法，那么它们是如何被调用的呢？毫无疑问，是使用反射。详情可以看看ObjectOutputStream中的writeSerialData方法，以及ObjectInputStream中的readSerialData方法。

* Externalizable接口
无论是使用transient关键字，还是使用writeObject()和readObject()方法，其实都是基于Serializable接口的序列化。JDK中提供了另一个序列化接口--Externalizable，使用该接口之后，之前基于Serializable接口的序列化机制就将失效。此时将Person类作如下修改

```java
public class Person implements Externalizable {  
 
    private String name = null;  
 
    transient private Integer age = null;  
 
    private Gender gender = null;  
 
    public Person() {  
        System.out.println("none-arg constructor");  
    }  
 
    public Person(String name, Integer age, Gender gender) {  
        System.out.println("arg constructor");  
        this.name = name;  
        this.age = age;  
        this.gender = gender;  
    }  
 
    private void writeObject(ObjectOutputStream out) throws IOException {  
        out.defaultWriteObject();  
        out.writeInt(age);  
    }  
 
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {  
        in.defaultReadObject();  
        age = in.readInt();  
    }  
 
    @Override 
    public void writeExternal(ObjectOutput out) throws IOException {  
 
    }  
 
    @Override 
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {  
 
    }  
    ...  
} 
```
此时再执行SimpleSerial程序后会得到以下结果
```java
arg constructor  
none-arg constructor  
[null, null, null] 
```
从该结果，一方面，可以看出Person对象中任何一个字段都没有被序列化。另一方面，如果细心的话，还可以发现这此次序列化过程调用了Person类的无参构造器。

Externalizable继承于Serializable，当使用该接口时，序列化的细节需要由程序员去完成。如上所示的代码，由于writeExternal()与readExternal()方法未作任何处理，那么该序列化行为将不会保存/读取任何一个字段。这也就是为什么输出结果中所有字段的值均为空。

另外，使用Externalizable进行序列化时，当读取对象时，会调用被序列化类的无参构造器去创建一个新的对象，然后再将被保存对象的字段的值分别填充到新对象中。这就是为什么在此次序列化过程中Person类的无参构造器会被调用。由于这个原因，实现Externalizable接口的类必须要提供一个无参的构造器，且它的访问权限为public。

对上述Person类进行进一步的修改，使其能够对name与age字段进行序列化，但忽略掉gender字段，如下代码所示：

```java
public class Person implements Externalizable {  
 
    private String name = null;  
 
    transient private Integer age = null;  
 
    private Gender gender = null;  
 
    public Person() {  
        System.out.println("none-arg constructor");  
    }  
 
    public Person(String name, Integer age, Gender gender) {  
        System.out.println("arg constructor");  
        this.name = name;  
        this.age = age;  
        this.gender = gender;  
    }  
 
    private void writeObject(ObjectOutputStream out) throws IOException {  
        out.defaultWriteObject();  
        out.writeInt(age);  
    }  
 
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {  
        in.defaultReadObject();  
        age = in.readInt();  
    }  
 
    @Override 
    public void writeExternal(ObjectOutput out) throws IOException {  
        out.writeObject(name);  
        out.writeInt(age);  
    }  
 
    @Override 
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {  
        name = (String) in.readObject();  
        age = in.readInt();  
    }  
    ...  
} 
```
执行SimpleSerial之后会有如下结果：

arg constructor  
none-arg constructor  
[John, 31, null] 

*  readResolve()方法

当我们使用Singleton模式时，应该是期望某个类的实例应该是唯一的，但如果该类是可序列化的，那么情况可能略有不同。此时对第2节使用的Person类进行修改，使其实现Singleton模式，如下所示：
```java
public class Person implements Serializable {  
 
    private static class InstanceHolder {  
        private static final Person instatnce = new Person("John", 31, Gender.MALE);  
    }  
 
    public static Person getInstance() {  
        return InstanceHolder.instatnce;  
    }  
 
    private String name = null;  
 
    private Integer age = null;  
 
    private Gender gender = null;  
 
    private Person() {  
        System.out.println("none-arg constructor");  
    }  
 
    private Person(String name, Integer age, Gender gender) {  
        System.out.println("arg constructor");  
        this.name = name;  
        this.age = age;  
        this.gender = gender;  
    }  
    ...  
} 
```
同时要修改SimpleSerial应用，使得能够保存/获取上述单例对象，并进行对象相等性比较，如下代码所示：
```java
public class SimpleSerial {  
 
    public static void main(String[] args) throws Exception {  
        File file = new File("person.out");  
        ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(file));  
        oout.writeObject(Person.getInstance()); // 保存单例对象  
        oout.close();  
 
        ObjectInputStream oin = new ObjectInputStream(new FileInputStream(file));  
        Object newPerson = oin.readObject();  
        oin.close();  
        System.out.println(newPerson);  
 
        System.out.println(Person.getInstance() == newPerson); // 将获取的对象与Person类中的单例对象进行相等性比较  
    }  
} 
```

执行上述应用程序后会得到如下结果：

arg constructor  
[John, 31, MALE]  
false 
值得注意的是，从文件person.out中获取的Person对象与Person类中的单例对象并不相等。为了能在序列化过程仍能保持单例的特性，可以在Person类中添加一个readResolve()方法，在该方法中直接返回Person的单例对象，如下所示：

```java
public class Person implements Serializable {  
 
    private static class InstanceHolder {  
        private static final Person instatnce = new Person("John", 31, Gender.MALE);  
    }  
 
    public static Person getInstance() {  
        return InstanceHolder.instatnce;  
    }  
 
    private String name = null;  
 
    private Integer age = null;  
 
    private Gender gender = null;  
 
    private Person() {  
        System.out.println("none-arg constructor");  
    }  
 
    private Person(String name, Integer age, Gender gender) {  
        System.out.println("arg constructor");  
        this.name = name;  
        this.age = age;  
        this.gender = gender;  
    }  
 
    private Object readResolve() throws ObjectStreamException {  
        return InstanceHolder.instatnce;  
    }  
    ...  
} 
```
再次执行本节的SimpleSerial应用后将如下输出：

arg constructor  
[John, 31, MALE]  
true 
无论是实现Serializable接口，或是Externalizable接口，当从I/O流中读取对象时，readResolve()方法都会被调用到。实际上就是用readResolve()中返回的对象直接替换在反序列化过程中创建的对象。




