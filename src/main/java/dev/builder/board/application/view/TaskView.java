package dev.builder.board.application.view;

import dev.builder.board.domain.model.Color;
import dev.builder.board.domain.model.Deadline;
import dev.builder.usermanagement.application.view.StudentView;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Representación inmutable de una tarea dentro del sistema.
 *
 * Este record actúa como una vista de lectura (DTO) que expone los datos relevantes
 * de una tarea, incluyendo su información básica, responsables, fechas clave
 * y archivos relacionados.
 *
 * @param id           Identificador único de la tarea.
 * @param title        Título breve y descriptivo de la tarea.
 * @param description  Descripción detallada del objetivo o instrucciones de la tarea.
 * @param color        Color asociado a la tarea (útil para interfaz visual o categorización).
 * @param createdAt    Fecha y hora en la que fue creada la tarea.
 * @param deadline     Fecha y hora límite para completar la tarea.
 * @param assignees    Conjunto de estudiantes asignados a la tarea.
 * @param startedAt    Fecha y hora opcional en la que se comenzó la tarea.
 * @param finishedAt   Fecha y hora opcional en la que se finalizó la tarea.
 * @param coverImage   Imagen de portada opcional asociada a la tarea.
 * @param attachments  Conjunto de archivos adjuntos relacionados con la tarea.
 */
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
