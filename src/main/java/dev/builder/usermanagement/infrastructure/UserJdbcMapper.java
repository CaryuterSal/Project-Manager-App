package dev.builder.usermanagement.infrastructure;

import dev.builder.usermanagement.domain.model.*;
import org.apache.poi.ss.formula.functions.Na;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserJdbcMapper {

    private enum UserColumns {
        EMAIL("email"),
        PASSWORD("password"),
        VERIFIED("verified"),
        CREATED_AT("created_at"),
        UPDATED_AT("updated_at"),
        TYPE("type");

        private final String columnName;

        UserColumns(String columnName) {
            this.columnName = columnName;
        }
    }

    public enum AdminColumns {
        CREATED_MANAGER("created");

        private final String columnName;

        AdminColumns(String columnName) {
            this.columnName = columnName;
        }
    }

    public enum StudentColumns {
        FIRST_NAME("first_NAME"),
        LAST_NAME("last_NAME"),
        CREATED_BY("created_by"),
        ACADEMIC_GROUP("group"),
        ACADEMIC_QUARTER("quarter");

        private final String columnName;

        StudentColumns(String columnName) {
            this.columnName = columnName;
        }
    }

    public enum ManagerColumns {
        CREATED_BY("created_by"),
        CREATED_STUDENT("created");

        private final String columnName;

        ManagerColumns(String columnName) {
            this.columnName = columnName;
        }
    }



    public static @NotNull User<?> rowToUser(@NotNull ResultSet rs) throws SQLException {
        String email = rs.getString(UserColumns.EMAIL.columnName);
        String password = rs.getString(UserColumns.PASSWORD.columnName);
        boolean  verified = rs.getBoolean(UserColumns.VERIFIED.columnName);
        LocalDateTime createdAt = rs.getTimestamp(UserColumns.CREATED_AT.columnName).toLocalDateTime();
        LocalDateTime updatedAt = rs.getTimestamp(UserColumns.UPDATED_AT.columnName).toLocalDateTime();
        UserType userType = extractUserType(rs);
        User<?> user = switch (userType) {
            case ADMIN: new Admin(
                    new Admin.Id(email),
                    password,
                    verified,
                    extractToManagersCreated(rs));
            case MANAGER: {
                String ownerEmail = rs.getString(ManagerColumns.CREATED_BY.columnName);
                yield new Manager(
                        new Admin.Id(ownerEmail),
                        new Manager.Id(email),
                        password,
                        verified,
                        extractStudentsCreated(rs));
            }
            case STUDENT:{
                String ownerEmail = rs.getString(StudentColumns.CREATED_BY.columnName);
                String firstName =  rs.getString(StudentColumns.FIRST_NAME.columnName);
                String lastName =  rs.getString(StudentColumns.LAST_NAME.columnName);
                String academicGroup = rs.getString(StudentColumns.ACADEMIC_GROUP.columnName);
                int academicQuarter = rs.getInt(StudentColumns.ACADEMIC_QUARTER.columnName);
                yield new Student(
                        new Manager.Id(ownerEmail),
                        new Student.Id(email),
                        password,
                        new Name(firstName, lastName),
                        new AcademicInfo(new AcademicQuarter(academicQuarter), new QuarterGroup(academicGroup.charAt(0))),
                        verified);
            }
        };
        user.hydrateAuditInfo(createdAt, updatedAt);
        return user;
    }

    public static UserType extractUserType(@NotNull ResultSet rs) throws SQLException {
        return UserType.valueOf(rs.getString(UserColumns.TYPE.columnName));
    }


    private static @NotNull List<Manager.Id> extractToManagersCreated(@NotNull ResultSet rs) throws SQLException {
        List<Manager.Id> managersIds = new ArrayList<>();
        do{
            String email = rs.getString(AdminColumns.CREATED_MANAGER.columnName);
            managersIds.add(new Manager.Id(email));
        }while (rs.next());
        return managersIds;
    }

    private static @NotNull List<Student.Id> extractStudentsCreated(@NotNull ResultSet rs) throws SQLException {
        List<Student.Id> studentsIds = new ArrayList<>();
        do{
            String email = rs.getString(ManagerColumns.CREATED_STUDENT.columnName);
            studentsIds.add(new Student.Id(email));
        }while (rs.next());
        return studentsIds;
    }
}
