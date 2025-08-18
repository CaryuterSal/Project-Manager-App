package dev.builder.board.infrastructure;

import dev.builder.board.domain.model.Board;
import dev.builder.board.domain.model.Stage;
import dev.builder.board.domain.port.out.BoardRepository;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.persistence.*;
import dev.builder.usermanagement.domain.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.*;

@Bean
public class JdbcBoardRepository extends TransactionalJdbcCrudRepository<Board, Board.Id> implements BoardRepository {


    private static final String INSERT = """
            INSERT INTO board(title, mnr_email)
            VALUES(?,?)
            """;

    private static final String EXISTS = """
            SELECT COUNT(*) AS total
            FROM board b
            JOIN manager m ON m.email = b.mnr_email
            JOIN app_user u ON u.email = m.email
            WHERE b.mnr_email = ?
            AND u.active = 1
            """;
    private static final String SELECT = String.format("""
            SELECT
                b.mnr_email as %s,
                b.title as %s,
                active_collaborators.sdt_email as %s,
                active_collaborators.issued_at as %s
            FROM board b
            JOIN manager m ON m.email = b.mnr_email
            JOIN app_user u ON u.email = m.email AND u.active = 1
            LEFT JOIN (
                SELECT issued_at, sdt_email, bad_email
                FROM student s
                JOIN app_user su ON su.email = s.email AND su.active = 1
                JOIN student_board sb ON sb.sdt_email = su.email
            ) active_collaborators ON active_collaborators.bad_email = b.mnr_email
            WHERE b.mnr_email = ?
            """, BoardJdbcMapper.BoardColumns.ID.columnName(),
            BoardJdbcMapper.BoardColumns.TITLE.columnName(),
            BoardJdbcMapper.BoardColumns.COLLABORATOR_ID.columnName(),
            BoardJdbcMapper.BoardColumns.COLLABORATOR_ISSUED_AT.columnName());

    private static final String SELECT_ALL = String.format("""
            SELECT
                b.mnr_email as %s,
                b.title as %s,
                active_collaborators.sdt_email as %s,
                active_collaborators.issued_at as %s
            FROM board b
            JOIN manager m ON m.email = b.mnr_email
            JOIN app_user u ON u.email = m.email AND u.active = 1
            LEFT JOIN (
                SELECT issued_at, sdt_email, bad_email
                FROM student s
                JOIN app_user su ON su.email = s.email AND su.active = 1
                JOIN student_board sb ON sb.sdt_email = su.email
            ) active_collaborators ON active_collaborators.bad_email = b.mnr_email
            """, BoardJdbcMapper.BoardColumns.ID.columnName(),
            BoardJdbcMapper.BoardColumns.TITLE.columnName(),
            BoardJdbcMapper.BoardColumns.COLLABORATOR_ID.columnName(),
            BoardJdbcMapper.BoardColumns.COLLABORATOR_ISSUED_AT.columnName());

    private static final String SELECT_BY_COLLABORATOR = String.format("""
            SELECT
                b.mnr_email as %s,
                b.title as %s,
                active_collaborators.sdt_email as %s,
                active_collaborators.issued_at as %s
            FROM student s
            JOIN app_user su ON su.email = s.email AND su.active = 1
            JOIN student_board sb ON sb.sdt_email = su.email
            JOIN board b ON b.mnr_email = sb.bad_email
            JOIN manager m ON m.email = b.mnr_email
            JOIN app_user u ON u.email = m.email AND u.active = 1
            LEFT JOIN (
                SELECT issued_at, sdt_email, bad_email
                FROM student s
                JOIN app_user su ON su.email = s.email AND su.active = 1
                JOIN student_board sb ON sb.sdt_email = su.email
            ) active_collaborators ON active_collaborators.bad_email = b.mnr_email
            WHERE s.email = ?
            """, BoardJdbcMapper.BoardColumns.ID.columnName(),
            BoardJdbcMapper.BoardColumns.TITLE.columnName(),
            BoardJdbcMapper.BoardColumns.COLLABORATOR_ID.columnName(),
            BoardJdbcMapper.BoardColumns.COLLABORATOR_ISSUED_AT.columnName());

    private static final Logger log = LoggerFactory.getLogger(JdbcBoardRepository.class);
    @Override
    protected Logger getLogger() {
        return log;
    }

    private final JdbcStageRepository boardStageRepository;
    private final JdbcBoardCollaboratorRepository boardCollaboratorRepository;

    @Inject
    public JdbcBoardRepository(ConnectionManager connectionManager, JdbcStageRepository boardStageRepository, JdbcBoardCollaboratorRepository boardCollaboratorRepository) {
        super(connectionManager);
        this.boardStageRepository = boardStageRepository;
        this.boardCollaboratorRepository = boardCollaboratorRepository;
    }

    @Override
    public Board save(Board entity) {
        return runInTransaction(
                connectionManager,
                log,
                this::save,
                entity
        );
    }

    @Override
    public Board save(Board board, Connection connection) {
        if(existsById(board.id(), connection)){
            return boardCollaboratorRepository.save(board,connection);
        }
        return create(board, connection);
    }

    private Board create(Board board, Connection connection) {
        try(PreparedStatement ps = connection.prepareStatement(INSERT)){
            ps.setString(1, board.title().value());
            ps.setString(2, board.id().userId().value());
            boolean updated =  ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("Board already exists");
            for(Stage.StageState stage : Stage.StageState.values()){
                Stage.Id stageId = new Stage.Id(board.id(), stage);
                boardStageRepository.save(new Stage(stageId, new HashSet<>()), connection);
            }
            return boardCollaboratorRepository.save(board, connection);
        } catch (SQLException ex){
            log.error(ex.getMessage(), ex);
            throw new RepositoryException(ex.getMessage(), ex);
        }
    }

    @Override
    public boolean deleteById(Board.Id id, Connection connection) {
        throw new UnsupportedOperationException("No se permite eliminar los tableros");
    }

    @Override
    public List<Board> findAll(Connection connection) {
        return executeQuery(
                SELECT_ALL,
                PreparedStatementFiller.NO_OP,
                rs -> rs.next() ? BoardJdbcMapper.rowToBoards(rs) : new ArrayList<>(),
                log,
                connection
        );
    }

    @Override
    public List<Board> findAllByCollaborator(Student.Id studentId) {
        return wrapWithConnection(
                connectionManager,
                log,
                this::findAllByCollaborator,
                studentId
        );
    }

    @Override
    public List<Board> findAllByCollaborator(Student.Id studentId, Connection connection) {
        return executeQuery(
                SELECT_BY_COLLABORATOR,
                ps -> ps.setString(1, studentId.value()),
                rs -> rs.next() ? BoardJdbcMapper.rowToBoards(rs) : new ArrayList<>(),
                log,
                connection
        );
    }

    @Override
    public Optional<Board> findById(Board.Id id, Connection connection) {
        return executeQuery(
                SELECT,
                ps -> ps.setString(1, id.userId().value()),
                rs -> rs.next() ? Optional.of(BoardJdbcMapper.rowToBoard(rs)) : Optional.empty(),
                log,
                connection
        );
    }

    @Override
    public boolean existsById(Board.Id id, Connection connection) {
        return executeQuery(
                EXISTS,
                ps -> ps.setString(1, id.userId().value()),
                rs -> rs.next() && rs.getInt("total") > 0,
                log,
                connection
        );
    }

}
