package dev.builder.usermanagement.domain.model;

import dev.builder.core.domain.ValueObject;
import org.jetbrains.annotations.NotNull;

public record Name(String firstName, String lastName) implements ValueObject<Name> {

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

    @Override
    public int compareTo(@NotNull Name name) {
        int fCmp =  firstName.compareTo(name.firstName);
        return  fCmp != 0 ? fCmp : lastName.compareTo(name.lastName);
    }
}
