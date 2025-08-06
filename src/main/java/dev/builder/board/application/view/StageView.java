package dev.builder.board.application.view;

import dev.builder.board.domain.model.Stage;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Representa una etapa (o columna) dentro del tablero de tareas,
 * como "TO_DO", "IN_PROGRESS", o "DONE".
 *
 * Las tareas dentro de esta etapa están ordenadas de manera lógica
 * de arriba hacia abajo, tal como se mostrarían en la interfaz de usuario.
 *
 * @param state El estado de la etapa, que indica el tipo de columna (por ejemplo, TODO).
 * @param tasks Las tareas asignadas a esta etapa, en el orden en que deben mostrarse.
 */
public record StageView(Stage.StageState state,
                        List<TaskView> tasks){
}
