package com.lmf.inventory.inventoryservice.infrastructure.config;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String FRAUD_APPROVED = "fraud.approved";

    public static final String INVENTORY_RESERVED = "inventory.reserved";

    public static final String INVENTORY_RESERVATION_FAILED = "inventory.reservation.failed";

    public static final String INVENTORY_RESERVATION_DLT = "inventory.reservation.dlt";

    public static final String PRODUCT_CREATED = "product.created";

    // Desfechos de pagamento consumidos para compensar/confirmar a reserva.
    public static final String PAYMENT_APPROVED = "payment.approved";

    public static final String PAYMENT_FAILED = "payment.failed";
}
