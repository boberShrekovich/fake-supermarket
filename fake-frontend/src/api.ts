import { state, clearAuth } from './state';
import type { CartItem } from './types';

export const API_BASE = 'http://localhost:8080';

export const ENDPOINTS = {
    login: '/api/v1/auth/login',
    register: '/api/v1/auth/register',
    products: '/api/v1/products',
    carts: '/api/v1/carts',
    orders: '/api/v1/orders',
};

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {

    const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        ...(options.headers as Record<string, string>),
    };

    if (state.token) {
        headers['Authorization'] = `Bearer ${state.token}`;
    }

    const res = await fetch(API_BASE + path, { ...options, headers });

    if (res.status === 401) {
        clearAuth();
        window.location.hash = '#/login';
        throw new Error('Сессия истекла, войдите заново');
    }

    if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText || `Ошибка сервера: ${res.status}`);
    }

    // Учитываем, что 201 Created или 204 No Content могут вернуть пустое тело
    const text = await res.text();
    return text ? JSON.parse(text) : (null as T);
}

export async function syncCartCount(): Promise<void> {
    if (!state.token || !state.userId) return;
    try {
        const items = await api<CartItem[]>(`${ENDPOINTS.carts}/${state.userId}`);
        state.cartCount = (items || []).reduce((sum, item) => sum + item.quantity, 0);
        const badge = document.getElementById('cart-badge');
        if (badge) badge.textContent = String(state.cartCount);
    } catch (e) {
        console.error('Ошибка обновления счетчика корзины', e);
    }
}
