package com.lmf.auth.authservice.domain.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String username) {
        super("Usuário não encontrado: " + username);
    }
}
