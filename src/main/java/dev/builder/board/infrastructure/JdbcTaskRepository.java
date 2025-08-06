package dev.builder.board.infrastructure;

import dev.builder.board.domain.model.Attachment;
import dev.builder.board.domain.model.Stage;
import dev.builder.board.domain.model.Task;
import dev.builder.core.domain.Entity;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.executeQuery;
import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.runInTransaction;

@Bean
public class JdbcTaskRepository extends TransactionalJdbcCrudRepository<Task, Task.Id> {

    private static final Logger log = LoggerFactory.getLogger(JdbcTaskRepository.class);

    @Override
    protected Logger getLogger() {
        return log;
    }


    private static final String INSERT = """
            INSERT INTO task(id, title, description, started_at, finished_at, deadline, clr_name)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE task
            SET title = ?,
            description = ?,
            started_at = ?,
            finished_at = ?,
            deadline = ?,
            clr_name = ?
            WHERE id = ?
            AND active = 1
            """;

    private static final String SELECT = String.format("""
            SELECT
                t.id as %s,
                st.bse_bad_email as %s,
                st.bse_sae_name as %s,
                st."order" as %s,
                t.title as %s,
                t.description as %s,
                t.created_at as %s,
                t.started_at as %s,
                t.finished_at as %s,
                t.deadline as %s,
                t.clr_name as %s,
                active_cover.id as %s,
                active_attachment.id as %s,
                active_assignee.sdt_email as %s
            FROM task t
            JOIN stage_task st ON st.tsk_id = t.id
            JOIN manager m ON m.email = st.bse_bad_email
            JOIN app_user u ON u.email = m.email AND u.active = 1
            LEFT JOIN (
                SELECT
                    f.id as id,
                    tac.tsk_id as tsk_id
                FROM task_cover tac
                JOIN "FILE" f ON f.id = tac.fle_id AND f.active = 1
            ) active_cover ON active_cover.tsk_id = t.id
            LEFT JOIN (
                SELECT
                    f.id as id,
                    ta.tsk_id as tsk_id
                FROM task_attachement ta
                JOIN "FILE" f ON f.id = ta.fle_id AND f.active = 1
            ) active_attachment ON active_attachment.tsk_id = t.id
            LEFT JOIN(
                SELECT
                    tas.tsk_id as tsk_id,
                    tas.sbd_sdt_email as sdt_email
                FROM student s
                JOIN app_user su ON su.email = s.email AND su.active = 1
                JOIN student_board sb ON sb.sdt_email = su.email
                JOIN task_assignee tas ON tas.sbd_sdt_email = sb.sdt_email
            ) active_assignee ON active_assignee.tsk_id = t.id
            WHERE t.id = ?
            AND t.active = 1
            """,
            TaskJdbcMapper.TaskColumns.ID.columnName(),
            TaskJdbcMapper.TaskColumns.BOARD.columnName(),
            TaskJdbcMapper.TaskColumns.STAGE.columnName(),
            TaskJdbcMapper.TaskColumns.ORDER.columnName(),
            TaskJdbcMapper.TaskColumns.TITLE.columnName(),
            TaskJdbcMapper.TaskColumns.DESCRIPTION.columnName(),
            TaskJdbcMapper.TaskColumns.CREATED_AT.columnName(),
            TaskJdbcMapper.TaskColumns.STARTED_AT.columnName(),
            TaskJdbcMapper.TaskColumns.FINISHED_AT.columnName(),
            TaskJdbcMapper.TaskColumns.DEADLINE.columnName(),
            TaskJdbcMapper.TaskColumns.COLOR.columnName(),
            TaskJdbcMapper.TaskColumns.COVER_IMAGE.columnName(),
            TaskJdbcMapper.TaskColumns.ATTACHMENT.columnName(),
            TaskJdbcMapper.TaskColumns.ASSIGNED_TO.columnName());

