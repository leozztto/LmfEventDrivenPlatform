package com.lmf.inventory.inventoryservice.infrastructure.kafka.consumer;

import com.lmf.inventory.inventoryservice.application.service.InboxEventService;
import com.lmf.inventory.inventoryservice.domain.event.EventMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbstractInboxConsumerTest {

    private InboxEventService inboxEventService;

    private TestConsumer consumer;

    @BeforeEach
    void setup() {

        inboxEventService = mock(InboxEventService.class);

        consumer = new TestConsumer(inboxEventService);
    }

    @Test
    @DisplayName("Should ignore duplicate event")
    void shouldIgnoreDuplicateEvent() {

        TestEvent testEvent = new TestEvent();

        when(inboxEventService.isDuplicate(testEvent.eventId().toString())).thenReturn(true);

        consumer.processEvent(testEvent);

        verify(inboxEventService).isDuplicate(testEvent.eventId().toString());

        verify(inboxEventService, never()).register(any(), any(), any());

        verify(inboxEventService, never()).markProcessed(any());

        verify(inboxEventService, never()).markFailed(any(), any());
    }

    @Test
    @DisplayName("Should process event successfully")
    void shouldProcessEventSuccessfully() {

        TestEvent testEvent = new TestEvent();

        when(inboxEventService.isDuplicate(any())).thenReturn(false);

        consumer.processEvent(testEvent);

        verify(inboxEventService).register(testEvent.eventId().toString(), testEvent.aggregateId(), testEvent.eventType());

        verify(inboxEventService).markProcessed(testEvent.eventId().toString());

        verify(inboxEventService, never()).markFailed(any(), any());
    }

    @Test
    @DisplayName("Should register before processing")
    void shouldRegisterBeforeProcessing() {

        TestEvent testEvent = new TestEvent();

        when(inboxEventService.isDuplicate(any())).thenReturn(false);

        consumer.processEvent(testEvent);

        verify(inboxEventService).register(eq(testEvent.eventId().toString()), eq(testEvent.aggregateId()), eq(testEvent.eventType()));
    }

    @Test
    @DisplayName("Should mark failed when exception occurs")
    void shouldMarkFailedWhenExceptionOccurs() {

        TestEvent testEvent = new TestEvent();

        when(inboxEventService.isDuplicate(any())).thenReturn(false);

        RuntimeException exception = new RuntimeException("processing error");

        assertThatThrownBy(() -> consumer.processEventWithException(testEvent, exception)).isSameAs(exception);

        verify(inboxEventService).markFailed(testEvent.eventId().toString(), "processing error");

        verify(inboxEventService, never()).markProcessed(any());
    }

    @Test
    @DisplayName("Should use exception class name when message is null")
    void shouldUseExceptionClassNameWhenMessageIsNull() {

        TestEvent testEvent = new TestEvent();

        when(inboxEventService.isDuplicate(any())).thenReturn(false);

        RuntimeException exception = new RuntimeException((String) null);

        assertThatThrownBy(() -> consumer.processEventWithException(testEvent, exception)).isSameAs(exception);

        verify(inboxEventService).markFailed(testEvent.eventId().toString(), "RuntimeException");
    }

    @Test
    @DisplayName("Should execute processor")
    void shouldExecuteProcessor() {

        TestEvent testEvent = new TestEvent();

        when(inboxEventService.isDuplicate(any())).thenReturn(false);

        consumer.processEvent(testEvent);

        assertThat(consumer.processorExecuted).isTrue();
    }

    private static class TestConsumer extends AbstractInboxConsumer<TestEvent> {

        boolean processorExecuted = false;

        TestConsumer(InboxEventService inboxEventService) {
            super(inboxEventService);
        }

        void processEvent(TestEvent event) {

            process(event, event.aggregateId(), e -> processorExecuted = true);
        }

        void processEventWithException(TestEvent event, RuntimeException exception) {

            process(event, event.aggregateId(), e -> {
                throw exception;
            });
        }
    }

    private static class TestEvent implements EventMessage {

        private final UUID eventId = UUID.randomUUID();

        private final UUID aggregateId = UUID.randomUUID();

        @Override
        public UUID eventId() {
            return eventId;
        }

        @Override
        public String eventType() {
            return "ORDER_CREATED";
        }

        UUID aggregateId() {
            return aggregateId;
        }
    }
}
