package dev.builder.usermanagement.domain.model;

import dev.builder.core.domain.ValueObject;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public record Email(String value) implements ValueObject<Email> {
    private static final Pattern emailPattern = Pattern.compile(
            "^(?!.*\\.\\.)[a-zA-Z0-9](?:[a-zA-Z0-9._%+-]{0,62}[a-zA-Z0-9])?@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z]{2,})+$"
    );

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
        return email != null && !email.isBlank() && emailPattern.matcher(email).matches();
    }

    @Override
    public int compareTo(@NotNull Email email) {
        return this.value.compareTo(email.value);
    }
}
