package dev.builder.board.domain.exception;

import dev.builder.core.infrastructure.properties.MessageLocalizer;

public class FileExistsException extends RuntimeException {
    public FileExistsException(MessageLocalizer localizer) {
        super(localizer.getMessage("file.exists"));
    }
}
