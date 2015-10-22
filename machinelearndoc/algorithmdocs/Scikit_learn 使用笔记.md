Scikit_learn 使用笔记

[toc]

##### sign prediction

经Edwin Chen的推荐，认识了scikit-learn这个非常强大的python机器学习工具包。这个帖子作为笔记。（其实都没有笔记的意义，因为他家文档做的太好了，不过还是为自己记记吧，为以后节省若干分钟）。如果有幸此文被想用scikit-learn的你看见，也还是非常希望你去它们的主页看文档。主页中最值得关注的几个部分：User Guide几乎是machine learning的索引，各种方法如何使用都有，Reference是各个类的用法索引。

###### s1. 导入数据

大多数数据的格式都是M个N维向量，分为训练集和测试集。所以，知道如何导入向量（矩阵）数据是最为关键的一点。这里要用到numpy来协助。假设数据格式是：

```shell
Stock prices    indicator1    indicator2

2.0             123           1252

1.0             ..            ..
```

导入代码：
```python
import numpy as np

f = open("filename.txt")

f.readline()  # 跳过头部

data = np.loadtxt(f)

X = data[:,1:]
Y = data[:,0]


```

libsvm格式的的导入
```shell
from sklearn.datasets import load_svmlight_file

X_train,y_train = load_svmlight_file("/path/to/train_dataset.txt")

# 将稀疏举证转化为完整的特征矩阵
X_train.todense()  

```

###### s2. Supervised Classification 几种常用方法：

logistic regression

```python
from sklearn.linear_model import LogisticRgression

clf2 = LogisiticRegression().fit(X,Y)
print clf2

 > LogisticRegression(C=1.0, intercept_scaling=1, dual=False, fit_intercept=True,penalty='l2', tol=0.0001)
 
clf2.predict_proba(X_new)

 > array([[  9.07512928e-01,   9.24770379e-02,   1.00343962e-05]])

```

Linear SVM (Linear kernel)
```python
from sklearn.svm import LinearSVC

clf = LinearSVC()

clf.fit(X,Y)

X_new = [[5.0,3.6,1.3,0.25]]

clf.predict(X_new)

array([0], dtype=int32)
```

SVM (RBF or other kernel)

```python
>>> from sklearn import svm

>>> clf = svm.SVC()

>>> clf.fit(X, Y) 

SVC(C=1.0, cache_size=200, class_weight=None, coef0=0.0, degree=3,

gamma=0.0, kernel='rbf', probability=False, shrinking=True, tol=0.001,

verbose=False)

>>> clf.predict([[2., 2.]])

array([ 1.])
```

Naive Bayes (Gaussian likelihood)

```python
from sklearn.naive_bayes import GaussianNB

>>> from sklearn import datasets

>>> gnb = GaussianNB()

>>> gnb = gnb.fit(x, y)

>>> gnb.predict(xx)#result[0] is the most likely class label
```

Decision Tree (classification not regression)

```python
>>> from sklearn import tree

>>> clf = tree.DecisionTreeClassifier()

>>> clf = clf.fit(X, Y)

>>> clf.predict([[2., 2.]])

array([ 1.])
```

Ensemble (Random Forests, classification not regression)

```python
 >>> from sklearn.ensemble import RandomForestClassifier

>>> clf = RandomForestClassifier(n_estimators=10)

>>> clf = clf.fit(X, Y)

>>> clf.predict(X_test) 
```

##### s3 Model Selection (Cross-validation) 

手工分training data和testing data当然可以了，但是更方便的方法是自动进行，scikit-learn也有相关的功能，这里记录下cross-validation的代码： 

```python
from sklearn improt cross_validation

from sklearn import svm

clf = svm.SVC(kernel="linear",C=1)

scores = cross_validation.cross_val_score(clf,iris.data,iris.target,cv=5) # 5-fold cv

# 指定评分函数
from sklearn import metrics
cross_validation.cross_val_score(clf,iris.data,iris.target,cv=5,score_func=metrics.f1_score)

```

###### s4 Sign Prediction Experiment

数据集，EPINIONS，有user与user之间的trust与distrust关系，以及interaction（对用户评论的有用程度打分）。

Features：网络拓扑feature参考"Predict positive and negative links in online social network"，用户交互信息feature。

一共设了3类instances，每类3次训练+测试，训练数据是测试数据的10倍，~80,000个29/5/34维向量，得出下面一些结论。时间 上，GNB最快（所有instance都是2~3秒跑完），DT非常快（有一类instance只用了1秒，其他都要4秒），LR很快(三类 instance的时间分别是2秒，5秒，~30秒)，RF也不慢（一个instance9秒，其他26秒），linear kernel的SVM要比LR慢好几倍（所有instance要跑30多秒），RBF kernel的SVM比linear SVM要慢20+倍到上百倍（第一个instance要11分钟，第二个instance跑了近两个小时）。准确度上 RF>LR>DT>GNB>SVM(RBF kernel)>SVM(Linear kernel)。GNB和SVM(linear kernel)、SVM(rbf kernel)在第二类instance上差的比较远（10~20个百分点），LR、DT都差不多，RF确实体现了ENSEMBLE方法的强大，比LR有 较为显著的提升（近2~4个百分点）。（注：由于到该文提交为止，RBF版的SVM才跑完一次测试中的两个instance，上面结果仅基于此。另外，我 还尝试了SGD等方法，总体上都不是特别理想，就不记了）。在feature的有效性上面，用户交互feature比网络拓扑feature更加有效百分 五到百分十。 

###### s5 通用测试源代码

这里是我写的用包括上述算法在内的多种算法的自动分类并10fold cross-validation的python代码，只要输入文件保持本文开头所述的格式（且不包含注释信息），即可用多种不同算法测试分类效果。


##### 模型持久化

可以采用Python内建的持久性模型 pickle 来保存scikit的模型: 

```python
from sklearn import svm
from sklearn import datasets

clf = svm.SVC()
iris = datsets.load_iris()
X,y = iris.data,iris.target
clf.fit(X,y)

improt pickle
s = pickle.dumps(clf)
clf2=pickle.loads(s)
clf2.predict(X[0])

y[0]

```
在scikit的特定情形下，用joblib’s来代替pickle（joblib.dump&joblib.load）会更吸引人，在大数据下效率更高，但只能pickle到磁盘而不是字符串：

```python
from sklearn.externals import joblib
joblib.dump(clf,"filename.pkl")

clf = joblib.load("filename.pkl")
```

##### 网格搜索 ：搜索估计参数

不能通过训练 estimators 来得到的参数可以通过 grid search得到 
交叉验证的最优参数区间。

获取一个 estimators  的参数名称和当前值：

```python
estimator.get_params()
```
这些参数通常称为超参数。

一个搜索由以下部分组成：

* 一个估计器
* 一个参数空间
* 一个用于搜索和采样的函数
* 一个交叉验证 构建（cross-validation scheme） 
* 一个评分函数

sckit-learn 提供了两个 搜索函数：

1. GridSearchCV :在指定的参数区间进行穷举
2. RandomizedSearchCV : 根据指定的分布函数 来进行参数的采样

###### 1. GridSearchCV

GridSearchCV 通过穷举指定的参数空间来进行参数的 选择：

```python
para_grid  = [
{'C':[1,10,100,1000],'kernel':['linear']},
{'C':[1,10,100,1000],'gamma':[0.001,0.0001],'kernel':['rbf']},
]
```

grid_search_digits.py

