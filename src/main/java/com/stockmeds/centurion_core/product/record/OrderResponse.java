package com.stockmeds.centurion_core.product.record;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockmeds.centurion_core.enums.PaymentMethod;
import com.stockmeds.centurion_core.enums.PaymentStatus;
import com.stockmeds.centurion_core.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public record OrderResponse(
        Long id,
        Integer accountId,
        String orderNumber,
        BigDecimal totalAmount,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal finalAmount,
        Status status,
        String shippingAddress,
        String billingAddress,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        String notes,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record OrderItemResponse(
            Long id,
            Integer productId,
            String productName,
            String productBrand,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice,
            BigDecimal discountAmount,
            BigDecimal taxAmount,
            String packaging,
            Boolean prescriptionRequired
    ) {}
}
