package com.lmf.payment.paymentservice.unit.application.gateway;

import com.lmf.payment.paymentservice.application.gateway.PaymentGateway;
import com.lmf.payment.paymentservice.infrastructure.gateway.PaymentGatewayResolver;
import com.lmf.payment.paymentservice.domain.exception.UnsupportedPaymentMethodException;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentGatewayResolverTest {

    private PaymentGatewayResolver paymentGatewayResolver;

    private PaymentGateway creditCardGateway;

    private PaymentGateway mercadoPagoGateway;

    @BeforeEach
    void setUp() {

        creditCardGateway = mock(PaymentGateway.class);
        mercadoPagoGateway = mock(PaymentGateway.class);

        when(creditCardGateway.supports()).thenReturn(PaymentMethod.CREDIT_CARD);

        when(mercadoPagoGateway.supports()).thenReturn(PaymentMethod.DEBIT_CARD);

        paymentGatewayResolver = new PaymentGatewayResolver(List.of(creditCardGateway, mercadoPagoGateway));
    }

    @Test
    @DisplayName("Deve resolver gateway de cartao de credito")
    void shouldResolveCreditCardGateway() {

        PaymentGateway resolvedGateway = paymentGatewayResolver.resolve(PaymentMethod.CREDIT_CARD);

        assertNotNull(resolvedGateway);
        assertEquals(creditCardGateway, resolvedGateway);
    }

    @Test
    @DisplayName("Deve resolver gateway Mercado Pago")
    void shouldResolveMercadoPagoGateway() {

        PaymentGateway resolvedGateway = paymentGatewayResolver.resolve(PaymentMethod.DEBIT_CARD);

        assertNotNull(resolvedGateway);
        assertEquals(mercadoPagoGateway, resolvedGateway);
    }

    @Test
    @DisplayName("Deve lançar excecao quando metodo de pagamento nao for suportado")
    void shouldThrowExceptionWhenPaymentMethodIsNotSupported() {

        UnsupportedPaymentMethodException exception = assertThrows(UnsupportedPaymentMethodException.class, () -> paymentGatewayResolver.resolve(PaymentMethod.PIX));

        assertNotNull(exception);
    }

    @Test
    @DisplayName("Deve criar mapa de gateways corretamente")
    void shouldCreateGatewayMapCorrectly() {

        PaymentGateway creditGatewayResolved = paymentGatewayResolver.resolve(PaymentMethod.CREDIT_CARD);

        PaymentGateway mercadoPagoResolved = paymentGatewayResolver.resolve(PaymentMethod.DEBIT_CARD);

        assertEquals(creditCardGateway, creditGatewayResolved);
        assertEquals(mercadoPagoGateway, mercadoPagoResolved);

        verify(creditCardGateway, atLeastOnce()).supports();
        verify(mercadoPagoGateway, atLeastOnce()).supports();
    }
}
