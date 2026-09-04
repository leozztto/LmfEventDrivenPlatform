package com.lmf.fraud.fraudservice.infrastructure.persistence.repository;

import com.lmf.fraud.fraudservice.infrastructure.persistence.entity.FraudCheckEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataFraudCheckRepository extends JpaRepository<FraudCheckEntity, UUID> {
}
