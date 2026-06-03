package com.lmf.payment.paymentservice.unit.infrasctruture.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.payment.paymentservice.domain.repository.OutboxEventRepository;
import com.lmf.payment.paymentservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.payment.paymentservice.infrastructure.kafka.outbox.OutboxEventProcessor;
import com.lmf.payment.paymentservice.infrastructure.kafka.outbox.PaymentEventPublisher;
import com.lmf.payment.paymentservice.infrastructure.outbox.OutboxStatus;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.OutboxEventEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventProcessorTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxEventProcessor outboxEventProcessor;

    private OutboxEventEntity outboxEventEntity;

    @BeforeEach
    void setUp() throws Exception {

        outboxEventEntity = new OutboxEventEntity(UUID.randomUUID(), "PAYMENT", "PAYMENT_CREATED", """
                {
                  "paymentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                  "orderId": "7fa85f64-5717-4562-b3fc-2c963f66afb1",
                  "status": "APPROVED",
                  "amount": 299.90
                }
                """, OutboxStatus.PENDING);
    }

    @Test
    void shouldProcessAndPublishPendingEventsSuccessfully() {

        when(outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)).thenReturn(List.of(outboxEventEntity));

        outboxEventProcessor.process();

        verify(outboxEventRepository, times(2)).update(outboxEventEntity);

        verify(paymentEventPublisher).publish(eq(KafkaTopics.PAYMENT_PROCESSED), eq(outboxEventEntity.getAggregateId().toString()), eq(outboxEventEntity.getPayload()));

        assertEquals(OutboxStatus.PUBLISHED, outboxEventEntity.getOutboxStatus());
    }

    @Test
    void shouldDoNothingWhenNoPendingEventsFound() {

        when(outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)).thenReturn(List.of());

        outboxEventProcessor.process();

        verify(outboxEventRepository, never()).update(any());
        verify(paymentEventPublisher, never()).publish(anyString(), anyString(), anyString());
    }

    @Test
    void shouldMarkEventAsPendingRetryWhenPublishingFails() {

        when(outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)).thenReturn(List.of(outboxEventEntity));

        doThrow(new RuntimeException("Kafka unavailable")).when(paymentEventPublisher).publish(anyString(), anyString(), anyString());

        outboxEventProcessor.process();

        verify(outboxEventRepository, atLeastOnce()).update(outboxEventEntity);
        verify(outboxEventRepository).save(outboxEventEntity);

        assertEquals(OutboxStatus.PENDING, outboxEventEntity.getOutboxStatus());
        assertEquals(1, outboxEventEntity.getRetryCount());
    }

    @Test
    void shouldPublishToDltWhenRetryLimitExceeded() throws Exception {

        setField("retryCount", 3);

        when(outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)).thenReturn(List.of(outboxEventEntity));

        doThrow(new RuntimeException("Permanent failure")).when(paymentEventPublisher).publish(eq(KafkaTopics.PAYMENT_PROCESSED), anyString(), anyString());

        outboxEventProcessor.process();

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);

        verify(paymentEventPublisher, times(2)).publish(topicCaptor.capture(), anyString(), anyString());

        List<String> publishedTopics = topicCaptor.getAllValues();

        assertEquals(KafkaTopics.PAYMENT_PROCESSED, publishedTopics.get(0));
        assertEquals(KafkaTopics.PAYMENT_FAILED_DLT, publishedTopics.get(1));

        assertEquals(OutboxStatus.DLT, outboxEventEntity.getOutboxStatus());
    }

    private void setField(String fieldName, Object value) throws Exception {

        Field field = OutboxEventEntity.class.getDeclaredField(fieldName);

        field.setAccessible(true);

        field.set(outboxEventEntity, value);
    }
}