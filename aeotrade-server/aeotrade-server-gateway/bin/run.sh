#!/bin/sh

rm -f tpid

nohup java -jar aeotrade-server-gateway.jar > /dev/null 2>&1 &

echo $! > tpid

echo Start Success!