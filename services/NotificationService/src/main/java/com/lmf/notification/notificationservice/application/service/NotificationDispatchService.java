package com.lmf.notification.notificationservice.application.service;

import com.lmf.notification.notificationservice.application.port.out.NotificationDeliveryException;
import com.lmf.notification.notificationservice.application.port.out.NotificationSender;
import com.lmf.notification.notificationservice.domain.model.Notification;
import com.lmf.notification.notificationservice.domain.model.NotificationChannel;
import com.lmf.notification.notificationservice.domain.model.NotificationContent;
import com.lmf.notification.notificationservice.domain.model.NotificationRecipient;
import com.lmf.notification.notificationservice.domain.model.NotificationType;
import com.lmf.notification.notificationservice.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Entrega a notificação pelo canal configurado e grava o registro no histórico. É best-effort:
 * uma falha de entrega vira um registro {@link com.lmf.notification.notificationservice.domain.model.NotificationStatus#FAILED}
 * e <b>não</b> é propagada — o consumidor Kafka considera o evento processado e a saga não reprocessa.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    // Fase 1: envio fake via log. EMAIL/SMS entram como adapters + seleção de canal depois.
    private static final NotificationChannel CHANNEL = NotificationChannel.LOG;

    private final NotificationSender notificationSender;

    private final NotificationRepository notificationRepository;

    public void dispatch(UUID orderId, UUID customerId, NotificationType type, NotificationText text, NotificationRecipient recipient) {

        if (recipient == null) {

            NotificationContent content = new NotificationContent(CHANNEL, null, text.subject(), text.body());

            notificationRepository.save(Notification.skipped(orderId, customerId, type, content,
                    "Destinatário desconhecido para o pedido " + orderId));

            log.warn("Notification skipped — unknown recipient. orderId={}, type={}", orderId, type);

            return;
        }

        NotificationContent content = new NotificationContent(CHANNEL, recipient.bestAddress(), text.subject(), text.body());

        Notification notification;

        try {

            notificationSender.send(content);

            notification = Notification.sent(orderId, customerId, type, content);

        } catch (NotificationDeliveryException exception) {

            log.error("Notification delivery failed. orderId={}, type={}, error={}", orderId, type, exception.getMessage());

            notification = Notification.failed(orderId, customerId, type, content, exception.getMessage());
        }

        notificationRepository.save(notification);

        log.info("Notification recorded. orderId={}, type={}, status={}", orderId, type, notification.getStatus());
    }
}
