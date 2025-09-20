// Обработка формы регистрации
document.getElementById('register-form').addEventListener('submit', function(event) {
    event.preventDefault(); // Предотвращаем перезагрузку страницы

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
            return response.text();
        })
        .then(message => {
            document.getElementById('message').textContent = message; // Показываем сообщение от сервера
            document.getElementById('register-form').reset(); // Очищаем форму
            setTimeout(() => {
                window.location.href = '/index.html?userId=1'; // Перенаправляем на профиль (временный ID)
            }, 1000);
        })
        .catch(error => {
            document.getElementById('message').textContent = error.message;
            console.error('Ошибка:', error);
        });
});