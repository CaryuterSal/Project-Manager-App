package dev.builder.usermanagement.application.service;

import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.usermanagement.application.query.FindManagerQuery;
import dev.builder.usermanagement.application.query.FindStudentQuery;
import dev.builder.usermanagement.application.view.ManagerView;
import dev.builder.usermanagement.application.view.StudentView;
import dev.builder.usermanagement.domain.port.in.UserService;

import java.util.Optional;

public class FindStudentQueryHandler implements RequestHandler<FindStudentQuery, Optional<StudentView>> {
    private final UserService userService;
    private final FindStudentQuery.FindStudentQueryValidator validator;

    public FindStudentQueryHandler(UserService userService, FindStudentQuery.FindStudentQueryValidator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    @Override
    public Optional<StudentView> handle(FindStudentQuery query) throws ValidationException {
        validator.validate(query);
        return userService.getStudent(query);
    }
}
