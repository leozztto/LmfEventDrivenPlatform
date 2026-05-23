package com.lmf.payment.paymentservice.events;

import java.util.UUID;

public record CustomerInfo(

        UUID customerId,

        String name,

        String email,

        String phone) {
}
