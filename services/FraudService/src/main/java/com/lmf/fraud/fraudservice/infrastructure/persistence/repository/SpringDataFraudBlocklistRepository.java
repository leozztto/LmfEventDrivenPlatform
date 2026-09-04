package com.lmf.fraud.fraudservice.infrastructure.persistence.repository;

import com.lmf.fraud.fraudservice.infrastructure.persistence.entity.FraudBlocklistEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataFraudBlocklistRepository extends JpaRepository<FraudBlocklistEntryEntity, UUID> {

    boolean existsByCustomerIdOrCustomerEmail(UUID customerId, String customerEmail);
}
