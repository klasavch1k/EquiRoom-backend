// Загружаем список всех пользователей при старте
let allUsers = []; // Храним полный список для фильтра

function loadUsers() {
    fetch('/api/v1/user')
        .then(response => {
            if (!response.ok) throw new Error('Ошибка загрузки пользователей: ' + response.status);
            return response.json();
        })
        .then(users => {
            console.log('Полученные пользователи:', users); // Лог для отладки
            allUsers = users; // Сохраняем полный список
            displayUsers(allUsers); // Отображаем всех
        })
        .catch(error => {
            console.error('Ошибка:', error); // Лог ошибки
            alert('Не удалось загрузить пользователей: ' + error.message);
        });
}

// Отображаем пользователей в сетке
function displayUsers(users) {
    const grid = document.querySelector('.user-grid');
    grid.innerHTML = '';
    if (users.length === 0) {
        grid.innerHTML = '<p>Нет пользователей для отображения.</p>';
        return;
    }
    users.forEach(user => {
        const card = document.createElement('div');
        card.className = 'user-card';
        card.innerHTML = `
            <div class="user-avatar" style="background-image: url('${user.nickName || 'placeholder-profile-pic.jpg'}');"></div>
            <div class="user-info">
                <div class="user-name">${user.firstName || 'Без имени'} ${user.lastName || ''}</div>
                <div>${user.email || 'Без email'}</div>
            </div>
        `;
        card.onclick = () => {
            window.location.href = `/index.html?userId=${user.id}`;
        };
        grid.appendChild(card);
    });
}

// Функция поиска (фильтр на фронте по имени или email)
function searchUsers() {
    const query = document.getElementById('search-input').value.toLowerCase();
    const filteredUsers = allUsers.filter(user =>
        (user.firstName?.toLowerCase().includes(query)) ||
        (user.lastName?.toLowerCase().includes(query)) ||
        (user.email?.toLowerCase().includes(query))
    );
    displayUsers(filteredUsers);
}

window.onload = loadUsers;