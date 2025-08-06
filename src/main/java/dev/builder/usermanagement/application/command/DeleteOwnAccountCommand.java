package dev.builder.usermanagement.application.command;

import dev.builder.core.application.Command;

/**
 * Elimina tu propia cuenta
 */
public record DeleteOwnAccountCommand() implements Command<Void> {
}
