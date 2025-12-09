# ---- 1단계: 빌드 ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# 프로젝트 전체 복사
COPY . .

# gradlew 실행권한 주고, 테스트는 빼고 bootJar 빌드
RUN chmod +x ./gradlew && ./gradlew clean bootJar -x test

# ---- 2단계: 실행 ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드된 JAR 하나를 app.jar로 복사
COPY --from=build /app/build/libs/*.jar app.jar

# Render에서 넘겨주는 PORT 사용 (로컬 기본 8080)
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
