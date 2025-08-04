package dev.builder.board.application.view;

import dev.builder.board.domain.model.Color;
import dev.builder.board.domain.model.Deadline;
import dev.builder.usermanagement.application.view.StudentView;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record TaskView(UUID id,
                       String title,
                       String description,
                       Color color,
                       LocalDateTime createdAt,
                       LocalDateTime deadline,
                       Set<StudentView> assignees,
                       Optional<LocalDateTime> startedAt,
                       Optional<LocalDateTime> finishedAt,
                       Optional<FileView> coverImage,
                       Set<FileView> attachments) {
}
