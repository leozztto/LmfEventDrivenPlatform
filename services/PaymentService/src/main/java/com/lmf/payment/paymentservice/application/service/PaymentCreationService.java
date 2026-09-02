package com.lmf.payment.paymentservice.application.service;

import com.lmf.payment.paymentservice.application.command.ProcessPaymentCommand;
import com.lmf.payment.paymentservice.application.gateway.PaymentGatewayProvider;
import com.lmf.payment.paymentservice.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentCreationService {

    private final PaymentGatewayProvider paymentGatewayProvider;

    public Payment create(ProcessPaymentCommand processPaymentCommand) {

        String provider = paymentGatewayProvider.resolve(processPaymentCommand.paymentMethod()).provider();

        return Payment.create(
                processPaymentCommand.orderId(),
                processPaymentCommand.customerId(),
                processPaymentCommand.amount(),
                processPaymentCommand.currency(),
                processPaymentCommand.paymentMethod(),
                processPaymentCommand.installments(),
                provider);
    }
}
