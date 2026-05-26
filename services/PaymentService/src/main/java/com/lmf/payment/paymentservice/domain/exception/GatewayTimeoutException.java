package com.lmf.payment.paymentservice.domain.exception;

public class GatewayTimeoutException extends PaymentGatewayException {

    public GatewayTimeoutException() {

        super("Gateway timeout");
    }
}
