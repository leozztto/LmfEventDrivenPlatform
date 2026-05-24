package com.lmf.payment.paymentservice.infrastructure.outbox;

public enum OutboxStatus {
    PENDING, PROCESSING, PUBLISHED, FAILED, DLQ;

    public static OutboxStatus fromName(String name) {
        try {
            return OutboxStatus.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No enum constant with name: " + name + ". Available values: " + java.util.Arrays.toString(values()));
        }
    }

    public static OutboxStatus[] getAllOutboxStatus() {
        return values();
    }
}
