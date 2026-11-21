package ru.psu.cartservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResponse {
    private Long productId;
    private String productName;
    private String imageURL;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
}
