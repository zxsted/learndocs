概率topic model 建模经验(DP详细说明)



[toc]

#####  Dirichlet Process & Dirichlet Distribution 
[传送](http://blog.csdn.net/xyqzki/article/details/10943805)

 狄利克雷过程（dirichlet process ）是目前变参数学习（non parameter）非常流行的一个理论，很多的工作都是基于这个理论来进行的，如HDP（hierarchical dirichlet process）。


下面我们谈谈dirichlet process的五种角度来理解它。


###### 第一种：原始定义：
假设存在在度量空间Θ上的分布H和一个参数α，如果对于度量空间Θ的任意一个可数划分（可以是有限或者无限的）A1,A2,...,An，都有下列式子成立：


(G(A1),G(A2),...,G(An))∼Dir(αH(A1),αH(A2),...,αH(An)),  这里Dir是dirichlet 分布，


我们称G是满足Dirichlet process的。


这个定义是1973年Ferguson最早提出的定义。在有了这个定义之后，我们怎么去构造一个dirichlet process（DP）出来呢？或者如果我们想从这个DP中抽取出一些样本，怎么抽呢？由于这个原因，我们有了下面三种构造性定义或者解释： 中国餐馆过程（CRP)，polya urn ，stick-breaking。


######第二种： 中国餐馆过程（CRP）

 假设一个中国餐馆有无限的桌子，第一个顾客到来之后坐在第一张桌子上。第二个顾客来到可以选择坐在第一张桌子上，也可以选择坐在一张新的桌子上，假设第n＋1个顾客到来的时候，已经有k张桌子上有顾客了，分别坐了n1,n2,...,nk个顾客，那么第n＋1个顾客可以以概率为ni/(α+n)坐在第i张桌子上，ni为第i张桌子上的顾客数；同时有概率为α/(α+n)选取一张新的桌子坐下。那么在n个顾客坐定之后，很显然CRP把这n个顾客分为了K个堆，即K个clusters，可以证明CRP就是一个DP。


注意这里有一个限制，每张桌子上只能有同一个dish，即一桌人喜欢吃同一道菜。


###### 第三种：Polya urn模型
 假设我们有一个缸，里面没有球，现在我们从一个分布H中选取一种颜色，然后把这种颜色涂在一个球上放入缸中；然后我们要么从缸中抽取一个球出来，然后再放入两个和这个球同种颜色的球进入缸中；要么就从分布H中选取一个颜色，然后把这种颜色涂在一个球上放入缸中。从缸中抽取某种颜色的一个球的概率是ni/(α+n)，ni是这种颜色的球的个数，n是总的球个数；不从缸中抽取而放入一种颜色的球的概率是α/(α+n)。很明显，polya urn模型和CRP有一一对应的关系，颜色对应一个桌子，坐新桌子对应于不从缸中选取而是从H中选取一种颜色涂球放入缸中。


###### 第四种：stick-breaking模型
 假设有一个长度为1的线段，我们从中选取 π1 长出来，剩下的部分再选取 π2 出来，循环下去，πn ，无穷下去，这个有点类似我们古代的一句话：


“一尺之踵，日取其半，万世不竭”，它们满足∑πi=1


对每个 πi ，我们都从分布H中选取一个θi，然后从F(θi)中选取出一个xi出来。这里的 θi 就对应一个cluster，类似地，我们可以看到数据自然地被分为了各个堆，可以证明这个模型仍然是一个DP。




###### 第五种：无限混合模型

 从stick－breaking模型我们看出，我们可以把DP看着是一个无限混合模型，即


G∼∑inf1πi∗F(θi)，其中∑πi＝1。πi 就是混合模型中每个子模型的权重。


目前应用最多的还是从第五种角度来看待问题，即把DP看着是一个无限混合模型，其中值得注意的是：


1）虽然DP是一个无限混合模型，但是可以证明，随着数据的增多，模型的个数是呈现log 增加的，即模型的个数的增长是比数据的增长要缓慢得多的；


2）DP是有一个马太效应在里面的，即越富裕的人越来越富裕，我们可以从第二和第三种解释中看到，每个桌子或者颜色已经有的数据越多，那么下一次被选中的概率越大，因为是与在桌子上的个数成正比的。


DP是一个复杂的随机过程，需要进一步深入理解，下篇将会继续这个话题。



