package com.lmf.payment.paymentservice.domain.exception;

import java.util.UUID;

public class PaymentNotFoundException extends BusinessException {

    public PaymentNotFoundException(UUID paymentId) {

        super("Payment not found: " + paymentId);
    }
}
