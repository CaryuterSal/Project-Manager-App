package dev.builder.board.application.controller;

import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.auth.infrastructure.Role;
import dev.builder.board.application.view.StageView;
import dev.builder.board.application.view.TaskView;
import dev.builder.board.application.command.CreateTaskCommand;
import dev.builder.board.domain.model.Color;
import dev.builder.board.domain.model.Stage.StageState;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.ViewNavigation;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Bean
public class ManagerBoardController implements Initializable {

    @FXML private ListView<TaskView> todoList;
    @FXML private ListView<TaskView> inProgressList;
    @FXML private ListView<TaskView> doneList;
    @FXML private Button btnAddTask;
    @FXML private Label lblError;

    private final SessionContext sessionContext;
    private final RequestDispatcher requestDispatcher;
    private final ViewNavigation viewNavigation;
    private final ObservableList<TaskView> toDoTasks = FXCollections.observableArrayList();
    private final ObservableList<TaskView> inProgressTasks = FXCollections.observableArrayList();
    private final ObservableList<TaskView> doneTasks = FXCollections.observableArrayList();

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

    public void onAddTaskClicked() {
        viewNavigation.openModal("create-task-view.fxml", StageStyle.TRANSPARENT);
    }

    public void onTaskCreated(StageView stage) {
        toDoTasks.setAll(stage.tasks());
    }


    private Node createTaskCard(TaskView task) {
        Label title = new Label(task.title());
        title.getStyleClass().add("task-card-title");
        VBox card = new VBox(title);
        card.getStyleClass().add("task-card");
        card.setMaxWidth(Double.MAX_VALUE);

        card.setOnMouseClicked(evt -> {
            try {
                /*Stage detailStage = new Stage();
                detailStage.initModality(Modality.APPLICATION_MODAL);
                detailStage.initOwner(card.getScene().getWindow());
                viewNavigation.navigate("edit-task-view.fxml", detailStage);
                TaskController detailCtrl = (TaskController) detailStage.getUserData();
                detailCtrl.setTask(task);
                detailStage.setTitle("Detalles de tarea");
                detailStage.showAndWait();*/
                onTaskClicked(task);
            } catch (Exception e) {
                if (lblError != null) {
                    lblError.setText("Error al abrir detalles: " + e.getMessage());
                }
            }
        });
        return card;
    }

    private void onTaskClicked(TaskView task) {
        try {
            Stage detailStage = new Stage();
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.initOwner(todoList.getScene().getWindow());
            viewNavigation.navigate("edit-task-view.fxml", detailStage);

            TaskController taskController = (TaskController) detailStage.getUserData();
            taskController.setTask(task);
            detailStage.setTitle("Detalles de tarea");
            detailStage.showAndWait();

        } catch (Exception e) {
            lblError.setText("Error al abrir los detalles de la tarea: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void onAccountClicked() {
        try {
            Stage cuentasStage = new Stage();
            cuentasStage.initModality(Modality.APPLICATION_MODAL);
            cuentasStage.initOwner(todoList.getScene().getWindow());
            viewNavigation.navigate("manager-account-view.fxml", cuentasStage);
            cuentasStage.setTitle("Cuentas");
            cuentasStage.show();
        } catch (Exception ex) {
            if (lblError != null) {
                lblError.setText("Error al abrir la vista de Cuentas: " + ex.getMessage());
            }
            ex.printStackTrace();
        }
    }
}
