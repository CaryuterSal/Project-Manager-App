package dev.builder.board.application.service;

import dev.builder.board.application.view.BoardView;
import dev.builder.board.application.view.CollaboratorView;
import dev.builder.board.application.view.StageView;
import dev.builder.board.application.view.TaskView;
import dev.builder.board.domain.model.*;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.service.UserViewMapper;
import dev.builder.usermanagement.domain.model.Student;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Bean
public class BoardViewMapper {

    private final StageViewMapper stageViewMapper;
    private final CollaboratorViewMapper collaboratorViewMapper;

    public BoardViewMapper(StageViewMapper stageViewMapper, CollaboratorViewMapper collaboratorViewMapper) {
        this.stageViewMapper = stageViewMapper;
        this.collaboratorViewMapper = collaboratorViewMapper;
    }

    public BoardView toView(@NotNull Board board, @NotNull Set<Student> collaborators, @NotNull Set<Stage> stages, Map<Stage.Id, Map<Task.Id, Set<Student>>> assigneesByStage, Map<Stage.Id,Map<Task.Id, Image>> coverImagesByStage, Map<Stage.Id,Map<Task.Id, Set<Attachment>>> attachmentsByStage) {
        Map<Student.Id, Student> studentById = collaborators.stream()
                .collect(Collectors.toMap(Student::id, Function.identity()));

        Set<CollaboratorView> collaboratorViews = board.collaborators().stream()
                .map(collaborator -> {
                    Student student = Optional.ofNullable(studentById.get(collaborator.id()))
                            .orElseThrow();
                    return collaboratorViewMapper.toView(collaborator, student);
                })
                .collect(Collectors.toUnmodifiableSet());

        Map<Stage.Id, Stage> stageById = stages.stream()
                .collect(Collectors.toMap(Stage::id, Function.identity()));
        List<StageView> stageViews = new ArrayList<>();
        for(Stage.Id stageId : board.stageIds()) {
            stageViews.add(stageViewMapper.toView(
                    Optional.ofNullable(stageById.get(stageId)).orElseThrow(),
                    Optional.ofNullable(assigneesByStage.get(stageId)).orElseThrow(),
                    Optional.ofNullable(coverImagesByStage.get(stageId)).orElseThrow(),
                    Optional.ofNullable(attachmentsByStage.get(stageId)).orElseThrow()
            ));
        }
        return new BoardView(
                collaboratorViews,
                stageViews
        );
    }


}
