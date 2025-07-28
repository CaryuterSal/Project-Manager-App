package dev.builder.usermanagement.infrastructure;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.persistence.RepositoryException;
import dev.builder.usermanagement.domain.model.AcademicQuarter;
import dev.builder.usermanagement.domain.model.QuarterGroup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

@Bean
public class JdbcAcademicQuarterRepository {


    private static final Logger LOGGER = Logger.getLogger(JdbcAcademicQuarterRepository.class.getName());
    private static final String INSERT = """
            INSERT INTO ACADEMIC_QUARTER("number")
            VALUES (?)
            """;

    private static final String EXISTS = """
            SELECT count(*) AS total
            FROM ACADEMIC_QUARTER
            WHERE "number" = ?;
            """;

    public boolean exists(AcademicQuarter academicQuarter, Connection connection) throws SQLException {
        try(PreparedStatement ps = connection.prepareStatement(EXISTS)){
            ps.setInt(1, academicQuarter.number());
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getInt("total") > 0;
            } else {
                throw new RepositoryException("There was an error reading Academic Quarter EXISTS query");
            }
        }
    }

    public boolean createOrIgnore(AcademicQuarter academicQuarter, Connection connection) throws SQLException {
            if (exists(academicQuarter, connection)) return false;
            try (PreparedStatement ps = connection.prepareStatement(INSERT)) {
                ps.setString(1, String.valueOf(academicQuarter.number()));
                return ps.execute();
            }
    }

}
