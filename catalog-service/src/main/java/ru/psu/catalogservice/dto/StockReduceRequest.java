package ru.psu.catalogservice.dto;

import lombok.Data;

@Data
public class StockReduceRequest {
    private Long productId;
    private Integer quantity;
}
