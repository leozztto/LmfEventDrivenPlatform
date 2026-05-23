package com.lmf.payment.paymentservice.application.usecase;

import com.lmf.payment.paymentservice.application.command.ProcessPaymentCommand;
import com.lmf.payment.paymentservice.domain.Payment;
import com.lmf.payment.paymentservice.domain.PaymentStatus;
import com.lmf.payment.paymentservice.ports.output.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessPaymentUseCase {

    private final PaymentRepository paymentRepository;

    @Transactional
    public void execute(ProcessPaymentCommand command) {

        Payment payment = Payment.builder().id(UUID.randomUUID()).orderId(command.orderId()).amount(command.amount()).paymentMethod(command.paymentMethod()).installments(command.installments()).status(PaymentStatus.PENDING).createdAt(OffsetDateTime.now()).build();

        paymentRepository.save(payment);
    }
}