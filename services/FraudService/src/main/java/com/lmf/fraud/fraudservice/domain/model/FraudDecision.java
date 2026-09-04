package com.lmf.fraud.fraudservice.domain.model;

/**
 * Resultado da avaliação das regras de fraude sobre um pedido.
 */
public record FraudDecision(boolean approved, String reason) {

    public static FraudDecision approve() {
        return new FraudDecision(true, null);
    }

    public static FraudDecision reject(String reason) {
        return new FraudDecision(false, reason);
    }
}
