package com.lmf.order.orderservice.domain.model.payment;

public enum PaymentMethod {

    CREDIT_CARD, DEBIT_CARD, PIX, BOLETO, PAYPAL, APPLE_PAY, GOOGLE_PAY;

    public static PaymentMethod fromName(String name) {
        try {
            return PaymentMethod.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid payment method: " + name);
        }
    }
}
