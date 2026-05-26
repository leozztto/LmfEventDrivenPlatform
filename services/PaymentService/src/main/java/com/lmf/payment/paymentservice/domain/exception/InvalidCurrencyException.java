package com.lmf.payment.paymentservice.domain.exception;

public class InvalidCurrencyException extends BusinessException {

    public InvalidCurrencyException(String message) {

        super(message);
    }
}
