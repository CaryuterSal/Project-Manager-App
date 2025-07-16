package dev.builder.usermanagement.domain.model;

import dev.builder.core.domain.ValueObject;

import java.util.regex.Pattern;

public record Email(String value) implements ValueObject {
    private static final Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    public Email{
        validate(value);
    }

    public static String validate(String email) {
        if(!isValid(email)){
            throw new IllegalArgumentException("Invalid value address");
        }
        return email;
    }
    public static boolean isValid(String email) {
        return email != null && emailPattern.matcher(email).matches();
    }
}
