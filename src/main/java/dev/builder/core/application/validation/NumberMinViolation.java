package dev.builder.core.application.validation;

import dev.builder.core.infrastructure.properties.MessageLocalizer;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * Se lanza cuando el valor numérico de un campo es menor al mínimo permitido.
 */
public class NumberMinViolation extends FieldViolationException {
    public NumberMinViolation(@NotNull MessageLocalizer messageResolver, String field, int min) {
        super(field, messageResolver.getMessage("validation.field.number.min", field, min));
    }

    public NumberMinViolation(@NotNull MessageLocalizer messageResolver, String field, BigDecimal min) {
        super(field, messageResolver.getMessage("validation.field.number.min.decimal", field, min));
    }
}
