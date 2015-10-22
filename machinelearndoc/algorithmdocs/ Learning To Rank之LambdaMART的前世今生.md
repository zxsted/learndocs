 Learning To Rank之LambdaMART的前世今生 
 
[传送](http://blog.csdn.net/huagong_adu/article/details/40710305) 

[toc]


##### 1. 简介

我们知道排序在很多应用场景中属于一个非常核心的模块，最直接的应用就是搜索引擎。当用户提交一个query，搜索引擎会召回很多文档，然后根据文档与query以及用户的相关程度对文档进行排序，这些文档如何排序直接决定了搜索引擎的用户体验。其他重要的应用场景还有在线广告、协同过滤、多媒体检索等的排序。

   LambdaMART是Learning To Rank的其中一个算法，适用于许多排序场景。它是微软Chris Burges大神的成果，最近几年非常火，屡次现身于各种机器学习大赛中，Yahoo! Learning to Rank Challenge比赛中夺冠队伍用的就是这个模型[1]，据说Bing和Facebook使用的也是这个模型。

   本文先简单介绍LambdaMART模型的组成部分，然后介绍与该模型相关的其他几个模型：RankNet、LambdaRank，接着重点介绍LambdaMART的原理，然后介绍LambdaMART的开源实现软件包Ranklib，最后以搜索下拉提示的个性化推荐场景说明LambdaMART的应用。
         
##### 2. 用到的符号

（用了大量的小图片，就不给出了，可以看原始的网页）

##### 3. LambdaMART 说明文字

LambdaMART模型从名字上可以拆分成Lambda和MART两部分，表示底层训练模型用的是MART（Multiple Additive Regression Tree），如果MART看起来比较陌生，那换成GBDT（GradientBoosting Decision Tree）估计大家都很熟悉了，没错，MART就是GBDT。Lambda是MART求解过程使用的梯度，其物理含义是一个待排序的文档下一次迭代应该排序的方向（向上或者向下）和强度。将MART和Lambda组合起来就是我们要介绍的LambdaMART。

##### 4. 神奇的Lambda

为什么LambdaMART可以很好的应用于排序场景？这主要受益于Lambda梯度的使用，前面介绍了Lambda的意义在于量化了一个待排序的文档在下一次迭代时应该调整的方向和强度。

   但Lambda最初并不是诞生于LambdaMART，而是在LambdaRank模型中被提出，而LambdaRank模型又是在RankNet模型的基础上改进而来。如此可见RankNet、LambdaRank、LambdaMART三个的关系很不一般，是一个神秘的基友群，下面我们逐个分析三个基友之间的关系[2]。
         

##### 5. RankNet

RankNet[3]是一个pairwise模型，它把排序问题转换成比较一个(i, j) pair的排序概率问题，即比较di 排在dj 前的概率。它首先计算每个文档的得分，然后根据得分计算文档pair的排序概率：

![](http://img.blog.csdn.net/20141102165945828?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

可以看到这其实就是逻辑回归的sigmoid函数[4]，由于delta 影响的是sigmoid函数的形状，对最终结果影响不大，因此默认使用 delta =1进行简化。RankNet证明了如果知道一个待排序文档的排列中相邻两个文档之间的排序概率，则通过推导可以算出每两个文档之间的排序概率。因此对于一个待排序文档序列，只需计算相邻文档之间的排序概率，不需要计算所有pair，减少计算量。


 然后用交叉熵[5]作为损失函数来衡量 p_ij（预测值） 对 p_ij(实际值)的拟合程度：
 
 ![](http://img.blog.csdn.net/20141102170520519?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

该损失函数有以下几个特点：

   1) 当两个相关性不同的文档算出来的模型分数相同时，损失函数的值大于0，仍会对这对pair做惩罚，使他们的排序位置区分开

   2) 损失函数是一个类线性函数，可以有效减少异常样本数据对模型的影响，因此具有鲁棒性

   Ranknet最终目标是训练出一个算分函数s=f(x:w)，使得所有pair的排序概率估计的损失最小：

![](http://img.blog.csdn.net/20141102170557101?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)


 RankNet采用神经网络模型优化损失函数，采用梯度下降法[6]求解：
 
 ![](http://img.blog.csdn.net/20141102170615868?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)
 
排序问题的评价指标一般有NDCG[7]、ERR[8]、MAP[9]、MRR[10]等，这些指标的特点是不平滑、不连续，无法求梯度，因此无法直接用梯度下降法求解。RankNet的创新点在于没有直接对这些指标进行优化，而是间接把优化目标转换为可以求梯度的基于概率的交叉熵损失函数进行求解。因此任何用梯度下降法优化目标函数的模型都可以采用该方法，RankNet采用的是神经网络模型，其他类似boosting tree等模型也可以使用该方法求解。

##### 6. LambdaRank

![](http://img.blog.csdn.net/20141102170933240?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

图1： pairwise error

如图 1所示，每个线条表示文档，蓝色表示相关文档，灰色表示不相关文档，RankNet以pairwise error的方式计算cost，左图的cost为13，右图通过把第一个相关文档下调3个位置，第二个文档上条5个位置，将cost降为11，但是像NDCG或者ERR等评价指标只关注top k个结果的排序，在优化过程中下调前面相关文档的位置不是我们想要得到的结果。图 1右图左边黑色的箭头表示RankNet下一轮的调序方向和强度，但我们真正需要的是右边红色箭头代表的方向和强度，即更关注靠前位置的相关文档的排序位置的提升。LambdaRank[11]正是基于这个思想演化而来，其中Lambda指的就是红色箭头，代表下一次迭代优化的方向和强度，也就是梯度。

 受LambdaNet的启发，LambdaRank对 DC|Dw_k做因式分解，如下：
 ![](http://img.blog.csdn.net/20141102171122674?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)
 
 其中：
 
 ![](http://img.blog.csdn.net/20141102171226353?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

代入上式得:

![](http://img.blog.csdn.net/20141102171248583?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

其中令：

![](http://img.blog.csdn.net/20141102171326491?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

对于{i,j} \in I 的文档pair，由于di 排在 dj之前 ，因此 S_ij = 1，所以有:

![](http://img.blog.csdn.net/20141102171500388?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

 因此，对每个文档di ，其Lambda为:
 ![](http://img.blog.csdn.net/20141102171646562?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)
 
，即每一个文档下一次调序的方向和强度取决于所有同一query的其他不同label的文档。

   同时LambdaRank还在Lambda中引入评价指标Z （如NDCG、ERR等），把交换两个文档的位置引起的评价指标的变化 
 ![](http://img.blog.csdn.net/20141102171701687?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)
作为其中一个因子，实验表明对模型效果有显著的提升：
![](http://img.blog.csdn.net/20141102171720476?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

 可以看出，LambdaRank不是通过显示定义损失函数再求梯度的方式对排序问题进行求解，而是分析排序问题需要的梯度的物理意义，直接定义梯度，可以反向推导出LambdaRank的损失函数为：
 
 ![](http://img.blog.csdn.net/20141102171837265?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)
 
LambdaRank相比RankNet的优势在于分解因式后训练速度变快，同时考虑了评价指标，直接对问题求解，效果更明显。

##### 7. LambdaMART

LambdaRank重新定义了梯度，赋予了梯度新的物理意义，因此，所有可以使用梯度下降法求解的模型都可以使用这个梯度，MART就是其中一种，将梯度Lambda和MART结合就是大名鼎鼎的LambdaMART[12]。

  MART[13][14]的原理是直接在函数空间对函数进行求解，模型结果由许多棵树组成，每棵树的拟合目标是损失函数的梯度，在LambdaMART中就是Lambda。LambdaMART的具体算法过程如下：

![](http://img.blog.csdn.net/20141102171923201?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

可以看出LambdaMART的框架其实就是MART，主要的创新在于中间计算的梯度使用的是Lambda，是pairwise的。MART需要设置的参数包括：树的数量M、叶子节点数L和学习率v，这3个参数可以通过验证集调节获取最优参数。

  MART支持“热启动”，即可以在已经训练好的模型基础上继续训练，在刚开始的时候通过初始化加载进来即可。下面简单介绍LambdaMART每一步的工作：

1.  每棵树的训练会先遍历所有的训练数据（label不同的文档pair），计算每个pair互换位置导致的指标变化 ![](http://img.blog.csdn.net/20141102172048234?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center) 以及Lambda ， 即： ![](http://img.blog.csdn.net/20141102172136031?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center),然后计算每个文档的Lambda：![](http://img.blog.csdn.net/20141102172139639?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center),再计算每个![](http://img.blog.csdn.net/20141102173638140?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center) 的导数wi，用于后面的Newton step求解叶子节点的数值。
2.   创建回归树拟合第一步生成的 ![](http://img.blog.csdn.net/20141102173651508?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center),划分树节点的标准是Mean Square Error，生成一颗叶子节点数为L的回归树。
3.  对第二步生成的回归树，计算每个叶子节点的数值，采用Newton step求解，即对落入该叶子节点的文档集，用公式 http://img.blog.csdn.net/20141102172228857?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center 计算该叶子节点的输出值
4. 跟新模型，将当前学习到的回归树加入到已有的模型中，用学习率v（也叫shrinkage系数）做regularization。


LambdaMART具有很多优势：

1. 适用于排序场景：不是传统的通过分类或者回归的方法求解排序问题，而是直接求解
2. 损失函数可导：通过损失函数的转换，将类似于NDCG这种无法求导的IR评价指标转换成可以求导的函数，并且富有了梯度的实际物理意义，数学解释非常漂亮
3. 增量学习：由于每次训练可以在已有的模型上继续训练，因此适合于增量学习
4. 组合特征：因为采用树模型，因此可以学到不同特征组合情况
5. 特征选择：因为是基于MART模型，因此也具有MART的优势，可以学到每个特征的重要性，可以做特征选择
6. 适用于正负样本比例失衡的数据：因为模型的训练对象具有不同label的文档pair，而不是预测每个文档的label，因此对正负样本比例失衡不敏感

##### 8. Ranklib 开源工具包
Ranklib[15]是一个开源的Learning ToRank工具包，里面实现了很多Learning To Rank算法模型，其中包括LambdaMART，其源码的算法实现流程大致如下：

![](http://img.blog.csdn.net/20141102172513531?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVhZ29uZ19hZHU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

该工具包定义的数据格式如下：

label           qid:$id      $feaid:$feavalue      $feaid:$feavalue      …      #description

  每行代表一个样本，相同查询请求的样本的qid相同，label表示该样本和该查询请求的相关程度，description描述该样本属于哪个待排序文档，用于区分不同的文档。

  该工具包是用Java实现的，在空间使用上感觉有些低效，但整体设计还是挺好的，Ranker的接口设计的很好，值得学习借鉴。

  另外还有很多其他的LambdaMART的开源实现，有兴趣的可以参考[16][17][18]

##### 9. LambdaMART 的应用

 最后我们以一个实际场景来介绍LambdaMART的应用。现在很多搜索引擎都有一个下拉提示的功能，学术上叫QAC（Query Auto-Completion，query自动补全），主要作用是在用户在搜索引擎输入框输入query的过程中输出一系列跟用户输入query前缀相匹配的query，供用户选择，减少用户的输入，让用户更加便捷的搜索。

 Milad Shokouhi[19]发现有一些query的热度有明显的用户群倾向，例如，当不同用户输入i时，年轻的女性用户倾向于搜instagram，而男性用户则倾向于搜imdb，所以可以对query的下拉提示做个性化排序。

 Milad Shokouhi使用LambdaMART模型作为个性化排序模型，使用了用户的长期历史、短期历史、性别、年龄、所处地域、提示query的原始排序位置等特征，最终效果提升了9%，效果非常明显。

 Milad Shokouhi的工作说明LambdaMART可以应用于个性化排序，且效果非常不错。

##### 10.   总结

  本文在一些相关书籍、paper和开源代码的基础上，简单梳理了LambdaMART的来龙去脉，简单总结：Lambda在RankNet出炉，在LambdaRank升华，在LambdaMART发扬光大，青出于蓝而胜于蓝，模型的数学推导和实际效果都非常漂亮，只要涉及到排序的场景都可以适用，是排序场景的“万金油”。

##### 参考文献

[1]      Learning to Rank Using an Ensemble ofLambda-Gradient Models

[2]      From RankNet to LambdaRank to LambdaMART: AnOverview

[3]      Learning to Rank using Gradient Descent

[4]      Wikipedia-Sigmoid Function

[5]      Wikipedia-Cross Entropy

[6]      Wikipedia-Gradient Descent

[7]      Wikipedia-NDCG

[8]      Expected Reciprocal Rank for Graded Relevance

[9]      Wikipedia-MAP

[10]  Wikipedia-MRR

[11]  Learning to Rank with Nonsmooth CostFunctions

[12]  Adapting boosting for information retrievalmeasures

[13]  Greedy function approximation: A gradientboosting machine

[14]  The Elements of Statistical Learning

[15]  RankLib

[16]  jforests

[17]  xgboost

[18]  gbm

[19]  Learning to Personalize QueryAuto-Completion



#### LambdaMART 开源实现Ranklib 的介绍

[传送](http://www.cnblogs.com/wowarsenal/p/3900359.html)

学习Machine Learning，阅读文献，看各种数学公式的推导，其实是一件很枯燥的事情。有的时候即使理解了数学推导过程，也仍然会一知半解，离自己写程序实现，似乎还有一道鸿沟。所幸的是，现在很多主流的Machine Learning方法，网上都有open source的实现，进一步的阅读这些源码，多做一些实验，有助于深入的理解方法。

Ranklib就是一套优秀的Learning to Rank领域的开源实现，其主页在：http://people.cs.umass.edu/~vdang/ranklib.html，从主页中可以看到实现了哪些方法。其中由微软发布的LambdaMART是IR业内常用的Learning to Rank模型，本文介绍RanklibV2.1(当前最新的时RanklibV2.3，应该大同小异)中的LambdaMART实现，用以帮助理解paper中阐述的方法。

LambdaMART.java中的LambdaMART.learn()是学习流程的管控函数，学习过程主要有下面四步构成：

1. 计算deltaNDCG以及lambda;

2. 以lambda作为label训练一棵regression tree;

3. 在tree的每个叶子节点通过预测的regression lambda值还原出gamma，即最终输出得分；

4. 用3的模型预测所有训练集合上的得分（+learningRate*gamma）,然后用这个得分对每个query的结果排序，计算新的每个query的base ndcg，以此为基础回到第1步，组成森林。

重复这个步骤，直到满足下列两个收敛条件之一：

1. 树的个数达到训练参数设置；

2. Random Forest在validation集合上没有变好。

下面用一组实际的数据来说明整个计算过程，假设我们有10个query的训练数据，每个query下有10个doc，每个q-d对有10个feature，如下：

```shell
 1 0 qid:1830 1:0.002736 2:0.000000 3:0.000000 4:0.000000 5:0.002736 6:0.000000 7:0.000000 8:0.000000 9:0.000000 10:0.000000
 2 0 qid:1830 1:0.025992 2:0.125000 3:0.000000 4:0.000000 5:0.027360 6:0.000000 7:0.000000 8:0.000000 9:0.000000 10:0.000000
 3 0 qid:1830 1:0.001368 2:0.000000 3:0.000000 4:0.000000 5:0.001368 6:0.000000 7:0.000000 8:0.000000 9:0.000000 10:0.000000
 4 1 qid:1830 1:0.188782 2:0.375000 3:0.333333 4:1.000000 5:0.195622 6:0.000000 7:0.000000 8:0.000000 9:0.000000 10:0.000000
 5 1 qid:1830 1:0.077975 2:0.500000 3:0.666667 4:0.000000 5:0.086183 6:0.000000 7:0.000000 8:0.000000 9:0.000000 10:0.000000
 6 0 qid:1830 1:0.075239 2:0.125000 3:0.333333 4:0.000000 5:0.077975 6:0.000000 7:0.000000 8:0.000000 9:0.000000 10:0.000000
 7 1 qid:1830 1:0.079343 2:0.250000 3:0.666667 4:0.000000 5:0.084815 6:0.000000 7:0.000000 8:0.000000 9:0.000000 10:0.000000
 8 1 qid:1830 1:0.147743 2:0.000000 3:0.000000 4:0.000000 5:0.147743 6:0.000000 7:0.000000 8:0.000000 9:0.000000 10:0.000000
 9 0 qid:1830 1:0.058824 2:0.000000 3:0.000000 4:0.000000 5:0.058824 6:0.000000 7:0.000000 8:0.000000 9:0.000000 10:0.000000
10 0 qid:1830 1:0.071135 2:0.125000 3:0.333333 4:0.000000 5:0.073871 6:0.000000 7:0.000000 8:0.000000 9:0.000000 10:0.000000
11 1 qid:1840 1:0.007364 2:0.200000 3:1.000000 4:0.500000 5:0.013158 6:0.000000 7:0.000000 8:0.000000 9:0.000000 10:0.000000
12 1 qid:1840 1:0.097202 2:0.000000 3:0.000000 4:0.000000 5:0.096491 6:0.000000 7:0.000000 8:0.000000 9:0.000000 10:0.000000
13 2 qid:1840 1:0.169367 2:0.000000 3:0.500000 4:0.000000 5:0.169591 6:0.000000 7:0.000000 8:0.000000 9:0.000000 10:0.000000
14 ......

```
为了简便，省略了余下的数据。上面的数据格式是按照Ranklib readme中要求的格式组织（类似于svmlight），除了行号之外，第一列是q-d对的实际label（人标注数据），第二列是qid，后面10列都是feature。

这份数据每组qid中的doc初始顺序可以是随机的，也可以是从实际的系统中获得的当前顺序。总之这个是计算ndcg的初始状态。对于qid=1830，它的10个doc的初始顺序的label序列是：0, 0, 0, 1, 1, 0, 1, 1, 0, 0(虽然这份序列中只有label值为0和1的，实际中也会有2，3等，由自己的标注标准决定)。我们知道dcg的计算公式是：

$$dcg(i)=\frac{2label(i)−1}{log2(i+1)} $$

i表示当前doc在这个qid下的位置（从1开始，避免分母为0），label(i)是doc(i)的标注值。而一个query的dcg则是其下所有doc的加和：
$$dcg(query)=\sum_i \frac{2label(i)−1}{log2(i+1)}$$

据上式可以计算初始状态下每个qid的dcg：

 dcg(qid=1830)=20−1log2(1+1)+20−1log2(2+1)+...+20−1log2(10+1)                                 =0+0+0+0.431+0.387+0+0.333+0.315+0+0=1.466
要计算ndcg，还需要计算理想集的dcg，将初始状态按照label排序，qid=1830得到的序列是1,1,1,1,0,0,0,0,0,0，计算dcg:

   ideal_dcg(qid=1830)=21−1log2(1+1)+21−1log2(2+1)+...+20−1log2(10+1)                                    =1+0.631+0.5+0.431+0+0+0+0+0+0=2.562


两者相除得到初始状态下qid=1830的ndcg:

        ndcg(qid=1830)=dcg(qid=1830)ideal_ndcg(qid=1830)=1.4662.562=0.572
        
 下面要计算每一个doc的deltaNDCG，公式如下：
 
 deltaNDCG(i,j)=|ndcg(original sequence)−ndcg(swap(i,j) sequence)|(3)

deltaNDCG(i,j)是将位置i和位置j的位置互换后产生的ndcg变化（其他位置均不变），显然有相同label的deltaNDCG(i,j)=0。

在qid=1830的初始序列0, 0, 0, 1, 1, 0, 1, 1, 0, 0，由于前3的label都一样，所以deltaNDCG(1,2)=deltaNDCG(1,3)=0，不为0的是deltaNDCG(1,4), deltaNDCG(1,5), deltaNDCG(1,7), deltaNDCG(1,8)。

将1，4位置互换，序列变为1, 0, 0, 0, 1, 0, 1, 1, 0, 0，计算得到dcg=2.036，整个deltaNDCG(1,4)的计算过程如下：

  dcg(qid=1830,swap(1,4))=21−1log2(1+1)+20−1log2(2+1)+...+20−1log2(10+1)

                                                  =1+0+0+0+0.387+0+0.333+0.315+0+0=2.036

        ndcg(swap(1,4))=dcg(swap(1,4))ideal_dcg=2.0362.562=0.795

        deltaNDCG(1,4)=detalNDCG(4,1)=|ndcg(original sequence)−ndcg(swap(1,4))|=|0.572−0.795|=0.222

同样过程可以计算出deltaNDCG(1,5)=0.239, deltaNDCG(1,7)=0.260, deltaNDCG(1,8)=0.267等。

进一步，要计算lambda(i)，根据paper，还需要ρ值，ρ可以理解为doci比docj差的概率，其计算公式为：

$$ρij=\frac{1}{1+eσ(si−sj)}$$(4)

Ranklib中直接取σ=1（σ的值决定rho的S曲线陡峭程度），如下图，蓝，红，绿三种颜色分别对应σ=1，2，4时ρ函数的曲线情形（横坐标是si-sj）:

![](http://images.cnitblog.com/i/317941/201408/091651548343426.png)
初始时，模型为空，所有模型预测得分都是0，所以si=sj=0，ρij≡1/2，lambda(i,j)的计算公式为：
λij=ρij∗|deltaNDCG(i,j)|

上式为Ranklib中实际使用的公式，而在paper中，还需要再乘以-σ，在σ=1时，就是符号正好相反，这两种方式是等价的，符号并不影响模型训练结果（其实大可以把代码中lambda的值前面加一个负号，只是注意在每轮计算train, valid和最后计算test的ndcg的时候，模型预测的得分modelScores要按升序排列——越负的doc越好，而不是源代码中按降序。最后训练出的模型是一样的，这说明这两种方式完全对称，所以符号的问题可以省略。甚至不乘以-σ，更符合人的习惯——分数越大越好，降序排列结果。）：


$$ \lambda_i=\sum_{j(label(i)>label(j))}\lambda_{ij}−\sum_{j(label(i)<label(j))}λ_{ij}$$

计算lambda(1)，由于label(1)=0，qid=1830中的其他doc的label都大于或者等于0，所以lamda(1)的计算中所有的lambda(1,j)都为负项。将之前计算的各deltaNDCG(1,j)代入，且初始状态下ρij≡1/2，所以:

 λ1=−0.5∗(deltaNDCG(1,3)+deltaNDCG(1,4)+deltaNDCG(1,6)+deltaNDCG(1,7))

            =−0.5∗(0.222+0.239+0.260+0.267)=−0.495

可以计算出初始状态下qid=1830各个doc的lambda值，如下：

```shell
 1 qId=1830    0.000   0.000   0.000   -0.111  -0.120  0.000   -0.130  -0.134  0.000   0.000   lambda(1): -0.495
 2 qId=1830    0.000   0.000   0.000   -0.039  -0.048  0.000   -0.058  -0.062  0.000   0.000   lambda(2): -0.206
 3 qId=1830    0.000   0.000   0.000   -0.014  -0.022  0.000   -0.033  -0.036  0.000   0.000   lambda(3): -0.104
 4 qId=1830    0.111   0.039   0.014   0.000   0.000   0.015   0.000   0.000   0.025   0.028   lambda(4): 0.231 
 5 qId=1830    0.120   0.048   0.022   0.000   0.000   0.006   0.000   0.000   0.017   0.019   lambda(5): 0.231 
 6 qId=1830    0.000   0.000   0.000   -0.015  -0.006  0.000   -0.004  -0.008  0.000   0.000   lambda(6): -0.033
 7 qId=1830    0.130   0.058   0.033   0.000   0.000   0.004   0.000   0.000   0.006   0.009   lambda(7): 0.240 
 8 qId=1830    0.134   0.062   0.036   0.000   0.000   0.008   0.000   0.000   0.003   0.005   lambda(8): 0.247 
 9 qId=1830    0.000   0.000   0.000   -0.025  -0.017  0.000   -0.006  -0.003  0.000   0.000   lambda(9): -0.051
10 qId=1830    0.000   0.000   0.000   -0.028  -0.019  0.000   -0.009  -0.005  0.000   0.000   lambda(10): -0.061
```
上表中每一列都是考虑了符号的lamda(i,j)，即如果label(i)<label(j)，则为负值，反之为正值，每行结尾的lamda(i)是前面的加和，即为最终的lambda(i)。

可以看到，lambda(i)在系统中表达了doc(i)上升或者下降的强度，label越高，位置越后，lambda(i)为正值，越大，表示趋向上升的方向，力度也越大；label越小，位置越靠前，lambda(i)为负值，越小，表示趋向下降的方向，力度也大（lambda(i)的绝对值表达了力度。）

然后Regression Tree开始以每个doc的lamda值为目标，训练模型。

上一节中介绍了 λ 的计算，lambdaMART就以计算的每个doc的 λ 值作为label，训练Regression Tree，并在最后对叶子节点上的样本 lambda 均值还原成 γ ，乘以learningRate加到此前的Regression Trees上，更新score，重新对query下的doc按score排序，再次计算deltaNDCG以及 λ ，如此迭代下去直至树的数目达到参数设定或者在validation集上不再持续变好（一般实践来说不在模型训练时设置validation集合，因为validation集合一般比训练集合小很多，很容易收敛，达不到效果，不如训练时一步到位，然后另起test集合做结果评估）。

其实Regression Tree的训练很简单，最主要的就是决定如何分裂节点。lambdaMART采用最朴素的最小二乘法，也就是最小化平方误差和来分裂节点：即对于某个选定的feature，选定一个值val，所有<=val的样本分到左子节点，>val的分到右子节点。然后分别对左右两个节点计算平方误差和，并加在一起作为这次分裂的代价。遍历所有feature以及所有可能的分裂点val(每个feature按值排序，每个不同的值都是可能的分裂点)，在这些分裂中找到代价最小的。

举个栗子，假设样本只有上一节中计算出 λ 的那10个：

```shell
qId=1830 features and lambdas
 2 qId=1830    1:0.003 2:0.000 3:0.000 4:0.000 5:0.003 6:0.000 7:0.000 8:0.000 9:0.000 10:0.000    lambda(1):-0.495
 3 qId=1830    1:0.026 2:0.125 3:0.000 4:0.000 5:0.027 6:0.000 7:0.000 8:0.000 9:0.000 10:0.000    lambda(2):-0.206
 4 qId=1830    1:0.001 2:0.000 3:0.000 4:0.000 5:0.001 6:0.000 7:0.000 8:0.000 9:0.000 10:0.000    lambda(3):-0.104
 5 qId=1830    1:0.189 2:0.375 3:0.333 4:1.000 5:0.196 6:0.000 7:0.000 8:0.000 9:0.000 10:0.000    lambda(4):0.231
 6 qId=1830    1:0.078 2:0.500 3:0.667 4:0.000 5:0.086 6:0.000 7:0.000 8:0.000 9:0.000 10:0.000    lambda(5):0.231
 7 qId=1830    1:0.075 2:0.125 3:0.333 4:0.000 5:0.078 6:0.000 7:0.000 8:0.000 9:0.000 10:0.000    lambda(6):-0.033
 8 qId=1830    1:0.079 2:0.250 3:0.667 4:0.000 5:0.085 6:0.000 7:0.000 8:0.000 9:0.000 10:0.000    lambda(7):0.240
 9 qId=1830    1:0.148 2:0.000 3:0.000 4:0.000 5:0.148 6:0.000 7:0.000 8:0.000 9:0.000 10:0.000    lambda(8):0.247
10 qId=1830    1:0.059 2:0.000 3:0.000 4:0.000 5:0.059 6:0.000 7:0.000 8:0.000 9:0.000 10:0.000    lambda(9):-0.051
11 qId=1830    1:0.071 2:0.125 3:0.333 4:0.000 5:0.074 6:0.000 7:0.000 8:0.000 9:0.000 10:0.000    lambda(10):-0.061
```
 
上表中除了第一列是qId，最后一列是lambda外，其余都是feature，比如我们选择feature(1)的0.059做分裂点，则左子节点<=0.059的doc有: 1, 2, 3, 9；而>0.059的被安排到右子节点，doc有4, 5, 6, 7, 8, 10。由此左右两个子节点的lambda均值分别为：

 

        λL¯=λ1+λ2+λ3+λ94=−0.495−0.206−0.104−0.0514=−0.214

        λR¯=λ4+λ5+λ6+λ7+λ8+λ106=0.231+0.231−0.033+0.240+0.247−0.0616=0.143

 

继续计算左右子节点的平方误差和：

 

        sL=∑i∈L(λi−λL¯)2=(−0.495+0.214)2+(−0.206+0.214)2+(−0.104+0.214)2+(−0.051+0.214)2=0.118

        sR=∑i∈R(λi−λR¯)2=(0.231−0.143)2+(0.231−0.143)2+(−0.033−0.143)2+(0.240−0.143)2+(0.247−0.143)2+(0.016−0.143)2=0.083

 

因此将feature(1)的0.059的均方差（分裂代价）是：

 

        Cost0.059@feature(1)=sL+sR=0.118+0.083=0.201

 

我们可以像上面那样遍历所有feature的不同值，尝试分裂，计算Cost，最终选择所有可能分裂中最小Cost的那一个作为分裂点。然后将 sL 和 sR 分别作为左右子节点的属性存储起来，并把分裂的样本也分别存储到左右子节点中，然后维护一个队列，始终按平方误差和 s 降序插入新分裂出的节点，每次从该队列头部拿出一个节点（并基于这个节点上的样本）进行分裂（即最大均方差优先分裂），直到树的分裂次数达到参数设定（训练时传入的leaf值，叶子节点的个数与分裂次数等价）。这样我们就训练出了一棵Regression Tree。

 

上面讲述了一棵树的标准分裂过程，需要多提一点的是，树的分裂还有一个参数设定：叶子节点上的最少样本数，比如我们设定为3，则在feature(1)处，0.001和0.003两个值都不能作为分裂点，因为用它们做分裂点，左子树的样本数分别是1和2，均<3。叶子节点的最少样本数越小，模型则拟合得越好，当然也容易过拟合（over-fitting）；反之如果设置得越大，模型则可能欠拟合（under-fitting），实践中可以使用cross validation的办法来寻找最佳的参数设定。
标签: 机器学习, Learning to Rank, lambdaMART, Ranklib



#### learning to Rank 简介

[传送](http://www.cnblogs.com/kemaswill/archive/2013/06/01/3109497.html)

去年实习时，因为项目需要，接触了一下Learning to Rank(以下简称L2R)，感觉很有意思，也有很大的应用价值。L2R将机器学习的技术很好的应用到了排序中，并提出了一些新的理论和算法，不仅有效地解决了排序的问题，其中一些算法(比如LambdaRank)的思想非常新颖，可以在其他领域中进行借鉴。鉴于排序在许多领域中的核心地位，L2R可以被广泛的应用在信息(文档)检索，协同过滤等领域。

  本文将对L2R做一个比较深入的介绍，主要参考了刘铁岩、李航等人的几篇相关文献[1,2,3]，我们将围绕以下几点来介绍L2R：现有的排序模型，为什么需要使用机器学习的方法来进行排序，L2R特征的选取，L2R训练数据的获取，L2R训练和测试，L2R算法分类和简介，L2R效果评价等。

##### 1. 现有的排序模型

 排序(Ranking)一直是信息检索的核心研究问题，有大量的成熟的方法，主要可以分为以下两类：相关度排序模型和重要性排序模型。
 
###### 1.1 相关度排序模型(Relevance Ranking Model)
 相关度排序模型根据查询和文档之间的相似度来对文档进行排序。常用的模型包括：布尔模型(Boolean Model)，向量空间模型(Vector Space Model)，隐语义分析(Latent Semantic Analysis)，BM25，LMIR模型等等。
 
###### 1.2  重要性排序模型(Importance Ranking Model)
重要性排序模型不考虑查询，而仅仅根据网页(亦即文档)之间的图结构来判断文档的权威程度，典型的权威网站包括Google，Yahoo!等。常用的模型包括PageRank，HITS，HillTop，TrustRank等等。


##### 2. 为什么需要使用机器学习的方法来进行排序

对于传统的排序模型，单个模型往往只能考虑某一个方面(相关度或者重要性)，所以只是用单个模型达不到要求。搜索引擎通常会组合多种排序模型来进行排序，但是，如何组合多个排序模型来形成一个新的排序模型，以及如何调节这些参数，都是一个很大的问题。

  使用机器学习的方法，我们可以把各个现有排序模型的输出作为特征，然后训练一个新的模型，并自动学得这个新的模型的参数，从而很方便的可以组合多个现有的排序模型来生成新的排序模型。
  
  
##### 3. L2R 的特征选取

与文本分类不同，L2R考虑的是给定查询的文档集合的排序。所以，L2R用到的特征不仅仅包含文档d本身的一些特征(比如是否是Spam)等，也包括文档d和给定查询q之间的相关度，以及文档在整个网络上的重要性(比如PageRank值等)，亦即我们可以使用相关性排序模型和重要性排序模型的输出来作为L2R的特征。

 1. 传统排序模型的输出，既包括相关性排序模型的输出f(q,d)，也包括重要性排序模型的输出。

 2. 文档本身的一些特征，比如是否是Spam等。
  
##### 4. L2R 训练数据的获取

  L2R的训练数据可以有三种形式：对于每个查询，各个文档的绝对相关值(非常相关，比较相关，不相关，等等)；对于每个查询，两两文档之间的相对相关值(文档1比文档2相关，文档4比文档3相关，等等)；对于每个查询，所有文档的按相关度排序的列表(文档1>文档2>文档3)。这三种形式的训练数据之间可以相互转换，详见[1]。

  训练数据的获取有两种主要方法：人工标注[3]和从日志文件中挖掘[4]。

  人工标注：首先从搜索引擎的搜索记录中随机抽取一些查询，将这些查询提交给多个不同的搜索引擎，然后选取各个搜索引擎返回结果的前K个，最后由专业人员来对这些文档按照和查询的相关度进行标注。

  从日志中挖掘：搜索引擎都有大量的日志记录用户的行为，我们可以从中提取出L2R的训练数据。Joachims提出了一种很有意思的方法[4]：给定一个查询，搜索引擎返回的结果列表为L，用户点击的文档的集合为C，如果一个文档di被点击过，另外一个文档dj没有被点击过，并且dj在结果列表中排在di之前，则di>dj就是一条训练记录。亦即训练数据为：{di>dj|di属于C，dj属于L-C，p(dj)<p(di)}，其中p(d)表示文档d在查询结果列表中的位置，越小表示越靠前。
  
##### 5. L2R 模型训练

 L2R是一个有监督学习过程。

  对与每个给定的查询-文档对(query document pair)，抽取相应的特征(既包括查询和文档之间的各种相关度，也包括文档本身的特征以及重要性等)，另外通过或者人工标注或者从日志中挖掘的方法来得到给定查询下文档集合的真实序列。然后我们使用L2R的各种算法来学到一个排序模型，使其输出的文档序列和真实序列尽可能相似。
  
  
##### 6. L2R 算法分类和简介

 L2R算法主要包括三种类别：PointWise，PairWise，ListWise。
 
###### 1. PointWise L2R

 PointWise方法只考虑给定查询下，单个文档的绝对相关度，而不考虑其他文档和给定查询的相关度。亦即给定查询q的一个真实文档序列，我们只需要考虑单个文档di和该查询的相关程度ci，亦即输入数据应该是如下的形式：
 ![](http://images.cnitblog.com/blog/326731/201306/01143025-0fac95de93ea4edc8592a41fc7470566.png)
 
Pointwise方法主要包括以下算法：Pranking (NIPS 2002), OAP-BPM (EMCL 2003), Ranking with Large Margin Principles (NIPS 2002), Constraint Ordinal Regression (ICML 2005)。

  Pointwise方法仅仅使用传统的分类，回归或者Ordinal Regression方法来对给定查询下单个文档的相关度进行建模。这种方法没有考虑到排序的一些特征，比如文档之间的排序结果针对的是给定查询下的文档集合，而Pointwise方法仅仅考虑单个文档的绝对相关度；另外，在排序中，排在最前的几个文档对排序效果的影响非常重要，Pointwise没有考虑这方面的影响。
  
###### 2. Pairwise L2R
 Pairwise方法考虑给定查询下，两个文档之间的相对相关度。亦即给定查询q的一个真实文档序列，我们只需要考虑任意两个相关度不同的文档之间的相对相关度：di>dj，或者di<dj。
 
 ![](http://images.cnitblog.com/blog/326731/201306/01143051-b74d64f1380e4317956feeac9695f1da.png)
 
 Pairwise方法主要包括以下几种算法：Learning to Retrieve Information (SCC 1995), Learning to Order Things (NIPS 1998), Ranking SVM (ICANN 1999), RankBoost (JMLR 2003), LDM (SIGIR 2005), RankNet (ICML 2005), Frank (SIGIR 2007), MHR(SIGIR 2007), Round Robin Ranking (ECML 2003), GBRank (SIGIR 2007), QBRank (NIPS 2007), MPRank (ICML 2007), IRSVM (SIGIR 2006) 。

  相比于Pointwise方法，Pairwise方法通过考虑两两文档之间的相对相关度来进行排序，有一定的进步。但是，Pairwise使用的这种基于两两文档之间相对相关度的损失函数，和真正衡量排序效果的一些指标之间，可能存在很大的不同，有时甚至是负相关，如下图所示(pairwise的损失函数和NDCG之呈现出负相关性)：
  
  ![](http://images.cnitblog.com/blog/326731/201306/01145822-2e392a00c9b3492695f900279b15dd9a.png)
  
 另外，有的Pairwise方法没有考虑到排序结果前几名对整个排序的重要性，也没有考虑不同查询对应的文档集合的大小对查询结果的影响(但是有的Pairwise方法对这些进行了改进，比如IR SVM就是对Ranking SVM针对以上缺点进行改进得到的算法)。
 
 
###### 3. Listwise L2R
与Pointwise和Pairwise方法不同，Listwise方法直接考虑给定查询下的文档集合的整体序列，直接优化模型输出的文档序列，使得其尽可能接近真实文档序列。

  Listwise算法主要包括以下几种算法：LambdaRank (NIPS 2006), AdaRank (SIGIR 2007), SVM-MAP (SIGIR 2007), SoftRank (LR4IR 2007), GPRank (LR4IR 2007), CCA (SIGIR 2007), RankCosine (IP&M 2007), ListNet (ICML 2007), ListMLE (ICML 2008) 。

  相比于Pointwise和Pairwise方法，Listwise方法直接优化给定查询下，整个文档集合的序列，所以比较好的解决了克服了以上算法的缺陷。Listwise方法中的LambdaMART(是对RankNet和LambdaRank的改进)在Yahoo Learning to Rank Challenge表现出最好的性能。
  
##### 7. L2R 效果评价
L2R是用机器学习的方法来进行排序，所以评价L2R效果的指标就是评价排序的指标，主要包括一下几种：

  1) WTA(Winners take all) 对于给定的查询q，如果模型返回的结果列表中，第一个文档是相关的，则WTA(q)=1，否则为0.

  2) MRR(Mean Reciprocal Rank) 对于给定查询q，如果第一个相关的文档的位置是R(q)，则MRR(q)=1/R(q)。

  3) MAP(Mean Average Precision) 对于每个真实相关的文档d，考虑其在模型排序结果中的位置P(d)，统计该位置之前的文档集合的分类准确率，取所有这些准确率的平均值。

  4) NDCG(Normalized Discounted Cumulative Gain) 是一种综合考虑模型排序结果和真实序列之间的关系的一种指标，也是最常用的衡量排序结果的指标，详见Wikipedia。

  5) RC(Rank Correlation) 使用相关度来衡量排序结果和真实序列之间的相似度，常用的指标是Kendall s Tau。 
  
##### 参考文献：

  [1]. Learning to Rank for Information Retrieval. Tie-yan Liu.

  [2]. Learning to Rank for Information Retrieval and Natural Language Processing. Hang Li.

  [3]. A Short Introduction to Learning to Rank. Hang Li.

  [4]. Optimizing Search Engines using Clickthrough Data. Thorsten Joachims. SIGKDD,2002.

  [5]. Learning to Rank小结


##### learning to Rank 之 RankNet 算法简介

[传送](http://www.cnblogs.com/kemaswill/archive/2013/08/14/kemaswill.html)

排序一直是信息检索的核心问题之一, Learning to Rank(简称LTR)用机器学习的思想来解决排序问题(关于Learning to Rank的简介请见我的博文Learning to Rank简介)。LTR有三种主要的方法：PointWise，PairWise，ListWise. RankNet是一种Pairwise方法, 由微软研究院的Chris Burges等人在2005年ICML上的一篇论文Learning to Rank Using Gradient Descent中提出，并被应用在微软的搜索引擎Bing当中。



###### 1. 损失函数 
损失函数一直是各种Learning to Rank算法的核心, RankNet亦然.

  RankNet是一种Pairwise方法, 定义了文档对<A, B>的概率(假设文档A, B的特征分别为xi,xj):
  
 ![](http://images.cnitblog.com/blog/326731/201308/14173353-db7fbd01ed464815bd263c16b2fe6e8c.png)
 
其中oij=oi-oj, oi=f(xi), RankNet使用神经网络来训练模型, 所以f(xi)是神经网络的输出。

  如果文档A比文档B和查询q更加相关, 则目标概率:=1, 如果文档B比文档A更相关, 目标函数=0, 如果A和B同样相关, 则=0.5.

  有了模型输出的概率Pij和目标概率, 我们使用交叉熵来作为训练的损失函数:
  
  ![](http://images.cnitblog.com/blog/326731/201308/14174202-05329a350baa453692259e2cd1639b74.png)
  
在三种不同的目标概率下， 损失函数和 o_ij 之间的关系如下：
![](http://images.cnitblog.com/blog/326731/201308/14175018-fec82bc5bb0945d1958cb81d9ef4abb2.png)
可以看到, 在=1时, oij越大损失函数越小, =0时, 越小损失函数越小, =0.5时, =0.5时损失函数最小。

  本身也有一些非常好的特性, 给定和, 得到:   ( 推到公式)
  
  ![](http://images.cnitblog.com/blog/326731/201308/14175521-6488044ef2b24e10a7b98038a56e0f20.png)

令==P, 得到P和的关系如下图所示:

![](http://images.cnitblog.com/blog/326731/201308/14180603-e00a757776754170b9a6b971a34f3db7.png)

 可以看到, 当P>0.5时, 亦即i>j, j>k时, 有>0.5, 亦即i>k, 这说明概率P具有一致性(consistency).


###### 2. RankNet 算法

 RankNet使用神经网络来训练模型, 使用梯度下降来优化损失函数。特别的, Chris Burges等人在论文中证明, 对于m个文档{d1,d2,...,dm}, 需要且只需要知道相邻文档之间的概率Pij,就可以算出任意两个文档之间的后验概率. 可以实现对m个文档做任意排列, 然后以排列后的相邻文档之间的概率Pij作为训练数据, 然后训练模型, 时间复杂度为O(N), 优于Ranking SVM的O(N2)。

  在使用神经网络进行训练时, 将排好序的文档逐个的放入神经网络进行训练, 然后通过前后两个文档之间的oij=oi-oj来训练模型, 每一次迭代, 前向传播m次, 后向反馈m-1次。

  RankLib中有RankNet等Learning to Rank算法的开源Java实现。


##### learning to Rank 之 Ranking svm 简介

排序一直是信息检索的核心问题之一，Learning to Rank(简称LTR)用机器学习的思想来解决排序问题(关于Learning to Rank的简介请见我的博文Learning to Rank简介)。LTR有三种主要的方法：PointWise，PairWise，ListWise。Ranking SVM算法是PointWise方法的一种，由R. Herbrich等人在2000提出, T. Joachims介绍了一种基于用户Clickthrough数据使用Ranking SVM来进行排序的方法(SIGKDD, 2002)。

###### 1. Ranking SVM 的主要思想

 Ranking SVM是一种Pointwise的排序算法, 给定查询q, 文档d1>d2>d3(亦即文档d1比文档d2相关, 文档d2比文档d3相关, x1, x2, x3分别是d1, d2, d3的特征)。为了使用机器学习的方法进行排序，我们将排序转化为一个分类问题。我们定义新的训练样本, 令x1-x2, x1-x3, x2-x3为正样本,令x2-x1, x3-x1, x3-x2为负样本, 然后训练一个二分类器(支持向量机)来对这些新的训练样本进行分类，如下图所示:
 
![](http://images.cnitblog.com/blog/326731/201308/06203139-bb1bbbed90b64334a3ae4e9aaaad5900.png)

![](http://images.cnitblog.com/blog/326731/201308/06203148-8ee37524771d4e11bfea4d081caad3c6.png)

左图中每个椭圆代表一个查询, 椭圆内的点代表那些要计算和该查询的相关度的文档, 三角代表很相关, 圆圈代表一般相关, 叉号代表不相关。我们把左图中的单个的文档转换成右图中的文档对(di, dj), 实心方块代表正样本, 亦即di>dj, 空心方块代表负样本, 亦即di<dj。

##### Ranking SVM
 将排序问题转化为分类问题之后, 我们就可以使用常用的机器学习方法解决该问题。 Ranking SVM使用SVM来进行分类:
 
![](http://images.cnitblog.com/blog/326731/201308/06204123-18f1da293d8149fd9766cd3477262ac8.png)

其中w为参数向量, x为文档的特征,y为文档对之间的相对相关性, ξ为松弛变量。

###### 3. 使用Clickthrough数据作为训练数据

  T. Joachims提出了一种非常巧妙的方法, 来使用Clickthrough数据作为Ranking SVM的训练数据。

  假设给定一个查询"Support Vector Machine", 搜索引擎的返回结果为
  
  ![](http://images.cnitblog.com/blog/326731/201308/06204543-d05e90d60a8c48308aa1e0c19ec98fea.png)
  
其中1, 3, 7三个结果被用户点击过, 其他的则没有。因为返回的结果本身是有序的, 用户更倾向于点击排在前面的结果, 所以用户的点击行为本身是有偏(Bias)的。为了从有偏的点击数据中获得文档的相关信息, 我们认为: 如果一个用户点击了a而没有点击b, 但是b在排序结果中的位置高于a, 则a>b。

  所以上面的用户点击行为意味着: 3>2, 7>2, 7>4, 7>5, 7>6。
  
###### 4.  Ranking SVM的开源实现

  H. Joachims的主页上有Ranking SVM的开源实现。

  数据的格式与LIBSVM的输入格式比较相似, 第一列代表文档的相关性, 值越大代表越相关, 第二列代表查询, 后面的代表特征
  
```shell
复制代码

3 qid:1 1:1 2:1 3:0 4:0.2 5:0 # 1A
2 qid:1 1:0 2:0 3:1 4:0.1 5:1 # 1B 
1 qid:1 1:0 2:1 3:0 4:0.4 5:0 # 1C
1 qid:1 1:0 2:0 3:1 4:0.3 5:0 # 1D  
1 qid:2 1:0 2:0 3:1 4:0.2 5:0 # 2A  
2 qid:2 1:1 2:0 3:1 4:0.4 5:0 # 2B 
1 qid:2 1:0 2:0 3:1 4:0.1 5:0 # 2C 
1 qid:2 1:0 2:0 3:1 4:0.2 5:0 # 2D  
2 qid:3 1:0 2:0 3:1 4:0.1 5:1 # 3A 
3 qid:3 1:1 2:1 3:0 4:0.3 5:0 # 3B 
4 qid:3 1:1 2:0 3:0 4:0.4 5:1 # 3C 
1 qid:3 1:0 2:1 3:1 4:0.5 5:0 # 3D

```
训练模型和对测试数据进行排序的代码分别为:

  ./svm_rank_learn path/to/train path/to/model 
 ./svm_classify path/to/test path/to/model path/to/rank_result
 
 
##### ListNet 算法简介

 排序一直是信息检索的核心问题之一，Learning to Rank(简称LTR)用机器学习的思想来解决排序问题。LTR有三种主要的方法：PointWise，PairWise，ListWise。ListNet算法就是ListWise方法的一种，由刘铁岩，李航等人在ICML2007的论文Learning to Rank:From Pairwise approach to Listwise Approach中提出。

  Pairwise方法的实际上是把排序问题转换成分类问题，以最小化文档对的分类错误为目标。但是评估排序结果的好坏通常采用MAP或NDCG等考虑文档排序的方法，所以Pairwise方法的损失函数并不是非常合适。 ListNet算法定义了一种Listwise的损失函数，该损失函数表示由我们的模型计算得来的文档排序和真正的文档排序之间的差异，ListNet最小化该损失函数以达到排序的目的。

  ListNet首先把文档的排序列表转换成概率分布，然后选取交叉熵来衡量由模型训练出的文档排序和真正的文档排序之间的差异，最小化这个差异值来完成排序。下面我们从如何把文档列表转换成概率，如何计算概率分布之间的差异值，如何优化差异值三个部分来介绍ListNet算法
  
  
###### 1. 组合概率和Top-K概率

1. 组合概率

 假设我们需要对n篇文档进行排序，我们用π=<π(1),π(2),...,π(n)>表示一种排列组合，其中π(i)表示排列在第i个位置的文档。设Φ(.)是一个递增和恒大于0的函数，Φ(x)可以是线性函数Φ(x)=αx或者指数函数Φ(x)=exp(x),则排列组合π的概率为：
 
 ![](http://images.cnitblog.com/blog/326731/201301/24165539-649592c28bf4437182918b1300844760.png)
 
 其中Sπ(j)表示排列在第j个位置的文档的得分。组合概率的计算复杂度为O(n!)，当文档的数量较多时，计算量太大，所以ListNet选用了另外一种概率:Top-K概率。
 
 2. Top -k 概率
 
 序列(j1,j2,...,jk)的Top-K概率表示这些文档排在n个文档中前K个的概率。在定义Top-K概率之前，需要首先定义前K个文档为(j1,j2,...,jk)的文档排序的Top-K Subgroup：
 
 ![](http://images.cnitblog.com/blog/326731/201301/24172336-923627886cd9444e930697cc2666f008.png)
 
而Gk代表所有的Top-K Subgroup集合：

![](http://images.cnitblog.com/blog/326731/201301/24172458-abdd729dfe1e4822a17e8d5d83672e53.png)

Gk中总共有N!/(N-k)!种不同的组合，大大低于组合概率的N!种组合。

n个文档中(j1,j2,...,jk)排在前k个的概率，亦即(j1,j2,...,jk)的Top-K概率为：

![](http://images.cnitblog.com/blog/326731/201301/24172854-ee06cbb272b24ce0a94f43eaca208ec1.png)

(j1,j2,...,jk)的Top-K概率的计算方法为：

![](http://images.cnitblog.com/blog/326731/201301/24173018-277847e80fec41deba11dae1c379525c.png)

###### 2 计算概率分布的差异值

在得到利用模型训练出的文档排序和真正的文档排序的概率分布之后，我们可以使用多种方法来计算两个概率分布之间的差异值作为损失函数，ListNet采用交叉熵来计算两个概率分布之间的差异。

  两个概率分布p和q之间的交叉熵定义为：
  
![](http://upload.wikimedia.org/math/c/b/a/cbad10a6095971e2b3ae438833ec4bf4.png)

 在ListNet中，假设Py(i)(g)表示实际的文档排序g的概率，而Pz(i)(g)表示模型计算得来的文档排序g的概率，则两个文档排序概率分布之间的交叉熵为：
 
 ![](http://images.cnitblog.com/blog/326731/201301/24173953-06ba9f239f44468cb9a70a3353edadca.png)
 
###### 3. 优化损失函数

 ListNet使用神经网络来计算文档的得分值，选取Φ(x)=exp(x)，然后使用梯度下降(Gradient Descent)的方法来不断更新神经网络的参数ω, 最小化损失函数, ω的迭代公式如下:
 
![](http://images.cnitblog.com/blog/326731/201301/24174423-ccf41e9e9daa411ea21a85a36ea7148a.png)


  
 


#### （附）受限波尔兹曼机

受限玻尔兹曼机(Restricted Boltzmann Machine,简称RBM)是由Hinton和Sejnowski于1986年提出的一种生成式随机神经网络(generative stochastic neural network)，该网络由一些可见单元(visible unit，对应可见变量，亦即数据样本)和一些隐藏单元(hidden unit，对应隐藏变量)构成，可见变量和隐藏变量都是二元变量，亦即其状态取{0,1}。整个网络是一个二部图，只有可见单元和隐藏单元之间才会存在边，可见单元之间以及隐藏单元之间都不会有边连接，如下图所示：

![](http://images.cnitblog.com/blog/326731/201307/21110338-415413c3686645a890cb490a36f8ef70.png)

图所示的RBM含有12个可见单元(构成一个向量v)和3个隐藏单元(构成一个向量h)，W是一个12*3的矩阵，表示可见单元和隐藏单元之间的边的权重。

##### 1. RBM的学习目标-最大化似然(Maximizing likelihood)

  RBM是一种基于能量(Energy-based)的模型，其可见变量v和隐藏变量h的联合配置(joint configuration)的能量为：

![](http://images.cnitblog.com/blog/326731/201307/21110949-bbc3c4b03f294c4bbd0ed3bbda20e7f6.png)

其中θ是RBM的参数{W, a, b}, W为可见单元和隐藏单元之间的边的权重，b和a分别为可见单元和隐藏单元的偏置(bias)。

  有了v和h的联合配置的能量之后，我们就可以得到v和h的联合概率：
  
  ![](http://images.cnitblog.com/blog/326731/201307/21111309-66ff27074d7e4395b9318ec90da8e2a2.png)

其中Z(θ)是归一化因子，也称为配分函数(partition function)。根据式子-1，可以将上式写为：

![](http://images.cnitblog.com/blog/326731/201307/21111713-7be5f608b8504de89dc72448773e573c.png)

我们希望最大化观测数据的似然函数P(v)，P(v)可由式子-3求P(v,h)对h的边缘分布得到:

![](http://images.cnitblog.com/blog/326731/201307/21112100-5814bd3e0606470f81b5b026115d61f3.png)

我们通过最大化P(v)来得到RBM的参数，最大化P(v)等同于最大化log(P(v))=L(θ)：

![](http://images.cnitblog.com/blog/326731/201307/21112339-69a71593ad54496b9e767433ff7646bf.png)

###### 2 . RBM的学习方法-cd(Contrastive Divergence，对比散列)

可以通过随机梯度下降(stichastic gradient descent)来最大化L(θ)，首先需要求得L(θ)对W的导数：

![](http://images.cnitblog.com/blog/326731/201307/21112601-bdada787edee432b92f6ca7bfcf943d8.png)

 经过简化可以得到：
 
![](http://images.cnitblog.com/blog/326731/201307/21112938-e96b9b93b793418f9f5c17562db559fa.png)

后者等于：
![](http://images.cnitblog.com/blog/326731/201307/21113221-7dd42f87085a4840b2255b71f7a4e070.png)

式子-7中的前者比较好计算，只需要求vihj在全部数据集上的平均值即可，而后者涉及到v，h的全部2|v|+|h|种组合，计算量非常大(基本不可解)。

  为了解决式子-8的计算问题，Hinton等人提出了一种高效的学习算法-CD(Contrastive Divergence)，其基本思想如下图所示：
  
 ![](http://images.cnitblog.com/blog/326731/201307/21165701-d04c0f58d5c4411980cd1f73637dd899.png)
 

 首先根据数据v来得到h的状态，然后通过h来重构(Reconstruct)可见向量v1，然后再根据v1来生成新的隐藏向量h1。因为RBM的特殊结构(层内无连接，层间有连接)， 所以在给定v时，各个隐藏单元hj的激活状态之间是相互独立的，反之，在给定h时，各个可见单元的激活状态vi也是相互独立的，亦即：
 
![](http://images.cnitblog.com/blog/326731/201307/21114405-febede8cfd01499ba72513e249dec2db.png)

重构的可见向量v1和隐藏向量h1就是对P(v,h)的一次抽样，多次抽样得到的样本集合可以看做是对P(v,h)的一种近似，使得式子-7的计算变得可行。

  RBM的权重的学习算法：
  
 1.   取一个样本数据，把可见变量的状态设置为这个样本数据。随机初始化W。
 2.   根据式子-9的第一个公式来更新隐藏变量的状态，亦即hj以P(hj=1|v)的概率设置为状态1，否则为0。然后对于每个边vihj，计算Pdata(vihj)=vi*hj(注意，vi和hj的状态都是取{0,1})。
 3.  根据h的状态和式子-9的第二个公式来重构v1，并且根据v1和式子-9的第一个公式来求得h1，计算Pmodel(v1ih1j)=v1i*h1j。
 4.   更新边vihj的权重Wij为Wij=Wij+L*(Pdata(vihj)=Pmodel(v1ih1j))。
 5.   取下一个数据样本，重复1-4的步骤。
 6.   以上过程迭代K次。


##### 深度神经网络存在的问题

###### 1. 常用的神经网络模型, 一般只包含输入层, 输出层和一个隐藏层：

![](http://images.cnitblog.com/blog/326731/201308/18144053-59fa75b367cd421994bcecffe18340d2.png)

理论上来说, 隐藏层越多, 模型的表达能力应该越强。但是, 当隐藏层数多于一层时, 如果我们使用随机值来初始化权重, 使用梯度下降来优化参数就会出现许多问题[1]:

  1.  如果初始权重值设置的过大, 则训练过程中权重值会落入局部最小值(而不是全局最小值)。
  2.  如果初始的权重值设置的过小, 则在使用BP调整参数时, 当误差传递到最前面几层时, 梯度值会很小, 从而使得权重的改变很小, 无法得到最优值。[疑问, 是否可以提高前几层的learning rate来解决这个问题?]

  所以, 如果初始的权重值已经比较接近最优解时, 使用梯度下降可以得到一个比较好的结果, Hinton等在2006年提出了一种新的方法[2]来求得这种比较接近最优解的初始权重。
  
###### 2. DEEP Belief NetWork

 DBN是由Hinton在2006年提出的一种概率生成模型, 由多个限制玻尔兹曼机(RBM)[3]堆栈而成:
 
 ![](http://images.cnitblog.com/blog/326731/201308/18150232-4dad0cc2c9b84177aaf31d3b7d143890.png)
 
在训练时, Hinton采用了逐层无监督的方法来学习参数。首先把数据向量x和第一层隐藏层作为一个RBM, 训练出这个RBM的参数(连接x和h1的权重, x和h1各个节点的偏置等等), 然后固定这个RBM的参数, 把h1视作可见向量, 把h2视作隐藏向量, 训练第二个RBM, 得到其参数, 然后固定这些参数, 训练h2和h3构成的RBM, 具体的训练算法如下:

![](http://images.cnitblog.com/blog/326731/201308/18151442-a849c8aa7ddc4454b832343926f266ff.png)

上图最右边就是最终训练得到的生成模型:

![](http://images.cnitblog.com/blog/326731/201308/18151139-9bff6318a201473e8a79173d1b578671.png)

用公式表示为：

![](http://images.cnitblog.com/blog/326731/201308/18151259-34d5e056b90d4ee3a69865a7a9347c59.png)

3. 利用DBN进行有监督学习

  在使用上述的逐层无监督方法学得节点之间的权重以及节点的偏置之后(亦即初始化), 可以在DBN的最顶层再加一层, 来表示我们希望得到的输出, 然后计算模型得到的输出和希望得到的输出之间的误差, 利用后向反馈的方法来进一步优化之前设置的初始权重。因为我们已经使用逐层无监督方法来初始化了权重值, 使其比较接近最优值, 解决了之前多层神经网络训练时存在的问题, 能够得到很好的效果。
  
  
##### 基于受限波尔兹曼机（RBM） 的协同过滤


 受限玻尔兹曼机是一种生成式随机神经网络(generative stochastic neural network), 详细介绍可见我的博文《受限玻尔兹曼机(RBM)简介》, 本文主要介绍RBM在协同过滤的应用。

###### 1. 受限玻尔兹曼机简单介绍

  传统的受限玻尔兹曼机是一种如下图所示, 其由一些可见单元(visible unit，对应可见变量，亦即数据样本)和一些隐藏单元(hidden unit，对应隐藏变量)构成，可见变量和隐藏变量都是二元变量，亦即其状态取{0,1}。整个网络是一个二部图，只有可见单元和隐藏单元之间才会存在边，可见单元之间以及隐藏单元之间都不会有边连接。
  
  
![](http://images.cnitblog.com/blog/326731/201308/19223512-2d487c10033442248c2fb7b847821d89.png)

将该模型应用到协同过滤需要解决以下两个问题:

  1.  鉴于RBM中的单元都是二元变量, 如果用这些二元变量来对整数值的评分建模?
  2.  用户的打分是非常稀疏的, 亦即用户只会对很少的物品(比如电影)打分, 如何处理这些缺失的评分?

###### 2. 基于RBM的协同过滤

  R. R. Salakhutdinov等人提出了一种使用RBM来进行协同过滤的方法:

  假设有m个电影, 则使用m个softmax单元来作为可见单元来构造RBM.  对于每个用户使用不同的RBM, 这些不同的RBM仅仅是可见单元不同, 因为不同的用户会对不同的电影打分, 所有的这些RBM的可见单元共用相同的偏置以及和隐藏单元的连接权重W. 该方法很好的解决了之前提到的问题:

    使用softmax来对用户的评分进行建模, softmax是一种组合可见单元, 包含k个二元单元, 第i个二元单元当且只当用户对该电影打分为i时才会置为1.
    如果一个用户没有对第j个电影评分, 则该用户的RBM中不存在第j个softmax单元.

 该模型如下图所示:
 
 ![](http://images.cnitblog.com/blog/326731/201308/19225020-39780e9ce01c4f2383ecadc9ea634b2d.png)
 
 可见单元v和隐藏单元h的条件概率为
 ![](http://images.cnitblog.com/blog/326731/201308/19225241-6a13de24dd564f21af7821d47e3347f4.png)
 
模型参数的学习过程非常类似于RBM的DC算法:

![](http://images.cnitblog.com/blog/326731/201308/19225351-7f48c19c415140e5ac5c0a7c4804a98d.png)

训练完成后，计算用户对未平间物品的预测评分的算法为：

![](http://images.cnitblog.com/blog/326731/201308/19225634-5d147617be3f41c29e106e539a89fa56.png)

###### 3. 条件 RBM (Conditional Restricted Boltzmann Machine)

 以上的RBM只用到了用户对电影的评分, 忽视了另外一种非常重要的信息: 用户浏览过哪些电影(但是没打分, 或者打分未知), 条件RBM把这种信息也进行了建模:
 
 ![](http://images.cnitblog.com/blog/326731/201308/19230019-7b9ec7b1b1eb44e9ba8d90f85039a487.png)
 
 其中的r是一个m维的向量, ri为1代表用户对浏览过第i个电影, 加入r后的模型的条件概率为:
 
 ![](http://images.cnitblog.com/blog/326731/201308/19230252-9dbcc6505a484c6aaca1aea76e9a1231.png)
 
权重D的学习过程为:

![](http://images.cnitblog.com/blog/326731/201308/19230358-ef66f0d1fac14ee59ad4c778cc53e3e9.png)











