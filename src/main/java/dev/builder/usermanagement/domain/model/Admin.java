package dev.builder.usermanagement.domain.model;

import dev.builder.core.domain.AuditInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Admin extends User<Admin.Id> {

    private final Set<Manager.Id> managersCreated;

    public Admin(Admin.Id id, String password, boolean verified, Set<Manager.Id> managersCreated) {
        super(id, password, verified);
        this.managersCreated = Objects.requireNonNull(managersCreated);
    }

    private Admin(Admin.Id id){
        super(id);
        this.managersCreated = new HashSet<>();
    }

    @Contract("_ -> new")
    public static @NotNull Admin invite(Admin.Id id){
        return new Admin(id);
    }

    @Override
    public Admin hydratedWithAuditInfo(AuditInfo auditInfo){
        this.hydrateAuditInfo(auditInfo);
        return this;
    }

    public void addCreatedManager(Manager.Id id){
        managersCreated.add(Objects.requireNonNull(id));
    }

    public Set<Manager.Id> managersInvited() {
        return Collections.unmodifiableSet(managersCreated);
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
