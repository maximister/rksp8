# ☁️ Деплой на виртуалку (простой способ)

## 📦 Шаг 1: Push образов в Docker Hub (на локальной машине)

```bash
cd /Users/maximister/dev/IdeaProjects/rksp8

# Логин в Docker Hub
docker login

# Установите ваш Docker Hub username
export DOCKER_USER="ваш-dockerhub-username"

# Тегируем локальные образы
docker tag rksp8-eureka-server:latest $DOCKER_USER/rksp8-eureka-server:latest
docker tag rksp8-config-server:latest $DOCKER_USER/rksp8-config-server:latest
docker tag rksp8-auth-server:latest $DOCKER_USER/rksp8-auth-server:latest
docker tag rksp8-parking-service:latest $DOCKER_USER/rksp8-parking-service:latest
docker tag rksp8-vehicle-service:latest $DOCKER_USER/rksp8-vehicle-service:latest
docker tag rksp8-reservation-service:latest $DOCKER_USER/rksp8-reservation-service:latest
docker tag rksp8-api-gateway:latest $DOCKER_USER/rksp8-api-gateway:latest

# Push в Docker Hub
docker push $DOCKER_USER/rksp8-eureka-server:latest
docker push $DOCKER_USER/rksp8-config-server:latest
docker push $DOCKER_USER/rksp8-auth-server:latest
docker push $DOCKER_USER/rksp8-parking-service:latest
docker push $DOCKER_USER/rksp8-vehicle-service:latest
docker push $DOCKER_USER/rksp8-reservation-service:latest
docker push $DOCKER_USER/rksp8-api-gateway:latest

# Создаем архив для виртуалки
tar -czf deploy.tar.gz config-repo/ frontend/ docker-compose-cloud.yml

# Копируем на виртуалку
scp deploy.tar.gz username@IP_ВИРТУАЛКИ:~/
```

---

## 🖥️ Шаг 2: Настройка виртуалки

```bash
# Подключаемся к виртуалке
ssh username@IP_ВИРТУАЛКИ

# Создаем директорию
mkdir -p ~/dev/app
cd ~/dev/app

# Распаковываем
tar -xzf ~/deploy.tar.gz

# Устанавливаем Docker (если еще не установлен)
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# ВАЖНО: Выходим и заходим снова для применения прав
exit
# (Заходим снова по SSH)

cd ~/dev/app

# Устанавливаем поддержку ARM64 образов
docker run --privileged --rm tonistiigi/binfmt --install all
```

---

## 🚀 Шаг 3: Запуск

```bash
cd ~/dev/app

# Устанавливаем переменную окружения (ваш Docker Hub username)
export DOCKER_USER="ваш-dockerhub-username"

# Запускаем всё!
docker compose -f docker-compose-cloud.yml up -d

# Смотрим логи
docker compose -f docker-compose-cloud.yml logs -f

# Проверяем статус
docker compose -f docker-compose-cloud.yml ps
```

---

## 🌐 Шаг 4: Настройка Frontend

```bash
# Устанавливаем Nginx
sudo apt update
sudo apt install nginx -y

# Копируем frontend
sudo cp -r frontend/* /var/www/html/

# Получаем внешний IP виртуалки
export VM_IP=$(curl -s ifconfig.me)
echo "Ваш IP: $VM_IP"

# Правим frontend для работы с виртуалкой
sudo sed -i "s|http://localhost:9000|http://$VM_IP:9000|g" /var/www/html/app.js
sudo sed -i "s|http://localhost:8090|http://$VM_IP:8090|g" /var/www/html/app.js

# Запускаем Nginx
sudo systemctl enable nginx
sudo systemctl start nginx
```

---

## 🔥 Открываем порты (Firewall)

**Для Yandex Cloud:**
- Откройте порты в веб-интерфейсе: 80, 8090, 9000, 8761

**Для UFW (Ubuntu):**
```bash
sudo ufw allow 80/tcp       # Frontend
sudo ufw allow 8090/tcp     # API Gateway
sudo ufw allow 9000/tcp     # Auth Server
sudo ufw allow 8761/tcp     # Eureka (опционально)
sudo ufw enable
```

---

## ✅ Готово!

Открывайте в браузере:
- **Frontend**: `http://ваш-IP/`
- **Eureka**: `http://ваш-IP:8761/`
- **API Gateway**: `http://ваш-IP:8090/`

---

## 🔄 Полезные команды

```bash
# Остановить всё
docker compose -f docker-compose-cloud.yml down

# Перезапустить
docker compose -f docker-compose-cloud.yml restart

# Логи конкретного сервиса
docker compose -f docker-compose-cloud.yml logs -f auth-server

# Удалить всё включая volumes
docker compose -f docker-compose-cloud.yml down -v

# Обновить образы из Docker Hub
docker compose -f docker-compose-cloud.yml pull
docker compose -f docker-compose-cloud.yml up -d
```

---

## 📝 Если что-то не работает

1. **Проверьте статус контейнеров:**
```bash
docker compose -f docker-compose-cloud.yml ps
```

2. **Проверьте логи:**
```bash
docker compose -f docker-compose-cloud.yml logs
```

3. **Проверьте что образы скачались:**
```bash
docker images | grep rksp8
```

4. **Убедитесь что используете правильный DOCKER_USER:**
```bash
echo $DOCKER_USER
```

