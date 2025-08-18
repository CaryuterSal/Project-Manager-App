package dev.builder.board.application.controller;

import dev.builder.board.application.command.DeleteTaskCommand;
import dev.builder.board.application.query.GetTaskQuery;
import dev.builder.board.application.view.TaskView;
import dev.builder.core.application.ErrorHandler;
import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.ViewNavigation;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.di.annotation.Prototype;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

@Prototype
public class StageTaskCardController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(StageTaskCardController.class);
    public MenuButton menuBtn;
    public Pane clickableField;
    public StackPane backgroundColor;
    private TaskView data;
    public MenuItem deleteBtn;
    private BoardController boardController;
    public Label titleLbl;
    public HBox dueDateInfo;
    public HBox attachmentsInfo;
    public Label attachmentsLbl;
    public HBox assigneesInfo;
    public Label dueDateLbl;
    public Label assigneeLbl;
    public Label assigneesExtras;

    private final MessageLocalizer messageLocalizer;
    private final ViewNavigation  viewNavigation;
    private final RequestDispatcher requestDispatcher;

    @Inject
    public StageTaskCardController(MessageLocalizer messageLocalizer, ViewNavigation viewNavigation, RequestDispatcher requestDispatcher) {
        this.messageLocalizer = messageLocalizer;
        this.viewNavigation = viewNavigation;
        this.requestDispatcher = requestDispatcher;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        clickableField.setOnMouseClicked(ev -> onClick(null));
        deleteBtn.setOnAction(this::deleteTask);
        clickableField.setOnDragDetected(ev -> {
            Dragboard db = clickableField.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(data.id().toString());
            db.setContent(content);
            ev.consume();
        });
    }

    private void update(){
        titleLbl.setText(data.title());
        backgroundColor.setStyle("-fx-background-color: %s;".formatted(data.color().toString()));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        dueDateLbl.setText(formatter.format(data.deadline()));
        if(data.attachments().isEmpty()){
            attachmentsInfo.setVisible(false);
            attachmentsInfo.setManaged(false);
        } else {
            attachmentsInfo.setVisible(true);
            attachmentsInfo.setManaged(true);
            attachmentsLbl.setText(messageLocalizer.getMessage("task.attachment.label", data.attachments().size()));
        }

        if(data.assignees().isEmpty()){
            assigneesInfo.setVisible(false);
            assigneesInfo.setManaged(false);
        } else {
            assigneesInfo.setVisible(true);
            assigneesInfo.setManaged(true);
            assigneeLbl.setText(data.assignees().stream().findFirst().get().email());
            if(data.assignees().size() > 1){
                assigneesExtras.setVisible(true);
                assigneesExtras.setManaged(true);
                assigneesExtras.setText("+%d".formatted(data.assignees().size() - 1));
            } else {
                assigneesExtras.setVisible(false);
                assigneesExtras.setManaged(false);
            }
        }
    }

    private void deleteTask(ActionEvent actionEvent) {

        boolean delete = viewNavigation.showConfirmationDialog(
                "Eliminar Tarea",
                "¿Estás seguro que quieres eliminar esta tarea? La acción no puede deshacerse");
        if(delete) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    requestDispatcher.dispatch(new DeleteTaskCommand(data.id()));
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                boardController.onTaskDeleted(data.id());
            });
            task.setOnFailed(event -> {
                ErrorHandler.showError("Hubo un error al eliminar el la tarea");
            });
            new Thread(task).start();
        }
    }

    private void onClick(ActionEvent actionEvent) {
        viewNavigation.openModal("edit-task-view.fxml", StageStyle.UTILITY,  controller -> {
            TaskFormController editTaskFormController = (TaskFormController) controller;
            editTaskFormController.setTask(data);
        });
        Task<Optional<TaskView>> task = new Task<>() {
            @Override
            protected Optional<TaskView> call() throws Exception {
                return requestDispatcher.dispatch(new GetTaskQuery(data.id()));
            }
        };
        task.setOnSucceeded(event -> {
            if(task.getValue().isEmpty()){
                boardController.onTaskDeleted(data.id());
            } else {
                data = task.getValue().get();
                update();
            }
        });
        task.setOnFailed(ev -> log.warn(task.getException().getMessage(), task.getException()));
        new Thread(task).start();
    }

    public void setData(TaskView taskView) {
        data = taskView;
        update();
    }

    public void setBoardController(BoardController boardController) {
        this.boardController = boardController;
    }
}
