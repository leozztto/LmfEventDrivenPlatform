package com.lmf.order.orderservice.infrastructure.messaging;

import com.lmf.order.orderservice.domain.model.OutboxStatus;
import com.lmf.order.orderservice.domain.repository.OutboxEventRepository;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OutboxEventEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxProcessor {

    private static final String ORDER_CREATED_TOPIC = "orders.created";

    private final OutboxEventRepository outboxEventRepository;
    private final OrderEventPublisher orderEventPublisher;

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void process() {

        var pendingEvents = outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        log.info("Processing pending outbox events. totalEvents={}", pendingEvents.size());

        if (pendingEvents.isEmpty()) {
            log.debug("No pending outbox events found");
            return;
        }

        for (var event : pendingEvents) {
            processEvent(event);
        }
    }

    private void processEvent(OutboxEventEntity outboxEventEntity) {

        try {

            log.info("Processing outbox event. eventId={}, retryCount={}", outboxEventEntity.getId(), outboxEventEntity.getRetryCount());

            outboxEventEntity.markAsProcessing();

            outboxEventRepository.update(outboxEventEntity);

            log.info("Publishing event to Kafka. topic={}, aggregateId={}, eventType={}, eventId={}", ORDER_CREATED_TOPIC, outboxEventEntity.getAggregateId(), outboxEventEntity.getEventType(), outboxEventEntity.getId());

            orderEventPublisher.publish(ORDER_CREATED_TOPIC, outboxEventEntity.getAggregateId().toString(), outboxEventEntity.getPayload());

            outboxEventEntity.markAsPublished();

            outboxEventRepository.update(outboxEventEntity);

            log.info("Outbox event published successfully. eventId={}, aggregateId={}, eventType={}, topic={}, status={}", outboxEventEntity.getId(), outboxEventEntity.getAggregateId(), outboxEventEntity.getEventType(), ORDER_CREATED_TOPIC, outboxEventEntity.getOutboxStatus());

        } catch (Exception ex) {

            outboxEventEntity.markAsFailed(ex.getMessage());

            outboxEventRepository.update(outboxEventEntity);

            log.warn("Outbox event failed. eventId={}, retryCount={}, error={}", outboxEventEntity.getId(), outboxEventEntity.getRetryCount(), ex.getMessage());

            if (outboxEventEntity.getOutboxStatus() == OutboxStatus.DLQ) {

                log.error("Outbox event moved to DLQ. eventId={}, retryCount={}, error={}", outboxEventEntity.getId(), outboxEventEntity.getRetryCount(), outboxEventEntity.getErrorMessage());

            } else {

                outboxEventEntity.markAsPendingRetry();

                outboxEventRepository.save(outboxEventEntity);

                log.info("Outbox event returned to PENDING for retry. eventId={}, retryCount={}", outboxEventEntity.getId(), outboxEventEntity.getRetryCount());
            }
        }
    }
}