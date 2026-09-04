package com.lmf.fraud.fraudservice.infrastructure.persistence.mapper;

import com.lmf.fraud.fraudservice.domain.model.FraudCheck;
import com.lmf.fraud.fraudservice.infrastructure.persistence.entity.FraudCheckEntity;

public final class FraudCheckEntityMapper {

    private FraudCheckEntityMapper() {
    }

    public static FraudCheckEntity toEntity(FraudCheck fraudCheck) {

        return new FraudCheckEntity(
                fraudCheck.getId(),
                fraudCheck.getOrderId(),
                fraudCheck.getCustomerId(),
                fraudCheck.isApproved() ? "APPROVED" : "REJECTED",
                fraudCheck.getReason(),
                fraudCheck.getTotalAmount(),
                fraudCheck.getCreatedAt());
    }

    public static FraudCheck toDomain(FraudCheckEntity entity) {

        return new FraudCheck(
                entity.getId(),
                entity.getOrderId(),
                entity.getCustomerId(),
                "APPROVED".equals(entity.getDecision()),
                entity.getReason(),
                entity.getTotalAmount(),
                entity.getCreatedAt());
    }
}
