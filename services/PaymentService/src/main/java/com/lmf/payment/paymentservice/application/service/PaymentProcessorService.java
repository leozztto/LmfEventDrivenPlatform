package com.lmf.payment.paymentservice.application.service;

import com.lmf.payment.paymentservice.application.gateway.PaymentGateway;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayRequest;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayResponse;
import com.lmf.payment.paymentservice.application.gateway.impl.PaymentGatewayResolver;
import com.lmf.payment.paymentservice.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentProcessorService {

    private final PaymentGatewayResolver gatewayResolver;

    public PaymentGatewayResponse process(Payment payment) {

        PaymentGatewayRequest paymentGatewayRequest = new PaymentGatewayRequest(payment.getId(), payment.getOrderId(), payment.getCustomerId(), payment.getAmount(), payment.getCurrency(), payment.getPaymentMethod(), payment.getInstallments());

        PaymentGateway paymentGateway = gatewayResolver.resolve(payment.getPaymentMethod());

        return paymentGateway.process(paymentGatewayRequest);
    }
}
