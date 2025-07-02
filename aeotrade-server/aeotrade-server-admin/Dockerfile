FROM hub.aeotrade.com/open-source/openjdk:11.0-jre-slim AS builder
WORKDIR /usr/share/aeotrade-server-admin

# 替换为阿里云 Debian 源
RUN sed -i 's|deb.debian.org|mirrors.aliyun.com|g' /etc/apt/sources.list && \
    sed -i 's|security.debian.org|mirrors.aliyun.com/debian-security|g' /etc/apt/sources.list && \
    apt-get update && \
    apt-get install -y libx11-dev fontconfig fonts-dejavu-core && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY bin/aeotrade-server-admin.jar .

RUN useradd -m aeotrade -s /bin/bash && \
    mkdir /usr/share/upload && \
    chown -R aeotrade:aeotrade /usr/share/aeotrade-server-admin /usr/share/upload

USER aeotrade
EXPOSE 8094
ENV TZ=Asia/Shanghai \
    LC_ALL=C.UTF-8 \
    LANG=C.UTF-8 
CMD ["java", "-jar", "aeotrade-server-admin.jar"]