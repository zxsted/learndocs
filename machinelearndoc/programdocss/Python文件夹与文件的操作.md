###[Python文件夹与文件的操作][0]

最近频繁的与文件操作打交道，这块比较弱，在百度上找到一篇比较不错的文章，这是原文的[传送门][1]

有关文件夹与文件的查找，删除等功能在os模块中实现，使用时需要导入这个模块:
```python
import os
```
####一、获取当前目录
```python
s = os.getwd()
```
s中保存的就是当前的目录（即文件夹）
下面是一个例子：将abc.py放在A文件夹，不管A放置在硬盘的哪个位置，都可以在A文件夹内生成一个新的文件夹。且文件夹的名字跟据时间自动生成：
```python
import os
import time
filder = time.strftime(r"%Y-%m-%d_%H-%M-%S",time.localtime())
os.makedirs(r'%s/%s' % (os.getcwd(),folder))
```


####二、更改当前目录
```python
os.chdir("C:\\123")
```
将当前目录设置为"C:\\123"，相当于DOC命令的cd C:\\123

 * 当指定目录不存在时，引发异常。
   异常类型： WindowsError    ( windows 下)
   
####三、将一个路径名分解为目录名和文件名两部分
```python
fpath,fname = os.path.split(‘要分割的的路径’)
```
例如
```python
a,b = os.path.split("c:\\123\\456\\test.txt")
print a
print b
```
显示：
```shell
c:\123\456
test.txt
```

####四、分解文件名的扩展名
```python
fpathhandname,fext = os.path.splittext("要分解的路径")
```
例如：
```python
a,b = os.path.splittext("c:\\123\\456\\text.txt")
print a
print b
```
显示：
```shell
c:\123\456\test
.txt
```

####五、判断一个路径（目录或文件）是否存在
```python
b = os.path.exists("你要判断的路径")
```
返回值b: True 或 False

####六、判断一个路径是否是文件
```python
b = os.path.isfile("你要判断的路径")
```
返回值b ： True 或 False

####七、判断一个路径是否是目录
```python
b = os.path.isdir("你要判断的路径")
```
返回值b: True 或 False

####八、获取某目录中的文件及子目录的列表
```python
L = os.listdir("你要判断的路径")
```
例如：
```python
L = os.listdir("c:/")
print L
```
L 是一个列表，这里面既有文件也有子目录

* 获取某指定目录下的所有子目录的列表

```python
def getDirList(p):
	p = str(p)
    if p == '':
    	return []
    p = p.replace("/","\\")
    if p[-1] != "\\":
    	p = p + "\\"
    a = os.listdir(p)
    b = [x for x in a if os.path.isdir(p + x)]
    return b

print getDirList("C:\\")
```
* 获取指定目录下面的所有文件
```python
def getFileList(p):
	p = str(p)
    if p = "":
    	return []
    p = p.replace("/","\\")
    if p[-1] != "\\":
    	p = p+ "\\"
```

####九、创建子目录
```python
os.makedirs(path) #path 是要创建的子目录
```
例如：
```python
os.makedirs("c:\\123\\456\\789")
```
调用有可能失败，
 1. path已经存在时（不管是文件还是文件夹）
 2. 驱动器不存在
 3. 磁盘已满
 4. 磁盘是只读的或者没有权限
 
####十、删除子目录
```python
os.rmdir(path) #path: 要删除的子目录
```
产生异常的原因：
 1. path不存在
 2. path子目录中有问间或下级子目录
 3. 没有操作权限或者只读

####十一、删除文件
```python
os.remove(filename) #filename : 要删除的文件
```
产生异常的原因：
 1. filename不存在
 2. 对filename文件，没有操作权限或只读
 
####十二、文件改名
os.name(oldfilename,newfilename)
产生异常原因：
 1. oldfilename旧文件名不存在
 2. newfilename新文件已经存在时，

####十三、用walk遍历指定文件夹
* os.walk()
函数声明： walk(top,topdown=True,onerror=None)
 1. 参数top表示需要遍历的目录树的路径
 2. 参数topdown的默认值是"True"，表示首先返回目录树下的文件，然后再遍历目录树的子目录。Topdown的值为“False”时，则表示先遍历目录树的子目录，返回目录下的文件，返回根目录下的文件。
 3. 参数onerror的默认值是“None”，表示忽略文件遍历时的错误，不为空则提供一个自定义的函数提示错误信息后继续遍历或则抛出异常后终止遍历
 4. 该函数返回一个元祖，该元组有3个元素，元素分别表示每次遍历路径名，目录列表和文件列表
 
 oswalk（）实例：
 ```python
 import os
 def VisitDir(path):
 	for root ,dirs,files in os.walk(path):
    	for filespath in files:
        	print os.path.join(root,filespath)
  if __name__ == "__main__":
  	path = "/root"
    VisitDir(path)
 ```

* os.path.walk()
函数声明： walk(top,func,arg)
 1. 参数top表示需要遍历的目录树的路径
 2. 参数func表示回调函数，对遍历路径进行处理。所谓会调函数，是作为某个参数的参数使用，当某个时间触发时，程序将调用定义号的回调函数处理某个任务，会调回调函数必须提供三个参数，第一个为walk（）函数的参数tag，第二个参数表示目录列表，第3个参数表示文件列表
 3. 参数arg是传递给回调参数func的元组，会调函数的一个参数必须是arg，为回调函数提供处理参数，参数arg可以为空。
 ```python
 import os,os.path
 def VisitDir(arg,dirname,names):
 	for filepath in names:
    	print os.path.join(dirname,filepath)
 if __name__ == "__main__":
 	path = '/home/username'
    os.path.walk(path,VisitDir,())
 ```

os.path.walk() 与 os.walk() 产生文件名列表并不相同。os.path.walk()产生目录树下的目录径和文件路径，而os.walk()只产生文件路径


下面是自己实现的遍历：
```python
def listdir(leval,path):
	for i in os.listdir(path):
    	print('|-'*(leval + 1) +i)
        if os.path.isdir(path+i):
        	listdir(leval+1,path+i)

path = 'c:' + os.sep+'ant'
print(path+os.sep)
listdir(0,path+os.sep)
```
 



[0]:http://www.cnblogs.com/yuxc/archive/2011/08/01/2124012.html
[1]:http://hi.baidu.com/jxq61/blog/item/c24a36a0d6897aa6caefd0ed.html