package dev.builder.usermanagement.domain.model;

import dev.builder.core.domain.ValueObject;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public record QuarterGroup(char value) implements ValueObject<QuarterGroup> {

    private static final Pattern onlyLettersPattern = Pattern.compile("^[a-zA-Z]+$");

    public QuarterGroup{
        validate(value);
    }

    public static char validate(char value) {
        if(!isValid(value)) {
            throw new IllegalArgumentException("Invalid Quarter Number");
        }
        return value;
    }

    public static boolean isValid(char value) {
        return !Character.isWhitespace(value) && onlyLettersPattern.matcher(String.valueOf(value)).matches();
    }

    @Override
    public int compareTo(@NotNull QuarterGroup quarterGroup) {
        return Character.compare(this.value, quarterGroup.value);
    }
}
