package ru.psu.catalogservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private int quantity;
    private String imageURL;
    private LocalDate manufactureDate;
    private Integer expirationDays;
}
