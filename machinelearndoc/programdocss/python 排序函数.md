
python 排序函数

[传送][0]

[toc]
#####一、简介
python对容器内数据的排序有两种，一种是容器自己的sort函数，一种是内建的sorted函数。

sort函数和sorted函数唯一的不同是，sort是在容器内排序，sorted生成一个新的排好序的容器。


对于一个简单的数组 L=[5,2,3,1,4].

sort: L.sort()

#####二、sorted(...)
 sorted(iterable, cmp=None, key=None, reverse=False) --> new sorted list

* iterable：
  待排序的可迭代类型的容器;

* cmp：
  用于比较的函数，比较什么由key决定,有默认值，迭代集合中的一项;

* key：
  用列表元素的某个已命名的属性或函数（只有一个参数并且返回一个用于排序的值）作为关键字，有默认值，迭代集合中的一项;

* reverse：
  排序规则. reverse = True 或者 reverse = False，有默认值。
返回值：是一个经过排序的可迭代类型，与iterable一样。

#####三、实例
如果是一个多维的列表 L=[(‘b’,2),(‘a’,1),(‘c’,3),(‘d’,4)].
有三种选择对这个多维列表进行排序

* 利用cmp函数
```python
    sorted(L, cmp=lambda x,y:cmp(x[1],y[1]))
    L.sort(cmp=lambda x,y:cmp(x[1],y[1]))
```

* 利用key
```python
    sorted(L, key=lambda x:x[1]);
    L.sort(key=lambda x:x[1]);
```

* 反序
```python
    以上几种排序均可加上参数reverse.
    例如 sorted(reverse=True), L.sort(reverse=True). 或者改成False
```

#####四、OrderedDict
 是collections中的一个包，能够记录字典元素插入的顺序，常常和排序函数一起使用来生成一个排序的字典。

比如，比如一个无序的字典
```python
d = {‘banana’:3,’apple’:4,’pear’:1,’orange’:2}
```
通过排序来生成一个有序的字典，有以下几种方式
```python
collections.OrderedDict(sorted(d.items(),key = lambda t:t[0]))
```
或者
```python
collections.OrderedDict(sorted(d.items(),key = lambda t:t[1]))
```
或者
```python
collections.OrderedDict(sorted(d.items(),key = lambda t:len(t[0])))
```

#####五、各种排序方法汇聚

我们知道Python的内置dictionary数据类型是无序的，通过key来获取对应的value。可是有时我们需要对dictionary中 的item进行排序输出，可能根据key，也可能根据value来排。到底有多少种方法可以实现对dictionary的内容进行排序输出呢？下面摘取了 一些精彩的解决办法。 

######1
```python
#最简单的方法，这个是按照key值排序： 
def sortedDictValues1(adict): 
	items = adict.items() 
	items.sort() 
	return [value for key, value in items] 
```
######2

```python
#又一个按照key值排序，貌似比上一个速度要快点 
def sortedDictValues2(adict): 
	keys = adict.keys() 
	keys.sort() 
	return [dict[key] for key in keys] 
```

######3
```python
#还是按key值排序，据说更快。。。而且当key为tuple的时候照样适用 
def sortedDictValues3(adict): 
	keys = adict.keys() 
	keys.sort() 
	return map(adict.get, keys) 
```

######4
```python
#一行语句搞定： 
[(k,di[k]) for k in sorted(di.keys())] 
```

######5
```python
#来一个根据value排序的，先把item的key和value交换位置放入一个list中，再根据list每个元素的第一个值，即原来的value值，排序： 
def sort_by_value(d): 
items=d.items() 
backitems=[[v[1],v[0]] for v in items] 
backitems.sort() 
return [ backitems[i][1] for i in range(0,len(backitems))] 
```

######6
```python
#还是一行搞定： 
[ v for v in sorted(di.values())] 
```

######7
```python
#用lambda表达式来排序，更灵活： 
sorted(d.items(), lambda x, y: cmp(x[1], y[1])), 或反序： 
sorted(d.items(), lambda x, y: cmp(x[1], y[1]), reverse=True) 
```

######8
```python
#用sorted函数的key= 参数排序： 
# 按照key进行排序 
print sorted(dict1.items(), key=lambda d: d[0]) 
# 按照value进行排序 
print sorted(dict1.items(), key=lambda d: d[1]) 
```

######9

```python
下面给出python内置sorted函数的帮助文档： 
sorted(...) 
sorted(iterable, cmp=None, key=None, reverse=False) --> new sorted list 
```

看了上面这么多种对dictionary排序的方法，其实它们的核心思想都一样，即把dictionary中的元素分离出来放到一个list中，对list排序，从而间接实现对dictionary的排序。这个“元素”可以是key，value或者item。 

######10

```python
#按照value排序可以用 
sorted(d.items, key=lambda d:d[1]) 
#若版本低不支持sorted 
#将key,value 以tuple一起放在一个list中 
l = [] 
l.append((akey,avalue))... 
#用sort（） 
l.sort(lambda a,b :cmp(a[1],b[1]))(cmp前加“-”表示降序排序)
```

















[0]:http://www.cnblogs.com/linyawen/archive/2012/03/15/2398302.html