// API Configuration
const API_BASE_URL = 'http://localhost:8090/api';
const AUTH_URL = 'http://localhost:9000/oauth2/token';
const CLIENT_ID = 'parking-system-client';
const CLIENT_SECRET = 'secret';

// State
let currentToken = null;
let currentRole = null;
let currentUsername = null;
let parkingSpots = [];
let vehicles = [];
let reservations = [];

// Authentication
async function login(role) {
    try {
        const scope = 'read write';
        
        const formData = new URLSearchParams();
        formData.append('grant_type', 'client_credentials');
        formData.append('scope', scope);

        const response = await fetch(AUTH_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Authorization': 'Basic ' + btoa(`${CLIENT_ID}:${CLIENT_SECRET}`)
            },
            body: formData
        });

        if (!response.ok) {
            throw new Error('Ошибка авторизации');
        }

        const data = await response.json();
        currentToken = data.access_token;
        currentRole = role;
        currentUsername = role === 'admin' ? 'Администратор' : 'Пользователь';

        // Update UI
        document.getElementById('loginSection').classList.add('hidden');
        document.getElementById('appSection').classList.remove('hidden');
        
        const authStatus = document.getElementById('authStatus');
        authStatus.innerHTML = `
            <span class="status-indicator online"></span>
            <span>${currentUsername}</span>
        `;

        // Скрываем управление парковочными местами для обычных пользователей
        if (role === 'user') {
            const addParkingBtn = document.getElementById('addParkingBtn');
            if (addParkingBtn) addParkingBtn.style.display = 'none';
        }

        showToast(`Успешный вход как ${currentUsername}!`, 'success');

        // Load initial data
        await loadAllData();
        showToast('Успешный вход в систему', 'success');
    } catch (error) {
        console.error('Login error:', error);
        showToast('Ошибка входа: ' + error.message, 'error');
    }
}

function logout() {
    currentToken = null;
    currentRole = null;
    currentUsername = null;
    
    document.getElementById('loginSection').classList.remove('hidden');
    document.getElementById('appSection').classList.add('hidden');
    document.getElementById('authStatus').innerHTML = `
        <span class="status-indicator offline"></span>
        <span>Не авторизован</span>
    `;
    showToast('Вы вышли из системы', 'success');
}

// API Calls
async function apiCall(endpoint, method = 'GET', body = null) {
    const options = {
        method,
        headers: {
            'Authorization': `Bearer ${currentToken}`,
            'Content-Type': 'application/json'
        }
    };

    if (body) {
        options.body = JSON.stringify(body);
    }

    const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
    
    if (!response.ok) {
        if (response.status === 403) {
            throw new Error('Недостаточно прав доступа');
        }
        if (response.status === 405) {
            throw new Error('Метод не разрешен');
        }
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    // Для DELETE запросов может не быть тела ответа
    if (method === 'DELETE' && response.status === 204) {
        return null;
    }

    // Проверяем, есть ли содержимое для парсинга
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        return response.json();
    }
    
    return null;
}

// Load Data
async function loadAllData() {
    await loadParkingSpots();
    await loadVehicles();
    await loadReservations();
}

async function loadParkingSpots() {
    try {
        parkingSpots = await apiCall('/spots');
        renderParkingSpots();
    } catch (error) {
        console.error('Error loading parking spots:', error);
        showToast('Ошибка загрузки парковочных мест: ' + error.message, 'error');
    }
}

async function loadVehicles() {
    try {
        vehicles = await apiCall('/vehicles');
        renderVehicles();
    } catch (error) {
        console.error('Error loading vehicles:', error);
        showToast('Ошибка загрузки автомобилей: ' + error.message, 'error');
    }
}

async function loadReservations() {
    try {
        reservations = await apiCall('/reservations');
        renderReservations();
    } catch (error) {
        console.error('Error loading reservations:', error);
        showToast('Ошибка загрузки бронирований: ' + error.message, 'error');
    }
}

// Render Functions
function renderParkingSpots() {
    const tbody = document.getElementById('parkingTableBody');
    
    if (parkingSpots.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty">Нет парковочных мест</td></tr>';
        return;
    }

    tbody.innerHTML = parkingSpots.map(spot => `
        <tr>
            <td>${spot.id}</td>
            <td><strong>${spot.number}</strong></td>
            <td>${spot.floor}</td>
            <td><span class="status-badge status-${spot.status.toLowerCase()}">${getStatusText(spot.status)}</span></td>
            <td>
                ${currentRole === 'admin' ? `
                    <button class="btn btn-sm btn-danger" onclick="deleteParkingSpot(${spot.id})">
                        Удалить
                    </button>
                ` : '-'}
            </td>
        </tr>
    `).join('');
}

