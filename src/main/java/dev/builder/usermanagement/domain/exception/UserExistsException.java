package dev.builder.usermanagement.domain.exception;

import dev.builder.core.infrastructure.properties.MessageLocalizer;

public class UserExistsException extends RuntimeException {
    public UserExistsException(MessageLocalizer messageLocalizer) {
        super(messageLocalizer.getMessage("user.exists"));
    }
}
