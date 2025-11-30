#!/bin/bash

echo "================================================"
echo "🐳 Запуск микросервисной системы в Docker"
echo "================================================"
echo ""

# Цвета для вывода
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Проверка Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker не установлен!${NC}"
    echo "Установите Docker: https://docs.docker.com/get-docker/"
    exit 1
fi

echo -e "${GREEN}✅ Docker найден: $(docker --version)${NC}"

# Проверка Docker Compose
if ! docker compose version &> /dev/null; then
    echo -e "${RED}❌ Docker Compose не установлен!${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Docker Compose найден: $(docker compose version)${NC}"
echo ""

# Остановка старых контейнеров
echo -e "${YELLOW}🛑 Остановка старых контейнеров...${NC}"
docker compose down

echo ""
echo -e "${YELLOW}🏗️  Сборка и запуск сервисов...${NC}"
echo "Это может занять 5-10 минут при первом запуске..."
echo ""

# Сборка и запуск
docker compose up --build -d

# Проверка статуса
echo ""
echo -e "${YELLOW}⏳ Ожидание запуска сервисов...${NC}"
sleep 10

echo ""
echo -e "${GREEN}📊 Статус сервисов:${NC}"
docker compose ps

echo ""
echo -e "${GREEN}================================================${NC}"
echo -e "${GREEN}✅ Система запущена!${NC}"
echo -e "${GREEN}================================================${NC}"
echo ""
echo "Полезные ссылки:"
echo "  🔍 Eureka Dashboard: http://localhost:8761"
echo "  ⚙️  Config Server: http://localhost:8888"
echo "  🔐 Auth Server: http://localhost:9000"
echo "  🚪 API Gateway: http://localhost:8080"
echo ""
echo "Команды:"
echo "  📜 Логи всех сервисов: docker compose logs -f"
echo "  📜 Логи одного сервиса: docker compose logs -f parking-service"
echo "  🛑 Остановить: docker compose down"
echo ""

