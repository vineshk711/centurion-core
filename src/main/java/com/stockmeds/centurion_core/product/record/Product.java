package com.stockmeds.centurion_core.product.record;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.Date;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public record Product(
    Integer id,
    String name,
    String description,
    Integer categoryId,
    String brand,
    String manufacturer,
    BigDecimal price,
    Integer stockQuantity,
    String unitOfMeasure,
    String variantName,
    String strength,
    String packaging,
    String salts,
    String indications,
    String keyIngredients,
    Date expiryDate,
    String batchNumber,
    String hsnCode,
    BigDecimal gstPercentage,
    Boolean prescriptionRequired,
    String imageUrl
) {
}
