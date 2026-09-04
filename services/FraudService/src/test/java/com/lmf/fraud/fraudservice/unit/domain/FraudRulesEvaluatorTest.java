package com.lmf.fraud.fraudservice.unit.domain;

import com.lmf.fraud.fraudservice.domain.model.FraudDecision;
import com.lmf.fraud.fraudservice.domain.repository.FraudBlocklistRepository;
import com.lmf.fraud.fraudservice.domain.service.FraudRulesEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FraudRulesEvaluatorTest {

    private FraudBlocklistRepository blocklistRepository;

    private FraudRulesEvaluator fraudRulesEvaluator;

    @BeforeEach
    void setUp() {

        blocklistRepository = mock(FraudBlocklistRepository.class);

        fraudRulesEvaluator = new FraudRulesEvaluator(new BigDecimal("5000.00"), blocklistRepository);
    }

    @Test
    @DisplayName("Should approve when amount is within limit and customer is not blocklisted")
    void shouldApproveWhenWithinLimitAndNotBlocklisted() {

        when(blocklistRepository.existsByCustomerIdOrEmail(any(), any())).thenReturn(false);

        FraudDecision decision = fraudRulesEvaluator.evaluate(UUID.randomUUID(), "ana@example.com", new BigDecimal("250.00"));

        assertThat(decision.approved()).isTrue();
        assertThat(decision.reason()).isNull();
    }

    @Test
    @DisplayName("Should reject when amount exceeds the configured limit")
    void shouldRejectWhenAmountExceedsLimit() {

        FraudDecision decision = fraudRulesEvaluator.evaluate(UUID.randomUUID(), "ana@example.com", new BigDecimal("5000.01"));

        assertThat(decision.approved()).isFalse();
        assertThat(decision.reason()).contains("exceeds limit");
    }

    @Test
    @DisplayName("Should reject when customer is blocklisted")
    void shouldRejectWhenCustomerIsBlocklisted() {

        when(blocklistRepository.existsByCustomerIdOrEmail(any(), any())).thenReturn(true);

        FraudDecision decision = fraudRulesEvaluator.evaluate(UUID.randomUUID(), "blocked@example.com", new BigDecimal("100.00"));

        assertThat(decision.approved()).isFalse();
        assertThat(decision.reason()).isEqualTo("Customer is blocklisted");
    }

    @Test
    @DisplayName("Should approve an amount exactly at the limit")
    void shouldApproveAmountAtLimit() {

        when(blocklistRepository.existsByCustomerIdOrEmail(any(), any())).thenReturn(false);

        FraudDecision decision = fraudRulesEvaluator.evaluate(UUID.randomUUID(), "ana@example.com", new BigDecimal("5000.00"));

        assertThat(decision.approved()).isTrue();
    }
}
