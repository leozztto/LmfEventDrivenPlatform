package com.lmf.order.orderservice.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "idempotency_keys", uniqueConstraints = @UniqueConstraint(columnNames = "idempotency_key"))
public class IdempotencyEntity {

    @Id
    private UUID id;

    @Column(name = "idempotency_key", nullable = false)
    private String key;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    protected IdempotencyEntity() {
    }

    public IdempotencyEntity(String key, UUID orderId) {
        this.id = UUID.randomUUID();
        this.key = key;
        this.orderId = orderId;
    }

    public String getKey() {
        return key;
    }

    public UUID getOrderId() {
        return orderId;
    }
}