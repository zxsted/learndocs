
###统计学习方法python实现1
[address](http://blog.sina.com.cn/s/blog_6b60259a0101dy6n.html)
(2013-08-30 23:52:43)
	

>    Written with StackEdit.

最近复习统计学习方法，决定用python把里头的算法实现一遍，代码拿出来共享。
第二章 感知机

```python  
#算法2.1 感知机的实现
#input  param1 待分类点list,list中为dict形式为{(x1,x2):y}
#       param2 学习率
#output (w,b)
#       f(x)=sign(w*x+b)



# 模型输出
def ganZhiJiResult(dict,w,b):
    x=list(dict.keys())[0]
    y=x[0]*w[0]+x[1]*w[1]+b
    if y>0:
        return 1
    elif y<</span>0:
        return -1
    else:
        return 0

#测试结果
def testResult(dict,w,b):
    return list(dict.values())[0]*ganZhiJiResult(dict,w,b)


def ganZhiJi(inputList,step):
    Queue=[]
    Queue.extend(inputList)
    w=(0,0)
    b=0
    while Queue:
        print('iter')
        item=Queue.pop(0)
        test=testResult(item,w,b)
        if test<=0:
            key=list(item.keys())[0]
            value=list(item.values())[0]
            w=(w[0]+step*key[0]*value,w[1]+step*key[1]*value)
            b=b+step*value
            print(w,b)
            Queue=[]
            Queue.extend(inputList)
    return w,b


if __name__=='__main__':
    testList=[{(3,3):1},{(4,3):1},{(1,1):-1}]
    ganZhiJi(testList,1)
```

```python
#算法2.2 感知机的实现 对偶形式
#input  param1 待分类点list,list中为dict形式为{(x1,x2):y}
#       param2 学习率step
#output (a,b)  w=sum(i=1~N,ai*yi*xi);b=sum(i=1~N,ai*yi)
#       a=(a1,a2```aN),ai=ai+step;b=b+step*yi
#       f(x)=sign(sum(j=1~N,aj*yj*xj*x)+b)
#       由于要用内积先算出Gram矩阵，G=[xi*xj]NXN

def generateG(testList):
    n=len(testList)
    array=[list(testList[i].keys())[0] for i in range(n)]
    G=[[array[i][0]*array[j][0]+array[i][1]*array[j][1] for j in range(n)] for i in range(n)]
    return G

#测试结果
#测试第xi的结果是否误分
def testResult1(i,a,b,testList,G):
    n=len(testList)
    keyarray=[list(testList[i].keys())[0] for i in range(n)]
    valuearray=[list(testList[i].values())[0] for i in range(n)]
    # print (keyarray,valuearray)
    array=[a[j]*valuearray[j]*G[j][i] for j in range(n)]
    return valuearray[i]*(sum(array)+b)


def ganZhiJi2(inputList,step):
    a=[0]*len(inputList)
    b=0
    G=generateG(inputList)
    fail=True
    while fail:
        for i in range(len(inputList)):
            item=inputList[i]
            if testResult1(i,a,b,inputList,G)<=0:
                a[i]=a[i]+step
                b+=step*list(item.values())[0]
                print (a,b)
                break
        else:
            fail=False
    return (a,b)


if __name__=='__main__':
    testList=[{(3,3):1},{(4,3):1},{(1,1):-1}]
    # ganZhiJi(testList,1)
    # print (generateG(testList))
    ganZhiJi2(testList,1)
```
