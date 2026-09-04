package com.lmf.fraud.fraudservice.infrastructure.web.response;

import com.lmf.fraud.fraudservice.domain.model.FraudBlocklistEntry;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BlocklistEntryResponse(

        UUID id,

        UUID customerId,

        String customerEmail,

        String reason,

        OffsetDateTime createdAt) {

    public static BlocklistEntryResponse from(FraudBlocklistEntry entry) {

        return new BlocklistEntryResponse(entry.getId(), entry.getCustomerId(), entry.getCustomerEmail(), entry.getReason(), entry.getCreatedAt());
    }
}
