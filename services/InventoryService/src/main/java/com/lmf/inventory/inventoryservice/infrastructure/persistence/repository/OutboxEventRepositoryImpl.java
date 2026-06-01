package com.lmf.inventory.inventoryservice.infrastructure.persistence.repository;

import com.lmf.inventory.inventoryservice.domain.repository.OutboxEventRepository;
import com.lmf.inventory.inventoryservice.infrastructure.outbox.OutboxStatus;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.OutboxEventEntity;
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
        return Optional.of(springDataOutboxEventRepository.getReferenceById(id));
    }
}
