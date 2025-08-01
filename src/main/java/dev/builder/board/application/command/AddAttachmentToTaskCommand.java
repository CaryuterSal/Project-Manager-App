package dev.builder.board.application.command;

import dev.builder.board.application.view.FileView;
import dev.builder.board.application.view.TaskView;
import dev.builder.core.application.Command;

import java.io.InputStream;
import java.util.UUID;

public record AddAttachmentToTaskCommand(UUID taskId, String filename, InputStream fileStream) implements Command<FileView> {
}
