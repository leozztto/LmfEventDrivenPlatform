package com.lmf.payment.paymentservice.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.payment.paymentservice.application.event.PaymentCreatedEvent;
import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.domain.repository.OutboxEventRepository;
import com.lmf.payment.paymentservice.infrastructure.mapper.PaymentCreatedEventMapper;
import com.lmf.payment.paymentservice.infrastructure.outbox.OutboxStatus;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.OutboxEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentEventService {

    private final PaymentCreatedEventMapper paymentCreatedEventMapper;

    private final OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper;

    public void publishPaymentCreated(Payment payment) {

        try {

            PaymentCreatedEvent paymentCreatedEvent = paymentCreatedEventMapper.toEvent(payment);

            String payload = objectMapper.writeValueAsString(paymentCreatedEvent);

            OutboxEventEntity outboxEventEntity = new OutboxEventEntity(payment.getId(), "PAYMENT", "PAYMENT_CREATED", payload, OutboxStatus.PENDING);

            outboxEventRepository.save(outboxEventEntity);

        } catch (JsonProcessingException ex) {

            throw new RuntimeException("Failed to create outbox event", ex);
        }
    }
}
