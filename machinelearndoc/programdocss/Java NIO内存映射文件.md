####Java NIO内存映射文件
[传送门][1]

文件通道，FileChannel通道提供了一个方法map()，它在打开的文件和ByteBuffer字节缓冲区之间直接开启了一个虚拟内存映射， 当通道调用map()的时候可以直接通过虚拟内存空间和物理内存空间使用MappedByteBuffer类以映射方式进行数据 读写。MappedByteBuffer对象可以从map()方法返回，类似基于内存的缓冲区，但实际上它的数据元素 却是直接存储在磁盘文 件上的。通过调用get()方法可以直接从文件中读取数据，即使这个文件被其他进程修改了，这些数据也可以映射出该文件的实际内容。通过内 存映射机制来操作文件的时候比起普通的文件读写方式更加高效，但是这种方式需要显示调用，学过操作 系统的人都明白操作系统的虚拟内存可以自动缓存内存页面，这些页面会由系统内存进行缓存而不去消耗JVM的内存堆空间。
　　内存映射文件提供了三种模式：
FileChannel.MapMode.READ_ONLY
FileChannel.MapMode.READ_WRITE
FileChannel.MapMode.PRIVATE
　　内存映射文件能让你创建和修改那些大到无法读入内存的文件。有了内存映射文件，你就可以认为文件已经全部读进了 内存，然后把它当成一个非常大的数组来访问了。这种解决思路能大大简化修改文件的代码。注意，你必须指明，它是从文件的哪个位置开始映射的，映射的范围又 有多大；也就是说，它还可以映射一个大文件的某个小片断。文件的访问好像只是一瞬间的事，这是因为，真正调入内存的只是其中的一小部分，其余部分则被放在 交换文件上。这样你就可以很方便地修改超大型的文件了(最大可以到2 GB)。
  
以下是三种映射模式的使用：
```java
pacakge org.susan.java.io;


import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MapFiles{
	public static void main(String[] args) {
    	//创建一个临时文件连接和管道
        File tempFile = File.createTempFile("mmaptest",null);
        RandomAccessFile file = new RandomAccessFile(tempFile,"rw");
        FileChannel channel = file.getChannel();
        ByteBuffer temp = ByteBuffer.allocate(100);   //定义一个100字的缓存
        //从位置0存放一些内容到文件
        temp.put("This is the file content".getBytes());
        temp.flip();
        channel.write(temp,0);
        temp.clear();
        temp.put("this is more file content".getBytes());
        temp.flip();
        channel.write(temp,8192);
        
        //针对同一个文件创建三种映射文件模式
        MappedByteBuffer ro = channel.map(FileChannel.MapModel.READ_ONLY,0,channel.size());
        MappedByteBuffer rw = channel.map(FileChannel.MapModel.READ_WRITE,0,channel.size());
        MappedByteBuffer cow = channel.map(FileChannel.MapModel.PRIVATE,0,channel.size());
        
        System.out.println("Begin");
        showBuffers(ro,rw,cow);
        //修改READ 模式拷贝位置
        cow.position(8);
        cow.put("COW".getBytes());
        System.out.println("Change to COW buffer");
        showBuffers(ro,rw,cow);
        //修改READ/WRITE模式拷贝位置
        rw.position(9)
        rw.put("R/W".getBytes());
        rw.position(8194);
        rw.put("R/W".getBytes());
        rw.force();
        System.out.println("Change to R/W buffer");
        showBufferd(ro,rw,cow);
        
        temp.clear();
        temp.put("Channel write ".getBytes());
        temp.flip();
        channel.write(temp, 0);
        temp.rewind();
        channel.write(temp, 8202);
        System.out.println("Write on channel");
        showBuffers(ro, rw, cow);
        // 再次修改
        cow.position(8207);
        cow.put(" COW2 ".getBytes());
        System.out.println("Second change to COW buffer");
        showBuffers(ro, rw, cow);

        rw.position(0);
        rw.put(" R/W2 ".getBytes());
        rw.position(8210);
        rw.put(" R/W2 ".getBytes());
        rw.force();
        System.out.println("Second change to R/W buffer");
        showBuffers(ro, rw, cow);
      
        channel.close();
        file.close();
        tempFile.delete();
        
    }
    
    //显示目前的缓冲区内容
    public static void showBuffers(ByteBuffer ro,ByteBuffer rw,ByteBuffer cow) throw IOException{
    	dumpBuffer("R/O",ro);
        dumpBuffer("R/W",rw);
        dumpBuffer("cow" ,cow);
        System.out.println("");
    }
    
    public static void dumpBuffer(String prefix,ByteBuffer buffer)
    				throws IOException{
    	System.out.println(prefix + ":");
        int nulls = 0;
        int limit =  buffer.limit();
        for (int i= 1；i < limit; i++){
        	char c = (char) buffer.get(i);
            if(c == '\u0000'){
            	nulls++;
                continue;
            }
            if (nulls != 0) {
            	System.out.println("|[" + nulls + "]|");
                nulls = 0;
            }
            System.out.println(c);
        }
        System.out.println();
    
    }

}
```









[1]:http://blog.sina.com.cn/s/blog_63743d1c0100q1ti.html










