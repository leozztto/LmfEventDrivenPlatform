package com.lmf.payment.paymentservice.unit.infrasctruture.observability;

import com.lmf.payment.paymentservice.infrastructure.observability.PaymentMetricsService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentMetricsServiceTest {

    private SimpleMeterRegistry meterRegistry;

    private PaymentMetricsService paymentMetricsService;

    @BeforeEach
    void setUp() {

        meterRegistry = new SimpleMeterRegistry();

        paymentMetricsService = new PaymentMetricsService(meterRegistry);
    }

    @Test
    @DisplayName("Should increment approved payments counter")
    void shouldIncrementApprovedPaymentsCounter() {

        paymentMetricsService.incrementApprovedPayments();

        Counter counter = meterRegistry.get("payment_approved_total").counter();

        assertEquals(1.0, counter.count());
    }

    @Test
    @DisplayName("Should increment failed payments counter")
    void shouldIncrementFailedPaymentsCounter() {

        paymentMetricsService.incrementFailedPayments();

        Counter counter = meterRegistry.get("payment_failed_total").counter();

        assertEquals(1.0, counter.count());
    }

    @Test
    @DisplayName("Should increment approved payments counter multiple times")
    void shouldIncrementApprovedPaymentsCounterMultipleTimes() {

        paymentMetricsService.incrementApprovedPayments();
        paymentMetricsService.incrementApprovedPayments();
        paymentMetricsService.incrementApprovedPayments();

        Counter counter = meterRegistry.get("payment_approved_total").counter();

        assertEquals(3.0, counter.count());
    }

    @Test
    @DisplayName("Should increment failed payments counter multiple times")
    void shouldIncrementFailedPaymentsCounterMultipleTimes() {

        paymentMetricsService.incrementFailedPayments();
        paymentMetricsService.incrementFailedPayments();

        Counter counter = meterRegistry.get("payment_failed_total").counter();

        assertEquals(2.0, counter.count());
    }
}