python  实现的language network

[传送](http://chaoslog.com/pythonshi-xian-de-language-network.html)


#### 前言
在“语言网络，结构、功能和进化”[1]一文中，大概了解了language network大概是怎么回事。现在尝试使用python对给定语料进行解析，构建词语共现网络并进行可视化。

#### 环境

NetworkX 

官方教程： http://networkx.github.io/documentation/latest/tutorial/tutorial.html#drawing-graphs


官方例子： http://networkx.github.io/documentation/latest/examples/index.html


```shell
$ sudo pip install network
```


#### NetworkX中文实例说明

```python
import matplotlib.pyplot as plt
import networkx as nx
from networkx.convert import convert_to_undirected, convert_to_directed



# You might notice that nodes and edges are not specified as NetworkX objects. This leaves you free to use meaningful items as nodes and edges. The most common choices are numbers or strings, but a node can be any hashable object (except None), and an edge can be associated with any object x using G.add_edge(n1,n2,object=x).
# 在networkx所构建的图中，所有的节点只需要是可哈希的对象就行；所有的边也可以指定对象（用对象作为关系类型），如果想让整个图抹去所有类的信息，可以用convert_node_labels_to_integers()，直接构造一个同构的基本图


G = nx.Graph()  # 创建图的数据结构


G.add_node(1)  # 往图中加一个节点
G.add_nodes_from([2, 3])  # 往图中加列表中的节点
H = nx.path_graph(10)  # 创建一个路径图，英文名叫nbunch，10表示这个图具有节点 0-9
nx.draw(H)
plt.show()
G.add_nodes_from(H)  # 把H中的所有节点加到G里
G.add_node(H)  # 把H作为一个节点加到G里


G.add_edge(1, 5)  # 增加边，自动增加节点
e = (2, 3)
G.add_edge(*e)  # *e表示把e中的元素拆开，按照方法add_edge的参数顺序依次把参数传进去
print G.edges()
G.add_edges_from([(1, 2), (1, 3)])  # 从可迭代的容器里增加一组边
G.add_edges_from(H.edges())  # 把另一个图中的边加进来


G.remove_node(H)  # 从G里删掉H中的节点
G.clear()  # 清空



# 增加重复的节点，会无视现有的节点, 新加节点和边若已在图中，不会改变
G.add_edges_from([(1, 2), (1, 3)])
G.add_edge(1, 2)
G.add_node(1)
G.add_node("spam")  # adds node "spam"
G.add_nodes_from("spam")  # adds 4 nodes: 's', 'p', 'a', 'm'


# 计数
G.number_of_nodes()
G.number_of_edges()

# 得到节点边和相邻点
print G.nodes()
print G.edges()
print G.neighbors(1)  # 怎么实现的呢？，不会是挨个遍历吧

# 删除节点
G.remove_nodes_from("spam")
G.remove_edge(1, 3)


H = nx.DiGraph(G)  # 从已有无向图构建有向图
print H.edges()  # 现在二元组的顺序很重要了
edgelist = [(0, 1), (1, 2), (2, 3)]
H = nx.Graph(edgelist)  # 根据边构建有向图




print G[1]  # 返回与1相邻的点，注意不要修改返回值，不要随便G[1] = xx
print G[1][2]  # 返回1 2这条边的信息

G.add_edge(1, 3)
G[1][3]['color'] = 'blue'


FG = nx.Graph()
FG.add_weighted_edges_from([(1, 2, 0.125), (1, 3, 0.75), (2, 4, 1.2), (3, 4, 0.375)])
for n, nbrs in FG.adjacency_iter():  # 怎么实现的？邻接迭代
for nbr, eattr in nbrs.items():
data = eattr['weight']
if data < 0.5: print('(%d, %d, %.3f)' % (n, nbr, data))


# Attributes such as weights, labels, colors, or whatever Python object you like, can be attached to graphs, nodes, or edges.
# 可以把任意对象附在graph nodes 或 edges上
G = nx.Graph(day="Friday")
G.graph['day'] = 'Monday'

G.add_node(1, time='5pm')
G.add_nodes_from([3], time='2pm')
G.node[1]
G.node[1]['room'] = 714
G.nodes(data=True)


# 以下这些函数，一定是  function_name(a, b,attr_dict=None, **attr)这样定义的
G.add_edge(1, 2, weight=4.7)   
G.add_edges_from([(3, 4), (4, 5)], color='red')
G.add_edges_from([(1, 2, {'color':'blue'}), (2, 3, {'weight':8})])
G[1][2]['weight'] = 4.7
G.edge[1][2]['weight'] = 4


# 有向图
DG = nx.DiGraph()
DG.add_weighted_edges_from([(1, 2, 0.5), (3, 1, 0.75)])
DG.out_degree(1, weight='weight')  # 怎么实现的呢
DG.degree(1, weight='weight')  # 怎么实现的呢
DG.successors(1)  # 后继结点，怎么实现的呢
DG.neighbors(1)  # 邻居节点 ，怎么实现的呢

# 转换为无向图
G = DG.to_undirected()
G = nx.Graph(DG)


# multigraph，允许两节点间存在多条边。注意，像最短路径这样的算法，在多边图上会失效
MG = nx.MultiGraph()
MG.add_weighted_edges_from([(1, 2, .5), (1, 2, .75), (2, 3, .5)])
# plot

print MG.degree(weight='weight')  # 算连接此节点所有weight之和
GG = nx.Graph()
for n, nbrs in MG.adjacency_iter():  # 每个点和每个点相邻的点的边，如果是多边，则还有
for nbr, edict in nbrs.items():  # 返回每个点和与n的边的dict（多边）
minvalue = min([d['weight'] for d in edict.values()])  # 找到最小的那个权
GG.add_edge(n, nbr, weight=minvalue)  # 加入到GG里
print nx.shortest_path(GG, 1, 3)  # 怎么实现的呢

G1 = nx.Graph()
G2 = nx.Graph()
nbunch = nx.path_graph(10)

nx.subgraph(G, nbunch)  # induce subgraph of G on nodes in nbunch, 从G中找到nbunch中节点所涉及到的子图
nx.union(G1, G2)  # graph union，合并图，G1和G2必须是没有交集的
nx.disjoint_union(G1, G2)  # graph union assuming all nodes are different，合并图，认为两个图中的节点都不同，会强制将节点转换成int
nx.cartesian_product(G1, G2)  # return Cartesian product graph 笛卡尔积
nx.compose(G1, G2)  # combine graphs identifying nodes common to both，将两个有交集的图G1和G2合并
nx.complement(G)  # graph complement, 返回补图
nx.create_empty_copy(G)  # return an empty copy of the same graph class， 返回一个空的同类型网络（有向，无向，multi）
convert_to_undirected(G)  # return an undirected representation of G
convert_to_directed(G)  # return a directed representation of G


# 经典图
petersen = nx.petersen_graph()
tutte = nx.tutte_graph()
maze = nx.sedgewick_maze_graph()
tet = nx.tetrahedral_graph()
K_5 = nx.complete_graph(5)
K_3_5 = nx.complete_bipartite_graph(3, 5)
barbell = nx.barbell_graph(10, 10)
lollipop = nx.lollipop_graph(10, 20)

# 随机图
er = nx.erdos_renyi_graph(100, 0.15)
ws = nx.watts_strogatz_graph(30, 3, 0.1)
ba = nx.barabasi_albert_graph(100, 5)
red = nx.random_lobster(100, 0.9, 0.9)

# 文件操作
nx.write_gml(red, "path.to.file")
mygraph = nx.read_gml("path.to.file")

# 对图的分析
G = nx.Graph()
G.add_edges_from([(1, 2), (1, 3)])
G.add_node("spam")  # adds node "spam"
nx.connected_components(G)  # 发现连通子图
sorted(nx.degree(G).values())  # 排序
nx.clustering(G)  # 簇
nx.degree(G)  # 图的度，怎么做的？
nx.degree(G, 1)  # 点1的度，怎么做的？
G.degree(1)  # 点1的读，怎么做的？


nx.draw(G)  # 画图
nx.draw_random(G)  # 随机画
nx.draw_circular(G)  # 用圆形画
nx.draw_spectral(G)  # 用图谱画



nx.draw(GG)
plt.show()
plt.savefig("path.png")  # 保存图片

nx.draw_graphviz(G)  # 用graphviz画，需要GraphViz和PyGraphviz
nx.write_dot(G, 'file.dot')  # 用dot画，需要PyDot支持


```



#### Networkx 底层实现
NetworkX的底层用了很多dictionary，看完了教程之后觉得有很多地方可以优化，因此追究了一下部分方法的底层实现。 NetworkX中的graph是由多个dictionary实现的，当创建一个Graph类时，会对应初始化
```python
self.graph = {}   # dictionary for graph attributes
self.node = {}# empty node dict (created before convert)
self.adj = {} # empty adjacency dict
self.edge = self.adj
```


  1.  当返回node或者是edge的iteration的时候，实际上是返回相应词典的itemiter return iter(self.node.items())。
  2.  当调用neighbors()，successors()，predecessors()，这一类寻找相邻节点的方法时，实际上就是用的dictionary的查询，因此，可以认为，寻找相邻节点时采用的是hash的方法，因为dictionary的查询用的是hash [3]。
  3.  一个节点的度，以无向图的neighbors为例，实际上是算的neighbor的数量：yield (n,len(nbrs)+(n in nbrs)) # return tuple (n,degree)，即nbrs的数量，外加当前节点是否在neighbor里（应该是考虑有自己指向自己的可能）。
  4.  最短路径shortest_path用的是Dijkstra算法。




#### 基于词同现的language network

思路：由于论文中的网络仅仅在相邻词之间作边，但实际中有些词语搭配是跨词的，因此设置了span。从文本中读取句子，对于每个句子，以当前词为中心，以span为半径(为了能看清，代码中暂取span=2)内的词与当前词都作边。代码如下： # coding:utf8 ''' Created on 2015年1月27日

```python
@author: James
'''

import networkx as nx
import matplotlib.pyplot as plt
from config.params import fiveCopusppisentences
from pylab import *  
mpl.rcParams['font.sans-serif'] = ['SimHei'] #指定默认字体  
mpl.rcParams['axes.unicode_minus'] = False #解决保存图像是负号'-'显示为方块的问题  
def add_sentence(graph, sentence, span=2):
    for i in range(len(sentence)):
        for j in range(max(0, i-span), i):
            graph.add_edge(sentence[j], sentence[i])
        for j in range(i+1, min(i+1+span, len(sentence))):
            graph.add_edge(sentence[i], sentence[j])

def get_coocurrence_network(corpus):
    G = nx.DiGraph()
    # 1 sentence per line
    for line in open(corpus):
        line = line.strip()
        if len(line) > 0:
            line = line[0].lower() + line[1:]
        sentence = line.split(' ')
        add_sentence(G, sentence)
    print 'load file done'
    return G

def draw_node(G, node):
    sub_graph = nx.DiGraph(G.subgraph(G.successors(node) + G.predecessors(node) + [node]))
    for word in sub_graph.nodes():
        print word
    nx.draw_networkx(sub_graph, alpha=0.6)

def show_word_network(corpus, word):
    G = get_coocurrence_network(corpus)
    # nx.draw_circular(G) 
    draw_node(G, word)
    plt.show()

show_word_network('D:/downloads/banfa', '办法/COM-NOUN')
show_word_network(fiveCopusppisentences, 'time')

```


```python
import networkx as nx
import matplotlib.pyplot as plt

def circle(n = 32):
    G = nx.Graph()
    for i in range(n):
        for j in range(n):
            if i != j:
                G.add_edge(i, j)
    nx.draw_circular(G)
    plt.show()

circle()

```



