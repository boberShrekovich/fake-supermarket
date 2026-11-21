package ru.psu.cartservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.psu.cartservice.entity.CartItemEntity;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    // Найти все товары в корзине конкретного пользователя
    List<CartItemEntity> findByUserId(Long userId);

    // Найти конкретный товар в корзине пользователя
    Optional<CartItemEntity> findByUserIdAndProductId(Long userId, Long productId);

    // Очистить всю корзину пользователя
    void deleteByUserId(Long userId);
}
