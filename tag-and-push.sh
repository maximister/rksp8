#!/bin/bash

# =============================================================================
# Скрипт для тегирования и публикации образов в Docker Hub
# =============================================================================

set -e

# Цвета для вывода
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# КОНФИГУРАЦИЯ
DOCKER_USER="${DOCKER_USER:-maximister}"  # Замените на ваш Docker Hub username

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

echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  Тегирование и публикация образов в Docker Hub${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "Docker Hub username: ${GREEN}$DOCKER_USER${NC}"
echo ""

# Проверка авторизации
if ! docker info | grep -q "Username"; then
    echo -e "${RED}❌ Ошибка: Не авторизованы в Docker Hub${NC}"
    echo "Выполните: docker login"
    exit 1
fi

echo -e "${GREEN}✅ Авторизован в Docker Hub${NC}"
echo ""

# Проверка наличия локальных образов
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  Проверка локальных образов${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

MISSING_IMAGES=0
for service in "${SERVICES[@]}"; do
    if docker images | grep -q "rksp8-$service"; then
        echo -e "${GREEN}✅ rksp8-$service найден${NC}"
    else
        echo -e "${RED}❌ rksp8-$service НЕ найден${NC}"
        MISSING_IMAGES=1
    fi
done

if [ $MISSING_IMAGES -eq 1 ]; then
    echo ""
    echo -e "${RED}❌ Не все образы собраны!${NC}"
    echo "Сначала соберите образы:"
    echo "  PLATFORM=linux/amd64 ./docker-build-fast.sh"
    exit 1
fi

echo ""

# Тегирование
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  Тегирование образов${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

for service in "${SERVICES[@]}"; do
    echo -e "${GREEN}▶ Тегирую $service...${NC}"
    docker tag "rksp8-$service:latest" "$DOCKER_USER/rksp8-$service:latest"
    echo -e "${GREEN}✅ $service -> $DOCKER_USER/rksp8-$service:latest${NC}"
done

echo ""

# Публикация
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  Публикация образов в Docker Hub${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

read -p "Хотите запушить образы в Docker Hub? (y/n): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}⚠ Публикация отменена${NC}"
    exit 0
fi

echo ""

for service in "${SERVICES[@]}"; do
    echo -e "${GREEN}▶ Публикую $service...${NC}"
    docker push "$DOCKER_USER/rksp8-$service:latest"
    echo -e "${GREEN}✅ $service опубликован${NC}"
    echo ""
done

# Итоги
echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  Готово!${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "${GREEN}✅ Все образы успешно опубликованы в Docker Hub!${NC}"
echo ""
echo "Ваши образы:"
for service in "${SERVICES[@]}"; do
    echo "  • $DOCKER_USER/rksp8-$service:latest"
done
echo ""
echo "Проверьте на Docker Hub:"
echo "  https://hub.docker.com/u/$DOCKER_USER"
echo ""
echo "Для использования на сервере:"
echo "  docker pull $DOCKER_USER/rksp8-eureka-server:latest"
echo "  docker pull $DOCKER_USER/rksp8-config-server:latest"
echo "  # ... и т.д."
echo ""

