package com.lmf.payment.paymentservice.integration.persistence;

import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import com.lmf.payment.paymentservice.domain.model.PaymentStatus;
import com.lmf.payment.paymentservice.domain.repository.PaymentRepository;
import com.lmf.payment.paymentservice.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void shouldSaveAndFindPaymentByOrderId() {

        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        Payment payment = Payment.restore(paymentId, orderId, customerId, new BigDecimal("299.90"), "BRL", PaymentMethod.CREDIT_CARD, 3, PaymentStatus.APPROVED, "MERCADO_PAGO", "txn-123", "APPROVED", OffsetDateTime.now(), OffsetDateTime.now(), null, OffsetDateTime.now(), null);

        paymentRepository.save(payment);

        Optional<Payment> optionalPayment = paymentRepository.findByOrderId(orderId);

        assertTrue(optionalPayment.isPresent());
        assertEquals(orderId, optionalPayment.get().getOrderId());
        assertEquals(PaymentStatus.APPROVED, optionalPayment.get().getPaymentStatus());
    }
}
