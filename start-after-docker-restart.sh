#!/bin/bash

# Скрипт для запуска системы после увеличения памяти Docker Desktop

echo "════════════════════════════════════════════════════════════════"
echo "  Проверка ресурсов Docker"
echo "════════════════════════════════════════════════════════════════"
echo ""

MEMORY=$(docker info 2>&1 | grep "Total Memory" | awk '{print $3}')
CPUS=$(docker info 2>&1 | grep "CPUs" | awk '{print $2}')

echo "Текущие ресурсы Docker:"
echo "  Memory: $MEMORY"
echo "  CPUs: $CPUS"
echo ""

# Проверка минимальных требований
MEMORY_NUM=$(echo $MEMORY | sed 's/GiB//')
if (( $(echo "$MEMORY_NUM < 5" | bc -l) )); then
    echo "⚠️  ВНИМАНИЕ: Памяти недостаточно!"
    echo "   Текущая: $MEMORY"
    echo "   Требуется: минимум 6 GiB"
    echo ""
    echo "Пожалуйста, увеличьте память в Docker Desktop Settings → Resources"
    echo ""
    read -p "Продолжить запуск? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo "════════════════════════════════════════════════════════════════"
echo "  Остановка старых контейнеров"
echo "════════════════════════════════════════════════════════════════"
docker-compose down -v

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "  Запуск всех сервисов"
echo "════════════════════════════════════════════════════════════════"
docker-compose up -d

echo ""
echo "⏳ Ожидание инициализации сервисов (2 минуты)..."
echo ""

for i in {1..12}; do
    sleep 10
    echo "   $((i * 10)) секунд..."
done

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "  Статус контейнеров"
echo "════════════════════════════════════════════════════════════════"
docker-compose ps

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "  Проверка здоровья сервисов"
echo "════════════════════════════════════════════════════════════════"
echo ""

check_health() {
    SERVICE=$1
    PORT=$2
    STATUS=$(curl -s http://localhost:$PORT/actuator/health 2>/dev/null | jq -r '.status' 2>/dev/null)
    if [ "$STATUS" == "UP" ]; then
        echo "✅ $SERVICE ($PORT) - UP"
    else
        echo "❌ $SERVICE ($PORT) - DOWN или недоступен"
    fi
}

check_health "Eureka Server    " 8761
check_health "Config Server    " 8888
check_health "Auth Server      " 9000
check_health "Parking Service  " 8081
check_health "Vehicle Service  " 8082
check_health "Reservation Svc  " 8083
check_health "API Gateway      " 8090

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "  Полезные ссылки"
echo "════════════════════════════════════════════════════════════════"
echo ""
echo "Eureka Dashboard:  http://localhost:8761"
echo "Фронтенд:          http://localhost:8000"
echo "                   (запустите: cd frontend && python3 -m http.server 8000)"
echo ""
echo "Для тестирования:  ./quick-test.sh"
echo ""
echo "════════════════════════════════════════════════════════════════"
echo "  ✅ Готово!"
echo "════════════════════════════════════════════════════════════════"

