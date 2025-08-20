# ---------- Stage 1: BUILD ----------
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Copy trước các file cấu hình để tối ưu cache
COPY gradlew settings.gradle* build.gradle* gradle/ ./
RUN chmod +x gradlew

# Kéo dependencies trước (tận dụng cache giữa các lần build)
# Không fail build nếu một số cấu hình không có (|| true)
RUN ./gradlew --no-daemon dependencies || true

# Copy toàn bộ source
COPY . .

# Build bootJar (bỏ test để build nhanh; bỏ -x test nếu bạn muốn chạy test)
RUN ./gradlew --no-daemon clean bootJar -x test

# ---------- Stage 2: RUNTIME ----------
FROM eclipse-temurin:17-jre-jammy AS runtime

# Tạo user không đặc quyền
RUN useradd -ms /bin/bash appuser
USER appuser

WORKDIR /app

# Copy file JAR đã build
# Nếu libs có nhiều JAR, đảm bảo chỉ còn 1 file hoặc đổi tên pattern cho đúng
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# Thiết lập một số tuỳ chọn JVM thân thiện với container
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=25.0 -XX:+ExitOnOutOfMemoryError"
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

EXPOSE 8080

# HEALTHCHECK (bật khi bạn có actuator)
# HEALTHCHECK --interval=30s --timeout=3s --retries=3 CMD wget -qO- "http://localhost:${SERVER_PORT}/actuator/health" || exit 1

ENTRYPOINT ["java","-jar","/app/app.jar"]