package dev.builder.usermanagement;

import dev.builder.shared.ContainerizedTest;
import dev.builder.shared.IntegrationTest;
import dev.builder.usermanagement.infrastructure.*;

@IntegrationTest
public class AdminRepositoryTest extends ContainerizedTest {

    private final JdbcAdminRepository adminRepository = new JdbcAdminRepository(connectionManager);
    private final JdbcManagerRepository managerRepository = new JdbcManagerRepository(connectionManager);
    private final JdbcStudentRepository studentRepository =  new JdbcStudentRepository(connectionManager,
            new JdbcAcademicInfoRepository(
                    new JdbcAcademicQuarterRepository(),
                    new JdbcQuarterGroupRepository()
            ));

    private final JdbcAnyUserRepository anyUserRepository = new JdbcAnyUserRepository(
            connectionManager,
            adminRepository,
            managerRepository,
            studentRepository
    );

    @Override
    protected void setupFirstRun() {
        adminRepository.setAnyUserRepository(anyUserRepository);
        managerRepository.setAnyUserRepository(anyUserRepository);
        studentRepository.setAnyUserRepository(anyUserRepository);
    }

    @Override
    protected void setup() {
    }
}
