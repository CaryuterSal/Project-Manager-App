package dev.builder.board.application.view;

import dev.builder.board.domain.model.Stage;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public record StageView(Stage.StageState state,
                        List<TaskView> tasks) {
}
