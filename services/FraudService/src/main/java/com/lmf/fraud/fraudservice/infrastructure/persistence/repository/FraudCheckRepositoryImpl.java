package com.lmf.fraud.fraudservice.infrastructure.persistence.repository;

import com.lmf.fraud.fraudservice.domain.model.FraudCheck;
import com.lmf.fraud.fraudservice.domain.repository.FraudCheckRepository;
import com.lmf.fraud.fraudservice.infrastructure.persistence.mapper.FraudCheckEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FraudCheckRepositoryImpl implements FraudCheckRepository {

    private final SpringDataFraudCheckRepository repository;

    @Override
    public FraudCheck save(FraudCheck fraudCheck) {

        return FraudCheckEntityMapper.toDomain(repository.save(FraudCheckEntityMapper.toEntity(fraudCheck)));
    }
}
