package com.lmf.payment.paymentservice.events;

import java.util.UUID;

public interface EventMessage {

    UUID eventId();

    String eventType();
}

