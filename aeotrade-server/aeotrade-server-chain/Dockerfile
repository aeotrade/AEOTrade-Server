FROM hub.aeotrade.com/aeotrade/openjdk:11.0-jre-slim AS builder
WORKDIR /app
COPY bin/aeotrade-server-chain.jar .
RUN java -Djarmode=layertools -jar aeotrade-server-chain.jar extract && rm aeotrade-server-chain.jar


FROM hub.aeotrade.com/aeotrade/openjdk:11.0-jre-slim AS runner
WORKDIR /usr/share/aeotrade-server-chain
    
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

RUN useradd -m aeotrade -s /bin/bash && \
    mkdir /usr/share/upload && \
    chown -R aeotrade:aeotrade /usr/share/aeotrade-server-chain /usr/share/upload

USER aeotrade
EXPOSE 8059
ENV TZ=Asia/Shanghai \
    LC_ALL=C.UTF-8 \
    LANG=C.UTF-8 
CMD ["java","org.springframework.boot.loader.JarLauncher"]