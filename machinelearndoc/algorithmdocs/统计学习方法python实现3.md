
###统计学习方法python实现3
(2013-10-14 16:36:42)
[转载](http://blog.sina.com.cn/s/blog_6b60259a0101eol6.html)
[toc]
####第五章 逻辑斯蒂回归和最大熵模型

```python
########################################################
#5.1 逻辑斯蒂回归实现
#原理：构造分类模型使得当前的数据集出现的可能性最大-》最大似然法
########################################################
def logit_regression():
    import pandas as pd# pandas 的教程：http://blog.csdn.net/overstack/article/details/9001565
    import statsmodels.api as sm
    import pylab as pl
    import numpy as np

    #步骤1:read the data in
    df = pd.read_csv("http://www.ats.ucla.edu/stat/data/binary.csv")

    # take a look at the dataset
    print (df.head())#最上面的几列，也有df.tail(3)
    #    admit  gre   gpa  rank
    # 0      0  380  3.61     3
    # 1      1  660  3.67     3
    # 2      1  800  4.00     1
    # 3      1  640  3.19     4
    # 4      0  520  2.93     4

    # rename the 'rank' column because there is also a DataFrame method called 'rank'
    df.columns = ["admit", "gre", "gpa", "prestige"]
    print (df.columns)
    # array([admit, gre, gpa, prestige], dtype=object)


    ##步骤2:得到数据的基本统计信息
    # summarize the data
    print (df.describe())
#             admit         gre         gpa   prestige
# count  400.000000  400.000000  400.000000  400.00000
# mean     0.317500  587.700000    3.389900    2.48500
# std      0.466087  115.516536    0.380567    0.94446
# min      0.000000  220.000000    2.260000    1.00000
# 25%      0.000000  520.000000    3.130000    2.00000
# 50%      0.000000  580.000000    3.395000    2.00000
# 75%      1.000000  660.000000    3.670000    3.00000
# max      1.000000  800.000000    4.000000    4.00000

    # take a look at the standard deviation of each column
    print (df.std())
# admit      0.466087
# gre      115.516536
# gpa        0.380567
# prestige   0.944460

    # frequency table cutting presitge and whether or not someone was admitted
    print (pd.crosstab(df['admit'], df['prestige'], rownames=['admit']))
# prestige   1   2   3   4
# admit
# 0         28  97  93  55
# 1         33  54  28  12

# plot all of the columns
    df.hist()
    pl.show()




    #步骤3:dummify rank #就是把多元变量变成二元变量
    dummy_ranks = pd.get_dummies(df['prestige'], prefix='prestige')
    print (dummy_ranks.head())
#    prestige_1  prestige_2  prestige_3  prestige_4
# 0           0           0           1           0
# 1           0           0           1           0
# 2           1           0           0           0
# 3           0           0           0           1
# 4           0           0           0           1

    # create a clean data frame for the regression
    cols_to_keep = ['admit', 'gre', 'gpa']
    data = df[cols_to_keep].join(dummy_ranks.ix[:, 'prestige_2':])
    print (data.head())#为什么不用prestige_1? prevent multicollinearity, or the dummy variable trap
#    admit  gre   gpa  prestige_2  prestige_3  prestige_4
# 0      0  380  3.61           0           1           0
# 1      1  660  3.67           0           1           0
# 2      1  800  4.00           0           0           0
# 3      1  640  3.19           0           0           1
# 4      0  520  2.93           0           0           1

# manually add the intercept
    data['intercept'] = 1.0


    #步骤4.Performing the regression
    train_cols = data.columns[1:]
    # Index([gre, gpa, prestige_2, prestige_3, prestige_4], dtype=object)
    logit = sm.Logit(data['admit'], data[train_cols])
    # fit the model
    result = logit.fit()



    #步骤5. Interpreting the result
    # cool enough to deserve it's own gist
    print (result.summary())

    # look at the confidence interval of each coeffecient
    print (result.conf_int())
    #                    0         1
    # gre         0.000120  0.004409
    # gpa         0.153684  1.454391
    # prestige_2 -1.295751 -0.055135
    # prestige_3 -2.016992 -0.663416
    # prestige_4 -2.370399 -0.732529
    # intercept  -6.224242 -1.755716


    #odds ratio
    #Take the exponential of each of the coeffecients to generate the odds ratios.
    #  This tells you how a 1 unit increase or decrease in a variable affects the odds of being admitted.
    # odds ratios only
    print (np.exp(result.params))
    # gre           1.002267
    # gpa           2.234545
    # prestige_2    0.508931
    # prestige_3    0.261792
    # prestige_4    0.211938
    # intercept     0.018500

    #预测
    data['admit_pred']=result.predict(data[train_cols])
    print (data.head())

    #更多http://blog.yhathq.com/posts/logistic-regression-and-python.html
```

####最大熵模型
```python
########################################################
#5.2 最大熵模型
#原理：同样是求得模型，使得模型的熵最大，也用最大似然法
#最大熵采取的原则就是：保留全部的不确定性，将风险降到最小
#迭代优化技术
########################################################
#注：（来自python 自然语言处理）一些迭代优化技术比别的快得多。当训练最大熵模型时，应避免使用广义
#迭代缩放（Generalized Iterative Scaling ，GIS）或改进的迭代缩放（Improv
#ed Iterative Scaling ，IIS ） ， 这两者都比共轭梯度 （Conjugate Gradient， CG）
#和 BFGS 优化方法慢很多。

#朴素贝叶斯分类器和最大熵分类器之间的一个重要差异是它们可以被用来回答问题的
#类型。朴素贝叶斯分类器是一个生成式分类器的例子，建立一个模型，预测 P(input, label)，
#即(input, label)对的联合概率。因此，生成式模型可以用来回答下列问题：
#1. 一个给定输入的最可能的标签是什么？
#2. 对于一个给定输入，一个给定标签有多大可能性？
#3. 最有可能的输入值是什么？
#4. 一个给定输入值的可能性有多大？
#5. 一个给定输入具有一个给定标签的可能性有多大？
#6. 对于一个可能有两个值中的一个值 （但我们不知道是哪个） 的输入， 最可能的标签
#是什么？
#另一方面，最大熵分类器是条件式分类器的一个例子。条件式分类器建立模型预测 P(l
#abel|input)——一个给定输入值的标签的概率。 因此， 条件式模型仍然可以被用来回答问题 1
#和 2。然而，条件式模型不能用来回答剩下的问题 3-6。
#一般情况下，生成式模型确实比条件式模型强大，因为我们可以从联合概率 P(input, la
#bel)计算出条件概率 P(label|input)，但反过来不行。然而，这种额外的能力是要付出代价的。
#由于该模型更强大的，它也有更多的“自由参数”需要学习的。而训练集的大小是固定的。
#因此， 使用一个更强大的模型时， 我们可用来训练每个参数的值的数据也更少， 使其难以找
#到最佳参数值。 结果是一个生成式模型回答问题 1 和 2 可能不会与条件式模型一样好， 因为
#条件式模型可以集中精力在这两个问题上。然而，如果我们确实需要像 3-6 问题的答案， 那
#么我们别无选择，只能使用生成式模型。
#生成式模型与条件式模型之间的差别类似与一张地形图和一张地平线的图片之间的区
#别。 虽然地形图可用于回答问题的更广泛， 制作一张精确的地形图也明显比制作一张精确的
#地平线图片更加困难。


def max_Entropy():
    #这里直接使用nltk中的最大熵训练器
    import nltk
    #1.提取特征,使用一个字典，这个字典称作特征集
    #1.1 特征提取器1
    def gender_feature(word):
        return {'last_letter':word[-1]}

    #1.2 特征提取器2
    def gender_feature2(word):
        return {'suffix1': word[-1:],'suffix2': word[-2:]}

    #1.3 特征提取器3  一个过拟合的特征提取器
    def gender_feature3(name):
        feature={}
        feature['firstletter']=name[0].lower()
        feature['lastletter']=name[-1].lower()
        for letter in 'abcdefghijklmnopqrstuvwxyz':
        # for letter in name:
            feature['count(%s)'%letter]=name.lower().count(letter)
            feature['has(%s)'%letter]=(letter in name.lower())
        return feature

    #2.生成训练集和测试集(包括预处理)
    from nltk.corpus import names
    import random
    _names=[(name,'male') for name in names.words('male.txt')]+\
            [(name,'female') for name in names.words('female.txt')]
    random.shuffle(_names)
    #分类器的输入的特征是 [(特征，组别)```]
    featureset=[(gender_feature(name),g) for (name,g) in _names]
    train_set,test_set=featureset[500:],featureset[:500]

    #另一种变化的做法是 数据集分成三部分 开发训练集，开发测试集，测试集
    #开发测试集用于检测，查看错误



    #3.使用最大熵分类器训练数据
    #classifier=nltk.NaiveBayesClassifier.train(train_set)
    classifier=nltk.MaxentClassifier.train(train_set)#对这个数据集，比朴素贝叶斯的准确率高点

    #4.使用，或者用测试集评估质量
    print(classifier.classify(gender_feature('Neo')))
    print(nltk.classify.accuracy(classifier,test_set))
    #我们可以检查分类器，确定哪些特征对于区分名字的性别是最有效的。
    classifier.show_most_informative_features(5)

