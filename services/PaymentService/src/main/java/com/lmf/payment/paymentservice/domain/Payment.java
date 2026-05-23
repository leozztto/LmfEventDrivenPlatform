package com.lmf.payment.paymentservice.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class Payment {

    private UUID id;

    private UUID orderId;

    private UUID customerId;

    private BigDecimal amount;

    private String currency;

    private PaymentMethod paymentMethod;

    private Integer installments;

    private PaymentStatus status;

    private String provider;

    private String transactionId;

    private String gatewayStatus;

    private OffsetDateTime createdAt;

    private OffsetDateTime paidAt;

    private OffsetDateTime failedAt;
}
