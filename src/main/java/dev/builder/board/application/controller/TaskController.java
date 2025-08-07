package dev.builder.board.application.controller;

import dev.builder.board.application.view.StageView;
import dev.builder.board.application.view.TaskView;
import dev.builder.board.application.command.CreateTaskCommand;
import dev.builder.board.domain.model.Color;
import dev.builder.board.domain.model.Stage.StageState;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.ViewNavigation;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;  // Importamos Parent
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Bean
public class TaskController implements Initializable {

    @FXML private RadioButton rbTitle;
    @FXML private TextField txtTitle;
    @FXML private Button btnMembers;
    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    @FXML private HTMLEditor descriptionEditor;
    @FXML private Button btnCancel;
    @FXML private Button btnAdd;
    @FXML private Label lblError;

    private final RequestDispatcher requestDispatcher;
    private final ViewNavigation viewNavigation;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TaskView createdTask;
    private Stage dialogStage;
    private TaskView task;

    @Inject
    public TaskController(RequestDispatcher requestDispatcher,
                          ViewNavigation viewNavigation) {
        this.requestDispatcher = requestDispatcher;
        this.viewNavigation = viewNavigation;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnCancel.setOnAction(e -> dialogStage.close());
        btnAdd.setOnAction(e -> onAddTask());
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setTask(TaskView task) {
        this.task = task;
        if (task != null) {
            txtTitle.setText(task.title());
            descriptionEditor.setHtmlText(task.description());
        }
    }

    @FXML
    private void onAddTask() {
        String title = txtTitle.getText();
        LocalDateTime due = dpEnd.getValue().atStartOfDay();
        String desc = descriptionEditor.getHtmlText();

        CreateTaskCommand cmd = new CreateTaskCommand(
                StageState.TO_DO,
                title,
                desc,
                Color.PINK,
                due
        );

        Task<StageView> creationTask = new Task<>() {
            @Override
            protected StageView call() throws Exception {
                return requestDispatcher.dispatch(cmd);
            }
        };

        creationTask.setOnSucceeded(e -> {
            StageView updatedStage = creationTask.getValue();
            List<TaskView> tasks = updatedStage.tasks();
            createdTask = tasks.get(tasks.size() - 1);
            dialogStage.close();
        });
        creationTask.setOnFailed(e -> {
            lblError.setText(creationTask.getException().getMessage());
        });

        executor.submit(creationTask);
    }

    public TaskView getCreatedTask() {
        return createdTask;
    }

    public Parent getRoot() {
        return btnAdd.getScene().getRoot();
    }
}
