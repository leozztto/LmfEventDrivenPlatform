package com.lmf.payment.paymentservice.unit.infrasctruture.kafka;

import com.lmf.payment.paymentservice.application.command.ProcessPaymentCommand;
import com.lmf.payment.paymentservice.application.service.InboxEventService;
import com.lmf.payment.paymentservice.application.usecase.ProcessPaymentUseCase;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import com.lmf.payment.paymentservice.events.InventoryReservedEvent;
import com.lmf.payment.paymentservice.events.PaymentInfo;
import com.lmf.payment.paymentservice.events.ReservedItem;
import com.lmf.payment.paymentservice.infrastructure.kafka.consumer.InventoryReservedConsumer;
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

    private InboxEventService inboxEventService;

    private InventoryReservedConsumer inventoryReservedConsumer;

    @BeforeEach
    void setUp() {

        processPaymentUseCase = mock(ProcessPaymentUseCase.class);

        inboxEventService = mock(InboxEventService.class);

        inventoryReservedConsumer = new InventoryReservedConsumer(inboxEventService, processPaymentUseCase);
    }

    @Test
    @DisplayName("Deve processar evento com sucesso")
    void shouldProcessEventSuccessfully() {

        InventoryReservedEvent event = buildEvent();

        when(inboxEventService.isDuplicate(event.eventId().toString())).thenReturn(false);

        inventoryReservedConsumer.consume(event);

        verify(inboxEventService).register(event.eventId().toString(), event.orderId(), event.eventType());

        verify(processPaymentUseCase).execute(any(ProcessPaymentCommand.class));

        verify(inboxEventService).markProcessed(event.eventId().toString());

        verify(inboxEventService, never()).markFailed(any(), any());
    }

    @Test
    @DisplayName("Deve ignorar evento duplicado")
    void shouldIgnoreDuplicatedEvent() {

        InventoryReservedEvent event = buildEvent();

        when(inboxEventService.isDuplicate(event.eventId().toString())).thenReturn(true);

        inventoryReservedConsumer.consume(event);

        verify(inboxEventService).isDuplicate(event.eventId().toString());

        verify(inboxEventService, never()).register(any(), any(), any());

        verifyNoInteractions(processPaymentUseCase);

        verify(inboxEventService, never()).markProcessed(any());

        verify(inboxEventService, never()).markFailed(any(), any());
    }

    @Test
    @DisplayName("Deve marcar evento como falho quando ocorrer erro")
    void shouldMarkEventAsFailedWhenErrorOccurs() {

        InventoryReservedEvent event = buildEvent();

        when(inboxEventService.isDuplicate(event.eventId().toString())).thenReturn(false);

        doThrow(new RuntimeException("Gateway timeout")).when(processPaymentUseCase).execute(any(ProcessPaymentCommand.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> inventoryReservedConsumer.consume(event));

        assertEquals("Gateway timeout", exception.getMessage());

        verify(inboxEventService).markFailed(event.eventId().toString(), "Gateway timeout");

        verify(inboxEventService, never()).markProcessed(any());
    }

    @Test
    @DisplayName("Deve converter InventoryReservedEvent para ProcessPaymentCommand")
    void shouldConvertEventToProcessPaymentCommandCorrectly() {

        InventoryReservedEvent event = buildEvent();

        when(inboxEventService.isDuplicate(event.eventId().toString())).thenReturn(false);

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

        assertEquals(event.payment().paymentMethod(), command.paymentMethod());

        assertEquals(event.payment().installments(), command.installments());
    }

    private InventoryReservedEvent buildEvent() {

        UUID eventId = UUID.randomUUID();

        UUID orderId = UUID.randomUUID();

        UUID customerId = UUID.randomUUID();

        return new InventoryReservedEvent(eventId, "INVENTORY_RESERVED", "1.0", OffsetDateTime.now(ZoneOffset.UTC), orderId, customerId, new BigDecimal("299.90"), new PaymentInfo(PaymentMethod.CREDIT_CARD, 3, new BigDecimal("299.90")), List.of(new ReservedItem(UUID.randomUUID(), 1), new ReservedItem(UUID.randomUUID(), 2)));
    }
}