package com.lmf.payment.paymentservice.infrastructure.persistence.repository;

import com.lmf.payment.paymentservice.infrastructure.outbox.OutboxStatus;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.lmf.payment.paymentservice.domain.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepositoryImpl implements OutboxEventRepository {

    private final SpringDataOutboxEventRepository springDataOutboxEventRepository;

    @Override
    public void save(OutboxEventEntity outboxEventEntity) {

        springDataOutboxEventRepository.save(outboxEventEntity);
    }

    @Override
    public List<OutboxEventEntity> findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus outboxStatus) {
        return springDataOutboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(outboxStatus);
    }

    @Override
    public void update(OutboxEventEntity outboxEventEntity) {
        springDataOutboxEventRepository.saveAndFlush(outboxEventEntity);
    }

    @Override
    public List<OutboxEventEntity> findAll() {
        return springDataOutboxEventRepository.findAll();
    }

    @Override
    public Optional<OutboxEventEntity> findById(UUID id) {
        return springDataOutboxEventRepository.findById(id);
    }
}
