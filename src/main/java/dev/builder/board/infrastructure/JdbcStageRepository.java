package dev.builder.board.infrastructure;

import dev.builder.board.domain.model.Board;
import dev.builder.board.domain.model.Stage;
import dev.builder.board.domain.model.Task;
import dev.builder.board.domain.port.out.StageRepository;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.persistence.ConnectionManager;
import dev.builder.core.infrastructure.persistence.RepositoryException;
import dev.builder.core.infrastructure.persistence.TransactionalJdbcCrudRepository;
import dev.builder.core.infrastructure.persistence.UUIDMapper;
import dev.builder.usermanagement.domain.model.Manager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.*;

@Bean
public class JdbcStageRepository extends TransactionalJdbcCrudRepository<Stage, Stage.Id> implements StageRepository {


    private static final String INSERT = """
            INSERT INTO board_stage(bad_email, sae_name)
            VALUES (?, ?)
            """;

    private static final String EXISTS = """
            SELECT COUNT(*) AS total
            FROM board_stage bs
            JOIN manager m ON m.email = bs.bad_email
            JOIN app_user u ON u.email = m.email AND u.active = 1
            WHERE bs.bad_email = ?
            AND bs.sae_name = ?
            """;

    private static final String SELECT_BY_TASK_AND_BOARD = String.format("""
            SELECT
                stk.bse_sae_name as %s
            FROM task t
            JOIN stage_task stk ON stk.tsk_id = t.id AND stk.bse_bad_email = ?
            JOIN manager m ON m.email = stk.bse_bad_email
            JOIN app_user u ON u.email = m.email AND u.active = 1
            WHERE t.id = ?
            AND t.active = 1
            """, StageJdbcMapper.StageColumns.STAGE.columnName());


    private static final String SELECT_BY_TASK = String.format("""
            SELECT
                stk.bse_sae_name as %s,
                stk.bse_bad_email as %s
            FROM task t
            JOIN stage_task stk ON stk.tsk_id = t.id
            JOIN manager m ON m.email = stk.bse_bad_email
            JOIN app_user u ON u.email = m.email AND u.active = 1
            WHERE t.id = ?
            AND t.active = 1
            """, StageJdbcMapper.StageColumns.STAGE.columnName(),
            StageJdbcMapper.StageColumns.BOARD_ID.columnName());

    private static final Logger log = LoggerFactory.getLogger(JdbcStageRepository.class);
    protected Logger getLogger() {
        return log;
    }

