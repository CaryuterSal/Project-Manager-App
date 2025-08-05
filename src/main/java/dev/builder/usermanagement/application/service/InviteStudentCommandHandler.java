package dev.builder.usermanagement.application.service;

import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.usermanagement.application.command.InviteManagerCommand;
import dev.builder.usermanagement.application.command.InviteStudentCommand;
import dev.builder.usermanagement.application.view.ManagerView;
import dev.builder.usermanagement.application.view.StudentView;
import dev.builder.usermanagement.domain.port.in.UserService;

public class InviteStudentCommandHandler implements RequestHandler<InviteStudentCommand, StudentView> {
    private final UserService userService;
    private final InviteStudentCommand.InviteStudentCommandValidator validator;

    public InviteStudentCommandHandler(UserService userService, InviteStudentCommand.InviteStudentCommandValidator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    @Override
    public StudentView handle(InviteStudentCommand request) throws ValidationException {
        validator.validate(request);
        return userService.registerStudent(request);
    }
}
