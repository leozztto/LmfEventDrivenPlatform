package com.lmf.fraud.fraudservice.application.service;

import com.lmf.fraud.fraudservice.application.usecase.ManageBlocklistUseCase;
import com.lmf.fraud.fraudservice.domain.exception.BlocklistEntryNotFoundException;
import com.lmf.fraud.fraudservice.domain.model.FraudBlocklistEntry;
import com.lmf.fraud.fraudservice.domain.repository.FraudBlocklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageBlocklistService implements ManageBlocklistUseCase {

    private final FraudBlocklistRepository blocklistRepository;

    @Override
    @Transactional
    public FraudBlocklistEntry create(UUID customerId, String customerEmail, String reason) {

        FraudBlocklistEntry entry = FraudBlocklistEntry.create(customerId, customerEmail, reason);

        FraudBlocklistEntry saved = blocklistRepository.save(entry);

        log.info("Blocklist entry created. id={}, customerId={}, customerEmail={}", saved.getId(), saved.getCustomerId(), saved.getCustomerEmail());

        return saved;
    }

    @Override
    @Transactional
    public void delete(UUID id) {

        blocklistRepository.findById(id).orElseThrow(() -> new BlocklistEntryNotFoundException(id));

        blocklistRepository.deleteById(id);

        log.info("Blocklist entry deleted. id={}", id);
    }
}
