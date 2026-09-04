package com.lmf.fraud.fraudservice.infrastructure.config;

import com.lmf.fraud.fraudservice.domain.repository.FraudBlocklistRepository;
import com.lmf.fraud.fraudservice.domain.service.FraudRulesEvaluator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Wiring do {@link FraudRulesEvaluator} — o domínio não conhece {@code @Value}, então o limite
 * configurável é lido aqui, na infraestrutura, e injetado via construtor.
 */
@Configuration
public class FraudRulesConfig {

    @Bean
    public FraudRulesEvaluator fraudRulesEvaluator(
            @Value("${fraud.rules.max-order-amount}") BigDecimal maxOrderAmount,
            FraudBlocklistRepository blocklistRepository) {

        return new FraudRulesEvaluator(maxOrderAmount, blocklistRepository);
    }
}
