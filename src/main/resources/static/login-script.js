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
            if (!response.ok) throw new Error('Ошибка входа: ' + response.statusText);
            return response.json();
        })
        .then(data => {
            localStorage.setItem('token', data.token);
            localStorage.setItem('userId', data.userId);
            localStorage.setItem('roles', JSON.stringify(data.roles)); // Сохраняем роли
            document.getElementById('message').textContent = 'Вход успешен!';
            setTimeout(() => {
                window.location.href = `/index.html?userId=${data.userId}`;
            }, 1000);
        })
        .catch(error => {
            document.getElementById('message').textContent = error.message;
            console.error('Ошибка:', error);
        });
});