package dev.builder.usermanagement.application.view;

import java.time.LocalDateTime;

public class ManagerView extends UserView{
    public ManagerView(String email, boolean verified, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(email, verified, createdAt, updatedAt);
    }
}
