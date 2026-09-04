package com.lmf.notification.notificationservice.integration;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lmf.notification.notificationservice.Fixtures;
import com.lmf.notification.notificationservice.application.usecase.NotifyInventoryReservationFailedUseCase;
import com.lmf.notification.notificationservice.application.usecase.NotifyOrderCreatedUseCase;
import com.lmf.notification.notificationservice.application.usecase.NotifyPaymentApprovedUseCase;
import com.lmf.notification.notificationservice.application.usecase.NotifyPaymentFailedUseCase;
import com.lmf.notification.notificationservice.domain.model.Notification;
import com.lmf.notification.notificationservice.domain.model.NotificationRecipient;
import com.lmf.notification.notificationservice.domain.model.NotificationStatus;
import com.lmf.notification.notificationservice.domain.model.NotificationType;
import com.lmf.notification.notificationservice.domain.repository.NotificationRecipientRepository;
import com.lmf.notification.notificationservice.domain.repository.NotificationRepository;
import com.lmf.notification.notificationservice.infrastructure.kafka.consumer.PaymentApprovedConsumer;
import com.lmf.notification.notificationservice.infrastructure.persistence.repository.SpringDataNotificationRecipientRepository;
import com.lmf.notification.notificationservice.infrastructure.persistence.repository.SpringDataNotificationRepository;
import com.lmf.platform.contracts.PaymentApprovedEvent;
import com.lmf.platform.messaging.InboxEventRepository;
import com.lmf.platform.messaging.InboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class NotificationFlowIntegrationTest extends AbstractIntegrationTest {

    private static final JsonMapper JSON = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .build();

    @Autowired
    private NotifyOrderCreatedUseCase notifyOrderCreatedUseCase;

    @Autowired
    private NotifyPaymentApprovedUseCase notifyPaymentApprovedUseCase;

    @Autowired
    private NotifyInventoryReservationFailedUseCase notifyInventoryReservationFailedUseCase;

    @Autowired
    private NotifyPaymentFailedUseCase notifyPaymentFailedUseCase;

    @Autowired
    private PaymentApprovedConsumer paymentApprovedConsumer;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationRecipientRepository notificationRecipientRepository;

    @Autowired
    private InboxEventRepository inboxEventRepository;

    @Autowired
    private SpringDataNotificationRepository springDataNotificationRepository;

    @Autowired
    private SpringDataNotificationRecipientRepository springDataNotificationRecipientRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @BeforeEach
    void clean() {
        springDataNotificationRepository.deleteAll();
        springDataNotificationRecipientRepository.deleteAll();
        inboxEventRepository.deleteAll();
    }

    @Test
    void orderCreatedStoresRecipientAndSendsNotification() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        notifyOrderCreatedUseCase.execute(Fixtures.orderCreated(orderId, customerId));

        assertThat(notificationRecipientRepository.findByOrderId(orderId)).get()
                .satisfies(r -> assertThat(r.getEmail()).isEqualTo("ana@example.com"));

        assertThat(notificationRepository.findByOrderId(orderId)).singleElement().satisfies(n -> {
            assertThat(n.getType()).isEqualTo(NotificationType.ORDER_CREATED);
            assertThat(n.getStatus()).isEqualTo(NotificationStatus.SENT);
        });
    }

    @Test
    void paymentApprovedReusesRecipientFromEarlierOrder() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        notifyOrderCreatedUseCase.execute(Fixtures.orderCreated(orderId, customerId));
        notifyPaymentApprovedUseCase.execute(Fixtures.paymentApproved(orderId, customerId));

        List<Notification> notifications = notificationRepository.findByOrderId(orderId);
        assertThat(notifications).hasSize(2);
        assertThat(notifications).anySatisfy(n -> {
            assertThat(n.getType()).isEqualTo(NotificationType.PAYMENT_APPROVED);
            assertThat(n.getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(n.getRecipient()).isEqualTo("ana@example.com");
        });
    }

    @Test
    void paymentFailedReusesRecipientAndRecordsSentNotification() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        notifyOrderCreatedUseCase.execute(Fixtures.orderCreated(orderId, customerId));
        notifyPaymentFailedUseCase.execute(Fixtures.paymentFailed(orderId, customerId));

        assertThat(notificationRepository.findByOrderId(orderId)).anySatisfy(n -> {
            assertThat(n.getType()).isEqualTo(NotificationType.PAYMENT_FAILED);
            assertThat(n.getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(n.getRecipient()).isEqualTo("ana@example.com");
            assertThat(n.getBody()).contains("Saldo insuficiente");
        });
    }

    @Test
    void repeatedOrderCreatedUpdatesRecipientWithoutDuplicating() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        notifyOrderCreatedUseCase.execute(Fixtures.orderCreated(orderId, customerId));
        notifyOrderCreatedUseCase.execute(Fixtures.orderCreated(orderId, customerId));

        assertThat(springDataNotificationRecipientRepository.count()).isEqualTo(1);
        assertThat(notificationRecipientRepository.findByOrderId(orderId)).get()
                .satisfies(r -> assertThat(r.getEmail()).isEqualTo("ana@example.com"));
        assertThat(notificationRepository.findByOrderId(orderId)).hasSize(2);
    }

    @Test
    void notificationForUnknownOrderIsSkipped() {

        UUID orderId = UUID.randomUUID();

        notifyInventoryReservationFailedUseCase.execute(Fixtures.inventoryReservationFailed(orderId));

        assertThat(notificationRepository.findByOrderId(orderId)).singleElement().satisfies(n -> {
            assertThat(n.getType()).isEqualTo(NotificationType.INVENTORY_RESERVATION_FAILED);
            assertThat(n.getStatus()).isEqualTo(NotificationStatus.SKIPPED);
            assertThat(n.getRecipient()).isNull();
        });
    }

    @Test
    void inboxDeduplicatesRepeatedDelivery() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        notificationRecipientRepository.save(NotificationRecipient.of(orderId, customerId, "Ana", "ana@example.com", "1"));

        PaymentApprovedEvent event = Fixtures.paymentApproved(orderId, customerId);

        paymentApprovedConsumer.consume(event);
        paymentApprovedConsumer.consume(event);

        assertThat(notificationRepository.findByOrderId(orderId)).hasSize(1);
        assertThat(inboxEventRepository.findByEventId(event.eventId().toString())).get()
                .satisfies(inbox -> assertThat(inbox.getStatus()).isEqualTo(InboxStatus.PROCESSED));
    }

    /**
     * Round-trip real pelo broker: prova que o novo consumer group {@code notification-service-group}
     * recebe {@code payment.approved} (fan-out da coreografia, sem tocar no PaymentService) e que a
     * config de deserialização do contrato está correta. Precisa de um broker acessível (Docker/Linux).
     */
    @Test
    void consumesPaymentApprovedPublishedToKafka() throws Exception {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        notificationRecipientRepository.save(NotificationRecipient.of(orderId, customerId, "Ana", "ana@example.com", "1"));

        // Este é o único teste que precisa dos listeners de fato conectados ao broker.
        kafkaListenerEndpointRegistry.start();

        PaymentApprovedEvent event = Fixtures.paymentApproved(orderId, customerId);
        kafkaTemplate.send("payment.approved", orderId.toString(), JSON.writeValueAsString(event));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(notificationRepository.findByOrderId(orderId)).anySatisfy(n -> {
                    assertThat(n.getType()).isEqualTo(NotificationType.PAYMENT_APPROVED);
                    assertThat(n.getStatus()).isEqualTo(NotificationStatus.SENT);
                }));
    }
}
