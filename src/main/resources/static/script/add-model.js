document.getElementById('add-model-form').addEventListener('submit', function(event) {
    event.preventDefault();

    const myUserId = localStorage.getItem('userId');
    const roles = JSON.parse(localStorage.getItem('roles')) || [];

    console.log('User ID:', myUserId, 'Roles:', roles); // Лог для отладки

    if (!myUserId) {
        alert('Вы не вошли в систему! Пожалуйста, войдите.');
        window.location.href = '/page/login.html';
        return;
    }

    const name = document.getElementById('name').value;
    const breed = document.getElementById('breed').value;
    const description = document.getElementById('description').value;
    const masterName = document.getElementById('masterName').value;
    const mediaLink = document.getElementById('mediaLink').value;

    const request = {
        name,
        breed,
        description: description || null,
        masterName: masterName || null,
        media: [{ link: mediaLink }]
    };

    console.log('Отправка данных:', request); // Лог для отладки

    fetch(`/api/v1/users/${myUserId}/collection/addModel`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(request)
    })
        .then(response => {
            console.log('Ответ сервера:', response.status, response.statusText); // Лог для отладки
            if (response.status === 401) {
                throw new Error('Сессия истекла. Пожалуйста, войдите снова.');
            }
            if (response.status === 403) {
                throw new Error('Ты можешь добавлять только в свою коллекцию!');
            }
            if (!response.ok) {
                throw new Error(`Ошибка добавления фигурки: ${response.statusText}`);
            }
            // Не парсим ответ, так как он пустой
            console.log('Ответ успешен, обработка...'); // Лог для отладки
            return;
        })
        .then(() => {
            console.log('Фигурка добавлена, редирект...'); // Лог для отладки
            document.getElementById('message').textContent = 'Фигурка добавлена!';
            document.getElementById('add-model-form').reset();
            console.log('Редирект на профиль:', `/page/index.html?userId=${myUserId}`); // Лог для отладки
            setTimeout(() => {
                window.location.href = `/page/index.html?userId=${myUserId}`;
            }, 1000);
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