package com.lmf.payment.paymentservice.unit.application.service;

import com.lmf.payment.paymentservice.application.gateway.PaymentGateway;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayRequest;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayResponse;
import com.lmf.payment.paymentservice.application.gateway.PaymentGatewayProvider;
import com.lmf.payment.paymentservice.application.service.PaymentProcessorService;
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

    private PaymentGatewayProvider gatewayProvider;

    private PaymentMetricsService metricsService;

    private PaymentGateway paymentGateway;

    private PaymentProcessorService paymentProcessorService;

    @BeforeEach
    void setUp() {

        gatewayProvider = mock(PaymentGatewayProvider.class);

        metricsService = mock(PaymentMetricsService.class);

        paymentGateway = mock(PaymentGateway.class);

        paymentProcessorService = new PaymentProcessorService(gatewayProvider, metricsService);
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

        when(gatewayProvider.resolve(PaymentMethod.CREDIT_CARD)).thenReturn(paymentGateway);

        PaymentGatewayResponse paymentGatewayResponse = new PaymentGatewayResponse(true, "tx-123", "APPROVED", null);

        when(paymentGateway.process(any(PaymentGatewayRequest.class))).thenReturn(paymentGatewayResponse);

        paymentProcessorService.process(payment);

        verify(payment).approve("tx-123", "APPROVED");

        verify(metricsService).incrementApprovedPayments();

        verify(metricsService, never()).incrementFailedPayments();
    }

    @Test
    @DisplayName("Deve reprovar pagamento (sem lançar exceção) quando gateway retornar falha")
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

        when(gatewayProvider.resolve(PaymentMethod.PIX)).thenReturn(paymentGateway);

        PaymentGatewayResponse paymentGatewayResponse = new PaymentGatewayResponse(false, null, "FAILED", "Card denied");

        when(paymentGateway.process(any(PaymentGatewayRequest.class))).thenReturn(paymentGatewayResponse);

        assertDoesNotThrow(() -> paymentProcessorService.process(payment));

        verify(payment).fail("Card denied", "FAILED");
        verify(payment, never()).approve(any(), any());

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
        when(payment.getPaymentMethod()).thenReturn(PaymentMethod.DEBIT_CARD);
        when(payment.getInstallments()).thenReturn(2);
        when(payment.getProvider()).thenReturn("MERCADO_PAGO");

        when(gatewayProvider.resolve(PaymentMethod.DEBIT_CARD)).thenReturn(paymentGateway);

        when(paymentGateway.process(any(PaymentGatewayRequest.class))).thenReturn(new PaymentGatewayResponse(true, "tx-999", "APPROVED", null));

        paymentProcessorService.process(payment);

        verify(gatewayProvider).resolve(PaymentMethod.DEBIT_CARD);
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

        when(gatewayProvider.resolve(PaymentMethod.CREDIT_CARD)).thenReturn(paymentGateway);

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