package com.lmf.payment.paymentservice.infrastructure.kafka.consumer;

import com.lmf.payment.paymentservice.application.service.InboxEventService;
import com.lmf.payment.paymentservice.events.EventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractInboxConsumer<T extends EventMessage> {

    private final InboxEventService inboxEventService;

    protected void process(T event, UUID aggregateId, EventProcessor<T> processor) {

        String eventId = event.eventId().toString();

        log.info("Processing event. eventId={}, aggregateId={}, eventType={}", eventId, aggregateId, event.eventType());

        if (inboxEventService.isDuplicate(eventId)) {

            log.info("Ignoring duplicate event. eventId={}", eventId);

            return;
        }

        inboxEventService.register(eventId, aggregateId, event.eventType());

        try {

            processor.process(event);

            inboxEventService.markProcessed(eventId);

            log.info("Event processed successfully. eventId={}, aggregateId={}, eventType={}", eventId, aggregateId, event.eventType());

        } catch (Exception exception) {

            inboxEventService.markFailed(eventId, extractReason(exception));

            log.error("Error processing event. eventId={}, aggregateId={}, eventType={}", eventId, aggregateId, event.eventType(), exception);

            throw exception;
        }
    }

    private String extractReason(Throwable throwable) {

        return Optional.ofNullable(throwable.getMessage()).orElse(throwable.getClass().getSimpleName());
    }

    @FunctionalInterface
    public interface EventProcessor<T> {

        void process(T event);
    }
}
