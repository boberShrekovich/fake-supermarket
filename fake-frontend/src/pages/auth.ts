import { api, ENDPOINTS, syncCartCount } from '../api';
import { setAuth } from '../state';
import type { AuthResponse } from '../types';

export function renderAuth(container: HTMLElement, isLogin: boolean): void {
    container.innerHTML = `
        <div class="form-box">
            <h2>${isLogin ? 'Вход в систему' : 'Регистрация'}</h2>
            <form id="auth-form">
                <div class="form-group">
                    <label>Логин</label>
                    <input type="text" id="username" required>
                </div>
                <div class="form-group">
                    <label>Пароль</label>
                    <input type="password" id="password" required>
                </div>
                <button type="submit" style="width:100%">${isLogin ? 'Войти' : 'Зарегистрироваться'}</button>
            </form>
        </div>
    `;

    const form = document.getElementById('auth-form') as HTMLFormElement;
    form.onsubmit = async (e) => {
        e.preventDefault();
        const username = (document.getElementById('username') as HTMLInputElement).value;
        const password = (document.getElementById('password') as HTMLInputElement).value;

        try {
            const url = isLogin ? ENDPOINTS.login : ENDPOINTS.register;
            const data = await api<AuthResponse>(url, {
                method: 'POST',
                body: JSON.stringify({ username, password }),
            });

            if (isLogin) {
                setAuth(data);
                await syncCartCount();
                window.location.hash = '#/';
            } else {
                alert('Регистрация успешна! Теперь войдите в аккаунт.');
                window.location.hash = '#/login';
            }
        } catch (err: any) {
            alert(err.message);
        }
    };
}
