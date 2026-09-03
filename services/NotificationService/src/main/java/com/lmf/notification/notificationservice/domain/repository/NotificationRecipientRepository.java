package com.lmf.notification.notificationservice.domain.repository;

import com.lmf.notification.notificationservice.domain.model.NotificationRecipient;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRecipientRepository {

    void save(NotificationRecipient recipient);

    Optional<NotificationRecipient> findByOrderId(UUID orderId);
}
