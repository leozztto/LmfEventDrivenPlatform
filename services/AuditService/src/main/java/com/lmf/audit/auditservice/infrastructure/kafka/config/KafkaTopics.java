package com.lmf.audit.auditservice.infrastructure.kafka.config;

/**
 * Os sete tópicos da coreografia da saga, todos lidos pelo AuditService como um terceiro leitor
 * (fan-out) — nenhum deles é produzido por este serviço. A DLT de cada um é {@code <topic>.dlt}.
 */
public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String ORDER_CREATED = "order.created";

    public static final String FRAUD_APPROVED = "fraud.approved";

    public static final String FRAUD_REJECTED = "fraud.rejected";

    public static final String INVENTORY_RESERVED = "inventory.reserved";

    public static final String INVENTORY_RESERVATION_FAILED = "inventory.reservation.failed";

    public static final String PAYMENT_APPROVED = "payment.approved";

    public static final String PAYMENT_FAILED = "payment.failed";

    public static final String GROUP_ID = "audit-service-group";
}
