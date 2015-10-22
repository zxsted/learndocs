#####model of SVDfeature
在协同过滤问题中有三个重要的因素：用户的兴趣，物品的特征，其他影响用户对物品的行为的因素。许多信息可以被用来对这些因素进行建模，例如用户对电影的浏览历史会反映出用户对电影的品味，电影的导演和演员的信息可以predict电影的特性。用户对电影评价的历史记录值会对用户对当前电影的喜好产生影响。  我们的模型使用向量总结了上述三中因素 （ $\alpha \in R^m,\beta \in \R^n, \gamma \in R^s $） ,喜好的评分公式如下：
$$ y^` = \left( \sum^s_{j=1}\gamma_j b_j^{g} + \sum^n_{j=1} \alpha_jb_j^{u} + \sum_{j=1}^m\beta_jb_j^{(i)}  \right)  + \left( \sum_{j=1}^n \beta_jp_j\right)^T\left( \sum_{j=1}^m\beta_jq_j\right)$$

这里介绍一下模型参数的定义 $\Theta = \{b^{g},b^{u},b^{i},p,q\}$,$p_j \in R^d$和$a_j \in R^d$ 是关于每个特制的d维隐形因子，$b_j^{(u)}$ $b_j^{(i)}$和 $b_j^{(g)}$ ,是直接影响偏好的因素。我们定义 $\alpha$ 为用户特征 $\beta$为商品特征 $\gamma$为全局（global）特征。

![][0]



[0]:http://img.blog.csdn.net/20131106175839156?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3NlcmNoZW4=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center