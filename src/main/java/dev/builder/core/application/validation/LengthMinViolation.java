package dev.builder.core.application.validation;

import dev.builder.core.infrastructure.properties.MessageLocalizer;
import org.jetbrains.annotations.NotNull;

/**
 * Se lanza cuando un campo de texto no alcanza la longitud mínima requerida.
 */
public class LengthMinViolation extends FieldViolationException {
    public LengthMinViolation(@NotNull MessageLocalizer messageResolver, String field, int min) {
        super(field,messageResolver.getMessage("validation.field.length.string.min", field, min));
    }
}
