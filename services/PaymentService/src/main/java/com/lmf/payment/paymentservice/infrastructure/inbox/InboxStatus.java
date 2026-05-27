package com.lmf.payment.paymentservice.infrastructure.inbox;

public enum InboxStatus {

    RECEIVED, PROCESSED, FAILED;

    public static InboxStatus fromName(String name) {
        try {
            return InboxStatus.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No enum constant with name: " + name + ". Available values: " + java.util.Arrays.toString(values()));
        }
    }

    public static InboxStatus[] getAllOutboxStatus() {
        return values();
    }
}
