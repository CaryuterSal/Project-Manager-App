package dev.builder.core.application.validation;

import dev.builder.core.infrastructure.properties.MessageLocalizer;
import org.jetbrains.annotations.NotNull;

/**
 * Se lanza cuando se espera que el valor numérico de un campo sea positivo, pero no lo es.
 */
public class NumberPositiveViolation extends NumberViolation {
    public NumberPositiveViolation(@NotNull MessageLocalizer messageResolver, String field) {
        super(field,messageResolver.getMessage("validation.field.number.positive", field));
    }
}
