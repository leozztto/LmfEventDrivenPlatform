package com.lmf.auth.authservice.infrastructure.persistence.mapper;

import com.lmf.auth.authservice.domain.model.user.Email;
import com.lmf.auth.authservice.domain.model.user.User;
import com.lmf.auth.authservice.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {

    public UserEntity toEntity(User user) {
        return new UserEntity(
                user.getId(),
                user.getUsername(),
                user.getEmail().value(),
                user.getPasswordHash(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getRoles());
    }

    public User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getUsername(),
                new Email(entity.getEmail()),
                entity.getPasswordHash(),
                entity.getRoles(),
                entity.isEnabled(),
                entity.getCreatedAt());
    }
}