function renderVehicles() {
    const tbody = document.getElementById('vehiclesTableBody');
    
    if (vehicles.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="empty">Нет автомобилей</td></tr>';
        return;
    }

    tbody.innerHTML = vehicles.map(vehicle => `
        <tr>
            <td>${vehicle.id}</td>
            <td><strong>${vehicle.licensePlate}</strong></td>
            <td>${vehicle.model}</td>
            <td>${vehicle.color}</td>
            <td>${vehicle.ownerName}</td>
            <td>
                ${currentRole === 'admin' ? `
                    <button class="btn btn-sm btn-danger" onclick="deleteVehicle(${vehicle.id})">
                        Удалить
                    </button>
                ` : '-'}
            </td>
        </tr>
    `).join('');
}

function renderReservations() {
    const tbody = document.getElementById('reservationsTableBody');
    
    if (reservations.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty">Нет бронирований</td></tr>';
        return;
    }

    tbody.innerHTML = reservations.map(reservation => {
        const spot = parkingSpots.find(s => s.id === reservation.parkingSpotId);
        const vehicle = vehicles.find(v => v.id === reservation.vehicleId);
        
        return `
            <tr>
                <td>${reservation.id}</td>
                <td>${spot ? spot.number : 'N/A'}</td>
                <td>${vehicle ? vehicle.licensePlate : 'N/A'}</td>
                <td>${formatDateTime(reservation.startTime)}</td>
                <td>${formatDateTime(reservation.endTime)}</td>
                <td><span class="status-badge status-${reservation.status.toLowerCase()}">${getReservationStatusText(reservation.status)}</span></td>
                <td>
                    ${currentRole === 'admin' && reservation.status === 'ACTIVE' ? `
                        <button class="btn btn-sm btn-danger" onclick="cancelReservation(${reservation.id})">
                            Отменить
                        </button>
                    ` : '-'}
                </td>
            </tr>
        `;
    }).join('');
}

// CRUD Operations - Parking Spots
function showAddParkingModal() {
    document.getElementById('addParkingModal').classList.remove('hidden');
    document.getElementById('parkingNumber').value = '';
    document.getElementById('parkingFloor').value = '';
    document.getElementById('parkingStatus').value = 'FREE';
}

async function addParkingSpot() {
    try {
        const number = document.getElementById('parkingNumber').value;
        const floor = parseInt(document.getElementById('parkingFloor').value);
        const status = document.getElementById('parkingStatus').value;

        if (!number || !floor) {
            showToast('Заполните все поля', 'error');
            return;
        }

        await apiCall('/spots', 'POST', { number, floor, status });
        
        closeModal('addParkingModal');
        await loadParkingSpots();
        showToast('Парковочное место добавлено', 'success');
    } catch (error) {
        console.error('Error adding parking spot:', error);
        showToast('Ошибка: ' + error.message, 'error');
    }
}

async function deleteParkingSpot(id) {
    if (!confirm('Вы уверены, что хотите удалить это парковочное место?')) {
        return;
    }

    try {
        await apiCall(`/spots/${id}`, 'DELETE');
        await loadParkingSpots();
        showToast('Парковочное место удалено', 'success');
    } catch (error) {
        console.error('Error deleting parking spot:', error);
        showToast('Ошибка: ' + error.message, 'error');
    }
}

// CRUD Operations - Vehicles
function showAddVehicleModal() {
    document.getElementById('addVehicleModal').classList.remove('hidden');
    document.getElementById('vehicleLicense').value = '';
    document.getElementById('vehicleModel').value = '';
    document.getElementById('vehicleColor').value = '';
    document.getElementById('vehicleOwner').value = '';
}

async function addVehicle() {
    try {
        const licensePlate = document.getElementById('vehicleLicense').value;
        const model = document.getElementById('vehicleModel').value;
        const color = document.getElementById('vehicleColor').value;
        const ownerName = document.getElementById('vehicleOwner').value;

        if (!licensePlate || !model || !color || !ownerName) {
            showToast('Заполните все поля', 'error');
            return;
        }

        await apiCall('/vehicles', 'POST', { licensePlate, model, color, ownerName });
        
        closeModal('addVehicleModal');
        await loadVehicles();
        showToast('Автомобиль добавлен', 'success');
    } catch (error) {
        console.error('Error adding vehicle:', error);
        showToast('Ошибка: ' + error.message, 'error');
    }
}

