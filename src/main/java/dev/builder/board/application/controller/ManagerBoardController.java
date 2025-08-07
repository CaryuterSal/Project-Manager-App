package dev.builder.board.application.controller;

import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.auth.infrastructure.Role;
import dev.builder.board.application.view.TaskView;
import dev.builder.board.application.command.CreateTaskCommand;
import dev.builder.board.domain.model.Color;
import dev.builder.board.domain.model.Stage.StageState;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.ViewNavigation;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.concurrent.Task;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Bean
public class ManagerBoardController implements Initializable {

    @FXML private VBox todoList;
    @FXML private VBox inProgressList;
    @FXML private VBox doneList;
    @FXML private Hyperlink btnAddTask;
    @FXML private Label lblError;
    @FXML private Button btnAddStudent, btnAccount, btnBoard;

    private final SessionContext sessionContext;
    private final RequestDispatcher requestDispatcher;
    private final ViewNavigation viewNavigation;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Inject
    public ManagerBoardController(SessionContext sessionContext,
                                  RequestDispatcher requestDispatcher,
                                  ViewNavigation viewNavigation) {
        this.sessionContext = sessionContext;
        this.requestDispatcher = requestDispatcher;
        this.viewNavigation = viewNavigation;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (sessionContext.hasRole(Role.MANAGER)) {
            btnAddTask.setOnAction(e -> onAddTaskClicked());
        } else if (sessionContext.hasRole(Role.STUDENT)) {
            Stage stage = (Stage) lblError.getScene().getWindow();
            viewNavigation.navigate("board-student-view.fxml", stage);
            return;
        } else {
            lblError.setText("No tienes permisos para acceder a este panel.");
        }
    }

    @FXML
    private void onAddTaskClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/builder/views/create-task-view.fxml"));

            if (loader.getLocation() == null) {
                System.out.println("Error: La ruta al FXML no es correcta.");
            }

            Parent dialogRoot = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(todoList.getScene().getWindow());
            dialog.setTitle("Crear nueva tarea");
            dialog.setScene(new Scene(dialogRoot));

            TaskController addCtrl = loader.getController();
            addCtrl.setDialogStage(dialog);
            dialog.showAndWait();

            TaskView newTask = addCtrl.getCreatedTask();
            if (newTask != null) {
                Node card = createTaskCard(newTask);
                todoList.getChildren().add(card);
            }
        } catch (IOException ex) {
            if (lblError != null) {
                lblError.setText("No se pudo abrir el formulario: " + ex.getMessage());
            } else {
                System.err.println("lblError no está inicializado.");
            }
            ex.printStackTrace();
        }
    }

    private Node createTaskCard(TaskView task) {
        Label title = new Label(task.title());
        title.getStyleClass().add("task-card-title");
        VBox card = new VBox(title);
        card.getStyleClass().add("task-card");
        card.setMaxWidth(Double.MAX_VALUE);
        card.setOnMouseClicked(evt -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/dev/builder/views/create-task-view.fxml")
                );
                Parent detailRoot = loader.load();
                TaskController detailCtrl = loader.getController();
                detailCtrl.setTask(task);
                Stage detailStage = new Stage();
                detailStage.initModality(Modality.APPLICATION_MODAL);
                detailStage.initOwner(card.getScene().getWindow());
                detailStage.setTitle("Detalles de tarea");
                detailStage.setScene(new Scene(detailRoot));
                detailStage.showAndWait();
            } catch (IOException e) {
                if (lblError != null) {
                    lblError.setText("Error al abrir detalles: " + e.getMessage());
                } else {
                    System.err.println("lblError no está inicializado.");
                }
            }
        });
        return card;
    }

    @FXML
    private void onAccountClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/builder/views/add-student-view.fxml"));

            if (loader.getLocation() == null) {
                System.out.println("Error: La ruta al FXML de cuentas no es correcta.");
            }

            Parent cuentasRoot = loader.load();

            Stage cuentasStage = new Stage();
            cuentasStage.initModality(Modality.APPLICATION_MODAL);
            cuentasStage.initOwner(todoList.getScene().getWindow());
            cuentasStage.setTitle("Cuentas");
            cuentasStage.setScene(new Scene(cuentasRoot));
            cuentasStage.show();

        } catch (IOException ex) {
            if (lblError != null) {
                lblError.setText("Error al abrir la vista de Cuentas: " + ex.getMessage());
            } else {
                System.err.println("lblError no está inicializado.");
            }
            ex.printStackTrace();
        }
    }
}
