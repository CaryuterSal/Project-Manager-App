package dev.builder.core.application.validation;

import dev.builder.core.infrastructure.properties.MessageLocalizer;
import org.jetbrains.annotations.NotNull;

/**
 * Se lanza cuando un campo de texto no se encuentra dentro del rango permitido de longitud.
 */
public class LengthRangeViolation extends RuntimeException {
    public LengthRangeViolation(@NotNull MessageLocalizer messageResolver, String field, int min, int max) {
        super(messageResolver.getMessage("validation.field.length.string.range", field, min, max));
    }
}
