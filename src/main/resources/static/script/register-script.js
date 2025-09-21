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
            if (!response.ok) throw new Error('Ошибка регистрации: ' + response.statusText);
            return response.json();
        })
        .then(data => {
            // После регистрации сразу логинимся
            fetch('/api/v1/user/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: requestData.email, password: requestData.password })
            })
                .then(response => {
                    if (!response.ok) throw new Error('Ошибка входа после регистрации');
                    return response.json();
                })
                .then(loginData => {
                    localStorage.setItem('token', loginData.token);
                    localStorage.setItem('userId', loginData.userId);
                    localStorage.setItem('roles', JSON.stringify(loginData.roles));
                    document.getElementById('message').textContent = data.message;
                    document.getElementById('register-form').reset();
                    setTimeout(() => {
                        window.location.href = `/page/index.html?userId=${loginData.userId}`;
                    }, 1000);
                });
        })
        .catch(error => {
            document.getElementById('message').textContent = error.message;
            console.error('Ошибка:', error);
        });
});