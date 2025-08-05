package dev.builder.usermanagement.application.service;

import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.usermanagement.application.query.FindAllUsersQuery;
import dev.builder.usermanagement.application.view.ManagerView;
import dev.builder.usermanagement.application.view.UserView;
import dev.builder.usermanagement.domain.port.in.UserService;

import java.util.List;
import java.util.Optional;

public class FindAllUsersQueryHandler implements RequestHandler<FindAllUsersQuery<UserView, FindAllUsersQuery.UserSortableField>, List<UserView>> {
    private final UserService userService;
    private final RequestValidator<FindAllUsersQuery<UserView, FindAllUsersQuery.UserSortableField>> validator;

    public FindAllUsersQueryHandler(UserService userService, RequestValidator<FindAllUsersQuery<UserView, FindAllUsersQuery.UserSortableField>> validator) {
        this.userService = userService;
        this.validator = validator;
    }

    @Override
    public List<UserView> handle(FindAllUsersQuery<UserView, FindAllUsersQuery.UserSortableField> query) throws ValidationException {
        validator.validate(query);
        return userService.getAllUsers(query);
    }
}
