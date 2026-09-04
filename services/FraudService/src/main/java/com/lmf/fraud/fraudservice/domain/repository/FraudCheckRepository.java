package com.lmf.fraud.fraudservice.domain.repository;

import com.lmf.fraud.fraudservice.domain.model.FraudCheck;

public interface FraudCheckRepository {

    FraudCheck save(FraudCheck fraudCheck);
}
