package com.lmf.payment.paymentservice.application.gateway.impl;

import com.lmf.payment.paymentservice.application.gateway.PaymentGateway;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayRequest;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayResponse;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class PixPaymentGateway implements PaymentGateway {

    @Override
    public PaymentGatewayResponse process(PaymentGatewayRequest paymentGatewayRequest) {
        return null;
    }

    @Override
    public PaymentMethod supports() {
        return PaymentMethod.PIX;
    }
}
