package com.lmf.payment.paymentservice.unit.infrasctruture.persistence;

import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import com.lmf.payment.paymentservice.domain.model.PaymentStatus;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.PaymentEntity;
import com.lmf.payment.paymentservice.infrastructure.persistence.mapper.PaymentEntityMapper;
import com.lmf.payment.paymentservice.infrastructure.persistence.repository.PaymentRepositoryImpl;
import com.lmf.payment.paymentservice.infrastructure.persistence.repository.SpringDataPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentRepositoryImplTest {

    @Mock
    private SpringDataPaymentRepository springDataPaymentRepository;

    @Mock
    private PaymentEntityMapper paymentEntityMapper;

    @InjectMocks
    private PaymentRepositoryImpl paymentRepository;

    private Payment payment;

    private PaymentEntity paymentEntity;

    @BeforeEach
    void setUp() {

        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        payment = mock(Payment.class);

        paymentEntity = new PaymentEntity(paymentId, orderId, customerId, new BigDecimal("299.90"), "BRL", PaymentMethod.CREDIT_CARD, 3, PaymentStatus.APPROVED, "MERCADO_PAGO", "txn-123", "APPROVED", now, now, null, now, null);
    }

    @Test
    @DisplayName("Should save payment successfully")
    void shouldSavePaymentSuccessfully() {

        when(paymentEntityMapper.toEntity(payment)).thenReturn(paymentEntity);

        when(springDataPaymentRepository.save(paymentEntity)).thenReturn(paymentEntity);

        when(paymentEntityMapper.toDomain(paymentEntity)).thenReturn(payment);

        Payment savedPayment = paymentRepository.save(payment);

        assertNotNull(savedPayment);
        assertEquals(payment, savedPayment);

        verify(paymentEntityMapper).toEntity(payment);
        verify(springDataPaymentRepository).save(paymentEntity);
        verify(paymentEntityMapper).toDomain(paymentEntity);
    }

    @Test
    @DisplayName("Should find payment by order id")
    void shouldFindPaymentByOrderId() {

        UUID orderId = UUID.randomUUID();

        when(springDataPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(paymentEntity));

        when(paymentEntityMapper.toDomain(paymentEntity)).thenReturn(payment);

        Optional<Payment> result = paymentRepository.findByOrderId(orderId);

        assertTrue(result.isPresent());
        assertEquals(payment, result.get());

        verify(springDataPaymentRepository).findByOrderId(orderId);
        verify(paymentEntityMapper).toDomain(paymentEntity);
    }

    @Test
    @DisplayName("Should return empty optional when payment not found")
    void shouldReturnEmptyOptionalWhenPaymentNotFound() {

        UUID orderId = UUID.randomUUID();

        when(springDataPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        Optional<Payment> result = paymentRepository.findByOrderId(orderId);

        assertTrue(result.isEmpty());

        verify(springDataPaymentRepository).findByOrderId(orderId);

        verify(paymentEntityMapper, never()).toDomain(any(PaymentEntity.class));
    }
}