package com.lmf.payment.paymentservice.infrastructure.persistence.entity;

import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import com.lmf.payment.paymentservice.domain.model.PaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class PaymentEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private Integer installments;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private String provider;

    private String transactionId;

    private String gatewayStatus;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime paidAt;

    private OffsetDateTime failedAt;

    private OffsetDateTime updatedAt;

    private String failureReason;

    protected PaymentEntity() {
    }

    public PaymentEntity(UUID id, UUID orderId, UUID customerId, BigDecimal amount, String currency, PaymentMethod paymentMethod, Integer installments, PaymentStatus paymentStatus, String provider, String transactionId, String gatewayStatus, OffsetDateTime createdAt, OffsetDateTime paidAt, OffsetDateTime failedAt, OffsetDateTime updatedAt, String failureReason) {

        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.installments = installments;
        this.paymentStatus = paymentStatus;
        this.provider = provider;
        this.transactionId = transactionId;
        this.gatewayStatus = gatewayStatus;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
        this.failedAt = failedAt;
        this.updatedAt = updatedAt;
        this.failureReason = failureReason;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public Integer getInstallments() {
        return installments;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public String getProvider() {
        return provider;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getGatewayStatus() {
        return gatewayStatus;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getPaidAt() {
        return paidAt;
    }

    public OffsetDateTime getFailedAt() {
        return failedAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }
}