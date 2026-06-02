package com.lmf.inventory.inventoryservice.infrastructure.kafka.producer;

import com.lmf.inventory.inventoryservice.domain.event.DltEvent;
import com.lmf.inventory.inventoryservice.domain.repository.OutboxEventRepository;
import com.lmf.inventory.inventoryservice.infrastructure.config.KafkaTopics;
import com.lmf.inventory.inventoryservice.infrastructure.outbox.OutboxStatus;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.OutboxEventEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {

    private final OutboxEventRepository outboxEventRepository;

    private final InventoryEventPublisher inventoryEventPublisher;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void process() {

        List<OutboxEventEntity> outboxEventEntities = outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (outboxEventEntities.isEmpty()) {
            log.debug("No pending outbox events found");
            return;
        }

        log.info("Publishing pending outbox events. batchSize={}", outboxEventEntities.size());

        for (OutboxEventEntity outboxEventEntity : outboxEventEntities) {

            processEvent(outboxEventEntity);
        }
    }

    private void processEvent(OutboxEventEntity outboxEventEntity) {
        try {

            log.info("Processing outbox event. eventId={}, retryCount={}", outboxEventEntity.getId(), outboxEventEntity.getRetryCount());

            outboxEventEntity.markAsProcessing();

            outboxEventRepository.update(outboxEventEntity);

            log.info("Publishing event to Kafka. topic={}, aggregateId={}, eventType={}, eventId={}", KafkaTopics.INVENTORY_RESERVED, outboxEventEntity.getAggregateId(), outboxEventEntity.getEventType(), outboxEventEntity.getId());

            inventoryEventPublisher.publish(KafkaTopics.INVENTORY_RESERVED, outboxEventEntity.getAggregateId().toString(), outboxEventEntity.toString());

            outboxEventEntity.markAsPublished();

            outboxEventRepository.update(outboxEventEntity);

            log.info("Outbox event published successfully. eventId={}, aggregateId={}, eventType={}, topic={}, status={}", outboxEventEntity.getId(), outboxEventEntity.getAggregateId(), outboxEventEntity.getEventType(), KafkaTopics.INVENTORY_RESERVED, outboxEventEntity.getOutboxStatus());

        } catch (Exception exception) {

            outboxEventEntity.markAsFailed(exception.getMessage());

            outboxEventRepository.update(outboxEventEntity);

            log.warn("Outbox event failed. eventId={}, retryCount={}, error={}", outboxEventEntity.getId(), outboxEventEntity.getRetryCount(), exception.getMessage());

            if (outboxEventEntity.getOutboxStatus() == OutboxStatus.DLT) {

                publishToDlt(outboxEventEntity);

                outboxEventRepository.save(outboxEventEntity);

                log.error("Outbox event moved to DLT. eventId={}, retryCount={}, error={}", outboxEventEntity.getId(), outboxEventEntity.getRetryCount(), outboxEventEntity.getErrorMessage());

            } else {

                outboxEventEntity.markAsPendingRetry();

                outboxEventRepository.save(outboxEventEntity);

                log.info("Outbox event returned to PENDING for retry. eventId={}, retryCount={}", outboxEventEntity.getId(), outboxEventEntity.getRetryCount());
            }
        }
    }

    private void publishToDlt(OutboxEventEntity outboxEventEntity) {

        DltEvent dltEvent = new DltEvent(outboxEventEntity.getId(), outboxEventEntity.getAggregateId(), outboxEventEntity.getEventType(), outboxEventEntity.getPayload(), outboxEventEntity.getErrorMessage(), outboxEventEntity.getRetryCount(), OffsetDateTime.now());

        inventoryEventPublisher.publish(KafkaTopics.INVENTORY_RESERVATION_DLT, outboxEventEntity.getAggregateId().toString(), dltEvent.toString());

        log.error("Event published to DLT topic. eventId={}, dltTopic={}", outboxEventEntity.getId(), KafkaTopics.INVENTORY_RESERVATION_DLT);
    }
}
