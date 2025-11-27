# Централизованные конфигурации

Эта папка содержит конфигурационные файлы для всех микросервисов системы управления парковкой.

## Структура файлов

- `application.yml` - Общие настройки для всех сервисов (Eureka, Actuator, Logging)
- `eureka-server.yml` - Конфигурация Eureka Server
- `config-server.yml` - Конфигурация Config Server (если нужна)
- `auth-server.yml` - Конфигурация Auth Server (OAuth2)
- `api-gateway.yml` - Конфигурация API Gateway (маршрутизация, OAuth2)
- `parking-service.yml` - Конфигурация Parking Service (БД, OAuth2)
- `vehicle-service.yml` - Конфигурация Vehicle Service (БД, OAuth2)
- `reservation-service.yml` - Конфигурация Reservation Service (БД, OAuth2, Feign)

## Как это работает

1. Config Server читает эти файлы при запуске
2. Микросервисы подключаются к Config Server и получают свои настройки
3. Настройки применяются при старте каждого сервиса

## Принцип именования

Имя файла должно совпадать с `spring.application.name` соответствующего сервиса:

```
parking-service.yml  ←→  spring.application.name: parking-service
vehicle-service.yml  ←→  spring.application.name: vehicle-service
```

## Изменение конфигурации

1. Отредактируйте нужный файл в этой папке
2. Закоммитьте изменения (если используется Git режим)
3. Перезапустите сервис ИЛИ вызовите `/actuator/refresh` (если используется @RefreshScope)

## Профили

Можно создавать конфигурации для разных окружений:

- `parking-service-dev.yml` - для разработки
- `parking-service-prod.yml` - для продакшена
- `parking-service-test.yml` - для тестирования

Активация профиля:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=prod
```

