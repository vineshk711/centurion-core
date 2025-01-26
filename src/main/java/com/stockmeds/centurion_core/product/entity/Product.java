package com.stockmeds.centurion_core.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "products")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    private String brand;
    private String manufacturer;
    private BigDecimal price;
    private Integer stockQuantity;
    private String unitOfMeasure;
    private String variantName;
    private String strength;
    private String packaging;
    private String salts;
    private String indications;
    private String keyIngredients;
    private Date expiryDate;
    private String batchNumber;
    private String hsnCode;
    private BigDecimal gstPercentage;

    private Boolean prescriptionRequired;

    @Column(columnDefinition = "tsvector")
    private String searchVector;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
