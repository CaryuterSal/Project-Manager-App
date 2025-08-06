package dev.builder.board.application.query;

import dev.builder.board.application.view.TaskView;
import dev.builder.core.application.Query;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.RequiredObjectValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

/**
 * Obtiene la información de una tarea como un {@link Optional<TaskView>} o un Optional vacío si no existe la tarea especificada
 * @param taskId el ID de la tarea a buscar
 */
public record GetTaskQuery(UUID taskId) implements Query<Optional<TaskView>> {

    public enum Fields{
        TASK_ID("taskId");
        private final String value;
        Fields(String value) {this.value = value;}
        public String value() {return value;}
    }

    @Bean
    public static class GetTaskQueryValidator extends BaseRequestValidator<GetTaskQuery>{
        private static final Logger log = LoggerFactory.getLogger(GetTaskQueryValidator.class);
        private final RequiredObjectValidator requiredObjectValidator;

        @Inject
        public GetTaskQueryValidator(RequiredObjectValidator requiredObjectValidator) {
            super(log);
            this.requiredObjectValidator = requiredObjectValidator;
        }

        @Override
        public void validate(GetTaskQuery value) throws ValidationException {
            validate(() -> requiredObjectValidator.validate(Fields.TASK_ID.value, value.taskId));
            throwIfAny();
        }
    }
}
