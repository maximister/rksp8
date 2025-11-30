#!/bin/bash

# Цвета для вывода
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Счетчики
PASSED=0
FAILED=0

# Функция для красивого вывода
print_test() {
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}TEST:${NC} $1"
}

print_success() {
    echo -e "${GREEN}✅ PASSED:${NC} $1"
    ((PASSED++))
}

print_failure() {
    echo -e "${RED}❌ FAILED:${NC} $1"
    ((FAILED++))
}

print_info() {
    echo -e "${YELLOW}ℹ️  INFO:${NC} $1"
}

echo -e "${BLUE}"
cat << 'EOF'
╔══════════════════════════════════════════════════════════════╗
║           🧪 АВТОМАТИЧЕСКОЕ ТЕСТИРОВАНИЕ СИСТЕМЫ             ║
╚══════════════════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

# Проверка зависимостей
print_info "Проверка необходимых утилит..."
for cmd in curl jq docker; do
    if ! command -v $cmd &> /dev/null; then
        print_failure "$cmd не установлен. Установите: brew install $cmd"
        exit 1
    fi
done

# ============================================
# ЭТАП 1: Проверка Docker контейнеров
# ============================================
echo ""
print_test "Этап 1: Проверка Docker контейнеров"

RUNNING=$(docker-compose -f docker-compose-fast.yml ps | grep -c "Up")
if [ "$RUNNING" -eq 7 ]; then
    print_success "Все 7 контейнеров запущены"
else
    print_failure "Запущено только $RUNNING из 7 контейнеров"
    print_info "Запустите: docker-compose -f docker-compose-fast.yml up -d"
    exit 1
fi

# ============================================
# ЭТАП 2: Проверка доступности сервисов
# ============================================
echo ""
print_test "Этап 2: Проверка доступности HTTP endpoints"

# Даем время на старт (если только что запустили)
print_info "Ожидание инициализации сервисов (30 сек)..."
sleep 30

# Eureka Server
if curl -s http://localhost:8761/actuator/health | jq -e '.status == "UP"' > /dev/null 2>&1; then
    print_success "Eureka Server доступен"
else
    print_failure "Eureka Server недоступен"
fi

# Config Server
if curl -s http://localhost:8888/actuator/health | jq -e '.status == "UP"' > /dev/null 2>&1; then
    print_success "Config Server доступен"
else
    print_failure "Config Server недоступен"
fi

# API Gateway
if curl -s -o /dev/null -w "%{http_code}" http://localhost:8090/actuator/health | grep -q "200\|401"; then
    print_success "API Gateway доступен"
else
    print_failure "API Gateway недоступен"
fi

# ============================================
# ЭТАП 3: Проверка регистрации в Eureka
# ============================================
echo ""
print_test "Этап 3: Проверка регистрации сервисов в Eureka"

