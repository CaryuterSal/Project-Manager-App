package dev.builder.board.application.view;
import dev.builder.usermanagement.application.view.StudentView;

import java.util.List;
import java.util.Set;

/**
 * Representa la vista de un tablero en el sistema, compuesto por etapas (stages) y colaboradores asignados.
 *
 * @param collaborators Conjunto de colaboradores que tienen acceso o participación en el tablero.
 * @param stages        Lista de etapas del tablero, cada una conteniendo tareas organizadas por estado.
 */
public record BoardView(Set<CollaboratorView> collaborators, List<StageView> stages) {
}
