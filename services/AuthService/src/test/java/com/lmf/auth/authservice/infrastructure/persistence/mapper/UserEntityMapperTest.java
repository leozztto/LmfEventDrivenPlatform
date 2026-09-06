package com.lmf.auth.authservice.infrastructure.persistence.mapper;

import com.lmf.auth.authservice.domain.model.user.Email;
import com.lmf.auth.authservice.domain.model.user.Role;
import com.lmf.auth.authservice.domain.model.user.User;
import com.lmf.auth.authservice.infrastructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityMapperTest {

    private final UserEntityMapper mapper = new UserEntityMapper();

    @Test
    void idaEVolta() {
        User original = new User(UUID.randomUUID(), "alice", new Email("alice@example.com"), "HASH",
                Set.of(Role.ROLE_USER, Role.ROLE_ADMIN), true, OffsetDateTime.now());

        UserEntity entity = mapper.toEntity(original);
        User roundTrip = mapper.toDomain(entity);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getUsername()).isEqualTo("alice");
        assertThat(roundTrip.getEmail().value()).isEqualTo("alice@example.com");
        assertThat(roundTrip.getPasswordHash()).isEqualTo("HASH");
        assertThat(roundTrip.isEnabled()).isTrue();
        assertThat(roundTrip.getRoles()).containsExactlyInAnyOrder(Role.ROLE_USER, Role.ROLE_ADMIN);
    }
}
