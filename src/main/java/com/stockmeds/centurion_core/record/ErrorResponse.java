package com.stockmeds.centurion_core.record;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    Integer code,
    String message,
    String details
) {
}
