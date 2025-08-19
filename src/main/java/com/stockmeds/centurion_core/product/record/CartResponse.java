package com.stockmeds.centurion_core.product.record;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public record CartResponse(
        Integer accountId,
        List<CartItemResponse> items,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    // Static factory method for creating an empty cart response
    public static CartResponse empty(Integer accountId) {
        return new CartResponse(
                accountId,
                List.of(), // empty list instead of null
                BigDecimal.ZERO,
                null, // no creation time for empty cart
                null  // no update time for empty cart
        );
    }

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
