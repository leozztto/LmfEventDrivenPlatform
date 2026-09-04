package com.lmf.fraud.fraudservice.infrastructure.persistence.mapper;

import com.lmf.fraud.fraudservice.domain.model.FraudBlocklistEntry;
import com.lmf.fraud.fraudservice.infrastructure.persistence.entity.FraudBlocklistEntryEntity;

public final class FraudBlocklistEntryMapper {

    private FraudBlocklistEntryMapper() {
    }

    public static FraudBlocklistEntryEntity toEntity(FraudBlocklistEntry entry) {

        return new FraudBlocklistEntryEntity(
                entry.getId(),
                entry.getCustomerId(),
                entry.getCustomerEmail(),
                entry.getReason(),
                entry.getCreatedAt());
    }

    public static FraudBlocklistEntry toDomain(FraudBlocklistEntryEntity entity) {

        return new FraudBlocklistEntry(
                entity.getId(),
                entity.getCustomerId(),
                entity.getCustomerEmail(),
                entity.getReason(),
                entity.getCreatedAt());
    }
}
