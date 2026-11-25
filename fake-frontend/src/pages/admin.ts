import { api, ENDPOINTS } from '../api';
import { state } from '../state';
import type { Product } from '../types';

export async function renderAdmin(container: HTMLElement): Promise<void> {
    if (state.role !== 'ROLE_ADMIN') {
        window.location.hash = '#/';
        return;
    }

    container.innerHTML = `
        <h2>Панель администратора</h2>
        <div class="form-box" style="max-width:100%; margin-top:0;">
            <h3 id="form-title">Добавить новый товар</h3>
            <form id="product-form" style="display:grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap:1rem;">
                <input type="hidden" id="prod-id">
                <div class="form-group">
                    <label>Название</label>
                    <input type="text" id="prod-name" required>
                </div>
                <div class="form-group">
                    <label>Цена (₽)</label>
                    <input type="number" step="0.01" id="prod-price" required>
                </div>
                <div class="form-group">
                    <label>Остаток на складе</label>
                    <input type="number" id="prod-stock" required>
                </div>
                <div class="form-group">
                    <label>Ссылка на картинку</label>
                    <input type="url" id="prod-img">
                </div>
                <div class="form-group">
                    <label>Дата производства</label>
                    <input type="date" id="prod-date" required>
                </div>
                <div class="form-group">
                    <label>Срок годности (дней)</label>
                    <input type="number" id="prod-shelf" required>
                </div>
                <div style="grid-column: 1 / -1; display:flex; gap:1rem;">
                    <button type="submit">Сохранить товар</button>
                    <button type="button" class="outline" id="reset-form-btn">Отмена</button>
                </div>
            </form>
        </div>

        <h3>Существующие товары</h3>
        <div id="admin-table">Загрузка...</div>
    `;

    const form = document.getElementById('product-form') as HTMLFormElement;
    const resetForm = () => {
        document.getElementById('form-title')!.textContent = 'Добавить новый товар';
        form.reset();
        (document.getElementById('prod-id') as HTMLInputElement).value = '';
    };

    document.getElementById('reset-form-btn')!.onclick = resetForm;

    form.onsubmit = async (e) => {
        e.preventDefault();
        const id = (document.getElementById('prod-id') as HTMLInputElement).value;
        const payload = {
            name: (document.getElementById('prod-name') as HTMLInputElement).value,
            price: parseFloat((document.getElementById('prod-price') as HTMLInputElement).value),
            quantity: parseInt((document.getElementById('prod-stock') as HTMLInputElement).value, 10),
            imageURL: (document.getElementById('prod-img') as HTMLInputElement).value,
            manufactureDate: (document.getElementById('prod-date') as HTMLInputElement).value,
            expirationDays: parseInt((document.getElementById('prod-shelf') as HTMLInputElement).value, 10),
        };

        try {
            if (id) {
                await api(`${ENDPOINTS.products}/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
            } else {
                await api(ENDPOINTS.products, { method: 'POST', body: JSON.stringify(payload) });
            }
            resetForm();
            await loadProductsTable();
        } catch (err: any) {
            alert('Ошибка сохранения: ' + err.message);
        }
    };

    async function loadProductsTable(): Promise<void> {
        const tableContainer = document.getElementById('admin-table')!;
        try {
            const products = await api<Product[]>(ENDPOINTS.products);
            tableContainer.innerHTML = `
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Название</th>
                            <th>Цена</th>
                            <th>Остаток</th>
                            <th>Годен (дн.)</th>
                            <th>Действия</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${products.map(p => `
                            <tr>
                                <td>${p.id}</td>
                                <td>${p.name}</td>
                                <td>${p.price} ₽</td>
                                <td>${p.quantity} шт.</td>
                                <td>${p.expirationDays || '—'}</td>
                                <td>
                                    <button class="outline edit-btn" data-id="${p.id}">Редактировать</button>
                                    <button class="danger delete-btn" data-id="${p.id}">Удалить</button>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            `;

            tableContainer.querySelectorAll<HTMLButtonElement>('.edit-btn').forEach(btn => {
                btn.onclick = () => {
                    const p = products.find(item => String(item.id) === btn.dataset.id);
                    if (!p) return;
                    document.getElementById('form-title')!.textContent = `Редактирование товара #${p.id}`;
                    (document.getElementById('prod-id') as HTMLInputElement).value = String(p.id);
                    (document.getElementById('prod-name') as HTMLInputElement).value = p.name;
                    (document.getElementById('prod-price') as HTMLInputElement).value = String(p.price);
                    (document.getElementById('prod-stock') as HTMLInputElement).value = String(p.quantity);
                    (document.getElementById('prod-img') as HTMLInputElement).value = p.imageURL || '';
                    (document.getElementById('prod-date') as HTMLInputElement).value = p.manufactureDate || '';
                    (document.getElementById('prod-shelf') as HTMLInputElement).value = p.expirationDays ? String(p.expirationDays) : '';
                    window.scrollTo({ top: 0, behavior: 'smooth' });
                };
            });

            tableContainer.querySelectorAll<HTMLButtonElement>('.delete-btn').forEach(btn => {
                btn.onclick = async () => {
                    if (!confirm('Удалить этот товар?')) return;
                    try {
                        await api(`${ENDPOINTS.products}/${btn.dataset.id}`, { method: 'DELETE' });
                        await loadProductsTable();
                    } catch (err: any) {
                        alert('Ошибка удаления: ' + err.message);
                    }
                };
            });
        } catch (e: any) {
            tableContainer.innerHTML = `<p style="color:red">Ошибка загрузки товаров: ${e.message}</p>`;
        }
    }

    await loadProductsTable();
}
