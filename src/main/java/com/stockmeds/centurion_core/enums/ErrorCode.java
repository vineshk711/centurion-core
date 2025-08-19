package com.stockmeds.centurion_core.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNKNOWN(1001, "Something went wrong"),
    EXTERNAL_CALL_FAILED(1002, "External API call failed"),
    USER_NOT_FOUND(1003, "User not found"),
    INVALID_OTP(1004, "Verification failed"),
    INVALID_JWT(1005, "Invalid JWT"),
    JWT_EXPIRED(1006, "Session expired"),
    INVALID_REQUEST(1007, "Invalid Request"),
    PRODUCT_NOT_FOUND(1008, "Product not found"),
    CART_ITEM_NOT_FOUND(1009, "Cart item not found"),
    CART_NOT_FOUND(1010, "Cart not found"),
    UNAUTHORIZED_CART_ACCESS(1011, "Unauthorized access to cart item"),
    PRODUCT_NOT_IN_CART(1012, "Product not found in cart"),
    ORDER_NOT_FOUND(1013, "Order not found"),
    CART_EMPTY(1014, "Cart is empty"),
    INVALID_QUANTITY(1015, "Invalid quantity"),
    INSUFFICIENT_STOCK(1016, "Insufficient stock available"),
    NO_VALID_ITEMS_TO_ORDER(1017, "No valid items to process for order");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
