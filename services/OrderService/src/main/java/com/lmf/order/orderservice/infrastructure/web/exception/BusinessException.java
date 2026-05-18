package com.lmf.order.orderservice.infrastructure.web.exception;

public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}