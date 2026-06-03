package com.lmf.payment.paymentservice.infrastructure.kafka.config;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String INVENTORY_RESERVED = "inventory.reserved";

    public static final String PAYMENT_PROCESSED = "payment.processed";

    public static final String PAYMENT_FAILED_DLT = "payment.failed.dlt";
}
