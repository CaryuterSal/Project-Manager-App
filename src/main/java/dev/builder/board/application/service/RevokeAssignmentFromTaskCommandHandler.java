package dev.builder.board.application.service;

import dev.builder.board.application.command.RevokeAssignmentFromTaskCommand;
import dev.builder.board.application.command.RevokeAssignmentFromTaskCommand;
import dev.builder.board.application.view.FileView;
import dev.builder.board.domain.port.in.TaskService;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;

@Bean
public class RevokeAssignmentFromTaskCommandHandler implements RequestHandler<RevokeAssignmentFromTaskCommand, Void> {
    private final RevokeAssignmentFromTaskCommand.RevokeAssignmentFromTaskCommandValidator validator;
    private final TaskService taskService;

    public RevokeAssignmentFromTaskCommandHandler(TaskService taskService, RevokeAssignmentFromTaskCommand.RevokeAssignmentFromTaskCommandValidator requestValidator) {
        this.validator = requestValidator;
        this.taskService = taskService;
    }

    @Override
    public Void handle(RevokeAssignmentFromTaskCommand command) throws ValidationException {
        validator.validate(command);
        taskService.removeAssignedStudent(command);
        return null;
    }
}
