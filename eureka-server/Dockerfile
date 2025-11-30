# Используем multi-stage build для оптимизации
FROM --platform=linux/amd64 maven:3.8-eclipse-temurin-17 AS build

WORKDIR /build

# Копируем parent POM и все pom.xml файлы модулей
COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/
COPY config-server/pom.xml config-server/
COPY auth-server/pom.xml auth-server/
COPY api-gateway/pom.xml api-gateway/
COPY parking-service/pom.xml parking-service/
COPY vehicle-service/pom.xml vehicle-service/
COPY reservation-service/pom.xml reservation-service/

# Скачиваем зависимости (это будет закешировано Docker)
RUN mvn -pl eureka-server -am dependency:go-offline -B || true

# Копируем исходный код
COPY eureka-server/src eureka-server/src

# Собираем только eureka-server
RUN mvn -pl eureka-server -am clean package -DskipTests -B

# Финальный образ
FROM --platform=linux/amd64 eclipse-temurin:17-jre

WORKDIR /app

# Копируем jar из build stage
COPY --from=build /build/eureka-server/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
