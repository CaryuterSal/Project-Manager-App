package dev.builder.usermanagement.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

public class User {

    //Se crearon Variables

        private UUID id;
        private String email;
        private String password;
        private boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        protected static final Pattern EMAIL = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        protected static final Pattern PASSWORD = Pattern.compile("^(?=.[a-z])(?=.[A-Z])(?=.*\\d).{5,}$");


        public User(UserId id) {
        }

        public User(UUID id, String email, String password, boolean active, LocalDateTime createdAt, LocalDateTime updateAT) {
            this.id = id;
            this.email = email;
            this.password = password;
            this.active = active;
            this.createdAt = createdAt;
            this.updatedAt = updateAT;
        }

        public User(UUID id, String email, String password, boolean active) {
            if (!EMAIL.matcher(email).matches()) {
                throw new IllegalArgumentException("Ivalid email format");
            }
            if (!PASSWORD.matcher(password).matches()) {
                throw new IllegalArgumentException("The password must have at least one uppercase character and one number");
            }

            this.id = id;
            this.email = email;
            this.password = password;
            this.active = active;
        }
        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public LocalDateTime getCreateAT() {
            return createdAt;
        }

        public void setCreateAT(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }


    public static class UserId {
    }
}
