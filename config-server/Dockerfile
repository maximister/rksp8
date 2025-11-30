# Используем multi-stage build для оптимизации
FROM --platform=linux/amd64 maven:3.8-eclipse-temurin-17 AS build

WORKDIR /build

# Копируем parent POM и все pom.xml файлы модулей
COPY pom.xml .
COPY config-server/pom.xml config-server/
COPY eureka-server/pom.xml eureka-server/
COPY auth-server/pom.xml auth-server/
COPY api-gateway/pom.xml api-gateway/
COPY parking-service/pom.xml parking-service/
COPY vehicle-service/pom.xml vehicle-service/
COPY reservation-service/pom.xml reservation-service/

# Скачиваем зависимости
RUN mvn -pl config-server -am dependency:go-offline -B || true

# Копируем исходный код и конфигурационные файлы
COPY config-server/src config-server/src
COPY config-repo /app/config-repo

# Собираем только config-server
RUN mvn -pl config-server -am clean package -DskipTests -B

# Финальный образ
FROM --platform=linux/amd64 eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /build/config-server/target/*.jar app.jar

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"]
