package dev.builder.usermanagement.domain.model;

import dev.builder.core.domain.ValueObject;

public record Name(String firstName, String lastName) implements ValueObject {

    public Name{
        validate(firstName, lastName);
    }

    public static void validate(String firstName, String lastName) {
        if(!isValid(firstName, lastName)) {
            throw new IllegalArgumentException("Invalid Academic info");
        }
    }

    public static boolean isValid(String firstName, String lastName) {
        return  firstName != null && lastName != null;
    }
}
