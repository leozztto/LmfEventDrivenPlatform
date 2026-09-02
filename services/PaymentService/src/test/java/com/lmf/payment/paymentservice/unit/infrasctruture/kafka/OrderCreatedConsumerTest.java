package com.lmf.payment.paymentservice.unit.infrasctruture.kafka;

import com.lmf.payment.paymentservice.application.command.ProcessPaymentCommand;
import com.lmf.platform.messaging.InboxService;
import com.lmf.payment.paymentservice.application.usecase.ProcessPaymentUseCase;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import com.lmf.payment.paymentservice.infrastructure.kafka.consumer.InventoryReservedConsumer;
import com.lmf.platform.contracts.InventoryReservedEvent;
import com.lmf.platform.contracts.PaymentInfo;
import com.lmf.platform.contracts.ReservedItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryReservedConsumerTest {

    private ProcessPaymentUseCase processPaymentUseCase;

    private InboxService inboxEventService;

    private InventoryReservedConsumer inventoryReservedConsumer;

    @BeforeEach
    void setUp() {

        processPaymentUseCase = mock(ProcessPaymentUseCase.class);

        inboxEventService = mock(InboxService.class);

        inventoryReservedConsumer = new InventoryReservedConsumer(inboxEventService, processPaymentUseCase);
    }

    @Test
    @DisplayName("Deve registrar, processar e marcar como processado no caminho feliz")
    void shouldProcessEventSuccessfully() {

        InventoryReservedEvent event = buildEvent();

        when(inboxEventService.isAlreadyProcessed(event.eventId().toString())).thenReturn(false);

        inventoryReservedConsumer.consume(event);

        verify(inboxEventService).register(event.eventId().toString(), event.orderId(), event.eventType());
        verify(processPaymentUseCase).execute(any(ProcessPaymentCommand.class));
        verify(inboxEventService).markProcessed(event.eventId().toString());
    }

    @Test
    @DisplayName("Deve ignorar evento já processado")
    void shouldIgnoreAlreadyProcessedEvent() {

        InventoryReservedEvent event = buildEvent();

        when(inboxEventService.isAlreadyProcessed(event.eventId().toString())).thenReturn(true);

        inventoryReservedConsumer.consume(event);

        verify(inboxEventService).isAlreadyProcessed(event.eventId().toString());
        verify(inboxEventService, never()).register(any(), any(), any());
        verifyNoInteractions(processPaymentUseCase);
        verify(inboxEventService, never()).markProcessed(any());
    }

    @Test
    @DisplayName("Deve propagar a exceção e não marcar como processado quando o caso de uso falha")
    void shouldPropagateExceptionWhenUseCaseFails() {

        InventoryReservedEvent event = buildEvent();

        when(inboxEventService.isAlreadyProcessed(event.eventId().toString())).thenReturn(false);

        doThrow(new RuntimeException("Gateway timeout")).when(processPaymentUseCase).execute(any(ProcessPaymentCommand.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> inventoryReservedConsumer.consume(event));

        assertEquals("Gateway timeout", exception.getMessage());
        verify(inboxEventService).register(event.eventId().toString(), event.orderId(), event.eventType());
        verify(inboxEventService, never()).markProcessed(any());
    }

    @Test
    @DisplayName("Deve converter InventoryReservedEvent para ProcessPaymentCommand")
    void shouldConvertEventToProcessPaymentCommandCorrectly() {

        InventoryReservedEvent event = buildEvent();

        when(inboxEventService.isAlreadyProcessed(event.eventId().toString())).thenReturn(false);

        ArgumentCaptor<ProcessPaymentCommand> captor = ArgumentCaptor.forClass(ProcessPaymentCommand.class);

        inventoryReservedConsumer.consume(event);

        verify(processPaymentUseCase).execute(captor.capture());

        ProcessPaymentCommand command = captor.getValue();

        assertEquals(event.orderId(), command.orderId());
        assertEquals(event.eventId(), command.eventId());
        assertEquals(event.eventType(), command.eventType());
        assertEquals(event.customerId(), command.customerId());
        assertEquals(event.totalAmount(), command.amount());
        assertEquals("BRL", command.currency());
        assertEquals(event.payment().paymentMethod().name(), command.paymentMethod().name());
        assertEquals(event.payment().installments(), command.installments());
    }

    private InventoryReservedEvent buildEvent() {

        return new InventoryReservedEvent(
                UUID.randomUUID(),
                InventoryReservedEvent.TYPE,
                "v1",
                OffsetDateTime.now(ZoneOffset.UTC),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("299.90"),
                new PaymentInfo(com.lmf.platform.contracts.PaymentMethod.CREDIT_CARD, 3, new BigDecimal("299.90")),
                List.of(new ReservedItem(UUID.randomUUID(), 1), new ReservedItem(UUID.randomUUID(), 2)));
    }
}
