package com.lmf.inventory.inventoryservice.infrastructure.kafka.producer;

import com.lmf.inventory.inventoryservice.infrastructure.outbox.OutboxStatus;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {

    private final SpringDataOutboxEventRepository springDataOutboxEventRepository;

    private final InventoryEventPublisher publisher;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void process() {

        List<OutboxEventEntity> outboxEventEntities = springDataOutboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (OutboxEventEntity outboxEventEntity : outboxEventEntities) {

            try {

                outboxEventEntity.markAsProcessing();

                publisher.publish(outboxEventEntity);

                outboxEventEntity.markAsPublished();

            } catch (Exception exception) {

                outboxEventEntity.markAsFailed(exception.getMessage());
            }
        }
    }
}
