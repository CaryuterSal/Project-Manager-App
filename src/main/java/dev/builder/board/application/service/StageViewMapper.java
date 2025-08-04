package dev.builder.board.application.service;

import dev.builder.board.application.view.FileView;
import dev.builder.board.application.view.StageView;
import dev.builder.board.domain.model.*;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.domain.model.Student;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Bean
public class StageViewMapper {

    private final TaskViewMapper taskViewMapper;

    @Inject
    public StageViewMapper(TaskViewMapper taskViewMapper) {
        this.taskViewMapper = taskViewMapper;
    }

    public StageView toView(@NotNull Stage stage, Map<Task.Id, Set<Student>> assigneesByTask, Map<Task.Id, Image> coverImage, Map<Task.Id, Set<Attachment>> attachments) {
        return new StageView(
                stage.state(),
                stage.tasks().stream()
                        .map(task -> taskViewMapper.toView(
                                task,
                                Optional.ofNullable(assigneesByTask.get(task.id())).orElseThrow(),
                                coverImage.get(task.id()),
                                Optional.ofNullable(attachments.get(task.id())).orElseThrow()))
                        .toList()
        );
    }
}
