package com.lmf.auth.authservice.application.usecase.command;

public record AuthenticateUserCommand(String usernameOrEmail, String password) {
}
