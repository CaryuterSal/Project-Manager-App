package dev.builder.board.domain.exception;

import dev.builder.core.infrastructure.properties.MessageLocalizer;

public class FileUnsupportedException extends RuntimeException {
    public FileUnsupportedException(MessageLocalizer messageLocalizer) {
        super(messageLocalizer.getMessage("file.unsupported"));
    }
}
