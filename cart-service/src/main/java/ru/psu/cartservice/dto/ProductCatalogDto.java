package ru.psu.cartservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProductCatalogDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String imageURL;
    private LocalDate manufactureDate;
    private Integer expirationDays;
}
