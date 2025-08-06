package dev.builder.board.application.view;
import dev.builder.usermanagement.application.view.StudentView;

import java.util.List;
import java.util.Set;

public record BoardView(Set<CollaboratorView> collaborators, List<StageView> stages) {
}
