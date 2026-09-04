package com.lmf.platform.contracts;

import java.math.BigDecimal;

public record PaymentInfo(

        PaymentMethod paymentMethod,

        Integer installments,

        BigDecimal amount) {
}
