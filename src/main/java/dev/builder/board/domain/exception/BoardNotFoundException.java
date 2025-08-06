package dev.builder.board.domain.exception;

import dev.builder.board.domain.model.Board;
import dev.builder.core.infrastructure.properties.MessageLocalizer;

public class BoardNotFoundException extends RuntimeException {
    public BoardNotFoundException(MessageLocalizer localizer, Board.Id board) {
        super(localizer.getMessage("board.not.found",  board.userId().value()));
    }
}
