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

COPY target/NyxBot.jar /app

EXPOSE 8080

# 使用非 root 用户运行应用
RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup && \
    chown -R appuser:appgroup /app

USER appuser

CMD ["java", "-XX:+UseZGC", "-Dfile.encoding=UTF-8", "-jar", "/app/NyxBot.jar"]