package com.lmf.auth.authservice.domain.service;

/**
 * Porta de hashing de senha. Mantém o domínio livre de dependência do Spring Security; a
 * implementação (BCrypt) fica na infraestrutura.
 */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
