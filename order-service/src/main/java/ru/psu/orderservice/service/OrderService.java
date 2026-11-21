package ru.psu.orderservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import ru.psu.orderservice.dto.*;
import ru.psu.orderservice.entity.OrderEntity;
import ru.psu.orderservice.entity.OrderItemEntity;
import ru.psu.orderservice.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestClient restClient;
    private final String cartServiceUrl;
    private final String catalogServiceUrl;

    public OrderService(
            OrderRepository orderRepository,
            @Value("${cart.service.url}") String cartServiceUrl,
            @Value("${catalog.service.url}") String catalogServiceUrl
    ) {
        this.orderRepository = orderRepository;
        this.cartServiceUrl = cartServiceUrl;
        this.catalogServiceUrl = catalogServiceUrl;
        this.restClient = RestClient.create();
    }

    // Оформить заказ из текущей корзины пользователя
    @Transactional
    public OrderResponse createOrder(Long userId) {
        // Получение товаров из корзины пользователя
        List<CartItemDto> cartItems = fetchCartItems(userId);

        if (cartItems == null || cartItems.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to place an order: the cart is empty"
            );
        }

        // Формирование списка на списание и отправление в Catalog Service
        List<StockReduceRequest> stockRequests = new ArrayList<>();
        for (CartItemDto item : cartItems) {
            stockRequests.add(new StockReduceRequest(item.getProductId(), item.getQuantity()));
        }
        reduceProductStock(stockRequests);

        // Создание заказа в базе данных
        OrderEntity order = new OrderEntity();
        order.setUserId(userId);
        order.setStatus("CREATED");
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();

        for (CartItemDto cartItem : cartItems) {
            OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setOrder(order); // привязываем позицию к заказу
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());

            orderItems.add(orderItem);

            BigDecimal itemTotal = cartItem.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        OrderEntity savedOrder = orderRepository.save(order);

        // Очистка корзины пользователя в Cart Service
        clearUserCart(userId);

        return mapToResponse(savedOrder);
    }

    // Получить историю заказов пользователя
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {
        List<OrderEntity> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<OrderResponse> responses = new ArrayList<>();

        for (OrderEntity order : orders) {
            responses.add(mapToResponse(order));
        }

        return responses;
    }


    private List<CartItemDto> fetchCartItems(Long userId) {
        try {
            return restClient.get()
                    .uri(cartServiceUrl + "/" + userId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<CartItemDto>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Failed to retrieve data from the cart service" + e.getMessage()
            );
        }
    }

    private void reduceProductStock(List<StockReduceRequest> stockRequests) {
        try {
            restClient.post()
                    .uri(catalogServiceUrl + "/reduce-stock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(stockRequests)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.BadRequest e) {
            // Если Catalog Service вернул 400 (недостаточно товара на складе)
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order processing declined: insufficient stock"
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "The catalog service is unavailable when writing off remaining stock."
            );
        }
    }

    private void clearUserCart(Long userId) {
        try {
            restClient.delete()
                    .uri(cartServiceUrl + "/" + userId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            // Если очистка корзины не удалась, логирование (заказ уже оформлен)
            System.err.println("Failed to empty the trash for user " + userId + ": " + e.getMessage());
        }
    }

    // Преобразование OrderEntity в OrderResponse
    private OrderResponse mapToResponse(OrderEntity order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());

        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItemEntity item : order.getItems()) {
            OrderItemResponse itemDto = new OrderItemResponse();
            itemDto.setProductId(item.getProductId());
            itemDto.setProductName(item.getProductName());
            itemDto.setPrice(item.getPrice());
            itemDto.setQuantity(item.getQuantity());
            itemResponses.add(itemDto);
        }

        response.setItems(itemResponses);
        return response;
    }
}
