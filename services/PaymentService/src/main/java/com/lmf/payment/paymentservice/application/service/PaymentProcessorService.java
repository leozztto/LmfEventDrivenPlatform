package com.lmf.payment.paymentservice.application.service;

import com.lmf.payment.paymentservice.application.gateway.PaymentGateway;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayRequest;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayResponse;
import com.lmf.payment.paymentservice.application.gateway.PaymentGatewayProvider;
import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.infrastructure.observability.PaymentMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessorService {

    private final PaymentGatewayProvider gatewayProvider;

    private final PaymentMetricsService metricsService;

    /**
     * Processa o pagamento no gateway e aplica o resultado ao agregado.
     * <p>
     * Uma <b>recusa</b> do gateway é um resultado de negócio: o pagamento fica {@code FAILED} e o
     * fluxo segue normalmente (o {@code PAYMENT_FAILED} é persistido no outbox pelo caso de uso).
     * Só um <b>erro de infraestrutura</b> do gateway (timeout, indisponibilidade) sobe como exceção,
     * para retentativa e, se persistir, DLT.
     */
    public void process(Payment payment) {

        log.info("Processing payment. paymentId={}, provider={}, amount={}, method={}", payment.getId(), payment.getProvider(), payment.getAmount(), payment.getPaymentMethod());

        PaymentGatewayRequest paymentGatewayRequest = new PaymentGatewayRequest(payment.getId(), payment.getOrderId(), payment.getCustomerId(), payment.getAmount(), payment.getCurrency(), payment.getPaymentMethod(), payment.getInstallments());

        PaymentGateway paymentGateway = gatewayProvider.resolve(payment.getPaymentMethod());

        log.info("Payment gateway resolved. paymentId={}, gateway={}", payment.getId(), paymentGateway.getClass().getSimpleName());

        PaymentGatewayResponse paymentGatewayResponse = paymentGateway.process(paymentGatewayRequest);

        if (paymentGatewayResponse.success()) {

            payment.approve(paymentGatewayResponse.transactionId(), paymentGatewayResponse.gatewayStatus());

            log.info("Payment approved. paymentId={}, transactionId={}", payment.getId(), paymentGatewayResponse.transactionId());

            metricsService.incrementApprovedPayments();

            return;
        }

        payment.fail(paymentGatewayResponse.failureReason(), paymentGatewayResponse.gatewayStatus());

        log.warn("Payment declined by gateway. paymentId={}, reason={}, gatewayStatus={}", payment.getId(), paymentGatewayResponse.failureReason(), paymentGatewayResponse.gatewayStatus());

        metricsService.incrementFailedPayments();
    }
}
