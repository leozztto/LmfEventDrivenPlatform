package com.lmf.payment.paymentservice.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.payment.paymentservice.application.event.PaymentApprovedEvent;
import com.lmf.payment.paymentservice.application.event.PaymentFailedEvent;
import com.lmf.payment.paymentservice.application.event.PaymentProcessingEvent;
import com.lmf.payment.paymentservice.domain.exception.EventSerializationException;
import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.domain.repository.OutboxEventRepository;
import com.lmf.payment.paymentservice.infrastructure.outbox.OutboxStatus;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.OutboxEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentEventService {

    private final OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper;

    public void publish(Payment payment) {

        switch (payment.getPaymentStatus()) {

            case PENDING -> publishProcessing(payment);

            case APPROVED -> publishApproved(payment);

            case FAILED -> publishFailed(payment);
        }
    }

    private void publishProcessing(Payment payment) {

        PaymentProcessingEvent event = new PaymentProcessingEvent(payment.getId(), payment.getOrderId(), payment.getCustomerId(), payment.getAmount(), payment.getCurrency(), payment.getPaymentMethod(), OffsetDateTime.now());

        saveOutbox(payment.getId(), "PAYMENT_PROCESSING", event);
    }

    private void publishApproved(Payment payment) {

        PaymentApprovedEvent event = new PaymentApprovedEvent(payment.getId(), payment.getOrderId(), payment.getCustomerId(), payment.getAmount(), payment.getCurrency(), payment.getPaymentMethod(), payment.getTransactionId(), payment.getProvider(), payment.getPaidAt());

        saveOutbox(payment.getId(), "PAYMENT_APPROVED", event);
    }

    private void publishFailed(Payment payment) {

        PaymentFailedEvent event = new PaymentFailedEvent(payment.getId(), payment.getOrderId(), payment.getCustomerId(), payment.getAmount(), payment.getCurrency(), payment.getPaymentMethod(), payment.getFailureReason(), payment.getGatewayStatus(), payment.getFailedAt());

        saveOutbox(payment.getId(), "PAYMENT_FAILED", event);
    }

    private void saveOutbox(UUID aggregateId, String eventType, Object payloadObject) {

        try {

            String payload = objectMapper.writeValueAsString(payloadObject);

            OutboxEventEntity outbox = new OutboxEventEntity(aggregateId, "PAYMENT", eventType, payload, OutboxStatus.PENDING);

            outboxEventRepository.save(outbox);

        } catch (JsonProcessingException ex) {

            throw new EventSerializationException("Failed to serialize event", ex);
        }
    }
}
