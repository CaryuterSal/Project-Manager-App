package dev.builder.board.application.query;

import dev.builder.board.application.view.BoardView;
import dev.builder.core.application.Command;

import java.util.List;

/**
 * Obtiene la información de los tableros dado el email de un estudiante
 */
public class GetBoardsByCollaboratorCommand implements Command<List<BoardView>> {
    public static GetBoardsByCollaboratorCommand me() {
        return new GetBoardsByCollaboratorCommand();
    }
}
