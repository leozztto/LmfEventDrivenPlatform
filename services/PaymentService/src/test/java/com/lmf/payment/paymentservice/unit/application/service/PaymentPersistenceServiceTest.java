package com.lmf.payment.paymentservice.unit.application.service;

import com.lmf.payment.paymentservice.application.service.PaymentPersistenceService;
import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.domain.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class PaymentPersistenceServiceTest {

    private PaymentRepository paymentRepository;

    private PaymentPersistenceService paymentPersistenceService;

    @BeforeEach
    void setUp() {

        paymentRepository = mock(PaymentRepository.class);

        paymentPersistenceService = new PaymentPersistenceService(paymentRepository);
    }

    @Test
    @DisplayName("Deve salvar pagamento com sucesso")
    void shouldSavePaymentSuccessfully() {

        Payment payment = mock(Payment.class);

        paymentPersistenceService.save(payment);

        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("Deve chamar save apenas uma vez")
    void shouldCallSaveOnlyOnce() {

        Payment payment = mock(Payment.class);

        paymentPersistenceService.save(payment);

        verify(paymentRepository, times(1)).save(payment);

        verifyNoMoreInteractions(paymentRepository);
    }
}