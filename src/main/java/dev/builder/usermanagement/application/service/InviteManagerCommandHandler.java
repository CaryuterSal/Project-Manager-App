package dev.builder.usermanagement.application.service;

import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.usermanagement.application.command.InviteManagerCommand;
import dev.builder.usermanagement.application.view.ManagerView;
import dev.builder.usermanagement.domain.port.in.UserService;

@Bean
public class InviteManagerCommandHandler implements RequestHandler<InviteManagerCommand, ManagerView> {
    private final UserService userService;
    private final InviteManagerCommand.InviteManagerCommandValidator validator;

    public InviteManagerCommandHandler(UserService userService, InviteManagerCommand.InviteManagerCommandValidator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    @Override
    public ManagerView handle(InviteManagerCommand request) throws ValidationException {
        validator.validate(request);
        return userService.registerManager(request);
    }
}
