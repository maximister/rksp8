#!/bin/bash

# Цвета
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║         🏥 БЫСТРАЯ ПРОВЕРКА ЗДОРОВЬЯ СИСТЕМЫ             ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""

check_service() {
    local name=$1
    local url=$2
    
    echo -n "Проверка $name... "
    
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 3 "$url" 2>/dev/null)
    
    if [ "$STATUS" = "200" ] || [ "$STATUS" = "401" ] || [ "$STATUS" = "403" ]; then
        echo -e "${GREEN}✅ Доступен (HTTP $STATUS)${NC}"
        return 0
    else
        echo -e "${RED}❌ Недоступен (HTTP $STATUS)${NC}"
        return 1
    fi
}

AVAILABLE=0
TOTAL=0

echo -e "${YELLOW}Инфраструктурные сервисы:${NC}"
check_service "Eureka Server      " "http://localhost:8761" && ((AVAILABLE++))
((TOTAL++))
check_service "Config Server      " "http://localhost:8888/actuator/health" && ((AVAILABLE++))
((TOTAL++))
check_service "Auth Server        " "http://localhost:9000/oauth2/token" && ((AVAILABLE++))
((TOTAL++))

echo ""
echo -e "${YELLOW}Бизнес сервисы:${NC}"
check_service "API Gateway        " "http://localhost:8090/actuator/health" && ((AVAILABLE++))
((TOTAL++))
check_service "Parking Service    " "http://localhost:8091/actuator/health" && ((AVAILABLE++))
((TOTAL++))
check_service "Vehicle Service    " "http://localhost:8092/actuator/health" && ((AVAILABLE++))
((TOTAL++))
check_service "Reservation Service" "http://localhost:8093/actuator/health" && ((AVAILABLE++))
((TOTAL++))

echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Итого: $AVAILABLE/$TOTAL сервисов доступны${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

if [ $AVAILABLE -eq $TOTAL ]; then
    echo -e "${GREEN}🎉 Все сервисы работают!${NC}"
    exit 0
elif [ $AVAILABLE -gt 0 ]; then
    echo -e "${YELLOW}⚠️  Некоторые сервисы недоступны${NC}"
    exit 1
else
    echo -e "${RED}❌ Сервисы не запущены${NC}"
    echo ""
    echo -e "${YELLOW}Возможные причины:${NC}"
    echo "  1. Docker контейнеры не запущены"
    echo "  2. Сервисы запущены локально но еще не стартовали"
    echo "  3. Порты заняты другими процессами"
    echo ""
    echo -e "${YELLOW}Попробуйте:${NC}"
    echo "  - Запустить Docker: docker-compose -f docker-compose-fast.yml up -d"
    echo "  - Проверить логи: docker-compose -f docker-compose-fast.yml logs"
    echo "  - Проверить порты: lsof -i :8761,8888,9000,8090,8091,8092,8093"
    exit 1
fi

