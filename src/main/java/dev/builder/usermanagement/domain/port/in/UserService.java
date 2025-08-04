package dev.builder.usermanagement.domain.port.in;

import dev.builder.usermanagement.application.command.*;
import dev.builder.usermanagement.application.query.*;
import dev.builder.usermanagement.application.view.*;

import java.util.List;
import java.util.Optional;

public interface UserService {
    AdminView registerAdmin(InviteAdminCommand command);
    ManagerView registerManager(InviteManagerCommand command);
    StudentView registerStudent(InviteStudentCommand command);
    void deleteUser(DeleteUserCommand command);
    UserView completeRegistration(CompleteRegistrationCommand command);
    Optional<UserView> getUser(FindUserQuery<UserView> query);
    Optional<AdminView> getAdmin(FindAdminQuery query);
    Optional<ManagerView> getManager(FindManagerQuery query);
    Optional<StudentView> getStudent(FindStudentQuery query);
    List<UserView> getAllUsers(FindAllUsersQuery<UserView, FindAllUsersQuery.UserSortableField> query);
    List<AdminView> getAllAdmins(FindAllAdminsQuery query);
    List<ManagerView> getAllManagers(FindAllManagersQuery query);
    List<StudentView> getAllStudents(FindAllStudentsQuery query);


}
