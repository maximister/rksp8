# Примеры API запросов

## Последовательность тестирования системы

### Шаг 1: Создать парковочные места

```bash
# Место 1
curl -X POST http://localhost:8080/api/parking/spots \
  -H "Content-Type: application/json" \
  -d '{
    "number": "A-101",
    "floor": 1,
    "status": "FREE"
  }'

# Место 2
curl -X POST http://localhost:8080/api/parking/spots \
  -H "Content-Type: application/json" \
  -d '{
    "number": "A-102",
    "floor": 1,
    "status": "FREE"
  }'

# Место 3
curl -X POST http://localhost:8080/api/parking/spots \
  -H "Content-Type: application/json" \
  -d '{
    "number": "B-201",
    "floor": 2,
    "status": "FREE"
  }'
```

### Шаг 2: Создать автомобили

```bash
# Автомобиль 1
curl -X POST http://localhost:8080/api/vehicles/vehicles \
  -H "Content-Type: application/json" \
  -d '{
    "plateNumber": "А123БВ",
    "model": "Toyota Camry",
    "color": "Черный",
    "ownerId": 1
  }'

# Автомобиль 2
curl -X POST http://localhost:8080/api/vehicles/vehicles \
  -H "Content-Type: application/json" \
  -d '{
    "plateNumber": "В456ГД",
    "model": "BMW X5",
    "color": "Белый",
    "ownerId": 2
  }'

# Автомобиль 3
curl -X POST http://localhost:8080/api/vehicles/vehicles \
  -H "Content-Type: application/json" \
  -d '{
    "plateNumber": "С789ЕЖ",
    "model": "Mercedes C-Class",
    "color": "Серебристый",
    "ownerId": 1
  }'
```

### Шаг 3: Создать бронирования

```bash
# Бронирование 1
curl -X POST http://localhost:8080/api/reservations/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "parkingSpotId": 1,
    "vehicleId": 1,
    "startTime": "2024-01-20T09:00:00",
    "endTime": "2024-01-20T18:00:00"
  }'

# Бронирование 2
curl -X POST http://localhost:8080/api/reservations/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "parkingSpotId": 2,
    "vehicleId": 2,
    "startTime": "2024-01-20T10:00:00",
    "endTime": "2024-01-20T19:00:00"
  }'
```

### Шаг 4: Получить данные

```bash
# Все парковочные места
curl http://localhost:8080/api/parking/spots

# Свободные места
curl http://localhost:8080/api/parking/spots/status/FREE

# Зарезервированные места
curl http://localhost:8080/api/parking/spots/status/RESERVED

# Все автомобили
curl http://localhost:8080/api/vehicles/vehicles

# Автомобили владельца 1
curl http://localhost:8080/api/vehicles/vehicles/owner/1

# Все бронирования
curl http://localhost:8080/api/reservations/reservations

# Детальная информация о бронировании (с данными о месте и авто)
curl http://localhost:8080/api/reservations/reservations/1/details

# Активные бронирования
curl http://localhost:8080/api/reservations/reservations/status/ACTIVE
```

### Шаг 5: Управление бронированиями

```bash
# Завершить бронирование (место освобождается)
curl -X PATCH http://localhost:8080/api/reservations/reservations/1/complete

# Отменить бронирование (место освобождается)
curl -X PATCH http://localhost:8080/api/reservations/reservations/2/cancel

# Проверить, что места стали FREE
curl http://localhost:8080/api/parking/spots/status/FREE
```

## Проверка межсервисного взаимодействия

### Endpoint с Feign Client

Этот endpoint демонстрирует взаимодействие между сервисами:

```bash
# Получить полную информацию о бронировании
# Reservation Service обращается к Parking Service и Vehicle Service через Feign
curl http://localhost:8080/api/reservations/reservations/1/details
```

Ответ будет содержать данные из всех трех сервисов:
```json
{
  "reservation": {
    "id": 1,
    "parkingSpotId": 1,
    "vehicleId": 1,
    "startTime": "2024-01-20T09:00:00",
    "endTime": "2024-01-20T18:00:00",
    "status": "ACTIVE"
  },
  "parkingSpot": {
    "id": 1,
    "number": "A-101",
    "floor": 1,
    "status": "RESERVED"
  },
  "vehicle": {
    "id": 1,
    "plateNumber": "А123БВ",
    "model": "Toyota Camry",
    "color": "Черный",
    "ownerId": 1
  }
}
```

## Дополнительные операции

### Обновление данных

```bash
# Обновить парковочное место
curl -X PUT http://localhost:8080/api/parking/spots/1 \
  -H "Content-Type: application/json" \
  -d '{
    "number": "A-101-VIP",
    "floor": 1,
    "status": "FREE"
  }'

# Обновить автомобиль
curl -X PUT http://localhost:8080/api/vehicles/vehicles/1 \
  -H "Content-Type: application/json" \
  -d '{
    "plateNumber": "А123БВ",
    "model": "Toyota Camry 2024",
    "color": "Черный",
    "ownerId": 1
  }'

# Изменить только статус парковочного места
curl -X PATCH "http://localhost:8080/api/parking/spots/1/status?status=OCCUPIED"
```

### Поиск

```bash
# Найти автомобиль по номеру
curl http://localhost:8080/api/vehicles/vehicles/plate/А123БВ

# Места на конкретном этаже
curl http://localhost:8080/api/parking/spots/floor/1

# Бронирования конкретного автомобиля
curl http://localhost:8080/api/reservations/reservations/vehicle/1

# Бронирования конкретного места
curl http://localhost:8080/api/reservations/reservations/parking-spot/1
```

### Удаление

```bash
# Удалить бронирование
curl -X DELETE http://localhost:8080/api/reservations/reservations/1

# Удалить автомобиль
curl -X DELETE http://localhost:8080/api/vehicles/vehicles/1

# Удалить парковочное место
curl -X DELETE http://localhost:8080/api/parking/spots/1
```

## Проверка работы через браузер

1. **Eureka Dashboard**: http://localhost:8761
   - Проверьте, что все сервисы зарегистрированы

2. **H2 Console** (для проверки БД):
   - Parking: http://localhost:8081/h2-console (JDBC URL: `jdbc:h2:mem:parkingdb`)
   - Vehicle: http://localhost:8082/h2-console (JDBC URL: `jdbc:h2:mem:vehicledb`)
   - Reservation: http://localhost:8083/h2-console (JDBC URL: `jdbc:h2:mem:reservationdb`)

3. **Простые GET запросы** можно выполнять прямо в браузере:
   - http://localhost:8080/api/parking/spots
   - http://localhost:8080/api/vehicles/vehicles
   - http://localhost:8080/api/reservations/reservations




