package com.lmf.payment.paymentservice.domain.exception;

import com.lmf.payment.paymentservice.domain.model.PaymentMethod;

public class UnsupportedPaymentMethodException extends RuntimeException {

    public UnsupportedPaymentMethodException(PaymentMethod paymentMethod) {
        super("Unsupported payment method: " + paymentMethod);
    }
}
