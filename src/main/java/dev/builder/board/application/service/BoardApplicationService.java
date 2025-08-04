package dev.builder.board.application.service;

import dev.builder.auth.application.service.UnauthorizedException;
import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.auth.infrastructure.Role;
import dev.builder.board.application.command.*;
import dev.builder.board.application.query.GetBoardQuery;
import dev.builder.board.application.query.GetTaskQuery;
import dev.builder.board.application.query.LoadAttachmentQuery;
import dev.builder.board.application.query.LoadCoverImageQuery;
import dev.builder.board.application.view.BoardView;
import dev.builder.board.application.view.FileView;
import dev.builder.board.application.view.StageView;
import dev.builder.board.application.view.TaskView;
import dev.builder.board.domain.exception.BoardNotFoundException;
import dev.builder.board.domain.exception.FileNotFoundException;
import dev.builder.board.domain.exception.StageNotFoundException;
import dev.builder.board.domain.exception.TaskNotFoundException;
import dev.builder.board.domain.model.*;
import dev.builder.board.domain.port.in.BoardService;
import dev.builder.board.domain.port.in.FileService;
import dev.builder.board.domain.port.in.TaskService;
import dev.builder.board.domain.port.out.*;
import dev.builder.board.domain.service.BoardCreationService;
import dev.builder.board.domain.service.TaskStageChangerService;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.persistence.*;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import dev.builder.usermanagement.domain.exception.StudentNotFoundException;
import dev.builder.usermanagement.domain.model.Manager;
import dev.builder.usermanagement.domain.model.Student;
import dev.builder.usermanagement.domain.port.out.ManagerRepository;
import dev.builder.usermanagement.domain.port.out.StudentRepository;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.util.*;

import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.runInTransaction;

@Bean
public class BoardApplicationService implements BoardService, TaskService, FileService {


    private static final Logger log = LoggerFactory.getLogger(BoardApplicationService.class);

    private final BoardRepository boardRepository;
    private final StageRepository stageRepository;
    private final AttachmentRepository attachmentRepository;
    private final ImageRepository imageRepository;
    private final StoredFileRepository storedFileRepository;
    private final StudentRepository studentRepository;
    private final ManagerRepository managerRepository;

    private final BoardCreationService boardCreationService;
    private final MessageLocalizer messageLocalizer;
    private final MimeTypeGenerator mimeTypeGenerator;
    private final SessionContext sessionContext;
    private final ConnectionManager connectionManager;

    private final BoardViewMapper boardViewMapper;
    private final StageViewMapper stageViewMapper;
    private final TaskViewMapper taskViewMapper;
    private final  FileViewMapper fileViewMapper;

    @Inject
    public BoardApplicationService(BoardRepository boardRepository, StageRepository stageRepository, AttachmentRepository attachmentRepository, ImageRepository imageRepository, StoredFileRepository storedFileRepository, StudentRepository studentRepository, ManagerRepository managerRepository, BoardCreationService boardCreationService, MessageLocalizer messageLocalizer, MimeTypeGenerator mimeTypeGenerator, SessionContext sessionContext, ConnectionManager connectionManager, BoardViewMapper boardViewMapper, StageViewMapper stageViewMapper, TaskViewMapper taskViewMapper, FileViewMapper fileViewMapper) {
        this.boardRepository = boardRepository;
        this.stageRepository = stageRepository;
        this.attachmentRepository = attachmentRepository;
        this.imageRepository = imageRepository;
        this.storedFileRepository = storedFileRepository;
        this.studentRepository = studentRepository;
        this.managerRepository = managerRepository;
        this.boardCreationService = boardCreationService;
        this.messageLocalizer = messageLocalizer;
        this.mimeTypeGenerator = mimeTypeGenerator;
        this.sessionContext = sessionContext;
        this.connectionManager = connectionManager;
        this.boardViewMapper = boardViewMapper;
        this.stageViewMapper = stageViewMapper;
        this.taskViewMapper = taskViewMapper;
        this.fileViewMapper = fileViewMapper;
    }

