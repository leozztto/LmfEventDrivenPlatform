package com.lmf.auth.authservice.domain.model.user;

import com.lmf.auth.authservice.domain.exception.DisabledUserException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    private static final Email EMAIL = new Email("alice@example.com");

    @Test
    void construtorNovoGeraIdHabilitadoECreatedAt() {
        User user = new User("alice", EMAIL, "hash", Set.of(Role.ROLE_USER));

        assertThat(user.getId()).isNotNull();
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getRoles()).containsExactly(Role.ROLE_USER);
    }

    @Test
    void exigeAoMenosUmPapel() {
        assertThatThrownBy(() -> new User("alice", EMAIL, "hash", Set.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void exigeUsernameEHash() {
        assertThatThrownBy(() -> new User(" ", EMAIL, "hash", Set.of(Role.ROLE_USER)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new User("alice", EMAIL, " ", Set.of(Role.ROLE_USER)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ensureEnabledFalhaQuandoDesabilitado() {
        User user = new User(UUID.randomUUID(), "bob", EMAIL, "hash", Set.of(Role.ROLE_USER), false, OffsetDateTime.now());
        assertThatThrownBy(user::ensureEnabled).isInstanceOf(DisabledUserException.class);
    }

    @Test
    void ensureEnabledPassaQuandoHabilitado() {
        User user = new User("carol", EMAIL, "hash", Set.of(Role.ROLE_USER));
        user.ensureEnabled();
    }
}
