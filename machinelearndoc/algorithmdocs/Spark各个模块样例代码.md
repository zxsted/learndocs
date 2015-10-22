spark learn

[toc]


#### optimize 优化算法

##### 并行逻辑回归的详述

[传送](http://blog.sina.com.cn/s/blog_6cb8e53d0101oetv.html)

![](http://s10.sinaimg.cn/mw690/001Zryslgy6Gx4AFEh379&690)

##### SGD

类关系图：

![类关系图](http://images.cnitblog.com/i/469775/201408/151942037646581.png)

函数调用路径：

![](http://images.cnitblog.com/i/469775/201408/151943281082615.png)

train->run 

run 函数的处理逻辑

1. 使用最优化算法来求得最优解
2. 根据最优解创建相应的回归模型  createModel

###### runMiniBatchSGD 是真正计算Gradient 和 Loss 的地方

```java

def runMiniBatch(
		data:RDD[(Double,Vector)],
		gradient:Gradient,
		updater:Updater,
		stepSize:Double,
		numIterations:Int,
		regParam:Double,
		miniBatchFraction:Double,
		initalWeights:Vector) :(Vector,Array[Double]) = {

	val stochasticLossHistory = new ArrayBuffer[Double](numIterations)

	val numExamples = data.count()      // 样本个数
	val miniBatchSize = numExamples * miniBatchFraction

	// if not data return intial weights to avoid nans
	if (numExamples == 0) {

		logInfo("GradientDescent.runMiniBatchSGD returning initial weights no data found")

		return (initalWeights,stochasticLossHistory.toArray)
	}

	if(numExamples * miniBatchFraction < 1) {

		logWarning("The miniBatchFraction is too small")
	}

	// 初始化 权重
	//
	var weights = Vectors.dense(initialWeights.toArray)
	val n = weights.size
	
	// 计算 者则化因子
	var regVal = updater.compute(
			weights,Vectors.dense(new Array[Double](weights.size)),0,1,regParam)._2

	for(i <- 1 to numIterations) {

		val bcWeights = data.context.broadcast(weights)    // 将 权重作为广播变量

		// 这是一个分布式操作	
		// 从全部数据中进行采样（faction 为 miniBatchFraction）
		// 将该subset 的 subweight 进行累计 这是一个mapreduce
		val (gradientSum,lossSum,miniBatchSize) = data.sample(false,miniBatchFraction,42+i)
		.treeAggregate((BDV.zeros[Double](n),0.0,0L))(
				seqOp = (c,v) => {
					// c： （grad,loss,count） , v:(label,features)
					val l = gradient.compute(v._2，v._1,bcWeights.value,Vectors.fromBreeze(c._1))
					(c._1,c_2+l,c._3+1)
				},

				combOp = (c1,c2) => {
					// c: (grad,loss,count)
					(c1._1 += c2._1,c1._2+c2._2,c1._3+c2._3)
				})
	

	if (miniBatchSize > 0) {

	/**
	 *OTE(Xinghao): lossSum is computed using the weights from the previous iteration
	 * and regVal is the regularization value computed in the previous iteration as well.
	 * */

	stochasticLossHistory.append(lossSum / miniBatchSize + regVal)

	val update = updater.compute(
			weights,Vectors.fromBreeze(gradientSum / miniBatchSize),
			stepSize,
			i,
			regParam)
	weights = update._1
	regVal = update._2
	} else {
		logWarning(s"Iteration ($i/$numIterations).The size of sampled batch is zero")
		}
	}


	logInfo("GradientDescent.runMiniBatchSGD finished.Last 10 stochastic losses %s".format(
				stochasticLossHistory.takeRight(10).mkString(",")))

	(weights,stochasticLossHistory.toArray)
}


```

观察上面的源代码，可以发现 整个 算法的 分布式关键是在 aggregate 中，它提供了 损失函数 
cost 以及它的梯度的计算的方法， 大都是分布式的， 以供 SGD 或者 LBFGS 进行递增量，以及一维搜索的计算。

下面是 aggregate 函数的定义：

```java

def aggregate[U:ClassTag](zeroValue:U)(seqOp:(U,T)=> U,combOp:(U,U) => u):u={

	// 复制 初始值
	var jobResult = Utils.clone(zeroValue,sc.env.closureSerializer.newInstance())
	val cleanSeqOp = sc.clean(seqOp)
	val cleanCombOp = sc.clean(combOp)
	val aggregatePattern = (it:Iterator[T])=>it.aggregate(zeroValue)(cleanSeqOp,cleanCombOp)
	val mergeResult = (index:Int,taskResult:U) => combOp(jobResult,taskResult)
	sc.runJob(this,aggratePartition,mergeResult)
	jobResult
}
```

 aggregate 函数有三个输入参数， 一个是初始值 ZeroValue, 二是seqOp 三是combOp

 >  1. seqOp,seqOp 会被并行执行， 具体由各个executor 上的task 来完成计算
 >  2. combOp combOp 则是串行执行， 其中combOp操作在 JobWatier 的taskSucceded函数中被调用
 
 下面是一个使用例子：
 
```java
val z = sc.parallelize(List(1,2,3,4,5,6),2)

z.aggregate(0)(match.max(_,_),_+_)
```
运行结果为 9



仔细观察一下运行时的日志输出, aggregate提交的job由一个stage(stage0)组成,
由于整个数据集被分成两个partition,所以为stage0创建了两个task并行处理。


LeastSquareGradient
讲完了aggregate函数的执行过程, 回过头来继续讲组成seqOp的gradient.compute函数。

LeastSquareGradient用来计算梯度和误差,注意cmopute中cumGraident会返回改变后的结果 ,这里计算公式依据的就是cost-function中的▽Q(w)

```java

class LeastSquaresGradient extends Gradient{

	override def compute(data:Vecotr,
			lable:Double,
			weights:Vector):(Vecotr,Double) = {

		val brzData = data.toBreeze
		val brzWeights = weights.toBreeze
		val diff = brzWeights.dot(brzData) - label
		val loss = diff * diff
		val gradient = brzData * (2.0 * diff)

		(Vectors.fromBreeze(gradient),loss)
	}


	override def cost(
			data:Vector,
			label:Double,
			weights:Vector,
			cumGradient:Vector):Double = {
		val brzData = data.toBreeze
		val brzWeights = weights.toBreeze

		val diff = brzWeights.dot(brzData) - label

		// 下面这句话 完成 y += a*x
		brzAxpy(2.0*diff,brzData,cumGradient.toBreeze)

		diff*diff
	}


}
```


在上述代码中频繁出现breeze相关的函数,你一定会很好奇,这是个什么新鲜玩艺。

说 开 了 其 实 一 点 也 不 稀 奇, 由 于 计 算 中 有 大 量 的 矩 阵(Matrix)
及 向量(Vector)计算,为了更好支持和封装这些计算引入了breeze库。
Breeze, Epic及Puck是scalanlp中三大支柱性项目, 具体可参数www.scalanlp.org

正则化过程

```java

val update = updater.compute(
		weights,Vectors.fromBreeze(gradientSum / miniBatchSize),stepSize,i,regParam)


```

下面是岭回归的代码实现

```java

class SquaredL2Updater extends Updater {

	override def compute(
			weightsOld : Vector,
			gradient:Vector,
			stepSize:Double,
			iter:Int,
			regParam:Double):(Vector,Double) = {
	// 
	val thisIterStepSize = stepSize / math.sqrt(iter)
	val brzWeights: BV[Double] = weightsOld.toBreeze.toDenseVector
	brzWeights :*=(1.0 - thisIterStepSize * regParam)
	brzAxpy(-thisIterStepSize,gradient.toBreeze,brzWeights)
	val norm = brzNorm(brzWeights,2.0)

	(Vectors.fromBreeze(brzWeights),0.5*regParam*norm*norm)
	}
}

```
 计算出权重系数(weights)和截距intecept，就可以用来创建线性回归模型LinearRegressionModel
 用模型的predict函数来对观测值进行预测

```java

class LinearRegressionModel(
		override val weights:Vector,
		override val intercept:Double)
		extends GeneralizedLinearModel(weights,intercept) with RegressionModel with Serializable{
	
		override proctected def predictPoint(
				dataMatrix:Vector,
				weightMatrix:Vector,
				intercept:Double):Double = {

			weightMatrix.toBreeze.dot(dataMatrix.toBreeze) + intercept
		}
}

```

下面是一个完整的程序调用实例

```java

import org.apache.spark.mllib.regression.LinearRegressionWithSGD
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.mllib.linalg.Vectors


/**
 * 加载和转化数据
 * */
val data = sc.textFile("mllib/data/ridge-data/lpsa.data")
val parsedData = data.map{
	line => val parts = line.split(",")
	LabeledPoint(parts(0).toDouble,Vectors.dense(parts(1).split(' ').map(_.toDouble)))
}

// 构建model
val numIterations = 100
val model = LinearRegressionWithSGD.train(parsedData,numIterations)

// 在训练集上进行model Evaluate model ,并且计算训练损失
val valuesAndPreds = parsedData.map{
	point => val prediction = model.predict(point.features)
	(point.label,prediction)
}

val MSE = valuesAndPreds.map{
	case(v,p) => math.pow((v-p),2)
}.mean()

println("training Mean Squared Error = " + MSE)

//

/**
 *  model 的使用要点：
 *
 *  找到对应的 假设函数   cost
 *  用于评估的 损失函数   measure
 *  用于最优化的求解方法
 *  正则化规则
 * */
```



##### LBFGS

###### LBFGS 的run部分俄

```java

def runLBFGS(
		data: RDD[(Double,Vector)],     // 训练数据的RDD
		gradient:Gradient,              // 梯度计算函数
		updater:Updater,                // 参数更新函数 如L2updater  L1Updater
		numCorrections:Int,             // 缓存个数
		convergenceTol:Double,          // 收敛阈值
		maxNumIterations: Int,          // 最大循环次数
		regParam:Double,                // 
		initialWeights: Vector:Vector   // 初始化权重向量
		) : (Vector,Array[Double]) = {


	val lossHistory = new ArrayBuffer[Double](maxIterations)     // 存储损失量
	
	val numExamples = data.count()        // RDD count 函数

	// CostFun 封装了 数据 ，梯度计算函数， 更新函数 用于 计算梯度 和 一次损失的计算
	// 损失计算函数 是用于一维搜索
	val costFun = new CostFun(data,gradient,updater,regParam,numExamples)    

	val lbfgs = new BreezeLBFGS[BDV[Double]](maxNumIterations,
											numCorrections,
											convergenceTol)

	// 调用循环
	val states = lbfgs.iterations(new CachedDiffFunction(costFun),initialWeights.toBreeze.toDenseVector)
	
	// state 封装了每一次循环的计算的结果 （checkpoint）
	var state = states.next()

	while(states.hasNext) {
		lossHistory.append(state.value)
		state = states.next()
	}

	lossHistory.append(state.value)

	val weights = Vectors.fromBreeze(state.x)

	logInfo("LBFGS.runLBFGS finished.Last 10 losses %s".fromat(
				lossHistory.takeRight(10).mkString(",")))

	(weights,lossHistory.toArray)

}


```

由上面的代码可以看出 lbfgs的重点是 costfunc的 封装和调用 也是分布式计算的核心， 观察它可以明白 spark 是如何进行分布式运算的

```java
/**
 * 可以看出 costFunc 是计算的中点，下面是它的代码
 *
 * */
private class CostFun(
		data:RDD[(Double,Vector)],
		gradient:Gradient,
		updater:Updater,
		regParam:Double,
		numExamples:Long) extends Diffunction[BDV[Double]] {

	private var i = 0

	override def calculate(weights:BDV[Double]) = {
		// Have a local copy to avoid the serialization of CostFunc object 
		// Which is not serlializable
		val localData = data
		val localGradient = gradient

		// 注意这里是分布式计算的 使用的是RDD 的aggrate 函数进行计算
		val (gradientSum,lossSum) = localData.aggregate
				((BDV.zeros[Double](weights.size),0.0))       // 初始 值 参数向量初始化为0 ， 损失值为0.0
				
				(seqOp = (c,v) => (c,v) match {               // c ： 是前一次计算值， v 是当前的值 
					case ((grad,loss),(label,features)) =>    // (grad,loss) : 是累积 梯度和 损失值， （label,features） shi dangqianzhi 
						val l = localGradient.compute(        // 计算梯度
							features,lablel,Vectors.fromBreeze(weights),Vectors.fromBreeze(grad))

						(grad,loss+l)
				},              // 相当于 map
				
				combOp = (c1,c2) => (c1,c2) match {           // 累计函数，相当于reduce 函数
					case ((grad1,loss1),(grad2,loss2)) =>
					(grad1 += grad2,loss1+loss2)
				})

		/**
		 * reval is sum of weight squares squares if it`s L2 updater;
		 * for other updater,the same logic is followed
		 * */
		val regVal = updater.compute(
				Vectors.fromBreeze(weights),
				Vectors.dense(new Array[Double](weights.size)),0,1,regParam)._2

		// 计算 损失函数
		val loss = lossSum / numExamples + regVal
		
		val gradientTotal = weights - updater.compute(
				Vectors.fromBreeze(weights),
				Vectors.dense(new Array[Double](weights.size)),1,1,regParam)._1.toBreeze

		// 计算平均梯度
		// gradientTotal = gradientSum / numExamples + gradientTotal
		axpy(1.0/numExamples,gradientSum,gradientTotal)

		i += 1

		(loss,gradientTotal)
	}

}

```

##### 小结

下面是 优化算法的感悟，是作者阅读 andrew ng 的论文 ： on optimization methods for deep learning 得到的结论：

[传送](http://blog.csdn.net/silence1214/article/details/21520217)
[作者的机器学习实验的博文系列](http://blog.csdn.net/silence1214/article/category/1183835/1)

1. 今天早上阅读了on optimization methods for deep learning这篇paper，是andrew NG组的博士的一篇paper，这篇paper介绍了L-BFGS，CG（共轭梯度）和SGD算法。

2. 个人感受最深的就是，我对CG没用过，用L-BFGS最多，SGD是最想用的，但是往往找不到好的模型参数。L-BFGS当样本过多的时候运行非常慢，调一次参数太慢了，SGD速度倒是很快，但是往往结果的差异性很大，没法找到那组参数。

3. 在文中作者给了很多的experiment，发现L-BFGS和CG的效果是最好的，效果好指的有2个方面：达到一样的精度的时候用的时间最少；用的时间一样的多的时候，精度最高。

4. 并且作者给出了**实验中用L-BFGS的话，使用mini-batch，也就是类似SGD的训练方法**，这样**可以让L-BFGS速度也很快，精度也很高**，同时这篇paper也给的有源代码，我好好阅读下再来说下怎么做。

注意上面说明了一个重要的观点 ： 
 
> **实验中用L-BFGS的话，使用mini-batch，也就是类似SGD的训练方法**，这样**可以让L-BFGS速度也很快，精度也很高**

#####  AutoEncoder 的 参数训练的 tied的方法技巧介绍

1. 在传统的AutoEncoder中我们知道，输入层到隐层有一个权重W1和bias b1，隐藏到输出层也有一个权重W2和截距项b2，此时我们也知道W1和W2的转置的型是同样大小的。但是在传统的AutoEncoder中我们对于W1和W2是单独训练的。

2. 在**tied weight中也就是W1和W2是tied的，此时让W2的转置等于W1，也就是两个W其实是一样的，**那么我们怎么做？因为如果初始化的时候让W2的转置等于W1，那么经过一次梯度下降由于W2上的grad和W1上的不一样，所以梯度下降一次之后，他们就不一样了怎么办呢？

3. 网上找了很多，其实真正的做法并没有找到。后来翻看了别人的代码，发现了做法是这样子的：

4. **初始化让W2的转置等于W1，第一次求梯度的时候，对于W2的梯度记到，暂时保存，比如用W2grad，接着计算W1的梯度，用W1grad来表示，那么返回的时候我们这样来返回,让W1grad+W2grad'，作为W1的梯度，那么出去的时候就是和W1同型的了，在函数内部进行cost计算的时候还是根据W1，自动扩展一个W2即可。。
**


5：我觉得在第四部中也可以直接返回W1grad即可。。此时还是有b1和b2的哈。。

#### mllib

##### 数据类型

###### vector

```scala
import org.apache.spark.mllib.linalg.{Vector, Vectors}

// Create a dense vector (1.0, 0.0, 3.0).
val dv: Vector = Vectors.dense(1.0, 0.0, 3.0)
// Create a sparse vector (1.0, 0.0, 3.0) by specifying its indices and values corresponding to nonzero entries.
val sv1: Vector = Vectors.sparse(3, Array(0, 2), Array(1.0, 3.0))
// Create a sparse vector (1.0, 0.0, 3.0) by specifying its nonzero entries.
val sv2: Vector = Vectors.sparse(3, Seq((0, 1.0), (2, 3.0)))
```

###### Labeled point

```scala
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

// Create a labeled point with a positive label and a dense feature vector.
val pos = LabeledPoint(1.0, Vectors.dense(1.0, 0.0, 3.0))

// Create a labeled point with a negative label and a sparse feature vector.
val neg = LabeledPoint(0.0, Vectors.sparse(3, Array(0, 2), Array(1.0, 3.0)))
```

加载svmLib 的稀疏类型的数据 ,注意返回的类型是 LabeledPoint

```scala
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD

val examples: RDD[LabeledPoint] = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_libsvm_data.txt")
```

###### Local Matrix

```scala
mport org.apache.spark.mllib.linalg.{Matrix, Matrices}

// Create a dense matrix ((1.0, 2.0), (3.0, 4.0), (5.0, 6.0))
val dm: Matrix = Matrices.dense(3, 2, Array(1.0, 3.0, 5.0, 2.0, 4.0, 6.0))
```

###### Distribute Matrix

* RowMatrix
 each row is represented by a local vector

```scala
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.linalg.distributed.RowMatrix

val rows: RDD[Vector] = ... // an RDD of local vectors
// Create a RowMatrix from an RDD[Vector].
val mat: RowMatrix = new RowMatrix(rows)

// Get its size.
val m = mat.numRows()
val n = mat.numCols()
```
* IndexedRowMatrix
index (long-typed) and a local vector.

```scala
import org.apache.spark.mllib.linalg.distributed.{IndexedRow, IndexedRowMatrix, RowMatrix}

val rows: RDD[IndexedRow] = ... // an RDD of indexed rows
// Create an IndexedRowMatrix from an RDD[IndexedRow].
val mat: IndexedRowMatrix = new IndexedRowMatrix(rows)

// Get its size.
val m = mat.numRows()
val n = mat.numCols()

// Drop its row indices.
val rowMat: RowMatrix = mat.toRowMatrix()
```
* CoordinateMatrix
(Long, Long, Double).

```scalae
import org.apache.spark.mllib.linalg.distributed.{CoordinateMatrix, MatrixEntry}

val entries: RDD[MatrixEntry] = ... // an RDD of matrix entries
// Create a CoordinateMatrix from an RDD[MatrixEntry].
val mat: CoordinateMatrix = new CoordinateMatrix(entries)

// Get its size.
val m = mat.numRows()
val n = mat.numCols()

// Convert it to an IndexRowMatrix whose rows are sparse vectors.
val indexedRowMatrix = mat.toIndexedRowMatrix()
```

* BlockMatrix

```scala
import org.apache.spark.mllib.linalg.distributed.{BlockMatrix, CoordinateMatrix, MatrixEntry}

val entries: RDD[MatrixEntry] = ... // an RDD of (i, j, v) matrix entries
// Create a CoordinateMatrix from an RDD[MatrixEntry].
val coordMat: CoordinateMatrix = new CoordinateMatrix(entries)
// Transform the CoordinateMatrix to a BlockMatrix
val matA: BlockMatrix = coordMat.toBlockMatrix().cache()

// Validate whether the BlockMatrix is set up properly. Throws an Exception when it is not valid.
// Nothing happens if it is valid.
matA.validate()

// Calculate A^T A.
val ata = matA.transpose.multiply(matA)
```

##### 基本统计

###### Summary statistics

colstats()

```scala
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.stat.{MultivariateStatisticalSummary, Statistics}

val observations: RDD[Vector] = ... // an RDD of Vectors

// Compute column summary statistics.
val summary: MultivariateStatisticalSummary = Statistics.colStats(observations)
println(summary.mean) // a dense vector containing the mean value for each column
println(summary.variance) // column-wise variance
println(summary.numNonzeros) // number of nonzeros in each column
```

###### Correlations

```scala
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.stat.Statistics

val sc: SparkContext = ...

val seriesX: RDD[Double] = ... // a series
val seriesY: RDD[Double] = ... // must have the same number of partitions and cardinality as seriesX

// compute the correlation using Pearson's method. Enter "spearman" for Spearman's method. If a 
// method is not specified, Pearson's method will be used by default. 
val correlation: Double = Statistics.corr(seriesX, seriesY, "pearson")

val data: RDD[Vector] = ... // note that each Vector is a row and not a column

// calculate the correlation matrix using Pearson's method. Use "spearman" for Spearman's method.
// If a method is not specified, Pearson's method will be used by default. 
val correlMatrix: Matrix = Statistics.corr(data, "pearson")
```

##### Stratified sampling

```scala
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.PairRDDFunctions

val sc: SparkContext = ...

val data = ... // an RDD[(K, V)] of any key value pairs
val fractions: Map[K, Double] = ... // specify the exact fraction desired from each key

// Get an exact sample from each stratum
val approxSample = data.sampleByKey(withReplacement = false, fractions)
val exactSample = data.sampleByKeyExact(withReplacement = false, fractions)
```

###### Hypothesis testing

```scala
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.stat.Statistics._

val sc: SparkContext = ...

val vec: Vector = ... // a vector composed of the frequencies of events

// compute the goodness of fit. If a second vector to test against is not supplied as a parameter, 
// the test runs against a uniform distribution.  
val goodnessOfFitTestResult = Statistics.chiSqTest(vec)
println(goodnessOfFitTestResult) // summary of the test including the p-value, degrees of freedom, 
                                 // test statistic, the method used, and the null hypothesis.

val mat: Matrix = ... // a contingency matrix

// conduct Pearson's independence test on the input contingency matrix
val independenceTestResult = Statistics.chiSqTest(mat) 
println(independenceTestResult) // summary of the test including the p-value, degrees of freedom...

val obs: RDD[LabeledPoint] = ... // (feature, label) pairs.

// The contingency table is constructed from the raw (feature, label) pairs and used to conduct
// the independence test. Returns an array containing the ChiSquaredTestResult for every feature 
// against the label.
val featureTestResults: Array[ChiSqTestResult] = Statistics.chiSqTest(obs)
var i = 1
featureTestResults.foreach { result =>
    println(s"Column $i:\n$result")
    i += 1
} // summary of the test

```

###### Random data generation

```scala
import org.apache.spark.SparkContext
import org.apache.spark.mllib.random.RandomRDDs._

val sc: SparkContext = ...

// Generate a random double RDD that contains 1 million i.i.d. values drawn from the
// standard normal distribution `N(0, 1)`, evenly distributed in 10 partitions.
val u = normalRDD(sc, 1000000L, 10)
// Apply a transform to get a random double RDD following `N(1, 4)`.
val v = u.map(x => 1.0 + 2.0 * x)
```

#### ml methods

##### classifier

###### svm

```scala
import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.util.MLUtils

/**
 * Created by zxsted on 15-9-6.
 */
object svmdemo {

  def main(args:Array[String]) = {

    val conf = new SparkConf().setAppName("svm classifier").setMaster("local")
    val sc = new SparkContext(conf)

    //  记载数据集
    val data = MLUtils.loadLibSVMFile(sc,"");

    // 将数据集划分成测试集和训练集

    val splits = data.randomSplit(Array(0.6,0.4),seed = 11L)

    val training = splits(0).cache()   // 保存训练集

    val test = splits(1)


    val numIterations = 100
    val model = SVMWithSGD.train(training,numIterations)

	
    // 通过模型的优化器设置模型的参数
    
    val svmAlg = new SVMWithSGD()
    
    svmAlg.optimizer
    .setNumIterations(200)
    .setRegParam(0.1)
    .setUpdater(new L1Updater)
    
    val modelL1 = svmAlg.run(training)


    model.clearThreshold()


    // 在测试集上进行验证
    val scoreAndLabels = test.map{ point =>
    val score = model.predict(point.features)
      (score,point.label)}

    //
    val metrics = new BinaryClassificationMetrics(scoreAndLabels)
    val auROC = metrics.areaUnderROC()

    println("Area under ROC = " + auROC)

    // 保存模型
    model.save(sc,"myModelPath")
    val sameModel = SVMModel.load(sc,"myModelPath")

  }
}

###### LR with LBFGS

```scala
import org.apache.spark.mllib.classification.{LogisticRegressionModel, LogisticRegressionWithLBFGS}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by zxsted on 15-9-6.
 */
class LGdemo {

  def main(args:Array[String]): Unit = {

    val conf = new SparkConf().setAppName("LG demo").setMaster("local")
    val sc = new SparkContext(conf)


    // 加载svm数据集
    val data = MLUtils.loadLibSVMFile(sc,"")

    val splits = data.randomSplit(Array(0.6,0.4),seed = 11L)

    val training = splits(0).cache()
    val test = splits(1)


    //  配置和运行模型

    val model = new LogisticRegressionWithLBFGS()
    .setNumClasses(10)
    .run(training)

    // 测试训练集
    val predictionAndLabels = test.map{ case LabeledPoint(label,features) =>
      val prediction = model.predict(features)
      (prediction,label)
    }

    // 度量结果
    val metrics = new MulticlassMetrics(predictionAndLabels)

    val precision = metrics.precision

    println("Precision = " + precision)

    // 保存模型
    model.save(sc,"myLRmodel")
    // 加载模型
    val sameModel = LogisticRegressionModel.load(sc,"myLRmodel")

  }

}
```

###### 线性回归

lasso 是使用 L1 regression ， Ridge 是使用 L2 regresion
RidgeRegressionWithSGD and LassoWithSGD can be used in a similar fashion as LinearRegressionWithSGD.

```scala
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.{LinearRegressionModel, LinearRegressionWithSGD, LabeledPoint}
import org.apache.spark.{SparkContext, SparkConf}

/**
 * Created by zxsted on 15-9-6.
 */
object LinearDemo {

  def main(args : Array[String])  = {

    val conf = new SparkConf().setAppName("linearRegression" ).setMaster("local")
    val sc = new SparkContext(conf)
    // 直接从文件中加载数据，构造labeledPoint

    val data = sc.textFile("")

    val parsedData = data.map{ line =>
      val parts = line.split(",")
      LabeledPoint(parts(0).toDouble,Vectors.dense(parts(1).split(" ").map(_.toDouble)))
    }.cache()


    val numIterations = 100
    val model = LinearRegressionWithSGD.train(parsedData,numIterations)


    val valuesAndPreds = parsedData.map{
      point =>val prediction = model.predict(point.features)
        (point.label,prediction)
    }

    val MSE = valuesAndPreds.map{
      case (v,p) => math.pow((v-p),2)
    }.mean()

    println("training Mean Squared Error = " + MSE )

    // 保存和加载模型
    model.save(sc,"myModelPath")

    val sameModel = LinearRegressionModel.load(sc,"myModelPath")
  }
}
```

###### Streaming linear regression

##### 协同过滤 （交替最小二乘法）

```scala

import org.apache.spark.mllib.recommendation.{MatrixFactorizationModel, ALS, Rating}
import org.apache.spark.{SparkContext, SparkConf}

/**
 * Created by zxsted on 15-9-6.
 */
object ALSDemo {

  def main(args:Array[String]) = {

    val conf = new SparkConf().setAppName("ALS demo").setMaster("local")
    val sc = new SparkContext(conf)

    // 加载数据
    val data = sc.textFile("")

    val ratings = data.map(_.split(',') match{
      case Array(user,item,rate) =>
        Rating(user.toInt,item.toInt,rate.toDouble)
    })


    val rank = 10
    val numIterations = 20
    val model = ALS.train(ratings,rank,numIterations,0.01)

    val usersProducts = ratings.map{ case Rating(user,product,rate) =>
      (user,product)
    }

    val predictions =
    model.predict(usersProducts).map{case Rating(user,product,rate) =>
      ((user,product),rate)
    }

    val ratesAndPreds = ratings.map{case Rating(user,product,rate) =>
      ((user,product),rate)}.join(predictions)


    val MSE = ratesAndPreds.map{case((user,product),(r1,r2)) =>
      val err = (r1-r2)
        err*err
    }.mean()

    println("Mean Squared Error = " + MSE)

    // 保存和加载模型
    model.save(sc,"myModelPath")

    val sameModel = MatrixFactorizationModel.load(sc,"myModelPath")
  }

}
```

If the rating matrix is derived from another source of information (e.g., it is inferred from other signals), you can use the trainImplicit method to get better results.

```scala
val alpha = 0.01
val lambda = 0.01
val model = ALS.trainImplicit(ratings, rank, numIterations, lambda, alpha)
```
In order to run the above application, follow the instructions provided in the Self-Contained Applications section of the Spark Quick Start guide. Be sure to also include spark-mllib to your build file as a dependency.


###### 朴素贝叶斯

```scala
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

val data = sc.textFile("data/mllib/sample_naive_bayes_data.txt")
val parsedData = data.map { line =>
  val parts = line.split(',')
  LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
}
// Split data into training (60%) and test (40%).
val splits = parsedData.randomSplit(Array(0.6, 0.4), seed = 11L)
val training = splits(0)
val test = splits(1)

val model = NaiveBayes.train(training, lambda = 1.0, modelType = "multinomial")

val predictionAndLabel = test.map(p => (model.predict(p.features), p.label))
val accuracy = 1.0 * predictionAndLabel.filter(x => x._1 == x._2).count() / test.count()

// Save and load model
model.save(sc, "myModelPath")
val sameModel = NaiveBayesModel.load(sc, "myModelPath")
```

###### 决策树

```scala


import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.mllib.util.MLUtils

// Load and parse the data file.
val data = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_libsvm_data.txt")
// Split the data into training and test sets (30% held out for testing)
val splits = data.randomSplit(Array(0.7, 0.3))
val (trainingData, testData) = (splits(0), splits(1))

// Train a DecisionTree model.
//  Empty categoricalFeaturesInfo indicates all features are continuous.
val numClasses = 2
val categoricalFeaturesInfo = Map[Int, Int]()
val impurity = "gini"
val maxDepth = 5
val maxBins = 32

val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
  impurity, maxDepth, maxBins)

// Evaluate model on test instances and compute test error
val labelAndPreds = testData.map { point =>
  val prediction = model.predict(point.features)
  (point.label, prediction)
}
val testErr = labelAndPreds.filter(r => r._1 != r._2).count.toDouble / testData.count()
println("Test Error = " + testErr)
println("Learned classification tree model:\n" + model.toDebugString)

// Save and load model
model.save(sc, "myModelPath")
val sameModel = DecisionTreeModel.load(sc, "myModelPath")


```

###### 回归树

```scala


import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.mllib.util.MLUtils

// Load and parse the data file.
val data = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_libsvm_data.txt")
// Split the data into training and test sets (30% held out for testing)
val splits = data.randomSplit(Array(0.7, 0.3))
val (trainingData, testData) = (splits(0), splits(1))

// Train a DecisionTree model.
//  Empty categoricalFeaturesInfo indicates all features are continuous.
val categoricalFeaturesInfo = Map[Int, Int]()
val impurity = "variance"
val maxDepth = 5
val maxBins = 32

val model = DecisionTree.trainRegressor(trainingData, categoricalFeaturesInfo, impurity,
  maxDepth, maxBins)

// Evaluate model on test instances and compute test error
val labelsAndPredictions = testData.map { point =>
  val prediction = model.predict(point.features)
  (point.label, prediction)
}
val testMSE = labelsAndPredictions.map{ case(v, p) => math.pow((v - p), 2)}.mean()
println("Test Mean Squared Error = " + testMSE)
println("Learned regression tree model:\n" + model.toDebugString)

// Save and load model
model.save(sc, "myModelPath")
val sameModel = DecisionTreeModel.load(sc, "myModelPath")


```

###### 随机森林用于分类

```scala


import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.mllib.util.MLUtils

// Load and parse the data file.
val data = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_libsvm_data.txt")
// Split the data into training and test sets (30% held out for testing)
val splits = data.randomSplit(Array(0.7, 0.3))
val (trainingData, testData) = (splits(0), splits(1))

// Train a RandomForest model.
//  Empty categoricalFeaturesInfo indicates all features are continuous.
val numClasses = 2
val categoricalFeaturesInfo = Map[Int, Int]()
val numTrees = 3 // Use more in practice.
val featureSubsetStrategy = "auto" // Let the algorithm choose.
val impurity = "gini"
val maxDepth = 4
val maxBins = 32

val model = RandomForest.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
  numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)

// Evaluate model on test instances and compute test error
val labelAndPreds = testData.map { point =>
  val prediction = model.predict(point.features)
  (point.label, prediction)
}
val testErr = labelAndPreds.filter(r => r._1 != r._2).count.toDouble / testData.count()
println("Test Error = " + testErr)
println("Learned classification forest model:\n" + model.toDebugString)

// Save and load model
model.save(sc, "myModelPath")
val sameModel = RandomForestModel.load(sc, "myModelPath")


```


###### 随机森林用于回归

```scala


import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.mllib.util.MLUtils

// Load and parse the data file.
val data = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_libsvm_data.txt")
// Split the data into training and test sets (30% held out for testing)
val splits = data.randomSplit(Array(0.7, 0.3))
val (trainingData, testData) = (splits(0), splits(1))

// Train a RandomForest model.
//  Empty categoricalFeaturesInfo indicates all features are continuous.
val numClasses = 2
val categoricalFeaturesInfo = Map[Int, Int]()
val numTrees = 3 // Use more in practice.
val featureSubsetStrategy = "auto" // Let the algorithm choose.
val impurity = "variance"
val maxDepth = 4
val maxBins = 32

val model = RandomForest.trainRegressor(trainingData, categoricalFeaturesInfo,
  numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)

// Evaluate model on test instances and compute test error
val labelsAndPredictions = testData.map { point =>
  val prediction = model.predict(point.features)
  (point.label, prediction)
}
val testMSE = labelsAndPredictions.map{ case(v, p) => math.pow((v - p), 2)}.mean()
println("Test Mean Squared Error = " + testMSE)
println("Learned regression forest model:\n" + model.toDebugString)

// Save and load model
model.save(sc, "myModelPath")
val sameModel = RandomForestModel.load(sc, "myModelPath")


```

###### GBDT 分类

```scala


import org.apache.spark.mllib.tree.GradientBoostedTrees
import org.apache.spark.mllib.tree.configuration.BoostingStrategy
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel
import org.apache.spark.mllib.util.MLUtils

// Load and parse the data file.
val data = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_libsvm_data.txt")
// Split the data into training and test sets (30% held out for testing)
val splits = data.randomSplit(Array(0.7, 0.3))
val (trainingData, testData) = (splits(0), splits(1))

// Train a GradientBoostedTrees model.
//  The defaultParams for Classification use LogLoss by default.
val boostingStrategy = BoostingStrategy.defaultParams("Classification")
boostingStrategy.numIterations = 3 // Note: Use more iterations in practice.
boostingStrategy.treeStrategy.numClasses = 2
boostingStrategy.treeStrategy.maxDepth = 5
//  Empty categoricalFeaturesInfo indicates all features are continuous.
boostingStrategy.treeStrategy.categoricalFeaturesInfo = Map[Int, Int]()

val model = GradientBoostedTrees.train(trainingData, boostingStrategy)

// Evaluate model on test instances and compute test error
val labelAndPreds = testData.map { point =>
  val prediction = model.predict(point.features)
  (point.label, prediction)
}
val testErr = labelAndPreds.filter(r => r._1 != r._2).count.toDouble / testData.count()
println("Test Error = " + testErr)
println("Learned classification GBT model:\n" + model.toDebugString)

// Save and load model
model.save(sc, "myModelPath")
val sameModel = GradientBoostedTreesModel.load(sc, "myModelPath")



```


###### GBDT 用于回归

```scala


import org.apache.spark.mllib.tree.GradientBoostedTrees
import org.apache.spark.mllib.tree.configuration.BoostingStrategy
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel
import org.apache.spark.mllib.util.MLUtils

// Load and parse the data file.
val data = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_libsvm_data.txt")
// Split the data into training and test sets (30% held out for testing)
val splits = data.randomSplit(Array(0.7, 0.3))
val (trainingData, testData) = (splits(0), splits(1))

// Train a GradientBoostedTrees model.
//  The defaultParams for Regression use SquaredError by default.
val boostingStrategy = BoostingStrategy.defaultParams("Regression")
boostingStrategy.numIterations = 3 // Note: Use more iterations in practice.
boostingStrategy.treeStrategy.maxDepth = 5
//  Empty categoricalFeaturesInfo indicates all features are continuous.
boostingStrategy.treeStrategy.categoricalFeaturesInfo = Map[Int, Int]()

val model = GradientBoostedTrees.train(trainingData, boostingStrategy)

// Evaluate model on test instances and compute test error
val labelsAndPredictions = testData.map { point =>
  val prediction = model.predict(point.features)
  (point.label, prediction)
}
val testMSE = labelsAndPredictions.map{ case(v, p) => math.pow((v - p), 2)}.mean()
println("Test Mean Squared Error = " + testMSE)
println("Learned regression GBT model:\n" + model.toDebugString)

// Save and load model
model.save(sc, "myModelPath")
val sameModel = GradientBoostedTreesModel.load(sc, "myModelPath")



```


##### 聚类

##### k-means

```scala

import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.mllib.linalg.Vectors

// Load and parse the data
// 注意这里数据的构建
val data = sc.textFile("data/mllib/kmeans_data.txt")
val parsedData = data.map(s => Vectors.dense(s.split(' ').map(_.toDouble))).cache()

// Cluster the data into two classes using KMeans
val numClusters = 2
val numIterations = 20
val clusters = KMeans.train(parsedData, numClusters, numIterations)

// Evaluate clustering by computing Within Set Sum of Squared Errors
val WSSSE = clusters.computeCost(parsedData)
println("Within Set Sum of Squared Errors = " + WSSSE)

// Save and load model
clusters.save(sc, "myModelPath")
val sameModel = KMeansModel.load(sc, "myModelPath")

```

###### 高斯混合分布

```scala
import org.apache.spark.mllib.clustering.GaussianMixture
import org.apache.spark.mllib.clustering.GaussianMixtureModel
import org.apache.spark.mllib.linalg.Vectors

// Load and parse the data
val data = sc.textFile("data/mllib/gmm_data.txt")
val parsedData = data.map(s => Vectors.dense(s.trim.split(' ').map(_.toDouble))).cache()

// Cluster the data into two classes using GaussianMixture
val gmm = new GaussianMixture().setK(2).run(parsedData)

// Save and load model
gmm.save(sc, "myGMMModel")
val sameModel = GaussianMixtureModel.load(sc, "myGMMModel")

// output parameters of max-likelihood model
for (i <- 0 until gmm.k) {
  println("weight=%f\nmu=%s\nsigma=\n%s\n" format
    (gmm.weights(i), gmm.gaussians(i).mu, gmm.gaussians(i).sigma))
}

```

###### Power iteration clustering (PIC)

```scala
import org.apache.spark.mllib.clustering.{PowerIterationClustering, PowerIterationClusteringModel}
import org.apache.spark.mllib.linalg.Vectors

val similarities: RDD[(Long, Long, Double)] = ...

val pic = new PowerIterationClustering()
  .setK(3)
  .setMaxIterations(20)
val model = pic.run(similarities)

model.assignments.foreach { a =>
  println(s"${a.id} -> ${a.cluster}")
}

// Save and load model
model.save(sc, "myModelPath")
val sameModel = PowerIterationClusteringModel.load(sc, "myModelPath")

```


###### LDA

```scala


import org.apache.spark.mllib.clustering.LDA
import org.apache.spark.mllib.linalg.Vectors

// Load and parse the data
val data = sc.textFile("data/mllib/sample_lda_data.txt")
val parsedData = data.map(s => Vectors.dense(s.trim.split(' ').map(_.toDouble)))
// Index documents with unique IDs
val corpus = parsedData.zipWithIndex.map(_.swap).cache()

// Cluster the documents into three topics using LDA
val ldaModel = new LDA().setK(3).run(corpus)

// Output topics. Each is a distribution over words (matching word count vectors)
println("Learned topics (as distributions over vocab of " + ldaModel.vocabSize + " words):")
val topics = ldaModel.topicsMatrix
for (topic <- Range(0, 3)) {
  print("Topic " + topic + ":")
  for (word <- Range(0, ldaModel.vocabSize)) { print(" " + topics(word, topic)); }
  println()
}


```

##### 维度规约

###### svd

```scala
import org.apache.spark.mllib.linalg.Matrix
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.mllib.linalg.SingularValueDecomposition

val mat: RowMatrix = ...

// Compute the top 20 singular values and corresponding singular vectors.
val svd: SingularValueDecomposition[RowMatrix, Matrix] = mat.computeSVD(20, computeU = true)
val U: RowMatrix = svd.U // The U factor is a RowMatrix.
val s: Vector = svd.s // The singular values are stored in a local dense vector.
val V: Matrix = svd.V // The V factor is a local dense matrix.
```

###### PCA

```scala
import org.apache.spark.mllib.linalg.Matrix
import org.apache.spark.mllib.linalg.distributed.RowMatrix

val mat: RowMatrix = ...

// Compute the top 10 principal components.
val pc: Matrix = mat.computePrincipalComponents(10) // Principal components are stored in a local dense matrix.

// Project the rows to the linear space spanned by the top 10 principal components.
val projected: RowMatrix = mat.multiply(pc)

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.feature.PCA

val data: RDD[LabeledPoint] = ...

// Compute the top 10 principal components.
val pca = new PCA(10).fit(data.map(_.features))

// Project vectors to the linear space spanned by the top 10 principal components, keeping the label
val projected = data.map(p => p.copy(features = pca.transform(p.features)))
```

##### 特征提取和转换

###### TFIDF

```scala
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector

val sc: SparkContext = ...

// Load documents (one per line).
val documents: RDD[Seq[String]] = sc.textFile("...").map(_.split(" ").toSeq)

val hashingTF = new HashingTF()
val tf: RDD[Vector] = hashingTF.transform(documents)

import org.apache.spark.mllib.feature.IDF

// ... continue from the previous example
tf.cache()
val idf = new IDF().fit(tf)
val tfidf: RDD[Vector] = idf.transform(tf)

import org.apache.spark.mllib.feature.IDF

// ... continue from the previous example
tf.cache()
val idf = new IDF(minDocFreq = 2).fit(tf)
val tfidf: RDD[Vector] = idf.transform(tf)
```


###### Word2Vec

```scala


import org.apache.spark._
import org.apache.spark.rdd._
import org.apache.spark.SparkContext._
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}

val input = sc.textFile("text8").map(line => line.split(" ").toSeq)

val word2vec = new Word2Vec()

val model = word2vec.fit(input)

val synonyms = model.findSynonyms("china", 40)

for((synonym, cosineSimilarity) <- synonyms) {
  println(s"$synonym $cosineSimilarity")
}

// Save and load model
model.save(sc, "myModelPath")
val sameModel = Word2VecModel.load(sc, "myModelPath")



```

###### 标准化

```scala


import org.apache.spark.SparkContext._
import org.apache.spark.mllib.feature.StandardScaler
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.util.MLUtils

val data = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_libsvm_data.txt")

val scaler1 = new StandardScaler().fit(data.map(x => x.features))
val scaler2 = new StandardScaler(withMean = true, withStd = true).fit(data.map(x => x.features))
// scaler3 is an identical model to scaler2, and will produce identical transformations
val scaler3 = new StandardScalerModel(scaler2.std, scaler2.mean)

// data1 will be unit variance.
val data1 = data.map(x => (x.label, scaler1.transform(x.features)))

// Without converting the features into dense vectors, transformation with zero mean will raise
// exception on sparse vector.
// data2 will be unit variance and zero mean.
val data2 = data.map(x => (x.label, scaler2.transform(Vectors.dense(x.features.toArray))))


```

###### normalizer

```scala


import org.apache.spark.SparkContext._
import org.apache.spark.mllib.feature.Normalizer
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.util.MLUtils

val data = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_libsvm_data.txt")

val normalizer1 = new Normalizer()
val normalizer2 = new Normalizer(p = Double.PositiveInfinity)

// Each sample in data1 will be normalized using $L^2$ norm.
val data1 = data.map(x => (x.label, normalizer1.transform(x.features)))

// Each sample in data2 will be normalized using $L^\infty$ norm.
val data2 = data.map(x => (x.label, normalizer2.transform(x.features)))


```

###### 特征选择 ChiSqSelector

```scala


import org.apache.spark.SparkContext._
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.mllib.feature.ChiSqSelector

// Load some data in libsvm format
val data = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_libsvm_data.txt")
// Discretize data in 16 equal bins since ChiSqSelector requires categorical features
val discretizedData = data.map { lp =>
  LabeledPoint(lp.label, Vectors.dense(lp.features.toArray.map { x => x / 16 } ) )
}
// Create ChiSqSelector that will select 50 features
val selector = new ChiSqSelector(50)
// Create ChiSqSelector model (selecting features)
val transformer = selector.fit(discretizedData)
// Filter the top 50 features from each feature vector
val filteredData = discretizedData.map { lp => 
  LabeledPoint(lp.label, transformer.transform(lp.features)) 
}


```

###### ElementwiseProduct

```scala


import org.apache.spark.SparkContext._
import org.apache.spark.mllib.feature.ElementwiseProduct
import org.apache.spark.mllib.linalg.Vectors

// Create some vector data; also works for sparse vectors
val data = sc.parallelize(Array(Vectors.dense(1.0, 2.0, 3.0), Vectors.dense(4.0, 5.0, 6.0)))

val transformingVector = Vectors.dense(0.0, 1.0, 2.0)
val transformer = new ElementwiseProduct(transformingVector)

// Batch transform and per-row transform give the same results:
val transformedData = transformer.transform(data)
val transformedData2 = data.map(x => transformer.transform(x))


```

###### 使用PCA 进行特征规约的例子

```scala


import org.apache.spark.mllib.regression.LinearRegressionWithSGD
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.feature.PCA

val data = sc.textFile("data/mllib/ridge-data/lpsa.data").map { line =>
  val parts = line.split(',')
  LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
}.cache()

val splits = data.randomSplit(Array(0.6, 0.4), seed = 11L)
val training = splits(0).cache()
val test = splits(1)

val pca = new PCA(training.first().features.size/2).fit(data.map(_.features))
val training_pca = training.map(p => p.copy(features = pca.transform(p.features)))
val test_pca = test.map(p => p.copy(features = pca.transform(p.features)))

val numIterations = 100
val model = LinearRegressionWithSGD.train(training, numIterations)
val model_pca = LinearRegressionWithSGD.train(training_pca, numIterations)

val valuesAndPreds = test.map { point =>
  val score = model.predict(point.features)
  (score, point.label)
}

val valuesAndPreds_pca = test_pca.map { point =>
  val score = model_pca.predict(point.features)
  (score, point.label)
}

val MSE = valuesAndPreds.map{case(v, p) => math.pow((v - p), 2)}.mean()
val MSE_pca = valuesAndPreds_pca.map{case(v, p) => math.pow((v - p), 2)}.mean()

println("Mean Squared Error = " + MSE)
println("PCA Mean Squared Error = " + MSE_pca)



```


###### 频繁项挖掘

```scala
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.fpm.{FPGrowth, FPGrowthModel}

val transactions: RDD[Array[String]] = ...

val fpg = new FPGrowth()
  .setMinSupport(0.2)
  .setNumPartitions(10)
val model = fpg.run(transactions)

model.freqItemsets.collect().foreach { itemset =>
  println(itemset.items.mkString("[", ",", "]") + ", " + itemset.freq)
}

```

#### 优化模块的开发

```scala


import org.apache.spark.SparkContext
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.mllib.optimization.{LBFGS, LogisticGradient, SquaredL2Updater}

val data = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_libsvm_data.txt")
val numFeatures = data.take(1)(0).features.size

// Split data into training (60%) and test (40%).
val splits = data.randomSplit(Array(0.6, 0.4), seed = 11L)

// Append 1 into the training data as intercept.
val training = splits(0).map(x => (x.label, MLUtils.appendBias(x.features))).cache()

val test = splits(1)

// Run training algorithm to build the model
val numCorrections = 10
val convergenceTol = 1e-4
val maxNumIterations = 20
val regParam = 0.1
val initialWeightsWithIntercept = Vectors.dense(new Array[Double](numFeatures + 1))

val (weightsWithIntercept, loss) = LBFGS.runLBFGS(
  training,
  new LogisticGradient(),
  new SquaredL2Updater(),
  numCorrections,
  convergenceTol,
  maxNumIterations,
  regParam,
  initialWeightsWithIntercept)

val model = new LogisticRegressionModel(
  Vectors.dense(weightsWithIntercept.toArray.slice(0, weightsWithIntercept.size - 1)),
  weightsWithIntercept(weightsWithIntercept.size - 1))

// Clear the default threshold.
model.clearThreshold()

// Compute raw scores on the test set.
val scoreAndLabels = test.map { point =>
  val score = model.predict(point.features)
  (score, point.label)
}

// Get evaluation metrics.
val metrics = new BinaryClassificationMetrics(scoreAndLabels)
val auROC = metrics.areaUnderROC()

println("Loss of each step in training process")
loss.foreach(println)
println("Area under ROC = " + auROC)


```


##### 应用例子

```scala


import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.sql.{Row, SQLContext}

val conf = new SparkConf().setAppName("SimpleParamsExample")
val sc = new SparkContext(conf)
val sqlContext = new SQLContext(sc)
import sqlContext.implicits._

// Prepare training data.
// We use LabeledPoint, which is a case class.  Spark SQL can convert RDDs of case classes
// into DataFrames, where it uses the case class metadata to infer the schema.
val training = sc.parallelize(Seq(
  LabeledPoint(1.0, Vectors.dense(0.0, 1.1, 0.1)),
  LabeledPoint(0.0, Vectors.dense(2.0, 1.0, -1.0)),
  LabeledPoint(0.0, Vectors.dense(2.0, 1.3, 1.0)),
  LabeledPoint(1.0, Vectors.dense(0.0, 1.2, -0.5))))

// Create a LogisticRegression instance.  This instance is an Estimator.
val lr = new LogisticRegression()
// Print out the parameters, documentation, and any default values.
println("LogisticRegression parameters:\n" + lr.explainParams() + "\n")

// We may set parameters using setter methods.
lr.setMaxIter(10)
  .setRegParam(0.01)

// Learn a LogisticRegression model.  This uses the parameters stored in lr.
val model1 = lr.fit(training.toDF)
// Since model1 is a Model (i.e., a Transformer produced by an Estimator),
// we can view the parameters it used during fit().
// This prints the parameter (name: value) pairs, where names are unique IDs for this
// LogisticRegression instance.
println("Model 1 was fit using parameters: " + model1.parent.extractParamMap)

// We may alternatively specify parameters using a ParamMap,
// which supports several methods for specifying parameters.
val paramMap = ParamMap(lr.maxIter -> 20)
paramMap.put(lr.maxIter, 30) // Specify 1 Param.  This overwrites the original maxIter.
paramMap.put(lr.regParam -> 0.1, lr.threshold -> 0.55) // Specify multiple Params.

// One can also combine ParamMaps.
val paramMap2 = ParamMap(lr.probabilityCol -> "myProbability") // Change output column name
val paramMapCombined = paramMap ++ paramMap2

// Now learn a new model using the paramMapCombined parameters.
// paramMapCombined overrides all parameters set earlier via lr.set* methods.
val model2 = lr.fit(training.toDF, paramMapCombined)
println("Model 2 was fit using parameters: " + model2.parent.extractParamMap)

// Prepare test data.
val test = sc.parallelize(Seq(
  LabeledPoint(1.0, Vectors.dense(-1.0, 1.5, 1.3)),
  LabeledPoint(0.0, Vectors.dense(3.0, 2.0, -0.1)),
  LabeledPoint(1.0, Vectors.dense(0.0, 2.2, -1.5))))

// Make predictions on test data using the Transformer.transform() method.
// LogisticRegression.transform will only use the 'features' column.
// Note that model2.transform() outputs a 'myProbability' column instead of the usual
// 'probability' column since we renamed the lr.probabilityCol parameter previously.
model2.transform(test.toDF)
  .select("features", "label", "myProbability", "prediction")
  .collect()
  .foreach { case Row(features: Vector, label: Double, prob: Vector, prediction: Double) =>
    println(s"($features, $label) -> prob=$prob, prediction=$prediction")
  }

sc.stop()


```


##### spark pipeline

```scala

```

#### graphx