# Используем multi-stage build для оптимизации
FROM --platform=linux/amd64 maven:3.8-eclipse-temurin-17 AS build

WORKDIR /build

# Копируем parent POM и все pom.xml файлы модулей
COPY pom.xml .
COPY parking-service/pom.xml parking-service/
COPY eureka-server/pom.xml eureka-server/
COPY config-server/pom.xml config-server/
COPY auth-server/pom.xml auth-server/
COPY api-gateway/pom.xml api-gateway/
COPY vehicle-service/pom.xml vehicle-service/
COPY reservation-service/pom.xml reservation-service/

# Скачиваем зависимости
RUN mvn -pl parking-service -am dependency:go-offline -B || true

# Копируем исходный код
COPY parking-service/src parking-service/src

# Собираем только parking-service
RUN mvn -pl parking-service -am clean package -DskipTests -B

# Финальный образ
FROM --platform=linux/amd64 eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /build/parking-service/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
