package com.lmf.notification.notificationservice.unit.domain;

import com.lmf.notification.notificationservice.domain.model.NotificationRecipient;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationRecipientTest {

    private final UUID orderId = UUID.randomUUID();

    private final UUID customerId = UUID.randomUUID();

    @Test
    void bestAddressPrefersEmail() {

        NotificationRecipient recipient = NotificationRecipient.of(orderId, customerId, "Ana", "ana@example.com", "11999998888");

        assertThat(recipient.bestAddress()).isEqualTo("ana@example.com");
    }

    @Test
    void bestAddressFallsBackToPhoneWhenEmailMissing() {

        assertThat(NotificationRecipient.of(orderId, customerId, "Ana", null, "11999998888").bestAddress())
                .isEqualTo("11999998888");
        assertThat(NotificationRecipient.of(orderId, customerId, "Ana", "  ", "11999998888").bestAddress())
                .isEqualTo("11999998888");
    }

    @Test
    void bestAddressIsNullWhenNoContact() {

        assertThat(NotificationRecipient.of(orderId, customerId, "Ana", null, null).bestAddress()).isNull();
    }

    @Test
    void updateReplacesContactAndBumpsTimestamp() {

        NotificationRecipient recipient = NotificationRecipient.of(orderId, customerId, "Old", "old@x.com", "0000");
        var before = recipient.getUpdatedAt();
        UUID newCustomer = UUID.randomUUID();

        recipient.update(newCustomer, "New", "new@x.com", "1111");

        assertThat(recipient.getCustomerId()).isEqualTo(newCustomer);
        assertThat(recipient.getName()).isEqualTo("New");
        assertThat(recipient.getEmail()).isEqualTo("new@x.com");
        assertThat(recipient.getPhone()).isEqualTo("1111");
        assertThat(recipient.getUpdatedAt()).isAfterOrEqualTo(before);
    }
}
