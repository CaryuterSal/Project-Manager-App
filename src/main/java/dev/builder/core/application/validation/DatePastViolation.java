package dev.builder.core.application.validation;

import dev.builder.core.infrastructure.properties.MessageLocalizer;
import org.jetbrains.annotations.NotNull;
/**
 * Se lanza cuando se espera que la fecha esté en el pasado, pero no lo está.
 */
public class DatePastViolation extends DateViolation {
    public DatePastViolation(@NotNull MessageLocalizer messageResolver, String field) {
        super(field, messageResolver.getMessage("validation.field.date.past", field));
    }
}
