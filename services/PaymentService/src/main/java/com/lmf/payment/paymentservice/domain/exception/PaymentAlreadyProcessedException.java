package com.lmf.payment.paymentservice.domain.exception;

import java.util.UUID;

public class PaymentAlreadyProcessedException extends RuntimeException {

    public PaymentAlreadyProcessedException(UUID orderId) {
        super("Payment already exists for order: " + orderId);
    }
}
