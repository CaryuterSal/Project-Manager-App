package dev.builder.usermanagement.infrastructure;

import dev.builder.core.domain.AuditInfo;
import dev.builder.core.infrastructure.persistence.RepositoryException;
import dev.builder.core.infrastructure.persistence.SQLFunction;
import dev.builder.usermanagement.domain.model.*;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

import static dev.builder.core.infrastructure.persistence.CommonMappers.groupResultSetByKey;

public class UserJdbcMapper {

    public enum UserColumns {
        EMAIL("email"),
        PASSWORD("password"),
        VERIFIED("verified"),
        CREATED_AT("created_at"),
        UPDATED_AT("updated_at"),
        TYPE("type");

        private final String columnName;

        public String columnName() {
            return columnName;
        }

        UserColumns(String columnName) {
            this.columnName = columnName;
        }
    }

    public enum StudentColumns {
        CREATED_BY("created_by_manager"),
        AS_CREATED("created_student"),
        FIRST_NAME("first_NAME"),
        LAST_NAME("last_NAME"),
        ACADEMIC_GROUP("academic_group"),
        ACADEMIC_QUARTER("academic_quarter");

        private final String columnName;

        public String columnName() {
            return columnName;
        }

        StudentColumns(String columnName) {
            this.columnName = columnName;
        }
    }

    public enum ManagerColumns {
        CREATED_BY("created_by_admin"),
        AS_CREATED("created_manager");

        private final String columnName;

        public String columnName() {
            return columnName;
        }

        ManagerColumns(String columnName) {
            this.columnName = columnName;
        }
    }

    private record  CommonUserInfo(String email, String password, boolean verified, AuditInfo auditInfo){

        private static CommonUserInfo fromResultSet(ResultSet rs) throws SQLException {
            String email = rs.getString(UserColumns.EMAIL.columnName);
            String password = rs.getString(UserColumns.PASSWORD.columnName);
            boolean  verified = rs.getBoolean(UserColumns.VERIFIED.columnName);
            LocalDateTime createdAt = rs.getTimestamp(UserColumns.CREATED_AT.columnName).toLocalDateTime();
            LocalDateTime updatedAt = rs.getTimestamp(UserColumns.UPDATED_AT.columnName).toLocalDateTime();
            return new CommonUserInfo(email, password, verified, new AuditInfo(createdAt, updatedAt));
        }
    }
    public static List<Admin> rowToAdmins(ResultSet rs) throws SQLException {
        List<Admin> admins = new ArrayList<>();
        Map<Admin.Id, Set<Manager.Id>> managersCreated = extractManagersCreatedByAdmin(rs);
        do{
            CommonUserInfo baseInfo = CommonUserInfo.fromResultSet(rs);
            Admin.Id id = new Admin.Id(baseInfo.email());
            admins.add(new Admin(
                    id,
                    baseInfo.password(),
                    baseInfo.verified(),
                    managersCreated.getOrDefault(id, new HashSet<>())
            ).hydratedWithAuditInfo(baseInfo.auditInfo()));
        } while (rs.next());
        return admins;
    }

    public static @NotNull Admin rowToAdmin(@NotNull ResultSet rs) throws SQLException {
        CommonUserInfo baseInfo = CommonUserInfo.fromResultSet(rs);
        Admin.Id id = new Admin.Id(baseInfo.email());
        Set<Manager.Id> createdManagers = extractManagersCreated(id, rs);
        return new Admin(
                id,
                baseInfo.password(),
                baseInfo.verified(),
                createdManagers
        ).hydratedWithAuditInfo(baseInfo.auditInfo());
    }

    public static List<Manager> rowToManagers(ResultSet rs) throws SQLException {
        List<Manager> managers = new ArrayList<>();
        Map<Manager.Id, Set<Student.Id>> managersCreated = extractStudentsCreatedByManager(rs);
        do{
            CommonUserInfo baseInfo = CommonUserInfo.fromResultSet(rs);
            Manager.Id id = new Manager.Id(baseInfo.email());
            Admin.Id creator = new Admin.Id(rs.getString(ManagerColumns.CREATED_BY.columnName));
            managers.add(new Manager(
                    creator,
                    id,
                    baseInfo.password(),
                    baseInfo.verified(),
                    managersCreated.getOrDefault(id, new HashSet<>())
            ).hydratedWithAuditInfo(baseInfo.auditInfo()));
        } while (rs.next());
        return managers;
    }

    public static @NotNull Manager rowToManager(@NotNull ResultSet rs) throws SQLException {
        CommonUserInfo baseInfo = CommonUserInfo.fromResultSet(rs);
        Manager.Id id = new Manager.Id(baseInfo.email());
        Admin.Id creator = new Admin.Id(rs.getString(ManagerColumns.CREATED_BY.columnName));
        Set<Student.Id> createdStudents = extractStudentsCreated(id, rs);
        return new Manager(
                creator,
                id,
                baseInfo.password(),
                baseInfo.verified(),
                createdStudents
        ).hydratedWithAuditInfo(baseInfo.auditInfo());
    }

