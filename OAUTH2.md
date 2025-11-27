# OAuth2 Авторизация в системе управления парковкой

## Архитектура OAuth2

```
┌─────────────┐     1. Запрос токена      ┌─────────────────┐
│   Client    │ ──────────────────────────▶│   Auth Server   │
│  (curl/app) │                            │   (port 9000)   │
└──────┬──────┘ ◀────────────────────────  └─────────────────┘
       │          2. JWT Access Token
       │
       │ 3. Запрос с токеном
       │    Authorization: Bearer <token>
       ▼
┌─────────────────┐
│   API Gateway   │ ─── Проверяет токен
│   (port 8080)   │
└────────┬────────┘
         │ 4. Передаёт токен дальше (TokenRelay)
         │
    ┌────┴────┬──────────────┐
    ▼         ▼              ▼
┌────────┐ ┌────────┐ ┌────────────┐
│Parking │ │Vehicle │ │Reservation │ ─── Каждый проверяет токен
│Service │ │Service │ │  Service   │     и права доступа (scopes)
└────────┘ └────────┘ └────────────┘
```

## Как это работает

### 1. Authorization Server (Auth Server)

**Auth Server** - это сервер авторизации, который:
- Хранит информацию о пользователях (в БД)
- Хранит информацию о клиентах (приложениях)
- Выдаёт JWT токены
- Предоставляет публичные ключи для проверки токенов (JWKS)

**Endpoints:**
- `POST /oauth2/token` - получение токена
- `GET /oauth2/jwks` - публичные ключи для проверки JWT
- `GET /.well-known/openid-configuration` - метаданные сервера

### 2. Resource Server (Микросервисы)

Каждый микросервис является **Resource Server** - он защищает свои ресурсы:
- Получает JWT токен в заголовке `Authorization: Bearer <token>`
- Проверяет подпись токена (используя публичные ключи от Auth Server)
- Проверяет срок действия токена
- Проверяет scopes (права доступа)

### 3. JWT Token

JWT токен содержит:
```json
{
  "sub": "parking-system-client",    // ID клиента
  "aud": "parking-system-client",    // Аудитория
  "scope": "read write",              // Права доступа
  "iss": "http://localhost:9000",    // Кто выдал
  "exp": 1706000000,                  // Срок действия
  "iat": 1705996400                   // Когда выдан
}
```

### 4. Scopes (Права доступа)

- `read` - право на чтение (GET запросы)
- `write` - право на запись (POST, PUT, PATCH, DELETE)

## Зарегистрированные клиенты

### 1. Service Client (для межсервисного взаимодействия)
- **Client ID:** `parking-system-client`
- **Client Secret:** `secret`
- **Grant Type:** `client_credentials`
- **Scopes:** `read`, `write`

### 2. Web Client (для пользователей)
- **Client ID:** `parking-web-client`
- **Client Secret:** `web-secret`
- **Grant Type:** `authorization_code`
- **Scopes:** `openid`, `profile`, `read`, `write`

## Тестовые пользователи

| Username | Password | Role  |
|----------|----------|-------|
| admin    | admin    | ADMIN |
| user     | user     | USER  |

## Примеры использования

### 1. Получение токена (Client Credentials Grant)

Этот способ используется для межсервисного взаимодействия или для API клиентов:

```bash
# Получаем токен
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u parking-system-client:secret \
  -d "grant_type=client_credentials" \
  -d "scope=read write"
```

Ответ:
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "read write"
}
```

### 2. Использование токена для запросов

```bash
# Сохраняем токен в переменную
TOKEN=$(curl -s -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u parking-system-client:secret \
  -d "grant_type=client_credentials" \
  -d "scope=read write" | jq -r '.access_token')

# Используем токен для GET запроса
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/parking/spots

# Используем токен для POST запроса
curl -X POST http://localhost:8080/api/parking/spots \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "number": "A-101",
    "floor": 1,
    "status": "FREE"
  }'
```

### 3. Запрос без токена (ошибка 401)

```bash
curl http://localhost:8080/api/parking/spots
# Ответ: 401 Unauthorized
```

### 4. Запрос с неверным scope (ошибка 403)

```bash
# Получаем токен только с правом на чтение
TOKEN=$(curl -s -X POST http://localhost:9000/oauth2/token \
  -u parking-system-client:secret \
  -d "grant_type=client_credentials" \
  -d "scope=read" | jq -r '.access_token')

