package dev.builder.usermanagement.application.service;

import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import dev.builder.usermanagement.application.command.DeleteUserCommand;
import dev.builder.usermanagement.domain.port.in.UserService;

@Bean
public class DeleteUserCommandHandler implements RequestHandler<DeleteUserCommand, Void> {

    private final UserService userService;
    private final DeleteUserCommand.DeleteUserCommandValidator validator;

    @Inject
    public DeleteUserCommandHandler(UserService userService, DeleteUserCommand.DeleteUserCommandValidator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    @Override
    public Void handle(DeleteUserCommand request) throws ValidationException {
        validator.validate(request);
        userService.deleteUser(request);
        return null;
    }
}
