package dev.builder.board.infrastructure;

import dev.builder.board.domain.model.*;
import dev.builder.core.infrastructure.persistence.UUIDMapper;
import dev.builder.usermanagement.domain.model.Manager;
import dev.builder.usermanagement.domain.model.Student;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

import static dev.builder.core.infrastructure.persistence.CommonMappers.groupResultSetByKey;

public class TaskJdbcMapper {

    public enum TaskColumns {
        ID("id"),
        BOARD("board"),
        STAGE("stage"),
        TITLE("title"),
        ORDER("task_order"),
        DESCRIPTION("description"),
        CREATED_AT("created_at"),
        STARTED_AT("started_at"),
        FINISHED_AT("finished_at"),
        DEADLINE("deadline"),
        COLOR("color"),
        COVER_IMAGE("cover_image"),
        ATTACHMENT("attachment"),
        ASSIGNED_TO("assigned_to");

        private final String columnName;

        public String columnName() {
            return columnName;
        }

        TaskColumns(String columnName) {
            this.columnName = columnName;
        }
    }

    public static @NotNull List<Task> rowToTasks(ResultSet resultSet) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        Map<Task.Id, Set<Attachment.Id>> attachments = extractAttachmentsByTask(resultSet);
        Map<Task.Id, Set<Student.Id>> assignations =  extractAssignationsByTask(resultSet);
        do {
            Task.Id id = extractTaskId(resultSet);
            tasks.add(rowToTask(
                    resultSet,
                    id,
                    attachments.getOrDefault(id, new HashSet<>()),
                    assignations.getOrDefault(id, new HashSet<>()))
            );
        } while(resultSet.next());
        return tasks;
    }

    public static @NotNull Task rowToTask(ResultSet resultSet) throws SQLException {
        Task.Id id = extractTaskId(resultSet);
        return rowToTask(resultSet, id, extractAttachments(resultSet, id), extractAssignations(resultSet, id));
    }

    static @NotNull Task rowToTask(ResultSet resultSet, Task.Id id, Set<Attachment.Id> attachments, Set<Student.Id> taskAssignations) throws SQLException {
        Title title = new Title(resultSet.getString(TaskColumns.TITLE.columnName));
        Stage.Id stage = extractStageId(resultSet);
        TaskDescription description = new TaskDescription(resultSet.getString(TaskColumns.DESCRIPTION.columnName));
        Order order = new Order(resultSet.getDouble(TaskColumns.ORDER.columnName));
        LocalDateTime createdAt = resultSet.getObject(TaskColumns.CREATED_AT.columnName, OffsetDateTime.class).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        OffsetDateTime startedAtOffset = resultSet.getObject(TaskColumns.STARTED_AT.columnName, OffsetDateTime.class);
        LocalDateTime startedAt = startedAtOffset == null ? null : startedAtOffset.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        OffsetDateTime finishedAtOffset = resultSet.getObject(TaskColumns.FINISHED_AT.columnName, OffsetDateTime.class);
        LocalDateTime finishedAt = finishedAtOffset == null ? null : finishedAtOffset.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        Deadline deadline = new Deadline(resultSet.getObject(TaskColumns.DEADLINE.columnName, OffsetDateTime.class).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
        Color color = Color.fromName(resultSet.getString(TaskColumns.COLOR.columnName));
        UUID coverImage = UUIDMapper.extractUUID(resultSet,TaskColumns.COVER_IMAGE.columnName);
        return new Task(
                id,
                stage,
                title,
                description,
                createdAt,
                order,
                color,
                deadline,
                startedAt == null ? null: new ExecutionPeriod(startedAt, finishedAt),
                coverImage == null ? null : new Image.Id(coverImage),
                attachments,
                taskAssignations
        );
    }

    public static Stage.Id extractStageId(ResultSet resultSet) throws SQLException {
        Board.Id board = new Board.Id(new Manager.Id(resultSet.getString(TaskColumns.BOARD.columnName)));
        StageName stageState = StageName.fromDbValue(resultSet.getString(TaskColumns.STAGE.columnName));
        return new Stage.Id(board, stageState.asDomain());
    }

    public static Set<Attachment.Id> extractAttachments(@NotNull ResultSet resultSet, Task.Id task) throws SQLException {
        return extractAttachmentsByTask(resultSet).getOrDefault(task, Collections.emptySet());
    }

    public static @NotNull Map<Task.Id, Set<Attachment.Id>> extractAttachmentsByTask(ResultSet rs) throws SQLException {
        return groupResultSetByKey(
                rs,
                TaskJdbcMapper::extractTaskId,
                r -> {
                    UUID uuid = UUIDMapper.extractUUID(rs, TaskColumns.ATTACHMENT.columnName());
                    if(uuid == null) return null;
                    return new Attachment.Id(uuid);
                }
        );
    }

    public static Set<Student.Id> extractAssignations(@NotNull ResultSet resultSet, Task.Id task) throws SQLException {
        return extractAssignationsByTask(resultSet).getOrDefault(task, Collections.emptySet());
    }

    public static @NotNull Map<Task.Id, Set<Student.Id>> extractAssignationsByTask(ResultSet rs) throws SQLException {
        return groupResultSetByKey(
                rs,
                TaskJdbcMapper::extractTaskId,
                r -> {
                    String email = r.getString(TaskColumns.ASSIGNED_TO.columnName);
                    if(email == null) return null;
                    return new Student.Id(email);
                }
        );
    }

    @Contract("_ -> new")
    public static Task.@NotNull Id extractTaskId(@NotNull ResultSet resultSet) throws SQLException {
        return new Task.Id(
                UUIDMapper.extractUUID(resultSet, TaskColumns.ID.columnName)
        );
    }

}
