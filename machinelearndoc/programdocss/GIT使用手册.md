GIT使用手册

[传送](http://blog.csdn.net/lanbing510/article/details/40588415)

[toc]

#### 一 关于添加、删除和回退

##### 1 git rm --cached file  
   想要git不再跟踪这个文件，但是又不想在硬盘中删除该文件

##### 2 在被git管理的目录中删除文件时，可以选择如下两种方式：
① rm +git commit -am "abc"
② git rm+git commit -m "abc"

##### 3 如果rm了，但想恢复     
    git checkout -- file

##### 4 gir rm后想恢复：
① 如果没提交   git reset HEAD
② 已经commit   git reset --hard  <之前的版本，可用git reflog查看>       

##### 5 修改后恢复
① 当你改乱了工作区某个文件的内容，想直接丢弃工作区的修改时，用命令git checkout -- file。

② 当你不但改乱了工作区某个文件的内容，还添加到了暂存区时，想丢弃修改，分两步，第一步用命令git reset HEAD file，就回到了场景1，第二步按场景1操作。

③ 已经提交了不合适的修改到版本库时，想要撤销本次提交，则进行版本回退（git reset --hard ）
HEAD时，表示最新的版本，上一个版本就是HEAD^，上上一个版本就是HEAD^^，往上100个写成HEAD~100。

##### 6 添加（git add）
① git add -u [<path>] 把<path>中所有tracked文件中被修改过或已删除文件的信息添加到索引库。它不会处理untracted的文件。省略<path>表示.,即当前目录。

② git add -A [<path>] 把<path>中所有tracked文件中被修改过或已删除文件和所有untracted的文件信息添加到索引库。省略<path>表示.,即当前目录。

③ git add -i [<path>] 查看<path>中被所有修改过或已删除文件但没有提交的文件。

####  二 查看历史和修改

1 git log
2 git log --pretty=oneline
3 git diff HEAD -- file  查看工作区和版本库里面最新版本的区别
4  git log --graph --pretty=oneline --abbrev-commit  比较好的查看分支记录


#### 三 Git配置

##### 1 初始配置
git config --global user.name "Your Name"
git config --global user.email "email@example.com"

##### 2 添加远程仓库和推送
git remote add original git@github.com:lanbing510/learngit.git
配置后第一次推送 git push -u original master 
加入-u git不但会把本地的master分支内容推送的远程新的master分支，还会把本地的master分支和远程的master分支关联起来，在以后的推送或者拉取时就可以简化命令: git push original master

##### 3 克隆 git clone 远程仓库地址

##### 4 配置忽略文件
① 在Git工作区的根目录下创建一个特殊的.gitignore文件，然后把要忽略的文件名填进去，Git就会自动忽略这些文件，一些配置文件可参见https://github.com/github/gitignore 
② 或者使用命令git config --global core.excludesfile ~/.gitignore_global 添加到git全局配置以减少每层目录的规则重复定义，更多请参考 http://blog.csdn.net/lanbing510/article/details/40588323 

##### 5 配置别名
如 git config --global alias.unstage 'reset HEAD' 以后就可以使用 git unstage 把暂存区的修改撤销掉（unstage），重新放回工作区
git config --global alias.last 'log -1'  --> 显示最后一次修改 git last
git config --global alias.lg "log --color --graph --pretty=format:'%Cred%h%Creset -%C(yellow)%d%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset' --abbrev-commit" git lg 更好的显示log
每个仓库的Git配置文件都放在.git/config文件中


#### 四 分支

##### 1 创建并切换分支 
git checkout -b newbranch
上面命令相当于 git branch newbranch 和 git checkout newbranch

##### 2 git branch命令查看当前分支

##### 3 合并某分支到当前分支：
   git merge name
##### 4  删除分支：
   git branch -d name

##### 5 禁用fast forward : 
   merge时加入-no-ff
合并分支时，如果可能，Git会用“Fast forward”模式，但这种模式下，删除分支后，会丢掉分支信息。如果要强制禁用“Fast forward”模式，Git就会在merge时生成一个新的commit，这样，从分支历史上就可以看出分支信息。

##### 6 当手头没有完成时，但需要修复之前的bug
git stash 把现场工作储存起来
git checkout master
git checkout -b fixbug
git add 
git commit -m "fix bug"
git checkout master
git merge --no-off -m "merge bug fix fixbug"  fixbug
git branch -d fixbug
git stash list 列出stash的内容
git stash apply 进行恢复 + git stash drop 删除stash = git stash pop

##### 7 开发一个新的feature，最好新建一个分支
git branch -D <name> 丢掉一个没有被合并过的分支

##### 8 推送分支
git push origin master 推送主分支
git push origin dev 推送新分支
master是主分支，需要时刻与远程同步
dev是开发分支，团队成员需要在上面工作，需要与远程同步
bug分支用于本地修复bug，不需要推送到远程，除非老板需要看
feature是否推送取决于你是否和人合作一起进行开发

##### 9 git clone
当从另一台电脑clone下来后，只能看到master分支，看不到dev，如果要在dev上开发，必须创建远程origin的dev分支到本地：git checkout -b dev origin/dev

##### 10 git push 失败
原因为远程分支比你的本地更新，首先git pull合并
如果合并有冲突，解决；如果git pull 提示 no tracking information，说明本地和远程分支的链接关系没有创建：git branch --set-upstream branch-name /origin/branch-name


#### 五 标签 

##### 1 git tag name 
例如： git tag v0.1 6224937
##### 2 git tag 查看标签
##### 3 git show <tag> 查看标签信息 
##### 4 还可以创建带有说明的标签，
  用-a指定标签名，-m指定说明文字： git tag -a v0.1 -m "version 0.1 released" 3628164
##### 5 用PGP签名标签： 
  git tag -s <tagname> -m "blablabla...“ 需首先安装gpg
##### 6 推送一个本地标签：
  git push origin <tagname>
##### 7 推送全部未推送过的本地标签：
  git push origin --tags
##### 8 删除一个本地标签：
  git tag -d <tagname>
##### 9 删除一个远程标签：
  git push origin :refs/tags/<tagname>


#### 六 搭建Git服务器

参考 http://www.liaoxuefeng.com/wiki/0013739516305929606dd18361248578c67b8067c8c017b000/00137583770360579bc4b458f044ce7afed3df579123eca000



#### 七 常出现的问题

##### 1  ! [rejected]       master -> master (non-fast-forward) 
解决方法： 
① git push -f 进行强推
② git fetch git merge

##### 2 重命名文件(包括改变大小写)
git mv --force myfile MyFile


八 总结：Git Cheat Sheet

![](http://img.blog.csdn.net/20141105002209296?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbGFuYmluZzUxMA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)