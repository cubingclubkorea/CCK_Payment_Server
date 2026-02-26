FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# 캐시 최적화
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon

# 소스 복사 후 빌드
COPY src src
RUN ./gradlew build -x test --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

# JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 (Spring 기본)
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]