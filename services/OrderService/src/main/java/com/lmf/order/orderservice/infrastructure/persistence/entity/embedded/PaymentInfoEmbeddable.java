package com.lmf.order.orderservice.infrastructure.persistence.entity.embedded;

import com.lmf.order.orderservice.domain.model.payment.PaymentMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfoEmbeddable {

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "installments")
    private Integer installments;

    @Column(name = "paid_amount", precision = 19, scale = 2)
    private BigDecimal paidAmount;
}