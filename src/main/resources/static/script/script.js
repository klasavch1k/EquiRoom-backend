// Получаем userId из URL или localStorage
const urlParams = new URLSearchParams(window.location.search);
let userId = urlParams.get('userId') || localStorage.getItem('userId') || null;
const myUserId = localStorage.getItem('userId') || null;
const roles = JSON.parse(localStorage.getItem('roles')) || [];

if (!userId) {
    alert('Вы не вошли в систему! Пожалуйста, войдите.');
    window.location.href = '/page/login.html';
}

function loadProfile() {
    const addButton = document.querySelector('.add-button');
    const editButton = document.querySelector('.edit-button');
    const logoutButton = document.querySelector('.logout-button');
    if (addButton && editButton && logoutButton) {
        if (roles.includes('ROLE_ADMIN') || userId === myUserId) {
            addButton.style.display = 'block';
            editButton.style.display = 'block';
            logoutButton.style.display = userId === myUserId ? 'block' : 'none';
        } else {
            addButton.style.display = 'none';
            editButton.style.display = 'none';
            logoutButton.style.display = 'none';
        }
    }

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
                window.location.href = '/page/login.html';
            }
        });

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
        .then(models => {
            models.sort((a, b) => new Date(b.releaseDate) - new Date(a.releaseDate));
            const grid = document.querySelector('.grid');
            grid.innerHTML = '';
            models.forEach(model => {
                const card = document.createElement('div');
                card.className = 'card';
                card.style.backgroundImage = `url(${model.avatar || 'placeholder-model.jpg'})`;
                card.onclick = () => showDetails(model.id);
                grid.appendChild(card);
            });
            document.querySelector('.stat-figurines .stat-value').textContent = models.length;
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
                window.location.href = '/page/login.html';
            }
        });
}

function showDetails(id) {
    window.location.href = `/page/model-details.html?modelId=${id}`;
}

function addFigurine() {
    if (!roles.includes('ROLE_ADMIN') && userId !== myUserId) {
        alert('Ты можешь добавлять только в свою коллекцию!');
        return;
    }
    window.location.href = '../page/add-model.html';
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
                    window.location.href = '/page/login.html';
                }
            });
    }
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('roles');
    window.location.href = '/page/login.html';
}

window.onload = loadProfile;