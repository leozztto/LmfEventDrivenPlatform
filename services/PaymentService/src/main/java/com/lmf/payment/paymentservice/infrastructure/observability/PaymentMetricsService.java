package com.lmf.payment.paymentservice.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class PaymentMetricsService {

    private final Counter approvedPayments;

    private final Counter failedPayments;

    public PaymentMetricsService(MeterRegistry meterRegistry) {

        this.approvedPayments = Counter.builder("payment_approved_total").description("Total approved payments").register(meterRegistry);

        this.failedPayments = Counter.builder("payment_failed_total").description("Total failed payments").register(meterRegistry);
    }

    public void incrementApprovedPayments() {

        approvedPayments.increment();
    }

    public void incrementFailedPayments() {

        failedPayments.increment();
    }
}
