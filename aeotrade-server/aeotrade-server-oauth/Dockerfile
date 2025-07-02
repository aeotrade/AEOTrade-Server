FROM hub.aeotrade.com/open-source/openjdk:11.0-jre-slim AS builder
WORKDIR /usr/share/aeotrade-server-oauth

COPY bin/aeotrade-server-oauth.jar .

RUN useradd -m aeotrade -s /bin/bash && \
    mkdir /usr/share/upload && \
    chown -R aeotrade:aeotrade /usr/share/aeotrade-server-oauth /usr/share/upload

USER aeotrade
EXPOSE 8080
ENV TZ=Asia/Shanghai \
    LC_ALL=C.UTF-8 \
    LANG=C.UTF-8
CMD ["java", "-jar", "aeotrade-server-oauth.jar"]