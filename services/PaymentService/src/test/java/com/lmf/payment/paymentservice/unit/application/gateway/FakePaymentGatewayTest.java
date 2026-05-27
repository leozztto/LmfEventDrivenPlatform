package com.lmf.payment.paymentservice.unit.application.gateway;

import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayRequest;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayResponse;
import com.lmf.payment.paymentservice.application.gateway.impl.FakePaymentGateway;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FakePaymentGatewayTest {

    private FakePaymentGateway fakePaymentGateway;

    @BeforeEach
    void setUp() {
        fakePaymentGateway = new FakePaymentGateway();
    }

    @Test
    @DisplayName("Deve aprovar pagamento quando valor for menor ou igual a 10000")
    void shouldApprovePaymentWhenAmountIsLessThanOrEqualTo10000() {

        PaymentGatewayRequest request = new PaymentGatewayRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(9999.99), "BRL", PaymentMethod.CREDIT_CARD, 1);

        PaymentGatewayResponse paymentGatewayResponse = fakePaymentGateway.process(request);

        assertNotNull(paymentGatewayResponse);
        assertTrue(paymentGatewayResponse.success());
        assertNotNull(paymentGatewayResponse.transactionId());
        assertEquals("APPROVED", paymentGatewayResponse.gatewayStatus());
        assertNull(paymentGatewayResponse.failureReason());
    }

    @Test
    @DisplayName("Deve aprovar pagamento quando valor for exatamente 10000")
    void shouldApprovePaymentWhenAmountIsExactly10000() {

        PaymentGatewayRequest paymentGatewayRequest = new PaymentGatewayRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(10000), "BRL", PaymentMethod.CREDIT_CARD, 1);

        PaymentGatewayResponse paymentGatewayResponse = fakePaymentGateway.process(paymentGatewayRequest);

        assertNotNull(paymentGatewayResponse);
        assertTrue(paymentGatewayResponse.success());
        assertNotNull(paymentGatewayResponse.transactionId());
        assertEquals("APPROVED", paymentGatewayResponse.gatewayStatus());
        assertNull(paymentGatewayResponse.failureReason());
    }

    @Test
    @DisplayName("Deve reprovar pagamento quando valor for maior que 10000")
    void shouldRejectPaymentWhenAmountIsGreaterThan10000() {

        PaymentGatewayRequest paymentGatewayRequest = new PaymentGatewayRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(10000.01), "BRL", PaymentMethod.CREDIT_CARD, 1);

        PaymentGatewayResponse paymentGatewayResponse = fakePaymentGateway.process(paymentGatewayRequest);

        assertNotNull(paymentGatewayResponse);
        assertFalse(paymentGatewayResponse.success());
        assertNull(paymentGatewayResponse.transactionId());
        assertEquals("FAILED", paymentGatewayResponse.gatewayStatus());
        assertEquals("Payment denied by gateway", paymentGatewayResponse.failureReason());
    }

    @Test
    @DisplayName("Deve retornar CREDIT_CARD como metodo suportado")
    void shouldReturnSupportedPaymentMethod() {

        PaymentMethod paymentMethod = fakePaymentGateway.supports();

        assertEquals(PaymentMethod.CREDIT_CARD, paymentMethod);
    }
}
