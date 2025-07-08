package dev.builder.board.application.service;

import dev.builder.board.domain.port.in.BoardService;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;

@Bean
public class BoardApplicationService implements BoardService {

    private final BoardService boardService;

    @Inject
    public BoardApplicationService(BoardService boardService) {
        this.boardService = boardService;
    }
}
