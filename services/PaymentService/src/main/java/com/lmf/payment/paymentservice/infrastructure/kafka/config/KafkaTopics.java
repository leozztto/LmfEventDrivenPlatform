package com.lmf.payment.paymentservice.infrastructure.kafka.config;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String ORDER_CREATED = "order.created";

    public static final String PAYMENT_CREATED = "payment.created";

    public static final String PAYMENT_CREATED_DLT = "payment.created.dlt";
}
