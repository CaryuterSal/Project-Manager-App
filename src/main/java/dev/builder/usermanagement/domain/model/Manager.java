package dev.builder.usermanagement.domain.model;

import dev.builder.usermanagement.domain.port.out.PasswordEncoder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Manager extends User<Manager.Id> {

    private final Admin.Id createdBy;
    private final List<Student.Id> studentsCreated;

    public Manager(Admin.Id createdBy, Manager.Id email, String password, boolean verified, List<Student.Id> studentsCreated) {
        super(email, password, verified);
        this.createdBy = createdBy;
        this.studentsCreated = studentsCreated;
    }

    @Contract("_, _ -> new")
    static @NotNull Manager invite(Admin.Id issuer, Manager.Id id){
        return new Manager(
                Objects.requireNonNull(issuer),
                Objects.requireNonNull(id),
                null,
                false,
                new ArrayList<>()
        );
    }


    public Student inviteStudent(Student.Id id, Name name){
        return Student.invite(this.id(), id, name);
    }

    public Admin.Id createdBy() {
        return createdBy;
    }

    public List<Student.Id> studentsCreated() {
        return Collections.unmodifiableList(studentsCreated);
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
