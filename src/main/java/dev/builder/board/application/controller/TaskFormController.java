package dev.builder.board.application.controller;

import dev.builder.board.application.command.CreateTaskCommand;
import dev.builder.board.application.view.StageView;
import dev.builder.board.application.view.TaskView;
import dev.builder.board.domain.model.Color;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.ViewNavigation;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Bean
public class TaskFormController implements Initializable {


    private final RequestDispatcher requestDispatcher;
    private final ManagerBoardController managerBoardController;
    private final ViewNavigation viewNavigation;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ImageView addCoverImageBtn;
    public Button colorSelector;
    public ContextMenu colorList;
    public TextField txtTitle;
    public MenuButton btnMembers;
    public FlowPane collaboratorsPillContainer;
    public DatePicker dpEnd;
    public Button addAttachmentBtn;
    public HTMLEditor descriptionEditor;
    public Button btnCancel;
    public Button btnSave;
    public FlowPane attachmentPillContainer;
    public Label startedAt;
    public Label dueDateStatus;
    public Label finishedAt;
    public HBox dateEditData;

    private TaskView createdTask;
    private TaskView task;

    @Inject
    public TaskFormController(RequestDispatcher requestDispatcher, ManagerBoardController managerBoardController,
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
            txtTitle.setText(task.title());
            descriptionEditor.setHtmlText(task.description());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
            task.startedAt().ifPresent(s -> startedAt.setText(s.toLocalDate().format(formatter)));
            task.finishedAt().ifPresent(s -> finishedAt.setText(s.toLocalDate().format(formatter)));
            if(task.deadline().isBefore(LocalDateTime.now())) {
                dueDateStatus.setText("Con Retraso");
                dueDateStatus.setStyle("-fx-text-fill: #9b1b1b;");
            } else {
                dueDateStatus.setText("En Tiempo");
                dueDateStatus.setStyle("-fx-text-fill: #1f851f;");
            }

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
