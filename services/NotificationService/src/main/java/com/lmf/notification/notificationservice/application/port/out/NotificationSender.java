package com.lmf.notification.notificationservice.application.port.out;

import com.lmf.notification.notificationservice.domain.model.NotificationContent;

/**
 * Porta de saída para a entrega da notificação. A implementação desta fase é um adapter fake que
 * escreve no log ({@code ConsoleNotificationSender}); e-mail/SMS entram como adapters adicionais
 * depois, sem mudar quem chama esta porta.
 */
public interface NotificationSender {

    /**
     * @throws NotificationDeliveryException quando o canal recusa ou falha a entrega. O chamador
     *         registra a notificação como {@code FAILED} — a falha de entrega é best-effort e não
     *         propaga para o consumidor Kafka (não reprocessa a saga).
     */
    void send(NotificationContent content) throws NotificationDeliveryException;
}