```python
from __future__ import print_function

from sklearn import datasets
from sklearn.cross_validation import train_test_split
from sklearn.grid_search import GridSearchCV
from sklearn.metrics import classification_report
from sklearn.svm import SVC

print(__doc__)

# Loading the Digits dataset
digits = datasets.load_digits()

# To apply an classifier on this data, we need to flatten the image, to
# turn the data in a (samples, feature) matrix:
n_samples = len(digits.images)
X = digits.images.reshape((n_samples, -1))
y = digits.target

# Split the dataset in two equal parts
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.5, random_state=0)

# Set the parameters by cross-validation
tuned_parameters = [{'kernel': ['rbf'], 'gamma': [1e-3, 1e-4],
                     'C': [1, 10, 100, 1000]},
                    {'kernel': ['linear'], 'C': [1, 10, 100, 1000]}]

scores = ['precision', 'recall']

for score in scores:
    print("# Tuning hyper-parameters for %s" % score)
    print()

    clf = GridSearchCV(SVC(C=1), tuned_parameters, cv=5,
                       scoring='%s_weighted' % score)
    clf.fit(X_train, y_train)

    print("Best parameters set found on development set:")
    print()
    print(clf.best_params_)
    print()
    print("Grid scores on development set:")
    print()
    for params, mean_score, scores in clf.grid_scores_:
        print("%0.3f (+/-%0.03f) for %r"
              % (mean_score, scores.std() * 2, params))
    print()

    print("Detailed classification report:")
    print()
    print("The model is trained on the full development set.")
    print("The scores are computed on the full evaluation set.")
    print()
    y_true, y_pred = y_test, clf.predict(X_test)
    print(classification_report(y_true, y_pred))
    print()

# Note the problem is too easy: the hyperparameter plateau is too flat and the
# output model is the same for precision and recall with ties in quality.
```

Sample pipeline for text feature extraction and evaluation

grid_search_text_feature_extraction.py

```python
# Author: Olivier Grisel <olivier.grisel@ensta.org>
#         Peter Prettenhofer <peter.prettenhofer@gmail.com>
#         Mathieu Blondel <mathieu@mblondel.org>
# License: BSD 3 clause

from __future__ import print_function

from pprint import pprint
from time import time
import logging

from sklearn.datasets import fetch_20newsgroups
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.linear_model import SGDClassifier
from sklearn.grid_search import GridSearchCV
from sklearn.pipeline import Pipeline

print(__doc__)

# Display progress logs on stdout
logging.basicConfig(level=logging.INFO,
                    format='%(asctime)s %(levelname)s %(message)s')


###############################################################################
# Load some categories from the training set
categories = [
    'alt.atheism',
    'talk.religion.misc',
]
# Uncomment the following to do the analysis on all the categories
#categories = None

print("Loading 20 newsgroups dataset for categories:")
print(categories)

data = fetch_20newsgroups(subset='train', categories=categories)
print("%d documents" % len(data.filenames))
print("%d categories" % len(data.target_names))
print()

###############################################################################
# define a pipeline combining a text feature extractor with a simple
# classifier
pipeline = Pipeline([
    ('vect', CountVectorizer()),
    ('tfidf', TfidfTransformer()),
    ('clf', SGDClassifier()),
])

# uncommenting more parameters will give better exploring power but will
# increase processing time in a combinatorial way
parameters = {
    'vect__max_df': (0.5, 0.75, 1.0),
    #'vect__max_features': (None, 5000, 10000, 50000),
    'vect__ngram_range': ((1, 1), (1, 2)),  # unigrams or bigrams
    #'tfidf__use_idf': (True, False),
    #'tfidf__norm': ('l1', 'l2'),
    'clf__alpha': (0.00001, 0.000001),
    'clf__penalty': ('l2', 'elasticnet'),
    #'clf__n_iter': (10, 50, 80),
}

if __name__ == "__main__":
    # multiprocessing requires the fork to happen in a __main__ protected
    # block

    # find the best parameters for both the feature extraction and the
    # classifier
    grid_search = GridSearchCV(pipeline, parameters, n_jobs=-1, verbose=1)

    print("Performing grid search...")
    print("pipeline:", [name for name, _ in pipeline.steps])
    print("parameters:")
    pprint(parameters)
    t0 = time()
    grid_search.fit(data.data, data.target)
    print("done in %0.3fs" % (time() - t0))
    print()

    print("Best score: %0.3f" % grid_search.best_score_)
    print("Best parameters set:")
    best_parameters = grid_search.best_estimator_.get_params()
    for param_name in sorted(parameters.keys()):
        print("\t%s: %r" % (param_name, best_parameters[param_name]))
```

###### 2 Randomized Parameter Optimization

 RandomizedSearchCV 实现了在参数空间上进行随机搜索的方法。它比蛮力搜索有以下几个好处：
 
```python
[{'C':scipy.stats.expon(scale=100),
  'gamma':scipy.stats.expon(scale=.1),
  'kernel':['rbf'],
  'class_weight':['auto',None]}]
```

上面使用了scipy.stats 模块， 它包含许多有用的分布 用于采样： expon , gamma,uniform, randint , 

Comparing randomized search and grid search for hyperparameter estimation


 randomized_search.py
```python
print(__doc__)

import numpy as np

from time import time
from operator import itemgetter
from scipy.stats import randint as sp_randint

from sklearn.grid_search import GridSearchCV, RandomizedSearchCV
from sklearn.datasets import load_digits
from sklearn.ensemble import RandomForestClassifier

# get some data
iris = load_digits()
X, y = iris.data, iris.target

# build a classifier
clf = RandomForestClassifier(n_estimators=20)


# Utility function to report best scores
def report(grid_scores, n_top=3):
    top_scores = sorted(grid_scores, key=itemgetter(1), reverse=True)[:n_top]
    for i, score in enumerate(top_scores):
        print("Model with rank: {0}".format(i + 1))
        print("Mean validation score: {0:.3f} (std: {1:.3f})".format(
              score.mean_validation_score,
              np.std(score.cv_validation_scores)))
        print("Parameters: {0}".format(score.parameters))
        print("")


# specify parameters and distributions to sample from
param_dist = {"max_depth": [3, None],
              "max_features": sp_randint(1, 11),
              "min_samples_split": sp_randint(1, 11),
              "min_samples_leaf": sp_randint(1, 11),
              "bootstrap": [True, False],
              "criterion": ["gini", "entropy"]}

# run randomized search
n_iter_search = 20
random_search = RandomizedSearchCV(clf, param_distributions=param_dist,
                                   n_iter=n_iter_search)

start = time()
random_search.fit(X, y)
print("RandomizedSearchCV took %.2f seconds for %d candidates"
      " parameter settings." % ((time() - start), n_iter_search))
report(random_search.grid_scores_)

# use a full grid over all parameters
param_grid = {"max_depth": [3, None],
              "max_features": [1, 3, 10],
              "min_samples_split": [1, 3, 10],
              "min_samples_leaf": [1, 3, 10],
              "bootstrap": [True, False],
              "criterion": ["gini", "entropy"]}

# run grid search
grid_search = GridSearchCV(clf, param_grid=param_grid)
start = time()
grid_search.fit(X, y)

print("GridSearchCV took %.2f seconds for %d candidate parameter settings."
      % (time() - start, len(grid_search.grid_scores_)))
report(grid_search.grid_scores_)

```


##### 使用管道链接多个 特征抽取方法

###### 管道基本用法

pipeline用于将多个估计器链接在一起， 可以将你的特征处理过程和估计器连接在一起，进行一次调用即可。


在pipleine中的所有估计器 除了最后一个必须是 transformer (含有tranformer 方法),最后一个可以是任何类型（如transformer ， classifier）

pipleline是有一系列的keyvalue对组成的， key是估计器的名称， value 是估计器的名称

