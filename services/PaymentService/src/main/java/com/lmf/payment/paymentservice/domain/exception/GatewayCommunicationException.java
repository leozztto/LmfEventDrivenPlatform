package com.lmf.payment.paymentservice.domain.exception;

public class GatewayCommunicationException extends PaymentGatewayException {

    public GatewayCommunicationException(Throwable cause) {

        super("Gateway communication failed", cause);
    }
}