    public static List<Student> rowToStudents(ResultSet rs) throws SQLException {
        List<Student> students = new ArrayList<>();
        do{
            CommonUserInfo baseInfo = CommonUserInfo.fromResultSet(rs);
            Student.Id id = new Student.Id(baseInfo.email());
            Manager.Id creator = new Manager.Id(rs.getString(StudentColumns.CREATED_BY.columnName));
            Name name = new Name(
                    rs.getString(StudentColumns.FIRST_NAME.columnName),
                    rs.getString(StudentColumns.LAST_NAME.columnName)
            );
            AcademicInfo academicInfo = new AcademicInfo(
                    new AcademicQuarter(rs.getInt(StudentColumns.ACADEMIC_QUARTER.columnName)),
                    new QuarterGroup(rs.getString(StudentColumns.ACADEMIC_GROUP.columnName).charAt(0))
            );
            students.add(new Student(
                    creator,
                    id,
                    baseInfo.password(),
                    name,
                    academicInfo,
                    baseInfo.verified()
            ).hydratedWithAuditInfo(baseInfo.auditInfo()));
        } while (rs.next());
        return students;
    }

    public static @NotNull Student rowToStudent(@NotNull ResultSet rs) throws SQLException {
        CommonUserInfo baseInfo = CommonUserInfo.fromResultSet(rs);
        Student.Id id = new Student.Id(baseInfo.email());
        Manager.Id creator = new Manager.Id(rs.getString(StudentColumns.CREATED_BY.columnName));
        Name name = new Name(
                rs.getString(StudentColumns.FIRST_NAME.columnName),
                rs.getString(StudentColumns.LAST_NAME.columnName)
        );
        AcademicInfo academicInfo = new AcademicInfo(
                new AcademicQuarter(rs.getInt(StudentColumns.ACADEMIC_QUARTER.columnName)),
                new QuarterGroup(rs.getString(StudentColumns.ACADEMIC_GROUP.columnName).charAt(0))
        );
        return new Student(
                creator,
                id,
                baseInfo.password(),
                name,
                academicInfo,
                baseInfo.verified()
        ).hydratedWithAuditInfo(baseInfo.auditInfo());
    }

    public static UserType extractUserType(@NotNull ResultSet rs) throws SQLException {
        return UserType.fromDbType(rs.getString(UserColumns.TYPE.columnName));
    }


    public static @NotNull AuditInfo extractAuditInfo(@NotNull ResultSet rs) throws SQLException {
        if(!rs.next()) throw new RepositoryException("There was an error extracting audit info for user");
        OffsetDateTime cOdt = rs.getObject(UserColumns.CREATED_AT.columnName, OffsetDateTime.class);
        LocalDateTime createdAt = cOdt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        OffsetDateTime uOdt = rs.getObject(UserColumns.UPDATED_AT.columnName, OffsetDateTime.class);
        LocalDateTime updatedAt = uOdt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        return new AuditInfo(createdAt,updatedAt);
    }

    private static @NotNull Set<Manager.Id> extractManagersCreated(Admin.Id creator, @NotNull ResultSet rs) throws SQLException {
        return extractManagersCreatedByAdmin(rs).getOrDefault(creator, Collections.emptySet());
    }

    private static @NotNull Set<Student.Id> extractStudentsCreated(Manager.Id creator, @NotNull ResultSet rs) throws SQLException {
        return extractStudentsCreatedByManager(rs).getOrDefault(creator, Collections.emptySet());
    }


    private static @NotNull Map<Admin.Id, Set<Manager.Id>> extractManagersCreatedByAdmin(@NotNull ResultSet rs) throws SQLException {
        return groupResultSetByKey(
                rs,
                r -> new Admin.Id(extractEmail(r)),
                r -> {
                    String email = r.getString(ManagerColumns.AS_CREATED.columnName);
                    if(email == null) return null;
                    return new Manager.Id(email);
                }
        );
    }

    private static @NotNull Map<Manager.Id, Set<Student.Id>> extractStudentsCreatedByManager(@NotNull ResultSet rs) throws SQLException {
        return groupResultSetByKey(
                rs,
                r -> new Manager.Id(extractEmail(r)),
                r -> {
                    String email = r.getString(StudentColumns.AS_CREATED.columnName);
                    if(email == null) return null;
                    return new Student.Id(email);
                }
        );
    }






    private static String extractEmail(@NotNull ResultSet rs) throws SQLException {
        return rs.getString(UserColumns.EMAIL.columnName);
    }
}
