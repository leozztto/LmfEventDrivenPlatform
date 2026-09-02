package com.lmf.platform.contracts;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItem(

        UUID productId,

        Integer quantity,

        BigDecimal unitPrice,

        BigDecimal subtotal) {
}
