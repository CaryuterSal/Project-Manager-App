package dev.builder.core.application.validation;

/**
 * Excepción base para todas las violaciones de validación de campos.
 *
 * <p>Se lanza cuando un campo específico no cumple con una restricción esperada.
 * Debe ser extendida por clases más específicas como {@link LengthMinViolation} o {@link NumberRangeViolation}.</p>
 */
public class FieldViolationException extends Exception {
    private final String field;

    public FieldViolationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
