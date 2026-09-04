package com.lmf.notification.notificationservice.e2e;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lmf.notification.notificationservice.Fixtures;
import com.lmf.notification.notificationservice.domain.model.NotificationStatus;
import com.lmf.notification.notificationservice.domain.model.NotificationType;
import com.lmf.notification.notificationservice.domain.repository.NotificationRecipientRepository;
import com.lmf.notification.notificationservice.domain.repository.NotificationRepository;
import com.lmf.notification.notificationservice.infrastructure.persistence.repository.SpringDataNotificationRecipientRepository;
import com.lmf.notification.notificationservice.infrastructure.persistence.repository.SpringDataNotificationRepository;
import com.lmf.notification.notificationservice.integration.AbstractIntegrationTest;
import com.lmf.platform.contracts.EventMessage;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.messaging.InboxEventRepository;
import org.junit.jupiter.api.AfterEach;
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

/**
 * Ponta a ponta pelo broker real: publica nos quatro tópicos da coreografia exatamente como
 * OrderService / PaymentService / InventoryService fariam (JSON no tópico de produção, sem type
 * headers) e verifica o desfecho apenas pelo estado persistido do NotificationService — contato
 * gravado a partir do {@code order.created} e um registro em {@code notifications} por evento, com
 * o status correto. Também exercita a idempotência de ponta a ponta (redelivery do mesmo eventId).
 *
 * <p>Precisa de um broker acessível (Docker/Linux). Roda no failsafe, no pacote {@code e2e}.
 */
class NotificationChoreographyE2ETest extends AbstractIntegrationTest {

    private static final JsonMapper JSON = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .build();

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

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

    @BeforeEach
    void setUp() {
        springDataNotificationRepository.deleteAll();
        springDataNotificationRecipientRepository.deleteAll();
        inboxEventRepository.deleteAll();
        kafkaListenerEndpointRegistry.start();
    }

    @AfterEach
    void stopListeners() {
        // Restaura o baseline "listeners desligados" para as classes de integração que rodam depois
        // e não esperam consumir do broker.
        kafkaListenerEndpointRegistry.stop();
    }

    @Test
    void happyPathSagaProducesRecipientAndOneNotificationPerEvent() throws Exception {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        publish("order.created", orderId, Fixtures.orderCreated(orderId, customerId));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(notificationRecipientRepository.findByOrderId(orderId)).get()
                        .satisfies(r -> assertThat(r.getEmail()).isEqualTo("ana@example.com")));

        publish("payment.approved", orderId, Fixtures.paymentApproved(orderId, customerId));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(typesFor(orderId)).contains(NotificationType.ORDER_CREATED, NotificationType.PAYMENT_APPROVED));

        assertThat(notificationRepository.findByOrderId(orderId))
                .allSatisfy(n -> assertThat(n.getStatus()).isEqualTo(NotificationStatus.SENT));
    }

    @Test
    void inventoryReservationFailedBeforeOrderKnownIsRecordedAsSkipped() {

        UUID orderId = UUID.randomUUID();

        publish("inventory.reservation.failed", orderId, Fixtures.inventoryReservationFailed(orderId));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(notificationRepository.findByOrderId(orderId)).singleElement().satisfies(n -> {
                    assertThat(n.getType()).isEqualTo(NotificationType.INVENTORY_RESERVATION_FAILED);
                    assertThat(n.getStatus()).isEqualTo(NotificationStatus.SKIPPED);
                    assertThat(n.getRecipient()).isNull();
                }));
    }

    @Test
    void paymentFailedAfterOrderKnownNotifiesTheStoredRecipient() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        publish("order.created", orderId, Fixtures.orderCreated(orderId, customerId));
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(notificationRecipientRepository.findByOrderId(orderId)).isPresent());

        publish("payment.failed", orderId, Fixtures.paymentFailed(orderId, customerId));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(notificationRepository.findByOrderId(orderId)).anySatisfy(n -> {
                    assertThat(n.getType()).isEqualTo(NotificationType.PAYMENT_FAILED);
                    assertThat(n.getStatus()).isEqualTo(NotificationStatus.SENT);
                    assertThat(n.getRecipient()).isEqualTo("ana@example.com");
                }));
    }

    @Test
    void redeliveryOfTheSameEventDoesNotDuplicateTheNotification() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        OrderCreatedEvent event = Fixtures.orderCreated(orderId, customerId);

        publish("order.created", orderId, event);
        publish("order.created", orderId, event);

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(notificationRepository.findByOrderId(orderId)).isNotEmpty());

        // Dá tempo para uma eventual segunda entrega ser processada (e deduplicada).
        await().during(Duration.ofSeconds(3)).atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(notificationRepository.findByOrderId(orderId)).hasSize(1));
    }

    private List<NotificationType> typesFor(UUID orderId) {
        return notificationRepository.findByOrderId(orderId).stream().map(n -> n.getType()).toList();
    }

    private void publish(String topic, UUID orderId, EventMessage event) {
        try {
            kafkaTemplate.send(topic, orderId.toString(), JSON.writeValueAsString(event)).get();
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao publicar em " + topic, exception);
        }
    }
}
