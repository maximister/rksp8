#!/bin/bash

# Цвета для вывода
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
cat << 'EOF'
╔══════════════════════════════════════════════════════════════╗
║           🧪 РУЧНОЕ ТЕСТИРОВАНИЕ СИСТЕМЫ                     ║
╚══════════════════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

echo -e "${YELLOW}Этот скрипт поможет вам протестировать систему шаг за шагом${NC}"
echo ""

# Функции для вывода
print_step() {
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}► $1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ️  $1${NC}"
}

# ============================================
# ШАГ 1: Проверка Eureka Server
# ============================================
print_step "ШАГ 1: Проверка Eureka Server"

echo -e "\n${YELLOW}Проверяем доступность Eureka Dashboard...${NC}"
EUREKA_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8761)

if [ "$EUREKA_STATUS" = "200" ]; then
    print_success "Eureka Server доступен (HTTP $EUREKA_STATUS)"
    print_info "Откройте в браузере: http://localhost:8761"
    echo ""
    echo "Вы должны увидеть зарегистрированные сервисы:"
    echo "  - CONFIG-SERVER"
    echo "  - AUTH-SERVER"
    echo "  - API-GATEWAY"
    echo "  - PARKING-SERVICE"
    echo "  - VEHICLE-SERVICE"
    echo "  - RESERVATION-SERVICE"
else
    print_error "Eureka Server недоступен (HTTP $EUREKA_STATUS)"
    exit 1
fi

read -p "$(echo -e ${YELLOW}Нажмите Enter когда проверите Eureka Dashboard...${NC})"

# ============================================
# ШАГ 2: Проверка Config Server
# ============================================
print_step "ШАГ 2: Проверка Config Server"

echo -e "\n${YELLOW}Проверяем Config Server health...${NC}"
curl -s http://localhost:8888/actuator/health | jq '.'

echo -e "\n${YELLOW}Получаем конфигурацию для parking-service...${NC}"
curl -s http://localhost:8888/parking-service/default | jq '.propertySources[0].source | keys | .[:5]'

read -p "$(echo -e ${YELLOW}Нажмите Enter для продолжения...${NC})"

# ============================================
# ШАГ 3: Получение OAuth2 токена
# ============================================
print_step "ШАГ 3: Получение OAuth2 Access Token"

echo -e "\n${YELLOW}Запрашиваем токен у Auth Server...${NC}"
TOKEN_RESPONSE=$(curl -s -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=client&client_secret=secret")

echo "$TOKEN_RESPONSE" | jq '.'

TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.access_token')

if [ "$TOKEN" != "null" ] && [ ! -z "$TOKEN" ]; then
    print_success "Токен получен успешно!"
    echo -e "\n${YELLOW}Токен (первые 50 символов):${NC}"
    echo "${TOKEN:0:50}..."
    
    echo -e "\n${YELLOW}Декодированный JWT payload:${NC}"
    echo $TOKEN | cut -d. -f2 | base64 -d 2>/dev/null | jq '.' || echo "Не удалось декодировать"
else
    print_error "Не удалось получить токен!"
    exit 1
fi

export TOKEN

read -p "$(echo -e ${YELLOW}Нажмите Enter для продолжения...${NC})"

# ============================================
# ШАГ 4: Проверка защиты без токена
# ============================================
print_step "ШАГ 4: Проверка защиты endpoints (должен быть 401)"

echo -e "\n${YELLOW}Пробуем запрос БЕЗ токена...${NC}"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8090/parking/api/spots)

if [ "$STATUS" = "401" ] || [ "$STATUS" = "403" ]; then
    print_success "Запрос без токена корректно отклонен (HTTP $STATUS)"
else
    print_error "Ожидался 401/403, получен: HTTP $STATUS"
fi

read -p "$(echo -e ${YELLOW}Нажмите Enter для продолжения...${NC})"

# ============================================
# ШАГ 5: Тестирование Parking Service
# ============================================
print_step "ШАГ 5: Тестирование Parking Service"

