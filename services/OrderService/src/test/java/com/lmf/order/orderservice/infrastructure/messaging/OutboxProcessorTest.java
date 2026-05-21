package com.lmf.order.orderservice.infrastructure.messaging;

import com.lmf.order.orderservice.domain.model.outbox.OutboxStatus;
import com.lmf.order.orderservice.domain.repository.OutboxEventRepository;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.lmf.order.orderservice.support.factory.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxProcessorTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OutboxProcessor outboxProcessor;

    @Test
    void shouldMarkEventAsPublished() {

        OutboxEventEntity outboxEventEntity = TestDataFactory.createOutboxEvent();

        when(outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)).thenReturn(List.of(outboxEventEntity));

        outboxProcessor.process();

        assertEquals(OutboxStatus.PUBLISHED, outboxEventEntity.getOutboxStatus());

        verify(outboxEventRepository, times(2)).update(outboxEventEntity);
    }
}
