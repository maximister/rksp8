# 🐳 Docker развертывание системы управления парковкой

## 📦 Что создано

Для каждого сервиса создан `Dockerfile`:
- ✅ `eureka-server/Dockerfile`
- ✅ `config-server/Dockerfile`
- ✅ `auth-server/Dockerfile`
- ✅ `api-gateway/Dockerfile`
- ✅ `parking-service/Dockerfile`
- ✅ `vehicle-service/Dockerfile`
- ✅ `reservation-service/Dockerfile`

Дополнительно:
- ✅ `docker-compose.yml` - оркестрация всех сервисов
- ✅ `.dockerignore` - оптимизация сборки

---

## 🚀 Быстрый старт

### Предварительные требования

- Docker Desktop или Docker Engine (версия 20.10+)
- Docker Compose (версия 2.0+)
- 4 GB RAM минимум

### Проверка установки Docker:

```bash
docker --version
docker-compose --version
```

### Запуск всей системы одной командой:

```bash
cd /Users/maximister/dev/IdeaProjects/rksp8

# Сборка и запуск всех сервисов
docker-compose up --build
```

**Время первой сборки:** ~5-10 минут (зависит от скорости интернета)

### Запуск в фоновом режиме:

```bash
docker-compose up -d --build
```

---

## 📊 Порядок запуска сервисов

Docker Compose автоматически запускает сервисы в правильном порядке:

```
1. Eureka Server (8761)     - ждет healthcheck
2. Config Server (8888)     - ждет Eureka
3. Auth Server (9000)       - ждет Config Server
4. Parking Service (8081)   - ждет Auth Server
5. Vehicle Service (8082)   - ждет Auth Server
6. Reservation Service (8083) - ждет Parking & Vehicle
7. API Gateway (8080)       - ждет все сервисы
```

---

## 🔍 Проверка работы

### 1. Проверить статус контейнеров:

```bash
docker-compose ps
```

Должны быть запущены все 7 контейнеров.

### 2. Проверить логи:

```bash
# Все сервисы
docker-compose logs

# Конкретный сервис
docker-compose logs eureka-server
docker-compose logs api-gateway

# Следить за логами в реальном времени
docker-compose logs -f
```

### 3. Проверить Eureka Dashboard:

Откройте в браузере: http://localhost:8761

Все сервисы должны быть зарегистрированы.

### 4. Проверить Config Server:

```bash
curl http://localhost:8888/actuator/health
```

### 5. Тестовый API запрос:

```bash
# Получить токен
TOKEN=$(curl -s -X POST http://localhost:9000/oauth2/token \
  -u parking-system-client:secret \
  -d "grant_type=client_credentials" \
  -d "scope=read write" | jq -r '.access_token')

# Создать парковочное место
curl -X POST http://localhost:8080/api/parking/spots \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "number": "A-101",
    "floor": 1,
    "status": "FREE"
  }'
```

---

## 🛠 Полезные команды

### Управление контейнерами:

```bash
# Остановить все сервисы
docker-compose down

# Остановить и удалить volumes
docker-compose down -v

# Перезапустить конкретный сервис
docker-compose restart parking-service

# Остановить конкретный сервис
docker-compose stop parking-service

# Запустить остановленный сервис
docker-compose start parking-service
```

### Просмотр ресурсов:

```bash
# Использование ресурсов
docker stats

# Список образов
docker images

# Список контейнеров
docker ps -a
```

### Очистка:

```bash
# Удалить все остановленные контейнеры
docker container prune

# Удалить неиспользуемые образы
docker image prune

# Удалить все (осторожно!)
docker system prune -a
```

### Пересборка конкретного сервиса:

```bash
# Пересобрать без кеша
docker-compose build --no-cache parking-service

# Пересобрать и запустить
docker-compose up -d --build parking-service
```

---

## 🏗 Архитектура Docker образов

### Multi-stage build

Все Dockerfile используют multi-stage сборку для оптимизации:

**Stage 1: Build** (eclipse-temurin:17-jdk-alpine)
- Компиляция Java кода
- Сборка JAR файла

