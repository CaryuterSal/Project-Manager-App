package dev.builder.board.application.service;

import dev.builder.board.application.view.CollaboratorView;
import dev.builder.board.domain.model.BoardCollaborator;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.service.UserViewMapper;
import dev.builder.usermanagement.application.view.StudentView;
import dev.builder.usermanagement.application.view.UserView;
import dev.builder.usermanagement.domain.model.Student;

@Bean
public class CollaboratorViewMapper {

    private final UserViewMapper userViewMapper;

    @Inject
    public CollaboratorViewMapper(UserViewMapper userViewMapper) {
        this.userViewMapper = userViewMapper;
    }

    public CollaboratorView toView(BoardCollaborator collaborator, Student student) {
        return new CollaboratorView(collaborator.issuedAt(),userViewMapper.fromStudent(student));
    }
}
