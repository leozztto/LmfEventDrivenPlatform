package com.lmf.payment.paymentservice.unit.domain;

import com.lmf.payment.paymentservice.domain.exception.InvalidCurrencyException;
import com.lmf.payment.paymentservice.domain.exception.InvalidInstallmentsException;
import com.lmf.payment.paymentservice.domain.exception.InvalidPaymentAmountException;
import com.lmf.payment.paymentservice.domain.exception.InvalidPaymentMethodException;
import com.lmf.payment.paymentservice.domain.exception.InvalidPaymentStateException;
import com.lmf.payment.paymentservice.domain.exception.InvalidProviderException;
import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import com.lmf.payment.paymentservice.domain.model.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

    @Test
    @DisplayName("Deve criar pagamento com sucesso")
    void shouldCreatePaymentSuccessfully() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        Payment payment = Payment.create(orderId, customerId, BigDecimal.valueOf(100), "brl", PaymentMethod.CREDIT_CARD, 2, "MERCADO_PAGO");

        assertNotNull(payment.getId());

        assertEquals(orderId, payment.getOrderId());

        assertEquals(customerId, payment.getCustomerId());

        assertEquals(BigDecimal.valueOf(100), payment.getAmount());

        assertEquals("BRL", payment.getCurrency());

        assertEquals(PaymentMethod.CREDIT_CARD, payment.getPaymentMethod());

        assertEquals(2, payment.getInstallments());

        assertEquals(PaymentStatus.PENDING, payment.getPaymentStatus());

        assertEquals("MERCADO_PAGO", payment.getProvider());

        assertEquals("PROCESSING", payment.getGatewayStatus());

        assertNotNull(payment.getCreatedAt());

        assertNotNull(payment.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve definir uma parcela por padrao")
    void shouldSetDefaultInstallments() {

        Payment payment = Payment.create(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(50), "BRL", PaymentMethod.CREDIT_CARD, null, "MERCADO_PAGO");

        assertEquals(1, payment.getInstallments());
    }

    @Test
    @DisplayName("Deve aprovar pagamento")
    void shouldApprovePayment() {

        Payment payment = createValidPayment();

        payment.approve("tx-123", "APPROVED");

        assertEquals(PaymentStatus.APPROVED, payment.getPaymentStatus());

        assertEquals("tx-123", payment.getTransactionId());

        assertEquals("APPROVED", payment.getGatewayStatus());

        assertNotNull(payment.getPaidAt());

        assertNotNull(payment.getUpdatedAt());

        assertTrue(payment.isApproved());

        assertFalse(payment.isPending());
    }

    @Test
    @DisplayName("Deve falhar pagamento")
    void shouldFailPayment() {

        Payment payment = createValidPayment();

        payment.fail("Card denied", "FAILED");

        assertEquals(PaymentStatus.FAILED, payment.getPaymentStatus());

        assertEquals("Card denied", payment.getFailureReason());

        assertEquals("FAILED", payment.getGatewayStatus());

        assertNotNull(payment.getFailedAt());

        assertNotNull(payment.getUpdatedAt());

        assertTrue(payment.isFailed());

        assertFalse(payment.isPending());
    }

    @Test
    @DisplayName("Deve cancelar pagamento")
    void shouldCancelPayment() {

        Payment payment = createValidPayment();

        payment.cancel();

        assertEquals(PaymentStatus.CANCELLED, payment.getPaymentStatus());

        assertEquals("CANCELLED", payment.getGatewayStatus());

        assertNotNull(payment.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve lançar excecao ao aprovar pagamento nao pendente")
    void shouldThrowExceptionWhenApprovingNonPendingPayment() {

        Payment payment = createValidPayment();

        payment.approve("tx-123", "APPROVED");

        assertThrows(InvalidPaymentStateException.class, () -> payment.approve("tx-456", "APPROVED"));
    }

    @Test
    @DisplayName("Deve lançar excecao ao falhar pagamento nao pendente")
    void shouldThrowExceptionWhenFailingNonPendingPayment() {

        Payment payment = createValidPayment();

        payment.fail("error", "FAILED");

        assertThrows(InvalidPaymentStateException.class, () -> payment.fail("another error", "FAILED"));
    }

    @Test
    @DisplayName("Deve lançar excecao ao cancelar pagamento nao pendente")
    void shouldThrowExceptionWhenCancellingNonPendingPayment() {

        Payment payment = createValidPayment();

        payment.cancel();

        assertThrows(InvalidPaymentStateException.class, payment::cancel);
    }

    @Test
    @DisplayName("Deve lançar excecao quando valor for nulo")
    void shouldThrowExceptionWhenAmountIsNull() {

        assertThrows(InvalidPaymentAmountException.class, () -> Payment.create(UUID.randomUUID(), UUID.randomUUID(), null, "BRL", PaymentMethod.CREDIT_CARD, 1, "MERCADO_PAGO"));
    }

    @Test
    @DisplayName("Deve lançar excecao quando valor for menor ou igual a zero")
    void shouldThrowExceptionWhenAmountIsLessThanOrEqualToZero() {

        assertThrows(InvalidPaymentAmountException.class, () -> Payment.create(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO, "BRL", PaymentMethod.CREDIT_CARD, 1, "MERCADO_PAGO"));
    }

    @Test
    @DisplayName("Deve lançar excecao quando moeda for invalida")
    void shouldThrowExceptionWhenCurrencyIsInvalid() {

        assertThrows(InvalidCurrencyException.class, () -> Payment.create(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(100), " ", PaymentMethod.CREDIT_CARD, 1, "MERCADO_PAGO"));
    }

    @Test
    @DisplayName("Deve lançar excecao quando provider for invalido")
    void shouldThrowExceptionWhenProviderIsInvalid() {

        assertThrows(InvalidProviderException.class, () -> Payment.create(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(100), "BRL", PaymentMethod.CREDIT_CARD, 1, ""));
    }

    @Test
    @DisplayName("Deve lançar excecao quando metodo de pagamento for nulo")
    void shouldThrowExceptionWhenPaymentMethodIsNull() {

        assertThrows(InvalidPaymentMethodException.class, () -> Payment.create(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(100), "BRL", null, 1, "MERCADO_PAGO"));
    }

    @Test
    @DisplayName("Deve lançar excecao quando parcelas forem invalidas")
    void shouldThrowExceptionWhenInstallmentsAreInvalid() {

        assertThrows(InvalidInstallmentsException.class, () -> Payment.create(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(100), "BRL", PaymentMethod.CREDIT_CARD, 0, "MERCADO_PAGO"));
    }

    @Test
    @DisplayName("Deve lançar excecao quando PIX tiver mais de uma parcela")
    void shouldThrowExceptionWhenPixHasMoreThanOneInstallment() {

        assertThrows(InvalidPaymentMethodException.class, () -> Payment.create(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(100), "BRL", PaymentMethod.PIX, 2, "MERCADO_PAGO"));
    }

    @Test
    @DisplayName("Deve restaurar pagamento corretamente")
    void shouldRestorePaymentCorrectly() {

        UUID paymentId = UUID.randomUUID();

        UUID orderId = UUID.randomUUID();

        UUID customerId = UUID.randomUUID();

        OffsetDateTime now = OffsetDateTime.now();

        Payment payment = Payment.restore(paymentId, orderId, customerId, BigDecimal.valueOf(500), "BRL", PaymentMethod.CREDIT_CARD, 3, PaymentStatus.APPROVED, "MERCADO_PAGO", "tx-999", "APPROVED", now, now, null, now, null);

        assertEquals(paymentId, payment.getId());

        assertEquals(orderId, payment.getOrderId());

        assertEquals(customerId, payment.getCustomerId());

        assertEquals(BigDecimal.valueOf(500), payment.getAmount());

        assertEquals(PaymentStatus.APPROVED, payment.getPaymentStatus());

        assertEquals("tx-999", payment.getTransactionId());

        assertEquals("APPROVED", payment.getGatewayStatus());
    }

    private Payment createValidPayment() {

        return Payment.create(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(100), "BRL", PaymentMethod.CREDIT_CARD, 1, "MERCADO_PAGO");
    }
}