package com.centurion.centurion_core.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNKNOWN(1001, "Something went wrong"),
    EXTERNAL_CALL_FAILED(1002, "External API call failed");

    private Integer code;
    private String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
