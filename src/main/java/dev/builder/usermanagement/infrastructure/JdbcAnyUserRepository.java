package dev.builder.usermanagement.infrastructure;

import dev.builder.core.domain.AuditInfo;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.persistence.ConnectionManager;
import dev.builder.core.infrastructure.persistence.RepositoryException;
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
import java.util.*;

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
    public Optional<? extends User<?>> findById(User.Id id) {
        try(Connection conn = connectionManager.getConnection()){
            return findById(id, conn);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(),e);
            throw new RepositoryException(e.getMessage(),e);
        }
    }

    @Override
    public Optional<? extends User<?>> findById(User.Id id, Connection connection) {
        try(PreparedStatement ps = connection.prepareStatement(SELECT_TYPE_BY_ID)) {
            ps.setString(1, id.value());

            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserType type = UserJdbcMapper.extractUserType(rs);
                    Optional<? extends User<?>> specificUser = switch (type) {
                        case ADMIN -> adminRepository.findById(new Admin.Id(id.value()));
                        case MANAGER -> managerRepository.findById(new Manager.Id(id.value()));
                        case STUDENT -> studentRepository.findById(new Student.Id(id.value()));
                    };
                    if (specificUser.isEmpty()) {
                        LOGGER.warn("{} with email {} is registered on app_user table but not in child table", type.name(), id.value());
                    }
                    return specificUser;
                }
            }
            return Optional.empty();
        } catch (SQLException e){
            LOGGER.error(e.getMessage(),e);
            throw new RepositoryException(e.getMessage(),e);
        }
    }

    @Override
    public List<? extends User<?>> findAll() {
        try(Connection conn = connectionManager.getConnection()){
            return findAll(conn);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(),e);
            throw new RepositoryException(e.getMessage(),e);
        }
    }

    @Override
    public List<? extends User<?>> findAll(Connection connection) {
        Set<User<?>> users = new HashSet<>();
        try( PreparedStatement ps = connection.prepareStatement(SELECT_ALL_WITH_TYPE)){

            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String email = rs.getString("email");
                    UserType type = UserJdbcMapper.extractUserType(rs);
                    Optional<? extends User<?>> retrievedUser = switch (type) {
                        case ADMIN -> adminRepository.findById(new Admin.Id(email));
                        case MANAGER -> managerRepository.findById(new Manager.Id(email));
                        case STUDENT -> studentRepository.findById(new Student.Id(email));
                    };
                    if (retrievedUser.isPresent()) {
                        users.add(retrievedUser.get());
                    } else {
                        LOGGER.warn("{} with email {} is registered on app_user table but not in child table", type.name(), email);
                    }
                }
                return new ArrayList<>(users);
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(User.Id id) {
        return existsById(SELECT_EXISTS, id);
    }

    @Override
    public boolean existsById(User.Id id, Connection connection) {
        return existsById(SELECT_EXISTS, id, connection);
    }

    @Override
    public boolean existsDeletedById(User.Id id) {
        return existsById(SELECT_EXISTS_DELETED, id);
    }

    @Override
    public boolean existsDeletedById(User.Id id, Connection connection) {
        return existsById(SELECT_EXISTS_DELETED, id, connection);
    }

    boolean existsById(String queryVariant, User.Id id){
        try(Connection conn = connectionManager.getConnection()){
            return existsById(queryVariant, id, conn);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(),e);
            throw new RepositoryException(e.getMessage(),e);
        }
    }

    boolean existsById(String queryVariant, User.@NotNull Id id, Connection connection){
        try(Connection conn = connectionManager.getConnection()){
            PreparedStatement ps = conn.prepareStatement(queryVariant);
            ps.setString(1, id.value());
            ResultSet rs = ps.executeQuery();
            return rs.getInt("total") > 0;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }


    @Override
    public boolean deleteById(User.Id id) {
        try(Connection conn = connectionManager.getConnection()){
           return deleteById(id, conn);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(),e);
            throw new RepositoryException(e.getMessage(),e);
        }
    }

    @Override
    public boolean deleteById(User.Id id, Connection connection) {
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
        try(PreparedStatement ps = conn.prepareStatement(INSERT, new String[]{"created_at", "updated_at"})) {
            ps.setString(1, user.email().value());
            ps.setString(2, user.password());
            ps.setString(3, UserType.fromDomainEntity(user).dbType());

            boolean updated = ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("Duplicate key on user insert");

            try(ResultSet rs = ps.getGeneratedKeys()){
                return UserJdbcMapper.extractAuditInfo(rs);
            }
        }
    }

    AuditInfo updateBaseUserInfo( @NotNull User<?> user, Connection conn) throws SQLException {
        try(PreparedStatement ps = conn.prepareStatement(UPDATE, new String[]{"created_at", "updated_at"})) {
            ps.setString(1, user.password());

            boolean updated = ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("Duplicate key on user insert");
            try(ResultSet rs = ps.getGeneratedKeys()){
                return UserJdbcMapper.extractAuditInfo(rs);
            }
        }
    }
}
