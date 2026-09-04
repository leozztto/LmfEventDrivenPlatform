package com.lmf.fraud.fraudservice.domain.service;

import com.lmf.fraud.fraudservice.domain.model.FraudDecision;
import com.lmf.fraud.fraudservice.domain.repository.FraudBlocklistRepository;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Regras de fraude da v1: limite de valor + lista de bloqueio. Classe pura de domínio, sem
 * dependência de framework — o limite e o repositório de blocklist são injetados via construtor
 * pela camada de infraestrutura (ver {@code FraudRulesConfig}).
 */
public class FraudRulesEvaluator {

    private final BigDecimal maxOrderAmount;
    private final FraudBlocklistRepository blocklistRepository;

    public FraudRulesEvaluator(BigDecimal maxOrderAmount, FraudBlocklistRepository blocklistRepository) {

        this.maxOrderAmount = maxOrderAmount;
        this.blocklistRepository = blocklistRepository;
    }

    public FraudDecision evaluate(UUID customerId, String customerEmail, BigDecimal totalAmount) {

        if (totalAmount != null && totalAmount.compareTo(maxOrderAmount) > 0) {
            return FraudDecision.reject("Order amount " + totalAmount + " exceeds limit " + maxOrderAmount);
        }

        if (blocklistRepository.existsByCustomerIdOrEmail(customerId, customerEmail)) {
            return FraudDecision.reject("Customer is blocklisted");
        }

        return FraudDecision.approve();
    }
}
