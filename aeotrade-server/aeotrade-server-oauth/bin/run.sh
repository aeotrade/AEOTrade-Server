#!/bin/sh

rm -f tpid

nohup java -jar aeotrade-provider-oauth.jar > /dev/null 2>&1 &

echo $! > tpid

echo Start Success!