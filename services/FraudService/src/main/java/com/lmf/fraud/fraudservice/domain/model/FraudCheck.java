package com.lmf.fraud.fraudservice.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Histórico de uma decisão de fraude tomada sobre um pedido — gravado independentemente do
 * resultado (aprovado ou rejeitado), na mesma transação em que o evento de saída é escrito no
 * outbox.
 */
public class FraudCheck {

    private final UUID id;
    private final UUID orderId;
    private final UUID customerId;
    private final boolean approved;
    private final String reason;
    private final BigDecimal totalAmount;
    private final OffsetDateTime createdAt;

    public FraudCheck(UUID id, UUID orderId, UUID customerId, boolean approved, String reason, BigDecimal totalAmount, OffsetDateTime createdAt) {

        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.approved = approved;
        this.reason = reason;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public static FraudCheck record(UUID orderId, UUID customerId, FraudDecision decision, BigDecimal totalAmount) {

        return new FraudCheck(UUID.randomUUID(), orderId, customerId, decision.approved(), decision.reason(), totalAmount, OffsetDateTime.now());
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public boolean isApproved() {
        return approved;
    }

    public String getReason() {
        return reason;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
