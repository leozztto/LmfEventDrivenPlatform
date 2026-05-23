package com.lmf.payment.paymentservice.ports.output;

import com.lmf.payment.paymentservice.events.PaymentCreatedEvent;

public interface PaymentEventPublisher {

    void publish(PaymentCreatedEvent paymentCreatedEvent);
}
