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
            // Сохраняем токен в localStorage
            localStorage.setItem('token', data.token);
            document.getElementById('message').textContent = 'Вход успешен!';

            // Временный redirect (пока хардкод userId)
            setTimeout(() => {
                window.location.href = '/index.html?userId=1';
            }, 1000);
        })
        .catch(error => {
            document.getElementById('message').textContent = error.message;
            console.error('Ошибка:', error);
        });
});