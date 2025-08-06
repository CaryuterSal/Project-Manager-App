package dev.builder.usermanagement.application.service;

import dev.builder.auth.application.service.UnauthorizedException;
import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import dev.builder.usermanagement.application.command.DeleteOwnAccountCommand;
import dev.builder.usermanagement.application.command.DeleteUserCommand;
import dev.builder.usermanagement.domain.port.in.UserService;

@Bean
public class DeleteOwnAccountCommandHandler implements RequestHandler<DeleteOwnAccountCommand, Void> {

    private final UserService userService;
    private final SessionContext sessionContext;
    private final MessageLocalizer messageLocalizer;

    @Inject
    public DeleteOwnAccountCommandHandler(UserService userService, SessionContext sessionContext, MessageLocalizer messageLocalizer) {
        this.userService = userService;
        this.sessionContext = sessionContext;
        this.messageLocalizer = messageLocalizer;
    }

    @Override
    public Void handle(DeleteOwnAccountCommand request) throws ValidationException {
        if(sessionContext.getCurrentUser() == null) throw new UnauthorizedException(messageLocalizer.getMessage("auth.session.invalid"));
        userService.deleteUser(new DeleteUserCommand(sessionContext.getCurrentUser()));
        return null;
    }
}
