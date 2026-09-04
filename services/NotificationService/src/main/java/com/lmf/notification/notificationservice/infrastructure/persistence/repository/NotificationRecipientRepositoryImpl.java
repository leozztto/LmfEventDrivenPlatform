package com.lmf.notification.notificationservice.infrastructure.persistence.repository;

import com.lmf.notification.notificationservice.domain.model.NotificationRecipient;
import com.lmf.notification.notificationservice.domain.repository.NotificationRecipientRepository;
import com.lmf.notification.notificationservice.infrastructure.persistence.entity.NotificationRecipientEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationRecipientRepositoryImpl implements NotificationRecipientRepository {

    private final SpringDataNotificationRecipientRepository springDataNotificationRecipientRepository;

    @Override
    public void save(NotificationRecipient recipient) {

        NotificationRecipientEntity entity = springDataNotificationRecipientRepository.findById(recipient.getOrderId())
                .map(existing -> {
                    existing.update(recipient.getCustomerId(), recipient.getName(), recipient.getEmail(), recipient.getPhone(), recipient.getUpdatedAt());
                    return existing;
                })
                .orElseGet(() -> toEntity(recipient));

        springDataNotificationRecipientRepository.save(entity);
    }

    @Override
    public Optional<NotificationRecipient> findByOrderId(UUID orderId) {

        return springDataNotificationRecipientRepository.findById(orderId).map(this::toDomain);
    }

    private NotificationRecipientEntity toEntity(NotificationRecipient recipient) {

        return NotificationRecipientEntity.builder()
                .orderId(recipient.getOrderId())
                .customerId(recipient.getCustomerId())
                .name(recipient.getName())
                .email(recipient.getEmail())
                .phone(recipient.getPhone())
                .updatedAt(recipient.getUpdatedAt())
                .build();
    }

    private NotificationRecipient toDomain(NotificationRecipientEntity entity) {

        return NotificationRecipient.restore(
                entity.getOrderId(),
                entity.getCustomerId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getUpdatedAt());
    }
}
