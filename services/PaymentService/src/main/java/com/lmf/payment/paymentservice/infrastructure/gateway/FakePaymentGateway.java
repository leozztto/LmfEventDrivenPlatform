package com.lmf.payment.paymentservice.infrastructure.gateway;

import com.lmf.payment.paymentservice.application.gateway.PaymentGateway;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayRequest;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayResponse;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Gateway de cartão simulado para ambientes de não-produção. Em {@code prod} quem atende cartão é o
 * {@link MercadoPagoGateway}.
 */
@Component
@Profile("!prod")
public class FakePaymentGateway implements PaymentGateway {

    private static final BigDecimal APPROVAL_LIMIT = BigDecimal.valueOf(10_000);

    @Override
    public PaymentGatewayResponse process(PaymentGatewayRequest paymentGatewayRequest) {

        boolean approved = paymentGatewayRequest.amount().compareTo(APPROVAL_LIMIT) <= 0;

        if (approved) {

            return new PaymentGatewayResponse(true, UUID.randomUUID().toString(), "APPROVED", null);
        }

        return new PaymentGatewayResponse(false, null, "FAILED", "Payment denied by gateway");
    }

    @Override
    public PaymentMethod supports() {
        return PaymentMethod.CREDIT_CARD;
    }

    @Override
    public String provider() {
        return "FAKE";
    }
}