    private static final String SELECT_ALL = String.format("""
           SELECT
                t.id as %s,
                 st.bse_bad_email as %s,
                 st.bse_sae_name as %s,
                 st."order" as %s,
                t.title as %s,
                t.description as %s,
                t.created_at as %s,
                t.started_at as %s,
                t.finished_at as %s,
                t.deadline as %s,
                t.clr_name as %s,
                active_cover.id as %s,
                active_attachment.id as %s,
                active_assignee.sdt_email as %s
            FROM task t
            JOIN stage_task st ON st.tsk_id = t.id
            JOIN manager m ON m.email = st.bse_bad_email
            JOIN app_user u ON u.email = m.email AND u.active = 1
            LEFT JOIN (
                SELECT
                    f.id as id,
                    tac.tsk_id as tsk_id
                FROM task_cover tac
                JOIN "FILE" f ON f.id = tac.fle_id AND f.active = 1
            ) active_cover ON active_cover.tsk_id = t.id
            LEFT JOIN (
                SELECT
                    f.id as id,
                    ta.tsk_id as tsk_id
                FROM task_attachement ta
                JOIN "FILE" f ON f.id = ta.fle_id AND f.active = 1
            ) active_attachment ON active_attachment.tsk_id = t.id
            LEFT JOIN(
                SELECT
                    tas.tsk_id as tsk_id,
                    tas.sbd_sdt_email as sdt_email
                FROM student s
                JOIN app_user su ON su.email = s.email AND su.active = 1
                JOIN student_board sb ON sb.sdt_email = su.email
                JOIN task_assignee tas ON tas.sbd_sdt_email = sb.sdt_email
            ) active_assignee ON active_assignee.tsk_id = t.id
            WHERE t.active = 1
           """,
            TaskJdbcMapper.TaskColumns.ID.columnName(),
            TaskJdbcMapper.TaskColumns.BOARD.columnName(),
            TaskJdbcMapper.TaskColumns.STAGE.columnName(),
            TaskJdbcMapper.TaskColumns.ORDER.columnName(),
            TaskJdbcMapper.TaskColumns.TITLE.columnName(),
            TaskJdbcMapper.TaskColumns.DESCRIPTION.columnName(),
            TaskJdbcMapper.TaskColumns.CREATED_AT.columnName(),
            TaskJdbcMapper.TaskColumns.STARTED_AT.columnName(),
            TaskJdbcMapper.TaskColumns.FINISHED_AT.columnName(),
            TaskJdbcMapper.TaskColumns.DEADLINE.columnName(),
            TaskJdbcMapper.TaskColumns.COLOR.columnName(),
            TaskJdbcMapper.TaskColumns.COVER_IMAGE.columnName(),
            TaskJdbcMapper.TaskColumns.ATTACHMENT.columnName(),
            TaskJdbcMapper.TaskColumns.ASSIGNED_TO.columnName());

    private static final String SELECT_ALL_BY_STAGE_ID = String.format("""
           SELECT
                t.id as %s,
                 st.bse_bad_email as %s,
                 st.bse_sae_name as %s,
                 st."order" as %s,
                t.title as %s,
                t.description as %s,
                t.created_at as %s,
                t.started_at as %s,
                t.finished_at as %s,
                t.deadline as %s,
                t.clr_name as %s,
                active_cover.id as %s,
                active_attachment.id as %s,
                active_assignee.sdt_email as %s
            FROM task t
            JOIN stage_task st ON st.tsk_id = t.id
            JOIN manager m ON m.email = st.bse_bad_email
            JOIN app_user u ON u.email = m.email AND u.active = 1
            LEFT JOIN (
                SELECT
                    f.id as id,
                    tac.tsk_id as tsk_id
                FROM task_cover tac
                JOIN "FILE" f ON f.id = tac.fle_id AND f.active = 1
            ) active_cover ON active_cover.tsk_id = t.id
            LEFT JOIN (
                SELECT
                    f.id as id,
                    ta.tsk_id as tsk_id
                FROM task_attachement ta
                JOIN "FILE" f ON f.id = ta.fle_id AND f.active = 1
            ) active_attachment ON active_attachment.tsk_id = t.id
            LEFT JOIN(
                SELECT
                    tas.tsk_id as tsk_id,
                    tas.sbd_sdt_email as sdt_email
                FROM student s
                JOIN app_user su ON su.email = s.email AND su.active = 1
                JOIN student_board sb ON sb.sdt_email = su.email
                JOIN task_assignee tas ON tas.sbd_sdt_email = sb.sdt_email
            ) active_assignee ON active_assignee.tsk_id = t.id
            WHERE st.bse_bad_email = ?
            AND st.bse_sae_name = ?
            AND t.active = 1
           """,
            TaskJdbcMapper.TaskColumns.ID.columnName(),
            TaskJdbcMapper.TaskColumns.BOARD.columnName(),
            TaskJdbcMapper.TaskColumns.STAGE.columnName(),
            TaskJdbcMapper.TaskColumns.ORDER.columnName(),
            TaskJdbcMapper.TaskColumns.TITLE.columnName(),
            TaskJdbcMapper.TaskColumns.DESCRIPTION.columnName(),
            TaskJdbcMapper.TaskColumns.CREATED_AT.columnName(),
            TaskJdbcMapper.TaskColumns.STARTED_AT.columnName(),
            TaskJdbcMapper.TaskColumns.FINISHED_AT.columnName(),
            TaskJdbcMapper.TaskColumns.DEADLINE.columnName(),
            TaskJdbcMapper.TaskColumns.COLOR.columnName(),
            TaskJdbcMapper.TaskColumns.COVER_IMAGE.columnName(),
            TaskJdbcMapper.TaskColumns.ATTACHMENT.columnName(),
            TaskJdbcMapper.TaskColumns.ASSIGNED_TO.columnName());

