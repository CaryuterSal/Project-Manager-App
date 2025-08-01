package dev.builder.usermanagement.application.service;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.usermanagement.application.view.AdminView;
import dev.builder.usermanagement.application.view.ManagerView;
import dev.builder.usermanagement.application.view.StudentView;
import dev.builder.usermanagement.application.view.UserView;
import dev.builder.usermanagement.domain.model.Admin;
import dev.builder.usermanagement.domain.model.Manager;
import dev.builder.usermanagement.domain.model.Student;
import dev.builder.usermanagement.domain.model.User;
import org.jetbrains.annotations.NotNull;

@Bean
public class UserViewMapper {
    
    public AdminView fromAdmin(@NotNull Admin admin){
        return new AdminView(
                admin.email().value(),
                admin.isVerified(),
                admin.createdAt(),
                admin.updatedAt()
        );
    }

    public ManagerView fromManager(@NotNull Manager manager){
        return new ManagerView(
                manager.email().value(),
                manager.isVerified(),
                manager.createdAt(),
                manager.updatedAt()
        );
    }

    public StudentView fromStudent(@NotNull Student student){
        return new StudentView(
                student.email().value(),
                student.isVerified(),
                student.createdAt(),
                student.updatedAt(),
                student.name().firstName(),
                student.name().lastName(),
                student.academicInfo().quarter().number(),
                student.academicInfo().group().value()
        );
    }

    public UserView fromUser(@NotNull User<?> user){
        return new UserView(
            user.email().value(),
            user.isVerified(),
            user.createdAt(),
            user.updatedAt()
        );
    }
}
