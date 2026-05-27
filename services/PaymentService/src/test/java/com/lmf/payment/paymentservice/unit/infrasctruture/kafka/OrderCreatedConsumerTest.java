package com.lmf.payment.paymentservice.unit.infrasctruture.kafka;

import com.lmf.payment.paymentservice.application.command.ProcessPaymentCommand;
import com.lmf.payment.paymentservice.application.service.InboxEventService;
import com.lmf.payment.paymentservice.application.usecase.ProcessPaymentUseCase;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import com.lmf.payment.paymentservice.events.*;
import com.lmf.payment.paymentservice.infrastructure.kafka.consumer.OrderCreatedConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class OrderCreatedConsumerTest {

    private ProcessPaymentUseCase processPaymentUseCase;

    private InboxEventService inboxEventService;

    private OrderCreatedConsumer orderCreatedConsumer;

    @BeforeEach
    void setUp() {

        processPaymentUseCase = mock(ProcessPaymentUseCase.class);

        inboxEventService = mock(InboxEventService.class);

        orderCreatedConsumer = new OrderCreatedConsumer(processPaymentUseCase, inboxEventService);
    }

    @Test
    @DisplayName("Deve processar evento com sucesso")
    void shouldProcessEventSuccessfully() {

        OrderCreatedEvent orderCreatedEvent = buildEvent();

        when(inboxEventService.isDuplicate(orderCreatedEvent.eventId().toString())).thenReturn(false);

        orderCreatedConsumer.consume(orderCreatedEvent, orderCreatedEvent.orderId().toString());

        verify(inboxEventService).register(orderCreatedEvent.eventId().toString(), orderCreatedEvent.orderId(), "ORDER_CREATED");

        verify(processPaymentUseCase).execute(any(ProcessPaymentCommand.class));

        verify(inboxEventService).markProcessed(orderCreatedEvent.eventId().toString());

        verify(inboxEventService, never()).markFailed(any(), any());
    }

    @Test
    @DisplayName("Deve ignorar evento duplicado")
    void shouldIgnoreDuplicatedEvent() {

        OrderCreatedEvent orderCreatedEvent = buildEvent();

        when(inboxEventService.isDuplicate(orderCreatedEvent.eventId().toString())).thenReturn(true);

        orderCreatedConsumer.consume(orderCreatedEvent, orderCreatedEvent.orderId().toString());

        verify(inboxEventService).isDuplicate(orderCreatedEvent.eventId().toString());

        verify(inboxEventService, never()).register(any(), any(), any());

        verifyNoInteractions(processPaymentUseCase);

        verify(inboxEventService, never()).markProcessed(any());

        verify(inboxEventService, never()).markFailed(any(), any());
    }

    @Test
    @DisplayName("Deve marcar evento como falho quando ocorrer erro")
    void shouldMarkEventAsFailedWhenErrorOccurs() {

        OrderCreatedEvent orderCreatedEvent = buildEvent();

        when(inboxEventService.isDuplicate(orderCreatedEvent.eventId().toString())).thenReturn(false);

        doThrow(new RuntimeException("Gateway timeout")).when(processPaymentUseCase).execute(any(ProcessPaymentCommand.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderCreatedConsumer.consume(orderCreatedEvent, orderCreatedEvent.orderId().toString()));

        assertEquals("Gateway timeout", exception.getMessage());

        verify(inboxEventService).markFailed(orderCreatedEvent.eventId().toString(), "Gateway timeout");

        verify(inboxEventService, never()).markProcessed(any());
    }

    @Test
    @DisplayName("Deve converter evento para ProcessPaymentCommand corretamente")
    void shouldConvertEventToProcessPaymentCommandCorrectly() {

        OrderCreatedEvent orderCreatedEvent = buildEvent();

        when(inboxEventService.isDuplicate(orderCreatedEvent.eventId().toString())).thenReturn(false);

        ArgumentCaptor<ProcessPaymentCommand> captor = ArgumentCaptor.forClass(ProcessPaymentCommand.class);

        orderCreatedConsumer.consume(orderCreatedEvent, orderCreatedEvent.orderId().toString());

        verify(processPaymentUseCase).execute(captor.capture());

        ProcessPaymentCommand command = captor.getValue();

        assertEquals(orderCreatedEvent.orderId(), command.orderId());

        assertEquals(orderCreatedEvent.customer().customerId(), command.customerId());

        assertEquals(orderCreatedEvent.totalAmount(), command.amount());

        assertEquals("BRL", command.currency());

        assertEquals(orderCreatedEvent.payment().paymentMethod(), command.paymentMethod());

        assertEquals(orderCreatedEvent.payment().installments(), command.installments());
    }

    private OrderCreatedEvent buildEvent() {

        UUID eventId = UUID.randomUUID();

        UUID orderId = UUID.randomUUID();

        UUID customerId = UUID.randomUUID();

        return new OrderCreatedEvent(eventId, "ORDER_CREATED", "1.0", OffsetDateTime.now(ZoneOffset.UTC), orderId, "CREATED", new BigDecimal("299.90"), new CustomerInfo(customerId, "Leandro Franceschetto", "leandro@email.com", "11999999999"), new ShippingAddress("Rua das Flores", "123", "São Paulo", "SP", "01010-000"), new PaymentInfo(PaymentMethod.fromName("CREDIT_CARD"), 3, new BigDecimal("299.90")), List.of(new OrderItem(UUID.randomUUID(), 1, new BigDecimal("250.00")), new OrderItem(UUID.randomUUID(), 1, new BigDecimal("49.90"))));
    }
}