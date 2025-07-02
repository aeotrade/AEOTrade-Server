FROM hub.aeotrade.com/open-source/openjdk:11.0-jre-slim AS builder
WORKDIR /usr/share/aeotrade-server-gateway

COPY bin/aeotrade-server-gateway.jar .

RUN useradd -m aeotrade -s /bin/bash && \
    chown -R aeotrade:aeotrade /usr/share/aeotrade-server-gateway

USER aeotrade
EXPOSE 8077
ENV TZ=Asia/Shanghai \
    LC_ALL=C.UTF-8 \
    LANG=C.UTF-8 
CMD ["java", "-jar", "aeotrade-server-gateway.jar"]