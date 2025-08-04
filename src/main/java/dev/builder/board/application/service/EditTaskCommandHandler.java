package dev.builder.board.application.service;

import dev.builder.board.application.command.EditTaskCommand;
import dev.builder.board.application.command.EditTaskCommand;
import dev.builder.board.application.view.FileView;
import dev.builder.board.application.view.TaskView;
import dev.builder.board.domain.port.in.TaskService;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;

@Bean
public class EditTaskCommandHandler implements RequestHandler<EditTaskCommand, TaskView> {
    private final RequestValidator<EditTaskCommand> validator;
    private final TaskService taskService;

    public EditTaskCommandHandler(TaskService taskService, RequestValidator<EditTaskCommand> requestValidator) {
        this.validator = requestValidator;
        this.taskService = taskService;
    }

    @Override
    public TaskView handle(EditTaskCommand command) throws ValidationException {
        validator.validate(command);
        return taskService.editTask(command);
    }
}