一篇参考
[传送](http://www.cnblogs.com/breezedeus/archive/2012/11/05/2754940.html)




Dirichlet Process (DP)被称为分布的分布。从DP抽取出的每个样本（一个函数）都可以被认为是一个离散随机变量的分布函数，这个随机变量以非零概率值在可数无穷个离散点上取值。比较有意思的是，从DP可以推导出几个非常著名的问题： Chinese Restaurant Process (CRP)、Polya Urn Scheme和Stick-breaking Process。简单的介绍可以见Edwin Chen的博文“Infinite Mixture Models with Nonparametric Bayes and the Dirichlet Process”。

 

DP的特性使得它在非参数贝叶斯聚类模型中可以被用作参数的先验分布。Dirichlet Process Mixture (DPM)是这种非参数贝叶斯聚类模型中的一个典型代表。DPM可以认为是有限混合（Finite Mixture，FM）模型的一个推广，FM（如Gaussian Mixture模型）必须首先给定类数，而DPM则不需要，它可以依据数据自行判断类数。理论上来说，DPM的类数随着log(样本点数量)的增长速度增长。目前研究者已经提出了很多训练DPM的算法，从Gibbs Sampling，到Collapsed Gibbs Sampling，到Variational方法。我自己实现了Collapsed Gibbs Sampling方法，速度是个很大的约束，跑大数据很费劲。DPM的一个另一个问题是它的类数由算法自动控制（虽然有个超参数alpha可以大致上调节类数），最终产生的类数可能与期望的差别很大。

 

想进一步了解DP和DPM的同学，可以去Yee W. Teh的主页上看看，里面可以找到很多相关的papers，slides，presentations，以及用Matlab写的DPM开源软件。想仔细了解DPM的各个算法及具体推导，建议看看Xiaodong Yu的博文，里面也有他总结的一个很详细的学习笔记（虽然里面有一些小笔误），以及更多的参考资料。我自己也写了一份总结，但是懒得用Latex打出来了，就以图片打包的方式放在网盘里了，只把最后一页的参考文献贴下面。那些参考文献可以直接Google后下载。对理论没有兴趣的同学请忽略吧，哈哈。


##### 概率模型与计算机视觉
林达华
美国麻省理工学院（MIT）博士

上世纪60年代, Marvin Minsky 在MIT让他的本科学生 Gerald Jay Sussman用一个暑假的时间完成一个有趣的Project : “link a camera to a computer and get the computer to describe what it saw”。从那时开始，特别是David Marr教授于1977年正式提出视觉计算理论，计算机视觉已经走过了四十多年的历史。可是，从今天看来，这个已入不惑之年的学科，依然显得如此年轻而朝气蓬勃。

在它几十年的发展历程中，多种流派的方法都曾各领风骚于一时。最近二十年中，计算机视觉发展最鲜明的特征就是机器学习与概率模型的广泛应用。在这里，我简单回顾一下对这个领域产生了重要影响的几个里程碑：
● 1984年：Stuart Geman和Donald Geman发表了一篇先驱性的论文：Stochastic Relaxation, Gibbs Distributions, and the Bayesian Restoration of Images. 在这篇文章里，两位Geman先生引入了一系列对计算机视觉以后的发展具有深远影响的概念和方法：Markov Random Field (MRF), Gibbs Sampling，以及Maximum a Posteriori estimate (MAP estimate)。这篇论文的意义是超前于时代的，它所建立的这一系列方法直到90年代中后期才开始被广泛关注。
● 1991年：Matthew Turk和Alex Pentland使用Eigenface进行人脸分类。从此，以矩阵的代数分解为基础的方法在视觉分析中被大量运用。其中有代表性的方法包括PCA, LDA，以及ICA。
● 1995年：Corinna Cortes和Vladimir Vapnik提出带有soft margin的Support Vector Machine (SVM)以及它的Kernel版本，并用它对手写数字进行分类。从此，SVM大受欢迎，并成为各种应用中的基准分类器。
● 1996年：Bruno Olshausen 和David Field 提出使用Overcomplete basis对图像进行稀疏编码(Sparse coding)。这个方向在初期的反响并不热烈。直到近些年，Compressed Sensing在信号处理领域成为炙手可热的方向。Sparse coding 在这一热潮的带动下，成为视觉领域一个活跃的研究方向。
● 90年代末：Graphical Model和Variational Inference逐步发展成熟。1998年，MIT出版社出版了由Michale Jordan主编的文集：Learning in Graphical Models。 这部书总结了那一时期关于Graphical Model的建模，分析和推断的主要成果——这些成果为Graphical Model在人工智能的各个领域的应用提供了方法论基础。进入21世纪，Graphical Model和Bayesian方法在视觉研究中的运用出现了井喷式的增长。
● 2001年：John Lafferty和Andrew McCallum等提出Conditional Random Field (CRF)。CRF为结构化的分类和预测提供了一种通用的工具。此后，语义结构开始被运用于视觉场景分析。
● 2003年：David Blei等提出Latent Dirichlet Allocation。2004年：Yee Whye Teh 等提出Hierarchical Dirichlet Process。各种参数化或者非参数化的Topic Model在此后不久被广泛用于语义层面的场景分析。
● 虽然Yahn Lecun等人在1993年已提出Convolutional Neural Network，但在vision中的应用效果一直欠佳。时至2006年，Geoffrey Hinton等人提出Deep Belief Network进行layer-wise的pretraining，应用效果取得突破性进展，其与之后Ruslan Salakhutdinov提出的Deep Boltzmann Machine重新点燃了视觉领域对于Neural Network和Boltzmann Machine的热情。

时间进入2013年，Probabilistic Graphical Model早已成为视觉领域中一种基本的建模工具。Probabilistic Graphical Model的研究涉及非常多的方面。 限于篇幅，在本文中，我只能简要介绍其中几个重要的方面，希望能为大家提供一些有用的参考。

Graphical Model的基本类型
基本的Graphical Model 可以大致分为两个类别：贝叶斯网络(Bayesian Network)和马尔可夫随机场(Markov Random Field)。它们的主要区别在于采用不同类型的图来表达变量之间的关系：贝叶斯网络采用有向无环图(Directed Acyclic Graph)来表达因果关系，马尔可夫随机场则采用无向图(Undirected Graph)来表达变量间的相互作用。这种结构上的区别导致了它们在建模和推断方面的一系列微妙的差异。一般来说，贝叶斯网络中每一个节点都对应于一个先验概率分布或者条件概率分布，因此整体的联合分布可以直接分解为所有单个节点所对应的分布的乘积。而对于马尔可夫场，由于变量之间没有明确的因果关系，它的联合概率分布通常会表达为一系列势函数（potential function）的乘积。通常情况下，这些乘积的积分并不等于1，因此，还要对其进行归一化才能形成一个有效的概率分布——这一点往往在实际应用中给参数估计造成非常大的困难。

值得一提的是，贝叶斯网络和马尔可夫随机场的分类主要是为了研究和学习的便利。在实际应用中所使用的模型在很多时候是它们的某种形式的结合。比如，一个马尔可夫随机场可以作为整体成为一个更大的贝叶斯网络的节点，又或者，多个贝叶斯网络可以通过马尔可夫随机场联系起来。这种混合型的模型提供了更丰富的表达结构，同时也会给模型的推断和估计带来新的挑战。

Graphical Model的新发展方向

在传统的Graphical Model的应用中，模型的设计者需要在设计阶段就固定整个模型的结构，比如它要使用哪些节点，它们相互之间如何关联等等。但是，在实际问题中，选择合适的模型结构往往是非常困难的——因为，我们在很多时候其实并不清楚数据的实际结构。为了解决这个问题，人们开始探索一种新的建立概率模型的方式——结构学习。在这种方法中，模型的结构在设计的阶段并不完全固定。设计者通常只需要设定模型结构所需要遵循的约束，然后再从模型学习的过程中同时推断出模型的实际结构。

结构学习直到今天仍然是机器学习中一个极具挑战性的方向。结构学习并没有固定的形式，不同的研究者往往会采取不同的途径。比如，结构学习中一个非常重要的问题，就是如何去发现变量之间的内部关联。对于这个问题，人们提出了多种截然不同的方法：比如，你可以先建立一个完全图连接所有的变量，然后选择一个子图来描述它们的实际结构，又或者，你可以引入潜在节点(latent node)来建立变量之间的关联。

Probabilistic Graphical Model的另外一个重要的发展方向是非参数化。与传统的参数化方法不同，非参数化方法是一种更为灵活的建模方式——非参数化模型的大小（比如节点的数量）可以随着数据的变化而变化。一个典型的非参数化模型就是基于狄利克莱过程(Dirichlet Process)的混合模型。这种模型引入狄利克莱过程作为部件(component)参数的先验分布，从而允许混合体中可以有任意多个部件。这从根本上克服了传统的有限混合模型中的一个难题，就是确定部件的数量。在近几年的文章中，非参数化模型开始被用于特征学习。在这方面，比较有代表性的工作就是基于Hierarchical Beta Process来学习不定数量的特征。

基于Graphical Model 的统计推断 (Inference)
完成模型的设计之后，下一步就是通过一定的算法从数据中去估计模型的参数，或推断我们感兴趣的其它未知变量的值。在贝叶斯方法中，模型的参数也通常被视为变量，它们和普通的变量并没有根本的区别。因此，参数估计也可以被视为是统计推断的一种特例。

除了最简单的一些模型，统计推断在计算上是非常困难的。一般而言，确切推断(exact inference)的复杂度取决于模型的tree width。对于很多实际模型，这个复杂度可能随着问题规模增长而指数增长。于是，人们退而求其次，转而探索具有多项式复杂度的近似推断(approximate inference)方法。

主流的近似推断方法有三种：

(1)基于平均场逼近(mean field approximation)的variational inference。这种方法通常用于由Exponential family distribution所组成的贝叶斯网络。其基本思想就是引入一个computationally tractable的upper bound逼近原模型的log partition function，从而有效地降低了优化的复杂度。大家所熟悉的EM算法就属于这类型算法的一种特例。

(2)Belief propagation。这种方法最初由Judea Pearl提出用于树状结构的统计推断。后来人们直接把这种算法用于带环的模型（忽略掉它本来对树状结构的要求）——在很多情况下仍然取得不错的实际效果，这就是loop belief propagation。在进一步的探索的过程中，人们发现了它与Bethe approximation的关系，并由此逐步建立起了对loopy belief propagation的理论解释，以及刻画出它在各种设定下的收敛条件。值得一提的是，由于Judea Pearl对人工智能和因果关系推断方法上的根本性贡献，他在2011年获得了计算机科学领域的最高奖——图灵奖。

基于message passing的方法在最近十年有很多新的发展。Martin Wainwright在2003年提出Tree-reweighted message passing，这种方法采用mixture of trees来逼近任意的graphical model，并利用mixture coefficient和edge probability之间的对偶关系建立了一种新的message passing的方法。这种方法是对belief propagation的推广。
Jason Johnson等人在2005年建立的walk sum analysis为高斯马尔可夫随机场上的belief propagation提供了系统的分析方法。这种方法成功刻画了belief propagation在高斯场上的收敛条件，也是后来提出的多种改进型的belief propagation的理论依据。Thomas Minka在他PhD期间所建立的expectation propagation也是belief propagation的在一般Graphical Model上的重要推广。

(3)蒙特卡罗采样(Monte Carlo sampling)。与基于优化的方法不同，蒙特卡罗方法通过对概率模型的随机模拟运行来收集样本，然后通过收集到的样本来估计变量的统计特性（比如，均值）。采样方法有三个方面的重要优点。第一，它提供了一种有严谨数学基础的方法来逼近概率计算中经常出现的积分（积分计算的复杂度随着空间维度的提高呈几何增长）。第二，采样过程最终获得的是整个联合分布的样本集，而不仅仅是对某些参数或者变量值的最优估计。这个样本集近似地提供了对整个分布的更全面的刻画。比如，你可以计算任意两个变量的相关系数。第三，它的渐近特性通常可以被严格证明。对于复杂的模型，由variational inference或者belief propagation所获得的解一般并不能保证是对问题的全局最优解。在大部分情况下，甚至无法了解它和最优解的距离有多远。如果使用采样，只要时间足够长，是可以任意逼近真实的分布的。而且采样过程的复杂度往往较为容易获得理论上的保证。

蒙特卡罗方法本身也是现代统计学中一个非常重要的分支。对它的研究在过去几十年来一直非常活跃。在机器学习领域中，常见的采样方法包括Gibbs Sampling, Metropolis-Hasting Sampling (M-H), Importance Sampling, Slice Sampling, 以及Hamiltonian Monte Carlo。其中，Gibbs Sampling由于可以纳入M-H方法中解释而通常被视为M-H的特例——虽然它们最初的motivation是不一样的。

Graphical Model以及与它相关的probabilistic inference是一个非常博大的领域，远非本文所能涵盖。在这篇文章中，我只能蜻蜓点水般地介绍了其中一些我较为熟悉的方面，希望能给在这方面有兴趣的朋友一点参考。

























