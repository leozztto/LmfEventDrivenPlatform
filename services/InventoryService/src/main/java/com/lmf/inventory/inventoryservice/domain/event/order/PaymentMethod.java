package com.lmf.inventory.inventoryservice.domain.event.order;

public enum PaymentMethod {
    CREDIT_CARD, DEBIT_CARD, PIX, BOLETO, PAYPAL, APPLE_PAY, GOOGLE_PAY;

    public static PaymentMethod fromName(String name) {
        try {
            return PaymentMethod.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No enum constant with name: " + name + ". Available values: " + java.util.Arrays.toString(values()));
        }
    }

    public static PaymentMethod[] getAllPaymentMethods() {
        return values();
    }
}