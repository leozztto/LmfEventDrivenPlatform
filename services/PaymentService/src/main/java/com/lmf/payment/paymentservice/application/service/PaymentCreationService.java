package com.lmf.payment.paymentservice.application.service;

import com.lmf.payment.paymentservice.application.command.ProcessPaymentCommand;
import com.lmf.payment.paymentservice.domain.model.Payment;
import org.springframework.stereotype.Service;

@Service
public class PaymentCreationService {

    public Payment create(ProcessPaymentCommand processPaymentCommand) {

        return Payment.create(processPaymentCommand.orderId(), processPaymentCommand.customerId(), processPaymentCommand.amount(), processPaymentCommand.currency(), processPaymentCommand.paymentMethod(), processPaymentCommand.installments(), "MERCADO_PAGO");
    }
}
