双数组Tire树（DoubleArrayire）的原理与实现

#####简介
双数组Tire是一种空间复杂度低的Tire树，应用于字符区间较大的语言（如中文、日文）等分词领域

双数组Trie (Double-Array Trie)结构由日本人JUN-ICHI AOE于1989年提出的，是Trie结构的压缩形式，仅用两个线性数组来表示Trie树，该结构有效结合了数字搜索树(Digital Search Tree)检索时间高效的特点和链式表示的Trie空间结构紧凑的特点。双数组Trie的本质是一个确定有限状态自动机（DFA），每个节点代表自动机的一个状态，根据变量不同，进行状态转移，当到达结束状态或无法转移时，完成一次查询操作。在双数组所有键中包含的字符之间的联系都是通过简单的数学加法运算表示，不仅提高了检索速度，而且省去了链式结构中使用的大量指针，节省了存储空间。

我看了几篇论文，发现中文里也就上面这篇质量最好，英文当属这篇[双数组Trie的一种实现][1]。不过我并不打算按论文的腔调摘抄理论，而是准备借助开源的 [darts-java][2] 写点代码分析与笔记，如果能帮到你，实属意外。

darts-java 是对 Taku Kudo 桑的 C++ 版 Double Array Trie 的 Java 移植，代码精简，只有一个Java文件，十分优美。


#####测试代码
```java
package com.hankcs;

import darts.DoubleArrayTire

import java.io.*;
import java.util.*;

/**
*dat的测试代码
*/
public class DatTest
{
	public static void main(String args[])  throws IOException{
    	BufferedReader reader = new BufferedReader(new FileReader(
         "./data/small.dic"));
         String line;
         List<String> words = new ArrayList<String>();
         Set<Charactor> charset = new HashSet<Character>();
         while((line = reader.readLine()) != null) {
         	words.add(line.trim());
            //制作一份码表debug
            for (char c : line.toCharArray())
            {
            	charset.add(c);
            }
         }
         reader.close();
         
         //这个字典如果需要加入新词必须按照字典序，参考下面的代码
         Cllections.sort(words);
         BufferedWriter writer = new BufferedWriter(new FIleWriter("./data/sort.dic",false));
         for(String word : words) 
         {
         	writer.write(w);
            writer.newLine();
         }
         System.out.println("字典词条："+ words.size());
         {
         	String infoCharSetValue = "";
            String infoCharsetCode = "";
            for(Character c : charset)
            {
            	infoCharsetValue += c.charValue() + " ";
                infoCharsetCode  += (int) c.charValue() + " ";
            }
            infoCharsetValue += '\n';
            infoCharsetCode += '\n';
            System.out.println(infoCharsetValue);
            System.out.println(infoCharsetCode);
         }
         
         //实例化一个DAT，
         DoubleArrayTire dat = new DoubleArrayTire();
         //用词list进行构建
         System.out.println("是否错误：" + dat.build(words));
         System.out.println(dat);
         List<Integer> integerList = dat.commonPrefixSearch("一举成名天下知");
         for(int index : integerList) {
         	System.out.println(words.get(index));
         }
         
    }

}
```
其中small.dic是一个微型的字典
```shell
一举
一举一动
一举成名
一举成名天下知
万能
万能胶
```
输出：
```shell
字典词条：6
胶    名    动    知    下    成    举    一    能    天    万    
33014 21517 21160 30693 19979 25104 20030 19968 33021 22825 19975 
是否错误: 0
一举
一举成名
一举成名天下知
```

#####Tire树的构造与双数组的构造
双数组Trie树归根结底还是属于Trie树，所以免不了有一颗树的构造过程。不过这棵树并没有保存下来，而是边构造树边维护双数组，双数组的信息足以表示整棵树。比如对于上面的例子，首先建立一个空的root节点：

```shell
Node{code=0,depth=0,left=0,right=6}
```
其中code指的是字符的编码，在Java中是双字节，depth是深度，left及right表示这个节点在字典中的索引范围。

