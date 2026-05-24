package com.lmf.payment.paymentservice.domain.exception;

import java.util.UUID;

public class PaymentAlreadyProcessedException extends RuntimeException {

    public PaymentAlreadyProcessedException(UUID paymentId) {

        super("Payment already processed: " + paymentId);
    }
}
