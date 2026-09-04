package com.lmf.notification.notificationservice.unit.application;

import com.lmf.notification.notificationservice.application.port.out.NotificationDeliveryException;
import com.lmf.notification.notificationservice.application.port.out.NotificationSender;
import com.lmf.notification.notificationservice.application.service.NotificationDispatchService;
import com.lmf.notification.notificationservice.application.service.NotificationText;
import com.lmf.notification.notificationservice.domain.model.Notification;
import com.lmf.notification.notificationservice.domain.model.NotificationRecipient;
import com.lmf.notification.notificationservice.domain.model.NotificationStatus;
import com.lmf.notification.notificationservice.domain.model.NotificationType;
import com.lmf.notification.notificationservice.domain.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class NotificationDispatchServiceTest {

    private NotificationSender notificationSender;

    private NotificationRepository notificationRepository;

    private NotificationDispatchService dispatchService;

    private final UUID orderId = UUID.randomUUID();

    private final UUID customerId = UUID.randomUUID();

    private final NotificationText text = new NotificationText("assunto", "corpo");

    @BeforeEach
    void setUp() {
        notificationSender = mock(NotificationSender.class);
        notificationRepository = mock(NotificationRepository.class);
        dispatchService = new NotificationDispatchService(notificationSender, notificationRepository);
    }

    @Test
    void recordsSkippedAndDoesNotCallSenderWhenRecipientIsNull() {

        dispatchService.dispatch(orderId, customerId, NotificationType.PAYMENT_APPROVED, text, null);

        verify(notificationSender, never()).send(any());
        assertThat(capturedNotification().getStatus()).isEqualTo(NotificationStatus.SKIPPED);
    }

    @Test
    void recordsSentWhenSenderSucceeds() {

        dispatchService.dispatch(orderId, customerId, NotificationType.ORDER_CREATED, text, recipient());

        Notification saved = capturedNotification();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(saved.getRecipient()).isEqualTo("ana@example.com");
    }

    @Test
    void recordsFailedAndDoesNotRethrowWhenDeliveryFails() {

        doThrow(new NotificationDeliveryException("smtp down")).when(notificationSender).send(any());

        assertThatCode(() -> dispatchService.dispatch(orderId, customerId, NotificationType.PAYMENT_FAILED, text, recipient()))
                .doesNotThrowAnyException();

        Notification saved = capturedNotification();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(saved.getFailureReason()).isEqualTo("smtp down");
    }

    @Test
    void usesPhoneAsRecipientWhenEmailMissing() {

        NotificationRecipient phoneOnly = NotificationRecipient.of(orderId, customerId, "Ana", null, "11999998888");

        dispatchService.dispatch(orderId, customerId, NotificationType.ORDER_CREATED, text, phoneOnly);

        Notification saved = capturedNotification();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(saved.getRecipient()).isEqualTo("11999998888");
    }

    private NotificationRecipient recipient() {
        return NotificationRecipient.of(orderId, customerId, "Ana", "ana@example.com", "11999998888");
    }

    private Notification capturedNotification() {
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        return captor.getValue();
    }
}