```python
from sklearn.pipleline import Pipeline
from sklearn.svm import SVC
from sklearn.decomposition import PCA

estimators = [('reduce_dim',PCA()),('svm',SVC())]
clf = Pipeline(estimaters)

print clf

>>>
Pipeline(steps=[('reduce_dim', PCA(copy=True, n_components=None,
    whiten=False)), ('svm', SVC(C=1.0, cache_size=200, class_weight=None,
    coef0=0.0, degree=3, gamma=0.0, kernel='rbf', max_iter=-1,
    probability=False, random_state=None, shrinking=True, tol=0.001,
    verbose=False))])

```

可以使用 mke_pipeline 方法更简单的构造pipleine  ,他接受多个估计器，自动为其赋名，并返回一个pipeline
```python
from sklearn.pipleline import Pipeline
from sklearn.native_bayes import MultinomialNB
from sklearn.preprocessing import Binarizer

pipeline = make_popeline(Binarizer(),MultinomialNB())

print pipeline

>>>
Pipeline(steps=[('binarizer', Binarizer(copy=True, threshold=0.0)),
                ('multinomialnb', MultinomialNB(alpha=1.0,
                                                class_prior=None,
                                                fit_prior=True))])

```

它将每个估计器作为一个step进行存储
```python
>>> clf.steps[0]
('reduce_dim', PCA(copy=True, n_components=None, whiten=False))

```
也可以使用字典的形式获取：
```python
>>> clf.named_steps['reduce_dim']
PCA(copy=True, n_components=None, whiten=False)
```
pipeline 中的估计器的参数可以使用set_params 进行传入
```python
>>> clf.set_params(svm__C=10) 
Pipeline(steps=[('reduce_dim', PCA(copy=True, n_components=None,
    whiten=False)), ('svm', SVC(C=10, cache_size=200, class_weight=None,
    coef0=0.0, degree=3, gamma=0.0, kernel='rbf', max_iter=-1,
    probability=False, random_state=None, shrinking=True, tol=0.001,
    verbose=False))])
```

例如在交叉验证中：
```python
>>> from sklearn.grid_search import GridSearchCV
>>> params = dict(reduce_dim__n_components=[2, 5, 10],
...               svm__C=[0.1, 10, 100])
>>> grid_search = GridSearchCV(clf, param_grid=params)
```

FeatureUnion 是Pipeline 的特征正处理部分的特例：
构造同pipeline一样：

```python
from sklearn.pipeline import FeatureUnion
>>> from sklearn.decomposition import PCA
>>> from sklearn.decomposition import KernelPCA
>>> estimators = [('linear_pca', PCA()), ('kernel_pca', KernelPCA())]
>>> combined = FeatureUnion(estimators)
>>> combined 
FeatureUnion(n_jobs=1, transformer_list=[('linear_pca', PCA(copy=True,
    n_components=None, whiten=False)), ('kernel_pca', KernelPCA(alpha=1.0,
    coef0=1, degree=3, eigen_solver='auto', fit_inverse_transform=False,
    gamma=None, kernel='linear', kernel_params=None, max_iter=None,
    n_components=None, remove_zero_eig=False, tol=0))],
    transformer_weights=None)
```
同样的可以使用 make_union,进行默认名称的构建

下面是一个链接特征预处理过程(pca +  单个参数选择)的例子：

```python

from sklearn.pipeline import Pipeline, FeatureUnion
from sklearn.grid_search import GridSearchCV
from sklearn.svm import SVC
from sklearn.datasets import load_iris
from sklearn.decomposition import PCA
from sklearn.feature_selection import SelectKBest

iris = load_iris()

X, y = iris.data, iris.target

# This dataset is way to high-dimensional. Better do PCA:
pca = PCA(n_components=2)

# Maybe some original features where good, too?
selection = SelectKBest(k=1)

# Build estimator from PCA and Univariate selection:

combined_features = FeatureUnion([("pca", pca), ("univ_select", selection)])

# Use combined features to transform dataset:
X_features = combined_features.fit(X, y).transform(X)

svm = SVC(kernel="linear")

# Do grid search over k, n_components and C:

pipeline = Pipeline([("features", combined_features), ("svm", svm)])

param_grid = dict(features__pca__n_components=[1, 2, 3],
                  features__univ_select__k=[1, 2],
                  svm__C=[0.1, 1, 10])

grid_search = GridSearchCV(pipeline, param_grid=param_grid, verbose=10)
grid_search.fit(X, y)
print(grid_search.best_estimator_)
```




真实世界中，有多种抽取特征的方法， 通常是将他们组成 链表进行 流水执行
同时也方便了交叉验证和网格搜索。

```python
# Author: Andreas Mueller <amueller@ais.uni-bonn.de>
#
# License: BSD 3 clause

from sklearn.pipeline import Pipeline, FeatureUnion
from sklearn.grid_search import GridSearchCV
from sklearn.svm import SVC
from sklearn.datasets import load_iris
from sklearn.decomposition import PCA
from sklearn.feature_selection import SelectKBest

iris = load_iris()

X, y = iris.data, iris.target

# This dataset is way to high-dimensional. Better do PCA:
pca = PCA(n_components=2)

# Maybe some original features where good, too?
selection = SelectKBest(k=1)

# Build estimator from PCA and Univariate selection:

combined_features = FeatureUnion([("pca", pca), ("univ_select", selection)])

# Use combined features to transform dataset:
X_features = combined_features.fit(X, y).transform(X)

svm = SVC(kernel="linear")

# Do grid search over k, n_components and C:

pipeline = Pipeline([("features", combined_features), ("svm", svm)])

param_grid = dict(features__pca__n_components=[1, 2, 3],
                  features__univ_select__k=[1, 2],
                  svm__C=[0.1, 1, 10])

grid_search = GridSearchCV(pipeline, param_grid=param_grid, verbose=10)
grid_search.fit(X, y)
print(grid_search.best_estimator_)
```

###### 链接anova(方差分析)特征选择和svm

```python
print(__doc__)

from sklearn import svm
from sklearn.datasets import samples_generator
from sklearn.feature_selection import SelectKBest, f_regression
from sklearn.pipeline import make_pipeline

# import some data to play with
X, y = samples_generator.make_classification(
    n_features=20, n_informative=3, n_redundant=0, n_classes=4,
    n_clusters_per_class=2)

# ANOVA SVM-C
# 1) anova filter, take 3 best ranked features
anova_filter = SelectKBest(f_regression, k=3)
# 2) svm
clf = svm.SVC(kernel='linear')

anova_svm = make_pipeline(anova_filter, clf)
anova_svm.fit(X, y)
anova_svm.predict(X)
```

###### 使用pipeline 链接 pca 和逻辑回归

```python
print(__doc__)


# Code source: Gaël Varoquaux
# Modified for documentation by Jaques Grobler
# License: BSD 3 clause


import numpy as np
import matplotlib.pyplot as plt

from sklearn import linear_model, decomposition, datasets
from sklearn.pipeline import Pipeline
from sklearn.grid_search import GridSearchCV

logistic = linear_model.LogisticRegression()

pca = decomposition.PCA()
pipe = Pipeline(steps=[('pca', pca), ('logistic', logistic)])

digits = datasets.load_digits()
X_digits = digits.data
y_digits = digits.target

###############################################################################
# Plot the PCA spectrum
pca.fit(X_digits)

plt.figure(1, figsize=(4, 3))
plt.clf()
plt.axes([.2, .2, .7, .7])
plt.plot(pca.explained_variance_, linewidth=2)
plt.axis('tight')
plt.xlabel('n_components')
plt.ylabel('explained_variance_')

###############################################################################
# Prediction

n_components = [20, 40, 64]
Cs = np.logspace(-4, 4, 3)

#Parameters of pipelines can be set using ‘__’ separated parameter names:

estimator = GridSearchCV(pipe,
                         dict(pca__n_components=n_components,
                              logistic__C=Cs))
estimator.fit(X_digits, y_digits)

plt.axvline(estimator.best_estimator_.named_steps['pca'].n_components,
            linestyle=':', label='n_components chosen')
plt.legend(prop=dict(size=12))
plt.show()
```

