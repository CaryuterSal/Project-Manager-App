package dev.builder.board.application.view;

import dev.builder.usermanagement.application.view.StudentView;

import java.time.LocalDateTime;

/**
 * Representa a un colaborador que ha sido asignado a una tarea u otro recurso, junto con la fecha y hora de su incorporación.
 *
 * @param issuedAt Fecha y hora en que se registró o añadió al colaborador.
 * @param student  Información del estudiante que colabora, representada como una vista simplificada.
 */
public record CollaboratorView(LocalDateTime issuedAt, StudentView student) {

}
