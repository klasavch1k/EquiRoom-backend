document.getElementById('login-form').addEventListener('submit', function(event) {
    event.preventDefault();

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    const requestData = {
        email: email,
        password: password
    };

    fetch('/api/v1/user/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestData)
    })
        .then(response => {
            if (!response.ok) throw new Error('Ошибка входа: ' + response.status);
            return response.json();
        })
        .then(data => {
            // Сохраняем токен и userId в localStorage
            localStorage.setItem('token', data.token);
            localStorage.setItem('userId', data.userId);
            document.getElementById('message').textContent = 'Вход успешен!';
            // Редирект на страницу профиля с userId
            setTimeout(() => {
                window.location.href = `/page/index.html?userId=${data.userId}`;
            }, 1000);
        })
        .catch(error => {
            document.getElementById('message').textContent = error.message;
            console.error('Ошибка:', error);
        });
});