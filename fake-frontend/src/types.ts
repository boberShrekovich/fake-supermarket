export type Role = 'ROLE_USER' | 'ROLE_ADMIN';

export interface AuthResponse {
    token: string;
    userId: number;
    username: string;
    role: Role;
}

export interface Product {
    id: number;
    name: string;
    price: number;
    quantity: number;
    imageURL?: string;
    manufactureDate?: string;
    expirationDays?: number;
}

export interface CartItem {
    productId: number;
    productName: string;
    imageURL?: string;
    price: number;
    quantity: number;
    totalPrice: number;
}

export interface OrderItem {
    productId: number;
    productName: string;
    price: number;
    quantity: number;
}

export interface Order {
    id: number;
    userId: number;
    totalAmount: number;
    status: string;
    createdAt: string;
    items: OrderItem[];
}