##### 交叉验证 Cross-validation: evaluating estimator performance

###### 划分数据集

```python
import numpy as np
from sklearn import cross_validation
from sklearn import datasets
from sklearn import svm

iris = datasets.load_iris()

iris.data.shape,iris.target.shape

X_train,y_train,X_test,y_test = cross_validation.train_test_split(iris.data,iris.target,test_size=0.4,random_state=0)

clf = svm.SVC(kernel='linear',C=1).fit(X_train,y_train)

clf.score(X_test,y_test)


```

###### k-fold

```python
clf = svm.SVC(kernel='linear',C=1)
scores = cross_validation.cross_val_score(
	clf,iris.data,iris.target,cv=5)
    
print("Accuray: %0.2f (+/- %0.2f)" % (scores.mean(),scores.std() * 2))
```

指定评分方法：

```python
>>> from sklearn import metrics
>>> scores = cross_validation.cross_val_score(clf, iris.data, iris.target,
...     cv=5, scoring='f1_weighted')
>>> scores                                              
array([ 0.96...,  1.  ...,  0.96...,  0.96...,  1.        ])

```

指定cv方案：
```python
>>> n_samples = iris.data.shape[0]
>>> cv = cross_validation.ShuffleSplit(n_samples, n_iter=3,
...     test_size=0.3, random_state=0)

>>> cross_validation.cross_val_score(clf, iris.data, iris.target, cv=cv)
...                                                     
array([ 0.97...,  0.97...,  1.        ])
```

链接特征处理和svm

```python
>>> from sklearn import preprocessing
>>> X_train, X_test, y_train, y_test = cross_validation.train_test_split(
...     iris.data, iris.target, test_size=0.4, random_state=0)
>>> scaler = preprocessing.StandardScaler().fit(X_train)
>>> X_train_transformed = scaler.transform(X_train)
>>> clf = svm.SVC(C=1).fit(X_train_transformed, y_train)
>>> X_test_transformed = scaler.transform(X_test)
>>> clf.score(X_test_transformed, y_test)  
0.9333...
```

使用pipeline 链接处理过程：

```python
>>> from sklearn.pipeline import make_pipeline
>>> clf = make_pipeline(preprocessing.StandardScaler(), svm.SVC(C=1))
>>> cross_validation.cross_val_score(clf, iris.data, iris.target, cv=cv)
...                                                 
array([ 0.97...,  0.93...,  0.95...])
```

获得交叉验证的分类器
```python
>>> predicted = cross_validation.cross_val_predict(clf, iris.data,
...                                                iris.target, cv=10)
>>> metrics.accuracy_score(iris.target, predicted) 
0.966...
```

实际使用交叉验证

每一个estimator都暴露一个score 函数来衡量它对指定数据集的分类效果

```python
from sklearn import datasets ,svm
digits = datasets.load_digits()
X_data = digits.data
y_target = digits.target
svc = svm.SVC(X=1,kernel='linear')
svc.fit(X_data[:-100],y_target[:-100]).score(X_data[-100:],y_target[-100:])
0.97999999999999998
```

使用k-fold 方法进行：
```python
import numpy as np

X_folds = np.array_split(X_digits,3)
y_folds = np.array_split(y_digits,3)
scores = list()

for k in range(3):
	X_train = list(X_folds)
    X_test = X_train.pop(k)
    X_train = np.concatenate(X_train)
    y_train = list(y_folds)
    y_test = y_train.pop(k)
    y_train = np.concatenate(y_train)
    scores.append(svc.fit(X_train,y_train).score(X_test,y_test))
 
print(scores)

[0.93489148580968284, 0.95659432387312182, 0.93989983305509184]
```

scikt learn 实现了上面函数的功能， 提供了KFold 函数接口 它接受数据的长度， 划分fold 的个数K  返回 每次循环式 train 和 test 数据集的索引

```python
from sklearn import cross_validation
k_fold = cross_validation.KFold(n=6,n_folds = 3)

for  train_indicates,test_indices in k_fold:
	print('Train : %s | test : %s' % (train_indeices,test_indices) )
    
Train: [2 3 4 5] | test: [0 1]
Train: [0 1 4 5] | test: [2 3]
Train: [0 1 2 3] | test: [4 5]
```

下面将其应用到 svc 的交叉验证中

```python
kfold = cross_validation.KFold(len(X_digits),n_folds=3)

[svc.fit(X_digits[train],y_digits[train]).score(X_digits[test],y_digits[test])]

[0.93489148580968284, 0.95659432387312182, 0.93989983305509184]

```

将上面的方案进一步简化 使用 cross_validation.cross_val_score （需要传入一个cv方案 ）：

```python
cross_validation.cross_val_score(svc, X_digits, y_digits, cv=kfold, n_jobs=-1)
array([ 0.93489149,  0.95659432,  0.93989983])
```
n_jobs ,指定了使用的线程数 ， 最好等于可用cpu的个数，指定为-1 ，意味着在所有cpu上执行


    Cross-validation generators

KFold (n, k) 	Split it K folds, train on K-1 and then test on left-out 	
StratifiedKFold (y, k) 	 It preserves the class ratios / label distribution within each fold. 	
LeaveOneOut (n) 	Leave one observation out 	
LeaveOneLabelOut (labels)  Takes a label array to group observations


与gridSearch的关系

GirdSearch 在 cv的基础上对 右超参数组成的网格 逐个执行cv ，最后选出评分最大的

默认使用 3-fold 交叉验证， 

```python
from sklearn.grid_search import GridSearchCV
Cs = np.logspace(-6,-1,10)

clf = GridSearchCV(estimator=svc,param_grid=dict(C=Cs),n_jobs = -1)

clf.fit(X_digits[:1000],y_digits[:1000])
clf.best_score_
>>>
0.925...

clf.best_estimator_.C 
>>> 
0.0077...

clf.score(X_digits[1000:], y_digits[1000:])      
>>>
0.943...

```



##### 混淆矩阵（Confusion matrix）

Example of confusion matrix usage to evaluate the quality of the output of a classifier on the iris data set. The diagonal elements represent the number of points for which the predicted label is equal to the true label, while off-diagonal elements are those that are mislabeled by the classifier. The higher the diagonal values of the confusion matrix the better, indicating many correct predictions.

The figures show the confusion matrix with and without normalization by class support size (number of elements in each class). This kind of normalization can be interesting in case of class imbalance to have a more visual interpretation of which class is being misclassified.

Here the results are not as good as they could be as our choice for the regularization parameter C was not the best. In real life applications this parameter is usually chosen using Grid Search: Searching for estimator parameters.

Script output:

```shell
Confusion matrix, without normalization
[[13  0  0]
 [ 0 10  6]
 [ 0  0  9]]
Normalized confusion matrix
[[ 1.    0.    0.  ]
 [ 0.    0.62  0.38]
 [ 0.    0.    1.  ]]
```




##### 使用Pipeline  链接特征选择和分类器

######  两种构造方法

1. Pipleline
  Pipeline 使用一个key -value 字典来初始化Pipeline
  
```python
from sklearn.pipeline import Pipeline
from sklearn.svm import SVC
from sklearn.decomposition import PCA

estimators = [('reduce_dim',PCA()),('svm',SVC())]
clf = Pipeline(estimators)

print clf

Pipeline(steps=[('reduce_dim', PCA(copy=True, n_components=None,
    whiten=False)), ('svm', SVC(C=1.0, cache_size=200, class_weight=None,
    coef0=0.0, degree=3, gamma=0.0, kernel='rbf', max_iter=-1,
    probability=False, random_state=None, shrinking=True, tol=0.001,
    verbose=False))])
```