    @Override
    public StageView createTask(CreateTaskCommand command) {
        sessionContext.requireRole(Role.MANAGER, "Solo dueños del tablero pueden crear tareas");
        Board.Id boardId = new Board.Id(new Manager.Id(sessionContext.getCurrentUser()));
        Stage.Id stageId = new Stage.Id(boardId, command.stage());

        return  runInTransaction(
                connectionManager,
                log,
                conn -> {
                    Stage containerStage = stageRepository.findById(stageId, conn).orElseThrow(
                            () -> new StageNotFoundException(messageLocalizer, stageId)
                    );

                    containerStage.createTask(
                            new Task.Id(UUIDGenerator.generateUUID()),
                            new Title(command.title()),
                            new TaskDescription(command.description()),
                            command.color(),
                            new Deadline(command.deadline())
                    );
                    Stage savedStage = stageRepository.save(containerStage, conn);
                    return getStageInfo(savedStage, conn);
                }
        );
    }

    @Override
    public StageView deleteTask(DeleteTaskCommand command) {
        sessionContext.requireRole(Role.MANAGER, "Solo dueños del tablero pueden eliminar tareas");
        Board.Id boardId = new Board.Id(new Manager.Id(sessionContext.getCurrentUser()));
        Task.Id taskId = new Task.Id(command.taskId());

        return runInTransaction(
                connectionManager,
                log,
                conn -> {
                    Set<Stage> boardStages = stageRepository.findByBoard(boardId, conn);
                    Stage containerStage = stageRepository.findByBoardAndContainingTask(boardId, taskId)
                                    .orElseThrow(
                                            () -> new TaskNotFoundException(messageLocalizer)
                                    );
                    containerStage.removeTask(taskId);
                    Stage savedStage = stageRepository.save(containerStage);
                    return getStageInfo(savedStage, conn);
                }
        );

    }

    @Override
    public void addCollaborator(AddCollaboratorCommand command) {
        sessionContext.requireRole(Role.MANAGER, "Solo dueños del tablero pueden agregar colaboradores");
        Board.Id boardId = new Board.Id(new Manager.Id(sessionContext.getCurrentUser()));
        Student.Id collaboratorId = new Student.Id(command.email());

        runInTransaction(
                connectionManager,
                log,
                conn -> {
                    Student collaborator = studentRepository.findById(collaboratorId, conn)
                            .orElseThrow(
                                    () -> new StudentNotFoundException(messageLocalizer, collaboratorId)
                            );
                    Board board = boardRepository.findById(boardId)
                            .orElseThrow(
                                    () -> new BoardNotFoundException(messageLocalizer, boardId)
                            );
                    board.addCollaborator(collaborator.id());
                    boardRepository.save(board, conn);
                    return null;
                }
        );
    }

    @Override
    public BoardView moveTask(MoveTaskCommand command) {
        return runInTransaction(
                connectionManager,
                log,
                conn -> {
                    Task.Id taskId = new Task.Id(command.taskId());
                    Stage sourceStage = assertCanEditTask(taskId, conn);
                    Stage targetStage = stageRepository.findById(new Stage.Id(sourceStage.boardId(), command.stage()), conn)
                                    .orElseThrow();
                    Task previousTask = command.placeAtEnd()
                                    .flatMap(s -> targetStage.tasks().isEmpty() ? Optional.empty() : Optional.of(targetStage.tasks().last()))
                                    .orElse(
                                        command.previousTask()
                                        .flatMap(prev -> targetStage.getTask(new Task.Id(prev)))
                                        .orElse(null)
                                    );
                    Task nextTask =  command.placeAtEnd()
                            .flatMap(s -> targetStage.tasks().isEmpty() ? Optional.empty() : Optional.of(targetStage.tasks().first()))
                            .orElse(
                                    command.nextTask()
                                            .flatMap(next -> targetStage.getTask(new Task.Id(next)))
                                            .orElse(null)
                            );



                    boolean moved = TaskStageChangerService.moveTask(sourceStage, targetStage, sourceStage.getTask(taskId).get(), previousTask, nextTask);
                    if(moved){
                        stageRepository.save(sourceStage, conn);
                        stageRepository.save(targetStage, conn);
                    }

                    Board containerBoard = boardRepository.findById(sourceStage.boardId(), conn)
                            .orElseThrow();
                    return getBoardInfo(containerBoard, conn);
                }
        );
    }

