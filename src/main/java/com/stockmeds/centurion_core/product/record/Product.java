package com.stockmeds.centurion_core.product.record;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockmeds.centurion_core.product.entity.ProductEntity;

import java.math.BigDecimal;
import java.util.Date;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public record Product(
        Integer id,
        String name,                     // e.g. "Crocin 500mg Tablet" or "Taxim-o 200 mg tablet or Honeytus 100ml syrup"
        String description,              // description for AI retrieval like "Paracetamol is analgesic pain reliever and a fever reducer or  Cefexime is an antibiotic used to treat bacterial infections."
        String brand,                    // e.g. "Crocin"
        String manufacturer,             // e.g. "GlaxoSmithKline"
        BigDecimal price,
        Integer stockQuantity,
        String unitOfMeasure,            // e.g. "Tablet", "Bottle", "Tube"
        String variantName,              // e.g. "Sugar Free", "Kids", "Extra Strength"
        String strength,                 // e.g. "500mg", "100ml"
        String packaging,                // e.g. "Strip of 10 tablets", "Bottle of 60ml"
        String salts,                    // e.g. "Paracetamol", "Ibuprofen"
        String indications,              // e.g. "Fever, Pain relief"
        String keyIngredients,           // e.g. "Paracetamol, Caffeine"
        Date expiryDate,
        String batchNumber,
        String hsnCode,
        BigDecimal gstPercentage,
        Boolean prescriptionRequired
) {

    public static Product fromProductEntity(ProductEntity entity) {
        return new Product(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getBrand(),
                entity.getManufacturer(),
                entity.getPrice(),
                entity.getStockQuantity(),
                entity.getUnitOfMeasure(),
                entity.getVariantName(),
                entity.getStrength(),
                entity.getPackaging(),
                entity.getSalts(),
                entity.getIndications(),
                entity.getKeyIngredients(),
                entity.getExpiryDate(),
                entity.getBatchNumber(),
                entity.getHsnCode(),
                entity.getGstPercentage(),
                entity.getPrescriptionRequired()
        );
    }
}
