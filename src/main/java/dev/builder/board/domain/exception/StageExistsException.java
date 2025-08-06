package dev.builder.board.domain.exception;

import dev.builder.board.domain.model.Stage;
import dev.builder.core.infrastructure.properties.MessageLocalizer;

public class StageExistsException extends RuntimeException {
    public StageExistsException(MessageLocalizer localizer, Stage.Id stage) {
        super(localizer.getMessage("board.stage.exists", stage.state().toString(), stage.boardId().userId().value()));
    }
}
