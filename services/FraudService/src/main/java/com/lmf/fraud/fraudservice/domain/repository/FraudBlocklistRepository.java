package com.lmf.fraud.fraudservice.domain.repository;

import com.lmf.fraud.fraudservice.domain.model.FraudBlocklistEntry;

import java.util.Optional;
import java.util.UUID;

public interface FraudBlocklistRepository {

    boolean existsByCustomerIdOrEmail(UUID customerId, String customerEmail);

    FraudBlocklistEntry save(FraudBlocklistEntry entry);

    Optional<FraudBlocklistEntry> findById(UUID id);

    void deleteById(UUID id);
}
