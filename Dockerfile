# 使用更轻量的基础镜像
FROM eclipse-temurin:21-jre-alpine

# 更新系统包并强制升级 libpng 到安全版本
RUN apk update && \
    apk upgrade --no-cache && \
    # 安装时区和中文环境（在安全更新后）
    apk add --no-cache tzdata font-noto-cjk && \
    # 设置时区
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    # 清理缓存（释放镜像空间）
    rm -rf /var/cache/apk/*

WORKDIR /app

# OCI 镜像标签 — GUI 管理工具（Portainer/Dockge 等）可解析为配置表单
LABEL org.opencontainers.image.title="NyxBot"
LABEL org.opencontainers.image.description="Warframe 游戏 QQBOT，支持 WorldState 通知、Market 查询、紫卡计算等功能"
LABEL org.opencontainers.image.vendor="KingPrimes"
LABEL nyxbot.config.SERVER_PORT="服务端口 (默认 8080)"
LABEL nyxbot.config.DEBUG="开启 Debug 日志 (true/false)"
LABEL nyxbot.config.SHIRO_WS_SERVER_ENABLE="启用 WS 服务端 (true/false)"
LABEL nyxbot.config.SHIRO_WS_SERVER_URL="WS 服务端路径 (默认 /ws/shiro)"
LABEL nyxbot.config.SHIRO_TOKEN="OneBot 连接 Token"
LABEL nyxbot.config.HTTP_PROXY="HTTP 代理地址 (如 http://127.0.0.1:7890)"
LABEL nyxbot.config.SOCKS_PROXY="SOCKS5 代理地址 (如 socks5://127.0.0.1:7890)"
LABEL nyxbot.config.PROXY_USER="代理认证用户名"
LABEL nyxbot.config.PROXY_PASSWORD="代理认证密码"
LABEL nyxbot.config.PLUGIN_PREFIX="指令需 @机器人 触发 (true/false)"

COPY target/NyxBot.jar /app

EXPOSE 8080

# 使用非 root 用户运行应用
RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup && \
    chown -R appuser:appgroup /app

USER appuser

CMD ["java", \
  "-XX:+UseZGC", \
  "-Dfile.encoding=UTF-8", \
  "-Djdk.virtualThreadScheduler.parallelism=2", \
  "-XX:+HeapDumpOnOutOfMemoryError", \
  "-XX:HeapDumpPath=./logs/heapdump.hprof", \
  "-jar", "/app/NyxBot.jar"]