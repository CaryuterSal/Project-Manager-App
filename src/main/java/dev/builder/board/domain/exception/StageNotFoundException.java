package dev.builder.board.domain.exception;

import dev.builder.board.domain.model.Stage;
import dev.builder.core.infrastructure.properties.MessageLocalizer;

public class StageNotFoundException extends RuntimeException {
    public StageNotFoundException(MessageLocalizer localizer, Stage.Id stage) {
        super(localizer.getMessage("board.stage.not.found", stage.state().toString(), stage.boardId().userId().value()));
    }
}
