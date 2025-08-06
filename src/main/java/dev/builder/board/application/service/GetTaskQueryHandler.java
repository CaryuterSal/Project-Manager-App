package dev.builder.board.application.service;

import dev.builder.board.application.query.GetTaskQuery;
import dev.builder.board.application.query.GetTaskQuery;
import dev.builder.board.application.view.BoardView;
import dev.builder.board.application.view.TaskView;
import dev.builder.board.domain.port.in.TaskService;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;

import java.util.Optional;

@Bean
public class GetTaskQueryHandler implements RequestHandler<GetTaskQuery, Optional<TaskView>> {
    private final GetTaskQuery.GetTaskQueryValidator validator;
    private final TaskService taskService;

    public GetTaskQueryHandler(TaskService taskService, GetTaskQuery.GetTaskQueryValidator requestValidator) {
        this.validator = requestValidator;
        this.taskService = taskService;
    }

    @Override
    public Optional<TaskView> handle(GetTaskQuery query) throws ValidationException {
        validator.validate(query);
        return taskService.getTask(query);
    }
}
