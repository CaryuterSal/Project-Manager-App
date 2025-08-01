package dev.builder.core.application.validation;

import dev.builder.core.infrastructure.properties.MessageLocalizer;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * Se lanza cuando el valor numérico de un campo es mayor al máximo permitido.
 */
public class NumberMaxViolation extends NumberViolation {

  public NumberMaxViolation(@NotNull MessageLocalizer messageResolver, String field, int max) {
    super(field, messageResolver.getMessage("validation.field.number.max", field, max));
  }

  public NumberMaxViolation(@NotNull MessageLocalizer messageResolver, String field, BigDecimal max) {
    super(field, messageResolver.getMessage("validation.field.number.max.decimal", field, max));
  }
}
