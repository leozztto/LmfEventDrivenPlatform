package com.lmf.order.orderservice.application.usecase;

import com.lmf.order.orderservice.domain.exception.OrderNotFoundException;
import com.lmf.order.orderservice.domain.model.order.Order;
import com.lmf.order.orderservice.domain.model.order.OrderStatus;
import com.lmf.order.orderservice.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Aplica ao pedido o desfecho da saga (pagamento aprovado/recusado, reserva de estoque falha).
 * <p>
 * É idempotente por estado: se o pedido não está mais em {@link OrderStatus#PENDING_PAYMENT}, a
 * transição já foi aplicada por uma entrega anterior e o evento é ignorado. Isso dispensa uma tabela
 * de inbox nesta fase (o Inbox Pattern comum entra na Fase 2).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateOrderStatusUseCase {

    private final OrderRepository orderRepository;

    @Transactional
    public void approvePayment(UUID orderId) {

        applyTransition(orderId, "payment approved", Order::approvePayment);
    }

    @Transactional
    public void rejectPayment(UUID orderId) {

        applyTransition(orderId, "payment failed", Order::rejectPayment);
    }

    @Transactional
    public void cancelForInventoryFailure(UUID orderId) {

        applyTransition(orderId, "inventory reservation failed", Order::cancel);
    }

    private void applyTransition(UUID orderId, String reason, Consumer<Order> transition) {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getOrderStatus() != OrderStatus.PENDING_PAYMENT) {

            log.info("Ignoring saga event — order no longer pending. orderId={}, currentStatus={}, reason={}", orderId, order.getOrderStatus(), reason);

            return;
        }

        transition.accept(order);

        orderRepository.save(order);

        log.info("Order status updated. orderId={}, newStatus={}, reason={}", orderId, order.getOrderStatus(), reason);
    }
}
