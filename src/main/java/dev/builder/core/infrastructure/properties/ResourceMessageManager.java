package dev.builder.core.infrastructure.properties;

import dev.builder.board.domain.model.Color;
import dev.builder.board.domain.model.Stage;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

public class ResourceMessageManager {

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

    public String translateColor(@NotNull Color color){
        String colorNameKey = color.name().toLowerCase();
        return messages.getString(MessageNamespaces.formatMessageProperty(MessageNamespaces.COLOR_NAMESPACE, colorNameKey));
    }

    public String translateBoardStage(@NotNull Stage stage){
        String stageKey = stage.id().state().toString().replaceAll("_", "-");
        return messages.getString(MessageNamespaces.formatMessageProperty(MessageNamespaces.STAGE_NAMESPACE, stageKey, MessageNamespaces.STAGE_TITLE_SUFFIX));
    }
}
