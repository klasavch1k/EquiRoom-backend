# ================== Этап сборки ==================
FROM gradle:8.10.0-jdk21 AS build
WORKDIR /app

# Копируем Gradle wrapper и конфиги (чтобы лучше кэшировалось)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Даём права на выполнение gradlew
RUN chmod +x gradlew

# Скачиваем зависимости (кэшируется)
RUN ./gradlew dependencies --no-daemon

# Копируем исходный код
COPY src ./src

# Собираем приложение (bootJar — создаёт executable jar)
RUN ./gradlew bootJar --no-daemon -x test

# ================== Этап запуска ==================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Создаём папку для фотографий
RUN mkdir -p /app/uploads

# Копируем только готовый jar из этапа сборки
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENV PORT=8080

ENTRYPOINT ["java", "-jar", "app.jar"]