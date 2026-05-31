# ---- Build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# Gradle 래퍼 및 빌드 스크립트 먼저 복사 (의존성 레이어 캐싱)
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# 소스 복사 후 빌드 (테스트 제외)
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon \
    && cp build/libs/*-SNAPSHOT.jar app.jar

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app
# 최소 권한: non-root 사용자로 실행 (Trivy DS-0002)
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
ENV TZ=Asia/Seoul
COPY --from=build --chown=appuser:appgroup /workspace/app.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
