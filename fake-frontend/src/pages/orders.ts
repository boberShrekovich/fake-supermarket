import { api, ENDPOINTS } from '../api';
import { state } from '../state';
import type { Order } from '../types';

export async function renderOrders(container: HTMLElement): Promise<void> {
    if (!state.token || !state.userId) {
        window.location.hash = '#/login';
        return;
    }

    container.innerHTML = '<h2>Мои заказы</h2><div id="orders-list">Загрузка...</div>';
    const list = document.getElementById('orders-list')!;

    try {
        const orders = await api<Order[]>(`${ENDPOINTS.orders}/user/${state.userId}`);
        orders.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

        if (orders.length === 0) {
            list.innerHTML = '<p>У вас пока нет оформленных заказов.</p>';
            return;
        }

        list.innerHTML = orders.map(o => `
            <div class="order-card">
                <div class="order-header">
                    <span>Заказ #${o.id} от ${new Date(o.createdAt).toLocaleString()}</span>
                    <span>Статус: <b>${o.status}</b> | Сумма: <b>${o.totalAmount} ₽</b> ▼</span>
                </div>
                <div class="order-details">
                    <table>
                        <thead>
                            <tr>
                                <th>Товар</th>
                                <th>Цена за шт.</th>
                                <th>Количество</th>
                                <th>Итого</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${(o.items || []).map(i => `
                                <tr>
                                    <td>${i.productName}</td>
                                    <td>${i.price} ₽</td>
                                    <td>${i.quantity} шт.</td>
                                    <td>${(i.price * i.quantity).toFixed(2)} ₽</td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `).join('');

        list.querySelectorAll<HTMLElement>('.order-header').forEach(header => {
            header.onclick = () => {
                header.nextElementSibling?.classList.toggle('open');
            };
        });
    } catch (e: any) {
        list.innerHTML = `<p style="color:red">Ошибка загрузки заказов: ${e.message}</p>`;
    }
}
