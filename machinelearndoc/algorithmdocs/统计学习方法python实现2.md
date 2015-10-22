
###统计学习方法python实现2
(2013-08-30 23:56:48)

[toc]
####第三章 k邻近法
```python
#算法3.1 k邻近算法
#一维
#多维算法为k-d树算法：见https://code.google.com/p/python-kdtree/downloads/list
#子算法：找到一个数组元素最接近的k个元素
#输入 列表，待寻找目标位置j
def findKElement(list,toFind,k):
    print (toFind)
    result=[]
    tempList=sorted(list)
    P=[]
    i=tempList.index(toFind)
    print (i)
    if i-k<</span>0:
        P=[999999]
        P.extend(tempList[0:i])
    else:
        P.extend(tempList[i-k:i])
    Q=[]
    if i+k>=len(tempList):
        Q=[9999999]
        Q.extend(reversed(tempList[i:len(tempList)]))
    else:
        Q.extend(reversed(tempList[i:i+k]))
    print (P,Q)
    while 1:
        if abs(P[len(P)-1]-toFind)<</span>abs(Q[len(Q)-1]-toFind):
            result.append(P.pop())
        else:
            result.append(Q.pop())
        if len(result)==k:
            break
    print (result)
    return result

def K_near(aDic,k,tofind):
    kkeyarray=findKElement(list(aDic.keys()),tofind,k)
    array=[aDic[kkeyarray[i]] for i in range(len(kkeyarray))]
    coutDict={}
    for i in range(len(array)):
        coutDict[array.count(array[i])]=array[i]
    maxCount=max(list(coutDict.keys()))
    return coutDict[maxCount]
```

####第四章 朴素贝叶斯

```python
#这里介绍使用nltk中的朴素贝叶斯算法
from nltk.corpus import names
import random


#特征提取器'
#特征提取器的结果是一个描述特征的字典'
def gender_feature(word):
    return {'last_letter':word[-1]}


corpus_name=([(name,'male') for name in names.words('male.txt')]+[(name,'female') for name in names.words('female.txt')])
random.shuffle(corpus_name)
featuresets=[(gender_feature(n),g) for (n,g) in corpus_name]
train_set,test_set=featuresets[:500],featuresets[500:]

#输入是 labeled_featuresets: A list of classified featuresets,i.e., a list of tuples ``(featureset, label)``.'
classifier=nltk.NaiveBayesClassifier.train(train_set)
# print(classifier.classify(gender_feature('hellen')))
print(nltk.classify.accuracy(classifier,train_set))
print(nltk.classify.accuracy(classifier,test_set))
classifier.show_most_informative_features(10)
```

####第五章 决策树算法

```python
# 介绍使用nltk中的决策树算法
'使用连续分类器进行词性标注'
def pos_features(sentence,i, history):
    features = {"suffix(1)": sentence[i][-1:],"suffix(2)": sentence[i][-2:],"suffix(3)": sentence[i][-3:]}
    if i ==0:
        features["prev-word"] = ""
        features["prev-tag"] = ""
    else:
        features["prev-word"] = sentence[i-1]
        features["prev-tag"] = history[i-1]
    return features



tagged_words = brown.tagged_words(categories='news')
print(len(tagged_words))
common_suffixes=find_common_suffixes()
featuresets =[(pos_features(n,common_suffixes), g) for (n,g) in tagged_words]
size = int(len(featuresets) *0.1)
# train_set, test_set = featuresets[size:], featuresets[:size]
train_set, test_set = featuresets[500:1000], featuresets[:500]
classifier = nltk.DecisionTreeClassifier.train(train_set)
print(nltk.classify.accuracy(classifier,test_set))
print(classifier.classify(pos_features('cats',common_suffixes)))
'可以打印出如何分类的伪代码'
print (classifier.pseudocode(depth=4))
```
