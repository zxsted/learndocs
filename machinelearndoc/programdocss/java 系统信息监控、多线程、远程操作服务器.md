java 系统信息监控、多线程、远程操作服务器

[toc]



```shell
暂存网址：

追逐自由 （数学大牛）
EM算法的另一种解释 F函数交替优化的解释： http://www.rustle.us/?p=35

一张图的故事——概率分布之间的关系：
1. http://www.rustle.us/?p=167
2. http://www.rustle.us/?p=231

一种古老的矩阵分解:QR分解： http://www.rustle.us/?p=477

另一个大牛 的：
【机器学习基础】核逻辑回归： http://doc.okbase.net/JasonDing1354/archive/142531.html

super： 
Softmax算法：逻辑回归的扩展： http://blog.csdn.net/zc02051126/article/details/9866347

隐马模型Python代码： http://blog.csdn.net/zc02051126/article/details/8655035

jerrylead 的 EM（Q函数）： EM算法[The EM Algorithm] ： http://blog.csdn.net/wolenski/article/details/7983764

marvin521：
  高斯RBM： http://blog.csdn.net/marvin521/article/details/8906278
  RBM （含自由能的解释）：http://blog.csdn.net/marvin521/article/details/8886971
  

  
径向基（RBF）神经网络（python实现）：  http://blog.csdn.net/acdreamers/article/details/46327761

从线性分类器到卷积神经网络： http://zhangliliang.com/2014/06/14/from-lr-to-cnn/

最大似然估计、MAP及贝叶斯估计： http://blog.csdn.net/yangliuy/article/details/8296481

凸优化眼里的世界： http://zhuanlan.zhihu.com/prml-paper-reading/19692149

July大牛：
	从贝叶斯方法谈到贝叶斯网络（有概率图的解释）： http://blog.csdn.net/v_july_v/article/details/40984699?utm_source=tuicool
    从拉普拉斯矩阵说到谱聚类： http://blog.csdn.net/v_july_v/article/details/40738211
    通俗理解LDA主题模型： http://blog.csdn.net/v_july_v/article/details/41209515
    数据挖掘中所需的概率论与数理统计知识（微积分、概率分布、期望、方差、协方差、数理统计简史、大数定律、中心极限定理、正态分布）： http://blog.csdn.net/v_july_v/article/details/8308762
    
浙大直博的大牛 （做bayes推断的）
Variational Bayes    ： http://www.blog.huajh7.com/variational-bayes/

lda：变分的推导（详细算法）：http://www.cnblogs.com/zjgtan/p/3952994.html
topic model（gibbs）：
http://www.cnblogs.com/zjgtan/p/3900362.html

LDA更清晰的解析（变分推断）： http://blog.csdn.net/feixiangcq/article/details/5655086


Dirichlet Processes

Dirichlet Processes 是一个什么样的随机过程？： http://www.zhihu.com/question/22717561

理解Dirichlet过程的几个要点：
一、 http://www.douban.com/note/165984225/

狄利克雷过程 文章列表： http://www.tuicool.com/topics/11020126

狄利克雷过程： http://m.blog.csdn.net/blog/deltaququ/45727085

狄利克雷过程混合模型、扩展模型及应用： http://wenku.baidu.com/link?url=CKnh5uTm7oHA-IwEc9f0T646XMD0hu-8uTGeOXjr86RYTl8f5CfloiNUyffR-gy2cK1RaTWBggYt28CY4tMKPGADF1M1NUrv6hpzUiU8KFa



```

java的
```java

残剑 （多线程，java基础笔记 反射 动态编译 webservice ）：http://www.cnblogs.com/liuling/category/445221.html


zhglhy 的博客： 
Java获取电脑CPU个数及系统信息： http://zhglhy.iteye.com/blog/2019910

 一些java使用（json，）：http://zhglhy.iteye.com/category/139539?page=2

Java 连接远程Linux 服务器执行 shell 脚本查看 CPU、内存、硬盘信息： http://www.faceye.net/search/147249.html

Java如何获取系统cpu 内存硬盘信息（moniter）：
http://www.it165.net/pro/html/201411/26036.html

Java IO 内存操作流（ByteArrayInputStream（内存接受） ，ByteArrayOutputStream内存写出 ）

Jsch 深入浅出： （java 链接ssh）：
http://xliangwu.iteye.com/blog/1499764
```

#### 1. 多线程

