package com.lmf.order.orderservice.infrastructure.persistence.repository;

import com.lmf.order.orderservice.domain.model.OutboxStatus;
import com.lmf.order.orderservice.domain.repository.OutboxEventRepository;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OutboxEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepositoryImpl implements OutboxEventRepository {

    private final SpringDataOutboxRepository springDataOutboxRepository;

    @Override
    public void save(OutboxEventEntity outboxEventEntity) {
        springDataOutboxRepository.save(outboxEventEntity);
    }

    @Override
    public List<OutboxEventEntity> findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus outboxStatus) {
        return springDataOutboxRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(outboxStatus);
    }

    @Override
    public void update(OutboxEventEntity outboxEventEntity) {
        springDataOutboxRepository.saveAndFlush(outboxEventEntity);
    }
}