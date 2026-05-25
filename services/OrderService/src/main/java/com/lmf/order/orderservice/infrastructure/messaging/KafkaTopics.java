package com.lmf.order.orderservice.infrastructure.messaging;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String ORDER_CREATED = "order.created";

    public static final String ORDER_CREATED_DLT = "order.created.dlt";
}
