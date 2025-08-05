package dev.builder.usermanagement.infrastructure;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.persistence.RepositoryException;
import dev.builder.usermanagement.domain.model.QuarterGroup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

@Bean
public class JdbcQuarterGroupRepository {

    private static final Logger LOGGER = Logger.getLogger(JdbcQuarterGroupRepository.class.getName());
    private static final String INSERT = """
            INSERT INTO ACADEMIC_GROUP(NAME)
            VALUES (?)
            """;

    private static final String EXISTS = """
            SELECT count(*) AS total
            FROM ACADEMIC_GROUP
            WHERE NAME = ?
            """;

    public boolean exists(QuarterGroup quarterGroup, Connection connection) throws SQLException {
        try(PreparedStatement ps = connection.prepareStatement(EXISTS)){
            ps.setString(1, String.valueOf(quarterGroup.value()));
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getInt("total") > 0;
            } else {
                throw new RepositoryException("There was an error reading Quarter Group EXISTS query");
            }
        }
    }

    public boolean createOrIgnore(QuarterGroup quarterGroup, Connection connection) throws SQLException {
            if (exists(quarterGroup, connection))  return false;

            try (PreparedStatement ps = connection.prepareStatement(INSERT)) {
                ps.setString(1, String.valueOf(quarterGroup.value()));
                return ps.execute();
            }
    }

}