    @Override
    public Optional<BoardView> getBoard(GetBoardQuery command) {
        if(command.boardOwner().isEmpty() && sessionContext.hasRole(Role.STUDENT)) {
            throw new IllegalArgumentException("Se debe especificar el tablero a obtener");
        }else if(command.boardOwner().isPresent() && sessionContext.hasRole(Role.MANAGER)) {
            Board.Id queryBoard =  new Board.Id(new Manager.Id(command.boardOwner().get()));
            Board.Id currentUserBoard = new Board.Id(new Manager.Id(sessionContext.getCurrentUser()));
            if(!queryBoard.equals(currentUserBoard)){
                throw new UnauthorizedException("Solo puedes ver el tablero del que eres dueño");
            }
        } else if(sessionContext.hasRole(Role.ADMIN)){
            throw new UnauthorizedException("Los administradores no pueden acceder a la información del tablero");
        }

        Board.Id boardId;
        if(sessionContext.hasRole(Role.MANAGER)) {
            boardId = new Board.Id(new Manager.Id(sessionContext.getCurrentUser()));
        } else {
            boardId = new Board.Id(new Manager.Id(command.boardOwner().get()));
        }
        return runInTransaction(
                connectionManager,
                log,
                conn -> boardRepository.findById(boardId, conn).map(b -> getBoardInfo(b, conn))
        );
    }

    @Override
    public Optional<InputStream> openAttachment(LoadAttachmentQuery query) {
        return runInTransaction(
                connectionManager,
                log,
                conn -> {
                    Attachment.Id attachmentId = new Attachment.Id(query.attachmentId());
                    Attachment attachment = attachmentRepository.findById(attachmentId, conn)
                            .orElseThrow(
                                    () -> new FileNotFoundException(messageLocalizer)
                            );
                    assertCanEditTask(attachment.attachedTo(), conn);
                    return storedFileRepository.openFile(attachmentId, conn);
                }
        );
    }

    @Override
    public Optional<InputStream> openCoverImage(LoadCoverImageQuery query) {
        return runInTransaction(
                connectionManager,
                log,
                conn -> {
                    Image.Id imageId = new Image.Id(query.imageId());
                    Image image = imageRepository.findById(imageId, conn)
                            .orElseThrow(
                                    () -> new FileNotFoundException(messageLocalizer)
                            );
                    assertCanEditTask(image.attachedTo(), conn);
                    return storedFileRepository.openFile(imageId, conn);
                }
        );
    }

    @Override
    public void createOwnBoard(Connection connection) {
        sessionContext.requireRole(Role.MANAGER, "Se requiere ser Manager para crear el tablero");
        Board board = boardCreationService.createBoardForOwner(new  Manager.Id(sessionContext.getCurrentUser()));
        boardRepository.save(board, connection);
    }

    @Override
    public TaskView editTask(EditTaskCommand command) {
        Task.Id taskId = new Task.Id(command.id());
        return findTaskInOwnBoardAndDo(
                taskId,
                (task, conn) -> {
                    task.changeTitle(new Title(command.title()));
                    task.changeDescription(new TaskDescription(command.description()));
                    task.markWithColor(command.color());
                    task.changeDeadline(new Deadline(command.deadline()));
                    return true;
                },
                (stage, conn) -> getTaskInfo(stage.getTask(taskId).orElseThrow(), conn)
        );
    }

    @Override
    public TaskView assignStudent(AssignStudentToTaskCommand command) {
        Task.Id taskId = new Task.Id(command.taskId());
        return findTaskInOwnBoardAndDo(
                taskId,
                (task, conn) -> {
                    Student.Id studentId = new Student.Id(command.studentEmail());
                    studentRepository.findById(studentId, conn)
                            .orElseThrow(
                                    () -> new StudentNotFoundException(messageLocalizer, studentId)
                            );
                    return task.assignStudent(studentId);
                },
                (stage, conn) -> getTaskInfo(stage.getTask(taskId).orElseThrow(), conn)
        );
    }

    @Override
    public void removeAssignedStudent(RevokeAssignmentFromTaskCommand command) {
        Task.Id taskId = new Task.Id(command.taskId());
        findTaskInOwnBoardAndDo(
                taskId,
                (task, conn) -> {
                    Student.Id studentId = new Student.Id(command.studentEmail());
                    studentRepository.findById(studentId, conn)
                            .orElseThrow(
                                    () -> new StudentNotFoundException(messageLocalizer, studentId)
                            );
                    return task.revokeAssignation(studentId);
                },
                (stage, conn) -> null
        );
    }

    @Override
    public FileView attachCoverImage(AttachCoverImageToTaskCommand command) {
        Task.Id taskId = new Task.Id(command.taskId());
        return runInTransaction(
                connectionManager,
                log,
                conn -> {
                    Stage containerStage = assertCanEditTask(taskId, conn);
                    Task task = containerStage.getTask(taskId).get();

                    if(task.coverImage().isPresent()) {
                        throw new IllegalStateException("La tarea ya tiene una imágen de carátula");
                    }
                    Image image = createImage(
                            new Image.Filename(command.filename()),
                            taskId,
                            command.imageStream(),
                            conn);
                    return fileViewMapper.toView(image);
                }
        );
    }

