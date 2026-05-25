package com.lmf.order.orderservice.infrastructure.messaging;

import com.lmf.order.orderservice.domain.model.outbox.OutboxStatus;
import com.lmf.order.orderservice.domain.repository.OutboxEventRepository;
import com.lmf.order.orderservice.infrastructure.messaging.event.DltEvent;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OutboxEventEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;

    private final OrderEventPublisher orderEventPublisher;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void process() {

        var pendingEvents = outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

         if (pendingEvents.isEmpty()) {
            log.debug("No pending outbox events found");
            return;
        }

        log.info("Processing pending outbox events. totalEvents={}", pendingEvents.size());

        for (var event : pendingEvents) {
            processEvent(event);
        }
    }

    private void processEvent(OutboxEventEntity outboxEventEntity) {

        try {

            log.info("Processing outbox event. eventId={}, retryCount={}", outboxEventEntity.getId(), outboxEventEntity.getRetryCount());

            outboxEventEntity.markAsProcessing();

            outboxEventRepository.update(outboxEventEntity);

            log.info("Publishing event to Kafka. topic={}, aggregateId={}, eventType={}, eventId={}", KafkaTopics.ORDER_CREATED, outboxEventEntity.getAggregateId(), outboxEventEntity.getEventType(), outboxEventEntity.getId());

            orderEventPublisher.publish(KafkaTopics.ORDER_CREATED, outboxEventEntity.getAggregateId().toString(), outboxEventEntity.getPayload());

            outboxEventEntity.markAsPublished();

            outboxEventRepository.update(outboxEventEntity);

            log.info("Outbox event published successfully. eventId={}, aggregateId={}, eventType={}, topic={}, status={}", outboxEventEntity.getId(), outboxEventEntity.getAggregateId(), outboxEventEntity.getEventType(), KafkaTopics.ORDER_CREATED, outboxEventEntity.getOutboxStatus());

        } catch (Exception ex) {

            outboxEventEntity.markAsFailed(ex.getMessage());

            outboxEventRepository.update(outboxEventEntity);

            log.warn("Outbox event failed. eventId={}, retryCount={}, error={}", outboxEventEntity.getId(), outboxEventEntity.getRetryCount(), ex.getMessage());

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

        orderEventPublisher.publish(KafkaTopics.ORDER_CREATED_DLT, outboxEventEntity.getAggregateId().toString(), dltEvent.toString());

        log.error("Event published to DLT topic. eventId={}, dltTopic={}", outboxEventEntity.getId(), KafkaTopics.ORDER_CREATED_DLT);
    }
}