FROM eclipse-temurin:17-jdk-alpine AS common-builder
WORKDIR /workspace

COPY common/gradle gradle
COPY common/gradlew gradlew
COPY common/build.gradle build.gradle
COPY common/settings.gradle settings.gradle
COPY common/src src

RUN chmod +x gradlew && ./gradlew publishToMavenLocal -x test --no-daemon

FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace

COPY --from=common-builder /root/.m2 /root/.m2

COPY season-service/gradle gradle
COPY season-service/gradlew gradlew
COPY season-service/build.gradle build.gradle
COPY season-service/settings.gradle settings.gradle
COPY season-service/src src

RUN chmod +x gradlew && ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]