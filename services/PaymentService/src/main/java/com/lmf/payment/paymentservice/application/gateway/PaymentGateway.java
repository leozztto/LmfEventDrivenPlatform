package com.lmf.payment.paymentservice.application.gateway;

import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayRequest;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayResponse;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;

public interface PaymentGateway {

    PaymentGatewayResponse process(PaymentGatewayRequest paymentGatewayRequest);

    PaymentMethod supports();
}