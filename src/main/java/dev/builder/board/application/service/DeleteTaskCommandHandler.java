package dev.builder.board.application.service;

import dev.builder.board.application.command.DeleteTaskCommand;
import dev.builder.board.application.command.DeleteTaskCommand;
import dev.builder.board.application.view.FileView;
import dev.builder.board.application.view.StageView;
import dev.builder.board.domain.port.in.BoardService;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;

@Bean
public class DeleteTaskCommandHandler implements RequestHandler<DeleteTaskCommand, StageView> {
    private final RequestValidator<DeleteTaskCommand> validator;
    private final BoardService boardService;

    public DeleteTaskCommandHandler(BoardService boardService, RequestValidator<DeleteTaskCommand> requestValidator) {
        this.validator = requestValidator;
        this.boardService = boardService;
    }

    @Override
    public StageView handle(DeleteTaskCommand command) throws ValidationException {
        validator.validate(command);
        return boardService.deleteTask(command);
    }
}