**Stage 2: Runtime** (eclipse-temurin:17-jre-alpine)
- Только JRE (без JDK) - меньше размер
- Копируется только JAR файл
- Alpine Linux - минимальный образ

**Размер образа:** ~150-200 MB на сервис

---

## 🌐 Сеть Docker

Все сервисы находятся в одной сети: `parking-network`

**Преимущества:**
- Сервисы обращаются друг к другу по имени контейнера
- Изолированы от внешней сети
- Безопасная коммуникация

**Пример:**
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
```

---

## 🔐 Переменные окружения

### Общие для всех сервисов:

```yaml
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
SPRING_CONFIG_IMPORT: optional:configserver:http://config-server:8888
```

### OAuth2:

```yaml
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER-URI: http://auth-server:9000
```

### Переопределение конфигурации:

Можно добавить в `docker-compose.yml`:

```yaml
environment:
  - SERVER_PORT=8081
  - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/parking
  - LOGGING_LEVEL_ROOT=DEBUG
```

---

## 📈 Мониторинг

### Healthchecks

Eureka и Config Server имеют healthcheck:

```yaml
healthcheck:
  test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8761/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 5
```

### Проверка здоровья:

```bash
docker-compose ps
```

Статус `healthy` означает что сервис готов.

---

## 🐛 Troubleshooting

### Сервис не запускается

```bash
# Проверить логи
docker-compose logs [service-name]

# Проверить healthcheck
docker inspect [container-name] | grep Health -A 10
```

### Порт уже занят

```bash
# Найти процесс на порту
lsof -ti:8080

# Убить процесс
lsof -ti:8080 | xargs kill

# Или изменить порт в docker-compose.yml
ports:
  - "8090:8080"  # внешний:внутренний
```

### Медленная сборка

```bash
# Использовать кеш Maven
# Добавить volume в docker-compose.yml:
volumes:
  - ~/.m2:/root/.m2
```

### OutOfMemory ошибки

Увеличить память для Docker Desktop:
- Settings → Resources → Memory → 4GB+

Или добавить в docker-compose.yml:

```yaml
deploy:
  resources:
    limits:
      memory: 512M
```

### Контейнеры не видят друг друга

Проверить сеть:

```bash
docker network ls
docker network inspect rksp8_parking-network
```

---

## 🚢 Production готовность

### Для продакшена нужно:

1. **Использовать внешнюю БД** (PostgreSQL вместо H2)

```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/parking
  - SPRING_DATASOURCE_USERNAME=parking_user
  - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
```

2. **Добавить volumes для данных**

```yaml
volumes:
  - postgres-data:/var/lib/postgresql/data
```

3. **Использовать secrets для паролей**

```yaml
secrets:
  - db_password
```

4. **Добавить reverse proxy (Nginx)**

5. **Настроить логирование** (ELK stack)

6. **Мониторинг** (Prometheus + Grafana)

---

## 📝 Docker Compose команды - шпаргалка

```bash
# Сборка и запуск
docker-compose up -d --build

# Остановка
docker-compose down

# Перезапуск
docker-compose restart

# Логи
docker-compose logs -f [service]

# Статус
docker-compose ps

# Выполнить команду в контейнере
docker-compose exec [service] sh

# Масштабирование (несколько экземпляров)
docker-compose up -d --scale parking-service=3
```

---

## ✅ Checklist перед деплоем

- [ ] Все контейнеры запущены: `docker-compose ps`
- [ ] Eureka Dashboard доступен: http://localhost:8761
- [ ] Config Server отвечает: http://localhost:8888/actuator/health
- [ ] API Gateway работает: http://localhost:8080
- [ ] OAuth2 токен получается успешно
- [ ] Тестовые запросы проходят

---

## 🎯 Следующие шаги

1. **Локально протестировать:** `docker-compose up`
2. **Оптимизировать образы** (если нужно)
3. **Запушить образы в Docker Hub** (опционально)
4. **Развернуть в облаке** (Yandex Cloud, AWS, etc.)

---

## 📚 Дополнительные ресурсы

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot with Docker](https://spring.io/guides/gs/spring-boot-docker/)

---

**Готово! Система контейнеризирована и готова к развертыванию! 🚀**
