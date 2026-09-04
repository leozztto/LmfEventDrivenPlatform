package com.lmf.fraud.fraudservice.application.usecase;

import com.lmf.fraud.fraudservice.domain.model.FraudBlocklistEntry;

import java.util.UUID;

public interface ManageBlocklistUseCase {

    FraudBlocklistEntry create(UUID customerId, String customerEmail, String reason);

    void delete(UUID id);
}
