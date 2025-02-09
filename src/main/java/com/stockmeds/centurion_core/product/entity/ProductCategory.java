package com.stockmeds.centurion_core.product.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stockmeds.centurion_core.product.dto.ProductCategoryDTO;
import jakarta.persistence.*;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "product_categories")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Ignore Hibernate proxies
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String description;
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    @JsonIgnore  // Prevent circular reference
    private ProductCategory parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCategory> subcategories;


    public ProductCategoryDTO toProductCategoryDTO() {
        return ProductCategoryDTO.builder()
                .id(id)
                .name(name)
                .description(description)
                .imageUrl(imageUrl)
                .subcategories(subcategories)
                .build();
    }
}


