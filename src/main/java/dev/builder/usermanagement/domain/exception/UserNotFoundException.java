package dev.builder.usermanagement.domain.exception;

import dev.builder.core.infrastructure.properties.MessageLocalizer;
import dev.builder.usermanagement.domain.model.User;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(MessageLocalizer localizer, User.Id<?> userId) {
        super(localizer.getMessage("user.not.found", userId.value()));
    }
}
