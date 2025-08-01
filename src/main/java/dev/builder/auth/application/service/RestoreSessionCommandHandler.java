package dev.builder.auth.application.service;

import dev.builder.auth.application.command.LoginCommand;
import dev.builder.auth.application.command.RestoreSessionCommand;
import dev.builder.auth.domain.port.in.AuthenticationService;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;

@Bean
public class RestoreSessionCommandHandler implements RequestHandler<RestoreSessionCommand, Boolean> {

    private final AuthenticationService authService;

    @Inject
    public RestoreSessionCommandHandler(AuthenticationService authService) {
        this.authService = authService;
    }

    @Override
    public Boolean handle(RestoreSessionCommand request) throws ValidationException {
        return authService.restoreSession();
    }
}
