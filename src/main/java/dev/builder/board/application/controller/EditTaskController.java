package dev.builder.board.application.controller;

import dev.builder.board.application.command.EditTaskCommand;
import dev.builder.board.application.view.TaskView;
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
public class EditTaskController implements Initializable {

    @FXML
    private TextField txtEditTitle;
    @FXML private DatePicker dpEndEdit;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Label lblError;
    @FXML private RadioButton rbTitle;
    @FXML private Button btnEditMembers;
    @FXML private DatePicker dpStartEdit;
    @FXML private HTMLEditor descriptionEditorEdit;

    private final RequestDispatcher requestDispatcher;
    private final ManagerBoardController managerBoardController;
    private final ViewNavigation viewNavigation;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TaskView createdTask;
    private Stage dialogStage;
    private TaskView task;

    @Inject
    public EditTaskController(RequestDispatcher requestDispatcher, ManagerBoardController managerBoardController,
                          ViewNavigation viewNavigation) {
        this.requestDispatcher = requestDispatcher;
        this.managerBoardController = managerBoardController;
        this.viewNavigation = viewNavigation;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnCancel.setOnAction(e -> dialogStage.close());
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
}
