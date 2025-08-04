package dev.builder.board.infrastructure;

import dev.builder.board.domain.model.Board;
import dev.builder.board.domain.model.BoardCollaborator;
import dev.builder.board.domain.model.Stage;
import dev.builder.board.domain.model.Title;
import dev.builder.core.infrastructure.persistence.CommonMappers;
import dev.builder.usermanagement.domain.model.Manager;
import dev.builder.usermanagement.domain.model.Student;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class BoardJdbcMapper {
    public enum BoardColumns{
        ID("mnr_email"),
        TITLE("title"),
        COLLABORATOR_ID("collaborator_id"),
        COLLABORATOR_ISSUED_AT("collaborator_issued_at");

        private final String columnName;

        public String columnName() {
            return columnName;
        }

        BoardColumns(String columnName) {
            this.columnName = columnName;
        }
    }

    public static @NotNull List<Board> rowToBoards(ResultSet rs) throws SQLException {
        List<Board> boards = new ArrayList<>();
        Map<Board.Id, Set<BoardCollaborator>> assignees = extractCollaboratorsByBoardId(rs);
        do {
            Board.Id id = extractBoardId(rs);
            boards.add(rowToBoard(
                        rs,
                        id,
                        assignees.getOrDefault(id, new HashSet<>())
            ));
        } while(!rs.next());
        return boards;
    }

    public static @NotNull Board rowToBoard(ResultSet rs) throws SQLException {
        Board.Id id = extractBoardId(rs);
        return rowToBoard(
                rs,
                id,
                extractCollaborators(rs,id)
        );
    }

    private static @NotNull Board rowToBoard(ResultSet rs, Board.Id id, Set<BoardCollaborator> assignees) throws SQLException {
        Title title = new Title(rs.getString(BoardColumns.TITLE.columnName));
        return new Board(
                id,
                title,
                Arrays.stream(Stage.StageState.values())
                        .map(state -> new Stage.Id(id, state))
                        .collect(Collectors.toSet()),
                assignees
        );
    }


    public static Set<BoardCollaborator> extractCollaborators(ResultSet resultSet, Board.Id board) throws SQLException {
        return extractCollaboratorsByBoardId(resultSet).getOrDefault(board, new HashSet<>());
    }

    public static Map<Board.Id, Set<BoardCollaborator>> extractCollaboratorsByBoardId(ResultSet rs) throws SQLException {
        return CommonMappers.groupResultSetByKey(
                rs,
                BoardJdbcMapper::extractBoardId,
                r -> {
                    Student.Id collaboratorId = new Student.Id(r.getString(BoardColumns.COLLABORATOR_ID.columnName));
                    LocalDateTime issuedAt = r.getObject(BoardColumns.COLLABORATOR_ISSUED_AT.columnName(), OffsetDateTime.class).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
                    return new BoardCollaborator(issuedAt, collaboratorId);
                }
        );
    }

    @Contract("_ -> new")
    public static Board.@NotNull Id extractBoardId(ResultSet rs) throws SQLException {
        return new Board.Id(new Manager.Id(rs.getString(BoardColumns.ID.columnName)));
    }

}
