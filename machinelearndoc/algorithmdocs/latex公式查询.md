Markdown输入数学公式
2014/05/04 acheng99	1条评论

原来markdown里是可以输入数学公式的，不过，不是一般的麻烦 Rstudio上有介绍：https://support.rstudio.com/hc/en-us/articles/200486328-Equations-in-R-Markdown 直接从网友写好的blog里搬一个过来，备用备查：

不知道怎么在wp里排版，效果很差，只能备用，特别是特殊符号，语法应该用一段时间就熟悉了。。。。
2014-6-10日更新

自己安装mathjax

git clone git://github.com/mathjax/MathJax.git MathJax
去掉多余的文件

rm -rf MathJax/.git*

上传到空间，在配置里自定义mathjax的路径
以下是转来的内容

MathJax是一款相当强悍的在网页显示数学公式的插件。本教程介绍MathJax如何使用LaTeX语法编写数学公式。

1．如何插入公式

LaTeX的数学公式有两种：行中公式和独立公式。行中公式放在文中与其它文字混编，独立公式单独成行。

行中公式可以用如下两种方法表示：

＼(数学公式＼)　或　￥数学公式￥（要把人民币符号换成美元符号）

独立公式可以用如下两种方法表示：

＼[数学公式＼]　或　￥￥数学公式￥￥（要把人民币符号换成美元符号）

例子：＼[J\alpha(x) = \sum{m=0}^\infty \frac{(-1)^m}{m! \Gamma (m + \alpha + 1)} {\left({ \frac{x}{2} }\right)}^{2m + \alpha}＼]

显示： J<em>\alpha(x) = \sum</em>{m=0}^\infty \frac{(-1)^m}{m! \Gamma (m + \alpha + 1)} {\left({ \frac{x}{2} }\right)}^{2m + \alpha}

2．如何输入上下标

^表示上标, _表示下标。如果上下标的内容多于一个字符，要用{}把这些内容括起来当成一个整体。上下标是可以嵌套的，也可以同时使用。

例子：x^{y^z}=(1+{\rm e}^x)^{-2xy^w}

显示： x^{y^z}=(1+{\rm e}^x)^{-2xy^w}

另外，如果要在左右两边都有上下标，可以用\sideset命令。

例子：\sideset{^12}{^34}\bigotimes

显示： \sideset{^1<em>2}{^3</em>4}\bigotimes

3．如何输入括号和分隔符

()、[]和|表示自己，{}表示{}。当要显示大号的括号或分隔符时，要用\left和\right命令。

例子：f(x,y,z) = 3y^2z \left( 3+\frac{7x+5}{1+y^2} \right)

显示： f(x,y,z) = 3y^2z \left( 3+\frac{7x+5}{1+y^2} \right)

有时候要用\left.或\right.进行匹配而不显示本身。

例子：\left. \frac{{\rm d}u}{{\rm d}x} \right| _{x=0}

显示： \left. \frac{{\rm d}u}{{\rm d}x} \right| _{x=0}

4．如何输入分数

例子：\frac{1}{3}　或　1 \over 3

显示： \frac{1}{3} 　或 1 \over 3

5．如何输入开方

例子：\sqrt{2}　和　\sqrt[n]{3}

显示： \sqrt{2} 　和　 \sqrt[n]{3}

6．如何输入省略号

数学公式中常见的省略号有两种，\ldots表示与文本底线对齐的省略号，\cdots表示与文本中线对齐的省略号。

例子：f(x1,x2,\ldots,xn) = x1^2 + x2^2 + \cdots + xn^2

显示： f(x<em>1,x</em>2,\ldots,x<em>n) = x</em>1^2 + x<em>2^2 + \cdots + x</em>n^2

7．如何输入矢量

例子：\vec{a} \cdot \vec{b}=0

显示： \vec{a} \cdot \vec{b}=0

8．如何输入积分

例子：\int_0^1 x^2 {\rm d}x

显示： \int_0^1 x^2 {\rm d}x

9．如何输入极限运算

例子：\lim_{n \rightarrow +\infty} \frac{1}{n(n+1)}

显示： \lim_{n \rightarrow +\infty} \frac{1}{n(n+1)}

10．如何输入累加、累乘运算

例子：\sum{i=0}^n \frac{1}{i^2}　和　\prod{i=0}^n \frac{1}{i^2}

显示： \sum<em>{i=0}^n \frac{1}{i^2} 　和　 \prod</em>{i=0}^n \frac{1}{i^2}

11．如何进行公式应用

先要在［mathjax］后添加：

＜script type="text/x-mathjax-config"＞ MathJax.Hub.Config({ TeX: {equationNumbers: { autoNumber: ["AMS"], useLabelIds: true}}, "HTML-CSS": {linebreaks: {automatic: true}}, SVG: {linebreaks: {automatic: true}} }); ＜/script＞

