package com.lmf.payment.paymentservice.ports.output;

import com.lmf.payment.paymentservice.domain.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    void save(Payment payment);

    Optional<Payment> findByOrderId(UUID orderId);
}
