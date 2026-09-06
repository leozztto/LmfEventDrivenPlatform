package com.lmf.auth.authservice.domain.exception;

/**
 * Lançada quando o login falha. A mensagem é propositalmente genérica para não revelar se o
 * problema foi usuário inexistente ou senha incorreta.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Credenciais inválidas");
    }
}