例子：＼begin{equation}\label{equation1}r = rF+ \beta(rM – r_F) + \epsilon＼end{equation}

显示：\begin{equation}\label{equation1}r = rF+ \beta(rM – r_F) + \epsilon\end{equation}

引用：请见公式( \ref{equation1} )

12．如何输入希腊字母

例子： \alpha　A　\beta　B　\gamma　\Gamma　\delta　\Delta　\epsilon　E \varepsilon　　\zeta　Z　\eta　H　\theta　\Theta　\vartheta \iota　I　\kappa　K　\lambda　\Lambda　\mu　M　\nu　N \xi　\Xi　o　O　\pi　\Pi　\varpi　　\rho　P \varrho　　\sigma　\Sigma　\varsigma　　\tau　T　\upsilon　\Upsilon \phi　\Phi　\varphi　　\chi　X　\psi　\Psi　\omega　\Omega

显示： \alpha \beta \gamma \Gamma \delta \Delta

\epsilon \varepsilon 　 \zeta \eta \theta \Theta 　

\vartheta \iota \kappa \lambda \Lambda 　 \mu \nu \xi

\Xi 　 \pi \Pi 　 \varpi 　 \rho \varrho 　

\sigma \Sigma \varsigma 　 \tau \upsilon \Upsilon \phi

\Phi 　 \varphi 　 \chi \psi \Psi 　 \omega \Omega

13．如何输入其它特殊字符

关系运算符： \pm ：\pm

\times ：\times

\div ：\div

\mid ：\mid

\nmid ：\nmid

\cdot ：\cdot

\circ ：\circ

\ast ：\ast

\bigodot ：\bigodot

\bigotimes ：\bigotimes

\bigoplus ：\bigoplus

\leq ：\leq

\geq ：\geq

\neq ：\neq

\approx ：\approx

\equiv ：\equiv

\sum ：\sum

\prod ：\prod

\coprod ：\coprod

集合运算符： \emptyset ：\emptyset

\in ：\in

\notin ：\notin

\subset ：\subset

\supset ：\supset

\subseteq ：\subseteq

\supseteq ：\supseteq

\bigcap ：\bigcap

\bigcup ：\bigcup

\bigvee ：\bigvee

\bigwedge ：\bigwedge

\biguplus ：\biguplus

\bigsqcup ：\bigsqcup

对数运算符： \log ：\log

\lg ：\lg

\ln ：\ln

三角运算符： \bot ：\bot

\angle ：\angle

30^\circ ：30^\circ

\sin ：\sin

\cos ：\cos

\tan ：\tan

\cot ：\cot

\sec ：\sec

\csc ：\csc

微积分运算符： \prime ：\prime

\int ：\int

\iint ：\iint

\iiint ：\iiint

\iiiint ：\iiiint

\oint ：\oint

\lim ：\lim

\infty ：\infty

\nabla ：\nabla

逻辑运算符： \because ：\because

\therefore ：\therefore

\forall ：\forall

\exists ：\exists

\not= ：\not=

\not> ：\not>

\not\subset ：\not\subset

戴帽符号： \hat{y} ：\hat{y}

\check{y} ：\check{y}

\breve{y} ：\breve{y}

连线符号： \overline{a+b+c+d} ：\overline{a+b+c+d}

\underline{a+b+c+d} ：\underline{a+b+c+d}

\overbrace{a+\underbrace{b+c}<em>{1.0}+d}^{2.0} ：\overbrace{a+\underbrace{b+c}{1.0}+d}^{2.0}

箭头符号： \uparrow ：\uparrow

\downarrow ：\downarrow

\Uparrow ：\Uparrow

\Downarrow ：\Downarrow

\rightarrow ：\rightarrow

\leftarrow ：\leftarrow

\Rightarrow ：\Rightarrow

\Leftarrow ：\Leftarrow

\longrightarrow ：\longrightarrow

\longleftarrow ：\longleftarrow

\Longrightarrow ：\Longrightarrow

\Longleftarrow ：\Longleftarrow

要输出字符　空格　#　$　%　&　_　{　}　，用命令：　\空格　#　\$　\%　\&　_　{　}

14．如何进行字体转换

要对公式的某一部分字符进行字体转换，可以用{\rm 需转换的部分字符}命令，其中\rm可以参照下表选择合适的字体。一般情况下，公式默认为意大利体。 \rm　　罗马体　　　　　　　\it　　意大利体 \bf　　黑体　　　　　　　　\cal 　花体 \sl　　倾斜体　　　　　　　\sf　　等线体 \mit 　数学斜体　　　　　　\tt　　打字机字体 \sc　　小体大写字母
