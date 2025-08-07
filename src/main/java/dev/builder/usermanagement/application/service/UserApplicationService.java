package dev.builder.usermanagement.application.service;

import dev.builder.auth.application.command.LoginCommand;
import dev.builder.auth.application.service.UnauthorizedException;
import dev.builder.auth.domain.port.in.AuthenticationService;
import dev.builder.auth.infrastructure.Role;
import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.board.domain.port.in.BoardService;
import dev.builder.core.application.Sort;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers;
import dev.builder.core.infrastructure.persistence.ConnectionManager;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import dev.builder.usermanagement.application.command.*;
import dev.builder.usermanagement.application.query.*;
import dev.builder.usermanagement.application.view.AdminView;
import dev.builder.usermanagement.application.view.ManagerView;
import dev.builder.usermanagement.application.view.StudentView;
import dev.builder.usermanagement.application.view.UserView;
import dev.builder.usermanagement.domain.exception.UserExistsException;
import dev.builder.usermanagement.domain.exception.UserNotFoundException;
import dev.builder.usermanagement.domain.model.*;
import dev.builder.usermanagement.domain.port.in.UserService;
import dev.builder.usermanagement.domain.port.out.*;
import dev.builder.usermanagement.domain.service.UserRegistrationService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.runInTransaction;