async function deleteVehicle(id) {
    if (!confirm('Вы уверены, что хотите удалить этот автомобиль?')) {
        return;
    }

    try {
        await apiCall(`/vehicles/${id}`, 'DELETE');
        await loadVehicles();
        showToast('Автомобиль удален', 'success');
    } catch (error) {
        console.error('Error deleting vehicle:', error);
        showToast('Ошибка: ' + error.message, 'error');
    }
}

// CRUD Operations - Reservations
async function showAddReservationModal() {
    document.getElementById('addReservationModal').classList.remove('hidden');
    
    // Populate parking spots dropdown (only FREE spots)
    const spotSelect = document.getElementById('reservationSpot');
    const freeSpots = parkingSpots.filter(s => s.status === 'FREE');
    spotSelect.innerHTML = '<option value="">Выберите место...</option>' + 
        freeSpots.map(spot => `<option value="${spot.id}">${spot.number} (Этаж ${spot.floor})</option>`).join('');
    
    // Populate vehicles dropdown
    const vehicleSelect = document.getElementById('reservationVehicle');
    vehicleSelect.innerHTML = '<option value="">Выберите автомобиль...</option>' + 
        vehicles.map(v => `<option value="${v.id}">${v.licensePlate} - ${v.model}</option>`).join('');
    
    // Set default times
    const now = new Date();
    const later = new Date(now.getTime() + 2 * 60 * 60 * 1000); // +2 hours
    
    document.getElementById('reservationStart').value = formatDateTimeLocal(now);
    document.getElementById('reservationEnd').value = formatDateTimeLocal(later);
}

async function addReservation() {
    try {
        const parkingSpotId = parseInt(document.getElementById('reservationSpot').value);
        const vehicleId = parseInt(document.getElementById('reservationVehicle').value);
        const startTime = document.getElementById('reservationStart').value;
        const endTime = document.getElementById('reservationEnd').value;

        if (!parkingSpotId || !vehicleId || !startTime || !endTime) {
            showToast('Заполните все поля', 'error');
            return;
        }

        await apiCall('/reservations', 'POST', {
            parkingSpotId,
            vehicleId,
            startTime: startTime + ':00', // Add seconds
            endTime: endTime + ':00'
        });
        
        closeModal('addReservationModal');
        await loadAllData(); // Reload all to update parking spot status
        showToast('Бронирование создано', 'success');
    } catch (error) {
        console.error('Error creating reservation:', error);
        showToast('Ошибка: ' + error.message, 'error');
    }
}

async function cancelReservation(id) {
    if (!confirm('Вы уверены, что хотите отменить это бронирование?')) {
        return;
    }

    try {
        await apiCall(`/reservations/${id}/cancel`, 'PATCH');
        await loadAllData(); // Reload all to update parking spot status
        showToast('Бронирование отменено', 'success');
    } catch (error) {
        console.error('Error cancelling reservation:', error);
        showToast('Ошибка: ' + error.message, 'error');
    }
}

// UI Helpers
function switchTab(tabName) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(tab => tab.classList.add('hidden'));
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    
    // Show selected tab
    document.getElementById(`${tabName}Tab`).classList.remove('hidden');
    event.target.classList.add('active');
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.add('hidden');
}

function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');
    
    toastMessage.textContent = message;
    toast.className = `toast ${type}`;
    toast.classList.remove('hidden');
    
    setTimeout(() => {
        toast.classList.add('hidden');
    }, 3000);
}

function getStatusText(status) {
    const statusMap = {
        'FREE': 'Свободно',
        'OCCUPIED': 'Занято',
        'RESERVED': 'Зарезервировано'
    };
    return statusMap[status] || status;
}

function getReservationStatusText(status) {
    const statusMap = {
        'ACTIVE': 'Активно',
        'CANCELLED': 'Отменено',
        'COMPLETED': 'Завершено'
    };
    return statusMap[status] || status;
}

function formatDateTime(dateTimeString) {
    if (!dateTimeString) return 'N/A';
    const date = new Date(dateTimeString);
    return date.toLocaleString('ru-RU', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatDateTimeLocal(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
}


// Close modal when clicking outside
window.onclick = function(event) {
    if (event.target.classList.contains('modal')) {
        event.target.classList.add('hidden');
    }
}

