package com.lmf.payment.paymentservice.unit.application.service;

import com.lmf.payment.paymentservice.application.command.ProcessPaymentCommand;
import com.lmf.payment.paymentservice.application.gateway.PaymentGateway;
import com.lmf.payment.paymentservice.application.gateway.PaymentGatewayProvider;
import com.lmf.payment.paymentservice.application.service.PaymentCreationService;
import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentCreationServiceTest {

    private PaymentGatewayProvider paymentGatewayProvider;

    private PaymentCreationService paymentCreationService;

    @BeforeEach
    void setUp() {

        paymentGatewayProvider = mock(PaymentGatewayProvider.class);

        PaymentGateway gateway = mock(PaymentGateway.class);
        when(gateway.provider()).thenReturn("FAKE");
        when(paymentGatewayProvider.resolve(any(PaymentMethod.class))).thenReturn(gateway);

        paymentCreationService = new PaymentCreationService(paymentGatewayProvider);
    }

    @Test
    @DisplayName("Deve criar pagamento com sucesso")
    void shouldCreatePaymentSuccessfully() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        ProcessPaymentCommand command = new ProcessPaymentCommand(orderId, UUID.randomUUID(), "INVENTORY_RESERVED", customerId, BigDecimal.valueOf(150.75), "BRL", PaymentMethod.CREDIT_CARD, 3);

        Payment payment = paymentCreationService.create(command);

        assertNotNull(payment);
        assertNotNull(payment.getId());
        assertEquals(orderId, payment.getOrderId());
        assertEquals(customerId, payment.getCustomerId());
        assertEquals(BigDecimal.valueOf(150.75), payment.getAmount());
        assertEquals("BRL", payment.getCurrency());
        assertEquals(PaymentMethod.CREDIT_CARD, payment.getPaymentMethod());
        assertEquals(3, payment.getInstallments());
    }

    @Test
    @DisplayName("Deve derivar o provider a partir do gateway resolvido")
    void shouldDeriveProviderFromGateway() {

        ProcessPaymentCommand command = new ProcessPaymentCommand(UUID.randomUUID(), UUID.randomUUID(), "INVENTORY_RESERVED", UUID.randomUUID(), BigDecimal.valueOf(999), "BRL", PaymentMethod.PIX, 1);

        Payment payment = paymentCreationService.create(command);

        assertEquals("FAKE", payment.getProvider());
        verify(paymentGatewayProvider).resolve(PaymentMethod.PIX);
    }
}
