# build
FROM openjdk:17.0-slim AS builder
WORKDIR /build

COPY gradlew .
COPY gradle gradle
COPY settings.gradle build.gradle /build/
COPY kyumall-client/ /build/kyumall-client
COPY kyumall-common/ /build/kyumall-common
RUN ./gradlew -x test --no-daemon kyumall-client:build

# APP
FROM openjdk:17.0-slim
WORKDIR /app

ENV ENV_DIR=src/main/resources
COPY --from=builder /build/kyumall-client/build/libs/*.jar .
EXPOSE 8080
ENTRYPOINT java -jar \
               -Dspring.config.location=classpath:/application.yml,/root/app/env/application-app-dev.yml,/root/app/env/application-ncloud.yml \
               -Dspring.profiles.active=dev \
               kyumall-client.jar

