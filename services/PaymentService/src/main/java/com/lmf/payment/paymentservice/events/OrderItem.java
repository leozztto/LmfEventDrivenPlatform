package com.lmf.payment.paymentservice.events;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItem(

        UUID productId,

        Integer quantity,

        BigDecimal price) {
}
