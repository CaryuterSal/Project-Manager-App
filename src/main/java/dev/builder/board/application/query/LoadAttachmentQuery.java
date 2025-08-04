package dev.builder.board.application.query;

import dev.builder.core.application.Query;
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

public record LoadAttachmentQuery(UUID attachmentId) implements Query<Optional<InputStream>> {

    public enum Fields{
        ATTACHMENT_ID("attachmentId"),;
        private final String value;
        Fields(String value) {this.value = value;}
        public String value() {return value;}
    }

    @Bean
    public static class LoadAttachmentQueryValidator extends BaseRequestValidator<LoadAttachmentQuery> {
        private static final Logger log = LoggerFactory.getLogger(LoadAttachmentQueryValidator.class);
        private final RequiredObjectValidator requiredObjectValidator;

        @Inject
        public LoadAttachmentQueryValidator( RequiredObjectValidator requiredObjectValidator) {
            super(log);
            this.requiredObjectValidator = requiredObjectValidator;
        }

        @Override
        public void validate(LoadAttachmentQuery value) throws ValidationException {
            validate(() -> requiredObjectValidator.validate(Fields.ATTACHMENT_ID.value, value.attachmentId));
            throwIfAny();
        }
    }
}
