###Hybrid Monte-Carlo Sampling
[地址](http://deeplearning.net/tutorial/hmc.html#hmc)
能量模型的最大似然学习需要一个鲁棒的算法来采样受限的波尔兹曼集的（rbm）负性粒子，训练RBM时使用CD或PCD，这两种方法使用典型的Gibbs采样，它使用 条件分布 P(h|v)和p(v|h)作为马尔可夫链的转移操作。

但是在某些情况下，条件分布是很难采样到的（例如在“mean-covariance RBM” 需要十分昂贵的矩阵inversions，）,并且，即使Gibbs 采样可以高效的运行，然而它通过一个随机游走操作，它可能不会有效的从一些分布中进行统计。在这种情况下，当需要采样连续的变量是，Hybrid Monte Carlo (HMC) 能够在处理过程中隐形的避免处理条件分布。

在HMC，模型的采样是通过一个模拟的物理系统，该系统中 粒子在一个高维场景下运动，同时受限于隐形能量和显性能量。根据标准的定义，粒子使用两个向量进行描述： 一个位置向量 $s \in \it R^D$和一个速率（velocity）向量 $\phi \in \it R^D $.粒子的联合状态被描述为： $\chi = (s,\phi)$,Hamiltonian 被定义为 隐形能量E(s)和运动能量K($\phi$)的和,如下：
$$ H(s,\phi) = E(s) + K(\phi) = E(s) + \frac{1}{2} \sum{\phi_i^2}$$

为了替代直接采样P（s），HMC直接从联合概率分布中进行采样$p(s,\phi) = \frac{1}{z}exp(-H(s,\phi)) = p(s)p(\phi)$.
因为两个变量是独立的，
通过边际化变量$\phi$是微不足道的 并且可以覆盖原来的分布

哈密尔顿动力学系统(Hamiltonian Dynamic)

状态s和速率$\phi$被修改，$H（s,\phi）$在模拟中是定值，微分方程(differential equations) 如下给出：
$$ \frac{ds_i}{dt} = \frac{dH}{d\phi} = \phi_i   \ \ \ \ \ \     （1）$$
$$ \frac{\phi_i}{dt} = - \frac{H}{ds_i} = - \frac{dE}{ds_i}  $$

就像上面所述的，上面的变换是保留容积和可逆的，上面的动态模型可以被用来作为一个马尔科夫链的转移操作，并且保证P（s,\phi）不变，然而这个链本身不是各态互异的（ergodic），因为仿真的运动保持一个不变(fixed)的Hamilton H(s,\phi)，hmc使用Gibbs 采样来速率替换hamiltonial的动态步骤，因为P（s）和P（\phi）是独立的，采样$\phi_new ~ P(\phi|s)$是很容易的，因为 $P（\phi | s） = p(\phi)$, $P(\phi)$ 从单变量高斯分布中获取。

The Leap-Frog Aloritm
在实际操作中，因为时间离散化的原因，我们不能准确的仿真Hamiltonian动态过程。有多种方法可以做到，为了保持马尔可夫链的不变性，（没翻译完）
leap-frog 算法保持上面的特性，并且进行以下三步的操作：
 $$\phi(t + \frac{\epsilon}{2} = \phi(t) - \frac{\epsilon}{2}\frac{d}{dsi} E(s(t)))$$
 $$s_i(t+\epsilon) = s_i(t) + \epsilon\phi_i(t + \frac{\epsilon}{2})$$
 $$\phi_i(t + \epsilon) = \phi_i(t + \epsilon/2) - \frac{\epsilon}{2} \frac{d}{ds_i}E(s(t + \epsilon))$$
我们可以执行速率的半步更新在时间t + $\frac{\epsilon}{2}$,它被用来计算$s(t+\epsilon)$ 和$\phi(t + \epsilon)$

接受/拒绝
在实际操作中，使用有限的(finite) stepsizes $\epsilon$将不会保持H（s,$\phi$），在仿真中会引入偏差。并且 使用float point numbers 表明上面的转换将不会有良好的可逆性

HMC通过添加一个Metropolis accept/reject 步骤，经过 n 个 leapfrog 步骤，新的状态$\chi^` = (s^`,\phi^`) $在以下条件下被接受：
$$Pacc(\chi,\chi^`) = min(1,frac{exp(-H(S^`,\phi^`))}{exp(-H(s,\phi))})$$

HMC Algorithm
在本算法中获取一个性的HMC采样通过以下步骤：
 1. sample a new velocity from a univariate Gaussian distribution
 2. perform n leapfrog steps to obtain the new state $\chi^`$
 3. perform accept/reject move of $\chi^`$
 
 























