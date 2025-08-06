package dev.builder.core.application.validation;

import dev.builder.core.infrastructure.properties.MessageLocalizer;
import org.jetbrains.annotations.NotNull;

/**
 * Se lanza cuando se espera que el valor numérico de un campo sea negativo, pero no lo es.
 */
public class NumberNegativeViolation extends NumberViolation {
    public NumberNegativeViolation(@NotNull MessageLocalizer messageResolver, String field) {
        super(field,messageResolver.getMessage("validation.field.number.negative", field));
    }
}
