package dev.builder.board.application.service;

import dev.builder.board.application.command.RemoveCoverImageCommand;
import dev.builder.board.application.command.RemoveCoverImageCommand;
import dev.builder.board.application.view.FileView;
import dev.builder.board.domain.port.in.TaskService;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;

@Bean
public class RemoveCoverImageCommandHandler implements RequestHandler<RemoveCoverImageCommand, Void> {
    private final RequestValidator<RemoveCoverImageCommand> validator;
    private final TaskService taskService;

    public RemoveCoverImageCommandHandler(TaskService taskService, RequestValidator<RemoveCoverImageCommand> requestValidator) {
        this.validator = requestValidator;
        this.taskService = taskService;
    }

    @Override
    public Void handle(RemoveCoverImageCommand command) throws ValidationException {
        validator.validate(command);
        taskService.deleteCoverImage(command);
        return null;
    }
}
