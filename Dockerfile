FROM eclipse-temurin:17-jre-alpine

ENV TZ="Asia/Shanghai"

RUN apk add --no-cache font-noto-cjk wget && \
    rm -rf /var/cache/apk/*

WORKDIR /app

COPY target/NyxBot.jar /app/

VOLUME [ "/app/data" ]
VOLUME [ "/app/DataSource" ]
VOLUME [ "/app/logs" ]
VOLUME [ "/app/locate.yaml" ]

EXPOSE 8080

CMD ["java", "-Dfile.encoding=UTF-8","-jar", "/app/NyxBot.jar"]
