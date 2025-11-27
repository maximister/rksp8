# ✅ Чеклист для практической работы №8

## Требования задания и статус выполнения

### ✅ 1. OAuth2 авторизация
- [x] Реализован Auth Server (порт 9000)
- [x] Используется протокол OAuth2 с JWT токенами
- [x] Все Resource Servers проверяют токены
- [x] API Gateway выполняет TokenRelay
- [x] Настроены клиенты и scopes (read, write)

**Файлы:**
- `auth-server/` - Authorization Server
- `*/config/SecurityConfig.java` - Настройки безопасности

### ✅ 2. Spring Cloud Config (Git)
- [x] Создан Config Server (порт 8888)
- [x] Конфигурации вынесены в папку `config-repo/`
- [x] Поддерживает режим Native (локальные файлы)
- [x] Поддерживает режим Git (удаленный репозиторий)
- [x] Все сервисы подключены к Config Server

**Файлы:**
- `config-server/` - Config Server модуль
- `config-repo/` - Централизованные конфигурации
- `CONFIG_SERVER_SETUP.md` - Инструкция
- `GIT_SETUP.md` - Настройка Git

### ✅ 3. API Gateway
- [x] Реализован на Spring Cloud Gateway
- [x] Единая точка входа (порт 8080)
- [x] Маршрутизация к микросервисам
- [x] Интеграция с Eureka (Service Discovery)
- [x] OAuth2 Resource Server
- [x] TokenRelay для передачи токенов

**Файлы:**
- `api-gateway/` - API Gateway модуль
- `config-repo/api-gateway.yml` - Конфигурация роутов

### ✅ 4. Service Discovery (Eureka)
- [x] Eureka Server запущен (порт 8761)
- [x] Все сервисы регистрируются в Eureka
- [x] Dashboard доступен: http://localhost:8761

**Файлы:**
- `eureka-server/` - Eureka Server модуль

### ✅ 5. Балансировка нагрузки
- [x] Spring Cloud LoadBalancer встроен в Gateway
- [x] Используется `lb://service-name` в роутах
- [x] Поддержка нескольких экземпляров сервисов
- [x] Алгоритм Round Robin по умолчанию

**Демонстрация:**
```bash
# Запустить несколько экземпляров одного сервиса:
cd parking-service
mvn spring-boot:run  # Первый на порту 8081

# В другом терминале:
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8091
```

### ✅ 6. Минимум 3 микросервиса
- [x] **Parking Service** - управление парковочными местами
- [x] **Vehicle Service** - управление автомобилями  
- [x] **Reservation Service** - управление бронированиями

**Файлы:**
- `parking-service/`
- `vehicle-service/`
- `reservation-service/`

### ✅ 7. FeignClient (межсервисное взаимодействие)
- [x] Reservation Service использует FeignClient
- [x] Обращается к Parking Service
- [x] Обращается к Vehicle Service
- [x] Передает OAuth2 токены через RequestInterceptor

**Файлы:**
- `reservation-service/client/ParkingServiceClient.java`
- `reservation-service/client/VehicleServiceClient.java`
- `reservation-service/config/FeignConfig.java`

**Демонстрация:**
```bash
# Endpoint с межсервисным взаимодействием:
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/reservations/reservations/1/details
```

### ✅ 8. Отдельные базы данных
- [x] Parking Service → `jdbc:h2:mem:parkingdb`
- [x] Vehicle Service → `jdbc:h2:mem:vehicledb`
- [x] Reservation Service → `jdbc:h2:mem:reservationdb`
- [x] Auth Server → `jdbc:h2:mem:authdb`

**H2 Консоли:**
- http://localhost:8081/h2-console
- http://localhost:8082/h2-console
- http://localhost:8083/h2-console
- http://localhost:9000/h2-console

### ❌ 9. Unit-тесты
- [ ] Тесты для контроллеров (MockMvc)
- [ ] Тесты для сервисов
- [ ] Тесты для репозиториев
- [ ] Минимум для каждого микросервиса