    private static final String SELECT_CREATED_AT = String.format("""
            SELECT
                created_at as %s
            FROM task
            WHERE id = ?
            """, TaskJdbcMapper.TaskColumns.CREATED_AT.columnName());
    private static final String EXISTS = """
            SELECT COUNT(*) AS total
            FROM task
            WHERE id = ?
            AND active = 1
            """;

    private static final String EXISTS_DELETED = """
            SELECT COUNT(*) AS total
            FROM task
            WHERE id = ?
            AND active = 0
            """;

    private static final String DELETE = """
            UPDATE task
            SET active = 0
            WHERE id = ?
            AND active = 1
            """;

    private static final String RECOVER = """
            UPDATE task
            SET active = 1
            WHERE id = ?
            AND active = 0
            """;

    private final JdbcTaskAssigneeRepository assigneeRepository;
    private final JdbcTaskStageRepository stageBindingRepository;

    @Inject
    public JdbcTaskRepository(ConnectionManager connectionManager, JdbcTaskAssigneeRepository assigneeRepository, JdbcTaskStageRepository stageBindingRepository) {
        super(connectionManager);
        this.assigneeRepository = assigneeRepository;
        this.stageBindingRepository = stageBindingRepository;
    }

    @Override
    public Task save(Task task) {
        return runInTransaction(
                connectionManager,
                log,
                this::save,
                task
        );
    }


    @Override
    public Task save(Task task, Connection connection) {
        try {
            Task savedTask;
            if(existsDeletedById(task.id(), connection)) {
                recover(task, connection);
            }
            if (existsById(task.id(), connection)) {
                savedTask = update(task, connection);
            } else {
                savedTask = create(task, connection);
            }
            savedTask = stageBindingRepository.save(savedTask, connection);
            savedTask = assigneeRepository.save(savedTask, connection);
            return savedTask;
        } catch (SQLException ex){
            log.error(ex.getMessage(), ex);
            throw new RepositoryException(ex.getMessage(), ex);
        }
    }

