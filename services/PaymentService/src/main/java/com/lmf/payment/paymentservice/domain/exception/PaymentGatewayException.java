package com.lmf.payment.paymentservice.domain.exception;

public class PaymentGatewayException extends BusinessException {

    public PaymentGatewayException(String message) {

        super(message);
    }

    public PaymentGatewayException(String message, Throwable cause) {

        super(message, cause);
    }
}
