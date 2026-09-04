package com.lmf.notification.notificationservice.infrastructure.persistence.repository;

import com.lmf.notification.notificationservice.domain.model.Notification;
import com.lmf.notification.notificationservice.domain.repository.NotificationRepository;
import com.lmf.notification.notificationservice.infrastructure.persistence.entity.NotificationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final SpringDataNotificationRepository springDataNotificationRepository;

    @Override
    public void save(Notification notification) {

        springDataNotificationRepository.save(toEntity(notification));
    }

    @Override
    public List<Notification> findByOrderId(UUID orderId) {

        return springDataNotificationRepository.findByOrderId(orderId).stream().map(this::toDomain).toList();
    }

    private NotificationEntity toEntity(Notification notification) {

        return NotificationEntity.builder()
                .id(notification.getId())
                .orderId(notification.getOrderId())
                .customerId(notification.getCustomerId())
                .type(notification.getType())
                .channel(notification.getChannel())
                .recipient(notification.getRecipient())
                .subject(notification.getSubject())
                .body(notification.getBody())
                .status(notification.getStatus())
                .failureReason(notification.getFailureReason())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .build();
    }

    private Notification toDomain(NotificationEntity entity) {

        return Notification.restore(
                entity.getId(),
                entity.getOrderId(),
                entity.getCustomerId(),
                entity.getType(),
                entity.getChannel(),
                entity.getRecipient(),
                entity.getSubject(),
                entity.getBody(),
                entity.getStatus(),
                entity.getFailureReason(),
                entity.getCreatedAt(),
                entity.getSentAt());
    }
}
