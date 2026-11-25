import { api, ENDPOINTS, syncCartCount } from '../api';
import { state } from '../state';
import type { Product } from '../types';

export async function renderCatalog(container: HTMLElement): Promise<void> {
    container.innerHTML = '<h2>Каталог товаров</h2><div class="grid" id="catalog-grid">Загрузка...</div>';
    try {
        const products = await api<Product[]>(ENDPOINTS.products);
        const grid = document.getElementById('catalog-grid')!;

        grid.innerHTML = products.map(p => `
            <div class="card">
                <div>
                    <img src="${p.imageURL || 'https://placeholder.com'}" alt="${p.name}">
                    <h3>${p.name}</h3>
                    <div class="meta">
                        Остаток: <b>${p.quantity} шт.</b><br>
                        Дата пр-ва: ${p.manufactureDate || '—'}<br>
                        Срок годности: ${p.expirationDays ? p.expirationDays + ' дн.' : '—'}
                    </div>
                </div>
                <div>
                    <div class="price">${p.price} ₽</div>
                    <button class="add-btn" data-id="${p.id}" style="width:100%" ${p.quantity <= 0 ? 'disabled' : ''}>
                        ${p.quantity > 0 ? 'В корзину' : 'Нет в наличии'}
                    </button>
                </div>
            </div>
        `).join('');

        grid.querySelectorAll<HTMLButtonElement>('.add-btn').forEach(btn => {
            btn.onclick = () => addToCart(Number(btn.dataset.id));
        });
    } catch (e: any) {
        container.innerHTML = `<p style="color:red">Ошибка загрузки: ${e.message}</p>`;
    }
}

async function addToCart(productId: number): Promise<void> {
    if (!state.token || !state.userId) {
        window.location.hash = '#/login';
        return;
    }
    try {
        await api(`${ENDPOINTS.carts}/${state.userId}/items`, {
            method: 'POST',
            body: JSON.stringify({ productId, quantity: 1 }),
        });
        await syncCartCount();
        alert('Товар добавлен в корзину');
    } catch (e: any) {
        alert('Ошибка: ' + e.message);
    }
}
