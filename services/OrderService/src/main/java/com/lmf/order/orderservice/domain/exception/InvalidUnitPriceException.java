package com.lmf.order.orderservice.domain.exception;

public class InvalidUnitPriceException extends RuntimeException {
    public InvalidUnitPriceException(String message) {
        super(message);
    }

    public InvalidUnitPriceException(String message, Throwable cause) {
        super(message, cause);
    }
}
