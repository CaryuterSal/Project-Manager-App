package dev.builder.usermanagement.infrastructure;

import dev.builder.board.domain.model.Board;
import dev.builder.board.domain.model.Task;
import dev.builder.core.domain.AuditInfo;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.di.annotation.Lazy;
import dev.builder.core.infrastructure.persistence.*;
import dev.builder.usermanagement.domain.model.Manager;
import dev.builder.usermanagement.domain.model.Student;
import dev.builder.usermanagement.domain.port.out.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.*;

@Bean
public class JdbcStudentRepository extends TransactionalJdbcCrudRepository<Student, Student.Id> implements StudentRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcStudentRepository.class);

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    private static final String SELECT_ALL = String.format("""
            SELECT
                u.*,
                s.first_name,
                s.last_name,
                s.qgp_agp_name as %s,
                s.qgp_aqr_number as %s,
                s.created_by as %s
            FROM student s
            JOIN app_user u ON u.email = s.email AND u.active = 1
            WHERE u.active = 1
            """,
            UserJdbcMapper.StudentColumns.ACADEMIC_GROUP.columnName(),
            UserJdbcMapper.StudentColumns.ACADEMIC_QUARTER.columnName(),
            UserJdbcMapper.StudentColumns.CREATED_BY.columnName());
    private static final String SELECT_BY_ID = String.format("""
            SELECT
                u.*,
                s.first_name,
                s.last_name,
                s.qgp_agp_name as %s,
                s.qgp_aqr_number as %s,
                s.created_by as %s
            FROM student s
            JOIN app_user u ON u.email = s.email AND u.active = 1
            WHERE u.email = ?
            AND
            u.active = 1
            """,
            UserJdbcMapper.StudentColumns.ACADEMIC_GROUP.columnName(),
            UserJdbcMapper.StudentColumns.ACADEMIC_QUARTER.columnName(),
            UserJdbcMapper.StudentColumns.CREATED_BY.columnName());


    private static final String SELECT_BY_CREATOR = String.format("""
           
            SELECT
                u.*,
                s.first_name,
                s.last_name,
                s.qgp_agp_name as %s,
                s.qgp_aqr_number as %s,
                s.created_by as %s
            FROM manager m
            JOIN app_user mu ON mu.email = m.email AND mu.active = 1
             JOIN student s ON s.created_by = m.email
            JOIN app_user u ON u.email = s.email AND u.active = 1
            WHERE a.email = ?
            """,
            UserJdbcMapper.StudentColumns.ACADEMIC_GROUP.columnName(),
            UserJdbcMapper.StudentColumns.ACADEMIC_QUARTER.columnName(),
            UserJdbcMapper.StudentColumns.CREATED_BY.columnName());


    private static final String INSERT = """
            INSERT INTO student(
                email,
                FIRST_NAME,
                LAST_NAME,
                created_by,
                QGP_AGP_NAME,
                QGP_AQR_NUMBER)
            VALUES (?, ?,?,?,?,?)
            """;

    private static final String UPDATE = """
            UPDATE STUDENT
            SET FIRST_NAME = ?,
                LAST_NAME = ?,
                QGP_AGP_NAME = ?,
                QGP_AQR_NUMBER = ?
            WHERE EMAIL = ?
            """;

    private static final String EXISTS_BY_ID = """
            SELECT count(*) AS total
            FROM STUDENT s
            JOIN APP_USER u ON u.email = s.email AND u.active = 1
            WHERE s.email = ?
            """;

    private static final String EXISTS_DELETED_BY_ID = """
            SELECT count(*) AS total
            FROM STUDENT s
            JOIN APP_USER u ON u.email = s.email AND u.active = 1
            WHERE s.email = ?
            """;

    private static String SELECT_ALL_BY_TASK = String.format("""
           
            SELECT
                u.*,
                s.first_name,
                s.last_name,
                s.qgp_agp_name as %s,
                s.qgp_aqr_number as %s,
                s.created_by as %s
            FROM task t
            JOIN task_assignee tae ON tae.tsk_id = t.id
            JOIN manager m ON tae.sbd_bad_email = m.email
            JOIN app_user mu ON mu.email = m.email AND mu.active = 1
            JOIN student s ON s.email = tae.sbd_sdt_email
            JOIN app_user u ON u.email = s.email AND u.active = 1
            WHERE t.id = ?
            """,
            UserJdbcMapper.StudentColumns.ACADEMIC_GROUP.columnName(),
            UserJdbcMapper.StudentColumns.ACADEMIC_QUARTER.columnName(),
            UserJdbcMapper.StudentColumns.CREATED_BY.columnName());

    private static String SELECT_ALL_BY_BOARD= String.format("""
           
            SELECT
                u.*,
                s.first_name,
                s.last_name,
                s.qgp_agp_name as %s,
                s.qgp_aqr_number as %s,
                s.created_by as %s
            FROM board b
            JOIN manager m ON b.mnr_email = m.email
            JOIN app_user mu ON mu.email = m.email AND mu.active = 1
            JOIN student_board sbd ON sbd.bad_email = mu.email
            JOIN student s ON s.email = sbd.sdt_email
            JOIN app_user u ON u.email = s.email AND u.active = 1
            WHERE b.mnr_email = ?
            """,
            UserJdbcMapper.StudentColumns.ACADEMIC_GROUP.columnName(),
            UserJdbcMapper.StudentColumns.ACADEMIC_QUARTER.columnName(),
            UserJdbcMapper.StudentColumns.CREATED_BY.columnName());

    private JdbcAnyUserRepository anyUserRepository;

    @Inject
    @Lazy
    public void setAnyUserRepository(JdbcAnyUserRepository anyUserRepository) {
        this.anyUserRepository = anyUserRepository;
    }

    private final JdbcAcademicInfoRepository academicInfoRepository;

    @Inject
    public JdbcStudentRepository(ConnectionManager connectionManager, JdbcAcademicInfoRepository academicInfoRepository) {
        super(connectionManager);
        this.academicInfoRepository = academicInfoRepository;
    }

    @Override
    public Optional<Student> findById(Student.Id id, Connection connection) {
        return executeQuery(
                SELECT_BY_ID,
                ps -> ps.setString(1, id.value()),
                rs -> rs.next() ? Optional.of(UserJdbcMapper.rowToStudent(rs)) : Optional.empty(),
                LOGGER,
                connection
        );
    }

    @Override
    public List<Student> findAll(Connection connection) {
        return executeQuery(
                SELECT_ALL,
                PreparedStatementFiller.NO_OP,
                rs -> rs.next() ? UserJdbcMapper.rowToStudents(rs) : Collections.emptyList(),
                LOGGER,
                connection
        );
    }

    @Override
    public List<Student> findByCreatedBy(Manager.Id id) {
        return wrapWithConnection(
                connectionManager,
                LOGGER,
                this::findByCreatedBy,
                id
        );
    }

    @Override
    public List<Student> findByCreatedBy(Manager.Id id, Connection connection) {
        return executeQuery(
                SELECT_BY_CREATOR,
                ps -> ps.setString(1, id.value()),
                rs -> rs.next() ? UserJdbcMapper.rowToStudents(rs) : Collections.emptyList(),
                LOGGER,
                connection
        );
    }

    @Override
    public Set<Student> findAssignedToTask(Task.Id id) {
        return wrapWithConnection(
                connectionManager,
                LOGGER,
                this::findAssignedToTask,
                id
        );
    }

    @Override
    public Set<Student> findAssignedToTask(Task.Id id, Connection connection) {
        return executeQuery(
                SELECT_ALL_BY_TASK,
                ps -> ps.setBytes(1, UUIDMapper.UUIDtoByteArray(id.value())),
                rs -> rs.next() ? new HashSet<>(UserJdbcMapper.rowToStudents(rs)) : new HashSet<>(),
                LOGGER,
                connection
        );
    }

    @Override
    public Set<Student> findCollaboratingOnBoard(Board.Id id) {
        return wrapWithConnection(
                connectionManager,
                LOGGER,
                this::findCollaboratingOnBoard,
                id
        );
    }

    @Override
    public Set<Student> findCollaboratingOnBoard(Board.Id id, Connection connection) {
        return executeQuery(
                SELECT_ALL_BY_BOARD,
                ps -> ps.setString(1, id.userId().value()),
                rs -> rs.next() ? new HashSet<>(UserJdbcMapper.rowToStudents(rs)) : new HashSet<>(),
                LOGGER,
                connection
        );
    }

    @Override
    public boolean deleteById(Student.Id id, Connection connection) {
        return anyUserRepository.deleteById(id, connection);
    }

    @Override
    public Student save(Student student) {
        return runInTransaction(
                connectionManager,
                LOGGER,
                this::save,
                student
        );
    }

    @Override
    public Student save(Student student, Connection connection) {
        if (existsById(student.id(), connection)) {
            return update(student, connection);
        } else {
            return create(student, connection);
        }
    }

    private Student update(Student student, Connection connection) {
        try(PreparedStatement ps = connection.prepareStatement(UPDATE)){
            academicInfoRepository.createOrIgnore(student.academicInfo(), connection);
            AuditInfo auditInfo = anyUserRepository.updateBaseUserInfo(student, connection);
            ps.setString(1, student.name().firstName());
            ps.setString(2, student.name().lastName());
            ps.setString(3, student.createdBy().value());
            ps.setString(4, String.valueOf(student.academicInfo().group().value()));
            ps.setInt(5, student.academicInfo().quarter().number());
            ps.setString(6, student.email().value());
            if(ps.executeUpdate() <= 0){
                throw new RepositoryException(String.format("Error updating student with name: %s", student.name()));
            }
            return student.hydratedWithAuditInfo(auditInfo);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    private Student create(Student student, Connection connection){
        try(PreparedStatement ps = connection.prepareStatement(INSERT)){
            academicInfoRepository.createOrIgnore(student.academicInfo(), connection);
            AuditInfo auditInfo = anyUserRepository.saveBaseUserInfo(student, connection);
            ps.setString(1, student.email().value());
            ps.setString(2, student.name().firstName());
            ps.setString(3, student.name().lastName());
            ps.setString(4, student.createdBy().value());
            ps.setString(5, String.valueOf(student.academicInfo().group().value()));
            ps.setInt(6, student.academicInfo().quarter().number());

            boolean updated = ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("Duplicate key on user insert");
            return student.hydratedWithAuditInfo(auditInfo);
        } catch (SQLException e){
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(Student.Id id, Connection connection) {
        return existsById(EXISTS_BY_ID, rs -> rs.setString(1, id.value()), id, connection);
    }

    @Override
    public boolean existsDeletedById(Student.Id id) {
        return wrapWithConnection(
                connectionManager,
                LOGGER,
                this::existsDeletedById,
                id
        );
    }

    @Override
    public boolean existsDeletedById(Student.Id id, Connection connection) {
        return existsById(EXISTS_DELETED_BY_ID, rs -> rs.setString(1, id.value()), id, connection);
    }
}
