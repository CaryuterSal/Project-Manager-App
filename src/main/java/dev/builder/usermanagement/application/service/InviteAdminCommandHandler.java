package dev.builder.usermanagement.application.service;

import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.usermanagement.application.command.InviteAdminCommand;
import dev.builder.usermanagement.application.view.AdminView;
import dev.builder.usermanagement.domain.port.in.UserService;

public class InviteAdminCommandHandler implements RequestHandler<InviteAdminCommand, AdminView> {

    private final UserService userService;
    private final InviteAdminCommand.InviteAdminCommandValidator validator;

    public InviteAdminCommandHandler(UserService userService, InviteAdminCommand.InviteAdminCommandValidator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    @Override
    public AdminView handle(InviteAdminCommand request) throws ValidationException {
        validator.validate(request);
        return userService.registerAdmin(request);
    }
}
