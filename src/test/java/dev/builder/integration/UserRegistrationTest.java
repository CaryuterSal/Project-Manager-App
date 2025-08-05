package dev.builder.integration;

import dev.builder.auth.application.command.LoginCommand;
import dev.builder.auth.application.command.LogoutCommand;
import dev.builder.auth.application.command.RestoreSessionCommand;
import dev.builder.auth.application.service.AuthenticationApplicationService;
import dev.builder.auth.application.service.LoginCommandHandler;
import dev.builder.auth.application.service.LogoutCommandHandler;
import dev.builder.auth.application.service.RestoreSessionCommandHandler;
import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.auth.infrastructure.EncryptedTokenStorage;
import dev.builder.auth.infrastructure.FileSessionStorage;
import dev.builder.auth.infrastructure.InMemorySessionContext;
import dev.builder.auth.infrastructure.Role;
import dev.builder.board.application.command.*;
import dev.builder.board.application.query.GetBoardQuery;
import dev.builder.board.application.query.GetTaskQuery;
import dev.builder.board.application.query.LoadAttachmentQuery;
import dev.builder.board.application.query.LoadCoverImageQuery;
import dev.builder.board.application.service.*;
import dev.builder.board.application.view.BoardView;
import dev.builder.board.domain.service.BoardCreationService;
import dev.builder.board.infrastructure.*;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.validation.PositiveValidator;
import dev.builder.core.application.validation.RequiredFieldValidator;
import dev.builder.core.application.validation.RequiredObjectValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.properties.*;
import dev.builder.shared.ContainerizedTest;
import dev.builder.usermanagement.application.command.CompleteRegistrationCommand;
import dev.builder.usermanagement.application.command.InviteManagerCommand;
import dev.builder.usermanagement.application.command.InviteStudentCommand;
import dev.builder.usermanagement.application.query.FindManagerQuery;
import dev.builder.usermanagement.application.query.FindStudentQuery;
import dev.builder.usermanagement.application.service.*;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.validator.NameValidator;
import dev.builder.usermanagement.application.validator.PasswordValidator;
import dev.builder.usermanagement.application.view.ManagerView;
import dev.builder.usermanagement.application.view.StudentView;
import dev.builder.usermanagement.domain.model.User;
import dev.builder.usermanagement.domain.port.out.AnyUserRepository;
import dev.builder.usermanagement.infrastructure.*;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class UserRegistrationTest extends ContainerizedTest {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RequestDispatcher requestDispatcher;
    private SessionContext sessionContext;
    private AnyUserRepository anyUserRepository;

    @Override
    protected void setupFirstRun() {
        JdbcAdminRepository adminRepository = new JdbcAdminRepository(connectionManager);
        JdbcManagerRepository managerRepository = new JdbcManagerRepository(connectionManager);
        JdbcStudentRepository studentRepository =  new JdbcStudentRepository(connectionManager,
                new JdbcAcademicInfoRepository(
                        new JdbcAcademicQuarterRepository(),
                        new JdbcQuarterGroupRepository()
                ));

        JdbcAnyUserRepository anyUserRepository = new JdbcAnyUserRepository(
                connectionManager,
                adminRepository,
                managerRepository,
                studentRepository
        );
        adminRepository.setAnyUserRepository(anyUserRepository);
        managerRepository.setAnyUserRepository(anyUserRepository);
        studentRepository.setAnyUserRepository(anyUserRepository);


        ResourceMessageManager messageLocalizer = new ResourceMessageManager();

        JdbcBoardCollaboratorRepository collaboratorRepository = new JdbcBoardCollaboratorRepository();
        JdbcTaskStageRepository taskStageRepository = new JdbcTaskStageRepository();
        JdbcImageRepository imageRepository = new JdbcImageRepository(connectionManager,messageLocalizer);
        JdbcAttachmentRepository attachmentRepository = new JdbcAttachmentRepository(connectionManager, messageLocalizer);
        JdbcStoredFileRepository storedFileRepository = new JdbcStoredFileRepository(connectionManager, imageRepository, attachmentRepository);
        imageRepository.setStoredFileRepository(storedFileRepository);
        attachmentRepository.setJdbcStoredFileRepository(storedFileRepository);
        JdbcTaskAssigneeRepository taskAssigneeRepository = new JdbcTaskAssigneeRepository();
        JdbcTaskRepository taskRepository = new JdbcTaskRepository(connectionManager, taskAssigneeRepository, taskStageRepository);
        JdbcStageRepository stageRepository =  new JdbcStageRepository(connectionManager, taskRepository);
        JdbcBoardRepository boardRepository =  new JdbcBoardRepository(
                connectionManager,
                stageRepository,
                collaboratorRepository
        );
        stageRepository.setJdbcBoardRepository(boardRepository);

        BoardCreationService boardCreationService = new BoardCreationService(messageLocalizer);
        TikaMimeTypeGenerator mimeTypeGenerator = new TikaMimeTypeGenerator();
        InMemorySessionContext sessionContext = new InMemorySessionContext();

        UserViewMapper userViewMapper = new UserViewMapper();
        FileViewMapper fileViewMapper = new FileViewMapper();
        TaskViewMapper taskViewMapper = new TaskViewMapper(userViewMapper, fileViewMapper);
        StageViewMapper stageViewMapper = new StageViewMapper(taskViewMapper);
        CollaboratorViewMapper collaboratorViewMapper = new CollaboratorViewMapper(userViewMapper);
        BoardViewMapper boardViewMapper = new BoardViewMapper(stageViewMapper, collaboratorViewMapper);
        BoardApplicationService boardApplicationService = new BoardApplicationService(
                boardRepository,
                stageRepository,
                attachmentRepository,
                imageRepository,
                storedFileRepository,
                studentRepository,
                managerRepository,
                boardCreationService,
                messageLocalizer,
                mimeTypeGenerator,
                sessionContext,
                connectionManager,
                boardViewMapper,
                stageViewMapper,
                taskViewMapper,
                fileViewMapper
        );

        ActiveProfileConfiguration activeProfileConfiguration = new ActiveProfileConfiguration(new EnvActiveProfileProvider(), new SystemActiveProfileProvider());
        SimpleApplicationProperties applicationProperties = new SimpleApplicationProperties(activeProfileConfiguration);
        BCryptPasswordHasher passwordHasher = new BCryptPasswordHasher();
        EncryptedTokenStorage encryptedTokenStorage = new EncryptedTokenStorage(applicationProperties);
        FileSessionStorage fileSessionStorage = new FileSessionStorage(sessionContext, encryptedTokenStorage, applicationProperties);

        AuthenticationApplicationService authenticationApplicationService = new AuthenticationApplicationService(anyUserRepository, messageLocalizer, sessionContext, fileSessionStorage, passwordHasher);

        UserApplicationService userApplicationService = new UserApplicationService(
                anyUserRepository,
                adminRepository,
                managerRepository,
                studentRepository,
                sessionContext,
                userViewMapper,
                connectionManager,
                messageLocalizer,
                passwordHasher
        );
        userApplicationService.setAuthenticationService(authenticationApplicationService);
        userApplicationService.setBoardService(boardApplicationService);
        RequiredFieldValidator requiredFieldValidator = new RequiredFieldValidator(messageLocalizer);
        RequiredObjectValidator requiredObjectValidator = new RequiredObjectValidator(messageLocalizer);
        EmailValidator emailValidator = new EmailValidator(messageLocalizer, requiredFieldValidator);
        AddAttachmentToTaskCommand.AddAttachmentToTaskCommandValidator addAttachmentToTaskCommandValidator= new AddAttachmentToTaskCommand.AddAttachmentToTaskCommandValidator(requiredFieldValidator, requiredObjectValidator, messageLocalizer);
        AddCollaboratorCommand.AddCollaboratorCommandValidator addCollaboratorCommandValidator = new AddCollaboratorCommand.AddCollaboratorCommandValidator(emailValidator);
        AssignStudentToTaskCommand.AssignStudentToTaskCommandValidator assignStudentToTaskCommandValidator = new AssignStudentToTaskCommand.AssignStudentToTaskCommandValidator(requiredObjectValidator, emailValidator);
        AttachCoverImageToTaskCommand.AttachCoverImageToTaskCommandValidator attachCoverImageToTaskCommandValidator = new AttachCoverImageToTaskCommand.AttachCoverImageToTaskCommandValidator(requiredFieldValidator,requiredObjectValidator,messageLocalizer);
        CreateTaskCommand.CreateTaskCommandValidator createTaskCommandValidator = new CreateTaskCommand.CreateTaskCommandValidator(requiredObjectValidator,requiredFieldValidator,messageLocalizer);
        DeleteTaskCommand.DeleteTaskCommandValidator deleteTaskCommandValidator = new DeleteTaskCommand.DeleteTaskCommandValidator(requiredObjectValidator);
        EditTaskCommand.EditTaskCommandValidator editTaskCommandValidator = new EditTaskCommand.EditTaskCommandValidator(requiredObjectValidator,requiredFieldValidator,messageLocalizer);
        MoveTaskCommand.MoveTaskCommandValidator moveTaskCommandValidator = new MoveTaskCommand.MoveTaskCommandValidator(requiredObjectValidator);
        RemoveAttachmentCommand.RemoveAttachmentCommandValidator removeAttachmentCommandValidator = new RemoveAttachmentCommand.RemoveAttachmentCommandValidator(requiredObjectValidator);
        RemoveCoverImageCommand.RemoveCoverImageCommandValidator removeCoverImageCommandValidator = new  RemoveCoverImageCommand.RemoveCoverImageCommandValidator(requiredObjectValidator);
        RevokeAssignmentFromTaskCommand.RevokeAssignmentFromTaskCommandValidator revokeAssignmentFromTaskCommandValidator = new RevokeAssignmentFromTaskCommand.RevokeAssignmentFromTaskCommandValidator(requiredObjectValidator,requiredFieldValidator);
        GetTaskQuery.GetTaskQueryValidator getTaskQueryValidator = new GetTaskQuery.GetTaskQueryValidator(requiredObjectValidator);
        LoadAttachmentQuery.LoadAttachmentQueryValidator loadAttachmentQueryValidator = new LoadAttachmentQuery.LoadAttachmentQueryValidator(requiredObjectValidator);
        LoadCoverImageQuery.LoadCoverImageQueryValidator loadCoverImageQueryValidator = new LoadCoverImageQuery.LoadCoverImageQueryValidator(requiredObjectValidator);

        AddAttachmentToTaskCommandHandler addAttachmentToTaskCommandHandler = new AddAttachmentToTaskCommandHandler(boardApplicationService, addAttachmentToTaskCommandValidator);
        AddCollaboratorCommandHandler addCollaboratorCommandHandler = new AddCollaboratorCommandHandler(boardApplicationService, addCollaboratorCommandValidator);
        AssignStudentToTaskCommandHandler assignStudentToTaskCommandHandler = new AssignStudentToTaskCommandHandler(boardApplicationService, assignStudentToTaskCommandValidator);
        AttachCoverImageToTaskCommandHandler attachCoverImageToTaskCommandHandler = new AttachCoverImageToTaskCommandHandler(boardApplicationService, attachCoverImageToTaskCommandValidator);
        CreateTaskCommandHandler createTaskCommandHandler = new CreateTaskCommandHandler(boardApplicationService, createTaskCommandValidator);
        DeleteTaskCommandHandler deleteTaskCommandHandler = new DeleteTaskCommandHandler(boardApplicationService, deleteTaskCommandValidator);
        EditTaskCommandHandler editTaskCommandHandler = new EditTaskCommandHandler(boardApplicationService, editTaskCommandValidator);
        MoveTaskCommandHandler moveTaskCommandHandler = new MoveTaskCommandHandler(boardApplicationService, moveTaskCommandValidator);
        RemoveAttachmentCommandHandler removeAttachmentCommandHandler = new RemoveAttachmentCommandHandler(boardApplicationService, removeAttachmentCommandValidator);
        RemoveCoverImageCommandHandler removeCoverImageCommandHandler = new RemoveCoverImageCommandHandler(boardApplicationService, removeCoverImageCommandValidator);
        RevokeAssignmentFromTaskCommandHandler revokeAssignmentFromTaskCommandHandler = new RevokeAssignmentFromTaskCommandHandler(boardApplicationService, revokeAssignmentFromTaskCommandValidator);
        GetBoardQueryHandler getBoardQueryHandler = new GetBoardQueryHandler(boardApplicationService);
        GetTaskQueryHandler getTaskQueryHandler = new GetTaskQueryHandler(boardApplicationService, getTaskQueryValidator);
        LoadAttachmentQueryHandler loadAttachmentQueryHandler = new LoadAttachmentQueryHandler(boardApplicationService, loadAttachmentQueryValidator);
        LoadCoverImageQueryHandler loadCoverImageQueryHandler = new LoadCoverImageQueryHandler(boardApplicationService, loadCoverImageQueryValidator);

        PasswordValidator passwordValidator = new PasswordValidator(messageLocalizer, requiredFieldValidator);
        LoginCommand.LoginCommandValidator loginCommandValidator = new LoginCommand.LoginCommandValidator(emailValidator, passwordValidator);

        LoginCommandHandler loginCommandHandler = new LoginCommandHandler(loginCommandValidator, authenticationApplicationService);
        LogoutCommandHandler logoutCommandHandler = new LogoutCommandHandler(authenticationApplicationService);
        RestoreSessionCommandHandler restoreSessionCommandHandler = new RestoreSessionCommandHandler(authenticationApplicationService);

        CompleteRegistrationCommand.CompleteRegistrationCommandValidator completeRegistrationCommandValidator = new CompleteRegistrationCommand.CompleteRegistrationCommandValidator(passwordValidator, emailValidator);
        InviteManagerCommand.InviteManagerCommandValidator inviteManagerCommandValidator = new InviteManagerCommand.InviteManagerCommandValidator(emailValidator);
        NameValidator nameValidator = new NameValidator(requiredFieldValidator);
        PositiveValidator positiveValidator = new PositiveValidator(messageLocalizer);
        InviteStudentCommand.InviteStudentCommandValidator inviteStudentCommandValidator = new InviteStudentCommand.InviteStudentCommandValidator(emailValidator, nameValidator, positiveValidator);
        FindManagerQuery.FindManagerQueryValidator findManagerQueryValidator = new FindManagerQuery.FindManagerQueryValidator(emailValidator);
        FindStudentQuery.FindStudentQueryValidator findStudentQueryValidator = new FindStudentQuery.FindStudentQueryValidator(emailValidator);

        CompleteRegistrationCommandHandler completeRegistrationCommandHandler = new CompleteRegistrationCommandHandler(userApplicationService, completeRegistrationCommandValidator);
        InviteManagerCommandHandler inviteManagerCommandHandler = new InviteManagerCommandHandler(userApplicationService, inviteManagerCommandValidator);
        InviteStudentCommandHandler inviteStudentCommandHandler = new InviteStudentCommandHandler(userApplicationService, inviteStudentCommandValidator);
        FindManagerQueryHandler findManagerQueryHandler = new FindManagerQueryHandler(userApplicationService, findManagerQueryValidator);
        FindStudentQueryHandler findStudentQueryHandler = new FindStudentQueryHandler(userApplicationService, findStudentQueryValidator);

        RequestDispatcher requestDispatcher = new RequestDispatcher();
        requestDispatcher.registerHandler(AddAttachmentToTaskCommand.class, addAttachmentToTaskCommandHandler);
        requestDispatcher.registerHandler(AddCollaboratorCommand.class, addCollaboratorCommandHandler);
        requestDispatcher.registerHandler(AssignStudentToTaskCommand.class, assignStudentToTaskCommandHandler);
        requestDispatcher.registerHandler(AttachCoverImageToTaskCommand.class, attachCoverImageToTaskCommandHandler);
        requestDispatcher.registerHandler(CreateTaskCommand.class, createTaskCommandHandler);
        requestDispatcher.registerHandler(DeleteTaskCommand.class, deleteTaskCommandHandler);
        requestDispatcher.registerHandler(EditTaskCommand.class, editTaskCommandHandler);
        requestDispatcher.registerHandler(MoveTaskCommand.class, moveTaskCommandHandler);
        requestDispatcher.registerHandler(RemoveAttachmentCommand.class, removeAttachmentCommandHandler);
        requestDispatcher.registerHandler(RemoveCoverImageCommand.class, removeCoverImageCommandHandler);
        requestDispatcher.registerHandler(RevokeAssignmentFromTaskCommand.class, revokeAssignmentFromTaskCommandHandler);
        requestDispatcher.registerHandler(GetBoardQuery.class, getBoardQueryHandler);
        requestDispatcher.registerHandler(GetTaskQuery.class, getTaskQueryHandler);
        requestDispatcher.registerHandler(LoadAttachmentQuery.class, loadAttachmentQueryHandler);
        requestDispatcher.registerHandler(LoadCoverImageQuery.class, loadCoverImageQueryHandler);
        requestDispatcher.registerHandler(LoginCommand.class, loginCommandHandler);
        requestDispatcher.registerHandler(LogoutCommand.class,  logoutCommandHandler);
        requestDispatcher.registerHandler(RestoreSessionCommand.class, restoreSessionCommandHandler);
        requestDispatcher.registerHandler(CompleteRegistrationCommand.class, completeRegistrationCommandHandler);
        requestDispatcher.registerHandler(InviteManagerCommand.class, inviteManagerCommandHandler);
        requestDispatcher.registerHandler(InviteStudentCommand.class, inviteStudentCommandHandler);
        requestDispatcher.registerHandler(FindManagerQuery.class, findManagerQueryHandler);
        requestDispatcher.registerHandler(FindStudentQuery.class, findStudentQueryHandler);

        this.requestDispatcher = requestDispatcher;
        this.sessionContext = sessionContext;
        this.anyUserRepository = anyUserRepository;

    }

    @Override
    protected void setup() {
    }

    @Test
    void test_invite_manager() throws ValidationException {
        executeInsertTestData();
        mockWithAdmin();
        String managerEmail = "test_manager@example.com";
        requestDispatcher.dispatch(new InviteManagerCommand(managerEmail));
        ManagerView manager = requestDispatcher.dispatch(FindManagerQuery.forEmail(managerEmail)).orElseThrow();
        assertAll(
                () -> assertThat(manager.verified()).isFalse()
        );
    }

    @Test
    void test_complete_registration_for_manager() throws ValidationException {
        executeInsertTestData();
        mockWithAdmin();
        String managerEmail = "test_manager@example.com";
        requestDispatcher.dispatch(new InviteManagerCommand(managerEmail));

        sessionContext.clear();
        requestDispatcher.dispatch(new CompleteRegistrationCommand(managerEmail, "customPassword123#"));

        ManagerView manager = requestDispatcher.dispatch(FindManagerQuery.forEmail(managerEmail)).orElseThrow();
        assertAll(
                () -> assertThat(manager.verified()).isTrue()
        );
        BoardView board = requestDispatcher.dispatch(GetBoardQuery.own()).orElseThrow();
    }

    @Test
    void test_login() throws ValidationException {
        executeInsertTestData();
        mockWithAdmin();
        String managerEmail = "test_manager@example.com";
        requestDispatcher.dispatch(new InviteManagerCommand(managerEmail));

        sessionContext.clear();
        requestDispatcher.dispatch(new CompleteRegistrationCommand(managerEmail, "customPassword123#"));
        sessionContext.clear();
        assertFalse(sessionContext.isAuthenticated());

        requestDispatcher.dispatch(new LoginCommand(managerEmail, "customPassword123#"));
        assertTrue(sessionContext.isAuthenticated());
        assertAll(
                () -> assertThat(sessionContext.getCurrentUser()).isEqualTo(managerEmail),
                () -> assertTrue(sessionContext.hasRole(Role.MANAGER))
        );
    }

    @Test
    void test_logout() throws ValidationException {
        executeInsertTestData();
        mockWithAdmin();
        String managerEmail = "test_manager@example.com";
        requestDispatcher.dispatch(new InviteManagerCommand(managerEmail));

        sessionContext.clear();
        requestDispatcher.dispatch(new CompleteRegistrationCommand(managerEmail, "customPassword123#"));
        sessionContext.clear();
        assertFalse(sessionContext.isAuthenticated());

        requestDispatcher.dispatch(new LoginCommand(managerEmail, "customPassword123#"));
        assertTrue(sessionContext.isAuthenticated());

        requestDispatcher.dispatch(new LogoutCommand());
        assertFalse(sessionContext.isAuthenticated());
    }

    @Test
    void test_recover_session_after_complete_registration() throws ValidationException {
        executeInsertTestData();
        mockWithAdmin();
        String managerEmail = "test_manager@example.com";
        requestDispatcher.dispatch(new InviteManagerCommand(managerEmail));
        sessionContext.clear();
        requestDispatcher.dispatch(new CompleteRegistrationCommand(managerEmail, "customPassword123#"));
        sessionContext.clear();
        assertFalse(sessionContext.isAuthenticated());


        boolean restored = requestDispatcher.dispatch(new RestoreSessionCommand());
        assertTrue(restored);
        assertTrue(sessionContext.isAuthenticated());
        assertAll(
                () -> assertThat(sessionContext.getCurrentUser()).isEqualTo(managerEmail),
                () -> assertTrue(sessionContext.hasRole(Role.MANAGER))
        );
    }

    @Test
    void test_recover_session_after_login() throws ValidationException {
        executeInsertTestData();
        mockWithAdmin();
        String managerEmail = "test_manager@example.com";
        requestDispatcher.dispatch(new InviteManagerCommand(managerEmail));
        sessionContext.clear();
        requestDispatcher.dispatch(new CompleteRegistrationCommand(managerEmail, "customPassword123#"));
        sessionContext.clear();
        requestDispatcher.dispatch(new LoginCommand(managerEmail, "customPassword123#"));
        sessionContext.clear();

        boolean restored = requestDispatcher.dispatch(new RestoreSessionCommand());
        assertTrue(restored);
        assertTrue(sessionContext.isAuthenticated());
        assertAll(
                () -> assertThat(sessionContext.getCurrentUser()).isEqualTo(managerEmail),
                () -> assertTrue(sessionContext.hasRole(Role.MANAGER))
        );
    }

    @Test
    void test_unable_to_recover_session_after_logout() throws ValidationException {
        executeInsertTestData();
        mockWithAdmin();
        String managerEmail = "test_manager@example.com";
        requestDispatcher.dispatch(new InviteManagerCommand(managerEmail));
        sessionContext.clear();
        requestDispatcher.dispatch(new CompleteRegistrationCommand(managerEmail, "customPassword123#"));
        requestDispatcher.dispatch(new LogoutCommand());
        boolean recovered = requestDispatcher.dispatch(new RestoreSessionCommand());
        assertFalse(recovered);
        assertFalse(sessionContext.isAuthenticated());
    }

    @Test
    void test_invite_student() throws ValidationException {
        executeInsertTestData();
        mockWithAdmin();
        String managerEmail = "test_manager@example.com";
        requestDispatcher.dispatch(new InviteManagerCommand(managerEmail));
        sessionContext.clear();
        requestDispatcher.dispatch(new CompleteRegistrationCommand(managerEmail, "customPassword123#"));

        InviteStudentCommand command = new InviteStudentCommand("test_student@example.com", "Jhon", "Doe", 1, 'F');
        requestDispatcher.dispatch(command);
        StudentView student = requestDispatcher.dispatch(FindStudentQuery.forEmail(command.email())).orElseThrow();

        assertAll(
                () -> assertThat(student.verified()).isFalse(),
                () -> assertThat(student.firstName()).isEqualTo(command.firstName()),
                () -> assertThat(student.lastName()).isEqualTo(command.lastName()),
                () -> assertThat(student.email()).isEqualTo(command.email()),
                () -> assertThat(student.quarter()).isEqualTo(command.academicQuarter()),
                () -> assertThat(student.group()).isEqualTo(command.academicGroup())
        );
    }


    private void mockWithAdmin(){
        mockWithUser("admin@example.com");
    }
    private void mockWithManager(){
        mockWithUser("manager1@example.com");
    }

    private void mockWithStudent1(){
        mockWithUser("student1@example.com");
    }

    private void mockWithStudent2(){
        mockWithUser("student2@example.com");
    }

    private void mockWithUser(String email){
        User<?> user = anyUserRepository.findById(new User.Id<>(email))
                .orElseThrow();
        sessionContext.setAuthentication(user);
    }
}
