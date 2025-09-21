function navigate(page) {
    const myUserId = localStorage.getItem('userId');
    if (!myUserId) {
        window.location.href = '/login.html';
        return;
    }
    switch (page) {
        case 'home':
            window.location.href = '/home.html';
            break;
        case 'search':
            window.location.href = '/search.html';
            break;
        case 'add':
            if (!JSON.parse(localStorage.getItem('roles') || '[]').includes('ROLE_ADMIN') && !myUserId) {
                alert('Ты можешь добавлять только в свою коллекцию!');
                window.location.href = `/index.html?userId=${myUserId}`;
                return;
            }
            window.location.href = '/add-horse.html';
            break;
        case 'chat':
            window.location.href = '/chat.html';
            break;
        case 'profile':
            window.location.href = `/index.html?userId=${myUserId}`;
            break;
    }
}