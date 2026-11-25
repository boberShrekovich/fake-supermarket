import './style.css';
import { state, clearAuth } from './state';
import { syncCartCount } from './api';
import { renderCatalog } from './pages/catalog';
import { renderAuth } from './pages/auth';
import { renderCart } from './pages/cart';
import { renderOrders } from './pages/orders';
import { renderAdmin } from './pages/admin';

function renderHeader(): void {
    const header = document.getElementById('app-header')!;
    let navHtml = `<a href="#/">Каталог</a>`;

    if (!state.token) {
        navHtml += `
            <a href="#/login">Войти</a>
            <a href="#/register">Регистрация</a>
        `;
    } else {
        navHtml += `
            <a href="#/cart">Корзина <span class="badge" id="cart-badge">${state.cartCount}</span></a>
            <a href="#/orders">Мои заказы</a>
        `;
        if (state.role === 'ROLE_ADMIN') {
            navHtml += `<a href="#/admin" style="color: var(--danger);">Админ-панель</a>`;
        }
        navHtml += `
            <span style="color:#64748b; font-size:0.9rem;">👤 ${state.username}</span>
            <button class="outline" id="logout-btn">Выйти</button>
        `;
    }

    header.innerHTML = `<a href="#/" class="logo">🛒 Супермаркет</a><nav>${navHtml}</nav>`;

    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.onclick = () => {
            clearAuth();
            window.location.hash = '#/';
        };
    }
}

function router(): void {
    renderHeader();
    const main = document.getElementById('app-main')!;
    const hash = window.location.hash || '#/';

    switch (hash) {
        case '#/':
            renderCatalog(main);
            break;
        case '#/login': 
            renderAuth(main, true); 
            break;
        case '#/register': 
            renderAuth(main, false); 
            break;
        case '#/cart': 
            renderCart(main); 
            break;
        case '#/orders':
            renderOrders(main); 
            break;
        case '#/admin': 
            renderAdmin(main); 
            break;
        default:
            renderCatalog(main);
    }
}

window.addEventListener('hashchange', router);
window.addEventListener('DOMContentLoaded', () => {
    syncCartCount();
    router();
});