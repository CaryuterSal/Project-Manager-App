package dev.builder.core.application.validation;

import dev.builder.core.application.Request;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @param <T>
 */
public abstract class BaseRequestValidator<T extends Request<?>> implements RequestValidator<T> {
    private final List<FieldViolationException> violations = new ArrayList<>();

    private final Logger logger;

    public BaseRequestValidator(Logger logger) {
        this.logger = logger;
    }

    public void validate(@NotNull ValidationTask validator) {
        try {
            validator.run();
        } catch (FieldViolationException e) {
            logger.debug(e.getMessage(), e);
            violations.add(e);
        }
    }

    public void throwIfAny() throws ValidationException {
        if (!violations.isEmpty()) {
            violations.clear();
            throw new ValidationException(violations);
        }
    }

    @FunctionalInterface
    public interface ValidationTask {
        void run() throws FieldViolationException;
    }
}
