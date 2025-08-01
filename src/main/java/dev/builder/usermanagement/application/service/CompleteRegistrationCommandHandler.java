package dev.builder.usermanagement.application.service;

import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.command.CompleteRegistrationCommand;
import dev.builder.usermanagement.application.command.InviteUserCommand;
import dev.builder.usermanagement.application.view.UserView;
import dev.builder.usermanagement.domain.port.in.UserService;

@Bean
public class CompleteRegistrationCommandHandler implements RequestHandler<CompleteRegistrationCommand, UserView> {

    private final UserService userService;
    private final RequestValidator<CompleteRegistrationCommand> requestValidator;

    @Inject
    public CompleteRegistrationCommandHandler(UserService userService, RequestValidator<CompleteRegistrationCommand> requestValidator) {
        this.userService = userService;
        this.requestValidator = requestValidator;
    }

    @Override
    public UserView handle(CompleteRegistrationCommand request) throws ValidationException {
        requestValidator.validate(request);
        return  userService.completeRegistration(request);
    }
}
