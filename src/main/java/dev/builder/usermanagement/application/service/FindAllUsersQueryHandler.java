package dev.builder.usermanagement.application.service;

import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.query.FindAllUsersQuery;
import dev.builder.usermanagement.application.view.ManagerView;
import dev.builder.usermanagement.application.view.UserView;
import dev.builder.usermanagement.domain.port.in.UserService;

import java.util.List;
import java.util.Optional;

@Bean
public class FindAllUsersQueryHandler implements RequestHandler<FindAllUsersQuery.FindAllUsersGenericQuery, List<UserView>> {
    private final UserService userService;
    private final FindAllUsersQuery.FindAllUsersQueryValidator validator;

    @Inject
    public FindAllUsersQueryHandler(UserService userService, FindAllUsersQuery.FindAllUsersQueryValidator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    @Override
    public List<UserView> handle(FindAllUsersQuery.FindAllUsersGenericQuery query) throws ValidationException {
        validator.validate(query);
        return userService.getAllUsers(query);
    }
}
