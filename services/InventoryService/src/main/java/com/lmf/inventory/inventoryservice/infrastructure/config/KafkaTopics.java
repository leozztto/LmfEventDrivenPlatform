package com.lmf.inventory.inventoryservice.infrastructure.config;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String ORDER_CREATED = "order.created";

    public static final String INVENTORY_RESERVED = "inventory.reserved";

    public static final String INVENTORY_RESERVATION_FAILED = "inventory.reservation.failed";
}
