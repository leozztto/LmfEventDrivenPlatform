package com.lmf.payment.paymentservice.infrastructure.exception;

public class NonRetryableException extends RuntimeException {

    public NonRetryableException(String message) {
        super(message);
    }
}
