package com.stockmeds.centurion_core.auth.record;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public record OtpResponse(
    String message,
    String token
) {
}
