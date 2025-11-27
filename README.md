# Практическая работа №8 - Система управления парковкой

Микросервисная архитектура для управления парковочными местами, автомобилями и бронированиями.

## 📋 Архитектура

Проект состоит из следующих сервисов:

### Инфраструктурные сервисы
- **Eureka Server** (порт 8761) - Service Discovery
- **Config Server** (порт 8888) - Централизованная конфигурация
- **Auth Server** (порт 9000) - OAuth2 Authorization Server
- **API Gateway** (порт 8080) - Точка входа в систему

### Бизнес-сервисы
1. **Parking Service** (порт 8081) - Управление парковочными местами
2. **Vehicle Service** (порт 8082) - Управление автомобилями
3. **Reservation Service** (порт 8083) - Управление бронированиями

## 🚀 Запуск проекта

### Предварительные требования
- Java 17+
- Maven 3.6+

### Автоматический запуск (macOS)

```bash
./start-with-config.sh
```

### Ручной запуск (правильный порядок!)

1. **Eureka Server** (порт 8761):
```bash
cd eureka-server
mvn spring-boot:run
```

2. **Config Server** (порт 8888):
```bash
cd config-server
mvn spring-boot:run
```

3. **Auth Server** (порт 9000):
```bash
cd auth-server
mvn spring-boot:run
```

4. **Остальные сервисы** (в любом порядке):
```bash
# В отдельных терминалах:
cd parking-service && mvn spring-boot:run
cd vehicle-service && mvn spring-boot:run
cd reservation-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

5. **Проверьте регистрацию сервисов**:
- Eureka Dashboard: http://localhost:8761
- Config Server Health: `curl http://localhost:8888/actuator/health`

## 📡 API Endpoints

### Через API Gateway (http://localhost:8080)

#### Parking Service
- `GET /api/parking/spots` - Получить все парковочные места
- `GET /api/parking/spots/{id}` - Получить место по ID
- `GET /api/parking/spots/status/{status}` - Места по статусу (FREE, OCCUPIED, RESERVED)
- `GET /api/parking/spots/floor/{floor}` - Места по этажу
- `POST /api/parking/spots` - Создать место
- `PUT /api/parking/spots/{id}` - Обновить место
- `PATCH /api/parking/spots/{id}/status?status={status}` - Изменить статус
- `DELETE /api/parking/spots/{id}` - Удалить место

#### Vehicle Service
- `GET /api/vehicles/vehicles` - Получить все автомобили
- `GET /api/vehicles/vehicles/{id}` - Получить автомобиль по ID
- `GET /api/vehicles/vehicles/plate/{plateNumber}` - Поиск по номеру
- `GET /api/vehicles/vehicles/owner/{ownerId}` - Автомобили владельца
- `POST /api/vehicles/vehicles` - Создать автомобиль
- `PUT /api/vehicles/vehicles/{id}` - Обновить автомобиль
- `DELETE /api/vehicles/vehicles/{id}` - Удалить автомобиль

#### Reservation Service
- `GET /api/reservations/reservations` - Получить все бронирования
- `GET /api/reservations/reservations/{id}` - Получить бронирование по ID
- `GET /api/reservations/reservations/{id}/details` - Полная информация (с данными о месте и авто)
- `GET /api/reservations/reservations/parking-spot/{parkingSpotId}` - Бронирования места
- `GET /api/reservations/reservations/vehicle/{vehicleId}` - Бронирования автомобиля
- `GET /api/reservations/reservations/status/{status}` - По статусу (ACTIVE, COMPLETED, CANCELLED)
- `POST /api/reservations/reservations` - Создать бронирование
- `PUT /api/reservations/reservations/{id}` - Обновить бронирование
- `PATCH /api/reservations/reservations/{id}/complete` - Завершить бронирование
- `PATCH /api/reservations/reservations/{id}/cancel` - Отменить бронирование
- `DELETE /api/reservations/reservations/{id}` - Удалить бронирование

## 🧪 Примеры использования

