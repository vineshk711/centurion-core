package com.stockmeds.centurion_core.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ProductDTO {
    private Integer id;
    private String name;
    private String description;
    private Integer categoryId;
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
    private String imageUrl;
}
