package com.lmf.payment.paymentservice.unit.application.gateway;

import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayRequest;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayResponse;
import com.lmf.payment.paymentservice.infrastructure.gateway.MercadoPagoGateway;
import com.lmf.payment.paymentservice.domain.exception.PaymentGatewayException;
import com.lmf.payment.paymentservice.domain.exception.PaymentTimeoutException;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MercadoPagoGatewayTest {

    private MercadoPagoGateway mercadoPagoGateway;

    @BeforeEach
    void setUp() {
        mercadoPagoGateway = new MercadoPagoGateway();
    }

    @Test
    @DisplayName("Deve processar pagamento com sucesso quando random for maior ou igual a 20")
    void shouldProcessPaymentSuccessfully() {

        PaymentGatewayRequest paymentGatewayRequest = buildRequest();

        ThreadLocalRandom threadLocalRandom = mock(ThreadLocalRandom.class);

        try (MockedStatic<ThreadLocalRandom> mockedStatic = mockStatic(ThreadLocalRandom.class)) {

            mockedStatic.when(ThreadLocalRandom::current).thenReturn(threadLocalRandom);

            when(threadLocalRandom.nextInt(100)).thenReturn(50);

            PaymentGatewayResponse paymentGatewayResponse = mercadoPagoGateway.process(paymentGatewayRequest);

            assertNotNull(paymentGatewayResponse);
            assertTrue(paymentGatewayResponse.success());
            assertNotNull(paymentGatewayResponse.transactionId());
            assertEquals("APPROVED", paymentGatewayResponse.gatewayStatus());
            assertNull(paymentGatewayResponse.failureReason());
        }
    }

    @Test
    @DisplayName("Deve lançar PaymentTimeoutException quando random for menor que 10")
    void shouldThrowPaymentTimeoutException() {

        PaymentGatewayRequest paymentGatewayRequest = buildRequest();

        ThreadLocalRandom threadLocalRandom = mock(ThreadLocalRandom.class);

        try (MockedStatic<ThreadLocalRandom> mockedStatic = mockStatic(ThreadLocalRandom.class)) {

            mockedStatic.when(ThreadLocalRandom::current).thenReturn(threadLocalRandom);

            when(threadLocalRandom.nextInt(100)).thenReturn(5);

            PaymentTimeoutException paymentTimeoutException = assertThrows(PaymentTimeoutException.class, () -> mercadoPagoGateway.process(paymentGatewayRequest));

            assertEquals("Gateway timeout while processing payment", paymentTimeoutException.getMessage());
        }
    }

    @Test
    @DisplayName("Deve lançar PaymentGatewayException quando random estiver entre 10 e 19")
    void shouldThrowPaymentGatewayException() {

        PaymentGatewayRequest paymentGatewayRequest = buildRequest();

        ThreadLocalRandom threadLocalRandom = mock(ThreadLocalRandom.class);

        try (MockedStatic<ThreadLocalRandom> mockedStatic = mockStatic(ThreadLocalRandom.class)) {

            mockedStatic.when(ThreadLocalRandom::current).thenReturn(threadLocalRandom);

            when(threadLocalRandom.nextInt(100)).thenReturn(15);

            PaymentGatewayException exception = assertThrows(PaymentGatewayException.class, () -> mercadoPagoGateway.process(paymentGatewayRequest));

            assertEquals("Temporary gateway failure", exception.getMessage());
        }
    }

    @Test
    @DisplayName("Deve retornar MERCAD_PAGO como metodo suportado")
    void shouldReturnSupportedPaymentMethod() {

        PaymentMethod paymentMethod = mercadoPagoGateway.supports();

        assertEquals(PaymentMethod.CREDIT_CARD, paymentMethod);
    }

    private PaymentGatewayRequest buildRequest() {

        return new PaymentGatewayRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(500), "BRL", PaymentMethod.CREDIT_CARD, 1);
    }
}