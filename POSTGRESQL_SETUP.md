# PostgreSQL Setup для локальной разработки

## 🎯 Переход с H2 на PostgreSQL

Проект был обновлен для использования PostgreSQL вместо in-memory H2 базы данных.

---

## 📦 Что изменилось

### 1. Dependencies (pom.xml)
- ✅ Добавлен `postgresql` драйвер для всех сервисов с БД
- ✅ H2 перенесен в `test` scope (используется только для тестов)

### 2. Конфигурация (config-repo/*.yml)
- ✅ `parking-service.yml` - PostgreSQL на порту 5432
- ✅ `vehicle-service.yml` - PostgreSQL на порту 5433
- ✅ `reservation-service.yml` - PostgreSQL на порту 5434
- ✅ Hibernate DDL: `create-drop` → `update` (для сохранения данных)
- ✅ Dialect: PostgreSQLDialect

### 3. Docker Compose
- ✅ Добавлены 3 PostgreSQL контейнера
- ✅ Volumes для персистентности данных
- ✅ Health checks для корректного старта

---

## 🚀 Локальный запуск с PostgreSQL

### Вариант 1: Запуск только PostgreSQL (рекомендуется для разработки)

```bash
# Запустить только PostgreSQL базы данных
docker-compose -f docker-compose-postgres-only.yml up -d

# Проверить статус
docker-compose -f docker-compose-postgres-only.yml ps

# Запустить сервисы в IntelliJ IDEA
# (они автоматически подключатся к PostgreSQL на localhost:5432/5433/5434)
```

### Вариант 2: Запуск всей системы в Docker

```bash
# Собрать JARs локально
mvn clean package -DskipTests

# Запустить всю систему
docker-compose up -d

# Проверить логи
docker-compose logs -f
```

---

## 🗄️ Подключение к PostgreSQL

### Parking Service
```
Host: localhost
Port: 5432
Database: parkingdb
User: parking_user
Password: parking_pass
```

### Vehicle Service
```
Host: localhost
Port: 5433
Database: vehicledb
User: vehicle_user
Password: vehicle_pass
```

### Reservation Service
```
Host: localhost
Port: 5434
Database: reservationdb
User: reservation_user
Password: reservation_pass
```

### Подключение через psql

```bash
# Parking DB
docker exec -it postgres-parking psql -U parking_user -d parkingdb

# Vehicle DB
docker exec -it postgres-vehicle psql -U vehicle_user -d vehicledb

# Reservation DB
docker exec -it postgres-reservation psql -U reservation_user -d reservationdb
```

---

## 🔍 Полезные команды

### Просмотр данных

```sql
-- Parking DB
SELECT * FROM parking_spots;

-- Vehicle DB
SELECT * FROM vehicles;

-- Reservation DB
SELECT * FROM reservations;
```

### Очистка данных

```bash
# Остановить и удалить контейнеры с данными
docker-compose -f docker-compose-postgres-only.yml down -v
```

---

## 🧪 Тесты

Тесты продолжают использовать H2 (in-memory) для быстрой работы:
- ✅ `application-test.yml` в каждом сервисе настроен на H2
- ✅ Тесты изолированы от production БД
- ✅ Не требуют запущенного PostgreSQL

```bash
# Запуск тестов (H2 используется автоматически)
mvn test
```

---

## 📊 Мониторинг

### Проверка health PostgreSQL

```bash
# Parking DB
docker exec postgres-parking pg_isready -U parking_user -d parkingdb

# Vehicle DB
docker exec postgres-vehicle pg_isready -U vehicle_user -d vehicledb

# Reservation DB
docker exec postgres-reservation pg_isready -U reservation_user -d reservationdb
```

### Просмотр логов

```bash
docker logs postgres-parking
docker logs postgres-vehicle
docker logs postgres-reservation
```

---

## 🐛 Troubleshooting

### Проблема: "Connection refused" при старте сервиса

**Решение:** Убедитесь, что PostgreSQL контейнеры запущены и healthy

```bash
docker-compose -f docker-compose-postgres-only.yml ps
```

### Проблема: "Database does not exist"

**Решение:** Пересоздайте контейнеры

```bash
docker-compose -f docker-compose-postgres-only.yml down -v
docker-compose -f docker-compose-postgres-only.yml up -d
```

### Проблема: Порт уже занят (5432/5433/5434)

**Решение:** Остановите другие PostgreSQL процессы или измените порты в `docker-compose-postgres-only.yml`

```bash
# Проверить, что использует порт
lsof -i :5432
lsof -i :5433
lsof -i :5434
```

---

## 🎯 Рекомендуемый workflow для разработки

1. **Запустите PostgreSQL один раз:**
   ```bash
   docker-compose -f docker-compose-postgres-only.yml up -d
   ```

2. **Запускайте сервисы в IntelliJ IDEA** в нужном порядке:
   - Eureka Server
   - Config Server
   - Auth Server
   - Parking Service
   - Vehicle Service
   - Reservation Service
   - API Gateway

3. **Данные сохраняются между перезапусками** сервисов (благодаря PostgreSQL volumes)

4. **Когда закончите работу:**
   ```bash
   # Остановить PostgreSQL (данные сохранятся)
   docker-compose -f docker-compose-postgres-only.yml stop
   
   # Или удалить всё с данными
   docker-compose -f docker-compose-postgres-only.yml down -v
   ```

---

## ✅ Преимущества PostgreSQL

- ✅ **Персистентность данных** - данные сохраняются между перезапусками
- ✅ **Production-ready** - реальная СУБД вместо in-memory
- ✅ **Изоляция** - каждый сервис имеет свою БД
- ✅ **Тестирование** - можно проверить работу с реальной БД
- ✅ **Мониторинг** - легко подключиться и посмотреть данные

---

## 📝 Примечания

- PostgreSQL версия: **15-alpine** (легковесный образ)
- Автоматическая миграция схемы: **Hibernate DDL Auto = update**
- Volumes: **Named volumes** для каждого сервиса
- Network: **parking-network** (bridge)

