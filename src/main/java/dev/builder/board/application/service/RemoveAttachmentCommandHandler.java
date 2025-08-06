package dev.builder.board.application.service;

import dev.builder.board.application.command.RemoveAttachmentCommand;
import dev.builder.board.application.command.RemoveAttachmentCommand;
import dev.builder.board.application.view.BoardView;
import dev.builder.board.application.view.FileView;
import dev.builder.board.domain.port.in.TaskService;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;

@Bean
public class RemoveAttachmentCommandHandler implements RequestHandler<RemoveAttachmentCommand, Void> {
    private final RemoveAttachmentCommand.RemoveAttachmentCommandValidator validator;
    private final TaskService taskService;

    public RemoveAttachmentCommandHandler(TaskService taskService, RemoveAttachmentCommand.RemoveAttachmentCommandValidator requestValidator) {
        this.validator = requestValidator;
        this.taskService = taskService;
    }

    @Override
    public Void handle(RemoveAttachmentCommand command) throws ValidationException {
        validator.validate(command);
        taskService.removeAttachment(command);
        return null;
    }
}
