package dev.builder.usermanagement.infrastructure;

import dev.builder.board.infrastructure.TaskJdbcMapper;
import dev.builder.core.domain.AuditInfo;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.persistence.*;
import dev.builder.usermanagement.domain.model.Admin;
import dev.builder.usermanagement.domain.model.Manager;
import dev.builder.usermanagement.domain.model.Student;
import dev.builder.usermanagement.domain.model.User;
import dev.builder.usermanagement.domain.port.out.AdminRepository;
import dev.builder.usermanagement.domain.port.out.AnyUserRepository;
import dev.builder.usermanagement.domain.port.out.ManagerRepository;
import dev.builder.usermanagement.domain.port.out.StudentRepository;
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
import java.util.*;

import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.executeQuery;
import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.wrapWithConnection;

@Bean
public class JdbcAnyUserRepository implements AnyUserRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcAnyUserRepository.class);

    private static final String INSERT = """
            INSERT INTO app_user(
                email,
                password,
                type)
            VALUES(?,?,?)
            """;
    private static final String UPDATE = """
            UPDATE app_user
            SET password = ?,
            verified = ?
            WHERE email = ?
            AND active = true
            """;
    private static final String SELECT_ALL_WITH_TYPE= """
            SELECT
                email,
                type
            FROM app_user
            WHERE active = 1
            """;
    private static final String SELECT_TYPE_BY_ID = """
            SELECT type
            FROM app_user
            WHERE email = ?
            AND
            active = 1
            """;

    private static final String SELECT_EXISTS = """
            SELECT COUNT(*) AS total
            FROM app_user
            WHERE email = ?
              AND active = 1
            """;
    private static final String SELECT_EXISTS_DELETED = """
            SELECT COUNT(*) AS total
            FROM app_user
            WHERE email = ?
              AND active = 0
            """;
    private static final String SELECT_AUDIT_INFO = String.format("""
            SELECT
                created_at as %s
                updated_at as %s
            FROM app_user
            WHERE email = ?
            """, UserJdbcMapper.UserColumns.CREATED_AT.columnName(),
            UserJdbcMapper.UserColumns.UPDATED_AT.columnName());
    private static final String DELETE = """
            UPDATE app_user
            SET active = 0
            WHERE email = ?
            AND
            active = 1
            """;


    private final AdminRepository adminRepository;
    private final ManagerRepository managerRepository;
    private final StudentRepository studentRepository;
    private final ConnectionManager connectionManager;

    @Inject
    public JdbcAnyUserRepository(ConnectionManager connectionManager, AdminRepository adminRepository, ManagerRepository managerRepository, StudentRepository studentRepository) {
        this.adminRepository = adminRepository;
        this.managerRepository = managerRepository;
        this.studentRepository = studentRepository;
        this.connectionManager = connectionManager;
    }

    @Override
    public User<?> save(User<?> user) {
        return wrapWithConnection(
                connectionManager,
                LOGGER,
                this::save,
                user
        );
    }

    @Override
    public User<?> save(User<?> user, Connection connection) {
        if(!existsById(user.id())){
            throw new RepositoryException("Use specific repository for creating new user");
        }

        try {
            AuditInfo auditInfo = updateBaseUserInfo(user,connection);
            return user.hydratedWithAuditInfo(auditInfo);
        } catch (SQLException e) {
            LOGGER.error("An error occurred while trying to save user", e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<? extends User<?>> findById(User.Id<?> id) {
        return wrapWithConnection(
                connectionManager,
                LOGGER,
                this::findById,
                id
        );
    }

    @Override
    public Optional<? extends User<?>> findById(User.Id<?> id, Connection connection) {
        return executeQuery(
                SELECT_TYPE_BY_ID,
                ps -> ps.setString(1, id.value()),
                rs -> rs.next() ?  findSpecificUserByEmail(id.value(), rs, connection) : Optional.empty(),
                LOGGER,
                connection
        );
    }

    @Override
    public List<? extends User<?>> findAll() {
        return wrapWithConnection(
                connectionManager,
                LOGGER,
                this::findAll
        );
    }

    @Override
    public List<? extends User<?>> findAll(Connection connection) {
        return executeQuery(
                SELECT_ALL_WITH_TYPE,
                PreparedStatementFiller.NO_OP,
                rs -> {
                    List<User<?>> users = new ArrayList<>();
                    while (rs.next()) {
                        String email = rs.getString("email");
                        Optional<? extends User<?>> retrievedUser = findSpecificUserByEmail(email, rs, connection);
                        retrievedUser.ifPresent(users::add);
                    }
                    return users;
                },
                LOGGER,
                connection
        );
    }

    private @NotNull Optional<? extends User<?>> findSpecificUserByEmail(String email, ResultSet typeResult, Connection connection) throws SQLException {
        UserType type = UserJdbcMapper.extractUserType(typeResult);
        Optional<? extends User<?>> specificUser = switch (type) {
            case ADMIN -> adminRepository.findById(new Admin.Id(email), connection);
            case MANAGER -> managerRepository.findById(new Manager.Id(email), connection);
            case STUDENT -> studentRepository.findById(new Student.Id(email), connection);
        };
        if (specificUser.isEmpty()) {
            LOGGER.warn("{} with email {} is registered on app_user table but not in child table", type.name(), email);
        }
        return specificUser;
    }

    @Override
    public boolean existsById(User.Id<?> id) {
        return wrapWithConnection(
                connectionManager,
                LOGGER,
                this::existsById,
                id
        );
    }

    @Override
    public boolean existsById(User.Id<?> id, Connection connection) {
        return existsById(SELECT_EXISTS, id, connection);
    }

    @Override
    public boolean existsDeletedById(User.Id<?> id) {
        return wrapWithConnection(
                connectionManager,
                LOGGER,
                this::existsDeletedById,
                id
        );
    }

    @Override
    public boolean existsDeletedById(User.Id<?> id, Connection connection) {
        return existsById(SELECT_EXISTS_DELETED, id, connection);
    }

    boolean existsById(String queryVariant, User.@NotNull Id<?> id, Connection connection){
        try(Connection conn = connectionManager.getConnection()){
            PreparedStatement ps = conn.prepareStatement(queryVariant);
            ps.setString(1, id.value());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt("total") > 0;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }


    @Override
    public boolean deleteById(User.Id<?> id) {
        return wrapWithConnection(
                connectionManager,
                LOGGER,
                this::deleteById,
                id
        );
    }


    @Override
    public boolean deleteById(User.Id<?> id, Connection connection) {
        try(PreparedStatement ps = connection.prepareStatement(DELETE)){
            ps.setString(1, id.value());
            int rs = ps.executeUpdate();
            return rs > 0;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(User<?> user) {
        return deleteById(user.id());
    }

    @Override
    public boolean delete(User<?> user, Connection connection) {
        return deleteById(user.id(), connection);
    }

    AuditInfo saveBaseUserInfo( @NotNull User<?> user, Connection conn) throws SQLException {
        try(PreparedStatement ps = conn.prepareStatement(INSERT)) {
            ps.setString(1, user.email().value());
            ps.setString(2, user.password());
            ps.setString(3, UserType.fromDomainEntity(user).dbType());

            boolean updated = ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("Duplicate key on user insert");

        }

        try(PreparedStatement ps = conn.prepareStatement(SELECT_AUDIT_INFO)){
            try(ResultSet rs = ps.executeQuery()){
                return UserJdbcMapper.extractAuditInfo(rs);
            }
        }
    }

    AuditInfo updateBaseUserInfo( @NotNull User<?> user, Connection conn) throws SQLException {
        try(PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, user.password());

            boolean updated = ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("Duplicate key on user insert");
        }

        try(PreparedStatement ps = conn.prepareStatement(SELECT_AUDIT_INFO)){
            try(ResultSet rs = ps.executeQuery()){
                return UserJdbcMapper.extractAuditInfo(rs);
            }
        }
    }
}
