package com.lmf.auth.authservice.domain.exception;

public class DisabledUserException extends RuntimeException {

    public DisabledUserException(String username) {
        super("Usuário desabilitado: " + username);
    }
}
