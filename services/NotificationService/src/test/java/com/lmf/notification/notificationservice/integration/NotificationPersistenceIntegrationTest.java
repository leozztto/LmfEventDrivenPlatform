package com.lmf.notification.notificationservice.integration;

import com.lmf.notification.notificationservice.domain.model.Notification;
import com.lmf.notification.notificationservice.domain.model.NotificationChannel;
import com.lmf.notification.notificationservice.domain.model.NotificationContent;
import com.lmf.notification.notificationservice.domain.model.NotificationRecipient;
import com.lmf.notification.notificationservice.domain.model.NotificationStatus;
import com.lmf.notification.notificationservice.domain.model.NotificationType;
import com.lmf.notification.notificationservice.domain.repository.NotificationRecipientRepository;
import com.lmf.notification.notificationservice.domain.repository.NotificationRepository;
import com.lmf.notification.notificationservice.infrastructure.persistence.repository.SpringDataNotificationRecipientRepository;
import com.lmf.notification.notificationservice.infrastructure.persistence.repository.SpringDataNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Round-trip real pelo Postgres: prova que os mappers manuais dos dois adapters de persistência
 * (domínio &lt;-&gt; entidade JPA) preservam todos os campos, incluindo enums, nulos e o desfecho.
 */
class NotificationPersistenceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationRecipientRepository notificationRecipientRepository;

    @Autowired
    private SpringDataNotificationRepository springDataNotificationRepository;

    @Autowired
    private SpringDataNotificationRecipientRepository springDataNotificationRecipientRepository;

    @BeforeEach
    void clean() {
        springDataNotificationRepository.deleteAll();
        springDataNotificationRecipientRepository.deleteAll();
    }

    @Test
    void sentNotificationRoundTrips() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        NotificationContent content = new NotificationContent(NotificationChannel.LOG, "ana@example.com", "Pedido recebido", "corpo");
        Notification sent = Notification.sent(orderId, customerId, NotificationType.ORDER_CREATED, content);

        notificationRepository.save(sent);

        assertThat(notificationRepository.findByOrderId(orderId)).singleElement().satisfies(n -> {
            assertThat(n.getId()).isEqualTo(sent.getId());
            assertThat(n.getCustomerId()).isEqualTo(customerId);
            assertThat(n.getType()).isEqualTo(NotificationType.ORDER_CREATED);
            assertThat(n.getChannel()).isEqualTo(NotificationChannel.LOG);
            assertThat(n.getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(n.getRecipient()).isEqualTo("ana@example.com");
            assertThat(n.getSubject()).isEqualTo("Pedido recebido");
            assertThat(n.getBody()).isEqualTo("corpo");
            assertThat(n.getFailureReason()).isNull();
            assertThat(n.getCreatedAt()).isNotNull();
            assertThat(n.getSentAt()).isNotNull();
        });
    }

    @Test
    void skippedNotificationRoundTripsWithoutRecipient() {

        UUID orderId = UUID.randomUUID();
        NotificationContent content = new NotificationContent(NotificationChannel.LOG, null, "assunto", "corpo");
        Notification skipped = Notification.skipped(orderId, null, NotificationType.PAYMENT_FAILED, content, "sem destinatário");

        notificationRepository.save(skipped);

        assertThat(notificationRepository.findByOrderId(orderId)).singleElement().satisfies(n -> {
            assertThat(n.getStatus()).isEqualTo(NotificationStatus.SKIPPED);
            assertThat(n.getRecipient()).isNull();
            assertThat(n.getCustomerId()).isNull();
            assertThat(n.getFailureReason()).isEqualTo("sem destinatário");
            assertThat(n.getSentAt()).isNull();
        });
    }

    @Test
    void recipientIsUpsertedByOrderId() {

        UUID orderId = UUID.randomUUID();

        notificationRecipientRepository.save(NotificationRecipient.of(orderId, UUID.randomUUID(), "Old", "old@x.com", "0000"));

        NotificationRecipient stored = notificationRecipientRepository.findByOrderId(orderId).orElseThrow();
        stored.update(UUID.randomUUID(), "New", "new@x.com", "1111");
        notificationRecipientRepository.save(stored);

        assertThat(springDataNotificationRecipientRepository.count()).isEqualTo(1);
        assertThat(notificationRecipientRepository.findByOrderId(orderId)).get().satisfies(r -> {
            assertThat(r.getName()).isEqualTo("New");
            assertThat(r.getEmail()).isEqualTo("new@x.com");
            assertThat(r.getPhone()).isEqualTo("1111");
        });
    }
}
