FROM hub.aeotrade.com/open-source/openjdk:11.0-jre-slim AS builder
WORKDIR /usr/share/aeotrade-server-message

COPY bin/aeotrade-server-message.jar .

RUN useradd -m aeotrade -s /bin/bash && \
    mkdir /usr/share/upload && \
    chown -R aeotrade:aeotrade /usr/share/aeotrade-server-message /usr/share/upload

USER aeotrade
EXPOSE 8087
ENV TZ=Asia/Shanghai \
    LC_ALL=C.UTF-8 \
    LANG=C.UTF-8 
CMD ["java", "-jar", "aeotrade-server-message.jar"]