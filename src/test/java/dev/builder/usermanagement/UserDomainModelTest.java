package dev.builder.usermanagement;

import dev.builder.core.domain.AuditInfo;
import dev.builder.usermanagement.domain.model.*;
import dev.builder.usermanagement.domain.port.out.PasswordEncoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class UserDomainModelTest {

    @ParameterizedTest
    @ValueSource(strings = {"fsdf", "", "    ", "fsdf@fds@", "semi@com", "user..dow@example.com", "user.doe.@example.com"})
    void test_invalid_user_id_format_throws(String email){
        assertAll(
                () -> assertThrowsExactly(IllegalArgumentException.class, () -> new Admin.Id(email)),
                () -> assertThrowsExactly(IllegalArgumentException.class, () -> new Manager.Id(email)),
                () -> assertThrowsExactly(IllegalArgumentException.class, () -> new Student.Id(email))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcdefg         ", "ABCfds", "12342     ", "@@@$$$", "aBBBBB", "   aB3B45", "abfdf@", "ABcsdd"})
    void test_password_is_invalid(String password){
        assertThrowsExactly(IllegalArgumentException.class, () -> new Password(password));
    }

    @Test
    void test_password_is_trimmed(){
        Password password = new Password("   12345FD#   ");
        assertThat(password.value()).isEqualTo(password.value().trim());
    }

    @ParameterizedTest
    @ValueSource(strings = {"aBBBBBB", "ab###@@", "     AbcdefG"})
    void test_password_is_weak(String password){
        Password generatedPassword = new Password(password);
        assertThat(generatedPassword.strength()).isEqualTo(Password.Strength.WEAK);
    }


    @ParameterizedTest
    @ValueSource(strings = {"Abcdefg9", "aB3@5678", "     abcD5678", "aB3@1234"})
    void test_password_is_normal(String password){
        Password generatedPassword = new Password(password);
        assertThat(generatedPassword.strength()).isEqualTo(Password.Strength.NORMAL);
    }


    @ParameterizedTest
    @ValueSource(strings = {"aB3@56789", "aB3@56789#XYZ", "     Abcd1234@!xyz", })
    void test_password_is_strong(String password){
        Password generatedPassword = new Password(password);
        assertThat(generatedPassword.strength()).isEqualTo(Password.Strength.STRONG);
    }

    @Test
    void test_created_date_after_update_throws(){
        assertThrowsExactly(IllegalArgumentException.class, () -> new AuditInfo(LocalDateTime.now(), LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    void test_created_date_before_update_succeeds(){
        new AuditInfo(LocalDateTime.now(), LocalDateTime.now().plusMinutes(1));
    }

    @Test
    void test_created_date_equals_update_succeeds(){
        new AuditInfo(LocalDateTime.now(), LocalDateTime.now());
    }

    @ParameterizedTest
    @MethodSource("bad_names")
    void test_invalid_name(String firstName, String lastName){
        assertThrowsExactly(IllegalArgumentException.class, () -> new Name(firstName, lastName));
    }


    @ParameterizedTest
    @MethodSource("good_names")
    void test_valid_name(String firstName, String lastName){
        Name name = new Name(firstName, lastName);
        assertAll(
                () -> assertThat(name.firstName()).isEqualTo(name.firstName().trim()),
                () -> assertThat(name.lastName()).isEqualTo(name.lastName().trim()),
                () -> assertThat(firstName).contains(name.firstName()),
                () -> assertThat(lastName).contains(name.lastName())
        );
    }


    private static Stream<Arguments> good_names(){
        return Stream.of(
                Arguments.of("Juan", "Perez"),
                Arguments.of("Anne-Marie","  O'Connor"),
                Arguments.of("José", "Ángel     ")
        );
    }

    private static Stream<Arguments> bad_names(){
        return Stream.of(
                Arguments.of("  ", ""),
                Arguments.of(null,"O'Connor")
        );
    }


    @Test
    void test_invite_user(){
        String email = "jhondoe@example.com";
        assertAll(
                () -> Admin.invite(new Admin.Id(email)),
                () -> Manager.invite(new Admin.Id("creator@example.com"), new Manager.Id(email)),
                () ->  Student.invite(new Manager.Id("creator@example.com"), new Student.Id(email), new Name("jhon", "doe"), new AcademicInfo(new AcademicQuarter(1), new QuarterGroup('G')))
        );
    }

    @Test
    void test_invite_admin_has_fields(){
        Admin admin = Admin.invite(new Admin.Id("admin@example.com"));
        assertAll(
                () -> assertFalse(admin.isHydrated()),
                () -> assertFalse(admin.isVerified()),
                () -> assertNull(admin.password()),
                () -> assertThrowsExactly(IllegalStateException.class, () -> admin.login(new Password("mockPass123"), (a, b) -> true)),
                () -> assertNull(admin.auditInfo()),
                () -> assertThat(admin.managersInvited()).isEmpty()
        );
    }

    @Test
    void test_invite_manager_has_fields(){
        Admin.Id creator = new Admin.Id("admin@example.com");
        Manager.Id id = new Manager.Id("manager@example.com");
        Manager manager = Manager.invite(creator, id);
        assertAll(
                () -> assertFalse(manager.isHydrated()),
                () -> assertFalse(manager.isVerified()),
                () -> assertNull(manager.password()),
                () -> assertThrowsExactly(IllegalStateException.class, () -> manager.login(new Password("mockPass123"), (a, b) -> true)),
                () -> assertNull(manager.auditInfo()),
                () -> assertThat(manager.id()).isEqualTo(id),
                () -> assertThat(manager.createdBy()).isEqualTo(creator),
                () -> assertThat(manager.studentsCreated()).isEmpty()
        );
    }

    @Test
    void test_invite_student_has_fields(){

        Manager.Id creator = new Manager.Id("manager@example.com");
        Student.Id id = new Student.Id("student@example.com");
        Name name = new Name("Juan", "Doe");
        QuarterGroup group = new QuarterGroup('G');
        AcademicQuarter academicQuarter = new AcademicQuarter(1);
        AcademicInfo academicInfo = new AcademicInfo(academicQuarter, group);
        Student student = Student.invite(creator, id, name, academicInfo);
        assertAll(
                () -> assertFalse(student.isHydrated()),
                () -> assertFalse(student.isVerified()),
                () -> assertNull(student.password()),
                () -> assertThrowsExactly(IllegalStateException.class, () -> student.login(new Password("mockPass123"), (a, b) -> true)),
                () -> assertNull(student.auditInfo()),
                () -> assertThat(student.createdBy()).isEqualTo(creator),
                () -> assertThat(student.id()).isEqualTo(id),
                () -> assertThat(student.name()).isEqualTo(name),
                () -> assertThat(student.academicInfo()).isEqualTo(academicInfo)
        );
    }

    @Test
    void test_create_password_on_non_verified_user(){
        Admin admin = Admin.invite(new Admin.Id("admin@example.com"));
        Password password = new Password("mockPass123");
        PasswordEncoder encoder = pass -> pass.value().concat("salt");
        admin.createPassword(password, encoder);
        assertAll(
                () -> assertTrue(admin.isVerified()),
                () -> assertThat(admin.password()).isEqualTo(encoder.encode(password))
        );
    }

    @Test
    void test_create_password_on_verified_user_throws(){
        Admin admin = new Admin(new Admin.Id("admin@eample.com"), "passhash", true, new HashSet<>());
        assertThrowsExactly(IllegalStateException.class, () -> admin.createPassword(new Password("newPassword123#"), Password::value));
    }

    @Test
    void test_add_created_students_to_manager(){
        Admin.Id creator = new Admin.Id("admin@example.com");
        Manager.Id id = new Manager.Id("manager@example.com");
        Manager manager = Manager.invite(creator, id);

        List<Student.Id> studentIds = Stream.of("student1@example.com", "student2@example.com").map(Student.Id::new).toList();

        for(Student.Id studentId : studentIds){
            manager.addCreatedStudent(studentId);
        }

        assertThat(manager.studentsCreated()).containsExactlyInAnyOrderElementsOf(studentIds);
    }

    @Test
    void test_add_created_managers_to_admin(){

        Admin admin = Admin.invite(new Admin.Id("admin@example.com"));

        List<Manager.Id> managerIds = Stream.of("manager1@example.com", "manager2@example.com").map(Manager.Id::new).toList();

        for(Manager.Id managerId : managerIds){
            admin.addCreatedManager(managerId);
        }

        assertThat(admin.managersInvited()).containsExactlyInAnyOrderElementsOf(managerIds);
    }
}
