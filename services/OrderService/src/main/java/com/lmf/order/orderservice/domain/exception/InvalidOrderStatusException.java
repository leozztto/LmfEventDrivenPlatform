package com.lmf.order.orderservice.domain.exception;

public class InvalidOrderStatusException extends RuntimeException {

    public InvalidOrderStatusException(String currentStatus) {
        super("Invalid order status: " + currentStatus);
    }
}