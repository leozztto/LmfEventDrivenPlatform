package com.lmf.notification.notificationservice.infrastructure.persistence.repository;

import com.lmf.notification.notificationservice.infrastructure.persistence.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataNotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    List<NotificationEntity> findByOrderId(UUID orderId);
}
