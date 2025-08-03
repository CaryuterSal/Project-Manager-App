package dev.builder.board.infrastructure;

import dev.builder.board.domain.model.Task;
import dev.builder.core.infrastructure.persistence.RepositoryException;
import dev.builder.core.infrastructure.persistence.UUIDMapper;
import dev.builder.usermanagement.domain.model.Student;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.executeQuery;

public class JdbcTaskAssigneeRepository {

    private static final String INSERT = """
            INSERT INTO task_assignee(sbd_sdt_email, sbd_bad_email, TSK_ID)
            VALUES (?, ?,?)
            """;
    private static final String DELETE = """
            DELETE FROM TASK_ASSIGNEE
            WHERE sbd_sdt_email = ?
            AND sbd_bad_email = ?
            AND tsk_id = ?
    """;
    private static final String SELECT_FOR_TASK = String.format("""
            SELECT
                tas.sbd_sdt_email as %s
            FROM student s
            JOIN app_user su ON su.email = s.email AND su.active = 1
            JOIN task_assignee tas ON tas.sbd_sdt_email = su.email
            JOIN manager m ON m.email = tas.sbd_bad_email
            JOIN app_user u ON u.email = m.email AND u.active = 1
            WHERE tas.sbd_tsk_id = ?
            """, TaskJdbcMapper.TaskColumns.ASSIGNED_TO.columnName());

    private static final Logger log = LoggerFactory.getLogger(JdbcTaskAssigneeRepository.class);

    public Task save(@NotNull Task task, Connection connection) {
        try {
            Set<Student.Id> existingAssignations = executeQuery(
                    SELECT_FOR_TASK,
                    ps -> ps.setBytes(1, UUIDMapper.UUIDtoByteArray(task.id().value())),
                    rs -> rs.next() ? TaskJdbcMapper.extractAssignations(rs, task.id()) : new HashSet<>(),
                    log,
                    connection
            );
            Set<Student.Id> missingAssignations = new HashSet<>(task.assignedStudents());
            missingAssignations.removeAll(existingAssignations);
            for (Student.Id assignedStudent : missingAssignations) {
                create(task, assignedStudent, connection);
            }

            Set<Student.Id> surplusAssignations = new HashSet<>(existingAssignations);
            surplusAssignations.removeAll(task.assignedStudents());
            for (Student.Id assignedStudent : surplusAssignations) {
                delete(task, assignedStudent, connection);
            }
            return task;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    private void delete(Task task, @NotNull Student.Id assignedStudent, Connection connection) throws SQLException {
        try(PreparedStatement ps = connection.prepareStatement(DELETE)){
            ps.setString(1, assignedStudent.value());
            ps.setString(2,  task.stage().boardId().userId().value());
            ps.setBytes(3, UUIDMapper.UUIDtoByteArray(task.id().value()));
            boolean updated = ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("There was an error while deleting a assignedStudent");
        }
    }

    private void create(Task task, @NotNull Student.Id assignedStudent, Connection connection) throws SQLException{
        try(PreparedStatement ps = connection.prepareStatement(INSERT)){
            ps.setString(1, assignedStudent.value());
            ps.setString(2,  task.stage().boardId().userId().value());
            ps.setBytes(3, UUIDMapper.UUIDtoByteArray(task.id().value()));
            boolean updated = ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("There was an error while inserting new Collaborator");
        }
    }
}
