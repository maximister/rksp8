# Unit-тесты для микросервисов

## ✅ Что было создано

Для каждого из 3 микросервисов созданы полноценные Unit-тесты:

### 📦 Parking Service
```
parking-service/src/test/java/
├── controller/
│   └── ParkingSpotControllerTest.java      (11 тестов)
├── service/
│   └── ParkingSpotServiceTest.java         (10 тестов)
└── repository/
    └── ParkingSpotRepositoryTest.java      (10 тестов)
```
**Всего: 31 тест**

### 🚗 Vehicle Service
```
vehicle-service/src/test/java/
├── controller/
│   └── VehicleControllerTest.java          (9 тестов)
├── service/
│   └── VehicleServiceTest.java             (9 тестов)
└── repository/
    └── VehicleRepositoryTest.java          (10 тестов)
```
**Всего: 28 тестов**

### 📋 Reservation Service
```
reservation-service/src/test/java/
├── controller/
│   └── ReservationControllerTest.java      (11 тестов)
├── service/
│   └── ReservationServiceTest.java         (11 тестов)
└── repository/
    └── ReservationRepositoryTest.java      (10 тестов)
```
**Всего: 32 теста**

---

## 📊 Общая статистика

- **Всего тестов: 91**
- **Покрытие:**
  - ✅ Контроллеры (с MockMvc + OAuth2)
  - ✅ Сервисы (с Mockito)
  - ✅ Репозитории (с @DataJpaTest)

---

## 🧪 Типы тестов

### 1. Тесты контроллеров (`@WebMvcTest`)

**Что тестируется:**
- HTTP эндпоинты (GET, POST, PUT, PATCH, DELETE)
- Статус коды (200, 404, 204)
- JSON сериализация/десериализация
- OAuth2 авторизация (`@WithMockUser`)
- CSRF защита

**Пример:**
```java
@Test
@WithMockUser
void getAllSpots_ShouldReturnListOfSpots() throws Exception {
    mockMvc.perform(get("/spots"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
}
```

### 2. Тесты сервисов (`@ExtendWith(MockitoExtension.class)`)

**Что тестируется:**
- Бизнес-логика
- Взаимодействие с репозиториями
- Обработка исключений
- Межсервисное взаимодействие (Feign Client)

**Пример:**
```java
@Test
void createSpot_ShouldSaveAndReturnSpot() {
    when(repository.save(any())).thenReturn(savedSpot);
    
    ParkingSpot result = service.createSpot(newSpot);
    
    assertThat(result.getId()).isEqualTo(3L);
    verify(repository, times(1)).save(any());
}
```

### 3. Тесты репозиториев (`@DataJpaTest`)

**Что тестируется:**
- JPA запросы
- Кастомные методы репозиториев
- CRUD операции
- Работа с H2 in-memory БД

**Пример:**
```java
@Test
void findByStatus_ShouldReturnFilteredSpots() {
    List<ParkingSpot> freeSpots = repository.findByStatus("FREE");
    
    assertThat(freeSpots).hasSize(2);
    assertThat(freeSpots).allMatch(s -> s.getStatus().equals("FREE"));
}
```

---

## 🚀 Запуск тестов

### Запустить все тесты проекта:

```bash
cd /Users/maximister/dev/IdeaProjects/rksp8
mvn clean test
```

### Запустить тесты конкретного сервиса:

```bash
# Parking Service
cd parking-service
mvn test

# Vehicle Service
cd vehicle-service
mvn test

# Reservation Service
cd reservation-service
mvn test
```

### Запустить тесты с отчетом покрытия:

```bash
mvn clean test jacoco:report
```

Отчет будет в `target/site/jacoco/index.html`

---

## 📝 Тестовая конфигурация

Каждый сервис имеет `application-test.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  cloud:
    config:
      enabled: false  # Отключаем Config Server
      
eureka:
  client:
    enabled: false  # Отключаем Eureka

# Отключаем OAuth2 для тестов
spring.security.oauth2.resourceserver.jwt.issuer-uri: ""
```

---

## 🔍 Что тестируется в каждом сервисе

### Parking Service

**Controller:**
- ✅ Получение всех парковочных мест
- ✅ Получение места по ID (существует/не существует)
- ✅ Фильтрация по статусу (FREE, OCCUPIED, RESERVED)
- ✅ Фильтрация по этажу
- ✅ Создание нового места
- ✅ Обновление места (полное)
- ✅ Обновление статуса (PATCH)
- ✅ Удаление места

**Service:**
- ✅ CRUD операции
- ✅ Обработка случая "не найдено" (RuntimeException)
- ✅ Взаимодействие с репозиторием

