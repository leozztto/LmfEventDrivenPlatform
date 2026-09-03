package com.lmf.notification.notificationservice.infrastructure.kafka.config;

/**
 * Tópicos consumidos pelo NotificationService. Todos são produzidos por outros serviços — este
 * serviço não publica eventos de negócio. A DLT de cada tópico é {@code <topic>.dlt}.
 */
public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String ORDER_CREATED = "order.created";

    public static final String PAYMENT_APPROVED = "payment.approved";

    public static final String PAYMENT_FAILED = "payment.failed";

    public static final String INVENTORY_RESERVATION_FAILED = "inventory.reservation.failed";

    public static final String GROUP_ID = "notification-service-group";
}
