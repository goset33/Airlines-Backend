# Этап 1: Сборка проекта
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Этап 2: Запуск на легкой JRE 17
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["sh", "-c", "java -Xmx380m -Dserver.port=${PORT:-8080} -jar app.jar"]
