package com.lmf.notification.notificationservice.infrastructure.persistence.repository;

import com.lmf.notification.notificationservice.infrastructure.persistence.entity.NotificationRecipientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataNotificationRecipientRepository extends JpaRepository<NotificationRecipientEntity, UUID> {
}
