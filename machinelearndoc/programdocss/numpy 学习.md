numpy 学习

```python
a = arange(5,dtype=float32)

a.dtype

m = array([arange(2),arange(2)])
m.shape
(2,2)

#自定义数据类型
t = dtype([('name',str_,40),('numitems',int32),('price',float32)])

itemz = array([('Meaning of life DVD',42,3.14),('Butter',13,2.72)],dtype=t)

# 索引
a[3:7]   # 切片
a[:7:2]  # 步长为2
a[::-1]  # 翻转

# 
b = arange(24).reshape(2,3,4)

#改变数组的维度
#展平数组
b.ravel()
b.flatten()

# 设置数组的维度
b.shape = (6,4)   # 直接改变数组维度
b.resize((2,12))  # 直接改变数组纬度
b = b.reshape((2,12)) 
b.transpose()

# 数组的组合
# numpy 数组有水平组合， 垂直组合 和 深度组合 

# 水平组合
a = arange(9).reshape(3,3)
b = 2 * a

hstack((a,b))
concatenate((a,b),axis=1)

# 垂直组合
vstack((a,b))
concatenate((a,b),axis=0)

# 深度组合
dstack((a,b))

# 列组合 与 hstack 相同
clumn_stack((a,b))

# 比较两个数组是否相同
colum_stack((a,b)) == hstack((a,b))  # 返回的是布尔矩阵

# 行组合 与vstack相同
row_stack((a,b))
row_stack((a,b)) = vstack((a,b))

# 分割数组
a = arange(9).reshape((3,3))
hsplit(a,3)
split(a,3,axis=1)
vsplit(a,3)
split(a,3,axis=0)

# 数组的属性
b.ndim    # 数组的维数
b.size    # 数组元素的总个数
b.itemsize  # 数组元素在内存总占的字节数
b.T       # 数组的转秩矩阵
b.real    # 数组的实数部分
b.imag    # 数组的虚数部分

b.flat    # 返回一个numpy.flatiter 对象， 就是数组的扁平迭代器

b.tolist() # 讲数组转换成普通的pythonlist
b.astype(int)  # 转换数组元素的类型


# 读取文件
# csv 文件
c,v = np.loadtxt('data.csv',delimiter=',',usecols=(6,7),unpack=True)

# 计算加权平均数
wavge = np.average(c,weights=v)

# 计算算数平均数
np.mean(c)

# 计算时间平均价格
t = np.arange(len(c))
tavge = np.average(c,weights=t)

# 最大值 和 最小值
np.max(h)
np.min(l)

# 计算极差
np.ptp(h)
np.ptp(l)

# 计算中位数
np.median(c)

# 排序
np.msort(c)

# 计算方差
np.var(c)

# 计差分
np.diff(c)

# where 返回指定条件的索引数组
pos_indexes = np.where(returns > 0)

# 日期分析
# 数据列和转换函数之间的映射  参数converters

def datestr2num(s):
	return datetime.datetime.strptime(
    s,"%d-%m-%Y").date().weekday()
    
dates,close = np.loadtxt('data.csv',delimiter=',',usecols=(1,6),
      converters={1:datestr2num},unpack=True)
      
# take 函数配合 where 函数 提取指定索引的项
indices = np.where(dates == i)
prices = np.take(close,indices)


#取出最大项和最小项的索引值
max_idx = np.argmax(averages)
min_idx = np.argmax(averages)

# 找到第一个星期一
first_monday = np.ravel(np.where(dates == 0))[0]
# 找到最后一个星期五
last_friday = np.ravel(np.where(dates == 4))[-2]

# 按照每个子数组 5 个元素， 用split 函数切分数组
weeks_indices = np.split(weeks_indices,5)

# 沿着坐标轴执行某个函数  #指定要执行的函数， 指定坐标轴， 可变数目的函数的参数
weeksummary = np.apply_along_axis(summarize,1,weeks_indices,open,high,low,close)

def summarize(a,o,h,l,c):
	monday_open = o[a[0]]
    week_high = np.max( np.take(h,a))
    week_low  = np.min( np.take(l,a))
    friday_close = c[a[-1]]
    
    return ("APPL",monday_open,week_high,week_low,friday_close)
    
# 将数据保存到txt文件中
np.savetxt("weeksummary.csv",weeksummary,delimiter=",",fmt="%s")

# 计算卷积
sma = np.convolve(weights,c)

# 返回一个区间内指定间隔数的数组
line = np.linspace(-1,0,5)

weight = np.exp(np.linspace(-1.,0.,N))
weight.sum()

# fill 将一个数组设置为指定的值
# 比 array.flat = scalar 快 
m.fill(sma[i - N - 1])

dev = np.sqrt(np.mean(dev))

# 线性回归  # 得到 系数向量 一个残差数组， A的秩 A 的奇异值
(x,residuals,rank,s) = np.linalg.lstsq(A,b)
print x,residuals,rank,s

def fit_line(t,y):
	A = np.vstack([t,np.ones_like(t)]).T
    return np.linalg.lstsq(A,y)[0]

# 计算点乘积
 np.dot(b,x)
 
# 条件选择和取交集
a1 = c[c > support]
a2 = c[c < resistance]

len(np.intersect1d(a1,a2))

#计算数组中所有元素的乘积
b = np.range(1,9)
b.prod()

# 相关性分析
# 协方差矩阵
covariance = np.cov(bhp_returns, vale_returns)
print "Coverariance", covariance

# 去对角线线上的元素
covariance.diagonal()

# 矩阵的迹 ： 对角线上的元素之和
covariance.trace()

# 计算相关系数
print covariance / (bhp_returns.std() * vale_returns.std())

np.corrcoef(bhp_returns,vale_returns)

# 多项式拟合
ploy = np.ployfit(t,bhp - value,int(sys.argv[1]))
# 预测
p_val = np.ployval(poly,t[-1] + 1)
# 导数
der = np.ployder(poly)
# 导数的根 ， 即多项式的极值点
np.roots(der)
 
# 正负
change = np.diff(c)
signs = np.sign(change)

pieces = np.piecewise(change,[change < 0,change >0],[-1,1])

# 避免使用循环
相当于 map函数

def calc_profit(open,high,low,close):
	#
    buy = open * float(sys.argc[1])
    if low < buy < high:
    	return (close - buy ) / buy
    else:
    	return 0
    print "Profits:",profits
    
func = np.vectorize(calc_profit)

profits = func(o,h,l,c)

# 矩阵
 A = np.mat('1 2 3; 4 5 6;7 8 9')
 B = np.matrix(data,copy=False)
 
 A.T
 
 A.I
 
 A = np.eye(2)
 
 # 通用函数
 def ultimate_answer(a):
 	result = np.zero_like(a)
    result.flat = 42
    return result
    
 ufunc = np.frompyfunc(ultimate_answer,1,1)  # 创建通用函数， 指定输入参数的个数为1，输出个数为1
 print "the answer" , ufunc(np.arange(4))
 
随机数
cash = np.zeros(1000)
cash[0] = 1000
outcome = np.random.binomial(9,0.5,size=len(cash))


## 深入学习Numpy模块

# 1. 线性代数 numpy.linalg  

# 计算逆矩阵
A = np.mat("0 1 2;1 0 3;4 -3 8")
inverse = np.linalg.inv(A)

# 求解线性方程组
A = np.mat("1 -2 1;0 2 -8;-4 5 9")
b = np.array([0,8,-9])

x = np.linalg.solve(A,b)

# 特征值和特征向量
A=np.mat("3 -2;1 0")
# 调用eigvals 函数求解特征值
print "Eigenvalues",np.linalg.eigvals(A)

# 使用eig函数求解特征值和特征向量，返回一个元祖， 存放着特征值和特征向量
eigenvalues,eigenvectors = np.linalg.eig(A)
print "First tuple of eig",eigenvalues
print "Second tuple of eig",eigenvectors

# 奇异值分解
# SVD 
A = np.mat("4 11 14;8 7 -2")

U,sigma,V = np.linalg.svd(A,full_matrices=False)

E = np.diag(Sigma)

# 计算广义逆矩阵 ， pinv
# inv矩阵要求输入是方阵， pinv要求输入是非方阵
A = np.mat("4 11 14;8 7 -2")

pseudoinv = np.linalg.pinv(A)

# 计算行列式
A = np.mat("3 4;5 6")
det = np.linalg.det(A)


# 绘制正态分布图
import numpy as np
import matplotlib.pyplot as plt

N=10000

normal_values = np.random.normal(size=N)
dummy,bins,dummy = plt.hist(normal_values,np.sqrt(N),normed=True,lw=1)
sigma=1
mu=0
plt.plot(bins,1/(sigma*np.sqrt(2 * np.pi))*np.exp(-(bins-mu)**2)/(2 * sigma**2),lw=2)
plt.show()


### 使用matplot 绘图

## 1. 绘制多项式图

import numpy as np
import matplotlib.pyplot as plt

func = np.ploy1d(np.array([1,2,3,4]).astype(float))
x = np.linespace(-10,10,30)
y = func(x)

plt.plot(x,y)
plt.xlabel('x')
plt.ylabel('y(x)')
plt.show()


## 绘制多项式函数以及其导数

import numpy as np
import matplotlib.pyplot as plt

func = np.ploy1d(np.array([1,2,3,4]).astype(float))
func1 = func.deriv(m=1)

x = np.linspace(-10,10,30)
y = func(x)
y1 = func1(x)

plt.plot(x,y,'ro',x,y1,'g--')
plt.xlabel('x')
plt.ylabel('y')
plt.show()


## 子图
import numpy as np
import matplotlib.pyplot as plt

func = np.ploy1d(np.array([1,2,3,4]).astype(float))
x = np.linspace(-10,10,30)
y = func(x)
func1 = func.derive(m=1)
y1=func1(x)
func2=func.derive(m=2)
y2=func2(x)

plt.subplot(311)
plt.plot(x,y,'r-')
plt.title("Polynomial")

plt.subplot(312)
plt.plot(x,y1,'b^')
plt.title("First Derivative")
plt.subplot(313)
plt.plot(x,y2,'go')
plt.title("Second Derivative")
plt.xlabel('x')
plt.ylabel('y')


### 绘制全年股票价格
from matplotlib.dates import DateFormatter

























```