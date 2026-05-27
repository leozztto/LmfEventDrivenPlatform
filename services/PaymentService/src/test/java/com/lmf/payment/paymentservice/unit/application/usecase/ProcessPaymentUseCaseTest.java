package com.lmf.payment.paymentservice.unit.application.usecase;

import com.lmf.payment.paymentservice.application.command.ProcessPaymentCommand;
import com.lmf.payment.paymentservice.application.service.PaymentCreationService;
import com.lmf.payment.paymentservice.application.service.PaymentEventService;
import com.lmf.payment.paymentservice.application.service.PaymentPersistenceService;
import com.lmf.payment.paymentservice.application.service.PaymentProcessorService;
import com.lmf.payment.paymentservice.application.service.PaymentValidationService;
import com.lmf.payment.paymentservice.application.usecase.ProcessPaymentUseCase;
import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProcessPaymentUseCaseTest {

    private PaymentValidationService paymentValidationService;

    private PaymentCreationService paymentCreationService;

    private PaymentProcessorService paymentProcessorService;

    private PaymentPersistenceService paymentPersistenceService;

    private PaymentEventService paymentEventService;

    private ProcessPaymentUseCase processPaymentUseCase;

    @BeforeEach
    void setUp() {

        paymentValidationService = mock(PaymentValidationService.class);

        paymentCreationService = mock(PaymentCreationService.class);

        paymentProcessorService = mock(PaymentProcessorService.class);

        paymentPersistenceService = mock(PaymentPersistenceService.class);

        paymentEventService = mock(PaymentEventService.class);

        processPaymentUseCase = new ProcessPaymentUseCase(paymentValidationService, paymentCreationService, paymentProcessorService, paymentPersistenceService, paymentEventService);
    }

    @Test
    @DisplayName("Deve executar fluxo de pagamento com sucesso")
    void shouldExecutePaymentWorkflowSuccessfully() {

        ProcessPaymentCommand processPaymentCommand = buildCommand();

        Payment payment = mock(Payment.class);

        when(paymentCreationService.create(processPaymentCommand)).thenReturn(payment);

        processPaymentUseCase.execute(processPaymentCommand);

        verify(paymentValidationService).validatePaymentDoesNotExist(processPaymentCommand.orderId());

        verify(paymentCreationService).create(processPaymentCommand);

        verify(paymentProcessorService).process(payment);

        verify(paymentPersistenceService).save(payment);

        verify(paymentEventService).publish(payment);
    }

    @Test
    @DisplayName("Deve parar fluxo quando validacao falhar")
    void shouldStopWorkflowWhenValidationFails() {

        ProcessPaymentCommand processPaymentCommand = buildCommand();

        doThrow(new RuntimeException("Payment already exists")).when(paymentValidationService).validatePaymentDoesNotExist(processPaymentCommand.orderId());

        assertThrows(RuntimeException.class, () -> processPaymentUseCase.execute(processPaymentCommand));

        verify(paymentValidationService).validatePaymentDoesNotExist(processPaymentCommand.orderId());

        verifyNoInteractions(paymentCreationService);

        verifyNoInteractions(paymentProcessorService);

        verifyNoInteractions(paymentPersistenceService);

        verifyNoInteractions(paymentEventService);
    }

    @Test
    @DisplayName("Deve parar fluxo quando processamento falhar")
    void shouldStopWorkflowWhenProcessingFails() {

        ProcessPaymentCommand processPaymentCommand = buildCommand();

        Payment payment = mock(Payment.class);

        when(paymentCreationService.create(processPaymentCommand)).thenReturn(payment);

        doThrow(new RuntimeException("Gateway timeout")).when(paymentProcessorService).process(payment);

        assertThrows(RuntimeException.class, () -> processPaymentUseCase.execute(processPaymentCommand));

        verify(paymentValidationService).validatePaymentDoesNotExist(processPaymentCommand.orderId());

        verify(paymentCreationService).create(processPaymentCommand);

        verify(paymentProcessorService).process(payment);

        verify(paymentPersistenceService, never()).save(any());

        verify(paymentEventService, never()).publish(any());
    }

    @Test
    @DisplayName("Deve parar fluxo quando persistencia falhar")
    void shouldStopWorkflowWhenPersistenceFails() {

        ProcessPaymentCommand processPaymentCommand = buildCommand();

        Payment payment = mock(Payment.class);

        when(paymentCreationService.create(processPaymentCommand)).thenReturn(payment);

        doThrow(new RuntimeException("Database error")).when(paymentPersistenceService).save(payment);

        assertThrows(RuntimeException.class, () -> processPaymentUseCase.execute(processPaymentCommand));

        verify(paymentProcessorService).process(payment);

        verify(paymentPersistenceService).save(payment);

        verify(paymentEventService, never()).publish(any());
    }

    @Test
    @DisplayName("Deve parar fluxo quando publicacao de evento falhar")
    void shouldStopWorkflowWhenEventPublicationFails() {

        ProcessPaymentCommand processPaymentCommand = buildCommand();

        Payment payment = mock(Payment.class);

        when(paymentCreationService.create(processPaymentCommand)).thenReturn(payment);

        doThrow(new RuntimeException("Kafka unavailable")).when(paymentEventService).publish(payment);

        assertThrows(RuntimeException.class, () -> processPaymentUseCase.execute(processPaymentCommand));

        verify(paymentPersistenceService).save(payment);

        verify(paymentEventService).publish(payment);
    }

    private ProcessPaymentCommand buildCommand() {

        return new ProcessPaymentCommand(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(100), "BRL", PaymentMethod.CREDIT_CARD, 1);
    }
}