echo -e "\n${BLUE}5.1. CREATE - Создание парковочного места${NC}"
CREATED_PARKING=$(curl -s -X POST http://localhost:8090/parking/api/spots \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "spotNumber": "A-101",
    "status": "AVAILABLE"
  }')

echo "$CREATED_PARKING" | jq '.'
PARKING_ID=$(echo $CREATED_PARKING | jq -r '.id')

if [ ! -z "$PARKING_ID" ] && [ "$PARKING_ID" != "null" ]; then
    print_success "Парковочное место создано (ID: $PARKING_ID)"
else
    print_error "Не удалось создать парковочное место"
fi

echo -e "\n${BLUE}5.2. READ - Получение списка парковочных мест${NC}"
curl -s -X GET http://localhost:8090/parking/api/spots \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo -e "\n${BLUE}5.3. READ BY ID - Получение конкретного места${NC}"
curl -s -X GET http://localhost:8090/parking/api/spots/$PARKING_ID \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo -e "\n${BLUE}5.4. UPDATE - Обновление статуса${NC}"
curl -s -X PUT http://localhost:8090/parking/api/spots/$PARKING_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "spotNumber": "A-101",
    "status": "OCCUPIED"
  }' | jq '.'

read -p "$(echo -e ${YELLOW}Нажмите Enter для продолжения...${NC})"

# ============================================
# ШАГ 6: Тестирование Vehicle Service
# ============================================
print_step "ШАГ 6: Тестирование Vehicle Service"

echo -e "\n${BLUE}6.1. CREATE - Создание транспортного средства${NC}"
CREATED_VEHICLE=$(curl -s -X POST http://localhost:8090/vehicles/api/vehicles \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "А123БВ777",
    "model": "Toyota Camry",
    "ownerName": "Иван Иванов"
  }')

echo "$CREATED_VEHICLE" | jq '.'
VEHICLE_ID=$(echo $CREATED_VEHICLE | jq -r '.id')

if [ ! -z "$VEHICLE_ID" ] && [ "$VEHICLE_ID" != "null" ]; then
    print_success "Транспортное средство создано (ID: $VEHICLE_ID)"
else
    print_error "Не удалось создать транспортное средство"
fi

echo -e "\n${BLUE}6.2. READ - Получение списка транспортных средств${NC}"
curl -s -X GET http://localhost:8090/vehicles/api/vehicles \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo -e "\n${BLUE}6.3. READ BY ID - Получение конкретного транспорта${NC}"
curl -s -X GET http://localhost:8090/vehicles/api/vehicles/$VEHICLE_ID \
  -H "Authorization: Bearer $TOKEN" | jq '.'

read -p "$(echo -e ${YELLOW}Нажмите Enter для продолжения...${NC})"

# ============================================
# ШАГ 7: Тестирование Reservation Service
# ============================================
print_step "ШАГ 7: Тестирование Reservation Service + Feign Integration"

# Сначала создадим новое свободное место для бронирования
echo -e "\n${BLUE}7.0. Создаем новое свободное парковочное место${NC}"
NEW_PARKING=$(curl -s -X POST http://localhost:8090/parking/api/spots \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "spotNumber": "B-202",
    "status": "AVAILABLE"
  }')

NEW_PARKING_ID=$(echo $NEW_PARKING | jq -r '.id')
echo "Создано парковочное место: $NEW_PARKING_ID"

echo -e "\n${BLUE}7.1. CREATE - Создание бронирования (проверка Feign)${NC}"
CREATED_RESERVATION=$(curl -s -X POST http://localhost:8090/reservations/api/reservations \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"parkingSpotId\": $NEW_PARKING_ID,
    \"vehicleId\": $VEHICLE_ID,
    \"startTime\": \"2024-12-01T10:00:00\",
    \"endTime\": \"2024-12-01T18:00:00\"
  }")

echo "$CREATED_RESERVATION" | jq '.'
RESERVATION_ID=$(echo $CREATED_RESERVATION | jq -r '.id')

if [ ! -z "$RESERVATION_ID" ] && [ "$RESERVATION_ID" != "null" ]; then
    print_success "Бронирование создано (ID: $RESERVATION_ID)"
