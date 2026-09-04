package com.lmf.platform.contracts;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Publicado no tópico {@code fraud.rejected} pelo FraudService quando um pedido é rejeitado pelas
 * regras de fraude (limite de valor ou lista de bloqueio). Consumido pelo OrderService para cancelar
 * o pedido (compensação) antes de qualquer reserva de estoque ou processamento de pagamento.
 */
public record FraudRejectedEvent(

        UUID eventId,

        String eventType,

        String eventVersion,

        OffsetDateTime occurredAt,

        UUID orderId,

        String reason

) implements EventMessage {

    public static final String TYPE = "FRAUD_REJECTED";
}
