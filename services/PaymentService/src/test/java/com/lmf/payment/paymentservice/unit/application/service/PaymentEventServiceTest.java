package com.lmf.payment.paymentservice.unit.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.payment.paymentservice.application.service.PaymentEventService;
import com.lmf.payment.paymentservice.domain.exception.EventSerializationException;
import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import com.lmf.payment.paymentservice.domain.model.PaymentStatus;
import com.lmf.payment.paymentservice.domain.repository.OutboxEventRepository;
import com.lmf.payment.paymentservice.infrastructure.outbox.OutboxStatus;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.OutboxEventEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentEventServiceTest {

    private OutboxEventRepository outboxEventRepository;
    private ObjectMapper objectMapper;

    private PaymentEventService paymentEventService;

    @BeforeEach
    void setUp() {

        outboxEventRepository = mock(OutboxEventRepository.class);

        objectMapper = mock(ObjectMapper.class);

        paymentEventService = new PaymentEventService(outboxEventRepository, objectMapper);
    }

    @Test
    @DisplayName("Deve publicar evento de processamento")
    void shouldPublishProcessingEvent() throws Exception {

        Payment payment = mock(Payment.class);

        UUID paymentId = UUID.randomUUID();

        when(payment.getId()).thenReturn(paymentId);
        when(payment.getOrderId()).thenReturn(UUID.randomUUID());
        when(payment.getCustomerId()).thenReturn(UUID.randomUUID());
        when(payment.getAmount()).thenReturn(BigDecimal.valueOf(100));
        when(payment.getCurrency()).thenReturn("BRL");
        when(payment.getPaymentMethod()).thenReturn(PaymentMethod.PIX);
        when(payment.getPaymentStatus()).thenReturn(PaymentStatus.PENDING);

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"status\":\"PENDING\"}");

        paymentEventService.publish(payment);

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);

        verify(outboxEventRepository).save(captor.capture());

        OutboxEventEntity outboxEventEntity = captor.getValue();

        assertEquals(paymentId, outboxEventEntity.getAggregateId());
        assertEquals("PAYMENT", outboxEventEntity.getAggregateType());
        assertEquals("PAYMENT_PROCESSING", outboxEventEntity.getEventType());
        assertEquals("{\"status\":\"PENDING\"}", outboxEventEntity.getPayload());
        assertEquals(OutboxStatus.PENDING, outboxEventEntity.getOutboxStatus());
    }

    @Test
    @DisplayName("Deve publicar evento aprovado")
    void shouldPublishApprovedEvent() throws Exception {

        Payment payment = mock(Payment.class);

        UUID paymentId = UUID.randomUUID();

        when(payment.getId()).thenReturn(paymentId);
        when(payment.getOrderId()).thenReturn(UUID.randomUUID());
        when(payment.getCustomerId()).thenReturn(UUID.randomUUID());
        when(payment.getAmount()).thenReturn(BigDecimal.valueOf(500));
        when(payment.getCurrency()).thenReturn("BRL");
        when(payment.getPaymentMethod()).thenReturn(PaymentMethod.CREDIT_CARD);

        when(payment.getPaymentStatus()).thenReturn(PaymentStatus.APPROVED);

        when(payment.getTransactionId()).thenReturn("tx-123");
        when(payment.getProvider()).thenReturn("MERCADO_PAGO");
        when(payment.getPaidAt()).thenReturn(OffsetDateTime.now());

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"status\":\"APPROVED\"}");

        paymentEventService.publish(payment);

        ArgumentCaptor<OutboxEventEntity> argumentCaptor = ArgumentCaptor.forClass(OutboxEventEntity.class);

        verify(outboxEventRepository).save(argumentCaptor.capture());

        OutboxEventEntity outboxEventEntity = argumentCaptor.getValue();

        assertEquals("PAYMENT_APPROVED", outboxEventEntity.getEventType());
        assertEquals("{\"status\":\"APPROVED\"}", outboxEventEntity.getPayload());
    }

    @Test
    @DisplayName("Deve publicar evento falho")
    void shouldPublishFailedEvent() throws Exception {

        Payment payment = mock(Payment.class);

        UUID paymentId = UUID.randomUUID();

        when(payment.getId()).thenReturn(paymentId);
        when(payment.getOrderId()).thenReturn(UUID.randomUUID());
        when(payment.getCustomerId()).thenReturn(UUID.randomUUID());
        when(payment.getAmount()).thenReturn(BigDecimal.valueOf(900));
        when(payment.getCurrency()).thenReturn("BRL");
        when(payment.getPaymentMethod()).thenReturn(PaymentMethod.MERCAD_PAGO);

        when(payment.getPaymentStatus()).thenReturn(PaymentStatus.FAILED);

        when(payment.getFailureReason()).thenReturn("Gateway unavailable");
        when(payment.getGatewayStatus()).thenReturn("FAILED");
        when(payment.getFailedAt()).thenReturn(OffsetDateTime.now());

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"status\":\"FAILED\"}");

        paymentEventService.publish(payment);

        ArgumentCaptor<OutboxEventEntity> argumentCaptor = ArgumentCaptor.forClass(OutboxEventEntity.class);

        verify(outboxEventRepository).save(argumentCaptor.capture());

        OutboxEventEntity outboxEventEntity = argumentCaptor.getValue();

        assertEquals("PAYMENT_FAILED", outboxEventEntity.getEventType());
        assertEquals("{\"status\":\"FAILED\"}", outboxEventEntity.getPayload());
    }

    @Test
    @DisplayName("Deve lançar EventSerializationException quando falhar serializacao")
    void shouldThrowEventSerializationException() throws Exception {

        Payment payment = mock(Payment.class);

        when(payment.getId()).thenReturn(UUID.randomUUID());
        when(payment.getOrderId()).thenReturn(UUID.randomUUID());
        when(payment.getCustomerId()).thenReturn(UUID.randomUUID());
        when(payment.getAmount()).thenReturn(BigDecimal.valueOf(100));
        when(payment.getCurrency()).thenReturn("BRL");
        when(payment.getPaymentMethod()).thenReturn(PaymentMethod.PIX);

        when(payment.getPaymentStatus()).thenReturn(PaymentStatus.PENDING);

        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("serialization error") {
        });

        EventSerializationException eventSerializationException = assertThrows(EventSerializationException.class, () -> paymentEventService.publish(payment));

        assertEquals("Failed to serialize event", eventSerializationException.getMessage());

        verify(outboxEventRepository, never()).save(any());
    }
}

