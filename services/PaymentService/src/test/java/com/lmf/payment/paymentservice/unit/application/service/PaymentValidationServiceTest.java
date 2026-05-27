package com.lmf.payment.paymentservice.unit.application.service;

import com.lmf.payment.paymentservice.application.service.PaymentValidationService;
import com.lmf.payment.paymentservice.domain.exception.PaymentAlreadyProcessedException;
import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.domain.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PaymentValidationServiceTest {

    private PaymentRepository paymentRepository;

    private PaymentValidationService paymentValidationService;

    @BeforeEach
    void setUp() {

        paymentRepository = mock(PaymentRepository.class);

        paymentValidationService = new PaymentValidationService(paymentRepository);
    }

    @Test
    @DisplayName("Nao deve lançar excecao quando pagamento nao existir")
    void shouldNotThrowExceptionWhenPaymentDoesNotExist() {

        UUID orderId = UUID.randomUUID();

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        paymentValidationService.validatePaymentDoesNotExist(orderId);

        verify(paymentRepository).findByOrderId(orderId);
    }

    @Test
    @DisplayName("Deve lançar PaymentAlreadyProcessedException quando pagamento ja existir")
    void shouldThrowPaymentAlreadyProcessedExceptionWhenPaymentAlreadyExists() {

        UUID orderId = UUID.randomUUID();

        Payment payment = mock(Payment.class);

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        assertThrows(PaymentAlreadyProcessedException.class, () -> paymentValidationService.validatePaymentDoesNotExist(orderId));

        verify(paymentRepository).findByOrderId(orderId);
    }

    @Test
    @DisplayName("Deve consultar repositorio apenas uma vez")
    void shouldCallRepositoryOnlyOnce() {

        UUID orderId = UUID.randomUUID();

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        paymentValidationService.validatePaymentDoesNotExist(orderId);

        verify(paymentRepository, times(1)).findByOrderId(orderId);

        verifyNoMoreInteractions(paymentRepository);
    }
}