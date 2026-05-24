package com.lmf.payment.paymentservice.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.payment.paymentservice.application.command.ProcessPaymentCommand;
import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.application.event.PaymentCreatedEvent;
import com.lmf.payment.paymentservice.domain.repository.PaymentRepository;
import com.lmf.payment.paymentservice.infrastructure.mapper.PaymentCreatedEventMapper;
import com.lmf.payment.paymentservice.infrastructure.outbox.OutboxStatus;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.lmf.payment.paymentservice.infrastructure.persistence.mapper.PaymentEntityMapper;
import com.lmf.payment.paymentservice.domain.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPaymentUseCase {

    private final PaymentRepository paymentRepository;

    private final PaymentCreatedEventMapper paymentCreatedEventMapper;

    private final OutboxEventRepository outboxEventRepository;

    private final PaymentEntityMapper paymentEntityMapper;

    private final ObjectMapper objectMapper;

    @Transactional
    public void execute(ProcessPaymentCommand processPaymentCommand) {

        log.info("Creating payment. orderId={}, amount={}, paymentMethod={}, installments={}", processPaymentCommand.orderId(), processPaymentCommand.amount(), processPaymentCommand.paymentMethod(), processPaymentCommand.installments());

        paymentRepository.findByOrderId(processPaymentCommand.orderId()).ifPresent(payment -> {

            throw new IllegalStateException("Payment already exists");
        });

        Payment payment = PaymentEntityMapper.toDomain(processPaymentCommand);

        paymentRepository.save(payment);

        log.info("Payment created successfully. paymentId={}, orderId={}, amount={}", payment.getId(), payment.getOrderId(), payment.getAmount());

        createOutboxEvent(payment);

    }

    private void createOutboxEvent(Payment payment) {

        try {

            PaymentCreatedEvent paymentCreatedEvent = paymentCreatedEventMapper.toEvent(payment);

            String payload = objectMapper.writeValueAsString(paymentCreatedEvent);

            OutboxEventEntity outboxEventEntity = new OutboxEventEntity(payment.getId(), "PAYMENT", "PAYMENT_CREATED", payload, OutboxStatus.PENDING);

            outboxEventRepository.save(outboxEventEntity);

            log.info("Outbox created successfully. eventId={}, payload={}", outboxEventEntity.getId(), outboxEventEntity.getPayload());

        } catch (JsonProcessingException ex) {

            throw new RuntimeException("Failed to create outbox event", ex);
        }
    }
}