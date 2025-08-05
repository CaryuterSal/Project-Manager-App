package dev.builder.board;

import dev.builder.auth.application.service.UnauthorizedException;
import dev.builder.auth.domain.port.in.AuthenticationService;
import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.auth.infrastructure.InMemorySessionContext;
import dev.builder.board.application.command.*;
import dev.builder.board.application.query.GetBoardQuery;
import dev.builder.board.application.query.GetTaskQuery;
import dev.builder.board.application.query.LoadAttachmentQuery;
import dev.builder.board.application.query.LoadCoverImageQuery;
import dev.builder.board.application.service.*;
import dev.builder.board.application.view.*;
import dev.builder.board.domain.exception.FileExistsException;
import dev.builder.board.domain.exception.FileUnsupportedException;
import dev.builder.board.domain.model.Color;
import dev.builder.board.domain.model.Stage;
import dev.builder.board.domain.model.StoredFile;
import dev.builder.board.domain.port.in.BoardService;
import dev.builder.board.domain.port.out.BoardRepository;
import dev.builder.board.domain.port.out.MimeTypeGenerator;
import dev.builder.board.domain.service.BoardCreationService;
import dev.builder.board.infrastructure.*;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.validation.*;
import dev.builder.core.infrastructure.persistence.UUIDMapper;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import dev.builder.core.infrastructure.properties.ResourceMessageManager;
import dev.builder.shared.ContainerizedTest;
import dev.builder.usermanagement.application.query.FindUserQuery;
import dev.builder.usermanagement.application.service.UserViewMapper;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.view.StudentView;
import dev.builder.usermanagement.application.view.UserView;
import dev.builder.usermanagement.domain.model.Student;
import dev.builder.usermanagement.domain.model.User;
import dev.builder.usermanagement.domain.port.out.AnyUserRepository;
import dev.builder.usermanagement.infrastructure.*;
import jakarta.xml.bind.DatatypeConverter;
import org.assertj.core.data.TemporalOffset;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.K;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
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


        ResourceMessageManager messageLocalizer = new ResourceMessageManager();

        JdbcBoardCollaboratorRepository  collaboratorRepository = new JdbcBoardCollaboratorRepository();
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
        BoardView boardView = requestDispatcher.dispatch(GetBoardQuery.own()).orElseThrow();
        assertNotNull(boardView.collaborators());
        assertNotNull(boardView.stages());
        assertAll(
                () -> assertThat(boardView.collaborators()).hasSize(2),
                () -> assertThat(boardView.collaborators().stream()
                        .map(CollaboratorView::student).map(StudentView::email))
                        .containsExactlyInAnyOrder("student1@example.com", "student2@example.com"),
                () -> assertThat(boardView.collaborators().stream()
                        .map(CollaboratorView::issuedAt))
                        .allSatisfy(issuedAt -> assertThat(issuedAt).isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS))),
                () -> assertThat(boardView.stages().stream()
                        .map(StageView::state))
                        .containsExactlyInAnyOrder(Stage.StageState.TO_DO, Stage.StageState.IN_PROGRESS, Stage.StageState.DONE),
                () -> assertThat(boardView.stages().stream()
                        .flatMap(sv -> sv.tasks().stream()
                                .map(TaskView::id)))
                        .containsExactlyInAnyOrder(
                                parseHexToUUID("D1B2C3D4E5F60123456789ABCDEF0005"),
                                parseHexToUUID("D3B2C3D4E5F60123456789ABCDEF0007"),
                                parseHexToUUID("D2B2C3D4E5F60123456789ABCDEF0006"))
        );
    }

    @Test
    void test_get_board_as_student() throws ValidationException {
        executeInsertTestData();
        mockWithStudent1();
        BoardView boardView = requestDispatcher.dispatch(GetBoardQuery.forOwner("manager1@example.com")).orElseThrow();
        assertNotNull(boardView.collaborators());
        assertNotNull(boardView.stages());
        assertAll(
                () -> assertThat(boardView.collaborators()).hasSize(2),
                () -> assertThat(boardView.collaborators().stream()
                        .map(CollaboratorView::student).map(StudentView::email))
                        .containsExactlyInAnyOrder("student1@example.com", "student2@example.com"),
                () -> assertThat(boardView.collaborators().stream()
                        .map(CollaboratorView::issuedAt))
                        .allSatisfy(issuedAt -> assertThat(issuedAt).isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS))),
                () -> assertThat(boardView.stages().stream()
                        .map(StageView::state))
                        .containsExactlyInAnyOrder(Stage.StageState.TO_DO, Stage.StageState.IN_PROGRESS, Stage.StageState.DONE),
                () -> assertThat(boardView.stages().stream()
                        .flatMap(sv -> sv.tasks().stream()
                                .map(TaskView::id)))
                        .containsExactlyInAnyOrder(
                                parseHexToUUID("D1B2C3D4E5F60123456789ABCDEF0005"),
                                parseHexToUUID("D3B2C3D4E5F60123456789ABCDEF0007"),
                                parseHexToUUID("D2B2C3D4E5F60123456789ABCDEF0006"))
        );
    }

    @Test
    void test_task_is_pushed_when_created() throws ValidationException {
        executeInsertTestData();
        mockWithManager();
        CreateTaskCommand command = new CreateTaskCommand(Stage.StageState.TO_DO, "tarea de prueba", "descripción de prueba", Color.AMBER, LocalDateTime.now().plusDays(10).minusSeconds(3));
        StageView stageView = requestDispatcher.dispatch(command);
        TaskView task =  stageView.tasks().stream()
                .filter(t -> t.title().equals(command.title()))
                .findFirst()
                .orElseThrow();
        assertThat(stageView.tasks().stream()
                .map(TaskView::id))
                .containsExactly(
                        parseHexToUUID("D1B2C3D4E5F60123456789ABCDEF0005"),
                        parseHexToUUID("D3B2C3D4E5F60123456789ABCDEF0007"),
                        task.id()
                );
    }

    @Test
    void test_get_own_board_for_student_fails() throws ValidationException {
        executeInsertTestData();
        mockWithStudent1();
        assertThrowsExactly(IllegalArgumentException.class, () -> requestDispatcher.dispatch(GetBoardQuery.own()));
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
                () -> assertThat(task.createdAt()).isCloseTo(LocalDateTime.now(), within(100, ChronoUnit.MILLIS)),
                () ->  assertThat(task.title()).isEqualTo(command.title()),
                () -> assertThat(task.description()).isEqualTo(command.description()),
                () -> assertThat(stageView.state()).isEqualTo(Stage.StageState.IN_PROGRESS),
                () -> assertThat(task.color()).isEqualTo(command.color()),
                () -> assertThat(task.deadline()).isCloseTo(command.deadline(), within(1, ChronoUnit.MILLIS)),
                () -> assertThat(task.assignees()).isEmpty(),
                () -> assertThat(task.coverImage()).isEmpty(),
                () -> assertThat(task.attachments()).isEmpty()
        );
    }

    @Test
    void test_create_task_and_then_retrieve() throws ValidationException {
        executeInsertTestData();
        mockWithManager();
        CreateTaskCommand command = new CreateTaskCommand(Stage.StageState.IN_PROGRESS, "tarea de prueba", "descripción de prueba", Color.AMBER, LocalDateTime.now().plusDays(10).minusSeconds(3));
        StageView stageView = requestDispatcher.dispatch(command);
        LocalDateTime mockCreated = LocalDateTime.now();
        TaskView originalTask = stageView.tasks().stream()
                .filter(t -> t.title().equals(command.title()))
                .findFirst()
                .orElseThrow();
        TaskView task = requestDispatcher.dispatch(new GetTaskQuery(originalTask.id())).orElseThrow();

        assertAll(
                () -> assertThat(originalTask.createdAt()).isEqualTo(task.createdAt()),
                () -> assertThat(task.createdAt()).isCloseTo(mockCreated, within(100, ChronoUnit.MILLIS)),
                () ->  assertThat(task.title()).isEqualTo(command.title()),
                () -> assertThat(task.description()).isEqualTo(command.description()),
                () -> assertThat(stageView.state()).isEqualTo(Stage.StageState.IN_PROGRESS),
                () -> assertThat(task.color()).isEqualTo(command.color()),
                () -> assertThat(task.deadline()).isCloseTo(command.deadline(), within(1, ChronoUnit.MILLIS)),
                () -> assertThat(task.assignees()).isEmpty(),
                () -> assertThat(task.coverImage()).isEmpty(),
                () -> assertThat(task.attachments()).isEmpty()
        );
    }


    @Test
    void test_create_task_fails_for_student() throws ValidationException {
        executeInsertTestData();
        mockWithStudent2();
        CreateTaskCommand command = new CreateTaskCommand(Stage.StageState.IN_PROGRESS, "tarea de prueba", "descripción de prueba", Color.AMBER, LocalDateTime.now().plusDays(10).minusSeconds(3));
        UnauthorizedException ex = assertThrowsExactly(UnauthorizedException.class, () -> requestDispatcher.dispatch(command));
        assertThat(ex).hasMessageContaining("Solo dueños del tablero pueden crear tareas");
    }

    @Test
    void test_update_task() throws ValidationException {
        executeInsertTestData();
        mockWithManager();
        TaskView originalTask = requestDispatcher.dispatch(new GetTaskQuery(parseHexToUUID("D2B2C3D4E5F60123456789ABCDEF0006"))).orElseThrow();
        EditTaskCommand command = new EditTaskCommand(originalTask.id(), "new title", "new description", Color.FUCHSIA, LocalDateTime.now().plusDays(10).minusSeconds(3));
        TaskView edited = requestDispatcher.dispatch(command);
        assertAll(
                () -> assertThat(originalTask.createdAt()).isEqualTo(edited.createdAt()),
                () -> assertThat(originalTask.assignees()).isEqualTo(edited.assignees()),
                () -> assertThat(originalTask.attachments()).isEqualTo(edited.attachments()),
                () -> assertThat(originalTask.coverImage()).isEqualTo(edited.coverImage()),
                () -> assertThat(originalTask.startedAt()).isEqualTo(edited.startedAt()),
                () -> assertThat(originalTask.finishedAt()).isEqualTo(edited.finishedAt()),
                () -> assertThat(edited.title()).isEqualTo(command.title()),
                () -> assertThat(edited.description()).isEqualTo(command.description()),
                () -> assertThat(edited.color()).isEqualTo(command.color()),
                () -> assertThat(edited.deadline()).isCloseTo(command.deadline(), within(1, ChronoUnit.MILLIS))
        );
    }

    @Test
    void test_assign_student_to_task() throws ValidationException{
        executeInsertTestData();
        mockWithManager();
        UUID taskId = parseHexToUUID("D2B2C3D4E5F60123456789ABCDEF0006");
        String studentEmail = "student1@example.com";
        requestDispatcher.dispatch(new AssignStudentToTaskCommand(taskId, studentEmail ));
        TaskView task = requestDispatcher.dispatch(new GetTaskQuery(taskId)).orElseThrow();
        assertThat(task.assignees()
                .stream().map(StudentView::email))
                .containsExactlyInAnyOrder("student2@example.com", studentEmail);
    }

    @Test
    void test_remove_assignment() throws ValidationException {
        executeInsertTestData();
        mockWithManager();
        UUID taskId = parseHexToUUID("D2B2C3D4E5F60123456789ABCDEF0006");
        String studentEmail = "student2@example.com";
        requestDispatcher.dispatch(new RevokeAssignmentFromTaskCommand(taskId, studentEmail));
        TaskView task = requestDispatcher.dispatch(new GetTaskQuery(taskId)).orElseThrow();
        assertThat(task.assignees()
                .stream().map(StudentView::email))
                .isEmpty();
    }

    @Test
    void test_move_task_to_related_to_not_assignee_task_fails() throws ValidationException {
        executeInsertTestData();
        mockWithStudent1();
        UnauthorizedException ex = assertThrowsExactly(UnauthorizedException.class, () ->requestDispatcher.dispatch(MoveTaskCommand.builder()
                .task(parseHexToUUID("D1B2C3D4E5F60123456789ABCDEF0005"))
                .toStage(Stage.StageState.IN_PROGRESS)
                        .placeBefore(parseHexToUUID("D2B2C3D4E5F60123456789ABCDEF0006"))
                        .build()
                ));
        assertThat(ex).hasMessage("No colaboras en esta tarea");
    }

    @Test
    void test_move_task_as_assignee_succeeds() throws ValidationException {
        executeInsertTestData();
        mockWithStudent1();
        requestDispatcher.dispatch(MoveTaskCommand.builder()
                .task(parseHexToUUID("D1B2C3D4E5F60123456789ABCDEF0005"))
                .toStage(Stage.StageState.IN_PROGRESS)
                .placeAtStart());
    }

    @Test
    void test_move_task_as_not_assignee_fails() throws ValidationException {
        executeInsertTestData();
        mockWithStudent2();
        UnauthorizedException ex = assertThrowsExactly(UnauthorizedException.class, () ->requestDispatcher.dispatch(MoveTaskCommand.builder()
                .task(parseHexToUUID("D1B2C3D4E5F60123456789ABCDEF0005"))
                .toStage(Stage.StageState.IN_PROGRESS)
                .placeAtStart()));
        assertThat(ex).hasMessage("No colaboras en esta tarea");
    }

    @Test
    void test_move_task_between_tasks_succeeds() throws ValidationException {
        executeInsertTestData();
        mockWithManager();
        TaskView created = requestDispatcher.dispatch(new CreateTaskCommand(Stage.StageState.IN_PROGRESS, "test", "test", Color.GRAY, LocalDateTime.now().plusDays(10)))
                .tasks().stream()
                .filter(t -> t.title().equals("test"))
                .findFirst()
                .orElseThrow();
        requestDispatcher.dispatch(MoveTaskCommand.builder()
                .task(parseHexToUUID("D1B2C3D4E5F60123456789ABCDEF0005"))
                .toStage(Stage.StageState.IN_PROGRESS)
                .placeAfter(parseHexToUUID("D2B2C3D4E5F60123456789ABCDEF0006"))
                .placeBefore(created.id())
                .build()
        );
        List<StageView> updatedStage = requestDispatcher.dispatch(GetBoardQuery.own()).orElseThrow()
                .stages();
        StageView todoStage = updatedStage.stream().filter(s -> s.state().equals(Stage.StageState.TO_DO)).findFirst().orElseThrow();
        StageView inProgressStage = updatedStage.stream().filter(s -> s.state().equals(Stage.StageState.IN_PROGRESS)).findFirst().orElseThrow();
        StageView doneStage = updatedStage.stream().filter(s -> s.state().equals(Stage.StageState.DONE)).findFirst().orElseThrow();

        assertAll(
                () -> assertThat(todoStage.tasks().stream().map(TaskView::id))
                                .containsExactly(parseHexToUUID("D3B2C3D4E5F60123456789ABCDEF0007")),
                () -> assertThat(inProgressStage.tasks().stream().map(TaskView::id))
                        .containsExactly(parseHexToUUID("D2B2C3D4E5F60123456789ABCDEF0006"), parseHexToUUID("D1B2C3D4E5F60123456789ABCDEF0005"), created.id()),
                () -> assertThat(doneStage.tasks()).isEmpty()
        );
    }

    @Test
    void test_move_task_from_to_do_marks_as_started() throws ValidationException, InterruptedException {
        executeInsertTestData();
        mockWithManager();
        Thread.sleep(2000);
        UUID taskId = parseHexToUUID("D1B2C3D4E5F60123456789ABCDEF0005");
        requestDispatcher.dispatch(MoveTaskCommand.builder()
                .task(taskId)
                .toStage(Stage.StageState.IN_PROGRESS)
                .placeAtStart());
        LocalDateTime mockMoved =  LocalDateTime.now();
        Thread.sleep(500);
        TaskView movedTask = requestDispatcher.dispatch(new GetTaskQuery(taskId)).orElseThrow();
        assertThat(movedTask.startedAt()).isPresent();
        assertAll(
                () -> assertThat(movedTask.startedAt().get()).isCloseTo(mockMoved, within(300, ChronoUnit.MILLIS)),
                () -> assertThat(movedTask.finishedAt()).isEmpty()
        );
    }

    @Test
    void test_move_task_from_to_do_to_done_marks_as_started_and_finished() throws ValidationException, InterruptedException {
        executeInsertTestData();
        mockWithManager();
        Thread.sleep(2000);
        UUID taskId = parseHexToUUID("D1B2C3D4E5F60123456789ABCDEF0005");
        requestDispatcher.dispatch(MoveTaskCommand.builder()
                .task(taskId)
                .toStage(Stage.StageState.DONE)
                .placeAtStart());
        LocalDateTime mockMoved =  LocalDateTime.now();
        Thread.sleep(500);
        TaskView movedTask = requestDispatcher.dispatch(new GetTaskQuery(taskId)).orElseThrow();
        assertThat(movedTask.startedAt()).isPresent();
        assertThat(movedTask.finishedAt()).isPresent();
        assertAll(
                () -> assertThat(movedTask.startedAt().get()).isCloseTo(mockMoved, within(300, ChronoUnit.MILLIS)),
                () -> assertThat(movedTask.finishedAt().get()).isCloseTo(mockMoved, within(300, ChronoUnit.MILLIS))
        );
    }

    @Test
    void test_create_task_in_progress_marks_as_started() throws ValidationException, InterruptedException {
        executeInsertTestData();
        mockWithManager();
        TaskView tv = requestDispatcher.dispatch(new CreateTaskCommand(Stage.StageState.IN_PROGRESS, "test", "test", Color.AMBER, LocalDateTime.now().plusDays(10)))
                .tasks().stream()
                .filter(t -> t.title().equals("test"))
                .findFirst().orElseThrow();
        LocalDateTime mockMoved =  LocalDateTime.now();
        TaskView retrieved = requestDispatcher.dispatch(new GetTaskQuery(tv.id())).orElseThrow();
        assertAll(
                () -> assertThat(retrieved.startedAt()).isPresent()
        );
        assertAll(
                () -> assertThat(retrieved.startedAt().get()).isCloseTo(mockMoved, within(300, ChronoUnit.MILLIS)),
                () -> assertThat(retrieved.finishedAt()).isEmpty()
        );
    }

    @Test
    void test_create_task_in_done_marks_as_finished() throws ValidationException, InterruptedException {
        executeInsertTestData();
        mockWithManager();
        TaskView tv = requestDispatcher.dispatch(new CreateTaskCommand(Stage.StageState.DONE, "test", "test", Color.AMBER, LocalDateTime.now().plusDays(10)))
                .tasks().stream()
                .filter(t -> t.title().equals("test"))
                .findFirst().orElseThrow();
        LocalDateTime mockMoved =  LocalDateTime.now();
        TaskView retrieved = requestDispatcher.dispatch(new GetTaskQuery(tv.id())).orElseThrow();
        assertAll(
                () -> assertThat(retrieved.startedAt()).isPresent(),
                () -> assertThat(retrieved.finishedAt()).isPresent()
        );
        assertAll(
                () -> assertThat(retrieved.startedAt().get()).isCloseTo(mockMoved, within(300, ChronoUnit.MILLIS)),
                () -> assertThat(retrieved.finishedAt().get()).isCloseTo(mockMoved, within(300, ChronoUnit.MILLIS))
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

    @Test
    void test_upload_attachment_as_collaborator() throws ValidationException, IOException {
        executeInsertTestData();
        mockWithStudent1();
        try(InputStream expected = getClass().getResourceAsStream("/dev/builder/test_image.png")) {
            AddAttachmentToTaskCommand command =  new AddAttachmentToTaskCommand(
                    parseHexToUUID("D1B2C3D4E5F60123456789ABCDEF0005"),
                    "test_imageFilename.png",
                    expected);
            FileView file = requestDispatcher.dispatch(command);
            assertAll(
                    () -> assertThat(file.id()).isNotNull(),
                    () -> assertThat(file.mimeType()).isEqualTo(StoredFile.MimeType.PNG),
                    () -> assertThat(file.name()).isEqualTo(command.filename())
            );
        }
    }

    @Test
    void test_upload_attachment_and_find_in_task() throws ValidationException, IOException {
        executeInsertTestData();
        mockWithManager();
        try(InputStream expected = getClass().getResourceAsStream("/dev/builder/test_image.png")) {
            UUID taskId = parseHexToUUID("D1B2C3D4E5F60123456789ABCDEF0005");
            AddAttachmentToTaskCommand command =  new AddAttachmentToTaskCommand(
                    taskId,
                    "test_imageFilename.png",
                    expected);
            requestDispatcher.dispatch(command);
            TaskView task = requestDispatcher.dispatch(new GetTaskQuery(taskId)).orElseThrow();
            FileView file = task.attachments().stream()
                    .filter(a -> a.name().equals(command.filename()))
                    .findFirst()
                    .orElseThrow();
            assertAll(
                    () -> assertThat(file.id()).isNotNull(),
                    () -> assertThat(file.mimeType()).isEqualTo(StoredFile.MimeType.PNG),
                    () -> assertThat(file.name()).isEqualTo(command.filename())
            );
        }
    }

    @Test
    void test_remove_attachment() throws ValidationException, IOException {
        executeInsertTestData();
        mockWithManager();
        UUID taskId = parseHexToUUID("D2B2C3D4E5F60123456789ABCDEF0006");
        requestDispatcher.dispatch(new RemoveAttachmentCommand(taskId, parseHexToUUID("E2B2C3D4E5F60123456789ABCDEF0009")));
        TaskView task = requestDispatcher.dispatch(new GetTaskQuery(taskId)).orElseThrow();
        assertThat(task.attachments()).isEmpty();
    }

    @Test
    void test_remove_task_cover() throws ValidationException, IOException {
        executeInsertTestData();
        mockWithManager();
        UUID taskId = parseHexToUUID("D1B2C3D4E5F60123456789ABCDEF0005");
        requestDispatcher.dispatch(new RemoveCoverImageCommand(taskId));
        TaskView task = requestDispatcher.dispatch(new GetTaskQuery(taskId)).orElseThrow();
        assertThat(task.coverImage()).isEmpty();
    }

    @Test
    void test_upload_cover_image() throws ValidationException, IOException {
        executeInsertTestData();
        mockWithManager();
        UUID taskId = parseHexToUUID("D2B2C3D4E5F60123456789ABCDEF0006");
        try(InputStream expected = getClass().getResourceAsStream("/dev/builder/test_image.png")) {
            FileView cover = requestDispatcher.dispatch(new AttachCoverImageToTaskCommand(taskId, "test_imageFilename.png", expected));
            assertAll(
                    () -> assertThat(cover.id()).isNotNull(),
                    () -> assertThat(cover.mimeType()).isEqualTo(StoredFile.MimeType.PNG),
                    () -> assertThat(cover.name()).isEqualTo("test_imageFilename.png")
            );

        }
    }

    @Test
    void test_upload_cover_image_and_find_in_task() throws ValidationException, IOException {
        executeInsertTestData();
        mockWithManager();
        UUID taskId = parseHexToUUID("D2B2C3D4E5F60123456789ABCDEF0006");
        try(InputStream expected = getClass().getResourceAsStream("/dev/builder/test_image.png")) {
            FileView cover = requestDispatcher.dispatch(new AttachCoverImageToTaskCommand(taskId, "test_imageFilename.png", expected));

            TaskView task = requestDispatcher.dispatch(new GetTaskQuery(taskId)).orElseThrow();
            assertThat(task.coverImage().map(FileView::id).get()).isEqualTo(cover.id());
        }
    }

    @Test
    void test_upload_cover_image_while_exists_one_fails() throws ValidationException, IOException {
        executeInsertTestData();
        mockWithManager();
        UUID taskId = parseHexToUUID("D1B2C3D4E5F60123456789ABCDEF0005");
        try(InputStream expected = getClass().getResourceAsStream("/dev/builder/test_image.png")) {
            IllegalStateException fd = assertThrowsExactly(IllegalStateException.class, () -> requestDispatcher.dispatch(new AttachCoverImageToTaskCommand(taskId, "test_imageFilename.png", expected)));
            assertThat(fd.getMessage()).isEqualTo("La tarea ya tiene una imágen de carátula");

        }
    }

    @Test
    void test_upload_attachment_and_load_file() throws ValidationException, IOException {
        executeInsertTestData();
        mockWithManager();
        try(InputStream expected = getClass().getResourceAsStream("/dev/builder/test_image.png")) {
            byte[] data = expected.readAllBytes();
            UUID taskId = parseHexToUUID("D1B2C3D4E5F60123456789ABCDEF0005");
            AddAttachmentToTaskCommand command =  new AddAttachmentToTaskCommand(
                    taskId,
                    "test_imageFilename.png",
                    new ByteArrayInputStream(data));
            FileView uploaded = requestDispatcher.dispatch(command);

            try(InputStream loaded = requestDispatcher.dispatch(new LoadAttachmentQuery(uploaded.id())).orElseThrow()){
                assertThat(data).isEqualTo(loaded.readAllBytes());
            }
        }
    }

    @Test
    void test_upload_file_with_unknown_fails() throws ValidationException, IOException {
        executeInsertTestData();
        mockWithStudent2();
        byte[] heavyLoad = new byte[100_000_000];
        try(InputStream expected = new ByteArrayInputStream(heavyLoad)) {
            FileUnsupportedException ex = assertThrowsExactly(FileUnsupportedException.class, () -> requestDispatcher.dispatch(
                    new AddAttachmentToTaskCommand(
                            parseHexToUUID("D2B2C3D4E5F60123456789ABCDEF0006"),
                            "heavy_data.png",
                            expected)
            ));
            assertThat(ex).hasMessage("El tipo de archivo no está soportado");
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