EUREKA_APPS=$(curl -s http://localhost:8761/eureka/apps -H "Accept: application/json" | jq -r '.applications.application[].name' 2>/dev/null)

for service in CONFIG-SERVER AUTH-SERVER API-GATEWAY PARKING-SERVICE VEHICLE-SERVICE RESERVATION-SERVICE; do
    if echo "$EUREKA_APPS" | grep -q "$service"; then
        print_success "$service зарегистрирован в Eureka"
    else
        print_failure "$service НЕ зарегистрирован в Eureka"
    fi
done

# ============================================
# ЭТАП 4: Получение OAuth2 токена
# ============================================
echo ""
print_test "Этап 4: Получение OAuth2 access token"

TOKEN_RESPONSE=$(curl -s -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=client&client_secret=secret")

TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.access_token' 2>/dev/null)

if [ "$TOKEN" != "null" ] && [ ! -z "$TOKEN" ]; then
    print_success "OAuth2 токен получен"
    print_info "Token: ${TOKEN:0:50}..."
else
    print_failure "Не удалось получить OAuth2 токен"
    echo "Response: $TOKEN_RESPONSE"
    exit 1
fi

# ============================================
# ЭТАП 5: Проверка защиты без токена
# ============================================
echo ""
print_test "Этап 5: Проверка защиты endpoints (без токена)"

STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8090/parking/api/spots)
if [ "$STATUS" = "401" ] || [ "$STATUS" = "403" ]; then
    print_success "Запрос без токена корректно отклонен (HTTP $STATUS)"
else
    print_failure "Запрос без токена должен возвращать 401/403, получен: HTTP $STATUS"
fi

# ============================================
# ЭТАП 6: Тестирование Parking Service
# ============================================
echo ""
print_test "Этап 6: Тестирование Parking Service CRUD"

# CREATE
CREATED=$(curl -s -X POST http://localhost:8090/parking/api/spots \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "spotNumber": "TEST-001",
    "status": "AVAILABLE"
  }')

SPOT_ID=$(echo $CREATED | jq -r '.id' 2>/dev/null)
if [ "$SPOT_ID" != "null" ] && [ ! -z "$SPOT_ID" ]; then
    print_success "Parking: CREATE - парковочное место создано (ID: $SPOT_ID)"
else
    print_failure "Parking: CREATE - не удалось создать парковочное место"
    echo "Response: $CREATED"
fi

# READ
if [ ! -z "$SPOT_ID" ]; then
    READ=$(curl -s -X GET http://localhost:8090/parking/api/spots/$SPOT_ID \
      -H "Authorization: Bearer $TOKEN")
    
    SPOT_NUMBER=$(echo $READ | jq -r '.spotNumber' 2>/dev/null)
    if [ "$SPOT_NUMBER" = "TEST-001" ]; then
        print_success "Parking: READ - парковочное место прочитано"
    else
        print_failure "Parking: READ - данные некорректны"
    fi
fi

# UPDATE
if [ ! -z "$SPOT_ID" ]; then
    UPDATED=$(curl -s -X PUT http://localhost:8090/parking/api/spots/$SPOT_ID \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "spotNumber": "TEST-001",
        "status": "OCCUPIED"
      }')
    
    NEW_STATUS=$(echo $UPDATED | jq -r '.status' 2>/dev/null)
    if [ "$NEW_STATUS" = "OCCUPIED" ]; then
        print_success "Parking: UPDATE - статус обновлен"
    else
        print_failure "Parking: UPDATE - обновление не сработало"
    fi
fi

# LIST
LIST=$(curl -s -X GET http://localhost:8090/parking/api/spots \
  -H "Authorization: Bearer $TOKEN")

COUNT=$(echo $LIST | jq '. | length' 2>/dev/null)
if [ "$COUNT" -gt 0 ]; then
    print_success "Parking: LIST - получен список ($COUNT элементов)"
else
    print_failure "Parking: LIST - список пуст или ошибка"
fi

# ============================================
# ЭТАП 7: Тестирование Vehicle Service
# ============================================
echo ""
print_test "Этап 7: Тестирование Vehicle Service CRUD"

# CREATE
VEHICLE_CREATED=$(curl -s -X POST http://localhost:8090/vehicles/api/vehicles \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "А999ТЕ777",
    "model": "Test Car",
    "ownerName": "Test Owner"
  }')

VEHICLE_ID=$(echo $VEHICLE_CREATED | jq -r '.id' 2>/dev/null)
if [ "$VEHICLE_ID" != "null" ] && [ ! -z "$VEHICLE_ID" ]; then
    print_success "Vehicle: CREATE - транспортное средство создано (ID: $VEHICLE_ID)"
else
    print_failure "Vehicle: CREATE - не удалось создать транспортное средство"
fi

# READ
if [ ! -z "$VEHICLE_ID" ]; then
    VEHICLE_READ=$(curl -s -X GET http://localhost:8090/vehicles/api/vehicles/$VEHICLE_ID \
      -H "Authorization: Bearer $TOKEN")
    
    LICENSE=$(echo $VEHICLE_READ | jq -r '.licensePlate' 2>/dev/null)
    if [ "$LICENSE" = "А999ТЕ777" ]; then
        print_success "Vehicle: READ - транспортное средство прочитано"
    else
        print_failure "Vehicle: READ - данные некорректны"
    fi
fi

# ============================================
# ЭТАП 8: Тестирование Reservation Service
# ============================================
echo ""
print_test "Этап 8: Тестирование Reservation Service + Feign интеграция"

# CREATE Reservation (тестирует Feign вызовы)
if [ ! -z "$SPOT_ID" ] && [ ! -z "$VEHICLE_ID" ]; then
    RES_CREATED=$(curl -s -X POST http://localhost:8090/reservations/api/reservations \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d "{
        \"parkingSpotId\": $SPOT_ID,
        \"vehicleId\": $VEHICLE_ID,
        \"startTime\": \"2024-12-01T10:00:00\",
        \"endTime\": \"2024-12-01T18:00:00\"
      }")
    
    RES_ID=$(echo $RES_CREATED | jq -r '.id' 2>/dev/null)
    if [ "$RES_ID" != "null" ] && [ ! -z "$RES_ID" ]; then
        print_success "Reservation: CREATE - бронирование создано (ID: $RES_ID)"
    else
        print_failure "Reservation: CREATE - не удалось создать бронирование"
        echo "Response: $RES_CREATED"
    fi
    
    # GET Details (проверяет Feign вызовы к Parking и Vehicle сервисам)
    if [ ! -z "$RES_ID" ]; then
        RES_DETAILS=$(curl -s -X GET http://localhost:8090/reservations/api/reservations/$RES_ID/details \
          -H "Authorization: Bearer $TOKEN")
        
        HAS_PARKING=$(echo $RES_DETAILS | jq -r '.parkingSpot' 2>/dev/null)
        HAS_VEHICLE=$(echo $RES_DETAILS | jq -r '.vehicle' 2>/dev/null)
        
        if [ "$HAS_PARKING" != "null" ] && [ "$HAS_VEHICLE" != "null" ]; then
            print_success "Reservation: DETAILS - Feign интеграция работает (получены данные из Parking и Vehicle сервисов)"
        else
            print_failure "Reservation: DETAILS - Feign интеграция не работает"
            echo "Details: $RES_DETAILS"
        fi
    fi
else
    print_failure "Reservation: Пропущено (нет Parking Spot или Vehicle)"
fi

# ============================================
# ЭТАП 9: Проверка Config Server
# ============================================
echo ""
print_test "Этап 9: Проверка Config Server"

CONFIG=$(curl -s http://localhost:8888/parking-service/default)
if echo $CONFIG | jq -e '.propertySources[0].source' > /dev/null 2>&1; then
    print_success "Config Server отдает конфигурации"
else
    print_failure "Config Server не отдает конфигурации"
fi

# ============================================
# ИТОГОВЫЙ ОТЧЕТ
# ============================================
echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}                    📊 ИТОГОВЫЙ ОТЧЕТ                    ${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${GREEN}✅ Пройдено тестов: $PASSED${NC}"
echo -e "${RED}❌ Провалено тестов: $FAILED${NC}"
echo ""

TOTAL=$((PASSED + FAILED))
PERCENTAGE=$((PASSED * 100 / TOTAL))

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║   🎉 ВСЕ ТЕСТЫ ПРОЙДЕНЫ! СИСТЕМА РАБОТАЕТ КОРРЕКТНО!   🎉  ║${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════════════════════════╝${NC}"
    exit 0
elif [ $PERCENTAGE -ge 80 ]; then
    echo -e "${YELLOW}⚠️  Большинство тестов пройдено ($PERCENTAGE%), но есть проблемы${NC}"
    exit 1
else
    echo -e "${RED}❌ Много провалов ($PERCENTAGE% успеха). Требуется отладка!${NC}"
    exit 1
fi

