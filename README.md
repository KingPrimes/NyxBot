<div align="center">

<img src="https://github.com/user-attachments/assets/549ad15a-a97a-42e8-b980-d0f28897d4ca" style="width: 35%" alt="">

# NYXBot

![GitHub forks](https://img.shields.io/github/forks/KingPrimes/NyxBot?style=social)
![GitHub Org's stars](https://img.shields.io/github/stars/KingPrimes/NyxBot?style=social)
[![QQ Group](https://img.shields.io/badge/QQ%20Group-260079469-blue)](https://jq.qq.com/?_wv=1027&k=RgqgJLij)
</br>

<img src="https://img.shields.io/badge/JDK-17+-brightgreen.svg?style=flat-square" alt="jdk-version">
<a href="https://github.com/howmanybots/onebot"><img src="https://img.shields.io/badge/OneBot-v11-blue?style=flat-square&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABABAMAAABYR2ztAAAAIVBMVEUAAAAAAAADAwMHBwceHh4UFBQNDQ0ZGRkoKCgvLy8iIiLWSdWYAAAAAXRSTlMAQObYZgAAAQVJREFUSMftlM0RgjAQhV+0ATYK6i1Xb+iMd0qgBEqgBEuwBOxU2QDKsjvojQPvkJ/ZL5sXkgWrFirK4MibYUdE3OR2nEpuKz1/q8CdNxNQgthZCXYVLjyoDQftaKuniHHWRnPh2GCUetR2/9HsMAXyUT4/3UHwtQT2AggSCGKeSAsFnxBIOuAggdh3AKTL7pDuCyABcMb0aQP7aM4AnAbc/wHwA5D2wDHTTe56gIIOUA/4YYV2e1sg713PXdZJAuncdZMAGkAukU9OAn40O849+0ornPwT93rphWF0mgAbauUrEOthlX8Zu7P5A6kZyKCJy75hhw1Mgr9RAUvX7A3csGqZegEdniCx30c3agAAAABJRU5ErkJggg==" alt=""></a>

项目创建于：2023-08-26 15:11:00
</div>

# 部署

----

1. 下载[OneBot 客户端](#客户端)
2. 配置客户端链接方式为 反向Websocket模式 填写链接
    ``` yaml
      ws://你服务器IP地址:端口号/ws/shiro
      
      ws://localhost:8080/ws/shiro
     ```
    - 以上链接为默认配置，若你使用了其他端口或域名，请自行修改
    - 你可以在web页面的配置中修改链接地址
        1. 在 web页面配置中选择 **配置 -> 配置服务**
        2. 在弹出的窗口中配置**端口号**与**地址**
        3. 点击确定按钮
        4. 之后重启NyxBot程序即可
3. 设置消息上报格式为 **CQ码**
4. 安装[Java21 Oracle](https://www.oracle.com/java/technologies/downloads/#java21)
   或 [Open Jdk 21](https://www.openlogic.com/openjdk-downloads)
5. 下载[NyxBot](https://github.com/KingPrimes/NyxBot/releases)
6. 打开命令行工具，跳转到 "NyxBot.jar" 所存放的目录 **启动命令** **java -jar NyxBot.jar**
7. 如若8080端口被占用或你想要使用别的端口可在启动命令后方添加参数.
   ``` cmd
   选择其中一个启动 NyxBot.jar
   java -Dserver.port = 8080 -jar NyxBot.jar
   
   java -jar NyxBot.jar --server.port = 8080
   ```

8. [详细部署文档](https://kingprimes.top/posts/d99b802/)
9. [云服务器推荐](#推广链接)

# 配置

1. 正常启动程序后会自动打开浏览器并跳转到 [配置页面 http://localhost:8080](http://localhost:8080)
2. 在web页面登录 **初始账号密码请查看控制台输出日志**
3. 默认是配置是黑名单模式
    - 当添加白名单之后,只有白名单中的群才能使用机器人
    - 当添加黑名单之后,黑名单中的群不能使用机器人
4. 所有的配置都可以在web页面进行修改
5. 发送**报错日志**需在启动参数末尾上添加 **--debug** 参数
      - ```shell
        # 如下启动命令
        java -jar NyxBot.jar --debug
         ```
      - Docker 平台添加 DEBUG = true 环境变量

# 客户端

----

OneBot协议客户端

| 项目地址                                                                  | 文档                                                             |
|-----------------------------------------------------------------------|----------------------------------------------------------------|
| [Gensokyo](https://github.com/Hoshinonyaruko/Gensokyo)                | [文档](https://github.com/Hoshinonyaruko/Gensokyo)               |
| [Mrs4s/go-cqhttp](https://github.com/Mrs4s/go-cqhttp)                 | [文档](https://docs.go-cqhttp.org/)                              |
| [lc-cn/onebots](https://github.com/lc-cn/onebots)                     | [文档](https://github.com/lc-cn/onebots)                         |
| [LLOneBot/LLOneBot](https://github.com/LLOneBot/LLOneBot)             | [文档](https://github.com/LLOneBot/LLOneBot/blob/main/README.md) |
| [whitechi73/OpenShamrock](https://github.com/whitechi73/OpenShamrock) | [文档](https://whitechi73.github.io/OpenShamrock/)               |

---

# [开发文档](./开发文档.md)

# 推广链接

## 首推 [阿里云-折扣专区](https://www.aliyun.com/minisite/goods?userCode=8dt5pt0g&share_source=copy_link) 和 [腾讯云-618盛典](https://curl.qcloud.com/TyfmLYii)

### [阿里云-新用户八折优惠](https://www.aliyun.com/minisite/goods?userCode=8dt5pt0g)

### [阿里云-折扣专区](https://www.aliyun.com/minisite/goods?userCode=8dt5pt0g&share_source=copy_link)

### [腾讯云-精选福利](https://curl.qcloud.com/iIyWOF4q)

### [腾讯云-618盛典](https://curl.qcloud.com/TyfmLYii)

## Stargazers over time

[![Stargazers over time](https://starchart.cc/KingPrimes/NyxBot.svg)](https://starchart.cc/KingPrimes/NyxBot)

# 鸣谢

- [Shiro](https://github.com/MisakaTAT/Shiro) OneBot 协议Sdk
- [RapidOcr-Java](https://github.com/MyMonsterCat/RapidOcr-Java) OCR 文字识别
