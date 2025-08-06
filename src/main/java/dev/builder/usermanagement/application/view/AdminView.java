package dev.builder.usermanagement.application.view;

import java.time.LocalDateTime;

public class AdminView extends UserView{

    public AdminView(String email, boolean verified, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(email, verified, createdAt, updatedAt);
    }
}
