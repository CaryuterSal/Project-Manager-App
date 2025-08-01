package dev.builder.core.application.validation;

/**
 * Excepción base para todas las violaciones relacionadas con campos de tipo fecha.
 */
public class DateViolation extends FieldViolationException {
    public DateViolation(String field,String message) {
        super(field, message);
    }
}
