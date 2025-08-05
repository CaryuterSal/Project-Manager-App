package dev.builder.auth.application.service;

import dev.builder.auth.application.command.LogoutCommand;
import dev.builder.auth.domain.port.in.AuthenticationService;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;

@Bean
public class LogoutCommandHandler implements RequestHandler<LogoutCommand, Void> {

    private final AuthenticationService authService;

    @Inject
    public LogoutCommandHandler(AuthenticationService authService) {
        this.authService = authService;
    }

    @Override
    public Void handle(LogoutCommand request) throws ValidationException {
        authService.logout();
        return null;
    }
}
