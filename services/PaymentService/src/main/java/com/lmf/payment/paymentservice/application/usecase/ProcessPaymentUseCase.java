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

        try {
            log.info("Starting payment workflow. orderId={}, customerId={}, amount={}, paymentMethod={}, installments={}", processPaymentCommand.orderId(), processPaymentCommand.customerId(), processPaymentCommand.amount(), processPaymentCommand.paymentMethod(), processPaymentCommand.installments());

            long start = System.currentTimeMillis();

            paymentValidationService.validatePaymentDoesNotExist(processPaymentCommand.orderId());

            Payment payment = paymentCreationService.create(processPaymentCommand);

            log.info("Payment created successfully. paymentId={}, orderId={}, status={}", payment.getId(), payment.getOrderId(), payment.getPaymentStatus());

            paymentProcessorService.process(payment);

            paymentPersistenceService.save(payment);

            paymentEventService.publish(payment);

            log.info("Payment workflow finished successfully. paymentId={}, orderId={}, status={}, durationMs={}", payment.getId(), payment.getOrderId(), payment.getPaymentStatus(), System.currentTimeMillis() - start);

        } catch (Exception ex) {

            log.error("Payment workflow failed. orderId={}, error={}", processPaymentCommand.orderId(), ex.getMessage(), ex);

            throw ex;
        }
    }
}
