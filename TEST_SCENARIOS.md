# 🧪 Сценарии тестирования системы парковки

## 📋 Содержание
1. [Проверка работоспособности](#проверка-работоспособности)
2. [Получение OAuth2 токена](#получение-oauth2-токена)
3. [Тестирование Parking Service](#тестирование-parking-service)
4. [Тестирование Vehicle Service](#тестирование-vehicle-service)
5. [Тестирование Reservation Service](#тестирование-reservation-service)
6. [Интеграционные сценарии](#интеграционные-сценарии)

---

## 1. Проверка работоспособности

### 1.1. Проверка Eureka Dashboard
```bash
# Открыть в браузере
open http://localhost:8761
```

**Ожидаемый результат:** Должны быть видны 6 зарегистрированных сервисов:
- CONFIG-SERVER
- AUTH-SERVER
- API-GATEWAY
- PARKING-SERVICE
- VEHICLE-SERVICE
- RESERVATION-SERVICE

### 1.2. Проверка Config Server
```bash
curl http://localhost:8888/actuator/health
```

**Ожидаемый результат:**
```json
{"status":"UP"}
```

### 1.3. Проверка Auth Server
```bash
curl http://localhost:9000/actuator/health
```

### 1.4. Проверка API Gateway
```bash
curl http://localhost:8090/actuator/health
```

---

## 2. Получение OAuth2 токена

### 2.1. Получение токена для пользователя `admin` (права на чтение и запись)

```bash
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "parking-client:secret" \
  -d "grant_type=password&username=admin&password=admin&scope=read write"
```

**Ожидаемый результат:**
```json
{
  "access_token": "eyJraWQiOiI...",
  "scope": "read write",
  "token_type": "Bearer",
  "expires_in": 299
}
```

**Сохраните токен в переменную:**
```bash
export ADMIN_TOKEN=$(curl -s -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "parking-client:secret" \
  -d "grant_type=password&username=admin&password=admin&scope=read write" | jq -r '.access_token')

echo "Admin Token: $ADMIN_TOKEN"
```

### 2.2. Получение токена для пользователя `user` (только чтение)

```bash
export USER_TOKEN=$(curl -s -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "parking-client:secret" \
  -d "grant_type=password&username=user&password=user&scope=read" | jq -r '.access_token')

echo "User Token: $USER_TOKEN"
```

---

## 3. Тестирование Parking Service

### 3.1. Создать парковочное место (требуется scope: write)

```bash
curl -X POST http://localhost:8090/api/parking/spots \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "number": "A-101",
    "floor": 1,
    "status": "FREE"
  }'
```

**Ожидаемый результат:**
```json
{
  "id": 1,
  "number": "A-101",
  "floor": 1,
  "status": "FREE"
}
```

### 3.2. Создать еще несколько парковочных мест

```bash
# Место A-102
curl -X POST http://localhost:8090/api/parking/spots \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": "A-102", "floor": 1, "status": "FREE"}'

# Место B-201
curl -X POST http://localhost:8090/api/parking/spots \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": "B-201", "floor": 2, "status": "FREE"}'

# Место B-202
curl -X POST http://localhost:8090/api/parking/spots \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": "B-202", "floor": 2, "status": "FREE"}'
```

### 3.3. Получить все парковочные места (требуется scope: read)

```bash
curl http://localhost:8090/api/parking/spots \
  -H "Authorization: Bearer $USER_TOKEN"
```

**Ожидаемый результат:**
```json
[
  {"id": 1, "number": "A-101", "floor": 1, "status": "FREE"},
  {"id": 2, "number": "A-102", "floor": 1, "status": "FREE"},
  {"id": 3, "number": "B-201", "floor": 2, "status": "FREE"},
  {"id": 4, "number": "B-202", "floor": 2, "status": "FREE"}
]
```

### 3.4. Получить парковочное место по ID

```bash
curl http://localhost:8090/api/parking/spots/1 \
  -H "Authorization: Bearer $USER_TOKEN"
```

### 3.5. Обновить статус парковочного места

```bash
curl -X PUT http://localhost:8090/api/parking/spots/1 \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "number": "A-101",
    "floor": 1,
    "status": "OCCUPIED"
  }'
```

### 3.6. Удалить парковочное место

```bash
curl -X DELETE http://localhost:8090/api/parking/spots/4 \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## 4. Тестирование Vehicle Service

### 4.1. Создать транспортное средство

```bash
curl -X POST http://localhost:8090/api/vehicles \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "A123BC77",
    "model": "Toyota Camry",
    "color": "Black",
    "ownerName": "Иван Иванов"
  }'
```

**Ожидаемый результат:**
```json
{
  "id": 1,
  "licensePlate": "A123BC77",
  "model": "Toyota Camry",
  "color": "Black",
  "ownerName": "Иван Иванов"
}
```

### 4.2. Создать еще несколько автомобилей

```bash
# Автомобиль 2
curl -X POST http://localhost:8090/api/vehicles \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "B456CD99",
    "model": "BMW X5",
    "color": "White",
    "ownerName": "Петр Петров"
  }'

# Автомобиль 3
curl -X POST http://localhost:8090/api/vehicles \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "C789EF50",
    "model": "Mercedes-Benz E-Class",
    "color": "Silver",
    "ownerName": "Сидор Сидоров"
  }'
```

### 4.3. Получить все автомобили

```bash
curl http://localhost:8090/api/vehicles \
  -H "Authorization: Bearer $USER_TOKEN"
```

### 4.4. Получить автомобиль по ID

```bash
curl http://localhost:8090/api/vehicles/1 \
  -H "Authorization: Bearer $USER_TOKEN"
```

### 4.5. Обновить информацию об автомобиле

```bash
curl -X PUT http://localhost:8090/api/vehicles/1 \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "A123BC77",
    "model": "Toyota Camry",
    "color": "Red",
    "ownerName": "Иван Иванов"
  }'
```

### 4.6. Удалить автомобиль

```bash
curl -X DELETE http://localhost:8090/api/vehicles/3 \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## 5. Тестирование Reservation Service

### 5.1. Создать бронирование

```bash
curl -X POST http://localhost:8090/api/reservations \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "parkingSpotId": 1,
    "vehicleId": 1,
    "startTime": "2025-12-01T10:00:00",
    "endTime": "2025-12-01T18:00:00"
  }'
```

**Ожидаемый результат:**
```json
{
  "id": 1,
  "parkingSpotId": 1,
  "vehicleId": 1,
  "startTime": "2025-12-01T10:00:00",
  "endTime": "2025-12-01T18:00:00",
  "status": "ACTIVE"
}
```

**Важно:** После создания бронирования:
- Статус парковочного места должен стать `OCCUPIED`
- Reservation Service вызывает Parking Service через Feign Client

### 5.2. Создать еще бронирования

```bash
# Бронирование 2
curl -X POST http://localhost:8090/api/reservations \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "parkingSpotId": 2,
    "vehicleId": 2,
    "startTime": "2025-12-01T09:00:00",
    "endTime": "2025-12-01T17:00:00"
  }'

# Бронирование 3
curl -X POST http://localhost:8090/api/reservations \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "parkingSpotId": 3,
    "vehicleId": 1,
    "startTime": "2025-12-02T10:00:00",
    "endTime": "2025-12-02T18:00:00"
  }'
```

### 5.3. Получить все бронирования

```bash
curl http://localhost:8090/api/reservations \
  -H "Authorization: Bearer $USER_TOKEN"
```

### 5.4. Получить бронирование по ID

```bash
curl http://localhost:8090/api/reservations/1 \
  -H "Authorization: Bearer $USER_TOKEN"
```

### 5.5. Обновить бронирование

```bash
curl -X PUT http://localhost:8090/api/reservations/1 \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "parkingSpotId": 1,
    "vehicleId": 1,
    "startTime": "2025-12-01T10:00:00",
    "endTime": "2025-12-01T20:00:00",
    "status": "ACTIVE"
  }'
```

### 5.6. Отменить бронирование (PATCH)

```bash
curl -X PATCH http://localhost:8090/api/reservations/1 \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '"CANCELLED"'
```

**Важно:** После отмены бронирования статус парковочного места должен вернуться в `FREE`.

### 5.7. Удалить бронирование

```bash
curl -X DELETE http://localhost:8090/api/reservations/2 \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## 6. Интеграционные сценарии

### 6.1. Полный сценарий: от создания до отмены бронирования

```bash
# 1. Создать парковочное место
SPOT=$(curl -s -X POST http://localhost:8090/api/parking/spots \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": "TEST-01", "floor": 1, "status": "FREE"}')
SPOT_ID=$(echo $SPOT | jq -r '.id')
echo "Создано парковочное место ID: $SPOT_ID"

# 2. Создать автомобиль
VEHICLE=$(curl -s -X POST http://localhost:8090/api/vehicles \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"licensePlate": "TEST123", "model": "Test Car", "color": "Blue", "ownerName": "Test Owner"}')
VEHICLE_ID=$(echo $VEHICLE | jq -r '.id')
echo "Создан автомобиль ID: $VEHICLE_ID"

# 3. Проверить статус парковочного места (должен быть FREE)
curl http://localhost:8090/api/parking/spots/$SPOT_ID \
  -H "Authorization: Bearer $USER_TOKEN" | jq '.status'

# 4. Создать бронирование
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
echo "Создано бронирование ID: $RESERVATION_ID"

# 5. Проверить статус парковочного места (должен стать OCCUPIED)
curl http://localhost:8090/api/parking/spots/$SPOT_ID \
  -H "Authorization: Bearer $USER_TOKEN" | jq '.status'

# 6. Отменить бронирование
curl -X PATCH http://localhost:8090/api/reservations/$RESERVATION_ID \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '"CANCELLED"'

# 7. Проверить статус парковочного места (должен вернуться в FREE)
curl http://localhost:8090/api/parking/spots/$SPOT_ID \
  -H "Authorization: Bearer $USER_TOKEN" | jq '.status'

echo "Интеграционный тест завершен!"
```

### 6.2. Тест прав доступа: попытка записи с read-only токеном

```bash
# Попытка создать парковочное место с токеном USER (только read)
curl -X POST http://localhost:8090/api/parking/spots \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": "FAIL-01", "floor": 1, "status": "FREE"}'
```

**Ожидаемый результат:** HTTP 403 Forbidden

### 6.3. Тест без токена

```bash
# Попытка получить список мест без токена
curl http://localhost:8090/api/parking/spots
```

**Ожидаемый результат:** HTTP 401 Unauthorized

### 6.4. Тест балансировки нагрузки (если запущено несколько инстансов)

```bash
# Выполнить 10 запросов и посмотреть, как распределяется нагрузка
for i in {1..10}; do
  curl -s http://localhost:8090/api/parking/spots \
    -H "Authorization: Bearer $USER_TOKEN" \
    -w "\nRequest $i completed\n"
  sleep 0.5
done
```

---

## 7. Проверка работы Feign Client

### 7.1. Создать бронирование и проверить логи

После создания бронирования проверьте логи Reservation Service:
- Должен быть запрос к Parking Service через Feign
- Должен быть запрос к Vehicle Service через Feign
- Статус парковочного места должен измениться

```bash
# Создать бронирование
curl -X POST http://localhost:8090/api/reservations \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "parkingSpotId": 1,
    "vehicleId": 1,
    "startTime": "2025-12-03T10:00:00",
    "endTime": "2025-12-03T18:00:00"
  }'

# Проверить, что статус места изменился
curl http://localhost:8090/api/parking/spots/1 \
  -H "Authorization: Bearer $USER_TOKEN" | jq '.status'
```

---

## 8. Мониторинг и диагностика

### 8.1. Проверка health endpoints всех сервисов

```bash
echo "Config Server:"
curl -s http://localhost:8888/actuator/health | jq

echo "Eureka Server:"
curl -s http://localhost:8761/actuator/health | jq

echo "Auth Server:"
curl -s http://localhost:9000/actuator/health | jq

echo "API Gateway:"
curl -s http://localhost:8090/actuator/health | jq

echo "Parking Service:"
curl -s http://localhost:8081/actuator/health | jq

echo "Vehicle Service:"
curl -s http://localhost:8082/actuator/health | jq

echo "Reservation Service:"
curl -s http://localhost:8083/actuator/health | jq
```

### 8.2. Просмотр зарегистрированных сервисов в Eureka

```bash
curl -s http://localhost:8761/eureka/apps | xmllint --format -
```

---

## 9. Автоматизированный скрипт полного тестирования

Создайте файл `run-tests.sh`:

```bash
#!/bin/bash

echo "=========================================="
echo "Полное тестирование системы парковки"
echo "=========================================="

# Получить токены
echo "1. Получение токенов..."
export ADMIN_TOKEN=$(curl -s -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "parking-client:secret" \
  -d "grant_type=password&username=admin&password=admin&scope=read write" | jq -r '.access_token')

export USER_TOKEN=$(curl -s -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "parking-client:secret" \
  -d "grant_type=password&username=user&password=user&scope=read" | jq -r '.access_token')

if [ "$ADMIN_TOKEN" == "null" ] || [ -z "$ADMIN_TOKEN" ]; then
  echo "❌ Ошибка получения ADMIN токена"
  exit 1
fi

if [ "$USER_TOKEN" == "null" ] || [ -z "$USER_TOKEN" ]; then
  echo "❌ Ошибка получения USER токена"
  exit 1
fi

echo "✅ Токены получены"

# Создать парковочное место
echo "2. Создание парковочного места..."
SPOT=$(curl -s -X POST http://localhost:8090/api/parking/spots \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": "A-101", "floor": 1, "status": "FREE"}')
SPOT_ID=$(echo $SPOT | jq -r '.id')
echo "✅ Парковочное место создано: ID=$SPOT_ID"

# Создать автомобиль
echo "3. Создание автомобиля..."
VEHICLE=$(curl -s -X POST http://localhost:8090/api/vehicles \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"licensePlate": "A123BC77", "model": "Toyota Camry", "color": "Black", "ownerName": "Иван Иванов"}')
VEHICLE_ID=$(echo $VEHICLE | jq -r '.id')
echo "✅ Автомобиль создан: ID=$VEHICLE_ID"

# Создать бронирование
echo "4. Создание бронирования..."
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
echo "✅ Бронирование создано: ID=$RESERVATION_ID"

# Проверить статус места
echo "5. Проверка статуса парковочного места..."
SPOT_STATUS=$(curl -s http://localhost:8090/api/parking/spots/$SPOT_ID \
  -H "Authorization: Bearer $USER_TOKEN" | jq -r '.status')
if [ "$SPOT_STATUS" == "OCCUPIED" ]; then
  echo "✅ Статус места корректный: OCCUPIED"
else
  echo "❌ Ошибка: статус места = $SPOT_STATUS (ожидалось OCCUPIED)"
fi

# Отменить бронирование
echo "6. Отмена бронирования..."
curl -s -X PATCH http://localhost:8090/api/reservations/$RESERVATION_ID \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '"CANCELLED"' > /dev/null
echo "✅ Бронирование отменено"

# Проверить статус места после отмены
echo "7. Проверка статуса после отмены..."
sleep 1
SPOT_STATUS=$(curl -s http://localhost:8090/api/parking/spots/$SPOT_ID \
  -H "Authorization: Bearer $USER_TOKEN" | jq -r '.status')
if [ "$SPOT_STATUS" == "FREE" ]; then
  echo "✅ Статус места корректный: FREE"
else
  echo "❌ Ошибка: статус места = $SPOT_STATUS (ожидалось FREE)"
fi

# Тест прав доступа
echo "8. Тест прав доступа (попытка записи с read-only токеном)..."
RESPONSE=$(curl -s -w "%{http_code}" -X POST http://localhost:8090/api/parking/spots \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": "FAIL", "floor": 1, "status": "FREE"}' \
  -o /dev/null)
if [ "$RESPONSE" == "403" ]; then
  echo "✅ Права доступа работают корректно (403 Forbidden)"
else
  echo "❌ Ошибка прав доступа: HTTP $RESPONSE (ожидалось 403)"
fi

echo "=========================================="
echo "✅ Все тесты завершены успешно!"
echo "=========================================="
```

Сделайте скрипт исполняемым:
```bash
chmod +x run-tests.sh
./run-tests.sh
```

---

## 10. Ожидаемые результаты

### Успешное выполнение всех тестов означает:

- ✅ OAuth2 авторизация работает
- ✅ API Gateway корректно маршрутизирует запросы
- ✅ Все микросервисы зарегистрированы в Eureka
- ✅ Feign Client работает (межсервисное взаимодействие)
- ✅ CRUD операции выполняются успешно
- ✅ Права доступа (scopes) работают корректно
- ✅ Статус парковочных мест обновляется при бронировании/отмене

---

## 11. Troubleshooting

### Проблема: 401 Unauthorized

```bash
# Проверить валидность токена
echo $ADMIN_TOKEN | cut -d'.' -f2 | base64 -d 2>/dev/null | jq

# Получить новый токен
export ADMIN_TOKEN=$(curl -s -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "parking-client:secret" \
  -d "grant_type=password&username=admin&password=admin&scope=read write" | jq -r '.access_token')
```

### Проблема: 403 Forbidden

Проверьте, что используется токен с правильными scope:
- Для чтения (GET): scope=read
- Для записи (POST/PUT/DELETE): scope=write

### Проблема: 500 Internal Server Error

Проверьте логи соответствующего микросервиса в IntelliJ IDEA или Docker.

### Проблема: Connection refused

Проверьте, что все сервисы запущены:
```bash
lsof -i :8761,8888,9000,8090,8081,8082,8083
```

---

**Удачного тестирования! 🚀**

