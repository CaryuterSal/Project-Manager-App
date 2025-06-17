package dev.builder.auth.domain.port.in;

import dev.builder.auth.application.command.LoginCommand;

public interface AuthenticationService {
    void login(LoginCommand loginCommand);
}