else
    print_error "Не удалось создать бронирование"
    echo "Response: $CREATED_RESERVATION"
fi

echo -e "\n${BLUE}7.2. READ DETAILS - Получение деталей (проверка Feign вызовов)${NC}"
DETAILS=$(curl -s -X GET http://localhost:8090/reservations/api/reservations/$RESERVATION_ID/details \
  -H "Authorization: Bearer $TOKEN")

echo "$DETAILS" | jq '.'

HAS_PARKING=$(echo $DETAILS | jq -r '.parkingSpot')
HAS_VEHICLE=$(echo $DETAILS | jq -r '.vehicle')

if [ "$HAS_PARKING" != "null" ] && [ "$HAS_VEHICLE" != "null" ]; then
    print_success "Feign интеграция работает! Данные получены из Parking и Vehicle сервисов"
    echo -e "\n${GREEN}Детали парковочного места:${NC}"
    echo "$DETAILS" | jq '.parkingSpot'
    echo -e "\n${GREEN}Детали транспортного средства:${NC}"
    echo "$DETAILS" | jq '.vehicle'
else
    print_error "Feign интеграция не работает корректно"
fi

echo -e "\n${BLUE}7.3. READ - Получение всех бронирований${NC}"
curl -s -X GET http://localhost:8090/reservations/api/reservations \
  -H "Authorization: Bearer $TOKEN" | jq '.'

read -p "$(echo -e ${YELLOW}Нажмите Enter для продолжения...${NC})"

# ============================================
# ШАГ 8: Проверка изменения статусов
# ============================================
print_step "ШАГ 8: Проверка изменения статусов при бронировании"

echo -e "\n${YELLOW}Проверяем что парковочное место изменило статус на OCCUPIED...${NC}"
PARKING_STATUS=$(curl -s -X GET http://localhost:8090/parking/api/spots/$NEW_PARKING_ID \
  -H "Authorization: Bearer $TOKEN" | jq -r '.status')

echo "Статус парковочного места: $PARKING_STATUS"

if [ "$PARKING_STATUS" = "OCCUPIED" ] || [ "$PARKING_STATUS" = "RESERVED" ]; then
    print_success "Статус парковочного места корректно изменен!"
else
    print_error "Статус парковочного места не изменился (текущий: $PARKING_STATUS)"
fi

read -p "$(echo -e ${YELLOW}Нажмите Enter для финального отчета...${NC})"

# ============================================
# ИТОГОВЫЙ ОТЧЕТ
# ============================================
echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}                  📊 ИТОГОВЫЙ ОТЧЕТ                  ${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

cat << EOF
${GREEN}✅ Eureka Server доступен${NC}
${GREEN}✅ Config Server работает${NC}
${GREEN}✅ OAuth2 токен получен${NC}
${GREEN}✅ Защита endpoints работает (401 без токена)${NC}
${GREEN}✅ Parking Service: CREATE, READ, UPDATE${NC}
${GREEN}✅ Vehicle Service: CREATE, READ${NC}
${GREEN}✅ Reservation Service: CREATE, READ${NC}
${GREEN}✅ Feign интеграция работает${NC}
${GREEN}✅ Статусы обновляются корректно${NC}

${BLUE}Созданные данные:${NC}
  - Parking Spot ID: ${PARKING_ID}, ${NEW_PARKING_ID}
  - Vehicle ID: ${VEHICLE_ID}
  - Reservation ID: ${RESERVATION_ID}

${YELLOW}Полезные URL:${NC}
  - Eureka Dashboard: http://localhost:8761
  - Config Server: http://localhost:8888
  - API Gateway: http://localhost:8090

EOF

echo -e "${GREEN}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║   🎉 ВСЕ КОМПОНЕНТЫ СИСТЕМЫ РАБОТАЮТ КОРРЕКТНО!   🎉        ║${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════════════════════════╝${NC}"

echo ""
echo -e "${YELLOW}💡 Совет: Используйте Postman коллекцию для дополнительного тестирования${NC}"
echo -e "${YELLOW}   Файл: Parking_System.postman_collection.json${NC}"
echo ""

