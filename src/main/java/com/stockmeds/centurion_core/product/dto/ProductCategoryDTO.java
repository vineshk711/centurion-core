package com.stockmeds.centurion_core.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockmeds.centurion_core.product.entity.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ProductCategoryDTO {
    private Integer id;
    private String name;
    private Integer parentCategoryId;
    private String description;
    private String imageUrl;
    private List<ProductCategory> subcategories;


}
