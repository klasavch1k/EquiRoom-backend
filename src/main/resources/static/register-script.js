document.getElementById('register-form').addEventListener('submit', function(event) {
    event.preventDefault();

    const firstName = document.getElementById('firstName').value;
    const lastName = document.getElementById('lastName').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    const requestData = {
        firstName: firstName,
        lastName: lastName,
        email: email,
        password: password
    };

    fetch('/api/v1/user/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestData)
    })
        .then(response => {
            if (!response.ok) throw new Error('Ошибка регистрации: ' + response.status);
            return response.json();
        })
        .then(data => {
            // Сохраняем userId в localStorage
            localStorage.setItem('userId', data.userId);
            document.getElementById('message').textContent = data.message;
            document.getElementById('register-form').reset();
            // Редирект на страницу профиля с userId
            setTimeout(() => {
                window.location.href = `/index.html?userId=${data.userId}`;
            }, 1000);
        })
        .catch(error => {
            document.getElementById('message').textContent = error.message;
            console.error('Ошибка:', error);
        });
});