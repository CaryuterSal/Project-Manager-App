package dev.builder.core.application.validation;

import dev.builder.core.infrastructure.properties.MessageLocalizer;
import org.jetbrains.annotations.NotNull;

/**
 * Se lanza cuando se espera que la fecha esté en el futuro, pero no lo está.
 */
public class DateFutureViolation extends DateViolation {
    public DateFutureViolation(@NotNull MessageLocalizer messageResolver, String field) {
        super(field, messageResolver.getMessage("validation.field.date.future", field));
    }
}
