package com.lmf.inventory.inventoryservice.domain.model;

/**
 * Ciclo de vida de uma reserva de estoque de um pedido:
 * <pre>
 * RESERVED ──► RELEASED   (pagamento recusado / pedido cancelado — estoque volta a ficar disponível)
 *          └─► CONFIRMED  (pagamento aprovado — a reserva vira baixa definitiva)
 * </pre>
 */
public enum ReservationStatus {

    RESERVED, RELEASED, CONFIRMED
}
