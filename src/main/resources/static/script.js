// Получаем userId из URL или localStorage
const urlParams = new URLSearchParams(window.location.search);
let userId = urlParams.get('userId') || localStorage.getItem('userId') || null;
const myUserId = localStorage.getItem('userId') || null;
const roles = JSON.parse(localStorage.getItem('roles')) || [];

if (!userId) {
    alert('Вы не вошли в систему! Пожалуйста, войдите.');
    window.location.href = '/login.html';
}

function loadProfile() {
    // Показываем или скрываем кнопки в зависимости от роли и страницы
    const addButton = document.querySelector('.add-button');
    const editButton = document.querySelector('.edit-button');
    const logoutButton = document.querySelector('.logout-button');
    if (addButton && editButton && logoutButton) {
        if (roles.includes('ROLE_ADMIN') || userId === myUserId) {
            addButton.style.display = 'block';
            editButton.style.display = 'block';
            logoutButton.style.display = userId === myUserId ? 'block' : 'none'; // Показываем "Выйти" только на своей странице
        } else {
            addButton.style.display = 'none';
            editButton.style.display = 'none';
            logoutButton.style.display = 'none';
        }
    }

    // Загружаем данные профиля
    fetch(`/api/v1/user/${userId}`, {
        headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    })
        .then(response => {
            if (response.status === 401) {
                throw new Error('Сессия истекла. Пожалуйста, войдите снова.');
            }
            if (!response.ok) throw new Error('Не удалось загрузить профиль');
            return response.json();
        })
        .then(profile => {
            document.querySelector('.profile-name').textContent = `${profile.firstName} ${profile.lastName}` || 'Неизвестно';
            document.querySelector('.profile-pic').style.backgroundImage = `url(${profile.nickName || 'placeholder-profile-pic.jpg'})`;
        })
        .catch(error => {
            console.error('Ошибка загрузки профиля:', error);
            alert(error.message);
            if (error.message.includes('Сессия истекла')) {
                localStorage.removeItem('token');
                localStorage.removeItem('userId');
                localStorage.removeItem('roles');
                window.location.href = '/login.html';
            }
        });

    // Загружаем коллекцию фигурок
    fetch(`/api/v1/users/${userId}/collection`, {
        headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    })
        .then(response => {
            if (response.status === 401) {
                throw new Error('Сессия истекла. Пожалуйста, войдите снова.');
            }
            if (!response.ok) throw new Error('Не удалось загрузить коллекцию');
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
            document.querySelector('.stat-figurines .stat-value').textContent = horses.length;
            document.querySelector('.stat-collecting .stat-value').textContent = 0;
            document.querySelector('.stat-members .stat-value').textContent = 0;
        })
        .catch(error => {
            console.error('Ошибка загрузки коллекции:', error);
            alert(error.message);
            if (error.message.includes('Сессия истекла')) {
                localStorage.removeItem('token');
                localStorage.removeItem('userId');
                localStorage.removeItem('roles');
                window.location.href = '/login.html';
            }
        });
}

function showDetails(id) {
    fetch(`/api/v1/horses/${id}`, {
        headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    })
        .then(response => {
            if (response.status === 401) {
                throw new Error('Сессия истекла. Пожалуйста, войдите снова.');
            }
            if (response.status === 403) {
                throw new Error('Вы не можете просматривать эту фигурку.');
            }
            if (!response.ok) throw new Error('Не удалось загрузить детали фигурки');
            return response.json();
        })
        .then(horse => {
            const details = `Имя: ${horse.name}\nПорода: ${horse.breed}\nОписание: ${horse.description || 'Нет'}\nМастер: ${horse.masterName || 'Не указан'}\nАватар: ${horse.avatar || 'Нет'}`;
            alert(details);
        })
        .catch(error => {
            console.error('Ошибка загрузки деталей:', error);
            alert(error.message);
            if (error.message.includes('Сессия истекла')) {
                localStorage.removeItem('token');
                localStorage.removeItem('userId');
                localStorage.removeItem('roles');
                window.location.href = '/login.html';
            }
        });
}

function addFigurine() {
    if (!roles.includes('ROLE_ADMIN') && userId !== myUserId) {
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
                if (response.status === 401) {
                    throw new Error('Сессия истекла. Пожалуйста, войдите снова.');
                }
                if (response.status === 403) {
                    throw new Error('Ты можешь добавлять только в свою коллекцию!');
                }
                if (!response.ok) throw new Error('Ошибка добавления фигурки');
                loadProfile();
                alert('Фигурка добавлена!');
            })
            .catch(error => {
                alert('Ошибка: ' + error.message);
                if (error.message.includes('Сессия истекла')) {
                    localStorage.removeItem('token');
                    localStorage.removeItem('userId');
                    localStorage.removeItem('roles');
                    window.location.href = '/login.html';
                }
            });
    }
}

function editDetails() {
    if (!roles.includes('ROLE_ADMIN') && userId !== myUserId) {
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
                if (response.status === 401) {
                    throw new Error('Сессия истекла. Пожалуйста, войдите снова.');
                }
                if (response.status === 403) {
                    throw new Error('Редактировать можно только свой профиль!');
                }
                if (!response.ok) throw new Error('Ошибка обновления профиля');
                loadProfile();
                alert('Профиль обновлён!');
            })
            .catch(error => {
                alert('Ошибка: ' + error.message);
                if (error.message.includes('Сессия истекла')) {
                    localStorage.removeItem('token');
                    localStorage.removeItem('userId');
                    localStorage.removeItem('roles');
                    window.location.href = '/login.html';
                }
            });
    }
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('roles');
    window.location.href = '/login.html';
}

window.onload = loadProfile;