package dev.builder.board;

import dev.builder.auth.domain.port.in.AuthenticationService;
import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.auth.infrastructure.InMemorySessionContext;
import dev.builder.board.application.command.*;
import dev.builder.board.application.query.GetBoardQuery;
import dev.builder.board.application.query.GetTaskQuery;
import dev.builder.board.application.query.LoadAttachmentQuery;
import dev.builder.board.application.query.LoadCoverImageQuery;
import dev.builder.board.application.service.*;
import dev.builder.board.application.view.FileView;
import dev.builder.board.application.view.StageView;
import dev.builder.board.application.view.TaskView;
import dev.builder.board.domain.model.Color;
import dev.builder.board.domain.model.Stage;
import dev.builder.board.domain.port.in.BoardService;
import dev.builder.board.domain.port.out.BoardRepository;
import dev.builder.board.domain.port.out.MimeTypeGenerator;
import dev.builder.board.domain.service.BoardCreationService;
import dev.builder.board.infrastructure.*;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.validation.RequiredFieldValidator;
import dev.builder.core.application.validation.RequiredObjectValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.persistence.UUIDMapper;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import dev.builder.core.infrastructure.properties.ResourceMessageManager;
import dev.builder.shared.ContainerizedTest;
import dev.builder.usermanagement.application.service.UserViewMapper;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.view.UserView;
import dev.builder.usermanagement.domain.model.User;
import dev.builder.usermanagement.domain.port.out.AnyUserRepository;
import dev.builder.usermanagement.infrastructure.*;
import jakarta.xml.bind.DatatypeConverter;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.K;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class BoardApplicationServiceTest extends ContainerizedTest {


    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RequestDispatcher requestDispatcher;
    private SessionContext sessionContext;
    private AnyUserRepository  anyUserRepository;

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


        JdbcBoardCollaboratorRepository  collaboratorRepository = new JdbcBoardCollaboratorRepository();
        JdbcTaskStageRepository taskStageRepository = new JdbcTaskStageRepository();
        JdbcImageRepository imageRepository = new JdbcImageRepository(connectionManager);
        JdbcAttachmentRepository attachmentRepository = new JdbcAttachmentRepository(connectionManager);
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

        ResourceMessageManager messageLocalizer = new ResourceMessageManager();
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

        this.requestDispatcher = requestDispatcher;
        this.sessionContext = sessionContext;
        this.anyUserRepository = anyUserRepository;

    }

    @Override
    protected void setup() {

    }

    @Test
    void test_get_board() throws ValidationException {
        executeInsertTestData();
        mockWithManager();
        requestDispatcher.dispatch(GetBoardQuery.own());
    }

    @Test
    void test_get_task_as_collaborator() throws ValidationException {
        executeInsertTestData();
        mockWithStudent2();
        TaskView task = requestDispatcher.dispatch(new GetTaskQuery(parseHexToUUID("D2B2C3D4E5F60123456789ABCDEF0006"))).orElseThrow();
        assertAll(
                () -> assertThat(task.title()).isEqualTo("Implement login"),
                () -> assertThat(task.description()).isEqualTo("Create login module with JWT"),
                () -> assertThat(task.color()).isEqualTo(Color.GREEN),
                () -> assertThat(task.deadline()).isEqualTo(LocalDateTime.parse("2025-12-10 23:59:59", DATE_FORMATTER)),
                () -> assertThat(task.assignees().stream().map(UserView::email).collect(Collectors.toSet())).containsExactlyInAnyOrder("student2@example.com"),
                () -> assertThat(task.attachments().stream().map(FileView::id).collect(Collectors.toSet())).containsExactlyInAnyOrder(parseHexToUUID("E2B2C3D4E5F60123456789ABCDEF0009")),
                () -> assertThat(task.coverImage()).isEmpty(),
                () -> assertThat(task.startedAt()).isEmpty(),
                () -> assertThat(task.finishedAt()).isEmpty()
        );
    }

    @Test
    void test_create_task() throws ValidationException {
        executeInsertTestData();
        mockWithManager();
        CreateTaskCommand command = new CreateTaskCommand(Stage.StageState.IN_PROGRESS, "tarea de prueba", "descripción de prueba", Color.AMBER, LocalDateTime.now().plusDays(10).minusSeconds(3));
        StageView stageView = requestDispatcher.dispatch(command);
        TaskView task =  stageView.tasks().stream()
                        .filter(t -> t.title().equals(command.title()))
                        .findFirst()
                        .orElseThrow();
        assertAll(
                () -> assertThat(task.createdAt()).isEqualTo(LocalDateTime.now()),
                () ->  assertThat(task.title()).isEqualTo(command.title()),
                () -> assertThat(task.description()).isEqualTo(command.description()),
                () -> assertThat(stageView.state()).isEqualTo(Stage.StageState.IN_PROGRESS),
                () -> assertThat(task.color()).isEqualTo(command.color()),
                () -> assertThat(task.deadline()).isEqualTo(command.deadline()),
                () -> assertThat(task.assignees()).isEmpty(),
                () -> assertThat(task.coverImage()).isEmpty(),
                () -> assertThat(task.attachments()).isEmpty()
        );
    }

    @Test
    void test_get_attachment_as_collaborator() throws ValidationException, IOException {
        executeInsertTestData();
        mockWithStudent2();
        try (InputStream file = requestDispatcher.dispatch(new LoadAttachmentQuery(parseHexToUUID("E2B2C3D4E5F60123456789ABCDEF0009"))).orElseThrow();
             InputStream expected = getClass().getResourceAsStream("/dev/builder/requirements.pdf")) {
            assertNotNull(file);
            assertNotNull(expected);
            assertArrayEquals(file.readAllBytes(), expected.readAllBytes());
        }
    }

    @Override
    protected void cleanup() {
        sessionContext.clear();
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
