package dev.builder.usermanagement.infrastructure;

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

}
