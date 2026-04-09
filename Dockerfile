# ================== Этап сборки ==================
FROM gradle:8.10.0-jdk21 AS build
WORKDIR /app

# Копируем wrapper и конфиги
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

# Скачиваем зависимости
RUN ./gradlew dependencies --no-daemon

# Копируем исходники
COPY src ./src

# Собираем с явным указанием Java 21 (игнорируем toolchain проекта)
RUN ./gradlew bootJar --no-daemon -x test \
    -Porg.gradle.java.installations.auto-download=false \
    -Porg.gradle.java.installations.auto-detect=false

# ================== Этап запуска ==================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN mkdir -p /app/uploads

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENV PORT=8080

ENTRYPOINT ["java", "-jar", "app.jar"]