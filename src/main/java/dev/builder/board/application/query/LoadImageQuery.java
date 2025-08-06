package dev.builder.board.application.query;

import dev.builder.core.application.Command;

import java.io.InputStream;
import java.util.UUID;

public record LoadImageQuery(UUID imageId) implements Command<InputStream> {
}
