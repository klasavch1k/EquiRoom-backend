document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const horseId = urlParams.get('horseId');
    const myUserId = localStorage.getItem('userId');
    const token = localStorage.getItem('token');

    console.log('Horse ID:', horseId, 'User ID:', myUserId); // Лог для отладки

    if (!myUserId || !token) {
        alert('Вы не вошли в систему! Пожалуйста, войдите.');
        window.location.href = '/login.html';
        return;
    }

    if (!horseId) {
        document.getElementById('message').textContent = 'Ошибка: ID лошади не указан';
        return;
    }

    fetch(`api/v1/users/{id}/collection/${horseId}`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
        .then(response => {
            console.log('Ответ сервера:', response.status, response.statusText); // Лог для отладки
            if (response.status === 401) {
                throw new Error('Сессия истекла. Пожалуйста, войдите снова.');
            }
            if (response.status === 403) {
                throw new Error('Вы не можете просматривать эту фигурку.');
            }
            if (!response.ok) {
                throw new Error(`Ошибка загрузки деталей: ${response.statusText}`);
            }
            return response.json();
        })
        .then(horse => {
            console.log('Данные лошади:', horse); // Лог для отладки
            document.getElementById('horse-name').textContent = horse.name || 'Не указано';
            document.getElementById('horse-breed').textContent = horse.breed || 'Не указано';
            document.getElementById('horse-description').textContent = horse.description || 'Нет описания';
            document.getElementById('horse-masterName').textContent = horse.masterName || 'Не указано';
            document.getElementById('horse-releaseDate').textContent = horse.releaseDate
                ? new Date(horse.releaseDate).toLocaleDateString('ru-RU')
                : 'Не указано';
            document.getElementById('horse-image').style.backgroundImage = `url(${horse.avatar || 'placeholder-horse.jpg'})`;
        })
        .catch(error => {
            console.error('Ошибка:', error.message); // Лог для отладки
            document.getElementById('message').textContent = 'Ошибка: ' + error.message;
            if (error.message.includes('Сессия истекла')) {
                localStorage.removeItem('token');
                localStorage.removeItem('userId');
                localStorage.removeItem('roles');
                window.location.href = '/login.html';
            }
        });
});