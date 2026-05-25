package com.lmf.payment.paymentservice.infrastructure.kafka.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.payment.paymentservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.payment.paymentservice.infrastructure.kafka.dlt.DltEvent;
import com.lmf.payment.paymentservice.infrastructure.outbox.OutboxStatus;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.lmf.payment.paymentservice.domain.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventProcessor {

    private final OutboxEventRepository outboxEventRepository;

    private final PaymentEventPublisher paymentEventPublisher;

    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void process() {

        var pendingEvents = outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            log.debug("No pending outbox events found");
            return;
        }

        log.info("Processing {} outbox events", pendingEvents.size());

        for (var event : pendingEvents) {
            processEvent(event);
        }
    }

    private void processEvent(OutboxEventEntity outboxEventEntity) {

        try {

            log.info("Processing outbox event. eventId={}, retryCount={}", outboxEventEntity.getId(), outboxEventEntity.getRetryCount());

            outboxEventEntity.markAsProcessing();

            outboxEventRepository.update(outboxEventEntity);

            log.info("Publishing event to Kafka. topic={}, aggregateId={}, eventType={}, eventId={}", KafkaTopics.PAYMENT_CREATED, outboxEventEntity.getAggregateId(), outboxEventEntity.getEventType(), outboxEventEntity.getId());

            paymentEventPublisher.publish(KafkaTopics.PAYMENT_CREATED, outboxEventEntity.getAggregateId().toString(), outboxEventEntity.getPayload());

            outboxEventEntity.markAsPublished();

            outboxEventRepository.update(outboxEventEntity);

            log.info("Outbox event published successfully. eventId={}, aggregateId={}, eventType={}, topic={}, status={}", outboxEventEntity.getId(), outboxEventEntity.getAggregateId(), outboxEventEntity.getEventType(), KafkaTopics.PAYMENT_CREATED, outboxEventEntity.getOutboxStatus());

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

        paymentEventPublisher.publish(KafkaTopics.PAYMENT_CREATED_DLT, outboxEventEntity.getAggregateId().toString(), dltEvent.toString());

        log.error("Event published to DLT topic. eventId={}, dltTopic={}", outboxEventEntity.getId(), KafkaTopics.PAYMENT_CREATED_DLT);
    }
}
