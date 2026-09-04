package com.lmf.fraud.fraudservice.infrastructure.kafka.config;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String ORDER_CREATED = "order.created";

    public static final String FRAUD_APPROVED = "fraud.approved";

    public static final String FRAUD_REJECTED = "fraud.rejected";
}
