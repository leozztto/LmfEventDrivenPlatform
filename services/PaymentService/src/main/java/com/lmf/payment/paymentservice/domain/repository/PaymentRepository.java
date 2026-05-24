package com.lmf.payment.paymentservice.domain.repository;

import com.lmf.payment.paymentservice.domain.model.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByOrderId(UUID orderId);
}
