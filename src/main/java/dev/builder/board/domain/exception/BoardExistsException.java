package dev.builder.board.domain.exception;

import dev.builder.board.domain.model.Board;
import dev.builder.core.infrastructure.properties.MessageLocalizer;

public class BoardExistsException extends RuntimeException {
    public BoardExistsException(MessageLocalizer localizer, Board .Id id) {
        super(localizer.getMessage("board.exists", id.userId().value()));
    }
}
