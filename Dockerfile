# ---- 1단계: 빌드 ----
FROM gradle:8.10-jdk17 AS build
WORKDIR /app

# Gradle 캐시 활용을 위해 설정 파일 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle/ gradle/

# 의존성 미리 다운(옵션이지만 빌드 빨라짐)
RUN gradle dependencies || true

# 나머지 소스 복사
COPY . .

# 테스트는 일단 제외하고 실행 가능한 JAR만 빌드
RUN gradle clean bootJar -x test

# ---- 2단계: 실행 ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드 결과 JAR를 app.jar로 복사 (이름 몰라도 됨, *.jar로 해결)
COPY --from=build /app/build/libs/*.jar app.jar

# Render가 주는 PORT 사용 (스프링에서 server.port=${PORT:8080} 써야 함)
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
