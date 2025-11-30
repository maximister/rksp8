#!/bin/bash

echo "════════════════════════════════════════════════════════"
echo "🔨 Пересборка API Gateway"
echo "════════════════════════════════════════════════════════"

cd /Users/maximister/dev/IdeaProjects/rksp8

# Устанавливаем JAVA_HOME
export JAVA_HOME=/Users/maximister/Library/Java/JavaVirtualMachines/openjdk-22.0.1/Contents/Home

echo "📦 Сборка api-gateway..."
/usr/local/bin/mvn clean package -pl api-gateway -am -DskipTests -q

if [ $? -eq 0 ]; then
    echo "✅ Сборка успешна!"
    echo ""
    echo "════════════════════════════════════════════════════════"
    echo "⚠️  ТЕПЕРЬ:"
    echo "════════════════════════════════════════════════════════"
    echo ""
    echo "1. Остановите API Gateway в IntelliJ IDEA"
    echo "2. Запустите API Gateway заново"
    echo "3. Подождите 20 секунд"
    echo "4. Обновите страницу фронтенда (F5)"
    echo ""
    echo "JAR файл: api-gateway/target/api-gateway-1.0-SNAPSHOT.jar"
    echo ""
else
    echo "❌ Ошибка сборки!"
    exit 1
fi

