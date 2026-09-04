package com.lmf.fraud.fraudservice.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "fraud_checks")
public class FraudCheckEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "decision", nullable = false)
    private String decision;

    @Column(name = "reason")
    private String reason;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected FraudCheckEntity() {
    }

    public FraudCheckEntity(UUID id, UUID orderId, UUID customerId, String decision, String reason, BigDecimal totalAmount, OffsetDateTime createdAt) {

        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.decision = decision;
        this.reason = reason;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
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

    public String getDecision() {
        return decision;
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
