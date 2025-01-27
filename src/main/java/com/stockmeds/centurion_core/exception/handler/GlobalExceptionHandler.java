package com.stockmeds.centurion_core.exception.handler;

import com.stockmeds.centurion_core.dto.ErrorResponse;
import com.stockmeds.centurion_core.enums.ErrorCode;

import com.stockmeds.centurion_core.exception.CustomException;
import com.stockmeds.centurion_core.exception.ExternalCallException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        log.error("Custom exception occurred", ex);
        return buildErrorResponse(ex, ex.getHttpStatus(), ex.getErrorCode());
    }

    @ExceptionHandler(ExternalCallException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(ExternalCallException ex) {
        log.error("External API call exception occurred", ex);
        return buildErrorResponse(ex, ex.getHttpStatus(), ex.getErrorCode());
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Server exception occurred", ex);
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.UNKNOWN);
    }


    private ResponseEntity<ErrorResponse> buildErrorResponse(Exception ex, HttpStatus status, ErrorCode errorCode) {
        ErrorResponse errorResponse = new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, status);
    }
}

