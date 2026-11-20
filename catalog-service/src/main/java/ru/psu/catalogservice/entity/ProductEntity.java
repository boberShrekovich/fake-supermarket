package ru.psu.catalogservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "products")
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(precision=10, scale=2, nullable=false)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDate manufactureDate;

    @Column(nullable = false)
    private int expirationDays;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "image_url", length = 500, nullable = false)
    private String imageURL;
}
