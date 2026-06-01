package com.lmf.inventory.inventoryservice.domain.event.order;

import java.math.BigDecimal;

public record PaymentInfo(

        PaymentMethod paymentMethod,

        Integer installments,

        BigDecimal paidAmount) {
}
