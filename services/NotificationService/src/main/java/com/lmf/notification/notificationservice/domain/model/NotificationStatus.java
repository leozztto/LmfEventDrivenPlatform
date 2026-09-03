package com.lmf.notification.notificationservice.domain.model;

/**
 * Desfecho do registro de notificação:
 * <ul>
 *   <li>{@link #SENT} — entregue pelo canal;</li>
 *   <li>{@link #FAILED} — o canal recusou/falhou a entrega;</li>
 *   <li>{@link #SKIPPED} — não havia destinatário conhecido para o pedido.</li>
 * </ul>
 */
public enum NotificationStatus {

    SENT,
    FAILED,
    SKIPPED
}
