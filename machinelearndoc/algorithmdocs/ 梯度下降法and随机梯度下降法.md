 梯度下降法and随机梯度下降法
分类： 机器学习 matlab 2012-11-04 23:03 5051人阅读 评论(3) 收藏 举报

1. 梯度下降法

梯度下降法的原理可以参考：斯坦福机器学习第一讲。

我实验所用的数据是100个二维点。

如果梯度下降算法不能正常运行，考虑使用更小的步长(也就是学习率)，这里需要注意两点：

1）对于足够小的,  能保证在每一步都减小；
2）但是如果太小，梯度下降算法收敛的会很慢；

总结：
1）如果太小，就会收敛很慢；
2）如果太大，就不能保证每一次迭代都减小，也就不能保证收敛；
如何选择-经验的方法：
..., 0.001, 0.003, 0.01, 0.03, 0.1, 0.3, 1...
约3倍于前一个数。

matlab源码：

[cpp] view plaincopy

    function [theta0,theta1]=Gradient_descent(X,Y);  
    theta0=0;  
    theta1=0;  
    t0=0;  
    t1=0;  
    while(1)  
        for i=1:1:100 %100个点  
            t0=t0+(theta0+theta1*X(i,1)-Y(i,1))*1;  
            t1=t1+(theta0+theta1*X(i,1)-Y(i,1))*X(i,1);  
        end  
        old_theta0=theta0;  
        old_theta1=theta1;  
        theta0=theta0-0.000001*t0 %0.000001表示学习率  
        theta1=theta1-0.000001*t1  
        t0=0;  
        t1=0;  
        if(sqrt((old_theta0-theta0)^2+(old_theta1-theta1)^2)<0.000001) % 这里是判断收敛的条件，当然可以有其他方法来做  
            break;  
        end  
    end  



2. 随机梯度下降法

随机梯度下降法适用于样本点数量非常庞大的情况，算法使得总体向着梯度下降快的方向下降。

matlab源码：

[cpp] view plaincopy

    function [theta0,theta1]=Gradient_descent_rand(X,Y);  
    theta0=0;  
    theta1=0;  
    t0=theta0;  
    t1=theta1;  
    for i=1:1:100  
        t0=theta0-0.01*(theta0+theta1*X(i,1)-Y(i,1))*1  
        t1=theta1-0.01*(theta0+theta1*X(i,1)-Y(i,1))*X(i,1)  
        theta0=t0  
        theta1=t1  
    end  
    




