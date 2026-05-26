package com.lmf.payment.paymentservice.application.gateway.dto;

public record PaymentGatewayResponse(

        boolean success,

        String transactionId,

        String gatewayStatus,

        String failureReason) {
}