比如：
![](http://ww1.sinaimg.cn/large/6cbb8645gw1eej8x5w4r6j20m00at75g.jpg)
然后按照字典序插入所有的字串节点
![](http://ww2.sinaimg.cn/large/6cbb8645gw1eej908mhn3j20lq0z3goq.jpg)
其中绿色节点为空字符，代表从根节点到词节点路径上的所有节点构成的一个词,下面是其构建顺序：
![](http://ww2.sinaimg.cn/large/6cbb8645gw1eej932ayymj20lq0z3djp.jpg)

在darts-java中，使用了两个数组base和check来维护Trie树，它们的下标以及值都代表着一个确定的状态。base储存当前的状态以供状态转移使用，check验证字串是否由同一个状态转移而来并且当check为负的时候代表字串结束。（PS 双数组Tire树的具体实现有多种，有的实现将base为负作为状态的结束，大同小异。）

假定有字符串状态s,当前字符串状态为t，假定t加了一个字符c就等于状态tc，加了一个字符x等于状态tx，那么有
```shell
base[t] + c = base[tc]
base[t] + x = base[tx]
check[tc] = check[tx]
```
可见，在单词“一举一动”中，虽然有两个“一”，但它们的前一个状态不同，所以对应的状态分别为“一”和“一举一”,在base数组中的下标不一样。
在每个节点插入的过程中会修改这两个数组，具体说来：
1. 初始化root节点的base[0]=1;check[0]=0;
2. 对于每一群兄弟节点，寻找一个begin值使得check[begin+a1...an] == 0,也就是找到了n个空闲空间，a1…an是siblings中的n个节点对应的code。
```shell
int pos = siblings.get(0).code;
while(true)
{
	pos++;
    begin = pos -siblings.get(0).code;  //当前位置离第一个兄弟节点的距离
    ......
}
```
3. 然后将这群兄弟节点的check设置为check[begin+ a1 ... an] = begin;很显然，叶子节点i的check[i]的值一定等于i，因为它是兄弟节点中的第一个，并且它的code为0。
```java
check[begin + siblings.get(i).code] = begin;
```
4、接着对每个兄弟节点，如果它没有孩子，也就是上图除root外的绿色节点（叶子节点），令其base为负值；否则为该节点的子节点的插入位置（也就是begin值），同时插入子节点（迭代跳转到步骤2）。
```java
if (fetch(siblings.get(i), new_siblings) == 0)  // 无子节点，也就是叶子节点，代表一个词的终止且不为其他词的前缀
            {
                base[begin + siblings.get(i).code] = -siblings.get(i).left - 1;
                ……
            }
            else
            {
                int h = insert(new_siblings);   // dfs
                base[begin + siblings.get(i).code] = h;
            }
```
这里给出这个例子的base check值以及码表，下表中X代表空
```shell
码表：
   胶    名    动    知    下    成    举    一    能    天    万    
33014 21517 21160 30693 19979 25104 20030 19968 33021 22825 19975 
 
DoubleArrayTrie{
char =      ×    一    万     ×    举     ×    动     ×     下    名    ×    知      ×     ×    能    一    天    成    胶
i    =      0 19970 19977 20032 20033 21162 21164 21519 21520 21522 30695 30699 33023 33024 33028 40001 44345 45137 66038
base =      1     2     6    -1 20032    -2 21162    -3     5 21519    -4 30695    -5    -6 33023     3  1540     4 33024
check=      0     1     1 20032     2 21162     3 21519  1540     4 30695     5 33023 33024     6 20032 21519 20032 33023
size=66039, allocSize=2097152, key=[一举, 一举一动, 一举成名, 一举成名天下知, 万能, 万能胶], keySize=6, progress=6, nextCheckPos=33024, error_=0}
```
#####前缀查询
定义当前状态p = base[0] = 1。按照字符串char的顺序walk：
如果base[p] == check[p] && base[p] < 0 则查到一个词；
然后状态转移，增加一个字符  p = base[char[i-1]] + char[i] + 1 。加1是为了与null节点区分开。
如果转移后base[char[i-1]] == check[base[char[i-1]] + char[i] + 1]，那么下次p就从base[base[char[i-1]] + char[i] + 1]开始。

结合例子如下：
```shell
字典词条：6
胶    名    动    知    下    成    举    一    能    天    万    
33014 21517 21160 30693 19979 25104 20030 19968 33021 22825 19975 
是否错误: 0
DoubleArrayTrie{
char =     ×    一    万    ×    举    ×    动    ×    下    名    ×    知    ×    ×    能    一    天    成    胶
i    =      0 19970 19977 20032 20033 21162 21164 21519 21520 21522 30695 30699 33023 33024 33028 40001 44345 45137 66038
base =      1     2     6    -1 20032    -2 21162    -3     5 21519    -4 30695    -5    -6 33023     3  1540     4 33024
check=      0     1     1 20032     2 21162     3 21519  1540     4 30695     5 33023 33024     6 20032 21519 20032 33023
size=66039, allocSize=2097152, key=null, keySize=6, progress=6, nextCheckPos=33024, error_=0}
 
i       =      0     0     1     1     2     2     3     3     4     4     5     5     6     6
b       =      1     1     2     2 20032 20032     4     4 21519 21519  1540  1540     5     5
 
p       =      1 19970     2 20033 20032 45137     4 21522 21519 44345  1540 21520     5 30699
base[p] =      1     2     0 20032    -1     4     0 21519    -3  1540     0     5     0 30695
check[p]=      0     1     0     2 20032 20032     0     4 21519 21519     0  1540     0     5
一举
一举成名
一举成名天下知
```

稍微解释下
初始空 base[0] = 1, p = 1;
转移 p = base[0] + {char[一] = 19968} + 1 = 1 + 19968 + 1 = 19970，                检查base[19970]!=0说明有“一”这个字符。
 而  base[base[19970]] = base[2] = 0 说明没遇到词尾
转移 p = base[19970] + {char[举] = 20030} + 1 = 2 + 20030 + 1 = 20033，            检查base[20033]!=0说明有“举”这个字符。
 而  base[base[20033]] = base[20032] = -1 && base[20032] == check[20032] 说明遇到一个词尾，即查出“一举”
转移 p = base[20033] + {char[成] = 25104} + 1 = 20032 + 25104+ 1 = 45137，         检查base[45137]!=0说明有“成”这个字符。




#####摄入理解DAT
* Double Array Trie 是 TRIE 树的一种变形,它是在保证 TRIE 树检索速度的前提下,提高空间利用率而提出的一种数据结构,本质上是一个确定有限自动机(deterministic finite automaton,简称 DFA)。
* 所谓的 DFA 就是一个能实现状态转移的自动机。对于一个给定的属于该自动机的状态和一个属于该自动机字母表 Σ 的字符,它都能根据事先给定的转移函数转移到下一个状态。
* 对于 Double Array Trie(以下简称 DAT),每个节点代表自动机的一个状态,根据变量的不同,进行状态转移,当到达结束状态或者无法转移的时候,完成查询。


#####DAT结构
* 定义
	* DAT 是采用两个线性数组(base[]和 check[]),base 和 check 数组拥有一致的下标,(下标)即 DFA 中的每一个状态,也即 TRIE 树中所说的节点,base 数组用于确定状态的转移,check 数组用于检验转移的正确性。因此,从状态 s 输入 c 到状态 t的一个转移必须满足如下条件:
```shell
base[s] + c == t
check[base[s] + c] == s
```
 * DAT也可以描述如下：
    1. 对于给定的状态s，如果有n个状态（字符 c1,c2,..,cn）的转移，在base数组中找到一段空位 t1,t2,t3,...,tn,使得t1 -c1,t2-c2,...,tn-cn都为base数组中下标为s的值，注意此处的t1,t2，....,tn在base数组中连续
    2. 对于转移的状态t1,t2,...,tn，其作为下标时， check[t1],check[t2],...,check[tn]的状态都为状态S；


* 图中最顶端有 256 的父节点,每个父节点都有 256 个子节点;那么无论汉字和字母,都可以分布在 256 个子节点上,但是如果词语只有 app 和 apple 以及 banana 三个词语,那么 256 个父节点显得有些浪费,实际上只需要 2 个父节点就可以了。
  * 如果节点的类型为整型,我们把所有的节点进行编号的话,且直接采用词语的首字母 ascii 码来直接作为节点,a(97), b(98)这两个父节点会使用到,其余的父节点是多余的,使用一个空指针即可。
  * 根据 256 叉树的定义,97 和 98 也会有 256 个子节点,但是词语的第二个字母显示,97 的下一个节点只有一个 p(112)节点,98 的下一个节点只有一个a(97)节点,则其余的节点仍然为空,这样,由于树型的算法的复杂度为 On,即最多 n 次匹配即可完成一次查找,而我们可以省略不用的节点,降低空间的空闲率。

* DAT匹配
  基于上述定义,DAT 的匹配过程如下:假设当前状态为 s,对于输入的字符 c 有:
```shell
t = base[s] + c;
if check[t] = s then
	next state = t;
else
	fail
endif
```

* DAT的构造
在DAT的构造过程中，一般有两种构造方法: 
1. 已知所有词语,静态构造双数组此方法构建时,是将所有词语全部放入到内存,对词语中所有的父节点和其下的子节点分别进行排序(一般为 ASCII 码排序),找出初的父节点数目和有多少个不同的子节点数,方便对内存进行分配。这样的优点是找到放置子节点空间完全能够容纳子节点,以后不需要进行扩充,相对复杂度较低,且构建速度相对很快。缺点是
以后添加词语不太灵活,每次需要重新构建
2. 动态输入词语,动态构造双数组
当 n 条词语准备构建双数组时,先以添加一条词语 cat 为例,双数组中 base[1024]数组根节点为 0(默认值,当然根据“个人爱好”可以随意指定) 下标为 0,,为词语 cat 的首字符"c“(99)找一个合适的位置,比如位置 100,即:
```shell
base[0] = 100
```
此处那么在 base[100]的位置下,加上字符"c"的 ascii 值得到下一个状态(t)的位置(199),然后在一个合适的位置(空闲的位置)197,使得
```shell
base[199] + 'a' = 197;
```
那么状态(t),即 base[199]的值可以通过上述公式得到仍然为 100
值得注意是,在状态(f),目前即字符"t",结束时,其 value 值可以做如下处理,如果状态(f)结束,没有子节点,则
```java
base(f) = -1 * f;
```
如果状态(t)结束,仍然有多个子节点,那么其 base 数组标记为
```java
base(f) = -1 * base(f);
```
当输入第二条词语 camera 时,仍然按照上述方式进行,当进行到字符 a 时,字符 a 位置的下标为 197,检查 check[base[197]+'m']是否为空位。
1. 如果为空位,则 base[197]的值仍然可以为 100;
2. 否则需要重新寻找两个空位位置 Ψ (base[197]+'m'),λ(base[197]+'t'),使得 base[base[197]+'m']=-1(-1 标记为空位状态,"m"为 camera 的第三个字符)和base[base[197]+'t']=-1("t"为 cat 的第三个字符),即第二级节点 a 后面的两个新节点能有位置存放新的偏移量,并使得 check[base[197]+'m']=197 和check[base[197]+'t']=197 即可,那么 base[197]的值需重新指向到新的位置(Ψ +λ -'m'-'t')/2。
接着继续重新构建下面新两个节点的 DAT 结构,且第一个节点的结构构造完成后,需要清除原来的构建。
动态构造双数组能够很方便的动态插入词语,不需要重新构造整个
TRIE 树,但是实现的逻辑相对复杂一些。
若初始状态申请的数组大小不足时,需要进行扩充,并将原来的数组拷贝到新增大的数组上,且原来的数组一般需要进行内存释放,如下图:
DAT 构造中,check 数组需要指向父节点,即 base 数组中父节点的下标即可,这里
有一副简图,描述了构造 DAT 双数组的方式:





DAT的伪代码介绍
[传送门][3]
一、TRIE树简介（以下简称T树）

TRIE树用于确定词条的快速检索，对于给定的一个字符串a1,a2,a3,…an，则采用TRIE
树搜索经过**最多n次匹配**即可完成一次查找，而与词库中词条的数目无关。它的缺点是空间空闲率高。
 
二、Double-Array Trie（双数组索引树，以下简称DAT）
  1）、DAT简介
   DAT是TRIE树的一种变形，它是在保证TRIE树检索速度的前提下，提高空间利用率而提出的一种数据结构。它本质是一个确定的有限状态自动机（DFA），每个节点代表自动机的一个状态，根据变量的不同，进行状态转移，当到达结束状态或者无法转移的时候，完成查询。
 
  2）、DAT结构
   DAT是采用两个线性数组（姑且叫它们为base和check数组）进行TRIE树的保存， base和check数组拥有**一致的下标**，（下标）即DFA中的每一个**状态**，也即TRIE树中所说的**节点**，base数组用于**确定状态的转移**，check数组用于**检验转移的正确性**。
   
定义：从状态s输入c到状态t的一个转移必须满足以下条件
```shell
base[s] + c == t
check[base[s] + c] == s
```

匹配
假设当前状态为 s,对于输入的字符 c 有:
```shell
t = base[s] + c;
if check[t] = s then
	next state = t;
else
	fail
endif
```

构造

```java
root_index = 1

procedure daInsertBranch(String key)
begin
	index = root_index
    for i = 0 to key.length
    begin
    	character c = key.get(i)
        t = base[index] + c    1
        [ 。。。此处执行冲突处理。。。]
        check[t] = index       2
        index = t
    end
    base[t] *= -1
    
end
```

冲突处理
在执行3的过程中，有可能在1处插入状态t时该位置已经被其他状态 t1所占用，这就产生了冲突。
解决冲突的基本思想是为t以及t的所有兄弟状态重新寻找一个合适的状态，相当于寻找一个合适的数组下标。
```shell
//寻找适当的base值，也相当与为所有子状态寻找合适的下标
Procedure intdaFindBase(character c , int oldbase_index)
begin
	if check[base[oldbase_index] + c] != 0 then
    	beign
        	foreach character a in ALPHABET(字母表)
            begin
            	if check[base[oldbase_index] + a] != 0 then
                	add a to child_list
            end
            add c to child_list
            base[oldbase_index]++;
        while (not fit each character)
        begin
        	base[oldbase_index]++
        end
     end
     return base[oldbase_index];
end


//重新分配
Procedure intadRelocateBase(int old_index,int new_index)
begin
	//拷贝所有节点到新的为，并修改拷贝节点的所有字节点的check值以保证
    //在移动后仍然是其字节点
    foreach character c in child_list
    	begin
        	copy cell from old_index to new_index
            begin
            	get all childs of old_index;
                check[child] = new_index;
            end
            //释放所有旧节点
            free old_index cell
        end
    base[oldbase_index] = newbase;
end
    	
```
实例介绍：
[传送门][4]
在darts-java中，使用了两个数组base和check来维护Trie树，它们的下标以及值都代表着一个确定的状态。base储存当前的状态以供状态转移使用，check验证字串是否由同一个状态转移而来并且当check为负的时候代表字串结束。（PS 双数组Tire树的具体实现有多种，有的实现将base为负作为状态的结束，大同小异。）


假定有字符串状态s,当前字符串状态为t，假定t加了一个字符c就等于状态tc，加了一个字符x等于状态tx，那么有
```shell
base[t] + c = base[tc]
base[t] + c = base[tx]
check[tc] = check[tx]
```
可见，在单词“一举一动”中，虽然有两个“一”，但它们的前一个状态不同，所以对应的状态分别为“一”和“一举一”,在base数组中的下标不一样。

在每个节点插入的过程中会修改这两个数组，具体说来：

1、初始化root节点base[0] = 1; check[0] = 0;

2、对于每一群兄弟节点，寻找一个begin值使得check[begin + a1...an]  == 0，也就是找到了n个空闲空间,a1…an是siblings中的n个节点对应的code。
```shell
 int pos = siblings.get(0).code;
        while (true)
        {
            pos++;
            begin = pos - siblings.get(0).code; // 当前位置离第一个兄弟节点的距离
            ……
        }
```
3、然后将这群兄弟节点的check设为check[begin + a1...an] = begin;很显然，叶子节点i的check[i]的值一定等于i，因为它是兄弟节点中的第一个，并且它的code为0。
```shell
check[begin + siblings.get(i).code] = begin;
```
4、接着对每个兄弟节点，如果它没有孩子，也就是上图除root外的绿色节点（叶子节点），令其base为负值；否则为该节点的子节点的插入位置（也就是begin值），同时插入子节点（迭代跳转到步骤2）。
```shell
if(fetch(siblings.get(i),new_siblings) == 0) //无字节点，即叶子节点，代表一个词的终止且不为其他词的前缀
{
	base[begin + siblings.get(i).code] = -siblings.get(i).left - 1;
    ......
}
else
{
	int h = insert(new_siblings); //dfs
    base[begin+siblings.get(i).code]=j;
}
```











[1]:http://xudongliang198421.blog.163.com/blog/static/9851503520118155553856
[2]:https://github.com/komiya-atsushi/darts-java
[3]:http://blog.csdn.net/zhoubl668/article/details/6957452
[4]:http://www.hankcs.com/program/java/双数组trie树doublearraytriejava实现.html