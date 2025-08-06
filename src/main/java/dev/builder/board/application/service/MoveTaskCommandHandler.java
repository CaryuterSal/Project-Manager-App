package dev.builder.board.application.service;

import dev.builder.board.application.command.MoveTaskCommand;
import dev.builder.board.application.command.MoveTaskCommand;
import dev.builder.board.application.view.BoardView;
import dev.builder.board.application.view.FileView;
import dev.builder.board.domain.port.in.BoardService;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;

@Bean
public class MoveTaskCommandHandler implements RequestHandler<MoveTaskCommand, BoardView> {
    private final MoveTaskCommand.MoveTaskCommandValidator validator;
    private final BoardService boardService;

    public MoveTaskCommandHandler(BoardService boardService, MoveTaskCommand.MoveTaskCommandValidator requestValidator) {
        this.validator = requestValidator;
        this.boardService = boardService;
    }

    @Override
    public BoardView handle(MoveTaskCommand command) throws ValidationException {
        validator.validate(command);
        return boardService.moveTask(command);
    }
}
