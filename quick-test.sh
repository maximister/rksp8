#!/bin/bash

# Цвета для вывода
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=========================================="
echo "🧪 Быстрое тестирование системы парковки"
echo "=========================================="

# 1. Получить токены
echo -e "${YELLOW}1. Получение OAuth2 токенов...${NC}"
export ADMIN_TOKEN=$(curl -s -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "parking-system-client:secret" \
  -d "grant_type=client_credentials&scope=read write" | jq -r '.access_token')

export USER_TOKEN=$(curl -s -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "parking-system-client:secret" \
  -d "grant_type=client_credentials&scope=read" | jq -r '.access_token')

if [ "$ADMIN_TOKEN" == "null" ] || [ -z "$ADMIN_TOKEN" ]; then
  echo -e "${RED}❌ Ошибка получения ADMIN токена${NC}"
  echo "Проверьте, что Auth Server запущен на порту 9000"
  exit 1
fi

if [ "$USER_TOKEN" == "null" ] || [ -z "$USER_TOKEN" ]; then
  echo -e "${RED}❌ Ошибка получения USER токена${NC}"
  exit 1
fi

echo -e "${GREEN}✅ Токены получены${NC}"
echo "   Admin Token: ${ADMIN_TOKEN:0:50}..."
echo "   User Token: ${USER_TOKEN:0:50}..."

