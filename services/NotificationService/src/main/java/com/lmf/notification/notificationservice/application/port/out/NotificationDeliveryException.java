package com.lmf.notification.notificationservice.application.port.out;

/**
 * Falha de entrega num canal de notificação. Tratada como best-effort: vira um registro
 * {@code FAILED}, não uma retentativa de saga.
 */
public class NotificationDeliveryException extends RuntimeException {

    public NotificationDeliveryException(String message) {
        super(message);
    }

    public NotificationDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
