package com.stockmeds.centurion_core.product.record;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public record CartResponse(
        Long id,
        Integer accountId,
        List<CartItemResponse> items,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record CartItemResponse(
            Long id,
            Integer productId,
            String productName,
            String productBrand,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice,
            String packaging,
            Boolean prescriptionRequired
    ) {}
}
