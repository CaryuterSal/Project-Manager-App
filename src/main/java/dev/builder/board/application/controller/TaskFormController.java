package dev.builder.board.application.controller;

import dev.builder.board.application.command.CreateTaskCommand;
import dev.builder.board.application.command.DeleteTaskCommand;
import dev.builder.board.application.command.EditTaskCommand;
import dev.builder.board.application.command.RemoveCoverImageCommand;
import dev.builder.board.application.view.StageView;
import dev.builder.board.application.view.TaskView;
import dev.builder.board.domain.model.Color;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.ViewNavigation;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
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
    private  BoardController boardController;
    private final MessageLocalizer messageLocalizer;

    public ImageView addCoverImageBtn;
    public MenuButton colorSelector;
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

    private TaskView task;
    private Color selectedColor;

    @Inject
    public TaskFormController(RequestDispatcher requestDispatcher, MessageLocalizer messageLocalizer) {
        this.requestDispatcher = requestDispatcher;
        this.messageLocalizer = messageLocalizer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnCancel.setOnAction(e -> close());
        btnSave.setOnAction(e -> onAddTask());
        loadColors();

        btnSave.disableProperty().bind(
                txtTitle.textProperty().isNotEmpty()
                        .and(dpEnd.valueProperty().isNotNull())
        );
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
        btnSave.setOnAction(e -> onEditTask());
    }

    private void loadColors(){
        for(Color color : Color.values()) {
            MenuItem menuItem = new MenuItem(messageLocalizer.getMessage("color.%s".formatted(color.toString().toLowerCase())));
            colorSelector.getItems().add(menuItem);
        }
    }

    private void onAddCoverImage(){
        Task<Void> deleteCoverTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                requestDispatcher.dispatch(new RemoveCoverImageCommand(task.id()));
                return null;
            }
        };
    }

    private void onAddTask() {
        String title = txtTitle.getText();
        LocalDateTime due = dpEnd.getValue().atStartOfDay();
        String desc = descriptionEditor.getHtmlText();

        CreateTaskCommand cmd = new CreateTaskCommand(
                dev.builder.board.domain.model.Stage.StageState.TO_DO,
                title,
                desc,
                selectedColor,
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
            boardController.onTaskCreated(updatedStage);
            close();
        });
        new Thread(creationTask).start();
    }

    private void onEditTask(){
        String title = txtTitle.getText();
        LocalDateTime due = dpEnd.getValue().atStartOfDay();
        String desc = descriptionEditor.getHtmlText();

        EditTaskCommand cmd = new EditTaskCommand(
                task.id(),
                task.title(),
                desc,
                selectedColor,
                due
        );
        Task<TaskView> task = new Task<>() {
            @Override
            protected TaskView call() throws Exception {
                return requestDispatcher.dispatch(cmd);
            }
        };

        task.setOnSucceeded(e -> {
            close();
        });
        new Thread(task).start();
    }

    private void close(){
        ((Stage)txtTitle.getScene().getWindow()).close();
    }

    void setBoardController(BoardController boardController) {
        this.boardController = boardController;
    }
}
