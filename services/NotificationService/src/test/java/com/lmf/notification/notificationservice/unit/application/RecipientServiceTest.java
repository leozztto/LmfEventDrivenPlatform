package com.lmf.notification.notificationservice.unit.application;

import com.lmf.notification.notificationservice.Fixtures;
import com.lmf.notification.notificationservice.application.service.RecipientService;
import com.lmf.notification.notificationservice.domain.model.NotificationRecipient;
import com.lmf.notification.notificationservice.domain.repository.NotificationRecipientRepository;
import com.lmf.platform.contracts.OrderCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecipientServiceTest {

    private NotificationRecipientRepository repository;

    private RecipientService recipientService;

    @BeforeEach
    void setUp() {
        repository = mock(NotificationRecipientRepository.class);
        recipientService = new RecipientService(repository);
    }

    @Test
    void createsRecipientFromOrderWhenNoneExists() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        OrderCreatedEvent event = Fixtures.orderCreated(orderId, customerId);
        when(repository.findByOrderId(orderId)).thenReturn(Optional.empty());

        NotificationRecipient result = recipientService.upsertFromOrder(event);

        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        assertThat(result.getEmail()).isEqualTo("ana@example.com");
        verify(repository).save(result);
    }

    @Test
    void updatesExistingRecipient() {

        UUID orderId = UUID.randomUUID();
        NotificationRecipient existing = NotificationRecipient.of(orderId, UUID.randomUUID(), "Old", "old@x.com", "0000");
        when(repository.findByOrderId(orderId)).thenReturn(Optional.of(existing));
        OrderCreatedEvent event = Fixtures.orderCreated(orderId, UUID.randomUUID());

        recipientService.upsertFromOrder(event);

        ArgumentCaptor<NotificationRecipient> captor = ArgumentCaptor.forClass(NotificationRecipient.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("ana@example.com");
        assertThat(captor.getValue().getName()).isEqualTo("Ana Souza");
    }

    @Test
    void toleratesOrderWithoutCustomer() {

        UUID orderId = UUID.randomUUID();
        when(repository.findByOrderId(orderId)).thenReturn(Optional.empty());

        NotificationRecipient result = recipientService.upsertFromOrder(Fixtures.orderCreatedWithoutCustomer(orderId));

        assertThat(result.getCustomerId()).isNull();
        assertThat(result.getEmail()).isNull();
        verify(repository).save(any());
    }

    @Test
    void resolveReturnsEmptyWhenUnknown() {

        UUID orderId = UUID.randomUUID();
        when(repository.findByOrderId(orderId)).thenReturn(Optional.empty());

        assertThat(recipientService.resolve(orderId)).isEmpty();
    }
}
