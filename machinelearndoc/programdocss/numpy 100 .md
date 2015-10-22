####numpy 100 

[传送][0]

```python
#1.引入包
import numpy as np

#2.打印版本
print np.__version__
np.__config__.show()

#3. 创建一个 size为10 的空向量
Z = np.zeros(10)

#4. 修改指定位置的值
Z[4] = 1

#5. 创建一个值域范围在 [10,49]的向量
Z = np.arange(10,49)

#6. 创建一个矩阵
Z = np.arange(9).reshape(3,3)

#7. 寻找数组中非零数的下标
nz = np.nonzero([1,2,0,0,4,0])

#8. 创建一个3*3的单位矩阵
Z = np.eye(3)

#9. 创建一个 5 * 5 的矩阵,次主对角线是1234， k = -1 是指对角线下面
Z = np.diag(1 + np.arange(4),k=-1)

#10. 创建一个3*3*3的随机数 数组
Z = np.random.random((3,3,3))

#11. 创建一个 8*8 的矩阵， 并各行赋值
Z = np.zeros((8,8),dtype=int)
Z[1::2,::2] = 1
Z[::2,1::2] = 1
print Z

#12. 创建一个 10*10 的随机数组，并找到最大值和最小值
Z = np.random.random((10,10))
Zmin,Zmax = Z.min(),Z.max()

#13. 使用tile function创建一个 8*8 的checkerboard
Z = np.tile(np.array([[0,1],[1,0]]),(4,4))
print Z

#14. 标准化一个 5*5 的随机矩阵 ( val ~ (0,1))
Z = np.random.random((5,5))
Zmax,Zmin = Z.max(),Z.min()
Z = (Z - Zmin) / (Zmax - Zmin)
print Z

#15. 5*3 的矩阵和 3*2 的矩阵相乘
Z = np.dot(np.ones((5,3)),np.ones((3,2)))

#16. 创建一个 5 * 5 的矩阵 ,行是从 0 到 4 (向量被广播了)
Z = np.zeros((5,5))
Z += np.arange(5)

#17. 生成（0-1）的十个元素的数组 （去掉12个的头尾）
Z = np.linspace(0,1,12,endpoint=True)[1:-1]

#18. 随机创建一个数组，并进行排序
Z = np.random.random(10)
Z.sort()

#19. 判断两个随机数组是否相等
A = np.random.randint(0,2,5)
B = np.random.randint(0,2,5)
equal = np.allclose(A,B)

#20 创建一个长度为30的向量，并求出它的平均值
Z = np.random.random(30)
m = Z.mean()

## Apprentice

#21 将数组的值设置为不可边的
Z = np.zeros(10)
Z.flags.writeable = False
Z[0] = 1

#22 将2 * n 的矩阵转化为极坐标系的值
Z = np.random.random((10,2))
X,Y = Z[:,0],Z[:,1]
R = np.sqrt(X ** 2 + Y ** 2)
T = np.arctan2(Y,X)

#23 替换随机数组的最大值为0,使用 argmax函数
Z = np.random.random(10)
Z[Z.argmax()] = 0

#24 创建一个结构数组， 使用x,y作为坐标系， 覆盖住区域(0,1) * (0,1)
Z = np.zeros((10,10),[('x',float),('y',float)])
Z['x'],Z['y'] = np.meshgrid(np.linespace(0,1,10),
                           np.linspace(0,1,10))

#25 打印各个scalar type的阈值和精度
for dtype in [np.int8,np.int32,np.int64]:
	print np.iinfo(dtype).min
    print np.iinfo(dtype).max

for dtype in [np.float32,np.float64]:
	print np.finfo(dtype).min
    print np.finfo(dtype).max
    print np.finfo(dtype).eps
    
#26 创建一个结构化数组，每个元素 代表一个 位置 和 颜色信息
Z = np.zeros(10,[('position',[('x',float,1),
                              ('y',float,1)]),
                 ('color', [('r',float,1),
                             ('g',flaot,1),
                             ('b',float,1)])])
#27. 计算各个点之间的距离
Z = np.random.random((10,2))
x,y = np.atleast_2d(Z[:,0]), np.atleast_2d(Z[:,1])
D = np.sqrt( (X - X.T)**2 + (Y - Y.T)**2 )
 # 使用scipy更快
 import scipy
 Z = np.random.random((10,2))
 D = scipy.spatial.distance.cdist(Z,Z)
 print D

# 28 生成一个2D的高斯分布
X,Y = np.meshgrid(np.linspace(-1,1,10),np.linspace(-1,1,10))
D = np.sqrt(X*X + Y*Y)
sigma,mu = 1.0,0.0
G = np.exp(-((D - mu)**2 / (2.0 * sigma**2)))

#29.检查2D数组是否有空值
Z = np.random.randint(0,3,(3,10))
print (~Z.any(axis=0)).any()

#30.从给定的数组中获取最近的数值
Z = np.random.uniform(0,1,10)
z = 0.5
m = Z.flat[np.abs(Z - z).argmin()]
print m

## Journeyman
#31.读取以逗号分割的数据文件
Z = np.genfromtxt("missing.dat",delimiter=",")

#32 使用生成器
def generate():
	for x in xrange(10):
    	yield x
Z = np.fromiter(generate(),dtype=float,count=-1)
print Z

#33 使用另一个数组索引 当前数组 并计算
z = np.ones(10)
i = np.random.randint(0,len(z),20)
z += np.bincount(i,minlength=len(z))


#34 使用索引数组 统计向量x，并将结果赋值给f    bincount ? 
X = [1,2,3,4,5,6]
I = [1,3,9,3,4,1]

F = np.bincount(I,X)

#35 考虑一个 (w,h,3) 图片 （dtype = ubyte） ,计算无重颜色的个数

w,h = 16,16
I = np.random.randint(0,2,(h,w,3)).astype(np.ubyte)
F = I[...,0]*256*256 + I[...,1]*256 + I[...,2]
n = len(np.unique(F))
print np.unique(I)

#36 对于一个四维数组， 如何对后两个axis 进行sum统计
A = np.random.randint(0,10,(3,4,3,4))
sum = A.reshape(A.shape[:,-2]+(-1,0)).sum(axis=-1)

#37.使用一个数组作为另一个数组的索引，获取子集，并对子集求均值
D = np.random.uniform(0,1,100)
S = np.random.randint(0,10,100)
D_sums = np.bincount(S,weights=D)
D_means = D_sums / D_counts
print D_means

#38. 向一个数组的每个元素中间插入3个0
Z = np.array([1,2,3,4,5])
nz = 3
Z0 = np.zeros(len(Z) + (lne(Z) - 1) * (nz))
Z0[::nz+1] = Z
print Z0

#39一个三维矩阵 (5,5,3) 与矩阵(5,5)相乘
A = np.ones((5,5,3))
B = 2 * np.ones((5,5))
print A * B[:,:,None]

#40 交换一个数组的两行
A = np.arange(25).reshape(5,5)
A[[0,1]] = A[[1,0]]
print A

#41 
```

















































[0]:https://github.com/zxsted/numpy-100




