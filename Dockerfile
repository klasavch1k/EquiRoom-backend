# Сборка проекта (Maven)
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Запуск приложения
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем собранный jar
COPY --from=build /app/target/*.jar app.jar

# Папка для загрузки фото (чтобы файлы не терялись при перезапуске)
RUN mkdir -p /app/uploads

EXPOSE 8080

# Важно для Render
ENV PORT=8080

ENTRYPOINT ["java", "-jar", "app.jar"]