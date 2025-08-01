package dev.builder.usermanagement.application.service;

import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.usermanagement.application.query.FindManagerQuery;
import dev.builder.usermanagement.application.query.FindUserQuery;
import dev.builder.usermanagement.application.view.ManagerView;
import dev.builder.usermanagement.application.view.UserView;
import dev.builder.usermanagement.domain.port.in.UserService;

import java.util.Optional;

public class FindUserQueryHandler implements RequestHandler<FindUserQuery<UserView>, Optional<UserView>> {
    private final UserService userService;
    private final RequestValidator<FindUserQuery<UserView>> validator;

    public FindUserQueryHandler(UserService userService, RequestValidator<FindUserQuery<UserView>> validator) {
        this.userService = userService;
        this.validator = validator;
    }

    @Override
    public Optional<UserView> handle(FindUserQuery<UserView> query) throws ValidationException {
        validator.validate(query);
        return userService.getUser(query);
    }
}
