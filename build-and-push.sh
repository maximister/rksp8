#!/bin/bash

# =============================================================================
# Скрипт сборки и загрузки образов в Docker Hub
# =============================================================================

set -e  # Остановить при ошибке

# КОНФИГУРАЦИЯ
# =============================================================================
DOCKER_USER="${DOCKER_USER:-maximister}"  # Замените на ваш username
VERSION="${VERSION:-latest}"

# Определяем платформу (для Apple Silicon используем arm64, для деплоя - amd64)
PLATFORM="${PLATFORM:-linux/arm64}"  # Измените на linux/amd64 для деплоя на сервер

# Цвета для вывода
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Список сервисов
SERVICES=(
    "eureka-server"
    "config-server"
    "auth-server"
    "parking-service"
    "vehicle-service"
    "reservation-service"
    "api-gateway"
)

# =============================================================================
# ФУНКЦИИ
# =============================================================================

print_header() {
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
}

print_step() {
    echo -e "${GREEN}▶ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# =============================================================================
# ПРОВЕРКИ
# =============================================================================

print_header "Проверка окружения"

# Проверка Docker
if ! command -v docker &> /dev/null; then
    print_error "Docker не установлен!"
    exit 1
fi
print_success "Docker установлен"

# Проверка авторизации в Docker Hub
if ! docker info | grep -q "Username"; then
    print_warning "Не авторизованы в Docker Hub"
    echo "Выполните: docker login"
    exit 1
fi
print_success "Авторизован в Docker Hub"

# Проверка username
if [ "$DOCKER_USER" = "your-dockerhub-username" ]; then
    print_error "Установите переменную DOCKER_USER!"
    echo "Пример: export DOCKER_USER=maximister"
    echo "Или отредактируйте этот скрипт и замените your-dockerhub-username"
    exit 1
fi

print_success "Docker Hub username: $DOCKER_USER"
print_success "Версия: $VERSION"

# =============================================================================
# СБОРКА ОБРАЗОВ
# =============================================================================

print_header "Сборка образов локально"

if [ -f "./docker-build-fast.sh" ]; then
    print_step "Используем docker-build-fast.sh для быстрой сборки..."
    chmod +x ./docker-build-fast.sh
    ./docker-build-fast.sh
else
    print_warning "docker-build-fast.sh не найден, собираем через Dockerfile..."
    
    for service in "${SERVICES[@]}"; do
        print_step "Сборка $service..."
        docker build -t "$service:latest" \
            -f "$service/Dockerfile" \
            --platform "$PLATFORM" \
            .
        print_success "$service собран"
    done
fi

# =============================================================================
# ТЕГИРОВАНИЕ
# =============================================================================

print_header "Тегирование образов"

for service in "${SERVICES[@]}"; do
    print_step "Тегируем $service..."
    
    # Тег latest
    docker tag "$service:latest" "$DOCKER_USER/$service:$VERSION"
    
    # Опционально: тег с датой
    DATE_TAG=$(date +%Y%m%d-%H%M%S)
    docker tag "$service:latest" "$DOCKER_USER/$service:$DATE_TAG"
    
    print_success "$service -> $DOCKER_USER/$service:$VERSION"
done

# =============================================================================
# ЗАГРУЗКА В DOCKER HUB
# =============================================================================

print_header "Загрузка образов в Docker Hub"

for service in "${SERVICES[@]}"; do
    print_step "Загружаем $service:$VERSION..."
    docker push "$DOCKER_USER/$service:$VERSION"
    print_success "$service:$VERSION загружен"
    
    # Загружаем также тег с датой
    DATE_TAG=$(date +%Y%m%d-%H%M%S)
    docker push "$DOCKER_USER/$service:$DATE_TAG" 2>/dev/null || true
done

# =============================================================================
# ИТОГИ
# =============================================================================

print_header "Готово!"

echo ""
echo -e "${GREEN}✓ Все образы успешно загружены в Docker Hub!${NC}"
echo ""
echo "Ваши образы:"
for service in "${SERVICES[@]}"; do
    echo "  • $DOCKER_USER/$service:$VERSION"
done
echo ""
echo "Для использования на VM:"
echo "  1. Скопируйте docker-compose-cloud.yml на сервер"
echo "  2. Выполните: export DOCKER_USER=$DOCKER_USER"
echo "  3. Выполните: docker-compose -f docker-compose-cloud.yml pull"
echo "  4. Выполните: docker-compose -f docker-compose-cloud.yml up -d"
echo ""
echo "Ссылка на Docker Hub:"
echo "  https://hub.docker.com/u/$DOCKER_USER"
echo ""