2. make_pipeline 

```python
from sklearn.pipeline import Pipeline
from sklearn.svm import SVC
from sklearn.decomposition import PCA

clf = make_pipeline(Binarizer(),MultimomialNB())


print clf 
Pipeline(steps=[('binarizer', Binarizer(copy=True, threshold=0.0)),
                ('multinomialnb', MultinomialNB(alpha=1.0,
                                                class_prior=None,
                                                fit_prior=True))])
```

###### pipeline 中的成员和访问方法

每一个estimator 是pipeline中的一个step

```python
clf.steps[0]

('reduce_dim', PCA(copy=True, n_components=None, whiten=False))
```

还可以使用名称直接访问estimator
```python
clf.named_steps['reduce_dim']
```

为pipeline中指定的estimator 设置参数的方法 
使用语法 ：  <estimator>__<parameter>
注意是两个下划线 estimator 是你在构造pipeline 时为该step 赋予的名称
```python
clf.set_params(svm__c=10)

Pipeline(steps=[('reduce_dim', PCA(copy=True, n_components=None,
    whiten=False)), ('svm', SVC(C=10, cache_size=200, class_weight=None,
    coef0=0.0, degree=3, gamma=0.0, kernel='rbf', max_iter=-1,
    probability=False, random_state=None, shrinking=True, tol=0.001,
    verbose=False))])
```

###### 与GridSearch结合是哟你

```python
from sklearn.grid_search import GridSearchCV

params_grid = dict(reduce_dim__n_components=[2,5,10]，
		svm__C=[0.1,10,100])
        
grid_search = GridSearchCV(clf,param_grid=params_grid)
```

###### FeatureUnion
FeatureUnion  同 Pipeline 一样，只不过是 专门用于feature extraction的

```python
from sklearn.pipeline import FeatureUnion
from sklearn.decomposition import PCA
from sklearn.decomposition import KernelPCA
estimators = [('linear_pca', PCA()), ('kernel_pca', KernelPCA())]
combined = FeatureUnion(estimators)
combined

FeatureUnion(n_jobs=1, transformer_list=[('linear_pca', PCA(copy=True,
    n_components=None, whiten=False)), ('kernel_pca', KernelPCA(alpha=1.0,
    coef0=1, degree=3, eigen_solver='auto', fit_inverse_transform=False,
    gamma=None, kernel='linear', kernel_params=None, max_iter=None,
    n_components=None, remove_zero_eig=False, tol=0))],
    transformer_weights=None)
```

此外还有make_union 函数



##### 流形学习（Manifold learning）


###### 简介

流形学习是个很广泛的概念。这里我主要谈的是自从2000年以后形成的流形学习概念和其主要代表方法。自从2000年以后，流形学习被认为属于非线性降维的一个分支。众所周知，引导这一领域迅速发展的是2000年Science杂志上的两篇文章: Isomap and LLE (Locally Linear Embedding)。

###### 1. 流形学习的基本概念


那流形学习是什莫呢？为了好懂，我尽可能应用少的数学概念来解释这个东西。所谓流形（manifold）就是一般的几何对象的总称。比如人，有中国人、美国人等等；流形就包括各种维数的曲线曲面等。和一般的降维分析一样，流形学习把一组在高维空间中的数据在低维空间中重新表示。和以往方法不同的是，在流形学习中有一个假设，就是所处理的数据采样于一个潜在的流形上，或是说对于这组数据存在一个潜在的流形。 对于不同的方法，对于流形性质的要求各不相同，这也就产生了在流形假设下的各种不同性质的假设，比如在Laplacian Eigenmaps中要假设这个流形是紧致黎曼流形等。对于描述流形上的点，我们要用坐标，而流形上本身是没有坐标的，所以为了表示流形上的点，必须把流形放入外围空间（ambient space）中，那末流形上的点就可以用外围空间的坐标来表示。比如R^3中的球面是个2维的曲面，因为球面上只有两个自由度，但是球面上的点一般是用外围R^3空间中的坐标表示的，所以我们看到的R^3中球面上的点有3个数来表示的。当然球面还有柱坐标球坐标等表示。对于R^3中的球面来说，那末流形学习可以粗略的概括为给出R^3中的表示，在保持球面上点某些几何性质的条件下，找出找到一组对应的内蕴坐标（intrinsic coordinate）表示，显然这个表示应该是两维的，因为球面的维数是两维的。这个过程也叫参数化（parameterization）。直观上来说，就是把这个球面尽量好的展开在通过原点的平面上。在PAMI中，这样的低维表示也叫内蕴特征（intrinsic feature）。一般外围空间的维数也叫观察维数，其表示也叫自然坐标（外围空间是欧式空间）表示,在统计中一般叫observation。


了解了流形学习的这个基础，那末流形学习中的一些是非也就很自然了，这个下面穿插来说。由此，如果你想学好流形学习里的方法，你至少要了解一些微分流形和黎曼几何的基本知识。


###### 2 . 代表方法

a) Isomap。

Josh Tenenbaum的Isomap开创了一个数据处理的新战场。在没有具体说Isomap之前，有必要先说说MDS（Multidimensional Scaling）这个方法。我们国内的很多人知道PCA，却很多人不知道MDS。PCA和MDS是相互对偶的两个方法。MDS就是理论上保持欧式距离的一个经典方法，MDS最早主要用于做数据的可视化。由于MDS得到的低维表示中心在原点，所以又可以说保持内积。也就是说，用低维空间中的内积近似高维空间中的距离。经典的MDS方法，高维空间中的距离一般用欧式距离。

Isomap就是借窝生蛋。他的理论框架就是MDS，但是放在流形的理论框架内，原始的距离换成了流形上的测地线（geodesic）距离。其它一模一样。所谓的测地线，就是流形上加速度为零的曲线，等同于欧式空间中的直线。我们经常听到说测地线是流形上两点之间距离最短的线。其实这末说是不严谨的。流形上两点之间距离最短的线是测地线，但是反过来不一定对。另外，如果任意两个点之间都存在一个测地线，那末这个流形必须是连通的邻域都是凸的。Isomap就是把任意两点的测地线距离（准确地说是最短距离）作为流形的几何描述，用MDS理论框架理论上保持这个点与点之间的最短距离。在Isomap中，测地线距离就是用两点之间图上的最短距离来近似的，这方面的算法是一般计算机系中用的图论中的经典算法。

如果你曾细致地看过Isomap主页上的matlab代码，你就会发现那个代码的实现复杂度远超与实际论文中叙述的算法。在那个代码中，除了论文中写出的算法外，还包括了 outlier detection和embedding scaling。这两样东西，保证了运行他们的程序得到了结果一般来说相对比较理想。但是，这在他们的算法中并没有叙述。如果你直接按照他论文中的方法来实现，你可以体会一下这个结果和他们结果的差距。从此我们也可以看出，那几个作者做学问的严谨态度，这是值得我们好好学习的。

另外比较有趣的是，Tenenbaum根本不是做与数据处理有关算法的人，他是做计算认知科学（computational cognition science）的。在做这个方法的时候，他还在stanford，02年就去了MIT开创一派，成了CoCoSci 的掌门人，他的组成长十分迅速。但是有趣的是，在Isomap之后，他包括他在MIT带的学生就从来再也没有做过类似的工作。其原因我今年夏天有所耳闻。他在今年参加 UCLA Alan Yuille 组织的一个summer school上说，（不是原文，是大意）我们经常忘了做研究的原始出发点是什莫。他做Isomap就是为了找一个好的visual perception的方法，他还坚持了他的方向和信仰，computational cognition，他没有随波逐流。而由他引导起来的 manifold learning 却快速的发展成了一个新的方向。

