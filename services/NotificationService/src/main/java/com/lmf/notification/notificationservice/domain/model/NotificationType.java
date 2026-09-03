package com.lmf.notification.notificationservice.domain.model;

/**
 * Tipo de notificação, um por evento de saga consumido pelo serviço.
 */
public enum NotificationType {

    ORDER_CREATED,
    PAYMENT_APPROVED,
    PAYMENT_FAILED,
    INVENTORY_RESERVATION_FAILED
}
