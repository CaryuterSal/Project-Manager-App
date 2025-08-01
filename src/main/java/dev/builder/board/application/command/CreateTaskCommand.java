package dev.builder.board.application.command;

import dev.builder.board.application.view.TaskView;
import dev.builder.board.domain.model.Stage;
import dev.builder.core.application.Command;

import java.time.LocalDateTime;

public record CreateTaskCommand(Stage.StageState stage, String title, String description, String colorName, LocalDateTime deadline) implements Command<TaskView> {
}
