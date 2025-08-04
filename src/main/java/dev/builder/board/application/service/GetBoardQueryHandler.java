package dev.builder.board.application.service;

import dev.builder.board.application.query.GetBoardQuery;
import dev.builder.board.application.query.GetBoardQuery;
import dev.builder.board.application.view.BoardView;
import dev.builder.board.application.view.FileView;
import dev.builder.board.domain.port.in.BoardService;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;

import java.util.Optional;

@Bean
public class GetBoardQueryHandler implements RequestHandler<GetBoardQuery, Optional<BoardView>> {
    private final BoardService boardService;

    public GetBoardQueryHandler(BoardService boardService) {
        this.boardService = boardService;
    }

    @Override
    public Optional<BoardView> handle(GetBoardQuery query) throws ValidationException {
        return boardService.getBoard(query);
    }
}
