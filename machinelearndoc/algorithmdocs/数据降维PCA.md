###PCA数据降维


####1.为什么要降维
李航老师在博客[《机器学习新动向：从人机交互》][1]中提到，学习精度越高，学习信度越高，学习模型越复杂，所需的样本也就越多。样本复杂度满足以下不等式：
$$
|{\rm S}| \geq \frac{1}{2\epsilon^{2}} (\ln \cdot | {\it h}| + \ln \frac{2}{\delta})\, for \,all \, {\it h} \in {\rm C}  
$$
由此可见，feature过多会造成模型复杂，训练速度过慢，因此引入降维。
* about isualization：
多维数据很难进行客户四化分析，因此我们需要降维

PCA的目的： 找到一个低维的平面将数据投射到其上，使得两点之间的拓朴（orthogonal）距离最小。

PCA 与Linear Regression 的区别：
* PCA衡量的是拓朴距离，linear regression是所有x点对应的真实值 y=g(x)与估计值之间的ertitical distance距离
* more general 的解释： PCA是寻找一个surface，将特征向量投影到其上，使得投影后各个特征点的拓扑距离最大（跟y没有关系，是寻找最能表现这些feature的一个平面）；而Linear Regression 是给出特征向量，根据x预测y，所以要进行回归

####2.PCA的流程
 1. 计算各个feature的平均值，计算$\mu j ;$ ($x_j^{(i)}$ 表示第i个样本的第j为特征的value)
   $$
 \mu j =\frac{\sum_m x_j ^{(i)}}{m} 
  $$
 2. 将每一个feature scaling： 将在不同的scale上的feature进行归一化；
 3. 将特征进行 mean normalization
 $$
 令 x_j^{(i)} =\frac {(x_j^{(i)} - \mu_j)}{s_j}
 $$
----------------------PCA算法选取k个主分量------------------------
 4. 求n*n的协方差举证$\sum$:
 $$
 \sum_{n \times n } = \frac{1}{m} \sum_{i=1}^{m} x^{(i)}(x^{(i)}) ^T
 $$
 5. 根据SVD求取特征值和特征向量：
 $$
 \sum = USV`
 $$
 $$
 \sum \sum` = USV` *VS`U` = U （\sum \sum`）U`
 $$
 $$
 \sum`\sum = VS`U` * USV` = V(\sum` \sum)v`
 $$
$$
i.e. U是、\sum \sum`的特征向量矩阵，V是\sum`\sum 的特征向量矩阵，都是n*n的矩阵 
$$
我们的目的是，从n为到k维，就是选取n个特征中最重要的k个，也就是选取出特征值最大的k个

 6. 按照特征值从大到小排列，重新组织U
 7. 选择k个分量 ：我们得到了一个n*n的矩阵$\sum$和U，这是从U中选取k个最重要的分量，即选取前k个特征向量，记为$ U_{reduce}$。该矩阵大小为 $n \times k$
对于一个n维特征向量x，就可以降维到k维向量z了
$$
Z_{k \cdot 1}^{(i)} = U_{k \cdot n}^{T} \cdot x_{n \cdot 1}^{(i)}
$$


####3.应用PCA降维的建议：
* PCA可以降维，而前面说过拟合问题是由纬度过高或者参数过多造成的，那么可以用PCA解决overfitting问题么
Ans：NO！应用PCA提取主成份可能会解决一些overfitting问题，但是部将以用这种方法，还是建议用第三章讲过的regularization(也称为 ridge regresion)来解决

* PCA 中主成份分析应用到那部分数据呢？
 Ans:Only Training Data！ 可以用Cross-Validation 和test Data进行检验，但是选择主分量的时候只能应用training data
 
* 不要盲目应用PCA
 notic：only当你在原数据上跑了一个比较好的结果，又嫌它太慢的时候才采取PCA进行降维，不然降了半天白降了。



[1]:http://blog.sina.com.cn/s/blog_7ad48fee01016d25.html