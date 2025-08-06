package dev.builder.board.application.service;

import dev.builder.board.application.command.CreateTaskCommand;
import dev.builder.board.application.command.CreateTaskCommand;
import dev.builder.board.application.view.FileView;
import dev.builder.board.application.view.StageView;
import dev.builder.board.domain.port.in.BoardService;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;

@Bean
public class CreateTaskCommandHandler implements RequestHandler<CreateTaskCommand, StageView> {
    private final CreateTaskCommand.CreateTaskCommandValidator validator;
    private final BoardService boardService;

    public CreateTaskCommandHandler(BoardService boardService, CreateTaskCommand.CreateTaskCommandValidator requestValidator) {
        this.validator = requestValidator;
        this.boardService = boardService;
    }

    @Override
    public StageView handle(CreateTaskCommand command) throws ValidationException {
        validator.validate(command);
        return boardService.createTask(command);
    }
}
