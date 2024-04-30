<div align="center">

<img src="https://github.com/KingPrimes/NyxBot/assets/50130875/2620c999-4d63-4d68-8df3-38d4457d9957" style="width: 35%" alt="">

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
4. 安装[Java17 Oracle](https://www.oracle.com/java/technologies/downloads/#java17)
   或 [Open Jdk 17](https://www.openlogic.com/openjdk-downloads)
5. 下载[NyxBot](https://github.com/KingPrimes/NyxBot/releases)
6. 打开命令行工具，跳转到 "NyxBot.jar" 所存放的目录 **启动命令** **java -jar NyxBot.jar**
7. 如若8080端口被占用或你想要使用别的端口可在启动命令后方添加参数.
   ``` cmd
   选择其中一个启动 NyxBot.jar
   java -Dserver.port = 8080 -jar NyxBot.jar
   
   java -jar NyxBot.jar --server.port = 8080
   ```

# 配置

1. 正常启动程序后会自动打开浏览器并跳转到 [配置页面 http://localhost:8080](http://localhost:8080)
2. 在web页面登录 默认账号: **admin** 密码：**admin123**
3. 默认是配置是白名单模式，请自行添加要使用机器人的群，**若不添加则发送任何指令机器人无响应**

   - 在web中选择 **配置 -> 黑白名单 -> 群白名单**
   - 点击添加按钮,则会弹出添加群的窗口
   - 选择要添加的群,点击确定按钮即可
   - 也可以直接切换成黑名单

4. 所有的配置都可以在web页面进行修改

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

# 赞助

### 本项目由 [![粒子GO](https://github.com/KingPrimes/NyxBot/assets/50130875/09cc82ad-4744-495b-9c67-883251ae5342)](https://www.lizigo.cn/aff/LYRPGUMP) 赞助

----

## Stargazers over time

[![Stargazers over time](https://starchart.cc/KingPrimes/NyxBot.svg)](https://starchart.cc/KingPrimes/NyxBot)

# 鸣谢

- [warframe-status](https://github.com/WFCD/warframe-status) 开放的Warframe状态查询接口
- [Shiro](https://github.com/MisakaTAT/Shiro) OneBot 协议Sdk
- [RapidOcr-Java](https://github.com/MyMonsterCat/RapidOcr-Java) OCR 文字识别
