package dev.builder.board.domain.exception;

import dev.builder.core.infrastructure.properties.MessageLocalizer;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(MessageLocalizer localizer) {
        super(localizer.getMessage("file.not.found"));
    }
}
