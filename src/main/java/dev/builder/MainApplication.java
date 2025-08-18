package dev.builder;

import dev.builder.auth.application.command.LoginCommand;
import dev.builder.auth.application.command.RestoreSessionCommand;
import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.auth.infrastructure.Role;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.ViewNavigation;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.runtime.AnnotationAwareDependencyContainer;
import dev.builder.core.infrastructure.di.runtime.DependencyContainer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    private static DependencyContainer dependencyContainer = AnnotationAwareDependencyContainer.getInstance();

    @Override
    public void start(Stage stage) throws IOException {

        RequestDispatcher requestDispatcher = dependencyContainer.getInstance(RequestDispatcher.class);
        SessionContext sessionContext = dependencyContainer.getInstance(SessionContext.class);
        ViewNavigation viewNavigation = dependencyContainer.getInstance(ViewNavigation.class);
        try {
            boolean isRecovered = requestDispatcher.dispatch(new RestoreSessionCommand());
            if (isRecovered) {
                if(sessionContext.hasRole(Role.ADMIN)) {
                    viewNavigation.navigate("admin-panel-view.fxml", stage);
                }else if(sessionContext.hasRole(Role.MANAGER)){
                    viewNavigation.navigate("board-main-view.fxml", stage);
                } else {
                    viewNavigation.navigate("board-student-view.fxml", stage);
                }
            } else {
                viewNavigation.navigate("hello-view.fxml", stage);
            }
        } catch (Exception ignored) {
        }
    }

    public static void main(String[] args) {
        dependencyContainer.scanPackage("dev.builder");
        launch();

    }
}