```

```python
########################################################
#5.2 最大熵模型
#原理：同样是求得模型，使得模型的熵最大，也用最大似然法
#最大熵采取的原则就是：保留全部的不确定性，将风险降到最小
#迭代优化技术
########################################################
#注：（来自python 自然语言处理）一些迭代优化技术比别的快得多。当训练最大熵模型时，应避免使用广义
#迭代缩放（Generalized Iterative Scaling ，GIS）或改进的迭代缩放（Improv
#ed Iterative Scaling ，IIS ） ， 这两者都比共轭梯度 （Conjugate Gradient， CG）
#和 BFGS 优化方法慢很多。

#朴素贝叶斯分类器和最大熵分类器之间的一个重要差异是它们可以被用来回答问题的
#类型。朴素贝叶斯分类器是一个生成式分类器的例子，建立一个模型，预测 P(input, label)，
#即(input, label)对的联合概率。因此，生成式模型可以用来回答下列问题：
#1. 一个给定输入的最可能的标签是什么？
#2. 对于一个给定输入，一个给定标签有多大可能性？
#3. 最有可能的输入值是什么？
#4. 一个给定输入值的可能性有多大？
#5. 一个给定输入具有一个给定标签的可能性有多大？
#6. 对于一个可能有两个值中的一个值 （但我们不知道是哪个） 的输入， 最可能的标签
#是什么？
#另一方面，最大熵分类器是条件式分类器的一个例子。条件式分类器建立模型预测 P(l
#abel|input)——一个给定输入值的标签的概率。 因此， 条件式模型仍然可以被用来回答问题 1
#和 2。然而，条件式模型不能用来回答剩下的问题 3-6。
#一般情况下，生成式模型确实比条件式模型强大，因为我们可以从联合概率 P(input, la
#bel)计算出条件概率 P(label|input)，但反过来不行。然而，这种额外的能力是要付出代价的。
#由于该模型更强大的，它也有更多的“自由参数”需要学习的。而训练集的大小是固定的。
#因此， 使用一个更强大的模型时， 我们可用来训练每个参数的值的数据也更少， 使其难以找
#到最佳参数值。 结果是一个生成式模型回答问题 1 和 2 可能不会与条件式模型一样好， 因为
#条件式模型可以集中精力在这两个问题上。然而，如果我们确实需要像 3-6 问题的答案， 那
#么我们别无选择，只能使用生成式模型。
#生成式模型与条件式模型之间的差别类似与一张地形图和一张地平线的图片之间的区
#别。 虽然地形图可用于回答问题的更广泛， 制作一张精确的地形图也明显比制作一张精确的
#地平线图片更加困难。


