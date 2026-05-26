package com.lmf.payment.paymentservice.application.usecase;

import com.lmf.payment.paymentservice.application.command.ProcessPaymentCommand;
import com.lmf.payment.paymentservice.application.service.*;
import com.lmf.payment.paymentservice.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPaymentUseCase {

    private final PaymentValidationService paymentValidationService;

    private final PaymentCreationService paymentCreationService;

    private final PaymentProcessorService paymentProcessorService;

    private final PaymentPersistenceService paymentPersistenceService;

    private final PaymentEventService paymentEventService;

    @Transactional
    public void execute(ProcessPaymentCommand processPaymentCommand) {

        log.info("Processing payment. orderId={}, amount={}", processPaymentCommand.orderId(), processPaymentCommand.amount());

        paymentValidationService.validatePaymentDoesNotExist(processPaymentCommand.orderId());

        Payment payment = paymentCreationService.create(processPaymentCommand);

        paymentProcessorService.process(payment);

        paymentPersistenceService.save(payment);

        paymentEventService.publish(payment);

        log.info("Payment processed successfully. paymentId={}, status={}", payment.getId(), payment.getPaymentStatus());
    }
}
