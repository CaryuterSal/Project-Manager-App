package dev.builder.board.application.service;

import dev.builder.board.application.command.AttachCoverImageToTaskCommand;
import dev.builder.board.application.command.AttachCoverImageToTaskCommand;
import dev.builder.board.application.view.FileView;
import dev.builder.board.domain.port.in.TaskService;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;

@Bean
public class AttachCoverImageToTaskCommandHandler implements RequestHandler<AttachCoverImageToTaskCommand, FileView> {
    private final RequestValidator<AttachCoverImageToTaskCommand> validator;
    private final TaskService taskService;

    public AttachCoverImageToTaskCommandHandler(TaskService taskService, RequestValidator<AttachCoverImageToTaskCommand> requestValidator) {
        this.validator = requestValidator;
        this.taskService = taskService;
    }

    @Override
    public FileView handle(AttachCoverImageToTaskCommand command) throws ValidationException {
        validator.validate(command);
        return taskService.attachCoverImage(command);
    }
}
