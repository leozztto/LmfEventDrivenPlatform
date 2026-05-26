package com.lmf.payment.paymentservice.application.gateway.impl;

import com.lmf.payment.paymentservice.application.gateway.PaymentGateway;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayRequest;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayResponse;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("local")
public class FakePaymentGateway implements PaymentGateway {

    @Override
    public PaymentGatewayResponse process(PaymentGatewayRequest paymentGatewayRequest) {

        boolean approved = paymentGatewayRequest.amount().doubleValue() <= 1000;

        if (approved) {

            return new PaymentGatewayResponse(true, UUID.randomUUID().toString(), "APPROVED", null);
        }

        return new PaymentGatewayResponse(false, null, "FAILED", "Payment denied by gateway");
    }

    @Override
    public PaymentMethod supports() {
        return PaymentMethod.CREDIT_CARD;
    }
}
