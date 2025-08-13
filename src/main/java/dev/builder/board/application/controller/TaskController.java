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
        btnSave.setOnAction(e -> onSaveTask());
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

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
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
                    createdTask = updatedStage.tasks().get(updatedStage.tasks().size() - 1);
                    dialogStage.close();
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

    private void onSaveTask() {
        if (task == null) {
            lblError.setText("No se puede editar, tarea no encontrada.");
            return;
        }

        String title = txtEditTitle.getText();
        String description = descriptionEditorEdit.getHtmlText();
        LocalDateTime startDate = dpStartEdit.getValue().atStartOfDay();
        LocalDateTime endDate = dpEndEdit.getValue().atStartOfDay();

        EditTaskCommand editCmd = new EditTaskCommand(
                task.id(),
                title,
                description,
                Color.PINK,
                endDate
        );

        executor.submit(() -> {
            try {
                Task<TaskView> editTask = new Task<>() {
                    @Override
                    protected TaskView call() throws Exception {
                        return requestDispatcher.dispatch(editCmd);
                    }
                };

                editTask.setOnSucceeded(e -> {
                    task = editTask.getValue();
                    dialogStage.close();
                });

                editTask.setOnFailed(e -> {
                    lblError.setText("Error al guardar los cambios: " + editTask.getException().getMessage());
                });

                executor.submit(editTask);
            } catch (Exception e) {
                lblError.setText("Error al guardar los cambios: " + e.getMessage());
            }
        });
    }

    public TaskView getCreatedTask() {
        return createdTask;
    }

    public TaskView getTask() {
        return task;
    }
}
