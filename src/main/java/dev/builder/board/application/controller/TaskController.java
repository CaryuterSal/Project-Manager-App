package dev.builder.board.application.controller;

import dev.builder.board.application.command.CreateTaskCommand;
import dev.builder.board.application.view.StageView;
import dev.builder.board.application.view.TaskView;
import dev.builder.board.application.command.EditTaskCommand;
import dev.builder.board.domain.model.Color;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.ViewNavigation;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Bean
public class TaskController implements Initializable {

    @FXML private TextField txtTitle, txtEditTitle;
    @FXML private DatePicker dpEnd, dpEndEdit;
    @FXML private Button btnAdd, btnSave;
    @FXML private Button btnCancel;
    @FXML private Label lblError;
    @FXML private RadioButton rbTitle;
    @FXML private Button btnMembers, btnEditMembers;
    @FXML private DatePicker dpStart, dpStartEdit;
    @FXML private HTMLEditor descriptionEditor, descriptionEditorEdit;

    private final RequestDispatcher requestDispatcher;
    private final ManagerBoardController managerBoardController;
    private final ViewNavigation viewNavigation;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TaskView createdTask;
    private TaskView task;

    @Inject
    public TaskController(RequestDispatcher requestDispatcher, ManagerBoardController managerBoardController,
                          ViewNavigation viewNavigation) {
        this.requestDispatcher = requestDispatcher;
        this.managerBoardController = managerBoardController;
        this.viewNavigation = viewNavigation;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnCancel.setOnAction(e -> close());
        btnAdd.setOnAction(e -> onAddTask());
    }

    public void setTask(TaskView task) {
        this.task = task;
        if (task != null) {
            txtEditTitle.setText(task.title());
            descriptionEditorEdit.setHtmlText(task.description());
            dpStartEdit.setValue(task.startedAt().orElse(null).toLocalDate());
            dpEndEdit.setValue(task.deadline().toLocalDate());
        }
    }

    private void onAddTask() {
        String title = txtTitle.getText();
        LocalDateTime due = dpEnd.getValue().atStartOfDay();
        String desc = descriptionEditor.getHtmlText();

        CreateTaskCommand cmd = new CreateTaskCommand(
                dev.builder.board.domain.model.Stage.StageState.TO_DO,
                title,
                desc,
                Color.PINK,
                due
        );

        executor.submit(() -> {
            try {
                Task<StageView> creationTask = new Task<>() {
                    @Override
                    protected StageView call() throws Exception {
                        return requestDispatcher.dispatch(cmd);
                    }
                };

                creationTask.setOnSucceeded(e -> {
                    StageView updatedStage = creationTask.getValue();
                    createdTask = updatedStage.tasks().getLast();
                    managerBoardController.onTaskCreated(updatedStage);
                    close();
                });

                creationTask.setOnFailed(e -> {
                    lblError.setText(creationTask.getException().getMessage());
                });

                executor.submit(creationTask);
            } catch (Exception e) {
                lblError.setText("Error al agregar la tarea: " + e.getMessage());
            }
        });
    }

    private void close(){
        ((Stage)txtTitle.getScene().getWindow()).close();
    }

    public TaskView getCreatedTask() {
        return createdTask;
    }

    public TaskView getTask() {
        return task;
    }
}
