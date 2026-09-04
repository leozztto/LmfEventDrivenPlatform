package com.lmf.fraud.fraudservice.domain.model;

import com.lmf.fraud.fraudservice.domain.exception.InvalidBlocklistEntryException;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Um registro de bloqueio de cliente, identificado por {@code customerId} e/ou {@code customerEmail}
 * — pelo menos um dos dois precisa estar presente.
 */
public class FraudBlocklistEntry {

    private final UUID id;
    private final UUID customerId;
    private final String customerEmail;
    private final String reason;
    private final OffsetDateTime createdAt;

    public FraudBlocklistEntry(UUID id, UUID customerId, String customerEmail, String reason, OffsetDateTime createdAt) {

        if (customerId == null && (customerEmail == null || customerEmail.isBlank())) {
            throw new InvalidBlocklistEntryException();
        }

        this.id = id;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public static FraudBlocklistEntry create(UUID customerId, String customerEmail, String reason) {

        return new FraudBlocklistEntry(UUID.randomUUID(), customerId, customerEmail, reason, OffsetDateTime.now());
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
