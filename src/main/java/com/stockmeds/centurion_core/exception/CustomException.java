package com.stockmeds.centurion_core.exception;


import com.stockmeds.centurion_core.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final ErrorCode errorCode;
    private final String customMessage;

    public CustomException(HttpStatus httpStatus, ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.customMessage = null;
    }

    public CustomException(HttpStatus httpStatus, ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }

    @Override
    public String getMessage() {
        return customMessage != null ? customMessage : errorCode.getMessage();
    }
}