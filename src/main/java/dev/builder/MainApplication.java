package dev.builder;

import dev.builder.core.infrastructure.di.runtime.AnnotationAwareDependencyContainer;
import dev.builder.core.infrastructure.di.runtime.DependencyContainer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    private static DependencyContainer dependencyContainer = AnnotationAwareDependencyContainer.getInstance();

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/dev/builder/views/hello-view.fxml"));
        fxmlLoader.setControllerFactory(dependencyContainer::getInstance);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("KED");
        stage.setScene(scene);
        stage.show();

    }
    public static void main(String[] args) {
        dependencyContainer.scanPackage("dev.builder");
        launch();

    }
}