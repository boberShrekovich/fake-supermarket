import type { AuthResponse, Role } from './types';

export const state = {
    token: localStorage.getItem('token'),
    userId: localStorage.getItem('userId'),
    username: localStorage.getItem('username'),
    role: localStorage.getItem('role') as Role | null,
    cartCount: 0,
};

export function setAuth(data: AuthResponse): void {
    localStorage.setItem('token', data.token);
    localStorage.setItem('userId', String(data.userId));
    localStorage.setItem('username', data.username);
    localStorage.setItem('role', data.role);

    state.token = data.token;
    state.userId = String(data.userId);
    state.username = data.username;
    state.role = data.role;
}

export function clearAuth(): void {
    localStorage.clear();
    state.token = null;
    state.userId = null;
    state.username = null;
    state.role = null;
    state.cartCount = 0;
}
