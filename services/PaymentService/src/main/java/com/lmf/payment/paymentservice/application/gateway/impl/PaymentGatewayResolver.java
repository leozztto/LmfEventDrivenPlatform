package com.lmf.payment.paymentservice.application.gateway.impl;

import com.lmf.payment.paymentservice.application.gateway.PaymentGateway;
import com.lmf.payment.paymentservice.domain.exception.UnsupportedPaymentMethodException;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentGatewayResolver {

    private final Map<PaymentMethod, PaymentGateway> gateways;

    public PaymentGatewayResolver(List<PaymentGateway> gateways) {

        this.gateways = gateways.stream().collect(Collectors.toMap(PaymentGateway::supports, Function.identity()));
    }

    public PaymentGateway resolve(PaymentMethod paymentMethod) {

        PaymentGateway paymentGateway = gateways.get(paymentMethod);

        if (paymentGateway == null) {
            throw new UnsupportedPaymentMethodException(paymentMethod);
        }

        return paymentGateway;
    }
}
