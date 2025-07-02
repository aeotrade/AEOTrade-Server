#!/bin/sh

#指定环境参数
APP_ENV_VARIABLES=" --nacos.url=192.168.0.36 "
JAVA_MEM_OPTS=" -server -Xmx256m "

APP_NAME="aeotrade-server-gateway.jar"

PIDS=`ps aux | grep java | grep "$APP_NAME" |awk '{print $2}'`
if [ -n "$PIDS" ]; then
    echo "ERROR: The $APP_NAME already started!"
    echo "PID: $PIDS"
    exit 1
fi

JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Duser.timezone=GMT+08"

echo "Starting the $APP_NAME ...\c"

nohup java $JAVA_MEM_OPTS $JAVA_OPTS -jar $APP_NAME $APP_ENV_VARIABLES > nohup.out 2>&1 < /dev/null &


echo "OK!"

PIDS=`ps aux | grep java | grep "$APP_NAME" | awk '{print $2}'`

echo "PID: $PIDS"