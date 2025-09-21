document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const modelId = urlParams.get('modelId');
    const myUserId = localStorage.getItem('userId');
    const token = localStorage.getItem('token');

    console.log('model ID:', modelId, 'User ID:', myUserId); // Лог для отладки

    if (!myUserId || !token) {
        alert('Вы не вошли в систему! Пожалуйста, войдите.');
        window.location.href = '/page/login.html';
        return;
    }

    if (!modelId) {
        document.getElementById('message').textContent = 'Ошибка: ID лошади не указан';
        return;
    }

    fetch(`/api/v1/users/${myUserId}/collection/${modelId}`, {
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
        .then(model => {
            console.log('Данные лошади:', model); // Лог для отладки
            document.getElementById('model-name').textContent = model.name || 'Не указано';
            document.getElementById('model-breed').textContent = model.breed || 'Не указано';
            document.getElementById('model-description').textContent = model.description || 'Нет описания';
            document.getElementById('model-masterName').textContent = model.masterName || 'Не указано';
            document.getElementById('model-releaseDate').textContent = model.releaseDate
                ? new Date(model.releaseDate).toLocaleDateString('ru-RU')
                : 'Не указано';
            document.getElementById('model-image').style.backgroundImage = `url(${model.avatar || 'placeholder-model.jpg'})`;
        })
        .catch(error => {
            console.error('Ошибка:', error.message); // Лог для отладки
            document.getElementById('message').textContent = 'Ошибка: ' + error.message;
            if (error.message.includes('Сессия истекла')) {
                localStorage.removeItem('token');
                localStorage.removeItem('userId');
                localStorage.removeItem('roles');
                window.location.href = '/page/login.html';
            }
        });
});