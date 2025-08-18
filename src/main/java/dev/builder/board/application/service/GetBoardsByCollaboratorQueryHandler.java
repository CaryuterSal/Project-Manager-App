package dev.builder.board.application.service;

import dev.builder.board.application.query.GetBoardQuery;
import dev.builder.board.application.query.GetBoardsByCollaboratorCommand;
import dev.builder.board.application.view.BoardView;
import dev.builder.board.domain.port.in.BoardService;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;

import java.util.List;
import java.util.Optional;

@Bean
public class GetBoardsByCollaboratorQueryHandler implements RequestHandler<GetBoardsByCollaboratorCommand, List<BoardView>> {
    private final BoardService boardService;

    public GetBoardsByCollaboratorQueryHandler(BoardService boardService) {
        this.boardService = boardService;
    }

    @Override
    public List<BoardView> handle(GetBoardsByCollaboratorCommand request) throws ValidationException {
        return boardService.findByCollaborator(request);
    }
}
