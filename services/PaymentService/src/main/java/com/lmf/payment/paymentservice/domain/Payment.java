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

    private BigDecimal amount;

    private PaymentMethod paymentMethod;

    private Integer installments;

    private PaymentStatus status;

    private OffsetDateTime createdAt;
}
