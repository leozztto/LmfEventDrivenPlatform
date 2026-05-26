package com.lmf.payment.paymentservice.domain.exception;

import com.lmf.payment.paymentservice.infrastructure.exception.NonRetryableException;

public class PaymentDeclinedException extends NonRetryableException {

    public PaymentDeclinedException(String message) {
        super(message);
    }
}
