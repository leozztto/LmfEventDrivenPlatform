package com.lmf.order.orderservice.infrastructure.messaging;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String ORDER_CREATED = "order.created";

    public static final String ORDER_CREATED_DLT = "order.created.dlt";

    // Desfechos da saga consumidos pelo OrderService.
    public static final String PAYMENT_APPROVED = "payment.approved";

    public static final String PAYMENT_FAILED = "payment.failed";

    public static final String INVENTORY_RESERVATION_FAILED = "inventory.reservation.failed";

    public static final String FRAUD_REJECTED = "fraud.rejected";

    public static final String SAGA_DLT = "order.saga.dlt";
}
