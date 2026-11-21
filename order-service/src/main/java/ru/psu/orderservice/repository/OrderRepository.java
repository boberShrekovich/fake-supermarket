package ru.psu.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.psu.orderservice.entity.OrderEntity;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    // Получить историю всех заказов конкретного пользователя
    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