**TODO:** Создать тесты!

### ❌ 10. Файлы для развертывания
- [ ] Dockerfile для каждого сервиса
- [ ] docker-compose.yml для всей системы
- [ ] ИЛИ Kubernetes манифесты

**TODO:** Создать Docker файлы!

### ❌ 11. Развертывание в облаке
- [ ] Yandex Cloud или VK Cloud
- [ ] ИЛИ minicube локально (если облако недоступно)

**TODO:** Развернуть в облаке!

## 📊 Прогресс: 8/11 (73%)

## 🚀 Что уже работает

### Запуск системы:
```bash
./start-with-config.sh  # Автоматический запуск всех сервисов
```

### Проверка:
```bash
# 1. Eureka Dashboard
open http://localhost:8761

# 2. Получить OAuth2 токен
TOKEN=$(curl -s -X POST http://localhost:9000/oauth2/token \
  -u parking-system-client:secret \
  -d "grant_type=client_credentials" \
  -d "scope=read write" | jq -r '.access_token')

# 3. Создать парковочное место
curl -X POST http://localhost:8080/api/parking/spots \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": "A-101", "floor": 1, "status": "FREE"}'

# 4. Создать автомобиль
curl -X POST http://localhost:8080/api/vehicles/vehicles \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"plateNumber": "А123БВ", "model": "Toyota", "color": "Black", "ownerId": 1}'

# 5. Создать бронирование (Feign + OAuth2)
curl -X POST http://localhost:8080/api/reservations/reservations \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"parkingSpotId": 1, "vehicleId": 1, "startTime": "2024-01-20T09:00:00", "endTime": "2024-01-20T18:00:00"}'

# 6. Получить детали бронирования (демонстрация FeignClient)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/reservations/reservations/1/details
```

## 📝 Что нужно доработать

### Приоритет 1: Unit-тесты
Создать тесты для каждого сервиса:
```
parking-service/src/test/java/
vehicle-service/src/test/java/
reservation-service/src/test/java/
```

### Приоритет 2: Docker
Создать Dockerfile и docker-compose.yml для контейнеризации.

### Приоритет 3: Облако
Развернуть в Yandex Cloud или использовать minikube.

## 📚 Документация

- [README.md](README.md) - Основная документация
- [CONFIG_SERVER_SETUP.md](CONFIG_SERVER_SETUP.md) - Config Server
- [GIT_SETUP.md](GIT_SETUP.md) - Настройка Git
- [OAUTH2.md](OAUTH2.md) - OAuth2 авторизация
- [API_EXAMPLES.md](API_EXAMPLES.md) - Примеры API
- [DEMO.md](DEMO.md) - Демонстрация

## 🎯 Для сдачи работы

1. ✅ Закоммитить все изменения в Git
2. ✅ Запустить все сервисы
3. ✅ Продемонстрировать работу OAuth2
4. ✅ Продемонстрировать Config Server
5. ✅ Продемонстрировать FeignClient
6. ✅ Показать Eureka Dashboard
7. ⏳ Написать Unit-тесты
8. ⏳ Создать Docker контейнеры
9. ⏳ Развернуть в облаке

## 💡 Подсказки

### Git команды:
```bash
git add .
git commit -m "Add Spring Cloud Config Server"
git push origin main
```

### Порты сервисов:
- 8761 - Eureka Server
- 8888 - Config Server
- 9000 - Auth Server
- 8080 - API Gateway
- 8081 - Parking Service
- 8082 - Vehicle Service
- 8083 - Reservation Service

### Логи для отладки:
```bash
# Смотреть логи сервиса:
cd parking-service
mvn spring-boot:run

# В логах искать:
# - "Fetching config from server" - подключение к Config Server
# - "Registered application" - регистрация в Eureka
# - "Mapped" - зарегистрированные endpoints
```

