package dev.builder.board.domain.exception;

import dev.builder.core.infrastructure.properties.MessageLocalizer;

public class TaskExistsException extends RuntimeException {
    public TaskExistsException(MessageLocalizer localizer) {
        super(localizer.getMessage("task.exists"));
    }
}
