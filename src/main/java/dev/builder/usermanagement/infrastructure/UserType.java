package dev.builder.usermanagement.infrastructure;

import dev.builder.usermanagement.domain.model.Admin;
import dev.builder.usermanagement.domain.model.Manager;
import dev.builder.usermanagement.domain.model.User;
import org.jetbrains.annotations.NotNull;

public enum UserType {
    ADMIN("admin"),
    MANAGER("manager"),
    STUDENT("student");

    private final String dbType;

    UserType(String dbType) {
        this.dbType = dbType;
    }

    public static @NotNull UserType fromDbType(String dbType) {
        for (UserType userType : UserType.values()) {
            if (userType.dbType.equals(dbType)) {
                return userType;
            }
        }
        throw new IllegalArgumentException("Unknown user type: " + dbType);
    }

    public static @NotNull UserType fromDomainEntity(User<?> entity) {
        if(entity instanceof Admin) {
            return ADMIN;
        } else if(entity instanceof Manager){
            return MANAGER;
        } else {
            return STUDENT;
        }
    }
    public String dbType() {
        return dbType;
    }
}
