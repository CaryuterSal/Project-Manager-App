package dev.builder.usermanagement.application.service;

import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.usermanagement.application.query.FindAdminQuery;
import dev.builder.usermanagement.application.view.AdminView;
import dev.builder.usermanagement.domain.port.in.UserService;

import java.util.Optional;

public class FindAdminQueryHandler implements RequestHandler<FindAdminQuery, Optional<AdminView>> {
    private final UserService userService;
    private final FindAdminQuery.FindAdminQueryValidator validator;

    public FindAdminQueryHandler(UserService userService, FindAdminQuery.FindAdminQueryValidator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    @Override
    public Optional<AdminView> handle(FindAdminQuery query) throws ValidationException {
        validator.validate(query);
        return userService.getAdmin(query);
    }
}
