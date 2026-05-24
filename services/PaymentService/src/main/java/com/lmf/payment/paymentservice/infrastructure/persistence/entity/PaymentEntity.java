package com.lmf.payment.paymentservice.infrastructure.persistence.entity;

import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import com.lmf.payment.paymentservice.domain.model.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private Integer installments;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private String provider;

    private String transactionId;

    private String gatewayStatus;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime paidAt;

    private OffsetDateTime failedAt;

    public PaymentEntity(UUID id, UUID orderId, UUID customerId, BigDecimal amount, String currency, PaymentMethod paymentMethod, Integer installments, PaymentStatus paymentStatus, String provider, OffsetDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.installments = installments;
        this.paymentStatus = paymentStatus;
        this.provider = provider;
        this.createdAt = createdAt;
    }

    public PaymentEntity(UUID orderId, UUID customerId, BigDecimal amount, String currency, PaymentMethod paymentMethod, Integer installments, PaymentStatus paymentStatus, String provider, OffsetDateTime createdAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.installments = installments;
        this.paymentStatus = paymentStatus;
        this.provider = provider;
        this.createdAt = createdAt;
    }
}
