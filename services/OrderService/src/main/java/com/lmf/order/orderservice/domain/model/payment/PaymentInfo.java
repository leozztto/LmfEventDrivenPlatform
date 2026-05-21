package com.lmf.order.orderservice.domain.model.payment;

import java.math.BigDecimal;

public record PaymentInfo(

        PaymentMethod paymentMethod,

        Integer installments,

        BigDecimal paidAmount) {
}
