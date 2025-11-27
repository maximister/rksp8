# Spring Cloud Config Server - Инструкция по настройке

## ✅ Что уже сделано

1. **Создан модуль `config-server`** с необходимыми зависимостями
2. **Создана папка `config-repo`** с конфигурационными файлами для всех сервисов
3. **Добавлены зависимости Config Client** во все микросервисы
4. **Обновлены `application.yml`** всех сервисов для подключения к Config Server

## 📁 Структура проекта

```
rksp8/
├── config-server/              ← Config Server (порт 8888)
│   ├── src/main/
│   │   ├── java/.../ConfigServerApplication.java
│   │   └── resources/application.yml
│   └── pom.xml
├── config-repo/                ← Конфигурационные файлы
│   ├── application.yml         (общие настройки)
│   ├── eureka-server.yml
│   ├── auth-server.yml
│   ├── api-gateway.yml
│   ├── parking-service.yml
│   ├── vehicle-service.yml
│   └── reservation-service.yml
├── eureka-server/
├── api-gateway/
├── parking-service/
├── vehicle-service/
├── reservation-service/
└── auth-server/
```

## 🚀 Порядок запуска сервисов (ВАЖНО!)

Config Server работает в **native режиме** (читает файлы из локальной папки `config-repo`).

### Правильный порядок:

```bash
1. Eureka Server    (порт 8761)  - Service Discovery
2. Config Server    (порт 8888)  - Сервер конфигурации
3. Auth Server      (порт 9000)  - OAuth2 авторизация
4. Parking Service  (порт 8081)
5. Vehicle Service  (порт 8082)
6. Reservation Service (порт 8083)
7. API Gateway      (порт 8080)  - Точка входа
```

### Команды для запуска:

```bash
# Терминал 1: Eureka Server
cd eureka-server
mvn spring-boot:run

# Подождите 30 секунд, затем:

# Терминал 2: Config Server
cd config-server
mvn spring-boot:run

# Подождите 10 секунд, затем в других терминалах:

# Терминал 3: Auth Server
cd auth-server
mvn spring-boot:run

# Терминал 4: Parking Service
cd parking-service
mvn spring-boot:run

# Терминал 5: Vehicle Service
cd vehicle-service
mvn spring-boot:run

# Терминал 6: Reservation Service
cd reservation-service
mvn spring-boot:run

# Терминал 7: API Gateway
cd api-gateway
mvn spring-boot:run
```

## 📋 Что нужно сделать для Git

### Шаг 1: Закоммитить конфигурационные файлы

```bash
cd /Users/maximister/dev/IdeaProjects/rksp8

# Добавить все новые файлы
git add config-server/
git add config-repo/
git add */pom.xml
git add */src/main/resources/application.yml
git add CONFIG_SERVER_SETUP.md

# Закоммитить
git commit -m "Add Spring Cloud Config Server with centralized configs"

# Запушить в удаленный репозиторий
git push origin main
```

### Шаг 2: Переключение на Git режим (опционально)

Если хотите, чтобы Config Server читал конфиги из Git (а не из локальной папки):

1. Создайте репозиторий на GitHub (например: `parking-system-configs`)
2. Отредактируйте `config-server/src/main/resources/application.yml`:

```yaml
spring:
  profiles:
    active: git  # Переключаем на git профиль
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-username/rksp8
          search-paths: config-repo
          clone-on-start: true
          default-label: main
```

## 🧪 Проверка работы Config Server

### 1. Проверить, что Config Server запустился:

```bash
curl http://localhost:8888/actuator/health
```

Ответ должен быть:
```json
{"status":"UP"}
```

### 2. Проверить конфигурацию для каждого сервиса:

```bash
# Конфиг для parking-service
curl http://localhost:8888/parking-service/default

# Конфиг для vehicle-service
curl http://localhost:8888/vehicle-service/default

# Конфиг для reservation-service
curl http://localhost:8888/reservation-service/default

# Конфиг для api-gateway
curl http://localhost:8888/api-gateway/default

# Конфиг для auth-server
curl http://localhost:8888/auth-server/default
```

Вы должны увидеть JSON с настройками для каждого сервиса.

### 3. Проверить регистрацию в Eureka:

Откройте http://localhost:8761 и убедитесь, что `CONFIG-SERVER` зарегистрирован.

## 🔧 Как работает Config Server

### Native режим (по умолчанию):

```
Config Server → Читает файлы из ./config-repo/ → Отдает клиентам
```

1. Config Server запускается и читает файлы из папки `config-repo`
2. Микросервисы при старте подключаются к Config Server (http://localhost:8888)
3. Config Server возвращает настройки из соответствующего файла:
   - `parking-service` → `config-repo/parking-service.yml`
   - `vehicle-service` → `config-repo/vehicle-service.yml`
   - и т.д.

### Git режим:

```
Config Server → Клонирует Git репозиторий → Читает конфиги → Отдает клиентам
```

## 📝 Настройки в application.yml клиентов

Все сервисы теперь имеют минимальный `application.yml`:

```yaml
spring:
  application:
    name: parking-service  # Имя сервиса
  config:
    import: "optional:configserver:http://localhost:8888"
  cloud:
    config:
      fail-fast: false  # Не падать, если Config Server недоступен
      retry:
        initial-interval: 1000
        max-attempts: 6
```

**`optional:`** - означает, что если Config Server недоступен, сервис всё равно запустится (но без настроек).

## 🎯 Преимущества централизованной конфигурации

✅ **Единое место** для всех настроек  
✅ **Версионность** через Git  
✅ **Изменение настроек** без пересборки приложений  
✅ **Разные профили** (dev, test, prod)  
✅ **Безопасность** - можно держать конфиги в приватном репозитории  

## 🔄 Обновление конфигурации без перезапуска

Если хотите, чтобы сервисы подхватывали изменения без перезапуска, добавьте `@RefreshScope`:

```java
@RestController
@RefreshScope  // ← Добавить эту аннотацию
public class ParkingSpotController {
    // ...
}
```

И вызовите:
```bash
curl -X POST http://localhost:8081/actuator/refresh
```

## ⚠️ Важные замечания

1. **Config Server должен запускаться ВТОРЫМ** (после Eureka)
2. **Все конфиги должны быть в папке `config-repo`** и закоммичены в Git
3. **Имя файла конфигурации** должно совпадать с `spring.application.name`
4. **`optional:configserver:`** позволяет сервису запуститься даже если Config Server недоступен

## 🐛 Проблемы и решения

### Config Server не запускается

```bash
# Проверьте, что папка config-repo существует
ls -la config-repo/

# Проверьте логи
cd config-server
mvn spring-boot:run
```

### Сервис не может подключиться к Config Server

1. Убедитесь, что Config Server запущен: `curl http://localhost:8888/actuator/health`
2. Проверьте имя сервиса в `spring.application.name`
3. Проверьте, что файл с именем сервиса существует в `config-repo/`

### Конфигурация не применяется

1. Перезапустите сервис
2. Проверьте логи - должно быть: `Fetching config from server at : http://localhost:8888`
3. Проверьте, что конфиг доступен: `curl http://localhost:8888/parking-service/default`

## 📚 Дополнительные ресурсы

- [Spring Cloud Config Documentation](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/)
- [Spring Cloud Config Server Guide](https://spring.io/guides/gs/centralized-configuration/)

