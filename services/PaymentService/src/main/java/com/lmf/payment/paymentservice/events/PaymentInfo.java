package com.lmf.payment.paymentservice.events;

import com.lmf.payment.paymentservice.domain.PaymentMethod;

import java.math.BigDecimal;

public record PaymentInfo(

        PaymentMethod paymentMethod,

        Integer installments,

        BigDecimal amount) {
}
