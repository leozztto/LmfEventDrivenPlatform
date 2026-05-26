package com.lmf.payment.paymentservice.infrastructure.persistence.mapper;

import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.PaymentEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentEntityMapper {

    PaymentEntity toEntity(Payment payment);

    default Payment toDomain(PaymentEntity entity) {

        return Payment.restore(entity.getId(), entity.getOrderId(), entity.getCustomerId(), entity.getAmount(), entity.getCurrency(), entity.getPaymentMethod(), entity.getInstallments(), entity.getPaymentStatus(), entity.getProvider(), entity.getTransactionId(), entity.getGatewayStatus(), entity.getCreatedAt(), entity.getPaidAt(), entity.getFailedAt(), entity.getUpdatedAt(), entity.getFailureReason());
    }
}