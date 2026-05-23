package com.lmf.payment.paymentservice.application.usecase;

import com.lmf.payment.paymentservice.application.command.ProcessPaymentCommand;
import com.lmf.payment.paymentservice.domain.Payment;
import com.lmf.payment.paymentservice.domain.PaymentStatus;
import com.lmf.payment.paymentservice.events.PaymentCreatedEvent;
import com.lmf.payment.paymentservice.ports.output.PaymentEventPublisher;
import com.lmf.payment.paymentservice.ports.output.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPaymentUseCase {

    private final PaymentRepository paymentRepository;

    private final PaymentEventPublisher paymentEventPublisher;

    @Transactional
    public void execute(ProcessPaymentCommand processPaymentCommand) {

        boolean paymentAlreadyExists = paymentRepository.findByOrderId(processPaymentCommand.orderId()).isPresent();

        if (paymentAlreadyExists) {

            log.warn("Payment already exists for orderId={}", processPaymentCommand.orderId());

            return;
        }

        Payment payment = Payment.builder().id(UUID.randomUUID()).orderId(processPaymentCommand.orderId()).customerId(processPaymentCommand.customerId()).amount(processPaymentCommand.amount()).currency(processPaymentCommand.currency()).paymentMethod(processPaymentCommand.paymentMethod()).installments(processPaymentCommand.installments()).status(PaymentStatus.PENDING).provider("MERCADO_PAGO").gatewayStatus("PROCESSING").createdAt(OffsetDateTime.now()).build();

        paymentRepository.save(payment);

        PaymentCreatedEvent paymentCreatedEvent = new PaymentCreatedEvent(UUID.randomUUID(), "PAYMENT_CREATED", "1.0", OffsetDateTime.now(), payment.getId(), payment.getOrderId(), payment.getCustomerId(), payment.getAmount(), payment.getCurrency(), payment.getPaymentMethod(), payment.getInstallments(), payment.getStatus(), payment.getProvider(), payment.getTransactionId(), payment.getGatewayStatus());

        paymentEventPublisher.publish(paymentCreatedEvent);

        log.info("Payment created successfully. paymentId={}, orderId={}", payment.getId(), payment.getOrderId());
    }
}