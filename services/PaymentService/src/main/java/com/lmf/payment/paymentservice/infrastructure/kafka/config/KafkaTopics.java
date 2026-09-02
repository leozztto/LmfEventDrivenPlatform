package com.lmf.payment.paymentservice.infrastructure.kafka.config;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String INVENTORY_RESERVED = "inventory.reserved";

    public static final String PAYMENT_PROCESSING = "payment.processing";

    public static final String PAYMENT_APPROVED = "payment.approved";

    public static final String PAYMENT_FAILED = "payment.failed";

    /** DLT do relay do outbox (não do consumo — esse usa {@code <topic>.dlt}). */
    public static final String PAYMENT_OUTBOX_DLT = "payment.outbox.dlt";
}
