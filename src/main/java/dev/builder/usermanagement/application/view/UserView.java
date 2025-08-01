package dev.builder.usermanagement.application.view;

import java.time.LocalDateTime;
import java.util.Objects;

public class UserView {

    private final String email;
    private final boolean verified;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public UserView(String email, boolean verified, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.email = email;
        this.verified = verified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String email() {
        return email;
    }

    public boolean verified() {
        return verified;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public LocalDateTime updatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        UserView userView = (UserView) o;
        return verified == userView.verified && Objects.equals(email, userView.email) && Objects.equals(createdAt, userView.createdAt) && Objects.equals(updatedAt, userView.updatedAt);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(email);
        result = 31 * result + Boolean.hashCode(verified);
        result = 31 * result + Objects.hashCode(createdAt);
        result = 31 * result + Objects.hashCode(updatedAt);
        return result;
    }
}
