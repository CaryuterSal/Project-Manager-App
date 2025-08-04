package dev.builder.board.infrastructure;

import dev.builder.board.domain.model.Board;
import dev.builder.board.domain.model.BoardCollaborator;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.persistence.RepositoryException;
import dev.builder.core.infrastructure.persistence.UUIDMapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.executeQuery;

@Bean
public class JdbcBoardCollaboratorRepository {

    private static final String INSERT = """
            INSERT INTO student_board(sdt_email, bad_email)
            VALUES (?, ?)
            """;
    private static final String DELETE = """
            DELETE FROM student_board
            WHERE sdt_email = ?
            AND bad_email = ?
    """;
    private static final String SELECT_FOR_BOARD = String.format("""
            SELECT
                sb.bad_email as %s,
                sb.sdt_email as %s,
                sb.issued_at as %s,
            FROM student s
            JOIN app_user su ON su.email = s.email AND su.active = 1
            JOIN student_board sb ON sb.sdt_email = su.email
            WHERE sb.bad_email = ?
            """, BoardJdbcMapper.BoardColumns.ID.columnName(),
            BoardJdbcMapper.BoardColumns.COLLABORATOR_ID.columnName(),
            BoardJdbcMapper.BoardColumns.COLLABORATOR_ISSUED_AT.columnName());

    private static final String SELECT_ISSUED_AT = String.format("""
            SELECT
                issued_at as %s
            FROM student_board
            WHERE sdt_email = ?
            AND bad_email = ?
            """, BoardJdbcMapper.BoardColumns.COLLABORATOR_ISSUED_AT.columnName());

    private static final Logger log = LoggerFactory.getLogger(JdbcBoardCollaboratorRepository.class);

    public Board save(@NotNull Board board, Connection connection) {
        try {
            Set<BoardCollaborator> existingCollaborators = executeQuery(
                    SELECT_FOR_BOARD,
                    ps -> ps.setString(1, board.id().userId().value()),
                    rs -> rs.next() ? BoardJdbcMapper.extractCollaborators(rs, board.id()) : new HashSet<>(),
                    log,
                    connection
            );
            Set<BoardCollaborator> missingCollaborators = new HashSet<>(board.collaborators());
            missingCollaborators.removeAll(existingCollaborators);
            for (BoardCollaborator collaborator : missingCollaborators) {
                create(board.id(), collaborator, connection);
            }

            Set<BoardCollaborator> surplusCollaborators = new HashSet<>(existingCollaborators);
            surplusCollaborators.removeAll(board.collaborators());
            for (BoardCollaborator collaborator : surplusCollaborators) {
                delete(board.id(), collaborator, connection);
            }
            return board;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    private void delete(Board.@NotNull Id boardId, @NotNull BoardCollaborator collaborator, Connection connection) throws SQLException {
        try(PreparedStatement ps = connection.prepareStatement(DELETE)){
            ps.setString(1, collaborator.id().value());
            ps.setString(2,  boardId.userId().value());
            boolean updated = ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("There was an error while deleting a collaborator");
        }
    }

    private void create(Board.@NotNull Id boardId, @NotNull BoardCollaborator boardCollaborator, Connection connection) throws SQLException{
        try(PreparedStatement ps = connection.prepareStatement(INSERT)){
            ps.setString(1, boardCollaborator.id().value());
            ps.setString(2,  boardId.userId().value());
            boolean updated = ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("There was an error while inserting new Collaborator");
        }

        try(PreparedStatement ps = connection.prepareStatement(SELECT_ISSUED_AT)){
            ps.setString(1,  boardCollaborator.id().value());
            ps.setString(2,  boardId.userId().value());
            try(ResultSet rs = ps.executeQuery()){
                rs.next();
                OffsetDateTime odt = rs.getObject(BoardJdbcMapper.BoardColumns.COLLABORATOR_ISSUED_AT.columnName(), OffsetDateTime.class);
                LocalDateTime issuedAt = odt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
                boardCollaborator.hydrateWithAuditInfo(issuedAt);
            }
        }
    }
}