def max_Entropy():
    #这里直接使用nltk中的最大熵训练器
    import nltk
    #1.提取特征,使用一个字典，这个字典称作特征集
    #1.1 特征提取器1
    def gender_feature(word):
        return {'last_letter':word[-1]}

    #1.2 特征提取器2
    def gender_feature2(word):
        return {'suffix1': word[-1:],'suffix2': word[-2:]}

    #1.3 特征提取器3  一个过拟合的特征提取器
    def gender_feature3(name):
        feature={}
        feature['firstletter']=name[0].lower()
        feature['lastletter']=name[-1].lower()
        for letter in 'abcdefghijklmnopqrstuvwxyz':
        # for letter in name:
            feature['count(%s)'%letter]=name.lower().count(letter)
            feature['has(%s)'%letter]=(letter in name.lower())
        return feature

    #2.生成训练集和测试集(包括预处理)
    from nltk.corpus import names
    import random
    _names=[(name,'male') for name in names.words('male.txt')]+\
            [(name,'female') for name in names.words('female.txt')]
    random.shuffle(_names)
    #分类器的输入的特征是 [(特征，组别)```]
    featureset=[(gender_feature(name),g) for (name,g) in _names]
    train_set,test_set=featureset[500:],featureset[:500]

    #另一种变化的做法是 数据集分成三部分 开发训练集，开发测试集，测试集
    #开发测试集用于检测，查看错误



    #3.使用最大熵分类器训练数据
    #classifier=nltk.NaiveBayesClassifier.train(train_set)
    classifier=nltk.MaxentClassifier.train(train_set)#对这个数据集，比朴素贝叶斯的准确率高点

    #4.使用，或者用测试集评估质量
    print(classifier.classify(gender_feature('Neo')))
    print(nltk.classify.accuracy(classifier,test_set))
    #我们可以检查分类器，确定哪些特征对于区分名字的性别是最有效的。
    classifier.show_most_informative_features(5)
