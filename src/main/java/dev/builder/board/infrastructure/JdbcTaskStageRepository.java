package dev.builder.board.infrastructure;

import dev.builder.board.domain.model.Task;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.persistence.RepositoryException;
import dev.builder.core.infrastructure.persistence.UUIDMapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.executeQuery;

@Bean
public class JdbcTaskStageRepository {

    private static final String INSERT = """
            INSERT INTO STAGE_TASK(bse_bad_email, BSE_SAE_NAME, TSK_ID,"order")
            VALUES (?, ?,?, ?)
            """;

    private static final String UPDATE = """
            UPDATE STAGE_TASK
            SET bse_sae_name = ?,
            "order" = ?
            WHERE tsk_id = ?
    """;

    private static final String EXISTS = """
            SELECT COUNT(*) AS total
              FROM manager m
              JOIN app_user u ON u.email = m.email AND u.active = 1
              JOIN stage_task sta ON sta.bse_bad_email = u.email
              JOIN task t ON t.id = sta.tsk_id AND t.active = 1
              WHERE t.id = ?
            """;

    private static final Logger log = LoggerFactory.getLogger(JdbcTaskStageRepository.class);


    public Task save(@NotNull Task task, Connection connection) {
        try {
            if(existsById(task.id(), connection)) {
                return update(task, connection);
            } else {
                return create(task, connection);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    public boolean existsById(@NotNull Task.Id id, Connection connection) {
        return executeQuery(
                EXISTS,
                ps -> ps.setBytes(1, UUIDMapper.UUIDtoByteArray(id.value())),
                rs -> rs.next() && rs.getInt("total") > 0,
                log,
                connection
        );
    }

    private Task update(Task task, Connection connection) throws SQLException {
        try(PreparedStatement ps = connection.prepareStatement(UPDATE)){
            ps.setString(1, StageName.fromDomain(task.stage().state()).getDbValue());
            ps.setDouble(2, task.order().value());
            ps.setBytes(3, UUIDMapper.UUIDtoByteArray(task.id().value()));
            boolean updated = ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("There was an error while updating a binding");
            return task;
        }
    }

    private Task create(Task task, Connection connection) throws SQLException{
        try(PreparedStatement ps = connection.prepareStatement(INSERT)){
            ps.setString(1, task.stage().boardId().userId().value());
            ps.setString(2, StageName.fromDomain(task.stage().state()).getDbValue());
            ps.setBytes(3, UUIDMapper.UUIDtoByteArray(task.id().value()));
            ps.setDouble(4, task.order().value());
            boolean updated = ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("There was an error while inserting new Collaborator");
            return task;
        }
    }
}