# Пытаемся создать ресурс (требует scope=write)
curl -X POST http://localhost:8080/api/parking/spots \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": "A-101", "floor": 1, "status": "FREE"}'
# Ответ: 403 Forbidden
```

## Полный сценарий тестирования

```bash
#!/bin/bash

echo "=== 1. Получаем токен ==="
TOKEN=$(curl -s -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u parking-system-client:secret \
  -d "grant_type=client_credentials" \
  -d "scope=read write" | jq -r '.access_token')

echo "Token: ${TOKEN:0:50}..."
echo ""

echo "=== 2. Создаём парковочное место ==="
curl -s -X POST http://localhost:8080/api/parking/spots \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": "A-101", "floor": 1, "status": "FREE"}' | jq
echo ""

echo "=== 3. Создаём автомобиль ==="
curl -s -X POST http://localhost:8080/api/vehicles/vehicles \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"plateNumber": "А123БВ", "model": "Toyota", "color": "Black", "ownerId": 1}' | jq
echo ""

echo "=== 4. Создаём бронирование ==="
curl -s -X POST http://localhost:8080/api/reservations/reservations \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"parkingSpotId": 1, "vehicleId": 1, "startTime": "2024-01-20T09:00:00", "endTime": "2024-01-20T18:00:00"}' | jq
echo ""

echo "=== 5. Получаем детали бронирования (Feign + OAuth2) ==="
curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/reservations/reservations/1/details | jq
echo ""

echo "=== 6. Проверяем запрос без токена (должен быть 401) ==="
curl -s -w "\nHTTP Status: %{http_code}\n" \
  http://localhost:8080/api/parking/spots
```

## Порядок запуска сервисов

1. **Eureka Server** (порт 8761) - Service Discovery
2. **Auth Server** (порт 9000) - Сервер авторизации
3. **Parking Service** (порт 8081)
4. **Vehicle Service** (порт 8082)
5. **Reservation Service** (порт 8083)
6. **API Gateway** (порт 8080)

## Проверка работы Auth Server

```bash
# Метаданные сервера
curl http://localhost:9000/.well-known/openid-configuration | jq

# Публичные ключи (JWKS)
curl http://localhost:9000/oauth2/jwks | jq
```

## Декодирование JWT токена

Можно использовать https://jwt.io или команду:

```bash
# Декодируем payload токена
echo $TOKEN | cut -d'.' -f2 | base64 -d 2>/dev/null | jq
```

## Безопасность

### Что проверяется:
1. **Подпись токена** - токен подписан приватным ключом Auth Server
2. **Issuer (iss)** - токен выдан нашим Auth Server
3. **Expiration (exp)** - токен не истёк
4. **Audience (aud)** - токен предназначен для нашего клиента
5. **Scope** - у клиента есть необходимые права

### Передача токена между сервисами

Когда Reservation Service вызывает Parking Service через Feign Client:
1. Reservation Service получает токен из входящего запроса
2. FeignConfig добавляет этот токен в исходящий запрос
3. Parking Service проверяет токен и выполняет запрос

```java
// FeignConfig.java
@Bean
public RequestInterceptor requestInterceptor() {
    return requestTemplate -> {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String token = jwtAuth.getToken().getTokenValue();
            requestTemplate.header("Authorization", "Bearer " + token);
        }
    };
}
```

## Диаграмма потока Client Credentials

```
┌────────┐                              ┌─────────────┐
│ Client │                              │ Auth Server │
└───┬────┘                              └──────┬──────┘
    │                                          │
    │  POST /oauth2/token                      │
    │  Authorization: Basic base64(id:secret)  │
    │  grant_type=client_credentials           │
    │  scope=read write                        │
    │─────────────────────────────────────────▶│
    │                                          │
    │                                          │ Проверяет client_id
    │                                          │ и client_secret
    │                                          │
    │           { access_token: "...",         │
    │             token_type: "Bearer",        │
    │             expires_in: 3600 }           │
    │◀─────────────────────────────────────────│
    │                                          │
    │                                          │
    │  GET /api/parking/spots                  │
    │  Authorization: Bearer <token>           │
    │─────────────────────────────────────────▶│ API Gateway
    │                                          │     │
    │                                          │     │ Проверяет JWT
    │                                          │     │ Передаёт в сервис
    │                                          │     ▼
    │                                          │ Parking Service
    │                                          │     │
    │                                          │     │ Проверяет JWT
    │                                          │     │ Проверяет scope
    │                                          │     │
    │           [{ id: 1, number: "A-101" }]   │◀────┘
    │◀─────────────────────────────────────────│
    │                                          │
```



