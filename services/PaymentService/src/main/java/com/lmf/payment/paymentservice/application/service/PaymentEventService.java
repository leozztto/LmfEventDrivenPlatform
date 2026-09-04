package com.lmf.payment.paymentservice.application.service;

import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.platform.contracts.PaymentApprovedEvent;
import com.lmf.platform.contracts.PaymentFailedEvent;
import com.lmf.platform.contracts.PaymentMethod;
import com.lmf.platform.messaging.OutboxWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventService {

    private static final String EVENT_VERSION = "v1";

    private final OutboxWriter outboxWriter;

    public void publish(Payment payment) {

        switch (payment.getPaymentStatus()) {

            case APPROVED -> publishApproved(payment);

            case FAILED -> publishFailed(payment);

            default -> log.warn("No outbox event mapped for payment status. paymentId={}, status={}", payment.getId(), payment.getPaymentStatus());
        }
    }

    private void publishApproved(Payment payment) {

        PaymentApprovedEvent event = new PaymentApprovedEvent(
                UUID.randomUUID(), PaymentApprovedEvent.TYPE, EVENT_VERSION, OffsetDateTime.now(),
                payment.getId(), payment.getOrderId(), payment.getCustomerId(),
                payment.getAmount(), payment.getCurrency(), toContractMethod(payment),
                payment.getTransactionId(), payment.getProvider());

        outboxWriter.write(payment.getId(), "PAYMENT", PaymentApprovedEvent.TYPE, event);
    }

    private void publishFailed(Payment payment) {

        PaymentFailedEvent event = new PaymentFailedEvent(
                UUID.randomUUID(), PaymentFailedEvent.TYPE, EVENT_VERSION, OffsetDateTime.now(),
                payment.getId(), payment.getOrderId(), payment.getCustomerId(),
                payment.getAmount(), payment.getCurrency(), toContractMethod(payment),
                payment.getFailureReason(), payment.getGatewayStatus());

        outboxWriter.write(payment.getId(), "PAYMENT", PaymentFailedEvent.TYPE, event);
    }

    private PaymentMethod toContractMethod(Payment payment) {

        return PaymentMethod.valueOf(payment.getPaymentMethod().name());
    }
}
