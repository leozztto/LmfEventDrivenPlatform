package com.lmf.payment.paymentservice.infrastructure.gateway;

import com.lmf.payment.paymentservice.application.gateway.PaymentGateway;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayRequest;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayResponse;
import com.lmf.payment.paymentservice.domain.exception.PaymentGatewayException;
import com.lmf.payment.paymentservice.domain.exception.PaymentTimeoutException;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Adquirente de cartão para produção (simulado). Só é ativado no profile {@code prod}; fora dele quem
 * atende cartão é o {@link FakePaymentGateway}.
 */
@Slf4j
@Component
@Profile("prod")
public class MercadoPagoGateway implements PaymentGateway {

    @Override
    public PaymentGatewayResponse process(PaymentGatewayRequest paymentGatewayRequest) {

        log.info("Processing payment by MercadoPago. paymentId={}, amount={}", paymentGatewayRequest.paymentId(), paymentGatewayRequest.amount());

        int random = ThreadLocalRandom.current().nextInt(100);

        if (random < 10) {

            throw new PaymentTimeoutException("Gateway timeout while processing payment");
        }

        if (random < 20) {

            throw new PaymentGatewayException("Temporary gateway failure");
        }

        return new PaymentGatewayResponse(true, UUID.randomUUID().toString(), "APPROVED", null);
    }

    @Override
    public PaymentMethod supports() {
        return PaymentMethod.CREDIT_CARD;
    }

    @Override
    public String provider() {
        return "MERCADO_PAGO";
    }
}
