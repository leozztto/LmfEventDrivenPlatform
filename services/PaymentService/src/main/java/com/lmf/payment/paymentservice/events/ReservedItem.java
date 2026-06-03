package com.lmf.payment.paymentservice.events;

import java.util.UUID;

public record ReservedItem(

        UUID productId,

        Integer quantity

) {
}
