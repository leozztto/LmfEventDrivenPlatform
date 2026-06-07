package com.lmf.payment.paymentservice.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.payment.paymentservice.domain.model.event.PaymentApprovedEvent;
import com.lmf.payment.paymentservice.domain.model.event.PaymentFailedEvent;
import com.lmf.payment.paymentservice.domain.model.event.PaymentProcessingEvent;
import com.lmf.payment.paymentservice.domain.exception.EventSerializationException;
import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.domain.repository.OutboxEventRepository;
import com.lmf.payment.paymentservice.infrastructure.outbox.OutboxStatus;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.OutboxEventEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
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

        PaymentProcessingEvent paymentProcessingEvent = new PaymentProcessingEvent(payment.getId(), payment.getOrderId(), payment.getCustomerId(), payment.getAmount(), payment.getCurrency(), payment.getPaymentMethod(), OffsetDateTime.now());

        saveOutbox(payment.getId(), "PAYMENT_PROCESSING", paymentProcessingEvent);
    }

    private void publishApproved(Payment payment) {

        PaymentApprovedEvent paymentApprovedEvent = new PaymentApprovedEvent(payment.getId(), payment.getOrderId(), payment.getCustomerId(), payment.getAmount(), payment.getCurrency(), payment.getPaymentMethod(), payment.getTransactionId(), payment.getProvider(), payment.getPaidAt());

        saveOutbox(payment.getId(), "PAYMENT_APPROVED", paymentApprovedEvent);
    }

    private void publishFailed(Payment payment) {

        PaymentFailedEvent paymentFailedEvent = new PaymentFailedEvent(payment.getId(), payment.getOrderId(), payment.getCustomerId(), payment.getAmount(), payment.getCurrency(), payment.getPaymentMethod(), payment.getFailureReason(), payment.getGatewayStatus(), payment.getFailedAt());

        saveOutbox(payment.getId(), "PAYMENT_FAILED", paymentFailedEvent);
    }

    private void saveOutbox(UUID aggregateId, String eventType, Object payloadObject) {

        try {

            String payload = objectMapper.writeValueAsString(payloadObject);

            OutboxEventEntity outboxEventEntity = new OutboxEventEntity(aggregateId, "PAYMENT", eventType, payload, OutboxStatus.PENDING);

            outboxEventRepository.save(outboxEventEntity);

            log.info("Outbox event created. eventType={}, eventId={}", "PAYMENT_CREATED", outboxEventEntity.getId());

        } catch (JsonProcessingException ex) {

            log.error("Failed to serialize payment created event. payment={}", payloadObject, ex);

            throw new EventSerializationException("Failed to serialize event", ex);
        }
    }
}
