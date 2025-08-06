package dev.builder.board.infrastructure;

import dev.builder.board.domain.model.*;
import dev.builder.core.infrastructure.persistence.CommonMappers;
import dev.builder.usermanagement.domain.model.Manager;
import dev.builder.usermanagement.domain.model.Student;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static dev.builder.core.infrastructure.persistence.CommonMappers.groupResultSetByKey;

public class StageJdbcMapper {
    public enum StageColumns{
        BOARD_ID("board"),
        STAGE("stage");
        private final String columnName;
        StageColumns(String columnName) {
            this.columnName = columnName;
        }
        public String columnName() {return columnName;}
    }

    public static @NotNull List<Stage> rowToStages(ResultSet resultSet) throws SQLException {
        Map<Stage.Id, Set<Task>> tasksByStage = extractTasksByStage(resultSet);
        List<Stage> stages = new ArrayList<>();
        do {
            Stage.Id id = extractStageId(resultSet);
            stages.add(new Stage(id, tasksByStage.getOrDefault(id, new HashSet<>())));
        } while (resultSet.next());
        return stages;
    }

    public static @NotNull Stage rowToStage(ResultSet resultSet) throws SQLException {
        Stage.Id id = extractStageId(resultSet);
        Set<Task> tasks = extractTasks(resultSet, id);
        return new Stage(id, tasks);
    }

    public static @NotNull Stage rowToStage(ResultSet resultSet, Set<Task> tasks) throws SQLException {
        Board.Id boardId = new Board.Id(new Manager.Id(resultSet.getString(StageColumns.BOARD_ID.columnName)));
        StageName stageName = StageName.fromDbValue(resultSet.getString(StageColumns.STAGE.columnName));
        return new Stage(
                new Stage.Id(boardId, stageName.asDomain()),
                tasks
        );
    }

    public static Set<Task> extractTasks(ResultSet resultSet, Stage.Id stage) throws SQLException {
        return extractTasksByStage(resultSet).getOrDefault(stage, new HashSet<>());
    }

    public static @NotNull Map<Stage.Id, Set<Task>> extractTasksByStage(ResultSet resultSet) throws SQLException {
        Map<Task.Id, Set<Attachment.Id>> attachments = TaskJdbcMapper.extractAttachmentsByTask(resultSet);
        Map<Task.Id, Set<Student.Id>> assignations =  TaskJdbcMapper.extractAssignationsByTask(resultSet);
        return groupResultSetByKey(
                resultSet,
                StageJdbcMapper::extractStageId,
                r -> {
                    Task.Id taskId = TaskJdbcMapper.extractTaskId(r);
                    return TaskJdbcMapper.rowToTask(
                            r,
                            taskId,
                            attachments.getOrDefault(taskId, new HashSet<>()),
                            assignations.getOrDefault(taskId, new HashSet<>())
                    );
                }
        );
    }

    public static Stage.@NotNull Id extractStageId(ResultSet resultSet) throws SQLException {
        Board.Id boardId = new Board.Id(new Manager.Id(resultSet.getString(StageColumns.BOARD_ID.columnName)));
        StageName stageName = StageName.fromDbValue(resultSet.getString(StageColumns.STAGE.columnName));
        return new Stage.Id(boardId, stageName.asDomain());
    }
}
