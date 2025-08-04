package dev.builder.board.application.service;

import dev.builder.board.application.command.AssignStudentToTaskCommand;
import dev.builder.board.application.command.AssignStudentToTaskCommand;
import dev.builder.board.application.view.FileView;
import dev.builder.board.domain.port.in.TaskService;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;

@Bean
public class AssignStudentToTaskCommandHandler implements RequestHandler<AssignStudentToTaskCommand, Void> {
    private final RequestValidator<AssignStudentToTaskCommand> validator;
    private final TaskService taskService;

    public AssignStudentToTaskCommandHandler(TaskService taskService, RequestValidator<AssignStudentToTaskCommand> requestValidator) {
        this.validator = requestValidator;
        this.taskService = taskService;
    }

    @Override
    public Void handle(AssignStudentToTaskCommand command) throws ValidationException {
        validator.validate(command);
        taskService.assignStudent(command);
        return null;
    }
}
