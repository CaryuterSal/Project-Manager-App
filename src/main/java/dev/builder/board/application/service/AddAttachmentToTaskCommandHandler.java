package dev.builder.board.application.service;

import dev.builder.board.application.command.AddAttachmentToTaskCommand;
import dev.builder.board.application.view.FileView;
import dev.builder.board.application.view.TaskView;
import dev.builder.board.domain.port.in.TaskService;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;

@Bean
public class AddAttachmentToTaskCommandHandler implements RequestHandler<AddAttachmentToTaskCommand, FileView> {

    private final RequestValidator<AddAttachmentToTaskCommand> validator;
    private final TaskService taskService;

    public AddAttachmentToTaskCommandHandler(TaskService taskService, RequestValidator<AddAttachmentToTaskCommand> requestValidator) {
        this.validator = requestValidator;
        this.taskService = taskService;
    }

    @Override
    public FileView handle(AddAttachmentToTaskCommand command) throws ValidationException {
        validator.validate(command);
        return taskService.addAttachment(command);
    }
}
