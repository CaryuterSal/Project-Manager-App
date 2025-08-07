package dev.builder.core.application;

import dev.builder.MainApplication;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.runtime.DependencyContainer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

@Bean
public class ViewNavigation {
    private  final  DependencyContainer dependencyContainer;

    public ViewNavigation(DependencyContainer dependencyContainer) {
        this.dependencyContainer = dependencyContainer;
    }

    public void navigate(String viewName, Stage stage){
        try {
            // Configura el FXMLLoader manualmente
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/dev/builder/views/%s".formatted(viewName)));
            loader.setControllerFactory(dependencyContainer::getInstance);
            // Si tu vista también tiene un controlador con dependencias, puedes hacer lo mismo que aquí.
            // loader.setControllerFactory(param -> new OtroController(...));

            Scene nuevaEscena = new Scene(loader.load());

            stage.setScene(nuevaEscena);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openModal(String viewName, StageStyle initStyle){
        try{
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/dev/builder/views/%s".formatted(viewName)));
            loader.setControllerFactory(dependencyContainer::getInstance);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(initStyle);
            stage.setScene(new Scene(loader.load()));
            stage.showAndWait();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
