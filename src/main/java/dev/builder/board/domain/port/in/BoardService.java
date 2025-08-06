package dev.builder.board.domain.port.in;

import dev.builder.board.application.command.AddCollaboratorCommand;
import dev.builder.board.application.command.CreateTaskCommand;
import dev.builder.board.application.command.DeleteTaskCommand;
import dev.builder.board.application.command.MoveTaskCommand;
import dev.builder.board.application.query.GetBoardQuery;
import dev.builder.board.application.view.BoardView;
import dev.builder.board.application.view.StageView;
import dev.builder.board.domain.model.Board;
import dev.builder.core.domain.ApplicationService;
import dev.builder.usermanagement.domain.model.Student;

import java.sql.Connection;
import java.util.Optional;

public interface BoardService extends ApplicationService {
    StageView createTask(CreateTaskCommand command);
    StageView deleteTask(DeleteTaskCommand command);
    void addCollaborator(AddCollaboratorCommand command);
    BoardView moveTask(MoveTaskCommand command);
    Optional<BoardView> getBoard(GetBoardQuery command);
    void createOwnBoard(Connection connection);
    void assignToInvitedBoard(Connection connection);
}
