package com.lmf.payment.paymentservice.application.service;

import com.lmf.payment.paymentservice.application.gateway.PaymentGateway;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayRequest;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayResponse;
import com.lmf.payment.paymentservice.application.gateway.impl.PaymentGatewayResolver;
import com.lmf.payment.paymentservice.domain.exception.PaymentDeclinedException;
import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.infrastructure.observability.PaymentMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessorService {

    private final PaymentGatewayResolver gatewayResolver;

    private final PaymentMetricsService metricsService;

    public void process(Payment payment) {

        log.info("Processing payment. paymentId={}, provider={}, amount={}, method={}", payment.getId(), payment.getProvider(), payment.getAmount(), payment.getPaymentMethod());

        PaymentGatewayRequest paymentGatewayRequest = new PaymentGatewayRequest(payment.getId(), payment.getOrderId(), payment.getCustomerId(), payment.getAmount(), payment.getCurrency(), payment.getPaymentMethod(), payment.getInstallments());

        log.info("Resolving payment gateway. paymentId={}, paymentMethod={}", payment.getId(), payment.getPaymentMethod());

        PaymentGateway paymentGateway = gatewayResolver.resolve(payment.getPaymentMethod());

        log.info("Payment gateway resolved. paymentId={}, gateway={}", payment.getId(), paymentGateway.getClass().getSimpleName());

        PaymentGatewayResponse paymentGatewayResponse = paymentGateway.process(paymentGatewayRequest);

        if (paymentGatewayResponse.success()) {

            payment.approve(paymentGatewayResponse.transactionId(), paymentGatewayResponse.gatewayStatus());

            log.info("Payment approved successfully. paymentId={}, transactionId={}", payment.getId(), paymentGatewayResponse.transactionId());

            metricsService.incrementApprovedPayments();

            return;
        }

        payment.fail(paymentGatewayResponse.failureReason(), paymentGatewayResponse.gatewayStatus());

        log.warn("Payment failed. paymentId={}, reason={}, gatewayStatus={}", payment.getId(), paymentGatewayResponse.failureReason(), paymentGatewayResponse.gatewayStatus());

        metricsService.incrementFailedPayments();

        throw new PaymentDeclinedException(paymentGatewayResponse.failureReason());
    }
}
