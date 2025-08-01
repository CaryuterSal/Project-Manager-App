package dev.builder.auth.infrastructure;

import dev.builder.usermanagement.domain.model.Admin;
import dev.builder.usermanagement.domain.model.Manager;
import dev.builder.usermanagement.domain.model.User;

public enum Role {
    ADMIN,
    MANAGER,
    STUDENT;

    public static Role fromUser(User<?> user){
        if(user instanceof Admin){
            return ADMIN;
        } else if(user instanceof Manager){
            return MANAGER;
        } else{
            return STUDENT;
        }
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
