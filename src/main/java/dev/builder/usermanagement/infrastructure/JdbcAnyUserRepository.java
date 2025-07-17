package dev.builder.usermanagement.infrastructure;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.persistence.ConnectionManager;
import dev.builder.usermanagement.domain.model.User;
import dev.builder.usermanagement.domain.port.out.AnyUserRepository;
import org.apache.poi.ss.formula.functions.T;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Bean
public class JdbcAnyUserRepository<ID extends User.Id> implements AnyUserRepository {

    private static final String SELECT_ALL = """
            SELECT * FROM app_user
            """;
    private static final String SELECT_BY_ID = """
            SELECT u.* FROM app_user u WHERE email = ?
            """;
    private static final String DELETE = """
            UPDATE app_user SET active = false WHERE id = ?
            """;

    private static final String SELECT_ADMIN_BY_ID = """
            SELECT u.*, a.email as""" + UserJdbcMapper.AdminColumns.CREATED_MANAGER + """
            FROM app_user u
            JOIN admin as a ON a.id = u.id WHERE a.id = ?
            """;
    private static final String SELECT_MANAGER_BY_ID = """
            SELECT u.*, m.email as""" + UserJdbcMapper.ManagerColumns.CREATED_STUDENT + """
            FROM app_user u
            JOIN manager as m ON m.id = u.id WHERE m.id = ?
            """;

    @Override
    public Optional<? extends User<?>> findById(User.Id id) {
        try(Connection conn = ConnectionManager.getConnection()){
            PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID);
            ps.setString(1, id.value());

            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                UserType userType = UserJdbcMapper.extractUserType(rs);
                if(userType.equals(UserType.STUDENT)) return Optional.of(UserJdbcMapper.rowToUser(rs));
                return findSpecificUser(conn, id, userType);
            }
            return Optional.empty();
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<? extends User<?>> findAll() {
        return List.of();
    }

    private Optional<? extends User<?>> findSpecificUser(Connection conn, User.Id id, @NotNull UserType type) throws SQLException {
        String script = switch (type){
            case MANAGER -> SELECT_MANAGER_BY_ID;
            case ADMIN -> SELECT_ADMIN_BY_ID;
            default -> SELECT_BY_ID;
        };
        PreparedStatement ps = conn.prepareStatement(script);
        ps.setString(1, id.value());
        ResultSet rs = ps.executeQuery();
        if(rs.next()){
            return Optional.of(UserJdbcMapper.rowToUser(rs));
        }
        return Optional.empty();
    }

    @Override
    public boolean existsById(User.Id id) {
        return false;
    }

    @Override
    public void deleteById(User.Id id) {

    }

    @Override
    public void delete(User<?> user) {

    }


}
