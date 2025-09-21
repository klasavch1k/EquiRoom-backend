// Парсим userId из URL или берём из localStorage
const urlParams = new URLSearchParams(window.location.search);
let userId = urlParams.get('userId') || localStorage.getItem('userId') || null;
const myUserId = localStorage.getItem('userId') || null; // Текущий юзер из localStorage

if (!userId) {
    alert('Пользователь не авторизован!');
    window.location.href = '/login.html';
}

function loadProfile() {
    // Загружаем профиль
    fetch(`/api/v1/user/${userId}`, {
        headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    })
        .then(response => {
            if (!response.ok) throw new Error('Ошибка загрузки профиля');
            return response.json();
        })
        .then(profile => {
            document.querySelector('.profile-name').textContent = `${profile.firstName} ${profile.lastName}` || 'Unknown';
            document.querySelector('.profile-pic').style.backgroundImage = `url(${profile.nickName || 'placeholder-profile-pic.jpg'})`;
        })
        .catch(error => {
            console.error('Ошибка профиля:', error);
            alert('Ошибка загрузки профиля: ' + error.message);
        });

    // Загружаем коллекцию
    fetch(`/api/v1/users/${userId}/collection`, {
        headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    })
        .then(response => {
            if (!response.ok) throw new Error('Ошибка загрузки коллекции');
            return response.json();
        })
        .then(horses => {
            const grid = document.querySelector('.grid');
            grid.innerHTML = '';
            horses.forEach(horse => {
                const card = document.createElement('div');
                card.className = 'card';
                card.style.backgroundImage = `url(${horse.avatar || 'placeholder-horse.jpg'})`;
                card.onclick = () => showDetails(horse.id);
                grid.appendChild(card);
            });
            // Обновляем статистику фигурок
            document.querySelector('.stat-figurines .stat-value').textContent = horses.length;
            document.querySelector('.stat-collecting .stat-value').textContent = 0;
            document.querySelector('.stat-members .stat-value').textContent = 0;
        })
        .catch(error => console.error('Ошибка коллекции:', error));
}

window.onload = loadProfile;

// Добавление фигурки
function addFigurine() {
    if (userId !== myUserId) {
        alert('Ты можешь добавлять только в свою коллекцию!');
        return;
    }
    const name = prompt('Имя лошади:');
    const breed = prompt('Порода:');
    const description = prompt('Описание:');
    const masterName = prompt('Имя мастера:');
    const mediaLink = prompt('Ссылка на аватар:');

    if (name && breed && mediaLink) {
        const request = { name, breed, description, masterName, media: [{ link: mediaLink }] };
        fetch(`/api/v1/users/${userId}/collection/addHorse`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify(request)
        })
            .then(response => {
                if (!response.ok) throw new Error('Ошибка добавления');
                loadProfile();
                alert('Фигурка добавлена!');
            })
            .catch(error => alert('Ошибка: ' + error.message));
    }
}

function editDetails() {
    if (userId !== myUserId) {
        alert('Редактировать можно только свой профиль!');
        return;
    }
    const firstName = prompt('Новое имя:');
    const lastName = prompt('Новая фамилия:');
    const nickName = prompt('Новый никнейм (для аватара):');
    if (firstName || lastName || nickName) {
        const dto = {};
        if (firstName) dto.firstName = firstName;
        if (lastName) dto.lastName = lastName;
        if (nickName) dto.nickName = nickName;
        fetch(`/api/v1/user`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify(dto)
        })
            .then(response => {
                if (!response.ok) throw new Error('Ошибка обновления');
                loadProfile();
                alert('Профиль обновлён!');
            })
            .catch(error => alert('Ошибка: ' + error.message));
    }
}

function showDetails(id) {
    alert(`Детали для ID ${id} пока не реализованы.`);
}