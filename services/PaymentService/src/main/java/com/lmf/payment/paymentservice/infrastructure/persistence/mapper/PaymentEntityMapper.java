package com.lmf.payment.paymentservice.infrastructure.persistence.mapper;

import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.PaymentEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentEntityMapper {


    default PaymentEntity toEntity(Payment payment) {

        return new PaymentEntity(payment.getId(), payment.getOrderId(), payment.getCustomerId(), payment.getAmount(), payment.getCurrency(), payment.getPaymentMethod(), payment.getInstallments(), payment.getPaymentStatus(), payment.getProvider(), payment.getTransactionId(), payment.getGatewayStatus(), payment.getCreatedAt(), payment.getPaidAt(), payment.getFailedAt(), payment.getUpdatedAt(), payment.getFailureReason());
    }

    default Payment toDomain(PaymentEntity entity) {

        return Payment.restore(entity.getId(), entity.getOrderId(), entity.getCustomerId(), entity.getAmount(), entity.getCurrency(), entity.getPaymentMethod(), entity.getInstallments(), entity.getPaymentStatus(), entity.getProvider(), entity.getTransactionId(), entity.getGatewayStatus(), entity.getCreatedAt(), entity.getPaidAt(), entity.getFailedAt(), entity.getUpdatedAt(), entity.getFailureReason());
    }
}