package com.lmf.payment.paymentservice.infrastructure.exception;

public class RetryableException extends RuntimeException {

    public RetryableException(String message) {
        super(message);
    }
}
