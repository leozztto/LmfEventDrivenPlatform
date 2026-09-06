package com.lmf.auth.authservice.application.usecase.command;

public record RegisterUserCommand(String username, String email, String password) {
}
