package dev.builder.usermanagement.application.service;

import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.usermanagement.application.query.FindAdminQuery;
import dev.builder.usermanagement.application.query.FindManagerQuery;
import dev.builder.usermanagement.application.view.AdminView;
import dev.builder.usermanagement.application.view.ManagerView;
import dev.builder.usermanagement.domain.port.in.UserService;

import java.util.Optional;

public class FindManagerQueryHandler implements RequestHandler<FindManagerQuery, Optional<ManagerView>> {
    private final UserService userService;
    private final RequestValidator<FindManagerQuery> validator;

    public FindManagerQueryHandler(UserService userService, RequestValidator<FindManagerQuery> validator) {
        this.userService = userService;
        this.validator = validator;
    }

    @Override
    public Optional<ManagerView> handle(FindManagerQuery query) throws ValidationException {
        validator.validate(query);
        return userService.getManager(query);
    }
}
