import { api, ENDPOINTS, syncCartCount } from '../api';
import { state } from '../state';
import type { CartItem, Order } from '../types';

export async function renderCart(container: HTMLElement): Promise<void> {
    if (!state.token || !state.userId) {
        window.location.hash = '#/login';
        return;
    }

    container.innerHTML = '<h2>Корзина</h2><div id="cart-content">Загрузка...</div>';
    const content = document.getElementById('cart-content')!;

    try {
        const items = await api<CartItem[]>(`${ENDPOINTS.carts}/${state.userId}`);

        if (!items || items.length === 0) {
            content.innerHTML = '<p>Корзина пуста. <a href="#/">Перейти в каталог</a></p>';
            return;
        }

        const total = items.reduce((sum, item) => sum + item.totalPrice, 0);

        content.innerHTML = `
            <table>
                <thead>
                    <tr>
                        <th>Фото</th>
                        <th>Название</th>
                        <th>Цена</th>
                        <th>Кол-во</th>
                        <th>Сумма</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    ${items.map(item => `
                        <tr>
                            <td><img src="${item.imageURL || 'https://placeholder.com'}" width="50" height="50" style="object-fit:cover; border-radius:4px;"></td>
                            <td>${item.productName}</td>
                            <td>${item.price} ₽</td>
                            <td>${item.quantity} шт.</td>
                            <td><b>${item.totalPrice} ₽</b></td>
                            <td><button class="danger remove-item-btn" data-id="${item.productId}">Удалить</button></td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
            <div style="margin-top:1.5rem; display:flex; justify-content:space-between; align-items:center; background:white; padding:1rem; border-radius:8px; box-shadow: 0 1px 3px rgba(0,0,0,0.05);">
                <button class="outline" id="clear-cart-btn">Очистить корзину</button>
                <div>
                    <span style="font-size:1.25rem; margin-right:1.5rem;">Итого к оплате: <b>${total.toFixed(2)} ₽</b></span>
                    <button id="checkout-btn">Оформить заказ</button>
                </div>
            </div>
        `;

        content.querySelectorAll<HTMLButtonElement>('.remove-item-btn').forEach(btn => {
            btn.onclick = async () => {
                await api(`${ENDPOINTS.carts}/${state.userId}/items/${btn.dataset.id}`, { method: 'DELETE' });
                await syncCartCount();
                renderCart(container);
            };
        });

        document.getElementById('clear-cart-btn')!.onclick = async () => {
            await api(`${ENDPOINTS.carts}/${state.userId}`, { method: 'DELETE' });
            await syncCartCount();
            renderCart(container);
        };

        document.getElementById('checkout-btn')!.onclick = async () => {
            try {
                await api<Order>(`${ENDPOINTS.orders}/${state.userId}`, { method: 'POST' });
                await syncCartCount();
                alert('Заказ успешно оформлен!');
                window.location.hash = '#/orders';
            } catch (err: any) {
                alert('Ошибка при оформлении заказа: ' + err.message);
            }
        };
    } catch (e: any) {
        content.innerHTML = `<p style="color:red">Ошибка загрузки корзины: ${e.message}</p>`;
    }
}
