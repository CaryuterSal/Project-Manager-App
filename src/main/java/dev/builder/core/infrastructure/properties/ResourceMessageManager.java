package dev.builder.core.infrastructure.properties;

import dev.builder.board.domain.model.Color;
import dev.builder.board.domain.model.Stage;
import dev.builder.core.infrastructure.di.annotation.Bean;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ResourceBundle;

@Bean
public class ResourceMessageManager implements MessageLocalizer, ColorLocalizer, BoardStageLocalizer {

    public static final class MessageNamespaces{
        public static final String SEPARATOR = ".";
        public static final String COLOR_NAMESPACE = "color";
        public static final String STAGE_NAMESPACE = "board.stage";
        public static final String STAGE_TITLE_SUFFIX = "title";

        public static @NotNull String formatMessageProperty(@NotNull String namespace, String... parts){
            return namespace + SEPARATOR + String.join(SEPARATOR, parts);
        }
    }

    private final ResourceBundle messages;

    public ResourceMessageManager() {
        this.messages =  ResourceBundle.getBundle("message");
    }

    @Override
    public String getMessage(String key, Object... args) {
        String unformatted =  messages.getString(key);
        MessageFormat messageFormat = new MessageFormat(messages.getString(key));
        return messageFormat.format(args);
    }

    @Override
    public String translateColor(@NotNull Color color){
        String colorNameKey = color.name().toLowerCase();
        return messages.getString(MessageNamespaces.formatMessageProperty(MessageNamespaces.COLOR_NAMESPACE, colorNameKey));
    }

    @Override
    public String translateBoardStage(@NotNull Stage stage){
        String stageKey = stage.id().state().toString().replaceAll("_", "-");
        return messages.getString(MessageNamespaces.formatMessageProperty(MessageNamespaces.STAGE_NAMESPACE, stageKey, MessageNamespaces.STAGE_TITLE_SUFFIX));
    }
}