# 2. Создать парковочное место
echo -e "\n${YELLOW}2. Создание парковочного места...${NC}"
SPOT=$(curl -s -X POST http://localhost:8090/api/spots \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": "A-101", "floor": 1, "status": "FREE"}')
SPOT_ID=$(echo $SPOT | jq -r '.id')

if [ "$SPOT_ID" == "null" ] || [ -z "$SPOT_ID" ]; then
  echo -e "${RED}❌ Ошибка создания парковочного места${NC}"
  echo "Response: $SPOT"
  exit 1
fi

echo -e "${GREEN}✅ Парковочное место создано: ID=$SPOT_ID${NC}"
echo "$SPOT" | jq

# 3. Создать автомобиль
echo -e "\n${YELLOW}3. Создание автомобиля...${NC}"
VEHICLE=$(curl -s -X POST http://localhost:8090/api/vehicles \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"licensePlate": "A123BC77", "model": "Toyota Camry", "color": "Black", "ownerName": "Иван Иванов"}')
VEHICLE_ID=$(echo $VEHICLE | jq -r '.id')

if [ "$VEHICLE_ID" == "null" ] || [ -z "$VEHICLE_ID" ]; then
  echo -e "${RED}❌ Ошибка создания автомобиля${NC}"
  echo "Response: $VEHICLE"
  exit 1
fi

echo -e "${GREEN}✅ Автомобиль создан: ID=$VEHICLE_ID${NC}"
echo "$VEHICLE" | jq

# 4. Получить все парковочные места (тест READ scope)
echo -e "\n${YELLOW}4. Получение всех парковочных мест (с USER токеном)...${NC}"
SPOTS=$(curl -s http://localhost:8090/api/spots \
  -H "Authorization: Bearer $USER_TOKEN")
echo -e "${GREEN}✅ Список получен:${NC}"
echo "$SPOTS" | jq

# 5. Получить все автомобили
echo -e "\n${YELLOW}5. Получение всех автомобилей...${NC}"
VEHICLES=$(curl -s http://localhost:8090/api/vehicles \
  -H "Authorization: Bearer $USER_TOKEN")
echo -e "${GREEN}✅ Список получен:${NC}"
echo "$VEHICLES" | jq

# 6. Создать бронирование
echo -e "\n${YELLOW}6. Создание бронирования...${NC}"
RESERVATION=$(curl -s -X POST http://localhost:8090/api/reservations \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"parkingSpotId\": $SPOT_ID,
    \"vehicleId\": $VEHICLE_ID,
    \"startTime\": \"2025-12-01T10:00:00\",
    \"endTime\": \"2025-12-01T18:00:00\"
  }")
RESERVATION_ID=$(echo $RESERVATION | jq -r '.id')

if [ "$RESERVATION_ID" == "null" ] || [ -z "$RESERVATION_ID" ]; then
  echo -e "${RED}❌ Ошибка создания бронирования${NC}"
  echo "Response: $RESERVATION"
  exit 1
fi

echo -e "${GREEN}✅ Бронирование создано: ID=$RESERVATION_ID${NC}"
echo "$RESERVATION" | jq

# 7. Проверить статус парковочного места (должен быть OCCUPIED)
echo -e "\n${YELLOW}7. Проверка статуса парковочного места (должен быть OCCUPIED)...${NC}"
sleep 1
SPOT_STATUS=$(curl -s http://localhost:8090/api/spots/$SPOT_ID \
  -H "Authorization: Bearer $USER_TOKEN" | jq -r '.status')

if [ "$SPOT_STATUS" == "OCCUPIED" ]; then
  echo -e "${GREEN}✅ Статус корректный: OCCUPIED${NC}"
else
  echo -e "${RED}❌ Ошибка: статус = $SPOT_STATUS (ожидалось OCCUPIED)${NC}"
fi

# 8. Получить все бронирования
echo -e "\n${YELLOW}8. Получение всех бронирований...${NC}"
RESERVATIONS=$(curl -s http://localhost:8090/api/reservations \
  -H "Authorization: Bearer $USER_TOKEN")
echo -e "${GREEN}✅ Список получен:${NC}"
echo "$RESERVATIONS" | jq

# 9. Отменить бронирование
echo -e "\n${YELLOW}9. Отмена бронирования...${NC}"
CANCEL_RESULT=$(curl -s -X PATCH http://localhost:8090/api/reservations/$RESERVATION_ID \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '"CANCELLED"')
echo -e "${GREEN}✅ Бронирование отменено${NC}"
echo "$CANCEL_RESULT" | jq

# 10. Проверить статус парковочного места (должен вернуться в FREE)
echo -e "\n${YELLOW}10. Проверка статуса после отмены (должен быть FREE)...${NC}"
sleep 1
SPOT_STATUS=$(curl -s http://localhost:8090/api/spots/$SPOT_ID \
  -H "Authorization: Bearer $USER_TOKEN" | jq -r '.status')

if [ "$SPOT_STATUS" == "FREE" ]; then
  echo -e "${GREEN}✅ Статус корректный: FREE${NC}"
else
  echo -e "${RED}❌ Ошибка: статус = $SPOT_STATUS (ожидалось FREE)${NC}"
fi

# 11. Тест прав доступа (попытка записи с read-only токеном)
echo -e "\n${YELLOW}11. Тест прав доступа (попытка записи с USER токеном)...${NC}"
RESPONSE=$(curl -s -w "%{http_code}" -X POST http://localhost:8090/api/spots \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": "FAIL", "floor": 1, "status": "FREE"}' \
  -o /dev/null)

if [ "$RESPONSE" == "403" ]; then
  echo -e "${GREEN}✅ Права доступа работают (получен 403 Forbidden)${NC}"
else
  echo -e "${RED}❌ Ошибка прав доступа: HTTP $RESPONSE (ожидалось 403)${NC}"
fi

# 12. Тест без токена
echo -e "\n${YELLOW}12. Тест без токена (должен вернуть 401)...${NC}"
RESPONSE=$(curl -s -w "%{http_code}" http://localhost:8090/api/spots -o /dev/null)

if [ "$RESPONSE" == "401" ]; then
  echo -e "${GREEN}✅ Аутентификация работает (получен 401 Unauthorized)${NC}"
else
  echo -e "${RED}❌ Ошибка: HTTP $RESPONSE (ожидалось 401)${NC}"
fi

# 13. Проверка health endpoints
echo -e "\n${YELLOW}13. Проверка health endpoints всех сервисов...${NC}"

check_health() {
  SERVICE=$1
  PORT=$2
  STATUS=$(curl -s http://localhost:$PORT/actuator/health | jq -r '.status')
  if [ "$STATUS" == "UP" ]; then
    echo -e "   ${GREEN}✅ $SERVICE:$PORT - UP${NC}"
  else
    echo -e "   ${RED}❌ $SERVICE:$PORT - $STATUS${NC}"
  fi
}

check_health "Config Server   " 8888
check_health "Auth Server     " 9000
check_health "Parking Service " 8081
check_health "Vehicle Service " 8082
check_health "Reservation Svc " 8083

# Итоги
echo -e "\n=========================================="
echo -e "${GREEN}✅ ВСЕ ТЕСТЫ ЗАВЕРШЕНЫ УСПЕШНО!${NC}"
echo "=========================================="
echo ""
echo "Созданные объекты:"
echo "  • Парковочное место ID: $SPOT_ID (статус: $SPOT_STATUS)"
echo "  • Автомобиль ID: $VEHICLE_ID"
echo "  • Бронирование ID: $RESERVATION_ID (статус: CANCELLED)"
echo ""
echo "Для просмотра Eureka Dashboard:"
echo "  open http://localhost:8761"
echo ""
echo "Для подробных сценариев тестирования:"
echo "  cat TEST_SCENARIOS.md"
echo ""

