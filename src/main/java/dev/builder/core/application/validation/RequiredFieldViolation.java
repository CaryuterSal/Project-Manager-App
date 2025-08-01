package dev.builder.core.application.validation;

import dev.builder.core.infrastructure.properties.MessageLocalizer;
import org.jetbrains.annotations.NotNull;

/**
 * Se lanza cuando un campo requerido está ausente o es nulo.
 */
public class RequiredFieldViolation extends FieldViolationException {

    public RequiredFieldViolation(@NotNull MessageLocalizer resource, String field) {
        super(field, resource.getMessage("validation.field.required", field));
    }
}
