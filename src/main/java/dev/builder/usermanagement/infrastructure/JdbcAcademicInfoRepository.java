package dev.builder.usermanagement.infrastructure;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.persistence.RepositoryException;
import dev.builder.usermanagement.domain.model.AcademicInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

@Bean
public class JdbcAcademicInfoRepository {


    private final JdbcAcademicQuarterRepository quarterRepository;
    private final JdbcQuarterGroupRepository groupRepository;

    @Inject
    public JdbcAcademicInfoRepository(JdbcAcademicQuarterRepository quarterRepository, JdbcQuarterGroupRepository groupRepository) {
        this.quarterRepository = quarterRepository;
        this.groupRepository = groupRepository;
    }

    private static final Logger LOGGER = Logger.getLogger(JdbcAcademicInfoRepository.class.getName());
    private static final String INSERT = """
            INSERT INTO QUARTER_GROUP(AGP_NAME, AQR_NUMBER)
            VALUES (?, ?)
            """;

    private static final String EXISTS = """
            SELECT count(*) AS total
            FROM QUARTER_GROUP
            WHERE AGP_NAME = ?
            AND AQR_NUMBER = ?
            """;

    public boolean exists(AcademicInfo academicInfo, Connection connection) throws SQLException {
        try(PreparedStatement ps = connection.prepareStatement(EXISTS)){
            ps.setString(1, String.valueOf(academicInfo.group().value()));
            ps.setInt(2, academicInfo.quarter().number());
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getInt("total") > 0;
            } else {
                throw new RepositoryException("There was an error reading Academic Info EXISTS query");
            }
        }
    }

    public boolean createOrIgnore(AcademicInfo academicInfo, Connection connection) throws SQLException {

            if (exists(academicInfo, connection)) return false;
            quarterRepository.createOrIgnore(academicInfo.quarter(), connection);
            groupRepository.createOrIgnore(academicInfo.group(), connection);

            try (PreparedStatement ps = connection.prepareStatement(INSERT)) {
                ps.setString(1, String.valueOf(academicInfo.group().value()));
                ps.setInt(2, academicInfo.quarter().number());
                return ps.execute();
            }
    }

}