这是一个值得我们好好思考的问题。我们做一个东西，选择一个研究方向究竟是为了什莫。你考虑过吗？
（当然，此问题也在问我自己）


b) LLE (Locally linear Embedding)

LLE在作者写出的表达式看,是个具有十分对称美的方法. 这种看上去的对称对于启发人很重要。LLE的思想就是，一个流形在很小的局部邻域上可以近似看成欧式的，就是局部线性的。那末，在小的局部邻域上，一个点就可以用它周围的点在最小二乘意义下最优的线性表示。LLE把这个线性拟合的系数当成这个流形局部几何性质的刻画。那末一个好的低维表示，就应该也具有同样的局部几何，所以利用同样的线性表示的表达式，最终写成一个二次型的形式，十分自然优美。

注意在LLE出现的两个加和优化的线性表达，第一个是求每一点的线性表示系数的。虽然原始公式中是写在一起的，但是求解时，是对每一个点分别来求得。第二个表示式，是已知所有点的线性表示系数，来求低维表示（或嵌入embedding）的，他是一个整体求解的过程。这两个表达式的转化正好中间转了个弯，使一些人困惑了，特别后面一个公式写成一个二次型的过程并不是那末直观，很多人往往在此卡住，而阻碍了全面的理解。我推荐大家去精读 Saul 在JMLR上的那篇LLE的长文。那篇文章无论在方法表达还是英文书写，我认为都是精品，值得好好玩味学习。

另外值得强调的是，对于每一点处拟合得到的系数归一化的操作特别重要，如果没有这一步，这个算法就没有效果。但是在原始论文中，他们是为了保持数据在平行移动下embedding不变。

LLE的matlab代码写得简洁明了，是一个样板。

在此有必要提提Lawrence Saul这个人。在Isomap和LLE的作者们中，Saul算是唯一一个以流形学习（并不限于）为研究对象开创学派的人。Saul早年主要做参数模型有关的算法。自从LLE以后，坐阵UPen创造了一个个佳绩。主要成就在于他的两个出色学生，Kilian Weinberger和 Fei Sha，做的方法。拿了很多奖，在此不多说，可以到他主页上去看。Weinberger把学习核矩阵引入到流形学习中来。他的这个方法在流形学习中影响到不是很显著，却是在 convex optimization 中人人得知。Fei Sha不用多说了，machine learning中一个闪亮的新星，中国留学生之骄傲。现在他们一个在Yahoo,一个在Jordan手下做PostDoc。


c) Laplacian Eigenmaps

要说哪一个方法被做的全面，那莫非LE莫属。如果只说LE这个方法本身，是不新的，许多年前在做mesh相关的领域就开始这莫用。但是放在黎曼几何的框架内，给出完整的几何分析的，应该是Belkin和Niyogi（LE作者）的功劳。

LE的基本思想就是用一个无向有权图来描述一个流形，然后通过用图的嵌入（graph embedding）来找低维表示。说白了，就是保持图的局部邻接关系的情况把这个图从高维空间中重新画在一个低维空间中（graph drawing）。

在至今为止的流行学习的典型方法中，LE是速度最快、效果相对来说不怎莫样的。但是LE有一个其他方法没有的特点，就是如果出现outlier情况下，它的鲁棒性（robustness）特别好。

后来Belkin和Niyogi又分析了LE的收敛性。大家不要忽视这个问题，很重要。鼓励有兴趣数学功底不错的人好好看看这篇文章。

 

d) Hessian Eigenmaps

如果你对黎曼几何不懂，基本上看不懂这个方法。又加作者表达的抽象，所以绝大多数人对这个方法了解不透彻。在此我就根据我自己的理解说说这个方法。

这个方法有两个重点：（1）如果一个流形是局部等距（isometric）欧式空间中一个开子集的，那末它的Hessian矩阵具有d+1维的零空间。（2）在每一点处，Hessian系数的估计。
首先作者是通过考察局部Hessian的二次型来得出结论的，如果一个流形局部等距于欧式空间中的一个开子集，那末由这个流形patch 到开子集到的映射函数是一个线性函数，线性函数的二次混合导数为零，所以局部上由Hessian系数构成的二次型也为零，这样把每一点都考虑到，过渡到全局的Hessian矩阵就有d+1维的零空间，其中一维是常函数构成的，也就是1向量。其它的d维子空间构成等距坐标。这就是理论基础的大意，当然作者在介绍的时候，为了保持理论严谨，作了一个由切坐标到等距坐标的过渡。

另外一个就是局部上Hessian系数的估计问题。我在此引用一段话：

If you approximate a function f(x) by a quadratic expansion

   f(x) = f(0) + (grad f)^T x  +  x^T Hf x + rem

then the hessian is what you get for the quadratic component.  So simply over a given neighborhood, develop the operator that approximates a function by its projection on 1, x_1,...,x_k,  x_1^2,...,x_k^2, x_1*x_2,... ,x_{k-1}*x_{k}.  Extract the component of the operator that delivers the projection on  x_1^2,...,x_k^2, x_1*x_2,... ,x_{k-1}*x_{k}.

dave

这段话是我在初学HE时候，写信问Dave Donoho，他给我的回信。希望大家领会。如果你了解了上述基本含义，再去细看两遍原始论文，也许会有更深的理解。由于HE牵扯到二阶导数的估计，所以对噪声很敏感。另外，HE的原始代码中在计算局部切坐标的时候，用的是奇异值分解(SVD)，所以如果想用他们的原始代码跑一下例如图像之类的真实数据，就特别的慢。其实把他们的代码改一下就可以了，利用一般PCA的快速计算方法，计算小尺寸矩阵的特征向量即可。还有，在原始代码中，他把Hessian系数归一化了，这也就是为什莫他们叫这个方法为 Hessian LLE 的原因之一。

Dave Dohono是学术界公认的大牛，在流形学习这一块，是他带着他的一个学生做的，Carrie Grimes。现在这个女性研究员在Google做 project leader，学术界女生同学的楷模 : )

 

e) LTSA (Local tangent space alignment)

很荣幸，这个是国内学者（浙江大学数学系的老师ZHANG Zhenyue）为第一作者做的一个在流行学习中最出色的方法。由于这个方法是由纯数学做数值分析出身的老师所做，所以原始论文看起来公式一大堆，好像很难似的。其实这个方法非常直观简单。

象 Hessian Eigenmaps 一样，流形的局部几何表达先用切坐标，也就是PCA的主子空间中的坐标。那末对于流形一点处的切空间，它是线性子空间，所以可以和欧式空间中的一个开子集建立同构关系，最简单的就是线性变换。在微分流形中，就叫做切映射 (tangential map)，是个很自然很基础的概念。把切坐标求出来，建立出切映射，剩下的就是数值计算了。最终这个算法划归为一个很简单的跌代加和形式。如果你已经明白了MDS，那末你就很容易明白，这个算法本质上就是MDS的从局部到整体的组合。


这里主要想重点强调一下，那个论文中使用的一个从局部几何到整体性质过渡的alignment技术。在spectral method（特征分解的）中，这个alignment方法特别有用。只要在数据的局部邻域上你的方法可以写成一个二次项的形式，就可以用。
其实LTSA最早的版本是在02年的DOCIS上。这个alignment方法在02年底Brand的 charting a manifold 中也出现，隐含在Hessian Eigenmaps中。在HE中，作者在从局部的Hessian矩阵过渡到全局的Hessian矩阵时，用了两层加号，其中就隐含了这个 alignment方法。后来国内一个叫 ZHAO Deli 的学生用这个方法重新写了LLE，发在Pattern Recognition上，一个短文。可以预见的是，这个方法还会被发扬光大。

