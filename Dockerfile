# 使用更轻量的基础镜像
FROM eclipse-temurin:17-jre-alpine

# 设置时区和中文环境（Alpine版）
RUN apk add --no-cache tzdata font-noto-cjk \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

WORKDIR /app

COPY target/NyxBot.jar /app

EXPOSE 8080

CMD ["java", "-Dfile.encoding=UTF-8", "-jar", "/app/NyxBot.jar"]