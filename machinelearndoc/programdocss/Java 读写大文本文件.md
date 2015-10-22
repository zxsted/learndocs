####Java 读写大文本文件（2GB以上） 
 [原文传送][0]
 如下的程序，将一个行数为fileLines的文本文件平均分为splitNum个小文本文件，其中换行符'r'是linux上的，windows的java换行符是'\r\n'：
 
```java
package kddcup2012.task2.FileSystem;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InpuitStreamReader;


public class FileSplit
{
	public static void main(String[] args) throws IOException{
    long timer = System.currentTimeMillis();
    int bufferSize = 20*1024*1024;     //设置读文件的缓存为20MB
    
    //建立缓冲文本输入流
    File file = new File("/media/Data/test.txt");
    FileInputStream fileInputStream = new FileInputStream(file);
    BufferedInputStream bufferedInputStream = new BufferedInputStream(FileInputStream);
    InputStreamReader inputStreamReader = new InputStreamReader(bufferedInputStream);
    BufferedReader input = new BufferedReader(inputStreamReader,bufferSize);
    
    int splitNum = 112 -1; //要分割的块数减一
    int fileLines = 23669283;  //输入文件的行数
    long perSplitLines = fileLines / splitNum; //每个块的行数
    for(int i = 0; i <= splitNum; ++i) {
    	//分割
        //每个块建立一个输出
        FileWriter output = new FileWriter("/home/ted/part"+i+".txt");
        String line = null;
        //逐行读取，逐行输出
        for(long lineCounter = 0;  lineCounter < perSplitLines && (line = input.readLine()) != null; ++ lineCounter){
        	output.append(line+"\r");
        }
        output.flush();
        output.close();
        output = null;
    }
    input.clsoe();
    timer = System.currentTimeMillis() - timer;
    System.out.println("处理时间：" + timer);
    }
}
```

以上程序处理大文本文件只需要30MB左右的内存空间（这和所设的读取缓冲大小有关），但是速度不是很快，在磁盘没有其他程序占用的情况下，将200MB文件分割为112份需要20秒（机器配置：Centrino2 P7450 CPU，2GB DDR3内存，Ubuntu11.10系统，硬盘最大读写速度大约60MB/S）。

另外，对于几百兆到2GB大小的文件，使用内存映射文件的话，速度会块一些，但是内存映射由于映射的文件长度不能超过java中int类型的最大值，所以只能处理2GB以下的文件。该方式参考：

```shell
http://www.cnblogs.com/phoebus0501/archive/2010/12/06/1897870.html

http://www.oschina.net/code/snippet_12_247

http://blog.sina.com.cn/s/blog_63743d1c0100q1ti.html
```






























[0]:http://blog.csdn.net/bhq2010/article/details/7376537