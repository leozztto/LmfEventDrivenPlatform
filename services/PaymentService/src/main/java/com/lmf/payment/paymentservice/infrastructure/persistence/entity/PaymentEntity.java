package com.lmf.payment.paymentservice.infrastructure.persistence.entity;

import com.lmf.payment.paymentservice.domain.PaymentMethod;
import com.lmf.payment.paymentservice.domain.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {

    @Id
    private UUID id;

    private UUID orderId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private Integer installments;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private OffsetDateTime createdAt;
}
