#!/bin/bash

REPOSITORY=/root/app

echo "> 현재 구동중인 애플리케이션 pid 확인"
CURRENT_PID=$(pgrep -fl kyumall | grep java | awk '{print $1}')
echo "현재 구동중인 어플리케이션 pid: $CURRENT_PID"

echo "어플리케이션 스스로 종료"
if [ -z "$CURRENT_PID" ]; then
    echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
    echo "> kill -15 $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep 5
fi

echo "> 새 어플리케이션 배포"
JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)
echo "> JAR Name: $JAR_NAME"

echo "> $JAR_NAME 에 실행권한 추가"
chmod +x $JAR_NAME

echo "> $JAR_NAME 실행"
nohup java -jar \
    -Dspring.config.location=classpath:/application.yml,/root/app/env/application-app-dev.yml,/root/app/env/application-ncloud.yml \
    -Dspring.profiles.active=dev \
    $JAR_NAME > /root/app/nohup.out 2>&1 &
