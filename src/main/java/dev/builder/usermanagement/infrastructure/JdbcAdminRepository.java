package dev.builder.usermanagement.infrastructure;

import dev.builder.core.domain.AuditInfo;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.di.annotation.Lazy;
import dev.builder.core.infrastructure.persistence.*;
import dev.builder.usermanagement.domain.model.Admin;
import dev.builder.usermanagement.domain.model.Manager;
import dev.builder.usermanagement.domain.port.out.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.executeQuery;
import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.wrapWithConnection;

@Bean
public class JdbcAdminRepository extends TransactionalJdbcCrudRepository<Admin,Admin.Id> implements AdminRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcAdminRepository.class);

    protected Logger getLogger() {return LOGGER;}

    private static final String SELECT_ALL = String.format("""
            SELECT
                u.*,
                m_active.email as %s
            FROM "ADMIN" a
            JOIN app_user u ON u.email = a.email AND u.active = 1
            LEFT JOIN (
                SELECT m.email, m.created_by
                FROM manager m
                JOIN app_user mu ON mu.email = m.email AND mu.active = 1
            ) m_active ON m_active.created_by = u.email
            """,
            UserJdbcMapper.ManagerColumns.AS_CREATED.columnName());
    private static final String SELECT_BY_ID = String.format("""
            SELECT
                u.*,
                m_active.email as %s
            FROM "ADMIN" a
            JOIN app_user u ON u.email = a.email AND u.active = 1
            LEFT JOIN (
                SELECT m.email, m.created_by
                FROM manager m
                JOIN app_user mu ON mu.email = m.email AND mu.active = 1
            ) m_active ON m_active.created_by = u.email
            WHERE u.email = ?
            """,
            UserJdbcMapper.ManagerColumns.AS_CREATED.columnName());


    private static final String SELECT_BY_CREATED_STUDENT = String.format("""
            SELECT
                u.*,
                m_active.email AS %s
            FROM manager m
            JOIN app_user mu ON mu.email = m.email AND mu.active = 1
            JOIN "ADMIN" a ON a.email = m.created_by
            JOIN app_user u ON u.email = a.email AND u.active = 1
            LEFT JOIN (
                SELECT m2.email, m2.created_by
                FROM manager m2
                JOIN app_user mu ON mu.email = m2.email AND mu.active = 1
            ) m_active ON m_active.created_by = u.email
            WHERE m.email = ?
            """,
            UserJdbcMapper.ManagerColumns.AS_CREATED.columnName());


    private static final String INSERT = """
            INSERT INTO "ADMIN"(email)
            VALUES (?)
            """;

    private static final String EXISTS_BY_ID = """
            SELECT count(*) AS total
            FROM "ADMIN" a
            JOIN APP_USER u ON u.email = a.email
            WHERE u.ACTIVE = 1
            """;

    private static final String EXISTS_DELETED_BY_ID = """
            SELECT count(*) AS total
            FROM "ADMIN" a
            JOIN APP_USER u ON u.email = a.email
            WHERE u.ACTIVE = 0
            """;

    private JdbcAnyUserRepository anyUserRepository;

    @Inject
    @Lazy
    public void setAnyUserRepository(JdbcAnyUserRepository anyUserRepository) {
        this.anyUserRepository = anyUserRepository;
    }

    @Inject
    public JdbcAdminRepository(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    public Admin save(Admin admin, Connection connection) {
        if(existsById(admin.id())){
            return update(admin, connection);
        } else {
            return create(admin, connection);
        }
    }

    private Admin create(Admin admin, Connection connection) {
        try(PreparedStatement ps = connection.prepareStatement(INSERT)){
            AuditInfo auditInfo = anyUserRepository.saveBaseUserInfo(admin, connection);
            ps.setString(1, admin.id().value());
            boolean updated = ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("Duplicate key on admin insert");
            return admin.hydratedWithAuditInfo(auditInfo);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    private Admin update(Admin admin, Connection connection) {
        try {
            AuditInfo auditInfo = anyUserRepository.updateBaseUserInfo(admin, connection);
            return admin.hydratedWithAuditInfo(auditInfo);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }


    @Override
    public boolean deleteById(Admin.Id id, Connection connection) {
        return anyUserRepository.deleteById(id, connection);
    }

    @Override
    public boolean deleteById(Admin.Id id) {
        return anyUserRepository.deleteById(id);
    }

    @Override
    public List<Admin> findAll(Connection connection) {
        return executeQuery(
                SELECT_ALL,
                PreparedStatementFiller.NO_OP,
                rs -> rs.next() ? UserJdbcMapper.rowToAdmins(rs) : Collections.emptyList(),
                LOGGER,
                connection
        );
    }

    @Override
    public Optional<Admin> findById(Admin.Id id, Connection connection) {
        return executeQuery(
                SELECT_BY_ID,
                ps -> ps.setString(1, id.value()),
                rs -> rs.next() ? Optional.of(UserJdbcMapper.rowToAdmin(rs)) : Optional.empty(),
                LOGGER,
                connection
        );
    }

    @Override
    public Optional<Admin> findByCreatedManager(Manager.Id id) {
        return wrapWithConnection(
                connectionManager,
                LOGGER,
                this::findByCreatedManager,
                id
        );
    }

    @Override
    public Optional<Admin> findByCreatedManager(Manager.Id id, Connection connection) {
        return executeQuery(
                SELECT_BY_CREATED_STUDENT,
                ps -> ps.setString(1, id.value()),
                rs -> rs.next() ? Optional.of(UserJdbcMapper.rowToAdmin(rs)) : Optional.empty(),
                LOGGER,
                connection
        );
    }

    @Override
    public boolean existsById(Admin.Id id, Connection connection) {
        return existsById(EXISTS_BY_ID, rs -> rs.setString(1, id.value()),id, connection);
    }

    @Override
    public boolean existsDeletedById(Admin.Id id) {
        return wrapWithConnection(
                connectionManager,
                LOGGER,
                this::existsDeletedById,
                id
        );
    }

    @Override
    public boolean existsDeletedById(Admin.Id id, Connection connection) {
        return existsById(EXISTS_DELETED_BY_ID, rs -> rs.setString(1, id.value()),id, connection);
    }
}
