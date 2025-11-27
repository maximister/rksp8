# Демонстрация работы системы управления парковкой

## Быстрый старт

### 1. Сборка проекта
```bash
./build-all.sh
```

### 2. Запуск сервисов

**Вариант A: Автоматический запуск (macOS)**
```bash
./start-all.sh
```

**Вариант B: Ручной запуск**

В отдельных терминалах запустите сервисы в следующем порядке:

```bash
# Терминал 1: Eureka Server
cd eureka-server
mvn spring-boot:run

# Подождите ~30 секунд, затем в других терминалах:

# Терминал 2: Parking Service
cd parking-service
mvn spring-boot:run

# Терминал 3: Vehicle Service
cd vehicle-service
mvn spring-boot:run

# Терминал 4: Reservation Service
cd reservation-service
mvn spring-boot:run

# Терминал 5: API Gateway
cd api-gateway
mvn spring-boot:run
```

### 3. Проверка запуска

Откройте в браузере: http://localhost:8761

Вы должны увидеть все сервисы в Eureka Dashboard:
- API-GATEWAY
- PARKING-SERVICE
- VEHICLE-SERVICE
- RESERVATION-SERVICE

## Сценарий демонстрации

### Сценарий 1: Базовая работа с данными

```bash
# 1. Создаём парковочное место
curl -X POST http://localhost:8080/api/parking/spots \
  -H "Content-Type: application/json" \
  -d '{
    "number": "A-101",
    "floor": 1,
    "status": "FREE"
  }'

# 2. Создаём автомобиль
curl -X POST http://localhost:8080/api/vehicles/vehicles \
  -H "Content-Type: application/json" \
  -d '{
    "plateNumber": "А123БВ",
    "model": "Toyota Camry",
    "color": "Черный",
    "ownerId": 1
  }'

# 3. Создаём бронирование (демонстрирует Feign - межсервисное взаимодействие)
curl -X POST http://localhost:8080/api/reservations/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "parkingSpotId": 1,
    "vehicleId": 1,
    "startTime": "2024-01-20T09:00:00",
    "endTime": "2024-01-20T18:00:00"
  }'

# 4. Проверяем, что статус места изменился на RESERVED
curl http://localhost:8080/api/parking/spots/1
```

### Сценарий 2: Демонстрация межсервисного взаимодействия (Feign Client)

```bash
# Получаем полную информацию о бронировании
# Reservation Service обращается к Parking Service и Vehicle Service
curl http://localhost:8080/api/reservations/reservations/1/details
```

**Что происходит:**
1. Запрос приходит на API Gateway
2. Gateway перенаправляет на Reservation Service
3. Reservation Service через Feign Client запрашивает данные:
   - У Parking Service - информацию о парковочном месте
   - У Vehicle Service - информацию об автомобиле
4. Все данные объединяются и возвращаются клиенту

### Сценарий 3: Завершение бронирования

```bash
# Завершаем бронирование
curl -X PATCH http://localhost:8080/api/reservations/reservations/1/complete

# Проверяем, что место снова FREE
curl http://localhost:8080/api/parking/spots/1
```

**Что происходит:**
1. Reservation Service через Feign Client обращается к Parking Service
2. Обновляет статус места с RESERVED на FREE
3. Обновляет статус бронирования на COMPLETED

## Демонстрация ключевых особенностей микросервисной архитектуры

### 1. Service Discovery (Eureka)
- Откройте http://localhost:8761
- Покажите зарегистрированные сервисы
- Остановите один сервис - он исчезнет из списка через 30 секунд

### 2. API Gateway
- Все запросы идут через единую точку входа (порт 8080)
- Gateway автоматически маршрутизирует запросы к нужным сервисам
- Использует Service Discovery для поиска сервисов

### 3. Межсервисное взаимодействие (Feign Client)
- Reservation Service вызывает Parking Service и Vehicle Service
- Синхронные REST вызовы через декларативные интерфейсы
- Демонстрируется в endpoint `/reservations/{id}/details`

### 4. Независимые базы данных
- Каждый сервис имеет свою БД (database per service pattern)
- H2 консоли:
  - http://localhost:8081/h2-console (Parking)
  - http://localhost:8082/h2-console (Vehicle)
  - http://localhost:8083/h2-console (Reservation)

### 5. Независимое масштабирование
- Каждый сервис может быть запущен в нескольких экземплярах
- Можно запустить второй экземпляр на другом порту:
  ```bash
  cd parking-service
  mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8091
  ```

## Полезные команды для демонстрации

```bash
# Получить все свободные места
curl http://localhost:8080/api/parking/spots/status/FREE

# Получить все активные бронирования
curl http://localhost:8080/api/reservations/reservations/status/ACTIVE

# Получить автомобили владельца
curl http://localhost:8080/api/vehicles/vehicles/owner/1

# Получить все места на 1 этаже
curl http://localhost:8080/api/parking/spots/floor/1
```

## Проблемы и решения

### Сервисы не регистрируются в Eureka
- Подождите 30-60 секунд
- Проверьте логи сервисов
- Убедитесь, что Eureka Server запущен первым

### Feign Client не может подключиться
- Убедитесь, что целевой сервис зарегистрирован в Eureka
- Проверьте имена сервисов в `@FeignClient(name = "...")`

### Порт уже занят
- Измените порт в application.yml нужного сервиса
- Или остановите процесс: `lsof -ti:8080 | xargs kill`

## Архитектурная диаграмма

```
                    ┌─────────────────┐
                    │  Eureka Server  │
                    │   (port 8761)   │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │  API Gateway    │
                    │   (port 8080)   │
                    └────────┬────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
    ┌─────▼─────┐     ┌─────▼─────┐     ┌─────▼─────┐
    │ Parking   │     │ Vehicle   │     │Reservation│
    │ Service   │◄────┤ Service   │◄────┤  Service  │
    │ (8081)    │     │ (8082)    │     │  (8083)   │
    └───────────┘     └───────────┘     └───────────┘
         │                  │                  │
    ┌────▼────┐        ┌────▼────┐        ┌────▼────┐
    │ H2 DB   │        │ H2 DB   │        │ H2 DB   │
    └─────────┘        └─────────┘        └─────────┘
```

## Что демонстрирует проект

✅ **Микросервисная архитектура** - разделение на независимые сервисы
✅ **Service Discovery** - автоматическая регистрация и обнаружение сервисов
✅ **API Gateway** - единая точка входа
✅ **Feign Client** - декларативные REST-клиенты для межсервисного взаимодействия
✅ **Database per Service** - каждый сервис имеет свою БД
✅ **REST API** - стандартный подход к построению API
✅ **Spring Boot** - современный фреймворк для микросервисов
✅ **Spring Cloud** - инструменты для построения распределенных систем




