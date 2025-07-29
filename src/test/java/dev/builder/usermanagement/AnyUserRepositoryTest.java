package dev.builder.usermanagement;

import dev.builder.shared.ContainerizedTest;
import dev.builder.shared.IntegrationTest;
import dev.builder.usermanagement.domain.model.Admin;
import dev.builder.usermanagement.domain.model.Manager;
import dev.builder.usermanagement.domain.model.Student;
import dev.builder.usermanagement.domain.model.User;
import dev.builder.usermanagement.infrastructure.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@IntegrationTest
public class AnyUserRepositoryTest extends ContainerizedTest {

    private JdbcAnyUserRepository anyUserRepository;

    @Override
    protected void setup() {

    }

    @Override
    protected void setupFirstRun() {
        JdbcAdminRepository adminRepository = new JdbcAdminRepository(connectionManager);
        JdbcManagerRepository managerRepository = new JdbcManagerRepository(connectionManager);
        JdbcStudentRepository studentRepository =  new JdbcStudentRepository(connectionManager,
                new JdbcAcademicInfoRepository(
                        new JdbcAcademicQuarterRepository(),
                        new JdbcQuarterGroupRepository()
                ));

        anyUserRepository = new JdbcAnyUserRepository(
                connectionManager,
                adminRepository,
                managerRepository,
                studentRepository
        );
        adminRepository.setAnyUserRepository(anyUserRepository);
        managerRepository.setAnyUserRepository(anyUserRepository);
        studentRepository.setAnyUserRepository(anyUserRepository);
    }

    @Test
    void test_find_all_with_records_return_right_number(){
        executeInsertTestData();
        List<? extends User<?>> users = anyUserRepository.findAll();
        assertThat(users).hasSize(4);
    }

    @Test
    void test_find_all_with_records_return_right_types(){
        executeInsertTestData();
        Map<Class<?>, Integer> occurrencesByType = new HashMap<>();
        for(User<?> user: anyUserRepository.findAll()){
            if(user.getClass().equals(Manager.class)){
                occurrencesByType.merge(Manager.class, 1, Integer::sum);
            } else if(user.getClass().equals(Student.class)){
                occurrencesByType.merge(Student.class, 1, Integer::sum);
            } else if(user.getClass().equals(Admin.class)){
                occurrencesByType.merge(Admin.class, 1, Integer::sum);
            }
        }

        assertThat(occurrencesByType.get(Manager.class)).isEqualTo(1);
        assertThat(occurrencesByType.get(Student.class)).isEqualTo(2);
        assertThat(occurrencesByType.get(Admin.class)).isEqualTo(1);
    }

    @Test
    void test_find_existing_admin_by_id(){
        executeInsertTestData();
        Optional<? extends User<?>> retrievedAdmin = anyUserRepository.findById(new User.Id("admin@example.com"));
        assertTrue(retrievedAdmin.isPresent());
        assertAll(
                () -> assertThat(retrievedAdmin.get()).isExactlyInstanceOf(Admin.class),
                () -> assertThat(retrievedAdmin.get().email().value()).isEqualTo("admin@example.com")
        );
    }

    @Test
    void test_find_existing_manager_by_id(){
        executeInsertTestData();
        Optional<? extends User<?>> retrievedManager = anyUserRepository.findById(new User.Id("manager1@example.com"));
        assertTrue(retrievedManager.isPresent());
        assertAll(
                () -> assertThat(retrievedManager.get()).isExactlyInstanceOf(Manager.class),
                () -> assertThat(retrievedManager.get().email().value()).isEqualTo("manager1@example.com")
        );
    }


    @Test
    void test_find_existing_student_by_id(){
        executeInsertTestData();
        Optional<? extends User<?>> retrievedStudent = anyUserRepository.findById(new User.Id("student1@example.com"));
        assertTrue(retrievedStudent.isPresent());
        assertAll(
                () -> assertThat(retrievedStudent.get()).isExactlyInstanceOf(Student.class),
                () -> assertThat(retrievedStudent.get().email().value()).isEqualTo("student1@example.com")
        );
    }


    @Test
    void test_find_all_values_are_populated(){
        executeInsertTestData();
        Optional<? extends User<?>> user = anyUserRepository.findById(new User.Id("admin@example.com"));
        assumeTrue(user.isPresent());
        assertAll(
                () -> assertThat(user.get().isHydrated()).isTrue(),
                () -> assertThat(user.get().isVerified()).isFalse(),
                () -> assertThat(user.get().createdAt()).isBefore(LocalDateTime.now()),
                () -> assertThat(user.get().updatedAt()).isBefore(LocalDateTime.now()),
                () -> assertThat(user.get().updatedAt()).isEqualTo(user.get().createdAt()),
                () -> assertThat(user.get().password()).isEqualTo("adminpasshash")
        );
    }

    @Test
    void test_delete_by_id(){
        executeInsertTestData();
        boolean deleted = anyUserRepository.deleteById(new User.Id("admin@example.com"));
        assertTrue(deleted);
    }

    @Test
    void test_delete(){
        executeInsertTestData();
        Optional<? extends User<?>> existingUser = anyUserRepository.findById(new User.Id("admin@example.com"));
        assumeTrue(existingUser.isPresent());
        boolean deleted = anyUserRepository.delete(existingUser.get());
        assertTrue(deleted);
    }

    @Test
    void test_deleted_user_is_not_found(){
        executeInsertTestData();
        anyUserRepository.deleteById(new User.Id("admin@example.com"));
        Optional<? extends User<?>> deletedUser = anyUserRepository.findById(new User.Id("admin@example.com"));
        assertTrue(deletedUser.isEmpty());
    }

    @Test
    void test_deleted_user_is_excluded_on_find_all(){
        executeInsertTestData();
        List<? extends User<?>> originalUsers = anyUserRepository.findAll();
        assertThat(originalUsers).hasSize(4);
        // Cuenta los estudiantes existentes
        assertWith(originalUsers.stream()
                .filter(user -> user instanceof Student).toList(),
                students -> assertThat(students.size()).isEqualTo(2),
                students -> assertThat(students.stream().map(s -> s.email().value()).toList()).containsExactly("student1@example.com", "student2@example.com")
        );

        anyUserRepository.deleteById(new User.Id("student1@example.com"));
        List<? extends User<?>> updatedUsers = anyUserRepository.findAll();
        assertThat(updatedUsers).hasSize(3);
        // Cuenta los estudiantes existentes
        assertWith(updatedUsers.stream()
                        .filter(user -> user instanceof Student).toList(),
                students -> assertThat(students.size()).isEqualTo(1),
                students -> assertThat(students.stream().map(s -> s.email().value()).toList()).containsExactly("student2@example.com")
        );
    }

    @Test
    void test_existing_user_exists_by_id(){
        executeInsertTestData();
        assertTrue(anyUserRepository.existsById(new User.Id("admin@example.com")));
    }

    @Test
    void test_non_existing_user__not_exists_by_id(){
        executeInsertTestData();
        assertFalse(anyUserRepository.existsById(new User.Id("nonexisting@example.com")));
    }

    @Test
    void test_deleted_user_not_exists_by_id(){
        executeInsertTestData();
        User.Id userId = new User.Id("admin@example.com");
        assertTrue(anyUserRepository.existsById(userId));
        anyUserRepository.deleteById(userId);
        assertFalse(anyUserRepository.existsById(userId));
    }
}