[zouxy09@qq.com](http://blog.csdn.net/zouxy09)
三、Python实现

我使用的Python是2.7.5版本的。附加的库有Numpy和Matplotlib。具体的安装和配置见前面的博文。在代码中已经有了比较详细的注释了。不知道有没有错误的地方，如果有，还望大家指正（每次的运行结果都有可能不同）。里面我写了个可视化结果的函数，但只能在二维的数据上面使用。直接贴代码：

logRegression.py

?
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
44
45
46
47
48
49
50
51
52
53
54
55
56
57
58
59
60
61
62
63
64
65
66
67
68
69
70
71
72
73
74
75
76
77
78
79
80
81
82
83
84
85
86
87
88
89
90
91
92
93
94
95
	
<strong>#################################################
# logRegression: Logistic Regression
# Author : zouxy
# Date   : 2014-03-02
# HomePage : http://blog.csdn.net/zouxy09
# Email  : zouxy09@qq.com
#################################################
 
from numpy import *
import matplotlib.pyplot as plt
import time
 
 
# calculate the sigmoid function
def sigmoid(inX):
    return 1.0 / (1 + exp(-inX))
 
 
# train a logistic regression model using some optional optimize algorithm
# input: train_x is a mat datatype, each row stands for one sample
#        train_y is mat datatype too, each row is the corresponding label
#        opts is optimize option include step and maximum number of iterations
def trainLogRegres(train_x, train_y, opts):
    # calculate training time
    startTime = time.time()
 
    numSamples, numFeatures = shape(train_x)
    alpha = opts['alpha']; maxIter = opts['maxIter']
    weights = ones((numFeatures, 1))
 
    # optimize through gradient descent algorilthm
    for k in range(maxIter):
        if opts['optimizeType'] == 'gradDescent': # gradient descent algorilthm
            output = sigmoid(train_x * weights)
            error = train_y - output
            weights = weights + alpha * train_x.transpose() * error
        elif opts['optimizeType'] == 'stocGradDescent': # stochastic gradient descent
            for i in range(numSamples):
                output = sigmoid(train_x[i, :] * weights)
                error = train_y[i, 0] - output
                weights = weights + alpha * train_x[i, :].transpose() * error
        elif opts['optimizeType'] == 'smoothStocGradDescent': # smooth stochastic gradient descent
            # randomly select samples to optimize for reducing cycle fluctuations
            dataIndex = range(numSamples)
            for i in range(numSamples):
                alpha = 4.0 / (1.0 + k + i) + 0.01
                randIndex = int(random.uniform(0, len(dataIndex)))
                output = sigmoid(train_x[randIndex, :] * weights)
                error = train_y[randIndex, 0] - output
                weights = weights + alpha * train_x[randIndex, :].transpose() * error
                del(dataIndex[randIndex]) # during one interation, delete the optimized sample
        else:
            raise NameError('Not support optimize method type!')
     
 
    print 'Congratulations, training complete! Took %fs!' % (time.time() - startTime)
    return weights
 
 
# test your trained Logistic Regression model given test set
def testLogRegres(weights, test_x, test_y):
    numSamples, numFeatures = shape(test_x)
    matchCount = 0
    for i in xrange(numSamples):
        predict = sigmoid(test_x[i, :] * weights)[0, 0] > 0.5
        if predict == bool(test_y[i, 0]):
            matchCount += 1
    accuracy = float(matchCount) / numSamples
    return accuracy
 
 
# show your trained logistic regression model only available with 2-D data
def showLogRegres(weights, train_x, train_y):
    # notice: train_x and train_y is mat datatype
    numSamples, numFeatures = shape(train_x)
    if numFeatures != 3:
        print "Sorry! I can not draw because the dimension of your data is not 2!"
        return 1
 
    # draw all samples
    for i in xrange(numSamples):
        if int(train_y[i, 0]) == 0:
            plt.plot(train_x[i, 1], train_x[i, 2], 'or')
        elif int(train_y[i, 0]) == 1:
            plt.plot(train_x[i, 1], train_x[i, 2], 'ob')
 
    # draw the classify line
    min_x = min(train_x[:, 1])[0, 0]
    max_x = max(train_x[:, 1])[0, 0]
    weights = weights.getA()  # convert mat to array
    y_min_x = float(-weights[0] - weights[1] * min_x) / weights[2]
    y_max_x = float(-weights[0] - weights[1] * max_x) / weights[2]
    plt.plot([min_x, max_x], [y_min_x, y_max_x], '-g')
    plt.xlabel('X1'); plt.ylabel('X2')
    plt.show()</strong>

四、测试结果

测试代码：

test_logRegression.py

?
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
	
#################################################
# logRegression: Logistic Regression
# Author : zouxy
# Date   : 2014-03-02
# HomePage : http://blog.csdn.net/zouxy09
# Email  : zouxy09@qq.com
#################################################
 
from numpy import *
import matplotlib.pyplot as plt
import time
 
def loadData():
    train_x = []
    train_y = []
    fileIn = open('E:/Python/Machine Learning in Action/testSet.txt')
    for line in fileIn.readlines():
        lineArr = line.strip().split()
        train_x.append([1.0, float(lineArr[0]), float(lineArr[1])])
        train_y.append(float(lineArr[2]))
    return mat(train_x), mat(train_y).transpose()
 
 
## step 1: load data
print "step 1: load data..."
train_x, train_y = loadData()
test_x = train_x; test_y = train_y
 
## step 2: training...
print "step 2: training..."
opts = {'alpha': 0.01, 'maxIter': 20, 'optimizeType': 'smoothStocGradDescent'}
optimalWeights = trainLogRegres(train_x, train_y, opts)
 
## step 3: testing
print "step 3: testing..."
accuracy = testLogRegres(optimalWeights, test_x, test_y)
 
## step 4: show the result
print "step 4: show the result..." 
print 'The classify accuracy is: %.3f%%' % (accuracy * 100)
showLogRegres(optimalWeights, train_x, train_y)

测试数据是二维的，共100个样本。有2个类。如下：

testSet.txt

?
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
44
45
46
47
48
49
50
51
52
53
54
55
56
57
58
59
60
61
62
63
64
65
66
67
68
69
70
71
72
73
74
75
76
77
78
79
80
81
82
83
84
85
86
87
88
89
90
91
92
93
94
95
96
97
98
99
100
	
-0.017612   14.053064   0
-1.395634   4.662541    1
-0.752157   6.538620    0
-1.322371   7.152853    0
0.423363    11.054677   0
0.406704    7.067335    1
0.667394    12.741452   0
-2.460150   6.866805    1
0.569411    9.548755    0
-0.026632   10.427743   0
0.850433    6.920334    1
1.347183    13.175500   0
1.176813    3.167020    1
-1.781871   9.097953    0
-0.566606   5.749003    1
0.931635    1.589505    1
-0.024205   6.151823    1
-0.036453   2.690988    1
-0.196949   0.444165    1
1.014459    5.754399    1
1.985298    3.230619    1
-1.693453   -0.557540   1
-0.576525   11.778922   0
-0.346811   -1.678730   1
-2.124484   2.672471    1
1.217916    9.597015    0
-0.733928   9.098687    0
-3.642001   -1.618087   1
0.315985    3.523953    1
1.416614    9.619232    0
-0.386323   3.989286    1
0.556921    8.294984    1
1.224863    11.587360   0
-1.347803   -2.406051   1
1.196604    4.951851    1
0.275221    9.543647    0
0.470575    9.332488    0
-1.889567   9.542662    0
-1.527893   12.150579   0
-1.185247   11.309318   0
-0.445678   3.297303    1
1.042222    6.105155    1
-0.618787   10.320986   0
1.152083    0.548467    1
0.828534    2.676045    1
-1.237728   10.549033   0
-0.683565   -2.166125   1
0.229456    5.921938    1
-0.959885   11.555336   0
0.492911    10.993324   0
0.184992    8.721488    0
-0.355715   10.325976   0
-0.397822   8.058397    0
0.824839    13.730343   0
1.507278    5.027866    1
0.099671    6.835839    1
-0.344008   10.717485   0
1.785928    7.718645    1
-0.918801   11.560217   0
-0.364009   4.747300    1
-0.841722   4.119083    1
0.490426    1.960539    1
-0.007194   9.075792    0
0.356107    12.447863   0
0.342578    12.281162   0
-0.810823   -1.466018   1
2.530777    6.476801    1
1.296683    11.607559   0
0.475487    12.040035   0
-0.783277   11.009725   0
0.074798    11.023650   0
-1.337472   0.468339    1
-0.102781   13.763651   0
-0.147324   2.874846    1
0.518389    9.887035    0
1.015399    7.571882    0
-1.658086   -0.027255   1
1.319944    2.171228    1
2.056216    5.019981    1
-0.851633   4.375691    1
-1.510047   6.061992    0
-1.076637   -3.181888   1
1.821096    10.283990   0
3.010150    8.401766    1
-1.099458   1.688274    1
-0.834872   -1.733869   1
-0.846637   3.849075    1
1.400102    12.628781   0
1.752842    5.468166    1
0.078557    0.059736    1
0.089392    -0.715300   1
1.825662    12.693808   0
0.197445    9.744638    0
0.126117    0.922311    1
-0.679797   1.220530    1
0.677983    2.556666    1
0.761349    10.693862   0
-2.168791   0.143632    1
1.388610    9.341997    0
0.317029    14.739025   0