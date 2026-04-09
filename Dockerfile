# ================== Этап сборки ==================
FROM gradle:8.10.0-jdk21 AS build
WORKDIR /app

# Копируем Gradle wrapper и конфигурационные файлы
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

# Скачиваем зависимости (кэшируется)
RUN ./gradlew dependencies --no-daemon

# Копируем исходный код
COPY src ./src

# Собираем приложение (bootJar создаёт executable jar)
RUN ./gradlew bootJar --no-daemon -x test

# ================== Этап запуска ==================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN mkdir -p /app/uploads

# Копируем готовый jar
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENV PORT=8080

ENTRYPOINT ["java", "-jar", "app.jar"]