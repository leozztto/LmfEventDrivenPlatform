package com.lmf.payment.paymentservice.application.gateway.impl;

import com.lmf.payment.paymentservice.application.gateway.PaymentGateway;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayRequest;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayResponse;
import com.lmf.payment.paymentservice.domain.exception.PaymentGatewayException;
import com.lmf.payment.paymentservice.domain.exception.PaymentTimeoutException;
import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class MercadoPagoGateway implements PaymentGateway {

    @Override
    public PaymentGatewayResponse process(PaymentGatewayRequest paymentGatewayRequest) {

        Payment payment = Payment.create(paymentGatewayRequest.orderId(), paymentGatewayRequest.customerId(), paymentGatewayRequest.amount(), paymentGatewayRequest.currency(), paymentGatewayRequest.paymentMethod(), paymentGatewayRequest.installments(), "MERCADO_PAGO");

        log.info("Processing payment by MercadoPago. paymentId={}, amount={}", payment.getId(), payment.getAmount());

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
        return PaymentMethod.MERCAD_PAGO;
    }
}
