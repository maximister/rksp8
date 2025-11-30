# Используем multi-stage build для оптимизации
FROM --platform=linux/amd64 maven:3.8-eclipse-temurin-17 AS build

WORKDIR /build

# Копируем parent POM и все pom.xml файлы модулей
COPY pom.xml .
COPY auth-server/pom.xml auth-server/
COPY eureka-server/pom.xml eureka-server/
COPY config-server/pom.xml config-server/
COPY api-gateway/pom.xml api-gateway/
COPY parking-service/pom.xml parking-service/
COPY vehicle-service/pom.xml vehicle-service/
COPY reservation-service/pom.xml reservation-service/

# Скачиваем зависимости
RUN mvn -pl auth-server -am dependency:go-offline -B || true

# Копируем исходный код
COPY auth-server/src auth-server/src

# Собираем только auth-server
RUN mvn -pl auth-server -am clean package -DskipTests -B

# Финальный образ
FROM --platform=linux/amd64 eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /build/auth-server/target/*.jar app.jar

EXPOSE 9000

ENTRYPOINT ["java", "-jar", "app.jar"]
