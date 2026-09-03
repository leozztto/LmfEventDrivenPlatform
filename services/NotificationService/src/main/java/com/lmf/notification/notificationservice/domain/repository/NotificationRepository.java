package com.lmf.notification.notificationservice.domain.repository;

import com.lmf.notification.notificationservice.domain.model.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository {

    void save(Notification notification);

    List<Notification> findByOrderId(UUID orderId);
}
