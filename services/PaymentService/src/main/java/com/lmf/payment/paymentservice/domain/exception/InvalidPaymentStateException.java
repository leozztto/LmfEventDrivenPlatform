package com.lmf.payment.paymentservice.domain.exception;

public class InvalidPaymentStateException extends BusinessException {

    public InvalidPaymentStateException(String message) {
        super(message);
    }
}
