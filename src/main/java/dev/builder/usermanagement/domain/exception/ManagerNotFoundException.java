package dev.builder.usermanagement.domain.exception;

import dev.builder.core.infrastructure.properties.MessageLocalizer;
import dev.builder.usermanagement.domain.model.Manager;

public class ManagerNotFoundException extends RuntimeException {
    public ManagerNotFoundException(MessageLocalizer localizer, Manager .Id managerId) {
        super(localizer.getMessage("user.manager.not.found", managerId.value()));
    }
}
