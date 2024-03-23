FROM eclipse-temurin:17-jre-focal

ENV TZ="Asia/Shanghai"

RUN apt update -y \
    && apt-get -y install fonts-noto-cjk wget \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY NyxBot.jar /app

VOLUME [ "/app" ]

EXPOSE 8080

CMD ["java", "-Dfile.encoding=UTF-8","-jar", "/app/NyxBot.jar"]