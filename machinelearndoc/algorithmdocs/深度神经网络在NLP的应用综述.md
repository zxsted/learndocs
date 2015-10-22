深度神经网络在NLP的应用综述

[toc]

#### 华为诺亚 机器翻译
[传送](http://www.csdn.net/article/2015-06-24/2825034)

##### 摘要

诺亚采取了一种独特的双边策略：既在传统翻译模型的架构下加入深度学习的组件，也同时尝试完全基于神经网络的深度架构。诺亚以一种以卷积神经网络（CNN）为主的架构作为其深度学习的主要工具。 

机器翻译，被认为是人工智能和自然语言处理的“圣杯”。上个世纪九十年代，机器翻译刚刚经历了一次变革，完成了从以规则为基础的方法到统计方法的变迁。而许多人相信我们正在经历第二次变革，这一次则是深度学习为主的方法。

深度学习是否能够取代传统的统计方法，业界现在尚无定论。但是对于很多前沿的机器翻译研究人员来说，他们其实更关心的是这次革命如何发生以及他们能否成为这次变革中的英雄。华为在香港和深圳的诺亚方舟实验室（下文简称“诺亚”）的研究员们就是其中的一份子。

诺亚资深研究员吕正东向CSDN介绍 ，诺亚自建立以来一直致力于数据处理和自然语言理解，机器翻译是其核心任务之一，深度学习已经成为诺亚研究机器翻译技术的主要工具。

诺亚采取了一种独特的双边策略：既在传统翻译模型的架构下加入深度学习的组件，也同时尝试完全基于神经网络的深度架构。诺亚以一种以卷积神经网络（CNN）为主的架构作为其深度学习的主要工具。CNN也是FFNN的一种特定形式，其在计算机视觉特别是图像识别方面已经取得了令人瞩目的成功，但是用于机器翻译还是相对新鲜的尝试。

吕正东介绍，诺亚最近的两项工作分别把CNN用于对源端的表示和对目标端句子的生成过程。

  *  在第一项工作中，CNN利用解码端的信号来对源端的句子进行有针对性的向量形式的总结，而这个方法在另一个FFNN的帮助下可以被直接用于解码过程。在传统机器模型中应用诺亚该CNN模型，BLEU值相对于传统模型可带来2个点的提升。【1】
  *  与第一项工作不同，第二项工作更贴近深度神经网络，所使用的架构是一种经过巧妙设计的CNN。当这个CNN模型被用来生成目标端的自然语言句子时，在BLEU值上也取得了不亚于上一个工作的提升。不仅如此，这个CNN模型还可以被直接用来对别的翻译模型生成的候选语句进行语言模型意义上的重排序，从而提升翻译语句的质量。【2】


在诺亚之前，机器翻译已经在短短两年时间内取得了惊人的进展，包含Google Brain、Raytheon BBN Technologies (BBN)、蒙特利尔大学和百度在内的多个研究机构都在不断地推进这项研究，当然也包括这些机构对深度学习的应用。

  *  Google Brain采取了一种最为极端的策略：以一个“编码”递归神经网络（RNN）发现对待翻译句子（源端）的表示，用另一个“解码”RNN将这个表示展开成（目标端）的句子。Google的模型有三亿多的参数，同时对训练数据的数量和平台都有较高要求。【3】
  *  蒙特利尔大学的策略和Google的接近，但是巧妙地利用一种自动对齐的策略来避免对源端的纯粹的向量表示，从而可以在较小规模的参数和数据集上也有较好的效果。【4】
  *  BBN的策略与前两者不同，他们更多的依赖传统模型而非深度的神经网络，但是在传统模型中增加了一个高效的前向神经网络（FFNN）的联合语言模型，从而大大提高了解码的效果。【5】
  *  百度新发布的翻译系统，应用了长短时记忆（LSTM，Long Short-Term Memory）的RNN模型。该模型擅长对自然语言建模，把任意长度的句子转化为特定维度的浮点数向量，同时“记住”句子中比较重要的单词，让“记忆”保存比较长的时间。该模型能够很好地解决自然语言句子向量化的难题。

去年获得ACL（自然语言处理领域的顶会）最佳论文奖的BBN模型，被广泛认为代表了国际先进水平，该模型相比较传统模型在BLEU值（用以衡量机器翻译效果）取得了1个点的提升。

诺亚的双边策略与Google Brain、BBN、和蒙特利尔大学的单边策略显然有所不同。在传统机器模型中应用诺亚第一项工作中的CNN模型，相对于BBN的最新模型也提升了1个点。诺亚第二项工作的提升，在深度神经网络也是非常好的效果。

由此看来，随着深度学习的普及应用，机器翻译的竞赛才刚刚开始且渐趋白热化。以深度学习为基础的框架是否能够取得优于传统模型的结果，其进展和速度如何，请您随时关注CSDN人工智能社区。如果您有好的人工智能技术成果分享，也欢迎投稿（zhoujd@csdn.net）。


##### 参考文献
【1】F. Meng, Z. Lu, M. Wang, H. Li, W. Jiang and Q. Liu. Encoding source language sentence with a convolutional neural network for machine translation. In Proceedings of ACL,2015.

【2】M. Wang, Z. Lu, H. Li, W. Jiang, and Q. Liu. genCNN: A convolutional architecture for wordsequence prediction. In Proceedings of ACL,2015.

【3】I. Sutskever, O. Vinyals, and Q. V. Le. Sequence to sequence learning with neural networks. In Advances in Neural Information Processing Systems, pages 3104-3112, 2014.

【4】D. Bahdanau, K. Cho, and Y. Bengio. Neural machine translation by jointly learning to align and translate. In Proceedings of ICLR, 2015.

【5】J. Devlin, R. Zbib,  Z. Huang, T. Lamar, R. Schwartz, and J. Makhoul. Fast and robust neural network joint models for statistical machine translation. In Proceedings of ACL, 2014



#### 有哪些LSTM(Long Short Term Memory)和RNN(Recurrent)网络的教程？ 

[传送](http://www.zhihu.com/question/29411132)

##### 先给出一个最快的了解+上手的教程：
  直接看theano官网的LSTM教程+代码：LSTM Networks for Sentiment Analysis

但是，前提是你有RNN的基础，因为LSTM本身不是一个完整的模型，LSTM是对RNN隐含层的改进。一般所称的LSTM网络全叫全了应该是使用LSTM单元的RNN网络。教程就给了个LSTM的图，它只是RNN框架中的一部分，如果你不知道RNN估计看不懂。

比较好的是，你只需要了解前馈过程，你都不需要自己求导就能写代码使用了。

补充，今天刚发现一个中文的博客：[LSTM简介以及数学推导(FULL BPTT)](http://blog.csdn.net/a635661820/article/details/45390671)

不过，稍微深入下去还是得老老实实的好好学，下面是我认为比较好的

##### 完整LSTM学习流程：

 我一直都觉得了解一个模型的前世今生对模型理解有巨大的帮助。到LSTM这里（假设题主零基础）那比较好的路线是MLP->RNN->LSTM。还有LSTM本身的发展路线（97年最原始的LSTM到forget gate到peephole再到CTC ）

按照这个路线学起来会比较顺，所以我优先推荐的两个教程都是按照这个路线来的：

  *  多伦多大学的 Alex Graves 的RNN专著《Supervised Sequence Labelling with Recurrent Neural Networks》
  *  Felix Gers的博士论文《Long short-term memory in recurrent neural networks》

这两个内容都挺多的，不过可以跳着看，反正我是没看完 ┑(￣Д ￣)┍

还有一个最新的（今年2015）的综述，《A Critical Review of Recurrent Neural Networks for Sequence Learning》不过很多内容都来自以上两个材料。

其他可以当做教程的材料还有：

《From Recurrent Neural Network to Long Short Term Memory Architecture Application to Handwriting Recognition Author》

《Generating Sequences With Recurrent Neural Networks》（这个有对应源码，虽然实例用法是错的，自己用的时候还得改代码，主要是摘出一些来用，供参考）

然后呢，可以开始编码了。除了前面提到的theano教程还有一些论文的开源代码，到github上搜就好了。

顺便安利一下theano，theano的自动求导和GPU透明对新手以及学术界研究者来说非常方便，LSTM拓扑结构对于求导来说很复杂，上来就写LSTM反向求导还要GPU编程代码非常费时间的，而且搞学术不是实现一个现有模型完了，得尝试创新，改模型，每改一次对应求导代码的修改都挺麻烦的。

其实到这应该算是一个阶段了，如果你想继续深入可以具体看看几篇经典论文，比如LSTM以及各个改进对应的经典论文。

还有楼上提到的《LSTM: A Search Space Odyssey》 通过从新进行各种实验来对比考查LSTM的各种改进（组件）的效果。挺有意义的，尤其是在指导如何使用LSTM方面。

不过，玩LSTM，最好有相应的硬件支持。我之前用Titan 780，现在实验室买了Titan X，应该可以说是很好的配置了（TitanX可以算顶配了）。但是我任务数据量不大跑一次实验都要好几个小时（前提是我独占一个显卡），（当然和我模型复杂有关系，LSTM只是其中一个模块）。

===========================================

如果想玩的深入一点可以看看LSTM最近的发展和应用。老的就不说了，就提一些比较新比较好玩的。

LSTM网络本质还是RNN网络，基于LSTM的RNN架构上的变化有最先的BRNN（双向），还有今年Socher他们提出的树状LSTM用于情感分析和句子相关度计算《Improved Semantic Representations From Tree-Structured Long Short-Term Memory Networks》（类似的还有一篇，不过看这个就够了）。他们的代码用Torch7实现，我为了整合到我系统里面自己实现了一个，但是发现效果并不好。我觉的这个跟用于建树的先验信息有关，看是不是和你任务相关。还有就是感觉树状LSTM对比BLSTM是有信息损失的，因为只能使用到子节点信息。要是感兴趣的话，这有一篇树状和线性RNN对比《(treeRNN vs seqRNN )When Are Tree Structures Necessary for Deep Learning of Representations?》。当然，关键在于树状这个概念重要，感觉现在的研究还没完全利用上树状的潜力。

今年ACL（2015）上有一篇层次的LSTM《A Hierarchical Neural Autoencoder for Paragraphs and Documents》。使用不同的LSTM分别处理词、句子和段落级别输入，并使用自动编码器（autoencoder）来检测LSTM的文档特征抽取和重建能力。

还有一篇文章《Chung J, Gulcehre C, Cho K, et al. Gated feedback recurrent neural networks[J]. arXiv preprint arXiv:1502.02367, 2015.》，把gated的思想从记忆单元扩展到了网络架构上，提出多层RNN各个层的隐含层数据可以相互利用（之前的多层RNN多隐含层只是单向自底向上连接），不过需要设置门（gated）来调节。

记忆单元方面，Bahdanau Dzmitry他们在构建RNN框架的机器翻译模型的时候使用了GRU单元（gated recurrent unit）替代LSTM，其实LSTM和GRU都可以说是gated hidden unit。两者效果相近，但是GRU相对LSTM来说参数更少，所以更加不容易过拟合。（大家堆模型堆到dropout也不管用的时候可以试试换上GRU这种参数少的模块）。这有篇比较的论文《（GRU/LSTM对比）Empirical Evaluation of Gated Recurrent Neural Networks on Sequence Modeling》


##### LSTM 的应用

应用嘛，宽泛点来说就是挖掘序列数据信息，大家可以对照自己的任务有没有这个点。比如（直接把毕设研究现状搬上来(｡･∀･)ﾉﾞ）：

先看比较好玩的，

###### 图像处理（对，不用CNN用RNN）：

《Visin F, Kastner K, Cho K, et al. ReNet: A Recurrent Neural Network Based Alternative to Convolutional Networks[J]. arXiv preprint arXiv:1505.00393, 2015》

4向RNN（使用LSTM单元）替代CNN。

###### 使用LSTM读懂python程序：

《Zaremba W, Sutskever I. Learning to execute[J]. arXiv preprint arXiv:1410.4615, 2014.》

使用基于LSTM的深度模型用于读懂python程序并且给出正确的程序输出。文章的输入是短小简单python程序，这些程序的输出大都是简单的数字，例如0-9之内加减法程序。模型一个字符一个字符的输入python程序，经过多层LSTM后输出数字结果，准确率达到99%

###### 手写识别：

《Liwicki M, Graves A, Bunke H, et al. A novel approach to on-line handwriting recognition based on bidirectional long short-term memory》

###### 机器翻译：

《Sutskever I, Vinyals O, Le Q V V. Sequence to sequence learning with neural networks[C]//Advances in neural information processing systems. 2014: 3104-3112.》

使用多层LSTM构建了一个seq2seq框架（输入一个序列根据任务不同产生另外一个序列），用于机器翻译。先用一个多层LSTM从不定长的源语言输入中学到特征v。然后使用特征v和语言模型（另一个多层LSTM）生成目标语言句子。

《Cho K, Van Merriënboer B, Gulcehre C, et al. Learning phrase representations using rnn encoder-decoder for statistical machine translation[J]. arXiv preprint arXiv:1406.1078, 2014.》

这篇文章第一次提出GRU和RNN encoder-decoder框架。使用RNN构建编码器-解码器（encoder-decoder）框架用于机器翻译。文章先用encoder从不定长的源语言输入中学到固定长度的特征V，然后decoder使用特征V和语言模型解码出目标语言句子

以上两篇文章提出的seq2seq和encoder-decoder这两个框架除了在机器翻译领域，在其他任务上也被广泛使用。

《Bahdanau D, Cho K, Bengio Y. Neural machine translation by jointly learning to align and translate[J]. arXiv preprint arXiv:1409.0473, 2014.》

在上一篇的基础上引入了BRNN用于抽取特征和注意力信号机制（attention signal）用于源语言和目标语言的对齐。

###### 对话生成：

《Shang L, Lu Z, Li H. Neural Responding Machine for Short-Text Conversation[J]. arXiv preprint arXiv:1503.02364, 2015.》

华为诺亚方舟实验室，李航老师他们的作品。基本思想是把对话看成是翻译过程。然后借鉴Bahdanau D他们的机器翻译方法（encoder-decoder，GRU，attention signal）解决。训练使用微博评论数据。

《VINYALS O, LE Q，.A Neural Conversational Model[J]. arXiv:1506.05869 [cs], 2015.》
google前两天出的论文（2015-6-19）。看报道说结果让人觉得“creepy”：Google's New Chatbot Taught Itself to Be Creepy 。还以为有什么NB模型，结果看了论文发现就是一套用seq2seq框架的实验报告。（对话可不是就是你一句我一句，一个序列对应产生另一序列么）。论文里倒是说的挺谨慎的，只是说纯数据驱动（没有任何规则）的模型能做到这样不错了，但还是有很多问题，需要大量修改（加规则呗？）。主要问题是缺乏上下文一致性。（模型只用对话的最后一句来产生下一句也挺奇怪的，为什么不用整个对话的历史信息？）

###### 句法分析：

《Vinyals O, Kaiser L, Koo T, et al. Grammar as a foreign language[J]. arXiv preprint arXiv:1412.7449, 2014.》

把LSTM用于句法分析任务，文章把树状的句法结构进行了线性表示，从而把句法分析问题转成翻译问题，然后套用机器翻译的seq2seq框架使用LSTM解决。

###### 信息检索：

《Palangi H, Deng L, Shen Y, et al. Deep Sentence Embedding Using the Long Short Term Memory Network: Analysis and Application to Information Retrieval[J]. arXiv preprint arXiv:1502.06922, 2015.》

使用LSTM获得大段文本或者整个文章的特征向量，用点击反馈来进行弱监督，最大化query的特性向量与被点击文档的特性向量相似度的同时最小化与其他未被点击的文档特性相似度。

###### 图文转换：

图文转换任务看做是特殊的图像到文本的翻译问题，还是使用encoder-decoder翻译框架。不同的是输入部分使用卷积神经网络（Convolutional Neural Networks，CNN）抽取图像的特征，输出部分使用LSTM生成文本。对应论文有：

《Karpathy A, Fei-Fei L. Deep visual-semantic alignments for generating image descriptions[J]. arXiv preprint arXiv:1412.2306, 2014.》

《Mao J, Xu W, Yang Y, et al. Deep captioning with multimodal recurrent neural networks (m-rnn)[J]. arXiv preprint arXiv:1412.6632, 2014.》

《Vinyals O, Toshev A, Bengio S, et al. Show and tell: A neural image caption generator[J]. arXiv preprint arXiv:1411.4555, 2014.》


就粘这么多吧，呼呼~复制粘贴好爽\(^o^)/~

其实，相关工作还有很多，各大会议以及arxiv上不断有新文章冒出来，实在是读不过来了。。。


##### vanishing grading 的两种表现
这里顺便可以跑题提一下在 Deep Learning 里经常会听到的 Vanishing Gradient 问题，因为 back propagation 是用 chain rule 将导数乘到一起，粗略地讲，如果每一层的导数都“小于一”的话，在层数较多的情况下很容易到后面乘着乘着就接近零了。反过来如果每一层的导数都“大于一”的话，gradient 乘到最后又会出现 blow up 的问题。人们发明了很多技术来处理这些问题，不过那不是今天的话题。

##### 附加动量法

 

附加动量法使网络在修正其权值时，不仅考虑误差在梯度上的作用，

而且考虑在误差曲面上变化趋势的影响。在没有附加动量的作用下，网络

可能陷入浅的局部极小值，利用附加动量的作用有可能滑过这些极小值。

 

该方法是在反向传播法的基础上在每一个权值（或阈值）的变化上加

上一项正比于前次权值（或阈值）变化量的值，并根据反向传播法来产生

新的权值（或阈值）变化。

 由于学习速率是固定的，因此网络的收敛速度慢，需要较长的训练时间。对于一些复杂问题，BP算法需要的训练时间可能非常长，这主要是由于学习速率太小造成的，可采用变化的学习速率或自适应的学习速率加以改进。

其次，BP算法可以使权值收敛到某个值，但并不保证其为误差平面的全局最小值，这是因为采用梯度下降法可能产生一个局部最小值。对于这个问题，可以采用附加动量法来解决。

再次，网络隐含层的层数和单元数的选择尚无理论上的指导，一般是根据经验或者通过反复实验确定。因此，网络往往存在很大的冗余性，在一定程度上也增加了网络学习的负担。

最后，网络的学习和记忆具有不稳定性。也就是说，如果增加了学习样本，训练好的网络就需要从头开始训练，对于以前的权值和阈值是没有记忆的。但是可以将预测、分类或聚类做的比较好的权值保存。

  



#####  Deep Learning论文笔记之（四）CNN卷积神经网络推导和实现 

[传送](http://blog.csdn.net/zouxy09/article/details/9993371)


##### 机器翻译自动评测方法BLEU值方法
[传送](http://m.blog.csdn.net/blog/hellonlp/8154615)


##### 概率图模型系列 （PGM） 

[传送](http://www.cnblogs.com/tornadomeet/category/361811.html)


##### 迁移学习 (皮果提) 
[传送](http://blog.csdn.net/itplus/article/details/14120413)

##### pi9nc  
[传送](http://blog.csdn.net/pi9nc/article/category/1262464/2)

##### MLAPP——机器学习的概率知识总结 
[杨乐](http://blog.csdn.net/u010487568/article/details/39997189)

##### hadoop深入研究:(八)——codec 
[传送](http://blog.csdn.net/lastsweetop/article/details/9173061)

##### 图算法
[传送](http://blog.csdn.net/lastsweetop/article/category/468851)


#### 语义分析的一些方法

[传送](http://dataunion.org/10781.html)

##### 上篇 (统计概率)
[传送](http://dataunion.org/?p=10748)
##### 中篇 (topic model)
[传送](http://dataunion.org/?p=10760)
##### 下篇 （图片语义理解）
[传送](http://dataunion.org/10781.html)



#### 我们是这样理解语言的

##### 中篇 统计语言模型
 [传送](http://www.flickering.cn/nlp/2015/02/%E6%88%91%E4%BB%AC%E6%98%AF%E8%BF%99%E6%A0%B7%E7%90%86%E8%A7%A3%E8%AF%AD%E8%A8%80%E7%9A%84-2%E7%BB%9F%E8%AE%A1%E8%AF%AD%E8%A8%80%E6%A8%A1%E5%9E%8B/)
 
##### 下篇 神经网络语言模型
 [](http://www.flickering.cn/nlp/2015/03/%E6%88%91%E4%BB%AC%E6%98%AF%E8%BF%99%E6%A0%B7%E7%90%86%E8%A7%A3%E8%AF%AD%E8%A8%80%E7%9A%84-3%E7%A5%9E%E7%BB%8F%E7%BD%91%E7%BB%9C%E8%AF%AD%E8%A8%80%E6%A8%A1%E5%9E%8B/)

#### Peacock：大规模主题模型及其在腾讯业务中的应用
[传送](http://www.flickering.cn/nlp/2015/03/peacock%EF%BC%9A%E5%A4%A7%E8%A7%84%E6%A8%A1%E4%B8%BB%E9%A2%98%E6%A8%A1%E5%9E%8B%E5%8F%8A%E5%85%B6%E5%9C%A8%E8%85%BE%E8%AE%AF%E4%B8%9A%E5%8A%A1%E4%B8%AD%E7%9A%84%E5%BA%94%E7%94%A8/)

#### LDA 与 PLSA 之间的区别

LDA克服了PLSA的缺点，LDA为每一个文档的topics的分布给了一个dirichlet distribution的prior。而PLSA没有。有人算过了，PLSA其实和LDA中取uniform dirichlet distribution的MAP/ML估计是一样的。从图上来说的话，产生式过程中，LDA在文档这一级别的外层多了一个参数a，用来生成不同文档中topics的分布。而PLSA，要么分开为每个文档单独分配topics的分布，要么就给一个分布假设吧。所以这样来说，LDA比PLSA更有鲁棒性？

就是 lda 给 doc-topic 分布添加了一个 先验分布 ，这样就不用每个文档一个 doc - topic 分布了， 而plsa 需要每个文档一个 doc-topic 分布 ， 更文档个数一样。  文档树一多， plsa的参数就越多 。 

LDA只是拉开了一个序幕，nonparametric 的hierarchical dirichlet processes和gaussian process才是漂亮的地方


PLSA可能发生的问题是overfitting，加入multinomial的conjugate prior是解决overfitting的一个思路。尝试对PLSA的参数加入一些先验，就会得到LDA最基本的形式。理解LDA的难点不仅在于如何理解gibbs sampling 或是variation bayes 或EP. 而在于理解后验无法拆解成来解. P(W)没有闭式解,所以就只能依赖蒙特卡罗等方法做近似.


从工程应用价值的角度看，这个数学方法的generalization，允许我们用一个训练好的模型解释任何一段文本中的语义。而pLSA只能理解训练文本中的语义。（虽然也有ad hoc的方法让pLSA理解新文本的语义，但是大都效率低，并且并不符合pLSA的数学定义。）这就让继续研究pLSA价值不明显了。”

NMF：一种矩阵分解，要求输入矩阵元素非负，目标和 SVD 一样。
pLSA：SVD 的一种概率解释方法——要求矩阵元素是非负整数。
LDA：pLSA 加上 topics 的 Dirichlet 先验分布后得到的 Bayesian model，数学上更漂亮。为什么是 Dirichlet 先验分布，主要是利用了 Dirichlet 和 multinomial 分布的共轭性，方便计算。 

google在07年左右或者更早的时候就抛弃pLSA转向LDA了吧。pLSA只能对训练样本中进行语义识别，而对不在样本中的文本是无法识别其语义的。而LDA能。
目前LDA的挑战主要在于长尾分类这块，Google推出Rephil解决这个问题,借此Google Adsense的收入


##### 最合理的解释


[传送](http://blog.csdn.net/feixiangcq/article/details/5649135)
而PLSA其实也是这个链，那它和LDA有什么区别呢？

最大的区别就在于，doc~topic这一级，PLSA把这一级的所有变量都看作模型的参数，即有多少文档那么就有多少模型的参数；而LDA引入了一个超 参数，对doc~topic这一个层级进行model。这样无论文档有多少，那么最外层模型显露出来的［对于doc~topic］就只有一个超参数。

那么加什么先验呢？
最基本的PLSA和LDA在刻画doc~topic和topic~word都利用了一个模型，就是multinomial model。为了计算的方便及先验的有意义，共轭先验是首选。multinomial distribution的共轭分布是Dirichlet distribution，很nice的一个分布。这也是Latent Dirichlet Allocation中Dirichlet的由来。


##### 最合理的解释的复杂版（数学理论更明白）
[LDA算法漫游指南](http://yuedu.baidu.com/ebook/d0b441a8ccbff121dd36839a?fr=booklist###)

[传送](http://www.zhihu.com/question/23642556/answer/38969800)


###### 关键点： plsa 概率生成过程， 模型参数组成

   pLSA的另一个名称是probabilistic latent semantic indexing(pLSI)，假设在一篇文档d中，主题用c来表示，词用w来表示，则有如下公式：
   $$p(w,d) = \sum_cP(c)P(d|c)P(w|c) = P(d) \sum_c P(c|d)p(w|c)$$
   
 对上面模型的解释：
   第一个等式是对称形式，其主要思路是认为文档和词都按照一定的概率分布（分别是P(d|c)和P(w|c)）从主题c中产生；
   第二个等式是非对称形式，更符合我们的直觉，主要思路是从该文档中按照一定概率分布选择一个主题（即P(c|d)），然后再从该主题中选择这个词，这个概率对应是P(w|c)，这个公式恰好和上文所讲的一致。即把这里的非对称形式的公式左右都除以P(d)便得到下面这个公式：
   $$ P(词|文档) = \sum_(主题)p(词|主题)p(主题|文档)$$
   
   盒盘图：
   ![](http://pic2.zhimg.com/a0f83b653d755d9dc799fc36015905c9_b.jpg)
   
   最大的矩形（盘子）里装有M个小盘子，即有M篇文档，每一篇文档d自身有个概率P(d)，从d到主题c有一个概率分布P(c|d)，随后从主题c到词w又是一个概率分布P(w|c)，由此构成了w和c的联合概率分布P(w,d).

 即有M篇文档，每一篇文档d自身有个概率P(d)，从d到主题c有一个概率分布P(c|d)，随后从主题c到词w又是一个概率分布P(w|c)，由此构成了w和c的联合概率分布P(w,d).

**pLSA的参数个数是cd+wc，所以参数个数随着文档d的增加而线性增加**。但是很重要的的是，pLSA只是对已有文档的建模，也就是说生成模型只是适合于这些用以训练pLSA算法的文档，并不是新文档的生成模型。这一点很重要，因为我们后文要说的pLSA很容易过拟合，还有LDA为了解决这些问题引入的狄利克雷分布都与此有关。 

###### LDA的优势
 在LDA中，每一篇文档都被看做是有一系列主题，在这一点上和pLSA是一致的。实际上，LDA的不同之处在于，**pLSA的主题的概率分布P(c|d)是一个确定的概率分布,(因为是统计model 不是bayes model)**，也就是虽然主题c不确定，但是c符合的概率分布是确定的，比如符合高斯分布，这个高斯分布的各参数是确定的，但是**在LDA中，这个高斯分布都是不确定的，高斯分布又服从一个狄利克雷先验分布(Dirichlet prior)**，说的绕口一点是主题的概率分布的概率分布，除了主题有这个特点之外，另外词在主题下的分布也不再是确定分布，同样也服从一个狄利克雷先验分布。所以实际上LDA是pLSA的改进版，延伸版。

这个改进有什么好处呢？就是我们上文说的**pLSA容易过拟合**，何谓过拟合？过拟合就是训练出来的模型对训练数据有很好的表征能力，但是一应用到新的训练数据上就挂了。这就是所谓的泛化能力不够。我们说一个人适应新环境的能力不行，也可以说他在他熟悉的环境里过拟合了。

可以从参数个数进行比较 ，原则上 同等训练集上 模型参数越多 模型越易过拟合， plsa 参数个数为WC+ DC    

那为什么pLSA容易过拟合，而LDA就这么牛逼呢？这个要展开讲，可以讲好多好多啊，可以扯到频率学派和贝叶斯学派关于概率的争论，这个争论至今悬而未决，在这里，我讲一下我自己的看法，说的不对的，希望指正。

pLSA中，主题的概率分布P(c|d)和词在主题下的概率分布P(w|c)既然是概率分布，那么就必须要有样本进行统计才能得到这些概率分布。更具体的讲，主题模型就是为了做这个事情的，训练已获得的数据样本，得到这些参数，那么一个pLSA模型便得到了，但是这个时候问题就来了：这些参数是建立在训练样本上得到的。这是个大问题啊！你怎么能确保新加入的数据同样符合这些参数呢？你能不能别这么草率鲁莽？但是频率学派就有这么任性，他们认为参数是存在并且是确定的， 只是我们未知而已，并且正是因为未知，我们才去训练pLSA的，训练之后得到的参数同样适合于新加入的数据，因为他们相信参数是确定的，既然适合于训练数据，那么也同样适合于新加入的数据了。

但是真实情况却不是这样，尤其是训练样本量比较少的情况下的时候，这个时候首先就不符合大数定律的条件（这里插一句大数定律和中心极限定律，在无数次独立同分布的随机事件中，事件的频率趋于一个稳定的概率值，这是大数定律；而同样的无数次独立同分布的随机事件中，事件的分布趋近于一个稳定的正态分布，而这个正太分布的期望值正是大数定律里面的概率值。所以，中心极限定理比大数定律揭示的现象更深刻，同时成立的条件当然也要相对来说苛刻一些。 非数学系出身，不对请直接喷），所以频率并不能很好的近似于概率，所以得到的参数肯定不好。我们都知道，概率的获取必须以拥有大量可重复性实验为前提，但是这里的主题模型训练显然并不能在每个场景下都有大量的训练数据。所以，当训练数据量偏小的时候，pLSA就无可避免的陷入了过拟合的泥潭里了。为了解决这个问题，LDA给这些参数都加入了一个先验知识，就是当数据量小的时候，我人为的给你一些专家性的指导，你这个参数应该这样不应该那样。比如你要统计一个地区的人口年龄分布，假如你手上有的训练数据是一所大学的人口数据，统计出来的结果肯定是年轻人占比绝大多数，这个时候你训练出来的模型肯定是有问题的，但是我现在加入一些先验知识进去，专家认为这个地区中老年人口怎么占比这么少？不行，我得给你修正修正，这个时候得到的结果就会好很多。所以LDA相比pLSA就优在这里，它对这些参数加入了一些先验的分布进去。（但是我这里并没有任何意思说贝叶斯学派优于频率学派，两学派各有自己的优势领域，比如很多频率学派对贝叶斯学派的攻击点之一是，在模型建立过程中，贝叶斯学派加入的先验知识难免主观片面，并且很多时候加入都只是为了数学模型上运算的方便。我这里只是举了一个适合贝叶斯学派的例子而已）

但是，当训练样本量足够大，pLSA的效果是可以等同于LDA的，因为过拟合的原因就是训练数据量太少，当把数据量提上去之后，过拟合现象会有明显的改观。


#### 主题模型的引入的原理和作用

主题模型是一个统计模型，用来抽离出一批文档中的“主题”。直觉上，已知一篇文档的一个特定主题，则我们有理由相信一些词会更可能出现在这篇文档，“狗”和“骨头”更有可能出现在一篇有关于狗的文档中，“猫”和“喵”更有可能出现在有关于猫的文档中，而英语当中的“the”和“is”在这些文档中出现的概率相当。一般来说一篇文档都含有多个主题，这些主题之间所占比例有所不同，一篇文档10%是有关猫90%有关狗，那么这篇文档“狗”这个词出现的次数可能会是“猫”的9倍。

如果上边的说法还不够形象，下边有两句话：

“马云、马化腾和李彦宏”

“阿里巴巴、腾讯和百度掌门人”

如果按照jaccard距离来表征两句话的相似性，将会得到两句话完全不相干的错误结论，这显然是不对的。但是一进行语义分析才发现这两句话其实说的完全一回事。或者更准确的说，**在这里把两句话的主题抽离出来**，将会得到“企业家”、“互联网”、“BAT”等等这些主题，这样就发现两句话主题上是完全相同的，由此可知这两句话具有很高的相似性。

主题模型中的主题实际就是一个标签，用这个标签尽最大可能去概括一段话或一篇文档的内容。这里有点类似小学时候经常被老师要求“总结中心思想”的作用，主题在这里就是“中心思想”。但又不仅仅是“中心思想”，数学上的主题模型更像一个语料库，在这个语料库里每一个词都有一个对应的概率分布去表征这个词的出现，在特定的主题里边，有些词出现概率高，另外一些词出现概率低，比如上边的主题是“狗”的文档中，“骨头”出现的概率就要比“喵”高。


##### plda 和 LDA  样本生成过程

###### plsa :  
 1. 以概率 P(di) 概率选择文档di
 2. 以p(zk | di) 的概率选择主题 zk
 3. 以P（wj| zk） 选择 生成一个单词
 这样就生成了一个（di，wj） 对， 
 (di,wj) 对的联合概率为：
  P(di,wj) = p(di)p(wj | di) p(wj|di) = sum_k p(wj|zk)p(zk|di)
  而 P（zk|di） 和 P(wj|zk) 对应了两组Multinomial分布， 我们就是求他们的参数
  
  
  
 









