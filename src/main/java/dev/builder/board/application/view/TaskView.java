package dev.builder.board.application.view;

import dev.builder.board.domain.model.Deadline;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record TaskView(UUID id,
                       String title,
                       String description,
                       ColorView color,
                       LocalDateTime createdAt,
                       LocalDateTime deadline,
                       Optional<LocalDateTime> startedAt,
                       Optional<LocalDateTime> finishedAt,
                       Optional<FileView> coverImage,
                       Set<FileView> attachments) {
}
