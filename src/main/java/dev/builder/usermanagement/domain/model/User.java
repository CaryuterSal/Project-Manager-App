package dev.builder.usermanagement.domain.model;

import dev.builder.core.domain.AggregateRoot;
import dev.builder.core.domain.Auditable;
import dev.builder.core.domain.ValueObject;
import dev.builder.usermanagement.domain.port.out.PasswordEncoder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

public abstract class User<ID extends User.Id> extends AggregateRoot<ID> implements Auditable {

    private final Id id;
    private String password;
    private boolean verified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User(ID id, String password, boolean verified) {
        super(id);
        this.id = Objects.requireNonNull(id);
        this.password = Objects.requireNonNull(password);
        this.verified = verified;
    }

    public void completeRegistration(Password newPassword, @NotNull PasswordEncoder encoder) {
        if(verified){
            throw new IllegalStateException("User has already completed registration");
        }
        this.password = encoder.encode(Objects.requireNonNull(newPassword));
        this.verified = true;
    }

    public void hydrateAuditInfo(LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public boolean login(Password password, @NotNull PasswordEncoder encoder) {
        if(!verified){
            throw new IllegalStateException("User has not completed registration yet");
        }
        if(password == null){
            throw new IllegalStateException("User already logged in");
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

    public LocalDateTime createdAt() {
        if(createdAt == null){
            throw new IllegalStateException("User has not hydrated yet");
        }
        return createdAt;
    }

    public LocalDateTime updatedAt() {
        if(updatedAt == null){
            throw new IllegalStateException("User has not hydrated yet");
        }
        return updatedAt;
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