ZHA Hongyuan 后来专门作了一篇文章来分析 alignment matrix 的谱性质，有兴趣地可以找来看看。

 

f) MVU (Maximum variance unfolding)

这个方法刚发出来以后，名字叫做Semi-definite Embedding (SDE)。构建一个局部的稀疏欧式距离矩阵以后，作者通过一定约束条件（主要是保持距离）来学习到一个核矩阵，对这个核矩阵做PCA就得到保持距离的 embedding，就这莫简单。但是就是这个方法得了多少奖，自己可以去找找看。个人观点认为，这个方法之所以被如此受人赏识，无论在vision还是在learning，除了给流形学习这一领域带来了一个新的解决问题的工具之外，还有两个重点，一是核方法（kernel），二是半正定规划（semi-definite programming），这两股风无论在哪个方向（learning and Vision）上都吹得正猛。


g) S-Logmaps

aa

这个方法不太被人所知，但是我认为这个是流形学习发展中的一个典型的方法（其实其他还有很多人也这莫认为）。就效果来说，这个方法不算好，说它是一个典型的方法，是因为这个方法应用了黎曼几何中一个很直观的性质。这个性质和法坐标(normal coordinate)、指数映射(exponential map)和距离函数(distance function)有关。

如果你了解黎曼几何，你会知道，对于流形上的一条测地线，如果给定初始点和初始点处测地线的切方向，那莫这个测地线就可以被唯一确定。这是因为在这些初始条件下，描述测地线的偏微分方程的解是唯一的。那末流形上的一条测地线就可以和其起点处的切平面上的点建立一个对应关系。我们可以在这个切平面上找到一点，这个点的方向就是这个测地线在起点处的切方向，其长度等于这个测地线上的长。这样的一个对应关系在局部上是一一对应的。那末这个在切平面上的对应点在切平面中就有一个坐标表示，这个表示就叫做测地线上对应点的法坐标表示（有的也叫指数坐标）。那末反过来，我们可以把切平面上的点映射到流形上，这个映射过程就叫做指数映射（Logmap就倒过来）。如果流形上每一个点都可以这样在同一个切平面上表示出来，那末我们就可以得到保持测地线长度的低维表示。如果这样做得到，流形必须可以被单坐标系统所覆盖。

如果给定流形上的采样点，如果要找到法坐标，我们需要知道两个东西，一是测地线距离，二是每个测地线在起点处的切方向。第一个东西好弄，利用Isomap中的方法直接就可以解决，关键是第二个。第二个作者利用了距离函数的梯度，这个梯度和那个切方向是一个等价的关系，一般的黎曼几何书中都有叙述。作者利用一个局部切坐标的二次泰勒展开来近似距离函数，而距离是知道的，就是测地线距离，局部切坐标也知道，那末通过求一个简单的最小二乘问题就可以估计出梯度方向。

如果明白这个方法的几何原理，你再去看那个方法的结果，你就会明白为什莫在距离中心点比较远的点的embedding都可以清楚地看到在一条条线上，效果不太好。


bb

最近这个思想被北大的一个年轻的老师 LIN Tong 发扬光大，就是ECCV‘06上的那篇，还有即将刊登出的TPAMI上的 Riemannian Manifold Learning，实为国内研究学者之荣幸。Lin的方法效果非常好，但是虽然取名叫Riemannian，没有应用到黎曼几何本身的性质，这样使他的方法更容易理解。

Lin也是以一个切空间为基准找法坐标，这个出发点和思想和Brun（S-Logmaps）的是一样的。但是Lin全是在局部上操作的，在得出切空间原点处局部邻域的法坐标以后，Lin采用逐步向外扩展的方法找到其他点的法坐标，在某一点处，保持此点到它邻域点的欧式距离和夹角，然后转化成一个最小二乘问题求出此点的法坐标，这样未知的利用已知的逐步向外扩展。说白了就像缝网一样，从几个临近的已知点开始，逐渐向外扩散的缝。效果好是必然的。


有人做了个好事情，做了个系统，把几个方法的matlab代码放在了一起 http://www.math.umn.edu/~wittman/mani/

以上提到方法论文，都可以用文中给出的关键词借助google.com找到。

 


3. 基本问题和个人观点

流形学习现在还基本处于理论探讨阶段，在实际中难以施展拳脚，不过在图形学中除外。我就说说几个基本的问题。

a. 谱方法对噪声十分敏感。希望大家自己做做实验体会一下，流形学习中谱方法的脆弱。
b. 采样问题对结果的影响。
c. 收敛性
d. 一个最尴尬的事情莫过于，如果用来做识别，流形学习线性化的方法比原来非线性的方法效果要好得多，如果用原始方法做识别，那个效果叫一个差。也正因为此，使很多人对流形学习产生了怀疑。原因方方面面 : )

e. 把偏微分几何方法引入到流形学习中来是一个很有希望的方向。这样的工作在最近一年已经有出现的迹象。

f. 坦白说，我已不能见庐山真面目了，还是留给大家来说吧

结尾写得有点草率，实在是精疲力尽了，不过还好主体部分写完。

 


4. 结束语

做学问的人有很多种，有的人学问做得很棒，但是独善其身者居多；有的人还谈不上做学问总想兼济天下。小弟不幸成了后一种人，总觉才学疏浅，力不从心，让各位见笑了。

今天一位朋友（filestorm）给我分享《列子御风》的故事，很受教育。鄙人功力不及二层，心却念是非，口却言利害，实在惭愧。





##### clustering

###### 输入说明

整个模块的聚类算法使用不同种类的输入：

* MeanShift 和 k-means  
   直接使用原始输入矩阵， 矩阵的形状是： [n_smaples,n_features]
   这样的输入数据可以通过 sklearn.feature_extraction 模块块进行获取， 
   他们使用存在于一个向量空间（特征空间）的特征数据。
   
* AffinityProGagation 和 SpectraClustering 
   使用数据的相似度矩阵作为输入数据， 矩阵的形状是 [n_smaple,n_sample],
   这样的数据可以使用 sklearn.metrics.pairwise 模块对 原始特征数据进行处理获得。
   这两个算法的特点是， 可以使用任意的数据，只要这些数据 可以生成similarity matrix 。


下面的图展示了 ，各种聚类算法在数据集上的表现

