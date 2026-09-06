package com.lmf.auth.authservice.domain.repository;

import com.lmf.auth.authservice.domain.model.user.User;

import java.util.Optional;

/**
 * Porta de persistência do agregado {@link User}. Implementada por um adapter na camada de
 * infraestrutura.
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findByUsername(String username);

    /** Busca por username OU e-mail (usado no login, que aceita qualquer um dos dois). */
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
