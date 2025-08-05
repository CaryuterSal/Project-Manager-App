package dev.builder.usermanagement.application.service;

import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.usermanagement.application.query.FindAllStudentsQuery;
import dev.builder.usermanagement.application.view.StudentView;
import dev.builder.usermanagement.domain.port.in.UserService;

import java.util.List;

public class FindAllStudentsQueryHandler implements RequestHandler<FindAllStudentsQuery, List<StudentView>> {
    private final UserService userService;
    private final FindAllStudentsQuery.FindAllStudentsQueryValidator validator;

    public FindAllStudentsQueryHandler(UserService userService, FindAllStudentsQuery.FindAllStudentsQueryValidator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    @Override
    public List<StudentView> handle(FindAllStudentsQuery query) throws ValidationException {
        validator.validate(query);
        return userService.getAllStudents(query);
    }
}