### 1. Создание парковочного места
```bash
curl -X POST http://localhost:8080/api/parking/spots \
  -H "Content-Type: application/json" \
  -d '{
    "number": "A-101",
    "floor": 1,
    "status": "FREE"
  }'
```

### 2. Создание автомобиля
```bash
curl -X POST http://localhost:8080/api/vehicles/vehicles \
  -H "Content-Type: application/json" \
  -d '{
    "plateNumber": "А123БВ",
    "model": "Toyota Camry",
    "color": "Черный",
    "ownerId": 1
  }'
```

### 3. Создание бронирования
```bash
curl -X POST http://localhost:8080/api/reservations/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "parkingSpotId": 1,
    "vehicleId": 1,
    "startTime": "2024-01-20T10:00:00",
    "endTime": "2024-01-20T18:00:00"
  }'
```

### 4. Получение полной информации о бронировании
```bash
curl http://localhost:8080/api/reservations/reservations/1/details
```

## 💾 База данных

Каждый сервис использует свою in-memory H2 базу данных.

H2 консоли доступны по адресам:
- Parking Service: http://localhost:8081/h2-console
- Vehicle Service: http://localhost:8082/h2-console
- Reservation Service: http://localhost:8083/h2-console

**Параметры подключения:**
- JDBC URL: `jdbc:h2:mem:{servicename}db` (например, `jdbc:h2:mem:parkingdb`)
- Username: `sa`
- Password: (оставить пустым)

## 🔧 Особенности реализации

### Межсервисное взаимодействие
- **Reservation Service** использует **Feign Client** для обращения к Parking Service и Vehicle Service
- При создании бронирования автоматически обновляется статус парковочного места на "RESERVED"
- При завершении/отмене бронирования статус возвращается в "FREE"

### Service Discovery
- Все сервисы регистрируются в Eureka
- API Gateway автоматически маршрутизирует запросы через Service Discovery

## 📊 Модель данных

### ParkingSpot (Парковочное место)
- id: Long
- number: String (номер места, например "A-101")
- floor: Integer (этаж)
- status: String (FREE, OCCUPIED, RESERVED)

### Vehicle (Автомобиль)
- id: Long
- plateNumber: String (гос. номер)
- model: String (модель)
- color: String (цвет)
- ownerId: Long (ID владельца)

### Reservation (Бронирование)
- id: Long
- parkingSpotId: Long
- vehicleId: Long
- startTime: LocalDateTime
- endTime: LocalDateTime
- status: String (ACTIVE, COMPLETED, CANCELLED)

## 🛠 Технологический стек

- Spring Boot 3.1.5
- Spring Cloud 2022.0.4
- **Spring Cloud Config** - Централизованная конфигурация
- **Spring Cloud Netflix Eureka** - Service Discovery
- **Spring Cloud Gateway** - API Gateway с балансировкой нагрузки
- **Spring Cloud OpenFeign** - Декларативные REST клиенты
- **Spring Security OAuth2** - Авторизация и аутентификация
- Spring Data JPA
- H2 Database
- Lombok
- Maven

## 📝 Заметки

- Проект использует Spring Boot 3.x, требуется Java 17+
- Все сервисы используют H2 с настройкой `ddl-auto: create-drop`, данные очищаются при перезапуске
- Конфигурация вынесена в **Spring Cloud Config Server** (папка `config-repo/`)
- Реализована **OAuth2 авторизация** с JWT токенами
- **Балансировка нагрузки** через Spring Cloud LoadBalancer (встроена в Gateway)

## 📚 Дополнительная документация

- [CONFIG_SERVER_SETUP.md](CONFIG_SERVER_SETUP.md) - Подробная инструкция по Config Server
- [GIT_SETUP.md](GIT_SETUP.md) - Настройка Git репозитория для конфигов
- [OAUTH2.md](OAUTH2.md) - Документация по OAuth2 авторизации
- [API_EXAMPLES.md](API_EXAMPLES.md) - Примеры API запросов
- [DEMO.md](DEMO.md) - Сценарии демонстрации