    private void recover(Task task, Connection connection) {
        try(PreparedStatement ps = connection.prepareStatement(RECOVER)){
            ps.setBytes(1, UUIDMapper.UUIDtoByteArray(task.id().value()));
            boolean updated =  ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("There was an error while trying to recover a task");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    private Task create(Task task, Connection connection) throws SQLException {
        try(PreparedStatement ps = connection.prepareStatement(INSERT)){
            ps.setBytes(1, UUIDMapper.UUIDtoByteArray(task.id().value()));
            ps.setString(2, task.title().value());
            ps.setString(3, task.description().value());
            Optional<OffsetDateTime> zonedStartedAt = task.executionPeriod().map(e -> e.start().atZone(ZoneId.systemDefault()).toOffsetDateTime());
            Optional<OffsetDateTime> zonedFinishedAt = task.executionPeriod().flatMap(e -> e.end().map(end -> end.atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            ps.setObject(4, zonedStartedAt.orElse(null));
            ps.setObject(5, zonedFinishedAt.orElse(null));
            OffsetDateTime zonedDeadline = task.deadline().value().atZone(ZoneId.systemDefault()).toOffsetDateTime();
            ps.setObject(6, zonedDeadline);
            ps.setString(7, task.color().toString());
            boolean updated = ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("There was an error saving task");
        }

        try(PreparedStatement ps = connection.prepareStatement(SELECT_CREATED_AT)){
            ps.setBytes(1, UUIDMapper.UUIDtoByteArray(task.id().value()));
            try(ResultSet rs = ps.executeQuery()){
                rs.next();
                OffsetDateTime odt = rs.getObject(TaskJdbcMapper.TaskColumns.CREATED_AT.columnName(), OffsetDateTime.class);
                LocalDateTime createdAt = odt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
                return task.hydratedWithAuditInfo(createdAt);
            }
        }
    }

    private Task update(Task task, Connection connection) throws SQLException {
        try(PreparedStatement ps = connection.prepareStatement(UPDATE)){
            ps.setString(1, task.title().value());
            ps.setString(2, task.description().value());
            Optional<OffsetDateTime> zonedStartedAt = task.executionPeriod().map(e -> e.start().atZone(ZoneId.systemDefault()).toOffsetDateTime());
            Optional<OffsetDateTime> zonedFinishedAt = task.executionPeriod().flatMap(e -> e.end().map(end -> end.atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            ps.setObject(3, zonedStartedAt.orElse(null));
            ps.setObject(4, zonedFinishedAt.orElse(null));
            OffsetDateTime zonedDeadline = task.deadline().value().atZone(ZoneId.systemDefault()).toOffsetDateTime();
            ps.setObject(5, zonedDeadline);
            ps.setString(6, task.color().toString());
            ps.setBytes(7, UUIDMapper.UUIDtoByteArray(task.id().value()));
            boolean updated = ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("There was an error saving task");
            return task;
        }
    }

    @Override
    public boolean deleteById(Task.Id id, Connection connection) {
        try(PreparedStatement ps = connection.prepareStatement(DELETE)){
            ps.setBytes(1,UUIDMapper.UUIDtoByteArray(id.value()));
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
            throw new RepositoryException(ex.getMessage(), ex);
        }
    }

    @Override
    public List<Task> findAll(Connection connection) {
        return executeQuery(
                SELECT_ALL,
                PreparedStatementFiller.NO_OP,
                rs -> rs.next() ? TaskJdbcMapper.rowToTasks(rs) : new ArrayList<>(),
                log,
                connection
        );
    }

    @Override
    public Optional<Task> findById(Task.Id id, Connection connection) {
        return executeQuery(
                SELECT,
                ps -> ps.setBytes(1, UUIDMapper.UUIDtoByteArray(id.value())),
                rs -> rs.next() ? Optional.of(TaskJdbcMapper.rowToTask(rs)) : Optional.empty(),
                log,
                connection
        );
    }

    public List<Task> findByStageId(Stage.Id stageId, Connection connection) {
        return executeQuery(
            SELECT_ALL_BY_STAGE_ID,
                ps -> {
                    ps.setString(1, stageId.boardId().userId().value());
                    ps.setString(2, StageName.fromDomain(stageId.state()).getDbValue());
                },
                rs -> rs.next() ? TaskJdbcMapper.rowToTasks(rs) : new ArrayList<>(),
                log,
                connection
        );
    }

    @Override
    public boolean existsById(Task.Id id, Connection connection) {
        return executeQuery(
                EXISTS,
                ps -> ps.setBytes(1, UUIDMapper.UUIDtoByteArray(id.value())),
                rs -> rs.next() && rs.getInt("total") > 0,
                log,
                connection
        );
    }

    public boolean existsDeletedById(Task.Id id, Connection connection) {
        return executeQuery(
                EXISTS_DELETED,
                ps -> ps.setBytes(1, UUIDMapper.UUIDtoByteArray(id.value())),
                rs -> rs.next() && rs.getInt("total") > 0,
                log,
                connection
        );
    }
}
