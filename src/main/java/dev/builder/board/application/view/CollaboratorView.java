package dev.builder.board.application.view;

import dev.builder.usermanagement.application.view.StudentView;

import java.time.LocalDateTime;

public record CollaboratorView(LocalDateTime issuedAt, StudentView student) {

}
