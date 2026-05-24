package com.lmf.payment.paymentservice.domain.model;

public enum PaymentStatus {

    PENDING, APPROVED, FAILED, REFUNDED, REJECTED;

    public static PaymentStatus fromName(String name) {
        try {
            return PaymentStatus.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No enum constant with name: " + name + ". Available values: " + java.util.Arrays.toString(values()));
        }
    }

    public static PaymentStatus[] getAllPaymentMethods() {
        return values();
    }
}
