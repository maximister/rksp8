#!/bin/bash

# Скрипт для запуска всех микросервисов
# Использование: ./start-all.sh

echo "======================================"
echo "Запуск системы управления парковкой"
echo "======================================"
echo ""

# Цвета для вывода
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Функция для запуска сервиса в новом терминале (macOS)
start_service() {
    local service_name=$1
    local port=$2
    
    echo -e "${YELLOW}Запуск $service_name на порту $port...${NC}"
    
    osascript -e "tell app \"Terminal\" 
        do script \"cd $(pwd)/$service_name && echo 'Запуск $service_name...' && mvn spring-boot:run\"
    end tell"
    
    sleep 2
}

# 1. Запускаем Eureka Server (должен быть первым)
echo -e "${GREEN}Шаг 1: Запуск Eureka Server${NC}"
start_service "eureka-server" "8761"
echo "Ожидание запуска Eureka Server (30 секунд)..."
sleep 30

# 2. Запускаем микросервисы
echo -e "${GREEN}Шаг 2: Запуск микросервисов${NC}"
start_service "parking-service" "8081"
sleep 5
start_service "vehicle-service" "8082"
sleep 5
start_service "reservation-service" "8083"
sleep 5

# 3. Запускаем API Gateway
echo -e "${GREEN}Шаг 3: Запуск API Gateway${NC}"
start_service "api-gateway" "8080"

echo ""
echo -e "${GREEN}======================================"
echo "Все сервисы запущены!"
echo "======================================${NC}"
echo ""
echo "Проверьте статус сервисов:"
echo "  Eureka Dashboard: http://localhost:8761"
echo "  API Gateway: http://localhost:8080"
echo ""
echo "Для тестирования используйте примеры из API_EXAMPLES.md"
echo ""




