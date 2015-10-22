### MarkDown 笔记

* Markdown 语法的目标是： 成为一种适用于网络书写的语言  
* Markdown 不是要取代 HTML 它的语法种类很少，只对应HTML标记中的一小部分，Markdown是一种书写格式，而HTML是一种发布格式，Markdown的格式语法致函该文本可以覆盖的范围。
* Markdown涵盖范围之内的标签都可以直接在文档里面使用HTML撰写。不需要额外标注这是HTML还是Markdown，只要直接加标签就可以了
* 但是一些区块元素还是需要一些制约的， 如div、table、 pre、p等标签，必须在前后加上空行与其他内容区隔开，并要求他们的开始标签和结束标签不能用制表符或者空格来缩进。Markdown 的生成器具有足够的智能，不会在HTML区块标签外加上不必要的p标签 

下面的例子是在markdown文件里加上一段HTML表格：  

这是一个普通的段落：
<table>
	<tr>
    	<td>Foo </td> <td></td>
    <tr>
</table>

 这是另一个普通段落  



<!--   使用javascript调用 mathjax 在页面中动态嵌入公式：
<script type="text/javascript" src="http://cdn.mathjax.org/mathjax/latest/MathJax.js?config=default"></script>

$$J\alpha(x) = \sum_{m=0}^\infty \frac{(-1)^m}{m! \Gamma (m + \alpha + 1)} {\left({ \frac{x}{2} }\right)}^{2m + \alpha}$$



这是行内的数学公式： $J\alpha(x) = \sum_{m=0}^\infty \frac{(-1)^m}{m! \Gamma (m + \alpha + 1)} {\left({ \frac{x}{2} }\right)}^{2m + \alpha}$
--》



<!-- 一个在线数学公式的引擎
<img src="http://www.forkosh.com/mathtex.cgi? \Large x=\frac{-b\pm\sqrt{b^2-4ac}}{2a}">-->


<!--插入数学公式的在线网址<img src="http://chart.googleapis.com/chart?cht=tx&chl=\Large " style="border:none;">-->

<!-- 另一个插入公式的在线网址格式较为清晰 <img src = "http://tex.72pines.com/latex.php?latex=$LaTeX公式代码$">-->



