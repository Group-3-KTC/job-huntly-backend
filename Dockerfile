# ---------- Stage 1: BUILD ----------
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Copy trước các file cấu hình để tối ưu cache
COPY gradlew settings.gradle* build.gradle* gradle/ ./
RUN chmod +x gradlew

RUN gradle --no-daemon build -x test || true

# Copy toàn bộ source
COPY . .

# Build bootJar (bỏ test để build nhanh; bỏ -x test nếu bạn muốn chạy test)
RUN ./gradlew --no-daemon clean bootJar -x test

# ---------- Stage 2: RUNTIME ----------
FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

RUN apt-get update && apt-get install -y \
    wkhtmltopdf \
    xfonts-75dpi \
    xfonts-base \
    && rm -rf /var/lib/apt/lists/*

# Copy file JAR đã build
# Nếu libs có nhiều JAR, đảm bảo chỉ còn 1 file hoặc đổi tên pattern cho đúng
COPY --from=builder /app/build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]