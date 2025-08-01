package dev.builder.auth.application.service;

import dev.builder.auth.application.command.LoginCommand;
import dev.builder.auth.domain.port.in.AuthenticationService;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;

@Bean
public class LoginCommandHandler implements RequestHandler<LoginCommand, Void> {

    private final RequestValidator<LoginCommand> validator;
    private final AuthenticationService authenticationService;

    @Inject
    public LoginCommandHandler(RequestValidator<LoginCommand> validator, AuthenticationService authenticationService) {
        this.validator = validator;
        this.authenticationService = authenticationService;
    }

    @Override
    public Void handle(LoginCommand request) throws ValidationException {
        validator.validate(request);
        authenticationService.login(request);
        return null;
    }
}
