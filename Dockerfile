# 1단계: 빌드
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Gradle Wrapper 및 빌드 스크립트 복사
COPY build.gradle settings.gradle gradlew ./
RUN chmod +x gradlew
COPY gradle ./gradle
COPY src ./src

# Gradle 빌드
RUN ./gradlew clean build -x test

# 2단계: 실행
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# 빌드된 jar 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]