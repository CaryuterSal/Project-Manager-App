package dev.builder.usermanagement.application.service;

import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.usermanagement.application.query.FindAllManagersQuery;
import dev.builder.usermanagement.application.view.ManagerView;
import dev.builder.usermanagement.domain.port.in.UserService;

import java.util.List;

public class FindAllManagersQueryHandler implements RequestHandler<FindAllManagersQuery, List<ManagerView>> {
    private final UserService userService;
    private final FindAllManagersQuery.FindAllManagersQueryValidator validator;

    public FindAllManagersQueryHandler(UserService userService, FindAllManagersQuery.FindAllManagersQueryValidator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    @Override
    public List<ManagerView> handle(FindAllManagersQuery query) throws ValidationException {
        validator.validate(query);
        return userService.getAllManagers(query);
    }
}
