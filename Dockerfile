FROM openjdk:8-jre-alpine

LABEL maintainer="zhongwenchao <z546948105@qq.com>"
LABEL description="zwc-generator - 代码生成工具"

WORKDIR /app

COPY target/zwc-generator-1.0.0.jar app.jar

EXPOSE 8899

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-jar", "app.jar"]
