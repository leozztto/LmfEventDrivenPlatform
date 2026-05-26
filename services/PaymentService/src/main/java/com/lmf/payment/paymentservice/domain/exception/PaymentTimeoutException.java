package com.lmf.payment.paymentservice.domain.exception;

import com.lmf.payment.paymentservice.infrastructure.exception.RetryableException;

public class PaymentTimeoutException extends RetryableException {

    public PaymentTimeoutException(String message) {
        super(message);
    }

    public PaymentTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
