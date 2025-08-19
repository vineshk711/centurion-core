package com.stockmeds.centurion_core.product.record;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockmeds.centurion_core.enums.PaymentMethod;

import java.util.List;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public record PlaceOrderRequest(
        String shippingAddress,
        String billingAddress,
        PaymentMethod paymentMethod,
        String notes,
        List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            Integer productId,
            Integer quantity
    ) {}
}
