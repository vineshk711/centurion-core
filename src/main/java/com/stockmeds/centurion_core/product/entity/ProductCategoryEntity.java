package com.stockmeds.centurion_core.product.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "product_categories")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Ignore Hibernate proxies
public class ProductCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String description;
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    @JsonIgnore  // Prevent circular reference
    private ProductCategoryEntity parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCategoryEntity> subcategories;


    public com.stockmeds.centurion_core.product.record.ProductCategory toProductCategoryDTO() {
        return new com.stockmeds.centurion_core.product.record.ProductCategory(
                id,
                name,
                parentCategory != null ? parentCategory.getId() : null,
                description,
                imageUrl,
                subcategories
        );
    }
}
