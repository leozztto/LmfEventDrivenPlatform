package com.lmf.payment.paymentservice.infrastructure.persistence.mapper;

import com.lmf.payment.paymentservice.application.command.ProcessPaymentCommand;
import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.domain.model.PaymentStatus;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class PaymentEntityMapper {

    public static PaymentEntity toEntity(Payment payment) {

        return new PaymentEntity(payment.getOrderId(), payment.getCustomerId(), payment.getAmount(), payment.getCurrency(), payment.getPaymentMethod(), payment.getInstallments(), PaymentStatus.PENDING, payment.getProvider(), payment.getCreatedAt());
    }

    public static Payment toDomain(PaymentEntity paymentEntity) {

        return new Payment(paymentEntity.getId(), paymentEntity.getOrderId(), paymentEntity.getCustomerId(), paymentEntity.getAmount(), paymentEntity.getCurrency(), paymentEntity.getPaymentMethod(), paymentEntity.getInstallments(), paymentEntity.getPaymentStatus(), paymentEntity.getProvider(), paymentEntity.getTransactionId(), paymentEntity.getGatewayStatus(), paymentEntity.getCreatedAt(), paymentEntity.getPaidAt(), paymentEntity.getFailedAt());
    }

    public static Payment toDomain(ProcessPaymentCommand processPaymentCommand) {

        return new Payment(processPaymentCommand.orderId(), processPaymentCommand.customerId(), processPaymentCommand.amount(), processPaymentCommand.currency(), processPaymentCommand.paymentMethod(), processPaymentCommand.installments(), PaymentStatus.PENDING, "", OffsetDateTime.now());
    }
}
