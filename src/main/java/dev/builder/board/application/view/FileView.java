package dev.builder.board.application.view;

import dev.builder.board.domain.model.StoredFile;

import java.util.UUID;

public record FileView(UUID id, String name, StoredFile.MimeType mimeType) {
}
