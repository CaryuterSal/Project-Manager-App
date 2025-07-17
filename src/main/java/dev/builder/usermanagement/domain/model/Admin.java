package dev.builder.usermanagement.domain.model;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Admin extends User<Admin.Id> {

    List<Manager.Id> managersCreated;

    public Admin(Admin.Id id, String password, boolean verified, List<Manager.Id> managersCreated) {
        super(id, password, verified);
        this.managersCreated = Objects.requireNonNull(managersCreated);
    }

    @Contract("_ -> new")
    public static @NotNull Admin invite(Admin.Id id){
        return new Admin(
                Objects.requireNonNull(id),
                null,
                false,
                new ArrayList<>()
        );
    }

    public void addCreatedManager(Manager.Id id){
        managersCreated.add(Objects.requireNonNull(id));
    }

    public List<Manager.Id> managersInvited() {
        return Collections.unmodifiableList(managersCreated);
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
