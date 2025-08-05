package dev.builder.board.application.service;

import dev.builder.board.application.command.AddCollaboratorCommand;
import dev.builder.board.application.view.FileView;
import dev.builder.board.application.view.TaskView;
import dev.builder.board.domain.port.in.BoardService;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;

@Bean
public class AddCollaboratorCommandHandler implements RequestHandler<AddCollaboratorCommand, Void> {

    private final RequestValidator<AddCollaboratorCommand> validator;
    private final BoardService boardService;

    public AddCollaboratorCommandHandler(BoardService boardService, RequestValidator<AddCollaboratorCommand> requestValidator) {
        this.validator = requestValidator;
        this.boardService = boardService;
    }

    @Override
    public Void handle(AddCollaboratorCommand command) throws ValidationException {
        validator.validate(command);
        boardService.addCollaborator(command);
        return null;
    }
}
