package com.stockmeds.centurion_core.product.record;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockmeds.centurion_core.product.entity.ProductCategoryEntity;

import java.util.List;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public record ProductCategory(
    Integer id,
    String name,
    Integer parentCategoryId,
    String description,
    String imageUrl,
    List<ProductCategoryEntity> subcategories
) {
}
