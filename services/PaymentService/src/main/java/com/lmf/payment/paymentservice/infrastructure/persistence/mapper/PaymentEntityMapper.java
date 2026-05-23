package com.lmf.payment.paymentservice.infrastructure.persistence.mapper;

import com.lmf.payment.paymentservice.domain.Payment;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.PaymentEntity;

public class PaymentEntityMapper {

    private PaymentEntityMapper() {
    }

    public static PaymentEntity toEntity(Payment payment) {

        return PaymentEntity.builder().id(payment.getId()).orderId(payment.getOrderId()).customerId(payment.getCustomerId()).amount(payment.getAmount()).currency(payment.getCurrency()).paymentMethod(payment.getPaymentMethod()).installments(payment.getInstallments()).status(payment.getStatus()).provider(payment.getProvider()).transactionId(payment.getTransactionId()).gatewayStatus(payment.getGatewayStatus()).createdAt(payment.getCreatedAt()).paidAt(payment.getPaidAt()).failedAt(payment.getFailedAt()).build();
    }

    public static Payment toDomain(PaymentEntity paymentEntity) {

        return Payment.builder().id(paymentEntity.getId()).orderId(paymentEntity.getOrderId()).customerId(paymentEntity.getCustomerId()).amount(paymentEntity.getAmount()).currency(paymentEntity.getCurrency()).paymentMethod(paymentEntity.getPaymentMethod()).installments(paymentEntity.getInstallments()).status(paymentEntity.getStatus()).provider(paymentEntity.getProvider()).transactionId(paymentEntity.getTransactionId()).gatewayStatus(paymentEntity.getGatewayStatus()).createdAt(paymentEntity.getCreatedAt()).paidAt(paymentEntity.getPaidAt()).failedAt(paymentEntity.getFailedAt()).build();
    }
}