    private final JdbcTaskRepository taskRepository;
    private JdbcBoardRepository boardRepository;
    @Inject
    public void setJdbcBoardRepository(JdbcBoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Inject
    public JdbcStageRepository(ConnectionManager connectionManager, JdbcTaskRepository taskRepository) {
        super(connectionManager);
        this.taskRepository = taskRepository;
    }

    @Override
    public Stage save(Stage stage) {
        return runInTransaction(
                connectionManager,
                log,
                this::save,
                stage
        );
    }

    @Override
    public Stage save(Stage stage, Connection connection) {
        if (existsById(stage.id(), connection)) {
            return update(stage, connection);
        } else {
            return create(stage, connection);
        }
    }

    @Contract("_, _ -> param1")
    private @NotNull Stage update(Stage stage, Connection connection) {
        Set<Task> existingTasks = new HashSet<>(taskRepository.findByStageId(stage.id(), connection));

        for(Task task: stage.tasks()){
            taskRepository.save(task, connection);
        }
        Set<Task> surplusTasks = new HashSet<>(existingTasks);
        surplusTasks.removeAll(stage.tasks());
        for(Task task: surplusTasks){
            taskRepository.delete(task, connection);
        }
        return stage;
    }

    @Contract("_, _ -> param1")
    private @NotNull Stage create(Stage stage, Connection connection) {
        try (PreparedStatement ps = connection.prepareStatement(INSERT)) {
            String boardDbId = stage.id().boardId().userId().value();
            ps.setString(1, boardDbId);
            String stageDbValue = StageName.fromDomain(stage.state()).getDbValue();
            ps.setString(2, stageDbValue );
            boolean updated = ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("Stage %s could not be created for board owned by %s".formatted(stageDbValue, boardDbId));
            for(Task task : stage.tasks()) {
                taskRepository.save(task, connection);
            }
            return stage;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Stage.Id id, Connection connection) {
        throw new UnsupportedOperationException("Delete of Stage is not supported");
    }

    @Override
    public List<Stage> findAll(Connection connection) {
        List<Board> existingBoards = boardRepository.findAll(connection);
        List<Stage.Id> existingStages = existingBoards.stream()
                .flatMap(b ->
                        Arrays.stream(Stage.StageState.values()).map(state -> new Stage.Id(b.id(), state))
                )
                .toList();
        return existingStages.stream()
                .map(id -> {
                    List<Task> tasks = taskRepository.findByStageId(id, connection);
                    return new Stage(id, new HashSet<>(tasks));
                })
                .toList();

    }

    @Override
    public Optional<Stage> findById(Stage.Id id, Connection connection) {
        if(!existsById(id, connection)) {
            return Optional.empty();
        }
        List<Task> tasks = taskRepository.findByStageId(id, connection);
        return Optional.of(new Stage(id, new HashSet<>(tasks)));
    }

    @Override
    public Set<Stage> findByBoard(Board.Id board) {
        return wrapWithConnection(
                connectionManager,
                log,
                this::findByBoard,
                board
        );
    }

    @Override
    public Set<Stage> findByBoard(Board.Id board, Connection connection) {
        if(!existsByBoard(board, connection)) {
            return new HashSet<>();
        }
        List<Stage.Id> existingStages = Arrays.stream(Stage.StageState.values())
                .map(state -> new Stage.Id(board, state))
                .toList();
        return existingStages.stream()
                .map(id -> {
                    List<Task> tasks = taskRepository.findByStageId(id, connection);
                    return new Stage(id, new HashSet<>(tasks));
                })
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<Stage> findByBoardAndContainingTask(Board.Id boardId, Task.Id taskId) {
        return runInTransaction(
                connectionManager,
                log,
                this::findByBoardAndContainingTask,
                boardId,
                taskId
        );
    }

    @Override
    public Optional<Stage> findByBoardAndContainingTask(Board.Id boardId, Task.Id taskId, Connection connection) {
        Optional<Stage.Id> stageId = executeQuery(
                SELECT_BY_TASK_AND_BOARD,
                ps -> {
                    ps.setString(1, boardId.userId().value());
                    ps.setBytes(2, UUIDMapper.UUIDtoByteArray(taskId.value()));
                },
                rs -> {
                    if(rs.next()) {
                        Stage.StageState stageState = StageName.fromDbValue(
                                rs.getString(StageJdbcMapper.StageColumns.STAGE.columnName()))
                                .asDomain();
                        return Optional.of(new Stage.Id(boardId, stageState));
                    }
                    return Optional.empty();
                },
                log,
                connection
        );
        if(stageId.isEmpty()) return Optional.empty();

        List<Task> tasks = taskRepository.findByStageId(stageId.get(), connection);
        return Optional.of(new Stage(stageId.get(), new HashSet<>(tasks)));
    }

    @Override
    public Optional<Stage> findByContainingTask(Task.Id taskId) {
        return wrapWithConnection(
                connectionManager,
                log,
                this::findByContainingTask,
                taskId
        );
    }

    @Override
    public Optional<Stage> findByContainingTask(Task.Id taskId, Connection connection) {
        Optional<Stage.Id> stageId = executeQuery(
                SELECT_BY_TASK,
                ps -> ps.setBytes(1, UUIDMapper.UUIDtoByteArray(taskId.value())),
                rs -> {
                    if(rs.next()) {
                        return Optional.of(StageJdbcMapper.extractStageId(rs));
                    }
                    return Optional.empty();
                },
                log,
                connection
        );
        if(stageId.isEmpty()) return Optional.empty();

        List<Task> tasks = taskRepository.findByStageId(stageId.get(), connection);
        return Optional.of(new Stage(stageId.get(), new HashSet<>(tasks)));
    }

    @Override
    public boolean existsById(Stage.Id id, Connection connection) {
        return executeQuery(
                EXISTS,
                ps -> {
                    ps.setString(1, id.boardId().userId().value());
                    ps.setString(2, StageName.fromDomain(id.state()).getDbValue());
                },
                rs -> rs.next() && rs.getInt("total") > 0,
                log,
                connection
        );
    }

    @Override
    public boolean existsByBoard(Board.Id stage) {
        return wrapWithConnection(
                connectionManager,
                log,
                this::existsByBoard,
                stage
        );
    }

    @Override
    public boolean existsByBoard(Board.Id board, Connection connection) {
        return executeQuery(
                EXISTS,
                ps -> {
                    ps.setString(1, board.userId().value());
                    ps.setString(2, StageName.fromDomain(Stage.StageState.DONE).getDbValue());
                },
                rs -> rs.next() && rs.getInt("total") > 0,
                log,
                connection
        );
    }
}
