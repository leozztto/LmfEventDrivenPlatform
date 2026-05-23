package com.lmf.payment.paymentservice.ports.output;

import com.lmf.payment.paymentservice.domain.Payment;

public interface PaymentRepository {

    void save(Payment payment);
}
