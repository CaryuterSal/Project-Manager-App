package dev.builder.usermanagement.application.service;

import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.usermanagement.application.query.FindAllAdminsQuery;
import dev.builder.usermanagement.application.query.FindManagerQuery;
import dev.builder.usermanagement.application.view.AdminView;
import dev.builder.usermanagement.application.view.ManagerView;
import dev.builder.usermanagement.domain.port.in.UserService;

import java.util.List;
import java.util.Optional;

public class FindAllAdminsQueryHandler implements RequestHandler<FindAllAdminsQuery, List<AdminView>> {
    private final UserService userService;
    private final RequestValidator<FindAllAdminsQuery> validator;

    public FindAllAdminsQueryHandler(UserService userService, RequestValidator<FindAllAdminsQuery> validator) {
        this.userService = userService;
        this.validator = validator;
    }

    @Override
    public List<AdminView> handle(FindAllAdminsQuery query) throws ValidationException {
        validator.validate(query);
        return userService.getAllAdmins(query);
    }
}
