package com.lmf.payment.paymentservice.unit.application.service;

import com.lmf.payment.paymentservice.application.gateway.PaymentGateway;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayRequest;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayResponse;
import com.lmf.payment.paymentservice.application.gateway.impl.PaymentGatewayResolver;
import com.lmf.payment.paymentservice.application.service.PaymentProcessorService;
import com.lmf.payment.paymentservice.domain.exception.PaymentDeclinedException;
import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import com.lmf.payment.paymentservice.infrastructure.observability.PaymentMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentProcessorServiceTest {

    private PaymentGatewayResolver gatewayResolver;

    private PaymentMetricsService metricsService;

    private PaymentGateway paymentGateway;

    private PaymentProcessorService paymentProcessorService;

    @BeforeEach
    void setUp() {

        gatewayResolver = mock(PaymentGatewayResolver.class);

        metricsService = mock(PaymentMetricsService.class);

        paymentGateway = mock(PaymentGateway.class);

        paymentProcessorService = new PaymentProcessorService(gatewayResolver, metricsService);
    }

    @Test
    @DisplayName("Deve aprovar pagamento com sucesso")
    void shouldApprovePaymentSuccessfully() {

        Payment payment = mock(Payment.class);

        UUID paymentId = UUID.randomUUID();

        when(payment.getId()).thenReturn(paymentId);
        when(payment.getOrderId()).thenReturn(UUID.randomUUID());
        when(payment.getCustomerId()).thenReturn(UUID.randomUUID());
        when(payment.getAmount()).thenReturn(BigDecimal.valueOf(100));
        when(payment.getCurrency()).thenReturn("BRL");
        when(payment.getPaymentMethod()).thenReturn(PaymentMethod.CREDIT_CARD);
        when(payment.getInstallments()).thenReturn(1);
        when(payment.getProvider()).thenReturn("MERCADO_PAGO");

        when(gatewayResolver.resolve(PaymentMethod.CREDIT_CARD)).thenReturn(paymentGateway);

        PaymentGatewayResponse paymentGatewayResponse = new PaymentGatewayResponse(true, "tx-123", "APPROVED", null);

        when(paymentGateway.process(any(PaymentGatewayRequest.class))).thenReturn(paymentGatewayResponse);

        paymentProcessorService.process(payment);

        verify(payment).approve("tx-123", "APPROVED");

        verify(metricsService).incrementApprovedPayments();

        verify(metricsService, never()).incrementFailedPayments();
    }

    @Test
    @DisplayName("Deve reprovar pagamento quando gateway retornar falha")
    void shouldFailPaymentWhenGatewayReturnsFailure() {

        Payment payment = mock(Payment.class);

        when(payment.getId()).thenReturn(UUID.randomUUID());
        when(payment.getOrderId()).thenReturn(UUID.randomUUID());
        when(payment.getCustomerId()).thenReturn(UUID.randomUUID());
        when(payment.getAmount()).thenReturn(BigDecimal.valueOf(500));
        when(payment.getCurrency()).thenReturn("BRL");
        when(payment.getPaymentMethod()).thenReturn(PaymentMethod.PIX);
        when(payment.getInstallments()).thenReturn(1);
        when(payment.getProvider()).thenReturn("MERCADO_PAGO");

        when(gatewayResolver.resolve(PaymentMethod.PIX)).thenReturn(paymentGateway);

        PaymentGatewayResponse paymentGatewayResponse = new PaymentGatewayResponse(false, null, "FAILED", "Card denied");

        when(paymentGateway.process(any(PaymentGatewayRequest.class))).thenReturn(paymentGatewayResponse);

        PaymentDeclinedException exception = assertThrows(PaymentDeclinedException.class, () -> paymentProcessorService.process(payment));

        assertEquals("Card denied", exception.getMessage());

        verify(payment).fail("Card denied", "FAILED");

        verify(metricsService).incrementFailedPayments();

        verify(metricsService, never()).incrementApprovedPayments();
    }

    @Test
    @DisplayName("Deve resolver gateway usando metodo de pagamento")
    void shouldResolveGatewayUsingPaymentMethod() {

        Payment payment = mock(Payment.class);

        when(payment.getId()).thenReturn(UUID.randomUUID());
        when(payment.getOrderId()).thenReturn(UUID.randomUUID());
        when(payment.getCustomerId()).thenReturn(UUID.randomUUID());
        when(payment.getAmount()).thenReturn(BigDecimal.valueOf(300));
        when(payment.getCurrency()).thenReturn("USD");
        when(payment.getPaymentMethod()).thenReturn(PaymentMethod.MERCAD_PAGO);
        when(payment.getInstallments()).thenReturn(2);
        when(payment.getProvider()).thenReturn("MERCADO_PAGO");

        when(gatewayResolver.resolve(PaymentMethod.MERCAD_PAGO)).thenReturn(paymentGateway);

        when(paymentGateway.process(any(PaymentGatewayRequest.class))).thenReturn(new PaymentGatewayResponse(true, "tx-999", "APPROVED", null));

        paymentProcessorService.process(payment);

        verify(gatewayResolver).resolve(PaymentMethod.MERCAD_PAGO);
    }

    @Test
    @DisplayName("Deve enviar request correto para gateway")
    void shouldSendCorrectRequestToGateway() {

        Payment payment = mock(Payment.class);

        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        when(payment.getId()).thenReturn(paymentId);
        when(payment.getOrderId()).thenReturn(orderId);
        when(payment.getCustomerId()).thenReturn(customerId);
        when(payment.getAmount()).thenReturn(BigDecimal.valueOf(750));
        when(payment.getCurrency()).thenReturn("BRL");
        when(payment.getPaymentMethod()).thenReturn(PaymentMethod.CREDIT_CARD);
        when(payment.getInstallments()).thenReturn(5);
        when(payment.getProvider()).thenReturn("MERCADO_PAGO");

        when(gatewayResolver.resolve(PaymentMethod.CREDIT_CARD)).thenReturn(paymentGateway);

        when(paymentGateway.process(any(PaymentGatewayRequest.class))).thenReturn(new PaymentGatewayResponse(true, "tx-abc", "APPROVED", null));

        paymentProcessorService.process(payment);

        ArgumentCaptor<PaymentGatewayRequest> captor = ArgumentCaptor.forClass(PaymentGatewayRequest.class);

        verify(paymentGateway).process(captor.capture());

        PaymentGatewayRequest paymentGatewayRequest = captor.getValue();

        assertEquals(paymentId, paymentGatewayRequest.paymentId());
        assertEquals(orderId, paymentGatewayRequest.orderId());
        assertEquals(customerId, paymentGatewayRequest.customerId());
        assertEquals(BigDecimal.valueOf(750), paymentGatewayRequest.amount());
        assertEquals("BRL", paymentGatewayRequest.currency());
        assertEquals(PaymentMethod.CREDIT_CARD, paymentGatewayRequest.paymentMethod());
        assertEquals(5, paymentGatewayRequest.installments());
    }
}