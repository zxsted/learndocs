git 日常使用指南

[toc] 

#### 0. git常用命令整理

[传送](http://justcoding.iteye.com/blog/1830388)

##### 初始化配置

```shell
    #配置使用git仓库的人员姓名  
    git config --global user.name "Your Name Comes Here"  
      
    #配置使用git仓库的人员email  
    git config --global user.email you@yourdomain.example.com  
      
    #配置到缓存 默认15分钟  
    git config --global credential.helper cache   
      
    #修改缓存时间  
    git config --global credential.helper 'cache --timeout=3600'    
      
    git config --global color.ui true  
    git config --global alias.co checkout  
    git config --global alias.ci commit  
    git config --global alias.st status  
    git config --global alias.br branch  
    git config --global core.editor "mate -w"    # 设置Editor使用textmate  
    git config -1 #列举所有配置  
      
    #用户的git配置文件~/.gitconfig  
```

##### 查看、添加、提交、删除、找回，重置修改文件

```shell
git help <command>  # 显示command的help  
git show            # 显示某次提交的内容  
git show $id  
   
git co  -- <file>   # 抛弃工作区修改  
git co  .           # 抛弃工作区修改  
   
git add <file>      # 将工作文件修改提交到本地暂存区  
git add .           # 将所有修改过的工作文件提交暂存区  
   
git rm <file>       # 从版本库中删除文件  
git rm <file> --cached  # 从版本库中删除文件，但不删除文件  
   
git reset <file>    # 从暂存区恢复到工作文件  
git reset -- .      # 从暂存区恢复到工作文件  
git reset --hard    # 恢复最近一次提交过的状态，即放弃上次提交后的所有本次修改  
   
git ci <file>  
git ci .  
git ci -a           # 将git add, git rm和git ci等操作都合并在一起做  
git ci -am "some comments"  
git ci --amend      # 修改最后一次提交记录  
   
git revert <$id>    # 恢复某次提交的状态，恢复动作本身也创建了一次提交对象  
git revert HEAD     # 恢复最后一次提交的状态 
```

##### 查看文件diff

```shell
    git diff <file>     # 比较当前文件和暂存区文件差异  
    git diff  
    git diff <$id1> <$id2>   # 比较两次提交之间的差异  
    git diff <branch1>..<branch2> # 在两个分支之间比较  
    git diff --staged   # 比较暂存区和版本库差异  
    git diff --cached   # 比较暂存区和版本库差异  
    git diff --stat     # 仅仅比较统计信息  
```

##### 查看提交记录

```shell
    git log  
    git log <file>      # 查看该文件每次提交记录  
    git log -p <file>   # 查看每次详细修改内容的diff  
    git log -p -2       # 查看最近两次详细修改内容的diff  
    git log --stat      #查看提交统计信息  
```
 tig

Mac上可以使用tig代替diff和log，brew install tig
##### 取得Git仓库
```shell
    #初始化一个版本仓库  
    git init  
      
    #Clone远程版本库  
    git clone git@xbc.me:wordpress.git  
      
    #添加远程版本库origin，语法为 git remote add [shortname] [url]  
    git remote add origin git@xbc.me:wordpress.git  
      
    #查看远程仓库  
    git remote -v  
    
```

##### 提交你的修改

```shell
    #添加当前修改的文件到暂存区  
    git add .  
      
    #如果你自动追踪文件，包括你已经手动删除的，状态为Deleted的文件  
    git add -u  
      
    #提交你的修改  
    git commit –m "你的注释"  
      
    #推送你的更新到远程服务器,语法为 git push [远程名] [本地分支]:[远程分支]  
    git push origin master  
      
    #查看文件状态  
    git status  
      
    #跟踪新文件  
    git add readme.txt  
      
    #从当前跟踪列表移除文件，并完全删除  
    git rm readme.txt  
      
    #仅在暂存区删除，保留文件在当前目录，不再跟踪  
    git rm –cached readme.txt  
      
    #重命名文件  
    git mv reademe.txt readme  
      
    #查看提交的历史记录  
    git log  
      
    #修改最后一次提交注释的，利用–amend参数  
    git commit --amend  
      
    #忘记提交某些修改，下面的三条命令只会得到一个提交。  
    git commit –m &quot;add readme.txt&quot;  
    git add readme_forgotten  
    git commit –amend  
      
    #假设你已经使用git add .，将修改过的文件a、b加到暂存区  
      
    #现在你只想提交a文件，不想提交b文件，应该这样  
    git reset HEAD b  
      
    #取消对文件的修改  
    git checkout –- readme.txt  
```

##### 查看、切换、创建和删除分支

```shell
    git br -r           # 查看远程分支  
    git br <new_branch> # 创建新的分支  
    git br -v           # 查看各个分支最后提交信息  
    git br --merged     # 查看已经被合并到当前分支的分支  
    git br --no-merged  # 查看尚未被合并到当前分支的分支  
       
    git co <branch>     # 切换到某个分支  
    git co -b <new_branch> # 创建新的分支，并且切换过去  
    git co -b <new_branch> <branch>  # 基于branch创建新的new_branch  
       
    git co $id          # 把某次历史提交记录checkout出来，但无分支信息，切换到其他分支会自动删除  
    git co $id -b <new_branch>  # 把某次历史提交记录checkout出来，创建成一个分支  
       
    git br -d <branch>  # 删除某个分支  
    git br -D <branch>  # 强制删除某个分支 (未被合并的分支被删除的时候需要强制)  
```

##### 分支合并和rebase

```shell
    git merge <branch>               # 将branch分支合并到当前分支  
    git merge origin/master --no-ff  # 不要Fast-Foward合并，这样可以生成merge提交  
       
    git rebase master <branch>       # 将master rebase到branch，相当于：  
    git co <branch> && git rebase master && git co master && git merge <branch>  
```

##### Git补丁管理(方便在多台机器上开发同步时用)

```shell
    git diff > ../sync.patch         # 生成补丁  
    git apply ../sync.patch          # 打补丁  
    git apply --check ../sync.patch  #测试补丁能否成功  
```

##### Git暂存管理

```shell
    git stash                        # 暂存  
    git stash list                   # 列所有stash  
    git stash apply                  # 恢复暂存的内容  
    git stash drop                   # 删除暂存区  
```

##### Git远程分支管理

```shell
git pull                         # 抓取远程仓库所有分支更新并合并到本地  
git pull --no-ff                 # 抓取远程仓库所有分支更新并合并到本地，不要快进合并  
git fetch origin                 # 抓取远程仓库更新  
git merge origin/master          # 将远程主分支合并到本地当前分支  
git co --track origin/branch     # 跟踪某个远程分支创建相应的本地分支  
git co -b <local_branch> origin/<remote_branch>  # 基于远程分支创建本地分支，功能同上  
   
git push                         # push所有分支  
git push origin master           # 将本地主分支推到远程主分支  
git push -u origin master        # 将本地主分支推到远程(如无远程主分支则创建，用于初始化远程仓库)  
git push origin <local_branch>   # 创建远程分支， origin是远程仓库名  
git push origin <local_branch>:<remote_branch>  # 创建远程分支  
git push origin :<remote_branch>  #先删除本地分支(git br -d <branch>)，然后再push删除远程分支
```

##### 基本的分支管理

```shell
    #创建一个分支  
    git branch iss53  
      
    #切换工作目录到iss53  
    git chekcout iss53  
      
    #将上面的命令合在一起，创建iss53分支并切换到iss53  
    git chekcout –b iss53  
      
    #合并iss53分支，当前工作目录为master  
    git merge iss53  
      
    #合并完成后，没有出现冲突，删除iss53分支  
    git branch –d iss53  
      
    #拉去远程仓库的数据，语法为 git fetch [remote-name]  
    git fetch  
      
    #fetch 会拉去最新的远程仓库数据，但不会自动到当前目录下，要自动合并  
    git pull  
      
    #查看远程仓库的信息  
    git remote show origin  
      
    #建立本地的dev分支追踪远程仓库的develop分支  
    git checkout –b dev origin/develop  
```

##### Git远程仓库管理

```shell
    git remote -v                    # 查看远程服务器地址和仓库名称  
    git remote show origin           # 查看远程服务器仓库状态  
    git remote add origin git@ github:robbin/robbin_site.git         # 添加远程仓库地址  
    git remote set-url origin git@ github.com:robbin/robbin_site.git # 设置远程仓库地址(用于修改远程仓库地址)  
    git remote rm <repository>       # 删除远程仓库  
```

##### 创建远程仓库

```shell
git clone --bare robbin_site robbin_site.git  # 用带版本的项目创建纯版本仓库  
scp -r my_project.git git@ git.csdn.net:~      # 将纯仓库上传到服务器上  
   
mkdir robbin_site.git && cd robbin_site.git && git --bare init # 在服务器创建纯仓库  
git remote add origin git@ github.com:robbin/robbin_site.git    # 设置远程仓库地址  
git push -u origin master                                      # 客户端首次提交  
git push -u origin develop  # 首次将本地develop分支提交到远程develop分支，并且track  
   
git remote set-head origin master   # 设置远程仓库的HEAD指向master分支 
```

##### 也可以命令设置跟踪远程库和本地库

```shell
    git branch --set-upstream master origin/master  
    git branch --set-upstream develop origin/develop  
```


#### 1. git 的 .gitignore的配置

.gitignore 配置文件用于配置不需要加入版本管理的文件，配置好该文件可以为我们的版本管理带来很大的便利，以下是个人对于配置 .gitignore 的一些心得。

##### 1、配置语法：

　　以斜杠“/”开头表示目录；

　　以星号“*”通配多个字符；

　　以问号“?”通配单个字符

　　以方括号“[]”包含单个字符的匹配列表；

　　以叹号“!”表示不忽略(跟踪)匹配到的文件或目录；

　　

　　此外，git 对于 .ignore 配置文件是按行从上到下进行规则匹配的，意味着如果前面的规则匹配的范围更大，则后面的规则将不会生效；

##### 2、示例：

　　（1）规则：fd1/*
　　　　  说明：忽略目录 fd1 下的全部内容；注意，不管是根目录下的 /fd1/ 目录，还是某个子目录 /child/fd1/ 目录，都会被忽略；

　　（2）规则：/fd1/*
　　　　  说明：忽略根目录下的 /fd1/ 目录的全部内容；

　　（3）规则：
  ```shell
/*
!.gitignore
!/fw/bin/
!/fw/sf/
  ```
说明：忽略全部内容，但是不忽略 .gitignore 文件、根目录下的 /fw/bin/ 和 /fw/sf/ 目录；


#### 2. git 错误解决


 1. git init 产生的目录解释
error: src refspec master does not match any.
引起该错误的原因是，目录中没有文件，空目录是不能提交上去的(只进行了init, 没有add和commit)

 2. error: insufficient permission for adding an object to repository database ./objects
服务端没有可写目录的权限

 3. 错误提示：fatal: remote origin already exists.
解决办法：$ git remote rm origin

 4. 错误提示：error: failed to push som refs to ........
解决办法：$ git pull origin master //先pull 下来 再push 上去

 5. 常用命令
git init //在当前项目工程下履行这个号令相当于把当前项目git化，变身！
git add .//把当前目次下代码参加git的跟踪中，意思就是交给git经管，提交到本地库
git add <file> //把当前文件参加的git的跟踪中，交给git经管，提交到本地库
git commit -m “…”//相当于写点提交信息
git remote add origin git＠github.com:ellocc/gittest.git //这个相当于指定本地库与github上的哪个项目相连
git push -u origin master //将本地库提交到github上。
git clone git＠github.com:ellocc/gittest.git  //将github上的项目down下来。
git fetch origin //取得长途更新，这里可以看做是筹办要取了
git merge origin/master //把更新的内容归并到本地分支/master
下面是删除文件后的提交
git status //可以看到我们删除的哪些文件
git add .   //删除之后的文件提交git经管。
git rm a.c //删除文件
git rm -r gittest //删除目次
git reset --hard HEAD 回滚到add之前的状态
git diff比较的是跟踪列表中的文件和文件系统中文件的差别


#### IntelliJ idea 中使用Git

优秀的东西一起使用,会营造出你意想不到的惊喜!
 
IDEA中 如何将本地项目提交到本地仓库和远程仓库(github),下面是想详细的操作步骤：

1.要使用GitHub,首先你需要下载一个Github  (地址:http://windows.github.com/)
这里使用的是for Windows (我的系统是win 8.1) 然后安装完成会得到如下的一个目录：

![](http://pic.w2bc.com/upload/201501/05/201501051721305321.png)

2. 在Idea 里面做相关配置：
打开设置面板(Ctrl+Alt+S),点击左边功能面板列表中的Version Control(版本控制)如下图：

![](http://pic.w2bc.com/upload/201501/05/201501051721313202.png)

在这里有许多进行版本控制的配置,我们要用的是Git
 
3. 然后我们点击第六项 GitHub(本文默认你已经拥有了一个github账号,如果没有请先注册)
然后Host一栏填写github 的地址: github.com
在 Login 一栏填写你的github 账号，Password 一栏填写密码
填写完成后点击 Test按钮，此时 IDEA 会根据你填写的内容远程访问github社区,如果账号和密码输入正确会提示你链接成功

![](http://pic.w2bc.com/upload/201501/05/201501051721316103.png)

4. 接下来，点击左边面板的功能列表中的Git 进行配置
这里面有许多配置，其实基本按照默认的就行了,无需做其他更多的操作。
在Path to Git executable一栏,选择刚才安装的git路径下bin\git.exe 然后点击后面的Test按钮,如果配置成功会看到如下界面：

![](http://pic.w2bc.com/upload/201501/05/201501051721319218.png)

然后点击, Apply，OK 按钮 完成配置。 IDEA对Git的相关配置到此就结束了。
 
关于项目的本地提交(Commit)
 
1.创建本地仓库
在IDEA中创建任意一个项目，在IDEA的菜单栏中选择 VCS (倒数第三项)，选择Import into Version Control (引入到版本控制) -->
Create Git Repository... -->选择一个存放的路径(本文为:I:\workspace\NCPlatform)--> OK
这样就创建了一个本地仓库， 以后代码的本地提交(Commit)的内容都会更新到这个选择的路径中
 
2.将项目提交到本地的Git
 选中项目(或者文件) 右键选择Git--->Add (此时没任何反应)---->commit(提交)  注意:一定要先add 再提交
此时项目文件就添加到本地仓库了

![](http://pic.w2bc.com/upload/201501/05/201501051721322040.png)

关于远程仓库的配置
1.在github上创建一个仓库 :
登陆你的github -->点击你的用户名 -->选择Repositories--> 点击绿色-->输入你的仓库名称
(此时远程仓库创建完成)
2.通过Git shell 配置远程仓库：
 ①进入到项目目录：
 
 ![](http://pic.w2bc.com/upload/201501/05/201501051721324482.png)
 
 然后复制刚才创建的远程仓库的HTTPS/SSH KEY (此处使用的SSH),在Git shell 中键入如下脚本
git remote add origin git@github.com:teamaxxiaohu/NCPlatform.git(此处为你自己远程仓库的key)

git push -u origin master (解释:该脚本将本地的master 推到刚才设置的github远程仓库中)
 
如果执行完成2条脚本,没有任何提示,也没任何错误,恭喜你成功了! 
 
3. 回到IDEA，选择项目 -->Git -->Repository --Push  即可将本地的文件推送到远程仓库中，然后刷新你的github仓库你就会看到
你提交的本地内容了,同时你在idea中也能看到你的操作信息。
![](http://pic.w2bc.com/upload/201501/05/201501051721327558.png)


注意：在执行 通过Gitshell配置远程仓库的时候可能会出现一些意外:
1.  提示这个仓库已经存在(fatal: remote origin already exists) ，如果是这样 就不需要使用add + 地址的形式了 ,请修改为:
git remote rm origin
 
2.提示不能移除配置信息错误(.error: Could not remove config section 'remote.origin')
解决方案: 在window/用户下面找到.gitconfig文件 (本文路径为:C:\Users\Vincent_2\.gitconfig)

打开它把里面的[remote "origin"]那一行删掉   重启gitshell   再重新配置。


#### Git学习笔记与IntelliJ IDEA整合

Git学习笔记与IntelliJ IDEA整合
一.Git学习笔记（基于Github）
　1.安装和配置Git
　　下载地址：http://git-scm.com/downloads
　　Git简要使用说明：http://rogerdudler.github.io/git-guide/index.zh.html
　　Github官方使用说明：https://help.github.com/articles/set-up-git
　　默认安装
　　配置
　　1）首先你要告诉git你的名字
　　git config --global user.name "Your Name Here"
　　2）关联邮箱地址：
　　git config --global user.email "your_email@example.com"
　　关联的邮箱地址最好跟github的一致，如果不一致点这里：https://help.github.com/articles/how-do-i-change-my-primary-email-address
 
　2.创建一个仓库
　　1）在Github上创建一个仓库（注册省略）
  ![](http://images.cnitblog.com/blog/569649/201310/04140911-0fd58f8cd18045a5af6dd67b93adf2ed.jpg)
  2）填写的仓库的信息；例子：Hello-World
  ![](http://images.cnitblog.com/blog/569649/201310/04141154-92ead401d9d341f387cabd048f584e6a.png)
  恭喜你!您已经成功创建了您的第一个仓库。
 
　3.为仓库创建一个README。
　　README不是GitHub上必须的一部分，但是他是一个非常好的主意，它可以描述你的项目或者是一些文档，比如如何安装或使用你的项目。
 
　　$mkdir ~/Hello-World
　　# 在你的目录下创建一个Hello-World的文件夹
 　　$cd ~/Hello-World
　　# 改变当前工作目录到你的新创建的目录
 
　　$git init
　　# 初始化 /Users/you/Hello-World/.git/
 
　　$touch README
　　#在你的hello world目录创建一个文件叫做“README”
　　在C盘用户目录下进入Hello-World，打开README，填写你想填的东西
 
　4.提交你的“README”
 
　　$git add README
　　# 将README添加的提交列表上
 
　　$git commit -m 'first commit'
　　# 提交你的文件,添加消息”第一次提交”
 
　5.推送你的提交到GitHub服务器上
　　　$git remote add origin https://github.com/username/Hello-World.git

    # 创建一个远程命名为“起源”指着你的GitHub库; username:为你的github用户名
    Hello-World这是区分大小写的,重要的是与服务器保持相同
    $git push origin master
    #发送你的提交的“大师”分支到GitHub
    此时会让你输入用户名和密码

     

　　OK!此时服务器上Hello-World上就可以看到你提交的README。
 
　6.创建分支
　　在某些时候你可能会发现自己想要为别人的项目,或想用别人的项目为起点。
　　这就是所谓的“分支”。对于本教程中,我们将使用 Spoon-Knife项目,托管在GitHub.com上。
 
　　1）创建"Spoon-Knife"分支
　　创建分支项目,单击“Fork”按钮在GitHub库中。
  ![](http://images.cnitblog.com/blog/569649/201310/04141556-14acc3afdd224a0b9af59226858b9e42.png)

2）克隆分支
　　你已经成功地分支了“ Spoon-Knife”,但到目前为止,它只存在在GitHub上。能够在这个工程上工作,你将需要克隆它到您的本地机器上。
 
　　$git clone https://github.com/username/Spoon-Knife.git
　　#克隆你的分支的仓库到当前目录
　　3）配置远程
　　当一个仓库是克隆的,它有一个默认的远程称为原点指向你的叉子在GitHub上，不是原始的仓库是分支，跟踪原始的仓库,您需要添加另一个远程命名的upstream
 
　　　$cd Spoon-Knife

    # 改变活动目录,进入 "Spoon-Knife" 目录
    $git remote add upstream https://github.com/octocat/Spoon-Knife.git
    # 分配原始库远程名称为“upstream ”
    $git fetch upstream
    # Pulls in changes not present in your local repository, without modifying your files

 
 
二. 使用IntelliJ IDEA分享、获取Github项目：
·　1.在IDEA中配置Git：
　　·选择菜单”File — Settings”，找到”Version Control — Git”：到Git的安装目录下的Git.exe执行文件所在地
  
  ![](http://images.cnitblog.com/blog/569649/201310/04141712-0e704b4563f04d438d54d3dfff3fd5ee.jpg)
  
  其次，配置你在Github上注册的账户：
　　填入你的Github账户信息，点击”Test”按钮测试连接，若链接成功会提示”Connection successful”。保存完成。
　　点击OK，此时可能要你输入IntelliJ IDEA的密码，如果没设，点击确定即可。
　　
  ![](http://images.cnitblog.com/blog/569649/201310/04141755-cbb7fc8d5dfe4e1ab98668f38512eddb.jpg)
  
  　2.分享项目Github上：
　　选择菜单”VCS — Import into Version Control — Share project on Github”：
  
  ![](http://images.cnitblog.com/blog/569649/201310/04141822-bd18d50c5d0f45b887fbbf96aa013e77.jpg)
  填写描述信息后，点击”Share”按钮即可。
 
　3.获取Github项目：
　　选择菜单”VCS — Checkout from Version Control — Github”：
  ![](http://images.cnitblog.com/blog/569649/201310/04141852-8788de7e15dc45d6858f11bc89132896.jpg)
  等待一段时间的验证和登陆，出现界面：
  ![](http://images.cnitblog.com/blog/569649/201310/04141924-fb318645cd234c909557a1719a3d8707.jpg)
  在”Git Repository URL”下来列表中既有你自己的项目，也有你在Github网站上”Wacth”的项目，选择后，选择你存放的路径，再输入你想要的项目名称，点击”Clone”按钮，即完成获取过程。
  
  
  
  

