##### 1.1 java利用FutureTask、ExecutorService 在多核时代充分利用CPU运算
[传送](http://www.cnblogs.com/jisheng/archive/2011/12/02/2272245.html)

###### 1.1.1 使用FutureTask 的例子

```java

package com.spell.threads;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * 测试FutureTask的用法，如果不想分支线程阻塞主线程，又想取得分支线程的执行结果，就用FutureTask
 *
 * @author Administrator
 *
 */
public class FutureTaskTest {

 /**
  * @param args
  */
 public static void main(String[] args) {
  CountNum cn = new CountNum(0);
  //FutureTask<Integer> 这里的表示返回的是Integer
  FutureTask<Integer> ft = new FutureTask<Integer>(cn);
  Thread td = new Thread(ft);
  System.out.println("futureTask开始执行计算:" + System.currentTimeMillis());
  td.start();
  System.out.println("main 主线程可以做些其他事情:" + System.currentTimeMillis());
  try {
   // futureTask的get方法会阻塞，知道可以取得结果为止
   Integer result = ft.get();
   System.out.println("计算的结果是:" + result);
  } catch (InterruptedException e) {
   e.printStackTrace();
  } catch (ExecutionException e) {
   e.printStackTrace();
  }
  System.out.println("取得分支线程执行的结果后，主线程可以继续处理其他事项");
 }

}

class CountNum implements Callable {
 private Integer sum;

 public CountNum(Integer sum) {
  this.sum = sum;
 }

 public Object call() throws Exception {
  for (int i = 0; i < 100; i++) {
   sum = sum + i;
  }
  // 休眠5秒钟，观察主线程行为，预期的结果是主线程会继续执行，到要取得FutureTask的结果是等待直至完成。
  Thread.sleep(3000);
  System.out.println("futureTask 执行完成" + System.currentTimeMillis());
  return sum;
 }

}

```

运行的结果是：
futureTask开始执行计算:1280114852250
main 主线程可以做些其他事情:1280114852250
futureTask 执行完成1280114855250
计算的结果是:4950
取得分支线程执行的结果后，主线程可以继续处理其他事项

###### 1.1.2 如果有多个FutureTask要执行批量运算，从而充分的利用多核CPU

```java

package com.ireader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by zxsted on 15-10-11.
 *
 * 测试多核实现，充分的利用CPU来运算数据，并且处理返回的结果,学习API专用
 */
public class MultiFutureTask {


    public static class GetSum implements Callable {

        private Integer total;
        private Integer sum = 0;

        public GetSum(Integer total) {
            this.total = total;
        }


        public Object call() throws Exception {
            for(int i = 1; i < total + 1; i++)
            {
                sum = sum + i;
                Thread.sleep(10);
            }

            System.out.println(Thread.currentThread().getName() + "sum:" + sum);
            return sum;
        }
    }


    public static void main(String[] args) throws InterruptedException {

        /** 存储划分好的任务线程的队列 */
        List<FutureTask<Integer>> list = new ArrayList<FutureTask<Integer>>();

        /** 执行任务的线程池 */
        ExecutorService exec = Executors.newFixedThreadPool(5);

        /** 创建线程任务，添加到任务队列方便取出结果， 然后提交线程池执行 */
        for(int i = 10; i < 20; i++) {
            // 创建对象
            FutureTask<Integer> ft = new FutureTask<Integer>(new GetSum(i));

            // 添加到任务队列
            list.add(ft);

            // 一个个提交给线程池， 也可以一次性的提交给线程池， exec.invokeAll(list);
            exec.submit(ft);
        }


        /** 这个过程中主线程可以执行其他任务*/
        for(int i = 0 ; i < 100; i++) {
            System.out.println("这个过程中主线程可以执行其他任务:" + i);
            Thread.sleep(15L);
        }

        /**开始统计结果*/
        Integer total = 0;
        for(FutureTask<Integer> tempFt:list) {
            try{
                total = total + tempFt.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        // 处理完毕，一定要记住关闭线程池，这个不能在统计之前关闭，因为如果线程多的话,执行中的可能被打断
        exec.shutdown();
        System.out.println("多线程计算后的总结果是:" + total);

    }

}


```


运行的结果是：
pool-1-thread-1 sum:120
pool-1-thread-1 sum:136
pool-1-thread-1 sum:153
pool-1-thread-1 sum:171
pool-1-thread-1 sum:190
pool-1-thread-2 sum:66
pool-1-thread-3 sum:78
pool-1-thread-4 sum:91
pool-1-thread-5 sum:105
多线程计算后的总结果是:1165


##### 1.2.1 线程池执行任务队列的一个辅助类

```java

package com.ireader.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zxsted on 15-10-10.
 *
 *  本地多线程工具类
 */
public class ConcurenceRunner {

    private static final ExecutorService exec;

    public static final int cpuNum;

    static {
        // 获取可以使用的cpu个数
        cpuNum = Runtime.getRuntime().availableProcessors();
        System.out.println("cpuNum:" + cpuNum);

        // 根据cpu个数初始化线程池size
        exec = Executors.newFixedThreadPool(cpuNum);
    }

    public static void run(Runnable task) {
        exec.execute(task);
    }

    public static void stop(){
        exec.shutdown();
    }



    /**
     *  具体任务继承 本类， 并提供任务列表 list ， 并在process 函数中处理
     * */
    public abstract static class TaskManager {
        private int workLength ;

        public TaskManager(int workLength) {
            this.workLength = workLength;
        }

        public void start(){
            int runCpu = cpuNum < workLength?cpuNum:workLength;

            // CountDownLatch  是一个计数器
            // CountDownLatch如其所写，是一个倒计数的锁存器，当计数减至0时触发特定的事件。利用这种特性，可以让主线程等待子线程的结束。
            final CountDownLatch gate = new CountDownLatch(runCpu);

            // 将任务平均分配给各个线程
            int fregLength = (workLength + runCpu - 1) / runCpu;

            for(int cpu = 0; cpu < runCpu;cpu++) {
                final int start = cpu * fregLength;
                int tmp = (cpu + 1) * fregLength;

                final int end = tmp <= workLength ? tmp : workLength;

                Runnable task = new Runnable() {

                    @Override
                    public void run() {
                        process(start,end);
                        gate.countDown();   // 当该线程完成任务就减1
                    }
                };
                ConcurenceRunner.run(task);
            }

            try{
                gate.await();
            } catch(InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        public abstract void process(int start,int end);
    }



}


```