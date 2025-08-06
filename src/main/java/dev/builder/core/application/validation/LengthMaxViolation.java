package dev.builder.core.application.validation;

import dev.builder.core.infrastructure.properties.MessageLocalizer;
import org.jetbrains.annotations.NotNull;

/**
 * Se lanza cuando un campo de texto excede la longitud máxima permitida.
 */
public class LengthMaxViolation extends FieldViolationException {
  public LengthMaxViolation(@NotNull MessageLocalizer messageResolver, String field, int max) {
    super(field,messageResolver.getMessage("validation.field.length.string.max", field, max));
  }
}
