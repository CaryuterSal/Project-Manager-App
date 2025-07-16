package dev.builder.usermanagement.domain.model;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Student extends User<User.Id>{

    private final Manager.Id createdBy;

    public Student(Manager.Id createdBy, Student.Id id, String password, boolean verified) {
        super(id, password, verified);
        this.createdBy = createdBy;
    }

    @Contract("_, _ -> new")
    static @NotNull Student invite(Manager.Id issuer, Student.Id id){
        return new Student(
                Objects.requireNonNull(issuer),
                Objects.requireNonNull(id),
                null,
                false
        );
    }

    public Manager.Id createdBy() {
        return createdBy;
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
