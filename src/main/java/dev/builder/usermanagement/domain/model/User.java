package dev.builder.usermanagement.domain.model;

import dev.builder.core.domain.AggregateRoot;
import dev.builder.core.domain.AuditInfo;
import dev.builder.core.domain.Auditable;
import dev.builder.core.domain.ValueObject;
import dev.builder.usermanagement.domain.port.out.PasswordEncoder;
import dev.builder.usermanagement.domain.port.out.PasswordMatcher;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class User<ID extends User.Id> extends AggregateRoot<ID> implements Auditable {

    private final Id id;
    private String password;
    private boolean verified;
    private AuditInfo auditInfo;

    public User(ID id, String password, boolean verified) {
        super(id);
        this.id = Objects.requireNonNull(id);
        this.password = Objects.requireNonNull(password);
        this.verified = verified;
    }

    protected User(ID id){
        super(id);
        this.id = Objects.requireNonNull(id);
    }

    public void createPassword(Password newPassword, @NotNull PasswordEncoder encoder) {
        if(verified){
            throw new IllegalStateException("User has already completed registration");
        }
        this.password = encoder.encode(Objects.requireNonNull(newPassword));
        this.verified = true;
    }

    @Override
    public void hydrateAuditInfo(AuditInfo auditInfo) {
        this.auditInfo = Objects.requireNonNull(auditInfo, "Audit info must not be null");
    }

    /**
     * Hidrata los campos de auditoría y devuelve la instancia concreta del usuario.
     * @param auditInfo la información de auditoria
     * @return esta instancia con tipo concreto T
     * @throws ClassCastException si se usa desde una instancia que no es T
     */
    public abstract User<ID> hydratedWithAuditInfo(AuditInfo auditInfo);

    public boolean login(Password password, @NotNull PasswordMatcher encoder) {
        if(!verified){
            throw new IllegalStateException("User has not completed registration yet");
        }
        if(password == null){
            throw new IllegalArgumentException("Password must not be null");
        }
        return encoder.matches(Objects.requireNonNull(password), this.password);
    }

    public Id email() {
        return id;
    }

    public String password() {
        return password;
    }

    public boolean isVerified() {
        return verified;
    }

    @Override
    public AuditInfo auditInfo() {
        return auditInfo;
    }



    public static class Id implements ValueObject {
        protected final Email email;

        public Id(String email) {
            this.email = new Email(email);
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

        public String value(){
            return email.value();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            Id id = (Id) o;
            return email.equals(id.email);
        }

        @Override
        public int hashCode() {
            return email.hashCode();
        }
    }
}
