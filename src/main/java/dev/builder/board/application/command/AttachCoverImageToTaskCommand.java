package dev.builder.board.application.command;

import dev.builder.board.application.view.FileView;
import dev.builder.core.application.Command;

import java.io.InputStream;
import java.util.UUID;

public record AttachCoverImageToTaskCommand(UUID taskId, String filename, InputStream imageStream) implements Command<FileView> {
}
