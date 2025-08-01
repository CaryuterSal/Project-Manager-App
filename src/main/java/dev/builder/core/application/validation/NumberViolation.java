package dev.builder.core.application.validation;

/**
 * Excepción base para todas las violaciones relacionadas con valores numéricos.
 */
public class NumberViolation extends FieldViolationException {
    public NumberViolation(String field, String message) {
        super(field, message);
    }
}
