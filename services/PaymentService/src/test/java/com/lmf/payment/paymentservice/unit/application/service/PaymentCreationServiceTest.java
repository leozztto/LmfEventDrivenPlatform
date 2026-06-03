package com.lmf.payment.paymentservice.unit.application.service;

import com.lmf.payment.paymentservice.application.command.ProcessPaymentCommand;
import com.lmf.payment.paymentservice.application.service.PaymentCreationService;
import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentCreationServiceTest {

    private PaymentCreationService paymentCreationService;

    @BeforeEach
    void setUp() {

        paymentCreationService = new PaymentCreationService();
    }

    @Test
    @DisplayName("Deve criar pagamento com sucesso")
    void shouldCreatePaymentSuccessfully() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        ProcessPaymentCommand command = new ProcessPaymentCommand(orderId, UUID.randomUUID(), "INVENTORY_RESERVED", customerId, BigDecimal.valueOf(150.75), "BRL", PaymentMethod.MERCAD_PAGO, 3);

        Payment payment = paymentCreationService.create(command);

        assertNotNull(payment);
        assertNotNull(payment.getId());

        assertEquals(orderId, payment.getOrderId());
        assertEquals(customerId, payment.getCustomerId());

        assertEquals(BigDecimal.valueOf(150.75), payment.getAmount());

        assertEquals("BRL", payment.getCurrency());

        assertEquals(PaymentMethod.MERCAD_PAGO, payment.getPaymentMethod());

        assertEquals(3, payment.getInstallments());

        assertEquals("MERCADO_PAGO", payment.getProvider());
    }

    @Test
    @DisplayName("Deve criar pagamento com uma parcela")
    void shouldCreatePaymentWithOneInstallment() {

        ProcessPaymentCommand command = new ProcessPaymentCommand(UUID.randomUUID(), UUID.randomUUID(), "INVENTORY_RESERVED", UUID.randomUUID(), BigDecimal.valueOf(50), "USD", PaymentMethod.CREDIT_CARD, 1);

        Payment payment = paymentCreationService.create(command);

        assertNotNull(payment);

        assertEquals(1, payment.getInstallments());

        assertEquals("USD", payment.getCurrency());

        assertEquals(PaymentMethod.CREDIT_CARD, payment.getPaymentMethod());
    }

    @Test
    @DisplayName("Deve criar pagamento mantendo provider fixo")
    void shouldCreatePaymentKeepingFixedProvider() {

        ProcessPaymentCommand command = new ProcessPaymentCommand(UUID.randomUUID(), UUID.randomUUID(), "INVENTORY_RESERVED", UUID.randomUUID(), BigDecimal.valueOf(999), "BRL", PaymentMethod.PIX, 1);

        Payment payment = paymentCreationService.create(command);

        assertEquals("MERCADO_PAGO", payment.getProvider());
    }
}