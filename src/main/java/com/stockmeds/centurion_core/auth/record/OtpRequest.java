package com.stockmeds.centurion_core.auth.record;

public record OtpRequest(
    String phoneNumber,
    String otp
) {
}
