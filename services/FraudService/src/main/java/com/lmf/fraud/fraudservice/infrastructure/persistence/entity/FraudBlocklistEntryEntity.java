package com.lmf.fraud.fraudservice.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "fraud_blocklist")
public class FraudBlocklistEntryEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected FraudBlocklistEntryEntity() {
    }

    public FraudBlocklistEntryEntity(UUID id, UUID customerId, String customerEmail, String reason, OffsetDateTime createdAt) {

        this.id = id;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getReason() {
        return reason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
