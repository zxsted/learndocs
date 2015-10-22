####GMM-HMM （块标注算法）
[传动](http://blog.csdn.net/abcjennifer/article/details/27346787)

GMM-HMM 原理，建模和测试过程  主要有三个问题：
1. HMM要解决的三个问题：
	1. Likelihood
    2. Decoding
    3. Training
    
2. GMM是什么，如何用GMM求某一个音素(phoneme)的概率
3. GMM-HMM解决语音识别
	3.1 识别
	3.2 训练
	  3.2.1  Traning the params of GMM
	  3.2.2  Traning the params of HMM


正文：
HMM
   一个有隐藏节点和可见节点的HMM过程
   隐藏节点表示状态，可见节点表示我们听到的语音和看到的信号
   最开始时，指定这个HMM结构，训练HMM模型时：给定n个时序信号 y1,y2,...,yT(训练样本)，用MLE（typically implemented in EM）估计参数：
   1. N各状态的初始概率
   2. 状态转移概率a
   3. 输出概率b
   ----------
   * 实际处理中，一个word由若干个phoneme（因素）；
   * 每个HMM对应于一个word或者因素（phoneme） 
   * 一个word表示成若干个states，每个states表示成一个因素

使用HMM需要解决3个问题：
1） likelihood:一个HMM生成一串observation序列x的概率<the forword algorithm>

  ![image](http://img.blog.csdn.net/20140530151854546?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEasthttp://img.blog.csdn.net/20140530151854546?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

其中，at(sj)表示HMM在时刻t处于状态j，且observation = {x1,....,xt}的概率 ![image_1](http://img.blog.csdn.net/20140530152949593?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast) aij是状态i到状态j的转移概率， bj(xt)表示在状态j时生成xt的概率，

1） Decoding：给定一串observation序列x，找出最可能从属的HMM状态序列<the Viterbi algorithm> 
实际计算时会做剪枝，不是计算每个可能的state序列的probability，而是用Viterbi approximation：
从时刻 1：t ，只记录转移概率最大的state和概率。
记Vt(si)为从时刻t-1的所有状态转移到时刻t时状态j的最大概率： 
![image_2](http://img.blog.csdn.net/20140530155625171?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
记![image_3](http://img.blog.csdn.net/20140530154949078?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)为：从时刻t-1的*哪个状态*转移到时刻t时状态为j的概率最大；
Viterbi approximation过程如下：
![image_4](http://img.blog.csdn.net/20140530155945437?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
然后根据记录的最可能转移状态序列$bt_t(s_i)$进行回溯：
![](http://img.blog.csdn.net/20140530160136578?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

3)Training:给定一个observation序列x,训练出HMM参数 $lambda$={aij,bij} ,EM (forword-Backward) algorithm


GMM
简单的理解混合高斯模型就是几个高斯的叠加 

  ![](http://img.blog.csdn.net/20140528180736578?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
  ![](http://img.blog.csdn.net/20140530134729015?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
  fig2.GMM illusstration and the probability of x
  
GMM for state sequence
每个state有一个GMM，包含K个高斯模型参数。如 “hi” (k = 3):
ps: sil表示slience（静音）
 ![](http://img.blog.csdn.net/20140528200425421?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
 fig3. use GMM to estimate the probability of a state sequence given observation{o1,o2,o3}
 其中，每个GMM有一些参数，就是我们要train的输出概率参数
 ![](http://img.blog.csdn.net/20140528200531906?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
 如何求参数呢？同KMease类似，如果已知每个点x^n属于某个类j的概率p(j|x^n),则可以估计其参数：
 ![](http://img.blog.csdn.net/20140530135251546?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
 其中：
 ![](http://img.blog.csdn.net/20140530135311953?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
 
 只要已知了这些参数，我们就可以在predict（识别）时在给定 input sequence的情况下，计算出一串状态转移的概率，如上图要计算的 state sequence 1 -> 2 -> 3的概率：
 ![](http://img.blog.csdn.net/20140528201041078?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
 fig5. probability of S1 -> S2 -> S3 given O1 -> O2 -> O3
 
 
GMM + HMM 
我们获得observation 是语音waveform,以下是一个词识别的全过程：
1)  将waveform切成等长frames,对每个frame提取特征(e.g.MFCC),
2)  对每个frame的特征跑GMM，得到每个frame(o_i)属于每个状态的概率b_state(o_i)
![](http://img.blog.csdn.net/20140528203714828?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

3) 根据每个单词的HMM状态转移概率a计算每个状态sequence生成该frame的概率；哪个词的HMM序列跑出来的概率最大，就判断这段语音属于该词
整体图：
![](http://img.blog.csdn.net/20140528175313171?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
fig7. speech recognition ，a big framework (from Encyclopedia of Information Systems,2002)

好了上面说了怎么作识别，下面谈一谈训练

---------

* Training the params of GMM
GMM参数：高斯分布参数:mean vector $\mu^j$;covariance matrix $\sum^j$
从上面fig4下面的公式我们已经可以看出想求参数必须要知道P(j|x),即 x属于第j个高斯的概率：
 $$P(j|x) = \frac{p(x|j)P(j)}{P(x)}$$
 fig8 .bayesian formula of P(j|x)
 
根据上图P(j|x),我们需要求P（x|j）和P（j）去估计P（j|x）.
这里由于P（x|j）和P（j）都不知道，需要用EM算法迭代估计以最大化P(x) = P(x1)*P(x2)*..*P(xn):
A. 初始化（可以使用kmeans）得到P（j）
B. 迭代：
	E（estimate）-step：根据当前参数(means,variances,mixing parameters)估计P（j|x）
    M (maximization) -step :根据当前P(j|x)计算GMM参数(根据fig4下面的的公式：)
    ![](http://img.blog.csdn.net/20140530135251546?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
其中：
![](http://img.blog.csdn.net/20140530135311953?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

* Training the params of HMM
前面已经有了GMM的traing过程，在这一步，我们的目标是：从Observation序列中估计HMM参数$\lambda$;
假设状态 -> observation服从单核高斯概率分布 ：
 ![](http://img.blog.csdn.net/20140530162550421?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast),则$\lambda$由两部分组成：
  ![](http://img.blog.csdn.net/20140530195145953?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
 
HMM训练过程：迭代
E(estimate)-step:给定observation序列，估计时刻t处于状态sj的概率 $\gamma_t (S_j)$
M(maximization)-step:根据$\gamma_t (S_j)$重新估计HMM参数aij
其中，

**E-step** :给定observation序列，估计时刻t处于状态sj的概率$\gamma_t(S_j)$
为了估计$\gamma_t(S_j)$，定义$\beta_t(S_j)$：t时刻处于状态sj的话，t时刻未来observation的概率。即：$\beta(s_j)=p(x_t+1,x_t+2,x_T |S(t) =sj,\lambda)$
这个可以递归计算：$\beta_t(s_i)$=从状态si转移到其他状态sj的概率$a_{ij}$ \* 状态i下观测到$x_{t+1}$的概率$bi_{t+1}$ * t时刻处于状态sj的话{t+1}后observation的概率$\beta_{t+1} (sj)$
即：
![](http://img.blog.csdn.net/20140530191353765?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

定义刚才的$\gamma (S_j)$ 为state occupation probability，表示给定observation序列，时刻t处于状态sj的概率P（S（t）=sj | x ,$\lambda$）。根据贝叶斯公式P（A|B，C） = P（A，B|C）/P（B|C），有：
![](http://img.blog.csdn.net/20140530194138937?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
由于分子p(A,B|C)为：

![](http://img.blog.csdn.net/20140530193757812?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

其中，at(sj)表示HMM在时刻t处于状态j，且observation={x1,x2,....,xt}的概率 $\alpha_t(s_j) = p(x_1,...,x_t,S(t) = s_j | \lambda)$;
$\beta_t(S_j)$：t时刻处于状态sj的话，t时刻未来observation的概率，且 $P(X | \lambda) = \alpha_T(S_E)$
finally ,带入$\lambda_t(S_j)$的定义有：
$$\gamma_t(S_j) = P(S(t) = s_j | X,\lambda) = \frac{1}{\alpha_T (S_E)} \alpha_t(j)\beta_t(j)$$
ok!bingo! 对应上面的E-step目标，只要给定了observation和当前的HMM参数 $\lambda$,我们就可以估计 $\gamma(s_j)$了吧。


**M-step** ：根据 $\gamma(s_j)$重新估计HMM参数 $\lambda$:
对于$\lambda$中高斯参数部分，和GMM中的M-step一样的（只不过这里写成向量形式）：
![](http://img.blog.csdn.net/20140530200004781?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
对于$\lambda$中的状态转移概率aij,定义C(si -> sj)为从状态Si转到Sj的次数，有：

 ![](http://img.blog.csdn.net/20140530200136921?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

实际计算时，定义每一时刻的转移概率 $\xi_t(s_i,s_j)$为时刻t从si -> sj的概率：
  ![](http://img.blog.csdn.net/20140530200424640?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
那么就有：
  ![](http://img.blog.csdn.net/20140530200615750?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
  
HMM的EM迭代过程的伪代码：
![](http://img.blog.csdn.net/20140530200730218?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWJjamVubmlmZXI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)




















































