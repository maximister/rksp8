#!/bin/bash

echo "================================================"
echo "Запуск микросервисной системы с Config Server"
echo "================================================"

# Цвета для вывода
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}1. Запуск Eureka Server (порт 8761)...${NC}"
cd eureka-server
osascript -e 'tell app "Terminal" to do script "cd '$PWD' && mvn spring-boot:run"'
cd ..
sleep 5

echo -e "${YELLOW}Ожидание запуска Eureka Server (30 секунд)...${NC}"
sleep 30

echo -e "${YELLOW}2. Запуск Config Server (порт 8888)...${NC}"
cd config-server
osascript -e 'tell app "Terminal" to do script "cd '$PWD' && mvn spring-boot:run"'
cd ..
sleep 5

echo -e "${YELLOW}Ожидание запуска Config Server (15 секунд)...${NC}"
sleep 15

echo -e "${YELLOW}3. Запуск Auth Server (порт 9000)...${NC}"
cd auth-server
osascript -e 'tell app "Terminal" to do script "cd '$PWD' && mvn spring-boot:run"'
cd ..
sleep 5

echo -e "${YELLOW}4. Запуск Parking Service (порт 8081)...${NC}"
cd parking-service
osascript -e 'tell app "Terminal" to do script "cd '$PWD' && mvn spring-boot:run"'
cd ..
sleep 2

echo -e "${YELLOW}5. Запуск Vehicle Service (порт 8082)...${NC}"
cd vehicle-service
osascript -e 'tell app "Terminal" to do script "cd '$PWD' && mvn spring-boot:run"'
cd ..
sleep 2

echo -e "${YELLOW}6. Запуск Reservation Service (порт 8083)...${NC}"
cd reservation-service
osascript -e 'tell app "Terminal" to do script "cd '$PWD' && mvn spring-boot:run"'
cd ..
sleep 2

echo -e "${YELLOW}7. Запуск API Gateway (порт 8080)...${NC}"
cd api-gateway
osascript -e 'tell app "Terminal" to do script "cd '$PWD' && mvn spring-boot:run"'
cd ..

echo ""
echo -e "${GREEN}================================================${NC}"
echo -e "${GREEN}Все сервисы запущены!${NC}"
echo -e "${GREEN}================================================${NC}"
echo ""
echo "Подождите 1-2 минуты для полной инициализации всех сервисов."
echo ""
echo "Проверьте статус:"
echo "  - Eureka Dashboard: http://localhost:8761"
echo "  - Config Server Health: curl http://localhost:8888/actuator/health"
echo "  - API Gateway: http://localhost:8080/api/parking/spots"
echo ""

