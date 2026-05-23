package com.lmf.payment.paymentservice.infrastructure.persistence.mapper;

import com.lmf.payment.paymentservice.domain.Payment;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.PaymentEntity;

public class PaymentEntityMapper {

    private PaymentEntityMapper() {
    }

    public static PaymentEntity toEntity(Payment payment) {

        return PaymentEntity.builder().id(payment.getId()).orderId(payment.getOrderId()).amount(payment.getAmount()).paymentMethod(payment.getPaymentMethod()).installments(payment.getInstallments()).status(payment.getStatus()).createdAt(payment.getCreatedAt()).build();
    }
}
