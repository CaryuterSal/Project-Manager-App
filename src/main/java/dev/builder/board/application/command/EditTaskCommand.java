package dev.builder.board.application.command;

import dev.builder.board.application.view.TaskView;
import dev.builder.core.application.Command;

import java.time.LocalDateTime;
import java.util.UUID;

public record EditTaskCommand(UUID id, String title, String description, String colorName, LocalDateTime deadline) implements Command<TaskView> {
}
