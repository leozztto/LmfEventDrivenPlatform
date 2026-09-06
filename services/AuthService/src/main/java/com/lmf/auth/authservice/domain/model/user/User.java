package com.lmf.auth.authservice.domain.model.user;

import com.lmf.auth.authservice.domain.exception.DisabledUserException;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Agregado de usuário. Sem dependências de framework.
 *
 * <p>Dois construtores, no mesmo molde do resto da plataforma: um "novo" (gera id, marca como
 * habilitado e registra o instante de criação) e um de "reidratação" (recebe todos os campos vindos
 * da persistência).
 */
public class User {

    private final UUID id;
    private final String username;
    private final Email email;
    private final String passwordHash;
    private final Set<Role> roles;
    private final boolean enabled;
    private final OffsetDateTime createdAt;

    public User(String username, Email email, String passwordHash, Set<Role> roles) {
        validate(username, email, passwordHash, roles);
        this.id = UUID.randomUUID();
        this.username = username.trim();
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = EnumSet.copyOf(roles);
        this.enabled = true;
        this.createdAt = OffsetDateTime.now();
    }

    public User(UUID id, String username, Email email, String passwordHash, Set<Role> roles,
                boolean enabled, OffsetDateTime createdAt) {
        validate(username, email, passwordHash, roles);
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = EnumSet.copyOf(roles);
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    private void validate(String username, Email email, String passwordHash, Set<Role> roles) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username é obrigatório");
        }
        if (email == null) {
            throw new IllegalArgumentException("email é obrigatório");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash é obrigatório");
        }
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("usuário precisa de ao menos um papel");
        }
    }

    /** Garante que o usuário está habilitado a autenticar; caso contrário lança {@link DisabledUserException}. */
    public void ensureEnabled() {
        if (!enabled) {
            throw new DisabledUserException(username);
        }
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Email getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Set<Role> getRoles() {
        return EnumSet.copyOf(roles);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
