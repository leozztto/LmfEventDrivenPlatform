package com.lmf.platform.messaging;

import com.lmf.platform.contracts.EventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Base para consumidores Kafka idempotentes.
 * <p>
 * O método {@code @KafkaListener} da subclasse deve ser {@code @Transactional}: assim o registro no
 * inbox, o processamento de negócio e o {@code markProcessed} ficam na mesma transação. Se o
 * processamento falhar, tudo é revertido e o evento pode ser reprocessado numa nova entrega — o
 * {@code DefaultErrorHandler} do Kafka cuida das retentativas e da DLT.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractInboxConsumer<T extends EventMessage> {

    private final InboxService inboxService;

    protected void process(T event, UUID aggregateId, EventProcessor<T> processor) {

        String eventId = event.eventId().toString();

        if (inboxService.isAlreadyProcessed(eventId)) {
            log.info("Ignoring already-processed event. eventId={}", eventId);
            return;
        }

        try {
            inboxService.register(eventId, aggregateId, event.eventType());
        } catch (DuplicateEventException exception) {
            log.info("Event is already being processed by another consumer. eventId={}", eventId);
            return;
        }

        processor.process(event);

        inboxService.markProcessed(eventId);

        log.info("Event processed. eventId={}, aggregateId={}, eventType={}", eventId, aggregateId, event.eventType());
    }

    @FunctionalInterface
    public interface EventProcessor<T> {
        void process(T event);
    }
}
