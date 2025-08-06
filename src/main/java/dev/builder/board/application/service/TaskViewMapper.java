package dev.builder.board.application.service;

import dev.builder.board.application.view.TaskView;
import dev.builder.board.domain.model.Attachment;
import dev.builder.board.domain.model.ExecutionPeriod;
import dev.builder.board.domain.model.Image;
import dev.builder.board.domain.model.Task;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.service.UserViewMapper;
import dev.builder.usermanagement.domain.model.Student;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Bean
public class TaskViewMapper {

    private final UserViewMapper userViewMapper;
    private final FileViewMapper fileViewMapper;

    @Inject
    public TaskViewMapper(UserViewMapper userViewMapper, FileViewMapper fileViewMapper) {
        this.userViewMapper = userViewMapper;
        this.fileViewMapper = fileViewMapper;
    }

    public TaskView toView(@NotNull Task task, @NotNull Set<Student> assignees, Image coverImage, @NotNull Set<Attachment> attachments){
        return new TaskView(
                task.id().value(),
                task.title().value(),
                task.description().value(),
                task.color(),
                task.createdAt(),
                task.deadline().value(),
                assignees.stream().map(userViewMapper::fromStudent).collect(Collectors.toUnmodifiableSet()),
                task.executionPeriod().map(ExecutionPeriod::start),
                task.executionPeriod().flatMap(ExecutionPeriod::end),
                Optional.ofNullable(coverImage).map(fileViewMapper::toView),
                attachments.stream().map(fileViewMapper::toView).collect(Collectors.toUnmodifiableSet())
        );
    }
}