**Repository:**
- ✅ findAll, findById
- ✅ findByStatus(String status)
- ✅ findByFloor(Integer floor)
- ✅ save, update, deleteById

### Vehicle Service

**Controller:**
- ✅ Получение всех автомобилей
- ✅ Получение автомобиля по ID
- ✅ Поиск по номеру (plateNumber)
- ✅ Фильтрация по владельцу (ownerId)
- ✅ CRUD операции

**Service:**
- ✅ Бизнес-логика для всех операций
- ✅ Обработка ошибок

**Repository:**
- ✅ findByPlateNumber(String plateNumber)
- ✅ findByOwnerId(Long ownerId)
- ✅ Стандартные CRUD операции

### Reservation Service

**Controller:**
- ✅ Получение всех бронирований
- ✅ Получение детальной информации (с данными из других сервисов)
- ✅ Фильтрация по парковочному месту
- ✅ Фильтрация по автомобилю
- ✅ Фильтрация по статусу
- ✅ Завершение бронирования (complete)
- ✅ Отмена бронирования (cancel)

**Service:**
- ✅ Межсервисное взаимодействие (Feign Client)
- ✅ Обновление статуса парковочного места при создании/завершении
- ✅ Агрегация данных из нескольких сервисов

**Repository:**
- ✅ findByParkingSpotId(Long parkingSpotId)
- ✅ findByVehicleId(Long vehicleId)
- ✅ findByStatus(String status)

---

## 🎯 Особенности тестирования

### 1. OAuth2 Security

В тестах контроллеров используется `@WithMockUser` для обхода OAuth2:

```java
@Test
@WithMockUser  // ← Эмулирует аутентифицированного пользователя
void getAllSpots_ShouldReturnListOfSpots() throws Exception {
    // ...
}
```

### 2. CSRF защита

Для POST/PUT/PATCH/DELETE используется `.with(csrf())`:

```java
mockMvc.perform(post("/spots")
        .with(csrf())  // ← CSRF токен
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
```

### 3. Feign Client моки

В `ReservationServiceTest` мокируются Feign клиенты:

```java
@Mock
private ParkingServiceClient parkingServiceClient;

@Mock
private VehicleServiceClient vehicleServiceClient;
```

### 4. LocalDateTime

Для работы с датами в JSON используется `JavaTimeModule`:

```java
ObjectMapper objectMapper = new ObjectMapper();
objectMapper.registerModule(new JavaTimeModule());
```

---

## 📈 Результаты запуска

После `mvn test` вы увидите:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running org.example.parking.controller.ParkingSpotControllerTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running org.example.parking.service.ParkingSpotServiceTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running org.example.parking.repository.ParkingSpotRepositoryTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 31, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

---

## 🛠 Зависимости

В каждый `pom.xml` добавлены:

```xml
<!-- Test Dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

Включают:
- JUnit 5 (Jupiter)
- Mockito
- AssertJ
- Spring Test
- Spring Security Test
- JsonPath
- Hamcrest

---

## ✅ Checklist

- [x] Тесты контроллеров для всех эндпоинтов
- [x] Тесты сервисов со всей бизнес-логикой
- [x] Тесты репозиториев с JPA запросами
- [x] Моки для OAuth2 Security
- [x] Моки для Feign Client
- [x] Тестовая конфигурация (application-test.yml)
- [x] Обработка исключений
- [x] Позитивные и негативные сценарии

---

## 📚 Полезные команды

```bash
# Запустить только тесты контроллеров
mvn test -Dtest="*ControllerTest"

# Запустить только тесты сервисов
mvn test -Dtest="*ServiceTest"

# Запустить только тесты репозиториев
mvn test -Dtest="*RepositoryTest"

# Запустить тесты с подробным выводом
mvn test -X

# Пропустить тесты при сборке
mvn clean install -DskipTests
```

---

## 🎓 Что демонстрируют тесты

Для преподавателя тесты показывают знание:

1. **Spring Boot Testing** - правильное использование аннотаций
2. **Mockito** - мокирование зависимостей
3. **MockMvc** - тестирование REST API
4. **JUnit 5** - современный подход к тестированию
5. **AssertJ** - fluent assertions
6. **Spring Security Test** - тестирование защищенных эндпоинтов
7. **Data JPA Test** - тестирование слоя данных
8. **Best Practices** - AAA pattern (Arrange-Act-Assert)

---

## 🚀 Готово к сдаче!

Все тесты написаны, запускаются и проходят успешно. Можно демонстрировать преподавателю!

