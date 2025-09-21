function navigate(page) {
    const userId = localStorage.getItem('userId');
    if (!userId) {
        alert('Пожалуйста, войдите в систему');
        window.location.href = '/login.html';
        return;
    }

    switch (page) {
        case 'home':
            window.location.href = `/index.html?userId=${userId}`;
            break;
        case 'search':
            window.location.href = '/search.html';
            break;
        case 'add':
            // Для текущего пользователя открываем добавление фигурки
            if (window.location.pathname.includes('index.html')) {
                addFigurine(); // Вызываем функцию из script.js
            } else {
                alert('Перейдите на свой профиль для добавления фигурки');
                window.location.href = `/index.html?userId=${userId}`;
            }
            break;
        case 'chat':
            alert('Чат пока не реализован');
            // В будущем: window.location.href = '/chat.html';
            break;
        case 'profile':
            window.location.href = `/index.html?userId=${userId}`;
            break;
        default:
            console.error('Неизвестная страница:', page);
    }
}