![ ](http://scikit-learn.org/stable/_images/plot_cluster_comparison_0011.png)

下面的表格详细说明了各种聚类的特性


|Method name |	Parameters |	Scalability |	Usecase 	|Geometry  (metric used)|
|:--: |	:--:|	:--: |:--:|:--:|
||||||
|K-Means|number of clusters| Very large n_smaples,medium n_clusters with MiniBatch code | General-purpose(泛化目标) , even cluster size（类的size很平均） ,not too many clusters| 两点之间的距离|
|Affinity propagation | damping（阻尼，衰减）， smaple prefereance|不随 样本个数 n_samples 进行扩展|很多类别， 类的size差异比较大（uneven cluster size） ,非平坦的几何形状（non-flat geometry） |图距离（Graph distance）（ 例如 最近邻图 nearest-neighbor graph） |
|Mean-shift| bandwidth|Not scalable with n_samples|很多类别， 类的size差异比较大（uneven cluster size） ,非平坦的几何形状（non-flat geometry） | 两点之间的距离|
|Spectral Clustering| number of clusters|Medium n_samples, small n_clusters|很少的类别，类的size 很平均， 非平坦的几何形状 |图距离（如nearest-neighbor graph）|
|Ward hierarchical clustering | number of clusters| Large n_sample and  n_clusters| 许多类别，possibly connectivity constraints（可能性联通约束） |两点之间的距离|
|Agglometative clustering|类的个数， linkage type ,distance| Large n_samples and n_cluster| 许多类别，有连通性约束， 非欧拉距离（non_ Euclidean distances）| 任何 pariwise 距离|
|DBSCAN|neighborhood size| very large n_smaples, medium n_clusters|非平坦几何形状，类的size 差异较大|最近点之间的距离|
|Gaussian clustering| many| not scalable| 平坦的几何形状，易于密度估计|Mahalanobis distances to centers（马氏距离中心）|
|Birch| branching factor, threshold, optional global clusterer.|Large n_clusters and n_samples|Large dataset, outlier removal, data reduction.|Euclidean distance between points|

非平坦的集合 聚类在 类有特殊的几何形状 时很有用。例如 非平坦的流形（manifold）.这时 标准的欧式距离就不适用了。 




![](http://images.cnblogs.com/cnblogs_com/breezedeus/201210/20121030193513419.jpg)




##### Feature Select


###### 特征哈希（Feature Hashing）
[传送](http://breezedeus.github.io/2014/11/20/breezedeus-feature-hashing.html)

在特征处理（Feature Processing）中我介绍了利用笛卡尔乘积的方法来构造组合特征。这种方法虽然简单，但麻烦的是会使得特征数量爆炸式增长。比如一个可以取N个不同值的类别特征，与一个可以去M个不同值的类别特征做笛卡尔乘积，就能构造出N*M个组合特征。

特征太多这个问题在具有个性化的问题里尤为突出。如果把用户id看成一个类别特征，那么它可以取的值的数量就等于用户数。把这个用户id特征与其他特征做笛卡尔积，就能产生庞大的特征集。**做广告算法的公司经常宣称自己模型里有几十上百亿的特征，基本都是这么搞出来的**。

当然，特征数量多的问题自古有之，目前也已经有很多用于降维的方法。比如聚类、PCA等都是常用的降维方法1。但这类方法在特征量和样本量很多的时候本身就计算量很大，所以对大问题也基本无能为力。

本文介绍一种很简单的降维方法——特征哈希（Feature Hashing）法2 3。


> 特征哈希法的目标是把原始的高维特征向量压缩成较低维特征向量，且尽量不损失原始特征的表达能力。

记哈希前的特征向量为x∈$R^N$。我们要把这个原始的N维特征向量压缩成M维（M < N）。 记h(n):{1,…,N}→{1,…,M}为一个选定的均匀哈希函数，而ξ(n):{1,…,N}→{−1,1}为另一个选定的均匀哈希函数。h(n)和ξ(n)是独立选取的，它们没关系。按下面方式计算哈希后的M维新特征向量ϕ∈$R^M$的第i个元素值（ϕ是依赖于x的，所以有时候也把ϕ写成ϕ(x)）：

$$\phi_i = \sum_{j:\ h(j)=i} \xi(j) \ x_j  $$

可以证明，按上面的方式生成的新特征ϕ在概率意义下保留了原始特征空间的内积，以及距离2：

$$
x^T x' \approx \phi^T \phi' \ \ \text{，} \\
	\parallel x - x' \parallel \approx\parallel \phi - \phi' \parallel \ \ \text{，}
$$

其中x和x′为两个原始特征向量，而ϕ和ϕ′为对应的哈希后的特征向量。

利用上面的哈希方法把x转变成ϕ后，就可以直接把ϕ用于机器学习算法了。这就是利用特征哈希法来降低特征数量的整个过程。需要说的是，这里面的两个哈希函数h和ξ并不要求非要是把整数哈希成整数，其实它们只要能把原始特征均匀哈希到新特征向量上就行。例如在NLP里，每个特征代表一个单词，那么只要保证h和ξ把单词均匀哈希到{1,…,M}和{−1,1}就行。

下面具体说明如何把特征哈希法应用于多任务学习（multitask learning）问题。所谓多任务学习，就是同时求解多个问题。个性化问题就是一种典型的多任务学习问题，它同时学习多个用户的兴趣偏好。

在世纪佳缘我们使用Logistic Regression (LogReg) 模型学习每个男性用户的交友兴趣以便预测他给具有某些特征的女性的发信概率。这时候学习一个男性用户的交友兴趣就是一个学习任务。记男性用户集合为U，抽取出的女性特征维度为d。我们为每个用户u∈U学习一组参数wu∈$R^d$。再加上一组全局参数w0，总共有N≜d⋅(1+∣U∣)个参数。这种表达方式就是把男性用户id与所有特征x做了笛卡尔积。下图给出了一个有3个用户且x长度为2时扩展后各个用户对应特征向量的示例图。LogReg模型通过计算$(w0+wu)^Tx$来获得最终的预测概率值。


![](http://breezedeus.github.io/images/feature_hashing1.png)

这个问题也可以转化到特征哈希后的空间来看。我们为每个用户引入一个不同的转换函数$ϕ_u(x)$。一般取$ϕ_u(x)=ϕ((u,x))$即可。那么用户u对应的扩展向量通过哈希转换后为

$$ x^h_u \triangleq \phi_0(x)+\phi_u(x) $$

扩展向量对应的权重参数$[w^T_0,…,w^T_{∣U∣}]$通过哈希转换后为:

$$ w^h \triangleq \phi_0(w_0) + \sum_{u \in \bf{U}} \phi_u(w_u)$$

那么在哈希转换后的空间里，
$$ (w^h)^T x^h_u \ = \left(\phi_0(w_0) + \sum_{u \in \bf{U}} \phi_u(w_u)\right)^T \left(\phi_0(x)+\phi_u(x)\right) \\
\approx \phi_0(w_0)^T \phi_0(x) + \phi_u(w_u)^T \phi_u(x) \\
\approx w_0^T x + w_u^T x \\
= (w_0 + w_u)^T x $$

这从理论上证明了特征哈希可用于此多任务学习问题。 上面公式中第一个近似等式利用了不同任务之间哈希转换后的参数ϕu(wu)与特征ϕu′(x)近似不相关2的结论，即：
$$ \phi_u(w_u)^T \phi_{u'}(x) \approx 0 , \ \forall u \neq u'  $$


具体实现算法时，我们并不需要关心wh，只需要把原始特征x通过哈希转换成xhu即可。剩下的就是标准机器学习流程了。

特征哈希法可以降低特征数量，从而加速算法训练与预测过程，以及降低内存消耗；但代价是通过哈希转换后学习的模型变得很难检验，我们很难对训练出的模型参数做出合理解释。特征哈希法的另一个问题是它会把多个原始特征哈希到相同的位置上，出现哈希里的collision现象。但实际实验表明这种collision对算法的精度影响很小3。

最后，总结下特征哈希法相对于其他机器学习降维算法的优势：

  1.  实现简单，所需额外计算量小；
  2.  可以添加新的任务（如新用户），或者新的原始特征而保持哈希转换后的特征长度不变，很适合任务数频繁变化的问题（如个性化推荐里新用户，新item的出现）；
  3.  可以保持原始特征的稀疏性，既然哈希转换时只有非0原始特征才起作用；
  4.  可以只哈希转换其中的一部分原始特征，而保留另一部分原始特征（如那些出现collision就会很影响精度的重要特征）。

###### References

    Trevor Hastie et al. The Elements of Statistical Learning, 2001. ↩

    Kilian Weinberger et al. Feature Hashing for Large Scale Multitask Learning, 2010. ↩ ↩2 ↩3

    Joshua Attenberg et al. Collaborative Email-Spam Filtering with the Hashing-Trick, 2009. ↩ ↩2