    @Override
    public FileView addAttachment(AddAttachmentToTaskCommand command) {
        Task.Id taskId = new Task.Id(command.taskId());
        return runInTransaction(
                connectionManager,
                log,
                conn -> {
                    Stage containerStage = assertCanEditTask(taskId, conn);
                    Attachment attachment = createAttachment(
                            new Attachment.Filename(command.filename()),
                            taskId,
                            command.fileStream(),
                            conn);
                    return fileViewMapper.toView(attachment);
                }
        );
    }

    @Override
    public void removeAttachment(RemoveAttachmentCommand command) {
        Task.Id taskId = new Task.Id(command.taskId());
        runInTransaction(
                connectionManager,
                log,
                conn -> {
                    Stage containerStage = assertCanEditTask(taskId, conn);
                    Attachment.Id attachmentId = new Attachment.Id(command.attachmentId());
                    Attachment attachment = attachmentRepository.findById(attachmentId, conn).
                            orElseThrow(() -> new FileNotFoundException(messageLocalizer));
                    if(!containerStage.getTask(taskId).get().attachments().contains(attachmentId)) {
                        throw new FileNotFoundException(messageLocalizer);
                    }
                    attachmentRepository.delete(attachment, conn);
                    return null;
                }
        );
    }

    @Override
    public void deleteCoverImage(RemoveCoverImageCommand command) {
        Task.Id taskId = new Task.Id(command.taskId());
        runInTransaction(
                connectionManager,
                log,
                conn -> {
                    Stage containerStage = assertCanEditTask(taskId, conn);
                    Task task = containerStage.getTask(taskId).get();
                    if(task.coverImage().isEmpty()) {
                        throw new FileNotFoundException(messageLocalizer);
                    }
                    Image image = imageRepository.findById(task.coverImage().get(), conn).
                            orElseThrow(() -> new FileNotFoundException(messageLocalizer));

                    imageRepository.delete(image, conn);
                    return null;
                }
        );
    }

    @Override
    public Optional<TaskView> getTask(GetTaskQuery query) {
        Task.Id taskId = new Task.Id(query.taskId());
        return runInTransaction(
                connectionManager,
                log,
                conn -> {
                    if(!sessionContext.hasAnyRole(Role.MANAGER, Role.STUDENT)){
                      return Optional.empty();
                    }
                    Optional<Stage> containingStage = stageRepository.findByContainingTask(taskId, conn);

                    if(containingStage.isEmpty()) {
                        return Optional.empty();
                    }

                    if(sessionContext.hasRole(Role.MANAGER)) {
                        Board.Id boardId = new Board.Id(new Manager.Id(sessionContext.getCurrentUser()));
                        if(!boardId.equals(containingStage.get().boardId())){
                            return Optional.empty();
                        }
                    }

                    Task task = containingStage.get().getTask(taskId).get();
                    if(sessionContext.hasRole(Role.STUDENT)) {
                        if(!task.assignedStudents().contains(new Student.Id(sessionContext.getCurrentUser()))) {
                            return Optional.empty();
                        }
                    }
                    return Optional.of(getTaskInfo(task, conn));
                }
        );
    }

    private Stage assertCanEditTask(Task.Id taskId, Connection connection) {
        if(!sessionContext.hasAnyRole(Role.MANAGER, Role.STUDENT)){
            throw new UnauthorizedException("Los administradores no pueden ver o modificar información de tareas");
        }
        Stage containingStage = stageRepository.findByContainingTask(taskId, connection)
                .orElseThrow(() -> new TaskNotFoundException(messageLocalizer));

        if(sessionContext.hasRole(Role.MANAGER)) {
            Board.Id boardId = new Board.Id(new Manager.Id(sessionContext.getCurrentUser()));
            if(!boardId.equals(containingStage.boardId())){
                throw new UnauthorizedException("Esta tarea no pertenece a tu tablero");
            }
        }

        if(sessionContext.hasRole(Role.STUDENT)) {
            Task task = containingStage.getTask(taskId).orElseThrow();
            if(!task.assignedStudents().contains(new Student.Id(sessionContext.getCurrentUser()))) {
                throw new UnauthorizedException("No colaboras en esta tarea");
            }
        }
        return containingStage;
    }

