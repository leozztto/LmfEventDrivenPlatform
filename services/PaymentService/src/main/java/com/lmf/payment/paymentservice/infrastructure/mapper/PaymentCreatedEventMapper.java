package com.lmf.payment.paymentservice.infrastructure.mapper;

import com.lmf.payment.paymentservice.application.event.PaymentCreatedEvent;
import com.lmf.payment.paymentservice.domain.model.Payment;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class PaymentCreatedEventMapper {

    public PaymentCreatedEvent toEvent(Payment payment) {

        return new PaymentCreatedEvent(UUID.randomUUID(), "PAYMENT_CREATED", "v1", OffsetDateTime.now(), payment.getId(), payment.getOrderId(), payment.getCustomerId(), payment.getAmount(), payment.getCurrency(), payment.getPaymentMethod(), payment.getInstallments(), payment.getPaymentStatus(), payment.getProvider(), payment.getTransactionId(), payment.getGatewayStatus());
    }
}
