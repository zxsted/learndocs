Seaborn统计可视化工具的使用

[传送](http://tym1060326.is-programmer.com/posts/47210.html)

[toc]

Seaborn是python中基于matplotlib的统计绘图模块。如果说matplotlib是“tries to make easy things easy and hard things possible”，那么seaborn则是让“hard things”也变简单。


##### 安装

 Seaborn需要python 2.7或者3.3+的环境。并且需要首先安装numpy、scipy、matplotlib和pandas模块，官网还推荐statsmodels和patsy两个模块。这些模块可以在相应官网下到，也可以在http://www.lfd.uci.edu/~gohlke/pythonlibs/网站下载（注意32/64位及对应python版本号）。

Seaborn需要在Github上下载源文件，然后在命令行运行python setup.py install即可安装到python的安装目录下。使用seaborn需要导入seaborn模块。

```python
import seaborn as sns
```

##### 绘图风格

 Seaborn和matplotlib的区别在于，seaborn把绘图风格参数与数据参数分开设置。这样我们可以轻易的改变图像的风格。Seaborn有两组函数对风格进行控制：axes_style()/set_style()函数和plotting_context()/set_context()函数。axes_style()函数和plotting_context()函数返回参数字典，set_style()函数和set_context()函数设置matplotlib。

首先介绍第一组*_style()函数。

Seaborn有5中预定义的主题：darkgrid（灰色背景+白网格）, whitegrid（白色背景+黑网格）, dark（仅灰色背景）, white（仅白色背景）和ticks（坐标轴带刻度）。默认的主题是darkgrid，修改主题可以使用set_style()函数。

```python
sns.set_style('white')
```

 建议在绘制大量数据元素时，使用whitegrid主题；如果想突出带有固定模式的数据时，建议不使用网格，即dark/white主题；在体现少量特殊的数据元素结构时，使用ticks主题。

如果不需要右方和上方的坐标轴，可以使用despine()函数去掉。注意，使用despine()函数需要在画数据元素之后使用，并且despine()函数只有在使用white或ticks主题时，才会有效果。另外，despine()函数可以选择哪些坐标轴被隐藏。

```python
sns.despine(left=True, bottom=True, right=False, top=False)
```
 offset_spines()可以使坐标轴移开一些，远离数据元素。

使用axes_style()可以方便的临时改变绘图主题。

```python
with sns.axes_style('darkgrid'):
	plt.subplot(211)
    plt.plot(x,y)
```

如果希望改变默认绘图主题的风格，可以将字典作为参数rc传入*_style()函数中。也可以使用sns.set_style(‘darkgrid’，rc)改变指定主题的风格参数。可以改变的风格参数列表可以通过sns.axes_style()的返回值得到。style的键值对列表的示例如下。

```python


    axes.labelcolor          .15
    axes.grid                True
    axes.axisbelow           True
    axes.edgecolor           white
    axes.linewidth           0
    axes.facecolor           #EAEAF2
    font.family              Arial
    grid.color               white
    grid.linestyle           -
    image.cmap               Greys
    legend.frameon           False
    legend.scatterpoints     1
    legend.numpoints         1
    lines.solid_capstyle     round
    pdf.fonttype             42
    text.color               .15
    xtick.color              .15
    ytick.color              .15
    xtick.direction          out
    ytick.direction          out

    xtick.major.size         0
    ytick.major.size         0
    xtick.minor.size         0
    ytick.minor.size         0

```



###### 下面介绍第二组*_context()函数。

 上下文（context）可以设置输出图片的大小尺寸（scale）。Seaborn中预定义的上下文有4种：paper、notebook、talk和poster。默认使用notebook上下文。
```python
    sns.set_context(‘paper’)
```
上例将上下文设置为paper，输出为最小的尺寸。

同样，plotting_context()函数也可以在with关键字后临时改变绘图风格，也可以传入字典参数，改变默认设置。
```python
    sns.set_context(‘notebook’,rc)
```
context键值对的示例列表如下。
```python
    axes.titlesize           12
    axes.labelsize           11
    figure.figsize           [ 8.   5.5]
    grid.linewidth           1
    lines.linewidth          1.75
    legend.fontsize          10
    lines.markeredgewidth    0
    lines.markersize         7
    patch.linewidth          0.3
    xtick.labelsize          10
    ytick.labelsize          10
    xtick.major.pad          7
    ytick.major.pad          7
    xtick.major.width        1
    ytick.major.width        1
    xtick.minor.width        0.5
    ytick.minor.width        0.5
```
两组绘图函数均可以通过set()函数控制参数。


##### 配色

 在介绍seaborn的配色使用之前，首先需要介绍seaborn的调色板（palette）功能。

通过sns.color_palette()函数设置或查看调色板（palette）,函数返回值是rgb元组的列表。调用sns.palplot()画出palette的每种颜色。
```python
    sns.palplot(sns.color_palette())
```
上例会显示seaborn预定义六个palette颜色：pastel，bold，muted，deep，dark和colorblind。

![](http://tym1060326.is-programmer.com/user_files/tym1060326/Image/color_palettes_7_0_thumb_20140527165852.png)

 另外可以通过color_pallete()设定palette的颜色。例如
```python
    sns.palplot(sns.color_palette(‘husl’,8))
```

![](http://tym1060326.is-programmer.com/user_files/tym1060326/Image/color_palettes_9_0_thumb_20140527165854.png)

 显示husl颜色空间平均分布的8个颜色。又如
```python
    sns.palplot(sns.color_palette(‘hls’,8))
```

![](http://tym1060326.is-programmer.com/user_files/tym1060326/Image/color_palettes_10_0_thumb_20140527165855.png)

 显示hls颜色空间平均分布的8个颜色。

还可以使用matplotlib中colormap的名字，例如
```python
    sns.palplot(sns.color_palette(‘coolwarm’,7))
```
![](http://tym1060326.is-programmer.com/user_files/tym1060326/Image/color_palettes_12_0_thumb_20140527165857.png)

 显示冷暖色平均分布的7个颜色。
```python
    sns.palplot(sns.color_palette(‘RdPu_r’,8))
```
![](http://tym1060326.is-programmer.com/user_files/tym1060326/Image/color_palettes_14_0_thumb_20140527165858.png)

显示连续的平均分布的8个颜色。
```python
    sns.palplot(sns.color_palette(‘Set2’,10))
```
![](http://tym1060326.is-programmer.com/user_files/tym1060326/Image/color_palettes_16_0_thumb_20140527165900.png)

显示定性的10个颜色。
```python
    sns.palplot(sns.color_palette(["#9b59b6", "#3498db", "#95a5a6", "#e74c3c", "#34495e", "#2ecc71"]))
```
![](http://tym1060326.is-programmer.com/user_files/tym1060326/Image/color_palettes_18_0_thumb_20140527165901.png)

显示自定义的6个颜色。

palette可以大致被分为3类:离散的（diverging）、连续的（sequential）和定性的（qualitative）。离散的palette适用于表示数据有自然的、有实际意义的断点；而连续的palette适用于表示数据从低到高等的变化；定性的palette适用于分类的数据。

另外还有两个函数可以自定义palette：blend_palette()和dark_palette()。

dark_palette()是根据指定的颜色，自动调整其灰度产生多个颜色，这些灰度增量不是均等的。

```python
sns.palplot(sns.dark_palette(‘MediumPurple’))
sns.palplot(sns.dark_palette(‘skyblue’,8,reverse=True))
```
![](http://tym1060326.is-programmer.com/user_files/tym1060326/Image/color_palettes_20_0_thumb_20140527165903.png)
![](http://tym1060326.is-programmer.com/user_files/tym1060326/Image/color_palettes_22_0_thumb_20140527165904.png)


 

dark_palette()函数默认返回颜色rgb列表，也可以返回colormap对象作为参数传给matplotlib函数。
```python
    pal = sns.dark_palette(‘palegreen’, as_cmap=True)

    sns.kdeplot(sample, cmap=pal)
```
blend_palette()可以更加灵活的指定自己喜欢的颜色。
```python
sns.palplot(sns.blend_palette([‘mediumseagreen’,‘ghostwhite’,‘#4168B7’], 9))
```
![](http://tym1060326.is-programmer.com/user_files/tym1060326/Image/color_palettes_28_0_thumb_20140527165905.png)
通过改变饱和度产生连续型palette使用desaturate()函数。
```python
 sns.palplot(sns.blend_palette([sns.desaturate(‘#009B76’,0),’#009B76’], 5))
```
在使用seaborn提供的绘图函数时，函数内部会调用调色板函数，因此只需将调色板字符串或*_palette()函数返回的对象传给color或者palette参数即可。
```python
    sns.tsplot(sample, color=‘muted’)

    sns.violinplot(sample, color=pal)

    sns.factorplot(sample, palette=‘OrRd’)

    sns.factorplot(sample, palette=‘Paired’)
```
另外color_palette()也可以在with关键词之后使用，用于临时改变配色。

```python
    with sns.color_palette(‘PuBuGn_d’):

        plot(sample)
```

还可以使用set_palette()改变matplotlib的配色，使所有绘图均使用同一palette，其参数与color_palette()相同。


#####  数据分布的可视化

简单的单变量分布可以使用matplotlib的hist()函数绘制直方图，双变量可以使用sns.jointplot()函数绘制六边形箱图阵（hexbin plot，网上就是这么翻译的= =）。

如果需要更进一步的描述数据分布，可以使用地毯图（rug plot）和核密度估计图（Kernel Density Estimate plot, KDE plot）。Seaborn的KDE图默认使用的是高斯核函数。

```python
sns.rugplot(data)
sns.kdeplot(data,shade=True)
```
 和plt.hist()函数的bins参数类似，sns.kdeplot()函数有bw参数可以指定核函数的带宽（bandwidth），使密度估计对（可能为噪声的）高频信号更加或更不加敏感。bw取正实数。

核函数也可以通过kernel参数指定，值为{“biw”,“cos”,“epa”,“gau”,“tri”,“triw”}。但是核函数对密度估计函数的形态影响并不是很大。

kdeplot()函数还可以画多变量的KDE图，当样本参数传入（n_units,n_variables）形状的DataFrame时，kdeplot()将自动绘制多变量KDE图。在高维KDE图中，只能用高斯核。

kdeplot()的cmap参数可以指定配色，例如‘BuGn_d’、‘PuRd_d’、‘Purples’等。

kdeplot()还有很多参数可以实现不同样式的KDE图，参考官方手册。

jointplot()函数不仅能够画双变量的联合分布概率估计图，还可以同时绘制每个变量的边缘分布。

```python
sns.jointplot('x','y',data,kind='kde')
sns.jointplot(x,y,kind='hex')
```

下面介绍displot()函数。displot()函数能够将多种绘图叠加在一个图中，该函数提供一个统一的接口来绘制直方图、核密度图和地毯图等。displot()会默认自动选择合适的bin的数量，来绘制直方图。函数可以设置rug、hist和kde参数来选择哪些图参与叠加绘制，[plot]_kws参数（例如kde_kws）赋值为参数名与对应值的键值对字典，来指定对应绘图的参数。参数字典中的参数名为字符串类型。

```python
sns.distplot(data, rug=False, hist=True, hist_kws={‘color’:‘slategray’})
```

#####  定量数据的线性模型绘图

线性模型用于理解激励（自变量）与响应（因变量）之间的线性关系。下面以介绍lmplot()函数的使用为主。

lmplot()函数的数据参数使用Pandas的DataFrame类型，并且需要提供激励和响应变量的在DataFrame中的name。

```python
sns.lmplot('total_bill','tip',tips)
```

![](http://tym1060326.is-programmer.com/user_files/tym1060326/Image/WindowsLiveWriter/Seaborn_C0E3/quantitative_linear_models_7_0%5B1%5D_thumb.png)

图中给出了样本的散点图和回归直线，以及95%置信区间的范围。可以使用ci参数指定置信区间的百分比，例如ci=68表示置信度为68%。和distplot()函数类似，lmplot()也可以使用字典参数指定不同绘图图层的参数，例如

```python
sns.lmplot(‘total_bill’,‘tip’,tips,
	scatter_kws={‘marker’:‘.’,‘color’:‘slategray’},
	line_kws={‘linewidth’:1,‘color’:‘seagreen’})
```
![](http://tym1060326.is-programmer.com/user_files/tym1060326/Image/WindowsLiveWriter/Seaborn_C0E3/quantitative_linear_models_11_0%5B1%5D_thumb.png)

 当数据是定量离散的，可以使用抖动参数提高视觉效果。
```python
    sns.lmplot(‘size’,‘tip’,tips,x_jitter=0.15)

```

![](http://tym1060326.is-programmer.com/user_files/tym1060326/Image/WindowsLiveWriter/Seaborn_C0E3/quantitative_linear_models_15_0%5B1%5D_thumb.png)

 离散变量的表示也可以使用估计值和范围代替，给x_estimator传入估计值的计算函数名即可。例子中使用的是均值估计。
```python
    sns.lmplot(‘size’,‘tip’,tips,x_estimator=np.mean)
```
![](http://tym1060326.is-programmer.com/user_files/tym1060326/Image/WindowsLiveWriter/Seaborn_C0E3/quantitative_linear_models_17_0%5B1%5D_thumb.png)

连续变量也可以使用x_bins参数离散化。

```python
    bins = [10, 20, 30, 40]
    sns.lmplot(‘total_bill’, ‘tip’, tips, x_bins=bins)    #x_bins也可以赋值为bins的数量

```

![](http://tym1060326.is-programmer.com/user_files/tym1060326/Image/WindowsLiveWriter/Seaborn_C0E3/quantitative_linear_models_19_0%5B1%5D_thumb.png)

 lmplot()函数支持使用hue参数区分数据集的不同子集，以便画出子集的线性回归函数。hue取值为DataFrame的某个name。

col参数也可以达到相似的效果，与hue参数的区别是，hue参数是将name所指定的不同类型的数据叠加在一张图中显示，而col参数将不同类型的数据画在不同图中。如果同时使用两个参数，则仍是不同图片，但样本点与回归直线的色彩不同。

lmplot()函数也有palette参数指定绘图调色板。hue_order参数通过传入列表指定顺序。

lmplot()函数可以绘制更高阶的回归拟合曲线，通过order参数指定阶数。

```python
sns.lmplot(‘size’, ‘total_bill’, tips, order=2)
```
![](http://tym1060326.is-programmer.com/user_files/tym1060326/Image/WindowsLiveWriter/Seaborn_C0E3/quantitative_linear_models_34_0%5B1%5D_thumb.png)

lmplot()函数还可以画出lowess图（局部加权光滑描点图？），但暂时没有置信区间。

```python
http://tym1060326.is-programmer.com/user_files/tym1060326/Image/WindowsLiveWriter/Seaborn_C0E3/quantitative_linear_models_36_0%5B1%5D_thumb.png
```

![](http://tym1060326.is-programmer.com/user_files/tym1060326/Image/WindowsLiveWriter/Seaborn_C0E3/quantitative_linear_models_36_0%5B1%5D_thumb.png)

mplot()可以实现逻辑回归。由于样本是二值分布的，当使用一般线性回归时，会出现在某些范围概率为负或超过1的情况，因此需要使用逻辑回归。
```python
sns.lmplot(‘total_bill’, ‘big_tip’, tips, y_jitter=0.05, logistic=True)
```

![](http://tym1060326.is-programmer.com/user_files/tym1060326/Image/WindowsLiveWriter/Seaborn_C0E3/quantitative_linear_models_44_01_thumb.png)

lmplot()可以通过参数降低噪声点对回归结果的影响，使用robust参数和n_boot参数减少bootstrap迭代的次数。

```python
sns.lmplot(‘total_bill’, ‘tip’, tips, robust=True, n_boot=500)
```
 当处理多个变量时，可能两个变量看上去是相关的，但引入另一个变量时就变得不相关。可以使用x_partial参数。

lmplot()实际上调用的是更底层的regplot()函数进行回归和绘图。regplot()函数不会改变已画好的图像。因此如果需要更多的控制，可以使用regplot()函数。regplot()函数可以使用lmplot()的绝大多数参数，但regplot()一次只能画一个点或一条回归线。另外，regplot()函数接受numpy数组作为数据，并可以单独设置颜色。

residplot()函数可以画出样本的残差（理论上散点应该无规律地分布在y=0附近）。但对样本进行适当变换，可能体现残差的高阶特征，可以使用lowess参数得到拟合曲线。

jointplot()函数也可以将kind参数设置为‘resid’或者‘reg’画出残差或回归曲线。

corrplot()函数是一个比较有用的函数，能够画出数据集变量相关矩阵的热度图，提供数据集变量两两之间的关系的宏观视图。函数默认使用排列检验得到相关矩阵的p值，当数据集较大时，排列检验将会相对比较耗时。当然如果数据集较大，p值可能并不是特别相关，因此可以不使用显著性检验。corrplot()的参数允许去掉颜色条、去掉星形标号、设置颜色、设置tail（只关心正值或负值）、设置颜色条的范围、去掉相关系数标注等。

还有interactplot()函数和coefplot()函数的使用可以参考官方手册。


##### Data-aware网格绘图

 多维数据需要将数据分为子集依次画图，这种绘图称为lattice(或者trellis)绘图。Seaborn基于matplotlib提供绘制多维数据的函数。Seaborn的接口要求使用Pandas的DataFrame作为数据参数，并且必须是结构化的数据（tidy data），每一行为一个观测，每一列为一个变量。

FacetGrid类能够方便的绘制这类图像。FacetGrid对象有三个维度：row,col和hue。下面是几个例子。

```python
tips = sns.load_dataset('tips')

g = sns.FaceGrid(tips,col='time') #只是将grid初始化，并不绘图

g.map(plt.hist,'tip')
```

![](http://tym1060326.is-programmer.com/user_files/tym1060326/Image/WindowsLiveWriter/Seaborn_C0E3/axis_grids_9_0%5B1%5D_2.png)

 注意得到最终图像需要使用map()函数。通过map()函数不同的参数，可以实现不同的绘图。第一个参数可以是plt.hist, plt.scatter, sns.regplot, sns.barplot, sns.distplot, sns.pointplot等。

需要画联合分布时，可以使用JointGrid类，其性质与FaceGrid类似。JointGrid对象使用plot()函数、plot_marginals()函数、plot_joint()函数和annotate()函数画不同样式的图像。具体参数及使用见官方手册。