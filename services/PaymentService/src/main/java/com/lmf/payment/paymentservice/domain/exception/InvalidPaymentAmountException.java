package com.lmf.payment.paymentservice.domain.exception;

public class InvalidPaymentAmountException extends BusinessException {

    public InvalidPaymentAmountException() {
        super("Payment amount must be greater than zero");
    }
}
