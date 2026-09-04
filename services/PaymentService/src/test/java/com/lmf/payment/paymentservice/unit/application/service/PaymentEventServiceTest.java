package com.lmf.payment.paymentservice.unit.application.service;

import com.lmf.payment.paymentservice.application.service.PaymentEventService;
import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import com.lmf.payment.paymentservice.domain.model.PaymentStatus;
import com.lmf.platform.contracts.PaymentApprovedEvent;
import com.lmf.platform.contracts.PaymentFailedEvent;
import com.lmf.platform.messaging.OutboxWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

class PaymentEventServiceTest {

    private OutboxWriter outboxWriter;

    private PaymentEventService paymentEventService;

    @BeforeEach
    void setUp() {

        outboxWriter = mock(OutboxWriter.class);

        paymentEventService = new PaymentEventService(outboxWriter);
    }

    @Test
    @DisplayName("Deve escrever PAYMENT_APPROVED no outbox")
    void shouldWriteApprovedEvent() {

        Payment payment = approvedPayment();

        paymentEventService.publish(payment);

        verify(outboxWriter).write(eq(payment.getId()), eq("PAYMENT"), eq("PAYMENT_APPROVED"), any(PaymentApprovedEvent.class));
    }

    @Test
    @DisplayName("Deve escrever PAYMENT_FAILED no outbox")
    void shouldWriteFailedEvent() {

        Payment payment = failedPayment();

        paymentEventService.publish(payment);

        verify(outboxWriter).write(eq(payment.getId()), eq("PAYMENT"), eq("PAYMENT_FAILED"), any(PaymentFailedEvent.class));
    }

    @Test
    @DisplayName("Não escreve nada para status sem evento mapeado (ex.: PENDING)")
    void shouldNotWriteForUnmappedStatus() {

        Payment payment = mock(Payment.class);
        when(payment.getId()).thenReturn(UUID.randomUUID());
        when(payment.getPaymentStatus()).thenReturn(PaymentStatus.PENDING);

        paymentEventService.publish(payment);

        verifyNoInteractions(outboxWriter);
    }

    private Payment approvedPayment() {
        Payment payment = mock(Payment.class);
        when(payment.getId()).thenReturn(UUID.randomUUID());
        when(payment.getOrderId()).thenReturn(UUID.randomUUID());
        when(payment.getCustomerId()).thenReturn(UUID.randomUUID());
        when(payment.getAmount()).thenReturn(BigDecimal.valueOf(100));
        when(payment.getCurrency()).thenReturn("BRL");
        when(payment.getPaymentMethod()).thenReturn(PaymentMethod.CREDIT_CARD);
        when(payment.getPaymentStatus()).thenReturn(PaymentStatus.APPROVED);
        when(payment.getTransactionId()).thenReturn("tx-1");
        when(payment.getProvider()).thenReturn("FAKE");
        when(payment.getPaidAt()).thenReturn(OffsetDateTime.now());
        return payment;
    }

    private Payment failedPayment() {
        Payment payment = mock(Payment.class);
        when(payment.getId()).thenReturn(UUID.randomUUID());
        when(payment.getOrderId()).thenReturn(UUID.randomUUID());
        when(payment.getCustomerId()).thenReturn(UUID.randomUUID());
        when(payment.getAmount()).thenReturn(BigDecimal.valueOf(900));
        when(payment.getCurrency()).thenReturn("BRL");
        when(payment.getPaymentMethod()).thenReturn(PaymentMethod.PIX);
        when(payment.getPaymentStatus()).thenReturn(PaymentStatus.FAILED);
        when(payment.getFailureReason()).thenReturn("declined");
        when(payment.getGatewayStatus()).thenReturn("FAILED");
        when(payment.getFailedAt()).thenReturn(OffsetDateTime.now());
        return payment;
    }
}
