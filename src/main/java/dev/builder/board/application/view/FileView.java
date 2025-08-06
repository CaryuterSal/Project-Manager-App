package dev.builder.board.application.view;

import dev.builder.board.domain.model.StoredFile;

import java.util.UUID;

/**
 * Representa la vista de un archivo asociado a una tarea u otro recurso del sistema.
 *
 * @param id       Identificador único del archivo.
 * @param name     Nombre original del archivo.
 * @param mimeType Tipo MIME del archivo, que indica su formato (por ejemplo, image/png, application/pdf).
 */
public record FileView(UUID id, String name, StoredFile.MimeType mimeType) {
}
