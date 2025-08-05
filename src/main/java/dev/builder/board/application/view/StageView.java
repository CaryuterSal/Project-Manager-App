package dev.builder.board.application.view;

import dev.builder.board.domain.model.Stage;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Vista que ordena por default las tareas según su órden dentro del Stage
 * @param state
 * @param tasks
 */
public record StageView(Stage.StageState state,
                        List<TaskView> tasks){
}
