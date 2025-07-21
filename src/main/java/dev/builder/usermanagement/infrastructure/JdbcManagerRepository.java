package dev.builder.usermanagement.infrastructure;

import dev.builder.core.domain.AuditInfo;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.persistence.ConnectionManager;
import dev.builder.core.infrastructure.persistence.RepositoryException;
import dev.builder.usermanagement.domain.model.Manager;
import dev.builder.usermanagement.domain.port.out.AnyUserRepository;
import dev.builder.usermanagement.domain.port.out.ManagerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Bean
public class JdbcManagerRepository implements ManagerRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcManagerRepository.class);

    private final JdbcAnyUserRepository anyUserRepository;

    @Inject
    public JdbcManagerRepository(JdbcAnyUserRepository anyUserRepository) {
        this.anyUserRepository = anyUserRepository;
    }

    private static final String SELECT_ALL = String.format("""
            SELECT
                u.*,
                s.email as %s,
                m.created_by as %s
            FROM manager m
            JOIN app_user u ON u.email = m.email
            JOIN student s ON s.created_by = u.email
            WHERE u.active = 1
            """,
            UserJdbcMapper.StudentColumns.AS_CREATED,
            UserJdbcMapper.ManagerColumns.CREATED_BY);
    private static final String SELECT_BY_ID = String.format("""
            SELECT
                u.*,
                s.email as %s,
                m.created_by as %s
            FROM manager m
            JOIN app_user u ON u.email = m.email
            JOIN student s ON s.created_by = u.email
            WHERE u.email = ?
            AND
            u.active = 1
            """,
            UserJdbcMapper.StudentColumns.AS_CREATED,
            UserJdbcMapper.ManagerColumns.CREATED_BY);

    private static final String INSERT = """
            INSERT INTO manager(
                email,
                created_by)
            VALUES (?, ?)
            """;

    @Override
    public Optional<Manager> findById(Manager.Id id) {
        try(Connection conn = ConnectionManager.getConnection()){
            return findById(id, conn);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Manager> findById(Manager.Id id, Connection connection) {
        try(PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID)){
            ps.setString(1, id.value());

            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(UserJdbcMapper.rowToManager(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    @Override
    public List<Manager> findAll() {
        try(Connection conn = ConnectionManager.getConnection()){
            return findAll(conn);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    @Override
    public List<Manager> findAll(Connection connection) {
        List<Manager> managers = new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID)){

            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                managers.add(UserJdbcMapper.rowToManager(rs));
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
        return managers;
    }

    @Override
    public boolean delete(Manager manager) {
        return deleteById(manager.id());
    }

    @Override
    public boolean delete(Manager aggregateRoot, Connection connection) {
        return deleteById(aggregateRoot.id(), connection);
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
    public Manager save(Manager manager) {
        try(Connection conn = ConnectionManager.getConnection()){
            return save(manager, conn);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    @Override
    public Manager save(Manager manager, Connection connection) {
        try(PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID)){
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

    @Override
    public boolean existsById(Manager.Id id) {
        return anyUserRepository.existsById(id);
    }

    @Override
    public boolean existsById(Manager.Id id, Connection connection) {
        return anyUserRepository.existsById(id,connection);
    }
}