    private Attachment createAttachment(Attachment.Filename filename, Task.Id forTask, InputStream data, Connection connection) {
        Attachment attachment = new Attachment(
                new Attachment.Id(UUIDGenerator.generateUUID()),
                forTask,
                filename,
                StoredFile.MimeType.fromValue(mimeTypeGenerator.generateMimeType(data))
        );
        return attachmentRepository.save(attachment, data, connection);
    }

    private Image createImage(Image.Filename filename, Task.Id forTask, InputStream data, Connection connection) {
        Image image = new Image(
                new Image.Id(UUIDGenerator.generateUUID()),
                forTask,
                filename,
                StoredFile.MimeType.fromValue(mimeTypeGenerator.generateMimeType(data))
        );
        return imageRepository.save(image, data, connection);
    }

    private <T> T findTaskInOwnBoardAndDo(Task.Id taskId, TransactionalOperation<Task, Boolean> operation, TransactionalOperation<Stage, T> resultMapper) {
        sessionContext.requireRole(Role.MANAGER, "Solo un manager puede realizar esta operación");
        return runInTransaction(
                connectionManager,
                log,
                conn -> {
                    Board.Id currentUserBoard = new Board.Id(new Manager.Id(sessionContext.getCurrentUser()));
                    Stage containerStage = stageRepository.findByBoardAndContainingTask(currentUserBoard, taskId)
                            .orElseThrow(() -> new TaskNotFoundException(messageLocalizer));
                    Task task = containerStage.getTask(taskId).orElseThrow();

                    boolean changed = operation.execute(task, conn);
                    if(changed) {
                        containerStage = stageRepository.save(containerStage, conn);
                    }
                    return resultMapper.execute(containerStage, conn);
                }
        );
    }


    private record TaskData(
            Map<Task.Id, Set<Student>> assignees,
            Map<Task.Id, Image> coverImages,
            Map<Task.Id, Set<Attachment>> attachments
    ) {}

    @Contract("_, _ -> new")
    private @NotNull TaskData loadTaskData(@NotNull Set<Task> tasks, Connection connection) {
        Map<Task.Id, Set<Student>> assignees = new HashMap<>();
        Map<Task.Id, Image> coverImages = new HashMap<>();
        Map<Task.Id, Set<Attachment>> attachments = new HashMap<>();

        for (Task task : tasks) {
            Task.Id taskId = task.id();
            assignees.put(taskId, studentRepository.findAssignedToTask(taskId, connection));
            coverImages.put(taskId, imageRepository.findByTaskId(taskId, connection).orElse(null));
            attachments.put(taskId, attachmentRepository.findByTaskId(taskId, connection));
        }

        return new TaskData(assignees, coverImages, attachments);
    }

    private TaskView getTaskInfo(@NotNull Task task, Connection connection) {
        return taskViewMapper.toView(
                task,
                studentRepository.findAssignedToTask(task.id(), connection),
                imageRepository.findByTaskId(task.id(), connection).orElse(null),
                attachmentRepository.findByTaskId(task.id(), connection)
        );
    }

    private StageView getStageInfo(@NotNull Stage stage, Connection connection) {
        TaskData taskData = loadTaskData(stage.tasks(), connection);

        return stageViewMapper.toView(
                stage,
                taskData.assignees(),
                taskData.coverImages(),
                taskData.attachments()
        );
    }

    private BoardView getBoardInfo(Board board, Connection connection) {
        Set<Stage> stages = stageRepository.findByBoard(board.id(), connection);
        Set<Student> collaborators = studentRepository.findCollaboratingOnBoard(board.id(), connection);

        Map<Stage.Id, Map<Task.Id, Set<Student>>> assigneesByStage = new HashMap<>();
        Map<Stage.Id, Map<Task.Id, Image>> coverImageByStage = new HashMap<>();
        Map<Stage.Id, Map<Task.Id, Set<Attachment>>> attachmentsByStage = new HashMap<>();

        for (Stage stage : stages) {
            TaskData taskData = loadTaskData(stage.tasks(), connection);
            assigneesByStage.put(stage.id(), taskData.assignees());
            coverImageByStage.put(stage.id(), taskData.coverImages());
            attachmentsByStage.put(stage.id(), taskData.attachments());
        }

        return boardViewMapper.toView(
                board,
                collaborators,
                stages,
                assigneesByStage,
                coverImageByStage,
                attachmentsByStage
        );
    }

}
