package dev.builder.board.domain.exception;

import dev.builder.core.infrastructure.properties.MessageLocalizer;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(MessageLocalizer localizer) {
        super(localizer.getMessage("task.not.found"));
    }
}
