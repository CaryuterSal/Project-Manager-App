package dev.builder.board.application.command;

import dev.builder.core.application.Command;

public record AddCollaboratorCommand(String email) implements Command<Void> {
}
