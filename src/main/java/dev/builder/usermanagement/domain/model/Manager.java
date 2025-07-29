package dev.builder.usermanagement.domain.model;

import dev.builder.core.domain.AuditInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Manager extends User<Manager.Id> {

    private final Admin.Id createdBy;
    private final Set<Student.Id> studentsCreated;

    public Manager(Admin.Id createdBy, Manager.Id email, String password, boolean verified, Set<Student.Id> studentsCreated) {
        super(email, password, verified);
        this.createdBy = Objects.requireNonNull(createdBy);
        this.studentsCreated = Objects.requireNonNull(studentsCreated);
    }

    private Manager(Admin.Id createdBy, Manager.Id email) {
        super(email);
        this.createdBy = Objects.requireNonNull(createdBy);
        this.studentsCreated = new HashSet<>();
    }

    @Contract("_, _ -> new")
    public static @NotNull Manager invite(Admin.Id issuer, Manager.Id id){
        return new Manager(issuer, id);
    }

    @Override
    public Manager hydratedWithAuditInfo(AuditInfo auditInfo){
        this.hydrateAuditInfo(auditInfo);
        return this;
    }

    public void addCreatedStudent(Student.Id id){
        studentsCreated.add(Objects.requireNonNull(id));
    }

    public Admin.Id createdBy() {
        return createdBy;
    }

    public Set<Student.Id> studentsCreated() {
        return Collections.unmodifiableSet(studentsCreated);
    }

    public static class Id extends User.Id {

        public Id(String email) {
            super(email);
        }

        public static String validate(String email){
            if(!isValid(email)){
                throw new IllegalArgumentException("Invalid email");
            }
            return email;
        }

        public static boolean isValid(String email) {
            return Email.isValid(email);
        }
    }
}
