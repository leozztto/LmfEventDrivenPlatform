package com.lmf.payment.paymentservice.domain.exception;

public class InvalidPaymentMethodException extends BusinessException {

    public InvalidPaymentMethodException(String message) {
        super(message);
    }
}