@Bean
public class UserApplicationService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserApplicationService.class);

    private final AnyUserRepository  anyUserRepository;
    private final AdminRepository adminRepository;
    private final ManagerRepository managerRepository;
    private final StudentRepository studentRepository;
    private final SessionContext sessionContext;
    private final UserViewMapper mapper;
    private final ConnectionManager connectionManager;
    private final MessageLocalizer messageLocalizer;
    private final PasswordEncoder passwordEncoder;

    private BoardService boardService;
    private AuthenticationService authService;

    @Inject
    public void setBoardService(BoardService boardService) {
        this.boardService = boardService;
    }

    @Inject
    public void setAuthenticationService(AuthenticationService authService) {
        this.authService = authService;
    }

    @Inject
    public UserApplicationService(AnyUserRepository anyUserRepository, AdminRepository adminRepository, ManagerRepository managerRepository, StudentRepository studentRepository, SessionContext sessionContext, UserViewMapper mapper, ConnectionManager connectionManager, MessageLocalizer messageLocalizer, PasswordEncoder passwordEncoder) {
        this.anyUserRepository = anyUserRepository;
        this.adminRepository = adminRepository;
        this.managerRepository = managerRepository;
        this.studentRepository = studentRepository;
        this.sessionContext = sessionContext;
        this.mapper = mapper;
        this.connectionManager = connectionManager;
        this.messageLocalizer = messageLocalizer;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AdminView registerAdmin(InviteAdminCommand command) {
        Admin.Id id = new Admin.Id(command.email());

        Admin saved =  runInTransaction(
                connectionManager,
                log,
                conn -> {
                    Optional<Admin> existent = adminRepository.findById(id, conn);
                    if(existent.isPresent()) {
                        throw new UserExistsException(messageLocalizer);
                    }
                    Admin admin = Admin.invite(id);
                    return adminRepository.save(admin, conn);
                }
        );
        return mapper.fromAdmin(saved);
    }

    @Override
    public ManagerView registerManager(InviteManagerCommand command) {
        sessionContext.requireRole(Role.ADMIN, messageLocalizer.getMessage("auth.session.requires.role", Role.ADMIN));
        Manager saved = runInTransaction(
                connectionManager,
                log,
                con -> {

                    Admin issuer = adminRepository.findById(new Admin.Id(sessionContext.getCurrentUser()), con)
                            .orElseThrow(() -> {
                                sessionContext.clear();
                                return new UnauthorizedException(messageLocalizer.getMessage("auth.session.stale"));
                            });
                    Manager.Id id = new Manager.Id(command.email());
                    Optional<Manager> existent = managerRepository.findById(id, con);
                    if(existent.isPresent()) {
                        throw new UserExistsException(messageLocalizer);
                    }
                    Manager manager = UserRegistrationService.registerNewManager(
                            issuer,
                            id
                    );
                    return managerRepository.save(manager, con);
                }
        );
        return mapper.fromManager(saved);
    }

    @Override
    public StudentView registerStudent(InviteStudentCommand command) {
        sessionContext.requireRole(Role.MANAGER, messageLocalizer.getMessage("auth.session.requires.role", Role.MANAGER));
        Student saved = runInTransaction(
                connectionManager,
                log,
                con -> {

                    Manager issuer = managerRepository.findById(new Manager.Id(sessionContext.getCurrentUser()), con)
                            .orElseThrow(() ->{
                                sessionContext.clear();
                                return new UnauthorizedException(messageLocalizer.getMessage("auth.session.stale"));
                            });
                    Student.Id id = new Student.Id(command.email());
                    Optional<Student> existent = studentRepository.findById(id, con);
                    if(existent.isPresent()) {
                        throw new UserExistsException(messageLocalizer);
                    }
                    Student student = UserRegistrationService.registerNewStudent(
                            issuer,
                            id,
                            new Name(command.firstName(), command.lastName()),
                            new AcademicInfo(
                                    new AcademicQuarter(command.academicQuarter()),
                                    new QuarterGroup(command.academicGroup())
                            )
                    );
                    return studentRepository.save(student, con);
                }
        );
        return mapper.fromStudent(saved);
    }

    @Override
    public void deleteUser(DeleteUserCommand command) {
        if(!sessionContext.isAuthenticated()) throw new UnauthorizedException("Se requiere una sesión activa para eliminar usuario");
        runInTransaction(
                connectionManager,
                log,
                conn -> {
                    User<?> ownUser = anyUserRepository.findById(new User.Id<>(sessionContext.getCurrentUser()), conn).orElseThrow();
                    User.Id<?> userToDeleteId = new User.Id<>(command.email());
                    User<?> userToDelete = anyUserRepository.findById(userToDeleteId, conn).orElseThrow(
                            () -> new UserNotFoundException(messageLocalizer, userToDeleteId)
                    );
                    if(ownUser.id().equals(userToDelete.id())) {
                        anyUserRepository.deleteById(userToDeleteId, conn);
                    } else if(ownUser instanceof Admin admin && userToDelete instanceof Manager manager && manager.createdBy().equals(admin.id())) {
                        anyUserRepository.deleteById(userToDeleteId, conn);
                    } else if(ownUser instanceof Manager manager && userToDelete instanceof Student student && student.createdBy().equals(manager.id())) {
                        anyUserRepository.deleteById(userToDeleteId, conn);
                    } else {
                        throw new UnauthorizedException("No tienes permisos para eliminar a este usuario");
                    }
                    return null;
                }
        );
    }

    @Override
    public UserView completeRegistration(CompleteRegistrationCommand command) {
        User<?> registered = runInTransaction(
                connectionManager,
                log,
                con -> {
                    User.Id<?> id = new User.Id<>(command.email());
                    User<?> invited = anyUserRepository.findById(id, con).orElseThrow(
                            () ->  new UserNotFoundException(messageLocalizer, id)
                    );
                    if(invited.isVerified()){
                        throw new IllegalStateException("Ya se verificó a este usuario");
                    }
                    invited.completeRegistration(new Password(command.password()),passwordEncoder);
                    User<?> savedUser = anyUserRepository.save(invited, con);
                    sessionContext.setAuthentication(savedUser);
                    if(savedUser instanceof Manager){
                        boardService.createOwnBoard(con);
                    } else if(savedUser instanceof Student){
                        boardService.assignToInvitedBoard(con);
                    }
                    return savedUser;
                }
        );
        authService.login(new LoginCommand(registered.id().value(), command.password()));
        return mapper.fromUser(registered);
    }

    @Override
    public Optional<UserView> getUser(FindUserQuery<UserView> query) {
        return anyUserRepository.findById(new User.Id<>(query.email())).map(mapper::fromUser);
    }

    @Override
    public Optional<AdminView> getAdmin(FindAdminQuery query) {
        return adminRepository.findById(new Admin.Id(query.email())).map(mapper::fromAdmin);
    }

    @Override
    public Optional<ManagerView> getManager(FindManagerQuery query) {
        return managerRepository.findById(new Manager.Id(query.email())).map(mapper::fromManager);
    }

    @Override
    public Optional<StudentView> getStudent(FindStudentQuery query) {
        return  studentRepository.findById(new Student.Id(query.email())).map(mapper::fromStudent);
    }

    private Comparator<User<?>> buildComparatorForUser(@NotNull Sort<FindAllUsersQuery.UserSortableField> sort) {

        Comparator<User<?>> comparator = switch (sort.field()) {
            case EMAIL -> Comparator.comparing(u -> u.email().value());
            case CREATED_AT -> Comparator.comparing(User::createdAt);
            case UPDATED_AT -> Comparator.comparing(User::updatedAt);
        };
        return sort.asc() ? comparator : comparator.reversed();
    }

    private Comparator<Manager> buildComparatorForManager(@NotNull Sort<FindAllManagersQuery.ManagerSortableField> sort) {
        Comparator<Manager> comparator = switch (sort.field()) {
            case EMAIL -> Comparator.comparing(u -> u.email().value());
            case CREATED_AT -> Comparator.comparing(User::createdAt);
            case UPDATED_AT -> Comparator.comparing(User::updatedAt);
            case CREATED_BY -> Comparator.comparing(Manager::createdBy);
        };
        return sort.asc() ? comparator : comparator.reversed();
    }

    private Comparator<Student> buildComparatorForStudent(@NotNull Sort<FindAllStudentsQuery.StudentSortableField> sort) {
        Comparator<Student> comparator = switch (sort.field()) {
            case EMAIL -> Comparator.comparing(student -> student.email().value());
            case CREATED_AT -> Comparator.comparing(User::createdAt);
            case UPDATED_AT -> Comparator.comparing(User::updatedAt);
            case GROUP -> Comparator.comparing(s -> s.academicInfo().group());
            case QUARTER -> Comparator.comparing(s -> s.academicInfo().quarter());
            case CREATED_BY -> Comparator.comparing(Student::createdBy);
            case NAME -> Comparator.comparing(Student::name);
        };
        return sort.asc() ? comparator : comparator.reversed();
    }

    private <U extends User<?>, Q extends FindAllUsersQuery<?,?>> List<U> filterByDate(List<U> users, Q query) {
        return users.stream()
                .filter(user -> query.minCreatedDate().map(min -> user.createdAt().isAfter(min)).orElse(true))
                .filter(user -> query.maxCreatedDate().map(max -> user.createdAt().isBefore(max)).orElse(true))
                .toList();
    }

    @Override
    public List<UserView> getAllUsers(FindAllUsersQuery<UserView, FindAllUsersQuery.UserSortableField> query) {

        List<? extends User<?>> found;
        if(query.emailLike().isPresent()){
            found = anyUserRepository.findWithEmailLike(query.emailLike().get());
        } else {
            found = anyUserRepository.findAll();
        }
        found = filterByDate(found, query);
        if(query.sort().isPresent()){
            found = new ArrayList<>(found);
            found.sort(buildComparatorForUser(query.sort().get()));
        }
        return found.stream().map(mapper::fromUser).collect(Collectors.toList());
    }

    @Override
    public List<AdminView> getAllAdmins(FindAllAdminsQuery query) {
        List<Admin> found;
        if(query.emailLike().isPresent()){
            found = adminRepository.findWithEmailLike(query.emailLike().get());
        } else {
            found = adminRepository.findAll();
        }
        found = filterByDate(found, query);
        if(query.sort().isPresent()){
            found = new ArrayList<>(found);
            found.sort(buildComparatorForUser(query.sort().get()));
        }
        return found.stream().map(mapper::fromAdmin).collect(Collectors.toList());
    }

    @Override
    public List<ManagerView> getAllManagers(FindAllManagersQuery query) {
        List<Manager> found;

        if(query.emailLike().isPresent()){
            found = managerRepository.findWithEmailLike(query.emailLike().get());
        } else {
            found = managerRepository.findAll();
        }
        found = filterByDate(found, query);
        found = found.stream()
                .filter(man -> query.createdBy().map(q -> man.createdBy().value().equals(q)).orElse(true))
                .toList();
        if(query.sort().isPresent()){
            found = new ArrayList<>(found);
            found.sort(buildComparatorForManager(query.sort().get()));
        }
        return found.stream().map(mapper::fromManager).collect(Collectors.toList());
    }

    @Override
    public List<StudentView> getAllStudents(FindAllStudentsQuery query) {
        List<Student> found;
        if(query.emailLike().isPresent()){
            found = studentRepository.findWithEmailLike(query.emailLike().get());
        } else {
            found = studentRepository.findAll();
        }
        found = filterByDate(found, query);
        found = found.stream()
                .filter(man -> query.createdBy().map(q -> man.createdBy().value().equals(q)).orElse(true))
                .filter(man -> query.academicGroup().map(q -> man.academicInfo().group().equals(new QuarterGroup(q))).orElse(true))
                .filter(man -> query.academicQuarter().map(q -> man.academicInfo().quarter().equals(new AcademicQuarter(q))).orElse(true))
                .toList();
        if(query.sort().isPresent()){
            found = new ArrayList<>(found);
            found.sort(buildComparatorForStudent(query.sort().get()));
        }
        return found.stream().map(mapper::fromStudent).collect(Collectors.toList());
    }
}
