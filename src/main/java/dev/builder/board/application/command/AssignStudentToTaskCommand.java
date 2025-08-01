package dev.builder.board.application.command;

import dev.builder.core.application.Command;

import java.util.UUID;

public record AssignStudentToTaskCommand(UUID taskId, String studentEmail) implements Command<Void> {
}
