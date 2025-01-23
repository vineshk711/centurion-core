package com.stockmeds.centurion_core.exception;

import com.stockmeds.centurion_core.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ExternalCallException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final ErrorCode errorCode;

    public ExternalCallException(HttpStatus httpStatus, ErrorCode errorCode, String details) {
        super(details);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }
}
