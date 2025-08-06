package dev.builder.board.application.query;

import dev.builder.core.application.Command;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.RequiredObjectValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

/**
 * Consulta para cargar una imágen de portada específica a partir de su identificador.
 *
 * Esta consulta devuelve un {@link java.util.Optional} que contiene el {@link java.io.InputStream}
 * del contenido del adjunto si existe, o un {@link java.util.Optional#empty()} si no se encuentra.
 *
 * @param imageId el ID de la imágen de portada
 */
public record LoadCoverImageQuery(UUID imageId) implements Command<Optional<InputStream>> {

    public enum Fields{
        IMAGE_ID("imageId"),;
        private final String value;
        Fields(String value) {this.value = value;}
        public String value() {return value;}
    }

    @Bean
    public static class LoadCoverImageQueryValidator extends BaseRequestValidator<LoadCoverImageQuery> {
        private static final Logger log = LoggerFactory.getLogger(LoadCoverImageQueryValidator.class);
        private final RequiredObjectValidator requiredObjectValidator;

        @Inject
        public LoadCoverImageQueryValidator( RequiredObjectValidator requiredObjectValidator) {
            super(log);
            this.requiredObjectValidator = requiredObjectValidator;
        }

        @Override
        public void validate(LoadCoverImageQuery value) throws ValidationException {
            validate(() -> requiredObjectValidator.validate(Fields.IMAGE_ID.value, value.imageId));
            throwIfAny();
        }
    }
}
