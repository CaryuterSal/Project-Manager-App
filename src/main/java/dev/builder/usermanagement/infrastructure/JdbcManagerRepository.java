package dev.builder.usermanagement.infrastructure;

import dev.builder.core.domain.AuditInfo;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.di.annotation.Lazy;
import dev.builder.core.infrastructure.persistence.*;
import dev.builder.usermanagement.domain.model.Admin;
import dev.builder.usermanagement.domain.model.Manager;
import dev.builder.usermanagement.domain.model.Student;
import dev.builder.usermanagement.domain.port.out.ManagerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.executeQuery;
import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.wrapWithConnection;

@Bean
public class JdbcManagerRepository extends TransactionalJdbcCrudRepository<Manager, Manager.Id> implements ManagerRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcManagerRepository.class);

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    private static final String SELECT_ALL = String.format("""
            SELECT
                u.*,
                s_active.email as %s,
                m.created_by as %s
            FROM manager m
            JOIN app_user u ON u.email = m.email AND u.active = 1
            LEFT JOIN (
                SELECT s.email, s.created_by
                FROM student s
                JOIN app_user su ON su.email = s.email AND su.active = 1
            ) s_active ON s_active.created_by = u.email
            """,
            UserJdbcMapper.StudentColumns.AS_CREATED.columnName(),
            UserJdbcMapper.ManagerColumns.CREATED_BY.columnName());


    private static final String SELECT_ALL_WITH_EMAIL_LIKE = String.format("""
            SELECT
                u.*,
                s_active.email as %s,
                m.created_by as %s
            FROM manager m
            JOIN app_user u ON u.email = m.email AND u.active = 1
            LEFT JOIN (
                SELECT s.email, s.created_by
                FROM student s
                JOIN app_user su ON su.email = s.email AND su.active = 1
            ) s_active ON s_active.created_by = u.email
            WHERE m.email LIKE ?
            """,
            UserJdbcMapper.StudentColumns.AS_CREATED.columnName(),
            UserJdbcMapper.ManagerColumns.CREATED_BY.columnName());

    private static final String SELECT_BY_ID = String.format("""
            SELECT
                u.*,
                s_active.email as %s,
                m.created_by as %s
            FROM manager m
            JOIN app_user u ON u.email = m.email AND u.active = 1
            LEFT JOIN (
                SELECT s.email, s.created_by
                FROM student s
                JOIN app_user su ON su.email = s.email AND su.active = 1
            ) s_active ON s_active.created_by = u.email
            WHERE u.email = ?
            """,
            UserJdbcMapper.StudentColumns.AS_CREATED.columnName(),
            UserJdbcMapper.ManagerColumns.CREATED_BY.columnName());

    private static final String SELECT_BY_CREATED_STUDENT = String.format("""
            SELECT
                u.*,
                s_active.email AS %s,
                m.created_by AS %s
            FROM student s
            JOIN app_user su ON su.email = s.email AND su.active = 1
            JOIN manager m ON m.email = s.created_by
            JOIN app_user u ON u.email = m.email AND u.active = 1
            LEFT JOIN (
                SELECT s2.email, s2.created_by
                FROM student s2
                JOIN app_user su ON su.email = s2.email AND su.active = 1
            ) s_active ON s_active.created_by = u.email
            WHERE s.email = ?
            """,
            UserJdbcMapper.StudentColumns.AS_CREATED.columnName(),
            UserJdbcMapper.ManagerColumns.CREATED_BY.columnName());

    private static final String SELECT_BY_CREATOR = String.format("""
           
            SELECT
                u.*,
                s_active.email AS %s,
                m.created_by AS %s
            FROM admin a
            JOIN app_user au ON au.email = a.email AND au.active = 1
             JOIN manager m ON m.created_by = a.email
            JOIN app_user u ON u.email = m.email AND u.active = 1
            LEFT JOIN (
                SELECT s.email, s.created_by
                FROM student s
                JOIN app_user su ON su.email = s.email AND su.active = 1
            ) s_active ON s_active.created_by = u.email
            WHERE a.email = ?
            """,
            UserJdbcMapper.StudentColumns.AS_CREATED.columnName(),
            UserJdbcMapper.ManagerColumns.CREATED_BY.columnName());

    private static final String INSERT = """
            INSERT INTO manager(
                email,
                created_by)
            VALUES (?, ?)
            """;

    private static final String EXISTS_BY_ID = """
            SELECT COUNT(*) AS total
            FROM manager m
            JOIN app_user u ON u.email = m.email
            WHERE u.email = ?
            AND
            u.active = 1
            """;

    private static final String EXISTS_DELETED_BY_ID = """
            SELECT COUNT(*) AS total
            FROM manager m
            JOIN app_user u ON u.email = m.email
            WHERE u.email = ?
            AND
            u.active = 0
            """;

    private JdbcAnyUserRepository anyUserRepository;

    @Inject
    @Lazy
    public void setAnyUserRepository(JdbcAnyUserRepository anyUserRepository) {
        this.anyUserRepository = anyUserRepository;
    }

    @Inject
    public JdbcManagerRepository(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    public Optional<Manager> findById(Manager.Id id, Connection connection) {
        return executeQuery(
                SELECT_BY_ID,
                ps -> ps.setString(1, id.value()),
                rs -> rs.next() ? Optional.of(UserJdbcMapper.rowToManager(rs)) : Optional.empty(),
                getLogger(),
                connection
        );
    }

    @Override
    public List<Manager> findAll(Connection connection) {
        return executeQuery(
                SELECT_ALL,
                PreparedStatementFiller.NO_OP,
                rs -> rs.next() ? UserJdbcMapper.rowToManagers(rs) : Collections.emptyList(),
                getLogger(),
                connection
        );
    }

    @Override
    public List<Manager> findWithEmailLike(String emailLike) {
        return wrapWithConnection(
                connectionManager,
                LOGGER,
                this::findWithEmailLike,
                emailLike
        );
    }

    @Override
    public List<Manager> findWithEmailLike(String emailLike, Connection connection) {
        return executeQuery(
                SELECT_ALL_WITH_EMAIL_LIKE,
                ps -> ps.setString(1, "%" + emailLike + "%"),
                rs -> rs.next() ? UserJdbcMapper.rowToManagers(rs) : Collections.emptyList(),
                getLogger(),
                connection
        );
    }

    @Override
    public List<Manager> findByCreatedBy(Admin.Id id) {
        return wrapWithConnection(connectionManager, LOGGER, this::findByCreatedBy, id);
    }

    @Override
    public List<Manager> findByCreatedBy(Admin.Id id, Connection connection) {
        return executeQuery(
                SELECT_BY_CREATOR,
                ps -> ps.setString(1, id.value()),
                rs -> rs.next() ? UserJdbcMapper.rowToManagers(rs) : Collections.emptyList(),
                getLogger(),
                connection
        );
    }

    @Override
    public Optional<Manager> findByCreatedStudent(Student.Id id) {
        return wrapWithConnection(connectionManager, LOGGER, this::findByCreatedStudent, id);
    }

    @Override
    public Optional<Manager> findByCreatedStudent(Student.Id id, Connection connection) {
        return executeQuery(
                SELECT_BY_CREATED_STUDENT,
                ps -> ps.setString(1, id.value()),
                rs -> rs.next() ? Optional.of(UserJdbcMapper.rowToManager(rs)) : Optional.empty(),
                getLogger(),
                connection
        );
    }

    @Override
    public boolean deleteById(Manager.Id id) {
        return anyUserRepository.deleteById(id);
    }


    @Override
    public boolean deleteById(Manager.Id id, Connection connection) {
        return anyUserRepository.deleteById(id,connection);
    }

    @Override
    public Manager save(Manager manager, Connection connection) {
        if(existsDeletedById(manager.id(), connection)){
            anyUserRepository.recover(manager.id(), connection);
            return findById(manager.id(), connection).orElseThrow();
        }
        if(existsById(manager.id(), connection)){
            return update(manager, connection);
        } else {
            return create(manager, connection);
        }
    }

    private Manager create(Manager manager, Connection connection){
        try(PreparedStatement ps = connection.prepareStatement(INSERT)){
            AuditInfo auditInfo = anyUserRepository.saveBaseUserInfo(manager, connection);
            ps.setString(1, manager.email().value());
            ps.setString(2, manager.createdBy().value());

            boolean updated = ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("Duplicate key on user insert");

            return manager.hydratedWithAuditInfo(auditInfo);
        } catch (SQLException e){
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    private Manager update(Manager manager, Connection connection){
        try {
            AuditInfo updatedAuditInfo = anyUserRepository.updateBaseUserInfo(manager, connection);
            return manager.hydratedWithAuditInfo(updatedAuditInfo);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(Manager.Id id, Connection connection) {
        return existsById(EXISTS_BY_ID, rs -> rs.setString(1, id.value()), id, connection);
    }

    @Override
    public boolean existsDeletedById(Manager.Id id) {
        return wrapWithConnection(connectionManager, LOGGER, this::existsDeletedById, id);
    }

    @Override
    public boolean existsDeletedById(Manager.Id id, Connection connection) {
        return existsById(EXISTS_DELETED_BY_ID, rs -> rs.setString(1, id.value()),id, connection);
    }
}
