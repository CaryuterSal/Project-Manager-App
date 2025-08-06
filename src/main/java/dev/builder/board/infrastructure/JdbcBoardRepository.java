package dev.builder.board.infrastructure;

import dev.builder.board.domain.model.Board;
import dev.builder.board.domain.model.Stage;
import dev.builder.board.domain.port.out.BoardRepository;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.executeQuery;
import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.runInTransaction;

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
                SELECT,
                PreparedStatementFiller.NO_OP,
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

    /*

    // Este metodo es para buscar un tablero por Id
    @Override
    public Optional<Board> findById(Board.Id id) {
        String query = "SELECT * FROM board WHERE mnr_email = ?";
        try (Connection conn = DefaultConnectionManager.getConnection();
        PreparedStatement ps = conn.prepareStatement(query)){

            ps.setString(1, id.uuid().toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()){
                Set<Stage .Id> stages = getStagesForBoard(id, conn);
                return Optional.of(new Board(id, stages));
            }
        } catch (SQLException e){
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<Board> findAll() {

        List<Board> boards = new ArrayList<>();
        String query = "SELECT * FROM board";

        try (Connection conn = DefaultConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Board.Id id = new Board.Id(UUID.fromString(rs.getString("mnr_email"))); // Ajusta si es necesario
                Set<Stage.Id> stages = getStagesForBoard(id, conn);
                boards.add(new Board(id, stages));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return boards;

    }

    private Set<Stage.Id> getStagesForBoard(Board.Id id, Connection conn) throws SQLException {
        Set<Stage.Id> stages = new HashSet<>();
        String query = "SELECT sae_NAME FROM board_stage WHERE bad_email = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, id.uuid().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                stages.add(new Stage.Id(rs.getString("sae_NAME")));
            }
        }
        return stages;
    }

    // Este metodo elimina un tablero usando el ojeto
    @Override
    public void delete(Board aggregateRoot) {
        deleteById(aggregateRoot.id());

    }

    // Este metodo elimina un tablero por id
    @Override
    public void deleteById(Board.Id id) {
        String deleteStages = "DELETE FROM board_stage WHERE bad_email = ?";
        String deleteBoard = "DELETE FROM board WHERE mnr_email = ?";

        try (Connection conn = DefaultConnectionManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psStages = conn.prepareStatement(deleteStages);
                 PreparedStatement psBoard = conn.prepareStatement(deleteBoard)) {

                psStages.setString(1, id.uuid().toString());
                psStages.executeUpdate();

                psBoard.setString(1, id.uuid().toString());
                psBoard.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // Este metodo crea o actualiza un tablero
    @Override
    public Board save(Board aggregateRoot) {
        if (existsById(aggregateRoot.id())) {
            return updateBoard(aggregateRoot);
        } else {
            return createBoard(aggregateRoot);
        }
    }

    //TODO: insertar columnas nuevas tambien
    private Board createBoard(Board aggregateRoot) {
        String insertBoard = "INSERT INTO board (mnr_email) VALUES (?)";
        try (Connection conn = DefaultConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertBoard)) {

            ps.setString(1, aggregateRoot.id().uuid().toString());
            ps.executeUpdate();
            return aggregateRoot;

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //TODO: Crear logica de actualizacion
    private Board updateBoard(Board aggregateRoot) {

    }

    // Este metodo verifica si existe un tablero con ese Id
    @Override
    public boolean existsById(Board.Id id) {

        String query = "SELECT 1 FROM board WHERE mnr_email = ?";
        try (Connection conn = DefaultConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, id.uuid().toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true si encontró algo
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;

    }

 */
}
