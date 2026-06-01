package com.lmf.inventory.inventoryservice.infrastructure.persistence.repository;

import com.lmf.inventory.inventoryservice.domain.repository.InboxEventRepository;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.InboxEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InboxEventRepositoryImpl implements InboxEventRepository {

    private final SpringDataInboxEventRepository springDataInboxEventRepository;

    @Override
    public boolean existsByEventId(String eventId) {

        return springDataInboxEventRepository.existsByEventId(eventId);
    }

    @Override
    public InboxEventEntity save(InboxEventEntity inboxEventEntity) {

        return springDataInboxEventRepository.save(inboxEventEntity);
    }

    @Override
    public Optional<InboxEventEntity> findByEventId(String eventId) {

        return springDataInboxEventRepository.findByEventId(eventId);
    }
}