```







95行代码实现最大熵模型训练
分类： 机器学习 2014-04-29 14:05 674人阅读 评论(2) 收藏 举报
机器学习最大熵NLP

关于最大熵模型的介绍请看：http://www.cnblogs.com/hexinuaa/p/3353479.html

下面是GIS训练算法的python实现，代码不到100行。

```python
from collections import defaultdict

import math


class MaxEnt(object):

    def __init__(self):

        self.feats = defaultdict(int)

        self.trainset = []

        self.labels = set()  

      

    def load_data(self,file):

        for line in open(file):

            fields = line.strip().split()

            # at least two columns

            if len(fields) < 2: continue

            # the first column is label

            label = fields[0]

            self.labels.add(label)

            for f in set(fields[1:]):

                # (label,f) tuple is feature 

                self.feats[(label,f)] += 1

            self.trainset.append(fields)

            

    def _initparams(self):

        self.size = len(self.trainset)

        # M param for GIS training algorithm

        self.M = max([len(record)-1 for record in self.trainset])

        self.ep_ = [0.0]*len(self.feats)

        for i,f in enumerate(self.feats):

            # calculate feature expectation on empirical distribution

            self.ep_[i] = float(self.feats[f])/float(self.size)

            # each feature function correspond to id

            self.feats[f] = i

        # init weight for each feature

        self.w = [0.0]*len(self.feats)

        self.lastw = self.w

        

    def probwgt(self,features,label):

        wgt = 0.0

        for f in features:

            if (label,f) in self.feats:

                wgt += self.w[self.feats[(label,f)]]

        return math.exp(wgt)

            

    """

    calculate feature expectation on model distribution

    """        

    def Ep(self):

        ep = [0.0]*len(self.feats)

        for record in self.trainset:

            features = record[1:]

            # calculate p(y|x)

            prob = self.calprob(features)

            for f in features:

                for w,l in prob:

                    # only focus on features from training data.

                    if (l,f) in self.feats:

                        # get feature id

                        idx = self.feats[(l,f)]

                        # sum(1/N * f(y,x)*p(y|x)), p(x) = 1/N

                        ep[idx] += w * (1.0/self.size)

        return ep

    

    def _convergence(self,lastw,w):

        for w1,w2 in zip(lastw,w):

            if abs(w1-w2) >= 0.01:

                return False

        return True

                

    def train(self, max_iter =1000):

        self._initparams()

        for i in range(max_iter):

            print 'iter %d ...'%(i+1)

            # calculate feature expectation on model distribution

            self.ep = self.Ep()           

            self.lastw = self.w[:]  

            for i,win enumerate(self.w):

                delta = 1.0/self.M * math.log(self.ep_[i]/self.ep[i])

                # update w

                self.w[i] += delta

            print self.w

            # test if the algorithm is convergence

            if self._convergence(self.lastw,self.w):

                break

    

    def calprob(self,features):

        wgts = [(self.probwgt(features, l),l) for l in self.labels]

        Z = sum([ w for w,l in wgts])

        prob = [ (w/Z,l) for w,l in wgts]

        return prob 

            

    def predict(self,input):

        features = input.strip().split()

        prob = self.calprob(features)

        prob.sort(reverse=True)

        return prob   
```