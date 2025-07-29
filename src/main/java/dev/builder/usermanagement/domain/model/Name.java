package dev.builder.usermanagement.domain.model;

import dev.builder.core.domain.ValueObject;

public record Name(String firstName, String lastName) implements ValueObject {

    public Name(String firstName, String lastName) {
        validate(firstName, lastName);
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
    }

    public static void validate(String firstName, String lastName) {
        if(!isValid(firstName, lastName)) {
            throw new IllegalArgumentException("Invalid Academic info");
        }
    }

    public static boolean isValid(String firstName, String lastName) {
        return  firstName != null && !firstName.isBlank() && lastName != null && !lastName.isBlank();
    }
}
