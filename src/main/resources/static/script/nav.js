function navigate(page) {
    const myUserId = localStorage.getItem('userId');
    if (!myUserId) {
        window.location.href = '/page/login.html';
        return;
    }
    switch (page) {
        case 'home':
            window.location.href = '/page/home.html';
            break;
        case 'search':
            window.location.href = '/page/search.html';
            break;
        case 'add':
            if (!JSON.parse(localStorage.getItem('roles') || '[]').includes('ROLE_ADMIN') && !myUserId) {
                alert('Ты можешь добавлять только в свою коллекцию!');
                window.location.href = `/page/index.html?userId=${myUserId}`;
                return;
            }
            window.location.href = '../page/add-model.html';
            break;
        case 'chat':
            window.location.href = '/page/chat.html';
            break;
        case 'profile':
            window.location.href = `/page/index.html?userId=${myUserId}`;
            break;
    }
}