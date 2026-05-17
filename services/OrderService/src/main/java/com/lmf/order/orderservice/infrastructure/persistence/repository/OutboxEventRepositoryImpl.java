package com.lmf.order.orderservice.infrastructure.persistence.repository;

import com.lmf.order.orderservice.domain.repository.OutboxEventRepository;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OutboxEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepositoryImpl implements OutboxEventRepository {

    private final JpaOutboxEventRepository jpaOutboxEventRepository;

    @Override
    public void save(OutboxEventEntity outboxEventEntity) {
        jpaOutboxEventRepository.save(outboxEventEntity);
    }
}