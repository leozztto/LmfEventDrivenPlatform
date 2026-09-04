package com.lmf.notification.notificationservice.unit.domain;

import com.lmf.notification.notificationservice.domain.model.Notification;
import com.lmf.notification.notificationservice.domain.model.NotificationChannel;
import com.lmf.notification.notificationservice.domain.model.NotificationContent;
import com.lmf.notification.notificationservice.domain.model.NotificationStatus;
import com.lmf.notification.notificationservice.domain.model.NotificationType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    private final UUID orderId = UUID.randomUUID();

    private final UUID customerId = UUID.randomUUID();

    private final NotificationContent content =
            new NotificationContent(NotificationChannel.LOG, "ana@example.com", "Pedido recebido", "corpo");

    @Test
    void sentCarriesStatusTimestampAndContent() {

        Notification notification = Notification.sent(orderId, customerId, NotificationType.ORDER_CREATED, content);

        assertThat(notification.getId()).isNotNull();
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getSentAt()).isNotNull();
        assertThat(notification.getFailureReason()).isNull();
        assertThat(notification.getChannel()).isEqualTo(NotificationChannel.LOG);
        assertThat(notification.getRecipient()).isEqualTo("ana@example.com");
        assertThat(notification.getSubject()).isEqualTo("Pedido recebido");
        assertThat(notification.getCreatedAt()).isNotNull();
    }

    @Test
    void failedCarriesReasonAndNoSentAt() {

        Notification notification = Notification.failed(orderId, customerId, NotificationType.PAYMENT_FAILED, content, "smtp down");

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getFailureReason()).isEqualTo("smtp down");
        assertThat(notification.getSentAt()).isNull();
    }

    @Test
    void skippedCarriesReasonAndNoSentAt() {

        Notification notification = Notification.skipped(orderId, null, NotificationType.INVENTORY_RESERVATION_FAILED, content, "sem destinatário");

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SKIPPED);
        assertThat(notification.getFailureReason()).isEqualTo("sem destinatário");
        assertThat(notification.getCustomerId()).isNull();
        assertThat(notification.getSentAt()).isNull();
    }
}
