package com.stockmeds.centurion_core.product.record;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public record UpdateCartItemRequest(
        Long cartItemId,
        Integer quantity
) {}
