package com.stockmeds.centurion_core.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNKNOWN(1001, "Something went wrong"),
    EXTERNAL_CALL_FAILED(1002, "External API call failed"),
    USER_NOT_FOUND(1003, "User not found"),
    INVALID_OTP(1004, "Verification failed"),
    INVALID_JWT(1004, "Invalid JWT"),
    JWT_EXPIRED(1004, "JWT expired");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
