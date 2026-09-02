package com.lmf.order.orderservice.domain.repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta para a deduplicação de requisições de criação de pedido por {@code Idempotency-Key}.
 */
public interface IdempotencyStore {

    Optional<UUID> findOrderIdByKey(String idempotencyKey);

    /**
     * Reserva a chave para um pedido. Lança {@link org.springframework.dao.DataIntegrityViolationException}
     * se a chave já estiver reservada (corrida entre requisições concorrentes).
     */
    void reserve(String idempotencyKey, UUID orderId);
}
