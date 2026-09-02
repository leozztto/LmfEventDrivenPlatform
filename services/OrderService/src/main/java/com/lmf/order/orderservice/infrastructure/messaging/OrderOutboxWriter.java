package com.lmf.order.orderservice.infrastructure.messaging;

import com.lmf.order.orderservice.domain.model.order.Order;
import com.lmf.order.orderservice.infrastructure.messaging.mapper.OrderCreatedEventMapper;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.messaging.OutboxWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapeia o agregado para o contrato de evento e delega a gravação no outbox à lib de mensageria.
 */
@Component
@RequiredArgsConstructor
public class OrderOutboxWriter {

    private final OrderCreatedEventMapper orderCreatedEventMapper;

    private final OutboxWriter outboxWriter;

    public void writeOrderCreated(Order order) {

        OrderCreatedEvent event = orderCreatedEventMapper.toEvent(order);

        outboxWriter.write(order.getId(), "ORDER", OrderCreatedEvent.TYPE, event);
    }
}
