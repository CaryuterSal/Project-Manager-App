package dev.builder.core.application;

import dev.builder.MainApplication;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.runtime.DependencyContainer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.poi.ss.formula.functions.T;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

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


    public  <T> void openModal(String viewName, StageStyle initStyle, Consumer<T> controllerConfigurer){
        try{
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/dev/builder/views/%s".formatted(viewName)));
            loader.setControllerFactory(dependencyContainer::getInstance);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(initStyle);
            stage.setScene(new Scene(loader.load()));
            controllerConfigurer.accept(loader.getController());
            stage.showAndWait();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public boolean showConfirmationDialog(String title, String content){
        ButtonType confirmarBtn = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        ButtonType eliminarBtn = new ButtonType("Cancelar", ButtonBar.ButtonData.NO);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, content, confirmarBtn, eliminarBtn);

        alert.setTitle(title);
        alert.setHeaderText("Selecciona una opción");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/dev/builder/assets/styles/delete-confirm-alert.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");

        Node confirmarButton = dialogPane.lookupButton(confirmarBtn);
        Node eliminarButton = dialogPane.lookupButton(eliminarBtn);

        confirmarButton.getStyleClass().add("confirmar-button");
        eliminarButton.getStyleClass().add("eliminar-button");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirmarBtn;
    }

}
