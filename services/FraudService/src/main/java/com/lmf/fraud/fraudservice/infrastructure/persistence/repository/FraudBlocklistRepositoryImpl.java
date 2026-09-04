package com.lmf.fraud.fraudservice.infrastructure.persistence.repository;

import com.lmf.fraud.fraudservice.domain.model.FraudBlocklistEntry;
import com.lmf.fraud.fraudservice.domain.repository.FraudBlocklistRepository;
import com.lmf.fraud.fraudservice.infrastructure.persistence.mapper.FraudBlocklistEntryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FraudBlocklistRepositoryImpl implements FraudBlocklistRepository {

    private final SpringDataFraudBlocklistRepository repository;

    @Override
    public boolean existsByCustomerIdOrEmail(UUID customerId, String customerEmail) {

        return repository.existsByCustomerIdOrCustomerEmail(customerId, customerEmail);
    }

    @Override
    public FraudBlocklistEntry save(FraudBlocklistEntry entry) {

        return FraudBlocklistEntryMapper.toDomain(repository.save(FraudBlocklistEntryMapper.toEntity(entry)));
    }

    @Override
    public Optional<FraudBlocklistEntry> findById(UUID id) {

        return repository.findById(id).map(FraudBlocklistEntryMapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {

        repository.deleteById(id);
    }
}
