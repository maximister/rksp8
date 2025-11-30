#!/bin/bash

echo "🚀 Быстрая сборка и запуск через Docker"
echo ""

# Шаг 1: Собираем все JAR файлы локально (используя ваш локальный Maven кэш)
echo "📦 Шаг 1/3: Сборка JAR файлов через IntelliJ IDEA Maven..."
echo "Откройте IntelliJ IDEA и выполните: Maven → rksp8 → Lifecycle → package"
echo "Или запустите вручную в терминале IDEA: mvn clean package -DskipTests"
echo ""
read -p "Нажмите Enter после того, как сборка в IDEA завершится..."

# Проверяем, что JAR файлы существуют
echo ""
echo "🔍 Проверка собранных JAR файлов..."
MISSING=0

for service in eureka-server config-server auth-server api-gateway parking-service vehicle-service reservation-service; do
    if [ -f "$service/target/"*.jar ]; then
        echo "✅ $service - JAR найден"
    else
        echo "❌ $service - JAR не найден!"
        MISSING=1
    fi
done

if [ $MISSING -eq 1 ]; then
    echo ""
    echo "❌ Не все JAR файлы собраны. Пожалуйста, соберите проект через IntelliJ IDEA."
    exit 1
fi

# Шаг 2: Создаем простые Dockerfiles для каждого сервиса
echo ""
echo "📝 Шаг 2/3: Создание оптимизированных Dockerfiles..."

for service in eureka-server config-server auth-server api-gateway parking-service vehicle-service reservation-service; do
    cat > "$service/Dockerfile.fast" << EOF
# Быстрый Dockerfile - использует локально собранный JAR
FROM --platform=linux/amd64 eclipse-temurin:17-jre

WORKDIR /app

# Копируем готовый JAR файл
COPY target/*.jar app.jar

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF
    echo "✅ $service/Dockerfile.fast создан"
done

# Шаг 3: Создаем docker-compose для быстрой сборки
echo ""
echo "📝 Создание docker-compose-fast.yml..."

cat > docker-compose-fast.yml << 'EOF'
services:
  eureka-server:
    build:
      context: ./eureka-server
      dockerfile: Dockerfile.fast
    container_name: eureka-server
    ports:
      - "8761:8761"
    networks:
      - parking-network
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8761/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 40s

  config-server:
    build:
      context: ./config-server
      dockerfile: Dockerfile.fast
    container_name: config-server
    ports:
      - "8888:8888"
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
    volumes:
      - ./config-repo:/app/config-repo:ro
    depends_on:
      eureka-server:
        condition: service_healthy
    networks:
      - parking-network
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8888/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 40s

  auth-server:
    build:
      context: ./auth-server
      dockerfile: Dockerfile.fast
    container_name: auth-server
    ports:
      - "9000:9000"
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_CONFIG_IMPORT=optional:configserver:http://config-server:8888
    depends_on:
      eureka-server:
        condition: service_healthy
      config-server:
        condition: service_healthy
    networks:
      - parking-network

  parking-service:
    build:
      context: ./parking-service
      dockerfile: Dockerfile.fast
    container_name: parking-service
    ports:
      - "8081:8081"
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_CONFIG_IMPORT=optional:configserver:http://config-server:8888
      - SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER-URI=http://auth-server:9000
    depends_on:
      eureka-server:
        condition: service_healthy
      config-server:
        condition: service_healthy
      auth-server:
        condition: service_started
    networks:
      - parking-network

  vehicle-service:
    build:
      context: ./vehicle-service
      dockerfile: Dockerfile.fast
    container_name: vehicle-service
    ports:
      - "8082:8082"
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_CONFIG_IMPORT=optional:configserver:http://config-server:8888
      - SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER-URI=http://auth-server:9000
    depends_on:
      eureka-server:
        condition: service_healthy
      config-server:
        condition: service_healthy
      auth-server:
        condition: service_started
    networks:
      - parking-network

  reservation-service:
    build:
      context: ./reservation-service
      dockerfile: Dockerfile.fast
    container_name: reservation-service
    ports:
      - "8083:8083"
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_CONFIG_IMPORT=optional:configserver:http://config-server:8888
      - SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER-URI=http://auth-server:9000
    depends_on:
      eureka-server:
        condition: service_healthy
      config-server:
        condition: service_healthy
      auth-server:
        condition: service_started
      parking-service:
        condition: service_started
      vehicle-service:
        condition: service_started
    networks:
      - parking-network

  api-gateway:
    build:
      context: ./api-gateway
      dockerfile: Dockerfile.fast
    container_name: api-gateway
    ports:
      - "8090:8080"
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_CONFIG_IMPORT=optional:configserver:http://config-server:8888
      - SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER-URI=http://auth-server:9000
    depends_on:
      eureka-server:
        condition: service_healthy
      config-server:
        condition: service_healthy
      auth-server:
        condition: service_started
      parking-service:
        condition: service_started
      vehicle-service:
        condition: service_started
      reservation-service:
        condition: service_started
    networks:
      - parking-network

networks:
  parking-network:
    driver: bridge
EOF

echo "✅ docker-compose-fast.yml создан"
echo ""
echo "🐳 Шаг 3/3: Сборка Docker образов (это будет быстро!)..."
docker-compose -f docker-compose-fast.yml build

echo ""
echo "✅ Готово! Теперь можно запустить:"
echo "   docker-compose -f docker-compose-fast.yml up -d"

