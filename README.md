# 📕 MarkIdea

MarkIdea 是一款开源免费的私有云笔记软件，支持跨平台部署，支持Markdown格式笔记，采用Git进行笔记的版本管理，亦可推送至远程Git仓库备份。

MarkIdea本身是一个Java程序，所以它可以部署多种平台上，包括但不限于Linux、macOS和Windows等，只要你的设备能安装jre或jdk即可，不依赖外部数据库（使用了Sqlite作为嵌入式数据库）。MarkIdea是B/S架构（浏览器/服务器），打开浏览器，访问网址即可，无需下载客户端。

使用MarkIdea记录的笔记是易于迁移，因为使用的笔记格式是Markdown——开放且轻量级的标记语言，倘若不再想使用MarkIdea，仅需要拷贝对应的目录出来即可，文件名即笔记的题目，文件夹名即笔记本名称。

MarkIdea基于Git来管理笔记的历史版本，想必大家也比较熟悉这个版本管理工具，同时MarkIdea也支持推送至远程Git仓库备份，提高笔记的安全性。

如有bug，欢迎反馈。如需尝试可访问[demo网站](http://sanshicloud.cn:8090)，用户名和密码均为admin，仅供尝试，服务器带宽较小，访问较为缓慢。亦可在[知乎](https://www.zhihu.com/people/hansanshi)私信反馈

## 📮 功能介绍

### 登录页面

![image-20200901190240696](https://gitee.com/hansanshi/image/raw/master/image-20200901190240696.png)

输入用户名和密码即可登录或注册（可以选择关闭注册功能）。

### 主页面

![image-20200901190921203](https://gitee.com/hansanshi/image/raw/master/image-20200901190921203.png)

主页面可以进行创建笔记本、新建笔记、移动笔记、搜索笔记和查看笔记历史等操作，点击左上角logo可以关闭或打开笔记侧边栏，右上角菜单可以选择进入设置页面或者注销登录。

### 设置页面

![image-20200901191516442](https://gitee.com/hansanshi/image/raw/master/2020-09-27-6.32.42.png)

目前设置功能，主要是文件管理、修改密码和设置备份至远程仓库、公开笔记管理。

目前只在Firefox浏览器和Chrome浏览器上进行过测试，其他浏览器效果未知。

## 🔩 服务器部署

运行环境仅需要安装jdk8，请根据自己系统安装对应jdk8并配置好环境，在此不再赘述。

下载[软件](https://github.com/Hansanshi/mark-idea/releases)后，在该目录下进入命令行，运行下面一行命令

```bash
# 后台运行MarkIdea
# <version> 替换下载文件名的版本号
# <your_username>替换为你想要的用户名
# <your_password>替换你想要的密码
# 如果想禁止注册，可以添加参数   --"register-forbidden"=true
nohup java -jar note-<version>.jar --username=<your_username> --password=<your_password>  2>&1 &
```

打开浏览器，访问`http://<server_ip>:8090`，即可开始使用，用户名和密码即`<your_username>`和`<your_password>`。

## 🖥️ 参与开发

欢迎各位参与本项目的开发中来，[联系我](https://www.zhihu.com/people/hansanshi)。

###  前端

前端是基于**Vue** + **ElementUI** + **axios**开发，参与开发需要你拥有前端技术栈基础。

### 后端

后端是基于**Spring Boot**开发，参与开发需要你熟悉**Java**、**Spring Boot**。

### 打包流程

假设你从一个空白操作系统开始

1. [前端仓库](https://github.com/Hansanshi/mark-idea-front)拉取（或者直接下载）至本地；

2. 在电脑上安装[Nodejs](https://nodejs.org)；

3. 命令行进入前端项目，依次运行

   ```bash
   # 安装依赖
   npm install
   # 打包
   npm run build
   ```

4. 进入dist目录下，压缩dist目录下（不要直接压缩dist目录）的文件为zip文件，重命名为`front.zip`；

5. 安装maven和java；

6. 后端仓库（即本仓库）拉取至本地；

7. 拷贝`front.zip`至resources文件下；

8. 命令行进入后端项目目录下，运行命令

   ```bash
   mvn package -Dmaven.test.skip=true
   ```

最后在target目录下，我们就可以拿到打包好的MarkIdea运行文件，`note-<version>.jar`。

## ⏰ 注意事项

1. 因为MarkDown编辑器采用的是[vditor](https://hacpai.com/article/1549638745630)，实现了CommonMark规范，故无意义的空白换行均会被自动吞掉，如果你一定需要换行，请使用工具栏的强制换行按钮。

2. 出于实现复杂度的考虑，笔记一旦被重命名或移动至其他笔记本，那么你无法再看见该笔记之前的本历史版本，但你仍然可以通过其他git工具查看到历史。

3. 同理，被清理的笔记仍然可以通过其他git工具查看到，尽管MarkIdea上看不到了。

4. 笔记名和笔记本名需要符合文件系统的要求。

## 🍉 更新记录

2020.12.27 工具栏固定，修复bug

2020.12.20 支持公开笔记，修复若干bug，升级编辑器

2020.10.10 支持搜索功能

2020.09.27 支持文件管理，UI细节调整，升级Vditor版本

## 🤖 TODO
1.远程仓库初始化
2.公开笔记聚合页
3.远程仓库界面优化
4.用户手册



