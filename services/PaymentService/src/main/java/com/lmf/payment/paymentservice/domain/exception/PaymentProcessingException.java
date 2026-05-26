package com.lmf.payment.paymentservice.domain.exception;

import com.lmf.payment.paymentservice.infrastructure.exception.RetryableException;

public class PaymentProcessingException extends RetryableException {

    public PaymentProcessingException(String message, Throwable cause) {

        super(message, cause);
    }
}
