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

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void process() {

        var pendingEvents = outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

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

            outboxEventEntity.markAsProcessing();
            outboxEventRepository.update(outboxEventEntity);

            orderEventPublisher.publish(
                    ORDER_CREATED_TOPIC,
                    outboxEventEntity.getAggregateId().toString(),
                    outboxEventEntity.getPayload()
            );

            outboxEventEntity.markAsPublished();
            outboxEventRepository.update(outboxEventEntity);

            log.info("Outbox event published successfully. eventId={}", outboxEventEntity.getId());

        } catch (Exception ex) {

            outboxEventEntity.markAsFailed();
            outboxEventRepository.update(outboxEventEntity);

            log.error("Failed to publish outbox event. eventId={}", outboxEventEntity.getId(), ex);
        }
    }
}